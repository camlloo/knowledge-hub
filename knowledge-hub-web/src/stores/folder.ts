/** 目录树与当前所在目录（侧栏与文件列表共享） */
import { defineStore } from 'pinia'
import { apiFolderTree, apiCreateFolder, apiUpdateFolder, apiDeleteFolder } from '@/api/folder'
import type { FolderNode } from '@/types'

export const useFolderStore = defineStore('folder', {
  state: () => ({
    tree: [] as FolderNode[],
    loaded: false,
    currentId: null as number | null,   // null = 根目录
  }),
  actions: {
    async loadTree() {
      try {
        this.tree = await apiFolderTree()
      } catch {
        // 后端目录接口未就绪：保持空树，界面给出优雅空态
        this.tree = []
      }
      this.loaded = true
    },
    async create(name: string, parentId: number | null) {
      await apiCreateFolder({ name, parentId })
      await this.loadTree()
    },
    async rename(id: number, name: string) {
      await apiUpdateFolder(id, { name })
      await this.loadTree()
    },
    async remove(id: number) {
      await apiDeleteFolder(id)
      if (this.currentId === id) this.currentId = null
      await this.loadTree()
    },
  },
})
