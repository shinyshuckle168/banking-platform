import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react({ include: /\.(jsx|js)$/ })],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/accounts': 'http://localhost:8080',
      '/customers': 'http://localhost:8080',
      '/notifications': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/setupTests.js',
    include: ['src/tests/**/*.{jsx,js}'],
    exclude: ['src/tests/testUtils.jsx'],
    coverage: {
      reporter: ['text', 'lcov'],
    },
  },
})
