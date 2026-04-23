<script setup lang="ts">
import * as echarts from 'echarts'
import { onMounted, ref, watch } from 'vue'

const props = defineProps<{
  indicators: { name: string; max: number }[]
  values: number[]
}>()

const chartRef = ref<HTMLDivElement | null>(null)

function render() {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    radar: { indicator: props.indicators },
    series: [{ type: 'radar', data: [{ value: props.values }] }],
  })
}

onMounted(render)
watch(() => props.values, render, { deep: true })
</script>

<template>
  <div ref="chartRef" style="width: 100%; height: 400px;"></div>
</template>
