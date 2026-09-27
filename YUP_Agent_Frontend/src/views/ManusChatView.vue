<script setup>
import { Bot } from 'lucide-vue-next'
import ChatWorkspace from '../components/ChatWorkspace.vue'

const prompts = [
  '拆解一个可执行的项目计划',
  '帮我分析这个任务的关键风险',
  '整理一份结构化的行动清单',
]
</script>

<template>
  <ChatWorkspace
    app-name="AI 超级智能体"
    app-description="复杂任务拆解、工具调用与多步骤执行"
    endpoint="/ai/manus/chat"
    chat-prefix="manus"
    session-key="yup-chat-id-manus"
    accent="teal"
    :icon="Bot"
    welcome-title="描述需要完成的任务。"
    welcome-description="我会实时返回处理过程和结果，适合需要规划、分析或分步骤执行的复杂任务。"
    :prompts="prompts"
  />
</template>