<template>
  <div class="page-section">
    <PageHeader title="项目管理" sub="管理非遗项目信息和分类" />
    
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="搜索项目名称" clearable style="width: 200px;" />
      </el-form-item>
      <el-form-item label="分类ID">
        <el-input v-model.number="query.categoryId" placeholder="分类ID" clearable style="width: 140px;" type="number" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="()=>{query.page=1;load()}" icon="Search">搜索</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="warning" @click="exportData" icon="Download">导出 Excel</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="openCreate" icon="Plus">新增项目</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="items" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="项目名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryId" label="分类ID" width="100" align="center" />
      <el-table-column prop="coverImage" label="封面图" width="100" align="center">
        <template #default="{ row }">
          <el-image 
            v-if="row.coverImage" 
            :src="row.coverImage" 
            :preview-src-list="[row.coverImage]"
            style="width: 60px; height: 60px; border-radius: 8px; cursor: pointer;" 
            fit="cover"
          />
          <span v-else style="color: var(--muted);">无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="intro" label="简介" min-width="250" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="edit(row)" icon="Edit">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)" icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      background
      layout="total, sizes, prev, pager, next, jumper"
      :page-size="query.size"
      :page-sizes="[10, 20, 50, 100]"
      :current-page="query.page"
      :total="total"
      @current-change="(p:number)=>{query.page=p; load()}"
      @size-change="(s:number)=>{query.size=s; query.page=1; load()}"
    />

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="visible" :title="isEdit ? '编辑项目' : '新增项目'" width="900px" :close-on-click-modal="false" append-to-body>
      <el-form :model="form" label-width="110px" style="padding: 20px;">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="项目名称" required>
              <el-input v-model="form.name" placeholder="请输入项目名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类ID" required>
              <el-input v-model.number="form.categoryId" placeholder="请输入分类ID" type="number" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="封面图片">
          <div class="cover-upload-wrapper">
            <ImageUpload v-model="form.coverImage" category="projects" />
            <el-input v-model="form.coverImage" placeholder="或直接输入图片URL" style="margin-top: 12px;" clearable />
          </div>
        </el-form-item>

        <el-form-item label="项目简介">
          <el-input 
            v-model="form.intro" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入项目的简要介绍（100-200字）" 
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="历史渊源">
          <el-input 
            v-model="form.history" 
            type="textarea" 
            :rows="6" 
            placeholder="请详细描述项目的历史背景、传承故事等" 
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="padding: 0 20px 20px; display: flex; justify-content: flex-end; gap: 12px;">
          <el-button @click="visible = false" size="large">取消</el-button>
          <el-button type="primary" @click="save" size="large" icon="Check">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '../../components/PageHeader.vue'
import ImageUpload from '../../components/ImageUpload.vue'

const items = ref<any[]>([])
const total = ref(0)
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const form = ref({ name: '', categoryId: null as any, coverImage: '', intro: '', history: '' })
const query = ref({ keyword: '', categoryId: undefined as any, page: 1, size: 10 })

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  if (query.value.categoryId != null && query.value.categoryId !== '') params.categoryId = query.value.categoryId
  const { data } = await http.get('/admin/projects/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { name: '', categoryId: null as any, coverImage: '', intro: '', history: '' }
  visible.value = true
}

// @ts-ignore
function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    name: row.name || '',
    categoryId: row.categoryId,
    coverImage: row.coverImage || '',
    intro: row.intro || '',
    history: row.history || ''
  }
  visible.value = true
}

async function save() {
  if (!form.value.name) { 
    ElMessage.error('请填写项目名称')
    return 
  }
  if (!form.value.categoryId) { 
    ElMessage.error('请填写分类ID')
    return 
  }
  
  try {
    if (isEdit.value && currentId.value != null) {
      await http.put(`/admin/projects/${currentId.value}`, form.value)
      ElMessage.success('项目更新成功')
    } else {
      await http.post('/admin/projects', form.value)
      ElMessage.success('项目创建成功')
    }
    visible.value = false
    load()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

async function del(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    await http.delete(`/admin/projects/${id}`)
    ElMessage.success('项目删除成功')
    load()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

async function exportData() {
  try {
    ElMessage.info('正在导出数据，请稍候...')
    const response = await http.get('/admin/projects/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    const timestamp = new Date().toISOString().slice(0, 10)
    link.setAttribute('download', `非遗项目数据_${timestamp}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('数据导出成功')
  } catch (e) {
    ElMessage.error('导出失败，请重试')
  }
}
</script>

<style scoped>
.cover-upload-wrapper {
  width: 100%;
}
</style>
