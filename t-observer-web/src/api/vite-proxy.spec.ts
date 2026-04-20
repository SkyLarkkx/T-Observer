// @vitest-environment node

import { describe, expect, it } from 'vitest'

import viteConfig from '../../vite.config'

describe('vite proxy config', () => {
  it('proxies /api requests to the backend server', () => {
    expect(viteConfig.server?.proxy?.['/api']).toMatchObject({
      target: 'http://localhost:8080',
      changeOrigin: true,
    })
  })
})
