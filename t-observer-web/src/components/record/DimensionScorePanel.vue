<script setup lang="ts">
import { computed } from 'vue'

import { normalizeScores, type ScoreItem } from '@/types/record'

const props = withDefaults(
  defineProps<{
    modelValue?: ScoreItem[]
    disabled?: boolean
  }>(),
  {
    modelValue: () => [],
    disabled: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: ScoreItem[]]
}>()

const scores = computed(() => normalizeScores(props.modelValue))

function updateScore(index: number, value?: number | null) {
  const nextScores = scores.value.map((score, scoreIndex) =>
    scoreIndex === index
      ? {
          ...score,
          scoreValue: typeof value === 'number' ? value : null,
        }
      : score,
  )

  emit('update:modelValue', nextScores)
}
</script>

<template>
  <section class="dimension-score-panel">
    <header class="dimension-score-panel__header">
      <div>
        <h3>五维评分</h3>
        <p>请按照 1.0 到 5.0 的区间完成评分，步长为 0.5。</p>
      </div>
    </header>

    <div class="dimension-score-panel__list">
      <article
        v-for="(score, index) in scores"
        :key="score.dimensionCode"
        class="dimension-score-panel__item"
      >
        <div class="dimension-score-panel__meta">
          <strong>{{ score.dimensionName }}</strong>
          <span>{{ score.dimensionCode }}</span>
        </div>

        <div class="dimension-score-panel__control">
          <el-input-number
            :model-value="score.scoreValue ?? undefined"
            :min="1"
            :max="5"
            :step="0.5"
            :precision="1"
            :disabled="disabled"
            controls-position="right"
            @update:model-value="updateScore(index, $event)"
          />
          <span class="dimension-score-panel__value">
            {{ score.scoreValue === null ? '--' : score.scoreValue.toFixed(1) }}
          </span>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.dimension-score-panel {
  display: grid;
  gap: 18px;
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.08), rgba(64, 158, 255, 0.02));
}

.dimension-score-panel__header h3 {
  margin: 0;
  font-size: 18px;
}

.dimension-score-panel__header p {
  margin: 8px 0 0;
  color: var(--ui-color-text-secondary);
  font-size: 14px;
}

.dimension-score-panel__list {
  display: grid;
  gap: 12px;
}

.dimension-score-panel__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
}

.dimension-score-panel__meta {
  display: grid;
  gap: 4px;
}

.dimension-score-panel__meta strong {
  font-size: 14px;
}

.dimension-score-panel__meta span {
  color: var(--ui-color-text-secondary);
  font-size: 12px;
}

.dimension-score-panel__control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dimension-score-panel__value {
  min-width: 36px;
  font-weight: 600;
  color: var(--ui-color-primary);
  text-align: right;
}

@media (max-width: 640px) {
  .dimension-score-panel__item {
    align-items: flex-start;
    flex-direction: column;
  }

  .dimension-score-panel__control {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
