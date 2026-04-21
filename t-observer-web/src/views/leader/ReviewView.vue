<script setup lang="ts">
import type { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { approveRecord, fetchReviewRecord, rejectRecord } from '@/api/reviews'
import type { ObservationRecord } from '@/types/record'

const route = useRoute()
const router = useRouter()
const recordId = Number(route.params.recordId)
const rejectReason = ref('')
const record = ref<ObservationRecord | null>(null)
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

const canReview = computed(() => record.value?.status === 'SUBMITTED')

function getAxiosMessage(error: unknown, fallback: string) {
  return (
    ((error as AxiosError<{ message?: string }>)?.response?.data?.message ?? '').trim() || fallback
  )
}

function formatDateTime(value: string | null) {
  if (!value) {
    return '--'
  }

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

async function loadRecord() {
  if (!Number.isFinite(recordId)) {
    errorMessage.value = '评审记录参数无效'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    record.value = await fetchReviewRecord(recordId)
  } catch (error) {
    errorMessage.value = getAxiosMessage(error, '评审记录加载失败，请稍后重试')
    record.value = null
  } finally {
    loading.value = false
  }
}

async function handleApprove() {
  if (!canReview.value) {
    return
  }

  submitting.value = true

  try {
    record.value = await approveRecord(recordId)
    ElMessage.success('已通过')
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '通过失败，请稍后重试'))
  } finally {
    submitting.value = false
  }
}

async function handleReject() {
  const reason = rejectReason.value.trim()
  if (!canReview.value) {
    return
  }
  if (!reason) {
    ElMessage.warning('退回时必须填写原因')
    return
  }

  submitting.value = true

  try {
    record.value = await rejectRecord(recordId, reason)
    ElMessage.success('已退回')
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '退回失败，请稍后重试'))
  } finally {
    submitting.value = false
  }
}

async function goBack() {
  await router.push({ name: 'leader-task-manage' })
}

onMounted(loadRecord)
</script>

<template>
  <section class="review-page">
    <header class="review-page__header">
      <div>
        <p class="review-page__eyebrow">记录评审</p>
        <h1>评审听课记录</h1>
      </div>
      <el-button text type="primary" @click="goBack">返回任务管理</el-button>
    </header>

    <el-skeleton v-if="loading" animated />

    <el-result
      v-else-if="errorMessage"
      icon="warning"
      title="记录加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="loadRecord">重新加载</el-button>
      </template>
    </el-result>

    <template v-else-if="record">
      <section class="review-page__card">
        <div class="review-page__title-row">
          <div>
            <span class="review-page__label">授课教师</span>
            <h2>{{ record.teacherName }}</h2>
          </div>
          <el-tag effect="plain">{{ record.status }}</el-tag>
        </div>

        <dl class="review-page__meta">
          <div>
            <dt>记录 ID</dt>
            <dd>{{ record.id }}</dd>
          </div>
          <div>
            <dt>任务 ID</dt>
            <dd>{{ record.taskId }}</dd>
          </div>
          <div>
            <dt>提交时间</dt>
            <dd>{{ formatDateTime(record.submittedAt) }}</dd>
          </div>
          <div>
            <dt>审批时间</dt>
            <dd>{{ formatDateTime(record.approvedAt) }}</dd>
          </div>
        </dl>
      </section>

      <section class="review-page__score-card">
        <h2>五维评分</h2>
        <div class="review-page__scores">
          <article v-for="score in record.scores" :key="score.dimensionCode">
            <span>{{ score.dimensionName }}</span>
            <strong>{{ score.scoreValue?.toFixed(1) ?? '--' }}</strong>
          </article>
        </div>
      </section>

      <section class="review-page__text-grid">
        <article>
          <h2>优势分析</h2>
          <p>{{ record.strengths }}</p>
        </article>
        <article>
          <h2>待改进项</h2>
          <p>{{ record.weaknesses }}</p>
        </article>
        <article>
          <h2>改进建议</h2>
          <p>{{ record.suggestions }}</p>
        </article>
      </section>

      <section v-if="record.rejectReason" class="review-page__card">
        <span class="review-page__label">退回原因</span>
        <p>{{ record.rejectReason }}</p>
      </section>

      <section class="review-page__actions">
        <textarea
          v-model="rejectReason"
          data-testid="review-reason"
          :disabled="!canReview || submitting"
          placeholder="填写退回原因"
        ></textarea>
        <div>
          <el-button
            type="primary"
            :disabled="!canReview"
            :loading="submitting"
            data-testid="review-approve"
            @click="handleApprove"
          >
            通过
          </el-button>
          <el-button
            type="danger"
            plain
            :disabled="!canReview"
            :loading="submitting"
            data-testid="review-reject"
            @click="handleReject"
          >
            退回
          </el-button>
        </div>
      </section>
    </template>
  </section>
</template>

<style scoped>
.review-page {
  display: grid;
  gap: 20px;
}

.review-page__header,
.review-page__card,
.review-page__score-card,
.review-page__text-grid article,
.review-page__actions {
  padding: 24px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.review-page__header,
.review-page__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.review-page__header h1,
.review-page__title-row h2,
.review-page__score-card h2,
.review-page__text-grid h2 {
  margin: 4px 0 0;
  font-size: 18px;
}

.review-page__eyebrow,
.review-page__label {
  margin: 0;
  color: #409eff;
  font-size: 13px;
  font-weight: 700;
}

.review-page__meta,
.review-page__scores,
.review-page__text-grid {
  display: grid;
  gap: 16px;
}

.review-page__meta {
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  margin: 20px 0 0;
}

.review-page__meta div,
.review-page__scores article {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f5f7fa;
}

.review-page__meta dt,
.review-page__scores span {
  color: #909399;
  font-size: 13px;
}

.review-page__meta dd {
  margin: 8px 0 0;
  color: #303133;
  font-weight: 600;
}

.review-page__scores {
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin-top: 16px;
}

.review-page__scores strong {
  display: block;
  margin-top: 8px;
  color: #409eff;
  font-size: 18px;
}

.review-page__text-grid {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.review-page__text-grid p,
.review-page__card p {
  margin: 10px 0 0;
  color: #606266;
  line-height: 1.6;
}

.review-page__actions {
  display: grid;
  gap: 14px;
}

.review-page__actions textarea {
  min-height: 88px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font: inherit;
  resize: vertical;
}

.review-page__actions textarea:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.14);
}

.review-page__actions div {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .review-page__header,
  .review-page__title-row {
    flex-direction: column;
  }
}
</style>
