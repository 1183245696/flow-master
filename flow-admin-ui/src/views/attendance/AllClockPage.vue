<template>
  <el-card>
    <template #header><span class="page-title">全员打卡记录</span></template>
    <el-form inline class="search-form">
      <el-form-item label="部门">
        <el-tree-select v-model="query.deptId" :data="deptTree" :props="{ label:'name', value:'id' }" clearable placeholder="全部" style="width:160px" />
      </el-form-item>
      <el-form-item label="员工ID"><el-input v-model="query.userId" clearable style="width:120px" /></el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DDTHH:mm:ss" style="width:260px" />
      </el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="userId" label="员工ID" width="100" />
      <el-table-column prop="deptId" label="部门ID" width="100" />
      <el-table-column prop="clockTime" label="打卡时间" width="180" />
      <el-table-column label="类型" width="80"><template #default="{ row }"><el-tag :type="row.type==='IN'?'success':'danger'" size="small">{{ row.type==='IN'?'上班':'下班' }}</el-tag></template></el-table-column>
      <el-table-column prop="distanceMeters" label="距离(m)" width="100" />
      <el-table-column label="有效" width="80"><template #default="{ row }"><el-tag :type="row.isValid?'success':'warning'" size="small">{{ row.isValid?'是':'否' }}</el-tag></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" :total="total" layout="total,prev,pager,next" @change="loadData" class="pagination" />
  </el-card>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { clockApi, deptApi } from '@/api'
const list = ref<any[]>([]); const total = ref(0); const loading = ref(false); const deptTree = ref<any[]>([]); const dateRange = ref<string[]>([])
const query = reactive({ pageNum: 1, pageSize: 20, deptId: null as number|null, userId: '' })
async function loadData() {
  loading.value = true
  const r: any = await clockApi.allRecords({ ...query, from: dateRange.value?.[0], to: dateRange.value?.[1] })
  list.value = r.data?.records || []; total.value = r.data?.total || 0; loading.value = false
}
onMounted(async () => { await loadData(); const r: any = await deptApi.tree(); deptTree.value = r.data || [] })
</script>
<style scoped>.page-title{font-weight:600;font-size:15px}.search-form{margin-bottom:12px}.pagination{margin-top:16px;display:flex;justify-content:flex-end}</style>
