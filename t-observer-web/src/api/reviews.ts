import type { ApiEnvelope } from '@/types/auth'
import type { ObservationRecord, ReviewListItem } from '@/types/record'

import { http } from './http'

export async function fetchReviewRecord(recordId: number) {
  const response = await http.get<ApiEnvelope<ObservationRecord>>(`/reviews/${recordId}`)
  return response.data.data
}

export async function fetchReviewRecords() {
  const response = await http.get<ApiEnvelope<ReviewListItem[]>>('/reviews')
  return response.data.data
}

export async function approveRecord(recordId: number) {
  const response = await http.post<ApiEnvelope<ObservationRecord>>(`/reviews/${recordId}/approve`)
  return response.data.data
}

export async function rejectRecord(recordId: number, reason: string) {
  const response = await http.post<ApiEnvelope<ObservationRecord>>(`/reviews/${recordId}/reject`, { reason })
  return response.data.data
}
