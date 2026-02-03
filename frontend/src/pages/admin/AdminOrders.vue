<template>
  <div class="page-section">
    <PageHeader title="订单管理" />
    <el-form inline :model="query" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="用户ID"><el-input v-model.number="query.userId" style="width:140px" /></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="()=>{query.page=1;load()}">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="userId" label="用户ID" width="100" align="center" />
      <el-table-column prop="totalAmount" label="金额" align="right" width="120">
        <template #default="{ row }">
          <span style="font-weight:600;color:var(--brand)">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center">
        <template #default="{ row }">
          <el-tag :type="getPaymentStatusType(row.paymentStatus)">{{ getPaymentStatusLabel(row.paymentStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑状态</el-button>
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

    <!-- Edit Dialog -->
    <el-dialog v-model="visible" title="更新订单状态" width="500px" append-to-body>
      <el-form :model="form" label-width="100px" style="padding: 20px;">
        <el-form-item label="订单状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="form.paymentStatus" style="width:100%">
            <el-option label="未支付" value="UNPAID" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="padding: 0 20px 20px;">
          <el-button @click="visible = false" size="large">取消</el-button>
          <el-button type="primary" @click="saveEdit" size="large">保存</el-button>
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

const items = ref<any[]>([])
const total = ref(0)
const query = ref({ userId: undefined as any, page: 1, size: 10 })
const visible = ref(false)
const form = ref({ status: '', paymentStatus: '' })
const currentId = ref<number | null>(null)

async function load() {
  const params:any = { page: query.value.page - 1, size: query.value.size }
  if (query.value.userId != null && query.value.userId !== '') params.userId = query.value.userId
  const { data } = await http.get('/admin/orders/search', { params })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function getStatusLabel(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'PENDING': '待处理', 'PROCESSING': '处理中', 'SHIPPED': '已发货',
    'DELIVERED': '已送达', 'COMPLETED': '已完成', 'CANCELLED': '已取消'
  }
  return map[s] || status
}

function getStatusType(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'PENDING': 'warning', 'PROCESSING': 'primary', 'SHIPPED': 'success',
    'DELIVERED': 'success', 'COMPLETED': 'success', 'CANCELLED': 'info'
  }
  return map[s] || ''
}

function getPaymentStatusLabel(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'UNPAID': '未支付', 'PAID': '已支付', 'REFUNDED': '已退款'
  }
  return map[s] || status
}

function getPaymentStatusType(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'UNPAID': 'danger', 'PAID': 'success', 'REFUNDED': 'info'
  }
  return map[s] || ''
}

// @ts-ignore
function openEdit(row: any) {
  currentId.value = row.id
  form.value = {
    status: row.status,
    paymentStatus: row.paymentStatus
  }
  visible.value = true
}

async function saveEdit() {
  if (!currentId.value) return
  try {
    await http.put(`/admin/orders/${currentId.value}/status`, form.value)
    ElMessage.success('已更新')
    visible.value = false
    load()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}
</script>
