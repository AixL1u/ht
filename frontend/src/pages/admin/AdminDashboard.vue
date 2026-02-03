<template>
  <div class="admin-dashboard">
    <PageHeader title="数据大屏" sub="实时数据统计和分析" />
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <StatCard 
        label="用户总数" 
        :value="stats.totalUsers"
        icon="User"
        icon-color="#6366f1"
      />
      <StatCard 
        label="活动数" 
        :value="stats.totalActivities"
        icon="Calendar"
        icon-color="#ec4899"
      />
      <StatCard 
        label="项目数" 
        :value="stats.totalProjects"
        icon="Folder"
        icon-color="#10b981"
      />
      <StatCard 
        label="商品数" 
        :value="stats.totalProducts"
        icon="ShoppingCart"
        icon-color="#f59e0b"
      />
      <StatCard 
        label="预约数" 
        :value="stats.totalReservations"
        icon="Tickets"
        icon-color="#8b5cf6"
      />
      <StatCard 
        label="订单数" 
        :value="stats.totalOrders"
        icon="List"
        icon-color="#06b6d4"
      />
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-grid">
      <ChartCard 
        title="用户增长趋势" 
        :option="userChartOption"
        height="300"
      />
      <ChartCard 
        title="活动预约分布" 
        :option="activityChartOption"
        height="300"
      />
      <ChartCard 
        title="商品销售排行" 
        :option="productChartOption"
        height="300"
      />
      <ChartCard 
        title="项目分类分布" 
        :option="projectChartOption"
        height="300"
      />
      <ChartCard 
        title="非遗项目地区分布" 
        :option="regionChartOption"
        height="300"
        style="grid-column: span 2"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import http from '../../api/http'
import PageHeader from '../../components/PageHeader.vue'
import StatCard from '../../components/StatCard.vue'
import ChartCard from '../../components/ChartCard.vue'

const stats = ref<any>({
  totalUsers: 0,
  totalActivities: 0,
  totalProjects: 0,
  totalProducts: 0,
  totalReservations: 0,
  totalOrders: 0
})

const userChartData = ref<any>({})
const activityChartData = ref<any>({})
const productChartData = ref<any>({})
const projectChartData = ref<any>({})
const regionChartData = ref<any>({})

// 用户增长趋势图表配置
const userChartOption = computed(() => ({
  title: { text: '用户增长趋势', left: 'center' },
  tooltip: { trigger: 'axis' },
  xAxis: { 
    type: 'category', 
    data: userChartData.value.months || []
  },
  yAxis: { type: 'value' },
  series: [{
    data: userChartData.value.values || [],
    type: 'line',
    smooth: true,
    itemStyle: { color: '#6366f1' },
    areaStyle: { color: 'rgba(99, 102, 241, 0.1)' }
  }],
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
}))

// 活动预约分布图表配置
const activityChartOption = computed(() => {
  const statusMap: any = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
    checked_in: '已核销',
    cancelled: '已取消'
  }
  
  const rawData = activityChartData.value.data || []
  const data = rawData.map((item: any) => ({
    ...item,
    name: statusMap[item.name] || item.name
  }))

  return {
    title: { text: '活动预约分布', left: 'center' },
    tooltip: { trigger: 'item' },
    series: [{
      data: data,
      type: 'pie',
      radius: ['40%', '70%'],
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      }
    }]
  }
})

// 商品销售排行图表配置
const productChartOption = computed(() => ({
  title: { text: '商品销售排行', left: 'center' },
  tooltip: { trigger: 'axis' },
  xAxis: { 
    type: 'category',
    data: (productChartData.value.data || []).map((item: any) => item.name)
  },
  yAxis: { type: 'value' },
  series: [{
    data: (productChartData.value.data || []).map((item: any) => item.value),
    type: 'bar',
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: '#ec4899' },
        { offset: 1, color: '#f472b6' }
      ])
    }
  }],
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
}))

// 项目分类分布图表配置
const projectChartOption = computed(() => ({
  title: { text: '项目分类分布', left: 'center' },
  tooltip: { trigger: 'item' },
  series: [{
    data: projectChartData.value.data || [],
    type: 'pie',
    radius: '50%',
    itemStyle: {
      borderRadius: 10,
      borderColor: '#fff',
      borderWidth: 2
    }
  }]
}))

// 非遗项目地区分布图表配置
const regionChartOption = computed(() => ({
  title: { text: '非遗项目地区分布', left: 'center' },
  tooltip: { trigger: 'axis' },
  xAxis: { 
    type: 'category',
    data: (regionChartData.value.data || []).map((item: any) => item.name),
    axisLabel: { rotate: 45 }
  },
  yAxis: { type: 'value' },
  series: [{
    data: (regionChartData.value.data || []).map((item: any) => item.value),
    type: 'bar',
    itemStyle: {
      color: '#10b981'
    }
  }],
  grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true }
}))

onMounted(async () => {
  try {
    // 获取统计数据
    const statsRes = await http.get('/admin/statistics')
    stats.value = statsRes.data

    // 获取图表数据
    const [usersRes, activitiesRes, productsRes, projectsRes, regionsRes] = await Promise.all([
      http.get('/admin/statistics/charts/users'),
      http.get('/admin/statistics/charts/activities'),
      http.get('/admin/statistics/charts/products'),
      http.get('/admin/statistics/charts/projects'),
      http.get('/projects/region-stats')
    ])

    userChartData.value = usersRes.data
    activityChartData.value = activitiesRes.data
    productChartData.value = productsRes.data
    projectChartData.value = projectsRes.data
    regionChartData.value = { data: regionsRes.data }
  } catch (e) {
    console.error('Failed to load dashboard data:', e)
  }
})
</script>

<style scoped>
.admin-dashboard {
  padding: 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 24px;
}

@media (max-width: 1400px) {
  .charts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .charts-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    padding: 0;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    margin-bottom: 24px;
  }

  .charts-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .charts-grid {
    gap: 12px;
  }
}
</style>
