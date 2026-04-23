export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
export type TaskRecordStatus = 'DRAFT' | 'SUBMITTED' | 'RETURNED' | 'APPROVED'

export type TaskListItem = {
  id: number
  title: string
  observerId: number
  observerName: string
  teacherName: string
  courseName: string
  lessonTime: string
  deadline: string
  status: TaskStatus
  remark: string | null
  recordStatus: TaskRecordStatus | null
  rejectReason: string | null
}

export type TaskQueryParams = {
  status?: TaskStatus
  pageNum?: number
  pageSize?: number
}

export type PageResult<T> = {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export type TaskCreatePayload = {
  title: string
  observerId: number
  teacherName: string
  courseName: string
  lessonTime: string
  deadline: string
  remark?: string
}

export const TASK_STATUS_LABELS: Record<TaskStatus, string> = {
  PENDING: '待填写',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
}

export const TASK_STATUS_FILTERS: Array<{ label: string; value: TaskStatus | 'ALL' }> = [
  { label: '全部', value: 'ALL' },
  { label: TASK_STATUS_LABELS.PENDING, value: 'PENDING' },
  { label: TASK_STATUS_LABELS.IN_PROGRESS, value: 'IN_PROGRESS' },
  { label: TASK_STATUS_LABELS.COMPLETED, value: 'COMPLETED' },
]
