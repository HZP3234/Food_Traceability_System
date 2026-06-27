<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue'
import {
  Check, CircleClose, Close, Clock, Connection, Document,
  Plus, Refresh, Search, View, Download,
} from '@element-plus/icons-vue'
import { traceCodeApi, productionApi } from '../services/api'
import Pagination from '../components/Pagination.vue'
import QRCode from 'qrcode'

// ---- Types ----
type BindRow = {
  bizType: string
  bizTypeName: string
  bizNo: string
  bindTime: string
}

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

type DetailData = {
  traceCode: string
  codeTypeName?: string
  packageLevelName?: string
  productName?: string
  enterpriseName?: string
  batchNo?: string
  traceCodeStatusName?: string
  contentHash?: string
  txHash?: string
  proofId?: string
  qualityResult?: number
  qualityReportUrl?: string
  reason?: string
  generateTime?: string
  enableTime?: string
  expireTime?: string
  operator?: string
  bindList?: BindRow[]
}

// ---- State ----
const loading = ref(false)
const rows = ref<TraceCodeRow[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return rows.value.slice(start, start + pageSize.value)
})
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showGenerate = ref(false)
const showDetail = ref(false)
const showStatus = ref(false)
const selected = ref<any>(null)
const detail = ref<DetailData | null>(null)
const detailBinds = ref<BindRow[]>([])
const statusAction = ref<'activate' | 'disable' | 'void'>('activate')

// Expanded rows — only one at a time (accordion)
const expandedCode = ref<string | null>(null)
const expandedData = reactive<Record<string, { loading: boolean; data: DetailData | null }>>({})

// QR cache
const qrCache = reactive<Record<string, { dataUrl: string; blobUrl: string }>>({})

const filters = ref({
  traceCode: '', productName: '', batchNo: '', traceCodeStatus: '', isOnChain: '',
})
// 从 sessionStorage 获取当前企业信息
let currentUser: { enterpriseUuid: string; enterpriseName: string; realName: string; username: string; roleType: string } | null = null
try {
  const raw = sessionStorage.getItem('fts-admin-user')
  if (raw) currentUser = JSON.parse(raw)
} catch { /* ignore */ }
const generateForm = ref({
  productName: '', enterpriseId: currentUser?.enterpriseUuid || '', enterpriseName: currentUser?.enterpriseName || '', batchNo: '',
  codeType: 1, packageLevel: 1, generateCount: 1, qualityResult: 1,
  expireTime: '', operator: currentUser?.realName || currentUser?.username || 'admin',
})
const statusForm = ref({ reason: '', operator: currentUser?.realName || currentUser?.username || 'admin' })
// 批次下拉选项
const batchOptions = ref<{ batchNo: string; productName: string; checkResult?: number }[]>([])
const batchLoading = ref(false)
// 选中批次时自动同步产品名称和质检结果
function onBatchSelect(batchNo: string) {
  const batch = batchOptions.value.find(b => b.batchNo === batchNo)
  if (batch) {
    generateForm.value.productName = batch.productName || ''
    // 使用批次已有的质检结果，默认为待检(3)
    generateForm.value.qualityResult = batch.checkResult != null && batch.checkResult > 0 ? batch.checkResult : 3
  }
}

// ---- Computed ----
const stats = computed(() => ({
  total: rows.value.length,
  active: rows.value.filter((row) => row.traceCodeStatus === 2).length,
  onChain: rows.value.filter((row) => row.isOnChain === 1).length,
  pending: rows.value.filter((row) => row.traceCodeStatus === 1).length,
}))

const actionLabel = computed(
  () => ({ activate: '激活溯源码', disable: '禁用溯源码', void: '作废溯源码' })[statusAction.value],
)
const actionTarget = computed(
  () => ({ activate: 2, disable: 3, void: 4 })[statusAction.value],
)

// ---- Helpers ----
function notify(type: 'success' | 'error', text: string) {
  toast.value = { type, text }
  window.setTimeout(() => (toast.value = null), 2600)
}

function statusClass(status?: number) {
  return (
    { 1: 'status-pending', 2: 'status-active', 3: 'status-disabled', 4: 'status-void', 5: 'status-expired' } as Record<number, string>
  )[status ?? 0] || 'status-pending'
}

function statusText(row: TraceCodeRow) {
  return (
    row.traceCodeStatusName ||
    ({ 0: '未绑定', 1: '待激活', 2: '已激活', 3: '已禁用', 4: '已作废', 5: '已过期' } as Record<number, string>)[
      row.traceCodeStatus ?? 0
    ] ||
    '未知'
  )
}

function qualityText(qr?: number) {
  if (qr == null) return '—'
  return ({ 1: '合格', 2: '不合格', 3: '待检' } as Record<number, string>)[qr] || '—'
}

function qualityClass(qr?: number) {
  if (qr == null) return ''
  return ({ 1: 'qa-pass', 2: 'qa-fail', 3: 'qa-pending' } as Record<number, string>)[qr] || ''
}

// ---- QR Code generation + download ----
async function getQrCode(code: string, size: number): Promise<string> {
  const key = `${code}_${size}`
  if (qrCache[key]) return qrCache[key].dataUrl
  try {
    const dataUrl = await QRCode.toDataURL(code, {
      width: size,
      margin: 1,
      color: { dark: '#244564', light: '#ffffff' },
    })
    // Also generate a blob URL for download
    const blob = await (await fetch(dataUrl)).blob()
    const blobUrl = URL.createObjectURL(blob)
    qrCache[key] = { dataUrl, blobUrl }
    return dataUrl
  } catch {
    return ''
  }
}

/** Click QR code to download as PNG */
function downloadQr(code: string, size: number) {
  const key = `${code}_${size}`
  const cached = qrCache[key]
  if (!cached) return
  const a = document.createElement('a')
  a.href = cached.dataUrl
  a.download = `QR_${code}.png`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

// ---- Data loading ----
async function loadList() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: 1, pageSize: 50 }
    Object.entries(filters.value).forEach(([key, value]) => {
      if (value !== '')
        params[key] = key === 'traceCodeStatus' || key === 'isOnChain' ? Number(value) : value
    })
    const data = await traceCodeApi.list(params)
    rows.value = Array.isArray(data) ? data : []
    currentPage.value = 1
    expandedCode.value = null
    Object.keys(expandedData).forEach((k) => delete expandedData[k])
  } catch (error: any) {
    rows.value = []
    currentPage.value = 1
    notify('error', `加载失败：${error.message || '请确认企业端服务已启动'}`)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    traceCode: '', productName: '', batchNo: '', traceCodeStatus: '', isOnChain: '',
  }
  loadList()
}

// ---- Row expand / collapse (accordion) ----
async function toggleExpand(row: TraceCodeRow) {
  const code = row.traceCode
  if (expandedCode.value === code) {
    expandedCode.value = null
    return
  }
  expandedCode.value = code
  if (!expandedData[code]) {
    expandedData[code] = { loading: true, data: null }
    try {
      const [detailResult, bindsResult] = await Promise.all([
        traceCodeApi.detail(code),
        traceCodeApi.listBinds(code),
      ])
      const d = detailResult as DetailData
      d.bindList = Array.isArray(bindsResult) ? bindsResult : []
      expandedData[code] = { loading: false, data: d }
      getQrCode(code, 200)
    } catch {
      expandedData[code] = { loading: false, data: null }
    }
  } else {
    getQrCode(code, 200)
  }
}

// ---- Generate ----
async function openGenerate() {
  generateForm.value = {
    productName: '', enterpriseId: currentUser?.enterpriseUuid || '', enterpriseName: currentUser?.enterpriseName || '', batchNo: '',
    codeType: 1, packageLevel: 1, generateCount: 1, qualityResult: 3,
    expireTime: '', operator: currentUser?.realName || currentUser?.username || 'admin',
  }
  // 加载生产批次下拉选项
  batchLoading.value = true
  try {
    const data = await productionApi.listProdBatch({})
    batchOptions.value = Array.isArray(data) ? data : []
  } catch { batchOptions.value = [] }
  finally { batchLoading.value = false }
  showGenerate.value = true
}

async function submitGenerate() {
  if (
    !generateForm.value.productName ||
    !generateForm.value.enterpriseId ||
    !generateForm.value.batchNo
  ) {
    notify('error', '请填写产品名称、企业与生产批次信息')
    return
  }
  try {
    const result = await traceCodeApi.batchGenerate(generateForm.value)
    showGenerate.value = false
    notify(
      'success',
      `已生成 ${Array.isArray(result) ? result.length : generateForm.value.generateCount} 个溯源码`,
    )
    loadList()
  } catch (error: any) {
    notify('error', `生成失败：${error.message}`)
  }
}

// ---- Detail modal ----
async function openDetail(row: TraceCodeRow) {
  selected.value = row
  detail.value = null
  detailBinds.value = []
  showDetail.value = true
  getQrCode(row.traceCode, 200)
  try {
    const [d, binds] = await Promise.all([
      traceCodeApi.detail(row.traceCode),
      traceCodeApi.listBinds(row.traceCode),
    ])
    detail.value = d as DetailData
    detailBinds.value = Array.isArray(binds) ? binds : []
  } catch (error: any) {
    notify('error', `详情加载失败：${error.message}`)
  }
}

// ---- Status change ----
function openStatus(row: TraceCodeRow, action: 'activate' | 'disable' | 'void') {
  selected.value = row
  statusAction.value = action
  statusForm.value = { reason: '', operator: 'admin' }
  showStatus.value = true
}

async function submitStatus() {
  if (
    (statusAction.value === 'disable' || statusAction.value === 'void') &&
    !statusForm.value.reason.trim()
  ) {
    notify('error', '禁用或作废必须填写原因')
    return
  }
  try {
    await traceCodeApi.updateStatus({
      traceCode: selected.value.traceCode,
      targetStatus: actionTarget.value,
      ...statusForm.value,
    })
    showStatus.value = false
    notify('success', `${selected.value.traceCode} 已${actionLabel.value.replace('溯源码', '')}`)
    loadList()
  } catch (error: any) {
    notify('error', `操作失败：${error.message}`)
  }
}

// ---- Verify ----
async function verify(row: TraceCodeRow) {
  try {
    notify('success', String(await traceCodeApi.verifyHash(row.traceCode)))
  } catch (error: any) {
    notify('error', `校验失败：${error.message}`)
  }
}

onMounted(loadList)
</script>

<template>
  <div class="trace-page">
    <!-- Toast -->
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <!-- Stats -->
    <section class="trace-stats">
      <article>
        <span><el-icon><Document /></el-icon> 当前查询结果</span>
        <b>{{ stats.total }}</b><em>条溯源码</em>
      </article>
      <article class="green">
        <span><el-icon><Check /></el-icon> 已激活</span>
        <b>{{ stats.active }}</b><em>可供消费者查询</em>
      </article>
      <article class="blue">
        <span><el-icon><Connection /></el-icon> 已上链存证</span>
        <b>{{ stats.onChain }}</b><em>Hash 完整可验证</em>
      </article>
      <article class="amber">
        <span><el-icon><Clock /></el-icon> 待激活</span>
        <b>{{ stats.pending }}</b><em>等待业务确认</em>
      </article>
    </section>

    <!-- Filters -->
    <section class="trace-panel filter-panel">
      <div class="filter-grid">
        <label>溯源码<input v-model="filters.traceCode" placeholder="输入完整或部分编码" @keyup.enter="loadList" /></label>
        <label>产品名称<input v-model="filters.productName" placeholder="例如：鲜牛奶" @keyup.enter="loadList" /></label>
        <label>生产批次<input v-model="filters.batchNo" placeholder="例如：PB20260601" @keyup.enter="loadList" /></label>
        <label>状态<select v-model="filters.traceCodeStatus"><option value="">全部状态</option><option value="1">待激活</option><option value="2">已激活</option><option value="3">已禁用</option><option value="4">已作废</option></select></label>
        <label>链上状态<select v-model="filters.isOnChain"><option value="">全部</option><option value="1">已上链</option><option value="0">未上链</option></select></label>
        <div class="filter-actions">
          <button class="secondary" @click="resetFilters"><el-icon><Refresh /></el-icon> 重置</button>
          <button class="primary" @click="loadList"><el-icon><Search /></el-icon> 查询</button>
        </div>
      </div>
    </section>

    <!-- List -->
    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>编码台账</p><h2>溯源码列表</h2></div>
        <button class="primary create" @click="openGenerate"><el-icon><Plus /></el-icon> 批量生成溯源码</button>
      </header>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>溯源码</th>
              <th>产品名称</th>
              <th>生产批次</th>
              <th>企业</th>
              <th>码类型</th>
              <th>状态</th>
              <th>链上存证</th>
              <th>生成时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="9" class="empty">正在加载数据…</td></tr>
            <tr v-else-if="!rows.length"><td colspan="9" class="empty">暂无数据。请启动企业端后查询，或点击右上角生成溯源码。</td></tr>

            <template v-for="row in paginatedRows" :key="row.traceCode">
              <tr :class="{ 'row-expanded': expandedCode === row.traceCode }">
                <td><code>{{ row.traceCode }}</code></td>
                <td><strong>{{ row.productName || '—' }}</strong></td>
                <td>{{ row.batchNo || '—' }}</td>
                <td>{{ row.enterpriseName || '—' }}</td>
                <td>{{ row.codeTypeName || '单品码' }}</td>
                <td><span class="status" :class="statusClass(row.traceCodeStatus)">{{ statusText(row) }}</span></td>
                <td><span class="chain" :class="{ yes: row.isOnChain === 1 }">{{ row.isOnChain === 1 ? '✓ 已上链' : '未上链' }}</span></td>
                <td>{{ row.generateTime || '—' }}</td>
                <td class="actions">
                  <button @click="toggleExpand(row)">
                    展开
                  </button>
                  <button @click="openDetail(row)"><el-icon><View /></el-icon> 详情</button>
                  <button @click="verify(row)"><el-icon><Connection /></el-icon> 验真</button>
                  <button v-if="row.traceCodeStatus === 1" @click="openStatus(row, 'activate')"><el-icon><Check /></el-icon> 激活</button>
                  <button v-if="row.traceCodeStatus === 2" class="danger" @click="openStatus(row, 'disable')"><el-icon><CircleClose /></el-icon> 禁用</button>
                  <button v-if="row.traceCodeStatus !== 4" class="danger" @click="openStatus(row, 'void')"><el-icon><Close /></el-icon> 作废</button>
                </td>
              </tr>

              <!-- Expanded detail panel -->
              <tr v-if="expandedCode === row.traceCode" class="expand-detail-row">
                <td colspan="9">
                  <div v-if="expandedData[row.traceCode]?.loading" class="expand-loading">加载详情中…</div>
                  <div v-else-if="expandedData[row.traceCode]?.data" class="expand-detail-panel">
                    <div class="expand-left">
                      <img
                        v-if="qrCache[`${row.traceCode}_200`]"
                        :src="qrCache[`${row.traceCode}_200`].dataUrl"
                        class="qr-large qr-clickable"
                        width="200"
                        height="200"
                        alt="二维码 — 点击下载"
                        title="点击下载二维码图片"
                        @click="downloadQr(row.traceCode, 200)"
                      />
                      <div v-else class="qr-large-placeholder">生成中…</div>
                      <code class="expand-code">{{ row.traceCode }}</code>
                      <button
                        v-if="qrCache[`${row.traceCode}_200`]"
                        class="btn-download-qr"
                        @click="downloadQr(row.traceCode, 200)"
                      >
                        <el-icon><Download /></el-icon> 保存二维码
                      </button>
                    </div>
                    <div class="expand-right">
                      <div class="detail-grid-3">
                        <div><span>产品名称</span><b>{{ expandedData[row.traceCode]!.data!.productName || '—' }}</b></div>
                        <div><span>生产批次</span><b>{{ expandedData[row.traceCode]!.data!.batchNo || '—' }}</b></div>
                        <div><span>企业名称</span><b>{{ expandedData[row.traceCode]!.data!.enterpriseName || '—' }}</b></div>
                        <div><span>码类型</span><b>{{ expandedData[row.traceCode]!.data!.codeTypeName || '—' }}</b></div>
                        <div><span>包装层级</span><b>{{ expandedData[row.traceCode]!.data!.packageLevelName || '—' }}</b></div>
                        <div><span>状态</span><b>{{ expandedData[row.traceCode]!.data!.traceCodeStatusName || '—' }}</b></div>
                        <div>
                          <span>质检结果</span>
                          <b :class="qualityClass(expandedData[row.traceCode]!.data!.qualityResult)">{{ qualityText(expandedData[row.traceCode]!.data!.qualityResult) }}</b>
                        </div>
                        <div><span>生成时间</span><b>{{ expandedData[row.traceCode]!.data!.generateTime || '—' }}</b></div>
                        <div><span>启用时间</span><b>{{ expandedData[row.traceCode]!.data!.enableTime || '—' }}</b></div>
                        <div><span>有效期</span><b>{{ expandedData[row.traceCode]!.data!.expireTime || '—' }}</b></div>
                        <div><span>操作人</span><b>{{ expandedData[row.traceCode]!.data!.operator || '—' }}</b></div>
                        <div class="full"><span>Content Hash (SHA-256)</span><code>{{ expandedData[row.traceCode]!.data!.contentHash || '—' }}</code></div>
                        <div v-if="expandedData[row.traceCode]!.data!.txHash" class="full"><span>区块链 Tx Hash</span><code>{{ expandedData[row.traceCode]!.data!.txHash }}</code></div>
                        <div v-if="expandedData[row.traceCode]!.data!.proofId" class="full"><span>存证编号</span><code>{{ expandedData[row.traceCode]!.data!.proofId }}</code></div>
                        <div v-if="expandedData[row.traceCode]!.data!.reason" class="full warning-detail"><span>状态原因</span><b>{{ expandedData[row.traceCode]!.data!.reason }}</b></div>
                      </div>

                      <!-- Bindings -->
                      <div v-if="(expandedData[row.traceCode]!.data!.bindList?.length || 0) > 0" class="bind-section">
                        <h4>📎 绑定业务数据</h4>
                        <table class="bind-table">
                          <thead><tr><th>业务类型</th><th>业务编号</th><th>绑定时间</th></tr></thead>
                          <tbody>
                            <tr v-for="(b, bi) in expandedData[row.traceCode]!.data!.bindList" :key="bi">
                              <td><span class="biz-tag">{{ b.bizTypeName || b.bizType }}</span></td>
                              <td><code>{{ b.bizNo }}</code></td>
                              <td>{{ b.bindTime || '—' }}</td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                      <div v-else class="bind-section">
                        <h4>📎 绑定业务数据</h4>
                        <p class="empty-hint">暂无绑定的业务数据</p>
                      </div>
                    </div>
                  </div>
                  <div v-else class="expand-loading">加载失败，请重试</div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
      <Pagination v-model="currentPage" :total="rows.length" :page-size="pageSize" />
    </section>

    <!-- ============ Generate Modal ============ -->
    <div v-if="showGenerate" class="trace-modal-backdrop" @click.self="showGenerate = false">
      <section class="trace-modal">
        <header>
          <div><p>批量生成</p><h2>创建一组可追溯编码</h2></div>
          <button @click="showGenerate = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body grid-form">
          <label>产品名称 *<input v-model="generateForm.productName" placeholder="产品名称" /></label>
          <label>企业名称<input v-model="generateForm.enterpriseName" placeholder="企业全称" disabled /></label>
          <label>企业 ID<input v-model="generateForm.enterpriseId" placeholder="企业 UUID" disabled /></label>
          <label>生产批次 *
            <select v-model="generateForm.batchNo" :disabled="batchLoading" @change="(e: Event) => onBatchSelect((e.target as HTMLSelectElement).value)">
              <option value="">{{ batchLoading ? '加载中…' : '请选择生产批次' }}</option>
              <option v-for="b in batchOptions" :key="b.batchNo" :value="b.batchNo">
                {{ b.batchNo }}{{ b.productName ? ' - ' + b.productName : '' }}{{ b.checkResult && b.checkResult > 0 ? (b.checkResult === 1 ? ' [合格]' : ' [不合格]') : '' }}
              </option>
            </select>
          </label>
          <label>生成数量 *<input v-model.number="generateForm.generateCount" min="1" max="10000" type="number" /></label>
          <label>码类型<select v-model.number="generateForm.codeType"><option :value="1">单品码</option><option :value="2">批次码</option><option :value="3">箱码</option><option :value="4">托盘码</option></select></label>
          <label>包装层级<select v-model.number="generateForm.packageLevel"><option :value="1">最小销售单元</option><option :value="2">外箱</option><option :value="3">托盘</option></select></label>
          <label>有效期<input v-model="generateForm.expireTime" placeholder="2027-06-24 23:59:59" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showGenerate = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" @click="submitGenerate"><el-icon><Check /></el-icon> 确认生成</button>
        </footer>
      </section>
    </div>

    <!-- ============ Detail Modal ============ -->
    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false">
      <section class="trace-modal detail-modal">
        <header>
          <div><p>编码详情</p><h2>{{ selected?.traceCode }}</h2></div>
          <button @click="showDetail = false"><el-icon><Close /></el-icon></button>
        </header>
        <div v-if="detail" class="modal-body">
          <div class="detail-modal-layout">
            <div class="detail-qr-box">
              <img
                v-if="selected?.traceCode && qrCache[`${selected.traceCode}_200`]"
                :src="qrCache[`${selected.traceCode}_200`].dataUrl"
                class="qr-large qr-clickable"
                width="200"
                height="200"
                alt="二维码 — 点击下载"
                title="点击下载二维码图片"
                @click="downloadQr(selected.traceCode, 200)"
              />
              <div v-else class="qr-large-placeholder">生成中…</div>
              <code>{{ selected?.traceCode }}</code>
              <button
                v-if="selected?.traceCode && qrCache[`${selected.traceCode}_200`]"
                class="btn-download-qr"
                @click="downloadQr(selected.traceCode, 200)"
              >
                <el-icon><Download /></el-icon> 保存二维码
              </button>
            </div>
            <div class="detail-modal-fields">
              <div class="detail-grid-2">
                <div><span>产品名称</span><b>{{ detail.productName || '—' }}</b></div>
                <div><span>生产批次</span><b>{{ detail.batchNo || '—' }}</b></div>
                <div><span>企业</span><b>{{ detail.enterpriseName || '—' }}</b></div>
                <div><span>状态</span><b>{{ detail.traceCodeStatusName || '—' }}</b></div>
                <div><span>码类型</span><b>{{ detail.codeTypeName || '—' }}</b></div>
                <div><span>包装层级</span><b>{{ detail.packageLevelName || '—' }}</b></div>
                <div>
                  <span>质检结果</span>
                  <b :class="qualityClass(detail.qualityResult)">{{ qualityText(detail.qualityResult) }}</b>
                </div>
                <div><span>生成时间</span><b>{{ detail.generateTime || '—' }}</b></div>
                <div><span>启用时间</span><b>{{ detail.enableTime || '—' }}</b></div>
                <div><span>有效期</span><b>{{ detail.expireTime || '—' }}</b></div>
                <div><span>操作人</span><b>{{ detail.operator || '—' }}</b></div>
              </div>
              <div class="full-hash"><span>Content Hash (SHA-256)</span><code>{{ detail.contentHash || '—' }}</code></div>
              <div v-if="detail.txHash" class="full-hash"><span>区块链 Tx Hash</span><code>{{ detail.txHash }}</code></div>
              <div v-if="detail.proofId" class="full-hash"><span>存证编号</span><code>{{ detail.proofId }}</code></div>
              <div v-if="detail.reason" class="full-hash warning-detail"><span>状态原因</span><b>{{ detail.reason }}</b></div>
            </div>
          </div>
          <!-- Binds -->
          <div v-if="detailBinds.length > 0" class="bind-section">
            <h4>📎 绑定业务数据（{{ detailBinds.length }} 条）</h4>
            <table class="bind-table">
              <thead><tr><th>业务类型</th><th>业务编号</th><th>绑定时间</th></tr></thead>
              <tbody>
                <tr v-for="(b, bi) in detailBinds" :key="bi">
                  <td><span class="biz-tag">{{ b.bizTypeName || b.bizType }}</span></td>
                  <td><code>{{ b.bizNo }}</code></td>
                  <td>{{ b.bindTime || '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="bind-section">
            <h4>📎 绑定业务数据</h4>
            <p class="empty-hint">暂无绑定的业务数据</p>
          </div>
        </div>
        <div v-else class="modal-body empty">正在读取详情…</div>
      </section>
    </div>

    <!-- ============ Status Modal ============ -->
    <div v-if="showStatus" class="trace-modal-backdrop" @click.self="showStatus = false">
      <section class="trace-modal status-modal">
        <header>
          <div><p>状态变更</p><h2>{{ actionLabel }}</h2></div>
          <button @click="showStatus = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body">
          <p class="target-code">{{ selected?.traceCode }}</p>
          <label v-if="statusAction !== 'activate'">操作原因 *<textarea v-model="statusForm.reason" placeholder="请说明原因，将写入操作记录" /></label>
          <label>操作人<input v-model="statusForm.operator" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showStatus = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" :class="{ 'danger-fill': statusAction !== 'activate' }" @click="submitStatus"><el-icon><Check /></el-icon> 确认{{ actionLabel }}</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

/* ===== QR Code ===== */
.qr-large { border-radius: 8px; border: 2px solid #e0e8f0; }
.qr-clickable { cursor: pointer; transition: box-shadow 0.15s; }
.qr-clickable:hover { box-shadow: 0 4px 16px #2467df40; border-color: #2467df; }
.qr-large-placeholder { display: grid; place-items: center; width: 200px; height: 200px; border-radius: 8px;
  background: #f6f9fc; color: #a0b4c8; border: 2px dashed #dce8f4; font-size: 14px; }
.btn-download-qr { margin-top: 4px; padding: 6px 14px; border: 1px solid #d7e4f0; border-radius: 6px;
  background: #fff; color: #2467df; font-size: 12px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; }
.btn-download-qr:hover { background: #eaf2ff; border-color: #2467df; }

/* ===== Table ===== */
.row-expanded { background: #f8fbff; }
.row-expanded td { border-bottom: 0; }

/* ===== Expanded detail panel ===== */
.expand-detail-row td { padding: 0; background: #f8fbff; border-top: 1px dashed #dce8f4; }
.expand-loading { padding: 24px; text-align: center; color: #92a6b9; }
.expand-detail-panel { display: flex; gap: 24px; padding: 20px 24px; }
.expand-left { flex-shrink: 0; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.expand-code { font-size: 12px; color: #2366d9; background: #f1f6ff; padding: 4px 10px; border-radius: 5px;
  font-family: Consolas, monospace; }
.expand-right { flex: 1; min-width: 0; overflow: hidden; }

/* 3-column detail grid for expand panel */
.detail-grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px 18px; }
.detail-grid-3 div { display: grid; gap: 4px; }
.detail-grid-3 span { color: #91a3b4; font-size: 12px; }
.detail-grid-3 b { color: #34536f; font-size: 13px; }
.detail-grid-3 .full { grid-column: 1 / -1; }
.detail-grid-3 code { display: block; overflow: auto; padding: 9px; border-radius: 6px; background: #f0f5fa;
  white-space: nowrap; font-size: 11px; word-break: break-all; max-width: 100%; }

/* 2-column detail grid for modal */
.detail-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px 18px; }
.detail-grid-2 div { display: grid; gap: 4px; }
.detail-grid-2 span { color: #91a3b4; font-size: 12px; }
.detail-grid-2 b { color: #34536f; font-size: 13px; }
.full-hash { margin-top: 8px; }
.full-hash span { display: block; color: #91a3b4; font-size: 12px; margin-bottom: 4px; }
.full-hash code { display: block; overflow: auto; padding: 9px; border-radius: 6px; background: #f0f5fa;
  white-space: nowrap; font-size: 11px; word-break: break-all; max-width: 100%; }

/* ===== Binding section ===== */
.bind-section { margin-top: 16px; padding-top: 14px; border-top: 1px solid #e8eff6; }
.bind-section h4 { margin: 0 0 10px; color: #34536f; font-size: 14px; }
.bind-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.bind-table th { padding: 7px 10px; background: #f0f5fa; color: #7087a1; text-align: left; font-size: 11px; }
.bind-table td { padding: 7px 10px; border-top: 1px solid #edf2f7; color: #46617d; }
.biz-tag { display: inline-block; padding: 2px 8px; border-radius: 10px; background: #eaf2ff; color: #2666df;
  font-size: 11px; font-weight: 700; }
.empty-hint { color: #92a6b9; font-size: 12px; }

/* ===== Detail modal layout ===== */
.detail-modal-layout { display: flex; gap: 20px; }
.detail-qr-box { flex-shrink: 0; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.detail-qr-box code { font-size: 12px; color: #2366d9; background: #f1f6ff; padding: 4px 10px; border-radius: 5px;
  font-family: Consolas, monospace; }
.detail-modal-fields { flex: 1; min-width: 0; overflow: hidden; }

/* ===== Quality badges ===== */
.qa-pass { color: #198658 !important; }
.qa-fail { color: #d34f59 !important; }
.qa-pending { color: #a4730a !important; }

/* ===== Misc ===== */
.warning-detail { padding: 10px; border-radius: 7px; background: #fff6ed; }
.target-code { margin: 0 0 18px; padding: 11px; border-radius: 7px; color: #2366d9; background: #f1f6ff;
  font-family: Consolas, monospace; }
</style>
