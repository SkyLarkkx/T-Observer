import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { approveRecord, fetchReviewRecord, rejectRecord } from '@/api/reviews'

import ReviewView from './ReviewView.vue'

const pushMock = vi.fn()
const routeState = vi.hoisted(() => ({
  params: {
    recordId: '1',
  } as Record<string, unknown>,
}))

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({
    push: pushMock,
  }),
}))

vi.mock('@/api/reviews', () => ({
  approveRecord: vi.fn(),
  fetchReviewRecord: vi.fn(),
  rejectRecord: vi.fn(),
}))

const submittedRecord = {
  id: 1,
  taskId: 1,
  observerId: 2,
  observerName: 'Member Li',
  teacherName: 'Teacher Zhao',
  strengths: 'Good pacing',
  weaknesses: 'Board writing can be clearer',
  suggestions: 'Add more questioning',
  status: 'SUBMITTED',
  rejectReason: null,
  submittedAt: '2026-04-20T10:00:00',
  approvedAt: null,
  scores: [
    {
      dimensionCode: 'TEACHING_DESIGN',
      dimensionName: 'Teaching Design',
      scoreValue: 4.5,
    },
  ],
}

describe('ReviewView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    routeState.params = { recordId: '1' }
  })

  it('loads and displays the submitted record detail', async () => {
    vi.mocked(fetchReviewRecord).mockResolvedValue(submittedRecord)

    const wrapper = mount(ReviewView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchReviewRecord).toHaveBeenCalledWith(1)
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('Teacher Zhao')
    })

    expect(wrapper.text()).toContain('Good pacing')
    expect(wrapper.text()).toContain('Member Li')
    expect(wrapper.text()).toContain('记录 ID')
    expect(wrapper.text()).toContain('任务 ID')
    expect(wrapper.text()).toContain('Teaching Design')
    expect(wrapper.text()).toContain('4.5')
  })

  it('approves the record and returns to the review list', async () => {
    vi.mocked(fetchReviewRecord).mockResolvedValue(submittedRecord)
    vi.mocked(approveRecord).mockResolvedValue({
      ...submittedRecord,
      status: 'APPROVED',
      approvedAt: '2026-04-20T11:00:00',
    })

    const wrapper = mount(ReviewView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchReviewRecord).toHaveBeenCalledWith(1)
    })

    await vi.waitFor(() => {
      expect(wrapper.find('[data-testid="review-approve"]').exists()).toBe(true)
    })

    await wrapper.find('[data-testid="review-approve"]').trigger('click')
    await flushPromises()

    expect(approveRecord).toHaveBeenCalledWith(1)
    expect(pushMock).toHaveBeenCalledWith({ name: 'leader-review-list' })
  })

  it('blocks approval when reject reason is not empty', async () => {
    vi.mocked(fetchReviewRecord).mockResolvedValue(submittedRecord)

    const wrapper = mount(ReviewView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.find('[data-testid="review-reason"]').exists()).toBe(true)
    })

    await wrapper.find('[data-testid="review-reason"]').setValue('Need more detail')
    await wrapper.find('[data-testid="review-approve"]').trigger('click')
    await flushPromises()

    expect(approveRecord).not.toHaveBeenCalled()
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('rejects the record with a reason and returns to the review list', async () => {
    vi.mocked(fetchReviewRecord).mockResolvedValue(submittedRecord)
    vi.mocked(rejectRecord).mockResolvedValue({
      ...submittedRecord,
      status: 'RETURNED',
      rejectReason: 'Need more detail',
    })

    const wrapper = mount(ReviewView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchReviewRecord).toHaveBeenCalledWith(1)
    })

    await vi.waitFor(() => {
      expect(wrapper.find('[data-testid="review-reason"]').exists()).toBe(true)
    })

    await wrapper.find('[data-testid="review-reason"]').setValue('Need more detail')
    await wrapper.find('[data-testid="review-reject"]').trigger('click')
    await flushPromises()

    expect(rejectRecord).toHaveBeenCalledWith(1, 'Need more detail')
    expect(pushMock).toHaveBeenCalledWith({ name: 'leader-review-list' })
  })

  it('keeps reviewed records read-only while allowing return', async () => {
    vi.mocked(fetchReviewRecord).mockResolvedValue({
      ...submittedRecord,
      status: 'APPROVED',
      approvedAt: '2026-04-20T11:00:00',
    })

    const wrapper = mount(ReviewView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.find('[data-testid="review-approve"]').exists()).toBe(true)
    })

    expect(wrapper.find('[data-testid="review-reason"]').element.disabled).toBe(true)
    expect(wrapper.find('[data-testid="review-approve"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('[data-testid="review-reject"]').attributes('disabled')).toBeDefined()

    await wrapper.find('[data-testid="review-back"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith({ name: 'leader-review-list' })
  })
})
