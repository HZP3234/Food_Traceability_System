<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Check, Clock, Close, Refresh, Search, Warning, WarningFilled } from '@element-plus/icons-vue'
import { coldChainApi, productionApi } from '../services/api'

const loading = ref(false)
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)

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

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

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
  } catch (e: any) { notify('error', '加载预警数据失败') }
  finally { loading.value = false }
}

onMounted(loadAlerts)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <section class="trace-stats">
      <article :class="stats.total > 0 ? 'red' : 'green'">
        <span><el-icon><WarningFilled /></el-icon> 预警总数</span>
        <b>{{ stats.total }}</b><em>条预警</em>
      </article>
      <article :class="stats.cold > 0 ? 'red' : ''">
        <span><el-icon><Warning /></el-icon> 冷链温度预警</span>
        <b>{{ stats.cold }}</b><em>条记录</em>
      </article>
      <article :class="stats.quality > 0 ? 'amber' : ''">
        <span><el-icon><Close /></el-icon> 质检不合格</span>
        <b>{{ stats.quality }}</b><em>条记录</em>
      </article>
      <article :class="stats.env > 0 ? 'amber' : ''">
        <span><el-icon><Clock /></el-icon> 环境异常</span>
        <b>{{ stats.env }}</b><em>条记录</em>
      </article>
    </section>

    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'all' }" @click="tab = 'all'"><el-icon><Warning /></el-icon> 全部预警</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'cold' }" @click="tab = 'cold'"><el-icon><Warning /></el-icon> 冷链预警</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'quality' }" @click="tab = 'quality'"><el-icon><Search /></el-icon> 质检异常</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'env' }" @click="tab = 'env'"><el-icon><Clock /></el-icon> 环境异常</button>
    </div>

    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>预警台账</p><h2>预警列表</h2></div>
        <button class="secondary" @click="loadAlerts" :disabled="loading"><el-icon><Refresh /></el-icon> 刷新</button>
      </header>
    </section>

    <div v-if="loading" class="trace-empty-state"><div class="empty-icon"><el-icon><Clock /></el-icon></div><p>加载中...</p></div>

    <template v-else>
      <!-- 冷链温度预警 -->
      <div v-if="(tab === 'all' || tab === 'cold') && coldChainAlerts.length" style="margin-bottom:18px">
        <section class="trace-panel list-panel">
          <header class="panel-header"><div><p>冷链预警</p><h2>温度预警记录</h2></div></header>
          <div class="table-wrap"><table><thead><tr><th>订单号</th><th>车牌</th><th>产品</th><th>生产批次</th><th>发运地</th><th>目的地</th><th>预警状态</th></tr></thead>
            <tbody>
              <tr v-for="row in coldChainAlerts" :key="row.transportId">
                <td><code>{{ row.orderNo }}</code></td><td>{{ row.plateNo }}</td><td>{{ row.productName }}</td>
                <td><code>{{ row.prodBatchNo }}</code></td><td>{{ row.departureName }}</td><td>{{ row.destinationName }}</td>
                <td><span class="status status-void">⚠ 温度预警</span></td>
              </tr>
            </tbody></table></div>
        </section>
      </div>

      <!-- 质检不合格 -->
      <div v-if="(tab === 'all' || tab === 'quality') && qualityFailures.length" style="margin-bottom:18px">
        <section class="trace-panel list-panel">
          <header class="panel-header"><div><p>质检异常</p><h2>不合格记录</h2></div></header>
          <div class="table-wrap"><table><thead><tr><th>检验编号</th><th>业务类型</th><th>业务批次</th><th>检验类型</th><th>检验人</th><th>日期</th><th>结果描述</th></tr></thead>
            <tbody>
              <tr v-for="row in qualityFailures" :key="row.inspectionId">
                <td><code>{{ row.inspectionNo }}</code></td>
                <td>{{ ['','原料','加工','生产','冷链','销售'][row.bizType] || row.bizType }}</td>
                <td><code>{{ row.bizBatchNo }}</code></td>
                <td>{{ ['','自检','抽检','全检'][row.inspectionType] || '其他' }}</td>
                <td>{{ row.inspector }}</td><td>{{ row.inspectionDate }}</td>
                <td><span class="status status-void">{{ row.resultDesc || '不合格' }}</span></td>
              </tr>
            </tbody></table></div>
        </section>
      </div>

      <!-- 环境异常 -->
      <div v-if="(tab === 'all' || tab === 'env') && envAbnormals.length" style="margin-bottom:18px">
        <section class="trace-panel list-panel">
          <header class="panel-header"><div><p>环境异常</p><h2>环境监控记录</h2></div></header>
          <div class="table-wrap"><table><thead><tr><th>生产线</th><th>温度</th><th>湿度</th><th>洁净度</th><th>异常描述</th><th>记录时间</th></tr></thead>
            <tbody>
              <tr v-for="row in envAbnormals" :key="row.recordId">
                <td>{{ row.productionLine }}</td><td>{{ row.temperature }}</td><td>{{ row.humidity }}</td><td>{{ row.cleanliness }}</td>
                <td style="color:#c72929">{{ row.abnormalDesc || '-' }}</td><td>{{ row.recordTime }}</td>
              </tr>
            </tbody></table></div>
        </section>
      </div>

      <!-- 无数据 -->
      <div v-if="stats.total === 0" class="trace-empty-state">
        <div class="empty-icon"><el-icon><Check /></el-icon></div>
        <p style="color:#22b46d;font-weight:700">当前无预警信息，系统运行正常</p>
        <p style="font-size:12px;color:#92a6b9">如有异常事件发生，将在此处汇总展示</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';
</style>
