<script setup lang="ts">
import { ref, computed, onMounted, inject, type Ref } from 'vue'
import { Box, Check, Close, Delete, Download, Edit, Plus, Search, Upload, Van, Warning } from '@element-plus/icons-vue'
import { coldChainApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
const isLogistics = computed(() => currentRole?.value === 'logistics' || currentRole?.value === 'super-admin')
const isOtherEnterprise = computed(() => !isLogistics.value)

const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)

// ==================== Tabs ====================
const tab = ref<'transport' | 'vehicle' | 'warehouse' | 'shipping'>('transport')

// ==================== Transport ====================
const loadingT = ref(false); const transports = ref<any[]>([])
const tFilters = ref({ transportStatus: '', prodBatchNo: '', plateNo: '' })
const showTModal = ref(false); const editingT = ref<any>(null); const showTConfirm = ref(false); const deletingTId = ref<number | null>(null)
const tForm = ref({ orderNo: '', plateNo: '', driverName: '', driverPhone: '', productName: '', prodBatchNo: '', departureName: '', destinationName: '', loadingTemp: '', departTime: '', estimatedArrival: '', transportMethod: 0, collectInterval: '', tempUpper: '', tempLower: '', humidUpper: '', humidLower: '', alertMethod: '', remark: '' })
const transportStatusLabels = ['', '待发运', '运输中', '已签收', '温度预警', '异常关闭']
const tStats = computed(() => ({
  total: transports.value.length,
  inTransit: transports.value.filter((r: any) => r.transportStatus === 2).length,
  alert: transports.value.filter((r: any) => r.transportStatus === 4).length,
  arrived: transports.value.filter((r: any) => r.transportStatus === 3).length,
}))

// ==================== Vehicle ====================
const loadingV = ref(false); const vehicles = ref<any[]>([])
const vFilters = ref({ vehicleStatus: '', ownerName: '', coldType: '' })
const showVModal = ref(false); const editingV = ref<any>(null); const showVConfirm = ref(false); const deletingVId = ref<number | null>(null)
const vForm = ref({ plateNo: '', vehicleModel: '', driverName: '', driverPhone: '', ownerName: '', coldType: '', tempRange: '', remark: '' })
const vehicleStatusLabels = ['', '空闲', '运输中', '维修中']

// ==================== Warehouse ====================
const loadingW = ref(false); const warehouses = ref<any[]>([])
const wFilters = ref({ warehouseType: '', warehouseStatus: '' })
const showWModal = ref(false); const editingW = ref<any>(null); const showWConfirm = ref(false); const deletingWId = ref<number | null>(null)
const wForm = ref({ warehouseName: '', warehouseType: 0, address: '', capacity: '', manager: '', tempRange: '', humidityRange: '', warehouseStatus: 0, remark: '' })
const warehouseTypeLabels = ['', '原料仓', '成品仓', '冷链仓', '中转仓']
const warehouseStatusLabels = ['', '启用', '停用']

// ==================== Shipping (发运签收) ====================
const shippingOrders = ref<any[]>([])
const loadingS = ref(false)
const showDispatchModal = ref(false)
const showReceiptModal = ref(false)
const dispatchTarget = ref<any>(null)
const receiptTarget = ref<any>(null)
const dispatchForm = ref({ departTime: '', loadingTemp: '', checkerName: '', packageIntact: '完好', sealNo: '', remark: '' })
const receiptForm = ref({ arriveTime: '', arriveTemp: '', packageIntact: '完好', quantityCheck: '一致', receiverName: '', receiverPhone: '', signPhoto: '', remark: '' })

// ==================== Helpers ====================
function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function statusClass(s: string) {
  if (['已签收', '空闲', '启用', '正常', '完好', '一致'].some(x => s.includes(x))) return 'status-active'
  if (['运输中', '待发运', '待签收'].some(x => s.includes(x))) return 'status-pending'
  if (['温度预警', '维修中', '异常', '破损'].some(x => s.includes(x))) return 'status-void'
  return 'status-active'
}

// ==================== Transport CRUD ====================
async function loadTransports() {
  loadingT.value = true
  try { const p: Record<string, any> = {}; if (tFilters.value.transportStatus) p.transportStatus = Number(tFilters.value.transportStatus); if (tFilters.value.prodBatchNo) p.prodBatchNo = tFilters.value.prodBatchNo; if (tFilters.value.plateNo) p.plateNo = tFilters.value.plateNo; const data = await coldChainApi.listTransport(p); transports.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载运输订单失败: ' + e.message) } finally { loadingT.value = false }
}
function openCreateT() {
  editingT.value = null
  tForm.value = { orderNo: '', plateNo: '', driverName: '', driverPhone: '', productName: '', prodBatchNo: '', departureName: '', destinationName: '', loadingTemp: '', departTime: '', estimatedArrival: '', transportMethod: 0, collectInterval: '', tempUpper: '', tempLower: '', humidUpper: '', humidLower: '', alertMethod: '', remark: '' }
  showTModal.value = true
}
function openEditT(row: any) {
  if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可编辑运输详情'); return }
  editingT.value = row
  tForm.value = { orderNo: row.orderNo ?? '', plateNo: row.plateNo ?? '', driverName: row.driverName ?? '', driverPhone: row.driverPhone ?? '', productName: row.productName ?? '', prodBatchNo: row.prodBatchNo ?? '', departureName: row.departureName ?? '', destinationName: row.destinationName ?? '', loadingTemp: row.loadingTemp ?? '', departTime: row.departTime ?? '', estimatedArrival: row.estimatedArrival ?? '', transportMethod: row.transportMethod ?? 0, collectInterval: row.collectInterval ?? '', tempUpper: row.tempUpper ?? '', tempLower: row.tempLower ?? '', humidUpper: row.humidUpper ?? '', humidLower: row.humidLower ?? '', alertMethod: row.alertMethod ?? '', remark: row.remark ?? '' }
  showTModal.value = true
}
async function submitT() {
  if (isOtherEnterprise.value && !tForm.value.orderNo.trim()) { notify('error', '请填写运输单号'); return }
  try {
    const data: Record<string, any> = { ...tForm.value }
    if (editingT.value) { data.transportId = editingT.value.transportId; await coldChainApi.updateTransport(data); notify('success', '运输订单更新成功') }
    else { await coldChainApi.createTransport(data); notify('success', '运输单号已创建，等待冷链物流商完善信息') }
    showTModal.value = false; loadTransports()
  } catch (e: any) { notify('error', '操作失败: ' + e.message) }
}
function confirmDeleteT(id: number) { if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可删除'); return }; deletingTId.value = id; showTConfirm.value = true }
async function doDeleteT() { try { await coldChainApi.deleteTransport(deletingTId.value!); notify('success', '订单删除成功'); showTConfirm.value = false; loadTransports() } catch (e: any) { notify('error', '删除失败: ' + e.message) } }
async function stateAction(action: string, id: number) {
  if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可执行此操作'); return }
  const labels: Record<string, string> = { depart: '发运', arrive: '签收', alert: '预警', close: '关闭' }
  if (!confirm(`确认执行"${labels[action] || action}"操作？`)) return
  try { if (action === 'depart') await coldChainApi.departTransport(id); else if (action === 'arrive') await coldChainApi.arriveTransport(id); else if (action === 'alert') await coldChainApi.alertTransport(id); else if (action === 'close') await coldChainApi.closeTransport(id); notify('success', '操作成功'); loadTransports(); loadShippingOrders() } catch (e: any) { notify('error', '操作失败: ' + e.message) }
}

// ==================== Vehicle CRUD (仅物流商) ====================
async function loadVehicles() { loadingV.value = true; try { const p: Record<string, any> = {}; if (vFilters.value.vehicleStatus) p.vehicleStatus = Number(vFilters.value.vehicleStatus); if (vFilters.value.ownerName) p.ownerName = vFilters.value.ownerName; if (vFilters.value.coldType) p.coldType = vFilters.value.coldType; const data = await coldChainApi.listVehicle(p); vehicles.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载车辆失败') } finally { loadingV.value = false } }
function openCreateV() { if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可注册车辆'); return }; editingV.value = null; vForm.value = { plateNo: '', vehicleModel: '', driverName: '', driverPhone: '', ownerName: '', coldType: '', tempRange: '', remark: '' }; showVModal.value = true }
function openEditV(row: any) { if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可编辑车辆'); return }; editingV.value = row; vForm.value = { plateNo: row.plateNo ?? '', vehicleModel: row.vehicleModel ?? '', driverName: row.driverName ?? '', driverPhone: row.driverPhone ?? '', ownerName: row.ownerName ?? '', coldType: row.coldType ?? '', tempRange: row.tempRange ?? '', remark: row.remark ?? '' }; showVModal.value = true }
async function submitV() { try { const data: Record<string, any> = { ...vForm.value }; if (editingV.value) { data.vehicleId = editingV.value.vehicleId; await coldChainApi.updateVehicle(data); notify('success', '车辆信息更新成功') } else { await coldChainApi.createVehicle(data); notify('success', '冷链车辆注册成功') } showVModal.value = false; loadVehicles() } catch (e: any) { notify('error', '操作失败') } }
function confirmDeleteV(id: number) { if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可删除'); return }; deletingVId.value = id; showVConfirm.value = true }
async function doDeleteV() { try { await coldChainApi.deleteVehicle(deletingVId.value!); notify('success', '车辆删除成功'); showVConfirm.value = false; loadVehicles() } catch (e: any) { notify('error', '删除失败') } }

// ==================== Warehouse CRUD ====================
async function loadWarehouses() { loadingW.value = true; try { const p: Record<string, any> = {}; if (wFilters.value.warehouseType) p.warehouseType = Number(wFilters.value.warehouseType); if (wFilters.value.warehouseStatus) p.warehouseStatus = Number(wFilters.value.warehouseStatus); const data = await coldChainApi.listWarehouse(p); warehouses.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载仓库失败') } finally { loadingW.value = false } }
function openCreateW() { if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可新增仓库'); return }; editingW.value = null; wForm.value = { warehouseName: '', warehouseType: 0, address: '', capacity: '', manager: '', tempRange: '', humidityRange: '', warehouseStatus: 0, remark: '' }; showWModal.value = true }
function openEditW(row: any) { if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可编辑仓库'); return }; editingW.value = row; wForm.value = { warehouseName: row.warehouseName ?? '', warehouseType: row.warehouseType ?? 0, address: row.address ?? '', capacity: row.capacity ?? '', manager: row.manager ?? '', tempRange: row.tempRange ?? '', humidityRange: row.humidityRange ?? '', warehouseStatus: row.warehouseStatus ?? 0, remark: row.remark ?? '' }; showWModal.value = true }
async function submitW() { try { const data: Record<string, any> = { ...wForm.value }; if (editingW.value) { data.warehouseId = editingW.value.warehouseId; await coldChainApi.updateWarehouse(data); notify('success', '仓库更新成功') } else { await coldChainApi.createWarehouse(data); notify('success', '仓库创建成功') } showWModal.value = false; loadWarehouses() } catch (e: any) { notify('error', '操作失败') } }
function confirmDeleteW(id: number) { if (isOtherEnterprise.value) { notify('error', '仅冷链物流商可删除'); return }; deletingWId.value = id; showWConfirm.value = true }
async function doDeleteW() { try { await coldChainApi.deleteWarehouse(deletingWId.value!); notify('success', '仓库删除成功'); showWConfirm.value = false; loadWarehouses() } catch (e: any) { notify('error', '删除失败') } }

// ==================== Shipping (发运签收) ====================
async function loadShippingOrders() {
  loadingS.value = true
  try { const data = await coldChainApi.listTransport({}); shippingOrders.value = (Array.isArray(data) ? data : []).filter((r: any) => r.transportStatus === 1 || r.transportStatus === 2 || r.transportStatus === 3) } catch { shippingOrders.value = [] } finally { loadingS.value = false }
}
function openDispatch(row: any) { dispatchTarget.value = row; dispatchForm.value = { departTime: row.departTime || new Date().toISOString().slice(0, 16), loadingTemp: row.loadingTemp || '', checkerName: '', packageIntact: '完好', sealNo: '', remark: '' }; showDispatchModal.value = true }
function openReceipt(row: any) { receiptTarget.value = row; receiptForm.value = { arriveTime: row.estimatedArrival || new Date().toISOString().slice(0, 16), arriveTemp: '', packageIntact: '完好', quantityCheck: '一致', receiverName: '', receiverPhone: '', signPhoto: '', remark: '' }; showReceiptModal.value = true }
async function submitDispatch() {
  if (!dispatchTarget.value) return
  if (!dispatchForm.value.checkerName.trim()) { notify('error', '请填写发运核查人'); return }
  try {
    await coldChainApi.recordNode({ orderNo: dispatchTarget.value.orderNo, nodeType: 1, nodeTime: dispatchForm.value.departTime, location: dispatchTarget.value.departureName, operator: dispatchForm.value.checkerName, statusDesc: `装载温度:${dispatchForm.value.loadingTemp}℃ 包装:${dispatchForm.value.packageIntact} 铅封号:${dispatchForm.value.sealNo}`, remark: dispatchForm.value.remark })
    await coldChainApi.departTransport(dispatchTarget.value.transportId)
    notify('success', `运输单 ${dispatchTarget.value.orderNo} 已正式发运`)
    showDispatchModal.value = false; loadTransports(); loadShippingOrders()
  } catch (e: any) { notify('error', '发运失败: ' + e.message) }
}
async function submitReceipt() {
  if (!receiptTarget.value) return
  if (!receiptForm.value.receiverName.trim()) { notify('error', '请填写签收人'); return }
  try {
    await coldChainApi.signReceipt({ orderNo: receiptTarget.value.orderNo, signTime: receiptForm.value.arriveTime, signName: receiptForm.value.receiverName, signPhone: receiptForm.value.receiverPhone, arriveTemp: receiptForm.value.arriveTemp, packageIntact: receiptForm.value.packageIntact, quantityCheck: receiptForm.value.quantityCheck, remark: receiptForm.value.remark })
    await coldChainApi.arriveTransport(receiptTarget.value.transportId)
    notify('success', `运输单 ${receiptTarget.value.orderNo} 已成功签收`)
    showReceiptModal.value = false; loadTransports(); loadShippingOrders()
  } catch (e: any) { notify('error', '签收失败: ' + e.message) }
}

function switchTab(t: typeof tab.value) {
  tab.value = t
  if (t === 'transport') loadTransports()
  else if (t === 'vehicle') loadVehicles()
  else if (t === 'warehouse') loadWarehouses()
  else if (t === 'shipping') loadShippingOrders()
}
onMounted(loadTransports)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <!-- Role Banner -->
    <div class="trace-role-banner" :class="isLogistics ? 'manufacturer' : 'supplier'">
      <span class="trace-role-badge">{{ isLogistics ? '冷链物流商' : '协作企业' }}</span>
      <span v-if="isLogistics">管理运输订单、冷链车辆、仓库、执行<strong>发运与签收</strong></span>
      <span v-else>创建运输单号、<strong>实时查看</strong>冷链物流状态。车辆与运输详情由冷链物流商维护。</span>
    </div>

    <!-- Stats -->
    <section v-if="tab === 'transport'" class="trace-stats">
      <article><span><el-icon><Van /></el-icon> 运输订单</span><b>{{ tStats.total }}</b><em>个订单</em></article>
      <article class="amber"><span><el-icon><Van /></el-icon> 运输中</span><b>{{ tStats.inTransit }}</b><em>正在运输</em></article>
      <article v-if="tStats.alert > 0" class="red"><span><el-icon><Warning /></el-icon> 温度预警</span><b>{{ tStats.alert }}</b><em>需处理</em></article>
      <article class="green"><span><el-icon><Check /></el-icon> 已签收</span><b>{{ tStats.arrived }}</b><em>已完成</em></article>
    </section>

    <!-- Tabs -->
    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'transport' }" @click="switchTab('transport')"><el-icon><Van /></el-icon> 运输订单</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'shipping' }" @click="switchTab('shipping')"><el-icon><Check /></el-icon> 发运签收</button>
      <button v-if="isLogistics" class="trace-tab-btn" :class="{ active: tab === 'vehicle' }" @click="switchTab('vehicle')"><el-icon><Van /></el-icon> 冷链车辆</button>
      <button v-if="isLogistics" class="trace-tab-btn" :class="{ active: tab === 'warehouse' }" @click="switchTab('warehouse')"><el-icon><Box /></el-icon> 仓库管理</button>
    </div>

    <!-- ==================== Transport ==================== -->
    <template v-if="tab === 'transport'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>状态<select v-model="tFilters.transportStatus"><option value="">全部</option><option value="1">待发运</option><option value="2">运输中</option><option value="3">已签收</option><option value="4">温度预警</option><option value="5">异常关闭</option></select></label>
          <label>生产批次<input v-model="tFilters.prodBatchNo" @keyup.enter="loadTransports" /></label>
          <label>车牌号<input v-model="tFilters.plateNo" @keyup.enter="loadTransports" /></label>
          <div class="filter-actions"><button class="primary" @click="loadTransports"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header">
          <div><p>运输台账</p><h2>运输订单列表</h2></div>
          <button class="primary create" @click="openCreateT"><el-icon><Plus /></el-icon> {{ isLogistics ? '创建运输订单' : '新建运输单号' }}</button>
        </header>
        <div class="table-wrap"><table><thead><tr><th>订单号</th><th>车牌</th><th>产品</th><th>生产批次</th><th>发运地</th><th>目的地</th><th>温度(℃)</th><th>状态</th><th>发运时间</th><th>预计到达</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loadingT"><td colspan="11" class="empty">加载中...</td></tr>
            <tr v-else-if="!transports.length"><td colspan="11" class="empty">暂无运输订单，点击"新建运输单号"开始</td></tr>
            <tr v-for="row in transports" :key="row.transportId">
              <td><code>{{ row.orderNo }}</code></td>
              <td>{{ row.plateNo || '待分配' }}</td>
              <td>{{ row.productName || '-' }}</td>
              <td><code>{{ row.prodBatchNo || '-' }}</code></td>
              <td>{{ row.departureName || '-' }}</td>
              <td>{{ row.destinationName || '-' }}</td>
              <td>{{ row.loadingTemp ? row.loadingTemp + '℃' : '-' }}</td>
              <td><span class="status" :class="statusClass(transportStatusLabels[row.transportStatus] || '')">{{ transportStatusLabels[row.transportStatus] || '-' }}</span></td>
              <td>{{ row.departTime || '-' }}</td>
              <td>{{ row.estimatedArrival || '-' }}</td>
              <td class="actions">
                <button v-if="isLogistics" @click="openEditT(row)"><el-icon><Edit /></el-icon> 编辑</button>
                <button v-else @click="openEditT(row)"><el-icon><Search /></el-icon> 查看</button>
                <button v-if="isLogistics && row.transportStatus === 1" @click="stateAction('depart', row.transportId)"><el-icon><Upload /></el-icon> 发运</button>
                <button v-if="isLogistics && row.transportStatus === 2" @click="stateAction('arrive', row.transportId)"><el-icon><Download /></el-icon> 签收</button>
                <button v-if="isLogistics && row.transportStatus === 4" class="danger" @click="stateAction('close', row.transportId)"><el-icon><Close /></el-icon> 关闭</button>
                <button v-if="isLogistics" class="danger" @click="confirmDeleteT(row.transportId)"><el-icon><Delete /></el-icon> 删除</button>
              </td>
            </tr>
          </tbody></table></div>
      </section>

      <!-- Transport Modal -->
      <div v-if="showTModal" class="trace-modal-backdrop" @click.self="showTModal = false">
        <section class="trace-modal" :style="{ width: isLogistics ? '760px' : '520px' }">
          <header><div><p>{{ isLogistics ? '运输管理' : '运输单号' }}</p><h2>{{ editingT ? (isLogistics ? '编辑运输订单' : '查看运输详情') : (isLogistics ? '创建运输订单' : '新建运输单号') }}</h2></div><button @click="showTModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body">
            <!-- 非物流商：简化表单 -->
            <template v-if="isOtherEnterprise">
              <div class="trace-hint info">💡 您只需填写<strong>运输单号</strong>即可发起运输请求。车辆分配、运输环境等由冷链物流商后续完善。</div>
              <div class="form-section">
                <div class="form-section-title"><span class="section-ico">运</span>运输基本信息</div>
                <div class="grid-form">
                  <label>运输单号 *<input v-model="tForm.orderNo" placeholder="请输入运输单号" /></label>
                  <label>产品名称<input v-model="tForm.productName" placeholder="如：鲜牛奶" /></label>
                  <label>关联生产批次<input v-model="tForm.prodBatchNo" placeholder="关联批次号" /></label>
                  <label>发运地<input v-model="tForm.departureName" placeholder="发货地址" /></label>
                  <label>目的地<input v-model="tForm.destinationName" placeholder="收货地址" /></label>
                  <label>预计到达时间<input v-model="tForm.estimatedArrival" /></label>
                </div>
              </div>
              <div class="trace-hint info" style="background:#fff8ed;border-color:#f5d78b;color:#a45f00;">⚡ 提交后冷链物流商将分配车辆并完善运输环境信息，您可在此<strong>实时查看</strong>运输进度。</div>
            </template>
            <!-- 物流商：完整表单 -->
            <template v-else>
              <div class="form-section">
                <div class="form-section-title"><span class="section-ico">运</span>运输基础信息</div>
                <div class="grid-form">
                  <label>订单号<input v-model="tForm.orderNo" /></label>
                  <label>车牌号 *<input v-model="tForm.plateNo" placeholder="分配冷链车辆" /></label>
                  <label>产品名称 *<input v-model="tForm.productName" /></label>
                  <label>生产批次 *<input v-model="tForm.prodBatchNo" /></label>
                  <label>驾驶员<input v-model="tForm.driverName" /></label>
                  <label>联系电话<input v-model="tForm.driverPhone" /></label>
                  <label>发运地<input v-model="tForm.departureName" /></label>
                  <label>目的地 *<input v-model="tForm.destinationName" /></label>
                  <label>装载温度 (℃)<input v-model="tForm.loadingTemp" /></label>
                  <label>发运时间<input v-model="tForm.departTime" /></label>
                  <label>预计到达<input v-model="tForm.estimatedArrival" /></label>
                  <label>运输方式<select v-model.number="tForm.transportMethod"><option :value="0">请选择</option><option :value="1">公路冷链</option><option :value="2">铁路冷链</option><option :value="3">航空冷链</option><option :value="4">海运冷链</option></select></label>
                </div>
              </div>
              <div class="form-section">
                <div class="form-section-title"><span class="section-ico">环</span>运输环境监测</div>
                <div class="grid-form">
                  <label>采集间隔 (min)<input v-model="tForm.collectInterval" placeholder="如：30" /></label>
                  <label>温度上限 (℃)<input v-model="tForm.tempUpper" placeholder="如：6.0" /></label>
                  <label>温度下限 (℃)<input v-model="tForm.tempLower" placeholder="如：2.0" /></label>
                  <label>湿度上限 (%RH)<input v-model="tForm.humidUpper" placeholder="如：75" /></label>
                  <label>湿度下限 (%RH)<input v-model="tForm.humidLower" placeholder="如：55" /></label>
                  <label>预警推送方式<input v-model="tForm.alertMethod" placeholder="短信+系统消息" /></label>
                </div>
              </div>
            </template>
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:10px">备注<textarea v-model="tForm.remark" :placeholder="isLogistics ? '请输入运输相关说明。' : '请输入补充说明。'" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          </div>
          <footer>
            <button class="secondary" @click="showTModal = false"><el-icon><Close /></el-icon> 取消</button>
            <button v-if="!isOtherEnterprise || !editingT" class="primary" @click="submitT"><el-icon><Check /></el-icon> {{ editingT ? '保存' : '提交' }}</button>
          </footer>
        </section>
      </div>
    </template>

    <!-- ==================== Shipping (发运签收) ==================== -->
    <template v-if="tab === 'shipping'">
      <section class="trace-stats">
        <article class="amber"><span><el-icon><Upload /></el-icon> 待发运</span><b>{{ shippingOrders.filter((r: any) => r.transportStatus === 1).length }}</b><em>等待发运</em></article>
        <article><span><el-icon><Van /></el-icon> 运输中</span><b>{{ shippingOrders.filter((r: any) => r.transportStatus === 2).length }}</b><em>在途运输</em></article>
        <article class="green"><span><el-icon><Check /></el-icon> 已签收</span><b>{{ shippingOrders.filter((r: any) => r.transportStatus === 3).length }}</b><em>已完成</em></article>
      </section>

      <!-- 待发运列表 -->
      <section class="trace-panel list-panel" style="margin-bottom:22px">
        <header class="panel-header">
          <div><p>发运管理</p><h2>待发运 / 运输中 订单</h2></div>
          <span style="color:#8195aa;font-size:13px">({{ shippingOrders.filter((r: any) => r.transportStatus <= 2).length }} 条)</span>
        </header>
        <div v-if="!shippingOrders.filter((r: any) => r.transportStatus <= 2).length" class="trace-empty-state">
          <div class="empty-icon"><el-icon><Check /></el-icon></div>
          <p>暂无待发运或运输中的订单</p>
        </div>
        <div v-else class="trace-row-list" style="padding:0 18px 18px">
          <div v-for="row in shippingOrders.filter((r: any) => r.transportStatus <= 2)" :key="row.transportId" class="trace-row-card">
            <span class="trace-mini-badge" :class="row.transportStatus === 1 ? 'amber' : 'blue'">{{ row.transportStatus === 1 ? '待' : '运' }}</span>
            <div style="flex:1">
              <strong>{{ row.orderNo }}</strong> — {{ row.productName || '未指定产品' }}
              <small style="display:block;color:#96a8b9;font-size:11px">
                车牌：{{ row.plateNo || '待分配' }} · {{ row.departureName || '-' }} → {{ row.destinationName || '-' }}
                · 装载温度：{{ row.loadingTemp ? row.loadingTemp + '℃' : '-' }}
              </small>
            </div>
            <span class="status" :class="row.transportStatus === 1 ? 'status-pending' : 'status-active'">{{ transportStatusLabels[row.transportStatus] || '-' }}</span>
            <button v-if="isLogistics && row.transportStatus === 1" class="primary btn-sm" @click="openDispatch(row)"><el-icon><Upload /></el-icon> 正式发运</button>
            <button v-if="isLogistics && row.transportStatus === 2" class="primary btn-sm btn-success" @click="openReceipt(row)"><el-icon><Download /></el-icon> 签收确认</button>
          </div>
        </div>
      </section>

      <!-- 已签收列表 -->
      <section class="trace-panel list-panel">
        <header class="panel-header">
          <div><p>签收记录</p><h2>已完成签收的订单</h2></div>
          <span style="color:#8195aa;font-size:13px">({{ shippingOrders.filter((r: any) => r.transportStatus === 3).length }} 条)</span>
        </header>
        <div v-if="!shippingOrders.filter((r: any) => r.transportStatus === 3).length" class="trace-empty-state" style="margin:18px">
          <div class="empty-icon"><el-icon><Download /></el-icon></div>
          <p>暂无已签收订单</p>
        </div>
        <div v-else class="trace-row-list" style="padding:0 18px 18px">
          <div v-for="row in shippingOrders.filter((r: any) => r.transportStatus === 3)" :key="row.transportId" class="trace-row-card">
            <span class="trace-mini-badge green">✓</span>
            <div style="flex:1">
              <strong>{{ row.orderNo }}</strong> — {{ row.productName || '未指定产品' }}
              <small style="display:block;color:#96a8b9;font-size:11px">车牌：{{ row.plateNo || '-' }} · {{ row.departureName || '-' }} → {{ row.destinationName || '-' }}</small>
            </div>
            <span class="status status-active">已签收</span>
          </div>
        </div>
      </section>

      <!-- Dispatch Modal -->
      <div v-if="showDispatchModal" class="trace-modal-backdrop" @click.self="showDispatchModal = false">
        <section class="trace-modal" style="width:620px">
          <header><div><p>发运管理</p><h2>正式发运 — {{ dispatchTarget?.orderNo }}</h2></div><button @click="showDispatchModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body">
            <div class="trace-hint info">📋 确认以下信息无误后执行发运。发运后运输状态将变更为<strong>"运输中"</strong>。</div>
            <div class="detail-grid" style="margin-bottom:16px">
              <div><span>运输单号</span><b>{{ dispatchTarget?.orderNo }}</b></div>
              <div><span>车牌号</span><b>{{ dispatchTarget?.plateNo || '待分配' }}</b></div>
              <div><span>产品</span><b>{{ dispatchTarget?.productName || '-' }}</b></div>
              <div><span>发运地 → 目的地</span><b>{{ dispatchTarget?.departureName || '-' }} → {{ dispatchTarget?.destinationName || '-' }}</b></div>
            </div>
            <div class="form-section">
              <div class="form-section-title"><span class="section-ico">发</span>发运核查</div>
              <div class="grid-form">
                <label>发运时间 *<input v-model="dispatchForm.departTime" type="datetime-local" /></label>
                <label>装载温度 (℃)<input v-model="dispatchForm.loadingTemp" placeholder="如：3.5" /></label>
                <label>发运核查人 *<input v-model="dispatchForm.checkerName" placeholder="核查人姓名" /></label>
                <label>包装完整性<select v-model="dispatchForm.packageIntact"><option>完好</option><option>破损</option><option>部分破损</option></select></label>
                <label>铅封号<input v-model="dispatchForm.sealNo" placeholder="铅封编号" /></label>
              </div>
            </div>
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:15px">发运备注<textarea v-model="dispatchForm.remark" placeholder="发运核查补充说明。" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          </div>
          <footer>
            <button class="secondary" @click="showDispatchModal = false"><el-icon><Close /></el-icon> 取消</button>
            <button class="primary" @click="submitDispatch"><el-icon><Upload /></el-icon> 确认发运</button>
          </footer>
        </section>
      </div>

      <!-- Receipt Modal -->
      <div v-if="showReceiptModal" class="trace-modal-backdrop" @click.self="showReceiptModal = false">
        <section class="trace-modal" style="width:620px">
          <header><div><p>签收管理</p><h2>电子签收 — {{ receiptTarget?.orderNo }}</h2></div><button @click="showReceiptModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body">
            <div class="trace-hint success">✓ 请确认货物完好、数量一致后进行签收。签收后订单状态将变更为<strong>"已签收"</strong>。</div>
            <div class="detail-grid" style="margin-bottom:16px">
              <div><span>运输单号</span><b>{{ receiptTarget?.orderNo }}</b></div>
              <div><span>车牌号</span><b>{{ receiptTarget?.plateNo || '-' }}</b></div>
              <div><span>产品</span><b>{{ receiptTarget?.productName || '-' }}</b></div>
              <div><span>发运地 → 目的地</span><b>{{ receiptTarget?.departureName || '-' }} → {{ receiptTarget?.destinationName || '-' }}</b></div>
            </div>
            <div class="form-section">
              <div class="form-section-title"><span class="section-ico">签</span>签收确认</div>
              <div class="grid-form">
                <label>到达时间 *<input v-model="receiptForm.arriveTime" type="datetime-local" /></label>
                <label>到达温度 (℃)<input v-model="receiptForm.arriveTemp" placeholder="如：4.1" /></label>
                <label>签收人 *<input v-model="receiptForm.receiverName" placeholder="签收人姓名" /></label>
                <label>联系电话<input v-model="receiptForm.receiverPhone" placeholder="签收人电话" /></label>
                <label>包装完整性<select v-model="receiptForm.packageIntact"><option>完好</option><option>破损</option><option>部分破损</option></select></label>
                <label>数量核对<select v-model="receiptForm.quantityCheck"><option>一致</option><option>短少</option><option>超出</option></select></label>
              </div>
            </div>
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:15px">签收备注<textarea v-model="receiptForm.remark" placeholder="签收补充说明。" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          </div>
          <footer>
            <button class="secondary" @click="showReceiptModal = false"><el-icon><Close /></el-icon> 取消</button>
            <button class="primary btn-success" @click="submitReceipt"><el-icon><Download /></el-icon> 确认签收</button>
          </footer>
        </section>
      </div>
    </template>

    <!-- ==================== Vehicle (仅物流商) ==================== -->
    <template v-if="tab === 'vehicle' && isLogistics">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>状态<select v-model="vFilters.vehicleStatus"><option value="">全部</option><option value="1">空闲</option><option value="2">运输中</option><option value="3">维修中</option></select></label>
          <label>物流企业<input v-model="vFilters.ownerName" @keyup.enter="loadVehicles" /></label>
          <label>冷链类型<input v-model="vFilters.coldType" @keyup.enter="loadVehicles" /></label>
          <div class="filter-actions"><button class="primary" @click="loadVehicles"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>车辆台账</p><h2>冷链车辆管理</h2></div><button class="primary create" @click="openCreateV"><el-icon><Plus /></el-icon> 注册车辆</button></header>
        <div class="table-wrap"><table><thead><tr><th>车牌号</th><th>车型</th><th>驾驶员</th><th>电话</th><th>企业</th><th>冷链类型</th><th>温控</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingV"><td colspan="9" class="empty">加载中...</td></tr><tr v-else-if="!vehicles.length"><td colspan="9" class="empty">暂无冷链车辆</td></tr>
            <tr v-for="row in vehicles" :key="row.vehicleId"><td><strong>{{ row.plateNo }}</strong></td><td>{{ row.vehicleModel || '-' }}</td><td>{{ row.driverName }}</td><td>{{ row.driverPhone }}</td><td>{{ row.ownerName }}</td><td>{{ row.coldType }}</td><td>{{ row.tempRange || '-' }}</td>
              <td><span class="status" :class="statusClass(vehicleStatusLabels[row.vehicleStatus] || '')">{{ vehicleStatusLabels[row.vehicleStatus] || '-' }}</span></td>
              <td class="actions"><button @click="openEditV(row)"><el-icon><Edit /></el-icon> 编辑</button><button class="danger" @click="confirmDeleteV(row.vehicleId)"><el-icon><Delete /></el-icon> 删除</button></td></tr></tbody></table></div>
      </section>
      <div v-if="showVModal" class="trace-modal-backdrop" @click.self="showVModal = false">
        <section class="trace-modal"><header><div><p>车辆管理</p><h2>{{ editingV ? '编辑车辆' : '注册冷链车辆' }}</h2></div><button @click="showVModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body grid-form">
            <label>车牌号 *<input v-model="vForm.plateNo" /></label><label>车型<input v-model="vForm.vehicleModel" /></label>
            <label>驾驶员<input v-model="vForm.driverName" /></label><label>联系电话<input v-model="vForm.driverPhone" /></label>
            <label>所属企业<input v-model="vForm.ownerName" /></label><label>冷链类型<input v-model="vForm.coldType" placeholder="冷藏/冷冻/恒温" /></label>
            <label>温控范围<input v-model="vForm.tempRange" placeholder="-18℃ ~ -10℃" /></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="vForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          <footer><button class="secondary" @click="showVModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitV"><el-icon><Plus /></el-icon> {{ editingV ? '保存' : '注册' }}</button></footer>
        </section>
      </div>
    </template>

    <!-- ==================== Warehouse (仅物流商) ==================== -->
    <template v-if="tab === 'warehouse' && isLogistics">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>类型<select v-model="wFilters.warehouseType"><option value="">全部</option><option value="1">原料仓</option><option value="2">成品仓</option><option value="3">冷链仓</option><option value="4">中转仓</option></select></label>
          <label>状态<select v-model="wFilters.warehouseStatus"><option value="">全部</option><option value="1">启用</option><option value="2">停用</option></select></label>
          <div class="filter-actions"><button class="primary" @click="loadWarehouses"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>仓储台账</p><h2>仓库列表</h2></div><button class="primary create" @click="openCreateW"><el-icon><Plus /></el-icon> 新增仓库</button></header>
        <div class="table-wrap"><table><thead><tr><th>仓库名称</th><th>类型</th><th>地址</th><th>容量</th><th>管理员</th><th>温控</th><th>湿度</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingW"><td colspan="9" class="empty">加载中...</td></tr><tr v-else-if="!warehouses.length"><td colspan="9" class="empty">暂无仓库</td></tr>
            <tr v-for="row in warehouses" :key="row.warehouseId"><td><strong>{{ row.warehouseName }}</strong></td><td>{{ warehouseTypeLabels[row.warehouseType] || '-' }}</td><td>{{ row.address }}</td><td>{{ row.capacity }}</td><td>{{ row.manager }}</td><td>{{ row.tempRange || '-' }}</td><td>{{ row.humidityRange || '-' }}</td>
              <td><span class="status" :class="row.warehouseStatus === 1 ? 'status-active' : 'status-disabled'">{{ warehouseStatusLabels[row.warehouseStatus] || '-' }}</span></td>
              <td class="actions"><button @click="openEditW(row)"><el-icon><Edit /></el-icon> 编辑</button><button class="danger" @click="confirmDeleteW(row.warehouseId)"><el-icon><Delete /></el-icon> 删除</button></td></tr></tbody></table></div>
      </section>
      <div v-if="showWModal" class="trace-modal-backdrop" @click.self="showWModal = false">
        <section class="trace-modal"><header><div><p>仓储管理</p><h2>{{ editingW ? '编辑仓库' : '新增仓库' }}</h2></div><button @click="showWModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body grid-form">
            <label>仓库名称 *<input v-model="wForm.warehouseName" /></label><label>类型<select v-model.number="wForm.warehouseType"><option :value="0">请选择</option><option :value="1">原料仓</option><option :value="2">成品仓</option><option :value="3">冷链仓</option><option :value="4">中转仓</option></select></label>
            <label>地址<input v-model="wForm.address" /></label><label>容量<input v-model="wForm.capacity" /></label>
            <label>管理员<input v-model="wForm.manager" /></label><label>温控范围<input v-model="wForm.tempRange" placeholder="-5℃ ~ 15℃" /></label>
            <label>湿度范围<input v-model="wForm.humidityRange" /></label><label>状态<select v-model.number="wForm.warehouseStatus"><option :value="0">请选择</option><option :value="1">启用</option><option :value="2">停用</option></select></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="wForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          <footer><button class="secondary" @click="showWModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitW"><el-icon><Plus /></el-icon> {{ editingW ? '保存' : '创建' }}</button></footer>
        </section>
      </div>
    </template>

    <!-- Delete Confirm -->
    <div v-if="showTConfirm || showVConfirm || showWConfirm" class="trace-confirm-overlay" @click.self="showTConfirm = showVConfirm = showWConfirm = false">
      <div class="trace-confirm-box"><h3>确认删除</h3><p>确定要删除该{{ showTConfirm ? '运输订单' : showVConfirm ? '冷链车辆' : '仓库' }}吗？</p><div class="trace-confirm-actions"><button class="secondary" @click="showTConfirm = showVConfirm = showWConfirm = false"><el-icon><Close /></el-icon> 取消</button><button class="primary danger-fill" @click="showTConfirm ? doDeleteT() : showVConfirm ? doDeleteV() : doDeleteW()"><el-icon><Delete /></el-icon> 确认删除</button></div></div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

.form-section {
  margin-bottom: 18px;
}

.form-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 2px solid #eaf2ff;
  font-size: 14px;
  font-weight: 800;
  color: #2467df;
}

.section-ico {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: inline-grid;
  place-items: center;
  background: #eaf2ff;
  color: #2467df;
  font-size: 12px;
  font-weight: 900;
  flex-shrink: 0;
}
</style>