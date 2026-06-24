<script setup lang="ts">
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { queryTraceability } from '@/api/traceability'
import ScannerOverlay from '@/components/ScannerOverlay.vue'

const router = useRouter()
const store = useAppStore()

function onScanSuccess(text: string) {
  const batchNo = text.trim()
  if (!batchNo) {
    showToast('未识别到有效溯源码')
    return
  }
  store.setBatchNo(batchNo)
  store.addHistory(batchNo)

  queryTraceability({ productBatchNo: batchNo })
    .then((res) => {
      if (res.code === 200 && res.data) {
        router.replace({ name: 'TraceResult', query: { batchNo } })
      } else {
        showToast(res.message || '未查到该批次信息')
        router.back()
      }
    })
    .catch((err) => {
      showToast(err.message || '查询失败')
      router.back()
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
