/** 用户接口（docs/02 §2，设计见 docs/03） */
import { http } from '@/utils/http'
import type { UserMe, UpdateMePayload } from '@/types/api-user'

export function apiMe() {
  return http.get<UserMe>('/v1/users/me')
}

export function apiUpdateMe(p: UpdateMePayload) {
  return http.put<UserMe>('/v1/users/me', p)
}

export function apiUpdatePassword(p: { oldPassword: string; newPassword: string }) {
  return http.put<void>('/v1/users/me/password', p)
}
