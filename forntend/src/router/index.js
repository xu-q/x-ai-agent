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
    path: '/admin',
    name: 'admin',
    component: () => import('../views/Admin.vue')
  },
  {
    path: '/admin/conversation/:conversationId',
    name: 'conversation-detail',
    component: () => import('../views/MessageDetail.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
