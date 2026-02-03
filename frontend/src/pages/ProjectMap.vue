<template>
  <div class="project-map-page">
    <div class="map-wrapper">
      <!-- 地图 -->
      <div ref="mapRef" class="map"></div>
      
      <!-- 悬浮筛选面板 -->
      <div class="filter-panel">
        <div class="panel-header">
          <h3>非遗项目地图</h3>
          <p>地理位置分布展示</p>
        </div>
        <div class="filter-content">
          <el-select 
            v-model="selectedRegion" 
            placeholder="选择地区"
            clearable
            @change="handleRegionChange"
            style="width: 100%; margin-bottom: 12px"
          >
            <el-option v-for="region in regions" :key="region" :label="region" :value="region" />
          </el-select>
          
          <el-select 
            v-model="selectedCategory" 
            placeholder="选择分类"
            clearable
            @change="handleCategoryChange"
            style="width: 100%; margin-bottom: 12px"
          >
            <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
          </el-select>
          
          <el-button type="primary" plain @click="resetFilters" style="width: 100%">重置筛选</el-button>
        </div>
        
        <div class="map-stats">
          <div class="stat-row">
            <span class="stat-label">总项目数</span>
            <span class="stat-value">{{ totalProjects }}</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">当前显示</span>
            <span class="stat-value highlight">{{ filteredProjects.length }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 项目列表 -->
    <div class="projects-section">
      <div class="container">
        <h3 class="section-title">项目列表</h3>
        <el-table :data="filteredProjects" stripe style="width: 100%" border>
          <el-table-column prop="name" label="项目名称" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <span style="font-weight: 600">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="140" align="center">
            <template #default="{ row }">
              <el-tag size="small" effect="light">{{ row.categoryName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="region" label="地区" width="120" align="center" />
          <el-table-column prop="intro" label="描述" min-width="300" show-overflow-tooltip />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <router-link :to="`/projects/${row.id}`" style="text-decoration: none;">
                <el-button type="primary" link size="small">查看详情</el-button>
              </router-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import http from '../api/http'
import PageHeader from '../components/PageHeader.vue'

const mapRef = ref<HTMLElement>()
let mapInstance: any = null

const selectedRegion = ref('')
const selectedCategory = ref('')
const regions = ref<string[]>([])
const categories = ref<string[]>([])
const projects = ref<any[]>([])
const projectsByRegion = ref<Record<string, any[]>>({})

const totalProjects = computed(() => projects.value.length)

const filteredProjects = computed(() => {
  return projects.value.filter(p => {
    const regionMatch = !selectedRegion.value || p.region === selectedRegion.value
    const categoryMatch = !selectedCategory.value || p.category === selectedCategory.value
    return regionMatch && categoryMatch
  })
})

onMounted(async () => {
  try {
    // 1. Load project data
    const mapRes = await http.get('/catalog/projects/map')
    const rawData = mapRes.data
    const rawProjects = Array.isArray(rawData) ? rawData : (rawData.data || [])

    // 2. Load full categories for mapping
    const [regionsRes, categoriesRes, fullCatsRes] = await Promise.all([
      http.get('/catalog/projects/regions'),
      http.get('/catalog/projects/categories'),
      http.get('/catalog/categories') // 获取完整分类列表用于ID映射
    ])

    // Build Category Map (ID -> Name)
    const catMap: Record<number, string> = {}
    const fullCats = fullCatsRes.data || []
    fullCats.forEach((c: any) => {
      catMap[c.id] = c.name
    })

    // Process projects with correct fields
    projects.value = rawProjects.map((p: any) => ({
      ...p,
      categoryName: catMap[p.categoryId] || '未分类', // Map ID to Name
      // Ensure coordinates exist (fallback to avoid map crash)
      longitude: p.longitude || 104.1954,
      latitude: p.latitude || 35.8617
    }))
    
    // Group projects by region for tooltip
    projectsByRegion.value = projects.value.reduce((acc, p) => {
      const region = p.region || '未知'
      if (!acc[region]) acc[region] = []
      acc[region].push(p)
      return acc
    }, {} as Record<string, any[]>)

    const regionsData = regionsRes.data
    regions.value = Array.isArray(regionsData) ? regionsData : (regionsData.data || [])

    const categoriesData = categoriesRes.data
    categories.value = Array.isArray(categoriesData) ? categoriesData : (categoriesData.data || [])

    // 3. Load Map Data & Init
    initMap()
  } catch (e) {
    console.error('Failed to load map data:', e)
  }
})

function initMap() {
  if (!mapRef.value) return

  import('echarts').then(async echarts => {
    mapInstance = echarts.init(mapRef.value)
    mapInstance.showLoading()

    try {
      // Load China Map GeoJSON
      const chinaMapRes = await axios.get('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
      echarts.registerMap('china', chinaMapRes.data)
      
      mapInstance.hideLoading()
      renderMap(echarts)
    } catch (e) {
      console.error('Failed to load China map JSON:', e)
      mapInstance.hideLoading()
    }
  })
}

function renderMap(echarts: any) {
  const option = {
    title: {
      text: '非遗项目分布地图',
      left: 'center',
      top: 20,
      textStyle: {
        fontSize: 18
      }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0,0,0,0.7)',
      borderColor: '#333',
      textStyle: { color: '#fff' },
      formatter: (params: any) => {
        // Hover on province
        if (params.componentType === 'geo' || (params.seriesType === 'map')) {
          const regionName = params.name
          const regionProjects = projectsByRegion.value[regionName] || []
          
          let html = `<div style="font-weight:bold;margin-bottom:4px;font-size:14px">${regionName}</div>`
          if (regionProjects.length > 0) {
            html += `<div style="font-size:12px;color:#ddd;margin-bottom:4px">共 ${regionProjects.length} 个项目：</div>`
            html += '<ul style="margin:0 0 0 16px;padding:0;font-size:12px;line-height:1.5">'
            // Show top 10 projects
            regionProjects.slice(0, 10).forEach(p => {
              html += `<li>${p.name} <span style="color:#aaa;font-size:10px">(${p.categoryName})</span></li>`
            })
            if (regionProjects.length > 10) {
              html += `<li>...等 ${regionProjects.length} 个</li>`
            }
            html += '</ul>'
          } else {
            html += '<div style="color:#999;font-size:12px">暂无项目数据</div>'
          }
          return html
        }
        // Hover on scatter point
        if (params.componentSubType === 'effectScatter') {
          const p = params.data
          const regionName = p.region
          const regionProjects = projectsByRegion.value[regionName] || []
          
          let html = `<div style="font-size:15px;font-weight:bold;color:#fff;margin-bottom:4px">${p.name}</div>`
          html += `<div style="font-size:12px;color:#ec4899;margin-bottom:8px">● ${p.category}</div>`
          
          // Show other projects in same region
          if (regionProjects.length > 1) {
            html += `<div style="border-top:1px solid rgba(255,255,255,0.2);margin-top:8px;padding-top:8px">`
            html += `<div style="font-size:12px;color:#ddd;margin-bottom:4px">${regionName} 其他项目：</div>`
            html += '<ul style="margin:0 0 0 16px;padding:0;font-size:12px;color:#ccc;line-height:1.4">'
            regionProjects
              .filter((item: any) => item.id !== p.id)
              .slice(0, 5)
              .forEach((item: any) => {
                html += `<li>${item.name}</li>`
              })
            if (regionProjects.length > 6) html += `<li>...</li>`
            html += '</ul></div>'
          }
          return html
        }
        return params.name
      }
    },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.2,
      label: {
        show: true,
        color: '#796a5b', // 棕色文字，更复古
        fontSize: 10
      },
      emphasis: {
        label: {
          show: true,
          color: '#5c4033'
        },
        itemStyle: {
          areaColor: '#ffe4c4' // 浅杏色高亮
        }
      },
      itemStyle: {
        areaColor: '#fffbf0', // 米色背景，更有纸张感
        borderColor: '#d1c7b7', // 浅棕色边框
        borderWidth: 1
      }
    },
    series: [
      {
        name: '非遗项目',
        type: 'effectScatter', // 使用带有涟漪特效的散点
        coordinateSystem: 'geo',
        data: filteredProjects.value.map(p => ({
          name: p.name,
          value: [p.longitude, p.latitude],
          category: p.categoryName,
          region: p.region,
          id: p.id
        })),
        symbolSize: 12,
        rippleEffect: {
          brushType: 'stroke',
          scale: 3,
          color: '#c23531' // 涟漪颜色
        },
        itemStyle: {
          color: '#c23531', // 朱红色
          shadowBlur: 10,
          shadowColor: 'rgba(194, 53, 49, 0.5)'
        },
        emphasis: {
          scale: true,
          itemStyle: {
            color: '#d94e5d', // 稍亮的红色
            borderColor: '#fff',
            borderWidth: 2
          }
        }
      }
    ]
  }

  mapInstance.setOption(option)

  mapInstance.on('click', (params: any) => {
    if (params.componentSubType === 'scatter' && params.data.id) {
      // Redirect to project detail
      window.location.href = `/projects/${params.data.id}`
    }
  })

  window.addEventListener('resize', () => {
    mapInstance?.resize()
  })
}

function handleRegionChange() {
  updateMap()
}

function handleCategoryChange() {
  updateMap()
}

function resetFilters() {
  selectedRegion.value = ''
  selectedCategory.value = ''
  updateMap()
}

function updateMap() {
  if (!mapInstance) return

  const option = {
    series: [
      {
        data: filteredProjects.value.map(p => ({
          name: p.name,
          value: [p.longitude || 104.0, p.latitude || 35.0],
          category: p.category,
          region: p.region,
          id: p.id
        }))
      }
    ]
  }

  mapInstance.setOption(option)
}
</script>

<style scoped>
.project-map-page {
  padding: 0;
  padding-top: 70px; /* 导航栏高度 */
  background-color: #f8fafc;
}

.map-wrapper {
  position: relative;
  height: 85vh;
  min-height: 600px;
  background-color: #fff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.map {
  width: 100%;
  height: 100%;
}

.filter-panel {
  position: absolute;
  top: 80px;
  left: 24px;
  width: 280px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  padding: 20px;
  z-index: 100;
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.panel-header {
  margin-bottom: 20px;
  text-align: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 20px;
  color: #1e293b;
  font-weight: 700;
}

.panel-header p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.map-stats {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-row:last-child {
  margin-bottom: 0;
}

.stat-label {
  color: #64748b;
  font-size: 14px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
}

.stat-value.highlight {
  color: var(--brand);
  font-size: 20px;
}

.projects-section {
  padding: 0 24px 48px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 24px;
  padding-left: 16px;
  border-left: 4px solid var(--brand);
}

@media (max-width: 768px) {
  .map-wrapper {
    height: auto;
    display: flex;
    flex-direction: column;
  }

  .filter-panel {
    position: relative;
    top: 0;
    left: 0;
    width: 100%;
    border-radius: 0;
    box-shadow: none;
    border: none;
    border-bottom: 1px solid #e2e8f0;
    order: -1;
    margin: 0;
  }
  
  .map {
    height: 50vh;
    min-height: 400px;
  }
  
  .projects-section {
    padding: 24px 16px;
  }
}
</style>
