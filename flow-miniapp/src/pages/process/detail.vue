<template>
  <view class="page">
    <view class="info-card">
      <view class="info-row"><text class="info-label">流程名称</text><text class="info-val">{{ instance?.processDefinitionName || '-' }}</text></view>
      <view class="info-row"><text class="info-label">发起人</text><text class="info-val">{{ instance?.startUserId || '-' }}</text></view>
      <view class="info-row"><text class="info-label">发起时间</text><text class="info-val">{{ instance?.startTime || '-' }}</text></view>
      <view class="info-row">
        <text class="info-label">状态</text>
        <text :class="['status-tag', instance?.ended ? 'status-done' : 'status-running']">
          {{ instance?.ended ? '已完成' : '进行中' }}
        </text>
      </view>
    </view>

    <!-- Pending tasks for current user -->
    <view v-if="myTasks.length > 0" class="task-card">
      <view class="section-title">待我审批</view>
      <view v-for="task in myTasks" :key="task.id" class="task-item">
        <text class="task-name">{{ task.name }}</text>
        <view class="task-actions">
          <button class="btn-approve" @click="approveTask(task)">通过</button>
          <button class="btn-reject" @click="showRejectDialog(task)">驳回</button>
        </view>
      </view>
    </view>

    <!-- Approval timeline -->
    <view class="timeline-card">
      <view class="section-title">审批记录</view>
      <view v-for="(comment, idx) in comments" :key="idx" class="timeline-item">
        <view class="timeline-dot" :class="comment.approved === false ? 'dot-red' : 'dot-green'" />
        <view class="timeline-content">
          <text class="timeline-user">{{ comment.userId }}</text>
          <text class="timeline-time">{{ comment.time }}</text>
          <text class="timeline-msg">{{ comment.fullMessage }}</text>
        </view>
      </view>
      <view v-if="!comments.length" class="empty"><text>暂无审批记录</text></view>
    </view>

    <!-- Reject dialog -->
    <view v-if="rejectVisible" class="dialog-mask">
      <view class="dialog">
        <text class="dialog-title">驳回原因</text>
        <textarea class="dialog-input" v-model="rejectComment" placeholder="请输入驳回原因" />
        <view class="dialog-btns">
          <button class="dialog-cancel" @click="rejectVisible = false">取消</button>
          <button class="dialog-confirm" @click="submitReject">确认驳回</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { processApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const instance = ref<any>(null)
const myTasks = ref<any[]>([])
const comments = ref<any[]>([])
const rejectVisible = ref(false)
const rejectComment = ref('')
const currentTask = ref<any>(null)
let processInstanceId = ''

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  processInstanceId = page?.options?.instanceId || ''
  if (!processInstanceId) return
  await loadData()
})

async function loadData() {
  const [tasks]: any[] = await Promise.allSettled([processApi.myPendingTasks()])
  if (tasks.status === 'fulfilled') {
    myTasks.value = (tasks.value || []).filter((t: any) => t.processInstanceId === processInstanceId)
  }
  // Comments would come from a dedicated history API in production
  comments.value = []
}

async function approveTask(task: any) {
  uni.showModal({
    title: '确认审批',
    content: '确定通过此审批节点？',
    success: async (res) => {
      if (!res.confirm) return
      await processApi.approve(task.id, { comment: '同意' })
      uni.showToast({ title: '审批成功', icon: 'success' })
      await loadData()
    }
  })
}

function showRejectDialog(task: any) {
  currentTask.value = task
  rejectComment.value = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectComment.value.trim()) {
    uni.showToast({ title: '请输入驳回原因', icon: 'none' })
    return
  }
  await processApi.reject(currentTask.value.id, { comment: rejectComment.value })
  rejectVisible.value = false
  uni.showToast({ title: '已驳回', icon: 'success' })
  await loadData()
}
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; padding: 20rpx; }
.info-card, .task-card, .timeline-card { background: #fff; border-radius: 16rpx; padding: 28rpx; margin-bottom: 20rpx; }
.info-row { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.info-label { color: #909399; font-size: 26rpx; }
.info-val { color: #303133; font-size: 26rpx; font-weight: 500; }
.status-tag { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 20rpx; }
.status-running { background: #ecf5ff; color: #409eff; }
.status-done { background: #f0f9eb; color: #67c23a; }
.section-title { font-size: 28rpx; font-weight: 700; color: #303133; margin-bottom: 24rpx; }
.task-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0;
             border-bottom: 1rpx solid #f5f5f5; }
.task-name { font-size: 28rpx; color: #303133; }
.task-actions { display: flex; gap: 16rpx; }
.btn-approve { font-size: 24rpx; background: #67c23a; color: #fff; border-radius: 32rpx; padding: 0 28rpx; height: 56rpx; line-height: 56rpx; }
.btn-reject { font-size: 24rpx; background: #f56c6c; color: #fff; border-radius: 32rpx; padding: 0 28rpx; height: 56rpx; line-height: 56rpx; }
.timeline-item { display: flex; gap: 20rpx; margin-bottom: 24rpx; }
.timeline-dot { width: 20rpx; height: 20rpx; border-radius: 50%; flex-shrink: 0; margin-top: 8rpx; }
.dot-green { background: #67c23a; } .dot-red { background: #f56c6c; }
.timeline-content { flex: 1; }
.timeline-user { font-size: 26rpx; font-weight: 600; display: block; }
.timeline-time { font-size: 22rpx; color: #909399; display: block; }
.timeline-msg { font-size: 24rpx; color: #606266; display: block; margin-top: 4rpx; }
.empty { text-align: center; padding: 40rpx; color: #909399; font-size: 26rpx; }
.dialog-mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.dialog { width: 580rpx; background: #fff; border-radius: 20rpx; padding: 40rpx; }
.dialog-title { font-size: 32rpx; font-weight: 700; display: block; margin-bottom: 24rpx; }
.dialog-input { width: 100%; height: 160rpx; border: 1rpx solid #dcdfe6; border-radius: 8rpx; padding: 16rpx; font-size: 26rpx; box-sizing: border-box; }
.dialog-btns { display: flex; gap: 20rpx; margin-top: 28rpx; }
.dialog-cancel { flex: 1; background: #f5f5f5; color: #606266; border-radius: 48rpx; }
.dialog-confirm { flex: 1; background: #f56c6c; color: #fff; border-radius: 48rpx; }
</style>
