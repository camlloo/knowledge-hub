/** 登录用户状态：令牌 + 个人信息（localStorage 持久化，刷新页面不丢） */
import { defineStore } from 'pinia'
import { apiLogin, apiLogout, type LoginPayload } from '@/api/auth'
import { apiMe } from '@/api/user'
import { clearSession, loadTokens } from '@/utils/http'
import type { UserMe } from '@/types'

const USER_KEY = 'kh.user'

function loadCachedUser(): UserMe | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try { return JSON.parse(raw) as UserMe } catch { return null }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    me: loadCachedUser(),
    logged: !!loadTokens(),
  }),
  getters: {
    nickname: (s) => s.me?.user.nickname || s.me?.user.username || '读者',
    quota: (s) => s.me?.quota ?? null,
  },
  actions: {
    async login(p: LoginPayload) {
      this.me = { user: (await apiLogin(p)).userInfo, quota: { quota: 0, used: 0, percentage: 0 } }
      this.logged = true
      this.cacheMe()
      // 登录后立刻拉全量资料与配额
      await this.fetchMe()
    },
    async fetchMe() {
      this.me = await apiMe()
      this.logged = true
      this.cacheMe()
    },
    async logout() {
      try { await apiLogout() } finally { clearSession() }
      this.me = null
      this.logged = false
    },
    /** 改密码成功后全端踢下线，本地直接清干净 */
    forceLogout() {
      clearSession()
      this.me = null
      this.logged = false
    },
    cacheMe() {
      if (this.me) localStorage.setItem(USER_KEY, JSON.stringify(this.me))
    },
  },
})
