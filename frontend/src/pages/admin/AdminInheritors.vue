<template>
  <div class="page-section">
    <PageHeader title="传承人管理" />
    <el-form inline :model="query" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="姓名" /></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="()=>{query.page=1;load()}">搜索</el-button></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="openCreate">新增</el-button></el-form-item>
    </el-form>

    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="region" label="地区" width="140" align="center" />
      <el-table-column prop="level" label="级别" width="120" align="center" />
      <el-table-column prop="avatarUrl" label="头像">
        <template #default="{ row }">
          <el-image 
            v-if="row.avatarUrl" 
            :src="row.avatarUrl" 
            :preview-src-list="[row.avatarUrl]"
            style="width: 40px; height: 40px; border-radius: 50%;" 
            fit="cover"
          />
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
    <el-dialog v-model="visible" :title="isEdit ? '编辑传承人' : '新增传承人'" width="850px" append-to-body>
      <el-form :model="form" label-width="100px" style="padding: 20px;">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="级别">
              <el-input v-model="form.level" placeholder="例如：国家级/省级" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="地区">
              <el-input v-model="form.region" placeholder="例如：江苏苏州" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式">
              <el-input v-model="form.contact" placeholder="电话或邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="头像">
          <ImageUpload v-model="form.avatarUrl" category="inheritors" />
        </el-form-item>

        <el-form-item label="技艺特点">
          <el-input v-model="form.skills" type="textarea" :rows="2" placeholder="掌握的技艺或特点" />
        </el-form-item>

        <el-form-item label="生平简介">
          <el-input v-model="form.bio" type="textarea" :rows="4" placeholder="个人生平经历" />
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
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const form = ref({ name: '', region: '', level: '', avatarUrl: '', bio: '', skills: '', contact: '' })
const query = ref({ keyword: '', page: 1, size: 10 })

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  const { data } = await http.get('/admin/inheritors/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { name: '', region: '', level: '', avatarUrl: '', bio: '', skills: '', contact: '' }
  visible.value = true
}

// @ts-ignore
function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    name: row.name || '',
    region: row.region || '',
    level: row.level || '',
    avatarUrl: row.avatarUrl || '',
    bio: row.bio || '',
    skills: row.skills || '',
    contact: row.contact || ''
  }
  visible.value = true
}

async function save() {
  if (!form.value.name) { ElMessage.error('请填写姓名'); return }
  if (isEdit.value && currentId.value != null) {
    await http.put(`/admin/inheritors/${currentId.value}`, form.value)
  } else {
    await http.post('/admin/inheritors', form.value)
  }
  ElMessage.success(isEdit.value ? '已更新' : '已新增')
  visible.value = false
  load()
}

async function del(id: number) {
  if (!confirm('确定要删除该传承人吗？')) return
  try {
    await http.delete(`/admin/inheritors/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}
</script>
