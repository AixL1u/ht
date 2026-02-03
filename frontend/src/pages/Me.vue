<template>
  <div class="page-section me-page">
    <PageHeader title="个人中心" />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <UserProfile :profile="profile" />
        <ChangePassword style="margin-top:20px" />
      </el-col>
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <MyAddresses :addresses="addresses" :userId="profile.id" @refresh="loadData" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../api/http'
import PageHeader from '../components/PageHeader.vue'
import UserProfile from '../components/UserProfile.vue'
import ChangePassword from '../components/ChangePassword.vue'
import MyAddresses from '../components/MyAddresses.vue'

const profile = ref<any>({})
const addresses = ref<any[]>([])

async function loadData() {
  const u = localStorage.getItem('user')
  if (!u) {
    location.href = '/login'
    return
  }
  
  try {
    const me = await http.get('/users/me').catch(() => ({ data: {} }))
    profile.value = me.data
    const userId = me.data?.id
    
    if (userId) {
      const [a] = await Promise.all([
        http.get('/addresses', { params: { userId } }).catch(() => ({ data: [] }))
      ])
      
      addresses.value = a.data
    }
  } catch (e) {
    console.error('加载用户数据失败:', e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.me-page {
  padding-top: 90px;
}
</style>