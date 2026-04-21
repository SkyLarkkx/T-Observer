import type { ApiEnvelope } from '@/types/auth'
import type { AnalyticsReport } from '@/types/analytics'

import { http } from './http'

export type AnalyticsGeneratePayload = {
  teacherName: string
  startTime: string
  endTime: string
}

export async function generateAnalytics(payload: AnalyticsGeneratePayload) {
  const response = await http.post<ApiEnvelope<AnalyticsReport>>('/analytics/generate', payload)
  return response.data.data
}
