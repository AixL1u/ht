<template>
  <div class="page-banner" :style="bgStyle">
    <div class="banner-inner">
      <h1 class="banner-title">{{ displayTitle }}</h1>
      <p class="banner-sub" v-if="displaySub">{{ displaySub }}</p>
    </div>
    <div class="banner-mask"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import http from '../api/http'

const props = defineProps<{
  title: string
  sub?: string
  image?: string
  pageKey?: string  // 页面标识，用于从后端获取配置
  theme?: 'default' | 'dark' | 'light'
}>()

const bannerConfig = ref<any>(null)

onMounted(async () => {
  if (props.pageKey) {
    try {
      const { data } = await http.get(`/page-banners/${props.pageKey}`)
      if (data && data.status === 'enabled') {
        bannerConfig.value = data
      }
    } catch (e: any) {
      // 静默处理404和400错误，使用默认配置
      if (e.response?.status !== 404 && e.response?.status !== 400) {
        console.warn('Failed to load banner config:', e)
      }
    }
  }
})

const displayTitle = computed(() => bannerConfig.value?.title || props.title)
const displaySub = computed(() => bannerConfig.value?.subtitle || props.sub)

const bgStyle = computed(() => {
  const img = bannerConfig.value?.imageUrl || props.image
  if (img) {
    return { backgroundImage: `url(${img})` }
  }
  return {}
})
</script>

<style scoped>
.page-banner {
  position: relative;
  height: 350px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2c3e50 0%, #4ca1af 100%); /* Fallback gradient */
  background-size: cover;
  background-position: center;
  overflow: hidden;
  margin-bottom: 40px;
  padding-top: 0; /* 导航栏透明覆盖在banner上 */
}

/* Texture Overlay */
.page-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  z-index: 1;
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1;
}

.banner-inner {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  padding: 0 20px;
  animation: fadeInUp 0.8s ease-out;
}

.banner-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: 2px;
  font-family: "Noto Serif SC", serif;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.banner-sub {
  font-size: 18px;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
  font-weight: 300;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .page-banner { height: 200px; }
  .banner-title { font-size: 28px; }
  .banner-sub { font-size: 14px; }
}
</style>
