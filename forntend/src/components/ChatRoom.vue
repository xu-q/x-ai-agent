<template>
  <div class="chat-room" :data-theme="theme">
    <!-- 登录选择弹层 -->
    <div v-if="!user" class="auth-mask">
      <div class="auth-dialog">
        <div class="auth-heart">♥</div>
        <h2 class="auth-title">欢迎来到 {{ title }}</h2>
        <p class="auth-sub">请选择进入方式</p>
        <template v-if="authMode === 'choose'">
          <button class="auth-btn auth-btn-main" @click="authMode = 'login'">登录 / 注册</button>
          <button class="auth-btn auth-btn-ghost" @click="guestLogin">游客登录</button>
          <button type="button" class="auth-link" @click="$router.push('/')">🏠 回到首页</button>
        </template>
        <form v-else class="auth-form" @submit.prevent="submitAuth">
          <div class="auth-tabs">
            <button type="button" :class="{ active: authTab === 'login' }" @click="switchAuthTab('login')">登录</button>
            <button type="button" :class="{ active: authTab === 'register' }" @click="switchAuthTab('register')">注册</button>
          </div>
          <input v-model.trim="authName" class="auth-input" placeholder="用户名" />
          <input v-model="authPwd" class="auth-input" type="password" placeholder="密码（至少 6 位）" />
          <template v-if="authTab === 'register'">
            <input v-model.trim="authPhone" class="auth-input" placeholder="手机号（选填）" />
          </template>
          <p v-if="authError" class="auth-error">{{ authError }}</p>
          <p v-if="authSuccess" class="auth-success">{{ authSuccess }}</p>
          <button type="submit" class="auth-btn auth-btn-main" :disabled="authLoading">
            {{ authLoading ? '处理中...' : (authTab === 'login' ? '登录' : '注册并登录') }}
          </button>
          <button type="button" class="auth-link" @click="authMode = 'choose'">← 返回选择</button>
          <button type="button" class="auth-link" @click="$router.push('/')">🏠 回到首页</button>
        </form>
      </div>
    </div>

    <!-- 顶部栏（微信风格） -->
    <header class="chat-header">
      <span class="back-btn" @click="$router.push('/')">
        <svg viewBox="0 0 24 24" width="22" height="22">
          <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </span>
      <span class="chat-title">{{ title }}</span>
      <span v-if="user" class="chat-user" :title="`已登录（${user.role || 'USER'}）`">
        {{ user.role === 'GUEST' ? '👻' : '👤' }} {{ user.name }}
        <button class="logout-btn" @click="doLogout">
          <svg viewBox="0 0 24 24" width="12" height="12">
            <path d="M15 12H4m4-4l-4 4 4 4m4-11h5a2 2 0 012 2v10a2 2 0 01-2 2h-5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          退出
        </button>
      </span>
      <span class="chat-id" :title="chatId">会话ID: {{ displayChatId }}</span>
      <button class="theme-btn" :title="nextThemeTitle" @click="toggleTheme">
        <!-- 白天模式：点击切到黑夜（月亮） -->
        <svg v-if="theme === 'light'" viewBox="0 0 24 24" width="20" height="20">
          <path d="M12 3a9 9 0 109 9c0-.46-.04-.92-.1-1.36a5.39 5.39 0 01-4.4 2.26 5.4 5.4 0 01-3.14-9.8c-.44-.06-.9-.1-1.36-.1z" fill="currentColor"/>
        </svg>
        <!-- 黑夜模式：点击切到恋爱模式（爱心） -->
        <svg v-else-if="theme === 'dark'" viewBox="0 0 24 24" width="20" height="20">
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="currentColor"/>
        </svg>
        <!-- 恋爱模式：点击切回白天（太阳） -->
        <svg v-else viewBox="0 0 24 24" width="20" height="20">
          <circle cx="12" cy="12" r="4" fill="currentColor"/>
          <g stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="12" y1="2" x2="12" y2="4"/>
            <line x1="12" y1="20" x2="12" y2="22"/>
            <line x1="2" y1="12" x2="4" y2="12"/>
            <line x1="20" y1="12" x2="22" y2="12"/>
            <line x1="4.93" y1="4.93" x2="6.34" y2="6.34"/>
            <line x1="17.66" y1="17.66" x2="19.07" y2="19.07"/>
            <line x1="4.93" y1="19.07" x2="6.34" y2="17.66"/>
            <line x1="17.66" y1="6.34" x2="19.07" y2="4.93"/>
          </g>
        </svg>
      </button>
    </header>

    <!-- 聊天记录区 -->
    <div ref="messageListRef" class="message-list">
      <template v-for="(msg, index) in messages" :key="msg.id">
        <!-- 时间分隔符 -->
        <div v-if="shouldShowTime(messages, index)" class="time-divider">
          {{ formatTime(msg.time) }}
        </div>

        <!-- 系统提示（欢迎语） -->
        <div v-if="msg.role === 'system'" class="system-tip">{{ msg.content }}</div>

        <!-- 消息行 -->
        <div
          v-else
          class="message-row"
          :class="msg.role === 'user' ? 'message-user' : 'message-ai'"
        >
          <div class="avatar" :class="msg.role === 'user' ? 'avatar-user' : 'avatar-ai'" :style="msg.role === 'ai' ? { background: aiGradient } : null">
            <span v-if="msg.role === 'ai'">{{ aiIcon }}</span>
            <svg v-else viewBox="0 0 24 24" width="20" height="20">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" fill="currentColor"/>
            </svg>
          </div>
          <div class="bubble-wrap">
            <div class="bubble" :class="msg.role === 'user' ? 'bubble-user' : 'bubble-ai'">
              <span v-if="msg.content">{{ msg.content }}</span>
              <span v-if="msg.loading && !msg.content" class="typing-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
              <span v-if="msg.loading && msg.content" class="cursor-blink">▋</span>
            </div>
            <div class="msg-time" :class="msg.role === 'user' ? 'msg-time-user' : 'msg-time-ai'">
              {{ formatTime(msg.time) }}
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 输入区（微信风格） -->
    <div class="input-area">
      <div class="resize-handle" @mousedown="startResize" title="拖拽调整高度"></div>
      <div class="input-row">
        <textarea
          v-model="inputText"
          class="input-box"
          :placeholder="loading ? 'AI 正在回复...' : '输入消息，Enter 发送，Shift+Enter 换行'"
          :disabled="loading"
          rows="1"
          ref="inputRef"
          @keydown.enter.exact.prevent="handleSend"
          @input="autoResize"
        ></textarea>
        <button
          class="send-btn"
          :class="{ 'send-btn-active': inputText.trim() && !loading }"
          :disabled="!inputText.trim() || loading"
          @click="handleSend"
        >
          {{ loading ? '回复中' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userLogin, userRegister, userLogout, saveAuthUser, clearAuth } from '../api/index.js'

const router = useRouter()

const props = defineProps({
  title: { type: String, default: 'AI 聊天' },
  chatId: { type: String, default: '' },
  aiIcon: { type: String, default: 'AI' },
  aiGradient: { type: String, default: 'linear-gradient(135deg, #667eea, #764ba2)' },
  welcome: { type: String, default: '' }
})

const emit = defineEmits(['send'])

const inputText = ref('')
const loading = ref(false)
const messages = ref([])
const messageListRef = ref(null)
const inputRef = ref(null)
const userResized = ref(false)
let msgIdCounter = 0

// 登录身份：null 未登录；{ id, name, role }，存 sessionStorage + 会话 Cookie（刷新不丢、关浏览器清除）
const user = ref(null)
try {
  user.value = JSON.parse(sessionStorage.getItem('chat-user') || 'null')
} catch {
  user.value = null
}

const authMode = ref('choose') // choose | form
const authTab = ref('login') // login | register
const authName = ref('')
const authPwd = ref('')
const authPhone = ref('')
const authError = ref('')
const authSuccess = ref('')
const authLoading = ref(false)

function switchAuthTab(tab) {
  authTab.value = tab
  authError.value = ''
  authSuccess.value = ''
}

function saveUser(u, token) {
  user.value = u
  saveAuthUser(u, token)
  // 切换身份时清空之前用户的聊天记录（含系统提示）
  messages.value = []
  messages.value.push({
    id: ++msgIdCounter,
    role: 'system',
    content: `${u.name} ${u.role === 'GUEST' ? '以游客身份' : ''}进入了聊天室`,
    time: Date.now()
  })
}

// 游客登录：调用后端注册接口自动注册 GUEST 角色账号，然后登录
async function guestLogin() {
  const guestName = `游客${Math.floor(1000 + Math.random() * 9000)}`
  const guestPwd = String(Math.floor(1000 + Math.random() * 9000))
  try {
    await userRegister({ username: guestName, password: guestPwd, role: 'GUEST' })
  } catch {
    // 注册失败（如用户名重复）也尝试直接登录，保证能进入
  }
  try {
    const res = await userLogin(guestName, guestPwd)
    const { user: u, token } = res.data
    saveUser({ id: u.id, name: u.username, role: u.role || 'GUEST' }, token)
    // 提示游客账号密码，方便后续用正式账号登录（复制到剪贴板）
    const tip = `🧾 您的游客账号：${guestName} / ${guestPwd}（关闭浏览器后失效）`
    messages.value.push({ id: ++msgIdCounter, role: 'system', content: tip, time: Date.now() })
    try { await navigator.clipboard.writeText(`${guestName} / ${guestPwd}`) } catch {}
  } catch (err) {
    authError.value = `游客登录失败：${err.response?.data?.message || err.message}`
  }
}

// 真实登录/注册调用
async function submitAuth() {
  authError.value = ''
  authSuccess.value = ''
  if (!authName.value || !authPwd.value) {
    authError.value = '请输入用户名和密码'
    return
  }
  if (authPwd.value.length < 6) {
    authError.value = '密码至少 6 位'
    return
  }
  authLoading.value = true
  try {
    if (authTab.value === 'register') {
      // 1. 先注册
      await userRegister({
        username: authName.value,
        password: authPwd.value,
        phone: authPhone.value || undefined
      })
      authSuccess.value = '注册成功，正在登录...'
    }
    // 2. 登录（注册后或直接登录都走这里）
    const res = await userLogin(authName.value, authPwd.value)
    const { user: u, token } = res.data
    saveUser({ id: u.id, name: u.username, role: u.role }, token)
    // 重置表单
    authName.value = ''
    authPwd.value = ''
    authPhone.value = ''
    authMode.value = 'choose'
  } catch (err) {
    const msg = err.response?.data?.message || err.message || '请求失败'
    authError.value = authTab.value === 'register' ? `注册失败：${msg}` : `登录失败：${msg}`
  } finally {
    authLoading.value = false
  }
}

async function doLogout() {
  try {
    await userLogout()
  } catch {
    // 后端登出失败不影响本地退出
  }
  clearAuth()
  user.value = null
  messages.value = []
  authMode.value = 'choose'
  router.push('/')
}

// 聊天背景主题：light（白天）/ dark（黑夜）/ love（恋爱，默认），从 localStorage 读取记忆
const themeOptions = ['light', 'dark', 'love']
const theme = ref(localStorage.getItem('chat-theme') || 'love')
if (!themeOptions.includes(theme.value)) theme.value = 'light'

const nextThemeTitle = computed(() => {
  if (theme.value === 'light') return '切换到黑夜模式'
  if (theme.value === 'dark') return '切换到恋爱模式'
  return '切换到白天模式'
})

function toggleTheme() {
  const idx = themeOptions.indexOf(theme.value)
  theme.value = themeOptions[(idx + 1) % themeOptions.length]
  localStorage.setItem('chat-theme', theme.value)
}

// 顶栏展示完整会话 ID（超宽时 CSS 自动省略，悬停可看全）
const displayChatId = computed(() => props.chatId || '')

onMounted(() => {
  // 进入页面先显示当前会话 ID
  if (props.chatId) {
    messages.value.push({ id: ++msgIdCounter, role: 'system', content: `当前会话 ID：${props.chatId}`, time: Date.now() })
  }
  // 显示欢迎语
  if (props.welcome) {
    messages.value.push({ id: ++msgIdCounter, role: 'system', content: props.welcome, time: Date.now() })
  }
  nextTick(() => inputRef.value?.focus())
})

/**
 * 是否显示时间分隔（首条消息，或与上一条间隔超过 3 分钟）
 */
function shouldShowTime(list, index) {
  const msg = list[index]
  if (msg.role === 'system') return false
  if (index === 0) return true
  const prev = list[index - 1]
  if (prev.role === 'system') return true
  return msg.time - prev.time > 3 * 60 * 1000
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const isToday = d.toDateString() === now.toDateString()
  if (isToday) return `${hh}:${mm}`
  return `${d.getMonth() + 1}/${d.getDate()} ${hh}:${mm}`
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

function autoResize() {
  if (userResized.value) return
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 96) + 'px'
}

function startResize(e) {
  e.preventDefault()
  userResized.value = true
  const el = inputRef.value
  if (!el) return
  const startY = e.clientY
  const startHeight = el.getBoundingClientRect().height
  const MIN_H = 36

  function onMove(ev) {
    const delta = startY - ev.clientY
    const newH = Math.max(MIN_H, startHeight + delta)
    el.style.height = newH + 'px'
  }
  function onUp() {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }
  document.body.style.cursor = 'ns-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 用户消息（右侧）
  messages.value.push({ id: ++msgIdCounter, role: 'user', content: text, time: Date.now() })
  inputText.value = ''
  nextTick(() => {
    if (inputRef.value && !userResized.value) inputRef.value.style.height = 'auto'
  })
  scrollToBottom()

  // AI 消息占位（左侧）
  messages.value.push({ id: ++msgIdCounter, role: 'ai', content: '', loading: true, time: Date.now() })
  scrollToBottom()

  loading.value = true
  emit('send', text)
}

/**
 * 追加 AI 回复内容
 */
function appendAiChunk(chunk) {
  const lastAi = [...messages.value].reverse().find((m) => m.role === 'ai')
  if (lastAi) {
    lastAi.content += chunk
    scrollToBottom()
  }
}

/**
 * 标记 AI 消息完成
 */
function finishAiMessage() {
  const lastAi = [...messages.value].reverse().find((m) => m.role === 'ai')
  if (lastAi) {
    lastAi.loading = false
  }
  loading.value = false
  nextTick(() => inputRef.value?.focus())
}

/**
 * 错误展示
 */
function showAiError(errMsg) {
  const lastAi = [...messages.value].reverse().find((m) => m.role === 'ai')
  if (lastAi) {
    lastAi.content = lastAi.content || `发生错误：${errMsg}`
    lastAi.loading = false
  }
  loading.value = false
}

defineExpose({ appendAiChunk, finishAiMessage, showAiError })
</script>

<style scoped>
/* ===== 主题变量：白天 ===== */
.chat-room {
  --bg-page: #ededed;
  --bg-header: #ededed;
  --bg-input: #f7f7f7;
  --bg-input-box: #fff;
  --bg-divider: rgba(0, 0, 0, 0.08);
  --bg-system: rgba(0, 0, 0, 0.06);
  --bg-chat-id: #fff;
  --border-color: #dcdcdc;
  --text-primary: #181818;
  --text-secondary: #888;
  --text-time: #999;
  --icon-color: #666;
  --hover-bg: #dcdcdc;
  --btn-disabled-bg: #e0e0e0;
  --btn-disabled-text: #888;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
  transition: background 0.3s, color 0.3s;
}

/* ===== 主题变量：黑夜 ===== */
.chat-room[data-theme='dark'] {
  --bg-page: #1a1a1a;
  --bg-header: #1f1f1f;
  --bg-input: #1f1f1f;
  --bg-input-box: #2a2a2a;
  --bg-divider: rgba(255, 255, 255, 0.1);
  --bg-system: rgba(255, 255, 255, 0.08);
  --bg-chat-id: #2a2a2a;
  --border-color: #2e2e2e;
  --text-primary: #e6e6e6;
  --text-secondary: #888;
  --text-time: #777;
  --icon-color: #aaa;
  --hover-bg: #2e2e2e;
  --btn-disabled-bg: #2a2a2a;
  --btn-disabled-text: #666;
}

/* ===== 主题变量：恋爱（柔和莫兰迪粉） ===== */
.chat-room[data-theme='love'] {
  --bg-page: #faf4f2;
  --bg-header: #f2e6e7;
  --bg-input: #f6ecec;
  --bg-input-box: #fffdfa;
  --bg-divider: rgba(185, 150, 155, 0.18);
  --bg-system: rgba(185, 150, 155, 0.12);
  --bg-chat-id: #fffdfa;
  --border-color: #e7d7d9;
  --text-primary: #5c4a4d;
  --text-secondary: #a89598;
  --text-time: #b7a8ab;
  --icon-color: #c49aa2;
  --hover-bg: #efe1e3;
  --btn-disabled-bg: #efe1e3;
  --btn-disabled-text: #b7a8ab;
}

/* ===== 登录选择弹层 ===== */
.auth-mask {
  position: absolute;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(60, 40, 45, 0.35);
  backdrop-filter: blur(6px);
}

.chat-room {
  position: relative;
}

.auth-dialog {
  width: 320px;
  max-width: 88%;
  background: var(--bg-input-box, #fffdfa);
  border: 1px solid var(--border-color, #e7d7d9);
  border-radius: 16px;
  padding: 32px 28px 24px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(90, 60, 65, 0.25);
}

.auth-heart {
  font-size: 40px;
  color: #d98c9b;
  line-height: 1;
  margin-bottom: 12px;
}

.auth-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.auth-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.auth-btn {
  display: block;
  width: 100%;
  padding: 11px 0;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.auth-btn-main {
  border: none;
  background: #d98c9b;
  color: #fff;
  margin-bottom: 12px;
}

.auth-btn-main:hover {
  background: #cc7d8d;
}

.auth-btn-ghost {
  border: 1px solid var(--border-color);
  background: transparent;
  color: var(--text-secondary);
}

.auth-btn-ghost:hover {
  border-color: #d9a3af;
  color: #c98a97;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.auth-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
}

.auth-tabs button {
  flex: 1;
  padding: 8px 0;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.auth-tabs button.active {
  background: #d98c9b;
  border-color: #d98c9b;
  color: #fff;
}

.auth-input {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-page);
  outline: none;
  transition: border-color 0.2s;
}

.auth-input:focus {
  border-color: #d9a3af;
}

.auth-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-error {
  font-size: 12px;
  color: #d96d7e;
  text-align: left;
}

.auth-success {
  font-size: 12px;
  color: #4caf50;
  text-align: left;
}

.auth-link {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
}

.auth-link:hover {
  color: #c98a97;
}

/* 头部身份标签 */
.chat-user {
  font-size: 11px;
  color: var(--text-secondary);
  background: var(--bg-chat-id);
  padding: 3px 6px 3px 8px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  border: 1px solid #d96d7e;
  background: #fff;
  color: #d96d7e;
  cursor: pointer;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  padding: 3px 8px;
  border-radius: 999px;
  margin-left: 2px;
  transition: all 0.2s;
}

.logout-btn:hover {
  color: #fff;
  background: #d96d7e;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(217, 109, 126, 0.35);
}

/* ===== 顶部栏（微信风格） ===== */
.chat-header {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  background: var(--bg-header);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  gap: 8px;
  transition: background 0.3s, border-color 0.3s;
}

.back-btn {
  display: flex;
  align-items: center;
  color: var(--text-primary);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: background 0.15s;
}

.back-btn:hover {
  background: var(--hover-bg);
}

.chat-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.chat-id {
  margin-left: auto;
  font-size: 11px;
  color: var(--text-secondary);
  background: var(--bg-chat-id);
  padding: 3px 8px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  white-space: nowrap;
}

.theme-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  color: var(--icon-color);
  flex-shrink: 0;
  transition: background 0.15s, color 0.3s;
}

.theme-btn:hover {
  background: var(--hover-bg);
}

/* ===== 消息列表 ===== */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px 20px;
  background: var(--bg-page);
  transition: background 0.3s;
}

.time-divider {
  text-align: center;
  font-size: 11px;
  color: var(--text-time);
  margin: 12px 0;
  background: var(--bg-divider);
  border-radius: 4px;
  padding: 2px 8px;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.system-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-time);
  margin: 12px auto;
  background: var(--bg-system);
  border-radius: 6px;
  padding: 6px 14px;
  width: fit-content;
  max-width: 80%;
  line-height: 1.5;
}

/* ===== 消息行 ===== */
.message-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
  gap: 8px;
  animation: msgIn 0.25s ease;
}

@keyframes msgIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-user {
  flex-direction: row-reverse;
}

/* ===== 头像（微信圆角方形） ===== */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
}

.avatar-user {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: #fff;
}

.avatar-ai {
  font-size: 20px;
}

/* ===== 气泡 ===== */
.bubble-wrap {
  max-width: 70%;
  position: relative;
  display: flex;
  flex-direction: column;
}

.msg-time {
  font-size: 11px;
  color: var(--text-time);
  margin-top: 4px;
  line-height: 1;
}

.msg-time-ai {
  margin-left: 2px;
  align-self: flex-start;
}

.msg-time-user {
  margin-right: 2px;
  align-self: flex-end;
}

.bubble {
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
  min-height: 20px;
}

/* AI 气泡：微信绿 + 左侧小尾巴（与用户气泡保持一致） */
.bubble-ai {
  background: #95ec69;
  color: #181818;
}

.bubble-ai::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 14px;
  border-width: 6px 7px 6px 0;
  border-style: solid;
  border-color: transparent #95ec69 transparent transparent;
}

/* 用户气泡：微信绿 + 右侧小尾巴 */
.bubble-user {
  background: #95ec69;
  color: #181818;
}

.bubble-user::before {
  content: '';
  position: absolute;
  right: -6px;
  top: 14px;
  border-width: 6px 0 6px 7px;
  border-style: solid;
  border-color: transparent transparent transparent #95ec69;
}

/* 黑夜模式下气泡颜色加深 */
.chat-room[data-theme='dark'] .bubble-ai,
.chat-room[data-theme='dark'] .bubble-user {
  background: #2b5e1e;
  color: #e6e6e6;
}

.chat-room[data-theme='dark'] .bubble-ai::before {
  border-color: transparent #2b5e1e transparent transparent;
}

.chat-room[data-theme='dark'] .bubble-user::before {
  border-color: transparent transparent transparent #2b5e1e;
}

/* 恋爱模式：柔和粉色系气泡与头像 */
.chat-room[data-theme='love'] .bubble-user {
  background: #f3d5da;
  color: #5c4449;
}

.chat-room[data-theme='love'] .bubble-user::before {
  border-color: transparent transparent transparent #f3d5da;
}

.chat-room[data-theme='love'] .bubble-ai {
  background: #fffdfa;
  color: #5c4a4d;
  box-shadow: inset 0 0 0 1px #ecdcdf;
}

.chat-room[data-theme='love'] .bubble-ai::before {
  border-color: transparent #fffdfa transparent transparent;
  filter: drop-shadow(-1px 0 0 #ecdcdf);
}

.chat-room[data-theme='love'] .avatar-user {
  background: linear-gradient(135deg, #eec3c9, #e3ccde);
}

.chat-room[data-theme='love'] .input-box:focus {
  border-color: #d9a3af;
}

.chat-room[data-theme='love'] .send-btn-active {
  background: #d98c9b;
  color: #fff;
}

.chat-room[data-theme='love'] .send-btn-active:hover {
  background: #cc7d8d;
}

.chat-room[data-theme='love'] .theme-btn {
  color: #c98a97;
}

/* ===== 打字指示器（三点跳动） ===== */
.typing-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 20px;
}

.typing-indicator .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #bbb;
  animation: typingBounce 1.2s infinite ease-in-out;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-indicator .dot:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typingBounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

/* 光标闪烁 */
.cursor-blink {
  display: inline-block;
  color: #999;
  animation: cursorBlink 1s infinite;
  margin-left: 2px;
}

@keyframes cursorBlink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

/* ===== 输入区（微信风格） ===== */
.input-area {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 0;
  background: var(--bg-input);
  border-top: 1px solid var(--border-color);
  flex-shrink: 0;
  transition: background 0.3s, border-color 0.3s;
}

.resize-handle {
  height: 5px;
  cursor: ns-resize;
  background: transparent;
  transition: background 0.15s;
  flex-shrink: 0;
}

.resize-handle:hover {
  background: var(--border-color);
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 8px 12px;
}

.input-box {
  flex: 1;
  resize: none;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 15px;
  line-height: 1.5;
  min-height: 36px;
  background: var(--bg-input-box);
  color: var(--text-primary);
  transition: border-color 0.2s, background 0.3s, color 0.3s;
}

.input-box:focus {
  border-color: #07c160;
  outline: none;
}

.input-box:disabled {
  background: var(--bg-input-box);
  cursor: not-allowed;
  opacity: 0.7;
}

.input-box::placeholder {
  color: var(--text-secondary);
}

.send-btn {
  padding: 0 16px;
  height: 36px;
  border-radius: 6px;
  background: var(--btn-disabled-bg);
  color: var(--btn-disabled-text);
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
  transition: all 0.2s;
}

.send-btn-active {
  background: #07c160;
  color: #fff;
}

.send-btn:disabled {
  cursor: not-allowed;
}

.send-btn-active:hover {
  background: #06ad56;
}
</style>
