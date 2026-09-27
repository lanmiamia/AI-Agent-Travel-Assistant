<script setup>
import {
  ArrowLeft,
  Bot,
  Check,
  Clipboard,
  Compass,
  Copy,
  MessageSquareText,
  Plane,
  Plus,
  Send,
  Square,
  UserRound,
} from 'lucide-vue-next'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { openChatStream } from '../services/chatStream'
import { createChatId, getOrCreateChatId } from '../utils/chatSession'

const props = defineProps({
  appName: {
    type: String,
    required: true,
  },
  appDescription: {
    type: String,
    required: true,
  },
  endpoint: {
    type: String,
    required: true,
  },
  chatPrefix: {
    type: String,
    required: true,
  },
  sessionKey: {
    type: String,
    required: true,
  },
  accent: {
    type: String,
    default: 'coral',
  },
  icon: {
    type: [Object, Function],
    required: true,
  },
  welcomeTitle: {
    type: String,
    required: true,
  },
  welcomeDescription: {
    type: String,
    required: true,
  },
  prompts: {
    type: Array,
    default: () => [],
  },
  supportsChatId: {
    type: Boolean,
    default: true,
  },
})

const applications = [
  {
    name: 'AI 旅游助手',
    description: '旅行规划',
    path: '/love-app',
    icon: Plane,
    accent: 'coral',
    sessionKey: 'yup-chat-id-love-app',
    chatPrefix: 'travel',
  },
  {
    name: 'AI 超级智能体',
    description: '通用任务',
    path: '/manus',
    icon: Bot,
    accent: 'teal',
    sessionKey: 'yup-chat-id-manus',
    chatPrefix: 'manus',
  },
]

const chatId = ref('')
const messages = ref([])
const input = ref('')
const isStreaming = ref(false)
const streamState = ref('idle')
const copied = ref(false)
const messageArea = ref(null)
const composer = ref(null)
let activeStream = null
let activeRenderer = null
let scrollFrame = null
let copyTimer = null

const shortChatId = computed(() => {
  if (chatId.value.length <= 27) {
    return chatId.value
  }

  return `${chatId.value.slice(0, 13)}...${chatId.value.slice(-8)}`
})

const hasUserMessages = computed(() =>
  messages.value.some((message) => message.role === 'user'),
)

const canSend = computed(() => input.value.trim().length > 0 && !isStreaming.value)

const statusMeta = computed(() => {
  const states = {
    idle: { label: '已就绪', className: 'is-idle' },
    connecting: { label: '连接中', className: 'is-connecting' },
    streaming: { label: '实时生成', className: 'is-streaming' },
    done: { label: '已响应', className: 'is-done' },
    error: { label: '连接异常', className: 'is-error' },
  }

  return states[streamState.value] || states.idle
})

function buildWelcomeMessage() {
  return {
    id: `assistant-welcome-${Date.now()}`,
    role: 'assistant',
    content: `${props.welcomeTitle}\n${props.welcomeDescription}`,
    streaming: false,
    error: false,
  }
}

function scheduleScroll() {
  if (scrollFrame !== null) {
    cancelAnimationFrame(scrollFrame)
  }

  scrollFrame = requestAnimationFrame(() => {
    nextTick(() => {
      if (messageArea.value) {
        messageArea.value.scrollTop = messageArea.value.scrollHeight
      }
    })
  })
}

function resizeComposer() {
  nextTick(() => {
    if (!composer.value) {
      return
    }

    composer.value.style.height = 'auto'
    composer.value.style.height = `${Math.min(composer.value.scrollHeight, 156)}px`
  })
}

function clearRendererFrame(renderer) {
  if (!renderer || renderer.frameId === null) {
    return
  }

  cancelAnimationFrame(renderer.frameId)
  renderer.frameId = null
}

function cancelRenderer(renderer) {
  if (!renderer) {
    return
  }

  clearRendererFrame(renderer)
  renderer.characters = []
  renderer.cursor = 0
  renderer.finished = true
  renderer.finalized = true

  if (activeRenderer === renderer) {
    activeRenderer = null
  }
}

function appendRendererCharacters(renderer, count) {
  const remaining = renderer.characters.length - renderer.cursor

  if (remaining <= 0) {
    return
  }

  const charactersToAppend = Math.min(count, remaining)
  const nextCharacters = renderer.characters
    .slice(renderer.cursor, renderer.cursor + charactersToAppend)
    .join('')

  renderer.cursor += charactersToAppend
  renderer.message.content += nextCharacters
  renderer.message.streaming = true
  streamState.value = 'streaming'
  scheduleScroll()

  if (renderer.cursor >= renderer.characters.length) {
    renderer.characters = []
    renderer.cursor = 0
  }
}

function finalizeRenderer(renderer) {
  if (!renderer || renderer.finalized) {
    return
  }

  clearRendererFrame(renderer)
  renderer.finalized = true

  if (activeRenderer === renderer) {
    activeRenderer = null
  }

  const { message } = renderer
  const result = renderer.result || {}
  const cancelled = result.reason === 'cancelled'

  message.streaming = false
  isStreaming.value = false

  if (message.content) {
    streamState.value = cancelled ? 'idle' : 'done'
  } else if (cancelled) {
    message.content = '已停止生成。'
    streamState.value = 'idle'
  } else {
    message.error = true
    message.content = result.error?.message || '本次响应没有返回内容，请稍后重试。'
    streamState.value = 'error'
  }

  scheduleScroll()
}

function scheduleRendererFrame(renderer) {
  if (!renderer || renderer.finalized || renderer.frameId !== null) {
    return
  }

  renderer.frameId = requestAnimationFrame(() => {
    renderer.frameId = null
    runRendererFrame(renderer)
  })
}

function runRendererFrame(renderer) {
  if (!renderer || renderer.finalized) {
    return
  }

  const remaining = renderer.characters.length - renderer.cursor

  if (remaining <= 0) {
    if (renderer.finished) {
      finalizeRenderer(renderer)
    }
    return
  }

  let charactersPerFrame = remaining > 240 ? 4 : remaining > 100 ? 2 : 1

  if (renderer.finished && remaining > charactersPerFrame) {
    charactersPerFrame = Math.max(charactersPerFrame, Math.ceil(remaining / 24))
  }

  appendRendererCharacters(renderer, charactersPerFrame)

  if (renderer.characters.length > renderer.cursor) {
    scheduleRendererFrame(renderer)
  } else if (renderer.finished) {
    finalizeRenderer(renderer)
  }
}

function enqueueStreamContent(renderer, content) {
  if (!renderer || renderer.finalized || !content) {
    return
  }

  for (const character of content) {
    renderer.characters.push(character)
  }

  renderer.message.streaming = true

  if (!renderer.started) {
    renderer.started = true
    appendRendererCharacters(renderer, 1)
  }

  scheduleRendererFrame(renderer)
}

function finishRenderer(renderer, result) {
  if (!renderer || renderer.finalized) {
    return
  }

  renderer.finished = true
  renderer.result = result

  if (result.reason === 'cancelled') {
    renderer.characters = []
    renderer.cursor = 0
    clearRendererFrame(renderer)
    finalizeRenderer(renderer)
    return
  }

  if (renderer.characters.length <= renderer.cursor) {
    finalizeRenderer(renderer)
    return
  }

  scheduleRendererFrame(renderer)
}

function sendMessage(prompt) {
  const messageText = typeof prompt === 'string' ? prompt.trim() : input.value.trim()

  if (!messageText || isStreaming.value) {
    return
  }

  input.value = ''
  resizeComposer()

  const userMessage = reactive({
    id: `user-${Date.now()}`,
    role: 'user',
    content: messageText,
    streaming: false,
    error: false,
  })
  const assistantMessage = reactive({
    id: `assistant-${Date.now()}`,
    role: 'assistant',
    content: '',
    streaming: true,
    error: false,
  })
  const renderer = {
    message: assistantMessage,
    characters: [],
    cursor: 0,
    frameId: null,
    started: false,
    finished: false,
    finalized: false,
    result: null,
  }

  messages.value.push(userMessage, assistantMessage)
  activeRenderer = renderer
  isStreaming.value = true
  streamState.value = 'connecting'
  scheduleScroll()

  try {
    activeStream = openChatStream({
      endpoint: props.endpoint,
      message: messageText,
      chatId: props.supportsChatId ? chatId.value : '',
      onOpen() {
        if (isStreaming.value) {
          streamState.value = 'streaming'
        }
      },
      onChunk(content) {
        enqueueStreamContent(renderer, content)
      },
      onFinish(result) {
        activeStream = null
        finishRenderer(renderer, result)
      },
    })
  } catch (error) {
    activeStream = null
    finishRenderer(renderer, { reason: 'error', error })
  }
}

function stopStreaming() {
  activeStream?.close()
}

function startNewSession() {
  activeStream?.close()
  cancelRenderer(activeRenderer)
  chatId.value = createChatId(props.chatPrefix)

  try {
    sessionStorage.setItem(props.sessionKey, chatId.value)
  } catch {
    // Session storage can be unavailable in private browsing modes.
  }

  messages.value = [buildWelcomeMessage()]
  input.value = ''
  streamState.value = 'idle'
  nextTick(() => {
    resizeComposer()
    composer.value?.focus()
  })
}

async function copyChatId() {
  try {
    await navigator.clipboard.writeText(chatId.value)
  } catch {
    const temporaryInput = document.createElement('textarea')
    temporaryInput.value = chatId.value
    temporaryInput.style.position = 'fixed'
    temporaryInput.style.opacity = '0'
    document.body.appendChild(temporaryInput)
    temporaryInput.select()
    document.execCommand('copy')
    temporaryInput.remove()
  }

  copied.value = true
  window.clearTimeout(copyTimer)
  copyTimer = window.setTimeout(() => {
    copied.value = false
  }, 1600)
}

function handleComposerKeydown(event) {
  if (
    event.key === 'Enter' &&
    !event.shiftKey &&
    !event.isComposing &&
    !event.nativeEvent?.isComposing
  ) {
    event.preventDefault()
    sendMessage()
  }
}

watch(messages, scheduleScroll, { deep: true, flush: 'post' })
watch(input, resizeComposer)

onMounted(() => {
  chatId.value = getOrCreateChatId(props.sessionKey, props.chatPrefix)
  messages.value = [buildWelcomeMessage()]
  nextTick(() => {
    resizeComposer()
    composer.value?.focus()
  })
})

onBeforeUnmount(() => {
  activeStream?.close()
  cancelRenderer(activeRenderer)
  window.clearTimeout(copyTimer)

  if (scrollFrame !== null) {
    cancelAnimationFrame(scrollFrame)
  }
})
</script>




<template>
  <div class="chat-page" :class="`accent-${accent}`">
    <aside class="chat-sidebar">
      <RouterLink class="sidebar-brand" to="/" aria-label="返回应用工作台首页">
        <span class="sidebar-brand-mark"><Compass :size="21" /></span>
        <span>
          <strong>YUP Agent</strong>
          <small>智能应用工作台</small>
        </span>
      </RouterLink>

      <nav class="application-nav" aria-label="应用切换">
        <RouterLink
          v-for="application in applications"
          :key="application.path"
          class="application-nav-item"
          active-class="is-active"
          :to="application.path"
        >
          <component :is="application.icon" :size="19" />
          <span>
            <strong>{{ application.name }}</strong>
            <small>{{ application.description }}</small>
          </span>
        </RouterLink>
      </nav>

      <section class="session-panel" aria-label="当前会话信息">
        <div class="session-panel-title">
          <span>当前会话</span>
          <MessageSquareText :size="17" />
        </div>

        <button
          class="chat-id-button"
          type="button"
          :title="`复制 Chat ID: ${chatId}`"
          @click="copyChatId"
        >
          <code>{{ shortChatId }}</code>
          <Check v-if="copied" :size="15" />
          <Copy v-else :size="15" />
        </button>

        <button class="new-session-button" type="button" @click="startNewSession">
          <Plus :size="17" />
          <span>新建会话</span>
        </button>

        <div class="endpoint-label">
          <span>SSE</span>
          <code>{{ endpoint }}</code>
        </div>
      </section>
    </aside>

    <main class="chat-main">
      <header class="chat-header">
        <div class="chat-heading">
          <RouterLink class="mobile-home-button" to="/" title="返回主页">
            <ArrowLeft :size="19" />
          </RouterLink>

          <span class="app-icon">
            <component :is="icon" :size="22" />
          </span>

          <div class="heading-text">
            <div class="heading-title-line">
              <h1>{{ appName }}</h1>
              <span class="status-pill" :class="statusMeta.className">
                <span class="status-dot"></span>
                {{ statusMeta.label }}
              </span>
            </div>
            <p>{{ appDescription }}</p>
          </div>
        </div>

        <div class="header-actions">
          <button
            class="icon-button"
            type="button"
            :title="`复制 Chat ID: ${chatId}`"
            @click="copyChatId"
          >
            <Check v-if="copied" :size="18" />
            <Clipboard v-else :size="18" />
          </button>

          <button
            class="new-session-header-button"
            type="button"
            title="新建会话"
            @click="startNewSession"
          >
            <Plus :size="18" />
            <span>新建会话</span>
          </button>
        </div>
      </header>

      <div class="mobile-session-bar">
        <span>会话</span>
        <code>{{ shortChatId }}</code>
        <button type="button" title="复制 Chat ID" @click="copyChatId">
          <Check v-if="copied" :size="15" />
          <Copy v-else :size="15" />
        </button>
      </div>

      <section
        ref="messageArea"
        class="message-area"
        role="log"
        aria-live="polite"
        aria-label="聊天记录"
      >
        <div class="message-column">
          <article
            v-for="message in messages"
            :key="message.id"
            class="message-row"
            :class="message.role"
          >
            <span class="message-avatar" :class="message.role">
              <UserRound v-if="message.role === 'user'" :size="18" />
              <component v-else :is="icon" :size="18" />
            </span>

            <div class="message-content">
              <div class="message-meta">
                <span>{{ message.role === 'user' ? '你' : appName }}</span>
                <span v-if="message.streaming">生成中</span>
              </div>

              <div
                class="message-bubble"
                :class="{ 'is-streaming': message.streaming, 'is-error': message.error }"
              >
                <span v-if="message.content" class="message-text">{{ message.content }}</span>
                <span
                  v-if="message.streaming"
                  class="stream-cursor" :class="{ 'is-waiting': !message.content }"
                  aria-hidden="true"
                ></span>
              </div>
            </div>
          </article>

          <div v-if="!hasUserMessages && prompts.length" class="prompt-suggestions">
            <button
              v-for="prompt in prompts"
              :key="prompt"
              type="button"
              @click="sendMessage(prompt)"
            >
              {{ prompt }}
            </button>
          </div>
        </div>
      </section>

      <footer class="composer-area">
        <form class="composer" @submit.prevent="sendMessage()">
          <textarea
            ref="composer"
            v-model="input"
            rows="1"
            :placeholder="`向${appName}发送消息`"
            :disabled="isStreaming"
            @keydown="handleComposerKeydown"
          ></textarea>

          <div class="composer-actions">
            <button
              v-if="isStreaming"
              class="stop-button"
              type="button"
              title="停止生成"
              @click="stopStreaming"
            >
              <Square :size="15" fill="currentColor" />
              <span>停止</span>
            </button>
            <button
              v-else
              class="send-button"
              type="submit"
              title="发送消息"
              :disabled="!canSend"
            >
              <Send :size="18" />
            </button>
          </div>
        </form>
      </footer>
    </main>
  </div>
</template>

<style scoped>
.chat-page {
  display: grid;
  width: 100%;
  height: 100vh;
  height: 100dvh;
  grid-template-columns: 288px minmax(0, 1fr);
  overflow: hidden;
  background: #f7f5f0;
}

.chat-page.accent-coral {
  --app-accent: var(--coral);
  --app-accent-dark: var(--coral-dark);
  --app-accent-soft: var(--coral-soft);
}

.chat-page.accent-teal {
  --app-accent: var(--teal);
  --app-accent-dark: var(--teal-dark);
  --app-accent-soft: var(--teal-soft);
}

.chat-sidebar {
  display: flex;
  min-height: 0;
  flex-direction: column;
  padding: 22px 18px;
  color: #eef4f7;
  background: var(--navy);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 6px 22px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.sidebar-brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  color: var(--navy);
  background: #ffffff;
}

.sidebar-brand > span:last-child {
  display: grid;
  gap: 1px;
}

.sidebar-brand strong {
  font-size: 14px;
}

.sidebar-brand small {
  color: rgba(238, 244, 247, 0.63);
  font-size: 11px;
}

.application-nav {
  display: grid;
  gap: 7px;
  padding: 22px 0;
}

.application-nav-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 12px 11px;
  border: 1px solid transparent;
  border-radius: 8px;
  color: rgba(238, 244, 247, 0.72);
  transition:
    color 160ms ease,
    background 160ms ease,
    border-color 160ms ease;
}

.application-nav-item:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.07);
}

.application-nav-item.is-active {
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.11);
}

.application-nav-item > span {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.application-nav-item strong {
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.application-nav-item small {
  color: rgba(238, 244, 247, 0.5);
  font-size: 11px;
}

.session-panel {
  display: grid;
  gap: 10px;
  margin-top: auto;
  padding: 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
}

.session-panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: rgba(238, 244, 247, 0.66);
  font-size: 11px;
  font-weight: 700;
}

.chat-id-button {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.14);
  cursor: pointer;
}

.chat-id-button code {
  min-width: 0;
  overflow: hidden;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.new-session-button,
.new-session-header-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
}

.new-session-button {
  width: 100%;
  padding: 10px;
  color: var(--navy);
  background: #ffffff;
}

.endpoint-label {
  display: grid;
  min-width: 0;
  gap: 4px;
  padding-top: 6px;
  color: rgba(238, 244, 247, 0.5);
  font-size: 10px;
}

.endpoint-label span {
  color: var(--app-accent);
  font-weight: 800;
}

.endpoint-label code {
  overflow: hidden;
  color: rgba(238, 244, 247, 0.66);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-main {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
  background: #f7f5f0;
}

.chat-header {
  display: flex;
  min-height: 82px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 17px 24px;
  border-bottom: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.72);
}

.chat-heading {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.mobile-home-button {
  display: none;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #ffffff;
}

.app-icon {
  display: grid;
  width: 42px;
  height: 42px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  color: var(--app-accent);
  background: var(--app-accent-soft);
}

.heading-text {
  min-width: 0;
}

.heading-title-line {
  display: flex;
  align-items: center;
  gap: 10px;
}

.heading-text h1 {
  overflow: hidden;
  margin: 0;
  font-size: 17px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.heading-text p {
  overflow: hidden;
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-pill {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 6px;
  padding: 5px 8px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--muted);
  background: #ffffff;
  font-size: 10px;
  font-weight: 750;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #8d969f;
}

.status-pill.is-connecting .status-dot,
.status-pill.is-streaming .status-dot {
  background: var(--app-accent);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--app-accent) 15%, transparent);
  animation: status-pulse 1.4s ease-in-out infinite;
}

.status-pill.is-done .status-dot {
  background: #2d9b70;
}

.status-pill.is-error .status-dot {
  background: var(--danger);
}

.header-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
}

.icon-button,
.new-session-header-button {
  height: 40px;
  border: 1px solid var(--line);
  color: #38434e;
  background: #ffffff;
}

.icon-button {
  display: grid;
  width: 40px;
  place-items: center;
  border-radius: 8px;
  cursor: pointer;
}

.new-session-header-button {
  padding: 0 13px;
}

.mobile-session-bar {
  display: none;
}

.message-area {
  min-height: 0;
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 32px 28px 24px;
}

.message-column {
  width: min(980px, 100%);
  margin: 0 auto;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  margin-bottom: 24px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.message-avatar {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 8px;
  color: var(--app-accent-dark);
  background: var(--app-accent-soft);
}

.message-avatar.user {
  color: #ffffff;
  border-color: var(--navy);
  background: var(--navy);
}

.message-content {
  min-width: 0;
  max-width: min(760px, calc(100% - 45px));
}

.message-row.user .message-content {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 1px 0 7px;
  color: var(--muted);
  font-size: 11px;
}

.message-row.user .message-meta {
  flex-direction: row-reverse;
}

.message-meta span:last-child:not(:first-child) {
  color: var(--app-accent-dark);
  font-weight: 700;
}

.message-bubble {
  position: relative;
  min-height: 42px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 2px 8px 8px;
  color: #27313c;
  background: #ffffff;
  box-shadow: var(--shadow-sm);
}

.message-row.user .message-bubble {
  border-color: var(--navy);
  border-radius: 8px 2px 8px 8px;
  color: #ffffff;
  background: var(--navy);
}

.message-bubble.is-error {
  border-color: rgba(190, 63, 63, 0.32);
  color: #8f3030;
  background: #fff6f5;
}

.message-text {
  display: block;
  overflow-wrap: anywhere;
  font-size: 14.5px;
  line-height: 1.78;
  white-space: pre-wrap;
}

.message-row.assistant .message-text {
  color: #27313c;
}

.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 15px;
  margin-left: 3px;
  vertical-align: -2px;
  background: var(--app-accent);
  animation: cursor-blink 0.9s steps(1) infinite;
}

.stream-cursor.is-waiting {
  margin-left: 0;
  vertical-align: 0;
}

.prompt-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
  padding: 4px 0 12px 45px;
}

.prompt-suggestions button {
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  color: #48535e;
  background: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  font-size: 12px;
  text-align: left;
  transition:
    color 160ms ease,
    border-color 160ms ease,
    background 160ms ease;
}

.prompt-suggestions button:hover {
  color: var(--app-accent-dark);
  border-color: color-mix(in srgb, var(--app-accent) 40%, var(--line));
  background: var(--app-accent-soft);
}

.composer-area {
  flex: 0 0 auto;
  padding: 14px 28px 20px;
  border-top: 1px solid rgba(223, 226, 223, 0.72);
  background: #f7f5f0;
}

.composer {
  display: flex;
  width: min(980px, 100%);
  min-height: 60px;
  align-items: flex-end;
  gap: 12px;
  margin: 0 auto;
  padding: 10px 10px 10px 16px;
  border: 1px solid var(--line-strong);
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(24, 32, 42, 0.08);
  transition:
    border-color 160ms ease,
    box-shadow 160ms ease;
}

.composer:focus-within {
  border-color: color-mix(in srgb, var(--app-accent) 55%, var(--line-strong));
  box-shadow:
    0 0 0 3px color-mix(in srgb, var(--app-accent) 10%, transparent),
    0 10px 30px rgba(24, 32, 42, 0.08);
}

.composer textarea {
  min-width: 0;
  min-height: 38px;
  max-height: 156px;
  flex: 1;
  resize: none;
  overflow-y: auto;
  padding: 8px 0 7px;
  border: 0;
  outline: 0;
  color: var(--ink);
  background: transparent;
  font-size: 14.5px;
  line-height: 1.55;
}

.composer textarea::placeholder {
  color: #939aa1;
}

.composer textarea:disabled {
  cursor: wait;
}

.composer-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
}

.send-button {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 8px;
  color: #ffffff;
  background: var(--app-accent);
  cursor: pointer;
  transition:
    transform 160ms ease,
    opacity 160ms ease,
    background 160ms ease;
}

.send-button:hover:not(:disabled) {
  background: var(--app-accent-dark);
  transform: translateY(-1px);
}

.send-button:disabled {
  cursor: not-allowed;
  opacity: 0.38;
}

.stop-button {
  display: inline-flex;
  height: 40px;
  align-items: center;
  gap: 7px;
  padding: 0 12px;
  border: 1px solid rgba(190, 63, 63, 0.24);
  border-radius: 8px;
  color: var(--danger);
  background: #fff5f4;
  cursor: pointer;
  font-size: 12px;
  font-weight: 750;
}

@keyframes cursor-blink {
  50% {
    opacity: 0;
  }
}

@keyframes status-pulse {
  50% {
    opacity: 0.45;
  }
}

@media (max-width: 900px) {
  .chat-page {
    grid-template-columns: 1fr;
  }

  .chat-sidebar {
    display: none;
  }

  .mobile-home-button {
    display: grid;
  }

  .mobile-session-bar {
    display: flex;
    min-height: 38px;
    flex: 0 0 auto;
    align-items: center;
    gap: 8px;
    padding: 7px 18px;
    border-bottom: 1px solid var(--line);
    color: var(--muted);
    background: rgba(255, 255, 255, 0.58);
    font-size: 11px;
  }

  .mobile-session-bar code {
    min-width: 0;
    overflow: hidden;
    color: #46515d;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mobile-session-bar button {
    display: grid;
    width: 28px;
    height: 28px;
    margin-left: auto;
    place-items: center;
    border: 1px solid var(--line);
    border-radius: 8px;
    color: #46515d;
    background: #ffffff;
    cursor: pointer;
  }
}

@media (max-width: 640px) {
  .chat-header {
    min-height: 70px;
    padding: 12px;
  }

  .app-icon {
    display: none;
  }

  .heading-title-line {
    align-items: flex-start;
    flex-direction: column;
    gap: 5px;
  }

  .heading-text h1 {
    font-size: 15px;
  }

  .heading-text p {
    display: none;
  }

  .new-session-header-button {
    width: 40px;
    padding: 0;
  }

  .new-session-header-button span {
    display: none;
  }

  .message-area {
    padding: 22px 12px 18px;
  }

  .message-row {
    gap: 8px;
    margin-bottom: 19px;
  }

  .message-avatar {
    width: 30px;
    height: 30px;
  }

  .message-content {
    max-width: calc(100% - 38px);
  }

  .message-bubble {
    padding: 10px 12px;
  }

  .message-text {
    font-size: 14px;
    line-height: 1.72;
  }

  .prompt-suggestions {
    padding-left: 38px;
  }

  .prompt-suggestions button {
    width: 100%;
  }

  .composer-area {
    padding: 10px 12px 14px;
  }

  .composer {
    padding-left: 13px;
  }

  .stop-button span {
    display: none;
  }

  .stop-button {
    width: 40px;
    justify-content: center;
    padding: 0;
  }
}
</style>