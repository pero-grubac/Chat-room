import { fireEvent, screen, within } from "@testing-library/react";

// Shared helpers for tests

// A stand-in for the axios instance that baseService creates
export const createAxiosMock = () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
  defaults: { headers: { common: {} } },
  interceptors: { request: { use: vi.fn() } },
});

// vi.mock("axios", mockAxios): every service shares one mocked instance
export const mockAxios = async () => {
  const instance = createAxiosMock();
  return { default: { create: vi.fn(() => instance) } };
};

// Answers "Yes" in the confirm dialog with this title. Dialogs of earlier tests may still be
// fading out (antd renders them outside React), so the dialog is picked by its title.
export const confirmDialog = async (title) => {
  const titles = await screen.findAllByText(title);
  const dialog = titles.at(-1).closest(".ant-modal-confirm");
  fireEvent.click(within(dialog).getByRole("button", { name: "Yes" }));
};
