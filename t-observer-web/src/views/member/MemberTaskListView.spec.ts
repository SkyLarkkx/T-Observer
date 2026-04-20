import ElementPlus from 'element-plus'
import { mount, RouterLinkStub } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { fetchTasks } from '@/api/tasks'

import MemberTaskListView from './MemberTaskListView.vue'

vi.mock('@/api/tasks', () => ({
  fetchTasks: vi.fn(),
}))

describe('MemberTaskListView', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders task filters, cards, and action labels from API data', async () => {
    vi.mocked(fetchTasks).mockResolvedValue([
      {
        id: 1,
        title: '高一数学听课',
        observerId: 2,
        observerName: '李老师',
        teacherName: '赵老师',
        courseName: '函数概念',
        lessonTime: '2026-04-20T09:00:00',
        deadline: '2026-04-22T18:00:00',
        status: 'PENDING',
        remark: '重点观察课堂互动',
      },
      {
        id: 2,
        title: '高二英语听课',
        observerId: 2,
        observerName: '李老师',
        teacherName: '王老师',
        courseName: '阅读理解',
        lessonTime: '2026-04-23T10:00:00',
        deadline: '2026-04-25T18:00:00',
        status: 'COMPLETED',
        remark: '',
      },
    ])

    const wrapper = mount(MemberTaskListView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    })

    await vi.waitFor(() => {
      expect(fetchTasks).toHaveBeenCalledWith(undefined)
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('高一数学听课')
    })

    expect(wrapper.text()).toContain('全部')
    expect(wrapper.text()).toContain('待填写')
    expect(wrapper.text()).toContain('进行中')
    expect(wrapper.text()).toContain('已完成')
    expect(wrapper.text()).toContain('高二英语听课')
    expect(wrapper.text()).toContain('去填报')
    expect(wrapper.text()).toContain('查看任务')
  })

  it('shows the empty state when there are no tasks', async () => {
    vi.mocked(fetchTasks).mockResolvedValue([])

    const wrapper = mount(MemberTaskListView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    })

    await vi.waitFor(() => {
      expect(fetchTasks).toHaveBeenCalled()
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('暂无听课任务')
    })
  })
})
