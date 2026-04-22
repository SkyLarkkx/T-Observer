<script setup lang="ts">
import type { AxiosError } from 'axios'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'

import { fetchMembers } from '@/api/auth'
import { createTask, fetchTasks } from '@/api/tasks'
import StatusTag from '@/components/common/StatusTag.vue'
import type { MemberOption } from '@/types/auth'
import {
  TASK_STATUS_FILTERS,
  type TaskCreatePayload,
  type TaskListItem,
  type TaskStatus,
} from '@/types/task'

const PAGE_SIZE = 10

const loading = ref(false)
const drawerOpen = ref(false)
const creating = ref(false)
const membersLoading = ref(false)
const errorMessage = ref('')
const tasks = ref<TaskListItem[]>([])
const memberOptions = ref<MemberOption[]>([])
const activeFilter = ref<TaskStatus | 'ALL'>('ALL')
const pageNum = ref(1)
const total = ref(0)
const pageJumpValue = ref('')
const isMobile = ref(false)
const detailDialogOpen = ref(false)
const detailTitle = ref('')
const detailContent = ref('')
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

function isReturned(task: TaskListItem) {
  return task.recordStatus === 'RETURNED'
}

function normalizeText(value: string | null) {
  return value?.trim() || '--'
}

function hasText(value: string | null) {
  return normalizeText(value) !== '--'
}

function openTextDetail(title: string, value: string | null) {
  detailTitle.value = `${title}详情`
  detailContent.value = normalizeText(value)
  detailDialogOpen.value = true
}

async function loadTasks(targetPage = pageNum.value) {
  loading.value = true
  errorMessage.value = ''

  try {
    const result = await fetchTasks({
      ...(activeFilter.value === 'ALL' ? {} : { status: activeFilter.value }),
      pageNum: targetPage,
      pageSize: PAGE_SIZE,
    })
    tasks.value = result.list
    total.value = result.total
    pageNum.value = result.pageNum
  } catch {
    errorMessage.value = '任务加载失败，请稍后重试'
    tasks.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadMembers(keyword = '') {
  membersLoading.value = true

  try {
    memberOptions.value = await fetchMembers(keyword.trim() || undefined)
  } catch (error) {
    ElMessage.error(getAxiosMessage(error, '成员列表加载失败，请稍后重试'))
    memberOptions.value = []
  } finally {
    membersLoading.value = false
  }
}

const rules: FormRules<TaskCreatePayload> = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  observerId: [
    { required: true, message: '请选择听课成员', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (typeof value !== 'number' || Number.isNaN(value) || value <= 0) {
          callback(new Error('请选择有效的听课成员'))
          return
        }
        callback()
      },
      trigger: 'change',
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
const maxPage = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))

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
  pageNum.value = 1
  await loadTasks(1)
}

async function handlePageChange(nextPage: number) {
  await loadTasks(nextPage)
}

async function handlePageJump() {
  const targetPage = Number(pageJumpValue.value)
  if (!Number.isFinite(targetPage) || targetPage < 1) {
    return
  }

  const normalizedPage = Math.min(Math.floor(targetPage), maxPage.value)
  pageJumpValue.value = String(normalizedPage)
  await loadTasks(normalizedPage)
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

function formatMemberLabel(member: MemberOption) {
  return `${member.realName}（${member.username}）`
}

async function handleMemberSearch(keyword: string) {
  await loadMembers(keyword)
}

onMounted(() => {
  updateViewport()
  window.addEventListener('resize', updateViewport)
  void loadTasks()
  void loadMembers()
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
        <el-table-column label="备注" min-width="240">
          <template #default="{ row }">
            <div class="leader-task-page__remark-cell">
              <span class="leader-task-page__remark-text">{{ normalizeText(row.remark) }}</span>
              <button
                v-if="hasText(row.remark)"
                class="leader-task-page__text-button"
                type="button"
                @click="openTextDetail('备注', row.remark)"
              >
                查看
              </button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="听课时间" min-width="170">
          <template #default="{ row }">{{ row.lessonTime.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="截止时间" min-width="160">
          <template #default="{ row }">{{ row.deadline.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="状态" width="190">
          <template #default="{ row }">
            <div class="leader-task-page__status-tags">
              <button
                v-if="isReturned(row)"
                class="leader-task-page__failed-tag"
                type="button"
                @click="openTextDetail('退回原因', row.rejectReason)"
              >
                未通过
              </button>
              <StatusTag :status="row.status" />
            </div>
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
            <div class="leader-task-page__status-tags">
              <button
                v-if="isReturned(task)"
                class="leader-task-page__failed-tag"
                type="button"
                @click="openTextDetail('退回原因', task.rejectReason)"
              >
                未通过
              </button>
              <StatusTag :status="task.status" />
            </div>
          </div>

          <p>成员：{{ task.observerName || `ID ${task.observerId}` }}</p>
          <p class="leader-task-card__remark">
            <span>备注：{{ normalizeText(task.remark) }}</span>
            <button
              v-if="hasText(task.remark)"
              class="leader-task-page__text-button"
              type="button"
              @click="openTextDetail('备注', task.remark)"
            >
              查看
            </button>
          </p>
          <p>听课时间：{{ task.lessonTime.replace('T', ' ') }}</p>
          <p>截止时间：{{ task.deadline.replace('T', ' ') }}</p>
        </article>
      </section>
    </template>

    <section v-if="total > 0" class="leader-task-page__pagination">
      <span>共 {{ total }} 条</span>
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="pageNum"
        :page-size="PAGE_SIZE"
        :total="total"
        @current-change="handlePageChange"
      />
      <label class="leader-task-page__jump">
        <span>跳至</span>
        <input
          v-model="pageJumpValue"
          data-testid="leader-page-jump-input"
          inputmode="numeric"
          min="1"
          type="number"
          @keydown.enter="handlePageJump"
        />
        <span>页</span>
        <button type="button" @click="handlePageJump">跳转</button>
      </label>
    </section>

    <el-drawer
      v-model="drawerOpen"
      title="新建听课任务"
      size="420px"
      destroy-on-close
      @opened="loadMembers()"
      @closed="formRef?.resetFields()"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：高一数学听课" />
        </el-form-item>

        <el-form-item label="听课成员" prop="observerId">
          <el-select
            v-model="form.observerId"
            filterable
            remote
            reserve-keyword
            clearable
            placeholder="请选择或搜索听课成员"
            :remote-method="handleMemberSearch"
            :loading="membersLoading"
            style="width: 100%"
          >
            <el-option
              v-for="member in memberOptions"
              :key="member.userId"
              :label="formatMemberLabel(member)"
              :value="member.userId"
            />
          </el-select>
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

    <el-dialog
      v-model="detailDialogOpen"
      :title="detailTitle"
      width="520px"
      :append-to-body="false"
    >
      <p class="leader-task-page__detail-text">{{ detailContent }}</p>
    </el-dialog>
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

.leader-task-page__remark-cell {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
}

.leader-task-page__remark-text {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.leader-task-page__text-button {
  flex: 0 0 auto;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--ui-color-primary);
  cursor: pointer;
  font: inherit;
  font-weight: 600;
}

.leader-task-page__status-tags {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.leader-task-page__failed-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  padding: 6px 10px;
  border: none;
  border-radius: 999px;
  background: #fef0f0;
  color: #f56c6c;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
}

.leader-task-page__pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 14px;
  color: var(--ui-color-text-secondary);
  font-size: 14px;
}

.leader-task-page__jump {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.leader-task-page__jump input {
  width: 64px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  color: #303133;
  font: inherit;
  text-align: center;
}

.leader-task-page__jump input:focus {
  border-color: var(--ui-color-primary);
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.14);
}

.leader-task-page__jump button {
  height: 32px;
  padding: 0 12px;
  border: none;
  border-radius: 8px;
  background: var(--ui-color-primary);
  color: #fff;
  cursor: pointer;
  font: inherit;
}

.leader-task-page__detail-text {
  margin: 0;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
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

.leader-task-card__remark {
  display: flex;
  align-items: center;
  gap: 10px;
}

.leader-task-card__remark span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

  .leader-task-page__pagination {
    align-items: center;
    flex-direction: column;
  }
}
</style>
