import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/Raw': 'http://localhost:8082',
      '/Production': 'http://localhost:8082',
      '/ColdChain': 'http://localhost:8082',
      '/Sales': 'http://localhost:8082',
      '/TraceCode': 'http://localhost:8082',
    },
  },
})
