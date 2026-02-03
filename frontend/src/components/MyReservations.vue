<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>我的预约</span>
      </div>
    </template>
    <el-table :data="reservations" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="activityTitle" label="活动名称" min-width="150">
        <template #default="{ row }">
          <span style="font-weight: 600">{{ row.activityTitle || '未知活动' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="活动时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预约时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.reservedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewDetail(row)">详情</el-button>
          <el-button 
            size="small" 
            type="danger" 
            link
            v-if="['pending','PENDING','approved','APPROVED'].includes(row.status)"
            @click="handleCancel(row)"
          >取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showDetail" title="预约详情" width="500px" destroy-on-close>
    <div v-if="currentItem" class="detail-content">
      <img v-if="currentItem.cover" :src="currentItem.cover" class="detail-cover" />
      <h3 class="detail-title">{{ currentItem.activityTitle }}</h3>
      
      <div class="detail-row">
        <label>状态：</label>
        <el-tag :type="getStatusType(currentItem.status)">{{ getStatusLabel(currentItem.status) }}</el-tag>
      </div>
      <div class="detail-row">
        <label>预约ID：</label>
        <span>{{ currentItem.id }}</span>
      </div>
      <div class="detail-row">
        <label>地点：</label>
        <span>{{ currentItem.location }}</span>
      </div>
      <div class="detail-row">
        <label>活动时间：</label>
        <span>{{ formatDate(currentItem.startTime) }} ~ {{ formatDate(currentItem.endTime) }}</span>
      </div>
      <div class="detail-row">
        <label>预约提交：</label>
        <span>{{ formatDate(currentItem.reservedAt) }}</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="showDetail = false">关闭</el-button>
      <el-button 
        type="danger" 
        v-if="currentItem && ['pending','PENDING','approved','APPROVED'].includes(currentItem.status)"
        @click="handleCancel(currentItem)"
      >取消预约</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'

const props = defineProps<{
  reservations: any[]
}>()

const emit = defineEmits(['refresh'])

const reservations = ref(props.reservations)
const showDetail = ref(false)
const currentItem = ref<any>(null)

watch(() => props.reservations, (newVal) => {
  reservations.value = newVal
}, { deep: true })

function viewDetail(row: any) {
  currentItem.value = row
  showDetail.value = true
}

async function handleCancel(row: any) {
  try {
    await ElMessageBox.confirm('确定要取消此预约吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const userStr = localStorage.getItem('user')
    if(!userStr) return
    const user = JSON.parse(userStr)
    console.log('Current User:', user)
    console.log('Canceling Reservation:', row.id, 'User ID:', user.id)

    if (!user.id) {
      ElMessage.error('用户信息异常，请重新登录')
      return
    }

    await http.delete(`/reservations/${row.id}?userId=${user.id}`)
    ElMessage.success('取消成功')
    emit('refresh') 
    showDetail.value = false
  } catch (e: any) {
    console.error('Cancel error:', e)
    if (e !== 'cancel') {
      const msg = e?.response?.data?.message || e?.message || '取消失败'
      ElMessage.error(msg)
    }
  }
}

function getStatusType(status: string) {
  const map: any = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info',
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'cancelled': 'info'
  }
  return map[status] || 'info'
}

function getStatusLabel(status: string) {
  const map: any = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消',
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'cancelled': '已取消'
  }
  return map[status] || status
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 16px;
}

.detail-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #1e293b;
}

.detail-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}
.detail-row label {
  width: 80px;
  color: #64748b;
  flex-shrink: 0;
}
.detail-row span {
  flex: 1;
  color: #1e293b;
}
</style>
