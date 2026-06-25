import { post, get, put } from './index'
import type { Result, UserInfo, UserInfoUpdateDTO } from '@/types'

export function sendCode(phone: string): Promise<Result<null>> {
  return post('/api/consumer/send-code', { phone })
}

export function login(phone: string, code: string): Promise<Result<UserInfo>> {
  return post('/api/consumer/login', { phone, code })
}

export function getUserInfo(phone: string): Promise<Result<UserInfo>> {
  return get('/api/consumer/info', { phone })
}

export function updateUserInfo(data: UserInfoUpdateDTO): Promise<Result<UserInfo>> {
  return put('/api/consumer/update', data)
}
