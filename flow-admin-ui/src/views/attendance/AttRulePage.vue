<template>
  <el-card>
    <template #header><div class="page-header"><span class="page-title">打卡规则</span><el-button type="primary" @click="openForm(null)">新增规则</el-button></div></template>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="name" label="规则名称" /><el-table-column prop="type" label="类型" width="120" /><el-table-column prop="workStart" label="上班时间" width="100" /><el-table-column prop="workEnd" label="下班时间" width="100" /><el-table-column prop="flexMinutes" label="弹性(分钟)" width="110" />
      <el-table-column label="操作" width="160"><template #default="{ row }"><el-button size="small" @click="openForm(row)">编辑</el-button><el-button size="small" type="danger" @click="deleteRow(row.id)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="formVisible" :title="form.id ? '编辑规则' : '新增规则'" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="规则名称" :rules="[{ required: true }]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="班制类型">
          <el-select v-model="form.type" style="width:160px"><el-option label="双休" value="双休" /><el-option label="大小周" value="大小周" /><el-option label="自定义" value="自定义" /></el-select>
        </el-form-item>
        <el-form-item label="工作日" v-if="form.type === '自定义'">
          <el-checkbox-group v-model="form.workDaysArr"><el-checkbox v-for="(d,i) in ['周一','周二','周三','周四','周五','周六','周日']" :key="i" :label="i+1">{{ d }}</el-checkbox></el-checkbox-group>
        </el-form-item>
        <el-form-item label="上班时间"><el-time-picker v-model="form.workStart" format="HH:mm" value-format="HH:mm" /></el-form-item>
        <el-form-item label="下班时间"><el-time-picker v-model="form.workEnd" format="HH:mm" value-format="HH:mm" /></el-form-item>
        <el-form-item label="弹性时间(分钟)"><el-input-number v-model="form.flexMinutes" :min="0" :max="120" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="formVisible = false">取消</el-button><el-button type="primary" @click="saveRow">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ruleApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
const list = ref<any[]>([]); const loading = ref(false); const formVisible = ref(false)
const form = reactive<any>({ id: null, name: '', type: '双休', workDaysArr: [1,2,3,4,5], workStart: '09:00', workEnd: '18:00', flexMinutes: 30 })
async function loadData() { loading.value = true; const r: any = await ruleApi.list(); list.value = r.data || []; loading.value = false }
function openForm(row: any) { Object.assign(form, { id: null, name: '', type: '双休', workDaysArr: [1,2,3,4,5], workStart: '09:00', workEnd: '18:00', flexMinutes: 30 }); if (row) { Object.assign(form, row); try { form.workDaysArr = JSON.parse(row.workDays || '[1,2,3,4,5]') } catch {} }; formVisible.value = true }
async function saveRow() { const payload = { ...form, workDays: JSON.stringify(form.workDaysArr) }; if (form.id) await ruleApi.update(form.id, payload); else await ruleApi.create(payload); ElMessage.success('保存成功'); formVisible.value = false; await loadData() }
async function deleteRow(id: number) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await ruleApi.delete(id); ElMessage.success('删除成功'); await loadData() }
onMounted(loadData)
</script>
<style scoped>.page-header{display:flex;justify-content:space-between;align-items:center}.page-title{font-weight:600;font-size:15px}</style>
