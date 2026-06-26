import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 5174,
    proxy: {
      '/auth': 'http://localhost:8081',
      '/Raw': 'http://localhost:8081',
      '/Production': 'http://localhost:8081',
      '/ColdChain': 'http://localhost:8081',
      '/Sales': 'http://localhost:8081',
      '/TraceCode': 'http://localhost:8081',
      '/api': 'http://localhost:8081',
    },
  },
})
