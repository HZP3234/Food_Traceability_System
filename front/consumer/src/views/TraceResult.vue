<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { queryTraceability, recordScan } from '@/api/traceability'
import type { TraceabilityVO } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const loading = ref(true)
const data = ref<TraceabilityVO | null>(null)

const batchNo = (route.query.batchNo as string) || ''

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.split('T')[0] || dateStr
}

function formatDateTime(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
}

function goComplaint() {
  if (!data.value) return
  router.push({
    name: 'ComplaintSubmit',
    query: {
      batchNumber: data.value.productBatchNo,
      enterpriseName: data.value.manufacturer,
      traceCode: data.value.traceCode || ''
    }
  })
}

onMounted(() => {
  if (!batchNo) {
    showToast({ message: '缺少批次号', className: 'toast-error' })
    router.replace({ name: 'Home' })
    return
  }

  const userId = store.userInfo?.consumerUuid

  const cached = store.traceResult
  if (cached && cached.productBatchNo === batchNo) {
    data.value = cached
    store.setTraceResult(null)
    loading.value = false
    recordScan({ productBatchNo: batchNo, userId, traceCode: cached.traceCode }).catch(() => {})
    return
  }

  queryTraceability({ productBatchNo: batchNo, userId })
    .then((res) => {
      if (res.code === 200 && res.data) {
        data.value = res.data
        recordScan({ productBatchNo: batchNo, userId, traceCode: res.data.traceCode }).catch(() => {})
      } else if (res.code === 404 || res.code === 400) {
        showToast({ message: res.message || '该商品没有溯源信息或溯源码被禁用', className: 'toast-error' })
      } else {
        showToast({ message: res.message || '查询失败，请稍后重试', className: 'toast-error' })
      }
    })
    .catch(() => {
      showToast({ message: '网络异常，请检查网络后重试', className: 'toast-error' })
    })
    .finally(() => {
      loading.value = false
    })
})
</script>

<template>
  <div class="trace-page">
    <van-nav-bar
      title="溯源结果"
      left-arrow
      @click-left="router.back"
      fixed
      placeholder
    />

    <van-loading v-if="loading" type="spinner" size="32" class="loading" />

    <template v-if="data">
      <!-- 产品信息卡片 -->
      <div class="product-card">
        <div class="product-badge">
          <van-icon name="passed" size="18" color="#fff" />
          <span>已认证</span>
        </div>
        <div class="product-name">{{ data.productName }}</div>
        <div class="product-batch">批次号：{{ data.productBatchNo }}</div>

        <div class="product-grid">
          <div class="product-cell">
            <span class="cell-label">生产商</span>
            <span class="cell-value">{{ data.manufacturer || '-' }}</span>
          </div>
          <div class="product-cell">
            <span class="cell-label">生产日期</span>
            <span class="cell-value">{{ formatDate(data.productionDate) }}</span>
          </div>
          <div class="product-cell">
            <span class="cell-label">生产线</span>
            <span class="cell-value">{{ data.productionLine || '-' }}</span>
          </div>
          <div class="product-cell">
            <span class="cell-label">质检结果</span>
            <span class="cell-value" :class="{ 'check-ok': data.checkResult === 1, 'check-fail': data.checkResult === 2 }">
              {{ data.checkResult === 1 ? '合格' : data.checkResult === 2 ? '不合格' : '-' }}
            </span>
          </div>
        </div>
        <div v-if="data.txHash" class="blockchain-tag">
          <van-icon name="certificate" size="14" />
          <span>区块链存证: {{ data.txHash.substring(0, 16) }}...</span>
        </div>
      </div>

      <!-- 溯源链路时间线 -->
      <div v-if="data.nodes && data.nodes.length" class="timeline-card">
        <div class="timeline-title">溯源链路</div>
        <div class="timeline">
          <div
            v-for="(node, index) in data.nodes"
            :key="node.id || index"
            class="timeline-item"
            :class="{ last: index === data.nodes.length - 1 }"
          >
            <div class="timeline-dot" :class="{ active: index === 0 }">
              <van-icon v-if="index === 0" name="checked" size="14" color="#fff" />
            </div>
            <div class="timeline-content">
              <div class="node-title">{{ node.nodeName }}</div>
              <div class="node-desc">{{ node.nodeDescription }}</div>
              <div class="node-meta">
                <span v-if="node.location">
                  <van-icon name="location-o" size="11" />
                  {{ node.location }}
                </span>
                <span v-if="node.nodeTime">
                  <van-icon name="clock-o" size="11" />
                  {{ formatDateTime(node.nodeTime) }}
                </span>
                <span v-if="node.operator">
                  <van-icon name="user-o" size="11" />
                  {{ node.operator }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <van-empty v-else description="暂无溯源节点信息" />

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <van-button type="warning" block round @click="goComplaint">
          我要投诉
        </van-button>
        <van-button type="default" block round @click="router.push('/')">
          继续查询
        </van-button>
      </div>
    </template>

    <van-empty v-if="!loading && !data" description="未找到溯源信息">
      <van-button round type="primary" to="/">返回首页</van-button>
    </van-empty>
  </div>
</template>

<style scoped>
.trace-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading {
  margin-top: 50vh;
  display: flex;
  justify-content: center;
}

/* 产品信息卡片 */
.product-card {
  margin: 16px 20px;
  padding: 20px 16px 16px;
  background: #fff;
  border-radius: 12px;
  position: relative;
}

.product-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #07c160, #06ad56);
  border-radius: 20px;
  color: #fff;
  font-size: 12px;
  margin-bottom: 12px;
}

.product-name {
  font-size: 22px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 6px;
}

.product-batch {
  font-size: 13px;
  color: #07c160;
  margin-bottom: 16px;
  font-weight: 500;
}

.blockchain-tag {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f0faf4;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #07c160;
}

.product-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.product-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cell-label {
  font-size: 11px;
  color: #969799;
}

.cell-value {
  font-size: 14px;
  color: #323233;
}

.check-ok {
  color: #07c160;
  font-weight: 600;
}

.check-fail {
  color: #ee0a24;
  font-weight: 600;
}

/* 时间线 */
.timeline-card {
  margin: 16px 20px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 16px;
}

.timeline {
  position: relative;
}

.timeline-item {
  display: flex;
  gap: 12px;
  padding-bottom: 20px;
  position: relative;
}

.timeline-item:not(.last)::before {
  content: '';
  position: absolute;
  left: 9px;
  top: 24px;
  bottom: 0;
  width: 2px;
  background: #ebedf0;
}

.timeline-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ebedf0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.timeline-dot.active {
  background: #07c160;
}

.timeline-content {
  flex: 1;
}

.node-title {
  font-size: 15px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 4px;
}

.node-desc {
  font-size: 13px;
  color: #646566;
  margin-bottom: 6px;
  line-height: 1.4;
}

.node-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 11px;
  color: #969799;
}

.node-meta span {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

/* 操作按钮 */
.action-buttons {
  margin: 32px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
