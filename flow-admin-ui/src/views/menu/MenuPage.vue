<!-- MenuPage.vue -->
<template>
  <el-card>
    <template #header>
      <div class="page-header"><span class="page-title">菜单管理</span>
        <el-button type="primary" @click="openForm(null)">新增菜单</el-button></div>
    </template>
    <el-table :data="menuTree" border row-key="id" default-expand-all v-loading="loading">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="path" label="路由路径" width="200" />
      <el-table-column prop="component" label="组件" width="200" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 'menu' ? 'primary' : 'warning'" size="small">{{ row.type === 'menu' ? '菜单' : '按钮' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteMenu(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="formVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" :rules="[{ required: true }]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio label="menu">菜单</el-radio><el-radio label="button">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.type === 'menu'"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="组件" v-if="form.type === 'menu'"><el-input v-model="form.component" /></el-form-item>
        <el-form-item label="HTTP方法" v-if="form.type === 'button'">
          <el-select v-model="form.method"><el-option v-for="m in ['GET','POST','PUT','DELETE']" :key="m" :label="m" :value="m" /></el-select>
        </el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="上级菜单">
          <el-tree-select v-model="form.parentId" :data="menuTree" :props="{ label: 'name', value: 'id' }" clearable />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenu">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { menuApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
const menuTree = ref<any[]>([]); const loading = ref(false); const formVisible = ref(false)
const form = reactive<any>({ id: null, name: '', type: 'menu', path: '', component: '', icon: '', parentId: null, sort: 0, method: 'GET' })
async function loadData() { loading.value = true; const r: any = await menuApi.tree(); menuTree.value = r.data || []; loading.value = false }
function openForm(row: any) { Object.assign(form, { id: null, name: '', type: 'menu', path: '', component: '', icon: '', parentId: null, sort: 0, method: 'GET' }); if (row) Object.assign(form, row); formVisible.value = true }
async function saveMenu() { if (form.id) await menuApi.update(form.id, form); else await menuApi.create(form); ElMessage.success('保存成功'); formVisible.value = false; await loadData() }
async function deleteMenu(id: number) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await menuApi.delete(id); ElMessage.success('删除成功'); await loadData() }
onMounted(loadData)
</script>
<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-weight: 600; font-size: 15px; }
</style>
