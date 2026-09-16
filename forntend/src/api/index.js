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

export default request
