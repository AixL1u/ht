<template>
  <div class="video-detail-page">
    <!-- 顶部导航栏 -->
    <div class="page-nav">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <router-link to="/videos">视听空间</router-link>
      <span class="sep">/</span>
      <span class="current">视频详情</span>
    </div>

    <!-- 标题区 -->
    <div class="video-header">
      <h1>{{ video?.title || '视频详情' }}</h1>
      <p class="video-meta">
        <el-icon><Clock /></el-icon>
        创建时间：{{ formatDate(video?.createdAt) }}
      </p>
    </div>

    <!-- 主内容区 -->
    <div class="video-main">
      <div class="video-content">
        <!-- 左侧播放器 -->
        <div class="player-section">
          <div class="player-wrapper">
            <video ref="videoRef" controls :poster="video?.coverUrl" class="video-player"></video>
            <div class="play-overlay" v-if="!isPlaying" @click="startPlay">
              <div class="play-btn">
                <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="info-section">
          <h2>{{ video?.title }}</h2>
          <div class="info-item">
            <el-icon><Film /></el-icon>
            <span>视频来源：非遗文化保护中心</span>
          </div>
          <div class="info-item">
            <el-icon><View /></el-icon>
            <span>播放量：{{ video?.viewCount || 0 }}</span>
          </div>
          <div class="info-item" v-if="video?.duration">
            <el-icon><Timer /></el-icon>
            <span>时长：{{ formatDuration(video.duration) }}</span>
          </div>
          <div class="info-item" v-if="video?.size">
            <el-icon><Document /></el-icon>
            <span>大小：{{ formatSize(video.size) }}</span>
          </div>
          <div class="video-desc" v-if="video?.description">
            <p>{{ video.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 其他资源 -->
    <div class="related-section" v-if="relatedVideos.length > 0">
      <div class="section-header">
        <h3>其他资源</h3>
      </div>
      <div class="related-grid">
        <div 
          class="related-card" 
          v-for="v in relatedVideos" 
          :key="v.id"
          @click="goToVideo(v.id)"
        >
          <div class="related-cover">
            <img :src="v.coverUrl || defaultCover" :alt="v.title" />
            <div class="play-icon">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
            </div>
          </div>
          <p class="related-title">{{ v.title || '未命名视频' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { Clock, Film, Timer, Document, View } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const video = ref<any>(null)
const relatedVideos = ref<any[]>([])
const videoRef = ref<HTMLVideoElement | null>(null)
const isPlaying = ref(false)
let hlsInstance: any = null

const defaultCover = 'https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800&q=80'

onMounted(() => {
  loadVideo()
})

onUnmounted(() => {
  destroyPlayer()
})

watch(() => route.params.id, () => {
  destroyPlayer()
  loadVideo()
})

async function loadVideo() {
  const id = route.params.id
  try {
    const { data: list } = await http.get('/media/videos')
    const all = list || []
    video.value = all.find((v: any) => v.id == id)
    relatedVideos.value = all.filter((v: any) => v.id != id).slice(0, 6)
    
    // 增加浏览量
    if (id) {
      http.post(`/media/videos/${id}/view`).catch(() => {})
    }
  } catch (e) {
    console.error(e)
  }
}

function startPlay() {
  isPlaying.value = true
  initPlayer()
}

async function initPlayer() {
  const el = videoRef.value
  const v = video.value
  if (!el || !v) return

  const hlsUrl = v.hlsUrl
  const originalUrl = v.url

  if (hlsUrl && hlsUrl.includes('.m3u8')) {
    const Hls = await ensureHls()
    if (Hls && Hls.isSupported()) {
      hlsInstance = new Hls()
      hlsInstance.loadSource(hlsUrl)
      hlsInstance.attachMedia(el)
      hlsInstance.on(Hls.Events.MANIFEST_PARSED, () => {
        el.play().catch(() => {})
      })
    } else if (el.canPlayType('application/vnd.apple.mpegurl')) {
      el.src = hlsUrl
      el.play().catch(() => {})
    }
  } else if (originalUrl) {
    el.src = originalUrl
    el.play().catch(() => {})
  }
}

async function ensureHls(): Promise<any> {
  const w = window as any
  if (w.Hls) return w.Hls
  return new Promise((resolve) => {
    const s = document.createElement('script')
    s.src = 'https://cdn.jsdelivr.net/npm/hls.js@latest'
    s.onload = () => resolve((window as any).Hls)
    s.onerror = () => resolve(null)
    document.head.appendChild(s)
  })
}

function destroyPlayer() {
  if (hlsInstance) {
    hlsInstance.destroy()
    hlsInstance = null
  }
  if (videoRef.value) {
    videoRef.value.pause()
    videoRef.value.src = ''
  }
  isPlaying.value = false
}

function goToVideo(id: number) {
  router.push(`/videos/${id}`)
}

function formatDate(d: string | Date | undefined) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('zh-CN')
}

function formatDuration(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}分${s}秒`
}

function formatSize(bytes: number) {
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<style scoped>
.video-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 80px;
}

.page-nav {
  max-width: 1100px;
  margin: 0 auto;
  padding: 14px 24px;
  font-size: 14px;
  color: #666;
}

.page-nav a {
  color: #666;
  text-decoration: none;
  transition: color 0.3s;
}

.page-nav a:hover {
  color: #c23531;
}

.page-nav .sep {
  margin: 0 8px;
  color: #ccc;
}

.page-nav .current {
  color: #333;
}

.video-header {
  text-align: center;
  padding: 30px 20px 25px;
  background: #fff;
  max-width: 1100px;
  margin: 50px auto 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.video-header h1 {
  font-size: 22px;
  color: #333;
  margin: 0 0 10px;
  font-weight: 600;
}

.video-meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 13px;
  margin: 0;
}

.video-main {
  max-width: 1100px;
  margin: 0 auto;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.video-content {
  display: flex;
  gap: 24px;
}

.player-section {
  flex: 1;
  min-width: 0;
}

.player-wrapper {
  position: relative;
  background: #000;
  border-radius: 6px;
  overflow: hidden;
  aspect-ratio: 16/9;
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: rgba(0,0,0,0.3);
}

.play-btn {
  width: 64px;
  height: 64px;
  background: rgba(255,255,255,0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.play-btn svg {
  width: 28px;
  height: 28px;
  color: #2c3e50;
  margin-left: 3px;
}

.play-overlay:hover .play-btn {
  transform: scale(1.1);
}

.info-section {
  width: 240px;
  flex-shrink: 0;
  color: #fff;
}

.info-section h2 {
  font-size: 16px;
  margin: 0 0 16px;
  line-height: 1.5;
  font-weight: 500;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(255,255,255,0.8);
  margin-bottom: 10px;
}

.info-item .el-icon {
  font-size: 14px;
  opacity: 0.7;
}

.video-desc {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.15);
}

.video-desc p {
  font-size: 13px;
  line-height: 1.7;
  color: rgba(255,255,255,0.75);
  margin: 0;
}

.related-section {
  max-width: 1100px;
  margin: 0 auto;
  padding: 30px 0 50px;
}

.section-header h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 20px;
  font-weight: 600;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.related-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.related-card:hover {
  transform: translateY(-4px);
}

.related-cover {
  position: relative;
  aspect-ratio: 16/9;
  border-radius: 6px;
  overflow: hidden;
  background: #000;
}

.related-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s;
}

.related-card:hover .related-cover img {
  opacity: 0.85;
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background: rgba(0,0,0,0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.related-card:hover .play-icon {
  opacity: 1;
}

.play-icon svg {
  width: 18px;
  height: 18px;
  color: #fff;
  margin-left: 2px;
}

.related-title {
  font-size: 13px;
  color: #333;
  margin: 8px 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .video-main {
    margin: 0 16px;
    padding: 20px;
  }
  .video-content {
    flex-direction: column;
  }
  .info-section {
    width: 100%;
  }
  .related-section {
    padding: 30px 16px 50px;
  }
  .related-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
