import { createPinia, setActivePinia } from 'pinia'
import { afterEach, describe, expect, it } from 'vitest'

import { useAuthStore } from './auth'

describe('auth store', () => {
  afterEach(() => {
    window.localStorage.clear()
  })

  it('stores login response in state and marks the session validated', () => {
    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptLogin({
      token: 'test-token',
      roleCode: 'LEADER',
      realName: '张老师',
      userId: 1,
    })

    expect(store.token).toBe('test-token')
    expect(store.roleCode).toBe('LEADER')
    expect(store.realName).toBe('张老师')
    expect(store.userId).toBe(1)
    expect(store.isValidated).toBe(true)
    expect(store.roleLabel).toBe('组长')
    expect(window.localStorage.getItem('t-observer-token')).toBe('test-token')
    expect(window.localStorage.getItem('t-observer-role')).toBe('LEADER')
    expect(window.localStorage.getItem('t-observer-name')).toBe('张老师')
    expect(window.localStorage.getItem('t-observer-user-id')).toBe('1')
  })

  it('refreshes user identity from /me without replacing the token', () => {
    window.localStorage.setItem('t-observer-token', 'cached-token')

    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptCurrentUser({
      userId: 2,
      username: 'member01',
      realName: '李老师',
      roleCode: 'MEMBER',
    })

    expect(store.token).toBe('cached-token')
    expect(store.roleCode).toBe('MEMBER')
    expect(store.realName).toBe('李老师')
    expect(store.userId).toBe(2)
    expect(store.isValidated).toBe(true)
    expect(store.roleLabel).toBe('教师')
  })

  it('clears state and localStorage on logout', () => {
    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptLogin({
      token: 'test-token',
      roleCode: 'ADMIN',
      realName: '管理员',
      userId: 3,
    })

    store.logout()

    expect(store.token).toBe('')
    expect(store.roleCode).toBe('')
    expect(store.realName).toBe('')
    expect(store.userId).toBe(0)
    expect(store.isValidated).toBe(false)
    expect(window.localStorage.getItem('t-observer-token')).toBe(null)
    expect(window.localStorage.getItem('t-observer-role')).toBe(null)
    expect(window.localStorage.getItem('t-observer-name')).toBe(null)
    expect(window.localStorage.getItem('t-observer-user-id')).toBe(null)
  })
})
