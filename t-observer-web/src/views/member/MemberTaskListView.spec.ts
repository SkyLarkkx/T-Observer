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
          remark: '重点观察课堂互动，关注学生真实回应与追问质量',
          rejectReason: '退回原因需要成员补充课堂互动证据和具体观察片段',
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
          recordStatus: 'APPROVED',
          remark: '',
          rejectReason: null,
        },
      ],
      total: 8,
      pageNum: 1,
      pageSize: 6,
    })

    const wrapper = mount(MemberTaskListView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    })

    await vi.waitFor(() => {
      expect(fetchTasks).toHaveBeenCalledWith({ pageNum: 1, pageSize: 6 })
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('高一数学听课')
    })

    expect(wrapper.text()).toContain('全部')
    expect(wrapper.text()).toContain('待填写')
    expect(wrapper.text()).toContain('进行中')
    expect(wrapper.text()).toContain('已完成')
    expect(wrapper.text()).toContain('高二英语听课')
    expect(wrapper.text()).toContain('未通过')
    expect(wrapper.text()).toContain('备注')
    expect(wrapper.text()).toContain('退回原因')
    expect(wrapper.text()).toContain('去填报')
    expect(wrapper.text()).toContain('查看任务')
    expect(wrapper.text()).toContain('共 8 条')
  })

  it('shows the empty state when there are no tasks', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 6,
    })

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

  it('opens a detail dialog for long remarks and reject reasons', async () => {
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
          remark: '这是一段超过二十四个字符的备注内容，用来验证成员列表可以查看完整备注。',
          rejectReason: '这是一段超过二十四个字符的退回原因，用来验证成员无需进入表单也能查看完整原因。',
        },
      ],
      total: 1,
      pageNum: 1,
      pageSize: 6,
    })

    const wrapper = mount(MemberTaskListView, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    })

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('高一数学听课')
    })

    const viewButtons = wrapper.findAll('button').filter((button) => button.text().includes('查看'))
    expect(viewButtons.length).toBeGreaterThanOrEqual(2)

    await viewButtons[1]?.trigger('click')

    expect(wrapper.text()).toContain('退回原因详情')
    expect(wrapper.text()).toContain('这是一段超过二十四个字符的退回原因，用来验证成员无需进入表单也能查看完整原因。')
  })
})
