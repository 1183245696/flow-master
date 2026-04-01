<!-- ProcessListPage.vue -->
<template>
  <div class="process-list-page">
    <div v-for="cat in groupedProcesses" :key="cat.code" class="category-section">
      <h3 class="cat-title">{{ cat.name }}</h3>
      <div class="process-grid">
        <div v-for="proc in cat.processes" :key="proc.id" class="process-card"
             @click="startProcess(proc)">
          <el-icon class="card-icon"><Document /></el-icon>
          <div class="card-name">{{ proc.name }}</div>
          <div class="card-desc">{{ proc.description }}</div>
        </div>
      </div>
    </div>
    <el-dialog v-model="startVisible" :title="`发起：${currentProc?.name}`" width="460px">
      <el-form :model="startForm" label-width="80px">
        <el-form-item label="备注"><el-input v-model="startForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStart">发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { processApi, categoryApi } from '@/api'
import { ElMessage } from 'element-plus'
const allProcesses = ref<any[]>([])
const allCategories = ref<any[]>([])
const startVisible = ref(false)
const currentProc = ref<any>(null)
const startForm = reactive({ remark: '' })
const groupedProcesses = computed(() =>
  allCategories.value.map(cat => ({
    ...cat,
    processes: allProcesses.value.filter(p => p.categoryCode === cat.code)
  })).filter(cat => cat.processes.length > 0)
)
function startProcess(proc: any) { currentProc.value = proc; startForm.remark = ''; startVisible.value = true }
async function submitStart() {
  await processApi.startProcess({ processKey: currentProc.value.processKey, variables: { remark: startForm.remark } })
  ElMessage.success('流程已发起'); startVisible.value = false
}
onMounted(async () => {
  const [pr, cr]: any[] = await Promise.all([processApi.listLatest(), categoryApi.list()])
  allProcesses.value = pr.data || []; allCategories.value = cr.data || []
})
</script>
<style scoped>
.process-list-page { display: flex; flex-direction: column; gap: 20px; }
.cat-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; border-left: 4px solid #409eff; padding-left: 8px; }
.process-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.process-card { width: 140px; height: 100px; border: 1px solid #e4e7ed; border-radius: 8px;
                display: flex; flex-direction: column; align-items: center; justify-content: center;
                gap: 6px; cursor: pointer; padding: 8px; text-align: center; transition: box-shadow .2s; }
.process-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.12); }
.card-icon { font-size: 30px; color: #409eff; }
.card-name { font-weight: 600; font-size: 13px; }
.card-desc { font-size: 11px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; width: 100%; }
</style>
