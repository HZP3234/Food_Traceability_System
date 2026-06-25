import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import { fileURLToPath, URL } from 'node:url'
import { existsSync, readFileSync } from 'node:fs'
import { resolve, dirname } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const certDir = resolve(__dirname, '.cert')
const keyPath = resolve(certDir, 'cert.key')
const certPath = resolve(certDir, 'cert.crt')

const httpsConfig = existsSync(keyPath) && existsSync(certPath)
  ? { key: readFileSync(keyPath), cert: readFileSync(certPath) }
  : undefined

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
    https: httpsConfig,
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
      '/api/upload': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/consumer': {
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
