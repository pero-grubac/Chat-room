// jest-dom adds DOM matchers such as toBeInTheDocument()
import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { Modal } from "antd";

// antd reads window.matchMedia, which jsdom does not implement
Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: (query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  }),
});

// jsdom does not implement pseudo-elements; antd asks for them while measuring scrollbars
const { getComputedStyle } = window;
window.getComputedStyle = (element) => getComputedStyle(element);

afterEach(() => {
  cleanup();
  // Modal.confirm renders outside the React tree, so cleanup() does not remove it
  Modal.destroyAll();
  sessionStorage.clear();
});
