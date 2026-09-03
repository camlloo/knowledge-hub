/** user 模块的出参补充类型（避免循环依赖 types.ts） */
export type { UserMe, Quota, UserInfo } from '@/types'

/** PUT /users/me 请求体：三字段可选，null 不更新 */
export interface UpdateMePayload {
  nickname?: string
  email?: string
  avatar?: string
}
