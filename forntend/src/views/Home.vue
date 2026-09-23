<template>
  <div class="home">
    <!-- 右上角登录区 -->
    <div class="auth-corner">
      <template v-if="user">
        <div class="corner-user">
          <span
            class="corner-avatar"
            :title="`已登录（${user.role || 'USER'}），点击进入个人中心`"
            @click="router.push('/profile')"
          >
            <img v-if="user.avatar" :src="user.avatar" alt="头像" class="corner-avatar-img" />
            <svg v-else viewBox="0 0 24 24" fill="currentColor" width="17" height="17">
              <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-3.9 0-8 2-8 5v1a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-1c0-3-4.1-5-8-5z"/>
            </svg>
            {{ user.name }}
          </span>
          <button class="corner-logout" @click="doLogout">退出</button>
        </div>
      </template>
      <button v-else class="corner-btn" @click="openAuth">登录 / 注册</button>
    </div>

    <!-- 登录/注册弹层（共享组件，purple 主题） -->
    <AuthDialog
      v-if="showAuth"
      theme="purple"
      title="AI 智能应用"
      closable
      @authenticated="onAuth"
      @close="showAuth = false"
    />

    <!-- 鼠标星尘拖尾 -->
    <span
      v-for="dot in trail"
      :key="dot.id"
      class="trail-dot"
      :style="dotStyle(dot)"
    ></span>
    <!-- 漂浮粒子 -->
    <div
      v-for="p in particles"
      :key="p.id"
      class="particle"
      :style="{
        left: p.left,
        top: p.top,
        width: p.size + 'px',
        height: p.size + 'px',
        animationDuration: p.duration + 's',
        animationDelay: p.delay + 's'
      }"
    ></div>
    <div class="home-bg">
      <div class="app-cards">
        <div
          v-for="app in apps"
          :key="app.path"
          class="app-card"
          @click="goTo(app.path)"
          @mousemove="onCardMove"
          @mouseleave="onCardLeave"
        >
          <div class="app-icon" :style="{ background: app.gradient }">
            {{ app.icon }}
          </div>
          <h2 class="app-name">{{ app.name }}</h2>
          <p class="app-desc">{{ app.desc }}</p>
        </div>

        <!-- 天气卡片（静态展示，与恋爱大师并排，动画一致） -->
        <div
          class="weather-card"
          @mousemove="onCardMove"
          @mouseleave="onCardLeave"
        >
          <div class="weather-head">
            <span class="weather-city">杭州</span>
            <span class="weather-date">09/22 周二</span>
          </div>
          <svg class="weather-icon" viewBox="0 0 48 48" width="72" height="72">
            <circle cx="19" cy="19" r="9" fill="#ffd53d"/>
            <g stroke="#ffd53d" stroke-width="2.4" stroke-linecap="round">
              <path d="M19 4.5v3.5M19 30v3.5M4.5 19H8M30 19h3.5M8.9 8.9l2.4 2.4M26.7 26.7l2.4 2.4M29.1 8.9l-2.4 2.4M11.3 26.7l-2.4 2.4"/>
            </g>
            <path d="M30 36.5a6.5 6.5 0 0 1 .9-12.9 9 9 0 0 1 17.3 2.4A5.7 5.7 0 0 1 46.5 36.5z" fill="#eef2ff" stroke="#c9d4ff" stroke-width="1.5"/>
          </svg>
          <div class="weather-temp">
            <span class="temp-num">26</span><span class="temp-unit">°C</span>
          </div>
          <p class="weather-text">多云转晴</p>
          <div class="weather-meta">
            <span>湿度 62%</span><i></i>
            <span>东南风 3级</span><i></i>
            <span>空气 优</span>
          </div>
          <div class="weather-forecast">
            <div class="forecast-item" v-for="d in weatherDays" :key="d.name">
              <span class="f-name">{{ d.name }}</span>
              <span class="f-icon" :style="{ color: d.color }">{{ d.emoji }}</span>
              <span class="f-temp">{{ d.range }}</span>
            </div>
          </div>
        </div>

        <!-- 每日 AI 热点卡片（静态展示，动画一致） -->
        <div
          class="trends-card"
          @mousemove="onCardMove"
          @mouseleave="onCardLeave"
        >
          <div class="trends-head">
            <span class="trends-title">🔥 每日 AI 热点</span>
            <span class="trends-date">09/22</span>
          </div>
          <ul class="trends-list">
            <li class="trend-item" v-for="(t, i) in aiTrends" :key="t.title">
              <span class="trend-rank" :class="`rank-${i + 1}`">{{ i + 1 }}</span>
              <span class="trend-title">{{ t.title }}</span>
              <span class="trend-heat">{{ t.heat }}</span>
            </li>
          </ul>
          <p class="trends-note">数据每日更新</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { userLogout, clearAuth } from '../api/index.js'
import AuthDialog from '../components/AuthDialog.vue'

const router = useRouter()

// ===== 右上角登录/注册 =====
const user = ref(JSON.parse(sessionStorage.getItem('chat-user') || 'null'))
const showAuth = ref(false)

function openAuth() {
  showAuth.value = true
}

// 弹层回调：组件已完成登录/注册并写入存储
function onAuth({ user: u }) {
  user.value = u
  showAuth.value = false
}

async function doLogout() {
  try {
    await userLogout()
  } catch { /* 后端失败也照常清理本地 */ }
  clearAuth()
  user.value = null
}

// 漂浮粒子：固定数量，随机位置/大小/速度，负延迟避免启动时同步
const particles = Array.from({ length: 30 }, (_, i) => ({
  id: i,
  left: Math.random() * 100 + '%',
  top: Math.random() * 40 + '%',
  size: Math.round(Math.random() * 8 + 4),
  duration: Math.round(Math.random() * 8 + 8),
  delay: -(Math.random() * 10).toFixed(2) + 's'
}))

// 鼠标星尘拖尾
const trail = ref([])
let trailId = 0
let lastSpawn = 0

function dotStyle(dot) {
  return {
    left: dot.x + 'px',
    top: dot.y + 'px',
    width: dot.size + 'px',
    height: dot.size + 'px',
    '--dx': dot.dx + 'px',
    '--dy': dot.dy + 'px'
  }
}

// 卡片 3D 倾斜：根据鼠标在卡片内的位置旋转
function onCardMove(e) {
  const card = e.currentTarget
  const rect = card.getBoundingClientRect()
  const px = (e.clientX - rect.left) / rect.width - 0.5
  const py = (e.clientY - rect.top) / rect.height - 0.5
  card.style.transform = `perspective(800px) rotateX(${(-py * 18).toFixed(2)}deg) rotateY(${(px * 18).toFixed(2)}deg) translateY(-8px)`
}

function onCardLeave(e) {
  e.currentTarget.style.transform = ''
}

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onMouseMove)
  clearTimeout(idleTimer)
})

// ===== 星尘拖尾：移动时跟随冒出，静止时在原地持续冒出 =====
let lastX = 0
let lastY = 0
let idleTimer = null

function spawnTrail(x, y) {
  const dot = {
    id: ++trailId,
    x: x + (Math.random() * 8 - 4),
    y: y + (Math.random() * 8 - 4),
    size: 5 + Math.random() * 8,
    dx: Math.random() * 36 - 18,
    dy: -(8 + Math.random() * 24)
  }
  trail.value.push(dot)
  if (trail.value.length > 40) trail.value.shift()
  setTimeout(() => {
    const idx = trail.value.findIndex((d) => d.id === dot.id)
    if (idx !== -1) trail.value.splice(idx, 1)
  }, 750)
}

function scheduleIdle() {
  clearTimeout(idleTimer)
  idleTimer = setTimeout(() => {
    spawnTrail(lastX, lastY)
    scheduleIdle()
  }, 26)
}

function onMouseMove(e) {
  const now = performance.now()
  if (now - lastSpawn < 26) return
  lastSpawn = now
  lastX = e.clientX
  lastY = e.clientY
  spawnTrail(lastX, lastY)
  scheduleIdle()
}

const apps = [
  {
    name: 'AI 恋爱大师',
    desc: '智能恋爱顾问，解答你的情感困惑',
    icon: '❤',
    gradient: 'linear-gradient(135deg, #ff6b6b, #ee5a6f)',
    path: '/love'
  }
]

// ===== 天气卡片（静态展示数据，接口后续接入） =====
const weatherDays = [
  { name: '今天', emoji: '⛅', range: '19~28°', color: '#ffb02e' },
  { name: '明天', emoji: '☀️', range: '20~29°', color: '#ffb02e' },
  { name: '后天', emoji: '🌧️', range: '18~24°', color: '#4d9ef7' }
]

// ===== 每日 AI 热点（静态展示数据，接口后续接入） =====
const aiTrends = [
  { title: '多模态大模型突破视频理解', heat: '12.8w' },
  { title: 'AI Agent 自主完成任务成趋势', heat: '9.6w' },
  { title: '开源模型性能逼近闭源', heat: '7.3w' },
  { title: 'AI 编程助手进入协同时代', heat: '5.1w' }
]

function goTo(path) {
  router.push(path)
}
</script>

<style scoped>
.home {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

/* ===== 右上角登录区 ===== */
.auth-corner {
  position: absolute;
  top: 20px;
  right: 24px;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* ===== 液态玻璃（Liquid Glass）质感 ===== */
.corner-btn,
.corner-user {
  position: relative;
  border-radius: 999px;
  border: none;
  background: rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.18),
    inset 0 2px 1px -1px rgba(255, 255, 255, 0.6),
    inset 0 -10px 14px -12px rgba(255, 255, 255, 0.22);
  transition:
    transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1),
    background 0.25s ease,
    box-shadow 0.25s ease;
}

/* 顶缘 specular 高光线 */
.corner-btn::before,
.corner-user::before {
  content: '';
  position: absolute;
  top: 0;
  left: 14%;
  right: 14%;
  height: 1px;
  border-radius: 999px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.95), transparent);
  pointer-events: none;
}

.corner-btn {
  padding: 9px 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-shadow: 0 1px 2px rgba(25, 15, 60, 0.3);
}

/* 用户身份 + 退出：一体玻璃胶囊 */
.corner-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 8px 7px 18px;
  color: #fff;
  font-size: 15px;
  text-shadow: 0 1px 2px rgba(25, 15, 60, 0.3);
}

.corner-user > * {
  position: relative;
  z-index: 1;
}

.corner-avatar {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 4px 10px;
  margin: -4px -10px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

/* 悬停头像：浮现柔和底色光斑 */
.corner-avatar:hover {
  background: rgba(255, 255, 255, 0.14);
}

.corner-avatar svg {
  display: block;
  flex-shrink: 0;
  opacity: 0.9;
}

/* 用户上传的头像 */
.corner-avatar-img {
  display: block;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.corner-avatar:active {
  transform: scale(0.94);
}

.corner-logout {
  padding: 6px 14px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.11);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0.5px rgba(255, 255, 255, 0.45);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

/* 悬停：整体微微提亮上浮；按压回弹，液态手感 */
.corner-btn:hover,
.corner-user:hover {
  transform: translateY(-1.5px);
  background: rgba(255, 255, 255, 0.14);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 2px 1px -1px rgba(255, 255, 255, 0.75),
    inset 0 -10px 14px -12px rgba(255, 255, 255, 0.3);
}

.corner-btn:active,
.corner-user:active,
.corner-logout:active {
  transform: scale(0.96);
}

.corner-logout:hover {
  background: rgba(255, 143, 163, 0.85);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.35),
    inset 0 1px 0.5px rgba(255, 255, 255, 0.6);
  color: #fff;
}

/* 鼠标星尘拖尾：发光小光点，飘散消逝 */
.trail-dot {
  position: fixed;
  top: 0;
  left: 0;
  border-radius: 50%;
  pointer-events: none;
  z-index: 5;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(196, 140, 255, 0.55) 55%,
    rgba(168, 85, 247, 0) 100%
  );
  box-shadow: 0 0 10px 2px rgba(196, 140, 255, 0.4);
  animation: trailFade 0.75s ease-out forwards;
  will-change: transform, opacity;
}

@keyframes trailFade {
  0% {
    opacity: 0.95;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(calc(-50% + var(--dx, 0px)), calc(-50% + var(--dy, 0px))) scale(0.15);
  }
}

/* 漂浮粒子 */
.particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  pointer-events: none;
  animation: fallDown linear infinite;
}

@keyframes fallDown {
  0% {
    transform: translateY(0);
    opacity: 0;
  }
  20% {
    opacity: 1;
  }
  100% {
    transform: translateY(80vh);
    opacity: 0;
  }
}

.home-bg {
  text-align: center;
  color: #fff;
  width: 100%;
  max-width: 1000px;
  padding: 40px 20px;
}

.app-cards {
  display: flex;
  gap: 32px;
  justify-content: center;
  flex-wrap: wrap;
}

.app-card {
  width: 280px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 40px 28px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.18);
  transition: transform 0.12s ease-out, background 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  cursor: pointer;
  will-change: transform;
}

.app-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.22);
}

/* ===== 天气卡片（与恋爱大师并排，液态玻璃，动画一致） ===== */
.weather-card {
  width: 280px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 22px 28px 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.18);
  color: #fff;
  text-align: center;
  cursor: default;
  will-change: transform;
}

.weather-head {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10px;
}

.weather-city {
  font-size: 17px;
  font-weight: 600;
}

.weather-date {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.weather-icon {
  display: block;
  margin: 12px auto 6px;
  filter: drop-shadow(0 4px 10px rgba(255, 213, 61, 0.4));
}

.weather-temp {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  line-height: 1;
}

.temp-num {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: -1px;
}

.temp-unit {
  font-size: 16px;
  font-weight: 600;
  margin-top: 5px;
  color: rgba(255, 255, 255, 0.85);
}

.weather-text {
  margin-top: 4px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.weather-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.weather-meta i {
  width: 1px;
  height: 11px;
  background: rgba(255, 255, 255, 0.22);
}

.weather-forecast {
  display: flex;
  justify-content: space-around;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.forecast-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.f-name {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.f-icon {
  font-size: 17px;
  line-height: 1;
}

.f-temp {
  font-size: 12px;
  font-weight: 500;
}

/* ===== 每日 AI 热点卡片（液态玻璃，动画一致） ===== */
.trends-card {
  width: 280px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 22px 22px 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.18);
  color: #fff;
  cursor: default;
  will-change: transform;
}

.trends-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.trends-title {
  font-size: 15px;
  font-weight: 600;
}

.trends-date {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.trends-list {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.trend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.trend-rank {
  flex: none;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.72);
}

.rank-1 {
  background: linear-gradient(135deg, #ff4d4f, #ff7a45);
  color: #fff;
}

.rank-2 {
  background: linear-gradient(135deg, #ff7a45, #ffa940);
  color: #fff;
}

.rank-3 {
  background: linear-gradient(135deg, #ffc53d, #ffd666);
  color: #7a4b00;
}

.trend-title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.trend-heat {
  flex: none;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.62);
}

.trends-note {
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  text-align: center;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
}

.app-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto 24px;
}

.app-name {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 10px;
}

.app-desc {
  font-size: 14px;
  opacity: 0.8;
  line-height: 1.6;
}
</style>
