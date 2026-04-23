import ElementPlus from 'element-plus'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'

import { login } from '@/api/auth'

import LoginView from './LoginView.vue'

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
}))

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders Chinese login copy and fields', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('本地账号登录')
    expect(wrapper.find('input[placeholder="请输入用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="请输入密码"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="login-submit"]').text()).toContain('登录')
  })

  it('submits credentials, stores login state, and jumps to /tasks', async () => {
    vi.mocked(login).mockResolvedValue({
      token: 'token-1',
      userId: 1,
      realName: '张老师',
      roleCode: 'LEADER',
    })

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/login', component: LoginView },
        { path: '/tasks', component: { template: '<div>tasks</div>' } },
      ],
    })

    await router.push('/login')
    await router.isReady()

    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus, createPinia(), router],
      },
    })

    await wrapper.find('input[placeholder="请输入用户名"]').setValue('leader01')
    await wrapper.find('input[placeholder="请输入密码"]').setValue('123456')
    await wrapper.find('[data-testid="login-submit"]').trigger('click')
    await flushPromises()

    expect(login).toHaveBeenCalledWith({
      username: 'leader01',
      password: '123456',
    })
    expect(router.currentRoute.value.fullPath).toBe('/tasks')
  })
})
