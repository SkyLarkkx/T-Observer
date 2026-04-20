<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { LocationQueryRaw } from 'vue-router'

import { fetchTasks } from '@/api/tasks'
import StatusTag from '@/components/common/StatusTag.vue'
import {
  TASK_STATUS_FILTERS,
  TASK_STATUS_LABELS,
  type TaskListItem,
  type TaskStatus,
} from '@/types/task'

const loading = ref(false)
const errorMessage = ref('')
const tasks = ref<TaskListItem[]>([])
const activeFilter = ref<TaskStatus | 'ALL'>('ALL')

const hasTasks = computed(() => tasks.value.length > 0)

function formatDateTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function buildTaskQuery(task: TaskListItem): LocationQueryRaw {
  return {
    title: task.title,
    teacherName: task.teacherName,
    courseName: task.courseName,
    lessonTime: task.lessonTime,
    deadline: task.deadline,
    remark: task.remark ?? '',
    status: task.status,
  }
}

function getActionText(status: TaskStatus) {
  switch (status) {
    case 'PENDING':
      return '去填报'
    case 'IN_PROGRESS':
      return '继续填报'
    case 'COMPLETED':
      return '查看任务'
  }
}

async function loadTasks() {
  loading.value = true
  errorMessage.value = ''

  try {
    tasks.value = await fetchTasks(
      activeFilter.value === 'ALL' ? undefined : { status: activeFilter.value },
    )
  } catch {
    errorMessage.value = '任务加载失败，请稍后重试'
    tasks.value = []
  } finally {
    loading.value = false
  }
}

async function switchFilter(filter: TaskStatus | 'ALL') {
  if (activeFilter.value === filter) {
    return
  }

  activeFilter.value = filter
  await loadTasks()
}

onMounted(loadTasks)
</script>

<template>
  <section class="member-task-page">
    <header class="member-task-page__hero">
      <div>
        <p class="member-task-page__eyebrow">成员任务</p>
        <h1>我的听课任务</h1>
        <p>成员仅能查看分配给自己的听课任务，并从任务卡片进入独立记录页。</p>
      </div>
      <el-tag effect="plain" round>MEMBER</el-tag>
    </header>

    <section class="member-task-page__filters">
      <button
        v-for="filter in TASK_STATUS_FILTERS"
        :key="filter.value"
        class="member-task-page__filter"
        :class="{ 'member-task-page__filter--active': activeFilter === filter.value }"
        type="button"
        @click="switchFilter(filter.value)"
      >
        {{ filter.label }}
      </button>
    </section>

    <section v-if="loading" class="member-task-page__grid">
      <el-skeleton v-for="item in 3" :key="item" animated class="member-task-page__skeleton">
        <template #template>
          <el-skeleton-item variant="p" style="width: 42%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 100%" />
          <el-skeleton-item variant="text" style="width: 72%" />
          <el-skeleton-item variant="button" style="margin-top: 20px; width: 120px" />
        </template>
      </el-skeleton>
    </section>

    <el-result
      v-else-if="errorMessage"
      icon="warning"
      title="任务加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="loadTasks">重新加载</el-button>
      </template>
    </el-result>

    <el-empty
      v-else-if="!hasTasks"
      description="当前筛选条件下没有待处理任务，可稍后刷新查看。"
    >
      <template #default>
        <div class="member-task-page__empty">
          <strong>暂无听课任务</strong>
        </div>
      </template>
    </el-empty>

    <section v-else class="member-task-page__grid">
      <article v-for="task in tasks" :key="task.id" class="task-card">
        <div class="task-card__header">
          <div>
            <h2>{{ task.title }}</h2>
            <p>{{ task.teacherName }} · {{ task.courseName }}</p>
          </div>
          <StatusTag :status="task.status" />
        </div>

        <dl class="task-card__meta">
          <div>
            <dt>听课时间</dt>
            <dd>{{ formatDateTime(task.lessonTime) }}</dd>
          </div>
          <div>
            <dt>截止时间</dt>
            <dd>{{ formatDateTime(task.deadline) }}</dd>
          </div>
        </dl>

        <p v-if="task.remark" class="task-card__remark">{{ task.remark }}</p>

        <footer class="task-card__footer">
          <span class="task-card__summary">{{ TASK_STATUS_LABELS[task.status] }}任务</span>
          <RouterLink
            class="task-card__link"
            :to="{
              name: 'member-record-form',
              params: { taskId: task.id },
              query: buildTaskQuery(task),
            }"
          >
            <el-button type="primary" plain>{{ getActionText(task.status) }}</el-button>
          </RouterLink>
        </footer>
      </article>
    </section>
  </section>
</template>

<style scoped>
.member-task-page {
  display: grid;
  gap: 20px;
}

.member-task-page__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.12), rgba(64, 158, 255, 0.03));
}

.member-task-page__hero h1 {
  margin: 6px 0 0;
  font-size: 24px;
}

.member-task-page__hero p {
  margin: 10px 0 0;
  color: var(--ui-color-text-secondary);
  line-height: 1.6;
}

.member-task-page__eyebrow {
  margin: 0;
  color: var(--ui-color-primary);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.member-task-page__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.member-task-page__filter {
  padding: 10px 16px;
  border: none;
  border-radius: 999px;
  background: #eef3f8;
  color: #425466;
  cursor: pointer;
  transition:
    background-color 0.18s ease,
    color 0.18s ease,
    transform 0.18s ease;
}

.member-task-page__filter:hover {
  transform: translateY(-1px);
}

.member-task-page__filter--active {
  background: var(--ui-color-primary);
  color: #fff;
  box-shadow: 0 10px 22px rgba(64, 158, 255, 0.22);
}

.member-task-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.member-task-page__skeleton,
.task-card {
  padding: 20px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.member-task-page__empty {
  color: var(--ui-color-text-secondary);
  font-size: 14px;
}

.task-card {
  display: grid;
  gap: 16px;
  border: 1px solid rgba(64, 158, 255, 0.08);
}

.task-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.task-card__header h2 {
  margin: 0;
  font-size: 18px;
}

.task-card__header p {
  margin: 8px 0 0;
  color: var(--ui-color-text-secondary);
  font-size: 14px;
}

.task-card__meta {
  display: grid;
  gap: 12px;
  margin: 0;
}

.task-card__meta div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px dashed rgba(148, 163, 184, 0.28);
}

.task-card__meta div:last-child {
  border-bottom: none;
}

.task-card__meta dt {
  color: var(--ui-color-text-secondary);
}

.task-card__meta dd {
  margin: 0;
  font-weight: 600;
  text-align: right;
}

.task-card__remark {
  margin: 0;
  padding: 14px;
  border-radius: 12px;
  background: #f8fbff;
  color: #425466;
  line-height: 1.6;
}

.task-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.task-card__summary {
  color: var(--ui-color-text-secondary);
  font-size: 14px;
}

.task-card__link {
  text-decoration: none;
}

@media (max-width: 640px) {
  .member-task-page__hero,
  .task-card__header,
  .task-card__footer,
  .task-card__meta div {
    align-items: flex-start;
    flex-direction: column;
  }

  .task-card__meta dd {
    text-align: left;
  }
}
</style>
