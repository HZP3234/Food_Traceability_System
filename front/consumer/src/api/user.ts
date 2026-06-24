import { post, get } from './index'
import type { Result, UserInfo } from '@/types'

export function sendCode(phone: string): Promise<Result<null>> {
  return post('/api/consumer/send-code', { phone })
}

export function login(phone: string, code: string): Promise<Result<UserInfo>> {
  return post('/api/consumer/login', { phone, code })
}

export function getUserInfo(phone: string): Promise<Result<UserInfo>> {
  return get('/api/consumer/info', { phone })
}
