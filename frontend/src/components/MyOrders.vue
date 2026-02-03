<template>
  <div class="my-orders-container">
    <div v-if="orders.length === 0" class="empty-state">
      <el-empty description="暂无订单记录" />
    </div>

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-no">
            <span class="label">订单号：</span>
            <span class="value">{{ order.id }}</span>
          </div>
          <div class="order-status">
            <el-tag :type="getOrderStatusType(order.status)" effect="plain" round>
              {{ getOrderStatusLabel(order.status) }}
            </el-tag>
          </div>
        </div>
        
        <div class="order-body">
          <div class="info-row">
            <div class="info-item">
              <span class="label">订单金额</span>
              <span class="amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">下单时间</span>
              <span class="time">{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付状态</span>
              <el-tag :type="getPaymentStatusType(order.paymentStatus)" size="small">
                {{ getPaymentStatusLabel(order.paymentStatus) }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="actions">
            <el-button v-if="isUnpaid(order)" type="danger" plain size="default" @click="payOrder(order)">
              去支付
            </el-button>
            <el-button type="primary" plain size="default" @click="viewDetail(order)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 支付弹窗 -->
  <el-dialog v-model="showPayDialog" title="收银台" width="480px" center destroy-on-close :show-close="payStatus !== 'processing' && payStatus !== 'success'" :close-on-click-modal="false">
    <div class="pay-dialog-content">
      <!-- 支付成功状态 -->
      <div v-if="payStatus === 'success'" class="pay-success-view">
        <el-icon class="success-icon"><SuccessFilled /></el-icon>
        <div class="success-text">支付成功</div>
        <div class="success-sub">正在跳转...</div>
      </div>

      <!-- 支付等待/进行状态 -->
      <template v-else>
        <div class="pay-amount">
          <div class="label">支付金额</div>
          <div class="value">¥{{ payingOrder?.totalAmount?.toFixed(2) }}</div>
        </div>
        
        <div class="pay-methods">
          <div 
            class="pay-method-item" 
            :class="{ active: payMethod === 'wechat' }"
            @click="payMethod = 'wechat'"
          >
            <img src="https://cdn-icons-png.flaticon.com/128/3983/3983945.png" class="icon" />
            <span>微信支付</span>
            <div class="radio-circle"></div>
          </div>
          <div 
            class="pay-method-item" 
            :class="{ active: payMethod === 'alipay' }"
            @click="payMethod = 'alipay'"
          >
            <img src="https://cdn-icons-png.flaticon.com/128/888/888857.png" class="icon" />
            <span>支付宝</span>
            <div class="radio-circle"></div>
          </div>
        </div>
      </template>
    </div>
    <template #footer v-if="payStatus !== 'success'">
      <el-button @click="showPayDialog = false" :disabled="payStatus === 'processing'">取消</el-button>
      <el-button type="primary" :loading="payStatus === 'processing'" @click="confirmPay" class="pay-btn">
        {{ payStatus === 'processing' ? '支付中...' : '立即支付' }}
      </el-button>
    </template>
  </el-dialog>

  <!-- 订单详情弹窗 -->
  <el-dialog v-model="showDetail" title="订单详情" width="800px" center destroy-on-close>
    <div v-if="currentOrder" class="order-detail">
      <!-- 状态步骤条 -->
      <div class="detail-steps">
        <el-steps :active="getStepActive(currentOrder.status)" finish-status="success" align-center>
          <el-step title="提交订单" />
          <el-step title="待发货" />
          <el-step title="已发货" />
          <el-step title="已完成" />
        </el-steps>
      </div>

      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="订单号">{{ currentOrder.id }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatDate(currentOrder.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="price-text">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getPaymentStatusType(currentOrder.paymentStatus)">{{ getPaymentStatusLabel(currentOrder.paymentStatus) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="product-list-section">
        <h4 class="section-title">商品清单</h4>
        <el-table :data="orderItems" border stripe>
          <el-table-column prop="productName" label="商品名称" min-width="150" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="小计" width="120" align="right">
            <template #default="{ row }">
              <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { SuccessFilled } from '@element-plus/icons-vue'

const props = defineProps<{
  orders: any[]
}>()

const orders = ref(props.orders)
const showDetail = ref(false)
const currentOrder = ref<any>(null)
const orderItems = ref<any[]>([])

// Payment state
const showPayDialog = ref(false)
const payingOrder = ref<any>(null)
const payMethod = ref('wechat')
const payStatus = ref('waiting') // waiting, processing, success

watch(() => props.orders, (newVal) => {
  orders.value = newVal
}, { deep: true })

function getOrderStatusType(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'PENDING': 'warning', 'PROCESSING': 'primary', 'SHIPPED': 'success',
    'DELIVERED': 'success', 'COMPLETED': 'success', 'CANCELLED': 'info'
  }
  return map[s] || 'info'
}

function getOrderStatusLabel(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'PENDING': '待处理', 'PROCESSING': '处理中', 'SHIPPED': '已发货',
    'DELIVERED': '已送达', 'COMPLETED': '已完成', 'CANCELLED': '已取消'
  }
  return map[s] || status
}

function getPaymentStatusType(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'UNPAID': 'danger', 'PAID': 'success', 'REFUNDED': 'warning'
  }
  return map[s] || 'info'
}

function getPaymentStatusLabel(status: string) {
  const s = (status || '').toString().toUpperCase().trim()
  const map: any = {
    'UNPAID': '未支付', 'PAID': '已支付', 'REFUNDED': '已退款'
  }
  return map[s] || status
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

function isUnpaid(order: any) {
  const s = (order.paymentStatus || '').toUpperCase()
  return s === 'UNPAID'
}

function getStepActive(status: string) {
  const s = (status || '').toUpperCase()
  if (s === 'PENDING') return 1
  if (s === 'PROCESSING') return 2
  if (s === 'SHIPPED') return 3
  if (s === 'DELIVERED' || s === 'COMPLETED') return 4
  return 0
}

async function viewDetail(row: any) {
  currentOrder.value = row
  try {
    const { data } = await http.get(`/orders/${row.id}/items`)
    orderItems.value = data || []
  } catch (e) {
    console.error('Failed to load order items', e)
    orderItems.value = []
  }
  showDetail.value = true
}

function payOrder(order: any) {
  payingOrder.value = order
  payMethod.value = 'wechat'
  payStatus.value = 'waiting'
  showPayDialog.value = true
}

async function confirmPay() {
  if (!payingOrder.value) return
  
  payStatus.value = 'processing'
  try {
    // Simulate payment delay
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Simulate payment API call
    await http.put(`/orders/${payingOrder.value.id}/status`, { status: 'PROCESSING', paymentStatus: 'PAID' })
    
    // Show success animation
    payStatus.value = 'success'
    
    // Update local state
    const idx = orders.value.findIndex(o => o.id === payingOrder.value.id)
    if (idx !== -1) {
      orders.value[idx].status = 'PROCESSING'
      orders.value[idx].paymentStatus = 'PAID'
    }
    
    // Close dialog after delay
    setTimeout(() => {
      showPayDialog.value = false
      payStatus.value = 'waiting'
    }, 1500)
    
  } catch (e) {
    ElMessage.error('支付失败')
    payStatus.value = 'waiting'
  }
}
</script>

<style scoped>
/* Pay Dialog Styles */
.pay-dialog-content {
  padding: 10px;
}
.pay-amount {
  text-align: center;
  margin-bottom: 30px;
}
.pay-amount .label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
.pay-amount .value {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  font-family: 'DIN', sans-serif;
}
.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.pay-method-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.pay-method-item:hover {
  border-color: #ccc;
}
.pay-method-item.active {
  border-color: var(--brand);
  background: #fffbfb;
}
.pay-method-item .icon {
  width: 28px;
  height: 28px;
  margin-right: 12px;
}
.pay-method-item span {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
}
.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
}
.pay-method-item.active .radio-circle {
  border-color: var(--brand);
}
.pay-method-item.active .radio-circle::after {
  content: '';
  position: absolute;
  top: 4px;
  left: 4px;
  width: 8px;
  height: 8px;
  background: var(--brand);
  border-radius: 50%;
}
.pay-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.pay-success-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}
.success-icon {
  font-size: 64px;
  color: #67C23A;
  margin-bottom: 16px;
  animation: scaleIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.success-text {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}
.success-sub {
  font-size: 14px;
  color: #999;
}

@keyframes scaleIn {
  from { transform: scale(0); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.my-orders-container {
  padding: 10px;
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.order-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}
.order-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}
.order-no {
  font-size: 14px;
  color: #666;
}
.order-no .value {
  font-weight: 600;
  color: #333;
  font-family: monospace;
}
.order-body {
  padding: 20px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.info-item .label {
  font-size: 12px;
  color: #999;
}
.info-item .amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--brand);
  font-family: 'DIN', sans-serif;
}
.info-item .time {
  font-size: 14px;
  color: #333;
}
.order-footer {
  padding: 12px 20px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  justify-content: flex-end;
  background: #fff;
}
.actions {
  display: flex;
  gap: 12px;
}

/* 详情弹窗样式 */
.detail-steps {
  margin-bottom: 30px;
  padding: 10px 0;
}
.detail-info {
  margin-bottom: 30px;
}
.price-text {
  color: var(--brand);
  font-weight: 700;
  font-size: 16px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid var(--brand);
}
.subtotal {
  color: var(--brand);
  font-weight: 600;
}

@media (max-width: 600px) {
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .order-footer {
    justify-content: stretch;
  }
  .actions {
    width: 100%;
  }
  .actions .el-button {
    flex: 1;
  }
}
</style>
