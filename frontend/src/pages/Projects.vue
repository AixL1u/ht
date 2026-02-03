<template>
  <div class="page-section">
    <PageBanner 
      title="非遗名录" 
      sub="汇集中华大地非物质文化遗产，探寻民族记忆"
      pageKey="projects"
      image="https://images.unsplash.com/photo-1599666672232-7228da72d677?w=1600&q=80"
    />

    <div class="container">
      <!-- Filter Bar -->
      <div class="filter-bar">
        <div class="search-box">
          <el-input 
            v-model="query.keyword" 
            placeholder="搜索项目名称..." 
            prefix-icon="Search" 
            clearable 
            @clear="load"
            @keyup.enter="load"
          />
        </div>
        <div class="filter-group">
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable @change="load" style="width: 160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
      </div>

      <!-- Project Grid -->
      <div v-if="projects.length > 0" class="project-grid">
        <div class="project-card" v-for="p in projects" :key="p.id" @click="go(p.id)">
          <div class="card-image">
            <img :src="p.coverImage || 'https://images.unsplash.com/photo-1599666672232-7228da72d677?w=800&q=80'" loading="lazy" />
            <div class="card-overlay">
              <span class="view-btn">查看详情</span>
            </div>
            <span class="category-tag">{{ getCategoryName(p.categoryId) }}</span>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ p.name }}</h3>
            <div class="card-meta">
              <span v-if="p.region"><el-icon><Location /></el-icon> {{ p.region }}</span>
              <span v-if="p.level"> · {{ p.level }}</span>
            </div>
            <p class="card-intro">{{ p.intro }}</p>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <el-empty description="暂无相关项目" />
      </div>

      <!-- Pagination (Client-side simulation or backend) -->
      <div class="pagination-wrapper" v-if="total > query.size">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="query.size"
          v-model:current-page="query.page"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { Location, Search } from '@element-plus/icons-vue'
import PageBanner from '../components/PageBanner.vue'

const router = useRouter()
const projects = ref<any[]>([])
const categories = ref<any[]>([])
const total = ref(0)
const query = ref({
  page: 1,
  size: 12,
  keyword: '',
  categoryId: ''
})

onMounted(async () => {
  loadCategories()
  load()
})

async function loadCategories() {
  try {
    const { data } = await http.get('/catalog/categories')
    categories.value = data || []
  } catch (e) { console.error(e) }
}

async function load() {
  try {
    const params: any = {
      page: query.value.page - 1,
      size: query.value.size
    }
    if (query.value.keyword) params.keyword = query.value.keyword
    if (query.value.categoryId) params.categoryId = query.value.categoryId

    const { data } = await http.get('/catalog/projects/search', { params })
    // Search returns Page object
    if (data && data.content) {
      projects.value = data.content
      total.value = data.totalElements
    } else {
      // Fallback if backend returns List
      projects.value = Array.isArray(data) ? data : []
      total.value = projects.value.length
    }
  } catch (e) {
    console.error(e)
  }
}

function getCategoryName(id: number) {
  const cat = categories.value.find(c => c.id === id)
  return cat ? cat.name : '非遗'
}

function go(id: number) {
  router.push(`/projects/${id}`)
}
</script>

<style scoped>
.page-section {
  min-height: 100vh;
  background-color: #f8fafc;
  padding-bottom: 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.filter-group {
  display: flex;
  gap: 12px;
}

/* Grid */
.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 32px;
}

.project-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  border: 1px solid rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
}

.project-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.12);
  border-color: rgba(0,0,0,0.05);
}

.card-image {
  height: 200px;
  position: relative;
  overflow: hidden;
}

.card-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.3), transparent 30%);
  opacity: 0;
  transition: opacity 0.3s;
}

.project-card:hover .card-image::after {
  opacity: 1;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.project-card:hover .card-image img {
  transform: scale(1.1);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  backdrop-filter: blur(2px);
  z-index: 2;
}

.project-card:hover .card-overlay {
  opacity: 1;
}

.view-btn {
  color: #fff;
  border: 1px solid rgba(255,255,255,0.8);
  padding: 8px 24px;
  border-radius: 30px;
  font-size: 14px;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(4px);
  font-weight: 500;
  letter-spacing: 1px;
}

.category-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  left: auto;
  background: rgba(255,255,255,0.95);
  color: #333;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  z-index: 3;
}

.card-content {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-intro {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 0;
}

.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .filter-bar { flex-direction: column; align-items: stretch; }
  .search-box { max-width: none; }
  .filter-group { width: 100%; }
  .filter-group .el-select { flex: 1; }
}
</style>
