<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { useAuth } from '@/composables/useAuth'
import { queryTraceability, recordScan } from '@/api/traceability'
import type { TraceabilityVO, TraceabilityNode } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const { requireLogin } = useAuth()
const loading = ref(true)
const data = ref<TraceabilityVO | null>(null)

const batchNo = (route.query.batchNo as string) || ''

// 四大溯源环节配置
const sectionConfig: Record<string, { label: string; icon: string; color: string; bg: string }> = {
  '原料':     { label: '原料供应',  icon: 'shop-o',         color: '#07c160', bg: '#f0faf4' },
  '生产':     { label: '生产加工',  icon: 'setting-o',      color: '#1989fa', bg: '#e8f4ff' },
  '冷链物流': { label: '冷链物流',  icon: 'logistics-o',    color: '#ff976a', bg: '#fff3ed' },
  '销售':     { label: '销售终端',  icon: 'shop-collect-o', color: '#9c6eff', bg: '#f5f0ff' },
}

// 将节点按四大环节分组
interface SectionGroup {
  key: string
  label: string
  icon: string
  color: string
  bg: string
  nodes: TraceabilityNode[]
}

const sections = computed<SectionGroup[]>(() => {
  const nodes = data.value?.nodes
  if (!nodes || !Array.isArray(nodes) || nodes.length === 0) return []
  const groups: SectionGroup[] = []
  const orderedKeys = ['原料', '生产', '冷链物流', '销售']
  const nodeMap = new Map<string, TraceabilityNode[]>()
  const otherNodes: TraceabilityNode[] = []

  for (const n of nodes) {
    const key = n.nodeName || '其他'
    if (orderedKeys.includes(key)) {
      if (!nodeMap.has(key)) nodeMap.set(key, [])
      nodeMap.get(key)!.push(n)
    } else {
      otherNodes.push(n)
    }
  }
  for (const key of orderedKeys) {
    const list = nodeMap.get(key)
    if (list && list.length) {
      const cfg = sectionConfig[key] || { label: key, icon: 'records-o', color: '#666', bg: '#f5f5f5' }
      groups.push({ key, label: cfg.label, icon: cfg.icon, color: cfg.color, bg: cfg.bg, nodes: list })
    }
  }
  // 未归类节点放在最后
  if (otherNodes.length) {
    groups.push({ key: '其他', label: '其他信息', icon: 'records-o', color: '#999', bg: '#f5f5f5', nodes: otherNodes })
  }
  return groups
})

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.split('T')[0] || dateStr
}

function formatDateTime(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
}

function goComplaint() {
  if (!requireLogin()) return
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
        <div class="product-grid">
          <div class="product-cell">
            <span class="cell-label">生产商</span>
            <span class="cell-value">{{ data.manufacturer || '-' }}</span>
          </div>
          <div class="product-cell">
            <span class="cell-label">供应商</span>
            <span class="cell-value">{{ data.supplierName || '-' }}</span>
          </div>
          <div class="product-cell">
            <span class="cell-label">生产日期</span>
            <span class="cell-value">{{ formatDate(data.productionDate) }}</span>
          </div>
          <div class="product-cell">
            <span class="cell-label">生产线</span>
            <span class="cell-value">{{ data.productionLine || '-' }}</span>
          </div>
        </div>
        <div v-if="data.txHash" class="blockchain-tag">
          <van-icon name="certificate" size="14" />
          <span>区块链存证: {{ data.txHash.substring(0, 16) }}...</span>
        </div>
      </div>

      <!-- 四大溯源环节 -->
      <div v-if="sections.length" class="sections-wrapper">
        <div class="section-title">
          <van-icon name="cluster-o" size="16" />
          <span>溯源链路</span>
        </div>
        <div
          v-for="(section, si) in sections"
          :key="section.key"
          class="section-block"
          :style="{ borderLeftColor: section.color }"
        >
          <!-- 环节标题 -->
          <div class="section-header" :style="{ background: section.bg }">
            <div class="section-icon" :style="{ background: section.color }">
              <van-icon :name="section.icon" size="16" color="#fff" />
            </div>
            <span class="section-label" :style="{ color: section.color }">{{ section.label }}</span>
            <span class="section-count">{{ section.nodes.length }}条记录</span>
          </div>
          <!-- 环节内的节点 -->
          <div class="timeline">
            <div
              v-for="(node, ni) in section.nodes"
              :key="node.id || ni"
              class="timeline-item"
              :class="{ last: ni === section.nodes.length - 1 }"
            >
              <div class="timeline-dot" :style="{ borderColor: section.color, background: ni === 0 ? section.color : '#fff' }" />
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

/* 四大溯源环节 */
.sections-wrapper {
  margin: 16px 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
  padding-left: 4px;
}

.section-block {
  margin-bottom: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;
  border-left: 4px solid #ccc;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 14px;
}

.section-icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  flex: 1;
}

.section-count {
  font-size: 11px;
  color: #969799;
}

/* 时间线 */
.timeline {
  position: relative;
  padding-left: 4px;
}

.timeline-item {
  display: flex;
  gap: 12px;
  padding-bottom: 18px;
  position: relative;
}

.timeline-item:not(.last)::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 22px;
  bottom: 0;
  width: 2px;
  background: #ebedf0;
}

.timeline-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #ccc;
  flex-shrink: 0;
  margin-top: 2px;
}

.timeline-content {
  flex: 1;
}

.node-title {
  font-size: 14px;
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
