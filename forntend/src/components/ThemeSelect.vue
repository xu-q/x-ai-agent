<template>
  <div ref="rootRef" class="ts">
    <button type="button" class="ts-trigger" @click="open = !open">
      <span class="ts-label">{{ currentLabel }}</span>
      <svg class="ts-arrow" :class="{ up: open }" viewBox="0 0 24 24" width="12" height="12">
        <path d="M6 9l6 6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>
    <transition name="ts-drop">
      <ul v-if="open" class="ts-menu">
        <li
          v-for="opt in options"
          :key="opt.value"
          class="ts-option"
          :class="{ selected: opt.value === modelValue }"
          @click="pick(opt)"
        >
          {{ opt.label }}
          <svg v-if="opt.value === modelValue" class="ts-check" viewBox="0 0 24 24" width="12" height="12">
            <path d="M20 6L9 17l-5-5" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </li>
      </ul>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  options: { type: Array, required: true },
  placeholder: { type: String, default: '请选择' }
})
const emit = defineEmits(['update:modelValue'])

const rootRef = ref(null)
const open = ref(false)

const currentLabel = computed(() => {
  const hit = props.options.find((o) => o.value === props.modelValue)
  return hit ? hit.label : props.placeholder
})

function pick(opt) {
  emit('update:modelValue', opt.value)
  open.value = false
}

function onDocClick(e) {
  if (open.value && rootRef.value && !rootRef.value.contains(e.target)) open.value = false
}

function onKeydown(e) {
  if (e.key === 'Escape') open.value = false
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
/* 主题色取 --tab-color（tab 容器内自动继承；弹窗内由 theme 类显式定义） */
.ts {
  position: relative;
  display: inline-block;
}

.ts-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  color: #1f2329;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.ts-trigger:hover {
  border-color: var(--tab-color, #f5319d);
}

.ts-trigger:focus-visible {
  outline: none;
  border-color: var(--tab-color, #f5319d);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--tab-color, #f5319d) 12%, transparent);
}

.ts-label {
  flex: 1;
  white-space: nowrap;
}

.ts-arrow {
  color: #86909c;
  transition: transform 0.2s;
}

.ts-arrow.up {
  transform: rotate(180deg);
}

.ts-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  min-width: 100%;
  margin: 0;
  padding: 4px;
  list-style: none;
  background: #fff;
  border-radius: 8px;
  box-shadow:
    0 4px 10px rgba(31, 35, 41, 0.08),
    0 8px 24px rgba(31, 35, 41, 0.12);
  z-index: 30;
}

.ts-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 10px;
  border-radius: 6px;
  font-size: 13px;
  color: #1f2329;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
}

/* 悬停项：当前页面主题色底 + 白字（替代系统默认蓝） */
.ts-option:hover {
  background: var(--tab-color, #f5319d);
  color: #fff;
}

.ts-option.selected {
  color: var(--tab-color, #f5319d);
  font-weight: 600;
}

.ts-option.selected:hover {
  color: #fff;
}

.ts-check {
  margin-left: auto;
  flex-shrink: 0;
}

/* 展开动画：淡入 + 上移 */
.ts-drop-enter-active,
.ts-drop-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}

.ts-drop-enter-from,
.ts-drop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
