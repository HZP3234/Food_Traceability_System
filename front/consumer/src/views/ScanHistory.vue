<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { getUserScans } from '@/api/traceability'
import type { ScanRecord } from '@/types'

const router = useRouter()
const store = useAppStore()
const loading = ref(true)
const list = ref<ScanRecord[]>([])

function formatDateTime(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
}

function onItemClick(record: ScanRecord) {
  router.push({ name: 'TraceResult', query: { batchNo: record.productBatchNo } })
}

onMounted(() => {
  const userId = store.userInfo?.consumerUuid
  if (!userId) {
    showToast('请先登录')
    router.replace({ name: 'Login', query: { redirect: '/scan-history' } })
    return
  }

  getUserScans(userId)
    .then((res) => {
      if (res.code === 200 && res.data) {
        list.value = res.data
      }
    })
    .catch(() => {
      showToast('加载失败')
    })
    .finally(() => {
      loading.value = false
    })
})
</script>

<template>
  <div class="sh-page">
    <van-nav-bar
      title="扫码历史"
      left-arrow
      @click-left="router.back"
      fixed
      placeholder
    />

    <van-loading v-if="loading" type="spinner" size="32" class="loading" />

    <van-empty v-if="!loading && list.length === 0" description="暂无扫码记录">
      <van-button round type="primary" to="/">去扫码</van-button>
    </van-empty>

    <div v-if="list.length > 0" class="list-wrap">
      <div
        v-for="item in list"
        :key="item.id"
        class="scan-item"
        @click="onItemClick(item)"
      >
        <div class="item-left">
          <div class="item-batch">{{ item.productBatchNo }}</div>
          <div class="item-time">
            <van-icon name="clock-o" size="12" />
            {{ formatDateTime(item.scanTime) }}
          </div>
        </div>
        <van-icon name="arrow" size="14" color="#c8c9cc" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.sh-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading {
  margin-top: 50vh;
  display: flex;
  justify-content: center;
}

.list-wrap {
  padding: 12px 16px;
}

.scan-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 10px;
  cursor: pointer;
}

.scan-item:active {
  background: #f7f8fa;
}

.item-left {
  flex: 1;
  min-width: 0;
}

.item-batch {
  font-size: 15px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 12px;
  color: #969799;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
