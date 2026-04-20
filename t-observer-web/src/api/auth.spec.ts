import { afterEach, describe, expect, it, vi } from 'vitest'

import { http } from './http'
import { fetchCurrentUser, login } from './auth'

vi.mock('./http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('auth api', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('posts login credentials to /auth/login and unwraps the payload', async () => {
    vi.mocked(http.post).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          token: 'token-1',
          userId: 1,
          realName: '张老师',
          roleCode: 'LEADER',
        },
      },
    })

    const result = await login({
      username: 'leader01',
      password: '123456',
    })

    expect(http.post).toHaveBeenCalledWith('/auth/login', {
      username: 'leader01',
      password: '123456',
    })
    expect(result.realName).toBe('张老师')
    expect(result.roleCode).toBe('LEADER')
  })

  it('gets /auth/me and unwraps the current user payload', async () => {
    vi.mocked(http.get).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          userId: 2,
          username: 'member01',
          realName: '李老师',
          roleCode: 'MEMBER',
        },
      },
    })

    const result = await fetchCurrentUser()

    expect(http.get).toHaveBeenCalledWith('/auth/me')
    expect(result.username).toBe('member01')
    expect(result.realName).toBe('李老师')
  })
})
