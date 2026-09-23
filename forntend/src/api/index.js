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

// 响应拦截器：统一解包 R<T> 响应体
request.interceptors.response.use(
  res => {
    const body = res.data
    // 统一响应体 { code, message, data }：非 200 直接 reject
    if (body && typeof body === 'object' && 'code' in body && body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    // 返回 R 对象本身，调用处以 res.data 取业务数据（与原本 res.data 语义一致）
    return body
  },
  err => {
    // 401 未授权：清除本地身份
    if (err.response?.status === 401) {
      clearAuth()
    }
    // 统一采用后端返回的提示信息：R 响应体的 message 直接覆盖 err.message，调用方读 e.message 即可
    const backendMsg = err.response?.data?.message
    if (backendMsg) {
      err.message = backendMsg
    }
    return Promise.reject(err)
  }
)

/**
 * AI 恋爱大师 SSE 接口地址（GET，返回 text/event-stream）
 */
export const LOVE_SSE_URL = `${BASE_URL}/doChatSSE`

/**
 * 生成会话 ID：32 位无横线 UUID
 */
export function generateChatId() {
  return crypto.randomUUID().replaceAll('-', '')
}

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

/**
 * 后台管理 - 用户列表（需管理员身份）
 */
export function listUsers() {
  return request.get('/user/list')
}

/**
 * 后台管理 - 更新用户（需管理员身份），data 传 UserRegisterDTO 字段
 */
export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

/**
 * 后台管理 - 删除用户（需管理员身份）
 */
export function removeUser(id) {
  return request.delete(`/user/${id}`)
}

/**
 * 个人中心 - 获取当前用户完整信息（已对接后端 UserController.getCurrentUser）
 * GET /user/info  → UserVO { id, username, phone, avatar, role, createTime }
 */
export function getUserInfo() {
  return request.get('/user/info')
}

/**
 * 上传头像（已对接后端 FileController.uploadAvatar）
 * POST /file/avatar  multipart/form-data 字段 file  → R<String>，data 为头像 URL
 */
export function uploadAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/file/avatar', fd)
}

/**
 * 当前登录用户更新自己的资料（头像 / 手机号，后端仅允许这两个字段，防越权）
 * PUT /user/profile  body: { avatar?, phone? }  → UserVO
 */
export function updateMyProfile(data) {
  return request.put('/user/profile', data)
}

/**
 * 个人中心 - 查询签到信息（后端接口待补充）
 * GET /user/sign/info  → { signedToday, continuousDays, monthDays, recentDates: ['2026-09-21', ...] }
 */
export function getSignInfo() {
  return request.get('/user/sign/info')
}

/**
 * 个人中心 - 每日签到（后端接口待补充）
 * POST /user/sign  → { signedToday, continuousDays, monthDays, recentDates }
 */
export function doSign() {
  return request.post('/user/sign')
}

/**
 * 统计管理 - 今日概览（后端接口待补充）
 * GET /stats/overview  → { todayActive, todaySign, totalUsers, todayConversations,
 *                          diffs: { active, sign, users, conversations } }  // 环比昨日百分比
 */
export function getStatsOverview() {
  return request.get('/stats/overview')
}

/**
 * 统计管理 - 趋势数据（后端接口待补充）
 * GET /stats/trend?days=7|30
 * → { dates: ['09/15', ...], activeCounts: [], signCounts: [], newUsers: [], userTotals: [], messageCounts: [] }
 */
export function getStatsTrend(days) {
  return request.get('/stats/trend', { params: { days } })
}

/**
 * 系统通知 - 查询通知列表（后端接口待补充）
 * GET /user/notices  → { list: [{ id, type: 'SYSTEM'|'ACTIVITY'|'UPDATE', title, content, createTime, read }] }
 */
export function getNotices() {
  return request.get('/user/notices')
}

/**
 * 系统通知 - 标记单条已读（后端接口待补充）
 * POST /user/notices/{id}/read
 */
export function markNoticeRead(id) {
  return request.post(`/user/notices/${id}/read`)
}

/**
 * 系统通知 - 全部已读（后端接口待补充）
 * POST /user/notices/read-all
 */
export function markAllNoticesRead() {
  return request.post('/user/notices/read-all')
}

/**
 * 会员中心 - 查询当前会员状态（后端接口待补充）
 * GET /membership/current  → { isVip, expireTime }
 */
export function getMembership() {
  return request.get('/membership/current')
}

/**
 * 会员中心 - 创建支付订单（后端接口待补充）
 * POST /membership/order  body: { planKey, channel }  → { orderId, qrCodeUrl }
 */
export function createPayOrder(planKey, channel) {
  return request.post('/membership/order', { planKey, channel })
}

/**
 * 会员中心 - 查询支付状态（后端接口待补充）
 * GET /membership/order/{orderId}/status  → { status: 'WAIT' | 'SUCCESS' }
 */
export function getPayStatus(orderId) {
  return request.get(`/membership/order/${orderId}/status`)
}

/**
 * 会员中心 - 查询在售套餐（后端接口待补充）
 * GET /membership/plans  → { list: [{ key, name, price, originalPrice, days, desc, onSale }] }
 */
export function getMembershipPlans() {
  return request.get('/membership/plans')
}

/**
 * 会员管理 - 套餐列表（后端接口待补充）
 * GET /admin/membership/plans  → { list: [{ key, name, price, originalPrice, days, desc, onSale }] }
 */
export function listMembershipPlans() {
  return request.get('/admin/membership/plans')
}

/**
 * 会员管理 - 调整套餐（后端接口待补充）
 * PUT /admin/membership/plans/{key}  body: { price, originalPrice, days, desc, onSale }
 */
export function updateMembershipPlan(key, data) {
  return request.put(`/admin/membership/plans/${key}`, data)
}

/**
 * 消息发布 - 通知列表（后端接口待补充）
 * GET /admin/notices  → { list: [{ id, type, title, content, scope, status, readCount, totalCount, createTime }] }
 */
export function listAdminNotices() {
  return request.get('/admin/notices')
}

/**
 * 消息发布 - 新建通知（后端接口待补充）
 * POST /admin/notices  body: { type, title, content, scope }
 */
export function createAdminNotice(data) {
  return request.post('/admin/notices', data)
}

/**
 * 消息发布 - 编辑通知（后端接口待补充）
 * PUT /admin/notices/{id}
 */
export function updateAdminNotice(id, data) {
  return request.put(`/admin/notices/${id}`, data)
}

/**
 * 消息发布 - 发布/撤回通知（后端接口待补充）
 * POST /admin/notices/{id}/publish | withdraw
 */
export function toggleAdminNotice(id, action) {
  return request.post(`/admin/notices/${id}/${action}`)
}

/**
 * 消息发布 - 删除通知（后端接口待补充）
 * DELETE /admin/notices/{id}
 */
export function removeAdminNotice(id) {
  return request.delete(`/admin/notices/${id}`)
}

/**
 * 积分明细 - 积分概览（后端接口待补充）
 * GET /user/points/summary  → { balance, totalEarned, totalSpent }
 */
export function getUserPointsSummary() {
  return request.get('/user/points/summary')
}

/**
 * 积分明细 - 积分流水（后端接口待补充）
 * GET /user/points/records  → { list: [{ id, type: 'SIGN_IN'|'RECHARGE'|'SPEND'|'ADMIN_ADJUST', title, points, createTime }] }
 */
export function getUserPointsRecords() {
  return request.get('/user/points/records')
}

/**
 * 积分管理 - 规则查询（后端接口待补充）
 * GET /admin/points/rules  → { signInBase, signInBonusPerDay, signInMax }
 */
export function getPointsRules() {
  return request.get('/admin/points/rules')
}

/**
 * 积分管理 - 保存规则（后端接口待补充）
 * PUT /admin/points/rules
 */
export function savePointsRules(data) {
  return request.put('/admin/points/rules', data)
}

/**
 * 积分管理 - 用户积分总览（后端接口待补充）
 * GET /admin/points/users  → { list: [{ userId, username, balance, totalEarned, totalSpent, lastChangeTime }] }
 */
export function listPointsUsers() {
  return request.get('/admin/points/users')
}

/**
 * 积分管理 - 调整积分（后端接口待补充）
 * POST /admin/points/adjust  body: { userId, points(正加负减), reason }
 */
export function adjustUserPoints(data) {
  return request.post('/admin/points/adjust', data)
}

export default request
