<template>
  <div class="tag-generator">
    <div class="generator-header">
      <h3>自动标签生成</h3>
      <p class="generator-desc">输入内容，系统将自动生成相关标签</p>
    </div>
    
    <div class="generator-content">
      <!-- 输入区域 -->
      <div class="input-section">
        <label class="input-label">内容输入</label>
        <el-input
          v-model="content"
          type="textarea"
          :rows="6"
          placeholder="输入项目描述、资讯内容或商品说明..."
          maxlength="1000"
          show-word-limit
        />
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="generateTags" :loading="loading">
          生成标签
        </el-button>
        <el-button @click="extractKeywords" :loading="loading">
          提取关键词
        </el-button>
        <el-button @click="clearContent">清空</el-button>
      </div>
      
      <!-- 生成的标签 -->
      <div v-if="generatedTags.length" class="tags-section">
        <label class="tags-label">生成的标签</label>
        <div class="tags-container">
          <el-tag
            v-for="tag in generatedTags"
            :key="tag"
            closable
            @close="removeTag(tag)"
            type="success"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      
      <!-- 提取的关键词 -->
      <div v-if="extractedKeywords.length" class="keywords-section">
        <label class="keywords-label">提取的关键词</label>
        <div class="keywords-container">
          <el-tag
            v-for="keyword in extractedKeywords"
            :key="keyword"
            @click="addTag(keyword)"
            style="cursor: pointer"
          >
            {{ keyword }}
            <el-icon style="margin-left: 4px"><Plus /></el-icon>
          </el-tag>
        </div>
      </div>
      
      <!-- 标签建议 -->
      <div class="suggestions-section">
        <label class="suggestions-label">推荐标签</label>
        <div class="suggestions-container">
          <el-tag
            v-for="tag in suggestions"
            :key="tag"
            @click="addTag(tag)"
            style="cursor: pointer"
          >
            {{ tag }}
            <el-icon style="margin-left: 4px"><Plus /></el-icon>
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import http from '../api/http'

const content = ref('')
const generatedTags = ref<string[]>([])
const extractedKeywords = ref<string[]>([])
const suggestions = ref<string[]>([])
const loading = ref(false)

onMounted(async () => {
  try {
    // 获取推荐标签
    const res = await http.get('/tags/all')
    suggestions.value = res.data.tags || []
  } catch (e) {
    console.error('Failed to load suggestions:', e)
  }
})

async function generateTags() {
  if (!content.value.trim()) {
    alert('请输入内容')
    return
  }
  
  loading.value = true
  try {
    const res = await http.post('/tags/generate', { content: content.value })
    generatedTags.value = res.data.tags || []
  } catch (e) {
    console.error('Failed to generate tags:', e)
  } finally {
    loading.value = false
  }
}

async function extractKeywords() {
  if (!content.value.trim()) {
    alert('请输入内容')
    return
  }
  
  loading.value = true
  try {
    const res = await http.post('/tags/extract', { content: content.value })
    extractedKeywords.value = res.data.keywords || []
  } catch (e) {
    console.error('Failed to extract keywords:', e)
  } finally {
    loading.value = false
  }
}

function addTag(tag: string) {
  if (!generatedTags.value.includes(tag)) {
    generatedTags.value.push(tag)
  }
}

function removeTag(tag: string) {
  generatedTags.value = generatedTags.value.filter(t => t !== tag)
}

function clearContent() {
  content.value = ''
  generatedTags.value = []
  extractedKeywords.value = []
}

defineExpose({
  generatedTags,
  clearContent
})
</script>

<style scoped>
.tag-generator {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.generator-header {
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-6);
  border-bottom: 1px solid var(--border);
}

.generator-header h3 {
  margin: 0 0 var(--space-2);
  font-size: 18px;
  font-weight: 600;
}

.generator-desc {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.generator-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.input-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.input-label {
  font-weight: 600;
  color: var(--text);
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.tags-section,
.keywords-section,
.suggestions-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.tags-label,
.keywords-label,
.suggestions-label {
  font-weight: 600;
  color: var(--text);
  font-size: 14px;
}

.tags-container,
.keywords-container,
.suggestions-container {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.tags-container :deep(.el-tag) {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border-color: #10b981;
  color: white;
}

.keywords-container :deep(.el-tag),
.suggestions-container :deep(.el-tag) {
  background: var(--bg-secondary);
  border-color: var(--border);
  color: var(--text);
  transition: all 0.3s ease;
}

.keywords-container :deep(.el-tag:hover),
.suggestions-container :deep(.el-tag:hover) {
  background: var(--brand);
  border-color: var(--brand);
  color: white;
}

@media (max-width: 768px) {
  .tag-generator {
    padding: var(--space-4);
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons :deep(.el-button) {
    width: 100%;
  }
}
</style>
