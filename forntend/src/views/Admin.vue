<template>
  <div class="admin">
    <header class="admin-header">
      <h1>后台管理</h1>
      <button class="back-btn" @click="router.push('/')">返回首页</button>
    </header>
    <main class="admin-main" :class="activeTab === 'messages' ? 'main-messages' : 'main-users'">
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
                <th class="sortable" @click="toggleSort('startTime')">
                  创建时间
                  <span class="sort-arrow">{{ sortArrow('startTime') }}</span>
                </th>
                <th class="sortable" @click="toggleSort('messageCount')">
                  消息数量
                  <span class="sort-arrow">{{ sortArrow('messageCount') }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="conv in filteredConversations" :key="conv.conversationId">
                <td>
                  <a class="conv-link" @click="goDetail(conv.conversationId)">
                    {{ conv.conversationId }}
                  </a>
                </td>
                <td>{{ formatTime(conv.startTime) }}</td>
                <td>{{ conv.messageCount }}</td>
              </tr>
            </tbody>
          </table>
        </template>

        <!-- 用户管理（占位） -->
        <template v-else>
          <p class="tip">用户管理功能建设中...</p>
          <div class="plan-box">
            <p class="plan-title">规划功能：</p>
            <ul class="plan-list">
              <li>用户列表（用户 ID、账号信息、创建时间等）</li>
              <li>搜索用户（按昵称、ID 等条件筛选）</li>
              <li>删除 / 禁用用户</li>
              <li>查看用户会话（跳转该用户的会话与消息记录）</li>
            </ul>
          </div>
        </template>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listConversations } from '../api'

const router = useRouter()

const tabs = [
  { key: 'messages', label: '消息管理' },
  { key: 'users', label: '用户管理' }
]
const activeTab = ref('messages')

const conversations = ref([])
const loading = ref(false)
const error = ref('')

// 搜索与排序状态
const searchText = ref('')
const sortField = ref('') // '' | 'startTime' | 'messageCount'
const sortOrder = ref(1) // 1 升序，-1 降序

const filteredConversations = computed(() => {
  let list = conversations.value
  const kw = searchText.value.toLowerCase()
  if (kw) {
    list = list.filter((c) => c.conversationId.toLowerCase().includes(kw))
  }
  if (sortField.value) {
    const field = sortField.value
    const order = sortOrder.value
    list = [...list].sort((a, b) => {
      const av = new Date(a[field]).getTime() || 0
      const bv = new Date(b[field]).getTime() || 0
      return (av - bv) * order
    })
  }
  return list
})

function toggleSort(field) {
  if (sortField.value === field) {
    if (sortOrder.value === 1) {
      sortOrder.value = -1 // 升序 -> 降序
    } else {
      sortField.value = '' // 降序 -> 取消排序
      sortOrder.value = 1
    }
  } else {
    sortField.value = field
    sortOrder.value = 1
  }
}

function sortArrow(field) {
  if (sortField.value !== field) return '↕'
  return sortOrder.value === 1 ? '↑' : '↓'
}

function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function goDetail(conversationId) {
  router.push(`/admin/conversation/${conversationId}`)
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await listConversations()
    conversations.value = res.data || []
  } catch (e) {
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
  padding: 8px 16px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  background: #fff;
  color: #4e5969;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
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
  max-width: 900px;
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

.plan-box {
  max-width: 480px;
  margin: 0 auto 32px;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 16px 20px;
}

.plan-title {
  font-size: 14px;
  font-weight: 600;
  color: #4e5969;
  margin-bottom: 8px;
}

.plan-list {
  padding-left: 20px;
  font-size: 13px;
  color: #86909c;
  line-height: 2;
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

.conv-link {
  color: #165dff;
  cursor: pointer;
  word-break: break-all;
}

.conv-link:hover {
  text-decoration: underline;
}
</style>
