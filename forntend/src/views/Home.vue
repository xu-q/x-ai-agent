<template>
  <div class="home">
    <div class="home-bg">
      <h1 class="home-title">AI 智能应用</h1>
      <p class="home-subtitle">选择一个应用开始对话</p>
      <div class="app-cards">
        <div
          v-for="app in apps"
          :key="app.path"
          class="app-card"
          @click="goTo(app.path)"
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
import { useRouter } from 'vue-router'

const router = useRouter()

const apps = [
  {
    name: 'AI 恋爱大师',
    desc: '智能恋爱顾问，解答你的情感困惑',
    icon: '❤',
    gradient: 'linear-gradient(135deg, #ff6b6b, #ee5a6f)',
    path: '/love'
  },
  {
    name: 'AI 超级智能体',
    desc: '全能智能助手，自主调用工具完成任务',
    icon: '🤖',
    gradient: 'linear-gradient(135deg, #4facfe, #00f2fe)',
    path: '/agent'
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
}

.home-bg {
  text-align: center;
  color: #fff;
  width: 100%;
  max-width: 900px;
  padding: 40px 20px;
}

.home-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 12px;
}

.home-subtitle {
  font-size: 18px;
  opacity: 0.85;
  margin-bottom: 48px;
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
  transition: all 0.3s ease;
  cursor: pointer;
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
