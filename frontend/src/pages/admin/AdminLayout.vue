<template>
  <el-container style="min-height: 100vh; height: 100vh; overflow: hidden;">
    <el-aside width="240px" class="admin-aside" style="height: 100vh; overflow: hidden;">
      <div class="admin-brand">
        <div class="brand-logo-wrapper">
          <svg class="logo-icon" viewBox="0 0 48 48" fill="none">
            <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M24 14l-10 5v10l10 5 10-5V19l-10-5z" fill="currentColor" opacity="0.3"/>
            <path d="M24 24l-6 3v6l6 3 6-3v-6l-6-3z" fill="currentColor"/>
          </svg>
          <span class="brand-text">非遗管理后台</span>
        </div>
      </div>
      <el-menu 
        router 
        :default-active="$route.path" 
        :default-openeds="openeds" 
        :unique-opened="true" 
        class="admin-menu"
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#fff"
      >
        <el-menu-item index="/admin">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <el-sub-menu index="catalog">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>目录数据</span>
          </template>
          <el-menu-item index="/admin/projects">项目管理</el-menu-item>
          <el-menu-item index="/admin/inheritors">传承人管理</el-menu-item>
          <el-menu-item index="/admin/banners">轮播图管理</el-menu-item>
          <el-menu-item index="/admin/page-banners">页面横幅</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="content">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/admin/news">资讯管理</el-menu-item>
          <el-menu-item index="/admin/forum">留言板管理</el-menu-item>
          <el-menu-item index="/admin/media">媒体上传</el-menu-item>
          <el-menu-item index="/admin/media-list">媒体管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="commerce">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>商城管理</span>
          </template>
          <el-menu-item index="/admin/products">商品管理</el-menu-item>
          <el-menu-item index="/admin/product-categories">商品分类</el-menu-item>
          <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="ops">
          <template #title>
            <el-icon><Operation /></el-icon>
            <span>运营与审核</span>
          </template>
          <el-menu-item index="/admin/activities">活动管理</el-menu-item>
          <el-menu-item index="/admin/reservations">预约审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/settings">站点设置</el-menu-item>
        </el-sub-menu>
      </el-menu>
      
      <div class="admin-footer">
        <el-button type="primary" plain size="default" class="footer-btn" @click="goHome">
          <el-icon><House /></el-icon> <span>返回前台</span>
        </el-button>
        <el-button type="danger" plain size="default" class="footer-btn" @click="logout">
          <el-icon><SwitchButton /></el-icon> <span>退出登录</span>
        </el-button>
      </div>
    </el-aside>
    
    <el-main class="admin-main" style="height: 100vh; overflow-y: auto; overflow-x: hidden;">
      <div class="admin-content">
        <router-view />
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Odometer, Collection, Document, Shop, Operation, Setting, SwitchButton, House } from '@element-plus/icons-vue'
import '../../admin-styles.css'

onMounted(() => {
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  if (!user || user.role !== 'ADMIN') {
    location.href = '/login'
  }
})

const route = useRoute()
const openeds = computed(() => {
  const p = route.path
  if (p.startsWith('/admin/projects') || p.startsWith('/admin/inheritors') || p.startsWith('/admin/banners') || p.startsWith('/admin/page-banners')) return ['catalog']
  if (p.startsWith('/admin/news') || p.startsWith('/admin/forum') || p.startsWith('/admin/media') || p.startsWith('/admin/media-list') || p.startsWith('/admin/videos')) return ['content']
  if (p.startsWith('/admin/products') || p.startsWith('/admin/product-categories') || p.startsWith('/admin/orders')) return ['commerce']
  if (p.startsWith('/admin/reservations') || p.startsWith('/admin/activities')) return ['ops']
  if (p.startsWith('/admin/users') || p.startsWith('/admin/settings')) return ['settings']
  return []
})

function goHome() {
  location.href = '/'
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  location.href = '/login'
}
</script>

<style scoped>
.admin-aside {
  background: linear-gradient(180deg, #1a1d29 0%, #0f1419 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 4px 0 24px rgba(0,0,0,0.15);
  z-index: 10;
  position: relative;
  height: 100vh;
  overflow: hidden;
}

.admin-aside::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 300px;
  background: radial-gradient(circle at 50% 0%, rgba(192, 44, 56, 0.12) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.admin-brand {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
  padding: 0 20px;
}

.brand-logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.brand-logo-wrapper:hover {
  transform: scale(1.05);
}

.logo-icon {
  width: 40px;
  height: 40px;
  color: #C02C38;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 8px rgba(192, 44, 56, 0.3));
}

.brand-logo-wrapper:hover .logo-icon {
  color: #E65C65;
  filter: drop-shadow(0 4px 12px rgba(192, 44, 56, 0.5));
}

.brand-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #fff;
  font-family: "Noto Serif SC", serif;
}

.admin-menu {
  border-right: none;
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  z-index: 1;
  min-height: 0;
  background: transparent;
}

.admin-menu::-webkit-scrollbar {
  width: 4px;
}

.admin-menu::-webkit-scrollbar-track {
  background: transparent;
}

.admin-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 2px;
  transition: background 0.3s ease;
}

.admin-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.25);
}

:deep(.el-menu) {
  background: transparent !important;
  border: none !important;
}

:deep(.el-menu-item) {
  margin: 3px 14px;
  border-radius: 12px;
  height: 50px;
  line-height: 50px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  background: transparent !important;
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 14px;
  font-weight: 500;
  padding-left: 20px !important;
}

:deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 12px;
  transition: all 0.3s ease;
}

:deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 0;
  background: linear-gradient(180deg, #C02C38 0%, #E65C65 100%);
  border-radius: 0 2px 2px 0;
  transition: height 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background: linear-gradient(90deg, rgba(192, 44, 56, 0.15) 0%, rgba(192, 44, 56, 0.05) 100%) !important;
  color: #fff !important;
  transform: translateX(4px);
}

:deep(.el-menu-item:hover::before) {
  height: 24px;
}

:deep(.el-menu-item:hover .el-icon) {
  transform: scale(1.1);
  color: #E65C65;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(192, 44, 56, 0.25) 0%, rgba(192, 44, 56, 0.08) 100%) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(192, 44, 56, 0.15);
}

:deep(.el-menu-item.is-active::before) {
  height: 32px;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #E65C65;
  transform: scale(1.15);
}

:deep(.el-sub-menu__title) {
  margin: 3px 14px;
  border-radius: 12px;
  height: 50px;
  line-height: 50px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent !important;
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 14px;
  font-weight: 500;
  padding-left: 20px !important;
}

:deep(.el-sub-menu__title .el-icon) {
  font-size: 18px;
  margin-right: 12px;
  transition: all 0.3s ease;
}

:deep(.el-sub-menu__title:hover) {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.03) 100%) !important;
  color: #fff !important;
  transform: translateX(4px);
}

:deep(.el-sub-menu__title:hover .el-icon) {
  transform: scale(1.1);
  color: #E65C65;
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  background: rgba(255, 255, 255, 0.05) !important;
  color: #fff !important;
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title .el-icon) {
  color: #E65C65;
}

:deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, 0.15) !important;
  margin: 4px 14px 8px;
  border-radius: 8px;
  padding: 4px 0;
}

:deep(.el-sub-menu .el-menu-item) {
  margin: 2px 8px;
  padding-left: 48px !important;
  height: 44px;
  line-height: 44px;
  font-size: 13px;
}

.admin-footer {
  padding: 20px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}

.footer-btn {
  width: 100%;
  margin: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 46px;
  border-radius: 12px !important;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
  background: rgba(255, 255, 255, 0.05) !important;
  color: rgba(255, 255, 255, 0.9) !important;
}

.footer-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.25) !important;
  background: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}

.footer-btn:active {
  transform: translateY(0);
}

.footer-btn .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.footer-btn.el-button--primary {
  border-color: rgba(192, 44, 56, 0.3) !important;
  background: rgba(192, 44, 56, 0.15) !important;
}

.footer-btn.el-button--primary:hover {
  border-color: rgba(192, 44, 56, 0.5) !important;
  background: rgba(192, 44, 56, 0.25) !important;
}

.footer-btn.el-button--danger {
  border-color: rgba(239, 68, 68, 0.3) !important;
  background: rgba(239, 68, 68, 0.15) !important;
}

.footer-btn.el-button--danger:hover {
  border-color: rgba(239, 68, 68, 0.5) !important;
  background: rgba(239, 68, 68, 0.25) !important;
}

.admin-main {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 32px;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

.admin-main::-webkit-scrollbar {
  width: 8px;
}

.admin-main::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.03);
}

.admin-main::-webkit-scrollbar-thumb {
  background: rgba(192, 44, 56, 0.2);
  border-radius: 4px;
  transition: background 0.3s ease;
}

.admin-main::-webkit-scrollbar-thumb:hover {
  background: rgba(192, 44, 56, 0.35);
}

.admin-main::before {
  content: '';
  position: fixed;
  top: 0;
  left: 240px;
  right: 0;
  height: 100%;
  background-image: 
    radial-gradient(at 20% 30%, rgba(192, 44, 56, 0.04) 0px, transparent 50%),
    radial-gradient(at 80% 70%, rgba(212, 175, 55, 0.04) 0px, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.admin-content {
  width: 100%;
  max-width: 100%;
  position: relative;
  z-index: 1;
}

@media (max-width: 768px) {
  .admin-main {
    padding: 20px;
  }
  
  .admin-main::before {
    left: 0;
  }
  
  .admin-brand {
    height: 70px;
  }
  
  .brand-logo {
    width: 42px;
    height: 42px;
    font-size: 20px;
  }
  
  .brand-text {
    font-size: 15px;
  }
}
</style>
