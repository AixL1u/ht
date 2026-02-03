<template>
  <div class="home-page">
    <!-- 欢迎通知条 -->
    <transition name="welcome-slide">
      <div v-if="showWelcome" class="welcome-toast" :class="welcomeIcon" @click="showWelcome = false">
        <span class="welcome-icon">
          <svg v-if="welcomeIcon === 'sun'" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="5" fill="currentColor"/>
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" fill="currentColor"/>
          </svg>
        </span>
        <span class="welcome-text">{{ welcomeText }}</span>
        <span class="welcome-close">×</span>
      </div>
    </transition>

    <!-- 1. Hero Banner -->
    <div class="hero-section">
      <el-carousel height="520px" arrow="hover" :interval="6000">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <div class="banner-slide" :style="{ backgroundImage: `url(${b.imageUrl})` }">
            <div class="banner-content">
              <h1 class="animate__animated animate__fadeInDown">传承非遗文化</h1>
              <p class="animate__animated animate__fadeInUp">守护中华文明的根脉，见证历史的活化石</p>
              <router-link to="/projects">
                <el-button type="primary" size="large" round class="explore-btn animate__animated animate__fadeInUp">立即探索</el-button>
              </router-link>
            </div>
            <div class="banner-mask"></div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 2. 统计数据 -->
    <div class="stats-bar">
      <div class="container">
        <el-row :gutter="40">
          <el-col :span="6" v-for="(stat, idx) in stats" :key="idx">
            <div class="stat-item">
              <div class="stat-icon"><component :is="stat.icon" /></div>
              <div class="stat-info">
                <div class="stat-num">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 3. 三栏资讯区域 -->
    <div class="section-block news-section bg-light">
      <div class="container">
        <el-row :gutter="24">
          <!-- 新闻动态 -->
          <el-col :span="8">
            <div class="news-panel">
              <div class="panel-header">
                <div class="panel-title">
                  <span class="title-icon">◆</span>
                  <span>新闻动态</span>
                </div>
                <router-link to="/news" class="panel-more">更多 <el-icon><ArrowRight /></el-icon></router-link>
              </div>
              <div class="news-featured" v-if="featuredNews">
                <div class="featured-img" @click="viewNewsDetail(featuredNews)">
                  <img :src="featuredNews.coverImage || 'https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=400'" />
                  <div class="featured-overlay">
                    <h4>{{ featuredNews.title }}</h4>
                  </div>
                </div>
              </div>
              <ul class="news-list">
                <li v-for="n in newsList" :key="n.id" @click="viewNewsDetail(n)">
                  <span class="news-title">{{ n.title }}</span>
                  <span class="news-date">{{ formatDate(n.publishTime) }}</span>
                </li>
              </ul>
            </div>
          </el-col>

          <!-- 热门活动 -->
          <el-col :span="8">
            <div class="news-panel">
              <div class="panel-header">
                <div class="panel-title">
                  <span class="title-icon">◆</span>
                  <span>热门活动</span>
                </div>
                <router-link to="/activities" class="panel-more">更多 <el-icon><ArrowRight /></el-icon></router-link>
              </div>
              <div class="activity-list">
                <div class="activity-item" v-for="a in featuredActivities" :key="a.id" @click="$router.push(`/activities/${a.id}`)">
                  <div class="activity-date">
                    <span class="day">{{ new Date(a.startTime).getDate() }}</span>
                    <span class="month">{{ new Date(a.startTime).getMonth() + 1 }}月</span>
                  </div>
                  <div class="activity-info">
                    <h5>{{ a.title }}</h5>
                    <p><el-icon><Location /></el-icon> {{ a.location }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 通知公告 -->
          <el-col :span="8">
            <div class="news-panel">
              <div class="panel-header">
                <div class="panel-title">
                  <span class="title-icon">◆</span>
                  <span>通知公告</span>
                </div>
                <router-link to="/news" class="panel-more">更多 <el-icon><ArrowRight /></el-icon></router-link>
              </div>
              <ul class="notice-list">
                <li v-for="n in noticeList" :key="n.id" @click="viewNewsDetail(n)">
                  <span class="notice-tag">{{ n.type === 'notice' ? '公告' : '通知' }}</span>
                  <span class="notice-title">{{ n.title }}</span>
                  <span class="notice-date">{{ formatDate(n.publishTime) }}</span>
                </li>
              </ul>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 4. 精选非遗项目 -->
    <div class="section-block bg-white">
      <div class="container">
        <div class="section-header">
          <h2>非遗项目</h2>
          <p>探寻传统工艺，感受匠心独运</p>
          <router-link to="/projects" class="more-link">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
        </div>
        <div class="project-grid">
          <div class="project-card" v-for="p in featuredProjects" :key="p.id" @click="$router.push(`/projects/${p.id}`)">
            <div class="card-img">
              <img :src="p.coverImage || 'https://images.unsplash.com/photo-1535083252457-6080fe29be45?w=800&q=80'" />
              <div class="card-tag">{{ p.categoryName }}</div>
            </div>
            <div class="card-body">
              <h3>{{ p.name }}</h3>
              <div class="card-meta">
                <span><el-icon><Location /></el-icon> {{ p.region }}</span>
                <span><el-icon><User /></el-icon> {{ p.level || '国家级' }}</span>
              </div>
              <p class="card-desc">{{ p.intro }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 5. 传承人风采 -->
    <div class="section-block bg-gray">
      <div class="container">
        <div class="section-header">
          <h2>传承人风采</h2>
          <p>薪火相传，一代宗师</p>
          <router-link to="/inheritors" class="more-link">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
        </div>
        <div class="inheritor-grid">
          <div class="inheritor-card" v-for="i in featuredInheritors" :key="i.id" @click="$router.push(`/inheritors/${i.id}`)">
            <div class="inheritor-avatar">
              <img v-if="i.avatarUrl" :src="i.avatarUrl" />
              <div v-else class="avatar-placeholder">{{ i.name.charAt(0) }}</div>
            </div>
            <div class="inheritor-info">
              <h4>{{ i.name }}</h4>
              <span class="level-tag">{{ i.level }}</span>
              <p>{{ i.skills }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 悬浮留言按钮 -->
    <div class="floating-message-btn" @click="$router.push('/forum')">
      <div class="message-icon">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z"/>
          <path d="M7 9h10v2H7zm0-3h10v2H7z"/>
        </svg>
      </div>
      <span class="message-text">我要留言</span>
    </div>

    <!-- 新闻详情弹窗 -->
    <el-dialog 
      v-model="newsDialogVisible" 
      :title="selectedNews?.title"
      width="800px"
      append-to-body
      destroy-on-close
    >
      <div class="news-dialog-content">
        <div class="news-dialog-meta">
          <div class="meta-date-box">
            <div class="meta-day">{{ getDay(selectedNews?.publishTime) }}</div>
            <div class="meta-month">{{ getMonth(selectedNews?.publishTime) }}</div>
          </div>
          <div class="meta-info">
            <el-tag size="small">{{ selectedNews?.type || '资讯' }}</el-tag>
            <span class="meta-author">
              <el-icon><User /></el-icon> {{ selectedNews?.author || '管理员' }}
            </span>
          </div>
        </div>
        <div class="news-dialog-body">
          <div class="content-text">{{ selectedNews?.content }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { Collection, User, Calendar, VideoPlay, ArrowRight, Location, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const banners = ref<any[]>([])
const featuredProjects = ref<any[]>([])
const featuredInheritors = ref<any[]>([])
const featuredActivities = ref<any[]>([])
const newsList = ref<any[]>([])
const noticeList = ref<any[]>([])
const featuredNews = ref<any>(null)
const showWelcome = ref(false)
const welcomeText = ref('')
const welcomeIcon = ref('sun')
const newsDialogVisible = ref(false)
const selectedNews = ref<any>(null)

const stats = ref([
  { label: '非遗项目', value: '3,000+', icon: markRaw(Collection) },
  { label: '传承人', value: '500+', icon: markRaw(User) },
  { label: '精彩活动', value: '120+', icon: markRaw(Calendar) },
  { label: '视听资料', value: '10TB+', icon: markRaw(VideoPlay) },
])

onMounted(async () => {
  loadBanners()
  loadProjects()
  loadInheritors()
  loadActivities()
  loadNews()
  loadStats()
  
  if (localStorage.getItem('showWelcome') === 'true') {
    localStorage.removeItem('showWelcome')
    showWelcomeDialog()
  }
})

function formatDate(d: string) {
  if (!d) return ''
  const date = new Date(d)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
}

function showWelcomeDialog() {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  
  try {
    const user = JSON.parse(userStr)
    const nickname = user?.nickname || user?.username || '用户'
    
    const hour = new Date().getHours()
    let greeting = '你好'
    let iconType = 'sun'
    
    if (hour < 6) { greeting = '夜深了'; iconType = 'moon' }
    else if (hour < 9) { greeting = '早上好'; iconType = 'sun' }
    else if (hour < 12) { greeting = '上午好'; iconType = 'sun' }
    else if (hour < 14) { greeting = '中午好'; iconType = 'sun' }
    else if (hour < 18) { greeting = '下午好'; iconType = 'sun' }
    else if (hour < 22) { greeting = '晚上好'; iconType = 'moon' }
    else { greeting = '夜深了'; iconType = 'moon' }
    
    showWelcome.value = true
    welcomeText.value = `${greeting}，${nickname}！欢迎回来`
    welcomeIcon.value = iconType
    
    setTimeout(() => { showWelcome.value = false }, 4000)
  } catch (e) { console.error('Failed to show welcome', e) }
}

async function loadStats() {
  try {
    const [projRes, actRes, inheritorRes] = await Promise.all([
      http.get('/catalog/projects/search?size=1'),
      http.get('/activities/search?size=1'),
      http.get('/catalog/inheritors')
    ])
    
    stats.value[0].value = (projRes.data.totalElements || 0).toLocaleString() + '+'
    stats.value[1].value = (inheritorRes.data.length || 0).toLocaleString() + '+'
    stats.value[2].value = (actRes.data.totalElements || 0).toLocaleString() + '+'
    stats.value[3].value = '500GB+'
  } catch (e) { console.error('Failed to load stats', e) }
}

async function loadBanners() {
  try {
    const { data } = await http.get('/banners')
    if (data && data.length > 0) {
      banners.value = data
    } else {
      banners.value = [
        { id: 1, imageUrl: 'https://images.unsplash.com/photo-1535083252457-6080fe29be45?w=1600&q=80' },
        { id: 2, imageUrl: 'https://images.unsplash.com/photo-1523527940847-a0462d087996?w=1600&q=80' }
      ]
    }
  } catch (e) { console.error(e) }
}

async function loadProjects() {
  const { data } = await http.get('/catalog/projects/search?size=4')
  featuredProjects.value = data.content || []
}

async function loadInheritors() {
  const { data } = await http.get('/catalog/inheritors')
  featuredInheritors.value = (data || []).slice(0, 8)
}

async function loadActivities() {
  const { data } = await http.get('/activities/search?size=4')
  featuredActivities.value = data.content || []
}

async function loadNews() {
  try {
    const { data } = await http.get('/news')
    const all = data || []
    // 分离新闻和公告 - 根据实际的type字段值
    // 新闻类型：政策动态、演出资讯、展览信息、国际交流、教育传承、媒体报道、学术活动、赛事预告、市场动态
    // 公告类型：活动预告
    const newsTypes = ['政策动态', '演出资讯', '展览信息', '国际交流', '教育传承', '媒体报道', '学术活动', '赛事预告', '市场动态']
    const noticeTypes = ['活动预告']
    
    const news = all.filter((n: any) => newsTypes.includes(n.type))
    const notices = all.filter((n: any) => noticeTypes.includes(n.type))
    
    if (news.length > 0) {
      featuredNews.value = news[0]
      newsList.value = news.slice(1, 6)
    }
    noticeList.value = notices.slice(0, 6)
  } catch (e) { console.error('Failed to load news', e) }
}

// 查看新闻详情
function viewNewsDetail(item: any) {
  selectedNews.value = item
  newsDialogVisible.value = true
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
</script>

<style scoped>
.home-page {
  background: #fff;
  position: relative;
}

/* 欢迎通知条 */
.welcome-toast {
  position: fixed;
  top: 90px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
  color: #fff;
  padding: 12px 20px 12px 16px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.welcome-toast.sun {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 20px rgba(245, 87, 108, 0.4);
}

.welcome-toast.moon {
  background: linear-gradient(135deg, #2c3e50 0%, #4a00e0 100%);
  box-shadow: 0 4px 20px rgba(74, 0, 224, 0.4);
}

.welcome-toast.sun .welcome-icon { color: #fff700; }
.welcome-toast.moon .welcome-icon { color: #ffd700; }

.welcome-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-icon svg { width: 100%; height: 100%; }
.welcome-text { font-size: 14px; font-weight: 500; }
.welcome-close { font-size: 16px; opacity: 0.7; margin-left: 4px; }

.welcome-slide-enter-active { animation: slideDown 0.5s ease-out; }
.welcome-slide-leave-active { animation: slideUp 0.3s ease-in; }

@keyframes slideDown {
  from { opacity: 0; transform: translateX(-50%) translateY(-30px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}

@keyframes slideUp {
  from { opacity: 1; transform: translateX(-50%) translateY(0); }
  to { opacity: 0; transform: translateX(-50%) translateY(-30px); }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Hero Section */
.hero-section { position: relative; }

.banner-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.6));
  z-index: 1;
}

.banner-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  max-width: 800px;
  padding: 0 20px;
}

.banner-content h1 {
  font-size: 56px;
  font-weight: 800;
  margin-bottom: 20px;
  letter-spacing: 4px;
  text-shadow: 0 4px 12px rgba(0,0,0,0.3);
  font-family: "Noto Serif SC", serif;
}

.banner-content p {
  font-size: 20px;
  margin-bottom: 40px;
  opacity: 0.95;
  font-weight: 300;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.explore-btn {
  padding: 14px 48px;
  font-size: 18px;
  font-weight: 600;
  background: var(--brand);
  border: none;
  border-radius: 50px;
  box-shadow: 0 8px 20px rgba(194, 53, 49, 0.4);
  transition: all 0.3s;
}

.explore-btn:hover {
  background: #d94e5d;
  transform: translateY(-3px) scale(1.02);
}

/* Stats Bar */
.stats-bar {
  margin-top: -60px;
  position: relative;
  z-index: 10;
  padding: 0 24px;
  margin-bottom: 30px;
}

.stats-bar .el-row {
  background: #fff;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  border-radius: 12px;
  padding: 36px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  border-right: 1px solid rgba(0,0,0,0.06);
  transition: transform 0.3s;
}

.stat-item:hover { transform: translateY(-3px); }
.el-col:last-child .stat-item { border-right: none; }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fdf2f2 0%, #fff1f0 100%);
  color: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  transition: all 0.3s;
}

.stat-item:hover .stat-icon {
  background: var(--brand);
  color: #fff;
}

.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

/* News Section - 三栏布局 */
.news-section {
  padding: 40px 0;
  background: #f8f9fc;
}

.news-panel {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  height: 100%;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 16px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.title-icon {
  color: var(--brand);
  font-size: 14px;
}

.panel-more {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.3s;
}

.panel-more:hover { color: var(--brand); }

/* 新闻动态 */
.news-featured {
  margin-bottom: 16px;
}

.featured-img {
  position: relative;
  height: 160px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
}

.featured-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.featured-img:hover img { transform: scale(1.05); }

.featured-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
}

.featured-overlay h4 {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.news-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  z-index: 1;
}

.news-list li:last-child { border-bottom: none; }
.news-list li:hover { padding-left: 8px; }
.news-list li:hover .news-title { color: var(--brand); }

.news-title {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 12px;
  transition: color 0.2s;
}

.news-date {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

/* 热门活动 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.activity-item:hover {
  background: #fff5f5;
  transform: translateX(4px);
}

.activity-date {
  width: 50px;
  text-align: center;
  flex-shrink: 0;
}

.activity-date .day {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--brand);
  line-height: 1;
}

.activity-date .month {
  font-size: 12px;
  color: #666;
}

.activity-info {
  flex: 1;
  min-width: 0;
}

.activity-info h5 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-info p {
  font-size: 12px;
  color: #999;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 通知公告 */
.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  z-index: 1;
}

.notice-list li:last-child { border-bottom: none; }
.notice-list li:hover { padding-left: 8px; }
.notice-list li:hover .notice-title { color: var(--brand); }

.notice-tag {
  display: inline-block;
  padding: 2px 8px;
  background: var(--brand);
  color: #fff;
  font-size: 11px;
  border-radius: 3px;
  margin-right: 10px;
  flex-shrink: 0;
}

.notice-title {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 12px;
  transition: color 0.2s;
}

.notice-date {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

/* Section Styles */
.section-block {
  padding: 50px 0;
}

.bg-gray { background: #f8f9fc; }
.bg-white { background: #fff; }
.bg-light { background: #f5f6f8; }

.section-header {
  text-align: center;
  margin-bottom: 36px;
}

.section-header h2 {
  font-size: 32px;
  color: #1a1a1a;
  margin-bottom: 12px;
  font-family: "Noto Serif SC", serif;
  font-weight: 700;
}

.section-header p {
  color: #64748b;
  font-size: 15px;
  margin-bottom: 20px;
}

.more-link {
  display: inline-flex;
  align-items: center;
  color: var(--brand);
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
  padding: 8px 20px;
  border-radius: 20px;
  background: rgba(194, 53, 49, 0.05);
}

.more-link:hover {
  background: rgba(194, 53, 49, 0.1);
  transform: translateX(5px);
}

/* Project Grid */
.project-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.project-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.project-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.12);
  border-color: rgba(194, 53, 49, 0.1);
}

.card-img {
  height: 140px;
  position: relative;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.project-card:hover img { transform: scale(1.1); }

.card-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255,255,255,0.95);
  color: var(--brand);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.card-body {
  padding: 16px;
}

.card-body h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #1a1a1a;
}

.card-meta {
  display: flex;
  gap: 12px;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 10px;
}

.card-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}

/* Inheritor Grid */
.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.inheritor-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}

.inheritor-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(194, 53, 49, 0.12);
}

.inheritor-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 16px;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  background: #f5f5f5;
}

.inheritor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e8e8e8 0%, #f5f5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #999;
  font-family: "Noto Serif SC", serif;
  font-weight: 700;
}

.inheritor-card:hover .avatar-placeholder {
  color: var(--brand);
  background: linear-gradient(135deg, #fff0f0 0%, #fff 100%);
}

.inheritor-info h4 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #1a1a1a;
  font-family: "Noto Serif SC", serif;
  font-weight: 600;
}

.level-tag {
  display: inline-block;
  font-size: 12px;
  color: var(--brand);
  background: rgba(194, 53, 49, 0.08);
  padding: 3px 12px;
  border-radius: 20px;
  margin-bottom: 8px;
}

.inheritor-info p {
  color: #64748b;
  font-size: 13px;
  margin: 0;
  line-height: 1.4;
}

/* 悬浮留言按钮 */
.floating-message-btn {
  position: fixed;
  right: 30px;
  bottom: 100px;
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #c23531 0%, #d94e5d 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(194, 53, 49, 0.4);
  transition: all 0.3s;
  z-index: 100;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.floating-message-btn::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(194, 53, 49, 0.4);
  animation: pulse-ring 2s ease-out infinite;
  z-index: -1;
}

@keyframes pulse-ring {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.5); opacity: 0; }
}

.floating-message-btn:hover {
  transform: scale(1.1);
  animation: none;
}

.floating-message-btn:hover::before { animation: none; opacity: 0; }

.floating-message-btn .message-icon {
  width: 28px;
  height: 28px;
  color: #fff;
  margin-bottom: 2px;
}

.floating-message-btn .message-icon svg { width: 100%; height: 100%; }

.floating-message-btn .message-text {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 1200px) {
  .project-grid, .inheritor-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 992px) {
  .project-grid, .inheritor-grid { grid-template-columns: repeat(2, 1fr); }
  .news-section .el-col { margin-bottom: 20px; }
}

@media (max-width: 768px) {
  .project-grid, .inheritor-grid { grid-template-columns: 1fr; }
  .banner-content h1 { font-size: 32px; }
  .stat-item { flex-direction: column; text-align: center; padding: 16px 0; border-right: none; }
  .floating-message-btn { right: 20px; bottom: 80px; width: 60px; height: 60px; }
}

/* 新闻详情弹窗样式 */
.news-dialog-content {
  padding: 0;
}

.news-dialog-meta {
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
  background: linear-gradient(135deg, var(--brand) 0%, var(--brand-dark) 100%);
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

.news-dialog-body {
  max-height: 60vh;
  overflow-y: auto;
}

.news-dialog-body .content-text {
  font-size: 15px;
  line-height: 2;
  color: var(--text);
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
