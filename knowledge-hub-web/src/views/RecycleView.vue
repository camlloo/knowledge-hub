<template>
  <div class="tray">
    <!-- 托盘说明：桌下等待销毁的文件，30 天倒计时 -->
    <div class="tray-head">
      <p class="tray-note">
        桌下的托盘。躺满 <strong class="num">30</strong> 天的文件将被销毁，届时无法找回。
      </p>
      <el-button
        v-if="items.length"
        type="danger" plain size="small"
        :loading="clearing"
        @click="clearAll"
      >清空托盘</el-button>
    </div>

    <div v-if="loading" class="tray-loading">
      <el-icon class="spin"><Loading /></el-icon>
      <span>正在翻看托盘…</span>
    </div>

    <div v-else-if="!items.length" class="tray-empty">
      <p class="empty-title">托盘是空的</p>
      <p class="empty-hint">删除的文件会在这里躺 30 天，随时可以捡回来。</p>
    </div>

    <div v-else class="tray-rows paper-scope">
      <div v-for="f in items" :key="f.id" class="trow">
        <span class="t-name" :title="f.name">{{ f.name }}</span>
        <span class="t-size num">{{ humanSize(f.size) }}</span>
        <span class="t-time num">删于 {{ humanTime(f.deletedAt) }}</span>
        <!-- 倒计时签：来自剪辑台的"挂签"纪律 -->
        <span class="t-count num" :class="{ urgent: daysLeft(f) <= 7 }">剩 {{ daysLeft(f) }} 天</span>
        <span class="t-ops">
          <el-button size="small" @click="restore(f)">捡回来</el-button>
          <el-button size="small" type="danger" plain @click="purge(f)">彻底销毁</el-button>
        </span>
      </div>
      <div class="tray-foot">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRecyclePage, apiRecycleRestore, apiRecyclePurge, apiRecycleClear, type RecycleItem } from '@/api/file'
import { humanSize, humanTime } from '@/types'

const loading = ref(false)
const clearing = ref(false)
const items = ref<RecycleItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 20

async function load() {
  loading.value = true
  try {
    const r = await apiRecyclePage(page.value, size)
    items.value = r.list
    total.value = r.total
  } catch {
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}
onMounted(load)
watch(page, load)

function daysLeft(f: RecycleItem) {
  if (!f.deletedAt) return 30
  const gone = Math.floor((Date.now() - new Date(f.deletedAt).getTime()) / 86_400_000)
  return Math.max(0, 30 - gone)
}

async function restore(f: RecycleItem) {
  await apiRecycleRestore(f.id)
  ElMessage.success(`「${f.name}」已回到桌面`)
  load()
}

async function purge(f: RecycleItem) {
  try {
    await ElMessageBox.confirm(
      `「${f.name}」将立即销毁，无法找回。`, '彻底销毁',
      { type: 'error', confirmButtonText: '销毁', cancelButtonText: '再躺几天' })
    await apiRecyclePurge(f.id)
    ElMessage.success('已销毁')
    load()
  } catch { /* 取消 */ }
}

async function clearAll() {
  try {
    await ElMessageBox.confirm(
      `托盘里 ${total.value} 个文件将全部销毁，无法找回。`, '清空托盘',
      { type: 'error', confirmButtonText: '全部销毁', cancelButtonText: '算了' })
    clearing.value = true
    await apiRecycleClear()
    ElMessage.success('托盘已清空')
    page.value = 1
    load()
  } catch { /* 取消 */ } finally {
    clearing.value = false
  }
}
</script>

<style scoped>
.tray { padding: 18px 26px 26px; }
.tray-head { display: flex; align-items: center; justify-content: space-between; padding-bottom: 12px; margin-bottom: 8px; border-bottom: 1px solid var(--paper-edge); }
.tray-note { margin: 0; font-size: 13px; color: var(--ink-soft); }
.tray-note strong { color: var(--seal); }

.tray-loading { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 80px 0; color: var(--ink-soft); }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.tray-empty { text-align: center; padding: 80px 0 60px; }
.empty-title { margin: 0; font-size: 17px; font-weight: 600; color: var(--ink); }
.empty-hint { margin: 8px 0 0; font-size: 13px; color: var(--ink-soft); }

.trow {
  display: grid;
  grid-template-columns: minmax(200px, 1fr) 90px 140px 88px 190px;
  gap: 10px; align-items: center;
  padding: 9px 10px;
  border-bottom: 1px solid rgba(220, 208, 178, .6);
  border-radius: 6px;
}
.trow:hover { background: #FAF8F1; }
.t-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 14px; color: var(--ink); }
.t-size, .t-time { color: var(--ink-soft); }
.t-count {
  justify-self: start;
  font-size: 11px; padding: 1px 8px;
  border: 1px solid var(--brass);
  color: var(--brass-deep);
  border-radius: 4px;
  transform: rotate(-3deg);
}
.t-count.urgent { border-color: var(--seal); color: var(--seal); }
.t-ops { display: flex; gap: 6px; justify-content: flex-end; }

.tray-foot { display: flex; justify-content: center; padding-top: 18px; }
</style>
