import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Projects from '../pages/Projects.vue'
import Activities from '../pages/Activities.vue'
import Videos from '../pages/Videos.vue'
import News from '../pages/News.vue'
import Shop from '../pages/Shop.vue'
import Orders from '../pages/Orders.vue'
import Forum from '../pages/Forum.vue'
import Me from '../pages/Me.vue'
import Login from '../pages/Login.vue'
import Cart from '../pages/Cart.vue'
import AdminLayout from '../pages/admin/AdminLayout.vue'
import AdminDashboard from '../pages/admin/AdminDashboard.vue'
import AdminReservations from '../pages/admin/AdminReservations.vue'
import AdminProducts from '../pages/admin/AdminProducts.vue'
import AdminNews from '../pages/admin/AdminNews.vue'
import AdminForum from '../pages/admin/AdminForum.vue'
import AdminMediaUpload from '../pages/admin/AdminMediaUpload.vue'
import AdminOrders from '../pages/admin/AdminOrders.vue'
import AdminBanners from '../pages/admin/AdminBanners.vue'
import AdminSettings from '../pages/admin/AdminSettings.vue'
import AdminUsers from '../pages/admin/AdminUsers.vue'
import AdminProjects from '../pages/admin/AdminProjects.vue'
import AdminInheritors from '../pages/admin/AdminInheritors.vue'
import Inheritors from '../pages/Inheritors.vue'
import InheritorDetail from '../pages/InheritorDetail.vue'
import ProjectDetail from '../pages/ProjectDetail.vue'
import ActivityDetail from '../pages/ActivityDetail.vue'
import AdminMediaList from '../pages/admin/AdminMediaList.vue'
import AdminProductCategories from '../pages/admin/AdminProductCategories.vue'
import AdminPageBanners from '../pages/admin/AdminPageBanners.vue'
import AdminActivities from '../pages/admin/AdminActivities.vue'
import ProjectMap from '../pages/ProjectMap.vue'
import KnowledgeGraph from '../pages/KnowledgeGraph.vue'
import ProductDetail from '../pages/ProductDetail.vue'
import VideoDetail from '../pages/VideoDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login, meta: { hideHeader: true } },
    { path: '/cart', component: Cart },
    { path: '/', component: Home },
    { path: '/projects', component: Projects },
    { path: '/projects/map', component: ProjectMap },
    { path: '/projects/graph', component: KnowledgeGraph, meta: { hideHeader: true } },
    { path: '/projects/:id', component: ProjectDetail },
    { path: '/activities', component: Activities },
    { path: '/activities/:id', component: ActivityDetail },
    { path: '/videos', component: Videos },
    { path: '/videos/:id', component: VideoDetail },
    { path: '/news', component: News },
    { path: '/shop', component: Shop },
    { path: '/shop/:id', component: ProductDetail },
    { path: '/orders', component: Orders },
    { path: '/forum', component: Forum, meta: { hideHeader: true } },
    { path: '/me', component: Me },
    { path: '/inheritors', component: Inheritors },
    { path: '/inheritors/:id', component: InheritorDetail },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { hideHeader: true },
      children: [
        { path: '', component: AdminDashboard },
        { path: 'reservations', component: AdminReservations },
        { path: 'users', component: AdminUsers },
        { path: 'projects', component: AdminProjects },
        { path: 'inheritors', component: AdminInheritors },
        { path: 'products', component: AdminProducts },
        { path: 'product-categories', component: AdminProductCategories },
        { path: 'news', component: AdminNews },
        { path: 'forum', component: AdminForum },
        { path: 'media', component: AdminMediaUpload },
        { path: 'media-list', component: AdminMediaList },
        { path: 'orders', component: AdminOrders }
        ,{ path: 'banners', component: AdminBanners }
        ,{ path: 'settings', component: AdminSettings }
        ,{ path: 'page-banners', component: AdminPageBanners }
        ,{ path: 'activities', component: AdminActivities }
      ]
    }
  ]
})

// global auth guards
const authRequired = new Set(['/me', '/orders'])
router.beforeEach((to, from, next) => {
  try {
    const token = localStorage.getItem('token')
    const userStr = localStorage.getItem('user')
    const role = userStr ? JSON.parse(userStr).role : null
    if (to.path.startsWith('/admin')) {
      if (!token || role !== 'ADMIN') {
        next(`/login?redirect=${encodeURIComponent(to.fullPath)}`); return
      }
    }
    if (authRequired.has(to.path)) {
      if (!token) { next(`/login?redirect=${encodeURIComponent(to.fullPath)}`); return }
    }
  } catch {}
  next()
})

export default router

