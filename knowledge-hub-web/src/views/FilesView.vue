<template>
  <FileBrowser
    :fetcher="fetcher"
    :empty-title="emptyTitle"
    empty-hint="点右上角「上传」，把第一份资料放上台面。"
    :reload-key="reloadKey"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import FileBrowser from '@/components/FileBrowser.vue'
import { apiPageFiles } from '@/api/file'
import { useFolderStore } from '@/stores/folder'

const route = useRoute()
const folder = useFolderStore()
const reloadKey = ref(0)
const keyword = ref('')

// 支持从侧栏目录树与顶栏搜索两个入口进入不同"桌面"
const fetcher = (page: number, size: number, sortBy: 'name' | 'size' | 'createdAt') =>
  apiPageFiles({
    page, size, sortBy,
    order: 'desc',
    folderId: folder.currentId,
    keyword: keyword.value || undefined,
  })

const emptyTitle = computed(() =>
  keyword.value ? '没有检索到匹配的文件' : '这张桌上还没有文件')

// 侧栏选择目录 → 重载；顶栏搜索词 → 重载
watch(() => folder.currentId, () => reloadKey.value++)
watch(() => route.query.k, (k) => {
  keyword.value = (k as string) ?? ''
  reloadKey.value++
}, { immediate: false })

// 深链：/files?folder=3
watch(() => route.query.folder, (v) => {
  if (v) folder.currentId = Number(v)
}, { immediate: true })
</script>
