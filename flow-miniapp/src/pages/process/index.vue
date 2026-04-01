<template>
  <view class="page">
    <!-- Top right bell -->
    <view class="nav-bell" @click="goMessages">
      <text class="bell-icon">🔔</text>
      <text v-if="unreadCount > 0" class="bell-badge">{{ unreadCount }}</text>
    </view>

    <!-- Category tabs -->
    <scroll-view scroll-x class="tab-scroll">
      <view class="tab-bar">
        <view
          v-for="cat in categories"
          :key="cat.code"
          :class="['tab-item', activeCategory === cat.code ? 'tab-active' : '']"
          @click="activeCategory = cat.code"
        >{{ cat.name }}</view>
      </view>
    </scroll-view>

    <!-- Process cards -->
    <scroll-view scroll-y class="process-list">
      <view
        v-for="proc in filteredProcesses"
        :key="proc.id"
        class="process-card"
        @click="onProcessClick(proc)"
      >
        <view class="card-icon-wrap">
          <text class="card-emoji">📋</text>
        </view>
        <view class="card-info">
          <text class="card-name">{{ proc.name }}</text>
          <text class="card-desc">{{ proc.description || proc.categoryCode }}</text>
        </view>
        <text class="card-arrow">›</text>
      </view>
      <view v-if="filteredProcesses.length === 0" class="empty">
        <text>暂无流程</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { processApi, messageApi } from '@/api'

const processes = ref<any[]>([])
const categories = ref<any[]>([{ code: '', name: '全部' }])
const activeCategory = ref('')
const unreadCount = ref(0)

const filteredProcesses = computed(() =>
  processes.value.filter(p => !activeCategory.value || p.categoryCode === activeCategory.value)
)

function onProcessClick(proc: any) {
  // Check if user has pending task for this process
  uni.navigateTo({ url: `/pages/process/start?processKey=${proc.processKey}&name=${proc.name}` })
}

function goMessages() {
  uni.navigateTo({ url: '/pages/messages/index' })
}

onMounted(async () => {
  const [procs, cats, count]: any[] = await Promise.allSettled([
    processApi.listLatest(),
    processApi.listCategories(),
    messageApi.unreadCount()
  ])
  if (procs.status === 'fulfilled') processes.value = procs.value || []
  if (cats.status === 'fulfilled') {
    categories.value = [{ code: '', name: '全部' }, ...(cats.value || [])]
  }
  if (count.status === 'fulfilled') unreadCount.value = count.value || 0
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #f5f5f5; }
.nav-bell { position: fixed; top: 20rpx; right: 30rpx; z-index: 99; }
.bell-icon { font-size: 48rpx; }
.bell-badge { position: absolute; top: -8rpx; right: -8rpx; background: #f56c6c;
              color: #fff; font-size: 20rpx; border-radius: 20rpx; padding: 2rpx 8rpx; min-width: 32rpx; text-align: center; }
.tab-scroll { background: #fff; flex-shrink: 0; }
.tab-bar { display: flex; padding: 0 20rpx; white-space: nowrap; }
.tab-item { display: inline-block; padding: 24rpx 28rpx; font-size: 28rpx; color: #666;
            border-bottom: 4rpx solid transparent; }
.tab-active { color: #409eff; border-bottom-color: #409eff; font-weight: 600; }
.process-list { flex: 1; padding: 20rpx; }
.process-card { display: flex; align-items: center; background: #fff; border-radius: 16rpx;
                padding: 28rpx 24rpx; margin-bottom: 16rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,.06); }
.card-icon-wrap { width: 80rpx; height: 80rpx; border-radius: 20rpx; background: #ecf5ff;
                  display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.card-emoji { font-size: 40rpx; }
.card-info { flex: 1; margin: 0 20rpx; }
.card-name { display: block; font-size: 30rpx; font-weight: 600; color: #303133; }
.card-desc { display: block; font-size: 24rpx; color: #909399; margin-top: 6rpx; }
.card-arrow { font-size: 40rpx; color: #c0c4cc; }
.empty { text-align: center; padding: 80rpx; color: #909399; font-size: 28rpx; }
</style>
