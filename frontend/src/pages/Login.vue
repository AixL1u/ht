<template>
  <div class="login-page" :style="bgStyle">
    <div class="login-card">
      <!-- 左侧品牌 -->
      <div class="card-left">
        <div class="brand">
          <div class="logo-icon">
            <svg viewBox="0 0 48 48" fill="none">
              <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M24 14l-10 5v10l10 5 10-5V19l-10-5z" fill="currentColor" opacity="0.3"/>
              <path d="M24 24l-6 3v6l6 3 6-3v-6l-6-3z" fill="currentColor"/>
            </svg>
          </div>
          <h1 class="logo-text">非遗传承</h1>
          <p class="slogan">守护千年文脉 · 传承民族记忆</p>
        </div>
      </div>
      
      <!-- 右侧表单 -->
      <div class="card-right">
        <span class="close-btn" @click="$router.push('/')">×</span>
        
        <div class="form-box">
          <h2 class="form-title">欢迎登录</h2>
          
          <!-- 身份选择 -->
          <div class="role-tabs">
            <span 
              class="role-tab" 
              :class="{ active: selectedRole === 'user' }"
              @click="selectedRole = 'user'"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              普通用户
            </span>
            <span 
              class="role-tab" 
              :class="{ active: selectedRole === 'admin' }"
              @click="selectedRole = 'admin'"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
              管理员
            </span>
          </div>
          
          <div class="input-group">
            <input type="text" v-model="username" placeholder="用户名" autocomplete="off" />
          </div>
          
          <div class="input-group">
            <div class="input-with-btn">
              <input 
                :type="showPwd ? 'text' : 'password'" 
                v-model="password" 
                placeholder="密码"
                autocomplete="off"
                @keyup.enter="login"
              />
              <span class="eye-btn" @click="showPwd = !showPwd">
                <svg v-if="!showPwd" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </span>
            </div>
          </div>
          
          <div class="options">
            <label class="checkbox-item">
              <input type="checkbox" v-model="rememberMe" />
              <span class="checkmark"></span>
              记住我
            </label>
          </div>
          
          <button class="btn" @click="login" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
          
          <p class="switch-text">没有账号？<a @click="isRegister = true">立即注册</a></p>
        </div>
      </div>
    </div>
    
    <!-- 注册弹窗 -->
    <div class="register-modal" v-if="isRegister" @click.self="isRegister = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>注册账号</h3>
          <span class="close" @click="isRegister = false">×</span>
        </div>
        <div class="input-group">
          <input type="text" v-model="reg.username" placeholder="用户名" />
        </div>
        <div class="input-group">
          <input type="password" v-model="reg.password" placeholder="密码" />
        </div>
        <div class="input-group">
          <input type="password" v-model="reg.confirmPassword" placeholder="确认密码" @keyup.enter="register" />
        </div>
        <button class="btn" @click="register" :disabled="loading">
          {{ loading ? '注册中...' : '立即注册' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { ElMessage } from 'element-plus'

const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPwd = ref(false)
const isRegister = ref(false)
const selectedRole = ref('user')
const reg = ref({ username: '', password: '', confirmPassword: '' })
const loading = ref(false)
const route = useRoute()
const router = useRouter()
const bannerConfig = ref<any>(null)

// 背景样式
const bgStyle = computed(() => {
  if (bannerConfig.value?.imageUrl) {
    return {
      backgroundImage: `url(${bannerConfig.value.imageUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {}
})

onMounted(async () => {
  // 清空表单，防止浏览器自动填充
  username.value = ''
  password.value = ''
  
  try {
    const { data } = await http.get('/page-banners/login')
    if (data && data.status === 'enabled') {
      bannerConfig.value = data
    }
  } catch (e) {
    // 忽略错误，使用默认背景
  }
})

async function login() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const { data } = await http.post('/auth/login', { username: username.value, password: password.value })
    
    // 存储 token 和用户信息
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    
    // 如果勾选了记住我，额外存储一个标记（可用于后续扩展）
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
    } else {
      localStorage.removeItem('rememberMe')
    }
    
    const r = (data.user && data.user.role) || 'USER'
    const rq = (route.query?.redirect as string | undefined) || ''
    let target = '/'
    
    // 根据选择的身份和实际权限跳转
    if (selectedRole.value === 'admin') {
      if (r === 'ADMIN') {
        target = '/admin'
      } else {
        ElMessage.warning('您没有管理员权限')
        target = '/'
      }
    } else {
      if (rq && rq.startsWith('/')) {
        target = (rq.startsWith('/admin') && r !== 'ADMIN') ? '/' : rq
      } else {
        target = '/'
      }
    }
    
    // 设置欢迎标记，在目标页面显示欢迎弹框
    localStorage.setItem('showWelcome', 'true')
    ElMessage.success('登录成功')
    
    // 使用 Vue Router 进行导航，避免页面刷新导致的问题
    // 对于管理后台，使用 location.href 确保完全刷新
    if (target.startsWith('/admin')) {
      window.location.href = target
    } else {
      router.push(target)
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function register() {
  if (!reg.value.username || !reg.value.password) { 
    ElMessage.error('请填写用户名和密码')
    return 
  }
  if (reg.value.password !== reg.value.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await http.post('/auth/register', { username: reg.value.username, password: reg.value.password })
    ElMessage.success('注册成功')
    username.value = reg.value.username
    isRegister.value = false
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  display: flex;
  width: 700px;
  background: #242424;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0,0,0,0.4);
}

.card-left {
  width: 260px;
  background: #b33a3a;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 30px;
}

.brand { text-align: center; }

.logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  color: #fff;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  font-family: "Noto Serif SC", serif;
  margin-bottom: 12px;
  letter-spacing: 4px;
}

.slogan {
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  letter-spacing: 1px;
  line-height: 1.6;
}

.card-right {
  flex: 1;
  padding: 40px 45px;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 20px;
  font-size: 22px;
  color: rgba(255,255,255,0.3);
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover { color: #fff; }

.form-box { padding-top: 10px; }

.form-title {
  color: #fff;
  font-size: 22px;
  font-weight: 500;
  margin-bottom: 24px;
}

/* 身份选择 */
.role-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.role-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 16px;
  background: rgba(255,255,255,0.05);
  border: 2px solid rgba(255,255,255,0.1);
  border-radius: 8px;
  color: rgba(255,255,255,0.5);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.role-tab svg {
  width: 20px;
  height: 20px;
}

.role-tab:hover {
  border-color: rgba(255,255,255,0.2);
  color: rgba(255,255,255,0.7);
}

.role-tab.active {
  background: rgba(179, 58, 58, 0.15);
  border-color: #b33a3a;
  color: #fff;
}

.role-tab.active svg {
  stroke: #b33a3a;
}

.input-group { margin-bottom: 18px; }

.input-group input {
  width: 100%;
  height: 48px;
  background: #f5f5f5;
  border: none;
  border-radius: 6px;
  padding: 0 16px;
  color: #333;
  font-size: 15px;
  outline: none;
}

.input-group input::placeholder { color: #999; }
.input-group input:focus { box-shadow: 0 0 0 2px rgba(179, 58, 58, 0.3); }

.input-with-btn { position: relative; }
.input-with-btn input { padding-right: 50px; }

.eye-btn {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  width: 22px;
  height: 22px;
  color: #999;
  transition: color 0.2s;
}

.eye-btn:hover { color: #666; }
.eye-btn svg { width: 100%; height: 100%; }

.options { margin-bottom: 24px; }

.checkbox-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(255,255,255,0.6);
  font-size: 14px;
  cursor: pointer;
  position: relative;
  padding-left: 26px;
}

.checkbox-item input { display: none; }

.checkmark {
  position: absolute;
  left: 0;
  width: 16px;
  height: 16px;
  background: transparent;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 3px;
}

.checkbox-item input:checked ~ .checkmark {
  background: #b33a3a;
  border-color: #b33a3a;
}

.checkbox-item input:checked ~ .checkmark::after {
  content: '✓';
  position: absolute;
  top: -2px;
  left: 2px;
  color: #fff;
  font-size: 12px;
}

.btn {
  width: 100%;
  height: 48px;
  background: #b33a3a;
  border: none;
  border-radius: 6px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  letter-spacing: 8px;
  transition: all 0.2s;
}

.btn:hover { background: #a03030; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.switch-text {
  text-align: center;
  margin-top: 20px;
  color: rgba(255,255,255,0.5);
  font-size: 14px;
}

.switch-text a {
  color: #b33a3a;
  cursor: pointer;
  margin-left: 4px;
}

.switch-text a:hover { text-decoration: underline; }

/* 注册弹窗 */
.register-modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 360px;
  background: #242424;
  border-radius: 8px;
  padding: 30px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.modal-header h3 { color: #fff; font-size: 18px; font-weight: 500; }
.modal-header .close { color: rgba(255,255,255,0.4); font-size: 24px; cursor: pointer; line-height: 1; }
.modal-header .close:hover { color: #fff; }

@media (max-width: 720px) {
  .login-card { width: 95%; flex-direction: column; }
  .card-left { width: 100%; padding: 40px; }
  .card-right { padding: 30px; }
}
</style>
