/** 目录接口（docs/02 §3）。后端未就绪时调用会失败，由调用方优雅降级 */
import { http } from '@/utils/http'

// TODO: 后端未实现（阶段①主线待开发），全部调用走 silent 静默降级；后端上线后移除 silent
import type { FolderNode } from '@/types'

export function apiFolderTree() {
  return http.get<FolderNode[]>('/v1/folders/tree', { silent: true })
}

export function apiCreateFolder(p: { name: string; parentId: number | null }) {
  return http.post<FolderNode>('/v1/folders', p, { silent: true })
}

export function apiUpdateFolder(id: number, p: { name?: string; parentId?: number | null }) {
  return http.put<void>(`/v1/folders/${id}`, p, { silent: true })
}

export function apiDeleteFolder(id: number) {
  return http.delete<void>(`/v1/folders/${id}`, { silent: true })
}
