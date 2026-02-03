<template>
  <div class="stat-card">
    <div class="stat-header">
      <span class="stat-label">{{ label }}</span>
      <el-icon v-if="icon" class="stat-icon" :style="{ color: iconColor }">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="stat-value">{{ formatValue(value) }}</div>
    <div v-if="change !== undefined" class="stat-change" :class="changeClass">
      <el-icon :size="12">
        <component :is="change > 0 ? 'CaretTop' : 'CaretBottom'" />
      </el-icon>
      <span>{{ Math.abs(change) }}%</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  label: string
  value: number | string
  change?: number
  icon?: string
  iconColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  iconColor: '#6366f1'
})

const changeClass = computed(() => {
  if (props.change === undefined) return ''
  return props.change > 0 ? 'positive' : 'negative'
})

function formatValue(val: number | string): string {
  if (typeof val === 'string') return val
  if (val >= 1000000) return (val / 1000000).toFixed(1) + 'M'
  if (val >= 1000) return (val / 1000).toFixed(1) + 'K'
  return val.toString()
}
</script>

<style scoped>
.stat-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(220, 214, 203, 0.6);
  border-radius: 16px;
  padding: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--brand) 0%, var(--accent) 100%);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s ease;
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-card:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(-8px);
  border-color: var(--brand);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-icon {
  font-size: 32px;
  opacity: 0.9;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.15) rotate(5deg);
  opacity: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--brand) 0%, var(--brand-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  line-height: 1;
  letter-spacing: -1px;
}

.stat-change {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.04);
}

.stat-change.positive {
  color: var(--success);
  background: rgba(46, 139, 87, 0.1);
}

.stat-change.negative {
  color: var(--danger);
  background: rgba(192, 44, 56, 0.1);
}

@media (max-width: 768px) {
  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 28px;
  }
  
  .stat-icon {
    font-size: 28px;
  }
}

@media (max-width: 480px) {
  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 24px;
  }
  
  .stat-label {
    font-size: 12px;
  }
}
</style>
