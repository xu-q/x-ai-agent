/**
 * SSE 流式请求工具
 * 基于 fetch + ReadableStream 实现，支持逐块解析 SSE 数据
 */

/**
 * 发起 SSE 流式 GET 请求
 * @param {string} url 请求地址（完整路径）
 * @param {Object} params 查询参数
 * @param {(chunk: string) => void} onMessage 每收到一段内容时的回调
 * @param {() => void} onDone 流结束回调
 * @param {(err: Error) => void} onError 错误回调
 * @returns {AbortController} 可用于中断请求
 */
export function fetchSSE(url, params, { onMessage, onDone, onError }) {
  const controller = new AbortController()

  const queryString = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v != null))
  ).toString()

  const fullUrl = queryString ? `${url}?${queryString}` : url

  fetch(fullUrl, {
    method: 'GET',
    signal: controller.signal,
    headers: { Accept: 'text/event-stream' }
  })
    .then(async (response) => {
      if (!response.ok) {
        // 优先展示后端返回的提示信息（R 响应体的 message）
        let msg = `请求失败，状态码：${response.status}`
        try {
          const body = await response.json()
          if (body?.message) msg = body.message
        } catch {
          /* 非 JSON 响应保持默认提示 */
        }
        throw new Error(msg)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      // eslint-disable-next-line no-constant-condition
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })

        // 按双换行分割事件块
        const parts = buffer.split('\n\n')
        buffer = parts.pop() // 保留未完成的部分

        for (const part of parts) {
          const text = parseSSEBlock(part)
          if (text !== null) {
            onMessage(text)
          }
        }
      }

      // 处理剩余缓冲
      if (buffer.trim()) {
        const text = parseSSEBlock(buffer)
        if (text !== null) onMessage(text)
      }
      onDone && onDone()
    })
    .catch((err) => {
      if (err.name === 'AbortError') return
      onError && onError(err)
    })

  return controller
}

/**
 * 解析单个 SSE 事件块，提取 data 内容
 * SSE 格式：每行以 "data:" 开头，可能多行，需拼接
 */
function parseSSEBlock(block) {
  const lines = block.split('\n')
  const dataLines = []
  for (const line of lines) {
    const trimmed = line.trim()
    if (trimmed.startsWith('data:')) {
      dataLines.push(trimmed.slice(5).replace(/^ /, ''))
    }
  }
  if (dataLines.length === 0) return null
  return dataLines.join('\n')
}
