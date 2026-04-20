<script setup lang="ts">
import { computed } from 'vue'

import { TASK_STATUS_LABELS, type TaskStatus } from '@/types/task'

const props = defineProps<{
  status: TaskStatus
}>()

const label = computed(() => TASK_STATUS_LABELS[props.status] ?? props.status)
const toneClass = computed(() => `status-tag--${props.status.toLowerCase()}`)
</script>

<template>
  <span class="status-tag" :class="toneClass">{{ label }}</span>
</template>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
}

.status-tag--pending {
  color: #b26a1b;
  background: #fff3e1;
}

.status-tag--in_progress {
  color: #2174d7;
  background: #eaf3ff;
}

.status-tag--completed {
  color: #18815a;
  background: #eaf8f2;
}
</style>
