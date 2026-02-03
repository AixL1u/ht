<template>
  <div class="page-section">
    <PageBanner 
      title="活动中心" 
      sub="参与非遗体验，让文化触手可及"
      pageKey="activities"
      image="https://images.unsplash.com/photo-1514525253440-b393452e3726?w=1600&q=80"
    />

    <div class="container">
      <el-tabs v-model="activeTab" class="activity-tabs">
        <el-tab-pane label="热门活动" name="list">
          <!-- 筛选 -->
          <div class="filter-bar">
            <el-input v-model="searchKeyword" placeholder="搜索活动..." prefix-icon="Search" style="width: 300px" clearable />
          </div>

          <div class="activity-list" v-if="filteredActivities.length > 0">
            <div class="activity-card" v-for="item in filteredActivities" :key="item.id" @click="goDetail(item.id)">
              <div class="act-cover">
                <img :src="item.cover || 'https://images.unsplash.com/photo-1514525253440-b393452e3726?w=800&q=80'" />
                <div class="act-status" :class="getStatusClass(item)">{{ getStatusText(item) }}</div>
              </div>
              <div class="act-content">
                <div class="act-header">
                  <div class="act-date-box">
                    <span class="month">{{ getMonth(item.startTime) }}月</span>
                    <span class="day">{{ getDay(item.startTime) }}</span>
                  </div>
                  <div class="act-title-box">
                    <h3>{{ item.title }}</h3>
                    <p><el-icon><Location /></el-icon> {{ item.location }}</p>
                  </div>
                </div>
                
                <div class="act-details">
                  <div class="detail-item">
                    <label>时间</label>
                    <span>{{ formatTimeRange(item.startTime, item.endTime) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>名额</label>
                    <span>{{ item.capacity || '不限' }}</span>
                  </div>
                </div>

                <div class="act-actions">
                  <el-button type="primary" :disabled="isEnded(item)" @click="reserve(item.id)" round style="width: 100%">
                    {{ isEnded(item) ? '已结束' : '立即预约' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无活动" />
        </el-tab-pane>

        <el-tab-pane label="我的预约" name="my">
          <div style="background: #fff; padding: 24px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
            <MyReservations :reservations="myReservations" @refresh="loadMyReservations" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { ElMessage } from 'element-plus'
import MyReservations from '../components/MyReservations.vue'
import { Location, Search } from '@element-plus/icons-vue'
import PageBanner from '../components/PageBanner.vue'

const router = useRouter()
const activities = ref<any[]>([])
const myReservations = ref<any[]>([])
const activeTab = ref('list')
const searchKeyword = ref('')

function goDetail(id: number) {
  if (!id || isNaN(id)) {
    ElMessage.error('无效的活动ID')
    return
  }
  router.push(`/activities/${id}`)
}

onMounted(async () => {
  loadActivities()
  loadMyReservations()
})

async function loadActivities() {
  try {
    const { data } = await http.get('/activities')
    activities.value = data || []
  } catch (e: any) {
    console.error('Failed to load activities:', e)
    if (e.response?.status !== 404 && e.response?.status !== 400) {
      ElMessage.error('加载活动列表失败')
    }
  }
}

async function loadMyReservations() {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    try {
      const { data } = await http.get('/reservations', { params: { userId: user.id } })
      myReservations.value = data || []
    } catch (e: any) {
      console.error('Failed to load reservations:', e)
      // 静默处理，不显示错误提示
    }
  }
}

const filteredActivities = computed(() => {
  if (!searchKeyword.value) return activities.value
  return activities.value.filter(a => a.title.includes(searchKeyword.value) || a.location.includes(searchKeyword.value))
})

function getMonth(t: string) { return new Date(t).getMonth() + 1 }
function getDay(t: string) { return new Date(t).getDate() }
function formatTimeRange(start: string, end: string) {
  const s = new Date(start)
  const e = new Date(end)
  return `${s.getHours()}:${String(s.getMinutes()).padStart(2, '0')} - ${e.getHours()}:${String(e.getMinutes()).padStart(2, '0')}`
}

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
  if (status === '报名中') return 'status-success'
  if (status === '进行中') return 'status-warning'
  return 'status-gray'
}

async function reserve(id: number) {
  const userStr = localStorage.getItem('user')
  if (!userStr) { ElMessage.info('请先登录/注册'); location.href = '/login'; return }
  const user = JSON.parse(userStr)
  try {
    await http.post(`/activities/${id}/reservations`, { userId: user.id })
    ElMessage.success('预约成功')
    loadMyReservations()
    activeTab.value = 'my'
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '预约失败')
  }
}
</script>

<style scoped>
.page-section {
  min-height: 100vh;
  background-color: #f8fafc;
  padding-bottom: 60px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.filter-bar {
  margin-bottom: 24px;
  display: flex;
  justify-content: flex-end;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  border: 1px solid #f1f5f9;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px -5px rgba(0,0,0,0.1);
}

.act-cover {
  height: 160px;
  position: relative;
}

.act-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.act-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.status-success { background: #10b981; }
.status-warning { background: #f59e0b; }
.status-gray { background: #94a3b8; }

.act-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.act-header {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.act-date-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px 12px;
  text-align: center;
  min-width: 60px;
}

.act-date-box .month {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.act-date-box .day {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #334155;
}

.act-title-box {
  flex: 1;
}

.act-title-box h3 {
  font-size: 18px;
  margin: 0 0 8px;
  color: #1e293b;
  line-height: 1.4;
}

.act-title-box p {
  color: #64748b;
  font-size: 13px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.act-details {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 4px;
}

.detail-item:last-child { margin-bottom: 0; }

.detail-item label { color: #94a3b8; }
.detail-item span { color: #334155; font-weight: 500; }

.act-actions {
  margin-top: auto;
}

/* Tabs Style Override */
.activity-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #e2e8f0;
}
.activity-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  height: 50px;
  line-height: 50px;
  color: #64748b;
}
.activity-tabs :deep(.el-tabs__item.is-active) {
  color: #d4af37;
  font-weight: 600;
}
.activity-tabs :deep(.el-tabs__active-bar) {
  background-color: #d4af37;
}
</style>

