<template>
  <div class="detail-page" v-if="project">
    <!-- Hero Header -->
    <div class="detail-hero" :style="{ backgroundImage: `url(${project.coverImage || 'https://images.unsplash.com/photo-1599666672232-7228da72d677?w=1600&q=80'})` }">
      <div class="hero-overlay"></div>
      <div class="hero-content container">
        <el-tag effect="dark" class="category-tag" color="#c23531">{{ categoryMap[project.categoryId] || '非遗' }}</el-tag>
        <h1 class="project-title">{{ project.name }}</h1>
        <p class="project-intro">{{ project.intro }}</p>
        <div class="project-meta">
          <span><el-icon><Location /></el-icon> {{ project.region }}</span>
          <span><el-icon><User /></el-icon> {{ project.level || '国家级' }}</span>
          <span><el-icon><Clock /></el-icon> 申报时间：{{ new Date(project.createdAt).getFullYear() }}年</span>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <el-row :gutter="40">
        <!-- Left Column: Content -->
        <el-col :span="16">
          <div class="content-block" v-if="project.history">
            <div class="block-header">
              <h2>历史渊源</h2>
            </div>
            <div class="text-content">{{ project.history }}</div>
          </div>

          <div class="content-block" v-if="project.craftsmanship">
            <div class="block-header">
              <h2>工艺特点</h2>
            </div>
            <div class="text-content">{{ project.craftsmanship }}</div>
          </div>

          <div class="content-block" v-if="project.inheritance">
            <div class="block-header">
              <h2>传承现状</h2>
            </div>
            <div class="text-content">{{ project.inheritance }}</div>
          </div>

          <!-- Media Gallery -->
          <div class="content-block" v-if="media.length">
            <div class="block-header">
              <h2>影像资料</h2>
            </div>
            <div class="media-grid">
              <div class="media-item" v-for="m in media" :key="m.id" @click="openMedia(m)">
                <div class="media-cover">
                  <img :src="m.coverUrl || m.url" v-if="m.type === 'image' || m.type === 'video'" />
                  <div class="play-icon" v-if="m.type === 'video'"><el-icon><VideoPlay /></el-icon></div>
                </div>
                <div class="media-title">{{ m.title }}</div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- Right Column: Sidebar -->
        <el-col :span="8">
          <!-- Inheritors -->
          <div class="sidebar-block">
            <div class="sidebar-header">代表性传承人</div>
            <div class="inheritor-list" v-if="inheritors.length">
              <div class="inheritor-item-sm" v-for="i in inheritors" :key="i.id" @click="$router.push(`/inheritors/${i.id}`)">
                <el-avatar :size="48" :src="i.avatarUrl || `https://ui-avatars.com/api/?name=${i.name}&background=random`" />
                <div class="info">
                  <div class="name">{{ i.name }}</div>
                  <div class="level">{{ i.level }}</div>
                </div>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
            <el-empty v-else description="暂无记录" :image-size="60" />
          </div>

          <!-- Related -->
          <div class="sidebar-block" v-if="relatedProjects.length">
            <div class="sidebar-header">相关推荐</div>
            <div class="related-list">
              <div class="related-item" v-for="rp in relatedProjects" :key="rp.id" @click="$router.push(`/projects/${rp.id}`)">
                <img :src="rp.coverImage || 'https://images.unsplash.com/photo-1599666672232-7228da72d677?w=200&q=80'" />
                <div class="info">
                  <div class="name">{{ rp.name }}</div>
                  <div class="cat">{{ categoryMap[rp.categoryId] || '非遗' }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Media Viewer -->
    <el-dialog v-model="showMediaViewer" width="800px" destroy-on-close>
      <img v-if="currentMedia?.type === 'image'" :src="currentMedia.url" style="width:100%" />
      <video v-else-if="currentMedia?.type === 'video'" :src="currentMedia.url" controls style="width:100%"></video>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import http from '../api/http'
import { Location, User, Clock, VideoPlay, ArrowRight } from '@element-plus/icons-vue'

const route = useRoute()
const project = ref<any>(null)
const inheritors = ref<any[]>([])
const media = ref<any[]>([])
const relatedProjects = ref<any[]>([])
const categoryMap = ref<Record<number, string>>({})
const showMediaViewer = ref(false)
const currentMedia = ref<any>(null)

async function load(id: number) {
  try {
    // Load categories for mapping
    const catsRes = await http.get('/catalog/categories')
    const cats = catsRes.data || []
    cats.forEach((c: any) => categoryMap.value[c.id] = c.name)

    project.value = await http.get(`/catalog/projects/${id}`).then(r => r.data).catch(() => null)
    
    // Inheritors
    const links = await http.get(`/catalog/projects/${id}/inheritors`).then(r => r.data).catch(() => [])
    const ids = (links || []).map((x: any) => x.inheritorId)
    if (ids.length > 0) {
      const responses = await Promise.all(ids.map((iid: number) => http.get(`/catalog/inheritors/${iid}`).catch(() => null)))
      inheritors.value = responses.map(r => r?.data).filter(Boolean)
    } else {
      inheritors.value = []
    }

    media.value = await http.get(`/catalog/media/project/${id}`).then(r => r.data).catch(() => [])
    relatedProjects.value = await http.get(`/catalog/projects/${id}/related`).then(r => r.data).catch(() => [])
  } catch (e) {
    console.error(e)
  }
}

function openMedia(m: any) {
  currentMedia.value = m
  showMediaViewer.value = true
}

onMounted(() => {
  if (route.params.id) load(Number(route.params.id))
})

watch(() => route.params.id, (newId) => {
  if (newId) {
    window.scrollTo(0, 0)
    load(Number(newId))
  }
})
</script>

<style scoped>
.detail-page {
  background: #fff;
  min-height: 100vh;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Hero */
.detail-hero {
  height: 450px;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: flex-end;
  margin-bottom: 40px;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1), rgba(0,0,0,0.8));
}

.hero-content {
  position: relative;
  z-index: 2;
  width: 100%;
  padding-bottom: 40px;
  color: #fff;
}

.category-tag {
  border: none;
  margin-bottom: 12px;
}

.project-title {
  font-size: 42px;
  margin: 0 0 16px;
  font-family: "Noto Serif SC", serif;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.project-intro {
  font-size: 16px;
  opacity: 0.9;
  max-width: 700px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.project-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  opacity: 0.8;
}

.project-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Content */
.main-content {
  padding-bottom: 60px;
}

.content-block {
  margin-bottom: 40px;
}

.block-header {
  border-bottom: 1px solid #eee;
  padding-bottom: 12px;
  margin-bottom: 20px;
}

.block-header h2 {
  font-size: 24px;
  color: #1e293b;
  margin: 0;
  border-left: 4px solid var(--brand);
  padding-left: 12px;
}

.text-content {
  font-size: 16px;
  line-height: 1.8;
  color: #334155;
  white-space: pre-wrap;
}

/* Media Grid */
.media-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.media-item {
  cursor: pointer;
}

.media-cover {
  height: 120px;
  background: #f1f5f9;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  margin-bottom: 8px;
}

.media-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.media-item:hover img {
  transform: scale(1.05);
}

.play-icon {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.3);
  color: #fff;
  font-size: 32px;
  opacity: 0;
  transition: opacity 0.3s;
}

.media-item:hover .play-icon {
  opacity: 1;
}

.media-title {
  font-size: 13px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Sidebar */
.sidebar-block {
  background: #f8fafc;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.sidebar-header {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
}

.inheritor-item-sm {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.inheritor-item-sm:hover {
  border-color: var(--brand);
  transform: translateX(2px);
}

.inheritor-item-sm .info {
  flex: 1;
}

.inheritor-item-sm .name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.inheritor-item-sm .level {
  font-size: 12px;
  color: #c23531;
  margin-top: 2px;
}

/* Related */
.related-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  cursor: pointer;
}

.related-item img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.related-item .info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.related-item .name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.4;
}

.related-item .cat {
  font-size: 12px;
  color: #94a3b8;
}

.related-item:hover .name {
  color: var(--brand);
}
</style>
