<template>
  <div class="page-section">
    <PageHeader title="商品管理">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </template>
    </PageHeader>
    <el-form inline :model="query" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="商品名" /></el-form-item>
      <el-form-item label="分类ID"><el-input v-model.number="query.categoryId" style="width:200px" /></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="()=>{query.page=1;load()}">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="items" style="margin-top:8px">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="stock" label="库存" />
      <el-table-column label="状态">
        <template #default="{ row }">
          {{ row.status === 'enabled' ? '启用' : '禁用' }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
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

    <el-dialog v-model="visible" :title="isEdit ? '编辑商品' : '新增商品'" width="850px" append-to-body>
      <el-form :model="form" label-width="100px" style="padding: 20px;">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格"><el-input v-model.number="form.price" type="number" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存"><el-input v-model.number="form.stock" type="number" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片">
          <div class="cover-upload-wrapper">
            <ImageUpload v-model="form.cover" category="products" />
            <el-input v-model="form.cover" placeholder="或直接输入图片URL" style="margin-top: 8px;" />
          </div>
        </el-form-item>
        <el-form-item label="图片集"><el-input v-model="form.images" placeholder="," /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="选择状态" style="width:100%">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="5" /></el-form-item>
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
const query = ref({ keyword: '', categoryId: undefined as any, page: 1, size: 10 })
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const categories = ref<any[]>([])
const form = ref({
  categoryId: null as any,
  name: '',
  description: '',
  price: null as any,
  stock: 0,
  cover: '',
  images: '',
  status: 'enabled'
})

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  if (query.value.categoryId != null && query.value.categoryId !== '') params.categoryId = query.value.categoryId
  const { data } = await http.get('/admin/products/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

async function ensureCategories() {
  if (categories.value.length === 0) {
    const { data } = await http.get('/product-categories')
    categories.value = data
  }
}

async function openCreate() {
  await ensureCategories()
  isEdit.value = false
  currentId.value = null
  form.value = { categoryId: null as any, name: '', description: '', price: null as any, stock: 0, cover: '', images: '', status: 'enabled' }
  visible.value = true
}

// @ts-ignore
async function edit(row: any) {
  await ensureCategories()
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    categoryId: row.categoryId,
    name: row.name,
    description: row.description,
    price: row.price,
    stock: row.stock,
    cover: row.cover,
    images: row.images,
    status: row.status || 'enabled'
  }
  visible.value = true
}

async function save() {
  if (!form.value.name || !form.value.categoryId) { ElMessage.error('请完善必填项'); return }
  if (isEdit.value && currentId.value != null) {
    await http.put(`/admin/products/${currentId.value}`, form.value)
  } else {
    await http.post('/admin/products', form.value)
  }
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function del(id: number) {
  if (!confirm('确定要删除该商品吗？')) return
  try {
    await http.delete(`/admin/products/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}
</script>

