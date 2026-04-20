import { createPinia, setActivePinia } from 'pinia'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createMemoryHistory } from 'vue-router'

import { fetchCurrentUser } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

import { createAppRouter } from './index'

vi.mock('@/api/auth', () => ({
  fetchCurrentUser: vi.fn(),
}))

describe('app router auth guard', () => {
  afterEach(() => {
    window.localStorage.clear()
    vi.clearAllMocks()
  })

  it('redirects anonymous users from protected routes to /login', async () => {
    setActivePinia(createPinia())
    const router = createAppRouter(createMemoryHistory())

    await router.push('/overview')
    await router.isReady()

    expect(router.currentRoute.value.fullPath).toBe('/login')
  })

  it('restores a persisted session through /api/auth/me before entering /overview', async () => {
    window.localStorage.setItem('t-observer-token', 'cached-token')
    setActivePinia(createPinia())
    const router = createAppRouter(createMemoryHistory())

    vi.mocked(fetchCurrentUser).mockResolvedValue({
      userId: 1,
      username: 'leader01',
      realName: '张老师',
      roleCode: 'LEADER',
    })

    await router.push('/overview')
    await router.isReady()

    const store = useAuthStore()
    expect(fetchCurrentUser).toHaveBeenCalledTimes(1)
    expect(router.currentRoute.value.fullPath).toBe('/overview')
    expect(store.realName).toBe('张老师')
    expect(store.isValidated).toBe(true)
  })

  it('clears auth and falls back to /login when /api/auth/me fails', async () => {
    window.localStorage.setItem('t-observer-token', 'expired-token')
    setActivePinia(createPinia())
    const router = createAppRouter(createMemoryHistory())

    vi.mocked(fetchCurrentUser).mockRejectedValue(new Error('401'))

    await router.push('/tasks')
    await router.isReady()

    const store = useAuthStore()
    expect(router.currentRoute.value.fullPath).toBe('/login')
    expect(store.token).toBe('')
    expect(store.isValidated).toBe(false)
  })
})
