import { message } from "antd";
import { handleGetForumRooms, handleLoginError, handleTokenError, isSessionLost } from "./ErrorMessage";

vi.mock("antd", () => ({ message: { error: vi.fn() } }));

const problem = (status, detail) => ({ response: { status, data: { status, detail } } });

describe("ErrorMessage", () => {
  beforeEach(() => vi.clearAllMocks());

  test("wrong credentials show a generic message", () => {
    handleLoginError(problem(401, "Invalid credentials"));
    handleTokenError();

    expect(message.error).toHaveBeenNthCalledWith(1, "Invalid credentials");
    expect(message.error).toHaveBeenNthCalledWith(2, "Invalid token");
  });

  test("a pending account sees why it cannot sign in", () => {
    handleLoginError(problem(403, "Account is not approved yet"));

    expect(message.error).toHaveBeenCalledWith("Account is not approved yet");
  });

  test("a rate limit is explained", () => {
    handleLoginError(problem(429, "Too many attempts, try again in 42 s"));

    expect(message.error).toHaveBeenCalledWith("Too many attempts, try again in 42 s");
  });

  test("a 401 means the session is gone", () => {
    expect(isSessionLost(problem(401, "JWT expired"))).toBe(true);
    expect(isSessionLost(problem(403, "Access denied"))).toBe(false);
    expect(isSessionLost(new Error("Network Error"))).toBe(false);
  });

  test("errors without a response do not crash", () => {
    expect(() => handleGetForumRooms(new Error("Network Error"))).not.toThrow();
    expect(() => handleLoginError(undefined)).not.toThrow();
    expect(message.error).toHaveBeenCalled();
  });
});
