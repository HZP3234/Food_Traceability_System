import axios from 'axios'
import type { Result } from '@/types'

const http = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络请求失败'
    return Promise.reject(new Error(msg))
  }
)

export function get<T>(url: string, params?: Record<string, any>): Promise<Result<T>> {
  return http.get(url, { params }) as unknown as Promise<Result<T>>
}

export function post<T>(url: string, data?: Record<string, any>): Promise<Result<T>> {
  return http.post(url, data) as unknown as Promise<Result<T>>
}

export function put<T>(url: string, data?: Record<string, any>): Promise<Result<T>> {
  return http.put(url, data) as unknown as Promise<Result<T>>
}

export function upload<T>(url: string, formData: FormData, onProgress?: (pct: number) => void): Promise<Result<T>> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('POST', url)

    xhr.upload.onprogress = (e) => {
      if (onProgress && e.lengthComputable) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    }

    xhr.onload = () => {
      try {
        const data = JSON.parse(xhr.responseText)
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve(data as Result<T>)
        } else {
          reject(new Error(data.message || `上传失败 (${xhr.status})`))
        }
      } catch {
        reject(new Error(`上传失败 (${xhr.status})`))
      }
    }

    xhr.onerror = () => reject(new Error('网络请求失败'))
    xhr.ontimeout = () => reject(new Error('上传超时'))
    xhr.timeout = 30000
    xhr.send(formData)
  })
}

export default http
