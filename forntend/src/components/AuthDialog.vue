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
      <p class="auth-sub">{{ mode === 'choose' ? '请选择进入方式' : (qrMode ? '支付宝扫码登录' : '请输入账号信息') }}</p>

      <template v-if="mode === 'choose'">
        <button class="auth-btn main" @click="mode = 'form'">登录 / 注册</button>
        <button v-if="showGuest" class="auth-btn ghost" :disabled="authLoading" @click="guestLogin">
          {{ authLoading ? '进入中...' : '游客登录' }}
        </button>
      </template>

      <!-- 内嵌支付宝扫码面板：二维码由后端渲染，PC 轮询扫码状态 -->
      <div v-else-if="qrMode" class="auth-qr">
        <div class="qr-box">
          <img v-if="qrImage" :src="qrImage" alt="支付宝登录二维码" />
          <div v-if="qrLoading && !qrImage" class="qr-mask">二维码生成中...</div>
          <div v-if="qrExpired" class="qr-expired" @click="alipayLogin">二维码已过期<br />点击刷新</div>
        </div>
        <p class="qr-tip">请使用手机支付宝扫一扫登录<br /><span>扫码后请在手机上确认授权</span></p>
        <p v-if="authError" class="auth-error">{{ authError }}</p>
        <button type="button" class="auth-link" @click="exitQr">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
          返回账号登录
        </button>
      </div>

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
        <div class="auth-social">
          <div class="social-divider"><span>其他登录方式</span></div>
          <div class="social-btns">
            <button
              v-for="s in socialList"
              :key="s.key"
              type="button"
              class="social-btn"
              :class="s.key"
              :disabled="qrLoading"
              :title="socialTitle(s)"
              @click="socialLogin(s.key)"
            >
              <svg v-if="s.key === 'wechat'" viewBox="0 0 24 24" width="22" height="22">
                <path d="M9.3 4.2c-3.8 0-6.9 2.5-6.9 5.7 0 1.8 1 3.4 2.5 4.5l-.6 2 2.2-1.1c.9.3 1.8.4 2.8.4-.2-.5-.3-1-.3-1.5 0-3 2.9-5.4 6.5-5.4h.4c-.6-2.6-3.3-4.6-6.6-4.6z" fill="currentColor"/>
                <path d="M21 14.2c0-2.5-2.6-4.6-5.7-4.6s-5.7 2.1-5.7 4.6 2.6 4.6 5.7 4.6c.6 0 1.2-.1 1.8-.3l2 1-.4-1.7c1.4-.9 2.3-2.2 2.3-3.6z" fill="currentColor" opacity=".88"/>
                <circle cx="7.4" cy="8.6" r=".9" fill="#fff"/>
                <circle cx="11.2" cy="8.6" r=".9" fill="#fff"/>
                <circle cx="13.6" cy="13.4" r=".7" fill="#fff"/>
                <circle cx="17" cy="13.4" r=".7" fill="#fff"/>
              </svg>
              <svg v-else-if="s.key === 'qq'" viewBox="0 0 24 24" width="22" height="22">
                <path d="M12 3c-3.6 0-6.5 2.7-6.5 6 0 1.8.8 3.3 2 4.4-.3 1-.9 2.4-.5 2.9.5.6 1.9-.2 2.9-.8.7.2 1.4.3 2.1.3s1.4-.1 2.1-.3c1 .6 2.4 1.4 2.9.8.4-.5-.2-1.9-.5-2.9 1.2-1.1 2-2.6 2-4.4 0-3.3-2.9-6-6.5-6z" fill="currentColor"/>
                <path d="M8.2 17.5c-.6.6-1 1.6-.7 1.9.4.4 1.6-.2 2.4-.7-.7-.3-1.3-.7-1.7-1.2zM15.8 17.5c-.4.5-1 .9-1.7 1.2.8.5 2 1.1 2.4.7.3-.3-.1-1.3-.7-1.9z" fill="currentColor"/>
                <circle cx="9.6" cy="8.7" r="1" fill="#fff"/>
                <circle cx="14.4" cy="8.7" r="1" fill="#fff"/>
                <path d="M10.6 11h2.8c.3 0 .4.3.2.5l-1.3 1c-.2.1-.4.1-.6 0l-1.3-1c-.2-.2-.1-.5.2-.5z" fill="#fff"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" width="24" height="24">
                <text x="12" y="17" text-anchor="middle" font-size="14" font-weight="700" fill="currentColor">支</text>
              </svg>
            </button>
          </div>
          <p v-if="qrMode" class="social-waiting">请在上方扫码面板中用手机支付宝扫码...</p>
        </div>
        <button v-if="showChoose" type="button" class="auth-link" @click="mode = 'choose'">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
          返回选择
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { userLogin, userRegister, saveAuthUser, getAlipayLoginQr, getAlipayLoginStatus } from '../api/index.js'

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
function finish(u, guest) {
  saveAuthUser(u)
  authName.value = ''
  authPwd.value = ''
  authPhone.value = ''
  emit('authenticated', { user: u, guest })
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
    const u = res.data
    finish({ id: u.id, name: u.username, role: u.role || 'GUEST' }, { name: guestName, pwd: guestPwd })
  } catch (err) {
    authError.value = `游客登录失败：${err.message}`
  } finally {
    authLoading.value = false
  }
}

// ===== 扫码登录（支付宝，内嵌二维码模式）=====
// 后端 /login/alipay/qr 生成二维码（state 存 Redis 5 分钟）；手机扫码确认后回调发生在
// 手机端，PC 通过 /login/alipay/status 每 2 秒轮询，SUCCESS 时后端已在当前会话写入登录态
const socialList = [
  { key: 'wechat', label: '微信' },
  { key: 'qq', label: 'QQ' },
  { key: 'alipay', label: '支付宝' }
]
const qrLoading = ref(false) // 二维码生成中
const qrMode = ref(false) // 是否展示扫码面板
const qrImage = ref('') // 后端渲染的二维码 data URL
const qrState = ref('') // 本次扫码的 state
const qrExpired = ref(false) // 二维码是否已过期
let qrPollTimer = null

function socialTitle(s) {
  return `${s.label}扫码登录`
}

function socialLogin(key) {
  if (key === 'alipay') return alipayLogin()
  authError.value = `${key === 'wechat' ? '微信' : 'QQ'}扫码登录即将上线，敬请期待`
}

// 停止轮询并复位加载状态（成功、过期、退出面板、组件卸载复用）
function stopAlipay() {
  if (qrPollTimer) clearInterval(qrPollTimer)
  qrPollTimer = null
  qrLoading.value = false
}

// 退出扫码面板，回到账号登录表单
function exitQr() {
  stopAlipay()
  qrMode.value = false
  qrImage.value = ''
  qrState.value = ''
  qrExpired.value = false
  authError.value = ''
}

// 获取二维码并进入扫码面板，同时开始轮询
async function alipayLogin() {
  if (qrLoading.value) return
  authError.value = ''
  authSuccess.value = ''
  qrExpired.value = false
  stopAlipay()
  qrLoading.value = true
  try {
    const res = await getAlipayLoginQr()
    qrState.value = res.data.state
    qrImage.value = res.data.qrImage
    qrMode.value = true
    qrPollTimer = setInterval(pollQrStatus, 2000)
  } catch (err) {
    authError.value = `获取支付宝二维码失败：${err.message || '请求失败'}`
  } finally {
    qrLoading.value = false
  }
}

// 轮询扫码状态：SUCCESS 用返回的用户完成登录；EXPIRED 提示刷新；PENDING 继续等
async function pollQrStatus() {
  try {
    const res = await getAlipayLoginStatus(qrState.value)
    const { status, user } = res.data
    if (status === 'SUCCESS') {
      stopAlipay()
      qrMode.value = false
      finish({ id: user.id, name: user.username, role: user.role })
    } else if (status === 'EXPIRED') {
      stopAlipay()
      qrExpired.value = true
    }
  } catch {
    // 单次轮询失败（网络抖动）忽略，下一轮继续
  }
}

onUnmounted(stopAlipay)

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
    const u = res.data
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
    finish({ id: u.id, name: u.username, role: u.role })
  } catch (err) {
    // 拦截器已统一将后端 message 写入 err.message
    const msg = err.message || '请求失败'
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
  background: var(--ad-mask);
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

.auth-dialog {
  position: relative;
  width: 420px;
  max-width: 90%;
  background: var(--ad-card-bg);
  border: 1px solid var(--ad-border);
  border-radius: 20px;
  animation: authIn 0.45s cubic-bezier(0.22, 1, 0.36, 1) both;
  padding: 44px 40px 32px;
  text-align: center;
  box-shadow: 0 24px 60px var(--ad-shadow);
}

@keyframes authIn {
  from {
    opacity: 0;
    transform: translateY(26px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 右上角图标按钮（关闭 / 回到首页） */
.auth-close,
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

.auth-close {
  font-size: 15px;
}

.auth-close:hover,
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
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 600;
  color: var(--ad-text);
  margin-bottom: 6px;
  letter-spacing: 2px;
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
  /* 实测校准：与扫码面板(335px)取同高，两模式切换时弹窗零高度跳动 */
  min-height: 340px;
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

/* 错误 / 成功提示：淡色底横幅 + 图标（与全站提示样式统一） */
.auth-error,
.auth-success {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  margin: 0;
  padding: 7px 12px;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.5;
  text-align: left;
  animation: tipBannerIn 0.25s ease;
}

.auth-error {
  color: var(--ad-error);
  background: color-mix(in srgb, var(--ad-error) 10%, transparent);
}

.auth-success {
  color: #00b42a;
  background: #e8ffea;
}

.auth-error::before,
.auth-success::before {
  content: '';
  flex: none;
  width: 13px;
  height: 13px;
  background: currentColor;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cline x1='12' y1='8' x2='12' y2='12'/%3E%3Cline x1='12' y1='16' x2='12.01' y2='16'/%3E%3C/svg%3E") no-repeat center / contain;
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cline x1='12' y1='8' x2='12' y2='12'/%3E%3Cline x1='12' y1='16' x2='12.01' y2='16'/%3E%3C/svg%3E") no-repeat center / contain;
}

.auth-success::before {
  -webkit-mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M22 11.08V12a10 10 0 1 1-5.93-9.14'/%3E%3Cpolyline points='22 4 12 14.01 9 11.01'/%3E%3C/svg%3E");
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M22 11.08V12a10 10 0 1 1-5.93-9.14'/%3E%3Cpolyline points='22 4 12 14.01 9 11.01'/%3E%3C/svg%3E");
}

@keyframes tipBannerIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

/* ===== 扫码登录（微信 / QQ / 支付宝）===== */
.auth-social {
  margin-top: 16px;
}

.social-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  color: var(--ad-text2);
  font-size: 12px;
  user-select: none;
}

.social-divider::before,
.social-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--ad-border);
}

.social-btns {
  display: flex;
  justify-content: center;
  gap: 22px;
}

.social-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
}

.social-btn.wechat {
  background: #07c160;
}

.social-btn.qq {
  background: #12b7f5;
}

.social-btn.alipay {
  background: #1677ff;
}

.social-btn:hover:not(:disabled) {
  transform: translateY(-3px) scale(1.06);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.18);
}

.social-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.social-waiting {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--ad-accent);
}

/* ===== 内嵌支付宝扫码面板 ===== */
.auth-qr {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  /* 与登录表单同高（340px），差值由 justify-content 均匀分布，切换零跳动 */
  min-height: 340px;
}

.qr-box {
  position: relative;
  width: 230px;
  height: 230px;
  padding: 10px;
  border: 1px solid var(--ad-border);
  border-radius: 12px;
  background: #fff;
}

.qr-box img {
  display: block;
  width: 100%;
  height: 100%;
}

.qr-mask,
.qr-expired {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 13px;
  text-align: center;
  cursor: pointer;
}

.qr-mask {
  background: rgba(255, 255, 255, 0.9);
  color: var(--ad-text2);
  cursor: default;
}

.qr-expired {
  background: rgba(0, 0, 0, 0.62);
  color: #fff;
  line-height: 1.8;
}

.qr-expired:hover {
  background: rgba(0, 0, 0, 0.5);
}

.qr-tip {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--ad-text2);
}

.qr-tip span {
  font-size: 12px;
  opacity: 0.75;
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
