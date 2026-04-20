import { defineComponent, h } from 'vue'
import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
  type RouterHistory,
} from 'vue-router'

import { fetchCurrentUser } from '@/api/auth'
import MainLayout from '@/layouts/MainLayout.vue'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/login/LoginView.vue'

const createPreviewPage = (title: string, description: string) =>
  defineComponent({
    name: `${title}Preview`,
    setup() {
      return () =>
        h('div', { class: 'route-preview' }, [
          h('h1', { class: 'route-preview__title' }, title),
          h('p', { class: 'route-preview__description' }, description),
        ])
    },
  })

const OverviewPreview = createPreviewPage('概览', '这是当前主布局的占位预览页。')
const TasksPreview = createPreviewPage('任务', '任务页面尚未接入，本页用于保持主布局内容完整。')
const RecordsPreview = createPreviewPage('记录', '记录页面尚未接入，本页用于保持主布局内容完整。')

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: { name: 'dashboard-overview' },
      },
      {
        path: 'overview',
        name: 'dashboard-overview',
        component: OverviewPreview,
        meta: { requiresAuth: true },
      },
      {
        path: 'tasks',
        name: 'dashboard-tasks',
        component: TasksPreview,
        meta: { requiresAuth: true },
      },
      {
        path: 'records',
        name: 'dashboard-records',
        component: RecordsPreview,
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true },
  },
]

export function createAppRouter(
  history: RouterHistory = createWebHistory(import.meta.env.BASE_URL),
) {
  const router = createRouter({
    history,
    routes,
  })

  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const hasToken = Boolean(authStore.token)
    const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
    const guestOnly = to.matched.some((record) => record.meta.guestOnly)

    if (!hasToken) {
      return requiresAuth ? { name: 'login' } : true
    }

    if (!authStore.isValidated) {
      try {
        const currentUser = await fetchCurrentUser()
        authStore.acceptCurrentUser(currentUser)
      } catch {
        authStore.logout()
        return guestOnly ? true : { name: 'login' }
      }
    }

    if (guestOnly) {
      return { name: 'dashboard-overview' }
    }

    return true
  })

  return router
}

const router = createAppRouter()

export default router
