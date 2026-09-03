/** 标签接口（docs/02 §7）。后端未就绪，调用失败由调用方降级 */
import { http } from '@/utils/http'
import type { TagItem } from '@/types'

// TODO: 后端未实现（阶段①主线待开发），全部调用走 silent 静默降级；后端上线后移除 silent

export function apiTags() {
  return http.get<TagItem[]>('/v1/tags', { silent: true })
}

export function apiCreateTag(p: { name: string; color?: string }) {
  return http.post<TagItem>('/v1/tags', p, { silent: true })
}

export function apiUpdateTag(id: number, p: { name?: string; color?: string }) {
  return http.put<void>(`/v1/tags/${id}`, p, { silent: true })
}

export function apiDeleteTag(id: number) {
  return http.delete<void>(`/v1/tags/${id}`, { silent: true })
}
