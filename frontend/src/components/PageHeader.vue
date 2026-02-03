<template>
  <div class="page-header">
    <div class="page-header-content">
      <slot>
        <h3 v-if="title" class="page-header-title">{{ title }}</h3>
      </slot>
      <div v-if="sub" class="page-header-subtitle">{{ sub }}</div>
    </div>
    <div class="page-header-actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props { title?: string; sub?: string }
const props = defineProps<Props>()
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 3px solid transparent;
  border-image: linear-gradient(90deg, var(--brand) 0%, var(--accent) 100%);
  border-image-slice: 1;
  position: relative;
  animation: slideInDown 0.5s ease-out;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, var(--brand) 0%, transparent 100%);
  animation: expandWidth 0.8s ease-out;
}

@keyframes expandWidth {
  from {
    width: 0;
  }
  to {
    width: 100px;
  }
}

.page-header-content {
  flex: 1;
}

.page-header-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--text) 0%, var(--brand) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.page-header-subtitle {
  color: var(--text-secondary);
  margin-top: 8px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.page-header-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 24px;
    padding-bottom: 20px;
  }

  .page-header-title {
    font-size: 24px;
  }

  .page-header-subtitle {
    font-size: 13px;
  }

  .page-header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .page-header {
    margin-bottom: 20px;
    padding-bottom: 16px;
  }

  .page-header-title {
    font-size: 20px;
  }

  .page-header-subtitle {
    font-size: 12px;
  }
}
</style>
