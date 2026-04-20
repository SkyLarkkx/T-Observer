import ElementPlus from 'element-plus'
import { createPinia, setActivePinia } from 'pinia'
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

import MainLayout from './MainLayout.vue'

describe('MainLayout', () => {
  it('renders the sidebar shell with Chinese user identity', async () => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()

    authStore.acceptLogin({
      token: 'token-1',
      userId: 1,
      realName: '张老师',
      roleCode: 'LEADER',
    })

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/overview',
          name: 'dashboard-overview',
          component: MainLayout,
        },
      ],
    })

    await router.push('/overview')
    await router.isReady()

    const wrapper = mount(MainLayout, {
      global: {
        plugins: [ElementPlus, router],
        stubs: {
          RouterLink: {
            template: '<a><slot /></a>',
          },
          RouterView: {
            template: '<div data-testid="layout-slot">路由内容</div>',
          },
        },
      },
    })

    expect(wrapper.find('[data-testid="app-sidebar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="app-breadcrumbs"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="user-menu-trigger"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('张老师')
  })

  it('shows clickable breadcrumb navigation and a back button on the record page', async () => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()

    authStore.acceptLogin({
      token: 'token-2',
      userId: 2,
      realName: '李老师',
      roleCode: 'MEMBER',
    })

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/',
          component: MainLayout,
          children: [
            {
              path: 'member/tasks',
              name: 'member-task-list',
              component: { template: '<div>任务列表</div>' },
            },
            {
              path: 'member/tasks/:taskId/record',
              name: 'member-record-form',
              component: { template: '<div>记录填写</div>' },
            },
          ],
        },
      ],
    })

    await router.push('/member/tasks/7/record')
    await router.isReady()

    const wrapper = mount(MainLayout, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    const pushSpy = vi.spyOn(router, 'push')

    expect(wrapper.text()).toContain('任务')
    expect(wrapper.text()).toContain('记录填写')
    expect(wrapper.find('[data-testid="breadcrumb-back"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="breadcrumb-link-member-task-list"]').exists()).toBe(true)

    await wrapper.find('[data-testid="breadcrumb-link-member-task-list"]').trigger('click')

    expect(pushSpy).toHaveBeenCalled()
  })
})
