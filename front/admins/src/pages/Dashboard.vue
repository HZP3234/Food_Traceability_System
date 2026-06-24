<script setup lang="ts">
import { ref, inject, onMounted, type Ref } from 'vue'
import { rawApi, productionApi, coldChainApi, salesApi, traceCodeApi, enterpriseApi, auditApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
const emit = defineEmits<{ (e: 'navigate', page: string): void }>()

const loading = ref(false)
const toast = ref<{ type: string; msg: string } | null>(null)

const stats = ref({
  rawBatches: 0,
  prodBatches: 0,
  processBatches: 0,
  traceCodes: 0,
  transports: 0,
  terminals: 0,
  enterprises: 0,
  auditLogs: 0,
  qualityFailures: 0,
  coldChainAlerts: 0,
})

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }

async function loadStats() {
  loading.value = true
  try {
    // 并行加载所有统计
    const results = await Promise.allSettled([
      rawApi.list({}),                                          // 0
      productionApi.listProdBatch({}),                          // 1
      productionApi.listProcessBatch({}),                       // 2
      traceCodeApi.list({ page: 1, pageSize: 1 }),             // 3
      coldChainApi.listTransport({}),                           // 4
      salesApi.listTerminal({}),                                // 5
      enterpriseApi.list({ page: 1, size: 1 }),                 // 6
      auditApi.list({ page: 1, size: 1 }),                      // 7
      productionApi.listInspection({ inspectionResult: 2 }),    // 8
      coldChainApi.listTransport({ transportStatus: 4 }),       // 9
    ])

    const getArr = (r: PromiseSettledResult<any>) => (r.status === 'fulfilled' && Array.isArray(r.value) ? r.value : [])
    const getPageTotal = (r: PromiseSettledResult<any>) => {
      if (r.status !== 'fulfilled') return 0
      const d = r.value
      return (d as any)?.data?.total ?? (Array.isArray(d) ? d.length : 0)
    }

    stats.value.rawBatches = getArr(results[0]).length
    stats.value.prodBatches = getArr(results[1]).length
    stats.value.processBatches = getArr(results[2]).length
    stats.value.traceCodes = getPageTotal(results[3])
    stats.value.transports = getArr(results[4]).length
    stats.value.terminals = getArr(results[5]).length
    stats.value.enterprises = getPageTotal(results[6])
    stats.value.auditLogs = getPageTotal(results[7])
    stats.value.qualityFailures = getArr(results[8]).length
    stats.value.coldChainAlerts = getArr(results[9]).length
  } catch (e: any) {
    // 静默处理
  } finally {
    loading.value = false
  }
}

const roleLabel: Record<string, string> = {
  'manufacturer': '加工生产商',
  'supplier': '原料供应商',
  'seller': '销售商',
  'logistics': '物流商',
  'regulator': '监管机构',
  'super-admin': '超级管理员',
}

const quickLinks = [
  { id: 'raw-batch', icon: '原', label: '原料批次', desc: '管理原料批次与质检' },
  { id: 'production-batch', icon: '产', label: '生产批次', desc: '生产/加工批次管理' },
  { id: 'trace-code', icon: '码', label: '溯源码', desc: '生成与管理溯源码' },
  { id: 'cold-chain', icon: '冷', label: '冷链物流', desc: '运输与仓储管理' },
  { id: 'sales-terminal', icon: '售', label: '销售终端', desc: '终端与库存管理' },
  { id: 'audit-log', icon: '志', label: '审计日志', desc: '操作日志与审计' },
]

onMounted(loadStats)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 欢迎横幅 -->
    <div class="dashboard-welcome">
      <div class="welcome-left">
        <h2>欢迎使用食品溯源管理系统</h2>
        <p>当前角色：<strong>{{ roleLabel[currentRole ?? 'manufacturer'] || currentRole }}</strong> · 全链路可信溯源，从源头到餐桌的每一环节都可验证</p>
      </div>
      <div class="welcome-right">
        <div class="welcome-icon">溯</div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="page-stats" style="grid-template-columns: repeat(5, 1fr)">
      <div class="stat-card">
        <div class="stat-meta"><span>原料批次</span><span class="stat-ico">原</span></div>
        <b>{{ stats.rawBatches }}</b>
      </div>
      <div class="stat-card green">
        <div class="stat-meta"><span>生产批次</span><span class="stat-ico">产</span></div>
        <b>{{ stats.prodBatches + stats.processBatches }}</b>
      </div>
      <div class="stat-card">
        <div class="stat-meta"><span>溯源码</span><span class="stat-ico">码</span></div>
        <b>{{ stats.traceCodes }}</b>
      </div>
      <div class="stat-card amber" v-if="stats.qualityFailures > 0 || stats.coldChainAlerts > 0">
        <div class="stat-meta"><span>待处理预警</span><span class="stat-ico">⚠</span></div>
        <b>{{ stats.qualityFailures + stats.coldChainAlerts }}</b>
      </div>
      <div class="stat-card green" v-else>
        <div class="stat-meta"><span>系统状态</span><span class="stat-ico">✓</span></div>
        <b>正常</b>
      </div>
      <div class="stat-card">
        <div class="stat-meta"><span>企业/终端</span><span class="stat-ico">企</span></div>
        <b>{{ stats.enterprises + stats.terminals }}</b>
      </div>
    </div>

    <!-- 详情行 -->
    <div class="dashboard-row">
      <!-- 预警摘要 -->
      <div class="dashboard-card" style="flex:1">
        <div class="card-header">
          <h3>⚠️ 预警摘要</h3>
        </div>
        <div class="alert-summary">
          <div class="alert-item" :class="{ danger: stats.coldChainAlerts > 0 }">
            <span class="alert-dot"></span>
            <div>
              <strong>冷链温度预警</strong>
              <p>{{ stats.coldChainAlerts }} 条未处理</p>
            </div>
            <span class="alert-count" :class="stats.coldChainAlerts > 0 ? 'danger-count' : 'safe-count'">{{ stats.coldChainAlerts }}</span>
          </div>
          <div class="alert-item" :class="{ danger: stats.qualityFailures > 0 }">
            <span class="alert-dot"></span>
            <div>
              <strong>质检不合格</strong>
              <p>{{ stats.qualityFailures }} 条记录</p>
            </div>
            <span class="alert-count" :class="stats.qualityFailures > 0 ? 'danger-count' : 'safe-count'">{{ stats.qualityFailures }}</span>
          </div>
          <div class="alert-item safe">
            <span class="alert-dot"></span>
            <div>
              <strong>企业资质异常</strong>
              <p>暂无异常</p>
            </div>
            <span class="alert-count safe-count">0</span>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="dashboard-card" style="flex:2">
        <div class="card-header">
          <h3>📌 快捷入口</h3>
        </div>
        <div class="quick-links">
          <button
            v-for="link in quickLinks"
            :key="link.id"
            class="quick-link-card"
            type="button"
            @click="$emit('navigate', link.id)"
          >
            <span class="ql-icon">{{ link.icon }}</span>
            <div class="ql-text">
              <strong>{{ link.label }}</strong>
              <small>{{ link.desc }}</small>
            </div>
          </button>
        </div>
      </div>
    </div>

    <!-- 数据统计行 -->
    <div class="dashboard-row">
      <div class="dashboard-card" style="flex:1">
        <div class="card-header"><h3>📊 流通环节概览</h3></div>
        <div class="summary-grid">
          <div class="summary-item"><span>运输订单</span><b>{{ stats.transports }}</b></div>
          <div class="summary-item"><span>销售终端</span><b>{{ stats.terminals }}</b></div>
          <div class="summary-item"><span>审计日志</span><b>{{ stats.auditLogs }}</b></div>
          <div class="summary-item"><span>企业主体</span><b>{{ stats.enterprises }}</b></div>
        </div>
      </div>
    </div>

    <div v-if="loading" style="text-align:center;padding:24px;color:#91a4bc">加载统计数据中...</div>
  </div>
</template>

<style scoped>
.dashboard-welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #eaf1fb, #f0f6ff);
  border: 1px solid #d0e0f5;
  border-radius: 14px;
  margin-bottom: 22px;
}
.welcome-left h2 { margin: 0 0 8px; color: #173c62; font-size: 20px; }
.welcome-left p { margin: 0; color: #6c84a3; font-size: 13px; line-height: 1.6; }
.welcome-icon {
  width: 64px; height: 64px; display: grid; place-items: center;
  border-radius: 14px 14px 18px 18px;
  color: #fff; background: linear-gradient(135deg, #13a4bd, #245ee8);
  font-size: 28px; font-weight: 800; box-shadow: 0 8px 22px rgba(29, 120, 205, 0.28);
}
.dashboard-row { display: flex; gap: 18px; margin-bottom: 18px; }
.dashboard-card {
  background: #fff; border: 1px solid #dce8f4; border-radius: 12px;
  padding: 18px 20px; box-shadow: 0 2px 8px rgba(43, 93, 135, 0.06);
}
.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.card-header h3 { margin: 0; font-size: 15px; color: #284666; }
.alert-summary { display: flex; flex-direction: column; gap: 12px; }
.alert-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; border-radius: 8px; background: #f9fbfd;
}
.alert-item.danger { background: #fff5f5; }
.alert-dot { width: 8px; height: 8px; border-radius: 50%; background: #22b46d; flex-shrink: 0; }
.alert-item.danger .alert-dot { background: #e5484d; }
.alert-item div { flex: 1; }
.alert-item strong { display: block; font-size: 13px; color: #34536f; }
.alert-item p { margin: 2px 0 0; font-size: 11px; color: #91a4bc; }
.alert-item.danger p { color: #c72929; }
.alert-count { font-size: 18px; font-weight: 700; }
.safe-count { color: #22b46d; }
.danger-count { color: #e5484d; }
.quick-links { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.quick-link-card {
  display: flex; align-items: center; gap: 12px;
  padding: 14px; border: 1px solid #e6eef6; border-radius: 10px;
  background: #fafcfe; cursor: pointer; text-align: left;
  transition: border-color .16s, box-shadow .16s, transform .16s;
}
.quick-link-card:hover { border-color: #bdd3f0; box-shadow: 0 4px 12px rgba(43, 93, 135, 0.08); transform: translateY(-1px); }
.ql-icon {
  width: 36px; height: 36px; display: grid; place-items: center; flex-shrink: 0;
  border-radius: 8px; color: #3778ed; background: #eef4ff; font-size: 16px; font-weight: 800;
}
.ql-text strong { display: block; font-size: 13px; color: #284666; }
.ql-text small { font-size: 11px; color: #91a4bc; }
.summary-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.summary-item { display: flex; flex-direction: column; gap: 4px; padding: 12px; background: #f8fbfe; border-radius: 8px; }
.summary-item span { font-size: 12px; color: #91a4bc; }
.summary-item b { font-size: 22px; color: #284666; }
</style>
