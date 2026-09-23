<template>
  <div class="detail">
    <header class="detail-header">
      <h1 class="detail-title">消息详情</h1>
      <span class="conv-id">{{ conversationId }}</span>
      <button class="back-btn" @click="router.push('/profile')">
        <svg viewBox="0 0 24 24" width="14" height="14">
          <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回列表
      </button>
    </header>
    <main ref="detailMainRef" class="detail-main" @scroll="onScroll">
      <p v-if="loading" class="tip">加载中...</p>
      <p v-else-if="error" class="tip tip-error">{{ error }}</p>
      <p v-else-if="messages.length === 0" class="tip">该会话暂无消息</p>
      <div v-else class="msg-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-item"
          :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'"
        >
          <div class="msg-when">
            <span class="when-date" :class="{ today: isToday(msg.createTime) }">{{ whenDate(msg.createTime) }}</span>
            <span class="when-time">{{ whenTime(msg.createTime) }}</span>
          </div>
          <div class="msg-node-col">
            <span class="msg-node"></span>
          </div>
          <div class="msg-body">
            <div class="msg-meta">
              <span class="msg-role" :class="msg.role === 'user' ? 'role-user' : 'role-ai'">
                {{ msg.role === 'user' ? '用户' : 'AI' }}
              </span>
            </div>
            <div class="msg-content" v-html="renderContent(msg.content)"></div>
          </div>
        </div>
      </div>
    </main>
    <transition name="fade">
      <button v-if="showBackTop" class="back-top" title="回到顶部" @click="scrollTop">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path d="M12 5l-7 7 1.4 1.4L11 8.8V19h2V8.8l4.6 4.6L19 12z" fill="currentColor"/>
        </svg>
      </button>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { listMessages } from '../api'

const router = useRouter()
const route = useRoute()

const conversationId = route.params.conversationId

const messages = ref([])
const loading = ref(false)
const error = ref('')

// ===== 轻量 Markdown 渲染：先转义防注入，再还原加粗/行内代码/列表/段落 =====
function escapeHtml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function inline(s) {
  return s
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

// 列表项内不做加粗强调，星号直接剥离为普通文字
function stripBold(s) {
  return s.replace(/\*\*([^*]+)\*\*/g, '$1').replace(/`([^`]+)`/g, '$1')
}

function renderContent(raw) {
  const lines = escapeHtml(raw).split(/\r?\n/)
  const out = []
  let list = null // 'ul' | 'ol'
  const closeList = () => {
    if (list) {
      out.push(`</${list}>`)
      list = null
    }
  }
  for (const line of lines) {
    const t = line.trim()
    if (!t) {
      closeList()
      continue
    }
    const ul = t.match(/^[-*]\s+(.*)$/)
    const ol = t.match(/^\d+[.、]\s+(.*)$/)
    if (ul) {
      if (list !== 'ul') {
        closeList()
        out.push('<ul>')
        list = 'ul'
      }
      out.push(`<li>${stripBold(ul[1])}</li>`)
    } else if (ol) {
      if (list !== 'ol') {
        closeList()
        out.push('<ol>')
        list = 'ol'
      }
      out.push(`<li>${stripBold(ol[1])}</li>`)
    } else {
      closeList()
      out.push(`<p>${inline(t)}</p>`)
    }
  }
  closeList()
  return out.join('')
}

// ===== 时间轴左列：日期（今日红）+ 精确到秒的时间 =====
const pad2 = (n) => String(n).padStart(2, '0')

function isToday(t) {
  return new Date(t).toDateString() === new Date().toDateString()
}

function whenDate(t) {
  const d = new Date(t)
  return isToday(t) ? '今日' : `${pad2(d.getMonth() + 1)}/${pad2(d.getDate())}`
}

function whenTime(t) {
  const d = new Date(t)
  return `${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`
}

// 回到顶部
const detailMainRef = ref(null)
const showBackTop = ref(false)

function onScroll() {
  const el = detailMainRef.value
  showBackTop.value = !!el && el.scrollTop > 200
}

function scrollTop() {
  detailMainRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await listMessages(conversationId)
    messages.value = res.data || []
  } catch {
    error.value = '加载消息失败，请确认后端服务已启动'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f6f7;
  position: relative;
}

/* 品牌氛围层：紫色光晕 + 噪点颗粒（与 Profile 页一致） */
.detail::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  background:
    radial-gradient(ellipse 42% 34% at 88% -6%, rgba(114, 46, 209, 0.09), transparent 62%),
    radial-gradient(ellipse 36% 30% at -4% 104%, rgba(22, 93, 255, 0.05), transparent 60%),
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='140' height='140'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2'/%3E%3C/filter%3E%3Crect width='140' height='140' filter='url(%23n)' opacity='0.028'/%3E%3C/svg%3E");
}

/* 内容浮在氛围层之上 */
.detail > * {
  position: relative;
  z-index: 1;
}

/* 回到顶部按钮：液态玻璃 */
.back-top {
  position: absolute;
  right: 32px;
  bottom: 40px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  color: #4e5969;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.6),
    0 4px 12px rgba(0, 0, 0, 0.12);
  transition: all 0.2s;
  z-index: 10;
}

.back-top:hover {
  color: #165dff;
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  border-bottom: 1px solid rgba(229, 230, 235, 0.8);
  animation: headIn 0.45s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}

@keyframes headIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 600;
  color: #1f2329;
  letter-spacing: 1px;
}

.conv-id {
  font-size: 13px;
  color: #86909c;
  word-break: break-all;
  font-family: var(--font-num);
}

.back-btn {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  background: #fff;
  color: #4e5969;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.back-btn svg {
  transition: transform 0.2s;
}

.back-btn:hover svg {
  transform: translateX(-2px);
}

.back-btn:hover {
  color: #165dff;
  border-color: #165dff;
}

.detail-main {
  flex: 1;
  overflow: auto;
  padding: 24px;
}

.tip {
  text-align: center;
  color: #86909c;
  padding: 32px 0;
  font-size: 14px;
}

.tip-error {
  color: #f53f3f;
}

.msg-list {
  position: relative;
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.msg-item {
  display: flex;
  gap: 12px;
  animation: rowIn 0.35s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}

@keyframes rowIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-item:nth-child(1) { animation-delay: 0.05s; }
.msg-item:nth-child(2) { animation-delay: 0.09s; }
.msg-item:nth-child(3) { animation-delay: 0.13s; }
.msg-item:nth-child(4) { animation-delay: 0.17s; }
.msg-item:nth-child(5) { animation-delay: 0.21s; }
.msg-item:nth-child(6) { animation-delay: 0.25s; }
.msg-item:nth-child(n + 7) { animation-delay: 0.29s; }

/* 左侧时间列：日期在上（今日红）、时间在下（等宽） */
.msg-when {
  width: 58px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  padding-top: 1px;
}

.when-date {
  font-size: 13px;
  font-weight: 700;
  color: #86909c;
  white-space: nowrap;
}

.when-date.today {
  color: #f53f3f;
}

.when-time {
  font-size: 12px;
  color: #86909c;
  font-family: var(--font-num);
  white-space: nowrap;
}

/* 节点列：竖线贯穿，首尾裁剪 */
.msg-node-col {
  position: relative;
  width: 14px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}

.msg-node-col::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  border-radius: 1px;
  background: #e5e6eb;
}

.msg-item:first-child .msg-node-col::before {
  top: 6px;
}

.msg-item:last-child .msg-node-col::before {
  bottom: auto;
  height: 6px;
}

/* 时间线节点：用户蓝环 / AI 绿环（样式对齐系统通知页） */
.msg-node {
  position: relative;
  z-index: 1;
  width: 12px;
  height: 12px;
  margin-top: 5px;
  border-radius: 50%;
  box-sizing: border-box;
  background: #fff;
  border: 3px solid #165dff;
  box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.12);
}

.msg-ai .msg-node {
  border-color: #00b42a;
  box-shadow: 0 0 0 3px rgba(0, 180, 42, 0.12);
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.msg-role {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.role-user {
  color: #165dff;
  background: rgba(22, 93, 255, 0.1);
}

.role-ai {
  color: #00b42a;
  background: rgba(0, 180, 42, 0.1);
}

.msg-content {
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow:
    inset 0 0 0 1px rgba(31, 35, 41, 0.04),
    0 1px 4px rgba(0, 0, 0, 0.06);
  font-size: 14px;
  color: #1f2329;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-user .msg-content {
  background: #eaf3ff;
}

.msg-ai .msg-content {
  background: #effaf1;
}

/* Markdown 渲染元素 */
.msg-content :deep(p) {
  margin: 0 0 8px;
}

.msg-content :deep(p:last-child) {
  margin-bottom: 0;
}

.msg-content :deep(ul),
.msg-content :deep(ol) {
  margin: 0 0 8px;
  padding-left: 22px;
}

.msg-content :deep(li) {
  margin: 3px 0;
}

.msg-content :deep(strong) {
  font-weight: 600;
}

.msg-content :deep(code) {
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(31, 35, 41, 0.06);
  font-family: var(--font-num);
  font-size: 13px;
}
</style>
