<script setup lang="ts">
import { ref, inject, onMounted, type Ref } from 'vue'
import { Box, Document, Grid, OfficeBuilding, SetUp, Shop, Van, Warning } from '@element-plus/icons-vue'
import { rawApi, productionApi, coldChainApi, salesApi, traceCodeApi, enterpriseApi, auditApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
const emit = defineEmits<{ (e: 'navigate', page: string): void }>()

const loading = ref(false)
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)

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

async function loadStats() {
  loading.value = true
  try {
    const results = await Promise.allSettled([
      rawApi.list({}),
      productionApi.listProdBatch({}),
      productionApi.listProcessBatch({}),
      traceCodeApi.list({ page: 1, pageSize: 1 }),
      coldChainApi.listTransport({}),
      salesApi.listTerminal({}),
      enterpriseApi.list({ page: 1, size: 1 }),
      auditApi.list({ page: 1, size: 1 }),
      productionApi.listInspection({ inspectionResult: 2 }),
      coldChainApi.listTransport({ transportStatus: 4 }),
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
  } catch (e: any) {} finally { loading.value = false }
}

const roleLabel: Record<string, string> = {
  'manufacturer': '加工生产商', 'supplier': '原料供应商', 'seller': '销售商',
  'logistics': '物流商', 'regulator': '监管机构', 'super-admin': '超级管理员',
}

const quickLinks = [
  { id: 'raw-batch', icon: Box, label: '原料批次', desc: '管理原料批次与质检' },
  { id: 'production-batch', icon: SetUp, label: '生产批次', desc: '生产/加工批次管理' },
  { id: 'trace-code', icon: Grid, label: '溯源码', desc: '生成与管理溯源码' },
  { id: 'cold-chain', icon: Van, label: '冷链物流', desc: '运输与仓储管理' },
  { id: 'sales-terminal', icon: Shop, label: '销售终端', desc: '终端与库存管理' },
  { id: 'audit-log', icon: Document, label: '审计日志', desc: '操作日志与审计' },
]

onMounted(loadStats)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <!-- 欢迎横幅 -->
    <div style="display:flex;align-items:center;justify-content:space-between;padding:24px 28px;background:linear-gradient(135deg,#eaf2ff,#f0f6ff);border:1px solid #d0e0f5;border-radius:14px;margin-bottom:22px">
      <div>
        <h2 style="margin:0 0 8px;color:#173c62;font-size:20px">欢迎使用食品溯源管理系统</h2>
        <p style="margin:0;color:#6c84a3;font-size:13px">当前角色：<strong>{{ roleLabel[currentRole ?? 'manufacturer'] || currentRole }}</strong> · 全链路可信溯源</p>
      </div>
      <div style="width:64px;height:64px;display:grid;place-items:center;border-radius:14px 14px 18px 18px;color:#fff;background:linear-gradient(135deg,#13a4bd,#245ee8);font-size:28px;font-weight:800">溯</div>
    </div>

    <!-- 统计卡片 -->
    <section class="trace-stats">
      <article><span><el-icon><Box /></el-icon> 原料批次</span><b>{{ stats.rawBatches }}</b><em>个批次</em></article>
      <article class="green"><span><el-icon><SetUp /></el-icon> 生产批次</span><b>{{ stats.prodBatches + stats.processBatches }}</b><em>含加工批次</em></article>
      <article><span><el-icon><Grid /></el-icon> 溯源码</span><b>{{ stats.traceCodes }}</b><em>个编码</em></article>
      <article :class="stats.qualityFailures > 0 || stats.coldChainAlerts > 0 ? 'red' : 'green'">
        <span><el-icon><Warning /></el-icon> {{ stats.qualityFailures + stats.coldChainAlerts > 0 ? '待处理预警' : '系统状态' }}</span>
        <b>{{ stats.qualityFailures + stats.coldChainAlerts > 0 ? stats.qualityFailures + stats.coldChainAlerts : '正常' }}</b>
        <em>{{ stats.qualityFailures + stats.coldChainAlerts > 0 ? '需关注' : '运行正常' }}</em>
      </article>
      <article><span><el-icon><OfficeBuilding /></el-icon> 企业/终端</span><b>{{ stats.enterprises + stats.terminals }}</b><em>家</em></article>
    </section>

    <!-- 详情行 -->
    <div style="display:flex;gap:18px;margin-bottom:18px">
      <!-- 预警摘要 -->
      <section class="trace-panel" style="flex:1;padding:18px 20px">
        <h3 style="margin:0 0 16px;font-size:15px;color:#25486b"><el-icon><Warning /></el-icon> 预警摘要</h3>
        <div style="display:flex;flex-direction:column;gap:12px">
          <div style="display:flex;align-items:center;gap:12px;padding:10px 12px;border-radius:8px" :style="stats.coldChainAlerts > 0 ? 'background:#fff5f5' : 'background:#f9fbfd'">
            <span style="width:8px;height:8px;border-radius:50%;flex-shrink:0" :style="{ background: stats.coldChainAlerts > 0 ? '#e5484d' : '#22b46d' }"></span>
            <div style="flex:1"><strong style="display:block;font-size:13px;color:#34536f">冷链温度预警</strong><p style="margin:2px 0 0;font-size:11px;color:#91a4bc">{{ stats.coldChainAlerts }} 条未处理</p></div>
            <span style="font-size:18px;font-weight:700" :style="{ color: stats.coldChainAlerts > 0 ? '#e5484d' : '#22b46d' }">{{ stats.coldChainAlerts }}</span>
          </div>
          <div style="display:flex;align-items:center;gap:12px;padding:10px 12px;border-radius:8px" :style="stats.qualityFailures > 0 ? 'background:#fff5f5' : 'background:#f9fbfd'">
            <span style="width:8px;height:8px;border-radius:50%;flex-shrink:0" :style="{ background: stats.qualityFailures > 0 ? '#e5484d' : '#22b46d' }"></span>
            <div style="flex:1"><strong style="display:block;font-size:13px;color:#34536f">质检不合格</strong><p style="margin:2px 0 0;font-size:11px;color:#91a4bc">{{ stats.qualityFailures }} 条记录</p></div>
            <span style="font-size:18px;font-weight:700" :style="{ color: stats.qualityFailures > 0 ? '#e5484d' : '#22b46d' }">{{ stats.qualityFailures }}</span>
          </div>
          <div style="display:flex;align-items:center;gap:12px;padding:10px 12px;border-radius:8px;background:#f9fbfd">
            <span style="width:8px;height:8px;border-radius:50%;flex-shrink:0;background:#22b46d"></span>
            <div style="flex:1"><strong style="display:block;font-size:13px;color:#34536f">企业资质异常</strong><p style="margin:2px 0 0;font-size:11px;color:#91a4bc">暂无异常</p></div>
            <span style="font-size:18px;font-weight:700;color:#22b46d">0</span>
          </div>
        </div>
      </section>

      <!-- 快捷入口 -->
      <section class="trace-panel" style="flex:2;padding:18px 20px">
        <h3 style="margin:0 0 16px;font-size:15px;color:#25486b">📌 快捷入口</h3>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px">
          <button v-for="link in quickLinks" :key="link.id" type="button" @click="$emit('navigate', link.id)"
            style="display:flex;align-items:center;gap:12px;padding:14px;border:1px solid #e6eef6;border-radius:10px;background:#fafcfe;cursor:pointer;text-align:left;transition:border-color .16s,box-shadow .16s,transform .16s"
            onmouseover="this.style.borderColor='#bdd3f0';this.style.boxShadow='0 4px 12px rgba(43,93,135,.08)';this.style.transform='translateY(-1px)'"
            onmouseout="this.style.borderColor='#e6eef6';this.style.boxShadow='none';this.style.transform='none'">
            <span style="width:36px;height:36px;display:grid;place-items:center;flex-shrink:0;border-radius:8px;color:#3778ed;background:#eef4ff;font-size:16px;font-weight:800">
              <el-icon><component :is="link.icon || Grid" /></el-icon>
            </span>
            <div><strong style="display:block;font-size:13px;color:#284666">{{ link.label }}</strong><small style="font-size:11px;color:#91a4bc">{{ link.desc }}</small></div>
          </button>
        </div>
      </section>
    </div>

    <!-- 流通环节概览 -->
    <section class="trace-panel" style="padding:18px 20px;margin-bottom:18px">
      <h3 style="margin:0 0 16px;font-size:15px;color:#25486b">📊 流通环节概览</h3>
      <div style="display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:14px">
        <div style="display:flex;flex-direction:column;gap:4px;padding:12px;background:#f8fbfe;border-radius:8px"><span style="font-size:12px;color:#91a4bc">运输订单</span><b style="font-size:22px;color:#284666">{{ stats.transports }}</b></div>
        <div style="display:flex;flex-direction:column;gap:4px;padding:12px;background:#f8fbfe;border-radius:8px"><span style="font-size:12px;color:#91a4bc">销售终端</span><b style="font-size:22px;color:#284666">{{ stats.terminals }}</b></div>
        <div style="display:flex;flex-direction:column;gap:4px;padding:12px;background:#f8fbfe;border-radius:8px"><span style="font-size:12px;color:#91a4bc">审计日志</span><b style="font-size:22px;color:#284666">{{ stats.auditLogs }}</b></div>
        <div style="display:flex;flex-direction:column;gap:4px;padding:12px;background:#f8fbfe;border-radius:8px"><span style="font-size:12px;color:#91a4bc">企业主体</span><b style="font-size:22px;color:#284666">{{ stats.enterprises }}</b></div>
      </div>
    </section>

    <div v-if="loading" class="trace-empty-state"><p>加载统计数据中...</p></div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';
</style>
