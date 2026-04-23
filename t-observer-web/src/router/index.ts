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
import type { RoleCode } from '@/types/auth'
import LeaderTaskManageView from '@/views/leader/LeaderTaskManageView.vue'
import ReviewListView from '@/views/leader/ReviewListView.vue'
import ReviewView from '@/views/leader/ReviewView.vue'
import AnalyticsView from '@/views/leader/AnalyticsView.vue'
import LoginView from '@/views/login/LoginView.vue'
import MemberTaskListView from '@/views/member/MemberTaskListView.vue'
import RecordFormView from '@/views/member/RecordFormView.vue'

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

const OverviewPreview = createPreviewPage('概览', '当前页面用于保留主布局概览入口。')

function resolveRoleTaskRoute(roleCode: RoleCode | '') {
  if (roleCode === 'LEADER') {
    return { name: 'leader-task-manage' as const }
  }

  if (roleCode === 'MEMBER') {
    return { name: 'member-task-list' as const }
  }

  return { name: 'dashboard-overview' as const }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: { name: 'dashboard-tasks' },
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
        component: OverviewPreview,
        meta: { requiresAuth: true },
      },
      {
        path: 'member/tasks',
        name: 'member-task-list',
        component: MemberTaskListView,
        meta: { requiresAuth: true },
      },
      {
        path: 'member/tasks/:taskId/record',
        name: 'member-record-form',
        component: RecordFormView,
        meta: { requiresAuth: true },
      },
      {
        path: 'leader/tasks',
        name: 'leader-task-manage',
        component: LeaderTaskManageView,
        meta: { requiresAuth: true },
      },
      {
        path: 'leader/reviews',
        name: 'leader-review-list',
        component: ReviewListView,
        meta: { requiresAuth: true },
      },
      {
        path: 'leader/reviews/:recordId',
        name: 'leader-review-form',
        component: ReviewView,
        meta: { requiresAuth: true },
      },
      {
        path: 'leader/analytics',
        name: 'leader-analytics',
        component: AnalyticsView,
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
      return resolveRoleTaskRoute(authStore.roleCode)
    }

    if (to.name === 'dashboard-tasks') {
      return resolveRoleTaskRoute(authStore.roleCode)
    }

    return true
  })

  return router
}

const router = createAppRouter()

export default router
