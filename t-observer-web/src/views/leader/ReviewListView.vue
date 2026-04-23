<script setup lang="ts">
import type { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetchReviewRecords } from '@/api/reviews'
import type { ReviewListItem } from '@/types/record'
import { formatDateTimeToMinute } from '@/utils/datetime'

const router = useRouter()
const records = ref<ReviewListItem[]>([])
const loading = ref(false)
const errorMessage = ref('')

const statusConfig: Record<string, { label: string; type: 'info' | 'success' | 'danger' }> = {
  SUBMITTED: { label: '待评审', type: 'info' },
  APPROVED: { label: '已完成', type: 'success' },
  RETURNED: { label: '未通过', type: 'danger' },
}

function getAxiosMessage(error: unknown, fallback: string) {
  return (
    ((error as AxiosError<{ message?: string }>)?.response?.data?.message ?? '').trim() || fallback
  )
}

function getStatusConfig(status: string) {
  return statusConfig[status] ?? { label: status, type: 'info' as const }
}

function getActionLabel(status: string) {
  return status === 'SUBMITTED' ? '评审' : '查看'
}

async function loadRecords() {
  loading.value = true
  errorMessage.value = ''

  try {
    records.value = await fetchReviewRecords()
  } catch (error) {
    errorMessage.value = getAxiosMessage(error, '评审列表加载失败，请稍后重试')
    records.value = []
  } finally {
    loading.value = false
  }
}

async function openRecord(row: ReviewListItem) {
  await router.push({ name: 'leader-review-form', params: { recordId: row.recordId } })
}

async function retryLoad() {
  await loadRecords()
  if (!errorMessage.value) {
    ElMessage.success('评审列表已更新')
  }
}

onMounted(loadRecords)
</script>

<template>
  <section class="review-list">
    <header class="review-list__header">
      <div>
        <p class="review-list__eyebrow">记录评审</p>
        <h1>评审列表</h1>
      </div>
      <el-button :loading="loading" @click="retryLoad">刷新</el-button>
    </header>

    <el-alert
      v-if="errorMessage"
      type="warning"
      :title="errorMessage"
      show-icon
      :closable="false"
    />

    <el-table
      v-loading="loading"
      :data="records"
      class="review-list__table"
      empty-text="暂无需要展示的评审记录"
      row-key="recordId"
    >
      <el-table-column prop="taskTitle" label="任务标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="teacherName" label="授课教师" min-width="110" />
      <el-table-column prop="courseName" label="课程名称" min-width="140" show-overflow-tooltip />
      <el-table-column label="听课时间" min-width="160">
        <template #default="{ row }">
          {{ formatDateTimeToMinute(row.lessonTime) }}
        </template>
      </el-table-column>
      <el-table-column label="截止时间" min-width="160">
        <template #default="{ row }">
          {{ formatDateTimeToMinute(row.deadline) }}
        </template>
      </el-table-column>
      <el-table-column label="状态标签" width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusConfig(row.recordStatus).type" effect="plain">
            {{ getStatusConfig(row.recordStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作按钮" width="110" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            :data-testid="`review-action-${row.recordId}`"
            @click="openRecord(row)"
          >
            {{ getActionLabel(row.recordStatus) }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<style scoped>
.review-list {
  display: grid;
  gap: 18px;
}

.review-list__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.review-list__header h1 {
  margin: 4px 0 0;
  color: #303133;
  font-size: 18px;
}

.review-list__eyebrow {
  margin: 0;
  color: #409eff;
  font-size: 13px;
  font-weight: 700;
}

.review-list__table {
  width: 100%;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .review-list__header {
    flex-direction: column;
  }
}
</style>
