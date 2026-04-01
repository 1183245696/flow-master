<template>
  <el-card>
    <template #header><span class="page-title">黑白名单管理</span></template>
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="黑名单" name="blacklist" />
      <el-tab-pane label="白名单" name="whitelist" />
    </el-tabs>
    <div class="toolbar">
      <el-button type="primary" @click="openForm">添加 IP</el-button>
    </div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="ipAddress" label="IP 地址" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="createTime" label="添加时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="deleteRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" layout="total,prev,pager,next" @change="loadData" class="pagination" />

    <el-dialog v-model="formVisible" :title="`添加${activeTab === 'blacklist' ? '黑' : '白'}名单 IP`" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="IP 地址" :rules="[{ required: true, pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: '请输入合法 IPv4 地址' }]">
          <el-input v-model="form.ipAddress" placeholder="例：192.168.1.100" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRow">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ipListApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
const activeTab = ref<'blacklist' | 'whitelist'>('blacklist')
const tableData = ref<any[]>([])
const total = ref(0); const page = ref(1); const loading = ref(false); const formVisible = ref(false)
const form = reactive({ ipAddress: '', remark: '' })
async function loadData() {
  loading.value = true
  const api = activeTab.value === 'blacklist' ? ipListApi.listBlacklist : ipListApi.listWhitelist
  const r: any = await api({ page: page.value, size: 20 })
  tableData.value = r.data?.records || []; total.value = r.data?.total || 0; loading.value = false
}
function openForm() { form.ipAddress = ''; form.remark = ''; formVisible.value = true }
async function saveRow() {
  const api = activeTab.value === 'blacklist' ? ipListApi.addBlacklist : ipListApi.addWhitelist
  await api(form); ElMessage.success('添加成功'); formVisible.value = false; await loadData()
}
async function deleteRow(id: number) {
  await ElMessageBox.confirm('确定删除此 IP？', '提示', { type: 'warning' })
  const api = activeTab.value === 'blacklist' ? ipListApi.deleteBlacklist : ipListApi.deleteWhitelist
  await api(id); ElMessage.success('删除成功'); await loadData()
}
onMounted(loadData)
</script>
<style scoped>
.page-title { font-weight: 600; font-size: 15px; }
.toolbar { margin: 12px 0; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
