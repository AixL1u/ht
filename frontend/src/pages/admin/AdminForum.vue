<template>
  <div class="page-section">
    <PageHeader title="留言板管理" />
    <el-form inline :model="query" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="标题" /></el-form-item>
      <el-form-item label="主题ID"><el-input v-model.number="query.topicId" style="width:200px" /></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="()=>{query.page=1;load()}">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="posts" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'VISIBLE' || row.status === 'visible' ? 'success' : 'danger'">
            {{ (row.status === 'VISIBLE' || row.status === 'visible') ? '可见' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <div style="display: flex; gap: 8px; justify-content: center; align-items: center;">
            <el-select v-model="row.status" size="small" style="width:100px">
              <el-option label="可见" value="VISIBLE" />
              <el-option label="隐藏" value="HIDDEN" />
            </el-select>
            <el-button size="small" type="primary" @click="update(row)">更新</el-button>
          </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import PageHeader from '../../components/PageHeader.vue'

const posts = ref<any[]>([])
const total = ref(0)
const query = ref({ keyword: '', topicId: undefined as any, page: 1, size: 10 })

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.keyword) params.keyword = query.value.keyword
  if (query.value.topicId != null && query.value.topicId !== '') params.topicId = query.value.topicId
  const { data } = await http.get('/forum/posts/search', { params })
  posts.value = data.content
  total.value = data.totalElements
}

onMounted(load)

// @ts-ignore
async function update(row: any) {
  await http.put(`/admin/forum/posts/${row.id}/status`, { status: row.status })
  ElMessage.success('已更新')
}
</script>

