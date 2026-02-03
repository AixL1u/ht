<template>
  <div class="page-section">
    <PageHeader title="轮播图管理" />
    <el-form inline class="toolbar" style="margin-bottom:12px">
      <el-form-item>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="图片" width="140" align="center">
        <template #default="{ row }">
          <img :src="row.imageUrl" alt="" style="width:120px;height:60px;object-fit:cover;border-radius:4px" />
        </template>
      </el-table-column>
      <el-table-column prop="link" label="链接" />
      <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'danger'">{{ row.status === 'enabled' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="visible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="600px" append-to-body>
      <el-form :model="form" label-width="80px" style="padding: 20px;">
        <el-form-item label="轮播图片">
          <div class="cover-upload-wrapper">
            <ImageUpload v-model="form.imageUrl" category="banners" />
            <el-input v-model="form.imageUrl" placeholder="或直接输入图片URL" style="margin-top: 8px;" />
          </div>
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="form.link" placeholder="跳转链接，例如 /projects/1" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%;">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
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
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const form = ref({ imageUrl: '', link: '', sortOrder: 0, status: 'enabled' })

async function load() {
  const { data } = await http.get('/banners')
  items.value = data
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { imageUrl: '', link: '', sortOrder: 0, status: 'enabled' }
  visible.value = true
}

// @ts-ignore
function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    imageUrl: row.imageUrl || '',
    link: row.link || '',
    sortOrder: row.sortOrder || 0,
    status: row.status || 'enabled'
  }
  visible.value = true
}

async function save() {
  if (!form.value.imageUrl) { ElMessage.error('请填写图片URL'); return }
  if (isEdit.value && currentId.value != null) {
    await http.put(`/admin/banners/${currentId.value}`, form.value)
  } else {
    await http.post('/admin/banners', form.value)
  }
  ElMessage.success(isEdit.value ? '已更新' : '已新增')
  visible.value = false
  load()
}

async function del(id: number) {
  if (!confirm('确定要删除该轮播图吗？')) return
  try {
    await http.delete(`/admin/banners/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}
</script>
