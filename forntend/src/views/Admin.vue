<template>
  <div class="admin">
    <header class="admin-header">
      <h1>后台管理</h1>
      <button class="back-btn" @click="router.push('/')">
        <svg viewBox="0 0 24 24" width="14" height="14">
          <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回首页
      </button>
    </header>
    <main v-if="isAdmin" class="admin-main" :class="activeTab === 'messages' ? 'main-messages' : 'main-users'">
      <section class="panel" :class="activeTab === 'messages' ? 'panel-messages' : 'panel-users'">
        <div class="tab-bar">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="[{ active: activeTab === tab.key }, `tab-${tab.key}`]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- 消息管理 -->
        <template v-if="activeTab === 'messages'">
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

        <!-- 用户管理 -->
        <template v-else>
          <div v-if="!isAdmin" class="tip">
            用户管理仅限管理员访问，请先在聊天室使用管理员账号登录
          </div>
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
                <option value="ADMIN">ADMIN</option>
                <option value="USER">USER</option>
                <option value="GUEST">GUEST</option>
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
                        {{ u.role || 'USER' }}
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
        </template>
      </section>
    </main>
    <main v-else class="admin-main">
      <section class="panel">
        <p class="tip">⛔ 仅管理员账号可访问后台管理</p>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listConversations, listUsers, updateUser, removeUser } from '../api'
import { formatTime } from '../utils/formatTime'

const router = useRouter()

const tabs = [
  { key: 'messages', label: '消息管理' },
  { key: 'users', label: '用户管理' }
]
const activeTab = ref('messages')

const conversations = ref([])
const loading = ref(false)
const error = ref('')

// 搜索状态
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

function goDetail(conversationId) {
  router.push(`/admin/conversation/${conversationId}`)
}

// ===== 用户管理 =====
// 当前登录身份（聊天室登录后写入 sessionStorage）
const currentUser = (() => {
  try {
    return JSON.parse(sessionStorage.getItem('chat-user') || 'null')
  } catch {
    return null
  }
})()
const isAdmin = computed(() => currentUser?.role === 'ADMIN')

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
    .filter((id) => id !== currentUser?.id)
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

// 首次切到用户管理且为管理员时再加载
watch(activeTab, (tab) => {
  if (tab === 'users' && isAdmin.value && !usersLoaded.value) {
    loadUsers()
  }
})

onMounted(async () => {
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
})
</script>

<style scoped>
.admin {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f6f7;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
}

.admin-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2329;
}

.back-btn {
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

.admin-main {
  flex: 1;
  overflow: auto;
  padding: 24px;
  transition: background 0.3s;
}

/* 消息管理：蓝色调；用户管理：绿色调 */
.main-messages {
  background: #e8effa;
}

.main-users {
  background: #e6f5ec;
}

.panel {
  max-width: 1100px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-top: 3px solid transparent;
  transition: background 0.3s, border-color 0.3s;
}

.panel-messages {
  background: #f3f8ff;
  border-top-color: #165dff;
}

.panel-users {
  background: #f0fbf5;
  border-top-color: #00b42a;
}

.tab-bar {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #e5e6eb;
  margin-bottom: 16px;
}

.tab-item {
  padding: 10px 4px;
  margin-right: 16px;
  border: none;
  background: transparent;
  color: #4e5969;
  font-size: 15px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #165dff;
}

.tab-item.active {
  font-weight: 600;
}

.tab-messages.active {
  color: #165dff;
  border-bottom-color: #165dff;
}

.tab-users.active {
  color: #00b42a;
  border-bottom-color: #00b42a;
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

.toolbar {
  display: flex;
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

.conv-link {
  color: #165dff;
  cursor: pointer;
  word-break: break-all;
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

.conv-link:hover {
  text-decoration: underline;
}

/* ===== 用户管理 ===== */
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

/* 复选框列与选中行高亮 */
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

.conv-table.user-table tbody tr.row-selected {
  background: rgba(0, 180, 42, 0.07);
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

.uid {
  font-size: 12px;
  color: #86909c;
  white-space: nowrap;
}

.time-cell {
  white-space: nowrap;
}

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
</style>
