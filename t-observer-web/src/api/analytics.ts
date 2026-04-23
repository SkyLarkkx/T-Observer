import type { ApiEnvelope } from '@/types/auth'
import type {
  AnalyticsReport,
  AnalyticsTeacherOption,
  SavedAnalyticsReportItem,
} from '@/types/analytics'
import type { PageResult } from '@/types/task'

import { http } from './http'

export type AnalyticsGeneratePayload = {
  teacherName: string
  startTime?: string
  endTime?: string
}

export async function fetchAnalyticsTeachers(keyword?: string) {
  const response = await http.get<ApiEnvelope<AnalyticsTeacherOption[]>>('/analytics/teachers', {
    params: keyword ? { keyword } : undefined,
  })
  return response.data.data
}

export async function generateAnalytics(payload: AnalyticsGeneratePayload) {
  const response = await http.post<ApiEnvelope<AnalyticsReport>>('/analytics/generate', payload)
  return response.data.data
}

export async function saveAnalyticsReport(payload: AnalyticsGeneratePayload) {
  const response = await http.post<ApiEnvelope<AnalyticsReport>>('/analytics/reports', payload)
  return response.data.data
}

export async function fetchSavedAnalyticsReports(pageNum = 1, pageSize = 10) {
  const response = await http.get<ApiEnvelope<PageResult<SavedAnalyticsReportItem>>>('/analytics/reports', {
    params: { pageNum, pageSize },
  })
  return response.data.data
}

export async function fetchSavedAnalyticsReportDetail(reportId: number) {
  const response = await http.get<ApiEnvelope<AnalyticsReport>>(`/analytics/reports/${reportId}`)
  return response.data.data
}
