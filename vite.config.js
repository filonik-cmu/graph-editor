import { resolve } from 'path'
import { defineConfig } from 'vite'
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  publicDir: resolve(__dirname, "./public"),
  plugins: [scalaJSPlugin()],
});
