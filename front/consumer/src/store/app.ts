import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 当前查询的批次号
  const currentBatchNo = ref('')

  // 搜索历史（存本地）
  const searchHistory = ref<string[]>(loadHistory())

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

  return { currentBatchNo, searchHistory, setBatchNo, addHistory, clearHistory }
})
