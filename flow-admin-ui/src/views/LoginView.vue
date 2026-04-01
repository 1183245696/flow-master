<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <el-icon class="logo-icon"><OfficeBuilding /></el-icon>
        <h2>OA Platform</h2>
        <p class="subtitle">企业一体化办公平台</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3a5f 0%, #0a1628 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-logo {
  text-align: center;
  margin-bottom: 36px;
}
.logo-icon {
  font-size: 48px;
  color: #409eff;
}
.login-logo h2 {
  margin: 8px 0 4px;
  font-size: 24px;
  color: #303133;
}
.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
.login-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.login-btn {
  width: 100%;
  margin-top: 8px;
  font-size: 16px;
  letter-spacing: 4px;
}
</style>
