<template>
  <el-dialog
    :model-value="modelValue && !!file"
    :title="file?.name ?? '预览'"
    width="72%"
    top="6vh"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="prev-body paper-scope">
      <div v-if="loading" class="prev-loading">
        <el-icon class="spin"><Loading /></el-icon>
        <span>正在从库房取件…</span>
      </div>

      <iframe v-else-if="kind === 'pdf'" :src="url" class="prev-frame" :title="file?.name" />

      <img v-else-if="kind === 'image'" :src="url" class="prev-img" :alt="file?.name" />

      <pre v-else-if="kind === 'text'" class="prev-text num">{{ text }}</pre>

      <div v-else class="prev-none">
        <p>这类文件还不会在灯下摊开。</p>
        <el-button type="primary" @click="download">下载查看</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { apiPreviewUrl } from '@/api/file'
import type { FileInfo } from '@/types'

const props = defineProps<{ modelValue: boolean; file: FileInfo | null }>()
const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const loading = ref(false)
const url = ref('')
const text = ref('')

const TEXT_EXTS = ['md', 'txt', 'json', 'log', 'csv', 'java', 'ts', 'js', 'py', 'sql', 'yml', 'yaml', 'xml', 'html', 'css']

const kind = computed(() => {
  const f = props.file
  if (!f) return 'none'
  const e = (f.ext ?? '').toLowerCase()
  if (e === 'pdf' || f.mimeType === 'application/pdf') return 'pdf'
  if (['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'].includes(e)) return 'image'
  if (TEXT_EXTS.includes(e) || (f.mimeType ?? '').startsWith('text/')) return 'text'
  return 'none'
})

watch(() => [props.modelValue, props.file?.id] as const, async ([open]) => {
  if (!open || !props.file) return
  loading.value = true
  text.value = ''
  try {
    // 预签名 URL 直连 MinIO，文件流不过后端
    url.value = await apiPreviewUrl(props.file.id)
    if (kind.value === 'text') {
      text.value = await fetch(url.value).then((r) => r.text())
    }
  } catch {
    url.value = ''
  } finally {
    loading.value = false
  }
})

function download() {
  if (url.value) window.open(url.value, '_blank')
}
</script>

<style scoped>
.prev-body { min-height: 380px; max-height: 72vh; display: flex; align-items: center; justify-content: center; }
.prev-loading { display: flex; align-items: center; gap: 10px; color: var(--ink-soft); }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.prev-frame { width: 100%; height: 70vh; border: 1px solid var(--paper-edge); border-radius: 6px; background: #FFFFFF; }
.prev-img { max-width: 100%; max-height: 70vh; object-fit: contain; border-radius: 6px; box-shadow: var(--shadow-paper); }
.prev-text {
  width: 100%; max-height: 70vh; overflow: auto;
  margin: 0; padding: 16px;
  background: #FFFFFF; border: 1px solid var(--paper-edge); border-radius: 6px;
  font-size: 13px; line-height: 1.7; color: var(--ink);
  white-space: pre-wrap; word-break: break-word;
}
.prev-none { text-align: center; color: var(--ink-soft); }
.prev-none p { margin: 0 0 14px; }
</style>
