import { upload } from './index'

export function uploadImage(formData: FormData, onProgress?: (pct: number) => void) {
  return upload<string>('/api/upload/image', formData, onProgress)
}
