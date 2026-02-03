<template>
  <div class="page-section">
    <PageHeader title="页面横幅管理" />
    
    <div class="banner-grid">
      <div v-for="page in pages" :key="page.key" class="banner-card">
        <div class="banner-preview" :style="getPreviewStyle(page.key)">
          <div class="preview-overlay">
            <h3>{{ getBannerTitle(page.key) || page.defaultTitle }}</h3>
            <p>{{ getBannerSubtitle(page.key) || page.defaultSub }}</p>
          </div>
        </div>
        <div class="banner-info">
          <span class="page-name">{{ page.name }}</span>
          <span class="page-key">{{ page.key }}</span>
        </div>
        <div class="banner-actions">
          <el-button size="small" type="primary" @click="editBanner(page)">配置</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="visible" :title="`配置 ${currentPage?.name} 横幅`" width="650px" append-to-body>
      <el-form :model="form" label-width="80px" style="padding: 20px;">
        <el-form-item label="标题">
          <el-input v-model="form.title" :placeholder="currentPage?.defaultTitle" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" :placeholder="currentPage?.defaultSub" />
        </el-form-item>
        <el-form-item label="背景图片">
          <ImageUpload v-model="form.imageUrl" category="banners" />
          <el-input v-model="form.imageUrl" placeholder="或直接输入图片URL" style="margin-top: 8px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%;">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用（使用默认）" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import PageHeader from '../../components/PageHeader.vue'
import ImageUpload from '../../components/ImageUpload.vue'

// 页面配置列表
const pages = [
  { key: 'login', name: '登录页面', defaultTitle: '非遗传承', defaultSub: '守护千年文脉 · 传承民族记忆' },
  { key: 'projects', name: '非遗项目', defaultTitle: '非遗名录', defaultSub: '汇集中华大地非物质文化遗产，探寻民族记忆' },
  { key: 'inheritors', name: '传承人', defaultTitle: '传承人', defaultSub: '守护技艺薪火，传承文化根脉' },
  { key: 'activities', name: '活动预约', defaultTitle: '活动预约', defaultSub: '参与非遗体验活动，感受传统文化魅力' },
  { key: 'shop', name: '文创商城', defaultTitle: '文创商城', defaultSub: '精选非遗文创产品，让传统走进生活' },
  { key: 'news', name: '资讯公告', defaultTitle: '资讯公告', defaultSub: '了解非遗动态，关注文化传承' },
  { key: 'videos', name: '视频展览', defaultTitle: '视频展览', defaultSub: '观看非遗纪录，领略匠心之美' },
]

const banners = ref<Record<string, any>>({})
const visible = ref(false)
const currentPage = ref<any>(null)
const form = ref({ title: '', subtitle: '', imageUrl: '', status: 'enabled' })

async function load() {
  const { data } = await http.get('/page-banners')
  banners.value = {}
  data.forEach((b: any) => {
    banners.value[b.pageKey] = b
  })
}

onMounted(load)

function getBannerTitle(key: string) {
  return banners.value[key]?.title
}

function getBannerSubtitle(key: string) {
  return banners.value[key]?.subtitle
}

function getPreviewStyle(key: string) {
  const banner = banners.value[key]
  if (banner?.imageUrl && banner.status === 'enabled') {
    return { backgroundImage: `url(${banner.imageUrl})` }
  }
  return {}
}

function editBanner(page: any) {
  currentPage.value = page
  const existing = banners.value[page.key]
  if (existing) {
    form.value = {
      title: existing.title || '',
      subtitle: existing.subtitle || '',
      imageUrl: existing.imageUrl || '',
      status: existing.status || 'enabled'
    }
  } else {
    form.value = { title: '', subtitle: '', imageUrl: '', status: 'enabled' }
  }
  visible.value = true
}

async function save() {
  await http.post('/admin/page-banners', {
    pageKey: currentPage.value.key,
    ...form.value
  })
  ElMessage.success('保存成功')
  visible.value = false
  load()
}
</script>

<style scoped>
.banner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.banner-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.banner-preview {
  height: 140px;
  background: linear-gradient(135deg, #2c3e50 0%, #4ca1af 100%);
  background-size: cover;
  background-position: center;
  position: relative;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-align: center;
  padding: 20px;
}

.preview-overlay h3 {
  font-size: 20px;
  margin-bottom: 8px;
  font-family: "Noto Serif SC", serif;
}

.preview-overlay p {
  font-size: 12px;
  opacity: 0.8;
}

.banner-info {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.page-name {
  font-weight: 600;
  color: #333;
}

.page-key {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.banner-actions {
  padding: 12px 16px;
  text-align: right;
}
</style>
