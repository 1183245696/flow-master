import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Request interceptor — attach Authorization header
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Response interceptor — handle 401/403
request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code && data.code !== 200) {
      if (data.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const authStore = useAuthStore()
        authStore.logout()
        router.push('/login')
      } else if (data.code === 403) {
        ElMessage.error('无接口请求权限')
      } else {
        ElMessage.error(data.message || '操作失败')
      }
      return Promise.reject(data)
    }
    return data
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error('无接口请求权限')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
