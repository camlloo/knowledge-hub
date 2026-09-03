/** 界面级共享状态：列表视图模式、上传完成信号 */
import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    viewMode: (localStorage.getItem('kh.view') ?? 'table') as 'table' | 'grid',
    /** 上传成功后自增，文件列表监听它自动刷新 */
    uploadTick: 0,
  }),
  actions: {
    setViewMode(m: 'table' | 'grid') {
      this.viewMode = m
      localStorage.setItem('kh.view', m)
    },
    notifyUploaded() {
      this.uploadTick++
    },
  },
})
