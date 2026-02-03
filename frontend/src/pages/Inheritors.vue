<template>
  <div class="page-section">
    <PageBanner 
      title="非遗传承人" 
      sub="薪火相传，匠心独运，见证非遗文化的守护者"
      pageKey="inheritors"
      image="https://images.unsplash.com/photo-1582650625119-3a31f8fa2699?w=1600&q=80"
    />

    <div class="container">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索传承人姓名或技艺..." prefix-icon="Search" style="width: 240px" clearable />
        <el-select v-model="filterLevel" placeholder="所有等级" style="width: 140px" clearable>
          <el-option label="国家级" value="国家级" />
          <el-option label="省级" value="省级" />
          <el-option label="市级" value="市级" />
        </el-select>
        <el-select v-model="filterRegion" placeholder="所有地区" style="width: 140px" clearable>
          <el-option v-for="r in regions" :key="r" :label="r" :value="r" />
        </el-select>
      </div>

      <!-- 传承人列表 -->
      <div class="inheritor-grid" v-if="filteredItems.length > 0">
        <div class="inheritor-card" v-for="i in filteredItems" :key="i.id" @click="go(i.id)">
          <div class="card-image">
            <img v-if="i.avatarUrl" :src="i.avatarUrl" :alt="i.name" />
            <div v-else class="avatar-placeholder">
              <span class="placeholder-text">{{ i.name.charAt(0) }}</span>
            </div>
            <div class="level-badge" :class="getLevelClass(i.level)">{{ i.level }}</div>
          </div>
          <div class="card-content">
            <h3 class="name">{{ i.name }} <span class="region">{{ i.region }}</span></h3>
            <p class="bio">{{ i.bio }}</p>
            <div class="skills">
              <span class="skill-tag" v-for="skill in getSkills(i.skills)" :key="skill">{{ skill }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="未找到相关传承人" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import PageBanner from '../components/PageBanner.vue'

const items = ref<any[]>([])
const searchKeyword = ref('')
const filterLevel = ref('')
const filterRegion = ref('')
const router = useRouter()

onMounted(async () => {
  try {
    // 使用分页接口获取所有数据（或者专门的列表接口）
    const { data } = await http.get('/catalog/inheritors')
    items.value = data
  } catch (e) {
    console.error(e)
  }
})

const regions = computed(() => {
  const s = new Set(items.value.map(i => i.region).filter(Boolean))
  return Array.from(s)
})

const filteredItems = computed(() => {
  return items.value.filter(i => {
    const matchName = !searchKeyword.value || 
      (i.name && i.name.includes(searchKeyword.value)) || 
      (i.skills && i.skills.includes(searchKeyword.value))
    const matchLevel = !filterLevel.value || i.level === filterLevel.value
    const matchRegion = !filterRegion.value || i.region === filterRegion.value
    return matchName && matchLevel && matchRegion
  })
})

function go(id: number) {
  router.push(`/inheritors/${id}`)
}

function getSkills(skillsStr: string) {
  if (!skillsStr) return []
  return skillsStr.split(/[,、，]/).slice(0, 3)
}

function getLevelClass(level: string) {
  if (level === '国家级') return 'level-national'
  if (level === '省级') return 'level-provincial'
  return 'level-city'
}
</script>

<style scoped>
.page-section {
  min-height: 100vh;
  background-color: #f8fafc;
  padding-bottom: 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 30px;
  justify-content: center;
  flex-wrap: wrap;
}

.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.inheritor-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #f1f5f9;
}

.inheritor-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.inheritor-card:hover .card-image img {
  transform: scale(1.05);
}

.level-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.level-national { background: linear-gradient(45deg, #c23531, #ff6b6b); }
.level-provincial { background: linear-gradient(45deg, #e6a23c, #fca5a5); }
.level-city { background: #909399; }

.card-content {
  padding: 20px;
}

.name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.region {
  font-size: 12px;
  color: #94a3b8;
  font-weight: normal;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}

.bio {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 42px;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  font-size: 12px;
  color: #c23531;
  background: #fff6f6;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #ffdede;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #fdfbf7 0%, #f5f0e6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8B6E35;
  font-family: "Noto Serif SC", serif;
  position: relative;
}

.avatar-placeholder::after {
  content: '';
  position: absolute;
  top: 10px; right: 10px; bottom: 10px; left: 10px;
  border: 1px solid rgba(139, 110, 53, 0.2);
}

.placeholder-text {
  font-size: 80px;
  font-weight: 700;
  opacity: 0.9;
  text-shadow: 2px 2px 0 rgba(255,255,255,0.6);
}
</style>
