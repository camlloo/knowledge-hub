/** 文件接口（docs/02 §4-5）：上传（秒传）、列表、收藏、URL、回收站 */
import { http } from '@/utils/http'
import type { FileInfo, PageResult } from '@/types'

// TODO: 后端未实现（阶段①主线待开发），全部调用走 silent 静默降级；后端上线后移除 silent

export interface FileQuery {
  page: number
  size: number
  folderId?: number | null
  keyword?: string
  star?: boolean
  sortBy?: 'name' | 'size' | 'createdAt'
  order?: 'asc' | 'desc'
}

export function apiPageFiles(q: FileQuery) {
  return http.get<PageResult<FileInfo>>('/v1/files', { params: q, silent: true })
}

export function apiRecentFiles(limit = 20) {
  return http.get<FileInfo[]>('/v1/files/recent', { params: { limit }, silent: true })
}

export function apiRenameOrMove(id: number, p: { name?: string; folderId?: number | null; categoryId?: number | null }) {
  return http.put<void>(`/v1/files/${id}`, p, { silent: true })
}

export function apiDeleteFile(id: number) {
  return http.delete<void>(`/v1/files/${id}`, { silent: true })
}

export function apiStar(id: number, starred: boolean) {
  return starred
    ? http.put<void>(`/v1/files/${id}/star`, undefined, { silent: true })
    : http.delete<void>(`/v1/files/${id}/star`, { silent: true })
}

/** 预签名 URL（attachment / inline） */
export function apiDownloadUrl(id: number) {
  return http.get<string>(`/v1/files/${id}/download-url`, { silent: true })
}

export function apiPreviewUrl(id: number) {
  return http.get<string>(`/v1/files/${id}/preview-url`, { silent: true })
}

/** 秒传预检：instant=true 直接复用存储对象，无需上传 */
export function apiHashCheck(p: { sha256: string; fileName: string; size: number; folderId?: number | null }) {
  return http.post<{ instant: boolean; fileId?: number }>('/v1/files/upload/hash-check', p, { silent: true })
}

/** 直传（multipart） */
export function apiUpload(p: {
  file: File
  folderId?: number | null
  sha256?: string
  onProgress?: (percent: number) => void
}) {
  const form = new FormData()
  form.append('file', p.file)
  if (p.folderId != null) form.append('folderId', String(p.folderId))
  if (p.sha256) form.append('sha256', p.sha256)
  return http.post<FileInfo>('/v1/files/upload', form, {
    silent: true,
    onUploadProgress: (e: { loaded: number; total?: number }) => {
      if (e.total) p.onProgress?.(Math.round((e.loaded / e.total) * 100))
    },
  })
}

/* ---------- 回收站（docs/02 §6） ---------- */

export interface RecycleItem extends FileInfo { deletedAt: string }

export function apiRecyclePage(page: number, size: number) {
  return http.get<PageResult<RecycleItem>>('/v1/recycle', { params: { page, size }, silent: true })
}

export function apiRecycleRestore(fileId: number) {
  return http.put<void>(`/v1/recycle/${fileId}/restore`, undefined, { silent: true })
}

export function apiRecyclePurge(fileId: number) {
  return http.delete<void>(`/v1/recycle/${fileId}`, { silent: true })
}

export function apiRecycleClear() {
  return http.delete<void>('/v1/recycle', { silent: true })
}
