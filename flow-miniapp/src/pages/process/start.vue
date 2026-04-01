<template>
  <view class="page">
    <view class="form-card">
      <view class="form-title">{{ processName }}</view>

      <view class="form-item">
        <text class="label">备注说明</text>
        <textarea class="textarea" v-model="formData.remark" placeholder="请输入备注（选填）" />
      </view>

      <!-- Leave request extra fields -->
      <view v-if="isLeaveProcess" class="form-item">
        <text class="label">开始时间</text>
        <picker mode="date" @change="formData.startDate = $event.detail.value">
          <view class="picker-val">{{ formData.startDate || '请选择' }}</view>
        </picker>
      </view>
      <view v-if="isLeaveProcess" class="form-item">
        <text class="label">结束时间</text>
        <picker mode="date" @change="formData.endDate = $event.detail.value">
          <view class="picker-val">{{ formData.endDate || '请选择' }}</view>
        </picker>
      </view>

      <!-- Reimbursement extra fields -->
      <view v-if="isReimbursement" class="form-item">
        <text class="label">报销金额（元）</text>
        <input class="input" type="digit" v-model="formData.amount" placeholder="请输入金额" />
      </view>
      <view v-if="isReimbursement" class="form-item">
        <text class="label">费用说明</text>
        <input class="input" v-model="formData.description" placeholder="请输入费用说明" />
      </view>
    </view>

    <view class="bottom-bar">
      <button class="submit-btn" :loading="loading" @click="submitProcess">发 起 流 程</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { processApi } from '@/api'

const processKey = ref('')
const processName = ref('')
const loading = ref(false)
const formData = reactive<any>({ remark: '', startDate: '', endDate: '', amount: '', description: '' })

const isLeaveProcess = computed(() =>
  processKey.value === 'personal_leave' || processKey.value === 'sick_leave'
)
const isReimbursement = computed(() => processKey.value === 'expense_reimbursement')

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  processKey.value = page?.options?.processKey || ''
  processName.value = decodeURIComponent(page?.options?.name || '发起流程')
})

async function submitProcess() {
  if (!processKey.value) return
  loading.value = true
  try {
    const variables: any = { remark: formData.remark }
    if (isLeaveProcess.value) {
      if (!formData.startDate || !formData.endDate) {
        uni.showToast({ title: '请选择请假时间', icon: 'none' })
        return
      }
      variables.startTime = formData.startDate + 'T00:00:00'
      variables.endTime   = formData.endDate + 'T23:59:59'
    }
    if (isReimbursement.value) {
      if (!formData.amount) {
        uni.showToast({ title: '请输入报销金额', icon: 'none' })
        return
      }
      variables.amount      = Number(formData.amount)
      variables.description = formData.description
    }
    await processApi.startProcess({ processKey: processKey.value, variables })
    uni.showToast({ title: '流程已发起', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 140rpx; }
.form-card { margin: 20rpx; background: #fff; border-radius: 16rpx; padding: 32rpx; }
.form-title { font-size: 34rpx; font-weight: 700; color: #303133; margin-bottom: 32rpx;
              padding-bottom: 20rpx; border-bottom: 1rpx solid #f0f0f0; }
.form-item { margin-bottom: 32rpx; }
.label { display: block; font-size: 26rpx; color: #606266; margin-bottom: 12rpx; }
.input { height: 72rpx; border: 1rpx solid #dcdfe6; border-radius: 8rpx; padding: 0 20rpx;
         font-size: 28rpx; background: #fafafa; }
.textarea { width: 100%; height: 160rpx; border: 1rpx solid #dcdfe6; border-radius: 8rpx;
            padding: 16rpx 20rpx; font-size: 28rpx; background: #fafafa; box-sizing: border-box; }
.picker-val { height: 72rpx; border: 1rpx solid #dcdfe6; border-radius: 8rpx; padding: 0 20rpx;
              font-size: 28rpx; line-height: 72rpx; color: #303133; background: #fafafa; }
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
              padding: 20rpx 40rpx; box-shadow: 0 -2rpx 12rpx rgba(0,0,0,.08); }
.submit-btn { background: #409eff; color: #fff; border-radius: 48rpx; font-size: 32rpx; letter-spacing: 4rpx; }
</style>
