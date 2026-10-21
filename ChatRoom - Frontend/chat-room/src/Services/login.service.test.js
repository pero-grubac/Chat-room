import axios from "axios";
import * as loginService from "./login.service";

vi.mock("axios", async () => (await import("../testUtils")).mockAxios());

const http = axios.create();
const admin = { username: "admin", role: "ROLE_ADMIN", permissions: ["ADD", "UPDATE", "DELETE"] };

describe("login.service", () => {
  beforeEach(() => vi.clearAllMocks());

  test("login step 1 posts the credentials", async () => {
    http.post.mockResolvedValue({ status: 200 });

    await loginService.loginUser("admin", "admin123");

    expect(http.post).toHaveBeenCalledWith("/auth/login", { username: "admin", password: "admin123" });
  });

  test("login step 2 returns the user and keeps nothing in browser storage", async () => {
    http.post.mockResolvedValue({ data: admin });

    const response = await loginService.verifyToken("admin", "admin123", "emailed-code");

    expect(http.post).toHaveBeenCalledWith("/auth/token", {
      username: "admin",
      password: "admin123",
      token: "emailed-code",
    });
    expect(response.data).toEqual(admin);
    expect(sessionStorage.length).toBe(0);
    expect(localStorage.length).toBe(0);
  });

  test("a rejected code is passed on to the caller", async () => {
    http.post.mockRejectedValue({ response: { status: 401 } });

    await expect(loginService.verifyToken("admin", "admin123", "guess")).rejects.toEqual({ response: { status: 401 } });
  });

  test("currentUser asks the server who is signed in", async () => {
    http.get.mockResolvedValue({ data: admin });

    const response = await loginService.currentUser();

    expect(http.get).toHaveBeenCalledWith("/auth/me");
    expect(response.data).toEqual(admin);
  });

  test("logout ends the session on the server", async () => {
    http.post.mockResolvedValue({ status: 204 });

    await loginService.logout();

    expect(http.post).toHaveBeenCalledWith("/auth/logout");
  });

  test("Google step 1 sends only the access token", async () => {
    http.post.mockResolvedValue({ status: 200, data: { email: "ana@gmail.com" } });

    await loginService.oauth("google-token");

    expect(http.post).toHaveBeenCalledWith("/auth/oauth", { accessToken: "google-token" });
  });

  test("OAuth code step", async () => {
    http.post.mockResolvedValue({ data: admin });

    await loginService.verifyTokenOAuth("admin@example.com", "code", "GOOGLE");

    expect(http.post).toHaveBeenCalledWith("/auth/oauth/token", {
      email: "admin@example.com",
      token: "code",
      type: "GOOGLE",
    });
  });

  test("registration posts the new account", async () => {
    http.post.mockResolvedValue({ status: 200 });

    await loginService.register("ana", "Secret123", "ana@example.com");

    expect(http.post).toHaveBeenCalledWith("/users/add", {
      username: "ana",
      password: "Secret123",
      email: "ana@example.com",
    });
  });
});
