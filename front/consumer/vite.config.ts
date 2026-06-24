import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    Components({
      resolvers: [VantResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api/traceability': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/complaint': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/TraceCode': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})
