import axios from "axios";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import Comments from "./Comments";
import reducer from "../Redux/slices/userSlice";

vi.mock("axios", async () => (await import("../../testUtils")).mockAxios());

const http = axios.create();
const published = [
  { idComment: 1, text: "Great concert", createdAt: "2026-10-21T10:00:00Z", user: { username: "ana" } },
];

const renderComments = () =>
  render(
    <Provider store={configureStore({ reducer: { users: reducer } })}>
      <MemoryRouter initialEntries={["/forum_rooms"]}>
        <Routes>
          <Route path="/forum_rooms" element={<Comments idroom={4} onClose={vi.fn()} />} />
          <Route path="/login" element={<h1>Login page</h1>} />
        </Routes>
      </MemoryRouter>
    </Provider>
  );

const send = (text) => {
  fireEvent.change(screen.getByPlaceholderText("Enter your comment"), { target: { value: text } });
  fireEvent.click(screen.getByRole("button", { name: /Send/ }));
};

describe("Forum comments", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    http.get.mockResolvedValue({ data: published });
  });

  test("shows text, author and time of published comments", async () => {
    renderComments();

    expect(await screen.findByText("Great concert")).toBeInTheDocument();
    expect(screen.getByText(/^ana \d+\.\d+\.2026/)).toBeInTheDocument();
    expect(http.get).toHaveBeenCalledWith("/comments/4");
  });

  test("a sent comment is announced as waiting for moderation", async () => {
    http.post.mockResolvedValue({ status: 200 });
    renderComments();
    await screen.findByText("Great concert");

    send("Hello everyone");

    expect(await screen.findByText(/appears once a moderator publishes it/)).toBeInTheDocument();
    expect(http.post).toHaveBeenCalledWith("/comments/create", { text: "Hello everyone", idForumRoom: 4 });
  });

  test("the box stops at the length the server accepts", async () => {
    renderComments();

    expect(screen.getByPlaceholderText("Enter your comment")).toHaveAttribute("maxLength", "100");
  });

  test("a WAF block signs the user out, as the server closed the session", async () => {
    http.post.mockRejectedValue({ response: { status: 400, data: { detail: "Blocked by the web application firewall" } } });
    renderComments();
    await screen.findByText("Great concert");

    send("<script>x</script>");

    expect(await screen.findByText("Login page")).toBeInTheDocument();
    await waitFor(() => expect(http.post).toHaveBeenCalledWith("/auth/logout"));
  });

  test("other errors keep the user signed in", async () => {
    http.post.mockRejectedValue({ response: { status: 404, data: { detail: "Forum room with id: 4 doesn't exists." } } });
    renderComments();
    await screen.findByText("Great concert");

    send("Hello");

    expect(await screen.findByText(/doesn't exists/)).toBeInTheDocument();
    expect(screen.queryByText("Login page")).not.toBeInTheDocument();
  });
});
