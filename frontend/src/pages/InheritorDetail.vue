<template>
  <div class="page-section" v-if="item">
    <div class="container">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-left">
          <div class="avatar-box" :class="{ 'no-avatar': !item.avatarUrl }">
            <img v-if="item.avatarUrl" :src="item.avatarUrl" />
            <div v-else class="avatar-placeholder">
              <span>{{ item.name?.substring(0, 1) }}</span>
            </div>
          </div>
        </div>
        <div class="profile-right">
          <div class="profile-name">
            <h1>{{ item.name }}</h1>
            <el-tag effect="dark" type="warning" class="level-tag">{{ item.level }}</el-tag>
          </div>
          <div class="profile-meta">
            <span><el-icon><Location /></el-icon> {{ item.region }}</span>
            <span><el-icon><Trophy /></el-icon> 代表性传承人</span>
          </div>
          <div class="profile-bio">{{ item.bio }}</div>
          <div class="skills-list">
            <span class="skill-badge" v-for="skill in getSkills(item.skills)" :key="skill">{{ skill }}</span>
          </div>
        </div>
      </div>

      <!-- Media & Works -->
      <div class="section-title">
        <h2>相关作品与影像</h2>
      </div>
      
      <div class="media-grid" v-if="media.length">
        <div class="media-card" v-for="m in media" :key="m.id" @click="openMedia(m)">
          <div class="media-img">
            <img :src="m.coverUrl || m.url" v-if="m.type === 'image' || m.type === 'video'" />
            <div class="play-icon" v-if="m.type === 'video'"><el-icon><VideoPlay /></el-icon></div>
          </div>
          <div class="media-info">
            <h3>{{ m.title || '未命名资料' }}</h3>
            <span class="media-type">{{ m.type === 'image' ? '图片' : '视频' }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-media">
        <el-empty description="暂无相关作品与影像资料" :image-size="120" />
      </div>
    </div>

    <!-- Media Viewer -->
    <el-dialog v-model="showMediaViewer" width="800px" destroy-on-close>
      <img v-if="currentMedia?.type === 'image'" :src="currentMedia.url" style="width:100%" />
      <video v-else-if="currentMedia?.type === 'video'" :src="currentMedia.url" controls style="width:100%"></video>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '../api/http'
import { Location, Trophy, VideoPlay } from '@element-plus/icons-vue'

const route = useRoute()
const item = ref<any>(null)
const media = ref<any[]>([])
const showMediaViewer = ref(false)
const currentMedia = ref<any>(null)

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    item.value = await http.get(`/catalog/inheritors/${id}`).then(r => r.data).catch(() => null)
    media.value = await http.get(`/catalog/media/inheritor/${id}`).then(r => r.data).catch(() => [])
  } catch (e) { console.error(e) }
})

function getSkills(str: string) {
  if (!str) return []
  return str.split(/[,、，]/)
}

function openMedia(m: any) {
  currentMedia.value = m
  showMediaViewer.value = true
}
</script>

<style scoped>
.page-section {
  min-height: 100vh;
  background-color: #f8fafc;
  padding: 40px 0;
  padding-top: 140px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Profile Header */
.profile-header {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  display: flex;
  gap: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  margin-top: 70px;
  margin-bottom: 40px;
}

.profile-left {
  flex-shrink: 0;
}

.avatar-box {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  border: 4px solid #fff;
}

.avatar-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #c23531 0%, #e74c3c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder span {
  font-size: 72px;
  font-weight: 700;
  color: #fff;
  font-family: "Noto Serif SC", serif;
}

.profile-right {
  flex: 1;
}

.profile-name {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.profile-name h1 {
  margin: 0;
  font-size: 32px;
  font-family: "Noto Serif SC", serif;
  color: #1e293b;
}

.level-tag {
  background-color: #c23531;
  border-color: #c23531;
}

.profile-meta {
  display: flex;
  gap: 24px;
  color: #64748b;
  margin-bottom: 24px;
  font-size: 14px;
}

.profile-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.profile-bio {
  font-size: 16px;
  line-height: 1.8;
  color: #334155;
  margin-bottom: 24px;
}

.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.skill-badge {
  background: #fdf2f2;
  color: #c23531;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  border: 1px solid #fee2e2;
}

/* Section Title */
.section-title {
  margin-bottom: 24px;
  border-left: 4px solid #c23531;
  padding-left: 16px;
}

.section-title h2 {
  margin: 0;
  font-size: 24px;
  color: #1e293b;
}

/* Media Grid */
.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.media-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.media-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.media-img {
  height: 160px;
  position: relative;
  background: #f1f5f9;
}

.media-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
}

.media-info {
  padding: 12px;
}

.media-info h3 {
  margin: 0 0 4px;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.media-type {
  font-size: 12px;
  color: #94a3b8;
}

.empty-media {
  background: #fff;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .profile-header { flex-direction: column; padding: 24px; }
  .avatar-box { width: 120px; height: 120px; margin: 0 auto; }
  .profile-right { text-align: center; }
  .profile-name { justify-content: center; }
  .profile-meta { justify-content: center; }
  .skills-list { justify-content: center; }
}
</style>
