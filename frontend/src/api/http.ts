import axios from 'axios'

// 开发环境使用 /api 代理，生产环境使用完整 URL
const apiBase = import.meta.env.DEV ? '/api' : (import.meta.env.VITE_API_BASE || 'http://localhost:8080/api')
const http = axios.create({ baseURL: apiBase })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    ;(config.headers as any).Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        // unwrap to inner data so callers keep using { data }
        res.data = body.data
        return res
      } else {
        // normalize error to match existing error handling paths
        return Promise.reject({ response: { data: { message: body.message, code: body.code } }, status: res.status })
      }
    }
    return res
  },
  (err) => {
    // 只有在请求需要认证的 API 时返回 401 才清除 token
    const url = err?.config?.url || ''
    const needsAuth = url.includes('/users/me') || url.includes('/admin/') || url.includes('/orders') || url.includes('/reservations')
    
    if (err?.response?.status === 401 && needsAuth) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (location.pathname !== '/login') location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default http

