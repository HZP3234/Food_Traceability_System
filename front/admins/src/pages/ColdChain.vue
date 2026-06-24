<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Box, Close, Delete, Download, Edit, Plus, Search, Van } from '@element-plus/icons-vue'
import { coldChainApi } from '../services/api'

const tab = ref<'transport' | 'vehicle' | 'warehouse'>('transport')
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)

// Transport
const loadingT = ref(false); const transports = ref<any[]>([])
const tFilters = ref({ transportStatus: '', prodBatchNo: '', plateNo: '', transportMethod: '' })
const showTModal = ref(false); const editingT = ref<any>(null); const showTConfirm = ref(false); const deletingTId = ref<number | null>(null)
const tForm = ref({ orderNo: '', plateNo: '', driverName: '', driverPhone: '', productName: '', prodBatchNo: '', departureName: '', destinationName: '', loadingTemp: '', departTime: '', estimatedArrival: '', transportMethod: 0, collectInterval: '', tempUpper: '', tempLower: '', humidUpper: '', humidLower: '', alertMethod: '', remark: '' })
const transportStatusLabels = ['', '待发运', '运输中', '已签收', '温度预警', '异常关闭']

// Vehicle
const loadingV = ref(false); const vehicles = ref<any[]>([])
const vFilters = ref({ vehicleStatus: '', ownerName: '', coldType: '' })
const showVModal = ref(false); const editingV = ref<any>(null); const showVConfirm = ref(false); const deletingVId = ref<number | null>(null)
const vForm = ref({ plateNo: '', vehicleModel: '', driverName: '', driverPhone: '', ownerName: '', coldType: '', tempRange: '', remark: '' })
const vehicleStatusLabels = ['', '空闲', '运输中', '维修中']

// Warehouse
const loadingW = ref(false); const warehouses = ref<any[]>([])
const wFilters = ref({ warehouseType: '', warehouseStatus: '' })
const showWModal = ref(false); const editingW = ref<any>(null); const showWConfirm = ref(false); const deletingWId = ref<number | null>(null)
const wForm = ref({ warehouseName: '', warehouseType: 0, address: '', capacity: '', manager: '', tempRange: '', humidityRange: '', warehouseStatus: 0, remark: '' })
const warehouseTypeLabels = ['', '原料仓', '成品仓', '冷链仓', '中转仓']
const warehouseStatusLabels = ['', '启用', '停用']

const tStats = computed(() => ({
  total: transports.value.length,
  inTransit: transports.value.filter((r: any) => r.transportStatus === 2).length,
  alert: transports.value.filter((r: any) => r.transportStatus === 4).length,
  arrived: transports.value.filter((r: any) => r.transportStatus === 3).length,
}))

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function statusClass(s: string) {
  if (['已签收', '空闲', '启用', '正常'].some(x => s.includes(x))) return 'status-active'
  if (['运输中', '待发运'].some(x => s.includes(x))) return 'status-pending'
  if (['温度预警', '维修中', '异常'].some(x => s.includes(x))) return 'status-void'
  return 'status-active'
}

// Transport CRUD
async function loadTransports() { loadingT.value = true; try { const p: Record<string, any> = {}; if (tFilters.value.transportStatus) p.transportStatus = Number(tFilters.value.transportStatus); if (tFilters.value.prodBatchNo) p.prodBatchNo = tFilters.value.prodBatchNo; if (tFilters.value.plateNo) p.plateNo = tFilters.value.plateNo; if (tFilters.value.transportMethod) p.transportMethod = Number(tFilters.value.transportMethod); const data = await coldChainApi.listTransport(p); transports.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载运输订单失败: ' + e.message) } finally { loadingT.value = false } }
function openCreateT() { editingT.value = null; tForm.value = { orderNo: '', plateNo: '', driverName: '', driverPhone: '', productName: '', prodBatchNo: '', departureName: '', destinationName: '', loadingTemp: '', departTime: '', estimatedArrival: '', transportMethod: 0, collectInterval: '', tempUpper: '', tempLower: '', humidUpper: '', humidLower: '', alertMethod: '', remark: '' }; showTModal.value = true }
function openEditT(row: any) { editingT.value = row; tForm.value = { orderNo: row.orderNo ?? '', plateNo: row.plateNo ?? '', driverName: row.driverName ?? '', driverPhone: row.driverPhone ?? '', productName: row.productName ?? '', prodBatchNo: row.prodBatchNo ?? '', departureName: row.departureName ?? '', destinationName: row.destinationName ?? '', loadingTemp: row.loadingTemp ?? '', departTime: row.departTime ?? '', estimatedArrival: row.estimatedArrival ?? '', transportMethod: row.transportMethod ?? 0, collectInterval: row.collectInterval ?? '', tempUpper: row.tempUpper ?? '', tempLower: row.tempLower ?? '', humidUpper: row.humidUpper ?? '', humidLower: row.humidLower ?? '', alertMethod: row.alertMethod ?? '', remark: row.remark ?? '' }; showTModal.value = true }
async function submitT() { try { const data: Record<string, any> = { ...tForm.value }; if (editingT.value) { data.transportId = editingT.value.transportId; await coldChainApi.updateTransport(data); notify('success', '运输订单更新成功') } else { await coldChainApi.createTransport(data); notify('success', '运输订单创建成功') } showTModal.value = false; loadTransports() } catch (e: any) { notify('error', '操作失败: ' + e.message) } }
function confirmDeleteT(id: number) { deletingTId.value = id; showTConfirm.value = true }
async function doDeleteT() { try { await coldChainApi.deleteTransport(deletingTId.value!); notify('success', '订单删除成功'); showTConfirm.value = false; loadTransports() } catch (e: any) { notify('error', '删除失败: ' + e.message) } }
async function stateAction(action: string, id: number) {
  const labels: Record<string, string> = { depart: '发运', arrive: '签收', alert: '预警', close: '关闭' }
  const actionLabel = labels[action] || action
  if (!confirm(`确认执行"${actionLabel}"操作？`)) return
  try { if (action === 'depart') await coldChainApi.departTransport(id); else if (action === 'arrive') await coldChainApi.arriveTransport(id); else if (action === 'alert') await coldChainApi.alertTransport(id); else if (action === 'close') await coldChainApi.closeTransport(id); notify('success', '操作成功'); loadTransports() } catch (e: any) { notify('error', '操作失败: ' + e.message) }
}

// Vehicle CRUD
async function loadVehicles() { loadingV.value = true; try { const p: Record<string, any> = {}; if (vFilters.value.vehicleStatus) p.vehicleStatus = Number(vFilters.value.vehicleStatus); if (vFilters.value.ownerName) p.ownerName = vFilters.value.ownerName; if (vFilters.value.coldType) p.coldType = vFilters.value.coldType; const data = await coldChainApi.listVehicle(p); vehicles.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载车辆失败') } finally { loadingV.value = false } }
function openCreateV() { editingV.value = null; vForm.value = { plateNo: '', vehicleModel: '', driverName: '', driverPhone: '', ownerName: '', coldType: '', tempRange: '', remark: '' }; showVModal.value = true }
function openEditV(row: any) { editingV.value = row; vForm.value = { plateNo: row.plateNo ?? '', vehicleModel: row.vehicleModel ?? '', driverName: row.driverName ?? '', driverPhone: row.driverPhone ?? '', ownerName: row.ownerName ?? '', coldType: row.coldType ?? '', tempRange: row.tempRange ?? '', remark: row.remark ?? '' }; showVModal.value = true }
async function submitV() { try { const data: Record<string, any> = { ...vForm.value }; if (editingV.value) { data.vehicleId = editingV.value.vehicleId; await coldChainApi.updateVehicle(data); notify('success', '车辆信息更新成功') } else { await coldChainApi.createVehicle(data); notify('success', '冷链车辆注册成功') } showVModal.value = false; loadVehicles() } catch (e: any) { notify('error', '操作失败') } }
function confirmDeleteV(id: number) { deletingVId.value = id; showVConfirm.value = true }
async function doDeleteV() { try { await coldChainApi.deleteVehicle(deletingVId.value!); notify('success', '车辆删除成功'); showVConfirm.value = false; loadVehicles() } catch (e: any) { notify('error', '删除失败') } }

// Warehouse CRUD
async function loadWarehouses() { loadingW.value = true; try { const p: Record<string, any> = {}; if (wFilters.value.warehouseType) p.warehouseType = Number(wFilters.value.warehouseType); if (wFilters.value.warehouseStatus) p.warehouseStatus = Number(wFilters.value.warehouseStatus); const data = await coldChainApi.listWarehouse(p); warehouses.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载仓库失败') } finally { loadingW.value = false } }
function openCreateW() { editingW.value = null; wForm.value = { warehouseName: '', warehouseType: 0, address: '', capacity: '', manager: '', tempRange: '', humidityRange: '', warehouseStatus: 0, remark: '' }; showWModal.value = true }
function openEditW(row: any) { editingW.value = row; wForm.value = { warehouseName: row.warehouseName ?? '', warehouseType: row.warehouseType ?? 0, address: row.address ?? '', capacity: row.capacity ?? '', manager: row.manager ?? '', tempRange: row.tempRange ?? '', humidityRange: row.humidityRange ?? '', warehouseStatus: row.warehouseStatus ?? 0, remark: row.remark ?? '' }; showWModal.value = true }
async function submitW() { try { const data: Record<string, any> = { ...wForm.value }; if (editingW.value) { data.warehouseId = editingW.value.warehouseId; await coldChainApi.updateWarehouse(data); notify('success', '仓库更新成功') } else { await coldChainApi.createWarehouse(data); notify('success', '仓库创建成功') } showWModal.value = false; loadWarehouses() } catch (e: any) { notify('error', '操作失败') } }
function confirmDeleteW(id: number) { deletingWId.value = id; showWConfirm.value = true }
async function doDeleteW() { try { await coldChainApi.deleteWarehouse(deletingWId.value!); notify('success', '仓库删除成功'); showWConfirm.value = false; loadWarehouses() } catch (e: any) { notify('error', '删除失败') } }

function switchTab(t: 'transport' | 'vehicle' | 'warehouse') { tab.value = t; if (t === 'transport') loadTransports(); else if (t === 'vehicle') loadVehicles(); else loadWarehouses() }
onMounted(loadTransports)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <section v-if="tab === 'transport'" class="trace-stats">
      <article><span><el-icon><Van /></el-icon> 运输订单总数</span><b>{{ tStats.total }}</b><em>个订单</em></article>
      <article class="amber"><span><el-icon><Van /></el-icon> 运输中</span><b>{{ tStats.inTransit }}</b><em>正在运输</em></article>
      <article v-if="tStats.alert > 0" class="red"><span><el-icon><Close /></el-icon> 温度预警</span><b>{{ tStats.alert }}</b><em>需要处理</em></article>
      <article class="green"><span><el-icon><Download /></el-icon> 已签收</span><b>{{ tStats.arrived }}</b><em>已确认</em></article>
    </section>

    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'transport' }" @click="switchTab('transport')"><el-icon><Van /></el-icon> 运输订单</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'vehicle' }" @click="switchTab('vehicle')"><el-icon><Van /></el-icon> 冷链车辆</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'warehouse' }" @click="switchTab('warehouse')"><el-icon><Box /></el-icon> 仓库管理</button>
    </div>

    <!-- Transport -->
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
        <header class="panel-header"><div><p>运输台账</p><h2>运输订单</h2></div><button class="primary create" @click="openCreateT"><el-icon><Plus /></el-icon> 创建运输订单</button></header>
        <div class="table-wrap"><table><thead><tr><th>订单号</th><th>车牌</th><th>产品</th><th>生产批次</th><th>发运地</th><th>目的地</th><th>状态</th><th>发运时间</th><th>预计到达</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingT"><td colspan="10" class="empty">加载中...</td></tr><tr v-else-if="!transports.length"><td colspan="10" class="empty">暂无运输订单</td></tr>
            <tr v-for="row in transports" :key="row.transportId"><td><code>{{ row.orderNo }}</code></td><td>{{ row.plateNo }}</td><td>{{ row.productName }}</td><td><code>{{ row.prodBatchNo }}</code></td><td>{{ row.departureName }}</td><td>{{ row.destinationName }}</td>
              <td><span class="status" :class="statusClass(transportStatusLabels[row.transportStatus] || '')">{{ transportStatusLabels[row.transportStatus] || '-' }}</span></td><td>{{ row.departTime || '-' }}</td><td>{{ row.estimatedArrival || '-' }}</td>
              <td class="actions">
                <button @click="openEditT(row)"><el-icon><Edit /></el-icon> 编辑</button>
                <button v-if="row.transportStatus === 1" @click="stateAction('depart', row.transportId)"><el-icon><Van /></el-icon> 发运</button>
                <button v-if="row.transportStatus === 2" @click="stateAction('arrive', row.transportId)"><el-icon><Download /></el-icon> 签收</button>
                <button v-if="row.transportStatus === 2" @click="stateAction('alert', row.transportId)"><el-icon><Close /></el-icon> 预警</button>
                <button v-if="row.transportStatus === 4" class="danger" @click="stateAction('close', row.transportId)"><el-icon><Close /></el-icon> 关闭</button>
                <button class="danger" @click="confirmDeleteT(row.transportId)"><el-icon><Delete /></el-icon> 删除</button>
              </td></tr></tbody></table></div>
      </section>

      <div v-if="showTModal" class="trace-modal-backdrop" @click.self="showTModal = false">
        <section class="trace-modal" style="width:720px"><header><div><p>运输管理</p><h2>{{ editingT ? '编辑运输订单' : '创建运输订单' }}</h2></div><button @click="showTModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body grid-form">
            <label>订单号<input v-model="tForm.orderNo" /></label>
            <label>车牌号 *<input v-model="tForm.plateNo" /></label>
            <label>产品名称 *<input v-model="tForm.productName" /></label>
            <label>生产批次 *<input v-model="tForm.prodBatchNo" /></label>
            <label>驾驶员<input v-model="tForm.driverName" /></label>
            <label>电话<input v-model="tForm.driverPhone" /></label>
            <label>发运地<input v-model="tForm.departureName" /></label>
            <label>目的地 *<input v-model="tForm.destinationName" /></label>
            <label>装载温度 (℃)<input v-model="tForm.loadingTemp" /></label>
            <label>预计到达<input v-model="tForm.estimatedArrival" /></label>
            <label>运输方式<select v-model.number="tForm.transportMethod"><option :value="0">请选择</option><option :value="1">公路</option><option :value="2">铁路</option><option :value="3">航空</option><option :value="4">海运</option></select></label>
            <label>采集间隔<input v-model="tForm.collectInterval" /></label>
            <label>温度上限 (℃)<input v-model="tForm.tempUpper" /></label>
            <label>温度下限 (℃)<input v-model="tForm.tempLower" /></label>
            <label>湿度上限 (%RH)<input v-model="tForm.humidUpper" /></label>
            <label>湿度下限 (%RH)<input v-model="tForm.humidLower" /></label>
            <label>预警方式<input v-model="tForm.alertMethod" /></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="tForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          <footer><button class="secondary" @click="showTModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitT"><el-icon><Plus /></el-icon> {{ editingT ? '保存' : '创建' }}</button></footer>
        </section>
      </div>
    </template>

    <!-- Vehicle -->
    <template v-if="tab === 'vehicle'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>状态<select v-model="vFilters.vehicleStatus"><option value="">全部</option><option value="1">空闲</option><option value="2">运输中</option><option value="3">维修中</option></select></label>
          <label>物流企业<input v-model="vFilters.ownerName" @keyup.enter="loadVehicles" /></label>
          <label>冷链类型<input v-model="vFilters.coldType" @keyup.enter="loadVehicles" /></label>
          <div class="filter-actions"><button class="primary" @click="loadVehicles"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>车辆台账</p><h2>冷链车辆</h2></div><button class="primary create" @click="openCreateV"><el-icon><Plus /></el-icon> 注册车辆</button></header>
        <div class="table-wrap"><table><thead><tr><th>车牌号</th><th>车型</th><th>驾驶员</th><th>电话</th><th>企业</th><th>冷链类型</th><th>温控</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-if="loadingV"><td colspan="9" class="empty">加载中...</td></tr><tr v-else-if="!vehicles.length"><td colspan="9" class="empty">暂无冷链车辆</td></tr>
            <tr v-for="row in vehicles" :key="row.vehicleId"><td><strong>{{ row.plateNo }}</strong></td><td>{{ row.vehicleModel || '-' }}</td><td>{{ row.driverName }}</td><td>{{ row.driverPhone }}</td><td>{{ row.ownerName }}</td><td>{{ row.coldType }}</td><td>{{ row.tempRange || '-' }}</td>
              <td><span class="status" :class="statusClass(vehicleStatusLabels[row.vehicleStatus] || '')">{{ vehicleStatusLabels[row.vehicleStatus] || '-' }}</span></td>
              <td class="actions"><button @click="openEditV(row)"><el-icon><Edit /></el-icon> 编辑</button><button class="danger" @click="confirmDeleteV(row.vehicleId)"><el-icon><Delete /></el-icon> 删除</button></td></tr></tbody></table></div>
      </section>

      <div v-if="showVModal" class="trace-modal-backdrop" @click.self="showVModal = false">
        <section class="trace-modal"><header><div><p>车辆管理</p><h2>{{ editingV ? '编辑车辆' : '注册冷链车辆' }}</h2></div><button @click="showVModal = false"><el-icon><Close /></el-icon></button></header>
          <div class="modal-body grid-form">
            <label>车牌号 *<input v-model="vForm.plateNo" /></label>
            <label>车型<input v-model="vForm.vehicleModel" /></label>
            <label>驾驶员<input v-model="vForm.driverName" /></label>
            <label>联系电话<input v-model="vForm.driverPhone" /></label>
            <label>所属企业<input v-model="vForm.ownerName" /></label>
            <label>冷链类型<input v-model="vForm.coldType" placeholder="冷藏/冷冻/恒温" /></label>
            <label>温控范围<input v-model="vForm.tempRange" placeholder="-18℃ ~ -10℃" /></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="vForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
          <footer><button class="secondary" @click="showVModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitV"><el-icon><Plus /></el-icon> {{ editingV ? '保存' : '注册' }}</button></footer>
        </section>
      </div>
    </template>

    <!-- Warehouse -->
    <template v-if="tab === 'warehouse'">
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
            <label>仓库名称 *<input v-model="wForm.warehouseName" /></label>
            <label>类型<select v-model.number="wForm.warehouseType"><option :value="0">请选择</option><option :value="1">原料仓</option><option :value="2">成品仓</option><option :value="3">冷链仓</option><option :value="4">中转仓</option></select></label>
            <label>地址<input v-model="wForm.address" /></label>
            <label>容量<input v-model="wForm.capacity" /></label>
            <label>管理员<input v-model="wForm.manager" /></label>
            <label>温控范围<input v-model="wForm.tempRange" placeholder="-5℃ ~ 15℃" /></label>
            <label>湿度范围<input v-model="wForm.humidityRange" /></label>
            <label>状态<select v-model.number="wForm.warehouseStatus"><option :value="0">请选择</option><option :value="1">启用</option><option :value="2">停用</option></select></label>
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
</style>
