import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { fetchRecordByTask, saveRecordDraft } from '@/api/records'

import RecordFormView from './RecordFormView.vue'

const pushMock = vi.fn()
const routeState = vi.hoisted(() => ({
  params: {} as Record<string, unknown>,
  query: {} as Record<string, unknown>,
}))

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({
    push: pushMock,
  }),
}))

vi.mock('@/api/records', () => ({
  fetchRecordByTask: vi.fn(),
  saveRecordDraft: vi.fn(),
  submitRecord: vi.fn(),
}))

function buildTaskQuery(status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED') {
  return {
    title: 'Task 7 Lesson Review',
    teacherName: 'Teacher Zhao',
    courseName: 'Functions',
    lessonTime: '2026-04-20T09:00:00',
    deadline: '2026-04-22T18:00:00',
    remark: 'Focus on classroom interaction',
    status,
  }
}

async function waitForTextareas(wrapper: ReturnType<typeof mount>) {
  await vi.waitFor(() => {
    expect(wrapper.findAll('textarea')).toHaveLength(3)
  })
}

describe('RecordFormView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    routeState.params = {}
    routeState.query = {}
  })

  it('shows a fallback state when task context is missing', () => {
    const wrapper = mount(RecordFormView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.find('.el-result').exists()).toBe(true)
    expect(wrapper.text()).toContain('返回任务列表')
  })

  it('renders task summary from route query and keeps completed tasks read-only', async () => {
    routeState.params = { taskId: '7' }
    routeState.query = buildTaskQuery('COMPLETED')
    vi.mocked(fetchRecordByTask).mockResolvedValue(null)

    const wrapper = mount(RecordFormView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchRecordByTask).toHaveBeenCalledWith(7)
    })

    await waitForTextareas(wrapper)

    expect(wrapper.text()).toContain('Task 7 Lesson Review')
    expect(wrapper.text()).toContain('Teacher Zhao')
    expect(wrapper.text()).toContain('当前任务已完成，记录页切换为只读展示。')

    const textareas = wrapper.findAll('textarea')
    expect(textareas).toHaveLength(3)
    expect(textareas.every((textarea) => textarea.element.disabled)).toBe(true)
  })

  it('loads an existing draft record for the current task', async () => {
    routeState.params = { taskId: '7' }
    routeState.query = buildTaskQuery('IN_PROGRESS')
    vi.mocked(fetchRecordByTask).mockResolvedValue({
      id: 11,
      taskId: 7,
      observerId: 2,
      observerName: 'Member Li',
      teacherName: 'Teacher Zhao',
      strengths: 'Existing strengths',
      weaknesses: 'Existing weaknesses',
      suggestions: 'Existing suggestions',
      status: 'DRAFT',
      rejectReason: null,
      submittedAt: null,
      approvedAt: null,
      scores: [],
    })

    const wrapper = mount(RecordFormView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchRecordByTask).toHaveBeenCalledWith(7)
    })

    await waitForTextareas(wrapper)

    const textareas = wrapper.findAll('textarea')
    expect(textareas[0]?.element.value).toBe('Existing strengths')
    expect(textareas[1]?.element.value).toBe('Existing weaknesses')
    expect(textareas[2]?.element.value).toBe('Existing suggestions')
  })

  it('loads submitted content into the read-only view for completed tasks', async () => {
    routeState.params = { taskId: '7' }
    routeState.query = buildTaskQuery('COMPLETED')
    vi.mocked(fetchRecordByTask).mockResolvedValue({
      id: 12,
      taskId: 7,
      observerId: 2,
      observerName: 'Member Li',
      teacherName: 'Teacher Zhao',
      strengths: 'Submitted strengths',
      weaknesses: 'Submitted weaknesses',
      suggestions: 'Submitted suggestions',
      status: 'SUBMITTED',
      rejectReason: null,
      submittedAt: '2026-04-20T10:00:00',
      approvedAt: null,
      scores: [],
    })

    const wrapper = mount(RecordFormView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchRecordByTask).toHaveBeenCalledWith(7)
    })

    await waitForTextareas(wrapper)

    const textareas = wrapper.findAll('textarea')
    expect(textareas[0]?.element.value).toBe('Submitted strengths')
    expect(textareas[1]?.element.value).toBe('Submitted weaknesses')
    expect(textareas[2]?.element.value).toBe('Submitted suggestions')
    expect(textareas.every((textarea) => textarea.element.disabled)).toBe(true)
  })

  it('returns to the task list after saving a draft', async () => {
    routeState.params = { taskId: '7' }
    routeState.query = buildTaskQuery('PENDING')
    vi.mocked(fetchRecordByTask).mockResolvedValue(null)
    vi.mocked(saveRecordDraft).mockResolvedValue({
      id: 11,
      taskId: 7,
      observerId: 2,
      observerName: 'Member Li',
      teacherName: 'Teacher Zhao',
      strengths: 'Draft strengths',
      weaknesses: '',
      suggestions: '',
      status: 'DRAFT',
      rejectReason: null,
      submittedAt: null,
      approvedAt: null,
      scores: [],
    })

    const wrapper = mount(RecordFormView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await vi.waitFor(() => {
      expect(fetchRecordByTask).toHaveBeenCalledWith(7)
    })

    const textareas = wrapper.findAll('textarea')
    await textareas[0]?.setValue('Draft strengths')

    await wrapper
      .findAll('button')
      .find((button) => button.text().includes('保存草稿'))
      ?.trigger('click')

    await vi.waitFor(() => {
      expect(saveRecordDraft).toHaveBeenCalled()
    })

    expect(pushMock).toHaveBeenCalledWith('/tasks')
  })
})
