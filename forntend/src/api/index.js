import axios from 'axios'

const BASE_URL = '/api'

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 30000
})

/**
 * AI 恋爱大师 SSE 接口地址（GET，返回 text/event-stream）
 * 后端 LoveAppController @RestController("/ai") 是 Bean 名称非路径前缀
 */
export const LOVE_SSE_URL = `${BASE_URL}/doChatSSE`

/**
 * AI 超级智能体 SSE 接口地址（GET，返回 SseEmitter）
 * 后端无 manus/chat 接口，使用 doChatWithEmitter（SseEmitter 流式）
 */
export const AGENT_SSE_URL = `${BASE_URL}/doChatWithEmitter`

/**
 * 后台管理 - 会话列表（含每个会话的消息数量）
 */
export function listConversations(userId = 0) {
  return request.get('/chat/conversations', { params: { userId } })
}

/**
 * 后台管理 - 指定会话的消息列表
 */
export function listMessages(conversationId) {
  return request.get('/chat/message', { params: { conversationId } })
}

export default request
