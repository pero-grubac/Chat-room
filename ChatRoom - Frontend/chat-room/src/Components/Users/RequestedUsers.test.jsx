import axios from "axios";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import { MemoryRouter } from "react-router-dom";
import RequestedUsers from "./RequestedUsers";
import reducer from "../Redux/slices/userSlice";
import { confirmDialog } from "../../testUtils";

vi.mock("axios", async () => (await import("../../testUtils")).mockAxios());

const http = axios.create();
const pending = [{ idUser: 9, username: "newbie", email: "newbie@example.com" }];
const rooms = [
  { id: 1, name: "Science" },
  { id: 2, name: "Music" },
];

const renderRequests = () =>
  render(
    <Provider store={configureStore({ reducer: { users: reducer } })}>
      <MemoryRouter>
        <RequestedUsers onClose={vi.fn()} />
      </MemoryRouter>
    </Provider>
  );

const openApproval = async () => {
  renderRequests();
  fireEvent.click(await screen.findByLabelText("Approve"));
  return screen.findByRole("dialog");
};

describe("Registration requests", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    http.get.mockImplementation((url) => Promise.resolve({ data: url === "/forumrooms" ? rooms : pending }));
    http.put.mockResolvedValue({ status: 200 });
  });

  // This used to send an empty permission list, so approving an administrator always failed
  test("approving as administrator sends every permission", async () => {
    const dialog = await openApproval();
    fireEvent.click(within(dialog).getByText("ADMIN"));
    fireEvent.click(within(dialog).getByRole("button", { name: "Approve" }));

    await waitFor(() =>
      expect(http.put).toHaveBeenCalledWith("/users/approve", {
        id: 9,
        isApproved: true,
        role: "ROLE_ADMIN",
        permissions: ["ADD", "UPDATE", "DELETE"],
        roomIds: [],
      })
    );
  });

  test("approving as moderator sends the chosen permissions and rooms", async () => {
    const dialog = await openApproval();
    fireEvent.click(within(dialog).getByText("MODERATOR"));
    fireEvent.click(within(dialog).getByLabelText("Publish / forbid"));
    fireEvent.click(await within(dialog).findByLabelText("Music"));
    fireEvent.click(within(dialog).getByRole("button", { name: "Approve" }));

    await waitFor(() =>
      expect(http.put).toHaveBeenCalledWith("/users/approve", {
        id: 9,
        isApproved: true,
        role: "ROLE_MODERATOR",
        permissions: ["ADD"],
        roomIds: [2],
      })
    );
  });

  test("nothing can be approved without a role", async () => {
    const dialog = await openApproval();

    expect(within(dialog).getByRole("button", { name: "Approve" })).toBeDisabled();
  });

  test("rejecting asks first and tells the server", async () => {
    renderRequests();
    fireEvent.click(await screen.findByLabelText("Reject"));
    await confirmDialog(/Reject the registration of newbie/);

    await waitFor(() =>
      expect(http.put).toHaveBeenCalledWith("/users/approve", {
        id: 9,
        isApproved: false,
        role: null,
        permissions: [],
        roomIds: [],
      })
    );
  });
});
