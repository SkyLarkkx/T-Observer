export interface RadarChartData {
  indicators: { name: string; max: number }[]
  values: number[]
}

export interface AnalyticsTeacherOption {
  teacherName: string
}

export interface AnalyticsReport {
  id?: number
  teacherName: string
  periodType: string
  periodValue: string
  startTime: string | null
  endTime: string | null
  generatedAt: string | null
  sampleCount: number
  radarChart: RadarChartData | null
  strengthSummary: string
  weaknessSummary: string
  conclusion: string
}

export interface SavedAnalyticsReportItem {
  id: number
  teacherName: string
  sampleCount: number
  startTime: string | null
  endTime: string | null
  generatedAt: string | null
}
