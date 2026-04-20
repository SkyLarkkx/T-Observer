import type { ApiEnvelope } from '@/types/auth'
import type { ObservationRecord, RecordDraftPayload, RecordSubmitPayload } from '@/types/record'

import { http } from './http'

export async function saveRecordDraft(payload: RecordDraftPayload) {
  const response = await http.post<ApiEnvelope<ObservationRecord>>('/records/save-draft', payload)
  return response.data.data
}

export async function submitRecord(payload: RecordSubmitPayload) {
  const response = await http.post<ApiEnvelope<ObservationRecord>>('/records/submit', payload)
  return response.data.data
}
