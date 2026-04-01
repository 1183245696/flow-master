<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo" @click="router.push('/home')">
        <span v-if="!collapsed">OA Platform</span>
        <el-icon v-else><HomeFilled /></el-icon>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/home"><el-icon><House /></el-icon><span>首页</span></el-menu-item>
        <el-sub-menu index="org">
          <template #title><el-icon><OfficeBuilding /></el-icon><span>组织架构</span></template>
          <el-menu-item index="/org">部门管理</el-menu-item>
          <el-menu-item index="/users">员工管理</el-menu-item>
          <el-menu-item index="/menus">菜单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="workflow">
          <template #title><el-icon><List /></el-icon><span>流程管理</span></template>
          <el-menu-item index="/process-config">流程配置</el-menu-item>
          <el-menu-item index="/process-list">流程列表</el-menu-item>
          <el-menu-item index="/process-category">流程分类</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="attendance">
          <template #title><el-icon><Clock /></el-icon><span>考勤管理</span></template>
          <el-menu-item index="/attendance-zones">考勤区域</el-menu-item>
          <el-menu-item index="/att-rules">打卡规则</el-menu-item>
          <el-menu-item index="/my-clock">我的打卡</el-menu-item>
          <el-menu-item index="/all-clock">全员打卡</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/blackwhitelist"><el-icon><Lock /></el-icon><span>黑白名单</span></el-menu-item>
        <el-menu-item index="/messages"><el-icon><Bell /></el-icon><span>消息中心</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- Top bar -->
      <el-header class="topbar">
        <el-icon class="collapse-btn" @click="collapsed = !collapsed">
          <Fold v-if="!collapsed" /><Expand v-else />
        </el-icon>
        <div class="topbar-right">
          <!-- Message bell -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="bell-badge">
            <el-icon class="bell-icon" @click="router.push('/messages')"><Bell /></el-icon>
          </el-badge>
          <!-- Avatar popover -->
          <el-popover placement="bottom-end" :width="200" trigger="hover">
            <template #reference>
              <el-avatar class="avatar" :src="userInfo?.avatar" @click="router.push('/profile')">
                {{ userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
            </template>
            <div class="user-popover">
              <p class="name">{{ userInfo?.realName }}</p>
              <p class="dept">部门：{{ userInfo?.deptName || '-' }}</p>
              <p class="pos">职位：{{ userInfo?.positionName || '-' }}</p>
              <el-divider />
              <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
            </div>
          </el-popover>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { messageApi } from '@/api'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)
const unreadCount = ref(0)
const userInfo = ref<any>(authStore.userInfo)

let stompClient: Client | null = null

onMounted(async () => {
  await authStore.loadUserInfo()
  userInfo.value = authStore.userInfo
  await loadUnreadCount()
  connectWebSocket()
})

onUnmounted(() => {
  stompClient?.deactivate()
})

async function loadUnreadCount() {
  try {
    const res: any = await messageApi.unreadCount()
    unreadCount.value = res.data
  } catch {}
}

function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: { 'X-User-Id': String(authStore.userId) },
    onConnect: () => {
      stompClient!.subscribe(`/user/${authStore.userId}/messages`, (msg) => {
        const data = JSON.parse(msg.body)
        if (data.unreadCount !== undefined) {
          unreadCount.value = data.unreadCount
        }
      })
    }
  })
  stompClient.activate()
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.sidebar { background: #001529; transition: width .25s; overflow: hidden; }
.logo { height: 64px; display: flex; align-items: center; justify-content: center;
        color: #fff; font-size: 18px; font-weight: bold; cursor: pointer; }
.topbar { display: flex; align-items: center; justify-content: space-between;
          background: #fff; border-bottom: 1px solid #f0f0f0; padding: 0 16px; }
.collapse-btn { font-size: 20px; cursor: pointer; }
.topbar-right { display: flex; align-items: center; gap: 20px; }
.bell-icon { font-size: 22px; cursor: pointer; }
.avatar { cursor: pointer; }
.main-content { background: #f5f5f5; padding: 16px; overflow-y: auto; }
.user-popover .name { font-weight: bold; margin: 0 0 4px; }
.user-popover .dept, .user-popover .pos { color: #666; margin: 2px 0; font-size: 13px; }
</style>
