<template>
  <el-card style="max-width:600px;margin:0 auto">
    <template #header><span class="page-title">个人信息</span></template>
    <el-form :model="form" label-width="90px" v-loading="loading">
      <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
      <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      <el-form-item label="工号"><el-input v-model="form.employeeNo" disabled /></el-form-item>
      <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
      <el-form-item><el-button type="primary" @click="save">保存</el-button></el-form-item>
    </el-form>
  </el-card>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
const authStore = useAuthStore(); const loading = ref(false)
const form = reactive<any>({ realName: '', phone: '', email: '', employeeNo: '', username: '' })
async function loadProfile() { loading.value = true; await authStore.loadUserInfo(); Object.assign(form, authStore.userInfo || {}); loading.value = false }
async function save() { await userApi.update(authStore.userInfo?.id, form); ElMessage.success('保存成功'); await authStore.loadUserInfo() }
onMounted(loadProfile)
</script>
<style scoped>.page-title{font-weight:600;font-size:15px}</style>
