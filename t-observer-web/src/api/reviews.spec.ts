import { afterEach, describe, expect, it, vi } from 'vitest'

import { http } from './http'
import { approveRecord, fetchReviewRecord, rejectRecord } from './reviews'

vi.mock('./http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

const approvedRecord = {
  id: 1,
  taskId: 1,
  observerId: 2,
  teacherName: 'Teacher Zhao',
  strengths: 'Good pacing',
  weaknesses: 'Board writing can be clearer',
  suggestions: 'Add more questioning',
  status: 'APPROVED',
  rejectReason: null,
  submittedAt: '2026-04-20T10:00:00',
  approvedAt: '2026-04-20T11:00:00',
  scores: [],
}

describe('reviews api', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('gets /reviews/:recordId and unwraps the review record detail', async () => {
    vi.mocked(http.get).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          ...approvedRecord,
          status: 'SUBMITTED',
        },
      },
    })

    const result = await fetchReviewRecord(1)

    expect(http.get).toHaveBeenCalledWith('/reviews/1')
    expect(result.teacherName).toBe('Teacher Zhao')
    expect(result.status).toBe('SUBMITTED')
  })

  it('posts approval to /reviews/:recordId/approve and unwraps the record', async () => {
    vi.mocked(http.post).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: approvedRecord,
      },
    })

    const result = await approveRecord(1)

    expect(http.post).toHaveBeenCalledWith('/reviews/1/approve')
    expect(result.status).toBe('APPROVED')
  })

  it('posts rejection reason to /reviews/:recordId/reject and unwraps the record', async () => {
    vi.mocked(http.post).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          ...approvedRecord,
          status: 'RETURNED',
          rejectReason: 'Need more detail',
        },
      },
    })

    const result = await rejectRecord(1, 'Need more detail')

    expect(http.post).toHaveBeenCalledWith('/reviews/1/reject', { reason: 'Need more detail' })
    expect(result.status).toBe('RETURNED')
    expect(result.rejectReason).toBe('Need more detail')
  })
})
