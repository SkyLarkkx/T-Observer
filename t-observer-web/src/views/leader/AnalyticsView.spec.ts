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

  it('calls the analytics API and renders sample-insufficient result', async () => {
    vi.mocked(generateAnalytics).mockResolvedValue({
      teacherName: 'Teacher Zhao',
      periodValue: '2026-04',
      sampleCount: 2,
      radarChart: null,
      strengthSummary: 'Good pacing',
      weaknessSummary: 'Needs more interaction',
      conclusion: '样本不足，暂不生成雷达图',
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
    await wrapper.find('[data-testid="analytics-period"]').setValue('2026-04')
    await wrapper.find('[data-testid="analytics-submit"]').trigger('click')

    await vi.waitFor(() => {
      expect(generateAnalytics).toHaveBeenCalledWith({
        teacherName: 'Teacher Zhao',
        periodType: 'MONTH',
        periodValue: '2026-04',
      })
    })

    expect(wrapper.text()).toContain('样本不足')
    expect(wrapper.text()).toContain('Good pacing')
  })
})
