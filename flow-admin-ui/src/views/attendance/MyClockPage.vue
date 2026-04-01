<template>
  <el-card>
    <template #header><span class="page-title">我的打卡记录</span></template>
    <el-form inline class="search-form">
      <el-form-item label="时间范围">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
          start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="clockTime" label="打卡时间" width="180" />
      <el-table-column label="类型" width="80"><template #default="{ row }"><el-tag :type="row.type === 'IN' ? 'success' : 'danger'" size="small">{{ row.type === 'IN' ? '上班' : '下班' }}</el-tag></template></el-table-column>
      <el-table-column prop="distanceMeters" label="距离(m)" width="100" />
      <el-table-column label="是否有效" width="100"><template #default="{ row }"><el-tag :type="row.isValid ? 'success' : 'warning'" size="small">{{ row.isValid ? '有效' : '无效' }}</el-tag></template></el-table-column>
      <el-table-column prop="lat" label="纬度" width="120" /><el-table-column prop="lng" label="经度" width="120" />
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" layout="total,prev,pager,next" @change="loadData" class="pagination" />
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { clockApi } from '@/api'
const list = ref<any[]>([]); const total = ref(0); const page = ref(1); const loading = ref(false)
const dateRange = ref<string[]>([])
async function loadData() {
  loading.value = true
  const r: any = await clockApi.myRecords({ pageNum: page.value, pageSize: 20, from: dateRange.value?.[0], to: dateRange.value?.[1] })
  list.value = r.data?.records || []; total.value = r.data?.total || 0; loading.value = false
}
onMounted(loadData)
</script>
<style scoped>.page-title{font-weight:600;font-size:15px}.search-form{margin-bottom:12px}.pagination{margin-top:16px;display:flex;justify-content:flex-end}</style>
