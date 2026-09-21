<template>
  <div class="home">
    <!-- 右上角登录区 -->
    <div class="auth-corner">
      <template v-if="user">
        <div class="corner-user">
          <span
            class="corner-avatar"
            :class="{ 'avatar-admin': user.role === 'ADMIN' }"
            :title="user.role === 'ADMIN' ? '已登录（管理员），点击进入后台管理' : `已登录（${user.role || 'USER'}）`"
            @click="user.role === 'ADMIN' && router.push('/admin')"
          >
            <svg viewBox="0 0 24 24" fill="currentColor" width="17" height="17">
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

function onMouseMove(e) {
  const now = performance.now()
  if (now - lastSpawn < 26) return
  lastSpawn = now
  const dot = {
    id: ++trailId,
    x: e.clientX + (Math.random() * 8 - 4),
    y: e.clientY + (Math.random() * 8 - 4),
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
})

const apps = [
  {
    name: 'AI 恋爱大师',
    desc: '智能恋爱顾问，解答你的情感困惑',
    icon: '❤',
    gradient: 'linear-gradient(135deg, #ff6b6b, #ee5a6f)',
    path: '/love'
  }
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

/* 管理员头像：可点击进入后台，悬停绿色提示 */
.avatar-admin {
  cursor: pointer;
}

.avatar-admin:hover {
  background: rgba(120, 235, 175, 0.22);
}

.avatar-admin:active {
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
  max-width: 900px;
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
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 20px;
  padding: 40px 28px;
  transition: transform 0.12s ease-out, background 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  will-change: transform;
}

.app-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2);
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
