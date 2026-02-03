<template>
  <div class="product-detail-page" v-if="product">
    <div class="container">
      <div class="breadcrumb">
        <router-link to="/shop">文创商城</router-link>
        <span class="sep">/</span>
        <span>{{ product.name }}</span>
      </div>

      <div class="product-main">
        <div class="product-gallery">
          <div class="main-image">
            <img :src="currentImage || product.cover || defaultImage" />
          </div>
          <div class="thumb-list" v-if="imageList.length > 1">
            <div 
              class="thumb-item" 
              v-for="(img, idx) in imageList" 
              :key="idx"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-price">
            <span class="currency">¥</span>
            <span class="amount">{{ product.price }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">库存</span>
            <span class="value">{{ product.stock }} 件</span>
          </div>
          <div class="info-row">
            <span class="label">销量</span>
            <span class="value">{{ product.salesCount || 0 }} 件</span>
          </div>

          <div class="quantity-row">
            <span class="label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
          </div>

          <div class="action-buttons">
            <el-button type="danger" size="large" @click="addToCart">
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </el-button>
            <el-button type="primary" size="large" @click="buyNow">立即购买</el-button>
          </div>
        </div>
      </div>

      <div class="product-detail">
        <div class="detail-header">
          <h2>商品详情</h2>
        </div>
        <div class="detail-content">
          <p>{{ product.description || '暂无详细描述' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import http from '../api/http'
import { useCartStore } from '../store/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const product = ref<any>(null)
const quantity = ref(1)
const currentImage = ref('')
const defaultImage = 'https://images.unsplash.com/photo-1578320780823-d71695267682?w=800&q=80'

const imageList = computed(() => {
  if (!product.value) return []
  const images: string[] = []
  if (product.value.cover) images.push(product.value.cover)
  if (product.value.images) {
    const extra = product.value.images.split(',').filter((s: string) => s.trim())
    images.push(...extra)
  }
  return images.length > 0 ? images : [defaultImage]
})

onMounted(async () => {
  const id = route.params.id
  try {
    const { data } = await http.get(`/products/${id}`)
    product.value = data
    currentImage.value = data.cover || defaultImage
  } catch (e) {
    ElMessage.error('商品不存在')
    router.push('/shop')
  }
})

function addToCart() {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  for (let i = 0; i < quantity.value; i++) {
    cartStore.addToCart(product.value)
  }
  ElMessage.success('已加入购物车')
}

function buyNow() {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  addToCart()
  router.push('/cart')
}
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 24px 0 60px;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
}

.breadcrumb {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 24px;
}

.breadcrumb a {
  color: var(--brand);
  text-decoration: none;
}

.breadcrumb .sep {
  margin: 0 8px;
}

.product-main {
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 32px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.product-gallery {
  flex: 0 0 450px;
}

.main-image {
  width: 100%;
  height: 450px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f5f9;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-list {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.thumb-item {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.thumb-item.active,
.thumb-item:hover {
  border-color: var(--brand);
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px;
}

.product-price {
  background: #fef2f2;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.product-price .currency {
  font-size: 16px;
  color: #dc2626;
}

.product-price .amount {
  font-size: 32px;
  font-weight: 700;
  color: #dc2626;
}

.info-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-row .label {
  width: 80px;
  color: #94a3b8;
  font-size: 14px;
}

.info-row .value {
  color: #334155;
  font-size: 14px;
}

.quantity-row {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.quantity-row .label {
  width: 80px;
  color: #94a3b8;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.action-buttons .el-button {
  padding: 20px 40px;
  font-size: 16px;
}

.product-detail {
  background: #fff;
  padding: 32px;
  border-radius: 12px;
}

.detail-header {
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 16px;
  margin-bottom: 20px;
}

.detail-header h2 {
  font-size: 18px;
  color: #1e293b;
  margin: 0;
  border-left: 4px solid var(--brand);
  padding-left: 12px;
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #475569;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .product-main {
    flex-direction: column;
  }
  .product-gallery {
    flex: none;
  }
  .main-image {
    height: 300px;
  }
}
</style>
