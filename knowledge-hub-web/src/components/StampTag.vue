/** 状态章：文件被理解程度的可视化（签名元素，斜盖的小章） */
<script lang="ts">
export default { name: 'StampTag' }
</script>
<script setup lang="ts">
import { computed } from 'vue'
import { STATUS_LABEL } from '@/types'

const props = defineProps<{ status: string; animate?: boolean }>()

const kind = computed(() => {
  const s = props.status
  if (s === 'READY') return 'ok'
  if (s === 'FAILED') return 'bad'
  if (s === 'UPLOADED') return 'raw'
  return 'doing'
})
const label = computed(() => STATUS_LABEL[props.status] ?? props.status)
</script>

<template>
  <span class="stamp" :class="[kind, { 'stamp-in': animate }]" :title="`处理状态：${label}`">{{ label }}</span>
</template>

<style scoped>
.stamp {
  display: inline-block;
  padding: 1px 8px 2px;
  border: 1.5px solid currentColor;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: .14em;
  transform: rotate(-4deg);
  user-select: none;
  line-height: 1.5;
}
.ok   { color: var(--stamp-green); }
.raw  { color: var(--ink-faint); }
.doing{ color: var(--brass-deep); }
.bad  { color: var(--seal); }
</style>
