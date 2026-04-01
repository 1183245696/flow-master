<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span class="page-title">流程分类</span>
        <el-button type="primary" @click="openForm(null)">新增分类</el-button>
      </div>
    </template>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="name" label="分类名称" /><el-table-column prop="code" label="分类编码" width="180" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="formVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" :rules="[{ required: true }]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码" :rules="[{ required: true }]"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
import { categoryApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
const tableData = ref<any[]>([])
const loading = ref(false)
const formVisible = ref(false)
const form = reactive<any>({ id: null, name: '', code: '', sort: 0 })
async function loadData() { loading.value = true; const r: any = await categoryApi.list(); tableData.value = r.data || []; loading.value = false }
function openForm(row: any) { Object.assign(form, { id: null, name: '', code: '', sort: 0 }); if (row) Object.assign(form, row); formVisible.value = true }
async function saveRow() {
  if (form.id) await categoryApi.update(form.id, form); else await categoryApi.create(form)
  ElMessage.success('保存成功'); formVisible.value = false; await loadData()
}
async function deleteRow(id: number) {
  await ElMessageBox.confirm('确定删除此分类？有流程绑定时将拒绝删除。', '提示', { type: 'warning' })
  try { await categoryApi.delete(id); ElMessage.success('删除成功'); await loadData() } catch {}
}
onMounted(loadData)
</script>
<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-weight: 600; font-size: 15px; }
</style>
