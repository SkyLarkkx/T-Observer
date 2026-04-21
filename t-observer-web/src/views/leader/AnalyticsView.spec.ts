import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { generateAnalytics } from '@/api/analytics'

import AnalyticsView from './AnalyticsView.vue'

vi.mock('@/api/analytics', () => ({
  generateAnalytics: vi.fn(),
}))

describe('AnalyticsView', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('calls the analytics API and renders analysis sample count', async () => {
    vi.mocked(generateAnalytics).mockResolvedValue({
      teacherName: 'Teacher Zhao',
      periodType: 'RANGE',
      periodValue: '2026-04-10T00:00:00 ~ 2026-04-30T23:59:59',
      sampleCount: 1,
      radarChart: {
        indicators: [{ name: 'Teaching Design', max: 5 }],
        values: [4.5],
      },
      strengthSummary: 'Good pacing',
      weaknessSummary: 'Needs more interaction',
      conclusion: '已根据 1 条已通过记录生成分析',
    })

    const wrapper = mount(AnalyticsView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RadarChart: true,
        },
      },
    })

    await wrapper.find('[data-testid="analytics-teacher"]').setValue('Teacher Zhao')
    await wrapper.find('[data-testid="analytics-start-time"]').setValue('2026-04-10T00:00')
    await wrapper.find('[data-testid="analytics-end-time"]').setValue('2026-04-30T23:59')
    await wrapper.find('[data-testid="analytics-submit"]').trigger('click')

    await vi.waitFor(() => {
      expect(generateAnalytics).toHaveBeenCalledWith({
        teacherName: 'Teacher Zhao',
        startTime: '2026-04-10T00:00:00',
        endTime: '2026-04-30T23:59:00',
      })
    })

    expect(wrapper.text()).toContain('分析样本数')
    expect(wrapper.text()).toContain('1')
    expect(wrapper.text()).toContain('已根据 1 条已通过记录生成分析')
    expect(wrapper.text()).toContain('Good pacing')
  })
})
