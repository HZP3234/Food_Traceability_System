<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { salesApi } from '../services/api'

const tab = ref<'terminal' | 'stock' | 'supplement'>('terminal')
const toast = ref<{ type: string; msg: string } | null>(null)

// ===== Terminal =====
const loadingT = ref(false)
const terminals = ref<any[]>([])
const tFilters = ref({ terminalType: '', area: '', terminalStatus: '' })
const showTModal = ref(false); const editingT = ref<any>(null); const showTConfirm = ref(false); const deletingTId = ref<number | null>(null)
const tForm = ref({ terminalCode: '', terminalName: '', terminalType: 0, area: '', address: '', operatorId: '', operatorName: '', terminalStatus: 0, remark: '' })
const terminalTypeLabels = ['', '超市', '便利店', '餐饮', '电商', '农贸', '其他']
const terminalStatusLabels = ['', '启用', '停用', '装修中']

// ===== Stock =====
const loadingS = ref(false)
const stocks = ref<any[]>([])
const sFilters = ref({ terminalId: '', prodBatchNo: '' })
const showSModal = ref(false); const editingS = ref<any>(null)
const sForm = ref({ stockCode: '', terminalId: '', terminalName: '', prodBatchId: '', prodBatchNo: '', productName: '', quantity: '', receivedTime: '', lastCheckTime: '', stockStatus: 0, remark: '' })
const stockStatusLabels = ['', '在库', '在售', '下架', '售罄']

// ===== Supplement =====
const loadingSup = ref(false)
const supplements = ref<any[]>([])
const supFilters = ref({ traceBatchNo: '', terminalCode: '' })
const showSupModal = ref(false); const editingSup = ref<any>(null)
const supForm = ref({ supplementCode: '', traceBatchId: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' })
const supplementStatusLabels = ['', '已提交', '已审核']

function flash(type: string, msg: string) {
  toast.value = { type, msg }
  setTimeout(() => (toast.value = null), 2600)
}

// ---- Terminal CRUD ----
async function loadTerminals() {
  loadingT.value = true
  try {
    const p: Record<string, any> = {}
    if (tFilters.value.terminalType) p.terminalType = Number(tFilters.value.terminalType)
    if (tFilters.value.area) p.area = tFilters.value.area
    if (tFilters.value.terminalStatus) p.terminalStatus = Number(tFilters.value.terminalStatus)
    const data = await salesApi.listTerminal(p)
    terminals.value = Array.isArray(data) ? data : []
  } catch (e: any) { flash('error', '加载销售终端失败: ' + e.message) }
  finally { loadingT.value = false }
}
function openCreateT() {
  editingT.value = null
  tForm.value = { terminalCode: '', terminalName: '', terminalType: 0, area: '', address: '', operatorId: '', operatorName: '', terminalStatus: 0, remark: '' }
  showTModal.value = true
}
function openEditT(row: any) {
  editingT.value = row
  tForm.value = { terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', terminalType: row.terminalType ?? 0, area: row.area ?? '', address: row.address ?? '', operatorId: row.operatorId ?? '', operatorName: row.operatorName ?? '', terminalStatus: row.terminalStatus ?? 0, remark: row.remark ?? '' }
  showTModal.value = true
}
async function submitT() {
  try {
    const data: Record<string, any> = { ...tForm.value }
    if (editingT.value) { data.terminalId = editingT.value.terminalId; await salesApi.updateTerminal(data); flash('success', '终端信息更新成功') }
    else { await salesApi.createTerminal(data); flash('success', '终端注册成功') }
    showTModal.value = false; loadTerminals()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}
function confirmDeleteT(id: number) { deletingTId.value = id; showTConfirm.value = true }
async function doDeleteT() {
  try { await salesApi.deleteTerminal(deletingTId.value!); flash('success', '终端删除成功'); showTConfirm.value = false; loadTerminals() }
  catch (e: any) { flash('error', '删除失败: ' + e.message) }
}

// ---- Stock CRUD ----
async function loadStocks() {
  loadingS.value = true
  try {
    if (sFilters.value.terminalId) { const d = await salesApi.listStock(sFilters.value.terminalId); stocks.value = Array.isArray(d) ? d : [] }
    else if (sFilters.value.prodBatchNo) { const d = await salesApi.listStockByBatch(sFilters.value.prodBatchNo); stocks.value = Array.isArray(d) ? d : [] }
    else stocks.value = []
  } catch (e: any) { flash('error', '加载库存失败: ' + e.message) }
  finally { loadingS.value = false }
}
function openCreateS() {
  editingS.value = null
  sForm.value = { stockCode: '', terminalId: '', terminalName: '', prodBatchId: '', prodBatchNo: '', productName: '', quantity: '', receivedTime: '', lastCheckTime: '', stockStatus: 0, remark: '' }
  showSModal.value = true
}
function openEditS(row: any) {
  editingS.value = row
  sForm.value = { stockCode: row.stockCode ?? '', terminalId: row.terminalId ?? '', terminalName: row.terminalName ?? '', prodBatchId: row.prodBatchId ?? '', prodBatchNo: row.prodBatchNo ?? '', productName: row.productName ?? '', quantity: row.quantity ?? '', receivedTime: row.receivedTime ?? '', lastCheckTime: row.lastCheckTime ?? '', stockStatus: row.stockStatus ?? 0, remark: row.remark ?? '' }
  showSModal.value = true
}
async function submitS() {
  try {
    const data: Record<string, any> = { ...sForm.value }
    if (editingS.value) { data.stockId = editingS.value.stockId; await salesApi.updateStock(data); flash('success', '库存盘点更新成功') }
    else { await salesApi.stockIn(data); flash('success', '产品入库成功') }
    showSModal.value = false; loadStocks()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}

// ---- Supplement CRUD ----
async function loadSupplements() {
  loadingSup.value = true
  try {
    if (supFilters.value.traceBatchNo) { const d = await salesApi.listSupplement(supFilters.value.traceBatchNo); supplements.value = Array.isArray(d) ? d : [] }
    else if (supFilters.value.terminalCode) { const d = await salesApi.listSupplementByTerminal(supFilters.value.terminalCode); supplements.value = Array.isArray(d) ? d : [] }
    else supplements.value = []
  } catch (e: any) { flash('error', '加载补充信息失败: ' + e.message) }
  finally { loadingSup.value = false }
}
function openCreateSup() {
  editingSup.value = null
  supForm.value = { supplementCode: '', traceBatchId: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' }
  showSupModal.value = true
}
function openEditSup(row: any) {
  editingSup.value = row
  supForm.value = { supplementCode: row.supplementCode ?? '', traceBatchId: row.traceBatchId ?? '', traceBatchNo: row.traceBatchNo ?? '', terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', salesCompany: row.salesCompany ?? '', salesBatchNo: row.salesBatchNo ?? '', temperature: row.temperature ?? '', humidity: row.humidity ?? '', storageMethod: row.storageMethod ?? 0, lightCondition: row.lightCondition ?? 0, shelfLife: row.shelfLife ?? '', locationCode: row.locationCode ?? '', quantity: row.quantity ?? '', inboundTime: row.inboundTime ?? '', operator: row.operator ?? '', supplementStatus: row.supplementStatus ?? 1, remark: row.remark ?? '' }
  showSupModal.value = true
}
async function submitSup() {
  try {
    const data: Record<string, any> = { ...supForm.value }
    if (editingSup.value) { data.supplementId = editingSup.value.supplementId; await salesApi.updateSupplement(data); flash('success', '补充信息更新成功') }
    else { await salesApi.supplementInfo(data); flash('success', '销售详情补充成功') }
    showSupModal.value = false; loadSupplements()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}

// ---- Anti Fraud ----
async function runAntiFraud() {
  try { await salesApi.runAntiFraud(); flash('success', '防窜货核验执行完毕') }
  catch (e: any) { flash('error', '核验失败: ' + e.message) }
}

function switchTab(t: 'terminal' | 'stock' | 'supplement') {
  tab.value = t
  if (t === 'terminal') loadTerminals()
  else if (t === 'stock') loadStocks()
  else loadSupplements()
}

onMounted(loadTerminals)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'terminal' }" @click="switchTab('terminal')">🏪 销售终端</button>
      <button class="tab-btn" :class="{ active: tab === 'stock' }" @click="switchTab('stock')">📦 库存管理</button>
      <button class="tab-btn" :class="{ active: tab === 'supplement' }" @click="switchTab('supplement')">📝 销售补充</button>
    </div>

    <!-- ============================ 销售终端 ============================ -->
    <template v-if="tab === 'terminal'">
      <div class="search-bar">
        <div class="search-field">
          <label>终端类型</label>
          <select v-model="tFilters.terminalType"><option value="">全部</option><option value="1">超市</option><option value="2">便利店</option><option value="3">餐饮</option><option value="4">电商</option><option value="5">农贸</option><option value="6">其他</option></select>
        </div>
        <div class="search-field"><label>所属区域</label><input v-model="tFilters.area" placeholder="区域" @keyup.enter="loadTerminals" /></div>
        <div class="search-field">
          <label>终端状态</label>
          <select v-model="tFilters.terminalStatus"><option value="">全部</option><option value="1">启用</option><option value="2">停用</option><option value="3">装修中</option></select>
        </div>
        <div class="search-field" style="align-self:end">
          <button class="btn btn-primary" @click="loadTerminals">🔍 查询</button>
          <button class="btn btn-outline btn-sm" style="margin-left:8px" @click="runAntiFraud">🔎 防窜货核验</button>
        </div>
      </div>

      <div class="toolbar">
        <h3>销售终端列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ terminals.length }} 条)</span></h3>
        <button class="btn btn-primary" @click="openCreateT">＋ 注册终端</button>
      </div>

      <div class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>终端编码</th><th>终端名称</th><th>类型</th><th>区域</th>
              <th>地址</th><th>运营商</th><th>状态</th><th>创建时间</th><th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loadingT"><td colspan="9" style="text-align:center;padding:32px">加载中...</td></tr>
            <tr v-else-if="!terminals.length"><td colspan="9"><div class="empty-state"><div class="empty-icon">🏪</div><p>暂无销售终端数据</p></div></td></tr>
            <tr v-for="row in terminals" :key="row.terminalId">
              <td><code style="color:#2666df;font-size:13px">{{ row.terminalCode }}</code></td>
              <td><strong>{{ row.terminalName }}</strong></td>
              <td>{{ terminalTypeLabels[row.terminalType] || '-' }}</td>
              <td>{{ row.area }}</td>
              <td>{{ row.address }}</td>
              <td>{{ row.operatorName }}</td>
              <td><span class="tag" :class="row.terminalStatus === 1 ? 'tag-success' : row.terminalStatus === 3 ? 'tag-warn' : 'tag-neutral'">{{ terminalStatusLabels[row.terminalStatus] || '-' }}</span></td>
              <td>{{ row.createTime }}</td>
              <td class="col-actions">
                <button class="btn btn-outline btn-sm" @click="openEditT(row)">✎ 编辑</button>
                <button class="btn btn-danger btn-sm" style="margin-left:4px" @click="confirmDeleteT(row.terminalId)">🗑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ============================ 库存管理 ============================ -->
    <template v-if="tab === 'stock'">
      <div class="search-bar">
        <div class="search-field"><label>终端ID</label><input v-model="sFilters.terminalId" placeholder="终端ID" @keyup.enter="loadStocks" /></div>
        <div class="search-field"><label>生产批次</label><input v-model="sFilters.prodBatchNo" placeholder="生产批次号" @keyup.enter="loadStocks" /></div>
        <div class="search-field" style="align-self:end">
          <button class="btn btn-primary" @click="loadStocks">🔍 查询</button>
        </div>
      </div>

      <div class="toolbar">
        <h3>库存列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ stocks.length }} 条)</span></h3>
        <button class="btn btn-primary" @click="openCreateS">＋ 产品入库</button>
      </div>

      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>库存编码</th><th>产品名称</th><th>生产批次</th><th>终端</th><th>数量</th><th>入库时间</th><th>盘点时间</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
          <tbody>
            <tr v-if="loadingS"><td colspan="9" style="text-align:center;padding:32px">加载中...</td></tr>
            <tr v-else-if="!stocks.length"><td colspan="9"><div class="empty-state"><div class="empty-icon">📦</div><p>暂无库存数据，请先搜索</p></div></td></tr>
            <tr v-for="row in stocks" :key="row.stockId">
              <td><code style="font-size:12px">{{ row.stockCode }}</code></td>
              <td>{{ row.productName }}</td>
              <td><code style="font-size:12px">{{ row.prodBatchNo }}</code></td>
              <td>{{ row.terminalName }}</td>
              <td>{{ row.quantity }}</td>
              <td>{{ row.receivedTime }}</td>
              <td>{{ row.lastCheckTime || '-' }}</td>
              <td><span class="tag" :class="row.stockStatus === 2 ? 'tag-success' : row.stockStatus === 3 ? 'tag-warn' : row.stockStatus === 4 ? 'tag-danger' : 'tag-info'">{{ stockStatusLabels[row.stockStatus] || '-' }}</span></td>
              <td class="col-actions">
                <button class="btn btn-outline btn-sm" @click="openEditS(row)">✎ 盘点</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ============================ 销售补充 ============================ -->
    <template v-if="tab === 'supplement'">
      <div class="search-bar">
        <div class="search-field"><label>溯源码批次</label><input v-model="supFilters.traceBatchNo" placeholder="溯源码批次号" @keyup.enter="loadSupplements" /></div>
        <div class="search-field"><label>终端编码</label><input v-model="supFilters.terminalCode" placeholder="终端编码" @keyup.enter="loadSupplements" /></div>
        <div class="search-field" style="align-self:end">
          <button class="btn btn-primary" @click="loadSupplements">🔍 查询</button>
        </div>
      </div>

      <div class="toolbar">
        <h3>销售补充信息 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ supplements.length }} 条)</span></h3>
        <button class="btn btn-primary" @click="openCreateSup">＋ 补充销售详情</button>
      </div>

      <div class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>补充编码</th><th>溯源码批次</th><th>终端</th><th>销售企业</th>
              <th>销售批次</th><th>数量</th><th>温度</th><th>湿度</th>
              <th>入库时间</th><th>状态</th><th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loadingSup"><td colspan="11" style="text-align:center;padding:32px">加载中...</td></tr>
            <tr v-else-if="!supplements.length"><td colspan="11"><div class="empty-state"><div class="empty-icon">📝</div><p>暂无补充信息，请先搜索</p></div></td></tr>
            <tr v-for="row in supplements" :key="row.supplementId">
              <td><code style="font-size:12px">{{ row.supplementCode }}</code></td>
              <td><code style="font-size:12px">{{ row.traceBatchNo }}</code></td>
              <td>{{ row.terminalName }}</td>
              <td>{{ row.salesCompany }}</td>
              <td>{{ row.salesBatchNo }}</td>
              <td>{{ row.quantity }}</td>
              <td>{{ row.temperature || '-' }}</td>
              <td>{{ row.humidity || '-' }}</td>
              <td>{{ row.inboundTime }}</td>
              <td><span class="tag" :class="row.supplementStatus === 2 ? 'tag-success' : 'tag-info'">{{ supplementStatusLabels[row.supplementStatus] || '-' }}</span></td>
              <td class="col-actions">
                <button class="btn btn-outline btn-sm" @click="openEditSup(row)">✎ 编辑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ==================== 模态框 ==================== -->

    <!-- 终端模态框 -->
    <div v-if="showTModal" class="modal-overlay" @click.self="showTModal = false">
      <div class="modal">
        <div class="modal-header"><h3>{{ editingT ? '编辑销售终端' : '注册销售终端' }}</h3><button class="modal-close" @click="showTModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group"><label>终端编码 *</label><input v-model="tForm.terminalCode" placeholder="终端编码" /></div>
            <div class="form-group"><label>终端名称 *</label><input v-model="tForm.terminalName" placeholder="终端名称" /></div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>终端类型</label>
              <select v-model.number="tForm.terminalType"><option :value="0">请选择</option><option :value="1">超市</option><option :value="2">便利店</option><option :value="3">餐饮</option><option :value="4">电商</option><option :value="5">农贸</option><option :value="6">其他</option></select>
            </div>
            <div class="form-group"><label>所属区域</label><input v-model="tForm.area" placeholder="如 广东省深圳市南山区" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>地址</label><input v-model="tForm.address" placeholder="详细地址" /></div>
            <div class="form-group"><label>运营商名称</label><input v-model="tForm.operatorName" placeholder="运营商名称" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>运营商编码</label><input v-model="tForm.operatorId" placeholder="运营商ID" /></div>
            <div class="form-group">
              <label>终端状态</label>
              <select v-model.number="tForm.terminalStatus"><option :value="0">请选择</option><option :value="1">启用</option><option :value="2">停用</option><option :value="3">装修中</option></select>
            </div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="tForm.remark" placeholder="备注" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showTModal = false">取消</button>
          <button class="btn btn-primary" @click="submitT">{{ editingT ? '保存' : '注册' }}</button>
        </div>
      </div>
    </div>

    <!-- 库存模态框 -->
    <div v-if="showSModal" class="modal-overlay" @click.self="showSModal = false">
      <div class="modal">
        <div class="modal-header"><h3>{{ editingS ? '库存盘点' : '产品入库' }}</h3><button class="modal-close" @click="showSModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group"><label>库存编码</label><input v-model="sForm.stockCode" placeholder="自动生成可留空" /></div>
            <div class="form-group"><label>产品名称 *</label><input v-model="sForm.productName" placeholder="产品名称" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>生产批次号 *</label><input v-model="sForm.prodBatchNo" placeholder="生产批次号" /></div>
            <div class="form-group"><label>生产批次ID</label><input v-model="sForm.prodBatchId" placeholder="生产批次ID" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>终端ID</label><input v-model="sForm.terminalId" placeholder="终端ID" /></div>
            <div class="form-group"><label>终端名称</label><input v-model="sForm.terminalName" placeholder="终端名称" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>数量 *</label><input v-model="sForm.quantity" placeholder="数量" type="number" /></div>
            <div class="form-group"><label>入库时间</label><input v-model="sForm.receivedTime" placeholder="2025-01-01" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>盘点时间</label><input v-model="sForm.lastCheckTime" placeholder="2025-01-01" /></div>
            <div class="form-group">
              <label>库存状态</label>
              <select v-model.number="sForm.stockStatus"><option :value="0">请选择</option><option :value="1">在库</option><option :value="2">在售</option><option :value="3">下架</option><option :value="4">售罄</option></select>
            </div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="sForm.remark" placeholder="备注" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showSModal = false">取消</button>
          <button class="btn btn-primary" @click="submitS">{{ editingS ? '保存' : '入库' }}</button>
        </div>
      </div>
    </div>

    <!-- 补充模态框 -->
    <div v-if="showSupModal" class="modal-overlay" @click.self="showSupModal = false">
      <div class="modal" style="width:700px">
        <div class="modal-header"><h3>{{ editingSup ? '编辑补充信息' : '补充销售详情' }}</h3><button class="modal-close" @click="showSupModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group"><label>补充编码</label><input v-model="supForm.supplementCode" placeholder="自动生成可留空" /></div>
            <div class="form-group"><label>溯源码批次号 *</label><input v-model="supForm.traceBatchNo" placeholder="溯源码批次号" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>终端编码</label><input v-model="supForm.terminalCode" placeholder="终端编码" /></div>
            <div class="form-group"><label>终端名称</label><input v-model="supForm.terminalName" placeholder="终端名称" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>销售企业</label><input v-model="supForm.salesCompany" placeholder="销售企业" /></div>
            <div class="form-group"><label>销售批次号</label><input v-model="supForm.salesBatchNo" placeholder="销售批次号" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>数量</label><input v-model="supForm.quantity" placeholder="数量" type="number" /></div>
            <div class="form-group"><label>入库时间</label><input v-model="supForm.inboundTime" placeholder="2025-01-01" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>储存温度</label><input v-model="supForm.temperature" placeholder="℃" /></div>
            <div class="form-group"><label>储存湿度</label><input v-model="supForm.humidity" placeholder="%RH" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>储存方式</label><input v-model="supForm.storageMethod" placeholder="0常温 1冷藏 2冷冻" /></div>
            <div class="form-group"><label>光照条件</label><input v-model="supForm.lightCondition" placeholder="0避光 1散光 2光照" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>保质期</label><input v-model="supForm.shelfLife" placeholder="如 12个月" /></div>
            <div class="form-group"><label>货位编码</label><input v-model="supForm.locationCode" placeholder="货位编码" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>操作员</label><input v-model="supForm.operator" placeholder="操作员" /></div>
            <div class="form-group">
              <label>状态</label>
              <select v-model.number="supForm.supplementStatus"><option :value="1">已提交</option><option :value="2">已审核</option></select>
            </div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="supForm.remark" placeholder="备注" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showSupModal = false">取消</button>
          <button class="btn btn-primary" @click="submitSup">{{ editingSup ? '保存' : '提交' }}</button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="showTConfirm" class="confirm-overlay" @click.self="showTConfirm = false">
      <div class="confirm-box">
        <div class="confirm-icon">⚠️</div>
        <h3>确认删除</h3>
        <p>删除后将不可恢复，确定要删除该销售终端吗？</p>
        <div class="confirm-actions">
          <button class="btn btn-outline" @click="showTConfirm = false">取消</button>
          <button class="btn btn-danger" @click="doDeleteT">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>
