<template>
  <div class="page-section">
    <PageHeader title="资讯管理">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </template>
    </PageHeader>
    <el-form inline :model="query" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="标题" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.type" style="width:200px" clearable>
          <el-option label="资讯" value="news" />
          <el-option label="公告" value="notice" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="()=>{query.page=1;load()}">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 'news' ? 'primary' : 'warning'">{{ row.type === 'news' ? '资讯' : '公告' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'draft' ? 'info' : 'success'">{{ row.status === 'draft' ? '草稿' : '已发布' }}</el-tag>
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
        prev-text="上一页"
        next-text="下一页"
        @current-change="(p:number)=>{query.page=p; load()}"
        @size-change="(s:number)=>{query.size=s; query.page=1; load()}"
      />
    </div>

    <el-dialog v-model="visible" :title="isEdit ? '编辑资讯' : '新增资讯'" width="850px" append-to-body>
      <el-form :model="form" label-width="100px" style="padding: 20px;">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型">
              <el-select v-model="form.type" placeholder="选择类型" style="width: 100%;">
                <el-option label="资讯" value="news" />
                <el-option label="公告" value="notice" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" placeholder="选择状态" style="width: 100%;">
                <el-option label="草稿" value="draft" />
                <el-option label="已发布" value="PUBLISHED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面">
          <ImageUpload v-model="form.coverImage" category="news" />
        </el-form-item>
        
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="padding: 0 20px 20px;">
          <el-button @click="visible=false" size="large">取消</el-button>
          <el-button type="primary" @click="save" size="large">保存</el-button>
        </div>
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
const query = ref({ keyword: '', type: '', page: 1, size: 10 })
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const form = ref({ title: '', type: 'news', coverImage: '', status: 'draft', content: '' })

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  if (query.value.type) params.type = query.value.type
  const { data } = await http.get('/admin/news/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { title: '', type: 'news', coverImage: '', status: 'draft', content: '' }
  visible.value = true
}

// @ts-ignore
function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    title: row.title || '',
    type: row.type || 'news',
    coverImage: row.coverImage || '',
    status: row.status || 'draft',
    content: row.content || ''
  }
  visible.value = true
}

async function save() {
  if (!form.value.title) { ElMessage.error('请填写标题'); return }
  try {
    if (isEdit.value && currentId.value != null) {
      await http.put(`/admin/news/${currentId.value}`, form.value)
    } else {
      await http.post('/admin/news', form.value)
    }
    ElMessage.success('已保存')
    visible.value = false
    load()
  } catch (error: any) {
    ElMessage.error(`保存失败：${error.response?.data?.message || error.message}`)
  }
}

async function del(id: number) {
  if (!confirm('确定要删除该资讯吗？')) return
  try {
    await http.delete(`/admin/news/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (error: any) {
    ElMessage.error(`删除失败：${error.response?.data?.message || error.message}`)
  }
}
</script>
