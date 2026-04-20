import { defineComponent, h } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import LoginView from '@/views/login/LoginView.vue'

const createPreviewPage = (title: string, description: string) =>
  defineComponent({
    name: `${title.replace(/\s+/g, '')}Preview`,
    setup() {
      return () =>
        h('div', { class: 'route-preview' }, [
          h('h1', { class: 'route-preview__title' }, title),
          h('p', { class: 'route-preview__description' }, description),
        ])
    },
  })

const OverviewPreview = createPreviewPage(
  'Overview',
  'This is the default dashboard preview for the shell route.',
)
const TasksPreview = createPreviewPage(
  'Tasks',
  'Task content is not wired yet, so this placeholder keeps the shell populated.',
)
const RecordsPreview = createPreviewPage(
  'Records',
  'Record content is not wired yet, so this placeholder keeps the shell populated.',
)

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          redirect: { name: 'dashboard-overview' },
        },
        {
          path: 'overview',
          name: 'dashboard-overview',
          component: OverviewPreview,
        },
        {
          path: 'tasks',
          name: 'dashboard-tasks',
          component: TasksPreview,
        },
        {
          path: 'records',
          name: 'dashboard-records',
          component: RecordsPreview,
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
  ],
})

export default router
