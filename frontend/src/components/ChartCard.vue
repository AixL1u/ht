<template>
  <div class="chart-card">
    <div class="chart-header">
      <h3 class="chart-title">{{ title }}</h3>
      <div v-if="$slots.actions" class="chart-actions">
        <slot name="actions" />
      </div>
    </div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

interface Props {
  title: string
  option: any
  height?: string | number
}

const props = withDefaults(defineProps<Props>(), {
  height: 300
})

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

onMounted(() => {
  initChart()
})

watch(() => props.option, () => {
  if (chart) {
    chart.setOption(props.option)
  }
}, { deep: true })

function initChart() {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  chart.setOption(props.option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart?.resize()
  })
}

defineExpose({
  getChart: () => chart
})
</script>

<style scoped>
.chart-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(220, 214, 203, 0.6);
  border-radius: 16px;
  padding: 28px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  animation: fadeInScale 0.6s ease-out;
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--brand) 0%, var(--accent) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
  border-color: var(--brand-light);
}

.chart-card:hover::before {
  opacity: 1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(220, 214, 203, 0.3);
}

.chart-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: 0.3px;
  position: relative;
  padding-left: 12px;
}

.chart-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--brand) 0%, var(--accent) 100%);
  border-radius: 2px;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.chart-container {
  width: 100%;
  min-height: v-bind('`${props.height}px`');
  position: relative;
}

@media (max-width: 768px) {
  .chart-card {
    padding: 20px;
  }

  .chart-header {
    margin-bottom: 20px;
    padding-bottom: 12px;
  }

  .chart-title {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .chart-card {
    padding: 16px;
  }

  .chart-header {
    margin-bottom: 16px;
  }

  .chart-title {
    font-size: 14px;
  }
}
</style>
