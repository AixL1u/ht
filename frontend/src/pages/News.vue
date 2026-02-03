<template>
  <div class="news-container">
    <PageHeader title="资讯公告" />

    <!-- 搜索和筛选区域 -->
    <div class="news-toolbar">
      <div class="search-box">
        <el-input 
          v-model="searchQuery"
          placeholder="搜索资讯标题..." 
          clearable
          @input="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <i class="el-icon-search"></i>
          </template>
        </el-input>
      </div>

      <div class="view-toggle">
        <el-button-group>
          <el-button 
            :type="viewMode === 'timeline' ? 'primary' : 'default'"
            @click="viewMode = 'timeline'"
            size="small"
          >
            <i class="el-icon-s-unfold"></i> 时间线
          </el-button>
          <el-button 
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            @click="viewMode = 'grid'"
            size="small"
          >
            <i class="el-icon-grid"></i> 网格
          </el-button>
          <el-button 
            :type="viewMode === 'list' ? 'primary' : 'default'"
            @click="viewMode = 'list'"
            size="small"
          >
            <i class="el-icon-menu"></i> 列表
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-bar">
      <span class="stat-item">
        <i class="el-icon-document"></i>
        共 <strong>{{ filteredNews.length }}</strong> 条资讯
      </span>
      <span class="stat-item">
        <i class="el-icon-date"></i>
        最新更新：<strong>{{ latestUpdateTime }}</strong>
      </span>
    </div>

    <!-- 时间线视图 -->
    <div v-if="viewMode === 'timeline' && filteredNews.length" class="timeline-view">
      <el-timeline>
        <el-timeline-item 
          v-for="n in filteredNews" 
          :key="n.id" 
          :timestamp="formatDate(n.publishTime)"
          placement="top"
        >
          <div class="news-card-timeline">
            <div class="news-header">
              <h3 class="news-title">{{ n.title }}</h3>
              <el-tag type="info" size="small">{{ n.category || '公告' }}</el-tag>
            </div>
            <p class="news-content">{{ truncateContent(n.content, 150) }}</p>
            <div class="news-footer">
              <span class="author">
                <i class="el-icon-user"></i> {{ n.author || '管理员' }}
              </span>
              <el-button text type="primary" size="small" @click="viewDetail(n)">
                查看详情 →
              </el-button>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- 网格视图 -->
    <div v-else-if="viewMode === 'grid' && filteredNews.length" class="grid-view">
      <div class="news-grid">
        <div 
          v-for="n in filteredNews" 
          :key="n.id"
          class="news-card-grid"
          @click="viewDetail(n)"
        >
          <div class="card-image" v-if="n.coverImage">
            <img :src="n.coverImage" :alt="n.title" />
            <div class="card-overlay">
              <el-button type="primary" text>查看详情</el-button>
            </div>
          </div>
          <div v-else class="card-placeholder">
            <i class="el-icon-document"></i>
          </div>

          <div class="card-content">
            <el-tag type="info" size="small" class="card-tag">{{ n.category || '公告' }}</el-tag>
            <h4 class="card-title">{{ n.title }}</h4>
            <p class="card-desc">{{ truncateContent(n.content, 80) }}</p>
            <div class="card-meta">
              <span class="meta-date">
                <i class="el-icon-date"></i> {{ formatDate(n.publishTime) }}
              </span>
              <span class="meta-author">
                <i class="el-icon-user"></i> {{ n.author || '管理员' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else-if="viewMode === 'list' && filteredNews.length" class="list-view">
      <div 
        v-for="n in filteredNews" 
        :key="n.id"
        class="news-card-list"
        @click="viewDetail(n)"
      >
        <div class="list-left">
          <div class="list-date">
            <div class="date-day">{{ getDay(n.publishTime) }}</div>
            <div class="date-month">{{ getMonth(n.publishTime) }}</div>
          </div>
        </div>

        <div class="list-content">
          <div class="list-header">
            <h3 class="list-title">{{ n.title }}</h3>
            <el-tag type="info" size="small">{{ n.category || '公告' }}</el-tag>
          </div>
          <p class="list-desc">{{ truncateContent(n.content, 200) }}</p>
          <div class="list-footer">
            <span class="footer-item">
              <i class="el-icon-user"></i> {{ n.author || '管理员' }}
            </span>
            <el-button text type="primary" size="small">
              查看详情 →
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!filteredNews.length" class="empty-state">
      <i class="el-icon-document"></i>
      <p v-if="searchQuery">没有找到匹配的资讯</p>
      <p v-else>暂无资讯公告</p>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="selectedNews?.title"
      width="800px"
      append-to-body
      destroy-on-close
    >
      <div class="dialog-content">
        <div class="dialog-meta">
          <div class="meta-date-box">
            <div class="meta-day">{{ getDay(selectedNews?.publishTime) }}</div>
            <div class="meta-month">{{ getMonth(selectedNews?.publishTime) }}</div>
          </div>
          <div class="meta-info">
            <el-tag size="small">{{ selectedNews?.type || '资讯' }}</el-tag>
            <span class="meta-author">
              <i class="el-icon-user"></i> {{ selectedNews?.author || '管理员' }}
            </span>
          </div>
        </div>
        <div class="dialog-body">
          <div class="content-text">{{ selectedNews?.content }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import PageHeader from '../components/PageHeader.vue'

const router = useRouter()
const news = ref<any[]>([])
const searchQuery = ref('')
const viewMode = ref<'timeline' | 'grid' | 'list'>('timeline')
const selectedNews = ref<any>(null)
const dialogVisible = ref(false)

// 过滤后的资讯
const filteredNews = computed(() => {
  if (!searchQuery.value) return news.value
  return news.value.filter(n => 
    n.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    n.content.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 最新更新时间
const latestUpdateTime = computed(() => {
  if (!news.value.length) return '暂无'
  return formatDate(news.value[0]?.publishTime)
})

// 加载资讯
onMounted(async () => {
  try {
    const { data } = await http.get('/news').catch(() => ({ data: [] }))
    news.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('加载资讯失败', e)
  }
})

// 搜索处理
function handleSearch() {
  // 搜索已通过 computed 处理
}

// 查看详情
function viewDetail(item: any) {
  selectedNews.value = item
  dialogVisible.value = true
}

// 关闭详情
function closeDetail() {
  selectedNews.value = null
  dialogVisible.value = false
}

// 获取日期的日
function getDay(date: string | Date): string {
  if (!date) return ''
  return new Date(date).getDate().toString().padStart(2, '0')
}

// 获取日期的月份
function getMonth(date: string | Date): string {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}`
}

// 格式化日期
function formatDate(date: string | Date): string {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit' 
  })
}

// 格式化日期时间
function formatDateTime(date: string | Date): string {
  if (!date) return '暂无时间'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 截断内容
function truncateContent(content: string, length: number): string {
  if (!content) return ''
  return content.length > length ? content.substring(0, length) + '...' : content
}
</script>

<style scoped lang="css">
/* 容器 */
.news-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-4);
  padding-top: 90px;
}

/* 工具栏 */
.news-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 250px;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  border-color: var(--border);
}

.view-toggle {
  display: flex;
  gap: var(--space-2);
}

/* 统计栏 */
.stats-bar {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius);
  margin-bottom: var(--space-6);
  font-size: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
}

.stat-item i {
  color: var(--primary);
  font-size: 16px;
}

.stat-item strong {
  color: var(--text-primary);
  font-weight: 600;
}

/* 时间线视图 */
.timeline-view {
  margin-bottom: var(--space-8);
}

.timeline-view :deep(.el-timeline) {
  padding: var(--space-4) 0;
}

.news-card-timeline {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-4);
  transition: all 0.3s ease;
}

.news-card-timeline:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.news-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.news-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.news-card-timeline:hover .news-title {
  color: var(--primary);
}

.news-content {
  margin: 0 0 var(--space-3) 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.news-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border);
  font-size: 12px;
}

.author {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-secondary);
}

.author i {
  color: var(--primary);
}

/* 网格视图 */
.grid-view {
  margin-bottom: var(--space-8);
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--space-4);
}

.news-card-grid {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.news-card-grid:hover {
  border-color: var(--primary);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.card-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: var(--bg-secondary);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.news-card-grid:hover .card-image img {
  transform: scale(1.05);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.news-card-grid:hover .card-overlay {
  opacity: 1;
}

.card-placeholder {
  width: 100%;
  height: 180px;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: var(--text-secondary);
  opacity: 0.3;
}

.card-content {
  padding: var(--space-4);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-tag {
  margin-bottom: var(--space-2);
}

.card-title {
  margin: 0 0 var(--space-2) 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
}

.news-card-grid:hover .card-title {
  color: var(--primary);
}

.card-desc {
  margin: 0 0 var(--space-3) 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.5;
  flex: 1;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border);
  font-size: 12px;
  color: var(--text-secondary);
}

.meta-date,
.meta-author {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.meta-date i,
.meta-author i {
  color: var(--primary);
}

/* 列表视图 */
.list-view {
  margin-bottom: var(--space-8);
}

.news-card-list {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  margin-bottom: var(--space-3);
  cursor: pointer;
  transition: all 0.3s ease;
}

.news-card-list:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.list-left {
  flex-shrink: 0;
}

.list-date {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-radius: var(--radius);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.date-day {
  font-size: 20px;
  line-height: 1;
}

.date-month {
  font-size: 11px;
  margin-top: 2px;
}

.list-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.list-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.news-card-list:hover .list-title {
  color: var(--primary);
}

.list-desc {
  margin: 0 0 var(--space-3) 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
  flex: 1;
}

.list-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border);
}

.footer-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: 12px;
  color: var(--text-secondary);
}

.footer-item i {
  color: var(--primary);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-8);
  color: var(--text-secondary);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

/* 详情对话框 */
.detail-content {
  padding: var(--space-4) 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border);
  margin-bottom: var(--space-4);
  font-size: 14px;
}

.detail-meta .meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-secondary);
}

.detail-meta .meta-item i {
  color: var(--primary);
}

.detail-text {
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 响应式 */
@media (max-width: 768px) {
  .news-container {
    padding: var(--space-2);
  }

  .news-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: auto;
  }

  .view-toggle {
    width: 100%;
    justify-content: space-between;
  }

  .stats-bar {
    flex-direction: column;
    gap: var(--space-2);
  }

  .news-grid {
    grid-template-columns: 1fr;
  }

  .news-card-list {
    flex-direction: column;
  }

  .list-date {
    width: 100%;
    height: auto;
    padding: var(--space-2);
  }

  .date-day {
    font-size: 18px;
  }

  .date-month {
    margin-top: 4px;
  }
}

/* 详情弹窗样式 */
.dialog-content {
  padding: 0;
}

.dialog-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 2px solid var(--border);
}

.meta-date-box {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #C02C38 0%, #8E2028 100%);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.meta-day {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.meta-month {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.9;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.meta-author {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 14px;
}

.meta-author i {
  color: #C02C38;
}

.dialog-body {
  max-height: 60vh;
  overflow-y: auto;
}

.dialog-body .content-text {
  font-size: 15px;
  line-height: 2;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

