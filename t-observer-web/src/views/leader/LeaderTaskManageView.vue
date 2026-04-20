<script setup lang="ts">
import type { AxiosError } from 'axios'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'

import { createTask, fetchTasks } from '@/api/tasks'
import StatusTag from '@/components/common/StatusTag.vue'
import {
  TASK_STATUS_FILTERS,
  type TaskCreatePayload,
  type TaskListItem,
  type TaskStatus,
} from '@/types/task'

const loading = ref(false)
const drawerOpen = ref(false)
const creating = ref(false)
const errorMessage = ref('')
const tasks = ref<TaskListItem[]>([])
const activeFilter = ref<TaskStatus | 'ALL'>('ALL')
const isMobile = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<TaskCreatePayload>({
  title: '',
  observerId: 0,
  teacherName: '',
  courseName: '',
  lessonTime: '',
  deadline: '',
  remark: '',
})

function updateViewport() {
  isMobile.value = window.innerWidth < 768
}

function getAxiosMessage(error: unknown, fallback: string) {
  return (
    ((error as AxiosError<{ message?: string }>)?.response?.data?.message ?? '').trim() || fallback
  )
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

const rules: FormRules<TaskCreatePayload> = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  observerId: [
    { required: true, message: '请输入听课成员 ID', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (typeof value !== 'number' || Number.isNaN(value) || value <= 0) {
          callback(new Error('成员 ID 必须为正整数'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  teacherName: [{ required: true, message: '请输入授课教师', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  lessonTime: [{ required: true, message: '请选择听课时间', trigger: 'change' }],
  deadline: [
    { required: true, message: '请选择截止时间', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (!value || !form.lessonTime) {
          callback()
          return
        }

        const lessonTime = new Date(form.lessonTime).getTime()
        const deadline = new Date(value).getTime()
        if (deadline < lessonTime) {
          callback(new Error('截止时间不能早于听课时间'))
          return
        }

        callback()
      },
      trigger: 'change',
    },
  ],
}

const emptyDescription = computed(() =>
  activeFilter.value === 'ALL'
    ? '可以先创建一条听课任务，分配给成员开始填写记录。'
    : '当前筛选条件下暂无任务，建议切换状态或新建任务。',
)

function resetForm() {
  form.title = ''
  form.observerId = 0
  form.teacherName = ''
  form.courseName = ''
  form.lessonTime = ''
  form.deadline = ''
  form.remark = ''
}

async function switchFilter(filter: TaskStatus | 'ALL') {
  if (activeFilter.value === filter) {
    return
  }

  activeFilter.value = filter
  await loadTasks()
}

async function handleCreateTask() {
  const isValid = await formRef.value
    ?.validate()
    .then(() => true)
    .catch(() => false)

  if (!isValid) {
    return
  }

  creating.value = true

  try {
    await createTask({
      ...form,
      remark: form.remark?.trim() || undefined,
    })
    ElMessage.success('任务创建成功')
    drawerOpen.value = false
    resetForm()
    await loadTasks()
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '任务创建失败，请稍后重试'))
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  updateViewport()
  window.addEventListener('resize', updateViewport)
  void loadTasks()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewport)
})
</script>

<template>
  <section class="leader-task-page">
    <header class="leader-task-page__hero">
      <div>
        <p class="leader-task-page__eyebrow">组长任务</p>
        <h1>任务管理</h1>
        <p>查看自己创建的任务，按状态筛选，并通过右侧抽屉快速创建新任务。</p>
      </div>
      <el-button type="primary" @click="drawerOpen = true">新建任务</el-button>
    </header>

    <section class="leader-task-page__filters">
      <button
        v-for="filter in TASK_STATUS_FILTERS"
        :key="filter.value"
        class="leader-task-page__filter"
        :class="{ 'leader-task-page__filter--active': activeFilter === filter.value }"
        type="button"
        @click="switchFilter(filter.value)"
      >
        {{ filter.label }}
      </button>
    </section>

    <section v-if="loading" class="leader-task-page__skeletons">
      <el-skeleton v-for="item in 4" :key="item" animated />
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

    <el-empty v-else-if="tasks.length === 0" :description="emptyDescription" />

    <template v-else>
      <el-table v-if="!isMobile" :data="tasks" class="leader-task-page__table">
        <el-table-column prop="title" label="任务标题" min-width="180" />
        <el-table-column prop="observerName" label="听课成员" min-width="120" />
        <el-table-column prop="teacherName" label="授课教师" min-width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="140" />
        <el-table-column label="听课时间" min-width="160">
          <template #default="{ row }">{{ row.lessonTime.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="截止时间" min-width="160">
          <template #default="{ row }">{{ row.deadline.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
      </el-table>

      <section v-else class="leader-task-page__cards">
        <article v-for="task in tasks" :key="task.id" class="leader-task-card">
          <div class="leader-task-card__header">
            <div>
              <h2>{{ task.title }}</h2>
              <p>{{ task.teacherName }} · {{ task.courseName }}</p>
            </div>
            <StatusTag :status="task.status" />
          </div>

          <p>成员：{{ task.observerName || `ID ${task.observerId}` }}</p>
          <p>听课时间：{{ task.lessonTime.replace('T', ' ') }}</p>
          <p>截止时间：{{ task.deadline.replace('T', ' ') }}</p>
        </article>
      </section>
    </template>

    <el-drawer
      v-model="drawerOpen"
      title="新建听课任务"
      size="420px"
      destroy-on-close
      @closed="formRef?.resetFields()"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：高一数学听课" />
        </el-form-item>

        <el-form-item label="听课成员 ID" prop="observerId">
          <el-input-number v-model="form.observerId" :min="1" :step="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="授课教师" prop="teacherName">
          <el-input v-model="form.teacherName" placeholder="请输入授课教师姓名" />
        </el-form-item>

        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>

        <el-form-item label="听课时间" prop="lessonTime">
          <el-date-picker
            v-model="form.lessonTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择听课时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择截止时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="4"
            placeholder="可填写本次任务希望重点关注的课堂要点。"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="leader-task-page__drawer-footer">
          <el-button @click="drawerOpen = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreateTask">
            创建任务
          </el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
.leader-task-page {
  display: grid;
  gap: 20px;
}

.leader-task-page__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.12), rgba(64, 158, 255, 0.03));
}

.leader-task-page__hero h1 {
  margin: 6px 0 0;
  font-size: 24px;
}

.leader-task-page__hero p {
  margin: 10px 0 0;
  color: var(--ui-color-text-secondary);
  line-height: 1.6;
}

.leader-task-page__eyebrow {
  margin: 0;
  color: var(--ui-color-primary);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.leader-task-page__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.leader-task-page__filter {
  padding: 10px 16px;
  border: none;
  border-radius: 999px;
  background: #eef3f8;
  color: #425466;
  cursor: pointer;
}

.leader-task-page__filter--active {
  background: var(--ui-color-primary);
  color: #fff;
}

.leader-task-page__table {
  overflow: hidden;
  border-radius: 16px;
}

.leader-task-page__cards {
  display: grid;
  gap: 16px;
}

.leader-task-card {
  padding: 20px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.leader-task-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.leader-task-card__header h2 {
  margin: 0;
  font-size: 18px;
}

.leader-task-card__header p,
.leader-task-card p {
  margin: 8px 0 0;
  color: var(--ui-color-text-secondary);
}

.leader-task-page__drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .leader-task-page__hero,
  .leader-task-card__header {
    flex-direction: column;
  }
}
</style>
