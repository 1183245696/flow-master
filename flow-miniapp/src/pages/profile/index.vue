<template>
  <view class="page">
    <!-- Avatar + basic info card -->
    <view class="profile-hero">
      <image class="avatar" :src="userInfo?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="hero-info">
        <text class="hero-name">{{ userInfo?.realName || '-' }}</text>
        <text class="hero-no">工号：{{ userInfo?.employeeNo || '-' }}</text>
      </view>
    </view>

    <!-- Contact info -->
    <view class="info-card">
      <view class="card-title">联系方式</view>
      <view class="info-row"><text class="info-label">手机</text><text class="info-val">{{ userInfo?.phone || '-' }}</text></view>
      <view class="info-row"><text class="info-label">邮箱</text><text class="info-val">{{ userInfo?.email || '-' }}</text></view>
      <view class="info-row"><text class="info-label">用户名</text><text class="info-val">{{ userInfo?.username || '-' }}</text></view>
    </view>

    <!-- Org info -->
    <view class="info-card">
      <view class="card-title">组织信息</view>
      <view class="info-row">
        <text class="info-label">部门路径</text>
        <text class="info-val dept-path">{{ userInfo?.deptPath || userInfo?.deptId || '-' }}</text>
      </view>
      <view class="info-row"><text class="info-label">职位</text><text class="info-val">{{ userInfo?.positionName || '-' }}</text></view>
    </view>

    <!-- Actions -->
    <view class="action-card">
      <view class="action-item" @click="goEdit">
        <text>编辑个人信息</text><text class="arrow">›</text>
      </view>
      <view class="action-item danger" @click="handleLogout">
        <text>退出登录</text><text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

function goEdit() {
  uni.navigateTo({ url: '/pages/profile/edit' })
}

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success(res) {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}

onMounted(() => userStore.loadUserInfo())
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; }
.profile-hero { background: linear-gradient(135deg, #1e3a5f, #2d6a9f); padding: 60rpx 40rpx 40rpx;
                display: flex; align-items: center; gap: 32rpx; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 60rpx; border: 4rpx solid rgba(255,255,255,.4); }
.hero-info { color: #fff; }
.hero-name { font-size: 38rpx; font-weight: 700; display: block; }
.hero-no { font-size: 24rpx; opacity: .8; display: block; margin-top: 8rpx; }
.info-card { background: #fff; margin: 20rpx; border-radius: 16rpx; padding: 28rpx; }
.card-title { font-size: 28rpx; font-weight: 700; color: #303133; margin-bottom: 20rpx;
              padding-bottom: 16rpx; border-bottom: 1rpx solid #f0f0f0; }
.info-row { display: flex; justify-content: space-between; align-items: flex-start;
            padding: 18rpx 0; border-bottom: 1rpx solid #f8f8f8; }
.info-label { color: #909399; font-size: 26rpx; width: 120rpx; flex-shrink: 0; }
.info-val { color: #303133; font-size: 26rpx; flex: 1; text-align: right; }
.dept-path { font-size: 22rpx; word-break: break-all; }
.action-card { background: #fff; margin: 20rpx; border-radius: 16rpx; overflow: hidden; }
.action-item { display: flex; justify-content: space-between; align-items: center;
               padding: 32rpx 28rpx; border-bottom: 1rpx solid #f5f5f5; font-size: 28rpx; color: #303133; }
.action-item.danger { color: #f56c6c; }
.arrow { color: #c0c4cc; font-size: 36rpx; }
</style>
