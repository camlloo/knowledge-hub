<template>
  <aside class="shelf dark-scope">
    <!-- 铭牌 -->
    <div class="brand">
      <span class="brand-mark">知</span>
      <div class="brand-text">
        <strong>知识中枢</strong>
        <small>KNOWLEDGE HUB</small>
      </div>
    </div>

    <!-- 导航：房间暗部里的四个房间 -->
    <nav class="nav">
      <RouterLink
        v-for="item in navs"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ on: isActive(item.path) }"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
        <span v-if="item.path === '/recycle'" class="nav-tag">30天</span>
      </RouterLink>
    </nav>

    <!-- 分类架：目录树 -->
    <div class="tree-head">
      <span class="tree-title">分类架</span>
      <button class="mini-btn" title="新建文件夹" @click="createFolder">
        <el-icon><Plus /></el-icon>
      </button>
    </div>
    <div class="tree-body">
      <el-tree
        v-if="folder.tree.length"
        :data="folder.tree"
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        :current-node-key="folder.currentId ?? undefined"
        :expand-on-click-node="false"
        @node-click="onPick"
      >
        <template #default="{ data }">
          <div class="tree-node" @mouseenter="hoverId = data.id" @mouseleave="hoverId = null">
            <el-icon class="tree-ico"><FolderOpened /></el-icon>
            <span class="tree-name">{{ data.name }}</span>
            <span v-if="hoverId === data.id" class="tree-ops">
              <el-icon title="重命名" @click.stop="renameFolder(data)"><EditPen /></el-icon>
              <el-icon title="删除" @click.stop="removeFolder(data)"><Delete /></el-icon>
            </span>
          </div>
        </template>
      </el-tree>
      <p v-else class="tree-empty">
        {{ folder.loaded ? '分类架还是空的。点右上角 +，立起第一个文件夹。' : '正在打开分类架…' }}
      </p>
    </div>

    <!-- 桌沿：配额墨线（渐短的线即剩余空间） -->
    <div class="quota">
      <div class="quota-line"><i :style="{ transform: `scaleX(${(quotaWidth) / 100})` }"></i></div>
      <div class="quota-text num">
        {{ usedText }} / {{ quotaText }}
      </div>
    </div>

    <!-- 读者：头像圆章 + 退出 -->
    <div class="reader">
      <span class="reader-seal">{{ firstChar }}</span>
      <div class="reader-text">
        <strong>{{ user.nickname }}</strong>
        <button class="reader-out" @click="doLogout">退出阅览室</button>
      </div>
      <button class="mini-btn" title="个人信息 / 修改密码" @click="emit('profile')">
        <el-icon><Setting /></el-icon>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Star, Clock, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useFolderStore } from '@/stores/folder'
import { humanSize, type FolderNode } from '@/types'

const emit = defineEmits<{ profile: [] }>()
const route = useRoute()
const router = useRouter()
const user = useUserStore()
const folder = useFolderStore()
const hoverId = ref<number | null>(null)

const navs = [
  { path: '/files', label: '全部文件', icon: markRaw(Document) },
  { path: '/starred', label: '收藏', icon: markRaw(Star) },
  { path: '/recent', label: '最近', icon: markRaw(Clock) },
  { path: '/recycle', label: '回收站', icon: markRaw(Delete) },
]
function isActive(p: string) { return route.path === p }

const firstChar = computed(() => user.nickname.slice(0, 1).toUpperCase())

const quotaWidth = computed(() => Math.min(100, user.quota?.percentage ?? 0))
const usedText = computed(() => humanSize(user.quota?.used ?? 0))
const quotaText = computed(() => humanSize(user.quota?.quota ?? 0))

onMounted(() => { folder.loadTree() })

function onPick(node: FolderNode) {
  folder.currentId = node.id
  router.push({ path: '/files', query: { folder: String(node.id) } })
}

async function createFolder() {
  try {
    const { value } = await ElMessageBox.prompt('文件夹名称', '立一个新分类', {
      inputValue: '',
      confirmButtonText: '立起来',
      cancelButtonText: '算了',
    })
    if (value?.trim()) {
      await folder.create(value.trim(), folder.currentId)
      ElMessage.success('分类已立起')
    }
  } catch { /* 取消 */ }
}

async function renameFolder(node: FolderNode) {
  try {
    const { value } = await ElMessageBox.prompt('新的名称', '重命名分类', { inputValue: node.name })
    if (value?.trim() && value.trim() !== node.name) await folder.rename(node.id, value.trim())
  } catch { /* 取消 */ }
}

async function removeFolder(node: FolderNode) {
  try {
    await ElMessageBox.confirm(
      `删除「${node.name}」？其中的文件将进入回收站，30 天内可恢复。`,
      '删除分类', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await folder.remove(node.id)
    ElMessage.success('已删除，文件在回收站')
  } catch { /* 取消 */ }
}

async function doLogout() {
  await user.logout()
  router.push('/login')
}
</script>

<style scoped>
.shelf {
  width: 264px; flex: none;
  display: flex; flex-direction: column;
  background: linear-gradient(180deg, #FBFAF6, var(--room-deep) 70%);
  color: var(--room-text);
  border-right: 1px solid var(--room-line);
  overflow: hidden;
}
.brand { display: flex; align-items: center; gap: 10px; padding: 18px 18px 14px; }
.brand-mark {
  width: 34px; height: 34px; border-radius: 8px;
  border: 1px solid var(--brass);
  display: grid; place-items: center;
  color: var(--brass-deep); font-weight: 600;
}
.brand-text { display: flex; flex-direction: column; line-height: 1.2; }
.brand-text strong { font-size: 15px; letter-spacing: .1em; }
.brand-text small { font-size: 10px; color: var(--room-text-dim); letter-spacing: .22em; }

.nav { display: flex; flex-direction: column; gap: 2px; padding: 4px 10px 10px; }
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 12px; border-radius: 8px;
  color: var(--room-text-dim); text-decoration: none; font-size: 14px;
  transition: background .15s, color .15s;
}
.nav-item:hover { color: var(--ink); background: var(--room-panel); }
.nav-item.on { color: var(--brass-deep); background: var(--brass-wash); box-shadow: inset 2px 0 0 var(--brass); font-weight: 600; }
.nav-tag {
  margin-left: auto; font-family: var(--font-mono); font-size: 10px;
  color: var(--room-text-dim); border: 1px solid var(--room-line);
  padding: 1px 6px; border-radius: 999px;
}

.tree-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 18px 6px;
  border-top: 1px solid var(--room-line);
}
.tree-title { font-size: 12px; letter-spacing: .2em; color: var(--room-text-dim); }
.mini-btn {
  width: 24px; height: 24px; border-radius: 6px;
  border: 1px solid var(--room-line); background: none;
  color: var(--room-text-dim); cursor: pointer;
  display: grid; place-items: center;
  transition: color .15s, border-color .15s;
}
.mini-btn:hover { color: var(--brass); border-color: var(--brass); }

.tree-body { flex: 1; overflow: auto; padding: 2px 10px 10px; }
.tree-body :deep(.el-tree) {
  background: transparent; color: var(--room-text);
  --el-tree-node-hover-bg-color: var(--room-panel);
}
.tree-body :deep(.el-tree-node__content) { height: 30px; border-radius: 6px; }
.tree-body :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: var(--brass-wash); color: var(--brass-deep); font-weight: 600;
}
.tree-node { display: flex; align-items: center; gap: 6px; width: 100%; padding-right: 4px; }
.tree-ico { color: var(--brass); flex: none; }
.tree-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
.tree-ops { display: flex; gap: 4px; color: var(--room-text-dim); }
.tree-ops .el-icon:hover { color: var(--brass); }
.tree-empty { padding: 14px 8px; font-size: 12.5px; color: var(--room-text-dim); line-height: 1.7; }

.quota { padding: 12px 18px; border-top: 1px solid var(--room-line); }
.quota-line { height: 3px; background: var(--room-line); border-radius: 2px; overflow: hidden; }
/* 用 transform 而非 width 做动画，避免布局抖动 */
.quota-line i {
  display: block; height: 100%; background: var(--brass);
  transform-origin: left center;
  transition: transform .5s ease;
}
.quota-text { margin-top: 6px; color: var(--room-text-dim); }

.reader {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 18px 16px;
  border-top: 1px solid var(--room-line);
}
.reader-seal {
  width: 36px; height: 36px; border-radius: 50%; flex: none;
  background: var(--brass-wash);
  border: 1px solid var(--brass);
  color: var(--brass-deep);
  display: grid; place-items: center; font-weight: 600;
}
.reader-text { flex: 1; line-height: 1.3; min-width: 0; }
.reader-text strong { display: block; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.reader-out { background: none; border: none; padding: 0; font-size: 12px; color: var(--room-text-dim); cursor: pointer; }
.reader-out:hover { color: var(--brass); }
</style>
