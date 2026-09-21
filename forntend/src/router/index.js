import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  { path: '/', name: 'home', component: Home },
  {
    path: '/love',
    name: 'love',
    component: () => import('../views/LoveMaster.vue')
  },
  {
    path: '/agent',
    name: 'agent',
    component: () => import('../views/SuperAgent.vue')
  },
  {
    path: '/profile/conversation/:conversationId',
    name: 'conversation-detail',
    component: () => import('../views/MessageDetail.vue')
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/Profile.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 个人中心需要登录态：未登录重定向首页
router.beforeEach((to) => {
  if (to.path === '/profile' && !sessionStorage.getItem('chat-user')) {
    return '/'
  }
})

export default router
