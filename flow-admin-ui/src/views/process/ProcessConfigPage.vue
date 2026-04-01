<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span class="page-title">流程配置</span>
        <el-button type="primary" @click="openForm(null)">新增流程</el-button>
      </div>
    </template>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="name" label="流程名称" />
      <el-table-column prop="processKey" label="流程标识" width="180" />
      <el-table-column prop="categoryCode" label="分类" width="120" />
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column label="固定" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.isPinned" @change="togglePin(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit / Create Dialog with BPMN designer -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑流程' : '新增流程'" width="90%" top="5vh">
      <el-form :model="form" label-width="100px" inline>
        <el-form-item label="流程名称" :rules="[{ required: true }]">
          <el-input v-model="form.name" style="width:200px" />
        </el-form-item>
        <el-form-item label="流程标识">
          <el-input v-model="form.processKey" :disabled="!!form.id" style="width:180px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryCode" style="width:150px">
            <el-option v-for="c in categories" :key="c.code" :label="c.name" :value="c.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" style="width:240px" />
        </el-form-item>
      </el-form>
      <!-- BPMN Designer container -->
      <div ref="bpmnContainer" class="bpmn-container"></div>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProcess">保存并部署</el-button>
      </template>
    </el-dialog>

    <!-- Detail Drawer -->
    <el-drawer v-model="detailVisible" :title="currentProc?.name" size="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="流程标识">{{ currentProc?.processKey }}</el-descriptions-item>
        <el-descriptions-item label="版本">v{{ currentProc?.version }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentProc?.categoryCode }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentProc?.description }}</el-descriptions-item>
        <el-descriptions-item label="部署时间">{{ currentProc?.deployedAt }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>历史版本（只读）</el-divider>
      <el-table :data="historyVersions" border size="small">
        <el-table-column prop="version" label="版本" width="60" />
        <el-table-column prop="deployedAt" label="部署时间" />
        <el-table-column prop="deploymentId" label="DeploymentId" show-overflow-tooltip />
      </el-table>
      <el-pagination v-model:current-page="historyPage" :total="historyTotal"
        layout="prev,pager,next" @change="loadHistory" class="pagination" small />
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { processApi, categoryApi } from '@/api'
import { ElMessage } from 'element-plus'

const tableData = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(false)
const formVisible = ref(false)
const detailVisible = ref(false)
const currentProc = ref<any>(null)
const historyVersions = ref<any[]>([])
const historyPage = ref(1)
const historyTotal = ref(0)
const bpmnContainer = ref<HTMLDivElement>()
let bpmnModeler: any = null

const form = reactive<any>({
  id: null, name: '', processKey: '', categoryCode: '', description: '', icon: '', bpmnXml: ''
})

const DEFAULT_BPMN = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://oaplatform.com/workflow">
  <process id="new_process" name="新流程" isExecutable="true">
    <startEvent id="start" /><endEvent id="end" />
    <sequenceFlow id="flow1" sourceRef="start" targetRef="end" />
  </process>
</definitions>`

async function initBpmn() {
  await nextTick()
  if (!bpmnContainer.value) return
  const BpmnModeler = (await import('bpmn-js/lib/Modeler')).default
  bpmnModeler = new BpmnModeler({ container: bpmnContainer.value })
  try {
    await bpmnModeler.importXML(form.bpmnXml || DEFAULT_BPMN)
  } catch (e) {
    console.error('BPMN import failed', e)
  }
}

async function openForm(row: any) {
  Object.assign(form, { id: null, name: '', processKey: '', categoryCode: '', description: '', icon: '', bpmnXml: '' })
  if (row) {
    Object.assign(form, row)
    form.bpmnXml = row.bpmnXml || DEFAULT_BPMN
  }
  formVisible.value = true
  setTimeout(initBpmn, 300)
}

async function saveProcess() {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    form.bpmnXml = xml
    await processApi.deploy(form)
    ElMessage.success('部署成功')
    formVisible.value = false
    await loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '部署失败')
  }
}

async function togglePin(row: any) {
  try {
    const newPin = !row.isPinned
    await processApi.togglePin(row.id, newPin)
    row.isPinned = newPin
    ElMessage.success(newPin ? '已固定' : '已取消固定')
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function openDetail(row: any) {
  currentProc.value = row
  historyPage.value = 1
  detailVisible.value = true
  await loadHistory()
}

async function loadHistory() {
  const res: any = await processApi.detail(currentProc.value.processKey, {
    historyPage: historyPage.value, historySize: 10
  })
  historyVersions.value = res.data?.history?.records || []
  historyTotal.value = res.data?.history?.total || 0
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await processApi.listLatest()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(async () => {
  await loadData()
  const res: any = await categoryApi.list()
  categories.value = res.data || []
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-weight: 600; font-size: 15px; }
.bpmn-container { height: 500px; border: 1px solid #e4e7ed; border-radius: 4px; margin-top: 12px; }
.pagination { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
