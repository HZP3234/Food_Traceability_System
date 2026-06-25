<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Check, CircleClose, Close, Clock, Connection, Document, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { traceCodeApi } from '../services/api'
import Pagination from '../components/Pagination.vue'

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
    currentPage.value = 1
  } catch (error: any) {
    rows.value = []
    currentPage.value = 1
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

    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>编码台账</p><h2>溯源码列表</h2></div>
        <button class="primary create" @click="showGenerate = true"><el-icon><Plus /></el-icon> 批量生成溯源码</button>
      </header>
      <div class="table-wrap">
        <table>
          <thead><tr><th>溯源码</th><th>产品 / 批次</th><th>企业</th><th>码类型</th><th>状态</th><th>链上存证</th><th>生成时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="8" class="empty">正在加载数据…</td></tr>
            <tr v-else-if="!rows.length"><td colspan="8" class="empty">暂无数据。请启动企业端后查询，或点击右上角生成溯源码。</td></tr>
            <tr v-for="row in paginatedRows" :key="row.traceCode">
              <td><code>{{ row.traceCode }}</code></td>
              <td><strong>{{ row.productName || '—' }}</strong><small>{{ row.batchNo || '未关联批次' }}</small></td>
              <td>{{ row.enterpriseName || '—' }}</td>
              <td>{{ row.codeTypeName || '单品码' }}<small>{{ row.packageLevelName || '' }}</small></td>
              <td><span class="status" :class="statusClass(row.traceCodeStatus)">{{ statusText(row) }}</span></td>
              <td><span class="chain" :class="{ yes: row.isOnChain === 1 }">{{ row.isOnChain === 1 ? '已上链' : '未上链' }}</span></td>
              <td>{{ row.generateTime || '—' }}</td>
              <td class="actions">
                <button @click="openDetail(row)"><el-icon><View /></el-icon> 详情</button>
                <button @click="verify(row)"><el-icon><Connection /></el-icon> 验真</button>
                <button v-if="row.traceCodeStatus === 1" @click="openStatus(row, 'activate')"><el-icon><Check /></el-icon> 激活</button>
                <button v-if="row.traceCodeStatus === 2" class="danger" @click="openStatus(row, 'disable')"><el-icon><CircleClose /></el-icon> 禁用</button>
                <button v-if="row.traceCodeStatus !== 4" class="danger" @click="openStatus(row, 'void')"><el-icon><Close /></el-icon> 作废</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <Pagination v-model="currentPage" :total="rows.length" :page-size="pageSize" />
    </section>

    <!-- 批量生成模态框 -->
    <div v-if="showGenerate" class="trace-modal-backdrop" @click.self="showGenerate = false">
      <section class="trace-modal">
        <header>
          <div><p>批量生成</p><h2>创建一组可追溯编码</h2></div>
          <button @click="showGenerate = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body grid-form">
          <label>产品 ID *<input v-model="generateForm.productId" placeholder="PROD2026001" /></label>
          <label>产品名称 *<input v-model="generateForm.productName" placeholder="产品名称" /></label>
          <label>企业 ID *<input v-model="generateForm.enterpriseId" placeholder="企业 UUID" /></label>
          <label>企业名称<input v-model="generateForm.enterpriseName" placeholder="企业全称" /></label>
          <label>生产批次 *<input v-model="generateForm.batchNo" placeholder="PB20260601" /></label>
          <label>生成数量 *<input v-model.number="generateForm.generateCount" min="1" max="10000" type="number" /></label>
          <label>码类型<select v-model.number="generateForm.codeType"><option :value="1">单品码</option><option :value="2">批次码</option><option :value="3">箱码</option><option :value="4">托盘码</option></select></label>
          <label>包装层级<select v-model.number="generateForm.packageLevel"><option :value="1">最小销售单元</option><option :value="2">外箱</option><option :value="3">托盘</option></select></label>
          <label>质检结果<select v-model.number="generateForm.qualityResult"><option :value="1">合格</option><option :value="3">待检</option><option :value="2">不合格</option></select></label>
          <label>有效期<input v-model="generateForm.expireTime" placeholder="2027-06-24 23:59:59" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showGenerate = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" @click="submitGenerate"><el-icon><Check /></el-icon> 确认生成</button>
        </footer>
      </section>
    </div>

    <!-- 详情模态框 -->
    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false">
      <section class="trace-modal detail-modal">
        <header>
          <div><p>编码详情</p><h2>{{ selected?.traceCode }}</h2></div>
          <button @click="showDetail = false"><el-icon><Close /></el-icon></button>
        </header>
        <div v-if="detail" class="modal-body detail-grid">
          <div><span>产品名称</span><b>{{ detail.productName || '—' }}</b></div>
          <div><span>生产批次</span><b>{{ detail.batchNo || '—' }}</b></div>
          <div><span>企业</span><b>{{ detail.enterpriseName || '—' }}</b></div>
          <div><span>状态</span><b>{{ detail.traceCodeStatusName || '—' }}</b></div>
          <div><span>生成时间</span><b>{{ detail.generateTime || '—' }}</b></div>
          <div><span>有效期</span><b>{{ detail.expireTime || '—' }}</b></div>
          <div class="full"><span>内容 Hash</span><code>{{ detail.contentHash || '—' }}</code></div>
          <div v-if="detail.reason" class="full warning-detail"><span>状态原因</span><b>{{ detail.reason }}</b></div>
        </div>
        <div v-else class="modal-body empty">正在读取详情…</div>
      </section>
    </div>

    <!-- 状态变更模态框 -->
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
</style>
