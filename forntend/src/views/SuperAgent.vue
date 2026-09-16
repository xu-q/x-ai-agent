<template>
  <ChatRoom
    ref="chatRoomRef"
    title="AI 超级智能体"
    :chat-id="chatId"
    ai-icon="🤖"
    ai-gradient="linear-gradient(135deg, #4facfe, #00f2fe)"
    welcome="我是 AI 超级智能体，可以自主调用工具帮你完成各种任务，请问有什么可以帮你？"
    @send="handleSend"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ChatRoom from '../components/ChatRoom.vue'
import { fetchSSE } from '../utils/sse'
import { AGENT_SSE_URL } from '../api'

const chatRoomRef = ref(null)
const chatId = ref('')

let currentController = null

// 进入页面后自动生成聊天室 ID，用于区分不同会话
onMounted(() => {
  chatId.value = generateChatId()
})

function generateChatId() {
  return 'agent_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
}

function handleSend(message) {
  // 中断上一次未完成的请求
  if (currentController) {
    currentController.abort()
  }

  currentController = fetchSSE(
    AGENT_SSE_URL,
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
