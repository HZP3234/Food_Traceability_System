<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { coldChainApi, productionApi } from '../services/api'

const loading = ref(false)
const toast = ref<{ type: string; msg: string } | null>(null)

const coldChainAlerts = ref<any[]>([])
const qualityFailures = ref<any[]>([])
const envAbnormals = ref<any[]>([])

const tab = ref<'all' | 'cold' | 'quality' | 'env'>('all')

const stats = computed(() => ({
  total: coldChainAlerts.value.length + qualityFailures.value.length + envAbnormals.value.length,
  cold: coldChainAlerts.value.length,
  quality: qualityFailures.value.length,
  env: envAbnormals.value.length,
}))

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }

async function loadAlerts() {
  loading.value = true
  try {
    const [cold, quality, env] = await Promise.allSettled([
      coldChainApi.listTransport({ transportStatus: 4 }),
      productionApi.listInspection({ inspectionResult: 2 }),
      productionApi.listEnvAbnormal(1),
    ])
    coldChainAlerts.value = cold.status === 'fulfilled' && Array.isArray(cold.value) ? cold.value : []
    qualityFailures.value = quality.status === 'fulfilled' && Array.isArray(quality.value) ? quality.value : []
    envAbnormals.value = env.status === 'fulfilled' && Array.isArray(env.value) ? env.value : []
  } catch (e: any) {
    flash('error', '加载预警数据失败')
  } finally {
    loading.value = false
  }
}

function tagClass(status: number) {
  return status === 4 ? 'tag-danger' : 'tag-warn'
}

onMounted(loadAlerts)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 统计卡片 -->
    <div class="page-stats">
      <div class="stat-card red" v-if="stats.total > 0">
        <div class="stat-meta"><span>预警总数</span><span class="stat-ico">⚠</span></div>
        <b>{{ stats.total }}</b>
      </div>
      <div class="stat-card green" v-else>
        <div class="stat-meta"><span>预警总数</span><span class="stat-ico">✓</span></div>
        <b>0</b>
      </div>
      <div class="stat-card" :class="stats.cold > 0 ? 'red' : ''">
        <div class="stat-meta"><span>冷链温度预警</span><span class="stat-ico">🌡</span></div>
        <b>{{ stats.cold }}</b>
      </div>
      <div class="stat-card" :class="stats.quality > 0 ? 'amber' : ''">
        <div class="stat-meta"><span>质检不合格</span><span class="stat-ico">🔬</span></div>
        <b>{{ stats.quality }}</b>
      </div>
      <div class="stat-card" :class="stats.env > 0 ? 'amber' : ''">
        <div class="stat-meta"><span>环境异常</span><span class="stat-ico">🌡</span></div>
        <b>{{ stats.env }}</b>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'all' }" @click="tab = 'all'">📋 全部预警 ({{ stats.total }})</button>
      <button class="tab-btn" :class="{ active: tab === 'cold' }" @click="tab = 'cold'">🚛 冷链预警 ({{ stats.cold }})</button>
      <button class="tab-btn" :class="{ active: tab === 'quality' }" @click="tab = 'quality'">🔬 质检异常 ({{ stats.quality }})</button>
      <button class="tab-btn" :class="{ active: tab === 'env' }" @click="tab = 'env'">🌡 环境异常 ({{ stats.env }})</button>
    </div>

    <div class="toolbar">
      <h3>预警列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ tab === 'all' ? stats.total : tab === 'cold' ? stats.cold : tab === 'quality' ? stats.quality : stats.env }} 条)</span></h3>
      <button class="btn btn-outline btn-sm" @click="loadAlerts" :disabled="loading">🔄 刷新</button>
    </div>

    <div v-if="loading" style="text-align:center;padding:40px;color:#91a4bc">加载中...</div>

    <template v-else>
      <!-- 冷链温度预警 -->
      <div v-if="(tab === 'all' || tab === 'cold') && coldChainAlerts.length" style="margin-bottom:18px">
        <h4 v-if="tab === 'all'" style="color:#c72929;font-size:14px;margin:0 0 10px">🚛 冷链温度预警</h4>
        <div class="data-table-wrap">
          <table class="data-table">
            <thead><tr><th>订单号</th><th>车牌</th><th>产品</th><th>生产批次</th><th>发运地</th><th>目的地</th><th>预警状态</th></tr></thead>
            <tbody>
              <tr v-for="row in coldChainAlerts" :key="row.transportId">
                <td><code style="color:#c72929;font-size:13px">{{ row.orderNo }}</code></td>
                <td>{{ row.plateNo }}</td>
                <td>{{ row.productName }}</td>
                <td><code style="font-size:12px">{{ row.prodBatchNo }}</code></td>
                <td>{{ row.departureName }}</td>
                <td>{{ row.destinationName }}</td>
                <td><span class="tag tag-danger">⚠ 温度预警</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 质检不合格 -->
      <div v-if="(tab === 'all' || tab === 'quality') && qualityFailures.length" style="margin-bottom:18px">
        <h4 v-if="tab === 'all'" style="color:#c72929;font-size:14px;margin:0 0 10px">🔬 质检不合格</h4>
        <div class="data-table-wrap">
          <table class="data-table">
            <thead><tr><th>检验编号</th><th>业务类型</th><th>业务批次</th><th>检验类型</th><th>检验人</th><th>日期</th><th>结果描述</th></tr></thead>
            <tbody>
              <tr v-for="row in qualityFailures" :key="row.inspectionId">
                <td><code style="font-size:12px">{{ row.inspectionNo }}</code></td>
                <td>{{ ['','原料','加工','生产','冷链','销售'][row.bizType] || row.bizType }}</td>
                <td><code style="font-size:12px">{{ row.bizBatchNo }}</code></td>
                <td>{{ ['','自检','抽检','全检'][row.inspectionType] || '其他' }}</td>
                <td>{{ row.inspector }}</td>
                <td>{{ row.inspectionDate }}</td>
                <td><span class="tag tag-danger">{{ row.resultDesc || '不合格' }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 环境异常 -->
      <div v-if="(tab === 'all' || tab === 'env') && envAbnormals.length" style="margin-bottom:18px">
        <h4 v-if="tab === 'all'" style="color:#c72929;font-size:14px;margin:0 0 10px">🌡 环境异常</h4>
        <div class="data-table-wrap">
          <table class="data-table">
            <thead><tr><th>生产线</th><th>温度</th><th>湿度</th><th>洁净度</th><th>异常描述</th><th>记录时间</th></tr></thead>
            <tbody>
              <tr v-for="row in envAbnormals" :key="row.recordId">
                <td>{{ row.productionLine }}</td>
                <td>{{ row.temperature }}</td>
                <td>{{ row.humidity }}</td>
                <td>{{ row.cleanliness }}</td>
                <td style="color:#c72929">{{ row.abnormalDesc || '-' }}</td>
                <td>{{ row.recordTime }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 无数据 -->
      <div v-if="stats.total === 0" class="empty-state" style="padding:40px">
        <div class="empty-icon">✅</div>
        <p style="color:#22b46d;font-weight:700">当前无预警信息，系统运行正常</p>
        <p style="font-size:12px;color:#91a4bc">如有异常事件发生，将在此处汇总展示</p>
      </div>
    </template>
  </div>
</template>
