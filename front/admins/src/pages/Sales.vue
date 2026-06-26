<script setup lang="ts">
import { ref, computed, inject, onMounted, type Ref } from 'vue'
import { Close, DocumentAdd, Plus, Search, Select, Shop, SoldOut, Upload } from '@element-plus/icons-vue'
import { salesApi, salesOrderApi, enterpriseApi, productionApi } from '../services/api'
import Pagination from '../components/Pagination.vue'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')!
const isSeller = computed(() => currentRole.value === 'seller')
const isManufacturer = computed(() => currentRole.value === 'manufacturer' || currentRole.value === 'super-admin')

// 当前登录用户信息
let currentUser = ''
let enterpriseName = ''
try {
  const raw = sessionStorage.getItem('fts-admin-user')
  if (raw) {
    const parsed = JSON.parse(raw)
    currentUser = parsed.realName || parsed.username || ''
    enterpriseName = parsed.enterpriseName || currentUser
  }
} catch { currentUser = sessionStorage.getItem('fts-admin-user') || '' }

const tab = ref<'upload' | 'order' | 'terminal' | 'stock' | 'supplement'>(isSeller.value ? 'upload' : 'order')
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)

// Terminal
const loadingT = ref(false); const terminals = ref<any[]>([])
const tFilters = ref({ terminalType: '', area: '', terminalStatus: '' })
const showTModal = ref(false); const editingT = ref<any>(null); const showTConfirm = ref(false); const deletingTId = ref<number | null>(null)
const tForm = ref({ terminalCode: '', terminalName: '', terminalType: 0, area: '', address: '', operatorName: '', remark: '' })
const terminalTypeLabels = ['', '超市', '便利店', '餐饮', '电商', '农贸', '其他']

// Stock
const loadingS = ref(false); const stocks = ref<any[]>([])
const sFilters = ref({ terminalId: '', prodBatchNo: '' })
const showSModal = ref(false); const editingS = ref<any>(null)
const sForm = ref({ stockCode: '', terminalId: '', terminalName: '', prodBatchNo: '', productName: '', quantity: '', receivedTime: '', stockStatus: 0, remark: '' })
const stockStatusLabels = ['', '在库', '在售', '下架', '售罄']

// Supplement
const loadingSup = ref(false); const supplements = ref<any[]>([])

// Pagination
const tPage = ref(1); const sPage = ref(1); const supPage = ref(1); const pageSize = ref(10)
const paginatedTerminals = computed(() => { const start = (tPage.value - 1) * pageSize.value; return terminals.value.slice(start, start + pageSize.value) })
const paginatedStocks = computed(() => { const start = (sPage.value - 1) * pageSize.value; return stocks.value.slice(start, start + pageSize.value) })
const paginatedSupplements = computed(() => { const start = (supPage.value - 1) * pageSize.value; return supplements.value.slice(start, start + pageSize.value) })
const supFilters = ref({ traceBatchNo: '', terminalCode: '' })
const showSupModal = ref(false); const editingSup = ref<any>(null)
const supForm = ref({ supplementCode: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' })

// Sales Order
const loadingO = ref(false); const orders = ref<any[]>([])
const oFilters = ref({ buyerName: '', productName: '', orderStatus: '' })
const showOModal = ref(false); const editingO = ref<any>(null); const showOConfirm = ref(false); const deletingOId = ref<number | null>(null)
const oForm = ref({ salesOrderCode: '', productName: '', prodBatchNo: '', buyerEnterpriseId: '', buyerEnterpriseName: '', sellerEnterpriseName: '', orderQuantity: '', unitPrice: '', totalAmount: '', orderDate: '', orderStatus: 1, remark: '' })
const oPage = ref(1)
const paginatedOrders = computed(() => { const start = (oPage.value - 1) * pageSize.value; return orders.value.slice(start, start + pageSize.value) })

// Order Detail
const showODModal = ref(false); const editingOD = ref<any>(null)
const odForm = ref({ detailCode: '', salesOrderId: '', salesOrderCode: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', inboundTime: '', actualQuantity: '', salesDate: '', detailStatus: 1, operator: '', remark: '' })
const orderDetail = ref<any>(null)

// 销售厂商搜索下拉
const buyerOptions = ref<any[]>([])
const buyerSearchText = ref('')
const buyerDropdownVisible = ref(false)

async function loadBuyerOptions() {
  try {
    const data = await enterpriseApi.search(buyerSearchText.value || '')
    // 只显示销售商（enterpriseType=4 即零售商）
    buyerOptions.value = (Array.isArray(data) ? data : []).filter((e: any) => e.enterpriseType === 4)
  } catch { buyerOptions.value = [] }
}

const filteredBuyerOptions = computed(() => {
  if (!buyerSearchText.value) return buyerOptions.value
  const q = buyerSearchText.value.toLowerCase()
  return buyerOptions.value.filter((e: any) =>
    (e.enterpriseName || '').toLowerCase().includes(q)
  )
})

// 生产批次下拉
const prodBatchOptions = ref<any[]>([])
const prodBatchSearchText = ref('')
const prodBatchDropdownVisible = ref(false)

async function loadProdBatchOptions() {
  try {
    const data = await productionApi.listProdBatch({})
    prodBatchOptions.value = Array.isArray(data) ? data : []
  } catch { prodBatchOptions.value = [] }
}

const filteredProdBatchOptions = computed(() => {
  if (!prodBatchSearchText.value) return prodBatchOptions.value
  const q = prodBatchSearchText.value.toLowerCase()
  return prodBatchOptions.value.filter((b: any) =>
    (b.batchNo || '').toLowerCase().includes(q) ||
    (b.productName || '').toLowerCase().includes(q)
  )
})

function onProdBatchSelect(b: any) {
  prodBatchDropdownVisible.value = false
  oForm.value.prodBatchNo = b.batchNo || ''
  prodBatchSearchText.value = (b.batchNo || '') + ' — ' + (b.productName || '')
}
function onProdBatchFocus() { prodBatchDropdownVisible.value = true; if (prodBatchOptions.value.length === 0) loadProdBatchOptions() }
function onProdBatchInput() { prodBatchDropdownVisible.value = true; loadProdBatchOptions() }
function onProdBatchBlur() { setTimeout(() => prodBatchDropdownVisible.value = false, 200) }

function onBuyerSelect(ent: any) {
  buyerDropdownVisible.value = false
  oForm.value.buyerEnterpriseName = ent.enterpriseName || ''
  oForm.value.buyerEnterpriseId = ent.enterpriseUuid || ''
  buyerSearchText.value = ent.enterpriseName || ''
}

function onBuyerFocus() { buyerDropdownVisible.value = true; if (buyerOptions.value.length === 0) loadBuyerOptions() }
function onBuyerInput() { buyerDropdownVisible.value = true; loadBuyerOptions() }
function onBuyerBlur() { setTimeout(() => buyerDropdownVisible.value = false, 200) }

// ====== 销售信息上传 Tab（销售商专用，仿 SupplierRawMaterials） ======
const loadingUp = ref(false)
const pendingOrders = ref<any[]>([])
const completedOrders = ref<any[]>([])

const uploadStats = computed(() => ({
  pending: pendingOrders.value.length,
  completed: completedOrders.value.length,
  total: pendingOrders.value.length + completedOrders.value.length,
}))

async function loadUploadData() {
  loadingUp.value = true
  try {
    const myName = enterpriseName || currentUser
    const all = await salesOrderApi.listOrder({ buyerName: myName })
    const data = Array.isArray(all) ? all : []
    pendingOrders.value = data.filter((r: any) => r.orderStatus === 1 && r.detailStatus === 0)
    completedOrders.value = data.filter((r: any) => r.orderStatus >= 2 || r.detailStatus >= 1)
  } catch (e: any) { notify('error', '加载失败: ' + e.message) }
  finally { loadingUp.value = false }
}

function openUploadDetail(row: any) { openDetailUpload(row) }

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(s: string) { if (['启用', '在售', '已审核', '已补充'].some(x => s.includes(x))) return 'status-active'; if (['装修中', '待', '已提交'].some(x => s.includes(x))) return 'status-pending'; if (['停用', '异常', '售罄'].some(x => s.includes(x))) return 'status-void'; return 'status-active' }

// Terminal CRUD
async function loadTerminals() { loadingT.value = true; try { const p: Record<string, any> = {}; if (tFilters.value.terminalType) p.terminalType = Number(tFilters.value.terminalType); if (tFilters.value.area) p.area = tFilters.value.area; if (tFilters.value.terminalStatus) p.terminalStatus = Number(tFilters.value.terminalStatus); const data = await salesApi.listTerminal(p); terminals.value = Array.isArray(data) ? data : []; tPage.value = 1 } catch (e: any) { notify('error', '加载失败') } finally { loadingT.value = false } }
function openCreateT() { editingT.value = null; tForm.value = { terminalCode: '', terminalName: '', terminalType: 0, area: '', address: '', operatorName: '', remark: '' }; showTModal.value = true }
function openEditT(row: any) { editingT.value = row; tForm.value = { terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', terminalType: row.terminalType ?? 0, area: row.area ?? '', address: row.address ?? '', operatorName: row.operatorName ?? '', remark: row.remark ?? '' }; showTModal.value = true }
async function submitT() { try { const data: Record<string, any> = { ...tForm.value }; if (editingT.value) { data.terminalId = editingT.value.terminalId; await salesApi.updateTerminal(data); notify('success', '终端更新成功') } else { await salesApi.createTerminal(data); notify('success', '终端注册成功') } showTModal.value = false; loadTerminals() } catch (e: any) { notify('error', '操作失败') } }
function confirmDeleteT(id: number) { deletingTId.value = id; showTConfirm.value = true }
async function doDeleteT() { try { await salesApi.deleteTerminal(deletingTId.value!); notify('success', '已删除'); showTConfirm.value = false; loadTerminals() } catch (e: any) { notify('error', '删除失败') } }

// Stock
async function loadStocks() { loadingS.value = true; try { if (sFilters.value.terminalId) { const d = await salesApi.listStock(sFilters.value.terminalId); stocks.value = Array.isArray(d) ? d : [] } else if (sFilters.value.prodBatchNo) { const d = await salesApi.listStockByBatch(sFilters.value.prodBatchNo); stocks.value = Array.isArray(d) ? d : [] } else { const d = await salesApi.listAllStock(); stocks.value = Array.isArray(d) ? d : [] }; sPage.value = 1 } catch (e: any) { console.error('Stock load error:', e); notify('error', '加载失败: ' + (e.message || '未知错误')) } finally { loadingS.value = false } }
function openCreateS() { editingS.value = null; sForm.value = { stockCode: '', terminalId: '', terminalName: '', prodBatchNo: '', productName: '', quantity: '', receivedTime: '', stockStatus: 0, remark: '' }; showSModal.value = true }
function openEditS(row: any) { editingS.value = row; sForm.value = { stockCode: row.stockCode ?? '', terminalId: row.terminalId ?? '', terminalName: row.terminalName ?? '', prodBatchNo: row.prodBatchNo ?? '', productName: row.productName ?? '', quantity: row.quantity ?? '', receivedTime: row.receivedTime ?? '', stockStatus: row.stockStatus ?? 0, remark: row.remark ?? '' }; showSModal.value = true }
async function submitS() { try { const data: Record<string, any> = { ...sForm.value }; if (editingS.value) { data.stockId = editingS.value.stockId; await salesApi.updateStock(data); notify('success', '库存更新成功') } else { await salesApi.stockIn(data); notify('success', '产品入库成功') } showSModal.value = false; loadStocks() } catch (e: any) { notify('error', '操作失败') } }

// Supplement
async function loadSupplements() { loadingSup.value = true; try { if (supFilters.value.traceBatchNo) { const d = await salesApi.listSupplement(supFilters.value.traceBatchNo); supplements.value = Array.isArray(d) ? d : [] } else if (supFilters.value.terminalCode) { const d = await salesApi.listSupplementByTerminal(supFilters.value.terminalCode); supplements.value = Array.isArray(d) ? d : [] } else supplements.value = []; supPage.value = 1 } catch (e: any) { notify('error', '加载失败') } finally { loadingSup.value = false } }
function openCreateSup() { editingSup.value = null; supForm.value = { supplementCode: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' }; showSupModal.value = true }
function openEditSup(row: any) { editingSup.value = row; supForm.value = { supplementCode: row.supplementCode ?? '', traceBatchNo: row.traceBatchNo ?? '', terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', salesCompany: row.salesCompany ?? '', salesBatchNo: row.salesBatchNo ?? '', temperature: row.temperature ?? '', humidity: row.humidity ?? '', storageMethod: row.storageMethod ?? 0, lightCondition: row.lightCondition ?? 0, shelfLife: row.shelfLife ?? '', locationCode: row.locationCode ?? '', quantity: row.quantity ?? '', inboundTime: row.inboundTime ?? '', operator: row.operator ?? '', supplementStatus: row.supplementStatus ?? 1, remark: row.remark ?? '' }; showSupModal.value = true }
async function submitSup() { try { const data: Record<string, any> = { ...supForm.value }; if (editingSup.value) { data.supplementId = editingSup.value.supplementId; await salesApi.updateSupplement(data); notify('success', '补充信息更新成功') } else { await salesApi.supplementInfo(data); notify('success', '销售详情补充成功') } showSupModal.value = false; loadSupplements() } catch (e: any) { notify('error', '操作失败') } }

async function runAntiFraud() { try { await salesApi.runAntiFraud(); notify('success', '防窜货核验执行完毕') } catch (e: any) { notify('error', '核验失败') } }

// Sales Order CRUD
async function loadOrders() { loadingO.value = true; try { const p: Record<string, any> = {}; if (oFilters.value.buyerName) p.buyerName = oFilters.value.buyerName; if (oFilters.value.productName) p.productName = oFilters.value.productName; if (oFilters.value.orderStatus) p.orderStatus = Number(oFilters.value.orderStatus); const data = await salesOrderApi.listOrder(p); orders.value = Array.isArray(data) ? data : []; oPage.value = 1 } catch (e: any) { notify('error', '加载失败') } finally { loadingO.value = false } }
function openCreateO() { editingO.value = null; oForm.value = { salesOrderCode: '', productName: '', prodBatchNo: '', buyerEnterpriseId: '', buyerEnterpriseName: '', sellerEnterpriseName: enterpriseName || currentUser, orderQuantity: '', unitPrice: '', totalAmount: '', orderDate: new Date().toISOString().slice(0, 10), orderStatus: 1, remark: '' }; buyerSearchText.value = ''; buyerOptions.value = []; prodBatchSearchText.value = ''; prodBatchOptions.value = []; loadProdBatchOptions(); showOModal.value = true }
function openEditO(row: any) { editingO.value = row; oForm.value = { salesOrderCode: row.salesOrderCode ?? '', productName: row.productName ?? '', prodBatchNo: row.prodBatchNo ?? '', buyerEnterpriseId: row.buyerEnterpriseId ?? '', buyerEnterpriseName: row.buyerEnterpriseName ?? '', sellerEnterpriseName: row.sellerEnterpriseName ?? '', orderQuantity: row.orderQuantity ?? '', unitPrice: row.unitPrice ?? '', totalAmount: row.totalAmount ?? '', orderDate: row.orderDate ?? '', orderStatus: row.orderStatus ?? 1, remark: row.remark ?? '' }; buyerSearchText.value = row.buyerEnterpriseName ?? ''; buyerOptions.value = []; prodBatchSearchText.value = row.prodBatchNo ? (row.prodBatchNo + ' — ' + (row.productName || '')) : ''; prodBatchOptions.value = []; showOModal.value = true }
async function submitO() { try { const data: Record<string, any> = { ...oForm.value }; if (editingO.value) { data.salesOrderId = editingO.value.salesOrderId; await salesOrderApi.updateOrder(data); notify('success', '订单更新成功') } else { const code = await salesOrderApi.createOrder(data); if (code && !code.includes('失败') && !code.includes('错误')) { notify('success', '订单创建成功 — ' + code) } else { notify('error', typeof code === 'string' ? code : '订单创建失败') } } showOModal.value = false; loadOrders() } catch (e: any) { notify('error', '操作失败: ' + (e.message || '')) } }
function confirmDeleteO(id: number) { deletingOId.value = id; showOConfirm.value = true }
async function doDeleteO() { try { await salesOrderApi.deleteOrder(deletingOId.value!); notify('success', '已删除'); showOConfirm.value = false; loadOrders() } catch (e: any) { notify('error', '删除失败') } }

// Order Detail
async function openDetailUpload(row: any) {
  editingOD.value = null;
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ');
  odForm.value = { detailCode: '', salesOrderId: row.salesOrderId ?? '', salesOrderCode: row.salesOrderCode ?? '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', inboundTime: now, actualQuantity: row.orderQuantity ?? '', salesDate: '', detailStatus: 1, operator: '', remark: '' };
  // 如果已有详情则加载
  try {
    const detail = await salesOrderApi.queryOrderDetail(row.salesOrderCode);
    if (detail && detail.detailId) { editingOD.value = detail; orderDetail.value = detail; odForm.value = { detailCode: detail.detailCode ?? '', salesOrderId: detail.salesOrderId ?? row.salesOrderId, salesOrderCode: detail.salesOrderCode ?? row.salesOrderCode, temperature: detail.temperature ?? '', humidity: detail.humidity ?? '', storageMethod: detail.storageMethod ?? 0, lightCondition: detail.lightCondition ?? 0, shelfLife: detail.shelfLife ?? '', locationCode: detail.locationCode ?? '', inboundTime: detail.inboundTime ?? now, actualQuantity: detail.actualQuantity ?? row.orderQuantity, salesDate: detail.salesDate ?? '', detailStatus: detail.detailStatus ?? 1, operator: detail.operator ?? '', remark: detail.remark ?? '' } }
  } catch { /* 还没有详情，使用默认值 */ }
  showODModal.value = true
}
async function submitOD() { try { const data: Record<string, any> = { ...odForm.value }; if (editingOD.value) { data.detailId = editingOD.value.detailId; await salesOrderApi.updateOrderDetail(data); notify('success', '详情更新成功') } else { await salesOrderApi.createOrderDetail(data); notify('success', '销售详情上传成功') } showODModal.value = false; if (tab.value === 'upload') loadUploadData(); else loadOrders() } catch (e: any) { notify('error', '操作失败: ' + (e.message || '')) } }

const stats = computed(() => {
  if (tab.value === 'upload') return [
    { label: '待补充', icon: Upload, cls: 'red', val: uploadStats.value.pending },
    { label: '已补充', icon: Select, cls: 'green', val: uploadStats.value.completed },
    { label: '总计', icon: DocumentAdd, cls: '', val: uploadStats.value.total },
    { label: '完成率', icon: Select, cls: 'blue', val: uploadStats.value.total > 0 ? Math.round(uploadStats.value.completed / uploadStats.value.total * 100) + '%' : '0%' },
  ]
  if (tab.value === 'order') return [
    { label: '销售订单', icon: DocumentAdd, cls: '', val: orders.value.length },
    { label: '已补充', icon: Upload, cls: 'green', val: orders.value.filter((r: any) => r.orderStatus === 2 || r.orderStatus === 3).length },
  ]
  if (tab.value === 'terminal') return [
    { label: '终端总数', icon: Shop, cls: '', val: terminals.value.length },
    { label: '已启用', icon: Shop, cls: 'green', val: terminals.value.filter((r: any) => r.terminalStatus === 1).length },
  ]
  if (tab.value === 'stock') return [
    { label: '库存记录', icon: SoldOut, cls: '', val: stocks.value.length },
    { label: '在售', icon: Shop, cls: 'green', val: stocks.value.filter((r: any) => r.stockStatus === 2).length },
  ]
  return [
    { label: '补充记录', icon: SoldOut, cls: '', val: supplements.value.length },
    { label: '已审核', icon: Shop, cls: 'green', val: supplements.value.filter((r: any) => r.supplementStatus === 2).length },
  ]
})

function switchTab(t: 'upload' | 'order' | 'terminal' | 'stock' | 'supplement') { tab.value = t; if (t === 'upload') loadUploadData(); else if (t === 'order') loadOrders(); else if (t === 'terminal') loadTerminals(); else if (t === 'stock') loadStocks(); else loadSupplements() }
onMounted(() => { if (isSeller.value) loadUploadData(); else loadOrders() })
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner" :class="isSeller ? 'seller' : 'manufacturer'">
      <span class="trace-role-badge">{{ isSeller ? '销售商' : '加工生产商' }}</span>
      <span v-if="isManufacturer">您创建销售订单 → 指定销售厂商 → <strong>销售商登录后补充销售详情</strong></span>
      <span v-else>生产商已创建销售订单 → <strong>您补充入库环境、储存条件、销售信息</strong></span>
    </div>

    <section class="trace-stats">
      <article v-for="s in stats" :key="s.label" :class="s.cls">
        <span><el-icon><component :is="s.icon" /></el-icon> {{ s.label }}</span>
        <b>{{ s.val }}</b><em>条记录</em>
      </article>
    </section>

    <div class="trace-tabs">
      <button v-if="isSeller" class="trace-tab-btn" :class="{ active: tab === 'upload' }" @click="switchTab('upload')"><el-icon><Upload /></el-icon> 销售信息上传</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'order' }" @click="switchTab('order')"><el-icon><DocumentAdd /></el-icon> 销售订单</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'terminal' }" @click="switchTab('terminal')"><el-icon><Shop /></el-icon> 销售终端</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'stock' }" @click="switchTab('stock')"><el-icon><SoldOut /></el-icon> 库存管理</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'supplement' }" @click="switchTab('supplement')"><el-icon><SoldOut /></el-icon> 销售补充</button>
    </div>

    <!-- Upload (销售商专用，仿 SupplierRawMaterials 模式) -->
    <template v-if="tab === 'upload'">
      <section class="trace-panel list-panel" style="margin-bottom:24px">
        <header class="panel-header">
          <div><p>销售台账</p><h2>销售信息上传</h2></div>
        </header>
      </section>

      <!-- 待补充详情列表 -->
      <section class="trace-panel list-panel" style="margin-bottom:24px">
        <header class="panel-header">
          <div><p>需要补充</p><h2>待补充详情的销售订单</h2></div>
          <span style="color:#c04550;font-size:13px;font-weight:700">({{ pendingOrders.length }} 条)</span>
        </header>
        <div v-if="loadingUp" style="text-align:center;padding:30px;color:#8195aa">加载中...</div>
        <div v-else-if="!pendingOrders.length" style="margin:18px;text-align:center;padding:30px;color:#8195aa">
          <div style="font-size:40px;margin-bottom:10px">📋</div>
          <p>所有订单已完成详情上传</p>
        </div>
        <div v-else style="padding:0 18px 18px">
          <div v-for="row in pendingOrders" :key="row.salesOrderId" style="display:flex;align-items:center;gap:14px;padding:12px 14px;margin-bottom:8px;border:1px solid #ffe8e8;border-radius:8px;background:#fff9f9">
            <span style="width:26px;height:26px;border-radius:50%;background:#ffe8e8;color:#c04550;display:grid;place-items:center;font-size:12px;font-weight:900;flex-shrink:0">!</span>
            <div style="flex:1">
              <strong>{{ row.salesOrderCode }}</strong> — {{ row.productName }}
              <small style="display:block;color:#96a8b9;font-size:11px">买方：{{ row.buyerEnterpriseName }} · 创建：{{ row.createTime }} · 状态：待补充详情</small>
            </div>
            <button class="primary" style="font-size:12px;padding:6px 12px" @click="openDetailUpload(row)"><el-icon><Upload /></el-icon> 补充销售详情</button>
          </div>
        </div>
      </section>

      <!-- 已补充列表 -->
      <section class="trace-panel list-panel">
        <header class="panel-header">
          <div><p>已补充</p><h2>已上传销售详情的订单</h2></div>
          <span style="color:#8195aa;font-size:13px">({{ completedOrders.length }} 条)</span>
        </header>
        <div v-if="loadingUp" style="text-align:center;padding:30px;color:#8195aa">加载中...</div>
        <div v-else-if="!completedOrders.length" style="margin:18px;text-align:center;padding:30px;color:#8195aa">
          <div style="font-size:40px;margin-bottom:10px">📭</div>
          <p>暂无已补充的记录</p>
        </div>
        <div v-else style="padding:0 18px 18px">
          <div v-for="row in completedOrders" :key="row.salesOrderId" style="display:flex;align-items:center;gap:14px;padding:12px 14px;margin-bottom:8px;border:1px solid #dce8f4;border-radius:8px;background:#fff">
            <span style="width:26px;height:26px;border-radius:50%;background:#e6f8ee;color:#2e9f5e;display:grid;place-items:center;font-size:12px;font-weight:900;flex-shrink:0">✓</span>
            <div style="flex:1">
              <strong>{{ row.salesOrderCode }}</strong> — {{ row.productName }}
              <small style="display:block;color:#96a8b9;font-size:11px">数量：{{ row.orderQuantity }} · 金额：{{ row.totalAmount }} · {{ row.orderDate }}</small>
            </div>
            <span class="status status-active">已补充</span>
            <button style="font-size:12px;padding:6px 12px" @click="openDetailUpload(row)"><el-icon><SoldOut /></el-icon> 查看详情</button>
          </div>
        </div>
      </section>
    </template>

    <!-- Order -->
    <template v-if="tab === 'order'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>销售厂商<input v-model="oFilters.buyerName" @keyup.enter="loadOrders" /></label>
          <label>产品名称<input v-model="oFilters.productName" @keyup.enter="loadOrders" /></label>
          <label>状态<select v-model="oFilters.orderStatus"><option value="">全部</option><option value="1">待补充</option><option value="2">已补充</option><option value="3">已完成</option></select></label>
          <div class="filter-actions"><button class="primary" @click="loadOrders"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>

      <section class="trace-panel list-panel">
        <header class="panel-header">
          <div><p>销售台账</p><h2>销售订单列表</h2></div>
          <button v-if="isManufacturer" class="primary create" @click="openCreateO"><el-icon><Plus /></el-icon> 创建销售订单</button>
        </header>
        <div class="table-wrap"><table><thead><tr><th>订单编码</th><th>产品名称</th><th>生产批次</th><th>销售厂商</th><th>数量</th><th>金额</th><th>订单日期</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingO"><td colspan="9" class="empty">加载中...</td></tr><tr v-else-if="!orders.length"><td colspan="9" class="empty">暂无销售订单</td></tr>
            <tr v-for="row in paginatedOrders" :key="row.salesOrderId">
              <td><code>{{ row.salesOrderCode }}</code></td><td><strong>{{ row.productName }}</strong></td>
              <td><code>{{ row.prodBatchNo }}</code></td><td>{{ row.buyerEnterpriseName }}</td>
              <td>{{ row.orderQuantity }}</td><td>{{ row.totalAmount }}</td><td>{{ row.orderDate }}</td>
              <td><span class="status" :class="row.orderStatus === 1 ? 'status-pending' : 'status-active'">{{ row.orderStatus === 1 ? '待补充' : row.orderStatus === 2 ? '已补充' : '已完成' }}</span></td>
              <td class="actions">
                <button v-if="isSeller && row.orderStatus === 1" class="primary" @click="openDetailUpload(row)"><el-icon><Upload /></el-icon> 补充销售详情</button>
                <button v-if="isSeller && row.orderStatus >= 2" @click="openDetailUpload(row)"><el-icon><SoldOut /></el-icon> 查看/编辑详情</button>
                <button v-if="isManufacturer" @click="openEditO(row)"><el-icon><SoldOut /></el-icon> 编辑</button>
                <button v-if="isManufacturer" class="danger" @click="confirmDeleteO(row.salesOrderId)"><el-icon><Close /></el-icon> 删除</button>
              </td>
            </tr></tbody></table></div>
      <Pagination v-model="oPage" :total="orders.length" :page-size="pageSize" />
      </section>

      <!-- Order Modal -->
      <div v-if="showOModal" class="trace-modal-backdrop" @click.self="showOModal = false">
        <section class="trace-modal"><header><div><p>销售订单</p><h2>{{ editingO ? '编辑销售订单' : '创建销售订单' }}</h2></div><button @click="showOModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body grid-form">
            <label>销售编码<input v-model="oForm.salesOrderCode" placeholder="留空则自动生成 SO 编码" :disabled="!!editingO" /></label>
            <label>产品名称 *<input v-model="oForm.productName" /></label>
            <label style="position:relative">生产批次
              <input v-model="prodBatchSearchText" placeholder="搜索并选择生产批次..." @focus="onProdBatchFocus" @input="onProdBatchInput" @blur="onProdBatchBlur" autocomplete="off" />
              <ul v-if="prodBatchDropdownVisible && filteredProdBatchOptions.length" class="search-dropdown">
                <li v-for="b in filteredProdBatchOptions" :key="b.prodBatchId || b.batchNo" @mousedown.prevent="onProdBatchSelect(b)">
                  <strong>{{ b.batchNo }}</strong><small>{{ b.productName || '' }}</small>
                </li>
              </ul>
            </label>
            <input type="hidden" :value="oForm.prodBatchNo" />
            <div class="form-row-2">
              <label style="position:relative">销售厂商（买方） *
                <input v-model="buyerSearchText" placeholder="搜索选择销售商..." @focus="onBuyerFocus" @input="onBuyerInput" @blur="onBuyerBlur" autocomplete="off" />
                <ul v-if="buyerDropdownVisible && filteredBuyerOptions.length" class="search-dropdown">
                  <li v-for="ent in filteredBuyerOptions" :key="ent.enterpriseUuid" @mousedown.prevent="onBuyerSelect(ent)">
                    <strong>{{ ent.enterpriseName }}</strong><small>销售商</small>
                  </li>
                </ul>
              </label>
              <input type="hidden" :value="oForm.buyerEnterpriseName" />
              <label>生产商（卖方）<input v-model="oForm.sellerEnterpriseName" readonly disabled style="background:#f5f7fa;color:#8195aa" /></label>
            </div>
            <label>数量<input v-model="oForm.orderQuantity" type="number" /></label>
            <label>单价<input v-model="oForm.unitPrice" type="number" step="0.01" /></label>
            <label>总金额<input v-model="oForm.totalAmount" type="number" step="0.01" /></label>
            <label>订单日期<input v-model="oForm.orderDate" type="date" /></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="oForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          <footer><button class="secondary" @click="showOModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitO"><el-icon><Plus /></el-icon> {{ editingO ? '保存' : '创建订单' }}</button></footer>
        </section>
      </div>

      <!-- Order Detail Modal -->
      <div v-if="showODModal" class="trace-modal-backdrop" @click.self="showODModal = false">
        <section class="trace-modal" style="width:760px"><header><div><p>销售详情</p><h2>{{ editingOD ? '编辑销售详情' : '补充销售详情' }}</h2></div><button @click="showODModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body">
            <div class="grid-form">
              <label>订单编码<code>{{ odForm.salesOrderCode }}</code></label>
              <label>入库温度<input v-model="odForm.temperature" placeholder="2-6℃" /></label>
              <label>入库湿度<input v-model="odForm.humidity" placeholder="45-65%RH" /></label>
              <label>储存方式<select v-model.number="odForm.storageMethod"><option :value="0">常温</option><option :value="1">冷藏</option><option :value="2">冷冻</option></select></label>
              <label>光照条件<select v-model.number="odForm.lightCondition"><option :value="0">避光</option><option :value="1">散光</option><option :value="2">光照</option></select></label>
              <label>上架保质期<input v-model="odForm.shelfLife" placeholder="上架后7天" /></label>
              <label>仓库/柜位编号<input v-model="odForm.locationCode" /></label>
              <label>入库时间<input v-model="odForm.inboundTime" /></label>
              <label>实际数量<input v-model="odForm.actualQuantity" type="number" /></label>
              <label>销售日期<input v-model="odForm.salesDate" type="date" /></label>
              <label>操作员<input v-model="odForm.operator" /></label>
            </div>
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:15px">备注<textarea v-model="odForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          </div>
          <footer><button class="secondary" @click="showODModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitOD"><el-icon><Upload /></el-icon> {{ editingOD ? '保存更改' : '提交详情' }}</button></footer>
        </section>
      </div>

      <!-- Order Delete Confirm -->
      <div v-if="showOConfirm" class="trace-confirm-overlay" @click.self="showOConfirm = false">
        <div class="trace-confirm-box"><h3>确认删除</h3><p>确定要删除该销售订单吗？</p><div class="trace-confirm-actions"><button class="secondary" @click="showOConfirm = false"><el-icon><Close /></el-icon> 取消</button><button class="primary danger-fill" @click="doDeleteO"><el-icon><Close /></el-icon> 确认删除</button></div></div>
      </div>
    </template>

    <!-- Terminal -->
    <template v-if="tab === 'terminal'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>类型<select v-model="tFilters.terminalType"><option value="">全部</option><option value="1">超市</option><option value="2">便利店</option><option value="3">餐饮</option><option value="4">电商</option><option value="5">农贸</option><option value="6">其他</option></select></label>
          <label>区域<input v-model="tFilters.area" @keyup.enter="loadTerminals" /></label>
          <div class="filter-actions">
            <button class="primary" @click="loadTerminals"><el-icon><Search /></el-icon> 查询</button>
            <button v-if="isManufacturer" class="secondary" @click="runAntiFraud"><el-icon><Search /></el-icon> 防窜货核验</button>
          </div>
        </div>
      </section>

      <section class="trace-panel list-panel">
        <header class="panel-header">
          <div><p>终端台账</p><h2>销售终端列表</h2></div>
          <button class="primary create" @click="openCreateT"><el-icon><Plus /></el-icon> 注册终端</button>
        </header>
        <div class="table-wrap"><table><thead><tr><th>终端编码</th><th>终端名称</th><th>类型</th><th>区域</th><th>运营商</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingT"><td colspan="8" class="empty">加载中...</td></tr><tr v-else-if="!terminals.length"><td colspan="8" class="empty">暂无销售终端</td></tr>
            <tr v-for="row in paginatedTerminals" :key="row.terminalId">
              <td><code>{{ row.terminalCode }}</code></td><td><strong>{{ row.terminalName }}</strong></td>
              <td>{{ terminalTypeLabels[row.terminalType] || '-' }}</td><td>{{ row.area }}</td><td>{{ row.operatorName }}</td>
              <td><span class="status" :class="tagClass(['','启用','停用','装修中'][row.terminalStatus] || '')">{{ ['','启用','停用','装修中'][row.terminalStatus] || '-' }}</span></td>
              <td>{{ row.createTime }}</td>
              <td class="actions">
                <button @click="openEditT(row)"><el-icon><SoldOut /></el-icon> 编辑</button>
                <button v-if="isManufacturer" class="danger" @click="confirmDeleteT(row.terminalId)"><el-icon><Close /></el-icon> 删除</button>
              </td>
            </tr></tbody></table></div>
      <Pagination v-model="tPage" :total="terminals.length" :page-size="pageSize" />
      </section>

      <!-- Terminal Modal -->
      <div v-if="showTModal" class="trace-modal-backdrop" @click.self="showTModal = false">
        <section class="trace-modal"><header><div><p>终端管理</p><h2>{{ editingT ? '编辑终端' : '注册销售终端' }}</h2></div><button @click="showTModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body grid-form">
            <label>终端编码 *<input v-model="tForm.terminalCode" /></label>
            <label>终端名称 *<input v-model="tForm.terminalName" /></label>
            <label>类型<select v-model.number="tForm.terminalType"><option :value="0">请选择</option><option :value="1">超市</option><option :value="2">便利店</option><option :value="3">餐饮</option><option :value="4">电商</option><option :value="5">农贸</option><option :value="6">其他</option></select></label>
            <label>区域<input v-model="tForm.area" /></label>
            <label>地址<input v-model="tForm.address" /></label>
            <label>运营商<input v-model="tForm.operatorName" /></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="tForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          <footer><button class="secondary" @click="showTModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitT"><el-icon><Plus /></el-icon> {{ editingT ? '保存' : '注册' }}</button></footer>
        </section>
      </div>
    </template>

    <!-- Stock -->
    <template v-if="tab === 'stock'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>终端ID<input v-model="sFilters.terminalId" @keyup.enter="loadStocks" /></label>
          <label>生产批次<input v-model="sFilters.prodBatchNo" @keyup.enter="loadStocks" /></label>
          <div class="filter-actions"><button class="primary" @click="loadStocks"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>库存台账</p><h2>库存列表</h2></div><button class="primary create" @click="openCreateS"><el-icon><Plus /></el-icon> 产品入库</button></header>
        <div class="table-wrap"><table><thead><tr><th>库存编码</th><th>产品</th><th>生产批次</th><th>终端</th><th>数量</th><th>入库时间</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingS"><td colspan="8" class="empty">加载中...</td></tr><tr v-else-if="!stocks.length"><td colspan="8" class="empty">暂无库存</td></tr>
            <tr v-for="row in paginatedStocks" :key="row.stockId"><td><code>{{ row.stockCode }}</code></td><td>{{ row.productName }}</td><td><code>{{ row.prodBatchNo }}</code></td><td>{{ row.terminalName }}</td><td>{{ row.quantity }}</td><td>{{ row.receivedTime }}</td>
              <td><span class="status" :class="tagClass(stockStatusLabels[row.stockStatus] || '')">{{ stockStatusLabels[row.stockStatus] || '-' }}</span></td>
              <td class="actions"><button @click="openEditS(row)"><el-icon><SoldOut /></el-icon> 盘点</button></td></tr></tbody></table></div>
      <Pagination v-model="sPage" :total="stocks.length" :page-size="pageSize" />
      </section>

      <div v-if="showSModal" class="trace-modal-backdrop" @click.self="showSModal = false">
        <section class="trace-modal"><header><div><p>库存管理</p><h2>{{ editingS ? '库存盘点' : '产品入库' }}</h2></div><button @click="showSModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body grid-form">
            <label>产品名称 *<input v-model="sForm.productName" /></label>
            <label>生产批次 *<input v-model="sForm.prodBatchNo" /></label>
            <label>终端名称<input v-model="sForm.terminalName" /></label>
            <label>数量 *<input v-model="sForm.quantity" type="number" /></label>
            <label>入库时间<input v-model="sForm.receivedTime" /></label>
            <label>状态<select v-model.number="sForm.stockStatus"><option :value="0">请选择</option><option :value="1">在库</option><option :value="2">在售</option><option :value="3">下架</option><option :value="4">售罄</option></select></label>
            <label>终端ID<input v-model="sForm.terminalId" /></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="sForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          <footer><button class="secondary" @click="showSModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitS"><el-icon><Plus /></el-icon> {{ editingS ? '保存' : '入库' }}</button></footer>
        </section>
      </div>
    </template>

    <!-- Supplement -->
    <template v-if="tab === 'supplement'">
      <div class="trace-hint" :class="isSeller ? 'info' : 'success'">
        <span v-if="isManufacturer">📋 生产商在溯源码管理中设置销售公司和批次号后，销售商在此补充储存环境等详情。</span>
        <span v-else>📋 生产商已设置销售公司，请补充入库环境、储存条件、光照、保质期等详情。</span>
      </div>
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>溯源码批次<input v-model="supFilters.traceBatchNo" @keyup.enter="loadSupplements" /></label>
          <label>终端编码<input v-model="supFilters.terminalCode" @keyup.enter="loadSupplements" /></label>
          <div class="filter-actions"><button class="primary" @click="loadSupplements"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>销售台账</p><h2>销售补充</h2></div><button v-if="isSeller" class="primary create" @click="openCreateSup"><el-icon><Plus /></el-icon> 补充销售详情</button></header>
        <div class="table-wrap"><table><thead><tr><th>补充编码</th><th>溯源码批次</th><th>销售企业</th><th>销售批次</th><th>温度</th><th>湿度</th><th>储存方式</th><th>保质期</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingSup"><td colspan="10" class="empty">加载中...</td></tr><tr v-else-if="!supplements.length"><td colspan="10" class="empty">暂无补充信息</td></tr>
            <tr v-for="row in paginatedSupplements" :key="row.supplementId"><td><code>{{ row.supplementCode }}</code></td><td><code>{{ row.traceBatchNo }}</code></td><td>{{ row.salesCompany }}</td><td>{{ row.salesBatchNo }}</td><td>{{ row.temperature || '-' }}</td><td>{{ row.humidity || '-' }}</td><td>{{ ['','常温','冷藏','冷冻'][row.storageMethod] || row.storageMethod || '-' }}</td><td>{{ row.shelfLife || '-' }}</td>
              <td><span class="status" :class="row.supplementStatus === 2 ? 'status-active' : 'status-pending'">{{ row.supplementStatus === 1 ? '已提交' : '已审核' }}</span></td>
              <td class="actions"><button v-if="isSeller" @click="openEditSup(row)"><el-icon><SoldOut /></el-icon> 编辑</button></td></tr></tbody></table></div>
      <Pagination v-model="supPage" :total="supplements.length" :page-size="pageSize" />
      </section>

      <div v-if="showSupModal" class="trace-modal-backdrop" @click.self="showSupModal = false">
        <section class="trace-modal" style="width:760px"><header><div><p>销售补充</p><h2>补充销售详情</h2></div><button @click="showSupModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body">
            <div class="grid-form">
              <label>溯源码批次 *<input v-model="supForm.traceBatchNo" /></label>
              <label>销售企业<input v-model="supForm.salesCompany" /></label>
              <label>销售批次号<input v-model="supForm.salesBatchNo" /></label>
              <label>终端编码<input v-model="supForm.terminalCode" /></label>
              <label>终端名称<input v-model="supForm.terminalName" /></label>
              <label>数量<input v-model="supForm.quantity" type="number" /></label>
              <label>入库时间<input v-model="supForm.inboundTime" /></label>
              <label>操作员<input v-model="supForm.operator" /></label>
              <label>入库温度<input v-model="supForm.temperature" placeholder="2-6℃" /></label>
              <label>入库湿度<input v-model="supForm.humidity" placeholder="45-65%RH" /></label>
              <label>储存方式<select v-model.number="supForm.storageMethod"><option :value="0">常温</option><option :value="1">冷藏</option><option :value="2">冷冻</option></select></label>
              <label>光照条件<select v-model.number="supForm.lightCondition"><option :value="0">避光</option><option :value="1">散光</option><option :value="2">光照</option></select></label>
              <label>上架保质期<input v-model="supForm.shelfLife" placeholder="上架后7天" /></label>
              <label>仓库/柜位编号<input v-model="supForm.locationCode" /></label>
            </div>
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:15px">备注<textarea v-model="supForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          </div>
          <footer><button class="secondary" @click="showSupModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitSup"><el-icon><Plus /></el-icon> 确认提交</button></footer>
        </section>
      </div>
    </template>

    <!-- 删除确认 -->
    <div v-if="showTConfirm" class="trace-confirm-overlay" @click.self="showTConfirm = false">
      <div class="trace-confirm-box"><h3>确认删除</h3><p>确定要删除该销售终端吗？</p><div class="trace-confirm-actions"><button class="secondary" @click="showTConfirm = false"><el-icon><Close /></el-icon> 取消</button><button class="primary danger-fill" @click="doDeleteT"><el-icon><Close /></el-icon> 确认删除</button></div></div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

/* 两列同行布局 */
.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

/* 可搜索下拉框 */
.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-height: 200px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #d7e4f0;
  border-radius: 7px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
  z-index: 100;
  list-style: none;
  margin: 2px 0 0 0;
  padding: 4px 0;
}
.search-dropdown li {
  padding: 9px 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.search-dropdown li:hover {
  background: #eaf2ff;
}
.search-dropdown li strong {
  font-size: 13px;
  color: #244564;
}
.search-dropdown li small {
  font-size: 11px;
  color: #8195aa;
}
</style>
