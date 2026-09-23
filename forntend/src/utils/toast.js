import { ref } from 'vue'

// 全站统一轻提示：屏幕正中小胶囊，自动消失（全局单例）
// 用法：showToast('文件超过上限 2MB', 'warn')
export const toast = ref(null)
let toastTimer = null
let toastSeq = 0

/**
 * 显示轻提示
 * @param {string} message 提示文案（优先后端返回的 message）
 * @param {'warn'|'success'|'error'|'info'} type 类型，默认警示橙
 * @param {number} duration 展示时长 ms，默认 3000
 */
export function showToast(message, type = 'warn', duration = 3000) {
  clearTimeout(toastTimer)
  toast.value = { id: ++toastSeq, message, type }
  toastTimer = setTimeout(() => (toast.value = null), duration)
}

// 点击提示胶囊可提前关闭
export function clearToast() {
  clearTimeout(toastTimer)
  toast.value = null
}
