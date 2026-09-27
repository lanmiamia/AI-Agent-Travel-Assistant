import { resolveApiUrl } from './http'

const STREAM_EVENT_NAMES = ['message', 'data', 'delta', 'token']
const COMPLETE_EVENT_NAMES = ['done', 'complete', 'finish']

export function normalizeSsePayload(rawPayload) {
  if (typeof rawPayload !== 'string') {
    return ''
  }

  const trimmedPayload = rawPayload.trim()
  const upperPayload = trimmedPayload.toUpperCase()

  if (!trimmedPayload || upperPayload === 'DONE' || upperPayload === '[DONE]') {
    return ''
  }

  try {
    const parsedPayload = JSON.parse(rawPayload)

    if (typeof parsedPayload === 'string') {
      return parsedPayload
    }

    for (const key of ['content', 'text', 'message', 'delta', 'data']) {
      if (typeof parsedPayload?.[key] === 'string') {
        return parsedPayload[key]
      }
    }

    const choiceContent = parsedPayload?.choices?.[0]?.delta?.content
    if (typeof choiceContent === 'string') {
      return choiceContent
    }
  } catch {
    return rawPayload
  }

  return ''
}

export function openChatStream({
  endpoint,
  message,
  chatId,
  onOpen,
  onChunk,
  onFinish,
  onError,
}) {
  const streamUrl = new URL(resolveApiUrl(endpoint), window.location.origin)
  streamUrl.searchParams.set('message', message)

  if (chatId) {
    streamUrl.searchParams.set('chatId', chatId)
  }

  const eventSource = new EventSource(streamUrl.toString())
  let settled = false
  let receivedData = false

  const finish = (reason, error = null) => {
    if (settled) {
      return
    }

    settled = true
    eventSource.close()
    onFinish?.({ reason, receivedData, error })
  }

  const handleChunk = (event) => {
    const content = normalizeSsePayload(event.data)

    if (!content) {
      if (event.data?.trim()?.toUpperCase() === '[DONE]') {
        finish('complete')
      }
      return
    }

    receivedData = true
    onChunk?.(content, event)
  }

  const handleStreamError = (event) => {
    if (settled) {
      return
    }

    if (receivedData) {
      finish('complete')
      return
    }

    const error = new Error(
      '无法建立实时连接，请确认后端服务已启动或 /api 代理配置正确。',
    )
    onError?.(error, event)
    finish('error', error)
  }

  STREAM_EVENT_NAMES.forEach((eventName) => {
    eventSource.addEventListener(eventName, handleChunk)
  })

  COMPLETE_EVENT_NAMES.forEach((eventName) => {
    eventSource.addEventListener(eventName, () => finish('complete'))
  })

  eventSource.addEventListener('error', handleStreamError)
  eventSource.onopen = () => onOpen?.()
  eventSource.onerror = handleStreamError

  return {
    close() {
      finish('cancelled')
    },
    get receivedData() {
      return receivedData
    },
  }
}