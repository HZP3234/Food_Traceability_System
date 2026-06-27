import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'

export function useAuth() {
  const router = useRouter()
  const store = useAppStore()

  function requireLogin(action?: () => void): boolean {
    if (!store.isLoggedIn) {
      showToast({
        message: '请先登录后再使用此功能',
        className: 'toast-black-text'
      })
      router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
      return false
    }
    if (action) {
      action()
    }
    return true
  }

  return { requireLogin, isLoggedIn: store.isLoggedIn }
}
