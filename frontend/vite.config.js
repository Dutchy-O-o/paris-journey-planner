import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// During development the frontend talks to the Spring Boot backend through a proxy,
// so the browser only ever calls same-origin "/api/..." (no CORS needed).
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8088',
        changeOrigin: true,
      },
    },
  },
})
