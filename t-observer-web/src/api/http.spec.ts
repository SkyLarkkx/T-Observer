import type { InternalAxiosRequestConfig } from 'axios'
import { afterEach, describe, expect, it } from 'vitest'

import { http } from './http'

describe('http client', () => {
  afterEach(() => {
    window.localStorage.clear()
  })

  it('uses the proxied API base URL', () => {
    expect(http.defaults.baseURL).toBe('/api')
  })

  it('injects X-Auth-Token from localStorage', async () => {
    window.localStorage.setItem('t-observer-token', 'test-token')

    const response = await http.get('/ping', {
      adapter: async (config: InternalAxiosRequestConfig) => ({
        config,
        data: null,
        headers: {},
        status: 200,
        statusText: 'OK',
      }),
    })

    expect(response.config.headers['X-Auth-Token']).toBe('test-token')
  })
})
