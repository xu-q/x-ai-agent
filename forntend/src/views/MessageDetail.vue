<template>
  <div class="detail">
    <header class="detail-header">
      <h1 class="detail-title">消息详情</h1>
      <span class="conv-id">{{ conversationId }}</span>
      <button class="back-btn" @click="router.push('/admin')">
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
          <div class="msg-meta">
            <span class="msg-role" :class="msg.role === 'user' ? 'role-user' : 'role-ai'">
              {{ msg.role === 'user' ? '用户' : 'AI' }}
            </span>
            <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
          </div>
          <div class="msg-content">{{ msg.content }}</div>
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
import { formatTime } from '../utils/formatTime'

const router = useRouter()
const route = useRoute()

const conversationId = route.params.conversationId

const messages = ref([])
const loading = ref(false)
const error = ref('')

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

/* 回到顶部按钮 */
.back-top {
  position: absolute;
  right: 32px;
  bottom: 40px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid #e5e6eb;
  background: #fff;
  color: #4e5969;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transition: all 0.2s;
  z-index: 10;
}

.back-top:hover {
  color: #165dff;
  border-color: #165dff;
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
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2329;
}

.conv-id {
  font-size: 13px;
  color: #86909c;
  word-break: break-all;
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
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.msg-item {
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-left: 3px solid transparent;
}

.msg-user {
  background: #eaf3ff;
  border-left-color: #165dff;
}

.msg-ai {
  background: #effaf1;
  border-left-color: #00b42a;
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

.msg-time {
  font-size: 12px;
  color: #86909c;
}

.msg-content {
  font-size: 14px;
  color: #1f2329;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
