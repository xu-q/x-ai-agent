<template>
  <div class="profile-page">
    <!-- 左侧边栏 -->
    <aside class="side">
      <!-- 用户身份卡：头像 + 用户名 + VIP 标识 -->
      <div class="user-brand">
        <img v-if="displayAvatar" :src="displayAvatar" alt="头像" class="brand-avatar" />
        <span v-else class="brand-avatar brand-fallback">
          <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
            <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-3.9 0-8 2-8 5v1a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-1c0-3-4.1-5-8-5z"/>
          </svg>
        </span>
        <div class="brand-user">
          <span class="brand-name" :title="user?.name">{{ user?.name || '未登录' }}</span>
          <div class="brand-badges">
            <span class="brand-role" :class="`role-${(user?.role || 'USER').toLowerCase()}`">
              {{ roleLabel(user?.role) }}
            </span>
            <span class="brand-vip" :class="{ 'vip-on': vip.isVip }">
              <svg viewBox="0 0 24 24" width="10" height="10" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 17l2-9 5 5 2-7 2 7 5-5 2 9H3z"/>
              </svg>
              VIP
            </span>
          </div>
        </div>
      </div>
      <nav class="side-nav">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="side-item"
          :class="[{ active: activeTab === tab.key }, `side-${tab.key}`]"
          @click="activeTab = tab.key"
        >
          <!-- 基本资料 -->
          <svg v-if="tab.key === 'profile'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <!-- 会员中心 -->
          <svg v-else-if="tab.key === 'vip'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 17l2-9 5 5 2-7 2 7 5-5 2 9H3z"/>
            <path d="M3 21h18"/>
          </svg>
          <!-- 消息管理 -->
          <svg v-else-if="tab.key === 'messages'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <!-- 用户管理 -->
          <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          {{ tab.label }}
        </button>
      </nav>
      <button class="back-btn" @click="router.push('/')">
        <svg viewBox="0 0 24 24" width="14" height="14">
          <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回首页
      </button>
    </aside>

    <main class="page-main" :class="`main-${activeTab}`">
      <section class="panel" :class="`panel-${activeTab}`">
        <!-- 个人中心 -->
        <template v-if="activeTab === 'profile'">
          <!-- 每日签到 -->
          <div class="sign-card">
            <div class="sign-head">
              <div class="sign-stats">
                <div class="sign-stat">
                  <strong>{{ sign.continuousDays }}</strong>
                  <span>连续签到（天）</span>
                </div>
                <div class="sign-divider"></div>
                <div class="sign-stat">
                  <strong>{{ sign.monthDays }}</strong>
                  <span>本月签到（天）</span>
                </div>
              </div>
              <button class="sign-btn" :disabled="signLoading || sign.signedToday" @click="handleSign">
                {{ signLoading ? '签到中...' : sign.signedToday ? '今日已签到' : '立即签到' }}
              </button>
            </div>
            <div class="sign-week">
              <span
                v-for="d in signWeek"
                :key="d.key"
                class="sign-day"
                :class="{ checked: d.checked, today: d.today }"
              >
                {{ d.label }}
              </span>
            </div>
            <p v-if="signSuccess" class="mini-tip tip-success">签到成功，已连续签到 {{ sign.continuousDays }} 天</p>
            <p v-if="signError" class="mini-tip tip-warn">{{ signError }}</p>
          </div>

          <div class="profile-card">
            <div class="avatar-wrap">
              <div class="avatar-lg" title="点击更换头像" @click="fileInput?.click()">
                <img v-if="displayAvatar" :src="displayAvatar" alt="头像" />
                <svg v-else viewBox="0 0 24 24" fill="currentColor" width="42" height="42">
                  <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-3.9 0-8 2-8 5v1a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-1c0-3-4.1-5-8-5z"/>
                </svg>
                <div class="avatar-mask">
                  <span v-if="uploading" class="mask-spinner"></span>
                  <span v-else>更换头像</span>
                </div>
              </div>
              <input
                ref="fileInput"
                type="file"
                accept="image/jpeg,image/png"
                class="avatar-input"
                @change="onFileChange"
              />
            </div>
            <div class="info-list">
              <div class="info-row">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ info.username || info.name || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ info.phone || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">角色</span>
                <span class="info-value">
                  <span class="role-badge" :class="`role-${(info.role || 'USER').toLowerCase()}`">
                    {{ roleLabel(info.role) }}
                  </span>
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">注册时间</span>
                <span class="info-value">{{ info.createTime ? formatTime(info.createTime) : '-' }}</span>
              </div>
              <p v-if="infoError" class="mini-tip">{{ infoError }}</p>
              <p v-if="uploadError" class="mini-tip tip-warn">{{ uploadError }}</p>
            </div>
          </div>
        </template>

        <!-- 会员中心 -->
        <template v-else-if="activeTab === 'vip'">
          <div class="vip-status" :class="{ 'vip-on': vip.isVip }">
            <div class="vip-status-icon">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 17l2-9 5 5 2-7 2 7 5-5 2 9H3z"/>
              </svg>
            </div>
            <div class="vip-status-text">
              <strong>{{ vip.isVip ? '会员生效中' : '普通用户' }}</strong>
              <span v-if="vip.isVip && vip.expireTime">有效期至 {{ formatTime(vip.expireTime) }}</span>
              <span v-else>开通会员享受专属权益</span>
            </div>
          </div>

          <div class="vip-plans">
            <div
              v-for="plan in plans"
              :key="plan.key"
              class="vip-card"
              :class="{ selected: selectedPlan === plan.key }"
              @click="selectedPlan = plan.key"
            >
              <span class="vip-name">{{ plan.name }}</span>
              <span class="vip-price"><em>¥</em>{{ plan.price }}</span>
              <span class="vip-desc">{{ plan.desc }}</span>
            </div>
          </div>

          <div class="channel-row">
            <div
              v-for="ch in channels"
              :key="ch.key"
              class="channel-item"
              :class="{ selected: selectedChannel === ch.key }"
              @click="selectedChannel = ch.key"
            >
              <svg viewBox="0 0 24 24" width="20" height="20">
                <rect width="24" height="24" rx="6" :fill="ch.color" />
                <text x="12" y="16.5" text-anchor="middle" fill="#fff" font-size="12" font-weight="600">{{ ch.glyph }}</text>
              </svg>
              <span>{{ ch.name }}</span>
            </div>
          </div>

          <button class="pay-btn" :disabled="payLoading" @click="createOrder">
            {{ payLoading ? '正在创建订单...' : `立即开通 ¥${selectedPlanInfo.price}` }}
          </button>
          <p v-if="payError" class="mini-tip tip-warn">{{ payError }}</p>
        </template>

        <!-- 消息管理（仅管理员） -->
        <template v-else-if="activeTab === 'messages'">
          <div class="toolbar">
            <input
              v-model.trim="searchText"
              class="search-input"
              type="text"
              placeholder="按会话 ID 搜索"
            />
          </div>
          <p v-if="loading" class="tip">加载中...</p>
          <p v-else-if="error" class="tip tip-error">{{ error }}</p>
          <p v-else-if="conversations.length === 0" class="tip">暂无会话数据</p>
          <p v-else-if="filteredConversations.length === 0" class="tip">未找到匹配的会话</p>
          <table v-else class="conv-table">
            <thead>
              <tr>
                <th>会话 ID</th>
                <th class="sortable" @click="convSort.toggle('messageCount')">
                  消息数量
                  <span class="sort-arrow">{{ convSort.arrow('messageCount') }}</span>
                </th>
                <th class="sortable" @click="convSort.toggle('startTime')">
                  创建时间
                  <span class="sort-arrow">{{ convSort.arrow('startTime') }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="conv in filteredConversations"
                :key="conv.conversationId"
                class="clickable"
                :title="`查看会话 ${conv.conversationId} 的消息详情`"
                @click="goDetail(conv.conversationId)"
              >
                <td>
                  <span class="conv-link">{{ conv.conversationId }}</span>
                </td>
                <td><span class="count-badge">{{ conv.messageCount }}</span></td>
                <td class="time-cell">{{ formatTime(conv.startTime) }}</td>
              </tr>
            </tbody>
          </table>
        </template>

        <!-- 用户管理（仅管理员） -->
        <template v-else>
          <div class="toolbar">
            <input
              v-model.trim="userSearch"
              class="search-input search-green"
              type="text"
              placeholder="按用户名 / ID / 手机号搜索"
            />
            <select v-model="roleFilter" class="role-select" title="按角色筛选">
              <option value="">全部角色</option>
              <option value="ADMIN">管理员</option>
              <option value="USER">用户</option>
              <option value="GUEST">游客</option>
            </select>
            <button class="refresh-btn" @click="loadUsers">刷新</button>
            <div v-if="selectedIds.length" class="batch-bar">
              <span class="batch-info">已选 {{ selectedIds.length }} 项</span>
              <button class="op-btn" :disabled="batchBusy" @click="batchSetStatus(1)">启用</button>
              <button class="op-btn" :disabled="batchBusy" @click="batchSetStatus(0)">禁用</button>
              <button class="op-btn op-danger" :disabled="batchBusy" @click="batchDelete">删除</button>
            </div>
          </div>
          <p v-if="usersLoading" class="tip">加载中...</p>
          <p v-else-if="usersError" class="tip tip-error">{{ usersError }}</p>
          <p v-else-if="users.length === 0" class="tip">暂无用户数据</p>
          <template v-else>
            <p v-if="actionError" class="action-error">{{ actionError }}</p>
            <p v-else-if="filteredUsers.length === 0" class="tip">未找到匹配的用户</p>
            <table v-else class="conv-table user-table">
              <thead>
                <tr>
                  <th class="col-check">
                    <input type="checkbox" :checked="isAllSelected" title="全选" @change="toggleAll" />
                  </th>
                  <th>用户 ID</th>
                  <th>用户名</th>
                  <th>手机号</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th class="sortable" @click="userSort.toggle('createTime')">
                    创建时间
                    <span class="sort-arrow">{{ userSort.arrow('createTime') }}</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="u in filteredUsers"
                  :key="u.id"
                  :class="{ 'row-selected': selectedIds.includes(u.id) }"
                >
                  <td class="col-check">
                    <input v-model="selectedIds" type="checkbox" :value="u.id" title="选择该用户" />
                  </td>
                  <td class="uid">{{ u.id }}</td>
                  <td>{{ u.username }}</td>
                  <td>{{ u.phone || '-' }}</td>
                  <td>
                    <span class="role-badge" :class="`role-${(u.role || 'USER').toLowerCase()}`">
                      {{ roleLabel(u.role) }}
                    </span>
                  </td>
                  <td>
                    <span class="status-badge" :class="u.status === 1 ? 'status-on' : 'status-off'">
                      {{ u.status === 1 ? '正常' : '已禁用' }}
                    </span>
                  </td>
                  <td class="time-cell">{{ formatTime(u.createTime) }}</td>
                </tr>
              </tbody>
            </table>
          </template>
        </template>
      </section>
    </main>

    <!-- 支付弹窗 -->
    <div v-if="payVisible" class="pay-mask" @click.self="closePay">
      <div class="pay-dialog">
        <button class="pay-close" title="关闭" @click="closePay">
          <svg viewBox="0 0 24 24" width="14" height="14">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <h2 class="pay-title">扫码支付</h2>
        <p class="pay-amount">
          {{ selectedChannelInfo.name }} · {{ selectedPlanInfo.name }}
          <strong>¥{{ selectedPlanInfo.price }}</strong>
        </p>
        <div class="pay-qr">
          <img v-if="payQr" :src="payQr" alt="支付二维码" />
          <span v-else class="pay-qr-placeholder">二维码生成中...</span>
        </div>
        <p class="pay-hint">请使用{{ selectedChannelInfo.name }}扫码完成支付</p>
        <p class="pay-status">{{ paid ? '支付成功，会员已开通' : '等待支付中...' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  saveAuthUser,
  getUserInfo,
  uploadAvatar,
  getSignInfo,
  doSign,
  getMembership,
  createPayOrder,
  getPayStatus,
  listConversations,
  listUsers,
  updateUser,
  removeUser
} from '../api'
import { formatTime } from '../utils/formatTime'

const router = useRouter()

// 当前登录身份（精简对象 {id, name, role, avatar?}）
const user = ref(JSON.parse(sessionStorage.getItem('chat-user') || 'null'))
const isAdmin = computed(() => user.value?.role === 'ADMIN')

const baseTabs = [
  { key: 'profile', label: '个人中心' },
  { key: 'vip', label: '会员中心' }
]
const adminTabs = [
  { key: 'messages', label: '消息管理' },
  { key: 'users', label: '用户管理' }
]
// 仅管理员展示后台管理类 tab
const tabs = computed(() => (isAdmin.value ? [...baseTabs, ...adminTabs] : baseTabs))
const activeTab = ref('profile')

const roleLabels = { ADMIN: '管理员', USER: '用户', GUEST: '游客' }
const roleLabel = (role) => roleLabels[role] || '用户'

// ===== 基本资料 =====
const info = ref({ name: user.value?.name, role: user.value?.role })
const infoError = ref('')

async function loadInfo() {
  try {
    const res = await getUserInfo()
    info.value = res.data || info.value
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    infoError.value = '完整资料需后端接口支持，当前展示本地信息'
  }
}

// 头像：本地预览优先（上传中即时反馈），否则用已保存头像
const fileInput = ref(null)
const previewUrl = ref('')
const uploading = ref(false)
const uploadError = ref('')
const displayAvatar = computed(() => previewUrl.value || user.value?.avatar || '')

function onFileChange(e) {
  const file = e.target.files[0]
  e.target.value = ''
  if (!file) return
  uploadError.value = ''
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    uploadError.value = '仅支持 JPG / PNG 格式图片'
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    uploadError.value = '图片大小不能超过 2MB'
    return
  }
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = URL.createObjectURL(file)
  doUpload(file)
}

async function doUpload(file) {
  uploading.value = true
  uploadError.value = ''
  try {
    const res = await uploadAvatar(file)
    const url = res.data?.avatar || ''
    if (!url) throw new Error('响应缺少头像地址')
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
    // 合并本地身份（token 传 null 不覆盖）
    const merged = { ...user.value, avatar: url }
    user.value = merged
    saveAuthUser(merged, null)
  } catch (e) {
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
    uploadError.value =
      e.response?.status === 401
        ? '登录已过期，请重新登录'
        : '头像上传服务暂未上线，请稍后再试'
  } finally {
    uploading.value = false
  }
}

// ===== 每日签到 =====
const sign = ref({ signedToday: false, continuousDays: 0, monthDays: 0, recentDates: [] })
const signLoading = ref(false)
const signError = ref('')
const signSuccess = ref(false)

function dateKey(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 最近 7 天签到格子（末位为今天）
const signWeek = computed(() => {
  const wd = ['日', '一', '二', '三', '四', '五', '六']
  const set = new Set(sign.value.recentDates || [])
  const list = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = dateKey(d)
    list.push({ key, checked: set.has(key), today: i === 0, label: i === 0 ? '今' : wd[d.getDay()] })
  }
  return list
})

async function loadSignInfo() {
  try {
    const res = await getSignInfo()
    if (res.data) sign.value = res.data
  } catch {
    /* 后端未就绪，展示默认状态 */
  }
}

async function handleSign() {
  signLoading.value = true
  signError.value = ''
  signSuccess.value = false
  try {
    const res = await doSign()
    // 后端返回全量数据则整体替换，否则本地补今天的记录
    if (res.data) sign.value = { ...sign.value, ...res.data }
    if (!sign.value.signedToday) {
      const today = dateKey(new Date())
      if (!sign.value.recentDates.includes(today)) sign.value.recentDates.push(today)
      sign.value.continuousDays += 1
      sign.value.monthDays += 1
      sign.value.signedToday = true
    }
    signSuccess.value = true
    setTimeout(() => (signSuccess.value = false), 3000)
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    signError.value = '签到服务即将上线，敬请期待'
  } finally {
    signLoading.value = false
  }
}

// ===== 会员中心 =====
const plans = [
  { key: 'MONTH', name: '月卡', price: 25, desc: '按月开通' },
  { key: 'QUARTER', name: '季卡', price: 68, desc: '折合 ¥22.7/月' },
  { key: 'YEAR', name: '年卡', price: 238, desc: '折合 ¥19.8/月' }
]
const selectedPlan = ref('MONTH')
const selectedPlanInfo = computed(() => plans.find((p) => p.key === selectedPlan.value))

const channels = [
  { key: 'ALIPAY', name: '支付宝', glyph: '支', color: '#1677ff' },
  { key: 'WECHAT', name: '微信支付', glyph: '微', color: '#07c160' }
]
const selectedChannel = ref('ALIPAY')
const selectedChannelInfo = computed(() => channels.find((c) => c.key === selectedChannel.value))

const vip = ref({ isVip: false, expireTime: '' })

async function loadMembership() {
  try {
    const res = await getMembership()
    if (res.data) vip.value = res.data
  } catch { /* 后端未就绪时按普通用户展示 */ }
}

// ===== 支付弹窗与轮询 =====
const payVisible = ref(false)
const payLoading = ref(false)
const payError = ref('')
const payQr = ref('')
const payOrderId = ref('')
const paid = ref(false)
let pollTimer = null
let pollFails = 0

async function createOrder() {
  payLoading.value = true
  payError.value = ''
  try {
    const res = await createPayOrder(selectedPlan.value, selectedChannel.value)
    payOrderId.value = res.data?.orderId || ''
    payQr.value = res.data?.qrCodeUrl || ''
    paid.value = false
    payVisible.value = true
    startPolling()
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    payError.value = '支付服务即将上线，敬请期待'
  } finally {
    payLoading.value = false
  }
}

function startPolling() {
  stopPolling()
  pollFails = 0
  pollTimer = setInterval(async () => {
    try {
      const res = await getPayStatus(payOrderId.value)
      pollFails = 0
      if (res.data?.status === 'SUCCESS') {
        paid.value = true
        vip.value = { isVip: true, expireTime: res.data?.expireTime || vip.value.expireTime }
        setTimeout(() => {
          closePay()
          loadMembership()
        }, 1200)
      }
    } catch {
      if (++pollFails >= 3) {
        stopPolling()
        payError.value = '支付状态查询失败，支付服务即将上线'
        closePay()
      }
    }
  }, 2000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function closePay() {
  stopPolling()
  payVisible.value = false
}

// ===== 消息管理（原后台管理） =====
const conversations = ref([])
const loading = ref(false)
const error = ref('')
const searchText = ref('')

// 通用三态排序：升序 -> 降序 -> 取消
function createSortState() {
  const field = ref('')
  const order = ref(1)
  return {
    field,
    order,
    toggle(f) {
      if (field.value !== f) {
        field.value = f
        order.value = 1
      } else if (order.value === 1) {
        order.value = -1
      } else {
        field.value = ''
        order.value = 1
      }
    },
    arrow(f) {
      if (field.value !== f) return '↕'
      return order.value === 1 ? '↑' : '↓'
    }
  }
}

// 按日期字段 + 方向排序
function sortByDate(list, field, order) {
  return [...list].sort((a, b) => {
    const av = new Date(a[field]).getTime() || 0
    const bv = new Date(b[field]).getTime() || 0
    return (av - bv) * order
  })
}

const convSort = createSortState()

const filteredConversations = computed(() => {
  let list = conversations.value
  const kw = searchText.value.toLowerCase()
  if (kw) {
    list = list.filter((c) => c.conversationId.toLowerCase().includes(kw))
  }
  if (convSort.field.value) {
    list = sortByDate(list, convSort.field.value, convSort.order.value)
  }
  return list
})

async function loadConversations() {
  loading.value = true
  error.value = ''
  try {
    const res = await listConversations()
    conversations.value = res.data || []
  } catch {
    error.value = '加载会话列表失败，请确认后端服务已启动'
  } finally {
    loading.value = false
  }
}

function goDetail(conversationId) {
  router.push(`/profile/conversation/${conversationId}`)
}

// ===== 用户管理（原后台管理） =====
const users = ref([])
const usersLoading = ref(false)
const usersError = ref('')
const actionError = ref('')
const userSearch = ref('')
const roleFilter = ref('') // '' 全部 | ADMIN | USER | GUEST
const userSort = createSortState()
const usersLoaded = ref(false)

const filteredUsers = computed(() => {
  let list = users.value
  const kw = userSearch.value.toLowerCase()
  if (kw) {
    list = list.filter(
      (u) =>
        (u.username || '').toLowerCase().includes(kw) ||
        (u.id || '').toLowerCase().includes(kw) ||
        (u.phone || '').includes(kw)
    )
  }
  if (roleFilter.value) {
    list = list.filter((u) => (u.role || 'USER') === roleFilter.value)
  }
  if (userSort.field.value) {
    list = sortByDate(list, userSort.field.value, userSort.order.value)
  }
  return list
})

async function loadUsers() {
  usersLoading.value = true
  usersError.value = ''
  actionError.value = ''
  try {
    const res = await listUsers()
    users.value = res.data || []
    usersLoaded.value = true
  } catch (e) {
    usersError.value =
      e.response?.status === 403
        ? '无权限访问：需要管理员身份'
        : '加载用户列表失败，请确认后端服务已启动'
  } finally {
    usersLoading.value = false
  }
}

// ===== 批量操作 =====
const selectedIds = ref([])
const batchBusy = ref(false)

const isAllSelected = computed(
  () => filteredUsers.value.length > 0 && selectedIds.value.length === filteredUsers.value.length
)

function toggleAll() {
  selectedIds.value = isAllSelected.value ? [] : filteredUsers.value.map((u) => u.id)
}

// 批量目标：跳过当前登录账号，防止自锁/自删
function pickTargets() {
  return selectedIds.value
    .filter((id) => id !== user.value?.id)
    .map((id) => users.value.find((u) => u.id === id))
    .filter(Boolean)
}

async function batchSetStatus(status) {
  const targets = pickTargets()
  if (!targets.length) {
    actionError.value = '不能对当前登录账号执行批量操作'
    return
  }
  batchBusy.value = true
  actionError.value = ''
  try {
    const results = await Promise.allSettled(
      targets.map((u) =>
        updateUser(u.id, { username: u.username, phone: u.phone, role: u.role, status })
      )
    )
    const failed = results.filter((r) => r.status === 'rejected').length
    if (failed) actionError.value = `${failed} 个用户操作失败，请重试`
    await loadUsers()
    selectedIds.value = []
  } finally {
    batchBusy.value = false
  }
}

async function batchDelete() {
  const targets = pickTargets()
  if (!targets.length) {
    actionError.value = '不能对当前登录账号执行批量操作'
    return
  }
  if (!window.confirm(`确定删除选中的 ${targets.length} 个用户吗？该操作不可恢复。`)) return
  batchBusy.value = true
  actionError.value = ''
  try {
    const results = await Promise.allSettled(targets.map((u) => removeUser(u.id)))
    const failed = results.filter((r) => r.status === 'rejected').length
    if (failed) actionError.value = `${failed} 个用户删除失败，请重试`
    await loadUsers()
    selectedIds.value = []
  } finally {
    batchBusy.value = false
  }
}

// 首次切到用户管理时再加载（仅管理员）
watch(activeTab, (tab) => {
  if (tab === 'users' && isAdmin.value && !usersLoaded.value) {
    loadUsers()
  }
})

onMounted(() => {
  loadInfo()
  loadSignInfo()
  loadMembership()
  if (isAdmin.value) loadConversations()
})

onBeforeUnmount(stopPolling)
</script>

<style scoped>
.profile-page {
  height: 100%;
  display: flex;
  background: #f5f6f7;
}

/* ===== 左侧边栏（与后台管理同风格） ===== */
.side {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 12px 16px;
  background: #fff;
  border-right: 1px solid #e5e6eb;
}

/* 用户身份卡：主题色卡片化，侧边栏视觉焦点 */
.user-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 10px;
  margin-bottom: 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(114, 45, 209, 0.12), rgba(102, 126, 234, 0.08));
  box-shadow: inset 0 0 0 1px rgba(114, 45, 209, 0.16);
}

.brand-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 0 0 2px #fff, 0 3px 10px rgba(114, 45, 209, 0.4);
}

.brand-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.brand-user {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
  min-width: 0;
}

.brand-name {
  max-width: 102px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2329;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 角色徽章：复用 role-x 配色，尺寸对齐 VIP 徽标 */
.brand-badges {
  display: flex;
  align-items: center;
  gap: 3px;
  flex-wrap: nowrap;
}

.brand-role {
  display: inline-flex;
  align-items: center;
  padding: 1.5px 6px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  line-height: 1.6;
  white-space: nowrap;
}

.brand-role.role-admin {
  background: #ffece8;
  color: #f53f3f;
}

.brand-role.role-user {
  background: #e8f3ff;
  color: #165dff;
}

.brand-role.role-guest {
  background: #f2f3f5;
  color: #86909c;
}

/* VIP 徽标：会员金色，非会员灰色 */
.brand-vip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1.5px 6px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
  background: #f2f3f5;
  color: #86909c;
}

.brand-vip.vip-on {
  background: linear-gradient(135deg, #ffc53d, #ff7d00);
  color: #fff;
  box-shadow: 0 1px 4px rgba(255, 125, 0, 0.35);
}

.side-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #4e5969;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.side-item svg {
  flex-shrink: 0;
}

.side-item:hover {
  background: #f2f3f5;
}

/* 激活项：主题色底色 + 左侧指示条（资料紫，会员橙，消息蓝，用户管理绿） */
.side-profile.active {
  background: rgba(114, 45, 209, 0.08);
  color: #722ed1;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #722ed1;
}

.side-messages.active {
  background: rgba(22, 93, 255, 0.08);
  color: #165dff;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #165dff;
}

.side-vip.active {
  background: rgba(255, 125, 0, 0.08);
  color: #ff7d00;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #ff7d00;
}

.side-users.active {
  background: rgba(0, 180, 42, 0.08);
  color: #00b42a;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #00b42a;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 9px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #fff;
  color: #4e5969;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
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

/* ===== 右侧内容区 ===== */
.page-main {
  flex: 1;
  overflow: auto;
  padding: 24px;
  transition: background 0.3s;
}

.main-profile {
  background: #efe9fb;
}

.main-messages {
  background: #e8effa;
}

.main-vip {
  background: #fdf1e3;
}

.main-users {
  background: #e6f5ec;
}

.panel {
  max-width: 1100px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 24px 28px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-top: 3px solid transparent;
  transition: background 0.3s, border-color 0.3s;
}

.panel-profile {
  background: #f7f3fd;
  border-top-color: #722ed1;
}

.panel-messages {
  background: #f3f8ff;
  border-top-color: #165dff;
}

.panel-vip {
  background: #fff7ec;
  border-top-color: #ff7d00;
}

.panel-users {
  background: #f0fbf5;
  border-top-color: #00b42a;
}

/* ===== 个人中心 ===== */
/* 每日签到卡：紫色主题 */
.sign-card {
  margin-bottom: 20px;
  padding: 16px 20px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(114, 45, 209, 0.08), rgba(102, 126, 234, 0.05));
  box-shadow: inset 0 0 0 1px rgba(114, 45, 209, 0.14);
}

.sign-head {
  display: flex;
  align-items: center;
}

.sign-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.sign-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.sign-stat strong {
  font-size: 22px;
  font-weight: 700;
  color: #722ed1;
  font-variant-numeric: tabular-nums;
}

.sign-stat span {
  font-size: 12px;
  color: #86909c;
}

.sign-divider {
  width: 1px;
  height: 30px;
  background: #e5e6eb;
}

.sign-btn {
  margin-left: auto;
  padding: 9px 22px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #9d5ce8, #722ed1);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
}

.sign-btn:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-1px);
}

.sign-btn:disabled {
  cursor: not-allowed;
  background: #f2f3f5;
  color: #86909c;
}

.sign-week {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
}

.sign-day {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 12px;
  color: #86909c;
  background: #f2f3f5;
  transition: background 0.2s, color 0.2s;
}

.sign-day.checked {
  background: rgba(114, 45, 209, 0.16);
  color: #722ed1;
  font-weight: 600;
}

.sign-day.today {
  box-shadow: inset 0 0 0 1.5px #722ed1;
  color: #722ed1;
  font-weight: 700;
}

.tip-success {
  color: #00b42a;
}

.profile-card {
  display: flex;
  gap: 32px;
  align-items: center;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar-lg {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  cursor: pointer;
}

.avatar-lg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 悬停遮罩：更换头像 */
.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-lg:hover .avatar-mask {
  opacity: 1;
}

.mask-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.avatar-input {
  display: none;
}

.info-list {
  flex: 1;
  min-width: 0;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f1f3;
  font-size: 14px;
}

.info-row:last-of-type {
  border-bottom: none;
}

.info-label {
  width: 80px;
  flex-shrink: 0;
  color: #86909c;
}

.info-value {
  color: #1f2329;
}

.mini-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #86909c;
}

.tip-warn {
  color: #ff7d00;
}

/* ===== 会员中心 ===== */
.vip-status {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  border-radius: 10px;
  background: #f7f8fa;
  margin-bottom: 20px;
}

.vip-status.vip-on {
  background: linear-gradient(135deg, #fff7ec, #ffefd8);
}

.vip-status-icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 125, 0, 0.1);
  color: #ff7d00;
  flex-shrink: 0;
}

.vip-status-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 13px;
  color: #86909c;
}

.vip-status-text strong {
  font-size: 16px;
  color: #1f2329;
}

.vip-status.vip-on .vip-status-text strong {
  color: #ff7d00;
}

.vip-plans {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.vip-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 20px 12px;
  border: 1.5px solid #e5e6eb;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.vip-card:hover {
  transform: translateY(-2px);
}

.vip-card.selected {
  border-color: #ff7d00;
  box-shadow: 0 4px 14px rgba(255, 125, 0, 0.18);
}

.vip-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2329;
}

.vip-price {
  font-size: 26px;
  font-weight: 700;
  color: #ff7d00;
}

.vip-price em {
  font-style: normal;
  font-size: 14px;
  margin-right: 2px;
}

.vip-desc {
  font-size: 12px;
  color: #86909c;
}

.channel-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.channel-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border: 1.5px solid #e5e6eb;
  border-radius: 10px;
  background: #fff;
  font-size: 14px;
  color: #1f2329;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.channel-item.selected {
  border-color: #ff7d00;
  box-shadow: 0 2px 10px rgba(255, 125, 0, 0.15);
}

.pay-btn {
  width: 100%;
  padding: 12px 0;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff9a2e, #ff7d00);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
}

.pay-btn:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-1px);
}

.pay-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===== 消息管理 / 用户管理（原后台管理） ===== */
.tip {
  text-align: center;
  color: #86909c;
  padding: 32px 0;
  font-size: 14px;
}

.tip-error {
  color: #f53f3f;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.search-input {
  width: 280px;
  max-width: 100%;
  padding: 8px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  font-size: 14px;
  color: #1f2329;
  background: #fff;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input:focus {
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.12);
}

.search-green:focus {
  border-color: #00b42a;
  box-shadow: 0 0 0 2px rgba(0, 180, 42, 0.12);
}

.role-select {
  margin-left: 12px;
  padding: 8px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  font-size: 14px;
  color: #1f2329;
  background: #fff;
  outline: none;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.role-select:focus {
  border-color: #00b42a;
  box-shadow: 0 0 0 2px rgba(0, 180, 42, 0.12);
}

.refresh-btn {
  margin-left: 12px;
  padding: 8px 14px;
  border: 1px solid #00b42a;
  border-radius: 6px;
  background: #fff;
  color: #00b42a;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background: #00b42a;
  color: #fff;
}

/* 批量操作条 */
.batch-bar {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-info {
  font-size: 13px;
  color: #4e5969;
  margin-right: 2px;
}

.op-btn {
  padding: 4px 12px;
  border: 1px solid #00b42a;
  border-radius: 6px;
  background: #fff;
  color: #00b42a;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.op-btn:hover:not(:disabled) {
  background: #00b42a;
  color: #fff;
}

.op-danger {
  border-color: #f53f3f;
  color: #f53f3f;
}

.op-danger:hover:not(:disabled) {
  background: #f53f3f;
  color: #fff;
}

.op-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-error {
  margin-bottom: 12px;
  padding: 10px 14px;
  background: #ffece8;
  border-radius: 6px;
  color: #f53f3f;
  font-size: 14px;
}

.sortable {
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
}

.sortable:hover {
  color: #165dff;
}

.sort-arrow {
  font-size: 12px;
  margin-left: 4px;
  opacity: 0.7;
}

.conv-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.conv-table th,
.conv-table td {
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f1f3;
}

.conv-table th {
  color: #86909c;
  font-weight: 500;
  background: #fafbfc;
}

.conv-table td {
  color: #1f2329;
}

/* 行悬停高亮：消息管理浅蓝、用户管理浅绿 */
.conv-table tbody tr {
  transition: background 0.15s;
}

.conv-table tbody tr:hover {
  background: rgba(22, 93, 255, 0.06);
}

.conv-table tbody tr.clickable {
  cursor: pointer;
}

.conv-table tbody tr.clickable:hover {
  background: rgba(22, 93, 255, 0.1);
}

.conv-table.user-table tbody tr:hover {
  background: rgba(0, 180, 42, 0.07);
}

.conv-table.user-table tbody tr.row-selected {
  background: rgba(0, 180, 42, 0.07);
}

.conv-link {
  color: #165dff;
  cursor: pointer;
  word-break: break-all;
}

.conv-link:hover {
  text-decoration: underline;
}

/* 消息数量气泡徽章 */
.count-badge {
  display: inline-block;
  min-width: 28px;
  padding: 2px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #165dff;
  background: rgba(22, 93, 255, 0.08);
  text-align: center;
  white-space: nowrap;
}

/* 复选框列 */
.col-check {
  width: 40px;
  text-align: center;
}

.col-check input {
  width: 15px;
  height: 15px;
  cursor: pointer;
  accent-color: #00b42a;
  vertical-align: middle;
}

.uid {
  font-size: 12px;
  color: #86909c;
  white-space: nowrap;
}

.time-cell {
  white-space: nowrap;
}

/* 角色与状态徽章 */
.role-badge,
.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.6;
  white-space: nowrap;
}

.role-admin {
  background: #ffece8;
  color: #f53f3f;
}

.role-user {
  background: #e8f3ff;
  color: #165dff;
}

.role-guest {
  background: #f2f3f5;
  color: #86909c;
}

.status-on {
  background: #e8ffea;
  color: #00b42a;
}

.status-off {
  background: #f7f8fa;
  color: #86909c;
}

/* ===== 支付弹窗 ===== */
.pay-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(31, 35, 41, 0.5);
}

.pay-dialog {
  position: relative;
  width: 320px;
  padding: 28px 24px 24px;
  border-radius: 12px;
  background: #fff;
  text-align: center;
}

.pay-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #86909c;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-close:hover {
  background: #f2f3f5;
  color: #1f2329;
}

.pay-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 8px;
}

.pay-amount {
  font-size: 13px;
  color: #86909c;
  margin-bottom: 16px;
}

.pay-amount strong {
  color: #ff7d00;
  font-size: 15px;
  margin-left: 4px;
}

.pay-qr {
  width: 200px;
  height: 200px;
  margin: 0 auto 12px;
  border: 1.5px dashed #e5e6eb;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #fafbfc;
}

.pay-qr img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pay-qr-placeholder {
  font-size: 12px;
  color: #86909c;
}

.pay-hint {
  font-size: 13px;
  color: #4e5969;
  margin-bottom: 6px;
}

.pay-status {
  font-size: 12px;
  color: #86909c;
}
</style>
