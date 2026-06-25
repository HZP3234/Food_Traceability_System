<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { queryByTraceCode } from '@/api/traceability'
import ScannerOverlay from '@/components/ScannerOverlay.vue'

const router = useRouter()
const store = useAppStore()

const showManualInput = ref(false)
const manualCode = ref('')

function doQuery(code: string) {
  const trimmed = code.trim()
  if (!trimmed) {
    showToast('未识别到有效溯源码')
    return
  }
  queryByTraceCode(trimmed, store.userInfo?.consumerUuid)
    .then((res) => {
      if (res.code === 200 && res.data) {
        store.setBatchNo(res.data.productBatchNo)
        store.setTraceResult(res.data)
        store.addHistory(trimmed)
        router.replace({ name: 'TraceResult', query: { batchNo: res.data.productBatchNo } })
      } else {
        showToast(res.message || '未查到该溯源码信息')
      }
    })
    .catch((err) => {
      showToast(err.message || '查询失败')
    })
}

function onScanSuccess(text: string) {
  doQuery(text)
}

function onManualInput() {
  manualCode.value = ''
  showManualInput.value = true
}

function onManualBeforeClose(action: string): boolean {
  if (action === 'confirm') {
    const code = manualCode.value.trim()
    if (!code) {
      showToast('请输入溯源码')
      return false
    }
    manualCode.value = ''
    doQuery(code)
  }
  return true
}

function onClose() {
  router.replace('/')
}
</script>

<template>
  <ScannerOverlay
    @scan-success="onScanSuccess"
    @close="onClose"
    @manual-input="onManualInput"
  />

  <van-dialog
    v-model:show="showManualInput"
    title="手动输入溯源码"
    show-cancel-button
    confirm-button-text="查询"
    :before-close="onManualBeforeClose"
  >
    <div class="manual-input-body">
      <van-field
        v-model="manualCode"
        placeholder="请输入或粘贴溯源码"
        clearable
        autofocus
        @keyup.enter="onManualBeforeClose('confirm')"
      />
    </div>
  </van-dialog>
</template>

<style scoped>
.manual-input-body {
  padding: 8px 16px 16px;
}
</style>
