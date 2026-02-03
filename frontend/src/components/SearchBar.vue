<template>
  <div class="search-bar">
    <div class="search-input-wrapper">
      <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索项目、资讯、商品..."
        class="search-input"
        @input="handleInput"
        @focus="showSuggestions = true"
        @blur="hideSuggestions"
        @keyup.enter="handleSearch"
      />
      <el-icon v-if="keyword" class="clear-icon" @click="clearSearch"><Close /></el-icon>
    </div>

    <!-- 搜索建议下拉框 -->
    <div v-if="showSuggestions && (suggestions.length || hotSearches.length)" class="suggestions-dropdown">
      <!-- 搜索建议 -->
      <div v-if="suggestions.length" class="suggestions-group">
        <div class="group-title">搜索建议</div>
        <div
          v-for="suggestion in suggestions"
          :key="suggestion"
          class="suggestion-item"
          @click="selectSuggestion(suggestion)"
        >
          <el-icon><Search /></el-icon>
          <span>{{ suggestion }}</span>
        </div>
      </div>

      <!-- 热门搜索 -->
      <div v-if="!keyword && hotSearches.length" class="suggestions-group">
        <div class="group-title">热门搜索</div>
        <div
          v-for="hot in hotSearches"
          :key="hot"
          class="suggestion-item"
          @click="selectSuggestion(hot)"
        >
          <el-icon><Star /></el-icon>
          <span>{{ hot }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="showResults && results" class="search-results">
      <!-- 项目结果 -->
      <div v-if="results.projects && results.projects.length" class="result-group">
        <div class="result-group-title">
          <span>非遗项目</span>
          <span class="result-count">({{ results.projects.length }})</span>
        </div>
        <router-link
          v-for="project in results.projects"
          :key="project.id"
          :to="project.url"
          class="result-item"
        >
          <div class="result-title">{{ project.title }}</div>
          <div class="result-desc">{{ project.description }}</div>
        </router-link>
      </div>

      <!-- 资讯结果 -->
      <div v-if="results.news && results.news.length" class="result-group">
        <div class="result-group-title">
          <span>资讯公告</span>
          <span class="result-count">({{ results.news.length }})</span>
        </div>
        <router-link
          v-for="item in results.news"
          :key="item.id"
          :to="item.url"
          class="result-item"
        >
          <div class="result-title">{{ item.title }}</div>
          <div class="result-desc">{{ item.description }}</div>
        </router-link>
      </div>

      <!-- 商品结果 -->
      <div v-if="results.products && results.products.length" class="result-group">
        <div class="result-group-title">
          <span>文创商品</span>
          <span class="result-count">({{ results.products.length }})</span>
        </div>
        <router-link
          v-for="product in results.products"
          :key="product.id"
          :to="product.url"
          class="result-item"
        >
          <div class="result-title">{{ product.title }}</div>
          <div class="result-desc">¥{{ product.price }}</div>
        </router-link>
      </div>

      <!-- 用户结果 -->
      <div v-if="results.users && results.users.length" class="result-group">
        <div class="result-group-title">
          <span>用户</span>
          <span class="result-count">({{ results.users.length }})</span>
        </div>
        <router-link
          v-for="user in results.users"
          :key="user.id"
          :to="user.url"
          class="result-item"
        >
          <div class="result-title">{{ user.title }}</div>
          <div class="result-desc">{{ user.description }}</div>
        </router-link>
      </div>

      <!-- 无结果提示 -->
      <div v-if="!results.total" class="no-results">
        <el-icon><Search /></el-icon>
        <div>未找到相关结果</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, Close, Star } from '@element-plus/icons-vue'
import http from '../api/http'

const keyword = ref('')
const suggestions = ref<string[]>([])
const hotSearches = ref<string[]>([])
const results = ref<any>(null)
const showSuggestions = ref(false)
const showResults = ref(false)

let searchTimeout: any = null

onMounted(async () => {
  try {
    // 获取热门搜索词
    const res = await http.get('/search/hot')
    hotSearches.value = res.data.hotSearches || []
  } catch (e) {
    console.error('Failed to load hot searches:', e)
  }
})

function handleInput() {
  // 清除之前的搜索结果
  showResults.value = false
  results.value = null

  // 防抖获取建议
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(async () => {
    if (keyword.value.trim()) {
      try {
        const res = await http.get('/search/suggestions', {
          params: { prefix: keyword.value }
        })
        console.log('Suggestions:', res.data.suggestions) // 添加调试日志
        suggestions.value = res.data.suggestions || []
      } catch (e) {
        console.error('Failed to load suggestions:', e)
      }
    } else {
      suggestions.value = []
    }
  }, 300)
}

function selectSuggestion(suggestion: string) {
  console.log('Selecting suggestion:', suggestion)
  keyword.value = suggestion
  handleSearch()
}

async function handleSearch() {
  console.log('Searching for:', keyword.value)
  if (!keyword.value.trim()) return

  try {
    showSuggestions.value = false
    const res = await http.get('/search', {
      params: { keyword: keyword.value }
    })
    console.log('Search results:', res.data.results) // 添加调试日志
    results.value = res.data.results
    showResults.value = true
  } catch (e) {
    console.error('Search failed:', e)
  }
}

function clearSearch() {
  console.log('Clearing search')
  keyword.value = ''
  suggestions.value = []
  results.value = null
  showSuggestions.value = false
  showResults.value = false
}

function hideSuggestions() {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}
</script>

<style scoped>
.search-bar {
  position: relative;
  width: 100%;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background-color: #F2F3F5; /* Light gray background */
  border: 1px solid transparent;
  border-radius: 99px; /* Pill shape */
  padding: 0 16px;
  transition: all 0.3s ease;
  height: 36px;
}

.search-input-wrapper:focus-within {
  background-color: #FFFFFF;
  border-color: var(--brand);
  box-shadow: 0 0 0 2px rgba(192, 44, 56, 0.1);
}

.search-icon {
  color: #909399;
  margin-left: 8px;
  cursor: pointer;
  font-size: 18px;
  flex-shrink: 0;
}

.search-icon:hover {
  color: var(--brand);
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  padding: 0;
  font-size: 14px;
  color: var(--text);
  height: 100%;
}

.search-input::placeholder {
  color: #909399;
}

.clear-icon {
  color: #C0C4CC;
  cursor: pointer;
  margin-right: 4px;
  font-size: 16px;
  flex-shrink: 0;
}

.clear-icon:hover {
  color: #909399;
}

.suggestions-dropdown,
.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--card);
  border: 1px solid var(--border);
  border-top: none;
  border-radius: 0 0 var(--radius) var(--radius);
  max-height: 400px;
  overflow-y: auto;
  z-index: 100;
  box-shadow: var(--elev-2);
}

.suggestions-group {
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border);
}

.suggestions-group:last-child {
  border-bottom: none;
}

.group-title {
  padding: var(--space-2) var(--space-4);
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 600;
  text-transform: uppercase;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  cursor: pointer;
  transition: all 0.3s ease;
}

.suggestion-item:hover {
  background: var(--bg-secondary);
  color: var(--brand);
}

.suggestion-item :deep(.el-icon) {
  font-size: 14px;
  flex-shrink: 0;
}

.result-group {
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border);
}

.result-group:last-child {
  border-bottom: none;
}

.result-group-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) var(--space-4);
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 600;
  text-transform: uppercase;
}

.result-count {
  color: var(--brand);
  font-weight: 600;
}

.result-item {
  display: block;
  padding: var(--space-3) var(--space-4);
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.result-item:hover {
  background: var(--bg-secondary);
  border-left-color: var(--brand);
}

.result-title {
  font-weight: 600;
  color: var(--text);
  margin-bottom: var(--space-1);
}

.result-desc {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--space-4);
  color: var(--text-secondary);
  text-align: center;
}

.no-results :deep(.el-icon) {
  font-size: 32px;
  margin-bottom: var(--space-2);
  opacity: 0.5;
}

@media (max-width: 768px) {
  .suggestions-dropdown,
  .search-results {
    max-height: 300px;
  }

  .result-item {
    padding: var(--space-2) var(--space-3);
  }
}
</style>
