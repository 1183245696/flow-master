import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      children: [
        { path: '', redirect: '/home' },
        { path: 'home', component: () => import('@/views/HomePage.vue') },
        { path: 'org', component: () => import('@/views/org/OrgPage.vue') },
        { path: 'users', component: () => import('@/views/user/UserPage.vue') },
        { path: 'process-config', component: () => import('@/views/process/ProcessConfigPage.vue') },
        { path: 'process-list', component: () => import('@/views/process/ProcessListPage.vue') },
        { path: 'process-category', component: () => import('@/views/process/ProcessCategoryPage.vue') },
        { path: 'blackwhitelist', component: () => import('@/views/blackwhite/BlackWhiteListPage.vue') },
        { path: 'menus', component: () => import('@/views/menu/MenuPage.vue') },
        { path: 'attendance-zones', component: () => import('@/views/attendance/AttZonePage.vue') },
        { path: 'att-rules', component: () => import('@/views/attendance/AttRulePage.vue') },
        { path: 'my-clock', component: () => import('@/views/attendance/MyClockPage.vue') },
        { path: 'all-clock', component: () => import('@/views/attendance/AllClockPage.vue') },
        { path: 'messages', component: () => import('@/views/message/MessagesPage.vue') },
        { path: 'profile', component: () => import('@/views/profile/ProfilePage.vue') }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (!to.meta.public && !authStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
