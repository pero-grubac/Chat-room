import axios from "axios";
import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import Register, { isStrongPassword } from "./Register";

vi.mock("axios", async () => (await import("../../testUtils")).mockAxios());

const http = axios.create();

const renderRegister = () =>
  render(
    <MemoryRouter initialEntries={["/register"]}>
      <Routes>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<h1>Login page</h1>} />
      </Routes>
    </MemoryRouter>
  );

const fill = (password) => {
  fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "newbie" } });
  fireEvent.change(screen.getByPlaceholderText("Email"), { target: { value: "newbie@example.com" } });
  fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: password } });
};

describe("Registration", () => {
  beforeEach(() => vi.clearAllMocks());

  test.each([
    ["Strong#123", true],
    ["Sh#1abc", false],
    ["nouppercase#1", false],
    ["NoDigits#here", false],
    ["NoSpecial123", false],
  ])("password %s is strong: %s", (password, strong) => {
    expect(isStrongPassword(password)).toBe(strong);
  });

  test("a valid form is sent once, without reloading the page, and returns to the login", async () => {
    http.post.mockResolvedValue({ status: 200 });
    renderRegister();
    fill("Strong#123");

    const submitted = fireEvent.submit(screen.getByRole("button", { name: "Register" }).closest("form"));

    // fireEvent returns false when the handler called preventDefault()
    expect(submitted).toBe(false);
    expect(await screen.findByText("Login page")).toBeInTheDocument();
    expect(http.post).toHaveBeenCalledTimes(1);
    expect(http.post).toHaveBeenCalledWith("/users/add", {
      username: "newbie",
      password: "Strong#123",
      email: "newbie@example.com",
    });
  });

  test("a weak password never reaches the server", async () => {
    renderRegister();
    fill("weak");

    fireEvent.submit(screen.getByRole("button", { name: "Register" }).closest("form"));

    expect(await screen.findByText(/Password must have 8 to 72 characters/)).toBeInTheDocument();
    expect(http.post).not.toHaveBeenCalled();
  });

  test("a taken username is explained", async () => {
    http.post.mockRejectedValue({ response: { status: 409, data: { detail: "User exists" } } });
    renderRegister();
    fill("Strong#123");

    fireEvent.submit(screen.getByRole("button", { name: "Register" }).closest("form"));

    expect(await screen.findByText("User exists")).toBeInTheDocument();
  });
});
