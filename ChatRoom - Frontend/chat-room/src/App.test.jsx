import axios from "axios";
import { render, screen } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import App from "./App";
import reducer from "./Components/Redux/slices/userSlice";

vi.mock("axios", async () => (await import("./testUtils")).mockAxios());

// Pages are replaced by markers: these tests cover routing, not page content
vi.mock("./Components/LoginSingUp/Login", () => ({ default: () => <h1>Login page</h1> }));
vi.mock("./Components/LoginSingUp/Register", () => ({ default: () => <h1>Register page</h1> }));
vi.mock("./Components/ForumRooms/ForumRooms", () => ({ default: () => <h1>Forum rooms page</h1> }));
vi.mock("./Components/Users/Users", () => ({ default: () => <h1>Users page</h1> }));

const http = axios.create();

const renderAt = (path) => {
  window.history.pushState({}, "", path);
  const store = configureStore({ reducer: { users: reducer } });
  return render(
    <Provider store={store}>
      <App />
    </Provider>
  );
};

const signedInAs = (role) =>
  http.get.mockResolvedValue({ data: { username: "someone", role, permissions: [] } });

const signedOut = () => http.get.mockRejectedValue({ response: { status: 401 } });

describe("App routing", () => {
  beforeEach(() => vi.clearAllMocks());

  test("the root redirects to the login page", async () => {
    signedOut();

    renderAt("/");

    expect(await screen.findByText("Login page")).toBeInTheDocument();
  });

  test("registration is reachable without signing in", async () => {
    signedOut();

    renderAt("/register");

    expect(await screen.findByText("Register page")).toBeInTheDocument();
  });

  test("a signed-in user who reloads the page stays on the forum rooms", async () => {
    signedInAs("ROLE_KORISNIK");

    renderAt("/forum_rooms");

    expect(await screen.findByText("Forum rooms page")).toBeInTheDocument();
    expect(http.get).toHaveBeenCalledWith("/auth/me");
  });

  test("an admin reaches user management", async () => {
    signedInAs("ROLE_ADMIN");

    renderAt("/users");

    expect(await screen.findByText("Users page")).toBeInTheDocument();
  });

  // This used to loop between "*" and /error404, which only existed for signed-in users
  test("a signed-out visitor of a protected page lands on the login", async () => {
    signedOut();

    renderAt("/forum_rooms");

    expect(await screen.findByText("Login page")).toBeInTheDocument();
  });

  test("an unknown page sends a signed-out visitor to the login", async () => {
    signedOut();

    renderAt("/no-such-page");

    expect(await screen.findByText("Login page")).toBeInTheDocument();
  });

  test("a signed-in user opening the login goes to the forum", async () => {
    signedInAs("ROLE_KORISNIK");

    renderAt("/login");

    expect(await screen.findByText("Forum rooms page")).toBeInTheDocument();
  });

  test("a regular user is kept out of user management", async () => {
    signedInAs("ROLE_KORISNIK");

    renderAt("/users");

    expect(await screen.findByText(/does not exist/i)).toBeInTheDocument();
    expect(screen.queryByText("Users page")).not.toBeInTheDocument();
  });
});
