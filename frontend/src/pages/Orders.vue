<template>
  <div class="page-section orders-page">
    <h2 style="margin-bottom: 20px;">我的订单</h2>
    <MyOrders :orders="orders" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../api/http'
import MyOrders from '../components/MyOrders.vue'

const orders = ref<any[]>([])
onMounted(async () => {
  const user = localStorage.getItem('user')
  if (!user) { location.href = '/login'; return }
  const userId = JSON.parse(user).id
  try {
    const { data } = await http.get('/orders', { params: { userId } })
    orders.value = data || []
  } catch (e) {
    console.error('Failed to load orders', e)
  }
})
</script>

<style scoped>
.orders-page {
  padding-top: 90px;
}
</style>