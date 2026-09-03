// 后端接口类型（与 docs/02 对齐）

/** 统一返回体 */
export interface R<T> { code: number; message: string; data: T }

/** 分页返回体 */
export interface PageResult<T> { list: T[]; total: number; page: number; size: number }

/** 用户信息（不含密码哈希） */
export interface UserInfo {
  id: number
  username: string
  nickname: string | null
  email: string | null
  avatar: string | null
  role: string
  storageQuota: number
  storageUsed: number
}

/** 存储配额视图 */
export interface Quota { quota: number; used: number; percentage: number }

/** GET/PUT /users/me 返回 */
export interface UserMe { user: UserInfo; quota: Quota }

/** 登录/刷新返回 */
export interface TokenResp {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userInfo: UserInfo
}

/** 标签 */
export interface TagItem { id: number; name: string; color?: string | null }

/** 文件元数据 */
export interface FileInfo {
  id: number
  name: string
  ext: string | null
  mimeType: string | null
  size: number
  sha256?: string
  categoryId?: number | null
  summary?: string | null
  status: string
  star: boolean
  folderId?: number | null
  lastAccessAt?: string | null
  deletedAt?: string | null
  createdAt: string
  updatedAt: string
  tags?: TagItem[]
}

/** 目录树节点 */
export interface FolderNode {
  id: number
  name: string
  parentId: number | null
  children?: FolderNode[]
}

/** 文件处理状态的中文章名 */
export const STATUS_LABEL: Record<string, string> = {
  UPLOADED: '已入库',
  PARSING: '解析中',
  SUMMARIZING: '编目中',
  CHUNKED: '编目中',
  EMBEDDED: '编目中',
  GRAPH_DONE: '编目中',
  READY: '已就绪',
  FAILED: '失败',
}

/** 人类可读的文件大小 */
export function humanSize(bytes: number): string {
  if (!bytes) return '—'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let v = bytes
  let i = 0
  while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }
  return `${v >= 100 || i === 0 ? Math.round(v) : v.toFixed(1)} ${units[i]}`
}

/** 人类可读时间 */
export function humanTime(iso: string | null | undefined): string {
  if (!iso) return '—'
  const d = new Date(iso)
  const diff = Date.now() - d.getTime()
  if (diff < 60_000) return '刚刚'
  if (diff < 3_600_000) return `${Math.floor(diff / 60_000)} 分钟前`
  if (diff < 86_400_000) return `${Math.floor(diff / 3_600_000)} 小时前`
  return `${d.getMonth() + 1}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
