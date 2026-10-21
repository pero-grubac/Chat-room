import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// The API lives under /api, the same as behind Nginx, so SPA routes such as /users never collide with it
const backend = process.env.VITE_API_TARGET ?? "http://localhost:8443";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      "/api": {
        target: backend,
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    },
  },
  build: {
    outDir: "build",
    // Libraries change less often than the app, so they get their own cacheable files
    rollupOptions: {
      output: {
        manualChunks: {
          react: ["react", "react-dom", "react-router-dom", "react-redux", "@reduxjs/toolkit"],
          antd: ["antd", "@ant-design/icons"],
        },
      },
    },
    chunkSizeWarningLimit: 1000,
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: "./src/setupTests.js",
    css: false,
    coverage: {
      provider: "v8",
      reporter: ["text-summary", "lcov", "html"],
      include: ["src/**/*.{js,jsx}"],
      exclude: ["src/**/*.test.{js,jsx}", "src/testUtils.js", "src/setupTests.js"],
    },
  },
});
