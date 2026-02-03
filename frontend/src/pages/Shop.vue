<template>
  <div class="page-section">
    <PageBanner 
      title="文创商城" 
      sub="传统与现代的碰撞，带走一份文化记忆"
      pageKey="shop" 
      image="https://images.unsplash.com/photo-1582650625119-3a31f8fa2699?w=1600&q=80"
    />

    <div class="container">
      <!-- Filter -->
      <div class="shop-filter">
        <el-radio-group v-model="filterCategoryId" size="large">
          <el-radio-button :label="0">全部商品</el-radio-button>
          <el-radio-button v-for="cat in categories" :key="cat.id" :label="cat.id">{{ cat.name }}</el-radio-button>
        </el-radio-group>
        
        <div class="search-box">
          <el-input v-model="searchKeyword" placeholder="搜索商品..." prefix-icon="Search" clearable />
        </div>
      </div>

      <!-- Product List -->
      <div class="product-grid" v-if="filteredProducts.length > 0">
        <div class="product-card" v-for="p in filteredProducts" :key="p.id" @click="$router.push(`/shop/${p.id}`)">
          <div class="prod-img">
            <img :src="p.cover || 'https://images.unsplash.com/photo-1578320780823-d71695267682?w=800&q=80'" />
          </div>
          <div class="prod-info">
            <h3 class="prod-name" :title="p.name">{{ p.name }}</h3>
            <p class="prod-desc">{{ p.description }}</p>
            <div class="prod-bottom">
              <div class="prod-price">
                <span class="currency">¥</span>
                <span class="amount">{{ p.price }}</span>
              </div>
              <div class="prod-sold">已售 {{ Math.floor(Math.random() * 100) + 10 }}</div>
            </div>
            <el-button type="danger" plain style="width: 100%; margin-top: 12px" @click.stop="addToCart(p)">加入购物车</el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无商品" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../store/cart'
import { Search, ShoppingCart, Star } from '@element-plus/icons-vue'
import PageBanner from '../components/PageBanner.vue'

const products = ref<any[]>([])
const categories = ref<any[]>([])
const isAuthed = ref<boolean>(!!localStorage.getItem('token'))
const cartStore = useCartStore()
const filterCategoryId = ref(0)
const searchKeyword = ref('')

onMounted(async () => {
  try {
    const [prodRes, catRes] = await Promise.all([
      http.get('/products'),
      http.get('/product-categories')
    ])
    products.value = prodRes.data || []
    categories.value = catRes.data || []
  } catch (e) { console.error(e) }
})

const filteredProducts = computed(() => {
  return products.value.filter(p => {
    const matchCategory = filterCategoryId.value === 0 || p.categoryId === filterCategoryId.value
    const matchKeyword = !searchKeyword.value || p.name.includes(searchKeyword.value)
    return matchCategory && matchKeyword
  })
})

function addToCart(p: any) {
  if (!isAuthed.value) {
    ElMessage.warning('请先登录')
    return
  }
  cartStore.addToCart(p)
  ElMessage.success('已加入购物车')
}
</script>

<style scoped>
.page-section {
  min-height: 100vh;
  background-color: #f8fafc;
  padding-bottom: 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.shop-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-box {
  width: 300px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px -5px rgba(0,0,0,0.1);
  border-color: #fbcfe8;
}

.prod-img {
  height: 240px;
  position: relative;
  overflow: hidden;
  background: #f8fafc;
}

.prod-img img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* contain might be better for products, but cover looks fuller */
  transition: transform 0.5s;
}

.product-card:hover .prod-img img {
  transform: scale(1.05);
}

.prod-actions {
  position: absolute;
  bottom: -50px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 10px;
  transition: bottom 0.3s;
  background: linear-gradient(to top, rgba(0,0,0,0.1), transparent);
}

.product-card:hover .prod-actions {
  bottom: 10px;
}

.prod-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.prod-name {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.prod-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 12px;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.prod-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: auto;
}

.prod-price {
  color: #ec4899;
  font-weight: 700;
  line-height: 1;
}

.currency {
  font-size: 14px;
  margin-right: 2px;
}

.amount {
  font-size: 20px;
}

.prod-sold {
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 768px) {
  .shop-filter { flex-direction: column; align-items: stretch; }
  .search-box { width: 100%; }
}
</style>

