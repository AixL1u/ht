<template>
  <div class="graph-page">
    <!-- 左侧控制面板 -->
    <div class="control-panel">
      <div class="panel-header">
        <div class="logo">
          <svg viewBox="0 0 48 48" fill="none">
            <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M24 14l-10 5v10l10 5 10-5V19l-10-5z" fill="currentColor" opacity="0.3"/>
            <path d="M24 24l-6 3v6l6 3 6-3v-6l-6-3z" fill="currentColor"/>
          </svg>
          <span>知识图谱</span>
        </div>
        <span class="back-btn" @click="$router.push('/projects')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </span>
      </div>
      
      <div class="search-box">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input v-model="searchText" placeholder="搜索节点..." @input="handleSearch" />
      </div>

      <div class="section">
        <h4>节点筛选</h4>
        <div class="filter-list">
          <label v-for="(cat, idx) in categories" :key="idx" class="filter-item" :class="{ active: cat.visible }">
            <input type="checkbox" v-model="cat.visible" @change="updateFilter" />
            <span class="filter-dot" :style="{ background: cat.color }"></span>
            <span class="filter-name">{{ cat.name }}</span>
            <span class="filter-count">{{ getCategoryCount(idx) }}</span>
          </label>
        </div>
      </div>

      <div class="section">
        <h4>关系类型</h4>
        <div class="relation-list">
          <div class="relation-item">
            <span class="line solid"></span>
            <span>属于分类</span>
          </div>
          <div class="relation-item">
            <span class="line dashed"></span>
            <span>传承关系</span>
          </div>
        </div>
      </div>

      <div class="section stats">
        <h4>数据统计</h4>
        <div class="stats-row">
          <div class="stat-card">
            <span class="num" style="color: #5470c6">{{ stats.categories }}</span>
            <span class="label">分类</span>
          </div>
          <div class="stat-card">
            <span class="num" style="color: #91cc75">{{ stats.projects }}</span>
            <span class="label">项目</span>
          </div>
        </div>
        <div class="stats-row">
          <div class="stat-card">
            <span class="num" style="color: #fac858">{{ stats.inheritors }}</span>
            <span class="label">传承人</span>
          </div>
          <div class="stat-card">
            <span class="num" style="color: #ee6666">{{ stats.links }}</span>
            <span class="label">关系</span>
          </div>
        </div>
      </div>

      <div class="section help">
        <h4>操作说明</h4>
        <ul>
          <li>🖱️ 拖拽画布移动视图</li>
          <li>🔍 滚轮缩放图谱</li>
          <li>👆 点击节点查看详情</li>
          <li>✨ 双击节点聚焦</li>
        </ul>
      </div>
    </div>

    <!-- 图谱区域 -->
    <div class="graph-container">
      <!-- 背景装饰 -->
      <div class="bg-grid"></div>
      
      <div ref="chartRef" class="chart-area"></div>
      
      <!-- 节点详情面板 -->
      <transition name="slide">
        <div v-if="selectedNode" class="detail-panel">
          <div class="detail-header">
            <span class="node-badge" :style="{ background: getNodeColor(selectedNode.category) }">
              {{ getCategoryName(selectedNode.category) }}
            </span>
            <span class="close-btn" @click="selectedNode = null">×</span>
          </div>
          <h3 class="node-title">{{ selectedNode.name }}</h3>
          
          <div class="node-meta" v-if="selectedNode.value">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            <span>关联 {{ selectedNode.value }} 个节点</span>
          </div>
          
          <div class="related-section" v-if="relatedNodes.length">
            <h4>关联节点 ({{ relatedNodes.length }})</h4>
            <div class="related-list">
              <div 
                v-for="node in relatedNodes" 
                :key="node.id" 
                class="related-item"
                @click="focusNode(node.id)"
              >
                <span class="dot" :style="{ background: getNodeColor(node.category) }"></span>
                <span class="name">{{ node.name }}</span>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 18l6-6-6-6"/>
                </svg>
              </div>
            </div>
          </div>
          
          <button v-if="selectedNode.id.startsWith('P_')" class="detail-btn" @click="viewDetail">
            查看项目详情
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </button>
        </div>
      </transition>

      <!-- 缩放控制 -->
      <div class="zoom-controls">
        <button @click="zoomIn" title="放大">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35M11 8v6M8 11h6"/>
          </svg>
        </button>
        <button @click="zoomOut" title="缩小">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35M8 11h6"/>
          </svg>
        </button>
        <button @click="resetZoom" title="重置">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
            <path d="M3 3v5h5"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import http from '../api/http'

const router = useRouter()
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const searchText = ref('')
const selectedNode = ref<any>(null)
const relatedNodes = ref<any[]>([])
const graphData = ref<any>({ nodes: [], links: [], categories: [] })

const categories = ref([
  { name: '分类', color: '#5470c6', visible: true },
  { name: '非遗项目', color: '#91cc75', visible: true },
  { name: '传承人', color: '#fac858', visible: true }
])

const stats = ref({ categories: 0, projects: 0, inheritors: 0, links: 0 })

function getCategoryCount(idx: number) {
  if (idx === 0) return stats.value.categories
  if (idx === 1) return stats.value.projects
  if (idx === 2) return stats.value.inheritors
  return 0
}

function getNodeColor(category: number) {
  return categories.value[category]?.color || '#999'
}

function getCategoryName(category: number) {
  return categories.value[category]?.name || '未知'
}

function handleSearch() {
  if (!chart || !searchText.value.trim()) {
    chart?.dispatchAction({ type: 'downplay' })
    return
  }
  
  const keyword = searchText.value.toLowerCase()
  const matchedNodes = graphData.value.nodes.filter((n: any) => 
    n.name.toLowerCase().includes(keyword)
  )
  
  if (matchedNodes.length > 0) {
    chart.dispatchAction({ type: 'downplay' })
    chart.dispatchAction({
      type: 'highlight',
      dataIndex: matchedNodes.map((n: any) => 
        graphData.value.nodes.findIndex((node: any) => node.id === n.id)
      )
    })
  }
}

function updateFilter() {
  if (!chart) return
  
  const filteredNodes = graphData.value.nodes.filter((n: any) => 
    categories.value[n.category]?.visible
  )
  const nodeIds = new Set(filteredNodes.map((n: any) => n.id))
  const filteredLinks = graphData.value.links.filter((l: any) => 
    nodeIds.has(l.source) && nodeIds.has(l.target)
  )
  
  chart.setOption({
    series: [{
      data: filteredNodes.map((n: any) => ({
        ...n,
        itemStyle: { color: getNodeColor(n.category) },
        label: { show: n.category < 2, color: '#fff' }
      })),
      links: filteredLinks
    }]
  })
}

function findRelatedNodes(nodeId: string) {
  const related: any[] = []
  graphData.value.links.forEach((link: any) => {
    if (link.source === nodeId) {
      const target = graphData.value.nodes.find((n: any) => n.id === link.target)
      if (target) related.push(target)
    } else if (link.target === nodeId) {
      const source = graphData.value.nodes.find((n: any) => n.id === link.source)
      if (source) related.push(source)
    }
  })
  return related
}

function focusNode(nodeId: string) {
  if (!chart) return
  const idx = graphData.value.nodes.findIndex((n: any) => n.id === nodeId)
  if (idx >= 0) {
    chart.dispatchAction({ type: 'downplay' })
    chart.dispatchAction({ type: 'highlight', dataIndex: idx })
    const node = graphData.value.nodes[idx]
    selectedNode.value = node
    relatedNodes.value = findRelatedNodes(nodeId)
  }
}

function viewDetail() {
  if (selectedNode.value?.id.startsWith('P_')) {
    const id = selectedNode.value.id.replace('P_', '')
    router.push(`/projects/${id}`)
  }
}

function zoomIn() {
  if (!chart) return
  const option = chart.getOption() as any
  const zoom = (option.series?.[0]?.zoom || 1) * 1.2
  chart.setOption({ series: [{ zoom: Math.min(zoom, 5) }] })
}

function zoomOut() {
  if (!chart) return
  const option = chart.getOption() as any
  const zoom = (option.series?.[0]?.zoom || 1) / 1.2
  chart.setOption({ series: [{ zoom: Math.max(zoom, 0.2) }] })
}

function resetZoom() {
  if (!chart) return
  chart.setOption({ series: [{ zoom: 1, center: null }] })
}

onMounted(async () => {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  chart.showLoading({ 
    text: '加载知识图谱...',
    color: '#5470c6',
    textColor: '#fff',
    maskColor: 'rgba(15, 23, 42, 0.8)'
  })

  try {
    const { data } = await http.get('/graph')
    graphData.value = data
    
    stats.value = {
      categories: data.nodes.filter((n: any) => n.category === 0).length,
      projects: data.nodes.filter((n: any) => n.category === 1).length,
      inheritors: data.nodes.filter((n: any) => n.category === 2).length,
      links: data.links.length
    }
    
    const option: any = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(30, 41, 59, 0.95)',
        borderColor: '#334155',
        textStyle: { color: '#fff' },
        formatter: (params: any) => {
          if (params.dataType === 'node') {
            const cat = getCategoryName(params.data.category)
            return `<div style="font-weight:600;margin-bottom:4px">${params.data.name}</div><div style="color:#94a3b8;font-size:12px">类型: ${cat}</div>`
          }
          return `<div style="color:#94a3b8;font-size:12px">${params.data.name || '关联'}</div>`
        }
      },
      series: [
        {
          type: 'graph',
          layout: 'force',
          data: data.nodes.map((n: any) => ({
            ...n,
            symbolSize: n.category === 0 ? 45 : n.category === 1 ? 30 : 20,
            itemStyle: { 
              color: getNodeColor(n.category),
              shadowBlur: 10,
              shadowColor: getNodeColor(n.category) + '40'
            },
            label: { 
              show: n.category < 2, 
              color: '#e2e8f0',
              fontSize: n.category === 0 ? 14 : 12,
              fontWeight: n.category === 0 ? 600 : 400
            }
          })),
          links: data.links.map((l: any) => ({
            ...l,
            lineStyle: { 
              type: l.name === '传承' ? [5, 5] : 'solid',
              opacity: 0.4,
              width: 1.5,
              color: '#475569'
            }
          })),
          categories: data.categories,
          roam: true,
          draggable: true,
          label: {
            position: 'right',
            formatter: '{b}',
            distance: 8
          },
          force: {
            initLayout: 'circular',
            repulsion: 500,
            gravity: 0.08,
            edgeLength: [100, 250],
            friction: 0.6
          },
          lineStyle: {
            curveness: 0.2
          },
          emphasis: {
            focus: 'adjacency',
            itemStyle: {
              shadowBlur: 20,
              shadowColor: '#fff'
            },
            lineStyle: { 
              width: 3,
              opacity: 0.8
            },
            label: { 
              show: true, 
              fontSize: 14,
              fontWeight: 600
            }
          }
        }
      ]
    }
    
    chart.setOption(option)
    chart.hideLoading()
    
    chart.on('click', (params: any) => {
      if (params.dataType === 'node') {
        selectedNode.value = params.data
        relatedNodes.value = findRelatedNodes(params.data.id)
      }
    })
    
    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error('Failed to load graph data', e)
    chart.hideLoading()
  }
})

function handleResize() {
  chart?.resize()
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.graph-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: #0a0f1a;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
}

/* 左侧面板 */
.control-panel {
  width: 260px;
  background: linear-gradient(180deg, #131b2e 0%, #0d1321 100%);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  border-right: 1px solid #1e293b;
  overflow-y: auto;
}

.control-panel::-webkit-scrollbar {
  width: 4px;
}

.control-panel::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 2px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.logo svg {
  width: 32px;
  height: 32px;
  color: #c23531;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.05);
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.05);
  border: 1px solid #1e293b;
  border-radius: 8px;
  padding: 0 12px;
  transition: all 0.2s;
}

.search-box:focus-within {
  border-color: #5470c6;
  background: rgba(84, 112, 198, 0.1);
}

.search-box svg {
  width: 16px;
  height: 16px;
  color: #475569;
  flex-shrink: 0;
}

.search-box input {
  flex: 1;
  padding: 10px 8px;
  background: transparent;
  border: none;
  color: #fff;
  font-size: 13px;
  outline: none;
}

.search-box input::placeholder {
  color: #475569;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section h4 {
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0;
}

.filter-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-item:hover {
  background: rgba(255,255,255,0.05);
}

.filter-item.active {
  background: rgba(255,255,255,0.08);
}

.filter-item input {
  display: none;
}

.filter-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.filter-name {
  flex: 1;
  color: #cbd5e1;
  font-size: 13px;
}

.filter-count {
  color: #475569;
  font-size: 12px;
  background: rgba(255,255,255,0.05);
  padding: 2px 8px;
  border-radius: 10px;
}

.relation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.relation-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #94a3b8;
  font-size: 12px;
}

.line {
  width: 24px;
  height: 2px;
}

.line.solid {
  background: #475569;
}

.line.dashed {
  background: repeating-linear-gradient(90deg, #475569 0, #475569 4px, transparent 4px, transparent 8px);
}

.stats-row {
  display: flex;
  gap: 8px;
}

.stat-card {
  flex: 1;
  background: rgba(255,255,255,0.03);
  border: 1px solid #1e293b;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}

.stat-card .num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-card .label {
  font-size: 11px;
  color: #64748b;
}

.help ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.help li {
  color: #64748b;
  font-size: 12px;
  padding: 4px 0;
}

/* 图谱区域 */
.graph-container {
  flex: 1;
  position: relative;
  background: radial-gradient(ellipse at center, #131b2e 0%, #0a0f1a 100%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.chart-area {
  width: 100%;
  height: 100%;
}

/* 详情面板 */
.detail-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 300px;
  background: rgba(19, 27, 46, 0.95);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(20px);
  border: 1px solid #1e293b;
  box-shadow: 0 20px 40px rgba(0,0,0,0.4);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.node-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}

.close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  font-size: 20px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.node-title {
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.node-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 20px;
}

.node-meta svg {
  width: 16px;
  height: 16px;
}

.related-section h4 {
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
  margin: 0 0 12px 0;
}

.related-list {
  max-height: 180px;
  overflow-y: auto;
  margin: 0 -8px;
  padding: 0 8px;
}

.related-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 4px;
  background: rgba(255,255,255,0.03);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.related-item:hover {
  background: rgba(255,255,255,0.08);
}

.related-item .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.related-item .name {
  flex: 1;
  color: #e2e8f0;
  font-size: 13px;
}

.related-item svg {
  width: 14px;
  height: 14px;
  color: #475569;
}

.detail-btn {
  width: 100%;
  margin-top: 20px;
  padding: 12px;
  background: linear-gradient(135deg, #5470c6 0%, #3b5998 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.detail-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(84, 112, 198, 0.3);
}

.detail-btn svg {
  width: 16px;
  height: 16px;
}

/* 缩放控制 */
.zoom-controls {
  position: absolute;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.zoom-controls button {
  width: 40px;
  height: 40px;
  background: rgba(19, 27, 46, 0.9);
  border: 1px solid #1e293b;
  border-radius: 10px;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.zoom-controls button:hover {
  background: rgba(30, 41, 59, 0.95);
  color: #fff;
  border-color: #334155;
}

.zoom-controls button svg {
  width: 18px;
  height: 18px;
}

/* 动画 */
.slide-enter-active, .slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from, .slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .control-panel {
    display: none;
  }
  
  .detail-panel {
    width: calc(100% - 40px);
    left: 20px;
    right: 20px;
    bottom: 20px;
    top: auto;
    max-height: 50vh;
    overflow-y: auto;
  }
}
</style>
