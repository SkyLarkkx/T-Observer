import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { fetchTasks } from '@/api/tasks'

import LeaderTaskManageView from './LeaderTaskManageView.vue'

vi.mock('@/api/tasks', () => ({
  fetchTasks: vi.fn(),
  createTask: vi.fn(),
}))

describe('LeaderTaskManageView', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders the leader task list from API data', async () => {
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
    ])

    const wrapper = mount(LeaderTaskManageView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('高一数学听课')
    })

    expect(wrapper.text()).toContain('任务管理')
    expect(wrapper.text()).toContain('新建任务')
    expect(wrapper.text()).toContain('赵老师')
    expect(wrapper.text()).toContain('待填写')
  })

  it('shows the empty state when the leader has no tasks yet', async () => {
    vi.mocked(fetchTasks).mockResolvedValue([])

    const wrapper = mount(LeaderTaskManageView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchTasks).toHaveBeenCalled()
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('可以先创建一条听课任务')
    })
  })
})
