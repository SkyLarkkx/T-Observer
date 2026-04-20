<script setup lang="ts">
import type { AxiosError } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { fetchRecordByTask, saveRecordDraft, submitRecord } from '@/api/records'
import StatusTag from '@/components/common/StatusTag.vue'
import DimensionScorePanel from '@/components/record/DimensionScorePanel.vue'
import {
  createEmptyScores,
  normalizeScores,
  type ObservationRecord,
  type RecordDraftPayload,
  type ScoreItem,
} from '@/types/record'
import type { TaskStatus } from '@/types/task'

type TaskContext = {
  id: number
  title: string
  teacherName: string
  courseName: string
  lessonTime: string
  deadline: string
  remark: string
  status: TaskStatus
}

const route = useRoute()
const router = useRouter()

const scores = ref<ScoreItem[]>(createEmptyScores())
const strengths = ref('')
const weaknesses = ref('')
const suggestions = ref('')
const loadingRecord = ref(false)
const isSavingDraft = ref(false)
const isSubmitting = ref(false)
const currentTaskStatus = ref<TaskStatus | null>(null)

function formatDateTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function getAxiosMessage(error: unknown, fallback: string) {
  return (
    ((error as AxiosError<{ message?: string }>)?.response?.data?.message ?? '').trim() || fallback
  )
}

function resolveStatus(value: unknown): TaskStatus | null {
  if (value === 'PENDING' || value === 'IN_PROGRESS' || value === 'COMPLETED') {
    return value
  }

  return null
}

const taskContext = computed<TaskContext | null>(() => {
  const taskId = Number(route.params.taskId)
  const title = String(route.query.title ?? '').trim()
  const teacherName = String(route.query.teacherName ?? '').trim()
  const courseName = String(route.query.courseName ?? '').trim()
  const lessonTime = String(route.query.lessonTime ?? '').trim()
  const deadline = String(route.query.deadline ?? '').trim()
  const status = resolveStatus(route.query.status)

  if (
    !Number.isFinite(taskId) ||
    !title ||
    !teacherName ||
    !courseName ||
    !lessonTime ||
    !deadline ||
    !status
  ) {
    return null
  }

  return {
    id: taskId,
    title,
    teacherName,
    courseName,
    lessonTime,
    deadline,
    remark: String(route.query.remark ?? ''),
    status,
  }
})

const displayStatus = computed<TaskStatus>(() => currentTaskStatus.value ?? taskContext.value?.status ?? 'PENDING')
const readOnly = computed(() => displayStatus.value === 'COMPLETED')

function buildPayload(): RecordDraftPayload | null {
  if (!taskContext.value) {
    return null
  }

  return {
    taskId: taskContext.value.id,
    teacherName: taskContext.value.teacherName,
    strengths: strengths.value.trim(),
    weaknesses: weaknesses.value.trim(),
    suggestions: suggestions.value.trim(),
    scores: normalizeScores(scores.value),
  }
}

function fillFromRecord(record: ObservationRecord) {
  scores.value = normalizeScores(record.scores)
  strengths.value = record.strengths ?? ''
  weaknesses.value = record.weaknesses ?? ''
  suggestions.value = record.suggestions ?? ''

  if (record.status === 'DRAFT' || record.status === 'RETURNED') {
    currentTaskStatus.value = 'IN_PROGRESS'
    return
  }

  currentTaskStatus.value = 'COMPLETED'
}

function validateSubmitPayload(payload: RecordDraftPayload) {
  if (!payload.strengths || !payload.weaknesses || !payload.suggestions) {
    ElMessage.warning('请先补全优点、待改进项和改进建议。')
    return false
  }

  const normalizedScores = normalizeScores(payload.scores)
  const completedScores = normalizedScores.filter((score) => typeof score.scoreValue === 'number')

  if (completedScores.length !== 5) {
    ElMessage.warning('提交前请完成全部 5 个维度评分。')
    return false
  }

  if (
    completedScores.some(
      (score) => score.scoreValue === null || score.scoreValue < 1 || score.scoreValue > 5,
    )
  ) {
    ElMessage.warning('评分必须处于 1.0 到 5.0 之间。')
    return false
  }

  return true
}

async function loadExistingRecord() {
  if (!taskContext.value) {
    return
  }

  loadingRecord.value = true

  try {
    const record = await fetchRecordByTask(taskContext.value.id)
    if (record) {
      fillFromRecord(record)
    }
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '记录加载失败，请稍后重试'))
  } finally {
    loadingRecord.value = false
  }
}

async function handleSaveDraft() {
  const payload = buildPayload()
  if (!payload || readOnly.value) {
    return
  }

  isSavingDraft.value = true

  try {
    const record = await saveRecordDraft(payload)
    fillFromRecord(record)
    ElMessage.success('草稿已保存')
    await router.push('/tasks')
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '草稿保存失败，请稍后重试'))
  } finally {
    isSavingDraft.value = false
  }
}

async function handleSubmit() {
  const payload = buildPayload()
  if (!payload || readOnly.value || !validateSubmitPayload(payload)) {
    return
  }

  try {
    await ElMessageBox.confirm('提交后任务将进入已完成状态，确认继续吗？', '确认提交', {
      type: 'warning',
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  isSubmitting.value = true

  try {
    const record = await submitRecord(payload)
    fillFromRecord(record)
    ElMessage.success('记录已提交')
    await router.push('/tasks')
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '提交失败，请检查填写内容后重试'))
  } finally {
    isSubmitting.value = false
  }
}

async function goBack() {
  await router.push('/tasks')
}

onMounted(() => {
  if (taskContext.value) {
    currentTaskStatus.value = taskContext.value.status
  }

  void loadExistingRecord()
})
</script>

<template>
  <section v-if="taskContext" class="record-form-page">
    <header class="record-form-page__hero">
      <div class="record-form-page__hero-main">
        <el-button text type="primary" @click="goBack">返回任务列表</el-button>
        <h1>听课记录填写</h1>
        <p>请围绕任务摘要完成五维评分与文本分析，支持先保存草稿再正式提交。</p>
      </div>
      <StatusTag :status="displayStatus" />
    </header>

    <el-alert
      v-if="readOnly"
      title="当前任务已完成，记录页切换为只读展示。"
      type="success"
      :closable="false"
      show-icon
    />

    <el-skeleton v-if="loadingRecord" animated>
      <template #template>
        <el-skeleton-item variant="p" style="width: 50%" />
        <el-skeleton-item variant="text" style="margin-top: 12px; width: 100%" />
        <el-skeleton-item variant="text" style="width: 80%" />
      </template>
    </el-skeleton>

    <template v-else>
      <section class="record-form-page__task-card">
        <div>
          <p class="record-form-page__eyebrow">任务摘要</p>
          <h2>{{ taskContext.title }}</h2>
        </div>

        <dl class="record-form-page__task-grid">
          <div>
            <dt>授课教师</dt>
            <dd>{{ taskContext.teacherName }}</dd>
          </div>
          <div>
            <dt>课程名称</dt>
            <dd>{{ taskContext.courseName }}</dd>
          </div>
          <div>
            <dt>听课时间</dt>
            <dd>{{ formatDateTime(taskContext.lessonTime) }}</dd>
          </div>
          <div>
            <dt>截止时间</dt>
            <dd>{{ formatDateTime(taskContext.deadline) }}</dd>
          </div>
        </dl>

        <p v-if="taskContext.remark" class="record-form-page__remark">{{ taskContext.remark }}</p>
      </section>

      <DimensionScorePanel v-model="scores" :disabled="readOnly" />

      <section class="record-form-page__text-grid">
        <article class="record-form-page__text-card">
          <header>
            <h3>优点分析</h3>
            <span>聚焦课堂亮点、节奏和互动设计。</span>
          </header>
          <el-input
            v-model="strengths"
            type="textarea"
            :rows="5"
            :disabled="readOnly"
            placeholder="请填写本次听课中观察到的主要亮点。"
          />
        </article>

        <article class="record-form-page__text-card">
          <header>
            <h3>待改进项</h3>
            <span>可从提问深度、课堂组织或内容呈现切入。</span>
          </header>
          <el-input
            v-model="weaknesses"
            type="textarea"
            :rows="5"
            :disabled="readOnly"
            placeholder="请填写需要继续改进的课堂环节。"
          />
        </article>

        <article class="record-form-page__text-card">
          <header>
            <h3>改进建议</h3>
            <span>建议尽量具体，便于后续跟进执行。</span>
          </header>
          <el-input
            v-model="suggestions"
            type="textarea"
            :rows="5"
            :disabled="readOnly"
            placeholder="请填写下一步改进建议。"
          />
        </article>
      </section>

      <footer class="record-form-page__footer">
        <p>草稿允许不完整保存，正式提交前需要补齐全部文本与评分。</p>
        <div class="record-form-page__actions">
          <el-button :disabled="readOnly" :loading="isSavingDraft" @click="handleSaveDraft">
            保存草稿
          </el-button>
          <el-button
            type="primary"
            :disabled="readOnly"
            :loading="isSubmitting"
            @click="handleSubmit"
          >
            提交记录
          </el-button>
        </div>
      </footer>
    </template>
  </section>

  <el-result
    v-else
    icon="warning"
    title="未找到任务信息"
    sub-title="请从任务列表重新进入该记录页面。"
  >
    <template #extra>
      <el-button type="primary" @click="goBack">返回任务列表</el-button>
    </template>
  </el-result>
</template>

<style scoped>
.record-form-page {
  display: grid;
  gap: 20px;
}

.record-form-page__hero,
.record-form-page__task-card,
.record-form-page__text-card,
.record-form-page__footer {
  padding: 24px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.record-form-page__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.12), rgba(64, 158, 255, 0.03));
}

.record-form-page__hero-main h1 {
  margin: 8px 0 0;
  font-size: 24px;
}

.record-form-page__hero-main p {
  margin: 10px 0 0;
  color: var(--ui-color-text-secondary);
  line-height: 1.6;
}

.record-form-page__eyebrow {
  margin: 0;
  color: var(--ui-color-primary);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.record-form-page__task-card h2 {
  margin: 6px 0 0;
  font-size: 20px;
}

.record-form-page__task-grid,
.record-form-page__text-grid {
  display: grid;
  gap: 16px;
}

.record-form-page__task-grid {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  margin: 20px 0 0;
}

.record-form-page__task-grid div {
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fbff;
}

.record-form-page__task-grid dt {
  color: var(--ui-color-text-secondary);
  font-size: 13px;
}

.record-form-page__task-grid dd {
  margin: 10px 0 0;
  font-weight: 600;
}

.record-form-page__remark {
  margin: 16px 0 0;
  color: var(--ui-color-text-secondary);
  line-height: 1.6;
}

.record-form-page__text-grid {
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.record-form-page__text-card {
  display: grid;
  gap: 14px;
}

.record-form-page__text-card header h3 {
  margin: 0;
  font-size: 18px;
}

.record-form-page__text-card header span {
  display: block;
  margin-top: 6px;
  color: var(--ui-color-text-secondary);
  font-size: 14px;
}

.record-form-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.record-form-page__footer p {
  margin: 0;
  color: var(--ui-color-text-secondary);
}

.record-form-page__actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .record-form-page__hero,
  .record-form-page__footer {
    flex-direction: column;
  }

  .record-form-page__actions {
    width: 100%;
  }

  .record-form-page__actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
