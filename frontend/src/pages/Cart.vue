<template>
  <div class="page-section cart-page">
    <!-- 标签页切换 -->
    <div class="cart-tab-header">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'cart' }" 
        @click="activeTab = 'cart'"
      >购物车</div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'orders' }" 
        @click="activeTab = 'orders'"
      >我的订单</div>
    </div>

    <!-- 购物车内容 -->
    <div v-if="activeTab === 'cart'">
        <div v-if="cartStore.items.length === 0" class="empty-cart-container">
          <div class="empty-content">
            <el-empty :image-size="200" description=" " />
            <div class="empty-text">
              <h3>您的购物车还是空的</h3>
              <p>快去挑选喜欢的非遗文创产品吧</p>
            </div>
            <router-link to="/shop">
              <el-button type="primary" size="large" round class="go-shop-btn">
                去逛逛商品
              </el-button>
            </router-link>
          </div>
        </div>

        <div v-if="cartStore.items.length > 0">
          <!-- 商品列表卡片 -->
          <div class="cart-section">
            <div class="section-title">
              <el-icon><ShoppingBag /></el-icon>
              <span>购物清单</span>
              <span class="item-count">共 {{ cartStore.totalCount }} 件商品</span>
            </div>
            
            <el-table 
              :data="cartStore.items" 
              class="cart-table" 
              :header-cell-style="{background:'#f8f9fa', color:'#333', height:'50px', fontSize:'14px'}"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column label="商品信息" min-width="400">
                <template #default="{ row }">
                  <div class="product-info">
                    <div class="product-cover">
                      <el-image 
                        :src="row.cover || 'https://via.placeholder.com/80x80?text=No+Image'" 
                        fit="cover"
                        style="width: 100%; height: 100%;"
                      >
                        <template #error>
                          <div class="image-slot">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                    </div>
                    <div class="product-meta">
                      <div class="product-name">{{ row.name }}</div>
                      <div class="product-desc">{{ row.description }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="150" align="center">
                <template #default="{ row }">
                  <span class="unit-price">¥{{ row.price }}</span>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="180" align="center">
                <template #default="{ row }">
                  <el-input-number 
                    v-model="row.quantity" 
                    :min="1" 
                    size="small" 
                    class="custom-input-number"
                    @change="(val: number) => cartStore.updateQuantity(row.id, val)" 
                  />
                </template>
              </el-table-column>
              <el-table-column label="小计" width="150" align="right">
                <template #default="{ row }">
                  <span class="subtotal-price">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <div class="action-btn" @click="cartStore.removeFromCart(row.id)">
                    <el-icon><Delete /></el-icon>
                    <span>删除</span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 收货地址卡片 -->
          <div class="cart-section checkout-section">
            <div class="section-header">
              <div class="section-title">
                <el-icon><Location /></el-icon>
                <span>收货地址</span>
              </div>
              <router-link to="/me" class="manage-addr-link">管理地址</router-link>
            </div>

            <div v-if="addresses.length === 0" class="empty-address">
              <el-empty description="暂无收货地址" :image-size="80">
                <router-link to="/me">
                  <el-button type="primary" plain>去添加地址</el-button>
                </router-link>
              </el-empty>
            </div>

            <div v-else class="address-grid">
              <div 
                v-for="addr in addresses" 
                :key="addr.id" 
                class="address-card"
                :class="{ active: selectedAddressId === addr.id }"
                @click="selectedAddressId = addr.id"
              >
                <div class="addr-header">
                  <span class="addr-name">{{ addr.name }}</span>
                  <span class="addr-phone">{{ addr.phone }}</span>
                </div>
                <div class="addr-body">
                  <span class="region-tag">{{ addr.region }}</span>
                  <span class="detail-text">{{ addr.detail }}</span>
                </div>
                <div class="check-mark" v-if="selectedAddressId === addr.id">
                  <el-icon><Check /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部结算栏 -->
          <div class="cart-footer">
            <div class="footer-left">
              <span class="selected-count">已选 <b>{{ selectedCount }}</b> 件商品</span>
            </div>
            <div class="footer-right">
              <div class="total-info">
                <span class="label">应付总额：</span>
                <span class="price">¥{{ selectedTotalPrice.toFixed(2) }}</span>
              </div>
              <el-button type="primary" size="large" class="checkout-btn" @click="checkout" :loading="submitting" :disabled="selectedCount === 0">
                立即结算
              </el-button>
            </div>
          </div>
        </div>
    </div>

    <!-- 我的订单内容 -->
    <div v-if="activeTab === 'orders'">
      <MyOrders :orders="myOrders" />
    </div>

    <!-- 支付弹窗 -->
    <el-dialog v-model="showPayDialog" title="收银台" width="480px" center destroy-on-close :show-close="payStatus !== 'processing' && payStatus !== 'success'" :close-on-click-modal="false">
      <div class="pay-dialog-content">
        <!-- 支付成功状态 -->
        <div v-if="payStatus === 'success'" class="pay-success-view">
          <el-icon class="success-icon"><SuccessFilled /></el-icon>
          <div class="success-text">支付成功</div>
          <div class="success-sub">即将跳转到订单列表...</div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCartStore } from '../store/cart'
import http from '../api/http'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import MyOrders from '../components/MyOrders.vue'
import { ShoppingBag, Delete, Location, Check, SuccessFilled, Picture } from '@element-plus/icons-vue'

const cartStore = useCartStore()
const router = useRouter()
const addresses = ref<any[]>([])
const selectedAddressId = ref<number | null>(null)
const submitting = ref(false)
const activeTab = ref('cart')
const myOrders = ref<any[]>([])

// Selection state
const selectedItems = ref<any[]>([])
const handleSelectionChange = (val: any[]) => {
  selectedItems.value = val
}
const selectedCount = computed(() => selectedItems.value.length)
const selectedTotalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// Payment state
const showPayDialog = ref(false)
const payingOrder = ref<any>(null)
const payMethod = ref('wechat')
const payStatus = ref('waiting') // waiting, processing, success

onMounted(async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)
  
  // Load addresses
  try {
    const { data } = await http.get('/addresses', { params: { userId: user.id } })
    addresses.value = data
    if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (e) {
    console.error('Failed to load addresses', e)
  }
  
  // Load orders
  loadOrders(user.id)
})

async function loadOrders(userId: number) {
  try {
    const { data } = await http.get('/orders', { params: { userId } })
    console.log('📦 Loaded orders:', data)
    myOrders.value = data || []
  } catch (e) {
    console.error('Failed to load orders', e)
  }
}

async function checkout() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }
  if (!selectedAddressId.value) {
    ElMessage.error('请选择收货地址')
    return
  }
  
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  const user = JSON.parse(userStr)

  submitting.value = true
  try {
    // Create order with SELECTED items
    const items = selectedItems.value.map(i => ({ productId: i.id, quantity: i.quantity }))
    
    const { data: order } = await http.post('/orders', {
      userId: user.id,
      addressId: selectedAddressId.value,
      items: items
    })
    
    ElMessage.success('订单创建成功')
    
    // Remove selected items from cart
    selectedItems.value.forEach(item => {
      cartStore.removeFromCart(item.id)
    })
    
    // Refresh orders
    await loadOrders(user.id)
    
    // Open payment dialog
    payingOrder.value = order
    payMethod.value = 'wechat'
    payStatus.value = 'waiting'
    showPayDialog.value = true
    
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '结算失败')
  } finally {
    submitting.value = false
  }
}

async function confirmPay() {
  if (!payingOrder.value) return
  
  payStatus.value = 'processing'
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    await http.put(`/orders/${payingOrder.value.id}/status`, { status: 'PROCESSING', paymentStatus: 'PAID' })
    
    payStatus.value = 'success'
    
    // Refresh orders list to update status
    const userStr = localStorage.getItem('user')
    if (userStr) loadOrders(JSON.parse(userStr).id)
    
    setTimeout(() => {
      showPayDialog.value = false
      payStatus.value = 'waiting'
      activeTab.value = 'orders' // Switch to orders tab
    }, 1500)
    
  } catch (e) {
    ElMessage.error('支付失败')
    payStatus.value = 'waiting'
  }
}
</script>

<style scoped>
.cart-page {
  padding-top: 90px;
}

.cart-tab-header {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid #eee;
  background: #fff;
  padding: 0 20px;
  border-radius: 12px 12px 0 0;
}
.tab-item {
  padding: 16px 32px;
  font-size: 16px;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.3s;
  font-weight: 500;
}
.tab-item:hover {
  color: var(--brand);
}
.tab-item.active {
  color: var(--brand);
  border-bottom-color: var(--brand);
  font-weight: 600;
}

.empty-cart-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}
.empty-content {
  text-align: center;
  padding: 40px;
}
.empty-text {
  margin: -20px 0 30px;
}
.empty-text h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}
.empty-text p {
  color: #999;
  font-size: 14px;
}
.go-shop-btn {
  padding: 12px 40px;
  font-size: 16px;
  transition: all 0.3s;
}
.go-shop-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--brand-rgb), 0.3);
}

/* Cart Section Styles */
.cart-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  border: 1px solid #f0f0f0;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
}
.item-count {
  font-size: 14px;
  font-weight: normal;
  color: #999;
  margin-left: 8px;
}

/* Product Table */
.cart-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}
.product-info {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.product-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  flex-shrink: 0;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}
.product-meta {
  flex: 1;
  padding-top: 4px;
}
.product-name {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 8px;
  color: #333;
  line-height: 1.4;
}
.product-desc {
  font-size: 13px;
  color: #999;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.unit-price {
  color: #666;
  font-family: 'DIN', sans-serif;
  font-size: 15px;
}
.subtotal-price {
  color: var(--brand);
  font-weight: 700;
  font-size: 16px;
  font-family: 'DIN', sans-serif;
}
.custom-input-number {
  width: 120px;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #999;
  cursor: pointer;
  font-size: 13px;
  transition: color 0.2s;
}
.action-btn:hover {
  color: #ff4d4f;
}

/* Checkout Section */
.checkout-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.manage-addr-link {
  font-size: 14px;
  color: var(--brand);
  text-decoration: none;
}
.manage-addr-link:hover {
  text-decoration: underline;
}
.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.address-card {
  border: 2px solid #eee;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  background: #fff;
}
.address-card:hover {
  border-color: #ccc;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.address-card.active {
  border-color: var(--brand);
  background: #fffbfb;
}
.addr-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 15px;
  color: #333;
}
.addr-phone {
  font-family: 'DIN', sans-serif;
  color: #666;
}
.addr-body {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
}
.region-tag {
  background: #f0f2f5;
  color: #606266;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 8px;
  font-size: 12px;
  white-space: nowrap;
}
.check-mark {
  position: absolute;
  right: 0;
  bottom: 0;
  background: var(--brand);
  color: #fff;
  padding: 4px 8px;
  border-radius: 8px 0 6px 0;
  font-size: 12px;
}

/* Footer */
.cart-footer {
  background: #fff;
  padding: 20px 30px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.08);
  position: sticky;
  bottom: 20px;
  z-index: 100;
  border: 1px solid #ebeef5;
}
.footer-left {
  color: #606266;
  font-size: 14px;
}
.selected-count b {
  color: var(--brand);
  font-size: 18px;
  margin: 0 4px;
  font-weight: 600;
}
.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}
.total-info {
  display: flex;
  align-items: baseline;
}
.total-info .label {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
}
.total-info .price {
  color: #ff4d4f;
  font-size: 32px;
  font-weight: 700;
  font-family: 'DIN', sans-serif;
  line-height: 1;
}
.checkout-btn {
  padding: 12px 48px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 24px;
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(255, 126, 95, 0.4);
  transition: all 0.3s;
}
.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 126, 95, 0.5);
}
.checkout-btn.is-disabled {
  background: #ccc;
  box-shadow: none;
}

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

@media (max-width: 768px) {
  .cart-table {
    display: block;
    overflow-x: auto;
  }
  .cart-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  .footer-right {
    justify-content: space-between;
  }
}
</style>
