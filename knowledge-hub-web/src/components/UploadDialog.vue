<template>
  <el-dialog
    :model-value="modelValue"
    title="把资料放上台面"
    width="520px"
    :close-on-click-modal="false"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="up-body paper-scope">
      <button class="pick" :disabled="busy" @click="pick?.click()">
        <el-icon><UploadFilled /></el-icon>
        <span>{{ queue.length ? '继续添加' : '选择文件（可多选）' }}</span>
      </button>
      <input ref="pick" type="file" multiple hidden @change="onPicked" />

      <ul v-if="queue.length" class="queue">
        <li v-for="it in queue" :key="it.uid">
          <span class="q-name" :title="it.file.name">{{ it.file.name }}</span>
          <span class="q-state num" :class="it.state">
            {{ stateText(it) }}
          </span>
          <el-progress
            v-if="it.state === 'uploading'"
            :percentage="it.percent"
            :show-text="false"
            :stroke-width="4"
            class="q-bar"
          />
        </li>
      </ul>

      <p class="up-note">
        相同内容的文件会被<a title="秒传：内容哈希一致即直接建档，无需传输">秒传</a>——只存一份，全场通用。
      </p>
    </div>

    <template #footer>
      <span class="dialog-foot">
        <el-button @click="emit('update:modelValue', false)">关上</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { sha256Hex } from '@/utils/hash'
import { apiHashCheck, apiUpload } from '@/api/file'
import { useFolderStore } from '@/stores/folder'
import { useUiStore } from '@/stores/ui'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [boolean]; uploaded: [] }>()
void props

const folder = useFolderStore()
const ui = useUiStore()
const pick = ref<HTMLInputElement>()
const busy = ref(false)

type State = 'waiting' | 'hashing' | 'instant' | 'uploading' | 'done' | 'fail'
interface Item { uid: number; file: File; state: State; percent: number }
const queue = ref<Item[]>([])
let uidSeq = 0

function stateText(it: Item) {
  switch (it.state) {
    case 'waiting': return '排队中'
    case 'hashing': return '计算指纹…'
    case 'instant': return '秒传 ✓'
    case 'uploading': return `${it.percent}%`
    case 'done': return '已上台 ✓'
    case 'fail': return '失败'
  }
}

async function onPicked(e: Event) {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  input.value = ''
  if (!files.length) return
  busy.value = true
  for (const file of files) {
    const it: Item = { uid: ++uidSeq, file, state: 'waiting', percent: 0 }
    queue.value.push(it)
    try {
      // ① 算指纹 → ② 秒传预检 → ③ 未命中才直传
      it.state = 'hashing'
      const sha256 = await sha256Hex(file)
      const check = await apiHashCheck({
        sha256, fileName: file.name, size: file.size,
        folderId: folder.currentId,
      })
      if (check.instant) {
        it.state = 'instant'
      } else {
        it.state = 'uploading'
        await apiUpload({ file, folderId: folder.currentId, sha256, onProgress: (p) => (it.percent = p) })
        it.state = 'done'
      }
      ui.notifyUploaded()
    } catch {
      it.state = 'fail'
    }
  }
  busy.value = false
  ElMessage.success('桌上的东西安置好了')
}
</script>

<style scoped>
.pick {
  width: 100%; padding: 26px 0;
  border: 1.5px dashed var(--brass);
  border-radius: var(--radius);
  background: rgba(176, 141, 74, .06);
  color: var(--brass-deep);
  font-size: 14px; cursor: pointer;
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  transition: background .15s;
}
.pick:hover { background: rgba(176, 141, 74, .12); }
.pick .el-icon { font-size: 26px; }

.queue { list-style: none; margin: 14px 0 0; padding: 0; max-height: 240px; overflow: auto; }
.queue li { display: flex; align-items: center; gap: 10px; padding: 7px 2px; border-bottom: 1px dashed var(--paper-edge); }
.q-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; color: var(--ink); }
.q-state { flex: none; color: var(--ink-soft); }
.q-state.done, .q-state.instant { color: var(--stamp-green); }
.q-state.fail { color: var(--seal); }
.q-bar { width: 90px; flex: none; }

.up-note { margin: 14px 0 0; font-size: 12px; color: var(--ink-soft); }
.up-note a { color: var(--brass-deep); cursor: help; }
.dialog-foot { display: inline-flex; }
</style>
