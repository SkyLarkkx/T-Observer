import { createPinia, setActivePinia } from 'pinia'
import { afterEach, describe, expect, it } from 'vitest'

import { useAuthStore } from './auth'

describe('auth store', () => {
  afterEach(() => {
    window.localStorage.clear()
  })

  it('stores login response in state and localStorage', () => {
    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptLogin({
      token: 'test-token',
      roleCode: 'LEADER',
      realName: 'Leader Zhang',
      userId: 1,
    })

    expect(store.token).toBe('test-token')
    expect(store.roleCode).toBe('LEADER')
    expect(store.realName).toBe('Leader Zhang')
    expect(store.userId).toBe(1)
    expect(window.localStorage.getItem('t-observer-token')).toBe('test-token')
    expect(window.localStorage.getItem('t-observer-role')).toBe('LEADER')
    expect(window.localStorage.getItem('t-observer-name')).toBe('Leader Zhang')
    expect(window.localStorage.getItem('t-observer-user-id')).toBe('1')
  })

  it('hydrates state from localStorage', () => {
    window.localStorage.setItem('t-observer-token', 'cached-token')
    window.localStorage.setItem('t-observer-role', 'MEMBER')
    window.localStorage.setItem('t-observer-name', 'Member Li')
    window.localStorage.setItem('t-observer-user-id', '2')

    setActivePinia(createPinia())
    const store = useAuthStore()

    expect(store.token).toBe('cached-token')
    expect(store.roleCode).toBe('MEMBER')
    expect(store.realName).toBe('Member Li')
    expect(store.userId).toBe(2)
  })
})
