import type { ApiEnvelope } from '@/types/auth'
import type { TaskCreatePayload, TaskListItem, TaskQueryParams } from '@/types/task'

import { http } from './http'

export async function fetchTasks(params?: TaskQueryParams) {
  const response = await http.get<ApiEnvelope<TaskListItem[]>>('/tasks', { params })
  return response.data.data
}

export async function createTask(payload: TaskCreatePayload) {
  const response = await http.post<ApiEnvelope<TaskListItem>>('/tasks', payload)
  return response.data.data
}
