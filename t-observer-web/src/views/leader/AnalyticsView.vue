<script setup lang="ts">
import type { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'

import { generateAnalytics, type AnalyticsGeneratePayload } from '@/api/analytics'
import RadarChart from '@/components/analytics/RadarChart.vue'
import type { AnalyticsReport } from '@/types/analytics'

const form = reactive<AnalyticsGeneratePayload>({
  teacherName: '',
  startTime: '',
  endTime: '',
})
const report = ref<AnalyticsReport | null>(null)
const loading = ref(false)
const errorMessage = ref('')

function getAxiosMessage(error: unknown, fallback: string) {
  return (
    ((error as AxiosError<{ message?: string }>)?.response?.data?.message ?? '').trim() || fallback
  )
}

function normalizeDateTime(value: string) {
  const trimmed = value.trim()
  if (trimmed.length === 16) {
    return `${trimmed}:00`
  }
  return trimmed
}

async function handleGenerate() {
  const teacherName = form.teacherName.trim()
  const startTime = normalizeDateTime(form.startTime)
  const endTime = normalizeDateTime(form.endTime)

  if (!teacherName || !startTime || !endTime) {
    ElMessage.warning('请填写授课教师和分析时间')
    return
  }

  if (new Date(startTime).getTime() > new Date(endTime).getTime()) {
    ElMessage.warning('分析开始时间不能晚于结束时间')
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    report.value = await generateAnalytics({
      teacherName,
      startTime,
      endTime,
    })
  } catch (error) {
    errorMessage.value = getAxiosMessage(error, '分析结果生成失败，请稍后重试')
    report.value = null
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="analytics-page">
    <header class="analytics-page__header">
      <div>
        <p class="analytics-page__eyebrow">教师分析</p>
        <h1>教师分析结果</h1>
      </div>
    </header>

    <section class="analytics-page__panel">
      <label>
        <span>授课教师</span>
        <input
          v-model="form.teacherName"
          class="analytics-page__input"
          data-testid="analytics-teacher"
          placeholder="例如：赵老师"
        />
      </label>

      <label>
        <span>分析开始时间</span>
        <input
          v-model="form.startTime"
          class="analytics-page__input"
          data-testid="analytics-start-time"
          type="datetime-local"
        />
      </label>

      <label>
        <span>分析结束时间</span>
        <input
          v-model="form.endTime"
          class="analytics-page__input"
          data-testid="analytics-end-time"
          type="datetime-local"
        />
      </label>

      <el-button
        type="primary"
        :loading="loading"
        data-testid="analytics-submit"
        @click="handleGenerate"
      >
        生成分析
      </el-button>
    </section>

    <el-alert
      v-if="errorMessage"
      type="error"
      :title="errorMessage"
      :closable="false"
      show-icon
    />

    <section v-if="report" class="analytics-page__result">
      <div class="analytics-page__summary">
        <div>
          <span>分析样本数</span>
          <strong>{{ report.sampleCount }}</strong>
        </div>
        <div>
          <span>分析时间</span>
          <strong>{{ report.periodValue }}</strong>
        </div>
      </div>

      <p v-if="!report.radarChart" class="analytics-page__empty">{{ report.conclusion }}</p>
      <RadarChart
        v-else
        :indicators="report.radarChart.indicators"
        :values="report.radarChart.values"
      />

      <section class="analytics-page__text-grid">
        <article>
          <h2>优势总结</h2>
          <p>{{ report.strengthSummary || '暂无优势总结' }}</p>
        </article>
        <article>
          <h2>改进重点</h2>
          <p>{{ report.weaknessSummary || '暂无改进重点' }}</p>
        </article>
        <article>
          <h2>结论</h2>
          <p>{{ report.conclusion }}</p>
        </article>
      </section>
    </section>
  </section>
</template>

<style scoped>
.analytics-page {
  display: grid;
  gap: 20px;
}

.analytics-page__header,
.analytics-page__panel,
.analytics-page__result,
.analytics-page__text-grid article {
  padding: 24px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.analytics-page__header h1,
.analytics-page__text-grid h2 {
  margin: 4px 0 0;
  font-size: 18px;
}

.analytics-page__eyebrow {
  margin: 0;
  color: #409eff;
  font-size: 13px;
  font-weight: 700;
}

.analytics-page__panel {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) minmax(190px, 1fr) minmax(190px, 1fr) auto;
  align-items: end;
  gap: 16px;
}

.analytics-page__panel label {
  display: grid;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.analytics-page__input {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  color: #303133;
  font: inherit;
}

.analytics-page__input:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.14);
}

.analytics-page__summary {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 18px;
}

.analytics-page__summary div {
  min-width: 120px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f5f7fa;
}

.analytics-page__summary span {
  display: block;
  color: #909399;
  font-size: 13px;
}

.analytics-page__summary strong {
  display: block;
  margin-top: 6px;
  color: #303133;
  font-size: 18px;
}

.analytics-page__empty {
  margin: 0;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
  color: #606266;
}

.analytics-page__text-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.analytics-page__text-grid p {
  margin: 10px 0 0;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .analytics-page__panel {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}

@media (max-width: 768px) {
  .analytics-page__panel {
    grid-template-columns: 1fr;
  }
}
</style>
