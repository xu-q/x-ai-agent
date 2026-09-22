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
          <!-- 对话管理 -->
          <svg v-else-if="tab.key === 'messages'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <!-- 系统通知 -->
          <svg v-else-if="tab.key === 'notice'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.7 21a2 2 0 0 1-3.4 0"/>
          </svg>
          <!-- 积分明细 -->
          <svg v-else-if="tab.key === 'points'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="9"/>
            <path d="M9 9.5l3 3.5 3-3.5"/>
            <path d="M12 13v4"/>
            <path d="M9.5 14.5h5"/>
          </svg>
          <!-- 统计管理 -->
          <svg v-else-if="tab.key === 'stats'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 21h18"/>
            <path d="M7 21v-7"/>
            <path d="M12 21V11"/>
            <path d="M17 21V5"/>
          </svg>
          <!-- 会员管理 -->
          <svg v-else-if="tab.key === 'plans'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <line x1="7" y1="7" x2="7.01" y2="7"/>
          </svg>
          <!-- 积分管理 -->
          <svg v-else-if="tab.key === 'pointsAdmin'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <ellipse cx="12" cy="5.5" rx="7" ry="2.5"/>
            <path d="M5 5.5v6.5c0 1.38 3.13 2.5 7 2.5s7-1.12 7-2.5V5.5"/>
            <path d="M5 12v6.5c0 1.38 3.13 2.5 7 2.5s7-1.12 7-2.5V12"/>
          </svg>
          <!-- 消息发布 -->
          <svg v-else-if="tab.key === 'publish'" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 11l18-5v12L3 14v-3z"/>
            <path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"/>
          </svg>
          <!-- 用户管理 -->
          <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          {{ tab.label }}
          <span v-if="tab.key === 'notice' && unreadCount" class="side-badge">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </button>
      </nav>
      <button class="back-btn" @click="router.push('/')">
        <svg viewBox="0 0 24 24" width="14" height="14">
          <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回首页
      </button>
      <button class="logout-btn" @click="doLogout">
        <svg viewBox="0 0 24 24" width="14" height="14">
          <path d="M15 12H4m0 0l4-4m-4 4l4 4M9 3h8a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H9" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        退出登录
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
            <p v-if="signSuccess" class="mini-tip tip-success">
              签到成功，已连续签到 {{ sign.continuousDays }} 天
              <template v-if="signPointsEarned != null">，+{{ signPointsEarned }} 积分</template>
            </p>
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
                <span class="info-value">
                  <span class="phone-wrap">
                    <template v-if="editingPhone">
                      <input
                        v-model="phoneDraft"
                        class="phone-input"
                        type="text"
                        maxlength="11"
                        placeholder="11 位手机号"
                        @keyup.enter="savePhone"
                      />
                      <button
                        class="phone-btn primary"
                        :disabled="phoneSaving || !/^1\d{10}$/.test(phoneDraft)"
                        @click="savePhone"
                      >{{ phoneSaving ? '保存中...' : '保存' }}</button>
                      <button class="phone-btn" @click="editingPhone = false">取消</button>
                    </template>
                    <template v-else>
                      {{ info.phone || '未绑定' }}
                      <button class="phone-btn" @click="startEditPhone">{{ info.phone ? '修改' : '绑定' }}</button>
                    </template>
                  </span>
                </span>
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
              <p v-if="phoneHint" class="mini-tip" :class="phoneHint.includes('失败') ? 'tip-warn' : 'tip-success'">{{ phoneHint }}</p>
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
              v-for="plan in onSalePlans"
              :key="plan.key"
              class="vip-card"
              :class="{ selected: selectedPlan === plan.key }"
              @click="selectedPlan = plan.key"
            >
              <span v-if="plan.originalPrice > plan.price" class="vip-origin">¥{{ plan.originalPrice }}</span>
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

        <!-- 系统通知（所有用户） -->
        <template v-else-if="activeTab === 'notice'">
          <div class="notice-toolbar">
            <div class="notice-filter">
              <button :class="{ on: noticeFilter === 'all' }" @click="noticeFilter = 'all'">全部</button>
              <button :class="{ on: noticeFilter === 'unread' }" @click="noticeFilter = 'unread'">
                未读{{ unreadCount ? ` (${unreadCount})` : '' }}
              </button>
            </div>
            <button class="read-all-btn" :disabled="!unreadCount" @click="handleReadAll">全部已读</button>
          </div>
          <p v-if="noticesLoading" class="tip">加载中...</p>
          <p v-else-if="noticesError" class="tip tip-error">{{ noticesError }}</p>
          <p v-else-if="filteredNotices.length === 0" class="tip">暂无通知</p>
          <ul v-else class="notice-timeline">
            <li
              v-for="item in displayNotices"
              :key="item.src.id"
              class="notice-item"
              :class="{ unread: !item.src.read }"
              @click="handleRead(item.src)"
            >
              <div class="notice-when" :class="{ today: item.today }">
                <span class="notice-date">{{ item.date }}</span>
                <span class="notice-time">{{ item.time }}</span>
              </div>
              <span class="notice-node"></span>
              <div class="notice-card">
                <div class="notice-head">
                  <span class="notice-type">{{ typeLabel(item.src.type) }}</span>
                  <span class="notice-title">{{ item.src.title }}</span>
                  <i v-if="!item.src.read" class="unread-dot"></i>
                </div>
                <p class="notice-content">{{ item.src.content }}</p>
              </div>
            </li>
          </ul>
          <p v-if="noticesHint" class="mini-tip tip-warn">{{ noticesHint }}</p>
        </template>
        <!-- 积分明细 -->
        <template v-else-if="activeTab === 'points'">
          <div class="points-balance-card">
            <div class="pb-left">
              <p class="pb-label">当前积分</p>
              <p class="pb-value">{{ pointsSummary.balance }}</p>
            </div>
            <div class="pb-right">
              <div class="pb-item">
                <p class="pb-label">累计获得</p>
                <p class="pb-num earn">+{{ pointsSummary.totalEarned }}</p>
              </div>
              <div class="pb-item">
                <p class="pb-label">累计消费</p>
                <p class="pb-num spend">-{{ pointsSummary.totalSpent }}</p>
              </div>
            </div>
          </div>
          <div class="points-filters">
            <button
              v-for="f in [['ALL', '全部'], ['EARN', '收入'], ['SPEND', '支出']]"
              :key="f[0]"
              class="points-filter-btn"
              :class="{ active: pointsFilter === f[0] }"
              @click="pointsFilter = f[0]"
            >{{ f[1] }}</button>
          </div>
          <ul class="points-list">
            <li v-for="r in filteredPointsRecords" :key="r.id" class="points-item">
              <span class="points-type" :class="`pt-${r.type.toLowerCase()}`">{{ recordMeta(r.type) }}</span>
              <div class="points-info">
                <p class="points-title">{{ r.title }}</p>
                <p class="points-time">{{ r.createTime }}</p>
              </div>
              <span class="points-delta" :class="r.points > 0 ? 'earn' : 'spend'">{{ fmtPoints(r.points) }}</span>
            </li>
            <li v-if="!filteredPointsRecords.length" class="points-empty">暂无积分记录</li>
          </ul>
          <p v-if="pointsHint" class="mini-tip tip-warn">{{ pointsHint }}</p>
        </template>

        <!-- 对话管理（仅管理员） -->
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
                  对话数量
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
        <template v-else-if="activeTab === 'users'">
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

        <!-- 统计管理（仅管理员） -->
        <template v-else-if="activeTab === 'stats'">
          <div class="stats-toolbar">
            <div class="range-switch">
              <button :class="{ on: trendDays === 7 }" @click="setRange(7)">近 7 天</button>
              <button :class="{ on: trendDays === 30 }" @click="setRange(30)">近 30 天</button>
            </div>
          </div>

          <!-- 指标卡 -->
          <div class="stat-cards">
            <div v-for="c in statCards" :key="c.label" class="stat-card">
              <span class="stat-label">{{ c.label }}</span>
              <strong class="stat-value">{{ fmtStat(c.value) }}</strong>
              <span class="stat-diff" :class="c.diff >= 0 ? 'up' : 'down'">
                {{ c.diff >= 0 ? '↑' : '↓' }} {{ Math.abs(c.diff).toFixed(1) }}% 较昨日
              </span>
            </div>
          </div>

          <!-- 趋势图 -->
          <div class="chart-grid">
            <div class="chart-card">
              <div class="chart-title">活跃与签到趋势</div>
              <SvgChart :labels="trend.dates" :series="activeSeries" :height="240" />
            </div>
            <div class="chart-card">
              <div class="chart-title">用户增长趋势</div>
              <SvgChart :labels="trend.dates" :series="userSeries" :height="240" />
            </div>
            <div class="chart-card wide">
              <div class="chart-title">对话数量趋势</div>
              <SvgChart :labels="trend.dates" :series="msgSeries" :height="220" />
            </div>
          </div>

          <p v-if="statsHint" class="mini-tip tip-warn">{{ statsHint }}</p>
        </template>
        <!-- 会员管理 -->
        <template v-else-if="activeTab === 'plans'">
          <div class="plans-grid">
            <div v-for="p in adminPlans" :key="p.key" class="plan-card" :class="{ off: p.onSale === false }">
              <div class="plan-name">{{ p.name }}</div>
              <div class="plan-price">
                <strong>¥{{ p.price }}</strong>
                <s v-if="p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</s>
              </div>
              <div class="plan-meta">{{ p.days }}天 · {{ p.desc }}</div>
              <span class="plan-state" :class="p.onSale === false ? 'is-off' : 'is-on'">
                {{ p.onSale === false ? '已下架' : '上架中' }}
              </span>
              <button class="plan-edit-btn" @click="openPlanEdit(p)">调整价格</button>
            </div>
          </div>
          <p v-if="adminPlansHint" class="mini-tip tip-warn">{{ adminPlansHint }}</p>
        </template>
        <!-- 积分管理 -->
        <template v-else-if="activeTab === 'pointsAdmin'">
          <div class="rules-card">
            <div class="rules-head">
              <h3 class="rules-title">积分规则</h3>
              <button class="rules-save-btn" :disabled="rulesSaving" @click="saveRules">
                {{ rulesSaving ? '保存中...' : '保存规则' }}
              </button>
            </div>
            <div class="rules-grid">
              <label class="plan-field">
                <span>签到基础分</span>
                <input v-model.number="pointsRules.signInBase" type="number" min="0" />
              </label>
              <label class="plan-field">
                <span>连续每天加成</span>
                <input v-model.number="pointsRules.signInBonusPerDay" type="number" min="0" />
              </label>
              <label class="plan-field">
                <span>单日封顶</span>
                <input v-model.number="pointsRules.signInMax" type="number" min="1" />
              </label>
            </div>
            <p v-if="rulesHint" class="mini-tip">{{ rulesHint }}</p>
          </div>
          <div class="pub-toolbar">
            <input v-model="pointsKeyword" class="rules-search" type="text" placeholder="按用户名搜索" />
          </div>
          <div class="table-wrap">
            <table class="pub-table">
              <thead>
                <tr>
                  <th>用户名</th>
                  <th>当前余额</th>
                  <th>累计获得</th>
                  <th>累计消费</th>
                  <th>最近变动</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in filteredPointsUsers" :key="u.userId">
                  <td class="pub-title-cell">{{ u.username }}</td>
                  <td><strong class="pt-balance">{{ u.balance }}</strong></td>
                  <td class="pt-earn">+{{ u.totalEarned }}</td>
                  <td class="pt-spend">-{{ u.totalSpent }}</td>
                  <td>{{ u.lastChangeTime }}</td>
                  <td>
                    <button class="pub-op primary" @click="openPointAdjust(u)">调整</button>
                  </td>
                </tr>
                <tr v-if="!filteredPointsUsers.length">
                  <td colspan="6" class="pub-empty">暂无用户</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-if="pointsUsersHint" class="mini-tip tip-warn">{{ pointsUsersHint }}</p>
        </template>
        <!-- 消息发布 -->
        <template v-else-if="activeTab === 'publish'">
          <div class="pub-toolbar">
            <div class="pub-filters">
              <select v-model="noticeTypeFilter" class="pub-select">
                <option value="ALL">全部类型</option>
                <option value="SYSTEM">系统</option>
                <option value="ACTIVITY">活动</option>
                <option value="UPDATE">更新</option>
              </select>
              <select v-model="noticeStatusFilter" class="pub-select">
                <option value="ALL">全部状态</option>
                <option value="DRAFT">草稿</option>
                <option value="PUBLISHED">已发布</option>
                <option value="WITHDRAWN">已撤回</option>
              </select>
            </div>
            <button class="pub-create-btn" @click="openNoticeCreate">+ 新建通知</button>
          </div>
          <div class="pub-batch-bar">
            <span class="pub-batch-count">已选 {{ selectedNoticeIds.length }} 项</span>
            <button class="pub-op primary" :disabled="!selectedNoticeIds.length" @click="batchSetNoticeStatus('publish')">批量发布</button>
            <button class="pub-op warn" :disabled="!selectedNoticeIds.length" @click="batchSetNoticeStatus('withdraw')">批量撤回</button>
            <button class="pub-op danger" :disabled="!selectedNoticeIds.length" @click="batchRemoveNotices()">批量删除</button>
          </div>
          <div class="table-wrap">
            <table class="pub-table">
              <thead>
                <tr>
                  <th class="pub-check-col">
                    <input type="checkbox" :checked="noticeAllSelected" @change="toggleNoticeAll" />
                  </th>
                  <th>标题</th>
                  <th>类型</th>
                  <th>范围</th>
                  <th>状态</th>
                  <th>已读 / 发送</th>
                  <th>创建时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="n in filteredAdminNotices" :key="n.id">
                  <td class="pub-check-col">
                    <input v-model="selectedNoticeIds" type="checkbox" :value="n.id" />
                  </td>
                  <td class="pub-title-cell" @click="openNoticeEdit(n)">{{ n.title }}</td>
                  <td><span class="pub-type" :class="`tt-${n.type.toLowerCase()}`">{{ typeLabel(n.type) }}</span></td>
                  <td>{{ scopeLabels[n.scope] || '全员' }}</td>
                  <td>
                    <span class="pub-status" :class="`st-${n.status.toLowerCase()}`">{{ statusLabels[n.status] }}</span>
                  </td>
                  <td><span class="pub-read-badge">{{ n.readCount }}/{{ n.totalCount }}</span></td>
                  <td>{{ n.createTime }}</td>
                </tr>
                <tr v-if="!filteredAdminNotices.length">
                  <td colspan="7" class="pub-empty">暂无符合条件的通知</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-if="adminNoticesHint" class="mini-tip tip-warn">{{ adminNoticesHint }}</p>
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

    <!-- 调价弹窗 -->
    <div v-if="planEditVisible" class="pay-mask" @click.self="planEditVisible = false">
      <div class="pay-dialog plan-dialog">
        <button class="pay-close" title="关闭" @click="planEditVisible = false">
          <svg viewBox="0 0 24 24" width="14" height="14">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <h2 class="pay-title">调整价格 - {{ planForm.name }}</h2>
        <div class="plan-form">
          <label class="plan-field">
            <span>现价（元）</span>
            <input v-model.number="planForm.price" type="number" min="1" />
          </label>
          <label class="plan-field">
            <span>划线原价（元）</span>
            <input v-model.number="planForm.originalPrice" type="number" min="0" />
          </label>
          <label class="plan-field">
            <span>时长（天）</span>
            <input v-model.number="planForm.days" type="number" min="1" />
          </label>
          <label class="plan-field">
            <span>权益描述</span>
            <input v-model="planForm.desc" type="text" placeholder="展示在套餐卡上的一句话" />
          </label>
          <label class="plan-field row">
            <span>上架销售</span>
            <input v-model="planForm.onSale" type="checkbox" />
          </label>
        </div>
        <button class="plan-save-btn" :disabled="planSaving || !(planForm.price > 0)" @click="savePlan">
          {{ planSaving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>

    <!-- 新建/编辑通知弹窗 -->
    <div v-if="noticeModalVisible" class="pay-mask" @click.self="noticeModalVisible = false">
      <div class="pay-dialog plan-dialog">
        <button class="pay-close" title="关闭" @click="noticeModalVisible = false">
          <svg viewBox="0 0 24 24" width="14" height="14">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <h2 class="pay-title">{{ noticeForm.id == null ? '新建通知' : '编辑通知' }}</h2>
        <div class="plan-form">
          <label class="plan-field">
            <span>类型</span>
            <select v-model="noticeForm.type" class="pub-select">
              <option value="SYSTEM">系统</option>
              <option value="ACTIVITY">活动</option>
              <option value="UPDATE">更新</option>
            </select>
          </label>
          <label class="plan-field">
            <span>发布范围</span>
            <select v-model="noticeForm.scope" class="pub-select">
              <option value="ALL">全员</option>
              <option value="VIP">仅会员</option>
              <option value="GUEST">仅游客</option>
            </select>
          </label>
          <label class="plan-field">
            <span>标题</span>
            <input v-model="noticeForm.title" type="text" maxlength="30" placeholder="不超过 30 字" />
          </label>
          <label class="plan-field">
            <span>内容</span>
            <textarea v-model="noticeForm.content" rows="4" placeholder="通知正文"></textarea>
          </label>
        </div>
        <p class="pub-form-tip">保存后为草稿，可在列表中发布</p>
        <button class="plan-save-btn" :disabled="noticeSaving || !noticeForm.title.trim() || !noticeForm.content.trim()" @click="saveNotice">
          {{ noticeSaving ? '保存中...' : '保存草稿' }}
        </button>
      </div>
    </div>

    <!-- 调整积分弹窗 -->
    <div v-if="pointAdjustVisible" class="pay-mask" @click.self="pointAdjustVisible = false">
      <div class="pay-dialog plan-dialog">
        <button class="pay-close" title="关闭" @click="pointAdjustVisible = false">
          <svg viewBox="0 0 24 24" width="14" height="14">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <h2 class="pay-title">调整积分 - {{ pointAdjustForm.username }}</h2>
        <div class="plan-form">
          <div class="plan-field row radio-row">
            <label class="radio-item">
              <input v-model="pointAdjustForm.mode" type="radio" value="add" />
              加分
            </label>
            <label class="radio-item">
              <input v-model="pointAdjustForm.mode" type="radio" value="sub" />
              减分
            </label>
          </div>
          <label class="plan-field">
            <span>积分数量</span>
            <input v-model.number="pointAdjustForm.points" type="number" min="1" />
          </label>
          <label class="plan-field">
            <span>原因备注</span>
            <input v-model="pointAdjustForm.reason" type="text" maxlength="50" placeholder="如：活动补偿（必填）" />
          </label>
        </div>
        <button
          class="plan-save-btn indigo"
          :disabled="pointAdjustSaving || !(pointAdjustForm.points > 0) || !pointAdjustForm.reason.trim()"
          @click="submitPointAdjust"
        >
          {{ pointAdjustSaving ? '提交中...' : '提交调整' }}
        </button>
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
  updateMyProfile,
  getSignInfo,
  doSign,
  getStatsOverview,
  getStatsTrend,
  getNotices,
  markNoticeRead,
  markAllNoticesRead,
  getMembership,
  getMembershipPlans,
  createPayOrder,
  getPayStatus,
  listMembershipPlans,
  updateMembershipPlan,
  listAdminNotices,
  createAdminNotice,
  updateAdminNotice,
  toggleAdminNotice,
  removeAdminNotice,
  getUserPointsSummary,
  getUserPointsRecords,
  getPointsRules,
  savePointsRules,
  listPointsUsers,
  adjustUserPoints,
  listConversations,
  listUsers,
  updateUser,
  removeUser,
  userLogout,
  clearAuth
} from '../api'
import { formatTime } from '../utils/formatTime'
import SvgChart from '../components/SvgChart.vue'

const router = useRouter()

// 退出登录：调后端（静默容错）→ 清本地身份 → 回首页
async function doLogout() {
  try {
    await userLogout()
  } catch { /* 后端失败也照常清理本地 */ }
  clearAuth()
  router.push('/')
}

// 当前登录身份（精简对象 {id, name, role, avatar?}）
const user = ref(JSON.parse(sessionStorage.getItem('chat-user') || 'null'))
const isAdmin = computed(() => user.value?.role === 'ADMIN')

const baseTabs = [
  { key: 'profile', label: '个人中心' },
  { key: 'vip', label: '会员中心' },
  { key: 'points', label: '积分明细' },
  { key: 'notice', label: '系统通知' }
]
const adminTabs = [
  { key: 'messages', label: '对话管理' },
  { key: 'users', label: '用户管理' },
  { key: 'stats', label: '统计管理' },
  { key: 'plans', label: '会员管理' },
  { key: 'pointsAdmin', label: '积分管理' },
  { key: 'publish', label: '消息发布' }
]
// 仅管理员展示后台管理类 tab
const tabs = computed(() => (isAdmin.value ? [...baseTabs, ...adminTabs] : baseTabs))
// 默认页：管理员进统计管理，其他用户进个人中心
const activeTab = ref(isAdmin.value ? 'stats' : 'profile')

const roleLabels = { ADMIN: '管理员', USER: '用户', GUEST: '游客' }
const roleLabel = (role) => roleLabels[role] || '用户'

// ===== 基本资料 =====
const info = ref({ name: user.value?.name, role: user.value?.role })
const infoError = ref('')

async function loadInfo() {
  try {
    const res = await getUserInfo()
    if (res.data) {
      info.value = res.data
      // 同步本地身份，首页胶囊 / 聊天室头像共用（token 传 null 不覆盖）
      const merged = { ...user.value, ...res.data, name: res.data.username || user.value?.name }
      user.value = merged
      saveAuthUser(merged, null)
    }
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    infoError.value = '完整资料需后端接口支持，当前展示本地信息'
  }
}

// 手机号绑定 / 修改（PUT /user/profile 仅允许更新头像与手机号）
const editingPhone = ref(false)
const phoneDraft = ref('')
const phoneSaving = ref(false)
const phoneHint = ref('')

function startEditPhone() {
  phoneDraft.value = info.value.phone || ''
  phoneHint.value = ''
  editingPhone.value = true
}

async function savePhone() {
  const phone = phoneDraft.value.trim()
  if (!/^1\d{10}$/.test(phone)) return
  phoneSaving.value = true
  phoneHint.value = ''
  try {
    const res = await updateMyProfile({ phone })
    info.value.phone = res.data?.phone || phone
    // 同步本地身份（token 传 null 不覆盖）
    user.value = { ...user.value, phone: info.value.phone }
    saveAuthUser(user.value, null)
    phoneHint.value = '手机号已更新'
    editingPhone.value = false
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    phoneHint.value = e.response?.status === 400 ? e.response.data?.message || '手机号格式不正确' : '保存失败，请稍后再试'
  } finally {
    phoneSaving.value = false
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
    // 后端 R<String>：data 直接是头像 URL
    const url = (typeof res.data === 'string' ? res.data : res.data?.data || res.data?.avatar) || ''
    if (!url) throw new Error('响应缺少头像地址')
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
    // 上传成功后调用用户信息更新接口落库
    try {
      await updateMyProfile({ avatar: url })
    } catch (e2) {
      if (e2.response?.status === 401) {
        router.push('/')
        return
      }
      // 落库失败不阻塞：本地仍生效，仅提示
      uploadError.value = '头像已上传但保存失败，重新登录后可能丢失'
    }
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
        : e.response?.status === 500
          ? '上传失败，请检查图片后重试'
          : '头像上传失败，请稍后再试'
  } finally {
    uploading.value = false
  }
}

// ===== 每日签到 =====
const sign = ref({ signedToday: false, continuousDays: 0, monthDays: 0, recentDates: [] })
const signLoading = ref(false)
const signError = ref('')
const signSuccess = ref(false)
// 签到获得的积分（后端返回 earnedPoints 时展示）
const signPointsEarned = ref(null)

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
    // 签到送积分：后端返回 earnedPoints 时同步积分概览
    if (res.data?.earnedPoints != null) {
      signPointsEarned.value = res.data.earnedPoints
      pointsSummary.value.balance += res.data.earnedPoints
      pointsSummary.value.totalEarned += res.data.earnedPoints
    }
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
// 默认价兜底；接口就绪后以管理端调价结果为准
const DEFAULT_PLANS = [
  { key: 'MONTH', name: '月卡', price: 25, originalPrice: 35, days: 30, desc: '按月开通', onSale: true },
  { key: 'QUARTER', name: '季卡', price: 68, originalPrice: 90, days: 90, desc: '折合 ¥22.7/月', onSale: true },
  { key: 'YEAR', name: '年卡', price: 238, originalPrice: 300, days: 365, desc: '折合 ¥19.8/月', onSale: true }
]
const plans = ref(DEFAULT_PLANS.map((p) => ({ ...p })))
// 用户侧仅展示上架套餐
const onSalePlans = computed(() => plans.value.filter((p) => p.onSale !== false))
const selectedPlan = ref('MONTH')
const selectedPlanInfo = computed(() => plans.value.find((p) => p.key === selectedPlan.value))

async function loadPlans() {
  try {
    const res = await getMembershipPlans()
    const list = res.data?.list
    if (Array.isArray(list) && list.length) {
      plans.value = list
      const cur = plans.value.find((p) => p.key === selectedPlan.value)
      if (!cur || cur.onSale === false) {
        selectedPlan.value = plans.value.find((p) => p.onSale !== false)?.key || selectedPlan.value
      }
    }
  } catch { /* 后端未就绪时用默认价 */ }
}

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

// ===== 对话管理（原后台管理） =====
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

// ===== 统计管理 =====
const statsOverview = ref({
  todayActive: 0, todaySign: 0, totalUsers: 0, todayConversations: 0, diffs: {}
})
const trend = ref({ dates: [], activeCounts: [], signCounts: [], newUsers: [], userTotals: [], messageCounts: [] })
const trendDays = ref(7)
const statsHint = ref('')
const statsLoaded = ref(false)

const statCards = computed(() => {
  const o = statsOverview.value
  const d = o.diffs || {}
  return [
    { label: '今日上线人数', value: o.todayActive, diff: d.active ?? 0 },
    { label: '今日签到人数', value: o.todaySign, diff: d.sign ?? 0 },
    { label: '用户总数量', value: o.totalUsers, diff: d.users ?? 0 },
    { label: '今日对话数', value: o.todayConversations, diff: d.conversations ?? 0 }
  ]
})
const fmtStat = (v) => (typeof v === 'number' ? v.toLocaleString() : '—')

// 三张图的系列配置（页面青色主题，配色区分指标）
const activeSeries = computed(() => [
  { name: '上线人数', color: '#0fc6c2', data: trend.value.activeCounts, type: 'line' },
  { name: '签到人数', color: '#722ed1', data: trend.value.signCounts, type: 'line' }
])
const userSeries = computed(() => [
  { name: '用户总量', color: '#3491fa', data: trend.value.userTotals, type: 'area' },
  { name: '每日新增', color: '#ff9a2e', data: trend.value.newUsers, type: 'bar' }
])
const msgSeries = computed(() => [
  { name: '对话数量', color: '#0fc6c2', data: trend.value.messageCounts, type: 'bar' }
])

// 后端未接入时的示例数据（页面明确标注）
function mockOverview() {
  return {
    todayActive: 126, todaySign: 89, totalUsers: 1284, todayConversations: 326,
    diffs: { active: 12.5, sign: -4.2, users: 1.8, conversations: 8.4 }
  }
}

function mockTrend(days) {
  const dates = [], activeCounts = [], signCounts = [], newUsers = [], userTotals = [], messageCounts = []
  let total = 1150 + Math.round(Math.random() * 100)
  const now = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    dates.push(`${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')}`)
    const wave = Math.sin(i / 2.5) * 0.3 + Math.sin(i / 7) * 0.2
    const active = Math.round(110 + wave * 45 + Math.random() * 25)
    activeCounts.push(active)
    signCounts.push(Math.round(active * (0.6 + Math.random() * 0.2)))
    const nu = 3 + Math.round(Math.random() * 8)
    newUsers.push(nu)
    total += nu
    userTotals.push(total)
    messageCounts.push(active * (12 + Math.round(Math.random() * 10)))
  }
  return { dates, activeCounts, signCounts, newUsers, userTotals, messageCounts }
}

async function loadStatsOverview() {
  try {
    const res = await getStatsOverview()
    if (res.data) {
      statsOverview.value = res.data
      statsHint.value = ''
    }
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    statsOverview.value = mockOverview()
    statsHint.value = '统计接口未接入，当前展示示例数据'
  }
}

async function loadTrend() {
  try {
    const res = await getStatsTrend(trendDays.value)
    if (res.data) {
      trend.value = res.data
      statsHint.value = ''
    }
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    trend.value = mockTrend(trendDays.value)
    statsHint.value = '统计接口未接入，当前展示示例数据'
  }
}

function setRange(days) {
  if (days === trendDays.value) return
  trendDays.value = days
  loadTrend()
}

// ===== 系统通知 =====
const notices = ref([])
const noticesLoading = ref(false)
const noticesError = ref('')
const noticesHint = ref('')
const noticeFilter = ref('all')

const unreadCount = computed(() => notices.value.filter((n) => !n.read).length)
const filteredNotices = computed(() =>
  noticeFilter.value === 'unread' ? notices.value.filter((n) => !n.read) : notices.value
)
const typeLabels = { SYSTEM: '系统', ACTIVITY: '活动', UPDATE: '更新' }
const typeLabel = (t) => typeLabels[t] || '系统'

// 时间轴左侧时间：今日显示「今日 + HH:MM」，历史显示「MM/DD + HH:MM」
function noticeWhen(t) {
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return { date: '--/--', time: '--:--', today: false }
  const now = new Date()
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
  const p2 = (v) => String(v).padStart(2, '0')
  return {
    date: sameDay ? '今日' : `${p2(d.getMonth() + 1)}/${p2(d.getDate())}`,
    time: `${p2(d.getHours())}:${p2(d.getMinutes())}`,
    today: sameDay
  }
}

// 装饰后的列表：保留原对象引用（已读状态仍响应），附加时间列展示字段
const displayNotices = computed(() =>
  filteredNotices.value.map((n) => ({ src: n, ...noticeWhen(n.createTime) }))
)

// 后端未接入时的示例数据
function mockNotices() {
  const mk = (minAgo, type, title, content, read) => ({
    id: `demo-${minAgo}`,
    type,
    title,
    content,
    read,
    createTime: new Date(Date.now() - minAgo * 60000).toISOString()
  })
  return {
    list: [
      mk(30, 'SYSTEM', '系统维护通知', '平台将于今晚 23:00 - 24:00 进行例行维护，期间服务可能出现短暂波动。', false),
      mk(180, 'ACTIVITY', '会员限时优惠', '年卡会员限时 8 折，进入会员中心即可参与。', false),
      mk(1440, 'UPDATE', '功能更新', '个人中心新增每日签到功能，快来连续签到赢好礼。', true),
      mk(2880, 'SYSTEM', '安全提醒', '请勿向任何人泄露您的账号密码与短信验证码。', true),
      mk(4320, 'ACTIVITY', '邀请有礼', '邀请好友注册，双方均可获得 3 天会员体验。', true),
      mk(7200, 'UPDATE', '对话导出上线', '对话记录已支持导出，进入对话详情页即可一键保存。', true)
    ]
  }
}

async function loadNotices() {
  noticesLoading.value = true
  noticesError.value = ''
  try {
    const res = await getNotices()
    notices.value = res.data?.list || []
    noticesHint.value = ''
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    notices.value = mockNotices().list
    noticesHint.value = '通知接口未接入，当前展示示例数据'
  } finally {
    noticesLoading.value = false
  }
}

// 点击置为已读：本地即时生效，接口静默容错（后端未接入不阻塞交互）
function handleRead(n) {
  if (n.read) return
  n.read = true
  markNoticeRead(n.id).catch(() => {})
}

function handleReadAll() {
  notices.value.forEach((n) => (n.read = true))
  markAllNoticesRead().catch(() => {})
}

// 管理类 tab 首次进入时再加载（仅管理员；immediate 覆盖默认 tab 的场景）；通知在页面加载时请求
watch(
  activeTab,
  (tab) => {
    if (!isAdmin.value) return
    if (tab === 'users' && !usersLoaded.value) {
      loadUsers()
    }
    if (tab === 'stats' && !statsLoaded.value) {
      statsLoaded.value = true
      loadStatsOverview()
      loadTrend()
    }
    if (tab === 'plans' && !adminPlansLoaded.value) {
      loadAdminPlans()
    }
    if (tab === 'publish' && !adminNoticesLoaded.value) {
      loadAdminNotices()
    }
    if (tab === 'points' && !pointsLoaded.value) {
      loadPoints()
    }
    if (tab === 'pointsAdmin' && !pointsAdminLoaded.value) {
      loadPointsRules()
      loadPointsUsers()
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadInfo()
  loadSignInfo()
  loadNotices()
  loadPlans()
  loadMembership()
  if (isAdmin.value) loadConversations()
})

// ===== 会员管理（仅管理员）=====
const adminPlans = ref([])
const adminPlansLoaded = ref(false)
const adminPlansHint = ref('')
const planEditVisible = ref(false)
const planSaving = ref(false)
const planForm = ref({ key: '', name: '', price: 0, originalPrice: 0, days: 30, desc: '', onSale: true })

async function loadAdminPlans() {
  adminPlansLoaded.value = true
  adminPlansHint.value = ''
  try {
    const res = await listMembershipPlans()
    const list = res.data?.list
    adminPlans.value = Array.isArray(list) && list.length ? list : DEFAULT_PLANS.map((p) => ({ ...p }))
    if (!Array.isArray(list)) adminPlansHint.value = '会员接口未接入，当前展示默认套餐'
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    adminPlans.value = DEFAULT_PLANS.map((p) => ({ ...p }))
    adminPlansHint.value = '会员接口未接入，当前展示默认套餐'
  }
}

function openPlanEdit(plan) {
  planForm.value = { ...plan }
  planEditVisible.value = true
}

async function savePlan() {
  const f = planForm.value
  if (!(f.price > 0)) return
  planSaving.value = true
  try {
    await updateMembershipPlan(f.key, {
      price: f.price,
      originalPrice: f.originalPrice,
      days: f.days,
      desc: f.desc,
      onSale: f.onSale
    })
    applyPlanChange(f)
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    // 后端未就绪：本地生效并同步用户侧展示
    applyPlanChange(f)
  } finally {
    planSaving.value = false
    planEditVisible.value = false
  }
}

// 调价后同步管理卡片与用户侧套餐卡
function applyPlanChange(f) {
  adminPlans.value = adminPlans.value.map((p) => (p.key === f.key ? { ...p, ...f } : p))
  plans.value = plans.value.map((p) => (p.key === f.key ? { ...p, ...f } : p))
}

// ===== 消息发布（仅管理员）=====
const scopeLabels = { ALL: '全员', VIP: '仅会员', GUEST: '仅游客' }
const statusLabels = { DRAFT: '草稿', PUBLISHED: '已发布', WITHDRAWN: '已撤回' }
const DEMO_ADMIN_NOTICES = [
  { id: 1, type: 'SYSTEM', title: '系统维护通知', content: '平台将于今晚 23:00 - 24:00 进行例行维护，期间服务可能出现短暂波动。', scope: 'ALL', status: 'PUBLISHED', readCount: 96, totalCount: 128, createTime: '2026-09-21 23:46' },
  { id: 2, type: 'ACTIVITY', title: '会员限时优惠', content: '年卡会员限时 8 折，进入会员中心即可参与。', scope: 'ALL', status: 'PUBLISHED', readCount: 64, totalCount: 128, createTime: '2026-09-21 21:22' },
  { id: 3, type: 'UPDATE', title: '功能更新', content: '个人中心新增每日签到功能，快来连续签到赢好礼。', scope: 'ALL', status: 'PUBLISHED', readCount: 128, totalCount: 128, createTime: '2026-09-21 00:22' },
  { id: 4, type: 'ACTIVITY', title: '双倍积分周末', content: '本周末签到可得双倍积分，记得每天回来签到。', scope: 'VIP', status: 'DRAFT', readCount: 0, totalCount: 0, createTime: '2026-09-20 18:40' },
  { id: 5, type: 'SYSTEM', title: '新版本灰度发布', content: '新版本已开始灰度发布，如遇问题请及时反馈。', scope: 'ALL', status: 'WITHDRAWN', readCount: 30, totalCount: 126, createTime: '2026-09-19 10:05' }
]
const adminNotices = ref([])
const adminNoticesLoaded = ref(false)
const adminNoticesHint = ref('')
const noticeTypeFilter = ref('ALL')
const noticeStatusFilter = ref('ALL')
const noticeModalVisible = ref(false)
const noticeSaving = ref(false)
const noticeForm = ref({ id: null, type: 'SYSTEM', title: '', content: '', scope: 'ALL' })

const filteredAdminNotices = computed(() =>
  adminNotices.value.filter(
    (n) =>
      (noticeTypeFilter.value === 'ALL' || n.type === noticeTypeFilter.value) &&
      (noticeStatusFilter.value === 'ALL' || n.status === noticeStatusFilter.value)
  )
)

async function loadAdminNotices() {
  adminNoticesLoaded.value = true
  adminNoticesHint.value = ''
  try {
    const res = await listAdminNotices()
    const list = res.data?.list
    adminNotices.value = Array.isArray(list) && list.length ? list : DEMO_ADMIN_NOTICES
    if (!Array.isArray(list)) adminNoticesHint.value = '通知接口未接入，当前展示示例数据'
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    adminNotices.value = DEMO_ADMIN_NOTICES
    adminNoticesHint.value = '通知接口未接入，当前展示示例数据'
  }
}

function openNoticeCreate() {
  noticeForm.value = { id: null, type: 'SYSTEM', title: '', content: '', scope: 'ALL' }
  noticeModalVisible.value = true
}

function openNoticeEdit(n) {
  noticeForm.value = { id: n.id, type: n.type, title: n.title, content: n.content, scope: n.scope }
  noticeModalVisible.value = true
}

async function saveNotice() {
  const f = noticeForm.value
  if (!f.title.trim() || !f.content.trim()) return
  noticeSaving.value = true
  const now = formatTime(new Date())
  try {
    if (f.id == null) await createAdminNotice(f)
    else await updateAdminNotice(f.id, f)
    applyNoticeSave(f, now)
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    // 后端未就绪：本地生效
    applyNoticeSave(f, now)
  } finally {
    noticeSaving.value = false
    noticeModalVisible.value = false
  }
}

function applyNoticeSave(f, now) {
  if (f.id == null) {
    adminNotices.value = [
      { ...f, id: Date.now(), status: 'DRAFT', readCount: 0, totalCount: 0, createTime: now },
      ...adminNotices.value
    ]
  } else {
    adminNotices.value = adminNotices.value.map((n) => (n.id === f.id ? { ...n, ...f } : n))
  }
}

// 复选框批量操作
const selectedNoticeIds = ref([])
const noticeAllSelected = computed(
  () =>
    filteredAdminNotices.value.length > 0 &&
    filteredAdminNotices.value.every((n) => selectedNoticeIds.value.includes(n.id))
)

function toggleNoticeAll(e) {
  selectedNoticeIds.value = e.target.checked ? filteredAdminNotices.value.map((n) => n.id) : []
}

// 批量发布/撤回：publish 对非已发布生效，withdraw 对已发布生效
async function batchSetNoticeStatus(action) {
  const ids = selectedNoticeIds.value
  if (!ids.length) return
  try {
    await Promise.all(ids.map((id) => toggleAdminNotice(id, action)))
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    // 后端未就绪：本地生效
  }
  adminNotices.value = adminNotices.value.map((n) => {
    if (!ids.includes(n.id)) return n
    if (action === 'publish') {
      return n.status === 'PUBLISHED'
        ? n
        : { ...n, status: 'PUBLISHED', readCount: 0, totalCount: 128 }
    }
    return n.status === 'PUBLISHED' ? { ...n, status: 'WITHDRAWN' } : n
  })
  selectedNoticeIds.value = []
}

async function batchRemoveNotices() {
  const ids = selectedNoticeIds.value
  if (!ids.length) return
  try {
    await Promise.all(ids.map((id) => removeAdminNotice(id)))
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    // 后端未就绪：本地生效
  }
  adminNotices.value = adminNotices.value.filter((n) => !ids.includes(n.id))
  selectedNoticeIds.value = []
}

// ===== 积分明细（用户）=====
const pointsSummary = ref({ balance: 0, totalEarned: 0, totalSpent: 0 })
const pointsRecords = ref([])
const pointsLoaded = ref(false)
const pointsHint = ref('')
const pointsFilter = ref('ALL')

const DEMO_POINTS_RECORDS = [
  { id: 1, type: 'SIGN_IN', title: '每日签到（连续 3 天）', points: 8, createTime: '2026-09-21 08:30' },
  { id: 2, type: 'RECHARGE', title: '充值会员赠送', points: 100, createTime: '2026-09-20 15:20' },
  { id: 3, type: 'SPEND', title: '积分兑换权益', points: -50, createTime: '2026-09-19 20:10' },
  { id: 4, type: 'SIGN_IN', title: '每日签到（连续 2 天）', points: 7, createTime: '2026-09-20 08:26' },
  { id: 5, type: 'ADMIN_ADJUST', title: '管理员调整：活动补偿', points: 20, createTime: '2026-09-18 10:05' },
  { id: 6, type: 'SIGN_IN', title: '每日签到', points: 5, createTime: '2026-09-17 08:31' }
]

const filteredPointsRecords = computed(() =>
  pointsFilter.value === 'ALL'
    ? pointsRecords.value
    : pointsRecords.value.filter((r) => (pointsFilter.value === 'EARN' ? r.points > 0 : r.points < 0))
)

const pointTypeLabels = { SIGN_IN: '签到', RECHARGE: '充值', SPEND: '消费', ADMIN_ADJUST: '调整' }
const recordMeta = (type) => pointTypeLabels[type] || '其他'
const fmtPoints = (n) => (n > 0 ? `+${n}` : `${n}`)

async function loadPoints() {
  pointsLoaded.value = true
  pointsHint.value = ''
  try {
    const [sumRes, recRes] = await Promise.all([getUserPointsSummary(), getUserPointsRecords()])
    if (sumRes.data) pointsSummary.value = sumRes.data
    const list = recRes.data?.list
    pointsRecords.value = Array.isArray(list) && list.length ? list : DEMO_POINTS_RECORDS
    if (!Array.isArray(list)) pointsHint.value = '积分接口未接入，当前展示示例数据'
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    pointsHint.value = '积分接口未接入，当前展示示例数据'
  }
}

// ===== 积分管理（仅管理员）=====
const DEFAULT_POINT_RULES = { signInBase: 5, signInBonusPerDay: 1, signInMax: 10 }
const pointsRules = ref({ ...DEFAULT_POINT_RULES })
const rulesSaving = ref(false)
const rulesHint = ref('')
const pointsUsers = ref([])
const pointsAdminLoaded = ref(false)
const pointsUsersHint = ref('')
const pointsKeyword = ref('')
const pointAdjustVisible = ref(false)
const pointAdjustSaving = ref(false)
const pointAdjustForm = ref({ userId: '', username: '', mode: 'add', points: 10, reason: '' })

const filteredPointsUsers = computed(() => {
  const kw = pointsKeyword.value.trim().toLowerCase()
  return kw ? pointsUsers.value.filter((u) => u.username.toLowerCase().includes(kw)) : pointsUsers.value
})

const DEMO_POINTS_USERS = [
  { userId: '6263ffa8', username: 'xuqing', balance: 2480, totalEarned: 3120, totalSpent: 640, lastChangeTime: '2026-09-21 08:30' },
  { userId: '99507168', username: 'xu', balance: 356, totalEarned: 406, totalSpent: 50, lastChangeTime: '2026-09-20 20:10' },
  { userId: '24999b27', username: '游客7588', balance: 15, totalEarned: 15, totalSpent: 0, lastChangeTime: '2026-09-18 08:26' }
]

async function loadPointsRules() {
  rulesHint.value = ''
  try {
    const res = await getPointsRules()
    if (res.data) pointsRules.value = res.data
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    pointsRules.value = { ...DEFAULT_POINT_RULES }
    rulesHint.value = '积分规则接口未接入，当前展示默认值'
  }
}

async function saveRules() {
  rulesSaving.value = true
  rulesHint.value = ''
  try {
    await savePointsRules(pointsRules.value)
    rulesHint.value = '规则已保存'
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    // 后端未就绪：本地生效
    rulesHint.value = '积分规则接口未接入，已本地记录'
  } finally {
    rulesSaving.value = false
  }
}

async function loadPointsUsers() {
  pointsAdminLoaded.value = true
  pointsUsersHint.value = ''
  try {
    const res = await listPointsUsers()
    const list = res.data?.list
    pointsUsers.value = Array.isArray(list) && list.length ? list : DEMO_POINTS_USERS
    if (!Array.isArray(list)) pointsUsersHint.value = '积分接口未接入，当前展示示例数据'
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    pointsUsers.value = DEMO_POINTS_USERS
    pointsUsersHint.value = '积分接口未接入，当前展示示例数据'
  }
}

function openPointAdjust(u) {
  pointAdjustForm.value = { userId: u.userId, username: u.username, mode: 'add', points: 10, reason: '' }
  pointAdjustVisible.value = true
}

async function submitPointAdjust() {
  const f = pointAdjustForm.value
  if (!(f.points > 0) || !f.reason.trim()) return
  pointAdjustSaving.value = true
  const delta = f.mode === 'add' ? f.points : -f.points
  try {
    await adjustUserPoints({ userId: f.userId, points: delta, reason: f.reason.trim() })
  } catch (e) {
    if (e.response?.status === 401) {
      router.push('/')
      return
    }
    // 后端未就绪：本地生效
  }
  pointsUsers.value = pointsUsers.value.map((u) =>
    u.userId === f.userId
      ? {
          ...u,
          balance: u.balance + delta,
          totalEarned: delta > 0 ? u.totalEarned + delta : u.totalEarned,
          totalSpent: delta < 0 ? u.totalSpent - delta : u.totalSpent,
          lastChangeTime: formatTime(new Date())
        }
      : u
  )
  pointAdjustSaving.value = false
  pointAdjustVisible.value = false
}

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

/* 未读数徽标（系统通知） */
.side-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  margin-left: auto;
  padding: 0 5px;
  border-radius: 999px;
  background: #f53f3f;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

/* 激活项：主题色底色 + 左侧指示条（资料紫，会员橙，通知红，对话蓝，用户管理绿，统计青） */
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

.side-notice.active {
  background: rgba(245, 63, 63, 0.08);
  color: #f53f3f;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #f53f3f;
}

.side-users.active {
  background: rgba(0, 180, 42, 0.08);
  color: #00b42a;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #00b42a;
}

.side-stats.active {
  background: rgba(15, 198, 194, 0.1);
  color: #0aa5a1;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #0fc6c2;
}

.side-plans.active {
  background: rgba(247, 186, 30, 0.12);
  color: #d48806;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #f7ba1e;
}

.side-publish.active {
  background: rgba(245, 49, 157, 0.08);
  color: #f5319d;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #f5319d;
}

.side-points.active {
  background: rgba(111, 174, 0, 0.1);
  color: #5c9000;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #6fae00;
}

.side-pointsAdmin.active {
  background: rgba(52, 70, 194, 0.1);
  color: #3446c2;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #3446c2;
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 8px;
  padding: 9px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #fff;
  color: #4e5969;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn svg {
  transition: transform 0.2s;
}

.logout-btn:hover {
  background: #f53f3f;
  border-color: #f53f3f;
  color: #fff;
}

.logout-btn:hover svg {
  transform: translateX(2px);
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

.main-notice {
  background: #fbecec;
}

.main-users {
  background: #e6f5ec;
}

.main-stats {
  background: #e4f5f5;
}

.main-plans {
  background: #faf4e0;
}

.main-publish {
  background: #fdeaf4;
}

.main-points {
  background: #f0f7e0;
}

.main-pointsAdmin {
  background: #e9ecf9;
}

.panel {
  max-width: 1400px;
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

.panel-notice {
  background: #fff5f5;
  border-top-color: #f53f3f;
}

.panel-users {
  background: #f0fbf5;
  border-top-color: #00b42a;
}

.panel-stats {
  background: #f2fbfb;
  border-top-color: #0fc6c2;
}

.panel-plans {
  background: #fffcf2;
  border-top-color: #f7ba1e;
}

.panel-publish {
  background: #fef3f9;
  border-top-color: #f5319d;
}

.panel-points {
  background: #f7fbee;
  border-top-color: #6fae00;
}

.panel-pointsAdmin {
  background: #f1f3fc;
  border-top-color: #3446c2;
}

/* ===== 统计管理（青色主题） ===== */
.stats-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.range-switch {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  background: #fff;
  box-shadow: inset 0 0 0 1px #e5e6eb;
}

.range-switch button {
  padding: 6px 16px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #4e5969;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.range-switch button.on {
  background: #0fc6c2;
  color: #fff;
  font-weight: 600;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 18px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(31, 35, 41, 0.06);
}

.stat-label {
  font-size: 13px;
  color: #86909c;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1f2329;
  font-variant-numeric: tabular-nums;
}

.stat-diff {
  font-size: 12px;
}

.stat-diff.up {
  color: #00b42a;
}

.stat-diff.down {
  color: #f53f3f;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.chart-card {
  padding: 16px 18px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(31, 35, 41, 0.06);
}

.chart-card.wide {
  grid-column: 1 / -1;
}

.chart-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2329;
}

/* ===== 系统通知（红色主题） ===== */
.notice-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.notice-filter {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  background: #fff;
  box-shadow: inset 0 0 0 1px #e5e6eb;
}

.notice-filter button {
  padding: 6px 16px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #4e5969;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.notice-filter button.on {
  background: #f53f3f;
  color: #fff;
  font-weight: 600;
}

.read-all-btn {
  padding: 7px 16px;
  border: 1px solid #e5e6eb;
  border-radius: 999px;
  background: #fff;
  color: #4e5969;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s, opacity 0.2s;
}

.read-all-btn:hover:not(:disabled) {
  color: #f53f3f;
  border-color: #f53f3f;
}

.read-all-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 时间轴容器 */
.notice-timeline {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.notice-item {
  position: relative;
  padding-left: 116px;
  cursor: pointer;
}

/* 左侧时间列：两行右对齐，紧贴节点 */
.notice-when {
  position: absolute;
  left: 0;
  top: 8px;
  width: 72px;
  text-align: right;
  line-height: 1.4;
}

.notice-date {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #4e5969;
}

.notice-when.today .notice-date {
  color: #f53f3f;
}

.notice-time {
  display: block;
  font-size: 12px;
  color: #86909c;
}

/* 节点连接线：从本节点中心延伸到下一个节点中心（节点白环自然遮住交界） */
.notice-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 90px;
  top: 18px;
  bottom: -14px;
  width: 2px;
  background: rgba(245, 63, 63, 0.16);
}

/* 节点：已读灰色，未读红色 + 光环 */
.notice-node {
  position: absolute;
  left: 84px;
  top: 18px;
  width: 14px;
  height: 14px;
  box-sizing: border-box;
  border-radius: 50%;
  border: 3px solid #c9cdd4;
  background: #fff;
  box-shadow: 0 0 0 3px #fff;
}

.notice-item.unread .notice-node {
  border-color: #f53f3f;
  box-shadow: 0 0 0 3px #fff, 0 0 0 5.5px rgba(245, 63, 63, 0.2);
}

/* 通知卡片 */
.notice-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(31, 35, 41, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}

.notice-item:hover .notice-card {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(31, 35, 41, 0.1);
}

.notice-item.unread .notice-card {
  box-shadow: inset 0 0 0 1px rgba(245, 63, 63, 0.35), 0 1px 4px rgba(31, 35, 41, 0.06);
}

/* 标题行：类型徽章 + 标题 + 未读点 + 时间 */
.notice-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 类型徽章：跟随已读状态配色 */
.notice-type {
  flex-shrink: 0;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: #f2f3f5;
  color: #86909c;
}

.notice-item.unread .notice-type {
  background: #ffece8;
  color: #f53f3f;
}

.notice-title {
  flex: 1 1 auto;
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  color: #4e5969;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-item.unread .notice-title {
  color: #1f2329;
}

.unread-dot {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f53f3f;
}

.notice-content {
  margin: 0;
  font-size: 13px;
  color: #86909c;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-item.unread .notice-content {
  color: #4e5969;
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
  position: relative;
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

.vip-origin {
  position: absolute;
  top: 8px;
  right: 10px;
  font-size: 12px;
  color: #c9cdd4;
  text-decoration: line-through;
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

/* ===== 对话管理 / 用户管理（原后台管理） ===== */
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

/* 行悬停高亮：对话管理浅蓝、用户管理浅绿 */
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

/* 对话数量气泡徽章 */
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

/* ===== 会员管理（金色主题） ===== */
.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.plan-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(247, 186, 30, 0.45);
  box-shadow: 0 1px 4px rgba(31, 35, 41, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}

.plan-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 14px rgba(212, 136, 6, 0.18);
}

.plan-card.off {
  opacity: 0.62;
  border-style: dashed;
}

.plan-name {
  font-size: 15px;
  font-weight: 700;
  color: #1f2329;
}

.plan-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.plan-price strong {
  font-size: 28px;
  color: #d48806;
}

.plan-price s {
  font-size: 13px;
  color: #c9cdd4;
}

.plan-meta {
  font-size: 12px;
  color: #86909c;
}

.plan-state {
  align-self: flex-start;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.plan-state.is-on {
  background: #e8f7ec;
  color: #00b42a;
}

.plan-state.is-off {
  background: #f2f3f5;
  color: #86909c;
}

.plan-edit-btn {
  margin-top: 6px;
  padding: 7px 0;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #f7ba1e, #d48806);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.2s, transform 0.2s;
}

.plan-edit-btn:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
}

/* 调价/通知弹窗表单（复用 pay-mask） */
.plan-dialog {
  width: 420px;
  max-width: 92vw;
}

.plan-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 16px 0;
}

.plan-field {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 13px;
  color: #4e5969;
}

.plan-field.row {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.plan-field input[type='number'],
.plan-field input[type='text'],
.plan-field textarea,
.plan-field select {
  padding: 8px 10px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  font-size: 13px;
  color: #1f2329;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}

.plan-field input:focus,
.plan-field textarea:focus,
.plan-field select:focus {
  border-color: #f7ba1e;
}

.plan-field textarea {
  resize: vertical;
}

.plan-save-btn {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #f7ba1e, #d48806);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.2s;
}

.plan-save-btn:hover:not(:disabled) {
  filter: brightness(1.08);
}

.plan-save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== 消息发布（玫红主题） ===== */
.pub-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.pub-filters {
  display: flex;
  gap: 10px;
}

.pub-select {
  padding: 7px 10px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  color: #1f2329;
  outline: none;
  cursor: pointer;
}

.pub-create-btn {
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #f5319d, #cb1e83);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.2s, transform 0.2s;
}

.pub-create-btn:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
}

.table-wrap {
  overflow-x: auto;
}

.pub-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.pub-table th,
.pub-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid #f2f3f5;
  white-space: nowrap;
}

.pub-table th {
  color: #86909c;
  font-weight: 600;
  background: #fdf3f9;
}

.pub-table tbody tr {
  transition: background 0.15s;
}

.pub-table tbody tr:hover {
  background: rgba(245, 49, 157, 0.05);
}

.pub-title-cell {
  color: #1f2329;
  font-weight: 500;
  cursor: pointer;
}

.pub-title-cell:hover {
  color: #f5319d;
}

.pub-type {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.pub-type.tt-system {
  background: #ffece8;
  color: #f53f3f;
}

.pub-type.tt-activity {
  background: #fff3e8;
  color: #ff7d00;
}

.pub-type.tt-update {
  background: #e8f3ff;
  color: #165dff;
}

.pub-status {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.pub-status.st-draft {
  background: #f2f3f5;
  color: #86909c;
}

.pub-status.st-published {
  background: #e8f7ec;
  color: #00b42a;
}

.pub-status.st-withdrawn {
  background: #fff3e8;
  color: #ff7d00;
}

.pub-read-badge {
  display: inline-block;
  min-width: 52px;
  text-align: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: #fdeaf4;
  color: #cb1e83;
  font-weight: 600;
}

/* 复选框列与批量操作栏 */
.pub-check-col {
  width: 40px;
  text-align: center !important;
}

.pub-check-col input {
  cursor: pointer;
}

.pub-batch-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  margin-bottom: 12px;
  border-radius: 10px;
  background: #fdeaf4;
  animation: pubBarIn 0.2s ease;
}

@keyframes pubBarIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pub-batch-count {
  font-size: 13px;
  font-weight: 600;
  color: #cb1e83;
  margin-right: 4px;
}

.pub-op {
  padding: 4px 12px;
  border-radius: 999px;
  border: 1px solid #e5e6eb;
  background: #fff;
  font-size: 12px;
  color: #4e5969;
  cursor: pointer;
  transition: all 0.15s;
}

.pub-op:hover {
  border-color: #f5319d;
  color: #f5319d;
}

.pub-op:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.pub-op.primary {
  border-color: #00b42a;
  color: #00b42a;
}

.pub-op.primary:hover {
  background: #00b42a;
  color: #fff;
}

.pub-op.warn {
  border-color: #ff7d00;
  color: #ff7d00;
}

.pub-op.warn:hover {
  background: #ff7d00;
  color: #fff;
}

.pub-op.danger:hover {
  border-color: #f53f3f;
  background: #f53f3f;
  color: #fff;
}

.pub-empty {
  text-align: center;
  color: #86909c;
  padding: 28px 0 !important;
}

.pub-form-tip {
  margin: 0 0 10px;
  font-size: 12px;
  color: #86909c;
}

/* 手机号行内编辑 */
.phone-wrap {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.phone-input {
  width: 156px;
  height: 30px;
  padding: 0 14px;
  border: 1px solid #e5e6eb;
  border-radius: 999px;
  font-size: 13px;
  color: #1f2329;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.phone-input::placeholder {
  color: #c9cdd4;
}

.phone-input:focus {
  border-color: #722ed1;
  box-shadow: 0 0 0 3px rgba(114, 45, 209, 0.12);
}

.phone-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  padding: 0 16px;
  border: 1px solid #e5e6eb;
  border-radius: 999px;
  background: #fff;
  font-size: 12px;
  color: #4e5969;
  cursor: pointer;
  transition: all 0.2s;
}

.phone-btn:hover:not(:disabled) {
  border-color: #722ed1;
  color: #722ed1;
}

.phone-btn.primary {
  border: none;
  background: linear-gradient(135deg, #8a4bea, #722ed1);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(114, 45, 209, 0.25);
}

.phone-btn.primary:hover:not(:disabled) {
  color: #fff;
  filter: brightness(1.08);
  transform: translateY(-1px);
}

.phone-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* ===== 积分明细（黄绿主题） ===== */
.points-balance-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(111, 174, 0, 0.12), rgba(111, 174, 0, 0.04));
  box-shadow: inset 0 0 0 1px rgba(111, 174, 0, 0.25);
}

.pb-label {
  font-size: 12px;
  color: #86909c;
}

.pb-value {
  font-size: 32px;
  font-weight: 800;
  color: #5c9000;
  line-height: 1.2;
}

.pb-right {
  display: flex;
  gap: 28px;
}

.pb-item {
  text-align: right;
}

.pb-num {
  font-size: 18px;
  font-weight: 700;
}

.pb-num.earn {
  color: #00b42a;
}

.pb-num.spend {
  color: #ff7d00;
}

.points-filters {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.points-filter-btn {
  padding: 6px 18px;
  border-radius: 999px;
  border: 1px solid #e5e6eb;
  background: #fff;
  font-size: 13px;
  color: #4e5969;
  cursor: pointer;
  transition: all 0.2s;
}

.points-filter-btn.active {
  background: #6fae00;
  border-color: #6fae00;
  color: #fff;
  font-weight: 600;
}

.points-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.points-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 6px;
  border-bottom: 1px solid #f2f3f5;
  transition: background 0.15s;
}

.points-item:hover {
  background: rgba(111, 174, 0, 0.05);
}

.points-type {
  flex-shrink: 0;
  width: 44px;
  text-align: center;
  padding: 3px 0;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.points-type.pt-sign_in {
  background: #e8f7ec;
  color: #00b42a;
}

.points-type.pt-recharge {
  background: #fff3e8;
  color: #ff7d00;
}

.points-type.pt-spend {
  background: #f2f3f5;
  color: #86909c;
}

.points-type.pt-admin_adjust {
  background: #e9ecf9;
  color: #3446c2;
}

.points-info {
  flex: 1;
  min-width: 0;
}

.points-title {
  margin: 0 0 2px;
  font-size: 13px;
  color: #1f2329;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.points-time {
  margin: 0;
  font-size: 12px;
  color: #c9cdd4;
}

.points-delta {
  font-size: 15px;
  font-weight: 700;
}

.points-delta.earn {
  color: #00b42a;
}

.points-delta.spend {
  color: #ff7d00;
}

.points-empty {
  padding: 28px 0;
  text-align: center;
  font-size: 13px;
  color: #86909c;
}

/* ===== 积分管理（靛蓝主题） ===== */
.rules-card {
  padding: 16px 20px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(31, 35, 41, 0.06);
}

.rules-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.rules-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #3446c2;
}

.rules-save-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #4d61e0, #3446c2);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.2s;
}

.rules-save-btn:hover:not(:disabled) {
  filter: brightness(1.1);
}

.rules-save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.rules-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.rules-search {
  width: 260px;
  padding: 7px 10px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  font-size: 13px;
  color: #1f2329;
  outline: none;
  transition: border-color 0.2s;
}

.rules-search:focus {
  border-color: #3446c2;
}

.pt-balance {
  font-size: 14px;
  color: #3446c2;
}

.pt-earn {
  color: #00b42a;
  font-weight: 600;
}

.pt-spend {
  color: #ff7d00;
  font-weight: 600;
}

/* 调整积分弹窗（复用 plan-form） */
.radio-row {
  gap: 18px;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #1f2329;
  cursor: pointer;
}

.radio-item input {
  accent-color: #3446c2;
  cursor: pointer;
}

.plan-save-btn.indigo {
  background: linear-gradient(135deg, #4d61e0, #3446c2);
}
</style>
