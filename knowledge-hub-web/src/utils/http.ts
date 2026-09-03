/**
 * HTTP 层：统一解包 R<T>；1010 时静默刷新令牌并重放，刷新失败跳登录。
 * 约定：拦截器已把响应解包为 data，业务代码直接拿到纯数据。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

export interface Tokens { accessToken: string; refreshToken: string }

const TOKEN_KEY = 'kh.tokens'

export function loadTokens(): Tokens | null {
  const raw = localStorage.getItem(TOKEN_KEY)
  if (!raw) return null
  try { return JSON.parse(raw) as Tokens } catch { return null }
}

export function saveTokens(t: Tokens | null) {
  if (t) localStorage.setItem(TOKEN_KEY, JSON.stringify(t))
  else localStorage.removeItem(TOKEN_KEY)
}

export function clearSession() {
  saveTokens(null)
  localStorage.removeItem('kh.user')
  window.location.href = '/login'
}

/** 单飞刷新：并发 1010 只发一次 refresh，其余排队等结果 */
let refreshing: Promise<string | null> | null = null
function refreshAccessToken(): Promise<string | null> {
  if (!refreshing) {
    refreshing = (async () => {
      const t = loadTokens()
      if (!t?.refreshToken) return null
      try {
        const resp = await axios.post('/api/v1/auth/refresh', { refreshToken: t.refreshToken })
        const body = resp.data
        if (body.code === 0) {
          saveTokens({ accessToken: body.data.accessToken, refreshToken: body.data.refreshToken })
          return body.data.accessToken as string
        }
        return null
      } catch {
        return null
      } finally {
        refreshing = null
      }
    })()
  }
  return refreshing
}

const instance = axios.create({ baseURL: '/api', timeout: 60_000 })

instance.interceptors.request.use((cfg) => {
  const t = loadTokens()
  if (t?.accessToken) cfg.headers.Authorization = `Bearer ${t.accessToken}`
  return cfg
})

instance.interceptors.response.use(async (resp) => {
  // 文件流（预览/下载直连 MinIO 时不经过这里；预留）
  if (resp.config.responseType === 'blob') return resp
  const body = resp.data
  const silent = (resp.config as { silent?: boolean }).silent === true
  if (body.code === 0) return body.data
  if (body.code === 1010) {
    const newToken = await refreshAccessToken()
    if (newToken) {
      // 用新令牌重放原请求
      resp.config.headers.Authorization = `Bearer ${newToken}`
      return instance.request(resp.config)
    }
    clearSession()
    return Promise.reject(new Error('登录已过期'))
  }
  // 业务错误：统一提示（后端文案已面向用户）；silent 供未上线接口优雅降级
  if (!silent) ElMessage.error(body.message || '操作失败')
  return Promise.reject(new Error(body.message || '操作失败'))
}, (err) => {
  const silent = (err?.config as { silent?: boolean } | undefined)?.silent === true
  if (!silent) ElMessage.error(err?.response ? '服务异常，请稍后再试' : '网络不通，请检查虚拟机中间件')
  return Promise.reject(err)
})

type Cfg = import('axios').AxiosRequestConfig & { silent?: boolean }

/** 拦截器已解包，这里把类型改成"直接返回 data" */
interface HttpLike {
  get<T = unknown>(url: string, config?: Cfg): Promise<T>
  post<T = unknown>(url: string, data?: object, config?: Cfg): Promise<T>
  put<T = unknown>(url: string, data?: object, config?: Cfg): Promise<T>
  delete<T = unknown>(url: string, config?: Cfg): Promise<T>
}
export const http = instance as unknown as HttpLike
