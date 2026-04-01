<template>
  <el-card>
    <template #header>
      <div class="page-header"><span class="page-title">考勤区域</span>
        <el-button type="primary" @click="openForm(null)">新增区域</el-button></div>
    </template>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="name" label="区域名称" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="lat" label="纬度" width="120" />
      <el-table-column prop="lng" label="经度" width="120" />
      <el-table-column prop="radiusMeters" label="半径(m)" width="100" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="formVisible" :title="form.id ? '编辑考勤区域' : '新增考勤区域'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="区域名称" :rules="[{ required: true }]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.lat" :precision="6" :step="0.000001" style="width:200px" />
          <el-tooltip content="在地图上点击自动填充坐标" placement="right">
            <el-button style="margin-left:8px" size="small" @click="pickFromMap">地图选点</el-button>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="经度"><el-input-number v-model="form.lng" :precision="6" :step="0.000001" style="width:200px" /></el-form-item>
        <el-form-item label="打卡半径(m)">
          <el-slider v-model="form.radiusMeters" :min="50" :max="2000" :step="50" show-input style="width:100%" />
        </el-form-item>
        <!-- Map container (Tencent Map JS SDK integration point) -->
        <el-form-item label="地图预览">
          <div id="map-container" ref="mapContainer" class="map-preview">
            <div class="map-placeholder">
              <el-icon style="font-size:40px;color:#c0c4cc"><Location /></el-icon>
              <p>集成腾讯地图 JS SDK 后在此显示地图</p>
              <p>当前坐标：{{ form.lat }}, {{ form.lng }} 半径：{{ form.radiusMeters }}m</p>
            </div>
          </div>
        </el-form-item>
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
import { zoneApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
const list = ref<any[]>([]); const loading = ref(false); const formVisible = ref(false)
const form = reactive<any>({ id: null, name: '', address: '', lat: 39.9042, lng: 116.4074, radiusMeters: 200 })
function pickFromMap() { ElMessage.info('请在地图上点击选点（需集成腾讯地图 JS SDK）') }
async function loadData() { loading.value = true; const r: any = await zoneApi.list(); list.value = r.data || []; loading.value = false }
function openForm(row: any) { Object.assign(form, { id: null, name: '', address: '', lat: 39.9042, lng: 116.4074, radiusMeters: 200 }); if (row) Object.assign(form, row); formVisible.value = true }
async function saveRow() { if (form.id) await zoneApi.update(form.id, form); else await zoneApi.create(form); ElMessage.success('保存成功'); formVisible.value = false; await loadData() }
async function deleteRow(id: number) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await zoneApi.delete(id); ElMessage.success('删除成功'); await loadData() }
onMounted(loadData)
</script>
<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-weight: 600; font-size: 15px; }
.map-preview { height: 200px; border: 1px solid #e4e7ed; border-radius: 4px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; }
.map-placeholder { text-align: center; color: #909399; }
</style>
