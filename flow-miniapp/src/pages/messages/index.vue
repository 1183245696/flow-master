<template>
  <view class="page">
    <!-- Tabs -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        :class="['tab', activeTab === tab.value ? 'tab-active' : '']"
        @click="switchTab(tab.value)"
      >{{ tab.label }}</view>
    </view>

    <!-- Message list -->
    <scroll-view scroll-y class="msg-list" @scrolltolower="loadMore">
      <view
        v-for="msg in messages"
        :key="msg.id"
        :class="['msg-item', !msg.isRead ? 'unread' : '']"
        @click="onMessageClick(msg)"
      >
        <view class="msg-dot" v-if="!msg.isRead" />
        <view class="msg-body">
          <view class="msg-header-row">
            <text class="msg-title">{{ msg.title }}</text>
            <text class="msg-time">{{ formatTime(msg.createTime) }}</text>
          </view>
          <text class="msg-content">{{ msg.content }}</text>
          <view class="msg-tag-row">
            <text :class="['msg-tag', `tag-${msg.category?.toLowerCase()}`]">{{ categoryLabel(msg.category) }}</text>
          </view>
        </view>
      </view>

      <view v-if="messages.length === 0 && !loading" class="empty">
        <text>暂无消息</text>
      </view>
      <view v-if="loading" class="loading"><text>加载中...</text></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { messageApi } from '@/api'

const tabs = [
  { label: '全部',   value: '' },
  { label: '流程消息', value: 'WORKFLOW' },
  { label: '系统消息', value: 'SYSTEM' }
]

const activeTab = ref('')
const messages = ref<any[]>([])
const page = ref(1)
const loading = ref(false)
const hasMore = ref(true)

function categoryLabel(cat: string) {
  const map: Record<string, string> = { WORKFLOW: '流程', SYSTEM: '系统', NOTICE: '公告' }
  return map[cat] || cat
}

function formatTime(dt: string) {
  if (!dt) return ''
  return dt.slice(0, 16).replace('T', ' ')
}

function switchTab(val: string) {
  activeTab.value = val
  page.value = 1
  messages.value = []
  hasMore.value = true
  loadData()
}

async function loadData() {
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res: any = await messageApi.list({
      category: activeTab.value || undefined,
      page: page.value,
      size: 20
    })
    const records = res?.records || []
    messages.value.push(...records)
    hasMore.value = records.length === 20
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (!hasMore.value) return
  page.value++
  await loadData()
}

async function onMessageClick(msg: any) {
  // Mark as read
  if (!msg.isRead) {
    await messageApi.markRead(msg.id)
    msg.isRead = true
  }
  // Navigate to workflow detail if applicable
  if (msg.category === 'WORKFLOW' && msg.sourceId) {
    uni.navigateTo({ url: `/pages/process/detail?instanceId=${msg.sourceId}` })
  }
}

onMounted(loadData)
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #f5f5f5; }
.tab-bar { display: flex; background: #fff; border-bottom: 1rpx solid #f0f0f0; flex-shrink: 0; }
.tab { flex: 1; text-align: center; padding: 24rpx 0; font-size: 26rpx; color: #666; border-bottom: 4rpx solid transparent; }
.tab-active { color: #409eff; border-bottom-color: #409eff; font-weight: 600; }
.msg-list { flex: 1; }
.msg-item { display: flex; background: #fff; margin: 16rpx 20rpx; border-radius: 16rpx;
            padding: 28rpx 24rpx; gap: 16rpx; position: relative;
            box-shadow: 0 2rpx 12rpx rgba(0,0,0,.04); }
.msg-item.unread { border-left: 6rpx solid #409eff; }
.msg-dot { width: 16rpx; height: 16rpx; background: #409eff; border-radius: 50%; flex-shrink: 0; margin-top: 8rpx; }
.msg-body { flex: 1; }
.msg-header-row { display: flex; justify-content: space-between; margin-bottom: 10rpx; }
.msg-title { font-size: 28rpx; font-weight: 600; color: #303133; flex: 1; }
.msg-time { font-size: 22rpx; color: #909399; flex-shrink: 0; margin-left: 12rpx; }
.msg-content { font-size: 24rpx; color: #606266; display: block; line-height: 1.5;
               display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.msg-tag-row { margin-top: 12rpx; }
.msg-tag { font-size: 20rpx; padding: 4rpx 16rpx; border-radius: 20rpx; }
.tag-workflow { background: #ecf5ff; color: #409eff; }
.tag-system   { background: #f0f9eb; color: #67c23a; }
.tag-notice   { background: #fdf6ec; color: #e6a23c; }
.empty { text-align: center; padding: 120rpx 0; color: #909399; font-size: 28rpx; }
.loading { text-align: center; padding: 32rpx; color: #c0c4cc; font-size: 24rpx; }
</style>
