import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import './styles.css'
import App from './App.vue'
import router from './router'

try {
  const app = createApp(App)
  
  // Global error handler
  app.config.errorHandler = (err, instance, info) => {
    console.error('Vue Error:', err, info)
  }
  
  // Unhandled promise rejection handler
  window.addEventListener('unhandledrejection', event => {
    console.error('Unhandled Promise Rejection:', event.reason)
  })
  
  app.use(createPinia())
  app.use(router)
  app.use(ElementPlus, { 
    size: 'small',
    locale: zhCn 
  })
  
  // 等待路由准备好后再挂载
  router.isReady().then(() => {
    app.mount('#app')
    console.log('App mounted successfully')
  }).catch(err => {
    console.error('Router initialization failed:', err)
  })
} catch (e) {
  console.error('Failed to initialize app:', e)
}

