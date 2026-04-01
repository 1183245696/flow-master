<template>
  <div class="home-page">
    <!-- Section 1: Pinned processes -->
    <el-card class="section-card">
      <template #header><span class="section-title">常用流程</span></template>
      <div class="pinned-grid">
        <div
          v-for="proc in pinnedList"
          :key="proc.id"
          class="process-btn"
          @click="openStartDialog(proc)"
        >
          <el-icon class="proc-icon" :style="{ color: proc.icon || '#409eff' }"><Document /></el-icon>
          <span>{{ proc.name }}</span>
        </div>
        <el-empty v-if="!pinnedList.length" description="暂无固定流程" />
      </div>
    </el-card>

    <!-- Section 2: Stat cards -->
    <div class="stat-grid">
      <el-card v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </el-card>
    </div>

    <!-- Section 3: Charts -->
    <div class="chart-row">
      <el-card class="chart-card">
        <template #header>
          <div class="chart-header">
            <span>流程统计</span>
            <div class="chart-filters">
              <el-select v-model="processFilter.range" size="small" @change="loadProcessChart" style="width:90px">
                <el-option label="近7天" value="7d" />
                <el-option label="近30天" value="30d" />
                <el-option label="近1年" value="1y" />
              </el-select>
              <el-select v-model="processFilter.deptId" clearable placeholder="部门" size="small" style="width:130px">
                <el-option v-for="d in deptList" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </div>
          </div>
        </template>
        <v-chart :option="processChartOption" style="height:280px" autoresize />
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <div class="chart-header">
            <span>考勤统计</span>
            <div class="chart-filters">
              <el-select v-model="attFilter.range" size="small" @change="loadAttChart" style="width:90px">
                <el-option label="近7天" value="7d" />
                <el-option label="近30天" value="30d" />
                <el-option label="近1年" value="1y" />
              </el-select>
              <el-select v-model="attFilter.deptId" clearable placeholder="部门" size="small" style="width:130px">
                <el-option v-for="d in deptList" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </div>
          </div>
        </template>
        <v-chart :option="attChartOption" style="height:280px" autoresize />
      </el-card>
    </div>

    <!-- Start Process Dialog -->
    <el-dialog v-model="startDialogVisible" :title="`发起：${currentProc?.name}`" width="500px">
      <el-form :model="startForm" label-width="90px">
        <el-form-item label="备注">
          <el-input v-model="startForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStart">发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { processApi, deptApi } from '@/api'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const pinnedList = ref<any[]>([])
const deptList = ref<any[]>([])
const startDialogVisible = ref(false)
const currentProc = ref<any>(null)
const startForm = reactive({ remark: '' })

const stats = ref([
  { label: '总部门数', value: '-' }, { label: '总职工数', value: '-' },
  { label: '总发起流程数', value: '-' }, { label: '总完成流程数', value: '-' },
  { label: '今日流程数', value: '-' }, { label: '今日完成', value: '-' },
  { label: '今日打卡人数', value: '-' }
])

const processFilter = reactive({ range: '7d', deptId: null as number | null })
const attFilter = reactive({ range: '7d', deptId: null as number | null })

const makeDates = (days: number) => Array.from({ length: days }, (_, i) => {
  const d = new Date(); d.setDate(d.getDate() - (days - 1 - i))
  return `${d.getMonth() + 1}/${d.getDate()}`
})

const processChartOption = ref<any>({})
const attChartOption = ref<any>({})

function buildLineOption(dates: string[], seriesA: number[], seriesB: number[], nameA: string, nameB: string) {
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: [nameA, nameB] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: nameA, type: 'line', smooth: true, data: seriesA, itemStyle: { color: '#409eff' } },
      { name: nameB, type: 'line', smooth: true, data: seriesB, itemStyle: { color: '#67c23a' } }
    ]
  }
}

function loadProcessChart() {
  const days = processFilter.range === '7d' ? 7 : processFilter.range === '30d' ? 30 : 12
  const dates = makeDates(days)
  // Mock data — replace with real API call
  const initiated = dates.map(() => Math.floor(Math.random() * 20))
  const completed = initiated.map(v => Math.floor(v * 0.7))
  processChartOption.value = buildLineOption(dates, initiated, completed, '发起数', '完成数')
}

function loadAttChart() {
  const days = attFilter.range === '7d' ? 7 : attFilter.range === '30d' ? 30 : 12
  const dates = makeDates(days)
  const clocked = dates.map(() => Math.floor(Math.random() * 100))
  const notClocked = clocked.map(v => Math.floor((120 - v)))
  attChartOption.value = buildLineOption(dates, clocked, notClocked, '已打卡', '未打卡')
}

function openStartDialog(proc: any) {
  currentProc.value = proc
  startForm.remark = ''
  startDialogVisible.value = true
}

async function submitStart() {
  try {
    await processApi.startProcess({
      processKey: currentProc.value.processKey,
      variables: { remark: startForm.remark }
    })
    ElMessage.success('流程已发起')
    startDialogVisible.value = false
  } catch {}
}

onMounted(async () => {
  const [pinnedRes, deptRes]: any[] = await Promise.allSettled([
    processApi.pinned(), deptApi.tree()
  ])
  if (pinnedRes.status === 'fulfilled') pinnedList.value = pinnedRes.value.data || []
  if (deptRes.status === 'fulfilled') deptList.value = deptRes.value.data || []
  loadProcessChart()
  loadAttChart()
})
</script>

<style scoped>
.home-page { display: flex; flex-direction: column; gap: 16px; }
.section-card, .chart-card { border-radius: 8px; }
.section-title { font-weight: 600; font-size: 15px; }
.pinned-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.process-btn { width: 110px; height: 80px; border: 1px solid #e4e7ed; border-radius: 8px;
               display: flex; flex-direction: column; align-items: center; justify-content: center;
               gap: 6px; cursor: pointer; transition: box-shadow .2s; }
.process-btn:hover { box-shadow: 0 4px 12px rgba(0,0,0,.12); }
.proc-icon { font-size: 28px; }
.stat-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 12px; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: 700; color: #409eff; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-header { display: flex; justify-content: space-between; align-items: center; }
.chart-filters { display: flex; gap: 8px; }
</style>
