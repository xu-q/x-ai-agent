<template>
  <router-view v-slot="{ Component }">
    <transition name="route" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <!-- 全站统一轻提示（置于路由外，切页不闪断） -->
  <ToastTip />
</template>

<script setup>
import ToastTip from './components/ToastTip.vue'
</script>
