<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { traceCodeApi } from '../services/api'

type TraceCodeRow = {
  traceCode: string
  codeType?: number
  codeTypeName?: string
  packageLevelName?: string
  productName?: string
  enterpriseName?: string
  batchNo?: string
  traceCodeStatus?: number
  traceCodeStatusName?: string
  contentHash?: string
  isOnChain?: number
  generateTime?: string
  expireTime?: string
}

const loading = ref(false)
const rows = ref<TraceCodeRow[]>([])
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showGenerate = ref(false)
const showDetail = ref(false)
const showStatus = ref(false)
const selected = ref<any>(null)
const detail = ref<any>(null)
const statusAction = ref<'activate' | 'disable' | 'void'>('activate')

const filters = ref({ traceCode: '', productName: '', batchNo: '', traceCodeStatus: '', isOnChain: '' })
const generateForm = ref({
  productId: '', productName: '', enterpriseId: '', enterpriseName: '', batchNo: '',
  codeType: 1, packageLevel: 1, generateCount: 1, qualityResult: 1,
  expireTime: '', operator: 'admin',
})
const statusForm = ref({ reason: '', operator: 'admin' })

const stats = computed(() => ({
  total: rows.value.length,
  active: rows.value.filter((row) => row.traceCodeStatus === 2).length,
  onChain: rows.value.filter((row) => row.isOnChain === 1).length,
  pending: rows.value.filter((row) => row.traceCodeStatus === 1).length,
}))

const actionLabel = computed(() => ({ activate: '激活溯源码', disable: '禁用溯源码', void: '作废溯源码' })[statusAction.value])
const actionTarget = computed(() => ({ activate: 2, disable: 3, void: 4 })[statusAction.value])

function notify(type: 'success' | 'error', text: string) {
  toast.value = { type, text }
  window.setTimeout(() => (toast.value = null), 2600)
}

function statusClass(status?: number) {
  return ({ 1: 'status-pending', 2: 'status-active', 3: 'status-disabled', 4: 'status-void', 5: 'status-expired' } as Record<number, string>)[status ?? 0] || 'status-pending'
}

function statusText(row: TraceCodeRow) {
  return row.traceCodeStatusName || ({ 0: '未绑定', 1: '待激活', 2: '已激活', 3: '已禁用', 4: '已作废', 5: '已过期' } as Record<number, string>)[row.traceCodeStatus ?? 0] || '未知'
}

async function loadList() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: 1, pageSize: 50 }
    Object.entries(filters.value).forEach(([key, value]) => {
      if (value !== '') params[key] = key === 'traceCodeStatus' || key === 'isOnChain' ? Number(value) : value
    })
    const data = await traceCodeApi.list(params)
    rows.value = Array.isArray(data) ? data : []
  } catch (error: any) {
    rows.value = []
    notify('error', `加载失败：${error.message || '请确认企业端服务已启动'}`)
  } finally { loading.value = false }
}

function resetFilters() {
  filters.value = { traceCode: '', productName: '', batchNo: '', traceCodeStatus: '', isOnChain: '' }
  loadList()
}

async function submitGenerate() {
  if (!generateForm.value.productId || !generateForm.value.productName || !generateForm.value.enterpriseId || !generateForm.value.batchNo) {
    notify('error', '请填写产品、企业与生产批次信息')
    return
  }
  try {
    const result = await traceCodeApi.batchGenerate(generateForm.value)
    showGenerate.value = false
    notify('success', `已生成 ${Array.isArray(result) ? result.length : generateForm.value.generateCount} 个溯源码`)
    loadList()
  } catch (error: any) { notify('error', `生成失败：${error.message}`) }
}

async function openDetail(row: TraceCodeRow) {
  selected.value = row
  detail.value = null
  showDetail.value = true
  try { detail.value = await traceCodeApi.detail(row.traceCode) }
  catch (error: any) { notify('error', `详情加载失败：${error.message}`) }
}

function openStatus(row: TraceCodeRow, action: 'activate' | 'disable' | 'void') {
  selected.value = row
  statusAction.value = action
  statusForm.value = { reason: '', operator: 'admin' }
  showStatus.value = true
}

async function submitStatus() {
  if ((statusAction.value === 'disable' || statusAction.value === 'void') && !statusForm.value.reason.trim()) {
    notify('error', '禁用或作废必须填写原因')
    return
  }
  try {
    await traceCodeApi.updateStatus({ traceCode: selected.value.traceCode, targetStatus: actionTarget.value, ...statusForm.value })
    showStatus.value = false
    notify('success', `${selected.value.traceCode} 已${actionLabel.value.replace('溯源码', '')}`)
    loadList()
  } catch (error: any) { notify('error', `操作失败：${error.message}`) }
}

async function verify(row: TraceCodeRow) {
  try { notify('success', String(await traceCodeApi.verifyHash(row.traceCode))) }
  catch (error: any) { notify('error', `校验失败：${error.message}`) }
}

onMounted(loadList)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <section class="trace-stats">
      <article><span>当前查询结果</span><b>{{ stats.total }}</b><em>条溯源码</em></article>
      <article class="green"><span>已激活</span><b>{{ stats.active }}</b><em>可供消费者查询</em></article>
      <article class="blue"><span>已上链存证</span><b>{{ stats.onChain }}</b><em>Hash 完整可验证</em></article>
      <article class="amber"><span>待激活</span><b>{{ stats.pending }}</b><em>等待业务确认</em></article>
    </section>

    <section class="trace-panel filter-panel">
      <div class="filter-grid">
        <label>溯源码<input v-model="filters.traceCode" placeholder="输入完整或部分编码" @keyup.enter="loadList" /></label>
        <label>产品名称<input v-model="filters.productName" placeholder="例如：鲜牛奶" @keyup.enter="loadList" /></label>
        <label>生产批次<input v-model="filters.batchNo" placeholder="例如：PB20260601" @keyup.enter="loadList" /></label>
        <label>状态<select v-model="filters.traceCodeStatus"><option value="">全部状态</option><option value="1">待激活</option><option value="2">已激活</option><option value="3">已禁用</option><option value="4">已作废</option></select></label>
        <label>链上状态<select v-model="filters.isOnChain"><option value="">全部</option><option value="1">已上链</option><option value="0">未上链</option></select></label>
        <div class="filter-actions"><button class="secondary" @click="resetFilters">重置</button><button class="primary" @click="loadList">查询</button></div>
      </div>
    </section>

    <section class="trace-panel list-panel">
      <header class="panel-header"><div><p>编码台账</p><h2>溯源码列表</h2></div><button class="primary create" @click="showGenerate = true">＋ 批量生成溯源码</button></header>
      <div class="table-wrap"><table><thead><tr><th>溯源码</th><th>产品 / 批次</th><th>企业</th><th>码类型</th><th>状态</th><th>链上存证</th><th>生成时间</th><th>操作</th></tr></thead>
        <tbody><tr v-if="loading"><td colspan="8" class="empty">正在加载数据…</td></tr><tr v-else-if="!rows.length"><td colspan="8" class="empty">暂无数据。请启动企业端后查询，或点击右上角生成溯源码。</td></tr>
          <tr v-for="row in rows" :key="row.traceCode"><td><code>{{ row.traceCode }}</code></td><td><strong>{{ row.productName || '—' }}</strong><small>{{ row.batchNo || '未关联批次' }}</small></td><td>{{ row.enterpriseName || '—' }}</td><td>{{ row.codeTypeName || '单品码' }}<small>{{ row.packageLevelName || '' }}</small></td><td><span class="status" :class="statusClass(row.traceCodeStatus)">{{ statusText(row) }}</span></td><td><span class="chain" :class="{ yes: row.isOnChain === 1 }">{{ row.isOnChain === 1 ? '已上链' : '未上链' }}</span></td><td>{{ row.generateTime || '—' }}</td><td class="actions"><button @click="openDetail(row)">详情</button><button @click="verify(row)">验真</button><button v-if="row.traceCodeStatus === 1" @click="openStatus(row, 'activate')">激活</button><button v-if="row.traceCodeStatus === 2" class="danger" @click="openStatus(row, 'disable')">禁用</button><button v-if="row.traceCodeStatus !== 4" class="danger" @click="openStatus(row, 'void')">作废</button></td></tr>
        </tbody></table></div>
    </section>

    <div v-if="showGenerate" class="trace-modal-backdrop" @click.self="showGenerate = false"><section class="trace-modal"><header><div><p>批量生成</p><h2>创建一组可追溯编码</h2></div><button @click="showGenerate = false">×</button></header><div class="modal-body grid-form"><label>产品 ID *<input v-model="generateForm.productId" placeholder="PROD2026001" /></label><label>产品名称 *<input v-model="generateForm.productName" placeholder="产品名称" /></label><label>企业 ID *<input v-model="generateForm.enterpriseId" placeholder="企业 UUID" /></label><label>企业名称<input v-model="generateForm.enterpriseName" placeholder="企业全称" /></label><label>生产批次 *<input v-model="generateForm.batchNo" placeholder="PB20260601" /></label><label>生成数量 *<input v-model.number="generateForm.generateCount" min="1" max="10000" type="number" /></label><label>码类型<select v-model.number="generateForm.codeType"><option :value="1">单品码</option><option :value="2">批次码</option><option :value="3">箱码</option><option :value="4">托盘码</option></select></label><label>包装层级<select v-model.number="generateForm.packageLevel"><option :value="1">最小销售单元</option><option :value="2">外箱</option><option :value="3">托盘</option></select></label><label>质检结果<select v-model.number="generateForm.qualityResult"><option :value="1">合格</option><option :value="3">待检</option><option :value="2">不合格</option></select></label><label>有效期<input v-model="generateForm.expireTime" placeholder="2027-06-24 23:59:59" /></label></div><footer><button class="secondary" @click="showGenerate = false">取消</button><button class="primary" @click="submitGenerate">确认生成</button></footer></section></div>

    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false"><section class="trace-modal detail-modal"><header><div><p>编码详情</p><h2>{{ selected?.traceCode }}</h2></div><button @click="showDetail = false">×</button></header><div v-if="detail" class="modal-body detail-grid"><div><span>产品名称</span><b>{{ detail.productName || '—' }}</b></div><div><span>生产批次</span><b>{{ detail.batchNo || '—' }}</b></div><div><span>企业</span><b>{{ detail.enterpriseName || '—' }}</b></div><div><span>状态</span><b>{{ detail.traceCodeStatusName || '—' }}</b></div><div><span>生成时间</span><b>{{ detail.generateTime || '—' }}</b></div><div><span>有效期</span><b>{{ detail.expireTime || '—' }}</b></div><div class="full"><span>内容 Hash</span><code>{{ detail.contentHash || '—' }}</code></div><div v-if="detail.reason" class="full warning-detail"><span>状态原因</span><b>{{ detail.reason }}</b></div></div><div v-else class="modal-body empty">正在读取详情…</div></section></div>

    <div v-if="showStatus" class="trace-modal-backdrop" @click.self="showStatus = false"><section class="trace-modal status-modal"><header><div><p>状态变更</p><h2>{{ actionLabel }}</h2></div><button @click="showStatus = false">×</button></header><div class="modal-body"><p class="target-code">{{ selected?.traceCode }}</p><label v-if="statusAction !== 'activate'">操作原因 *<textarea v-model="statusForm.reason" placeholder="请说明原因，将写入操作记录" /></label><label>操作人<input v-model="statusForm.operator" /></label></div><footer><button class="secondary" @click="showStatus = false">取消</button><button class="primary" :class="{ 'danger-fill': statusAction !== 'activate' }" @click="submitStatus">确认{{ actionLabel }}</button></footer></section></div>
  </div>
</template>

<style scoped>
.trace-page{color:#244564}.trace-stats{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-bottom:18px}.trace-stats article{position:relative;overflow:hidden;padding:17px 18px;border:1px solid #dce8f4;border-radius:12px;background:#fff;box-shadow:0 4px 13px #2b5d8710}.trace-stats article:before{position:absolute;right:-14px;bottom:-25px;width:76px;height:76px;border-radius:50%;background:#eaf2ff;content:''}.trace-stats .green:before{background:#e6f8ee}.trace-stats .blue:before{background:#eaf2ff}.trace-stats .amber:before{background:#fff3d9}.trace-stats span,.trace-stats em{display:block;font-size:12px;font-style:normal;color:#8195aa}.trace-stats b{display:inline-block;margin:7px 6px 1px 0;color:#173c62;font-size:26px}.trace-panel{border:1px solid #dce8f4;border-radius:12px;background:#fff;box-shadow:0 4px 13px #2b5d870c}.filter-panel{padding:16px 18px;margin-bottom:18px}.filter-grid{display:grid;grid-template-columns:1.25fr 1fr 1fr .8fr .8fr auto;gap:12px;align-items:end}.filter-grid label,.grid-form label,.status-modal label{display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700}.filter-grid input,.filter-grid select,.grid-form input,.grid-form select,.status-modal input,.status-modal textarea{width:100%;padding:9px 10px;border:1px solid #d7e4f0;border-radius:7px;outline-color:#2a6cea;color:#294b6e;background:#fff}.filter-actions{display:flex;gap:8px}.primary,.secondary{border:0;border-radius:7px;padding:9px 14px;font-weight:700}.primary{color:#fff;background:#2467df;box-shadow:0 5px 10px #2368d629}.secondary{color:#507391;background:#f0f5fa}.panel-header{display:flex;align-items:center;justify-content:space-between;padding:18px 19px 14px;border-bottom:1px solid #e6eef6}.panel-header p,.trace-modal header p{margin:0 0 3px;color:#4b83d7;font-size:12px;font-weight:800;letter-spacing:.08em}.panel-header h2,.trace-modal h2{margin:0;color:#25486b;font-size:18px}.create{padding:10px 15px}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse;min-width:1060px}th{padding:12px 14px;background:#f8fbfe;color:#7087a1;font-size:12px;text-align:left}td{padding:13px 14px;border-top:1px solid #edf2f7;color:#46617d;font-size:13px}td strong,td small{display:block}td strong{color:#294863;font-size:13px}td small{margin-top:4px;color:#96a8b9;font-size:11px}code{color:#2366d9;font-family:Consolas,monospace;font-size:12px}.empty{padding:42px;text-align:center;color:#92a6b9}.status,.chain{display:inline-block;border-radius:20px;padding:4px 8px;font-size:11px;font-weight:700}.status-pending{color:#a4730a;background:#fff4d9}.status-active,.chain.yes{color:#198658;background:#e5f8ef}.status-disabled,.status-void{color:#c04550;background:#ffebed}.status-expired{color:#6e7985;background:#edf0f3}.chain{color:#8092a4;background:#eff3f7}.actions{white-space:nowrap}.actions button{margin-right:7px;padding:0;border:0;color:#2468dc;background:transparent;font-size:12px}.actions .danger{color:#d24d58}.trace-toast{position:fixed;z-index:20;top:20px;right:26px;max-width:430px;padding:12px 16px;border-radius:9px;color:#fff;box-shadow:0 10px 25px #163d6840;font-size:13px}.trace-toast.success{background:#1d9a66}.trace-toast.error{background:#d34f59}.trace-modal-backdrop{position:fixed;z-index:15;inset:0;display:grid;place-items:center;background:#17385b65;backdrop-filter:blur(3px)}.trace-modal{width:min(680px,calc(100vw - 42px));border-radius:14px;background:#fff;box-shadow:0 24px 65px #0d2b4f4a}.trace-modal header{display:flex;align-items:center;justify-content:space-between;padding:20px 23px;border-bottom:1px solid #e8eff6}.trace-modal header button{width:28px;height:28px;border:0;border-radius:50%;color:#7890a8;background:#eef4f9;font-size:20px}.modal-body{padding:22px 23px}.grid-form{display:grid;grid-template-columns:1fr 1fr;gap:15px 18px}.trace-modal footer{display:flex;justify-content:flex-end;gap:10px;padding:15px 23px;border-top:1px solid #e8eff6}.detail-grid{display:grid;grid-template-columns:1fr 1fr;gap:18px}.detail-grid div{display:grid;gap:5px}.detail-grid span{color:#91a3b4;font-size:12px}.detail-grid b{color:#34536f;font-size:13px}.detail-grid .full{grid-column:1/-1}.detail-grid code{display:block;overflow:auto;padding:9px;border-radius:6px;background:#f6f9fc;white-space:nowrap}.warning-detail{padding:10px;border-radius:7px;background:#fff6ed}.target-code{margin:0 0 18px;padding:11px;border-radius:7px;color:#2366d9;background:#f1f6ff;font-family:Consolas,monospace}.status-modal textarea{min-height:84px;resize:vertical}.status-modal label+label{margin-top:16px}.danger-fill{background:#d34f59}@media(max-width:1250px){.trace-stats{grid-template-columns:repeat(2,1fr)}.filter-grid{grid-template-columns:repeat(3,1fr)}}
</style>
