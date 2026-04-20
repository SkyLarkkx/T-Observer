export type DimensionCode =
  | 'TEACHING_DESIGN'
  | 'CLASSROOM_ORGANIZATION'
  | 'TEACHING_CONTENT'
  | 'INTERACTION_FEEDBACK'
  | 'TEACHING_EFFECTIVENESS'

export type ScoreItem = {
  dimensionCode: DimensionCode
  dimensionName: string
  scoreValue: number | null
}

export type RecordDraftPayload = {
  taskId: number
  teacherName: string
  strengths: string
  weaknesses: string
  suggestions: string
  scores: ScoreItem[]
}

export type RecordSubmitPayload = RecordDraftPayload

export type ObservationRecord = {
  id: number
  taskId: number
  observerId: number
  teacherName: string
  strengths: string
  weaknesses: string
  suggestions: string
  status: string
  rejectReason: string | null
  submittedAt: string | null
  approvedAt: string | null
  scores: ScoreItem[]
}

export const DIMENSION_OPTIONS: Array<Pick<ScoreItem, 'dimensionCode' | 'dimensionName'>> = [
  { dimensionCode: 'TEACHING_DESIGN', dimensionName: '教学设计' },
  { dimensionCode: 'CLASSROOM_ORGANIZATION', dimensionName: '课堂组织' },
  { dimensionCode: 'TEACHING_CONTENT', dimensionName: '教学内容' },
  { dimensionCode: 'INTERACTION_FEEDBACK', dimensionName: '互动反馈' },
  { dimensionCode: 'TEACHING_EFFECTIVENESS', dimensionName: '教学效果' },
]

export function createEmptyScores(): ScoreItem[] {
  return DIMENSION_OPTIONS.map((dimension) => ({
    ...dimension,
    scoreValue: null,
  }))
}

export function normalizeScores(scores?: Partial<ScoreItem>[]): ScoreItem[] {
  const scoreByCode = new Map(
    (scores ?? []).map((score) => [score.dimensionCode, score] as const),
  )

  return DIMENSION_OPTIONS.map((dimension) => {
    const existing = scoreByCode.get(dimension.dimensionCode)

    return {
      dimensionCode: dimension.dimensionCode,
      dimensionName: existing?.dimensionName?.trim() || dimension.dimensionName,
      scoreValue: typeof existing?.scoreValue === 'number' ? existing.scoreValue : null,
    }
  })
}
