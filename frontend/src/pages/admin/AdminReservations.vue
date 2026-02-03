<template>
  <div class="page-section">
    <PageHeader title="预约审核" />
    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="userId" label="用户ID" width="100" align="center" />
      <el-table-column prop="activityId" label="活动ID" width="100" align="center" />
      <el-table-column label="状态" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button 
            size="small" 
            type="success" 
            @click="approve(row.id)" 
            :disabled="row.status !== 'pending'"
          >通过</el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="reject(row.id)"
            :disabled="row.status !== 'pending'"
          >驳回</el-button>
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

const items = ref<any[]>([])
const total = ref(0)
const query = ref({ page: 1, size: 10 })

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  const { data } = await http.get('/admin/reservations/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function getStatusLabel(status: string) {
  const map: any = { 
    pending: '待审核', 
    approved: '已通过', 
    rejected: '已驳回', 
    checked_in: '已核销',
    cancelled: '已取消',
    APPROVED: '已通过', // 兼容后端可能返回大写
    REJECTED: '已驳回',
    PENDING: '待审核'
  }
  return map[status] || status
}

function getStatusType(status: string) {
  const map: any = { 
    pending: 'warning', 
    approved: 'success', 
    rejected: 'danger', 
    checked_in: 'info',
    cancelled: 'info',
    APPROVED: 'success',
    REJECTED: 'danger',
    PENDING: 'warning'
  }
  return map[status] || ''
}

async function approve(id: number) {
  await http.put(`/admin/reservations/${id}`, { status: 'APPROVED', approvedBy: 0 })
  ElMessage.success('已通过')
  load()
}
async function reject(id: number) {
  await http.put(`/admin/reservations/${id}`, { status: 'REJECTED', approvedBy: 0 })
  ElMessage.success('已驳回')
  load()
}
</script>
