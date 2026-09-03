<template>
  <div class="room-frame">
    <ShelfSidebar @profile="profileOpen = true" />

    <div class="main-col">
      <!-- 黄铜顶栏：面包屑 · 搜索 · 上传 -->
      <header class="topbar dark-scope">
        <div class="crumb">
          <span class="crumb-room">{{ crumbRoom }}</span>
          <el-icon v-if="folder.currentId" class="crumb-sep"><ArrowRight /></el-icon>
          <span v-if="folder.currentId" class="crumb-folder">{{ currentFolderName }}</span>
        </div>

        <div class="search">
          <el-icon class="search-ico"><Search /></el-icon>
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="检索文件名…（回车）"
            @keydown.enter="doSearch"
          />
          <button v-if="keyword" class="search-clear" title="清空" @click="clearSearch">
            <el-icon><Close /></el-icon>
          </button>
        </div>

        <button class="brass-upload" @click="uploadOpen = true">
          <el-icon><UploadFilled /></el-icon>
          <span>上传</span>
        </button>
      </header>

      <!-- 灯下光池：内容区（表格模式光在左上，网格模式光在正中） -->
      <main class="lamp-pool paper-scope" :class="{ 'grid-light': ui.viewMode === 'grid' }">
        <router-view />
      </main>
    </div>

    <UploadDialog v-model="uploadOpen" @uploaded="ui.notifyUploaded()" />
    <ProfileDialog v-model="profileOpen" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowRight, Search, Close, UploadFilled } from '@element-plus/icons-vue'
import ShelfSidebar from '@/components/ShelfSidebar.vue'
import UploadDialog from '@/components/UploadDialog.vue'
import ProfileDialog from '@/components/ProfileDialog.vue'
import { useFolderStore } from '@/stores/folder'
import { useUiStore } from '@/stores/ui'
import type { FolderNode } from '@/types'

const route = useRoute()
const router = useRouter()
const folder = useFolderStore()
const ui = useUiStore()

const uploadOpen = ref(false)
const profileOpen = ref(false)

const crumbRoom = computed(() => {
  const map: Record<string, string> = {
    files: '全部文件', starred: '收藏', recent: '最近', recycle: '回收站',
  }
  return map[route.name as string] ?? '文件库'
})
const currentFolderName = computed(() => {
  if (!folder.currentId) return ''
  const find = (nodes: FolderNode[]): string | null => {
    for (const n of nodes) {
      if (n.id === folder.currentId) return n.name
      const c = n.children ? find(n.children) : null
      if (c) return c
    }
    return null
  }
  return find(folder.tree) ?? '…'
})

const keyword = ref('')
watch(() => route.query.k, (v) => { keyword.value = (v as string) ?? '' }, { immediate: true })

function doSearch() {
  router.push({ path: '/files', query: keyword.value ? { k: keyword.value } : {} })
}
function clearSearch() {
  keyword.value = ''
  router.push({ path: '/files' })
}
</script>

<style scoped>
.room-frame { display: flex; height: 100vh; overflow: hidden; }
.main-col { flex: 1; display: flex; flex-direction: column; min-width: 0; }

/* 顶栏：白底 + 细分隔线，黄铜只出现在动作上 */
.topbar {
  height: 60px; flex: none;
  display: flex; align-items: center; gap: 16px;
  padding: 0 20px;
  background: var(--paper-bright);
  border-bottom: 1px solid var(--paper-edge);
}
.crumb { display: flex; align-items: center; gap: 8px; color: var(--ink); font-size: 14px; min-width: 0; }
.crumb-room { font-weight: 600; letter-spacing: .04em; white-space: nowrap; }
.crumb-sep { color: var(--ink-faint); }
.crumb-folder { color: var(--brass-deep); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.search {
  flex: 1; max-width: 460px; margin-left: auto;
  display: flex; align-items: center; gap: 8px;
  height: 36px; padding: 0 12px;
  background: #F5F4EE;
  border: 1px solid var(--paper-edge);
  border-radius: 8px;
  transition: border-color .15s, background .15s;
}
.search:focus-within { border-color: var(--brass); background: #FFFFFF; }
.search-ico { color: var(--ink-faint); }
.search-input {
  flex: 1; background: none; border: none; outline: none;
  color: var(--ink); font-size: 13px;
  caret-color: var(--brass-deep);
}
.search-input::placeholder { color: var(--ink-faint); }
.search-clear { background: none; border: none; color: var(--ink-faint); cursor: pointer; display: grid; place-items: center; }
.search-clear:hover { color: var(--brass-deep); }

.brass-upload {
  height: 36px; padding: 0 16px;
  display: flex; align-items: center; gap: 6px;
  border: none; border-radius: 8px; cursor: pointer;
  background: linear-gradient(180deg, var(--brass-hi), var(--brass));
  color: #FFF9EC; font-size: 13.5px; font-weight: 600; letter-spacing: .06em;
  box-shadow: 0 1px 0 rgba(255,255,255,.3) inset, 0 4px 12px rgba(117, 92, 43, .28);
  transition: filter .15s, transform .05s;
}
.brass-upload:hover { filter: brightness(1.06); }
.brass-upload:active { transform: translateY(1px); }

.main-col .lamp-pool { flex: 1; overflow: auto; }
</style>
