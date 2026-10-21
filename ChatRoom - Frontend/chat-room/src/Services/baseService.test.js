import axios from "axios";
import base from "./baseService";

vi.mock("axios", async () => {
  const { createAxiosMock } = await import("../testUtils");
  return { default: { create: vi.fn(() => createAxiosMock()) } };
});

describe("baseService", () => {
  beforeEach(() => axios.create.mockClear());

  test("talks JSON to the /api prefix with the session cookie", () => {
    const instance = base.service();

    expect(axios.create).toHaveBeenCalledWith({ baseURL: "/api", withCredentials: true });
    expect(instance.defaults.headers.common["Content-Type"]).toBe("application/json");
  });

  test("never handles a token itself", () => {
    const instance = base.service();

    expect(instance.interceptors.request.use).not.toHaveBeenCalled();
  });
});
