import axios from "axios";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import Login from "./Login";
import reducer from "../Redux/slices/userSlice";

vi.mock("axios", async () => (await import("../../testUtils")).mockAxios());
// The Google popup is replaced: clicking the button hands an access token straight to onSuccess
vi.mock("@react-oauth/google", () => ({
  useGoogleLogin: (options) => () => options.onSuccess({ access_token: "google-token" }),
}));

const http = axios.create();

const renderLogin = () => {
  const store = configureStore({ reducer: { users: reducer } });
  const view = render(
    <Provider store={store}>
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/forum_rooms" element={<h1>Forum rooms page</h1>} />
        </Routes>
      </MemoryRouter>
    </Provider>
  );
  return { ...view, store };
};

const type = (container, selector, value) =>
  fireEvent.change(container.querySelector(selector), { target: { value } });

const submitPassword = (container) => {
  type(container, 'input[type="text"]', "admin");
  type(container, 'input[type="password"]', "admin123");
  fireEvent.click(screen.getByRole("button", { name: "Login" }));
};

describe("Login page", () => {
  beforeEach(() => vi.clearAllMocks());

  test("password, then emailed code, then the forum rooms", async () => {
    http.post.mockImplementation((url) =>
      Promise.resolve(
        url === "/auth/token"
          ? { status: 200, data: { username: "admin", role: "ROLE_ADMIN", permissions: ["ADD"] } }
          : { status: 200 }
      )
    );
    http.get.mockResolvedValue({ data: { username: "admin", role: "ROLE_ADMIN", permissions: ["ADD"] } });
    const { container, store } = renderLogin();

    submitPassword(container);

    expect(await screen.findByRole("heading", { name: "Token" })).toBeInTheDocument();
    expect(http.post).toHaveBeenCalledWith("/auth/login", { username: "admin", password: "admin123" });

    type(container, 'input[type="text"]', "emailed-code");
    fireEvent.click(screen.getByRole("button", { name: "Login with Token" }));

    expect(await screen.findByText("Forum rooms page")).toBeInTheDocument();
    expect(http.post).toHaveBeenCalledWith("/auth/token", {
      username: "admin",
      password: "admin123",
      token: "emailed-code",
    });
    expect(store.getState().users.authenticated).toBe(true);
    expect(store.getState().users.role).toBe("ROLE_ADMIN");
  });

  test("wrong password keeps the user on the password step", async () => {
    http.post.mockRejectedValue({ response: { status: 401, data: { detail: "Invalid credentials" } } });
    const { container } = renderLogin();

    submitPassword(container);

    expect(await screen.findByText("Invalid credentials")).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Token" })).not.toBeInTheDocument();
  });

  test("a pending account is told it is not approved", async () => {
    http.post.mockRejectedValue({ response: { status: 403, data: { detail: "Account is not approved yet" } } });
    const { container } = renderLogin();

    submitPassword(container);

    expect(await screen.findByText("Account is not approved yet")).toBeInTheDocument();
  });

  test("a wrong code sends the user back to the password step", async () => {
    http.post.mockImplementation((url) =>
      url === "/auth/token" ? Promise.reject({ response: { status: 401 } }) : Promise.resolve({ status: 200 })
    );
    const { container } = renderLogin();
    submitPassword(container);
    await screen.findByRole("heading", { name: "Token" });

    type(container, 'input[type="text"]', "guess");
    fireEvent.click(screen.getByRole("button", { name: "Login with Token" }));

    await waitFor(() => expect(screen.getByRole("heading", { name: "Login" })).toBeInTheDocument());
    expect(screen.queryByText("Forum rooms page")).not.toBeInTheDocument();
  });

  describe("with Google", () => {
    test("an approved account continues with the emailed code", async () => {
      http.post.mockImplementation((url) =>
        Promise.resolve(
          url === "/auth/oauth"
            ? { status: 200, data: { email: "ana@gmail.com" } }
            : { status: 200, data: { username: "ana", role: "ROLE_KORISNIK", permissions: [] } }
        )
      );
      http.get.mockResolvedValue({ data: { username: "ana", role: "ROLE_KORISNIK", permissions: [] } });
      const { container } = renderLogin();

      fireEvent.click(screen.getByRole("button", { name: /Sign in with Google/ }));

      expect(await screen.findByRole("heading", { name: "Token" })).toBeInTheDocument();
      expect(http.post).toHaveBeenCalledWith("/auth/oauth", { accessToken: "google-token" });

      type(container, 'input[type="text"]', "emailed-code");
      fireEvent.click(screen.getByRole("button", { name: "Login with Token" }));

      expect(await screen.findByText("Forum rooms page")).toBeInTheDocument();
      expect(http.post).toHaveBeenCalledWith("/auth/oauth/token", {
        email: "ana@gmail.com",
        token: "emailed-code",
        type: "GOOGLE",
      });
    });

    test("a new account is told to wait for approval", async () => {
      http.post.mockResolvedValue({ status: 201, data: { email: "ana@gmail.com" } });
      renderLogin();

      fireEvent.click(screen.getByRole("button", { name: /Sign in with Google/ }));

      expect(await screen.findByText(/Wait for an administrator/)).toBeInTheDocument();
      expect(screen.queryByRole("heading", { name: "Token" })).not.toBeInTheDocument();
    });

    test("a pending or blocked account is refused", async () => {
      http.post.mockRejectedValue({ response: { status: 403, data: { email: "ana@gmail.com" } } });
      renderLogin();

      fireEvent.click(screen.getByRole("button", { name: /Sign in with Google/ }));

      expect(await screen.findByText("Your account is not approved yet.")).toBeInTheDocument();
    });
  });
});
