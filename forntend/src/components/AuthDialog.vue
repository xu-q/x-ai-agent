<template>
  <div class="auth-mask" :data-auth-theme="theme" @click.self="closable && emit('close')">
    <div class="auth-dialog">
      <button v-if="closable" type="button" class="auth-close" title="关闭" @click="emit('close')">✕</button>
      <button v-if="showHomeBtn" type="button" class="auth-home-btn" title="回到首页" @click="router.push('/')">
        <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M6 6l12 12M18 6L6 18"/>
        </svg>
      </button>
      <div class="auth-heart">{{ theme === 'purple' ? '✨' : '♥' }}</div>
      <h2 class="auth-title">欢迎来到 {{ title }}</h2>
      <p class="auth-sub">{{ mode === 'choose' ? '请选择进入方式' : '请输入账号信息' }}</p>

      <template v-if="mode === 'choose'">
        <button class="auth-btn main" @click="mode = 'form'">登录 / 注册</button>
        <button v-if="showGuest" class="auth-btn ghost" :disabled="authLoading" @click="guestLogin">
          {{ authLoading ? '进入中...' : '游客登录' }}
        </button>
      </template>

      <form v-else class="auth-form" @submit.prevent="submitAuth">
        <div class="auth-tabs">
          <button type="button" :class="{ active: authTab === 'login' }" @click="switchTab('login')">登录</button>
          <button type="button" :class="{ active: authTab === 'register' }" @click="switchTab('register')">注册</button>
        </div>
        <input v-model.trim="authName" class="auth-input" placeholder="用户名" />
        <input v-model="authPwd" class="auth-input" type="password" placeholder="密码（至少 6 位）" />
        <label v-if="authTab === 'login'" class="auth-remember">
          <input v-model="rememberMe" type="checkbox" />
          <span>记住账号密码</span>
        </label>
        <input v-if="authTab === 'register'" v-model.trim="authPhone" class="auth-input" placeholder="手机号（选填）" />
        <p v-if="authError" class="auth-error">{{ authError }}</p>
        <p v-if="authSuccess" class="auth-success">{{ authSuccess }}</p>
        <button type="submit" class="auth-btn main" :disabled="authLoading">
          {{ authLoading ? '处理中...' : (authTab === 'login' ? '登录' : '注册并登录') }}
        </button>
        <button v-if="showChoose" type="button" class="auth-link" @click="mode = 'choose'">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
          返回选择
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userLogin, userRegister, saveAuthUser } from '../api/index.js'

const props = defineProps({
  title: { type: String, default: 'AI 应用' },
  // 主题：love 粉色（聊天室）/ purple 紫色（首页），颜色随所在页面背景变化
  theme: { type: String, default: 'love' },
  showChoose: { type: Boolean, default: false },
  showGuest: { type: Boolean, default: false },
  showHomeBtn: { type: Boolean, default: false },
  closable: { type: Boolean, default: false }
})

const emit = defineEmits(['authenticated', 'close'])
const router = useRouter()

const mode = ref(props.showChoose ? 'choose' : 'form')
const authTab = ref('login')
const authName = ref('')
const authPwd = ref('')
const authPhone = ref('')
const authError = ref('')
const authSuccess = ref('')
const authLoading = ref(false)

// 记住账号密码：localStorage 持久化（Base64 简单编码，避免明文直接暴露）
const REMEMBER_KEY = 'chat-remember'
const rememberMe = ref(false)

try {
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved) {
    const { username, password } = JSON.parse(atob(saved))
    if (username && password) {
      authName.value = username
      authPwd.value = password
      rememberMe.value = true
    }
  }
} catch {
  // 记住的数据损坏时忽略，正常显示空表单
}

function switchTab(tab) {
  authTab.value = tab
  authError.value = ''
  authSuccess.value = ''
}

// 统一出口：写存储 + 通知父组件（guest 存在时为游客登录，父组件可展示账号提示）
function finish(u, token, guest) {
  saveAuthUser(u, token)
  authName.value = ''
  authPwd.value = ''
  authPhone.value = ''
  emit('authenticated', { user: u, token, guest })
}

// 游客登录：自动注册 GUEST 角色账号并登录
async function guestLogin() {
  const guestName = `游客${Math.floor(1000 + Math.random() * 9000)}`
  const guestPwd = String(Math.floor(1000 + Math.random() * 9000))
  authLoading.value = true
  try {
    try {
      await userRegister({ username: guestName, password: guestPwd, role: 'GUEST' })
    } catch {
      // 注册失败（如用户名重复）也尝试直接登录，保证能进入
    }
    const res = await userLogin(guestName, guestPwd)
    const { user: u, token } = res.data
    finish({ id: u.id, name: u.username, role: u.role || 'GUEST' }, token, { name: guestName, pwd: guestPwd })
  } catch (err) {
    authError.value = `游客登录失败：${err.response?.data?.message || err.message}`
  } finally {
    authLoading.value = false
  }
}

// 登录 / 注册（注册成功自动登录）
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
      await userRegister({
        username: authName.value,
        password: authPwd.value,
        phone: authPhone.value || undefined
      })
      authSuccess.value = '注册成功，正在登录...'
    }
    const res = await userLogin(authName.value, authPwd.value)
    const { user: u, token } = res.data
    // 登录成功后按勾选状态保存 / 清除记住的账号密码
    if (authTab.value === 'login') {
      if (rememberMe.value) {
        localStorage.setItem(
          REMEMBER_KEY,
          btoa(JSON.stringify({ username: authName.value, password: authPwd.value }))
        )
      } else {
        localStorage.removeItem(REMEMBER_KEY)
      }
    }
    finish({ id: u.id, name: u.username, role: u.role }, token)
  } catch (err) {
    const msg = err.response?.data?.message || err.message || '请求失败'
    authError.value = authTab.value === 'register' ? `注册失败：${msg}` : `登录失败：${msg}`
  } finally {
    authLoading.value = false
  }
}
</script>

<style scoped>
.auth-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(6px);
}

/* ===== 主题变量：颜色随所在页面背景变化 ===== */
.auth-mask[data-auth-theme='love'] {
  --ad-accent: #d98c9b;
  --ad-accent-hover: #cc7d8d;
  --ad-soft: rgba(217, 140, 155, 0.12);
  --ad-error: #d96d7e;
  --ad-card-bg: var(--bg-input-box, #fffdfa);
  --ad-border: var(--border-color, #e7d7d9);
  --ad-text: var(--text-primary, #5c4a4d);
  --ad-text2: var(--text-secondary, #8a7578);
  --ad-input-bg: var(--bg-page, #faf6f5);
  --ad-mask: rgba(60, 40, 45, 0.35);
  --ad-shadow: rgba(90, 60, 65, 0.28);
}

.auth-mask[data-auth-theme='purple'] {
  --ad-accent: #667eea;
  --ad-accent-hover: #5578e8;
  --ad-soft: rgba(102, 126, 234, 0.12);
  --ad-error: #e05c72;
  --ad-card-bg: #fff;
  --ad-border: #e2ddf0;
  --ad-text: #3a3550;
  --ad-text2: #7a7590;
  --ad-input-bg: #f7f6fc;
  --ad-mask: rgba(30, 20, 50, 0.45);
  --ad-shadow: rgba(30, 20, 60, 0.35);
}

.auth-mask {
  background: var(--ad-mask);
}

.auth-dialog {
  position: relative;
  width: 420px;
  max-width: 90%;
  background: var(--ad-card-bg);
  border: 1px solid var(--ad-border);
  border-radius: 20px;
  padding: 44px 40px 32px;
  text-align: center;
  box-shadow: 0 24px 60px var(--ad-shadow);
}

.auth-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--ad-text2);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.auth-close:hover {
  background: var(--ad-soft);
  color: var(--ad-text);
  transform: rotate(90deg);
}

.auth-home-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--ad-text2);
  cursor: pointer;
  transition: all 0.2s;
}

.auth-home-btn:hover {
  background: var(--ad-soft);
  color: var(--ad-text);
  transform: rotate(90deg);
}

.auth-heart {
  font-size: 40px;
  color: var(--ad-accent);
  line-height: 1;
  margin-bottom: 12px;
}

.auth-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--ad-text);
  margin-bottom: 6px;
}

.auth-sub {
  font-size: 13px;
  color: var(--ad-text2);
  margin-bottom: 24px;
}

.auth-btn {
  display: block;
  width: 100%;
  padding: 11px 0;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.auth-btn.main {
  border: none;
  background: var(--ad-accent);
  color: #fff;
  margin-bottom: 12px;
}

.auth-btn.main:hover:not(:disabled) {
  background: var(--ad-accent-hover);
}

.auth-btn.ghost {
  border: 1px solid var(--ad-border);
  background: transparent;
  color: var(--ad-text2);
}

.auth-btn.ghost:hover:not(:disabled) {
  border-color: var(--ad-accent);
  color: var(--ad-accent);
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
  border: 1px solid var(--ad-border);
  border-radius: 8px;
  background: transparent;
  color: var(--ad-text2);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.auth-tabs button.active {
  background: var(--ad-accent);
  border-color: var(--ad-accent);
  color: #fff;
}

.auth-input {
  padding: 10px 12px;
  border: 1px solid var(--ad-border);
  border-radius: 8px;
  font-size: 14px;
  color: var(--ad-text);
  background: var(--ad-input-bg);
  outline: none;
  transition: border-color 0.2s;
}

.auth-input:focus {
  border-color: var(--ad-accent);
}

.auth-remember {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--ad-text2);
  cursor: pointer;
  user-select: none;
  margin: -2px 0;
}

.auth-remember input {
  width: 15px;
  height: 15px;
  accent-color: var(--ad-accent);
  cursor: pointer;
}

.auth-remember:hover {
  color: var(--ad-accent);
}

.auth-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-error {
  font-size: 12px;
  color: var(--ad-error);
  text-align: left;
  margin: 0;
}

.auth-success {
  font-size: 12px;
  color: #4caf50;
  text-align: left;
  margin: 0;
}

.auth-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  align-self: center;
  padding: 6px 16px;
  border: none;
  border-radius: 999px;
  background: var(--ad-soft);
  color: var(--ad-accent);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.auth-link svg {
  display: block;
  transition: transform 0.2s;
}

.auth-link:hover {
  background: var(--ad-accent);
  color: #fff;
}

.auth-link:hover svg {
  transform: translateX(-2px);
}
</style>
