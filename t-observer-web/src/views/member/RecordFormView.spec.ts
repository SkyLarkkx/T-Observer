import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

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

    expect(wrapper.text()).toContain('未找到任务信息')
    expect(wrapper.text()).toContain('返回任务列表')
  })

  it('renders task summary from route query and keeps completed tasks read-only', () => {
    routeState.params = { taskId: '7' }
    routeState.query = {
      title: '高一数学听课',
      teacherName: '赵老师',
      courseName: '函数概念',
      lessonTime: '2026-04-20T09:00:00',
      deadline: '2026-04-22T18:00:00',
      remark: '重点观察课堂互动',
      status: 'COMPLETED',
    }

    const wrapper = mount(RecordFormView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('听课记录填写')
    expect(wrapper.text()).toContain('高一数学听课')
    expect(wrapper.text()).toContain('赵老师')
    expect(wrapper.text()).toContain('当前任务已完成，记录页切换为只读展示。')
    expect(wrapper.text()).toContain('已完成')
  })
})
