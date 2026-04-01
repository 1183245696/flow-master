<template>
  <el-card>
    <template #header><span class="page-title">消息中心</span></template>
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="全部" name="" /><el-tab-pane label="流程消息" name="WORKFLOW" /><el-tab-pane label="系统消息" name="SYSTEM" /><el-tab-pane label="公告" name="NOTICE" />
    </el-tabs>
    <el-list v-loading="loading">
      <el-table :data="list" border @row-click="onRowClick">
        <el-table-column label="状态" width="60"><template #default="{ row }"><el-badge :is-dot="!row.isRead" /></template></el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button v-if="!row.isRead" size="small" @click.stop="markRead(row)">标为已读</el-button></template></el-table-column>
      </el-table>
    </el-list>
    <el-pagination v-model:current-page="page" :total="total" layout="total,prev,pager,next" @change="loadData" class="pagination" />
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { messageApi } from '@/api'
import { useRouter } from 'vue-router'
const router = useRouter()
const list = ref<any[]>([]); const total = ref(0); const page = ref(1); const loading = ref(false); const activeTab = ref('')
async function loadData() {
  loading.value = true
  const r: any = await messageApi.list({ category: activeTab.value || undefined, page: page.value, size: 20 })
  list.value = r.data?.records || []; total.value = r.data?.total || 0; loading.value = false
}
async function markRead(row: any) { await messageApi.markRead(row.id); row.isRead = true }
async function onRowClick(row: any) {
  if (!row.isRead) await markRead(row)
  if (row.category === 'WORKFLOW' && row.sourceId) {
    router.push({ path: '/process-list', query: { instanceId: row.sourceId } })
  }
}
onMounted(loadData)
</script>
<style scoped>.page-title{font-weight:600;font-size:15px}.pagination{margin-top:16px;display:flex;justify-content:flex-end}</style>
