<template>
  <view class="page">
    <!-- Header info -->
    <view class="header-card">
      <view class="user-info-row">
        <text class="user-name">{{ userInfo?.realName || '-' }}</text>
        <text class="separator">|</text>
        <text class="dept-name">{{ userInfo?.deptName || '所在部门' }}</text>
        <text class="separator">|</text>
        <text class="rule-name">{{ ruleName }}</text>
      </view>
      <view class="date-text">{{ todayStr }}</view>
    </view>

    <!-- Clock button -->
    <view class="clock-section">
      <view
        :class="['clock-btn', canClock ? 'clock-active' : 'clock-disabled']"
        @click="handleClock"
      >
        <text class="clock-label">{{ canClock ? '点击打卡' : '不在打卡范围内' }}</text>
        <text class="clock-distance" v-if="distance !== null">距离：{{ distance }}m</text>
      </view>
      <text class="clock-hint" v-if="!canClock && distance !== null">
        您距离考勤区域 {{ distance }}m，请到达范围内再打卡
      </text>
    </view>

    <!-- Today's records -->
    <view class="today-card">
      <view class="section-title">今日打卡</view>
      <view v-if="todayRecords.length > 0" class="record-pair">
        <view v-for="rec in todayRecords" :key="rec.id" class="record-item">
          <text :class="['rec-type', rec.type === 'IN' ? 'type-in' : 'type-out']">
            {{ rec.type === 'IN' ? '上班' : '下班' }}
          </text>
          <text class="rec-time">{{ formatTime(rec.clockTime) }}</text>
          <text :class="['rec-status', rec.isValid ? 'valid' : 'invalid']">
            {{ rec.isValid ? '✓ 有效' : '✗ 无效' }}
          </text>
        </view>
      </view>
      <view v-else class="empty-record"><text>今日暂无打卡记录</text></view>
    </view>

    <!-- Tab: Calendar / History -->
    <view class="tab-bar">
      <view :class="['tab', viewMode === 'calendar' ? 'tab-active' : '']" @click="viewMode = 'calendar'">日历视图</view>
      <view :class="['tab', viewMode === 'history' ? 'tab-active' : '']" @click="viewMode = 'history'">历史记录</view>
    </view>

    <!-- Calendar -->
    <view v-if="viewMode === 'calendar'" class="calendar-card">
      <view class="cal-header">
        <text @click="prevMonth">‹</text>
        <text>{{ calYear }}年{{ calMonth + 1 }}月</text>
        <text @click="nextMonth">›</text>
      </view>
      <view class="cal-weekdays">
        <text v-for="d in ['日','一','二','三','四','五','六']" :key="d">{{ d }}</text>
      </view>
      <view class="cal-grid">
        <view
          v-for="(day, i) in calDays"
          :key="i"
          :class="['cal-day', day === selectedDay ? 'cal-selected' : '', !day ? 'cal-empty' : '']"
          @click="day && selectDay(day)"
        >
          <text>{{ day || '' }}</text>
          <view v-if="day && hasDot(day)" class="cal-dot" />
        </view>
      </view>
      <!-- Selected day records -->
      <view v-if="selectedDayRecords.length > 0" class="day-records">
        <view v-for="r in selectedDayRecords" :key="r.id" class="day-rec-item">
          <text>{{ r.type === 'IN' ? '上班' : '下班' }} {{ formatTime(r.clockTime) }}</text>
          <text :class="r.isValid ? 'valid' : 'invalid'">{{ r.isValid ? '有效' : '无效' }}</text>
        </view>
      </view>
    </view>

    <!-- History list -->
    <view v-if="viewMode === 'history'" class="history-card">
      <view v-for="rec in historyRecords" :key="rec.id" class="history-item">
        <text class="his-date">{{ rec.clockTime?.slice(0, 10) }}</text>
        <text :class="['his-type', rec.type === 'IN' ? 'type-in' : 'type-out']">{{ rec.type === 'IN' ? '上班' : '下班' }}</text>
        <text class="his-time">{{ formatTime(rec.clockTime) }}</text>
        <text :class="rec.isValid ? 'valid' : 'invalid'">{{ rec.isValid ? '有效' : '无效' }}</text>
      </view>
      <view class="load-more" v-if="hasMore" @click="loadMoreHistory">加载更多</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { attendanceApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const ruleName = ref('标准班制')
const canClock = ref(false)
const distance = ref<number | null>(null)
const todayRecords = ref<any[]>([])
const historyRecords = ref<any[]>([])
const hasMore = ref(true)
const historyPage = ref(1)
const viewMode = ref<'calendar' | 'history'>('calendar')
const selectedDay = ref<number | null>(null)
const selectedDayRecords = ref<any[]>([])
const deptZone = ref<any>(null)

const now = new Date()
const todayStr = computed(() => `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`)
const calYear = ref(now.getFullYear())
const calMonth = ref(now.getMonth())

const calDays = computed(() => {
  const first = new Date(calYear.value, calMonth.value, 1).getDay()
  const total = new Date(calYear.value, calMonth.value + 1, 0).getDate()
  const days: (number | null)[] = Array(first).fill(null)
  for (let d = 1; d <= total; d++) days.push(d)
  return days
})

function hasDot(day: number) {
  const dateStr = `${calYear.value}-${String(calMonth.value + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  return historyRecords.value.some(r => r.clockTime?.startsWith(dateStr))
}

function selectDay(day: number) {
  selectedDay.value = day
  const dateStr = `${calYear.value}-${String(calMonth.value + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  selectedDayRecords.value = historyRecords.value.filter(r => r.clockTime?.startsWith(dateStr))
}

function prevMonth() { if (calMonth.value === 0) { calMonth.value = 11; calYear.value-- } else calMonth.value-- }
function nextMonth() { if (calMonth.value === 11) { calMonth.value = 0; calYear.value++ } else calMonth.value++ }

function formatTime(dt: string) { return dt ? dt.slice(11, 16) : '-' }

async function checkLocation() {
  return new Promise<void>((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: async (res) => {
        const { latitude, longitude } = res
        if (deptZone.value) {
          const dist = haversine(latitude, longitude, deptZone.value.lat, deptZone.value.lng)
          distance.value = dist
          canClock.value = dist <= deptZone.value.radiusMeters
        } else {
          canClock.value = true
          distance.value = 0
        }
        resolve()
      },
      fail: () => {
        uni.showToast({ title: '无法获取位置，请检查权限', icon: 'none' })
        resolve()
      }
    })
  })
}

function haversine(lat1: number, lng1: number, lat2: number, lng2: number) {
  const R = 6371000
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLng / 2) ** 2
  return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)))
}

async function handleClock() {
  await checkLocation()
  if (!canClock.value) {
    uni.showToast({ title: '不在打卡范围内', icon: 'none' })
    return
  }
  uni.getLocation({
    type: 'gcj02',
    success: async ({ latitude, longitude }) => {
      const type = todayRecords.value.some(r => r.type === 'IN') ? 'OUT' : 'IN'
      await attendanceApi.clock({
        deptId: userInfo.value?.deptId,
        lat: latitude,
        lng: longitude,
        type
      })
      uni.showToast({ title: '打卡成功', icon: 'success' })
      await loadTodayRecords()
    }
  })
}

async function loadTodayRecords() {
  const today = new Date()
  const from = `${today.toISOString().slice(0, 10)}T00:00:00`
  const to   = `${today.toISOString().slice(0, 10)}T23:59:59`
  const res: any = await attendanceApi.myRecords({ pageNum: 1, pageSize: 20, from, to })
  todayRecords.value = res?.records || []
}

async function loadMoreHistory() {
  historyPage.value++
  const res: any = await attendanceApi.myRecords({ pageNum: historyPage.value, pageSize: 20 })
  const records = res?.records || []
  historyRecords.value.push(...records)
  hasMore.value = records.length === 20
}

onMounted(async () => {
  await userStore.loadUserInfo()
  const zonesRes: any = await attendanceApi.zones()
  if (zonesRes?.length) deptZone.value = zonesRes[0]
  await Promise.all([loadTodayRecords(), checkLocation()])
  const hist: any = await attendanceApi.myRecords({ pageNum: 1, pageSize: 50 })
  historyRecords.value = hist?.records || []
})
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; }
.header-card { background: linear-gradient(135deg, #1e3a5f, #2d6a9f); padding: 40rpx 32rpx; color: #fff; }
.user-info-row { display: flex; align-items: center; gap: 16rpx; font-size: 28rpx; }
.user-name { font-weight: 700; font-size: 32rpx; }
.separator { color: rgba(255,255,255,.4); }
.dept-name, .rule-name { font-size: 24rpx; opacity: .85; }
.date-text { font-size: 24rpx; opacity: .7; margin-top: 8rpx; }
.clock-section { display: flex; flex-direction: column; align-items: center; padding: 48rpx 32rpx; }
.clock-btn { width: 280rpx; height: 280rpx; border-radius: 50%; display: flex; flex-direction: column;
             align-items: center; justify-content: center; gap: 12rpx; }
.clock-active { background: radial-gradient(circle, #67c23a, #4a9e2a);
                box-shadow: 0 0 0 20rpx rgba(103,194,58,.2), 0 0 0 40rpx rgba(103,194,58,.1); }
.clock-disabled { background: #dcdfe6; }
.clock-label { color: #fff; font-size: 32rpx; font-weight: 700; }
.clock-distance { color: rgba(255,255,255,.8); font-size: 22rpx; }
.clock-hint { font-size: 24rpx; color: #f56c6c; margin-top: 20rpx; text-align: center; }
.today-card, .calendar-card, .history-card { background: #fff; margin: 0 20rpx 20rpx; border-radius: 16rpx; padding: 28rpx; }
.section-title { font-size: 28rpx; font-weight: 700; margin-bottom: 20rpx; }
.record-item { display: flex; gap: 20rpx; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.rec-type { font-size: 26rpx; font-weight: 600; width: 80rpx; }
.type-in { color: #67c23a; } .type-out { color: #e6a23c; }
.rec-time { flex: 1; font-size: 26rpx; color: #303133; }
.valid { color: #67c23a; font-size: 22rpx; } .invalid { color: #f56c6c; font-size: 22rpx; }
.empty-record { text-align: center; padding: 32rpx; color: #909399; font-size: 26rpx; }
.tab-bar { display: flex; background: #fff; margin: 0 20rpx; border-radius: 16rpx 16rpx 0 0; }
.tab { flex: 1; text-align: center; padding: 24rpx; font-size: 28rpx; color: #606266;
       border-bottom: 4rpx solid transparent; }
.tab-active { color: #409eff; border-bottom-color: #409eff; font-weight: 600; }
.cal-header { display: flex; justify-content: space-between; align-items: center;
              font-size: 30rpx; font-weight: 600; margin-bottom: 20rpx; }
.cal-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center;
                font-size: 22rpx; color: #909399; margin-bottom: 8rpx; }
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); }
.cal-day { display: flex; flex-direction: column; align-items: center; justify-content: center;
           height: 70rpx; font-size: 26rpx; border-radius: 8rpx; position: relative; }
.cal-selected { background: #409eff; color: #fff; border-radius: 50%; }
.cal-dot { width: 8rpx; height: 8rpx; background: #409eff; border-radius: 50%; position: absolute; bottom: 4rpx; }
.cal-selected .cal-dot { background: #fff; }
.day-records { margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx solid #f0f0f0; }
.day-rec-item { display: flex; justify-content: space-between; padding: 12rpx 0; font-size: 26rpx; }
.history-item { display: flex; gap: 16rpx; align-items: center; padding: 20rpx 0;
                border-bottom: 1rpx solid #f5f5f5; font-size: 26rpx; }
.his-date { color: #909399; width: 160rpx; flex-shrink: 0; }
.his-type { width: 70rpx; font-weight: 600; }
.his-time { flex: 1; }
.load-more { text-align: center; padding: 24rpx; color: #409eff; font-size: 26rpx; }
</style>
