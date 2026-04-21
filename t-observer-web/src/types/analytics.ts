export interface RadarChartData {
  indicators: { name: string; max: number }[]
  values: number[]
}

export interface AnalyticsReport {
  teacherName: string
  periodType: string
  periodValue: string
  sampleCount: number
  radarChart: RadarChartData | null
  strengthSummary: string
  weaknessSummary: string
  conclusion: string
}
