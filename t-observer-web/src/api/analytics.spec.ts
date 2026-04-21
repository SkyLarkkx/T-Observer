import { afterEach, describe, expect, it, vi } from 'vitest'

import { http } from './http'
import { generateAnalytics } from './analytics'

vi.mock('./http', () => ({
  http: {
    post: vi.fn(),
  },
}))

describe('analytics api', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('posts generation payload to /analytics/generate and unwraps the report', async () => {
    vi.mocked(http.post).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          teacherName: 'Teacher Zhao',
          periodType: 'RANGE',
          periodValue: '2026-04-10T00:00:00 ~ 2026-04-30T23:59:59',
          sampleCount: 3,
          radarChart: {
            indicators: [{ name: 'Teaching Design', max: 5 }],
            values: [4.5],
          },
          strengthSummary: 'Strong design',
          weaknessSummary: 'Needs more interaction',
          conclusion: 'Ready',
        },
      },
    })

    const result = await generateAnalytics({
      teacherName: 'Teacher Zhao',
      startTime: '2026-04-10T00:00:00',
      endTime: '2026-04-30T23:59:59',
    })

    expect(http.post).toHaveBeenCalledWith('/analytics/generate', {
      teacherName: 'Teacher Zhao',
      startTime: '2026-04-10T00:00:00',
      endTime: '2026-04-30T23:59:59',
    })
    expect(result.sampleCount).toBe(3)
    expect(result.radarChart?.values).toEqual([4.5])
  })
})
