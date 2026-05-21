import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    proxy: {
      "/um": {
        target: "http://localhost:8081",
        changeOrigin: true,
        rewrite: (p: string) => p.replace(/^\/um/, ""),
      },
      "/ht": {
        target: "http://localhost:8084",
        changeOrigin: true,
        rewrite: (p: string) => p.replace(/^\/ht/, ""),
      },
      "/dp": {
        target: "http://localhost:8082",
        changeOrigin: true,
        rewrite: (p: string) => p.replace(/^\/dp/, ""),
      },
      "/ds": {
        target: "http://localhost:8083",
        changeOrigin: true,
        rewrite: (p: string) => p.replace(/^\/ds/, ""),
      },
      "/kc": {
        target: "http://localhost:8080",
        changeOrigin: true,
        rewrite: (p: string) => p.replace(/^\/kc/, ""),
      },
    },
  },
});
