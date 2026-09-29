<script setup>
import { computed } from 'vue'

// 通用分页条（服务端分页）：父组件传当前页 / 总条数 / 每页条数，页码变化时 emit('change', 新页码)
// 数据加载由父组件负责（watch 页码或监听 change 事件均可）
const props = defineProps({
  page: { type: Number, required: true },     // 当前页（从 1 开始）
  total: { type: Number, default: 0 },        // 总条数
  pageSize: { type: Number, default: 20 }     // 每页条数
})

const emit = defineEmits(['change'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

// 页码列表：≤7 页全部展示，否则取首尾页 + 当前页前后一页，间断处插入省略号
const pageList = computed(() => {
  const total = totalPages.value
  const cur = props.page
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const set = new Set([1, total, cur - 1, cur, cur + 1].filter((p) => p >= 1 && p <= total))
  const out = []
  let prev = 0
  for (const p of [...set].sort((a, b) => a - b)) {
    if (p - prev > 1) out.push('…')
    out.push(p)
    prev = p
  }
  return out
})

function go(p) {
  if (p === '…' || p < 1 || p > totalPages.value || p === props.page) return
  emit('change', p)
}
</script>

<template>
  <div class="pager">
    <span class="pager-total">共 {{ total }} 条</span>
    <div class="pager-pages">
      <button class="pager-btn nav" :disabled="page <= 1" @click="go(page - 1)">‹</button>
      <template v-for="(p, i) in pageList" :key="i">
        <span v-if="p === '…'" class="pager-ellipsis">…</span>
        <button v-else class="pager-btn" :class="{ active: p === page }" @click="go(p)">{{ p }}</button>
      </template>
      <button class="pager-btn nav" :disabled="page >= totalPages" @click="go(page + 1)">›</button>
    </div>
  </div>
</template>

<style scoped>
/* 主题色走 CSS 变量，父组件可按页面主题覆盖（默认靛蓝） */
.pager {
  --pager-color: #3446c2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 2px 0;
}

.pager-total {
  font-size: 12px;
  color: #86909c;
}

.pager-pages {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pager-btn {
  min-width: 30px;
  height: 30px;
  padding: 0 8px;
  border: 1px solid color-mix(in srgb, var(--pager-color) 22%, transparent);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.75);
  font-family: var(--font-num);
  font-size: 13px;
  color: var(--pager-color);
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, transform 0.15s;
}

.pager-btn:hover:not(:disabled):not(.active) {
  background: color-mix(in srgb, var(--pager-color) 8%, transparent);
  border-color: color-mix(in srgb, var(--pager-color) 45%, transparent);
  transform: translateY(-1.5px);
}

.pager-btn.active {
  background: var(--pager-color);
  border-color: var(--pager-color);
  color: #fff;
  cursor: default;
}

.pager-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pager-ellipsis {
  min-width: 20px;
  text-align: center;
  font-size: 13px;
  color: #86909c;
  user-select: none;
}
</style>
