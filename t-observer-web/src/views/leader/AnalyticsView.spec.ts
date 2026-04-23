import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  fetchAnalyticsTeachers,
  fetchSavedAnalyticsReportDetail,
  fetchSavedAnalyticsReports,
  generateAnalytics,
  saveAnalyticsReport,
} from '@/api/analytics'

import AnalyticsView from './AnalyticsView.vue'

vi.mock('@/api/analytics', () => ({
  fetchAnalyticsTeachers: vi.fn(),
  fetchSavedAnalyticsReportDetail: vi.fn(),
  fetchSavedAnalyticsReports: vi.fn(),
  generateAnalytics: vi.fn(),
  saveAnalyticsReport: vi.fn(),
}))

const generatedReport = {
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
  strengthSummary: 'Good pacing',
  weaknessSummary: 'Needs more interaction',
  conclusion: '已根据 3 条已通过记录生成分析',
}

describe('AnalyticsView', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('generates analytics with optional times and saves the generated report', async () => {
    vi.mocked(fetchAnalyticsTeachers).mockResolvedValue([{ teacherName: 'Teacher Zhao' }])
    vi.mocked(fetchSavedAnalyticsReports).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(generateAnalytics).mockResolvedValue(generatedReport)
    vi.mocked(saveAnalyticsReport).mockResolvedValue({
      ...generatedReport,
      id: 1,
      generatedAt: '2026-04-23T22:00:00',
    })

    const wrapper = mount(AnalyticsView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RadarChart: true,
        },
      },
    })

    await vi.waitFor(() => {
      expect(fetchAnalyticsTeachers).toHaveBeenCalled()
    })

    const generateButton = wrapper.find('[data-testid="analytics-submit"]')
    expect(generateButton.attributes('disabled')).toBeDefined()

    wrapper.findComponent({ name: 'ElSelect' }).vm.$emit('update:modelValue', 'Teacher Zhao')
    await flushPromises()

    expect(wrapper.find('[data-testid="analytics-submit"]').attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-testid="analytics-submit"]').trigger('click')
    await flushPromises()

    expect(generateAnalytics).toHaveBeenCalledWith({
      teacherName: 'Teacher Zhao',
    })
    expect(wrapper.text()).toContain('分析结果')
    expect(wrapper.text()).toContain('分析样本数')
    expect(wrapper.text()).toContain('3')

    await wrapper.find('[data-testid="analytics-save"]').trigger('click')
    await flushPromises()

    expect(saveAnalyticsReport).toHaveBeenCalledWith({
      teacherName: 'Teacher Zhao',
    })
    expect(fetchSavedAnalyticsReports).toHaveBeenLastCalledWith(1, 10)
    expect(wrapper.text()).toContain('保存报告列表')
  })

  it('views a saved report in readonly mode', async () => {
    vi.mocked(fetchAnalyticsTeachers).mockResolvedValue([{ teacherName: 'Teacher Zhao' }])
    vi.mocked(fetchSavedAnalyticsReports).mockResolvedValue({
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
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchSavedAnalyticsReportDetail).mockResolvedValue({
      ...generatedReport,
      id: 1,
      generatedAt: '2026-04-23T22:00:00',
    })

    const wrapper = mount(AnalyticsView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RadarChart: true,
        },
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('保存报告列表')
    })

    await vi.waitFor(() => {
      expect(wrapper.find('[data-testid="analytics-view-1"]').exists()).toBe(true)
    })

    await wrapper.find('[data-testid="analytics-view-1"]').trigger('click')
    await flushPromises()

    expect(fetchSavedAnalyticsReportDetail).toHaveBeenCalledWith(1)
    expect(wrapper.text()).toContain('Teacher Zhao')
    expect(wrapper.find('[data-testid="analytics-save"]').attributes('disabled')).toBeDefined()

    await wrapper.find('[data-testid="analytics-back"]').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('保存报告列表')
  })
})
