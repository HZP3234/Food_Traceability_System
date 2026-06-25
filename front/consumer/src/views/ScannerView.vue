<script setup lang="ts">
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { queryByTraceCode } from '@/api/traceability'
import ScannerOverlay from '@/components/ScannerOverlay.vue'

const router = useRouter()
const store = useAppStore()

function onScanSuccess(text: string) {
  const code = text.trim()
  if (!code) {
    showToast('未识别到有效溯源码')
    return
  }

  queryByTraceCode(code, store.userInfo?.consumerUuid)
    .then((res) => {
      if (res.code === 200 && res.data) {
        store.setBatchNo(res.data.productBatchNo)
        store.setTraceResult(res.data)
        store.addHistory(code)
        router.replace({ name: 'TraceResult', query: { batchNo: res.data.productBatchNo } })
      } else {
        showToast(res.message || '未查到该溯源码信息')
      }
    })
    .catch((err) => {
      showToast(err.message || '查询失败')
    })
}

function onClose() {
  router.back()
}
</script>

<template>
  <ScannerOverlay
    @scan-success="onScanSuccess"
    @close="onClose"
  />
</template>
