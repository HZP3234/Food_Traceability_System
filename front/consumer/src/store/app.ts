import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { TraceabilityVO, UserInfo } from '@/types'

export const useAppStore = defineStore('app', () => {

  const traceResult = ref<TraceabilityVO | null>(null)
  const userInfo = ref<UserInfo | null>(loadUser())

  const isLoggedIn = computed(() => !!userInfo.value?.phone)

  function setTraceResult(data: TraceabilityVO | null) {
    traceResult.value = data
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('consumer_user', JSON.stringify(info))
  }

  function loadUser(): UserInfo | null {
    try {
      return JSON.parse(localStorage.getItem('consumer_user') || 'null')
    } catch {
      return null
    }
  }

  function logout() {
    userInfo.value = null
    localStorage.removeItem('consumer_user')
  }

  return {
    traceResult,
    userInfo,
    isLoggedIn,
    setTraceResult,
    setUserInfo,
    logout
  }
})
