import axios from "axios";
import { configureStore } from "@reduxjs/toolkit";
import reducer, { authState, logout } from "./userSlice";

vi.mock("axios", async () => (await import("../../../testUtils")).mockAxios());

const http = axios.create();
const createStore = () => configureStore({ reducer: { users: reducer } });
const moderator = { username: "mod", role: "ROLE_MODERATOR", permissions: ["ADD", "DELETE"], rooms: [1, 3] };

describe("userSlice", () => {
  beforeEach(() => vi.clearAllMocks());

  test("starts signed out and not yet initialized", () => {
    const state = reducer(undefined, { type: "@@INIT" });

    expect(state.authenticated).toBe(false);
    expect(state.initialized).toBe(false);
    expect(state.role).toBeNull();
    expect(state.permissions).toEqual([]);
    expect(state.rooms).toEqual([]);
  });

  test("restores the session from the server", async () => {
    http.get.mockResolvedValue({ data: moderator });
    const store = createStore();

    await store.dispatch(authState());

    expect(http.get).toHaveBeenCalledWith("/auth/me");
    const state = store.getState().users;
    expect(state.authenticated).toBe(true);
    expect(state.initialized).toBe(true);
    expect(state.user).toBe("mod");
    expect(state.role).toBe("ROLE_MODERATOR");
    expect(state.permissions).toEqual(["ADD", "DELETE"]);
    expect(state.rooms).toEqual([1, 3]);
  });

  test("a 401 from the server means signed out", async () => {
    http.get.mockRejectedValue({ response: { status: 401 } });
    const store = createStore();

    await store.dispatch(authState());

    const state = store.getState().users;
    expect(state.authenticated).toBe(false);
    expect(state.initialized).toBe(true);
  });

  test("an older session is replaced, not merged", async () => {
    const store = createStore();
    http.get.mockResolvedValue({ data: moderator });
    await store.dispatch(authState());

    http.get.mockResolvedValue({ data: { username: "ana", role: "ROLE_KORISNIK", permissions: [] } });
    await store.dispatch(authState());

    expect(store.getState().users.rooms).toEqual([]);
    expect(store.getState().users.permissions).toEqual([]);
  });

  test("logout tells the server and resets the state", async () => {
    http.get.mockResolvedValue({ data: moderator });
    http.post.mockResolvedValue({ status: 204 });
    const store = createStore();
    await store.dispatch(authState());

    await store.dispatch(logout());

    expect(http.post).toHaveBeenCalledWith("/auth/logout");
    const state = store.getState().users;
    expect(state.authenticated).toBe(false);
    expect(state.role).toBeNull();
    expect(state.permissions).toEqual([]);
  });

  test("logout still signs out locally when the server is unreachable", async () => {
    http.get.mockResolvedValue({ data: moderator });
    http.post.mockRejectedValue(new Error("Network Error"));
    const store = createStore();
    await store.dispatch(authState());

    await store.dispatch(logout());

    expect(store.getState().users.authenticated).toBe(false);
  });
});
