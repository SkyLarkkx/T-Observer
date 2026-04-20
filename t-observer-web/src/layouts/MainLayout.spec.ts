import ElementPlus from 'element-plus'
import { createPinia, setActivePinia } from 'pinia'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
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
    expect(wrapper.text()).toContain('组长')
  })
})
