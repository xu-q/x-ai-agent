<template>
  <transition name="center-toast">
    <div v-if="toast" :key="toast.id" class="center-toast" :class="`t-${toast.type}`" @click="clearToast">
      <!-- 警示（橙） -->
      <svg v-if="toast.type === 'warn'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
        <line x1="12" y1="9" x2="12" y2="13"/>
        <line x1="12" y1="17" x2="12.01" y2="17"/>
      </svg>
      <!-- 成功（绿） -->
      <svg v-else-if="toast.type === 'success'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <path d="M8 12l3 3 5-6"/>
      </svg>
      <!-- 错误（红） -->
      <svg v-else-if="toast.type === 'error'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <!-- 信息（灰） -->
      <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="11" x2="12" y2="16"/>
        <line x1="12" y1="8" x2="12.01" y2="8"/>
      </svg>
      {{ toast.message }}
    </div>
  </transition>
</template>

<script setup>
import { toast, clearToast } from '../utils/toast'
</script>

<style scoped>
/* 屏幕正中小胶囊：无遮罩不打扰，3 秒自动消失，点击可提前关闭 */
.center-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 200;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px) saturate(150%);
  border: 1px solid rgba(31, 35, 41, 0.08);
  box-shadow: 0 12px 40px rgba(31, 35, 41, 0.2);
  color: #1f2329;
  font-size: 14px;
  white-space: nowrap;
  cursor: pointer;
}

.center-toast svg {
  flex: none;
}

.t-warn svg {
  color: #ff7d00;
}

.t-success svg {
  color: #00b42a;
}

.t-error svg {
  color: #f53f3f;
}

.t-info svg {
  color: #4e5969;
}

.center-toast-enter-active {
  animation: centerToastIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.center-toast-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.center-toast-enter-from {
  opacity: 0;
}

.center-toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.92);
}

@keyframes centerToastIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.88);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}
</style>
