<template>
  <ChatRoom
    ref="chatRoomRef"
    title="AI 恋爱大师"
    :chat-id="chatId"
    ai-icon="❤"
    ai-gradient="linear-gradient(135deg, #ff6b6b, #ee5a6f)"
    welcome="我是你的 AI 恋爱大师，有任何情感困惑都可以和我说～"
    @send="handleSend"
  />
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import ChatRoom from '../components/ChatRoom.vue'
import { fetchSSE } from '../utils/sse'
import { LOVE_SSE_URL, generateChatId } from '../api'

const chatRoomRef = ref(null)
const chatId = ref('')

let currentController = null

// 进入页面后自动生成聊天室 ID，用于区分不同会话
onMounted(() => {
  chatId.value = generateChatId()
})

// 离开页面时中断未完成的流式请求，避免后台持续下载
onBeforeUnmount(() => {
  currentController?.abort()
  currentController = null
})

function handleSend(message) {
  // 中断上一次未完成的请求
  if (currentController) {
    currentController.abort()
  }

  currentController = fetchSSE(
    LOVE_SSE_URL,
    { message, chatId: chatId.value },
    {
      onMessage: (chunk) => {
        chatRoomRef.value?.appendAiChunk(chunk)
      },
      onDone: () => {
        chatRoomRef.value?.finishAiMessage()
        currentController = null
      },
      onError: (err) => {
        chatRoomRef.value?.showAiError(err.message)
        currentController = null
      }
    }
  )
}
</script>
