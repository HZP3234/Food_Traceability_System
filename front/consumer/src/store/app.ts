import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'

export const useAppStore = defineStore('app', () => {
  const currentBatchNo = ref('')

  const searchHistory = ref<string[]>(loadHistory())

  const userInfo = ref<UserInfo | null>(loadUser())

  const isLoggedIn = computed(() => !!userInfo.value?.phone)

  function setBatchNo(val: string) {
    currentBatchNo.value = val
  }

  function addHistory(keyword: string) {
    if (!keyword.trim()) return
    searchHistory.value = [
      keyword,
      ...searchHistory.value.filter((h) => h !== keyword)
    ].slice(0, 10)
    saveHistory(searchHistory.value)
  }

  function clearHistory() {
    searchHistory.value = []
    saveHistory([])
  }

  function loadHistory(): string[] {
    try {
      return JSON.parse(localStorage.getItem('trace_search_history') || '[]')
    } catch {
      return []
    }
  }

  function saveHistory(list: string[]) {
    localStorage.setItem('trace_search_history', JSON.stringify(list))
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
    currentBatchNo,
    searchHistory,
    userInfo,
    isLoggedIn,
    setBatchNo,
    addHistory,
    clearHistory,
    setUserInfo,
    logout
  }
})
