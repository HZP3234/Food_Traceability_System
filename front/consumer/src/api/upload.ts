import { upload } from './index'

export function uploadImage(formData: FormData) {
  return upload<string>('/api/upload/image', formData)
}
