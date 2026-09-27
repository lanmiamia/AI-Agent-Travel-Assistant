<script setup>
import { ArrowRight, ArrowUpRight, Bot, Compass, Plane, RadioTower } from 'lucide-vue-next'
import { API_BASE_URL } from '../services/http'

const applications = [
  {
    code: 'APP 01',
    name: 'AI 旅游助手',
    description: '围绕目的地、行程、预算与偏好，实时生成可执行的旅行建议。',
    path: '/love-app',
    icon: Plane,
    accent: '#e35d49',
    softAccent: '#fff0eb',
    capability: '旅行规划',
  },
  {
    code: 'APP 02',
    name: 'AI 超级智能体',
    description: '面向复杂任务和多步骤执行，通过实时流返回智能体处理结果。',
    path: '/manus',
    icon: Bot,
    accent: '#167d75',
    softAccent: '#e8f5f2',
    capability: '通用任务',
  },
]
</script>

<template>
  <div class="home-page">
    <header class="home-header">
      <RouterLink class="brand" to="/" aria-label="返回应用工作台首页">
        <span class="brand-mark"><Compass :size="22" /></span>
        <span class="brand-copy">
          <strong>YUP Agent</strong>
          <small>智能应用工作台</small>
        </span>
      </RouterLink>

      <div class="service-state" :title="`后端接口前缀：${API_BASE_URL}`">
        <span class="service-dot"></span>
        <RadioTower :size="16" />
        <span>{{ API_BASE_URL }}</span>
      </div>
    </header>

    <main class="home-main">
      <section class="workspace-heading">
        <div class="heading-copy">
          <span class="eyebrow">应用工作台</span>
          <h1>选择一个智能体开始会话</h1>
          <p>两个应用使用独立的实时会话，并按各自的任务场景提供响应。</p>
        </div>

        <div class="heading-summary" aria-label="应用概览">
          <div>
            <strong>02</strong>
            <span>可用应用</span>
          </div>
          <div>
            <strong>SSE</strong>
            <span>实时输出</span>
          </div>
        </div>
      </section>

      <section class="application-grid" aria-label="应用列表">
        <RouterLink
          v-for="application in applications"
          :key="application.path"
          class="application-card"
          :style="{
            '--accent': application.accent,
            '--soft-accent': application.softAccent,
          }"
          :to="application.path"
        >
          <div class="card-topline">
            <span class="application-icon">
              <component :is="application.icon" :size="25" />
            </span>
            <span class="enter-icon">
              <ArrowUpRight :size="20" />
            </span>
          </div>

          <div class="card-content">
            <span class="application-code">{{ application.code }}</span>
            <h2>{{ application.name }}</h2>
            <p>{{ application.description }}</p>
          </div>

          <div class="card-footer">
            <span>{{ application.capability }}</span>
            <span class="open-label">
              打开应用
              <ArrowRight :size="17" />
            </span>
          </div>
        </RouterLink>
      </section>
    </main>

    <footer class="home-footer">
      <span>API 前缀</span>
      <code>{{ API_BASE_URL }}</code>
    </footer>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  min-height: 100vh;
  min-height: 100dvh;
  flex-direction: column;
  background:
    linear-gradient(90deg, rgba(23, 50, 77, 0.035) 1px, transparent 1px),
    linear-gradient(rgba(23, 50, 77, 0.035) 1px, transparent 1px),
    var(--page-bg);
  background-size: 32px 32px;
}

.home-header {
  display: flex;
  width: min(1180px, calc(100% - 48px));
  min-height: 82px;
  align-items: center;
  justify-content: space-between;
  margin: 0 auto;
  border-bottom: 1px solid rgba(24, 32, 42, 0.12);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
  color: #ffffff;
  background: var(--navy);
  box-shadow: var(--shadow-sm);
}

.brand-copy {
  display: grid;
  gap: 1px;
}

.brand-copy strong {
  font-size: 15px;
  line-height: 1.2;
}

.brand-copy small {
  color: var(--muted);
  font-size: 12px;
}

.service-state {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  color: #4b5560;
  background: rgba(255, 255, 255, 0.76);
  font-size: 12px;
  font-weight: 650;
}

.service-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #2d9b70;
  box-shadow: 0 0 0 4px rgba(45, 155, 112, 0.12);
}

.home-main {
  width: min(1180px, calc(100% - 48px));
  flex: 1;
  margin: 0 auto;
  padding: 74px 0 56px;
}

.workspace-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 32px;
  margin-bottom: 42px;
}

.heading-copy {
  max-width: 680px;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  color: var(--coral-dark);
  font-size: 12px;
  font-weight: 760;
}

.workspace-heading h1 {
  max-width: 720px;
  margin: 12px 0 14px;
  color: var(--ink);
  font-size: clamp(34px, 4.5vw, 58px);
  font-weight: 760;
  line-height: 1.06;
}

.workspace-heading p {
  max-width: 620px;
  margin: 0;
  color: var(--muted);
  font-size: 16px;
  line-height: 1.8;
}

.heading-summary {
  display: flex;
  flex: 0 0 auto;
  gap: 10px;
}

.heading-summary > div {
  display: grid;
  min-width: 132px;
  gap: 4px;
  padding: 16px 18px;
  border: 1px solid rgba(24, 32, 42, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
}

.heading-summary strong {
  color: var(--navy);
  font-size: 22px;
  line-height: 1;
}

.heading-summary span {
  color: var(--muted);
  font-size: 12px;
}

.application-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.application-card {
  display: flex;
  min-height: 340px;
  flex-direction: column;
  justify-content: space-between;
  padding: 26px;
  overflow: hidden;
  border: 1px solid rgba(24, 32, 42, 0.13);
  border-radius: 8px;
  background: #ffffff;
  box-shadow: var(--shadow-sm);
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    box-shadow 180ms ease;
}

.application-card:hover {
  border-color: color-mix(in srgb, var(--accent) 55%, var(--line));
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

.card-topline,
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.application-icon {
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border-radius: 8px;
  color: var(--accent);
  background: var(--soft-accent);
}

.enter-icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 8px;
  color: var(--muted);
  transition:
    color 180ms ease,
    background 180ms ease,
    transform 180ms ease;
}

.application-card:hover .enter-icon {
  color: #ffffff;
  background: var(--accent);
  transform: translate(2px, -2px);
}

.card-content {
  padding: 34px 0 38px;
}

.application-code {
  color: var(--accent);
  font-size: 12px;
  font-weight: 780;
}

.card-content h2 {
  margin: 10px 0 12px;
  font-size: 26px;
  line-height: 1.2;
}

.card-content p {
  max-width: 480px;
  margin: 0;
  color: var(--muted);
  font-size: 15px;
  line-height: 1.75;
}

.card-footer {
  padding-top: 20px;
  border-top: 1px solid var(--line);
  color: var(--muted);
  font-size: 13px;
  font-weight: 650;
}

.open-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--ink);
}

.open-label svg {
  transition: transform 180ms ease;
}

.application-card:hover .open-label svg {
  transform: translateX(3px);
}

.home-footer {
  display: flex;
  width: min(1180px, calc(100% - 48px));
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin: 0 auto;
  padding: 20px 0 26px;
  color: var(--muted);
  font-size: 12px;
}

.home-footer code {
  color: #46515d;
  font-size: 12px;
}

@media (max-width: 820px) {
  .home-header,
  .home-main,
  .home-footer {
    width: min(100% - 32px, 680px);
  }

  .home-main {
    padding-top: 52px;
  }

  .workspace-heading {
    align-items: flex-start;
    flex-direction: column;
    margin-bottom: 30px;
  }

  .heading-summary {
    width: 100%;
  }

  .heading-summary > div {
    flex: 1;
  }

  .application-grid {
    grid-template-columns: 1fr;
  }

  .application-card {
    min-height: 306px;
  }
}

@media (max-width: 560px) {
  .home-header {
    min-height: 72px;
  }

  .brand-copy small,
  .service-state span:last-child {
    display: none;
  }

  .service-state {
    padding: 9px;
  }

  .service-dot {
    display: none;
  }

  .home-main {
    padding: 38px 0 44px;
  }

  .workspace-heading h1 {
    font-size: 37px;
  }

  .workspace-heading p {
    font-size: 14px;
  }

  .heading-summary > div {
    min-width: 0;
    padding: 14px;
  }

  .application-card {
    min-height: 290px;
    padding: 20px;
  }

  .card-content {
    padding: 26px 0 30px;
  }

  .card-content h2 {
    font-size: 23px;
  }

  .home-footer {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }

  .home-footer code {
    overflow-wrap: anywhere;
  }
}
</style>