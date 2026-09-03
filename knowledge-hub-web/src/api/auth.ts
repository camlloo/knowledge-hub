/** 认证接口（docs/02 §1） */
import { http, saveTokens, loadTokens } from '@/utils/http'
import type { TokenResp, UserInfo } from '@/types'

export interface RegisterPayload { username: string; password: string; nickname?: string }
export interface LoginPayload { username: string; password: string }

export function apiRegister(p: RegisterPayload) {
  return http.post<UserInfo>('/v1/auth/register', p)
}

export async function apiLogin(p: LoginPayload) {
  const resp = await http.post<TokenResp>('/v1/auth/login', p)
  saveTokens({ accessToken: resp.accessToken, refreshToken: resp.refreshToken })
  return resp
}

/** 退出：撤销当前 refreshToken */
export function apiLogout() {
  const t = loadTokens()
  if (!t) return Promise.resolve()
  return http.post<void>('/v1/auth/logout', { refreshToken: t.refreshToken })
}
