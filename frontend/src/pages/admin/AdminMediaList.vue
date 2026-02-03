<template>
  <div class="page-section">
    <PageHeader title="媒体管理" />
    
    <!-- 搜索工具栏 -->
    <div class="toolbar-card">
      <el-form inline :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="搜索标题" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" style="width: 120px" clearable>
            <el-option label="全部" value="" />
            <el-option label="视频" value="video" />
            <el-option label="图片" value="image" />
            <el-option label="音频" value="audio" />
            <el-option label="文件" value="file" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" style="width: 140px" clearable>
            <el-option label="全部" value="" />
            <el-option-group label="视频分类">
              <el-option label="非遗纪录片" value="documentary" />
              <el-option label="技艺展示" value="craft" />
              <el-option label="传承人访谈" value="interview" />
              <el-option label="活动记录" value="activity" />
              <el-option label="教学视频" value="tutorial" />
            </el-option-group>
            <el-option-group label="图片分类">
              <el-option label="作品展示" value="artwork" />
              <el-option label="活动照片" value="event" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="items" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="预览" width="120" align="center">
          <template #default="{ row }">
            <div class="preview-cell">
              <img v-if="row.type === 'image'" :src="row.url" class="preview-img" @click="previewMedia(row)" />
              <img v-else-if="row.type === 'video' && row.coverUrl" :src="row.coverUrl" class="preview-img" @click="previewMedia(row)" />
              <div v-else-if="row.type === 'video'" class="preview-placeholder video" @click="previewMedia(row)">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div v-else-if="row.type === 'audio'" class="preview-placeholder audio">
                <el-icon><Headset /></el-icon>
              </div>
              <div v-else class="preview-placeholder file">
                <el-icon><Document /></el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            <span class="title-text">{{ row.title || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="110" align="center">
          <template #default="{ row }">
            <span>{{ getCategoryLabel(row.category) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ready'" type="success" size="small">就绪</el-tag>
            <el-tag v-else-if="row.status === 'queued'" type="warning" size="small">转码中</el-tag>
            <el-tag v-else size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览量" width="80" align="center" v-if="query.type === 'video' || !query.type">
          <template #default="{ row }">
            {{ row.viewCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="大小" width="90" align="center">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="editMedia(row)">编辑</el-button>
            <el-button size="small" type="primary" link @click="copyUrl(row)">复制链接</el-button>
            <el-button size="small" type="danger" link @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :current-page="query.page"
          :total="total"
          @current-change="(p:number) => { query.page = p; load() }"
          @size-change="(s:number) => { query.size = s; query.page = 1; load() }"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑媒体" width="500px" append-to-body>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" style="width: 100%" clearable>
            <el-option label="非遗纪录片" value="documentary" />
            <el-option label="技艺展示" value="craft" />
            <el-option label="传承人访谈" value="interview" />
            <el-option label="活动记录" value="activity" />
            <el-option label="教学视频" value="tutorial" />
            <el-option label="作品展示" value="artwork" />
            <el-option label="活动照片" value="event" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" :title="previewItem?.title || '预览'" width="800px" destroy-on-close append-to-body>
      <div class="preview-content">
        <img v-if="previewItem?.type === 'image'" :src="previewItem.url" class="preview-full-img" />
        <video v-else-if="previewItem?.type === 'video'" :src="previewItem.hlsUrl || previewItem.url" controls class="preview-video" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import { VideoPlay, Headset, Document } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'

const items = ref<any[]>([])
const total = ref(0)
const query = ref({ keyword: '', type: '', category: '', page: 1, size: 10 })

const editVisible = ref(false)
const editForm = ref<any>({})
const previewVisible = ref(false)
const previewItem = ref<any>(null)

async function load() {
  const params: any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  if (query.value.type) params.type = query.value.type
  if (query.value.category) params.category = query.value.category
  const { data } = await http.get('/admin/media/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function search() {
  query.value.page = 1
  load()
}

function resetQuery() {
  query.value = { keyword: '', type: '', category: '', page: 1, size: 10 }
  load()
}

function getTypeLabel(type: string) {
  const map: any = { image: '图片', video: '视频', audio: '音频', file: '文件' }
  return map[type] || type
}

function getTypeTag(type: string) {
  const map: any = { image: 'success', video: 'primary', audio: 'warning', file: 'info' }
  return map[type] || ''
}

function getCategoryLabel(cat: string) {
  const map: any = {
    documentary: '非遗纪录片', craft: '技艺展示', interview: '传承人访谈',
    activity: '活动记录', tutorial: '教学视频', artwork: '作品展示',
    event: '活动照片', inheritor: '传承人照片', project: '项目图片',
    oral: '口述历史', music: '传统音乐', lecture: '讲座录音',
    research: '研究资料', application: '申报材料', other: '其他'
  }
  return map[cat] || cat
}

function formatSize(bytes: number) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function editMedia(row: any) {
  editForm.value = { id: row.id, title: row.title, category: row.category }
  editVisible.value = true
}

async function saveEdit() {
  try {
    await http.put(`/admin/media/${editForm.value.id}`, editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

function previewMedia(row: any) {
  previewItem.value = row
  previewVisible.value = true
}

function copyUrl(row: any) {
  navigator.clipboard.writeText(location.origin + row.url)
  ElMessage.success('链接已复制')
}

async function del(id: number) {
  if (!confirm('确定要删除该媒体吗？')) return
  try {
    await http.delete(`/admin/media/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}
</script>

<style scoped>
.toolbar-card {
  background: #fff;
  padding: 16px 20px 0;
  border-radius: 8px;
  margin-bottom: 16px;
}

.table-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.preview-cell {
  display: flex;
  justify-content: center;
}

.preview-img {
  width: 80px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
}

.preview-img:hover {
  transform: scale(1.05);
}

.preview-placeholder {
  width: 80px;
  height: 50px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
}

.preview-placeholder.video {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
}

.preview-placeholder.audio {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.preview-placeholder.file {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.title-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.preview-content {
  display: flex;
  justify-content: center;
}

.preview-full-img {
  max-width: 100%;
  max-height: 500px;
}

.preview-video {
  width: 100%;
  max-height: 450px;
  background: #000;
}
</style>
