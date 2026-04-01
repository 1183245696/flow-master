<template>
  <view class="login-page">
    <view class="logo-area">
      <image src="/static/logo.png" class="logo" mode="aspectFit" />
      <text class="app-name">OA Platform</text>
      <text class="subtitle">企业一体化办公平台</text>
    </view>
    <view class="form-area">
      <view class="input-wrap">
        <input class="input" v-model="form.username" placeholder="请输入用户名" type="text" />
      </view>
      <view class="input-wrap">
        <input class="input" v-model="form.password" placeholder="请输入密码" type="password" />
      </view>
      <button class="login-btn" :loading="loading" @click="handleLogin">登 录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    uni.switchTab({ url: '/pages/process/index' })
  } catch {
    // error shown by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; background: linear-gradient(160deg, #1e3a5f, #0a1628);
              display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40rpx; }
.logo-area { text-align: center; margin-bottom: 80rpx; }
.logo { width: 120rpx; height: 120rpx; border-radius: 24rpx; }
.app-name { display: block; color: #fff; font-size: 44rpx; font-weight: bold; margin-top: 20rpx; }
.subtitle { display: block; color: rgba(255,255,255,0.6); font-size: 26rpx; margin-top: 8rpx; }
.form-area { width: 100%; background: #fff; border-radius: 24rpx; padding: 48rpx 40rpx; }
.input-wrap { border-bottom: 1rpx solid #eee; margin-bottom: 32rpx; }
.input { height: 80rpx; font-size: 30rpx; width: 100%; }
.login-btn { margin-top: 24rpx; background: #409eff; color: #fff; border-radius: 48rpx;
             height: 90rpx; font-size: 34rpx; letter-spacing: 8rpx; }
</style>
