import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
  },
  {
    path: '/love-app',
    name: 'love-app',
    component: () => import('../views/LoveChatView.vue'),
  },
  {
    path: '/manus',
    name: 'manus',
    component: () => import('../views/ManusChatView.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

export default createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})