<script setup lang="ts">
import type { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'

import {
  fetchAnalyticsTeachers,
  fetchSavedAnalyticsReportDetail,
  fetchSavedAnalyticsReports,
  generateAnalytics,
  saveAnalyticsReport,
  type AnalyticsGeneratePayload,
} from '@/api/analytics'
import RadarChart from '@/components/analytics/RadarChart.vue'
import type {
  AnalyticsReport,
  AnalyticsTeacherOption,
  SavedAnalyticsReportItem,
} from '@/types/analytics'
import { formatDateTimeToMinute } from '@/utils/datetime'

const PAGE_SIZE = 10
const ANALYTICS_START_PICKER_POPPER_CLASS = 'analytics-start-picker-popper'
const ANALYTICS_END_PICKER_POPPER_CLASS = 'analytics-end-picker-popper'

const form = reactive<AnalyticsGeneratePayload>({
  teacherName: '',
  startTime: '',
  endTime: '',
})
const teacherOptions = ref<AnalyticsTeacherOption[]>([])
const savedReports = ref<SavedAnalyticsReportItem[]>([])
const currentReport = ref<AnalyticsReport | null>(null)
const loading = ref(false)
const saving = ref(false)
const detailLoading = ref(false)
const teacherLoading = ref(false)
const listLoading = ref(false)
const errorMessage = ref('')
const reportMode = ref<'list' | 'generated' | 'saved'>('list')
const pageNum = ref(1)
const total = ref(0)
const pageJumpValue = ref('')

const canGenerate = computed(() => form.teacherName.trim().length > 0)
const maxPage = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))
const saveDisabled = computed(() => reportMode.value === 'saved' || saving.value || !currentReport.value)

function getAxiosMessage(error: unknown, fallback: string) {
  return (
    ((error as AxiosError<{ message?: string }>)?.response?.data?.message ?? '').trim() || fallback
  )
}

function buildPayload() {
  const payload: AnalyticsGeneratePayload = {
    teacherName: form.teacherName.trim(),
  }

  if (form.startTime?.trim()) {
    payload.startTime = form.startTime.trim()
  }
  if (form.endTime?.trim()) {
    payload.endTime = form.endTime.trim()
  }

  return payload
}

function formatBoundary(value: string | null) {
  return formatDateTimeToMinute(value)
}

function createTimeShortcuts(field: 'startTime' | 'endTime') {
  return [
    {
      text: '清除',
      onClick: () => {
        form[field] = ''
      },
    },
    {
      text: '现在',
      value: () => new Date(),
    },
  ]
}

async function loadTeacherOptions(keyword = '') {
  teacherLoading.value = true

  try {
    teacherOptions.value = await fetchAnalyticsTeachers(keyword.trim() || undefined)
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '授课教师列表加载失败，请稍后重试'))
    teacherOptions.value = []
  } finally {
    teacherLoading.value = false
  }
}

function findVisiblePickerRoot(popperClass: string) {
  const roots = Array.from(document.querySelectorAll(`.${popperClass}`)).filter(
    (element): element is HTMLElement => element instanceof HTMLElement,
  )

  for (let index = roots.length - 1; index >= 0; index -= 1) {
    const root = roots[index]
    if (!root) {
      continue
    }
    const style = window.getComputedStyle(root)
    if (style.display !== 'none' && style.visibility !== 'hidden') {
      return root
    }
  }

  return roots.at(-1) ?? null
}

function formatPickerDateTimeValue(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function mountPickerFooterActions(
  popperClass: string,
  actionClass: string,
  actions: Array<{ label: string; onClick: () => void }>,
  attempt = 0,
) {
  nextTick(() => {
    const pickerRoot = findVisiblePickerRoot(popperClass)
    const footer = pickerRoot?.querySelector('.el-picker-panel__footer')
    if (!(footer instanceof HTMLElement)) {
      if (attempt < 10) {
        window.setTimeout(() => {
          mountPickerFooterActions(popperClass, actionClass, actions, attempt + 1)
        }, 16)
      }
      return
    }

    pickerRoot?.querySelector('.el-picker-panel__sidebar')?.remove()
    footer.querySelector(`.${actionClass}`)?.remove()

    const container = document.createElement('div')
    container.className = actionClass

    for (const action of actions) {
      const button = document.createElement('button')
      button.type = 'button'
      button.className = `${actionClass}__button`
      button.textContent = action.label
      button.addEventListener('click', action.onClick)
      container.appendChild(button)
    }

    const confirmButton = footer.querySelector('.el-picker-panel__btn, .el-picker-panel__link-btn')
    if (confirmButton) {
      footer.insertBefore(container, confirmButton)
      return
    }

    footer.appendChild(container)
  })
}

function handleAnalyticsPickerVisibleChange(field: 'startTime' | 'endTime', visible: boolean) {
  if (!visible) {
    return
  }

  mountPickerFooterActions(
    field === 'startTime' ? ANALYTICS_START_PICKER_POPPER_CLASS : ANALYTICS_END_PICKER_POPPER_CLASS,
    `analytics-picker-footer-actions--${field}`,
    [
      {
        label: '\u6e05\u9664',
        onClick: () => {
          form[field] = ''
        },
      },
      {
        label: '\u73b0\u5728',
        onClick: () => {
          form[field] = formatPickerDateTimeValue(new Date())
        },
      },
    ],
  )
}

async function loadSavedReports(targetPage = pageNum.value) {
  listLoading.value = true

  try {
    const result = await fetchSavedAnalyticsReports(targetPage, PAGE_SIZE)
    savedReports.value = result.list
    total.value = result.total
    pageNum.value = result.pageNum
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '保存报告列表加载失败，请稍后重试'))
    savedReports.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

async function handleTeacherSearch(keyword: string) {
  await loadTeacherOptions(keyword)
}

async function handleGenerate() {
  if (!canGenerate.value) {
    return
  }

  const payload = buildPayload()
  if (
    payload.startTime &&
    payload.endTime &&
    new Date(payload.startTime).getTime() > new Date(payload.endTime).getTime()
  ) {
    ElMessage.warning('分析开始时间不能晚于结束时间')
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    currentReport.value = await generateAnalytics(payload)
    reportMode.value = 'generated'
  } catch (error) {
    errorMessage.value = getAxiosMessage(error, '分析结果生成失败，请稍后重试')
    currentReport.value = null
    reportMode.value = 'list'
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!currentReport.value || saveDisabled.value) {
    return
  }

  saving.value = true

  try {
    await saveAnalyticsReport(buildPayload())
    ElMessage.success('分析报告已保存')
    currentReport.value = null
    reportMode.value = 'list'
    pageNum.value = 1
    await loadSavedReports(1)
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '分析报告保存失败，请稍后重试'))
  } finally {
    saving.value = false
  }
}

function handleBack() {
  currentReport.value = null
  reportMode.value = 'list'
}

async function handleViewReport(row: SavedAnalyticsReportItem) {
  detailLoading.value = true
  errorMessage.value = ''

  try {
    currentReport.value = await fetchSavedAnalyticsReportDetail(row.id)
    reportMode.value = 'saved'
  } catch (error) {
    errorMessage.value = getAxiosMessage(error, '保存报告加载失败，请稍后重试')
    currentReport.value = null
    reportMode.value = 'list'
  } finally {
    detailLoading.value = false
  }
}

async function handlePageChange(nextPage: number) {
  await loadSavedReports(nextPage)
}

async function handlePageJump() {
  const targetPage = Number(pageJumpValue.value)
  if (!Number.isFinite(targetPage) || targetPage < 1) {
    return
  }

  const normalizedPage = Math.min(Math.floor(targetPage), maxPage.value)
  pageJumpValue.value = String(normalizedPage)
  await loadSavedReports(normalizedPage)
}

onMounted(() => {
  void loadTeacherOptions()
  void loadSavedReports()
})
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
      <div class="analytics-page__query-fields">
        <label class="analytics-page__field analytics-page__field--teacher">
        <span>授课教师</span>
        <el-select
          v-model="form.teacherName"
          data-testid="analytics-teacher"
          filterable
          remote
          clearable
          reserve-keyword
          placeholder="请选择授课教师"
          :remote-method="handleTeacherSearch"
          :loading="teacherLoading"
        >
          <el-option
            v-for="teacher in teacherOptions"
            :key="teacher.teacherName"
            :label="teacher.teacherName"
            :value="teacher.teacherName"
          />
        </el-select>
        </label>

        <label class="analytics-page__field analytics-page__field--start">
        <span>分析开始时间</span>
        <el-date-picker
          v-model="form.startTime"
          data-testid="analytics-start-time"
          type="datetime"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm"
          clearable
          :show-now="false"
          :popper-class="ANALYTICS_START_PICKER_POPPER_CLASS"
          @visible-change="handleAnalyticsPickerVisibleChange('startTime', $event)"
          placeholder="请选择分析开始时间"
        />
        </label>

        <label class="analytics-page__field analytics-page__field--end">
        <span>分析结束时间</span>
        <el-date-picker
          v-model="form.endTime"
          data-testid="analytics-end-time"
          type="datetime"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm"
          clearable
          :show-now="false"
          :popper-class="ANALYTICS_END_PICKER_POPPER_CLASS"
          @visible-change="handleAnalyticsPickerVisibleChange('endTime', $event)"
          placeholder="请选择分析结束时间"
        />
        </label>

      </div>

      <el-button
        class="analytics-page__action"
        type="primary"
        :disabled="!canGenerate"
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

    <section v-if="reportMode !== 'list' && currentReport" class="analytics-page__result">
      <header class="analytics-page__result-header">
        <div>
          <p class="analytics-page__eyebrow">分析结果</p>
          <h2>{{ currentReport.teacherName }}</h2>
        </div>
        <div class="analytics-page__result-actions">
          <el-button data-testid="analytics-back" @click="handleBack">返回</el-button>
          <el-button
            type="primary"
            data-testid="analytics-save"
            :disabled="saveDisabled"
            :loading="saving"
            @click="handleSave"
          >
            保存
          </el-button>
        </div>
      </header>

      <el-skeleton v-if="detailLoading" animated />

      <template v-else>
        <div class="analytics-page__summary">
          <div>
            <span>分析样本数</span>
            <strong>{{ currentReport.sampleCount }}</strong>
          </div>
          <div>
            <span>分析开始时间</span>
            <strong>{{ formatBoundary(currentReport.startTime) }}</strong>
          </div>
          <div>
            <span>分析结束时间</span>
            <strong>{{ formatBoundary(currentReport.endTime) }}</strong>
          </div>
          <div>
            <span>生成时间</span>
            <strong>{{ formatBoundary(currentReport.generatedAt) }}</strong>
          </div>
        </div>

        <p class="analytics-page__period">{{ currentReport.periodValue }}</p>

        <p v-if="!currentReport.radarChart" class="analytics-page__empty">
          {{ currentReport.conclusion }}
        </p>
        <RadarChart
          v-else
          :indicators="currentReport.radarChart.indicators"
          :values="currentReport.radarChart.values"
        />

        <section class="analytics-page__text-grid">
          <article>
            <h2>优势总结</h2>
            <p>{{ currentReport.strengthSummary || '暂无优势总结' }}</p>
          </article>
          <article>
            <h2>改进重点</h2>
            <p>{{ currentReport.weaknessSummary || '暂无改进重点' }}</p>
          </article>
          <article>
            <h2>结论</h2>
            <p>{{ currentReport.conclusion }}</p>
          </article>
        </section>
      </template>
    </section>

    <section v-else class="analytics-page__saved">
      <header class="analytics-page__saved-header">
        <div>
          <p class="analytics-page__eyebrow">报告列表</p>
          <h2>保存报告列表</h2>
        </div>
      </header>

      <el-table
        v-loading="listLoading"
        :data="savedReports"
        class="analytics-page__table"
        empty-text="暂无已保存分析报告"
        row-key="id"
      >
        <el-table-column prop="teacherName" label="授课教师" min-width="140" />
        <el-table-column prop="sampleCount" label="分析样本数" width="120" />
        <el-table-column label="分析开始时间" min-width="160">
          <template #default="{ row }">
            {{ formatBoundary(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="分析结束时间" min-width="160">
          <template #default="{ row }">
            {{ formatBoundary(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="生成时间" min-width="160">
          <template #default="{ row }">
            {{ formatBoundary(row.generatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作按钮" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :data-testid="`analytics-view-${row.id}`"
              @click="handleViewReport(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <section v-if="total > 0" class="analytics-page__pagination">
        <span>共 {{ total }} 条</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="pageNum"
          :page-size="PAGE_SIZE"
          :total="total"
          @current-change="handlePageChange"
        />
        <label class="analytics-page__jump">
          <span>跳至</span>
          <input
            v-model="pageJumpValue"
            data-testid="analytics-page-jump-input"
            inputmode="numeric"
            min="1"
            type="number"
            @keydown.enter="handlePageJump"
          />
          <span>页</span>
          <button type="button" @click="handlePageJump">跳转</button>
        </label>
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
.analytics-page__saved,
.analytics-page__text-grid article {
  padding: 24px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.analytics-page__header h1,
.analytics-page__result-header h2,
.analytics-page__saved-header h2,
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
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.analytics-page__query-fields {
  display: grid;
  grid-template-columns: repeat(3, minmax(220px, 280px));
  align-items: end;
  gap: 16px;
  flex: 1 1 auto;
}

.analytics-page__panel label {
  display: grid;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.analytics-page__field {
  min-width: 0;
}

.analytics-page__action {
  flex: 0 0 auto;
}

.analytics-page__result-header,
.analytics-page__saved-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.analytics-page__result-actions {
  display: flex;
  gap: 12px;
}

.analytics-page__summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
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
  font-size: 16px;
}

.analytics-page__period {
  margin: 0 0 18px;
  color: #606266;
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

.analytics-page__table {
  margin-top: 16px;
}

.analytics-page__pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 18px;
  color: #606266;
  font-size: 14px;
}

.analytics-page__jump {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.analytics-page__jump input {
  width: 64px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  color: #303133;
  font: inherit;
  text-align: center;
}

.analytics-page__jump input:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.14);
}

.analytics-page__jump button {
  height: 32px;
  padding: 0 12px;
  border: none;
  border-radius: 8px;
  background: #409eff;
  color: #fff;
  cursor: pointer;
  font: inherit;
}

@media (max-width: 1200px) {
  .analytics-page__panel {
    flex-direction: column;
    align-items: stretch;
  }

  .analytics-page__query-fields {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 768px) {
  .analytics-page__query-fields {
    grid-template-columns: 1fr;
  }

  .analytics-page__result-header,
  .analytics-page__saved-header {
    flex-direction: column;
  }

  .analytics-page__pagination {
    flex-direction: column;
  }
}
</style>

<style>
.analytics-start-picker-popper .el-picker-panel__sidebar,
.analytics-end-picker-popper .el-picker-panel__sidebar {
  display: none !important;
}

.analytics-start-picker-popper .el-picker-panel__footer,
.analytics-end-picker-popper .el-picker-panel__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-height: 48px;
  padding: 8px 12px;
}

.analytics-start-picker-popper .el-picker-panel__btn,
.analytics-end-picker-popper .el-picker-panel__btn {
  box-sizing: border-box;
  min-width: 72px;
  height: 32px;
  margin: 0;
  padding: 0 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 30px;
}

.analytics-picker-footer-actions--startTime,
.analytics-picker-footer-actions--endTime {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 8px;
}

.analytics-picker-footer-actions--startTime__button,
.analytics-picker-footer-actions--endTime__button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #409eff;
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  line-height: 20px;
  white-space: nowrap;
}

.analytics-picker-footer-actions--startTime__button:hover,
.analytics-picker-footer-actions--endTime__button:hover {
  background: rgba(64, 158, 255, 0.08);
}
</style>
