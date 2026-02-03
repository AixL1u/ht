<template>
  <div class="page-section">
    <PageHeader title="视频管理" />
    <el-form inline :model="query" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="标题" /></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="()=>{query.page=1;load()}">搜索</el-button></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="openCreate">新增</el-button></el-form-item>
    </el-form>

    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="duration" label="时长(秒)" width="100" align="center" />
      <el-table-column label="封面" width="120" align="center">
        <template #default="{ row }">
          <el-image 
            v-if="row.cover" 
            :src="row.cover" 
            :preview-src-list="[row.cover]"
            style="width: 80px; height: 45px; border-radius: 4px;" 
            fit="cover"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="播放" width="100" align="center">
        <template #default="{ row }">
          <a v-if="row.hlsUrl" :href="row.hlsUrl" target="_blank" style="color: var(--brand)">HLS</a>
          <a v-else-if="row.url" :href="row.url" target="_blank" style="color: var(--brand)">直链</a>
          <span v-else>-</span>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="visible" :title="isEdit ? '编辑视频' : '新增视频'" width="850px" append-to-body>
      <el-form :model="form" label-width="100px" style="padding: 20px;">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入视频标题" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="时长(秒)">
              <el-input-number v-model="form.duration" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" placeholder="选择状态" style="width: 100%;">
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="草稿" value="DRAFT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="视频URL">
          <el-input v-model="form.url" placeholder="请输入视频播放地址" />
        </el-form-item>

        <el-form-item label="封面">
          <ImageUpload v-model="form.cover" category="videos" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="padding: 0 20px 20px;">
          <el-button @click="visible = false" size="large">取消</el-button>
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
const query = ref({ keyword: '', page: 1, size: 10 })
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const form = ref({ title: '', url: '', cover: '', duration: 0, status: 'PUBLISHED' })

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  const { data } = await http.get('/admin/videos/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { title: '', url: '', cover: '', duration: 0, status: 'PUBLISHED' }
  visible.value = true
}

function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    title: row.title || '',
    url: row.url || '',
    cover: row.cover || '',
    duration: row.duration || 0,
    status: row.status || 'PUBLISHED'
  }
  visible.value = true
}

async function save() {
  if (!form.value.title) { ElMessage.error('请填写标题'); return }
  try {
    if (isEdit.value && currentId.value != null) {
      await http.put(`/admin/videos/${currentId.value}`, form.value)
    } else {
      await http.post('/admin/videos', form.value)
    }
    ElMessage.success(isEdit.value ? '已更新' : '已新增')
    visible.value = false
    load()
  } catch (error: any) {
    ElMessage.error(`保存失败：${error.response?.data?.message || error.message}`)
  }
}

async function del(id: number) {
  if (!confirm('确定要删除该视频吗？')) return
  await http.delete(`/admin/videos/${id}`)
  ElMessage.success('已删除')
  load()
}
</script>
