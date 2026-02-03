<template>
  <div class="page-section">
    <PageBanner 
      title="视听空间" 
      sub="光影记录非遗，声音传承记忆"
      pageKey="videos"
      image="https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=1600&q=80"
    />

    <div class="container">
      <!-- 左侧分类 + 右侧内容 -->
      <div class="main-layout">
        <!-- 左侧边栏 -->
        <div class="sidebar">
          <div class="sidebar-section">
            <h3>视频分类</h3>
            <ul class="category-list">
              <li :class="{ active: !currentCategory }" @click="currentCategory = ''">全部视频</li>
              <li :class="{ active: currentCategory === 'documentary' }" @click="currentCategory = 'documentary'">非遗纪录片</li>
              <li :class="{ active: currentCategory === 'craft' }" @click="currentCategory = 'craft'">技艺展示</li>
              <li :class="{ active: currentCategory === 'interview' }" @click="currentCategory = 'interview'">传承人访谈</li>
              <li :class="{ active: currentCategory === 'activity' }" @click="currentCategory = 'activity'">活动记录</li>
              <li :class="{ active: currentCategory === 'tutorial' }" @click="currentCategory = 'tutorial'">教学视频</li>
            </ul>
          </div>
          <div class="sidebar-section">
            <h3>热门标签</h3>
            <div class="tag-cloud">
              <span 
                class="tag" 
                :class="{ active: currentTag === tag }"
                v-for="tag in tags" 
                :key="tag"
                @click="toggleTag(tag)"
              >{{ tag }}</span>
            </div>
          </div>
        </div>

        <!-- 右侧内容 -->
        <div class="content-area">
          <div class="content-header">
            <div class="result-info">
              共 <span class="count">{{ filteredVideos.length }}</span> 个视频
            </div>
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索视频..." 
              prefix-icon="Search" 
              style="width: 240px" 
              clearable 
            />
          </div>

          <div class="video-grid" v-if="filteredVideos.length > 0">
            <div class="video-card" v-for="v in filteredVideos" :key="v.id" @click="goToDetail(v.id)">
              <div class="video-cover">
                <img :src="v.coverUrl || v.cover || defaultCover" />
                <div class="play-btn">
                  <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                </div>
                <div class="video-duration" v-if="v.duration">{{ formatDuration(v.duration) }}</div>
                <div class="video-status" v-if="v.status === 'queued'">转码中...</div>
              </div>
              <div class="video-info">
                <h4 class="video-title">{{ v.title || '未命名视频' }}</h4>
                <div class="video-meta">
                  <span class="views"><el-icon><View /></el-icon> {{ v.viewCount || 0 }}</span>
                  <span class="date">{{ formatDate(v.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无视频" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { Search, View } from '@element-plus/icons-vue'
import PageBanner from '../components/PageBanner.vue'

const router = useRouter()
const videos = ref<any[]>([])
const searchKeyword = ref('')
const currentCategory = ref('')
const currentTag = ref('')
const tags = ['传统技艺', '民间艺术', '非遗传承', '手工艺', '文化遗产']
const defaultCover = 'https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800&q=80'

onMounted(async () => {
  try {
    const { data } = await http.get('/media/videos')
    videos.value = data || []
  } catch (e) { console.error(e) }
})

const filteredVideos = computed(() => {
  let list = videos.value
  if (searchKeyword.value) {
    list = list.filter(v => v.title?.includes(searchKeyword.value))
  }
  if (currentCategory.value) {
    list = list.filter(v => v.category === currentCategory.value)
  }
  if (currentTag.value) {
    // 按实际tags字段过滤（tags是逗号分隔的字符串）
    list = list.filter(v => v.tags?.split(',').includes(currentTag.value))
  }
  return list
})

function toggleTag(tag: string) {
  currentTag.value = currentTag.value === tag ? '' : tag
}

function goToDetail(id: number) {
  router.push(`/videos/${id}`)
}

function formatDuration(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function formatDate(d: string | Date | undefined) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.page-section {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.main-layout {
  display: flex;
  gap: 24px;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
}

.sidebar-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.sidebar-section h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li {
  padding: 10px 12px;
  color: #666;
  cursor: pointer;
  border-radius: 6px;
  margin-bottom: 4px;
  transition: all 0.3s;
  font-size: 14px;
}

.category-list li:hover {
  background: #f5f5f5;
  color: #333;
}

.category-list li.active {
  background: linear-gradient(135deg, #c23531 0%, #d4574e 100%);
  color: #fff;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 10px;
  background: #f5f5f5;
  color: #666;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.tag:hover {
  background: #c23531;
  color: #fff;
}

.tag.active {
  background: #c23531;
  color: #fff;
}

.content-area {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.result-info {
  font-size: 14px;
  color: #666;
}

.result-info .count {
  color: #c23531;
  font-weight: 600;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.video-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.video-cover {
  position: relative;
  aspect-ratio: 16/9;
  background: #000;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s;
}

.video-card:hover .video-cover img {
  opacity: 0.9;
}

.play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s;
}

.play-btn svg {
  width: 20px;
  height: 20px;
  margin-left: 2px;
}

.video-card:hover .play-btn {
  opacity: 1;
  background: rgba(194, 53, 49, 0.85);
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0,0,0,0.75);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.video-status {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(255, 152, 0, 0.9);
  color: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.video-info {
  padding: 14px;
}

.video-title {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.video-meta .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 992px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
