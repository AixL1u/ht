<template>
  <div class="activity-detail-page" v-if="activity">
    <!-- Hero Banner -->
    <div class="detail-hero" :style="{ backgroundImage: `url(${activity.cover || 'https://images.unsplash.com/photo-1514525253440-b393452e3726?w=1600&q=80'})` }">
      <div class="hero-overlay"></div>
      <div class="hero-content container">
        <div class="status-tag" :class="getStatusClass(activity)">{{ getStatusText(activity) }}</div>
        <h1 class="activity-title">{{ activity.title }}</h1>
        <div class="activity-meta">
          <span><el-icon><Location /></el-icon> {{ activity.location }}</span>
          <span><el-icon><Calendar /></el-icon> {{ formatDate(activity.startTime) }}</span>
          <span><el-icon><User /></el-icon> 名额 {{ activity.capacity || '不限' }}</span>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <el-row :gutter="40">
        <!-- Left: Details -->
        <el-col :span="16">
          <div class="content-block">
            <div class="block-header">
              <h2>活动详情</h2>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <label>活动时间</label>
                <span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
              </div>
              <div class="info-item">
                <label>活动地点</label>
                <span>{{ activity.location }}</span>
              </div>
              <div class="info-item">
                <label>活动名额</label>
                <span>{{ activity.capacity || '不限' }} 人</span>
              </div>
              <div class="info-item">
                <label>活动状态</label>
                <el-tag :type="getStatusType(activity)">{{ getStatusText(activity) }}</el-tag>
              </div>
            </div>
          </div>

          <div class="content-block" v-if="activity.description">
            <div class="block-header">
              <h2>活动介绍</h2>
            </div>
            <div class="intro-content">
              <div class="intro-text" v-html="formatDescription(activity.description)"></div>
              
              <!-- 活动亮点 -->
              <div class="highlights" v-if="activity.description">
                <h3><el-icon><Star /></el-icon> 活动亮点</h3>
                <div class="highlight-list">
                  <div class="highlight-item">
                    <div class="highlight-icon"><el-icon><View /></el-icon></div>
                    <div class="highlight-text">
                      <strong>沉浸式体验</strong>
                      <p>近距离感受非遗文化魅力，亲身参与传统技艺</p>
                    </div>
                  </div>
                  <div class="highlight-item">
                    <div class="highlight-icon"><el-icon><UserFilled /></el-icon></div>
                    <div class="highlight-text">
                      <strong>大师指导</strong>
                      <p>非遗传承人现场讲解，传授独门技艺</p>
                    </div>
                  </div>
                  <div class="highlight-item">
                    <div class="highlight-icon"><el-icon><Present /></el-icon></div>
                    <div class="highlight-text">
                      <strong>专属纪念</strong>
                      <p>活动结束可获得精美纪念品或亲手制作的作品</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 温馨提示 -->
              <div class="notice-box">
                <h3><el-icon><WarningFilled /></el-icon> 温馨提示</h3>
                <ul>
                  <li>请提前15分钟到达活动现场签到</li>
                  <li>活动过程中请遵守现场秩序，听从工作人员安排</li>
                  <li>如需取消预约，请提前24小时联系我们</li>
                  <li>活动现场会有摄影记录，参与即视为同意肖像使用</li>
                </ul>
              </div>
            </div>
          </div>
        </el-col>

        <!-- Right: Sidebar -->
        <el-col :span="8">
          <div class="sidebar-block reservation-card">
            <div class="price-info" v-if="activity.price">
              <span class="price">¥{{ activity.price }}</span>
              <span class="unit">/人</span>
            </div>
            <div class="price-info" v-else>
              <span class="free">免费</span>
            </div>
            
            <el-button 
              type="primary" 
              size="large" 
              style="width: 100%; margin-top: 20px"
              :disabled="isEnded(activity)"
              @click="handleReserve"
            >
              {{ isEnded(activity) ? '活动已结束' : '立即预约' }}
            </el-button>
            
            <div class="tips">
              <p><el-icon><InfoFilled /></el-icon> 预约成功后请准时参加</p>
              <p><el-icon><InfoFilled /></el-icon> 如需取消请提前24小时</p>
            </div>
          </div>

          <div class="sidebar-block">
            <div class="sidebar-header">活动时间</div>
            <div class="time-display">
              <div class="date-box">
                <span class="month">{{ getMonth(activity.startTime) }}月</span>
                <span class="day">{{ getDay(activity.startTime) }}</span>
                <span class="weekday">{{ getWeekday(activity.startTime) }}</span>
              </div>
              <div class="time-range">
                <span>{{ formatTime(activity.startTime) }}</span>
                <span class="separator">至</span>
                <span>{{ formatTime(activity.endTime) }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { Location, Calendar, User, InfoFilled, Star, WarningFilled, View, UserFilled, Present } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const activity = ref<any>(null)

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id || isNaN(id)) {
    ElMessage.error('无效的活动ID')
    router.push('/activities')
    return
  }
  
  try {
    const { data } = await http.get(`/activities/${id}`)
    activity.value = data
  } catch (e: any) {
    console.error('Failed to load activity:', e)
    const status = e.response?.status
    if (status === 404) {
      ElMessage.error('活动不存在')
    } else if (status === 400) {
      ElMessage.error('请求参数错误')
    } else {
      ElMessage.error('加载活动失败，请稍后重试')
    }
    router.push('/activities')
  }
})

function isEnded(item: any) {
  return new Date(item.endTime) < new Date()
}

function getStatusText(item: any) {
  const now = new Date()
  const start = new Date(item.startTime)
  const end = new Date(item.endTime)
  if (now < start) return '报名中'
  if (now >= start && now <= end) return '进行中'
  return '已结束'
}

function getStatusClass(item: any) {
  const status = getStatusText(item)
  if (status === '报名中') return 'status-open'
  if (status === '进行中') return 'status-ongoing'
  return 'status-ended'
}

function getStatusType(item: any) {
  const status = getStatusText(item)
  if (status === '报名中') return 'success'
  if (status === '进行中') return 'warning'
  return 'info'
}

function getMonth(t: string) { return new Date(t).getMonth() + 1 }
function getDay(t: string) { return new Date(t).getDate() }
function getWeekday(t: string) {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date(t).getDay()]
}

function formatDate(t: string) {
  const d = new Date(t)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function formatDateTime(t: string) {
  const d = new Date(t)
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatTime(t: string) {
  const d = new Date(t)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatDescription(desc: string) {
  if (!desc) return ''
  // 将换行转为段落，处理【标题】格式
  return desc
    .split('\n')
    .filter(p => p.trim())
    .map(p => {
      // 如果是【xxx】格式的标题，加粗显示
      if (p.trim().startsWith('【') && p.trim().endsWith('】')) {
        return `<h4 class="section-title">${p}</h4>`
      }
      // 如果是数字开头的列表项
      if (/^\d+\./.test(p.trim())) {
        return `<p class="list-item">${p}</p>`
      }
      return `<p>${p}</p>`
    })
    .join('')
}

async function handleReserve() {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.info('请先登录')
    router.push('/login')
    return
  }
  const user = JSON.parse(userStr)
  try {
    await http.post(`/activities/${activity.value.id}/reservations`, { userId: user.id })
    ElMessage.success('预约成功！')
    router.push('/activities')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '预约失败')
  }
}
</script>

<style scoped>
.activity-detail-page {
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
  height: 400px;
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
  background: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.7));
}

.hero-content {
  position: relative;
  z-index: 2;
  width: 100%;
  padding-bottom: 40px;
  color: #fff;
}

.status-tag {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.status-open { background: #10b981; color: #fff; }
.status-ongoing { background: #f59e0b; color: #fff; }
.status-ended { background: #94a3b8; color: #fff; }

.activity-title {
  font-size: 36px;
  margin: 0 0 20px;
  font-family: "Noto Serif SC", serif;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.activity-meta {
  display: flex;
  gap: 24px;
  font-size: 15px;
  opacity: 0.9;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Content */
.main-content {
  padding-bottom: 60px;
}

.content-block {
  margin-bottom: 32px;
}

.block-header {
  border-bottom: 1px solid #eee;
  padding-bottom: 12px;
  margin-bottom: 20px;
}

.block-header h2 {
  font-size: 22px;
  color: #1e293b;
  margin: 0;
  border-left: 4px solid var(--brand);
  padding-left: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item label {
  font-size: 13px;
  color: #94a3b8;
}

.info-item span {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
}

.text-content {
  font-size: 15px;
  line-height: 1.8;
  color: #334155;
  white-space: pre-wrap;
}

/* 活动介绍美化 */
.intro-content {
  color: #334155;
}

.intro-text {
  font-size: 15px;
  line-height: 2;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  border-radius: 8px;
  border-left: 4px solid var(--brand);
}

.intro-text :deep(p) {
  margin: 0 0 12px;
  text-indent: 2em;
}

.intro-text :deep(p:last-child) {
  margin-bottom: 0;
}

.intro-text :deep(.section-title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--brand);
  margin: 20px 0 12px;
  text-indent: 0;
}

.intro-text :deep(.section-title:first-child) {
  margin-top: 0;
}

.intro-text :deep(.list-item) {
  text-indent: 0;
  padding-left: 1em;
}

.highlights {
  margin-bottom: 30px;
}

.highlights h3 {
  font-size: 18px;
  color: #1e293b;
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.highlights h3 .el-icon {
  color: #f59e0b;
}

.highlight-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.highlight-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.highlight-item:hover {
  border-color: var(--brand);
  box-shadow: 0 4px 12px rgba(194, 53, 49, 0.1);
  transform: translateY(-2px);
}

.highlight-icon {
  font-size: 28px;
  margin-bottom: 12px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe4e4 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--brand);
}

.highlight-text strong {
  display: block;
  font-size: 15px;
  color: #1e293b;
  margin-bottom: 6px;
}

.highlight-text p {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.notice-box {
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 20px;
}

.notice-box h3 {
  font-size: 16px;
  color: #92400e;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-box h3 .el-icon {
  color: #f59e0b;
}

.notice-box ul {
  margin: 0;
  padding-left: 20px;
}

.notice-box li {
  font-size: 14px;
  color: #78350f;
  line-height: 2;
}

/* Sidebar */
.sidebar-block {
  background: #f8fafc;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.reservation-card {
  background: linear-gradient(135deg, #fff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}

.price-info {
  text-align: center;
}

.price {
  font-size: 36px;
  font-weight: 700;
  color: var(--brand);
}

.unit {
  font-size: 14px;
  color: #64748b;
}

.free {
  font-size: 28px;
  font-weight: 600;
  color: #10b981;
}

.tips {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.tips p {
  font-size: 12px;
  color: #94a3b8;
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sidebar-header {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
}

.time-display {
  display: flex;
  align-items: center;
  gap: 20px;
}

.date-box {
  background: var(--brand);
  color: #fff;
  padding: 12px 16px;
  border-radius: 8px;
  text-align: center;
  min-width: 70px;
}

.date-box .month {
  display: block;
  font-size: 12px;
  opacity: 0.9;
}

.date-box .day {
  display: block;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.date-box .weekday {
  display: block;
  font-size: 12px;
  opacity: 0.9;
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 15px;
  color: #334155;
}

.time-range .separator {
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 768px) {
  .detail-hero { height: 300px; }
  .activity-title { font-size: 24px; }
  .activity-meta { flex-direction: column; gap: 8px; }
  .info-grid { grid-template-columns: 1fr; }
  .highlight-list { grid-template-columns: 1fr; }
}
</style>
