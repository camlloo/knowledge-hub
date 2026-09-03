/** 黄铜折角收藏标：状态是标记而非颜色（来自剪辑台的纪律） */
<script lang="ts">
export default { name: 'BrassStar' }
</script>
<script setup lang="ts">
const props = defineProps<{ on: boolean }>()
const emit = defineEmits<{ toggle: [] }>()
</script>

<template>
  <button
    class="fold"
    :class="{ on: props.on }"
    :title="props.on ? '取消收藏' : '收藏'"
    @click.stop="emit('toggle')"
  >
    <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
      <!-- 折角书签：一个确定的"标记" -->
      <path d="M3 1.5h10v13l-5-3.6-5 3.6z" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
      <path v-if="props.on" d="M3 1.5h10v13l-5-3.6-5 3.6z" fill="currentColor" />
    </svg>
  </button>
</template>

<style scoped>
.fold {
  width: 26px; height: 26px;
  display: grid; place-items: center;
  background: none; border: none; border-radius: 6px;
  color: var(--ink-faint);
  cursor: pointer;
  transition: color .15s, background .15s, transform .1s;
}
.fold:hover { color: var(--brass-deep); background: rgba(176, 141, 74, .12); }
.fold.on { color: var(--brass); }
.fold.on:active { animation: stamp-in .38s cubic-bezier(.2, .8, .3, 1); }
</style>
