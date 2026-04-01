import type { Directive } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * v-permission="'GET:/v1/users/**'"
 * Hides element if user lacks the button permission code
 */
export const permission: Directive = {
  mounted(el, binding) {
    const authStore = useAuthStore()
    const code = binding.value as string
    if (!authStore.hasPermission(code)) {
      el.style.display = 'none'
    }
  },
  updated(el, binding) {
    const authStore = useAuthStore()
    const code = binding.value as string
    el.style.display = authStore.hasPermission(code) ? '' : 'none'
  }
}
