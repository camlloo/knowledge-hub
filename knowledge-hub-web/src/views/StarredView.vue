<template>
  <FileBrowser
    :fetcher="fetcher"
    empty-title="还没有夹上任何黄铜折角"
    empty-hint="在文件行点亮折角标，重要的资料会自己聚到这里。"
    :reload-key="0"
  />
</template>

<script setup lang="ts">
import FileBrowser from '@/components/FileBrowser.vue'
import { apiPageFiles } from '@/api/file'

const fetcher = (page: number, size: number, sortBy: 'name' | 'size' | 'createdAt') =>
  apiPageFiles({ page, size, sortBy, order: 'desc', star: true })
</script>
