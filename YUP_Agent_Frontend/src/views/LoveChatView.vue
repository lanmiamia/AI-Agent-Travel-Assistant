<script setup>
import { Plane } from 'lucide-vue-next'
import ChatWorkspace from '../components/ChatWorkspace.vue'

const prompts = [
  '帮我规划三天上海亲子游',
  '第一次去成都，怎么安排行程？',
  '预算 5000 元，推荐一个国内海岛目的地',
]
</script>

<template>
  <ChatWorkspace
    app-name="AI 旅游助手"
    app-description="目的地、行程、预算与旅行偏好建议"
    endpoint="/ai/love_app/chat/sse"
    chat-prefix="travel"
    session-key="yup-chat-id-love-app"
    accent="coral"
    :icon="Plane"
    welcome-title="告诉我你的旅行想法。"
    welcome-description="可以包含目的地、天数、预算、同行人和偏好，我会基于这些信息实时生成建议。"
    :prompts="prompts"
    supports-chat-id
  />
</template>