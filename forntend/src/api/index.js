import axios from 'axios'

const BASE_URL = '/api'

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 30000
})

// ===== 登录态存储：sessionStorage + 会话 Cookie 双写 =====
// sessionStorage 刷新不丢失；会话 Cookie 兜底支持新标签页恢复，且浏览器关闭时自动失效
function setCookie(name, value) {
  document.cookie = `${name}=${encodeURIComponent(value)}; path=/`
}

function getCookie(name) {
  const m = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`))
  return m ? decodeURIComponent(m[1]) : null
}

function removeCookie(name) {
  document.cookie = `${name}=; path=/; max-age=0`
}

// 保存登录身份与 token（双写 sessionStorage 和会话 Cookie）
export function saveAuthUser(user, token) {
  sessionStorage.setItem('chat-user', JSON.stringify(user))
  setCookie('chat-user', JSON.stringify(user))
  if (token) {
    sessionStorage.setItem('chat-token', token)
    setCookie('chat-token', token)
  }
}

// 清除登录态（登出或 401）
export function clearAuth() {
  sessionStorage.removeItem('chat-user')
  sessionStorage.removeItem('chat-token')
  removeCookie('chat-user')
  removeCookie('chat-token')
}

// 模块加载时兜底恢复：sessionStorage 为空（如新标签页）时从会话 Cookie 恢复
if (!sessionStorage.getItem('chat-user')) {
  const savedUser = getCookie('chat-user')
  if (savedUser) {
    sessionStorage.setItem('chat-user', savedUser)
    const savedToken = getCookie('chat-token')
    if (savedToken) sessionStorage.setItem('chat-token', savedToken)
  }
}

// 请求拦截器：自动带上 token
request.interceptors.request.use(config => {
  const token = sessionStorage.getItem('chat-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  res => res,
  err => {
    // 401 未授权：清除本地身份
    if (err.response?.status === 401) {
      clearAuth()
    }
    return Promise.reject(err)
  }
)

/**
 * AI 恋爱大师 SSE 接口地址（GET，返回 text/event-stream）
 */
export const LOVE_SSE_URL = `${BASE_URL}/doChatSSE`

/**
 * AI 超级智能体 SSE 接口地址（GET，返回 SseEmitter）
 */
export const AGENT_SSE_URL = `${BASE_URL}/doChatWithEmitter`

/**
 * 用户登录
 * POST /user/login  → { user, token }
 */
export function userLogin(username, password) {
  return request.post('/user/login', { username, password })
}

/**
 * 用户注册
 * POST /user/register  → UserVO
 */
export function userRegister({ username, password, phone, role }) {
  return request.post('/user/register', { username, password, phone, role })
}

/**
 * 用户登出
 */
export function userLogout() {
  return request.post('/user/logout')
}

/**
 * 后台管理 - 会话列表
 */
export function listConversations(userId = 0) {
  return request.get('/chat/conversations', { params: { userId } })
}

/**
 * 后台管理 - 指定会话的消息列表
 */
export function listMessages(conversationId) {
  return request.get('/chat/message', { params: { conversationId } })
}

export default request
