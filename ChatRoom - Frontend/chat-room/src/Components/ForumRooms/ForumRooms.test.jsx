import axios from "axios";
import { render, screen } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import { MemoryRouter } from "react-router-dom";
import ForumRooms from "./ForumRooms";
import reducer, { authState } from "../Redux/slices/userSlice";

vi.mock("axios", async () => (await import("../../testUtils")).mockAxios());

const http = axios.create();
const rooms = [
  { id: 1, name: "Science" },
  { id: 2, name: "Music" },
];

const renderAs = async (user) => {
  http.get.mockImplementation((url) =>
    Promise.resolve({ data: url === "/auth/me" ? user : rooms })
  );
  const store = configureStore({ reducer: { users: reducer } });
  await store.dispatch(authState());
  render(
    <Provider store={store}>
      <MemoryRouter>
        <ForumRooms />
      </MemoryRouter>
    </Provider>
  );
  await screen.findByText("Music");
};

// antd icons are labelled with their icon name: "form" opens the moderation queue
const queueButtons = () => screen.queryAllByRole("img", { name: "form" });

describe("Forum rooms", () => {
  beforeEach(() => vi.clearAllMocks());

  test("a moderator gets the moderation queue only for their rooms", async () => {
    await renderAs({ username: "mod", role: "ROLE_MODERATOR", permissions: ["ADD"], rooms: [2] });

    expect(queueButtons()).toHaveLength(1);
  });

  test("an admin moderates every room", async () => {
    await renderAs({ username: "admin", role: "ROLE_ADMIN", permissions: ["ADD", "UPDATE", "DELETE"], rooms: [] });

    expect(queueButtons()).toHaveLength(2);
  });

  test("a regular user only reads and writes", async () => {
    await renderAs({ username: "ana", role: "ROLE_KORISNIK", permissions: [], rooms: [1] });

    expect(queueButtons()).toHaveLength(0);
    expect(screen.getAllByRole("img", { name: "mail" })).toHaveLength(2);
  });
});
