<template>
  <div class="app-layout">
    <header class="app-header" :class="{ 'is-scrolled': isScrolled, 'home-mode': hasBanner }" v-if="shouldShowHeader" @mouseleave="handleHeaderLeave">
      <div class="header-inner">
        <!-- Left: Brand -->
        <a class="brand" href="/">
          <svg class="logo-icon" viewBox="0 0 48 48" fill="none">
            <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M24 14l-10 5v10l10 5 10-5V19l-10-5z" fill="currentColor" opacity="0.3"/>
            <path d="M24 24l-6 3v6l6 3 6-3v-6l-6-3z" fill="currentColor"/>
          </svg>
          <span class="logo-text">非遗传承</span>
        </a>

        <!-- Date Info -->
        <div class="date-info desktop-only">
          <span class="date-text">{{ dateInfo.date }}</span>
          <span class="date-divider">|</span>
          <span class="date-text">{{ dateInfo.weekday }}</span>
          <span class="date-divider">|</span>
          <span class="date-text">{{ dateInfo.lunar }}</span>
          <span class="date-text solar-term" v-if="dateInfo.solarTerm">{{ dateInfo.solarTerm }}</span>
        </div>
        
        <!-- Center: Menu -->
        <nav class="nav-menu desktop-only">
          <div 
            v-for="item in menuItems" 
            :key="item.path" 
            class="nav-item"
            @mouseenter="handleMenuEnter(item)"
          >
            <router-link 
              :to="item.path" 
              class="nav-link" 
              active-class="active"
              :class="{ 'has-sub': item.children }"
            >
              {{ item.label }}
            </router-link>
          </div>
        </nav>

        <!-- Right: Actions -->
        <div class="nav-right">
          <div class="search-trigger desktop-only">
            <SearchBar />
          </div>
          
          <template v-if="isAuthed">
            <el-dropdown trigger="click" popper-class="cart-dropdown">
              <div class="cart-btn">
                <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0" class="cart-badge">
                  <el-icon><ShoppingCart /></el-icon>
                </el-badge>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/cart')">购物车</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/orders')">我的订单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <el-dropdown trigger="click" popper-class="user-dropdown">
              <div class="user-profile">
                <el-avatar :size="32" :src="currentUser?.avatarUrl || 'https://ui-avatars.com/api/?background=random'" />
                <span class="username desktop-only">{{ currentUser?.nickname || currentUser?.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="currentUser?.role === 'ADMIN'" @click="router.push('/admin')">后台管理</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/me')">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <el-button type="primary" round class="login-btn" @click="goLogin">登录 / 注册</el-button>
          </template>

          <div class="mobile-menu-toggle" @click="mobileMenuOpen = !mobileMenuOpen">
            <el-icon size="24"><component :is="mobileMenuOpen ? 'Close' : 'Menu'" /></el-icon>
          </div>
        </div>
      </div>

      <!-- Sub Menu Panel -->
      <div class="sub-nav-panel" :class="{ 'is-visible': activeSubMenu.length > 0 }">
        <div class="sub-nav-inner">
          <router-link 
            v-for="sub in activeSubMenu" 
            :key="sub.path" 
            :to="sub.path" 
            class="sub-nav-item"
          >
            {{ sub.label }}
          </router-link>
        </div>
      </div>
    </header>

    <!-- Mobile Menu Overlay -->
    <transition name="fade">
      <div v-if="mobileMenuOpen" class="mobile-menu-overlay" @click="mobileMenuOpen = false"></div>
    </transition>
    <transition name="slide-right">
      <div v-if="mobileMenuOpen" class="mobile-menu-drawer">
        <div class="mobile-menu-header">
          <span class="logo-text">非遗传承</span>
          <el-icon size="24" @click="mobileMenuOpen = false"><Close /></el-icon>
        </div>
        <div class="mobile-nav-links">
          <router-link to="/" class="m-link" @click="mobileMenuOpen = false">首页</router-link>
          <router-link to="/projects" class="m-link" @click="mobileMenuOpen = false">非遗项目</router-link>
          <router-link to="/inheritors" class="m-link" @click="mobileMenuOpen = false">传承人</router-link>
          <router-link to="/activities" class="m-link" @click="mobileMenuOpen = false">活动预约</router-link>
          <router-link to="/shop" class="m-link" @click="mobileMenuOpen = false">文创商城</router-link>
          <router-link to="/news" class="m-link" @click="mobileMenuOpen = false">资讯公告</router-link>
        </div>
      </div>
    </transition>

    <main class="app-main" :class="{ 'home-layout': hasBanner, 'no-header': !shouldShowHeader }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="app-footer" v-if="shouldShowHeader">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="brand-logo">
              <svg class="logo-icon" viewBox="0 0 48 48" fill="none">
                <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
                <path d="M24 14l-10 5v10l10 5 10-5V19l-10-5z" fill="currentColor" opacity="0.3"/>
                <path d="M24 24l-6 3v6l6 3 6-3v-6l-6-3z" fill="currentColor"/>
              </svg>
              <span class="logo-text">非遗传承</span>
            </div>
            <p>致力于保护和传承非物质文化遗产，<br>让传统文化焕发新的生机。</p>
          </div>
          <div class="footer-links">
            <h4>探索</h4>
            <router-link to="/projects">非遗项目</router-link>
            <router-link to="/inheritors">传承人</router-link>
            <router-link to="/videos">视频展览</router-link>
          </div>
          <div class="footer-links">
            <h4>服务</h4>
            <router-link to="/activities">活动预约</router-link>
            <router-link to="/shop">文创商城</router-link>
            <router-link to="/news">资讯公告</router-link>
          </div>
          <div class="footer-contact">
            <h4>联系我们</h4>
            <p>Email: contact@heritage.cn</p>
            <p>Tel: 010-88888888</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2025 非遗传承信息管理系统 · 保留所有权利</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from './store/cart'
import { ShoppingCart, ArrowDown, Close, Menu } from '@element-plus/icons-vue'
import http from './api/http'
import SearchBar from './components/SearchBar.vue'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

// Navigation Data
const menuItems = [
  { label: '首页', path: '/' },
  { 
    label: '非遗文化', 
    id: 'culture',
    path: '/projects', // 点击一级菜单也可跳转
    children: [
      { label: '非遗项目', path: '/projects' },
      { label: '非遗地图', path: '/projects/map' },
      { label: '知识图谱', path: '/projects/graph' },
      { label: '传承人', path: '/inheritors' },
      { label: '视频展览', path: '/videos' }
    ]
  },
  { 
    label: '生活服务', 
    id: 'service',
    path: '/activities',
    children: [
      { label: '活动预约', path: '/activities' },
      { label: '文创商城', path: '/shop' }
    ]
  },
  { label: '资讯公告', path: '/news' }
]

const activeMenuId = ref<string | null>(null)
const activeSubMenu = computed(() => {
  const item = menuItems.find(m => m.id === activeMenuId.value)
  return item?.children || []
})

const isScrolled = ref(false)
const mobileMenuOpen = ref(false)
const currentUser = ref<any>(null)
const isAuthed = ref(false)

// 日期信息
const dateInfo = ref({
  date: '',
  weekday: '',
  lunar: '',
  solarTerm: ''
})

// 农历数据
const lunarInfo = [
  0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
  0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
  0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
  0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
  0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
  0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
  0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
  0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
  0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
  0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
  0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
  0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
  0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
  0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
  0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
  0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
  0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
  0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
  0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
  0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252
]

const lunarMonths = ['正', '二', '三', '四', '五', '六', '七', '八', '九', '十', '冬', '腊']
const lunarDays = ['初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
  '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
  '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十']
const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

// 节气数据
const solarTerms = ['小寒', '大寒', '立春', '雨水', '惊蛰', '春分', '清明', '谷雨', '立夏', '小满', '芒种', '夏至', '小暑', '大暑', '立秋', '处暑', '白露', '秋分', '寒露', '霜降', '立冬', '小雪', '大雪', '冬至']
const solarTermInfo = [0, 21208, 42467, 63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072, 240693, 263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758]

function getLunarDate(date: Date) {
  const baseDate = new Date(1900, 0, 31)
  let offset = Math.floor((date.getTime() - baseDate.getTime()) / 86400000)
  
  let year = 1900
  let daysInYear = 0
  
  for (let i = 1900; i < 2100 && offset > 0; i++) {
    daysInYear = getLunarYearDays(i)
    offset -= daysInYear
    year++
  }
  
  if (offset < 0) {
    offset += daysInYear
    year--
  }
  
  const leapMonth = getLeapMonth(year)
  let isLeap = false
  let month = 1
  
  for (let i = 1; i < 13 && offset > 0; i++) {
    if (leapMonth > 0 && i === leapMonth + 1 && !isLeap) {
      --i
      isLeap = true
      daysInYear = getLeapDays(year)
    } else {
      daysInYear = getLunarMonthDays(year, i)
    }
    
    if (isLeap && i === leapMonth + 1) isLeap = false
    offset -= daysInYear
    if (!isLeap) month++
  }
  
  if (offset < 0) {
    offset += daysInYear
    month--
  }
  
  const day = offset + 1
  return { year, month, day, isLeap }
}

function getLunarYearDays(year: number) {
  let sum = 348
  for (let i = 0x8000; i > 0x8; i >>= 1) {
    sum += (lunarInfo[year - 1900] & i) ? 1 : 0
  }
  return sum + getLeapDays(year)
}

function getLeapMonth(year: number) {
  return lunarInfo[year - 1900] & 0xf
}

function getLeapDays(year: number) {
  if (getLeapMonth(year)) {
    return (lunarInfo[year - 1900] & 0x10000) ? 30 : 29
  }
  return 0
}

function getLunarMonthDays(year: number, month: number) {
  return (lunarInfo[year - 1900] & (0x10000 >> month)) ? 30 : 29
}

function getSolarTerm(date: Date) {
  const year = date.getFullYear()
  const month = date.getMonth()
  const day = date.getDate()
  
  const baseDate = new Date(1900, 0, 6, 2, 5, 0)
  const idx1 = month * 2
  const idx2 = month * 2 + 1
  
  const term1Day = Math.floor((new Date(baseDate.getTime() + solarTermInfo[idx1] * 60000).getTime() - new Date(year, month, 1).getTime()) / 86400000) + 1
  const term2Day = Math.floor((new Date(baseDate.getTime() + solarTermInfo[idx2] * 60000).getTime() - new Date(year, month, 1).getTime()) / 86400000) + 1
  
  if (day === term1Day) return solarTerms[idx1]
  if (day === term2Day) return solarTerms[idx2]
  return ''
}

function updateDateInfo() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  
  const lunar = getLunarDate(now)
  const lunarStr = `农历${lunarMonths[lunar.month - 1]}月${lunarDays[lunar.day - 1]}`
  const solarTerm = getSolarTerm(now)
  
  dateInfo.value = {
    date: `${year}-${month}-${day}`,
    weekday: weekdays[now.getDay()],
    lunar: lunarStr,
    solarTerm: solarTerm
  }
}

const shouldShowHeader = computed(() => {
  return !route.meta.hideHeader
})

// 有 banner 的页面列表，这些页面导航栏透明
const bannerPages = ['/', '/projects', '/activities', '/shop', '/inheritors', '/videos']
const hasBanner = computed(() => {
  // 精确匹配
  if (bannerPages.includes(route.path)) return true
  // 详情页面也使用透明导航栏
  if (route.path.startsWith('/projects/') && route.path !== '/projects/map' && route.path !== '/projects/graph') return true
  if (route.path.startsWith('/inheritors/')) return true
  if (route.path.startsWith('/activities/')) return true
  return false
})

function handleMenuEnter(item: any) {
  if (item.children) {
    activeMenuId.value = item.id
  } else {
    activeMenuId.value = null
  }
}

function handleHeaderLeave() {
  activeMenuId.value = null
}

function refreshAuth() {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  isAuthed.value = !!token
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr)
    } catch (e) {
      currentUser.value = null
    }
  }
}

function handleScroll() {
  isScrolled.value = window.scrollY > 50
}

function goLogin() {
  location.href = '/login'
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  location.href = '/login'
}

onMounted(() => {
  refreshAuth()
  updateDateInfo()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style>
/* Global Reset & Variables */
:root {
  --brand: #c23531;
  --brand-dark: #8b2422;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --bg-body: #f8fafc;
  --header-height: 70px;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
  color: var(--text-primary);
  background: var(--bg-body);
  -webkit-font-smoothing: antialiased;
}

a { text-decoration: none; color: inherit; }
ul { list-style: none; padding: 0; margin: 0; }

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}
</style>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* Header */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 70px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Homepage Transparent Mode (Initial) */
.app-header.home-mode:not(.is-scrolled) {
  background: transparent;
  backdrop-filter: none;
  border-bottom: none;
  box-shadow: none;
}

.app-header.is-scrolled {
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  max-width: 1440px;
  margin: 0 auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--brand);
  transition: all 0.3s ease;
}

/* Homepage Transparent Mode: Brand Color */
.app-header.home-mode:not(.is-scrolled) .brand {
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

/* Homepage Transparent Mode: Logo Text */
.app-header.home-mode:not(.is-scrolled) .logo-text {
  color: #fff;
}

.brand:hover {
  transform: scale(1.02);
}

.logo-icon {
  width: 28px;
  height: 28px;
  color: var(--brand);
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  font-family: "Noto Serif SC", serif;
  letter-spacing: 1px;
  color: #333;
  transition: color 0.3s;
}

/* Date Info */
.date-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-left: 24px;
  padding: 6px 16px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 20px;
  transition: all 0.3s;
}

.app-header.home-mode:not(.is-scrolled) .date-info {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.9);
}

.date-text {
  white-space: nowrap;
}

.date-divider {
  color: #ccc;
  font-weight: 300;
}

.app-header.home-mode:not(.is-scrolled) .date-divider {
  color: rgba(255, 255, 255, 0.5);
}

.solar-term {
  color: var(--brand);
  font-weight: 600;
  margin-left: 4px;
}

.app-header.home-mode:not(.is-scrolled) .solar-term {
  color: #ffd700;
}

.nav-menu {
  display: flex;
  gap: 8px;
  margin-left: 40px;
}

.nav-link {
  text-decoration: none;
  color: #555;
  font-weight: 500;
  font-size: 15px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

/* Homepage Transparent Mode: Nav Links */
.app-header.home-mode:not(.is-scrolled) .nav-link {
  color: #fff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.nav-link:hover {
  color: var(--brand);
  background: rgba(0,0,0,0.03);
}

.app-header.home-mode:not(.is-scrolled) .nav-link:hover {
  background: rgba(255,255,255,0.2);
  color: #fff;
}

.nav-link.active {
  color: var(--brand);
  background: rgba(192, 44, 56, 0.08);
  font-weight: 600;
}

.app-header.home-mode:not(.is-scrolled) .nav-link.active {
  background: #fff;
  color: var(--brand);
  text-shadow: none;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
  position: relative;
}

/* Homepage Transparent Mode: Cart Icon */
.app-header.home-mode:not(.is-scrolled) .cart-btn {
  color: #fff;
}

.cart-btn .el-icon {
  font-size: 22px;
}

.cart-btn:hover {
  background: rgba(0,0,0,0.05);
  color: var(--brand);
}

.app-header.home-mode:not(.is-scrolled) .cart-btn:hover {
  background: rgba(255,255,255,0.2);
  color: #fff;
}

.cart-badge {
  display: flex;
  align-items: center;
}

.cart-badge :deep(.el-badge__content) {
  border: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  top: 0px;
  right: 0px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 20px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.app-header.home-mode:not(.is-scrolled) .user-profile {
  color: #fff;
}

.user-profile:hover {
  background: rgba(0,0,0,0.03);
  border-color: rgba(0,0,0,0.05);
}

.app-header.home-mode:not(.is-scrolled) .user-profile:hover {
  background: rgba(255,255,255,0.2);
  border-color: transparent;
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.app-header.home-mode:not(.is-scrolled) .username {
  color: #fff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.login-btn {
  padding: 8px 24px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.mobile-menu-btn {
  display: none;
  font-size: 24px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
}

/* Dropdown Styles - Deprecated, keeping for reference or removal */
/* Sub Nav Panel */
.sub-nav-panel {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: rgba(255, 255, 255, 0.95); /* Default White Frosted */
  backdrop-filter: blur(20px);
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease, background 0.3s ease;
  opacity: 0;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  border-top: 1px solid rgba(0,0,0,0.05);
}

/* Transparent Mode Adaptation for Sub Nav */
.app-header.home-mode:not(.is-scrolled) .sub-nav-panel {
  background: rgba(0, 0, 0, 0.6); /* Dark Transparent for Banner */
  border-top: 1px solid rgba(255,255,255,0.1);
  box-shadow: none;
}

.sub-nav-panel.is-visible {
  max-height: 60px;
  opacity: 1;
}

.sub-nav-inner {
  max-width: 1440px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  padding: 0 32px;
}

.sub-nav-item {
  color: #555;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
  position: relative;
  padding: 4px 0;
}

/* Transparent Mode Adaptation for Sub Nav Items */
.app-header.home-mode:not(.is-scrolled) .sub-nav-item {
  color: rgba(255, 255, 255, 0.9);
}

.sub-nav-item:hover {
  color: var(--brand);
}

.app-header.home-mode:not(.is-scrolled) .sub-nav-item:hover {
  color: #fff;
  text-shadow: 0 0 8px rgba(255,255,255,0.5);
}

.sub-nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--brand);
  transition: all 0.3s;
  transform: translateX(-50%);
}

.app-header.home-mode:not(.is-scrolled) .sub-nav-item::after {
  background: #fff;
}

.sub-nav-item:hover::after {
  width: 100%;
}

/* Adjust Nav Menu for Hover trigger */
.nav-item {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
}

.nav-link.has-sub::after {
  /* Indicator for sub menu if needed */
}

/* Hide sub panel when mobile menu is active (if needed logic) */


/* Main Content */
.app-main {
  padding-top: var(--header-height);
  flex: 1;
}

.app-main.home-layout {
  padding-top: 0;
}

.app-main.no-header {
  padding-top: 0;
}

/* Footer */
.app-footer {
  background: #1e293b;
  color: #fff;
  padding: 24px 0 12px; /* 大幅减少上下内边距 */
  margin-top: auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 20px;
  margin-bottom: 16px; /* 减少底部间距 */
}

.footer-brand .brand-logo {
  font-size: 18px; /* 缩小Logo */
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
  font-family: "Noto Serif SC", serif;
}

.footer-brand p {
  color: #94a3b8;
  line-height: 1.4;
  font-size: 12px;
  margin: 0;
}

.footer-links h4, .footer-contact h4 {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px; /* 减少标题下间距 */
  color: #fff;
}

.footer-links a {
  display: block;
  color: #94a3b8;
  margin-bottom: 4px; /* 减少链接间距 */
  font-size: 12px;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #fff;
}

.footer-contact p {
  color: #94a3b8;
  margin-bottom: 4px; /* 减少联系方式间距 */
  font-size: 12px;
}

.footer-bottom {
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 12px; /* 减少版权区顶部间距 */
  text-align: center;
  color: #64748b;
  font-size: 12px;
}

/* Mobile Menu */
.mobile-menu-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1001;
}

.mobile-menu-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 280px;
  background: #fff;
  z-index: 1002;
  padding: 20px;
  box-shadow: -4px 0 20px rgba(0,0,0,0.1);
}

.mobile-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.mobile-nav-links {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.m-link {
  font-size: 16px;
  color: var(--text-primary);
  padding: 12px;
  border-radius: 8px;
  transition: background 0.3s;
}

.m-link:hover, .m-link.router-link-active {
  background: #f8fafc;
  color: var(--brand);
  font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
  .desktop-only { display: none !important; }
  .mobile-menu-toggle { display: block; }
  .footer-grid { grid-template-columns: 1fr; gap: 30px; }
}

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.3s; }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }
</style>

<style>
/* 导航下拉菜单样式 - 去掉黑框 */
.nav-dropdown {
  border: none !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
  border-radius: 8px !important;
}

.nav-dropdown .el-dropdown-menu {
  border: none !important;
  padding: 8px 0;
}

.nav-dropdown .el-dropdown-menu__item {
  padding: 10px 20px;
  font-size: 14px;
}

.nav-dropdown .el-dropdown-menu__item:hover {
  background-color: #fff5f5;
  color: var(--brand);
}

/* 去掉下拉触发器的黑框 */
.el-dropdown:focus,
.el-dropdown:focus-visible,
.el-dropdown .el-tooltip__trigger:focus,
.el-dropdown .el-tooltip__trigger:focus-visible,
.nav-link.dropdown-trigger:focus,
.nav-link.dropdown-trigger:focus-visible,
.el-dropdown__caret-button:focus {
  outline: none !important;
  border: none !important;
  box-shadow: none !important;
}

.el-dropdown .el-tooltip__trigger {
  outline: none !important;
}
</style>
