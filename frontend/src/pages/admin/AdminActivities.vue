<template>
  <div class="page-section">
    <PageHeader title="活动管理">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增活动</el-button>
      </template>
    </PageHeader>
    <el-form inline :model="query" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="活动标题" clearable /></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="()=>{query.page=1;load()}">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="活动标题" min-width="180" />
      <el-table-column prop="location" label="地点" width="150" />
      <el-table-column label="活动时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="capacity" label="名额" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :page-size="query.size"
        :page-sizes="[10,20,50]"
        :current-page="query.page"
        :total="total"
        @current-change="(p:number)=>{query.page=p; load()}"
        @size-change="(s:number)=>{query.size=s; query.page=1; load()}"
      />
    </div>

    <el-dialog v-model="visible" :title="isEdit ? '编辑活动' : '新增活动'" width="850px" append-to-body>
      <el-form :model="form" label-width="80px" style="padding: 20px;">
        <el-form-item label="活动标题">
          <el-input v-model="form.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="活动名额">
              <el-input-number v-model="form.capacity" :min="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片">
          <ImageUpload v-model="form.cover" category="activities" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入活动描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
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

const items = ref<any[]>([])
const total = ref(0)
const query = ref({ keyword: '', page: 1, size: 10 })
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const form = ref({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  capacity: 50,
  status: 'DRAFT',
  cover: '',
  description: ''
})

async function load() {
  const params: any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  const { data } = await http.get('/activities/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { title: '', location: '', startTime: '', endTime: '', capacity: 50, status: 'DRAFT', cover: '', description: '' }
  visible.value = true
}

function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    title: row.title || '',
    location: row.location || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    capacity: row.capacity || 50,
    status: row.status || 'DRAFT',
    cover: row.cover || '',
    description: row.description || ''
  }
  visible.value = true
}

async function save() {
  if (!form.value.title) { ElMessage.error('请填写活动标题'); return }
  if (!form.value.startTime || !form.value.endTime) { ElMessage.error('请选择活动时间'); return }
  try {
    if (isEdit.value && currentId.value != null) {
      await http.put(`/admin/activities/${currentId.value}`, form.value)
    } else {
      await http.post('/admin/activities', form.value)
    }
    ElMessage.success('已保存')
    visible.value = false
    load()
  } catch (error: any) {
    ElMessage.error(`保存失败：${error.response?.data?.message || error.message}`)
  }
}

async function del(id: number) {
  if (!confirm('确定要删除该活动吗？')) return
  try {
    await http.delete(`/admin/activities/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (error: any) {
    ElMessage.error(`删除失败：${error.response?.data?.message || error.message}`)
  }
}

function formatDateTime(t: string) {
  if (!t) return '-'
  const d = new Date(t)
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getStatusText(row: any) {
  const now = new Date()
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  if (row.status === 'CANCELLED') return '已取消'
  if (row.status === 'DRAFT') return '草稿'
  if (now < start) return '报名中'
  if (now >= start && now <= end) return '进行中'
  return '已结束'
}

function getStatusType(row: any) {
  const status = getStatusText(row)
  if (status === '报名中') return 'success'
  if (status === '进行中') return 'warning'
  if (status === '草稿') return 'info'
  if (status === '已取消') return 'danger'
  return 'info'
}
</script>
