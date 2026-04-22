import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { fetchMembers } from '@/api/auth'
import { fetchTasks } from '@/api/tasks'

import LeaderTaskManageView from './LeaderTaskManageView.vue'

vi.mock('@/api/tasks', () => ({
  fetchTasks: vi.fn(),
  createTask: vi.fn(),
}))

vi.mock('@/api/auth', async () => {
  const actual = await vi.importActual<typeof import('@/api/auth')>('@/api/auth')
  return {
    ...actual,
    fetchMembers: vi.fn(),
  }
})

describe('LeaderTaskManageView', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders the leader task list from API data', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [
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
          recordStatus: 'RETURNED',
          remark: '这是一段超过表格备注列可用宽度的备注内容，用来验证组长列表可以查看完整备注。',
          rejectReason: '请补充课堂互动证据和具体观察片段后重新提交。',
        },
      ],
      total: 12,
      pageNum: 1,
      pageSize: 10,
    })

    const wrapper = mount(LeaderTaskManageView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('高一数学听课')
    })

    expect(fetchTasks).toHaveBeenCalledWith({ pageNum: 1, pageSize: 10 })

    expect(wrapper.text()).toContain('任务管理')
    expect(wrapper.text()).toContain('新建任务')
    expect(wrapper.text()).toContain('赵老师')
    expect(wrapper.text()).toContain('备注')
    expect(wrapper.text()).toContain('未通过')
    expect(wrapper.text()).toContain('待填写')
    expect(wrapper.text()).toContain('共 12 条')
  })

  it('shows the empty state when the leader has no tasks yet', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchMembers).mockResolvedValue([
      {
        userId: 2,
        username: 'member01',
        realName: '李老师',
        roleCode: 'MEMBER',
      },
    ])

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

  it('loads member options and uses member name selection instead of raw id input', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchMembers).mockResolvedValue([
      {
        userId: 2,
        username: 'member01',
        realName: '李老师',
        roleCode: 'MEMBER',
      },
    ])

    const wrapper = mount(LeaderTaskManageView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchMembers).toHaveBeenCalled()
    })

    await wrapper.findAll('button').find((button) => button.text().includes('新建任务'))?.trigger('click')

    expect(wrapper.text()).toContain('听课成员')
    expect(wrapper.text()).not.toContain('听课成员 ID')
  })

  it('opens dialogs for long remarks and returned-task reasons', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [
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
          recordStatus: 'RETURNED',
          remark: '这是一段超过表格备注列可用宽度的备注内容，用来验证组长列表可以查看完整备注。',
          rejectReason: '这是一段超过二十四个字符的退回原因，用来验证点击未通过标签可以查看完整原因。',
        },
      ],
      total: 1,
      pageNum: 1,
      pageSize: 10,
    })

    const wrapper = mount(LeaderTaskManageView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('高一数学听课')
    })

    await wrapper.findAll('button').find((button) => button.text().includes('查看'))?.trigger('click')

    expect(wrapper.text()).toContain('备注详情')
    expect(wrapper.text()).toContain('这是一段超过表格备注列可用宽度的备注内容，用来验证组长列表可以查看完整备注。')

    await wrapper.findAll('button').find((button) => button.text().includes('未通过'))?.trigger('click')

    expect(wrapper.text()).toContain('退回原因详情')
    expect(wrapper.text()).toContain('这是一段超过二十四个字符的退回原因，用来验证点击未通过标签可以查看完整原因。')
  })
})
