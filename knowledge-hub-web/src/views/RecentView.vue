<template>
  <FileBrowser
    :fetcher="fetcher"
    empty-title="还没有翻阅记录"
    empty-hint="你打开过的文件会在这里留下折页，方便接着看。"
    :reload-key="0"
  />
</template>

<script setup lang="ts">
import FileBrowser from '@/components/FileBrowser.vue'
import { apiRecentFiles } from '@/api/file'
import type { PageResult, FileInfo } from '@/types'

// 最近访问返回平铺数组，包装成分页结构以复用浏览器
const fetcher = async (): Promise<PageResult<FileInfo>> => {
  const list = await apiRecentFiles(20)
  return { list, total: list.length, page: 1, size: list.length || 1 }
}
</script>
