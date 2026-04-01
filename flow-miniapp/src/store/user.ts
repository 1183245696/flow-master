import { defineStore } from 'pinia'
import { ref } from 'vue'
import { setToken, clearToken } from '@/utils/request'
import { authApi, profileApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<any>(null)
  const isLoggedIn = ref(!!uni.getStorageSync('access_token'))

  async function login(username: string, password: string) {
    const res: any = await authApi.login(username, password)
    setToken(res.accessToken, res.refreshToken)
    isLoggedIn.value = true
    await loadUserInfo()
  }

  async function loadUserInfo() {
    try {
      userInfo.value = await profileApi.getMe()
    } catch (e) {
      console.error('Load user info failed', e)
    }
  }

  function logout() {
    userInfo.value = null
    isLoggedIn.value = false
    clearToken()
  }

  return { userInfo, isLoggedIn, login, loadUserInfo, logout }
})
