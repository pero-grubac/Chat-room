import axios from "axios";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import { MemoryRouter } from "react-router-dom";
import Users from "./Users";
import { confirmDialog } from "../../testUtils";
import reducer, { authState } from "../Redux/slices/userSlice";

vi.mock("axios", async () => (await import("../../testUtils")).mockAxios());

const http = axios.create();

const rooms = [
  { id: 1, name: "Science" },
  { id: 2, name: "Music" },
];
const mod = {
  idUser: 7,
  username: "mod",
  email: "mod@example.com",
  role: "ROLE_MODERATOR",
  permissions: [{ permission: "ADD" }, { permission: "DELETE" }],
  isApproved: 1,
  roomIds: [2],
};
const blocked = { ...mod, idUser: 8, username: "eve", permissions: [], role: "ROLE_KORISNIK", isApproved: 0, roomIds: [] };
const rejectedRegistration = { ...blocked, idUser: 9, username: "spam", role: null };
const events = [
  { id: 1, type: "WAF_BLOCKED", username: "eve", ipAddress: "10.0.0.5", message: "Field text, rule HTML_TAG", createdAt: "2026-10-21T10:00:00Z" },
];


const renderUsers = () =>
  render(
    <Provider store={configureStore({ reducer: { users: reducer } })}>
      <MemoryRouter>
        <Users />
      </MemoryRouter>
    </Provider>
  );

describe("User management", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    http.get.mockImplementation((url) => {
      if (url === "/users/approved/1") return Promise.resolve({ data: [mod] });
      if (url === "/users/approved/0") return Promise.resolve({ data: [blocked, rejectedRegistration] });
      if (url === "/forumrooms") return Promise.resolve({ data: rooms });
      if (url === "/security-events") return Promise.resolve({ data: events });
      return Promise.reject(new Error(url));
    });
    http.put.mockResolvedValue({ status: 200 });
  });

  test("lists active and blocked accounts with permissions and rooms", async () => {
    renderUsers();

    const modRow = (await screen.findByText("mod")).closest("tr");
    expect(within(modRow).getByText("ADD, DELETE")).toBeInTheDocument();
    expect(within(modRow).getByText("Music")).toBeInTheDocument();
    expect(within(modRow).getByText("active")).toBeInTheDocument();
    expect(within(screen.getByText("eve").closest("tr")).getByText("blocked")).toBeInTheDocument();
    expect(screen.queryByText("spam")).not.toBeInTheDocument();
  });

  test("blocking asks first, then calls the API", async () => {
    renderUsers();
    const modRow = (await screen.findByText("mod")).closest("tr");

    fireEvent.click(within(modRow).getByLabelText("Block"));
    await confirmDialog(/^Block mod/);

    await waitFor(() => expect(http.put).toHaveBeenCalledWith("/users/access", { id: 7, allowed: false }));
  });

  test("a blocked account can be restored", async () => {
    renderUsers();
    const eveRow = (await screen.findByText("eve")).closest("tr");

    fireEvent.click(within(eveRow).getByLabelText("Unblock"));
    await confirmDialog(/Restore access for eve/);

    await waitFor(() => expect(http.put).toHaveBeenCalledWith("/users/access", { id: 8, allowed: true }));
  });

  test("editing a moderator sends the exact permissions and rooms", async () => {
    renderUsers();
    const modRow = (await screen.findByText("mod")).closest("tr");
    fireEvent.click(within(modRow).getByLabelText("Edit"));

    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByLabelText("Delete"));
    fireEvent.click(within(dialog).getByLabelText("Science"));
    fireEvent.click(within(dialog).getByRole("button", { name: "Save" }));

    await waitFor(() =>
      expect(http.put).toHaveBeenCalledWith("/users/changeRole", {
        id: 7,
        role: "ROLE_MODERATOR",
        permissions: ["ADD"],
        roomIds: [1, 2],
      })
    );
  });

  test("an admin gets every permission and no room list", async () => {
    renderUsers();
    const modRow = (await screen.findByText("mod")).closest("tr");
    fireEvent.click(within(modRow).getByLabelText("Edit"));

    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByText("ADMIN"));
    fireEvent.click(within(dialog).getByRole("button", { name: "Save" }));

    await waitFor(() =>
      expect(http.put).toHaveBeenCalledWith("/users/changeRole", {
        id: 7,
        role: "ROLE_ADMIN",
        permissions: ["ADD", "UPDATE", "DELETE"],
        roomIds: [],
      })
    );
  });

  test("a moderator cannot be saved without a room", async () => {
    renderUsers();
    const modRow = (await screen.findByText("mod")).closest("tr");
    fireEvent.click(within(modRow).getByLabelText("Edit"));

    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByLabelText("Music"));

    expect(within(dialog).getByText("A moderator needs at least one room.")).toBeInTheDocument();
    expect(within(dialog).getByRole("button", { name: "Save" })).toBeDisabled();
  });

  test("a failed save keeps the admin signed in", async () => {
    http.put.mockRejectedValue({ response: { status: 409, data: { detail: "ROLE_ADMIN must have all permissions" } } });
    renderUsers();
    const modRow = (await screen.findByText("mod")).closest("tr");
    fireEvent.click(within(modRow).getByLabelText("Edit"));
    fireEvent.click(within(await screen.findByRole("dialog")).getByRole("button", { name: "Save" }));

    expect(await screen.findByText("ROLE_ADMIN must have all permissions")).toBeInTheDocument();
    expect(http.post).not.toHaveBeenCalledWith("/auth/logout");
  });

  test("an admin cannot block their own account", async () => {
    const store = configureStore({ reducer: { users: reducer } });
    const me = { ...mod, idUser: 1, username: "admin", role: "ROLE_ADMIN", roomIds: [] };
    http.get.mockImplementation((url) => {
      if (url === "/auth/me") return Promise.resolve({ data: { username: "admin", role: "ROLE_ADMIN", permissions: [] } });
      if (url === "/users/approved/1") return Promise.resolve({ data: [me, mod] });
      if (url === "/forumrooms") return Promise.resolve({ data: rooms });
      return Promise.resolve({ data: [] });
    });
    await store.dispatch(authState());
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Users />
        </MemoryRouter>
      </Provider>
    );

    const adminRow = (await screen.findByText("admin")).closest("tr");
    expect(within(adminRow).queryByLabelText("Block")).not.toBeInTheDocument();
    expect(within(screen.getByText("mod").closest("tr")).getByLabelText("Block")).toBeInTheDocument();
  });

  test("the security log shows SIEM events", async () => {
    renderUsers();
    await screen.findByText("mod");

    fireEvent.click(screen.getByTitle("Security log"));

    expect(await screen.findByText("WAF_BLOCKED")).toBeInTheDocument();
    expect(screen.getByText("10.0.0.5")).toBeInTheDocument();
    expect(http.get).toHaveBeenCalledWith("/security-events", { params: { limit: 200 } });
  });
});
