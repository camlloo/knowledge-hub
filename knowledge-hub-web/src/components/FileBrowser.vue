<template>
  <div class="bench">
    <!-- 桌沿工具行 -->
    <div class="bench-tools">
      <span class="bench-count num">{{ loading ? '…' : total }} 份在桌</span>
      <div class="bench-right">
        <select v-model="sortBy" class="sort" aria-label="排序">
          <option value="createdAt">按时间</option>
          <option value="name">按名称</option>
          <option value="size">按大小</option>
        </select>
        <div class="view-toggle" role="tablist" aria-label="视图模式">
          <button
            :class="{ on: ui.viewMode === 'table' }" title="表格视图"
            @click="ui.setViewMode('table')"
          ><el-icon><Tickets /></el-icon></button>
          <button
            :class="{ on: ui.viewMode === 'grid' }" title="网格视图"
            @click="ui.setViewMode('grid')"
          ><el-icon><Menu /></el-icon></button>
        </div>
      </div>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="bench-loading">
      <el-icon class="spin"><Loading /></el-icon>
      <span>正在翻动桌上的卡片…</span>
    </div>

    <!-- 空态：直白命名自己 -->
    <div v-else-if="!items.length" class="bench-empty">
      <div class="empty-lamp" aria-hidden="true"></div>
      <p class="empty-title">{{ emptyTitle }}</p>
      <p class="empty-hint">{{ emptyHint }}</p>
    </div>

    <!-- 表格模式 -->
    <div v-else-if="ui.viewMode === 'table'" class="rows paper-scope">
      <div class="row row-head">
        <span class="c-star"></span>
        <span class="c-name">名称</span>
        <span class="c-tags">标签</span>
        <span class="c-size">大小</span>
        <span class="c-time">修改时间</span>
        <span class="c-status">状态</span>
        <span class="c-ops"></span>
      </div>
      <div v-for="f in items" :key="f.id" class="row">
        <span class="c-star"><BrassStar :on="f.star" @toggle="toggleStar(f)" /></span>
        <span class="c-name cell-name" :title="f.name" @click="openPreview(f)">
          <el-icon class="file-ico" :class="iconKind(f)"><component :is="iconOf(f)" /></el-icon>
          <span class="name-text">{{ f.name }}</span>
        </span>
        <span class="c-tags">
          <span v-for="t in f.tags ?? []" :key="t.id" class="tag-chip">{{ t.name }}</span>
          <span v-if="!f.tags?.length" class="dim">—</span>
        </span>
        <span class="c-size num">{{ humanSize(f.size) }}</span>
        <span class="c-time num">{{ humanTime(f.updatedAt) }}</span>
        <span class="c-status"><StampTag :status="f.status" /></span>
        <span class="c-ops">
          <button class="op" title="下载" @click="download(f)"><el-icon><Download /></el-icon></button>
          <button class="op" title="重命名" @click="rename(f)"><el-icon><EditPen /></el-icon></button>
          <button class="op op-danger" title="移入回收站" @click="remove(f)"><el-icon><Delete /></el-icon></button>
        </span>
      </div>
    </div>

    <!-- 网格模式 -->
    <div v-else class="cards paper-scope">
      <div v-for="f in items" :key="f.id" class="gcard">
        <span class="gcard-star"><BrassStar :on="f.star" @toggle="toggleStar(f)" /></span>
        <div class="gcard-icon" @click="openPreview(f)">
          <el-icon :class="iconKind(f)"><component :is="iconOf(f)" /></el-icon>
        </div>
        <div class="gcard-name" :title="f.name" @click="openPreview(f)">{{ f.name }}</div>
        <div class="gcard-meta num">{{ humanSize(f.size) }} · {{ humanTime(f.updatedAt) }}</div>
        <div class="gcard-stamp"><StampTag :status="f.status" /></div>
        <div class="gcard-ops">
          <button class="op" title="下载" @click="download(f)"><el-icon><Download /></el-icon></button>
          <button class="op op-danger" title="移入回收站" @click="remove(f)"><el-icon><Delete /></el-icon></button>
        </div>
      </div>
    </div>

    <!-- 桌沿：分页 -->
    <div v-if="!loading && total > size" class="bench-foot">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        background
      />
    </div>

    <PreviewDialog v-model="previewOpen" :file="previewFile" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tickets, Menu, Loading, Download, EditPen, Delete, Document, Picture, Tickets as MdIco, VideoPlay, Headset, Box } from '@element-plus/icons-vue'
import BrassStar from '@/components/BrassStar.vue'
import StampTag from '@/components/StampTag.vue'
import PreviewDialog from '@/components/PreviewDialog.vue'
import { apiStar, apiDownloadUrl, apiRenameOrMove, apiDeleteFile } from '@/api/file'
import { useUiStore } from '@/stores/ui'
import { humanSize, humanTime, type FileInfo, type PageResult } from '@/types'

type SortBy = 'name' | 'size' | 'createdAt'

const props = defineProps<{
  fetcher: (page: number, size: number, sortBy: SortBy) => Promise<PageResult<FileInfo>>
  emptyTitle: string
  emptyHint: string
  reloadKey?: number
}>()

const ui = useUiStore()
const loading = ref(false)
const items = ref<FileInfo[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const sortBy = ref<SortBy>('createdAt')
const previewOpen = ref(false)
const previewFile = ref<FileInfo | null>(null)

async function load() {
  loading.value = true
  try {
    const r = await props.fetcher(page.value, size, sortBy.value)
    items.value = r.list
    total.value = r.total
  } catch {
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

watch([page, sortBy], load, { immediate: true })
watch(() => props.reloadKey, () => { page.value = 1; load() })

async function toggleStar(f: FileInfo) {
  await apiStar(f.id, !f.star)
  f.star = !f.star
  ElMessage.success(f.star ? '已夹上黄铜折角' : '已取下折角')
}

function openPreview(f: FileInfo) { previewFile.value = f; previewOpen.value = true }

async function download(f: FileInfo) {
  const url = await apiDownloadUrl(f.id)
  window.open(url, '_blank')
}

async function rename(f: FileInfo) {
  try {
    const { value } = await ElMessageBox.prompt('新的文件名', '重命名', { inputValue: f.name })
    const name = value?.trim()
    if (name && name !== f.name) {
      await apiRenameOrMove(f.id, { name })
      f.name = name
      ElMessage.success('已重命名')
    }
  } catch { /* 取消 */ }
}

async function remove(f: FileInfo) {
  try {
    await ElMessageBox.confirm(
      `「${f.name}」将移入回收站，30 天内可恢复。`, '移入回收站',
      { type: 'warning', confirmButtonText: '收进托盘', cancelButtonText: '取消' })
    await apiDeleteFile(f.id)
    ElMessage.success('已收进回收站托盘')
    load()
  } catch { /* 取消 */ }
}

/* 文件图标按扩展名分派（统一线重的真实图标库） */
const DOC_EXTS = ['md', 'txt', 'doc', 'docx', 'rtf']
const IMG_EXTS = ['png', 'jpg', 'jpeg', 'gif', 'webp', 'svg', 'bmp']
const VID_EXTS = ['mp4', 'mkv', 'avi', 'mov', 'webm']
const AUD_EXTS = ['mp3', 'wav', 'flac', 'ogg']
const ARC_EXTS = ['zip', 'rar', '7z', 'tar', 'gz']

function iconKind(f: FileInfo) {
  const e = (f.ext ?? '').toLowerCase()
  if (IMG_EXTS.includes(e)) return 'k-img'
  if (VID_EXTS.includes(e)) return 'k-vid'
  if (AUD_EXTS.includes(e)) return 'k-aud'
  if (ARC_EXTS.includes(e)) return 'k-arc'
  return 'k-doc'
}
function iconOf(f: FileInfo) {
  const e = (f.ext ?? '').toLowerCase()
  if (IMG_EXTS.includes(e)) return Picture
  if (VID_EXTS.includes(e)) return VideoPlay
  if (AUD_EXTS.includes(e)) return Headset
  if (ARC_EXTS.includes(e)) return Box
  if (DOC_EXTS.includes(e)) return MdIco
  return Document
}

defineExpose({ reload: load })
</script>

<style scoped>
.bench { padding: 18px 26px 26px; }

.bench-tools {
  display: flex; align-items: center; justify-content: space-between;
  padding-bottom: 12px; margin-bottom: 6px;
  border-bottom: 1px solid var(--paper-edge);
}
.bench-count { color: var(--ink-soft); }
.bench-right { display: flex; align-items: center; gap: 10px; }

.sort {
  height: 30px; padding: 0 8px;
  border: 1px solid var(--paper-edge); border-radius: 6px;
  background: #FFFFFF; color: var(--ink-soft); font-size: 12.5px;
}
.view-toggle { display: flex; border: 1px solid var(--paper-edge); border-radius: 6px; overflow: hidden; }
.view-toggle button {
  width: 32px; height: 30px;
  display: grid; place-items: center;
  background: #FFFFFF; border: none; cursor: pointer;
  color: var(--ink-faint);
}
.view-toggle button + button { border-left: 1px solid var(--paper-edge); }
.view-toggle button.on { background: var(--brass); color: #FFF9EC; }

/* 表格行 */
.rows { display: flex; flex-direction: column; }
.row {
  display: grid;
  grid-template-columns: 34px minmax(220px, 1fr) 150px 90px 130px 88px 108px;
  gap: 10px; align-items: center;
  padding: 9px 10px;
  border-bottom: 1px solid rgba(220, 208, 178, .6);
  border-radius: 6px;
}
.row:not(.row-head):hover { background: #FAF8F1; }
.row-head {
  font-size: 12px; color: var(--ink-faint); letter-spacing: .08em;
  border-bottom: 1px solid var(--paper-edge);
  padding-top: 4px; padding-bottom: 6px;
}
.cell-name {
  display: flex; align-items: center; gap: 8px; min-width: 0;
  cursor: pointer; font-size: 14px; color: var(--ink);
}
.cell-name:hover .name-text { color: var(--brass-deep); text-decoration: underline; text-underline-offset: 3px; }
.name-text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-ico { color: var(--brass-deep); flex: none; }
.file-ico.k-img { color: #6B8E5A; }
.file-ico.k-vid { color: #7A5CA8; }
.file-ico.k-aud { color: #A8674E; }
.c-tags { display: flex; gap: 4px; flex-wrap: wrap; overflow: hidden; }
.tag-chip {
  font-size: 11px; color: var(--ink-soft);
  background: rgba(176, 141, 74, .13);
  border-radius: 4px; padding: 1px 7px;
  white-space: nowrap;
}
.dim { color: var(--ink-faint); }
.c-size, .c-time { color: var(--ink-soft); }
.c-status { display: flex; }
.ops, .c-ops { display: flex; gap: 2px; justify-content: flex-end; }
.op {
  width: 26px; height: 26px; border-radius: 6px;
  display: grid; place-items: center;
  background: none; border: none; cursor: pointer;
  color: var(--ink-faint);
}
.op:hover { color: var(--brass-deep); background: rgba(176, 141, 74, .12); }
.op-danger:hover { color: var(--seal); background: rgba(140, 59, 46, .1); }

/* 网格卡片 */
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
  gap: 14px;
}
.gcard {
  position: relative;
  background: #FFFFFF;
  border: 1px solid var(--paper-edge);
  border-radius: var(--radius);
  padding: 16px 12px 10px;
  text-align: center;
  box-shadow: var(--shadow-paper);
  transition: transform .15s, box-shadow .15s;
}
.gcard:hover { transform: translateY(-2px); box-shadow: 0 2px 4px rgba(43,42,36,.08), 0 12px 28px rgba(43,42,36,.14); }
.gcard-star { position: absolute; top: 6px; right: 6px; }
.gcard-icon { font-size: 40px; color: var(--brass-deep); padding: 8px 0 6px; cursor: pointer; }
.gcard-icon.k-img { color: #6B8E5A; }
.gcard-name {
  font-size: 13.5px; color: var(--ink);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  cursor: pointer;
}
.gcard-name:hover { color: var(--brass-deep); }
.gcard-meta { margin-top: 4px; color: var(--ink-faint); }
.gcard-stamp { margin-top: 8px; }
.gcard-ops { display: flex; justify-content: center; gap: 2px; margin-top: 4px; opacity: 0; transition: opacity .15s; }
.gcard:hover .gcard-ops { opacity: 1; }

/* 空态 */
.bench-empty { text-align: center; padding: 72px 0 60px; }
.empty-lamp {
  width: 120px; height: 60px; margin: 0 auto 18px;
  background: radial-gradient(closest-side, rgba(196, 155, 84, .28), rgba(196, 155, 84, 0));
  border-radius: 50%;
}
.empty-title { margin: 0; font-size: 17px; font-weight: 600; color: var(--ink); }
.empty-hint { margin: 8px 0 0; font-size: 13px; color: var(--ink-soft); }

.bench-loading {
  display: flex; align-items: center; justify-content: center; gap: 10px;
  padding: 80px 0; color: var(--ink-soft); font-size: 14px;
}
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.bench-foot { display: flex; justify-content: center; padding-top: 18px; }
</style>
