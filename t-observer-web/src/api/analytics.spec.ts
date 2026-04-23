import { afterEach, describe, expect, it, vi } from 'vitest'

import { http } from './http'
import {
  fetchAnalyticsTeachers,
  fetchSavedAnalyticsReportDetail,
  fetchSavedAnalyticsReports,
  generateAnalytics,
  saveAnalyticsReport,
} from './analytics'

vi.mock('./http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('analytics api', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('loads analytics teacher options', async () => {
    vi.mocked(http.get).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: [{ teacherName: 'Teacher Zhao' }],
      },
    })

    const result = await fetchAnalyticsTeachers('Zhao')

    expect(http.get).toHaveBeenCalledWith('/analytics/teachers', {
      params: { keyword: 'Zhao' },
    })
    expect(result[0]?.teacherName).toBe('Teacher Zhao')
  })

  it('posts generation payload to /analytics/generate and unwraps the report', async () => {
    vi.mocked(http.post).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          teacherName: 'Teacher Zhao',
          periodType: 'RANGE',
          periodValue: '全部已通过记录',
          startTime: null,
          endTime: null,
          generatedAt: null,
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
    })

    expect(http.post).toHaveBeenCalledWith('/analytics/generate', {
      teacherName: 'Teacher Zhao',
    })
    expect(result.sampleCount).toBe(3)
    expect(result.radarChart?.values).toEqual([4.5])
  })

  it('posts save payload to /analytics/reports', async () => {
    vi.mocked(http.post).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          teacherName: 'Teacher Zhao',
          periodType: 'RANGE',
          periodValue: '全部已通过记录',
          startTime: null,
          endTime: null,
          generatedAt: '2026-04-23T22:00:00',
          sampleCount: 3,
          radarChart: null,
          strengthSummary: '',
          weaknessSummary: '',
          conclusion: 'Ready',
        },
      },
    })

    const result = await saveAnalyticsReport({
      teacherName: 'Teacher Zhao',
      startTime: '2026-04-10T00:00',
    })

    expect(http.post).toHaveBeenCalledWith('/analytics/reports', {
      teacherName: 'Teacher Zhao',
      startTime: '2026-04-10T00:00',
    })
    expect(result.id).toBe(1)
  })

  it('loads saved reports with pagination', async () => {
    vi.mocked(http.get).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          list: [
            {
              id: 1,
              teacherName: 'Teacher Zhao',
              sampleCount: 3,
              startTime: null,
              endTime: null,
              generatedAt: '2026-04-23T22:00:00',
            },
          ],
          total: 1,
          pageNum: 2,
          pageSize: 5,
        },
      },
    })

    const result = await fetchSavedAnalyticsReports(2, 5)

    expect(http.get).toHaveBeenCalledWith('/analytics/reports', {
      params: { pageNum: 2, pageSize: 5 },
    })
    expect(result.list[0]?.teacherName).toBe('Teacher Zhao')
    expect(result.pageNum).toBe(2)
  })

  it('loads saved report detail by id', async () => {
    vi.mocked(http.get).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          teacherName: 'Teacher Zhao',
          periodType: 'RANGE',
          periodValue: '全部已通过记录',
          startTime: null,
          endTime: null,
          generatedAt: '2026-04-23T22:00:00',
          sampleCount: 3,
          radarChart: null,
          strengthSummary: '',
          weaknessSummary: '',
          conclusion: 'Ready',
        },
      },
    })

    const result = await fetchSavedAnalyticsReportDetail(1)

    expect(http.get).toHaveBeenCalledWith('/analytics/reports/1')
    expect(result.id).toBe(1)
  })
})
