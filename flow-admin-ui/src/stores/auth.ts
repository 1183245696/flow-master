import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('access_token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refresh_token') || '')
  const userId = ref<number | null>(null)
  const username = ref<string>('')
  const permissions = ref<string[]>([])
  const userInfo = ref<any>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(user: string, pass: string) {
    const res: any = await request.post('/v1/auth/login', { username: user, password: pass })
    token.value = res.data.accessToken
    refreshToken.value = res.data.refreshToken
    userId.value = res.data.userId
    username.value = res.data.username
    localStorage.setItem('access_token', res.data.accessToken)
    localStorage.setItem('refresh_token', res.data.refreshToken)
    await loadUserInfo()
  }

  async function loadUserInfo() {
    try {
      const res: any = await request.get('/v1/users/me')
      userInfo.value = res.data
      userId.value = res.data.userId
    } catch (e) {
      console.error('Load user info failed', e)
    }
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userId.value = null
    username.value = ''
    permissions.value = []
    userInfo.value = null
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  }

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  return { token, userId, username, userInfo, isLoggedIn, permissions, login, logout, loadUserInfo, hasPermission }
})
