<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span class="page-title">员工管理</span>
        <el-button type="primary" v-permission="'POST:/v1/users'" @click="openForm(null)">新增员工</el-button>
      </div>
    </template>

    <!-- Search -->
    <el-form inline class="search-form">
      <el-form-item label="姓名"><el-input v-model="query.keyword" clearable placeholder="搜索姓名" /></el-form-item>
      <el-form-item label="部门">
        <el-tree-select v-model="query.deptId" :data="deptTree" :props="{ label: 'name', value: 'id' }" clearable placeholder="全部" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width:100px">
          <el-option label="正常" :value="0" /><el-option label="禁用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="employeeNo" label="工号" width="120" />
      <el-table-column prop="deptId" label="部门" width="150" />
      <el-table-column prop="positionId" label="职位" width="150" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" v-permission="'PUT:/v1/users/**'" @click="openForm(row)">编辑</el-button>
          <el-button size="small" @click="openTransferHistory(row)">调动记录</el-button>
          <el-button size="small" type="danger" v-permission="'DELETE:/v1/users/**'" @click="deleteUser(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pagination" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total,prev,pager,next" @change="loadData" />

    <!-- User Form Dialog -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑员工' : '新增员工'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名" :rules="[{ required: true }]"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="用户名" :rules="[{ required: true }]"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" :placeholder="form.id ? '不修改请留空' : ''" /></el-form-item>
        <el-form-item label="工号"><el-input v-model="form.employeeNo" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="部门">
          <el-tree-select v-model="form.deptId" :data="deptTree" :props="{ label: 'name', value: 'id' }" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio><el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>

    <!-- Transfer History Dialog -->
    <el-dialog v-model="historyVisible" :title="`${currentUser?.realName} — 调动记录`" width="700px">
      <el-table :data="historyData" border>
        <el-table-column prop="fromDeptId" label="原部门" />
        <el-table-column prop="toDeptId" label="调入部门" />
        <el-table-column prop="fromPositionId" label="原职位" />
        <el-table-column prop="toPositionId" label="调入职位" />
        <el-table-column prop="reason" label="调动原因" />
        <el-table-column prop="transferTime" label="调动时间" width="160" />
      </el-table>
      <el-pagination v-model:current-page="historyQuery.pageNum" :total="historyTotal"
        layout="total,prev,pager,next" @change="loadHistory" class="pagination" />
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { userApi, deptApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const formVisible = ref(false)
const historyVisible = ref(false)
const deptTree = ref<any[]>([])
const historyData = ref<any[]>([])
const historyTotal = ref(0)
const currentUser = ref<any>(null)

const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', deptId: null as number | null, status: null as number | null })
const historyQuery = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive<any>({ id: null, realName: '', username: '', password: '', employeeNo: '', phone: '', email: '', deptId: null, status: 0 })

async function loadData() {
  loading.value = true
  try {
    const res: any = await userApi.page(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openForm(row: any) {
  Object.assign(form, { id: null, realName: '', username: '', password: '', employeeNo: '', phone: '', email: '', deptId: null, status: 0 })
  if (row) Object.assign(form, row)
  formVisible.value = true
}

async function saveUser() {
  try {
    if (form.id) await userApi.update(form.id, form)
    else await userApi.create(form)
    ElMessage.success('保存成功')
    formVisible.value = false
    await loadData()
  } catch {}
}

async function deleteUser(id: number) {
  await ElMessageBox.confirm('确定删除该员工？', '警告', { type: 'warning' })
  await userApi.delete(id)
  ElMessage.success('删除成功')
  await loadData()
}

async function openTransferHistory(row: any) {
  currentUser.value = row
  historyQuery.pageNum = 1
  historyVisible.value = true
  await loadHistory()
}

async function loadHistory() {
  const res: any = await userApi.transferHistory(currentUser.value.userId, historyQuery)
  historyData.value = res.data?.records || []
  historyTotal.value = res.data?.total || 0
}

onMounted(async () => {
  await loadData()
  const res: any = await deptApi.tree()
  deptTree.value = res.data || []
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-weight: 600; font-size: 15px; }
.search-form { margin-bottom: 12px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
