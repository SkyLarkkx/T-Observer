import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { fetchReviewRecords } from '@/api/reviews'

import ReviewListView from './ReviewListView.vue'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
}))

vi.mock('@/api/reviews', () => ({
  fetchReviewRecords: vi.fn(),
}))

const reviewRows = [
  {
    recordId: 1,
    taskId: 11,
    taskTitle: 'Grade 1 Math Observation',
    observerName: 'Member Li',
    teacherName: 'Teacher Zhao',
    courseName: 'Function Concept',
    lessonTime: '2026-04-20T09:00:00',
    deadline: '2026-04-22T18:00:00',
    recordStatus: 'SUBMITTED',
    submittedAt: '2026-04-20T10:00:00',
  },
  {
    recordId: 2,
    taskId: 12,
    taskTitle: 'Grade 2 Chinese Observation',
    observerName: 'Member Wang',
    teacherName: 'Teacher Qian',
    courseName: 'Writing',
    lessonTime: '2026-04-21T09:00:00',
    deadline: '2026-04-23T18:00:00',
    recordStatus: 'APPROVED',
    submittedAt: '2026-04-21T10:00:00',
  },
  {
    recordId: 3,
    taskId: 13,
    taskTitle: 'Grade 3 English Observation',
    observerName: 'Member Zhao',
    teacherName: 'Teacher Sun',
    courseName: 'Reading',
    lessonTime: '2026-04-22T09:00:00',
    deadline: '2026-04-24T18:00:00',
    recordStatus: 'RETURNED',
    submittedAt: '2026-04-22T10:00:00',
  },
]

describe('ReviewListView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders review rows as a table with status labels and actions', async () => {
    vi.mocked(fetchReviewRecords).mockResolvedValue(reviewRows)

    const wrapper = mount(ReviewListView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchReviewRecords).toHaveBeenCalled()
    })
    await flushPromises()

    expect(wrapper.text()).toContain('任务标题')
    expect(wrapper.text()).toContain('授课教师')
    expect(wrapper.text()).toContain('Grade 1 Math Observation')
    expect(wrapper.text()).toContain('待评审')
    expect(wrapper.text()).toContain('已完成')
    expect(wrapper.text()).toContain('未通过')
    expect(wrapper.text()).toContain('评审')
    expect(wrapper.text()).toContain('查看')
  })

  it('opens the review detail when action button is clicked', async () => {
    vi.mocked(fetchReviewRecords).mockResolvedValue(reviewRows)

    const wrapper = mount(ReviewListView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.find('[data-testid="review-action-1"]').exists()).toBe(true)
    })

    await wrapper.find('[data-testid="review-action-1"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith({
      name: 'leader-review-form',
      params: { recordId: 1 },
    })
  })
})
