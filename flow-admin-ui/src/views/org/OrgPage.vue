<template>
  <div class="org-page">
    <!-- Left: dept tree -->
    <el-card class="tree-panel">
      <template #header>
        <div class="panel-header">
          <span>部门管理</span>
          <div>
            <el-button size="small" type="primary" @click="openDeptForm(null)">新增</el-button>
            <el-button size="small" @click="exportDepts">导出</el-button>
          </div>
        </div>
      </template>
      <el-tree
        :data="deptTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        highlight-current
        @node-click="onDeptClick"
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <span>{{ node.label }}</span>
            <span class="tree-actions">
              <el-icon @click.stop="openDeptForm(data)"><Edit /></el-icon>
              <el-icon @click.stop="deleteDept(data.id)"><Delete /></el-icon>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <!-- Right: position list -->
    <el-card class="list-panel">
      <template #header>
        <div class="panel-header">
          <span>职位管理{{ currentDept ? `（${currentDept.name}）` : '' }}</span>
          <el-button size="small" type="primary" @click="openPositionForm(null)">新增职位</el-button>
        </div>
      </template>
      <el-table :data="positions" border stripe>
        <el-table-column prop="name" label="职位名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openPositionForm(row)">编辑</el-button>
            <el-button size="small" @click="assignMenus(row)">分配权限</el-button>
            <el-button size="small" type="danger" @click="deletePosition(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dept Form Dialog -->
    <el-dialog v-model="deptDialogVisible" :title="deptForm.id ? '编辑部门' : '新增部门'" width="520px">
      <el-form :model="deptForm" label-width="100px" ref="deptFormRef">
        <el-form-item label="部门名称" prop="name" :rules="[{ required: true, message: '请输入部门名称' }]">
          <el-input v-model="deptForm.name" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-tree-select v-model="deptForm.parentId" :data="deptTree"
            :props="{ label: 'name', value: 'id' }" clearable placeholder="根部门" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="deptForm.leaderUserId" placeholder="用户ID" />
        </el-form-item>
        <el-form-item label="考勤区域">
          <el-select v-model="deptForm.attendanceZoneId" clearable placeholder="请选择">
            <el-option v-for="z in zones" :key="z.id" :label="z.name" :value="z.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="打卡规则">
          <el-select v-model="deptForm.ruleId" clearable placeholder="请选择">
            <el-option v-for="r in rules" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDept">保存</el-button>
      </template>
    </el-dialog>

    <!-- Position Form Dialog -->
    <el-dialog v-model="posDialogVisible" :title="posForm.id ? '编辑职位' : '新增职位'" width="420px">
      <el-form :model="posForm" label-width="90px">
        <el-form-item label="职位名称" :rules="[{ required: true }]">
          <el-input v-model="posForm.name" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="posForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="posDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePosition">保存</el-button>
      </template>
    </el-dialog>

    <!-- Assign Menus Dialog -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        :default-checked-keys="checkedMenuIds"
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenuAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { deptApi, positionApi, menuApi, zoneApi, ruleApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const deptTree = ref<any[]>([])
const positions = ref<any[]>([])
const zones = ref<any[]>([])
const rules = ref<any[]>([])
const menuTree = ref<any[]>([])
const currentDept = ref<any>(null)
const deptDialogVisible = ref(false)
const posDialogVisible = ref(false)
const menuDialogVisible = ref(false)
const menuTreeRef = ref<any>()
const currentPositionId = ref<number | null>(null)
const checkedMenuIds = ref<number[]>([])

const deptForm = reactive<any>({ id: null, name: '', parentId: null, leaderUserId: null, attendanceZoneId: null, ruleId: null })
const posForm = reactive<any>({ id: null, name: '', deptId: null, sort: 0 })

async function loadTree() {
  const res: any = await deptApi.tree()
  deptTree.value = res.data || []
}

async function onDeptClick(data: any) {
  currentDept.value = data
  const res: any = await positionApi.page({ deptId: data.id, pageNum: 1, pageSize: 100 })
  positions.value = res.data?.records || []
}

function openDeptForm(dept: any) {
  Object.assign(deptForm, { id: null, name: '', parentId: null, leaderUserId: null, attendanceZoneId: null, ruleId: null })
  if (dept) Object.assign(deptForm, dept)
  deptDialogVisible.value = true
}

async function saveDept() {
  try {
    if (deptForm.id) await deptApi.update(deptForm.id, deptForm)
    else await deptApi.create(deptForm)
    ElMessage.success('保存成功')
    deptDialogVisible.value = false
    await loadTree()
  } catch {}
}

async function deleteDept(id: number) {
  await ElMessageBox.confirm('确定删除此部门？', '提示', { type: 'warning' })
  await deptApi.delete(id)
  ElMessage.success('删除成功')
  await loadTree()
}

async function exportDepts() {
  const res: any = await deptApi.export()
  const url = URL.createObjectURL(new Blob([res]))
  const a = document.createElement('a'); a.href = url; a.download = '部门列表.xlsx'; a.click()
}

function openPositionForm(pos: any) {
  Object.assign(posForm, { id: null, name: '', deptId: currentDept.value?.id, sort: 0 })
  if (pos) Object.assign(posForm, pos)
  posDialogVisible.value = true
}

async function savePosition() {
  try {
    if (posForm.id) await positionApi.update(posForm.id, posForm)
    else await positionApi.create({ ...posForm, deptId: currentDept.value?.id })
    ElMessage.success('保存成功')
    posDialogVisible.value = false
    if (currentDept.value) await onDeptClick(currentDept.value)
  } catch {}
}

async function deletePosition(id: number) {
  await ElMessageBox.confirm('确定删除此职位？', '提示', { type: 'warning' })
  await positionApi.delete(id)
  ElMessage.success('删除成功')
  if (currentDept.value) await onDeptClick(currentDept.value)
}

async function assignMenus(pos: any) {
  currentPositionId.value = pos.id
  const res: any = await menuApi.tree()
  menuTree.value = res.data || []
  checkedMenuIds.value = []
  menuDialogVisible.value = true
}

async function saveMenuAssign() {
  const ids = menuTreeRef.value?.getCheckedKeys() || []
  await positionApi.assignMenus(currentPositionId.value!, ids)
  ElMessage.success('权限分配成功')
  menuDialogVisible.value = false
}

onMounted(async () => {
  await loadTree()
  const [zRes, rRes]: any[] = await Promise.all([zoneApi.list(), ruleApi.list()])
  zones.value = zRes.data || []
  rules.value = rRes.data || []
})
</script>

<style scoped>
.org-page { display: flex; gap: 16px; height: calc(100vh - 120px); }
.tree-panel { width: 280px; overflow-y: auto; flex-shrink: 0; }
.list-panel { flex: 1; overflow-y: auto; }
.panel-header { display: flex; justify-content: space-between; align-items: center; }
.tree-node { display: flex; justify-content: space-between; width: 100%; }
.tree-actions { display: none; gap: 8px; }
.tree-node:hover .tree-actions { display: flex; }
</style>
