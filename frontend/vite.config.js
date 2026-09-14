import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite config for the Placement Management System frontend.
// The dev server proxies /api calls to the Spring Boot backend on :8080
// so the browser never has to worry about cross-origin ports while developing.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
