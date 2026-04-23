import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { fetchMembers } from '@/api/auth'
import { createTask, fetchTasks } from '@/api/tasks'

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

const memberOption = {
  userId: 2,
  username: 'member01',
  realName: 'Member Li',
  roleCode: 'MEMBER',
} as const

function mountView() {
  return mount(LeaderTaskManageView, {
    global: {
      plugins: [ElementPlus],
    },
  })
}

describe('LeaderTaskManageView', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('loads the first page of leader tasks', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [
        {
          id: 1,
          title: 'Task One',
          observerId: 2,
          observerName: 'Member Li',
          teacherName: 'Teacher Zhao',
          courseName: 'Function Concepts',
          lessonTime: '2026-04-20T09:00:00',
          deadline: '2026-04-22T18:00:00',
          status: 'PENDING',
          recordStatus: 'RETURNED',
          remark: 'Long remark content for leader dialog preview.',
          rejectReason: 'Long reject reason content for returned task preview.',
        },
      ],
      total: 12,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchMembers).mockResolvedValue([memberOption])

    const wrapper = mountView()

    await vi.waitFor(() => {
      expect(fetchTasks).toHaveBeenCalledWith({ pageNum: 1, pageSize: 10 })
    })

    const vm = wrapper.vm as unknown as {
      tasks: Array<{ title: string; observerName: string; teacherName: string; courseName: string }>
      total: number
      pageNum: number
    }

    expect(vm.tasks).toHaveLength(1)
    expect(vm.tasks[0]?.title).toBe('Task One')
    expect(vm.tasks[0]?.observerName).toBe('Member Li')
    expect(vm.tasks[0]?.teacherName).toBe('Teacher Zhao')
    expect(vm.tasks[0]?.courseName).toBe('Function Concepts')
    expect(vm.total).toBe(12)
    expect(vm.pageNum).toBe(1)
  })

  it('keeps the empty-state data when the task list is empty', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchMembers).mockResolvedValue([memberOption])

    const wrapper = mountView()

    await vi.waitFor(() => {
      expect(fetchTasks).toHaveBeenCalled()
    })

    const vm = wrapper.vm as unknown as { tasks: unknown[]; total: number; emptyDescription: string }
    expect(vm.tasks).toHaveLength(0)
    expect(vm.total).toBe(0)
    expect(vm.emptyDescription).toContain('\u521b\u5efa')
  })

  it('loads members and wires footer-aware datetime picker props', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchMembers).mockResolvedValue([memberOption])

    const wrapper = mountView()

    await vi.waitFor(() => {
      expect(fetchMembers).toHaveBeenCalled()
    })

    await wrapper
      .findAll('button')
      .find((button) => button.text().includes('\u65b0\u5efa\u4efb\u52a1'))
      ?.trigger('click')
    await flushPromises()

    const datePickers = wrapper.findAllComponents({ name: 'ElDatePicker' })
    expect(datePickers).toHaveLength(2)
    expect(datePickers[0]?.props('showNow')).toBe(false)
    expect(datePickers[0]?.props('valueFormat')).toBe('YYYY-MM-DDTHH:mm')
    expect(datePickers[0]?.props('popperClass')).toBe('task-lesson-time-picker-popper')
    expect(datePickers[1]?.props('popperClass')).toBe('task-deadline-picker-popper')
  })

  it('opens detail dialog content through the shared helper', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchMembers).mockResolvedValue([memberOption])

    const wrapper = mountView()
    const vm = wrapper.vm as unknown as {
      openTextDetail: (title: string, value: string | null) => void
      detailDialogOpen: boolean
      detailTitle: string
      detailContent: string
    }

    vm.openTextDetail('\u5907\u6ce8', 'Long remark content for leader dialog preview.')
    await flushPromises()

    expect(vm.detailDialogOpen).toBe(true)
    expect(vm.detailTitle).toContain('\u5907\u6ce8')
    expect(vm.detailContent).toBe('Long remark content for leader dialog preview.')
  })

  it('jumps to the requested page on enter', async () => {
    vi.mocked(fetchTasks)
      .mockResolvedValueOnce({
        list: [],
        total: 25,
        pageNum: 1,
        pageSize: 10,
      })
      .mockResolvedValueOnce({
        list: [],
        total: 25,
        pageNum: 3,
        pageSize: 10,
      })
    vi.mocked(fetchMembers).mockResolvedValue([memberOption])

    const wrapper = mountView()

    await vi.waitFor(() => {
      expect(fetchTasks).toHaveBeenCalledTimes(1)
    })

    const vm = wrapper.vm as unknown as {
      pageJumpValue: string
      handlePageJump: () => Promise<void>
    }
    vm.pageJumpValue = '3'
    await vm.handlePageJump()
    await flushPromises()

    expect(fetchTasks).toHaveBeenLastCalledWith({ pageNum: 3, pageSize: 10 })
  })

  it('submits task creation with minute-level values', async () => {
    vi.mocked(fetchTasks).mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    })
    vi.mocked(fetchMembers).mockResolvedValue([memberOption])
    vi.mocked(createTask).mockResolvedValue()

    const wrapper = mountView()

    await vi.waitFor(() => {
      expect(fetchMembers).toHaveBeenCalled()
    })

    const vm = wrapper.vm as unknown as {
      form: {
        title: string
        observerId: number
        teacherName: string
        courseName: string
        lessonTime: string
        deadline: string
        remark: string
      }
      formRef: { validate: () => Promise<void> }
      handleCreateTask: () => Promise<void>
    }

    vm.form.title = 'Task One'
    vm.form.observerId = 2
    vm.form.teacherName = 'Teacher Zhao'
    vm.form.courseName = 'Function Concepts'
    vm.form.lessonTime = '2026-04-20T09:00'
    vm.form.deadline = '2026-04-22T18:00'
    vm.form.remark = 'Remark text'
    vm.formRef = {
      validate: vi.fn().mockResolvedValue(undefined),
    }

    await vm.handleCreateTask()
    await flushPromises()

    expect(createTask).toHaveBeenCalledWith({
      title: 'Task One',
      observerId: 2,
      teacherName: 'Teacher Zhao',
      courseName: 'Function Concepts',
      lessonTime: '2026-04-20T09:00',
      deadline: '2026-04-22T18:00',
      remark: 'Remark text',
    })
  })
})
