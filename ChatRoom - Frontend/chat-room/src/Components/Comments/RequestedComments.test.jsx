import axios from "axios";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import { MemoryRouter } from "react-router-dom";
import RequestedComments from "./RequestedComments";
import { confirmDialog } from "../../testUtils";
import reducer from "../Redux/slices/userSlice";

vi.mock("axios", async () => (await import("../../testUtils")).mockAxios());

const http = axios.create();

const pending = [
  { idComment: 11, text: "Hello all", createdAt: "2026-10-21T10:00:00Z", user: { username: "ana" } },
];

const renderQueue = (permissions) =>
  render(
    <Provider store={configureStore({ reducer: { users: reducer } })}>
      <MemoryRouter>
        <RequestedComments permissions={permissions} idRoom={1} onClose={vi.fn()} />
      </MemoryRouter>
    </Provider>
  );


describe("Moderation queue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    http.get.mockResolvedValue({ data: pending });
    http.put.mockResolvedValue({ status: 200 });
  });

  test("shows the author, date and text of pending comments", async () => {
    renderQueue(["ADD"]);

    expect(await screen.findByText("Hello all")).toBeInTheDocument();
    expect(screen.getByText("ana")).toBeInTheDocument();
    expect(http.get).toHaveBeenCalledWith("/comments/requests/1");
  });

  test("publishing sends isApproved 1", async () => {
    renderQueue(["ADD"]);
    fireEvent.click(await screen.findByLabelText("Publish"));
    await confirmDialog(/Publish this comment/);

    await waitFor(() => expect(http.put).toHaveBeenCalledWith("/comments/approve", { id: 11, isApproved: 1 }));
  });

  test("forbidding sends isApproved 0", async () => {
    renderQueue(["ADD"]);
    fireEvent.click(await screen.findByLabelText("Forbid"));
    await confirmDialog(/Forbid this comment/);

    await waitFor(() => expect(http.put).toHaveBeenCalledWith("/comments/approve", { id: 11, isApproved: 0 }));
  });

  test("actions follow the moderator's permissions", async () => {
    renderQueue(["UPDATE"]);
    await screen.findByText("Hello all");

    expect(screen.queryByLabelText("Publish")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Forbid")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Delete")).not.toBeInTheDocument();
    expect(screen.getByLabelText("Adjust")).toBeInTheDocument();
  });
});
