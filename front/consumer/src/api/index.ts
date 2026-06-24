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

export function upload<T>(url: string, formData: FormData): Promise<Result<T>> {
  return http.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as unknown as Promise<Result<T>>
}

export default http
