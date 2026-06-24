<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { coldChainApi } from '../services/api'

const tab = ref<'transport' | 'vehicle' | 'warehouse'>('transport')
const toast = ref<{ type: string; msg: string } | null>(null)

// Transport
const loadingT = ref(false); const transports = ref<any[]>([])
const tFilters = ref({ transportStatus: '', prodBatchNo: '', plateNo: '', transportMethod: '' })
const showTModal = ref(false); const editingT = ref<any>(null); const showTConfirm = ref(false); const deletingTId = ref<number | null>(null)
const tForm = ref({ orderNo: '', plateNo: '', driverName: '', driverPhone: '', productName: '', prodBatchNo: '', departureName: '', destinationName: '', loadingTemp: '', departTime: '', estimatedArrival: '', transportMethod: 0, collectInterval: '', tempUpper: '', tempLower: '', humidUpper: '', humidLower: '', alertMethod: '', remark: '' })
const transportStatusLabels = ['', '待发运', '运输中', '已签收', '温度预警', '异常关闭']
const transportMethodLabels = ['', '公路', '铁路', '航空', '海运']

// Vehicle
const loadingV = ref(false); const vehicles = ref<any[]>([])
const vFilters = ref({ vehicleStatus: '', ownerName: '', coldType: '' })
const showVModal = ref(false); const editingV = ref<any>(null)
const vForm = ref({ plateNo: '', vehicleModel: '', driverName: '', driverPhone: '', ownerName: '', coldType: '', tempRange: '', remark: '' })
const vehicleStatusLabels = ['', '空闲', '运输中', '维修中']

// Warehouse
const loadingW = ref(false); const warehouses = ref<any[]>([])
const wFilters = ref({ warehouseType: '', warehouseStatus: '' })
const showWModal = ref(false); const editingW = ref<any>(null)
const wForm = ref({ warehouseName: '', warehouseType: 0, address: '', capacity: '', manager: '', tempRange: '', humidityRange: '', warehouseStatus: 0, remark: '' })
const warehouseTypeLabels = ['', '原料仓', '成品仓', '冷链仓', '中转仓']
const warehouseStatusLabels = ['', '启用', '停用']

// Stats (对齐 app.js logistics datasets)
const tStats = computed(() => ({
  total: transports.value.length,
  inTransit: transports.value.filter((r: any) => r.transportStatus === 2).length,
  alert: transports.value.filter((r: any) => r.transportStatus === 4).length,
  arrived: transports.value.filter((r: any) => r.transportStatus === 3).length,
}))

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(s: string) {
  if (['已签收', '空闲', '启用', '正常'].some(x => s.includes(x))) return 'tag-success'
  if (['运输中', '待发运'].some(x => s.includes(x))) return 'tag-info'
  if (['温度预警', '维修中', '异常'].some(x => s.includes(x))) return 'tag-danger'
  return 'tag-neutral'
}

// Transport CRUD
async function loadTransports() {
  loadingT.value = true
  try { const p: Record<string, any> = {}; if (tFilters.value.transportStatus) p.transportStatus = Number(tFilters.value.transportStatus); if (tFilters.value.prodBatchNo) p.prodBatchNo = tFilters.value.prodBatchNo; if (tFilters.value.plateNo) p.plateNo = tFilters.value.plateNo; if (tFilters.value.transportMethod) p.transportMethod = Number(tFilters.value.transportMethod); const data = await coldChainApi.listTransport(p); transports.value = Array.isArray(data) ? data : [] }
  catch (e: any) { flash('error', '加载运输订单失败: ' + e.message) }
  finally { loadingT.value = false }
}
function openCreateT() { editingT.value = null; tForm.value = { orderNo: '', plateNo: '', driverName: '', driverPhone: '', productName: '', prodBatchNo: '', departureName: '', destinationName: '', loadingTemp: '', departTime: '', estimatedArrival: '', transportMethod: 0, collectInterval: '', tempUpper: '', tempLower: '', humidUpper: '', humidLower: '', alertMethod: '', remark: '' }; showTModal.value = true }
function openEditT(row: any) { editingT.value = row; tForm.value = { orderNo: row.orderNo ?? '', plateNo: row.plateNo ?? '', driverName: row.driverName ?? '', driverPhone: row.driverPhone ?? '', productName: row.productName ?? '', prodBatchNo: row.prodBatchNo ?? '', departureName: row.departureName ?? '', destinationName: row.destinationName ?? '', loadingTemp: row.loadingTemp ?? '', departTime: row.departTime ?? '', estimatedArrival: row.estimatedArrival ?? '', transportMethod: row.transportMethod ?? 0, collectInterval: row.collectInterval ?? '', tempUpper: row.tempUpper ?? '', tempLower: row.tempLower ?? '', humidUpper: row.humidUpper ?? '', humidLower: row.humidLower ?? '', alertMethod: row.alertMethod ?? '', remark: row.remark ?? '' }; showTModal.value = true }
async function submitT() { try { const data: Record<string, any> = { ...tForm.value }; if (editingT.value) { data.transportId = editingT.value.transportId; await coldChainApi.updateTransport(data); flash('success', '运输订单更新成功') } else { await coldChainApi.createTransport(data); flash('success', '运输订单创建成功') } showTModal.value = false; loadTransports() } catch (e: any) { flash('error', '操作失败: ' + e.message) } }
function confirmDeleteT(id: number) { deletingTId.value = id; showTConfirm.value = true }
async function doDeleteT() { try { await coldChainApi.deleteTransport(deletingTId.value!); flash('success', '订单删除成功'); showTConfirm.value = false; loadTransports() } catch (e: any) { flash('error', '删除失败: ' + e.message) } }
async function stateAction(action: string, id: number) { try { if (action === 'depart') await coldChainApi.departTransport(id); else if (action === 'arrive') await coldChainApi.arriveTransport(id); else if (action === 'alert') await coldChainApi.alertTransport(id); else if (action === 'close') await coldChainApi.closeTransport(id); flash('success', '操作成功'); loadTransports() } catch (e: any) { flash('error', '操作失败: ' + e.message) } }

// Vehicle CRUD
async function loadVehicles() { loadingV.value = true; try { const p: Record<string, any> = {}; if (vFilters.value.vehicleStatus) p.vehicleStatus = Number(vFilters.value.vehicleStatus); if (vFilters.value.ownerName) p.ownerName = vFilters.value.ownerName; if (vFilters.value.coldType) p.coldType = vFilters.value.coldType; const data = await coldChainApi.listVehicle(p); vehicles.value = Array.isArray(data) ? data : [] } catch (e: any) { flash('error', '加载车辆失败') } finally { loadingV.value = false } }
function openCreateV() { editingV.value = null; vForm.value = { plateNo: '', vehicleModel: '', driverName: '', driverPhone: '', ownerName: '', coldType: '', tempRange: '', remark: '' }; showVModal.value = true }
function openEditV(row: any) { editingV.value = row; vForm.value = { plateNo: row.plateNo ?? '', vehicleModel: row.vehicleModel ?? '', driverName: row.driverName ?? '', driverPhone: row.driverPhone ?? '', ownerName: row.ownerName ?? '', coldType: row.coldType ?? '', tempRange: row.tempRange ?? '', remark: row.remark ?? '' }; showVModal.value = true }
async function submitV() { try { const data: Record<string, any> = { ...vForm.value }; if (editingV.value) { data.vehicleId = editingV.value.vehicleId; await coldChainApi.updateVehicle(data); flash('success', '车辆信息更新成功') } else { await coldChainApi.createVehicle(data); flash('success', '冷链车辆注册成功') } showVModal.value = false; loadVehicles() } catch (e: any) { flash('error', '操作失败') } }
async function deleteVehicle(id: number) { if (!confirm('确认删除？')) return; try { await coldChainApi.deleteVehicle(id); flash('success', '车辆删除成功'); loadVehicles() } catch (e: any) { flash('error', '删除失败') } }

// Warehouse CRUD
async function loadWarehouses() { loadingW.value = true; try { const p: Record<string, any> = {}; if (wFilters.value.warehouseType) p.warehouseType = Number(wFilters.value.warehouseType); if (wFilters.value.warehouseStatus) p.warehouseStatus = Number(wFilters.value.warehouseStatus); const data = await coldChainApi.listWarehouse(p); warehouses.value = Array.isArray(data) ? data : [] } catch (e: any) { flash('error', '加载仓库失败') } finally { loadingW.value = false } }
function openCreateW() { editingW.value = null; wForm.value = { warehouseName: '', warehouseType: 0, address: '', capacity: '', manager: '', tempRange: '', humidityRange: '', warehouseStatus: 0, remark: '' }; showWModal.value = true }
function openEditW(row: any) { editingW.value = row; wForm.value = { warehouseName: row.warehouseName ?? '', warehouseType: row.warehouseType ?? 0, address: row.address ?? '', capacity: row.capacity ?? '', manager: row.manager ?? '', tempRange: row.tempRange ?? '', humidityRange: row.humidityRange ?? '', warehouseStatus: row.warehouseStatus ?? 0, remark: row.remark ?? '' }; showWModal.value = true }
async function submitW() { try { const data: Record<string, any> = { ...wForm.value }; if (editingW.value) { data.warehouseId = editingW.value.warehouseId; await coldChainApi.updateWarehouse(data); flash('success', '仓库更新成功') } else { await coldChainApi.createWarehouse(data); flash('success', '仓库创建成功') } showWModal.value = false; loadWarehouses() } catch (e: any) { flash('error', '操作失败') } }
async function deleteWarehouse(id: number) { if (!confirm('确认删除？')) return; try { await coldChainApi.deleteWarehouse(id); flash('success', '仓库删除成功'); loadWarehouses() } catch (e: any) { flash('error', '删除失败') } }

function switchTab(t: 'transport' | 'vehicle' | 'warehouse') { tab.value = t; if (t === 'transport') loadTransports(); else if (t === 'vehicle') loadVehicles(); else loadWarehouses() }
onMounted(loadTransports)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- Stats -->
    <div class="page-stats" v-if="tab === 'transport'">
      <div class="stat-card"><div class="stat-meta"><span>运输订单总数</span><span class="stat-ico">运</span></div><b>{{ tStats.total }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>运输中</span><span class="stat-ico">→</span></div><b>{{ tStats.inTransit }}</b></div>
      <div class="stat-card red" v-if="tStats.alert > 0"><div class="stat-meta"><span>温度预警</span><span class="stat-ico">⚠</span></div><b>{{ tStats.alert }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>已签收</span><span class="stat-ico">✓</span></div><b>{{ tStats.arrived }}</b></div>
    </div>

    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'transport' }" @click="switchTab('transport')">🚛 运输订单</button>
      <button class="tab-btn" :class="{ active: tab === 'vehicle' }" @click="switchTab('vehicle')">🚚 冷链车辆</button>
      <button class="tab-btn" :class="{ active: tab === 'warehouse' }" @click="switchTab('warehouse')">🏪 仓库管理</button>
    </div>

    <!-- Transport -->
    <template v-if="tab === 'transport'">
      <div class="search-bar">
        <div class="search-field"><label>状态</label><select v-model="tFilters.transportStatus"><option value="">全部</option><option value="1">待发运</option><option value="2">运输中</option><option value="3">已签收</option><option value="4">温度预警</option><option value="5">异常关闭</option></select></div>
        <div class="search-field"><label>生产批次</label><input v-model="tFilters.prodBatchNo" @keyup.enter="loadTransports" /></div>
        <div class="search-field"><label>车牌号</label><input v-model="tFilters.plateNo" @keyup.enter="loadTransports" /></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadTransports">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>运输订单 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ transports.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateT">＋ 创建运输订单</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>订单号</th><th>车牌</th><th>产品</th><th>生产批次</th><th>发运地</th><th>目的地</th><th>状态</th><th>发运时间</th><th>预计到达</th><th class="col-actions">操作</th></tr></thead>
        <tbody><tr v-if="loadingT"><td colspan="10" style="text-align:center;padding:32px">加载中...</td></tr><tr v-else-if="!transports.length"><td colspan="10"><div class="empty-state"><div class="empty-icon">🚛</div><p>暂无运输订单</p></div></td></tr>
          <tr v-for="row in transports" :key="row.transportId"><td><code style="color:#2666df;font-size:13px">{{ row.orderNo }}</code></td><td>{{ row.plateNo }}</td><td>{{ row.productName }}</td><td><code style="font-size:12px">{{ row.prodBatchNo }}</code></td><td>{{ row.departureName }}</td><td>{{ row.destinationName }}</td>
            <td><span class="tag" :class="tagClass(transportStatusLabels[row.transportStatus] || '')">{{ transportStatusLabels[row.transportStatus] || '-' }}</span></td><td>{{ row.departTime || '-' }}</td><td>{{ row.estimatedArrival || '-' }}</td>
            <td class="col-actions">
              <button class="btn btn-outline btn-sm" @click="openEditT(row)">✎</button>
              <button v-if="row.transportStatus === 1" class="btn btn-success btn-sm" style="margin-left:3px" @click="stateAction('depart', row.transportId)">🚀 发运</button>
              <button v-if="row.transportStatus === 2" class="btn btn-success btn-sm" style="margin-left:3px" @click="stateAction('arrive', row.transportId)">✓ 签收</button>
              <button v-if="row.transportStatus === 2" class="btn btn-outline btn-sm" style="margin-left:3px" @click="stateAction('alert', row.transportId)">⚠ 预警</button>
              <button v-if="row.transportStatus === 4" class="btn btn-danger btn-sm" style="margin-left:3px" @click="stateAction('close', row.transportId)">✕ 关闭</button>
              <button class="btn btn-danger btn-sm" style="margin-left:3px" @click="confirmDeleteT(row.transportId)">🗑</button>
            </td></tr></tbody></table></div>

      <!-- Transport Modal -->
      <div v-if="showTModal" class="modal-overlay" @click.self="showTModal = false"><div class="modal" style="width:720px"><div class="modal-header"><h3>{{ editingT ? '编辑运输订单' : '创建运输订单' }}</h3><button class="modal-close" @click="showTModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">基</span>基本信息</div>
            <div class="form-row"><div class="form-group"><label>订单号</label><input v-model="tForm.orderNo" /></div><div class="form-group"><label>车牌号 *</label><input v-model="tForm.plateNo" /></div></div>
            <div class="form-row"><div class="form-group"><label>产品名称 *</label><input v-model="tForm.productName" /></div><div class="form-group"><label>生产批次 *</label><input v-model="tForm.prodBatchNo" /></div></div>
            <div class="form-row"><div class="form-group"><label>驾驶员</label><input v-model="tForm.driverName" /></div><div class="form-group"><label>电话</label><input v-model="tForm.driverPhone" /></div></div>
            <div class="form-row"><div class="form-group"><label>发运地</label><input v-model="tForm.departureName" /></div><div class="form-group"><label>目的地 *</label><input v-model="tForm.destinationName" /></div></div>
            <div class="form-row"><div class="form-group"><label>装载温度 (℃)</label><input v-model="tForm.loadingTemp" /></div><div class="form-group"><label>预计到达</label><input v-model="tForm.estimatedArrival" /></div></div>
            <div class="form-row"><div class="form-group"><label>运输方式</label><select v-model.number="tForm.transportMethod"><option :value="0">请选择</option><option :value="1">公路</option><option :value="2">铁路</option><option :value="3">航空</option><option :value="4">海运</option></select></div><div class="form-group"><label>采集间隔</label><input v-model="tForm.collectInterval" /></div></div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">阈</span>温湿度阈值</div>
            <div class="form-row"><div class="form-group"><label>温度上限 (℃)</label><input v-model="tForm.tempUpper" /></div><div class="form-group"><label>温度下限 (℃)</label><input v-model="tForm.tempLower" /></div></div>
            <div class="form-row"><div class="form-group"><label>湿度上限 (%RH)</label><input v-model="tForm.humidUpper" /></div><div class="form-group"><label>湿度下限 (%RH)</label><input v-model="tForm.humidLower" /></div></div>
            <div class="form-group"><label>预警方式</label><input v-model="tForm.alertMethod" /></div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="tForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showTModal = false">取消</button><button class="btn btn-primary" @click="submitT">{{ editingT ? '保存' : '创建' }}</button></div>
      </div></div>
    </template>

    <!-- Vehicle -->
    <template v-if="tab === 'vehicle'">
      <div class="search-bar">
        <div class="search-field"><label>状态</label><select v-model="vFilters.vehicleStatus"><option value="">全部</option><option value="1">空闲</option><option value="2">运输中</option><option value="3">维修中</option></select></div>
        <div class="search-field"><label>物流企业</label><input v-model="vFilters.ownerName" @keyup.enter="loadVehicles" /></div>
        <div class="search-field"><label>冷链类型</label><input v-model="vFilters.coldType" @keyup.enter="loadVehicles" /></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadVehicles">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>冷链车辆 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ vehicles.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateV">＋ 注册车辆</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>车牌号</th><th>车型</th><th>驾驶员</th><th>电话</th><th>企业</th><th>冷链类型</th><th>温控</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
        <tbody><tr v-if="loadingV"><td colspan="9" style="text-align:center;padding:32px">加载中...</td></tr><tr v-else-if="!vehicles.length"><td colspan="9"><div class="empty-state"><div class="empty-icon">🚚</div><p>暂无冷链车辆</p></div></td></tr>
          <tr v-for="row in vehicles" :key="row.vehicleId"><td><strong>{{ row.plateNo }}</strong></td><td>{{ row.vehicleModel || '-' }}</td><td>{{ row.driverName }}</td><td>{{ row.driverPhone }}</td><td>{{ row.ownerName }}</td><td>{{ row.coldType }}</td><td>{{ row.tempRange || '-' }}</td>
            <td><span class="tag" :class="tagClass(vehicleStatusLabels[row.vehicleStatus] || '')">{{ vehicleStatusLabels[row.vehicleStatus] || '-' }}</span></td>
            <td class="col-actions"><button class="btn btn-outline btn-sm" @click="openEditV(row)">✎</button><button class="btn btn-danger btn-sm" style="margin-left:4px" @click="deleteVehicle(row.vehicleId)">🗑</button></td></tr></tbody></table></div>

      <!-- Vehicle Modal -->
      <div v-if="showVModal" class="modal-overlay" @click.self="showVModal = false"><div class="modal"><div class="modal-header"><h3>{{ editingV ? '编辑车辆' : '注册冷链车辆' }}</h3><button class="modal-close" @click="showVModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row"><div class="form-group"><label>车牌号 *</label><input v-model="vForm.plateNo" /></div><div class="form-group"><label>车型</label><input v-model="vForm.vehicleModel" /></div></div>
          <div class="form-row"><div class="form-group"><label>驾驶员</label><input v-model="vForm.driverName" /></div><div class="form-group"><label>联系电话</label><input v-model="vForm.driverPhone" /></div></div>
          <div class="form-row"><div class="form-group"><label>所属企业</label><input v-model="vForm.ownerName" /></div><div class="form-group"><label>冷链类型</label><input v-model="vForm.coldType" placeholder="冷藏/冷冻/恒温" /></div></div>
          <div class="form-row"><div class="form-group"><label>温控范围</label><input v-model="vForm.tempRange" placeholder="-18℃ ~ -10℃" /></div></div>
          <div class="form-group"><label>备注</label><textarea v-model="vForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showVModal = false">取消</button><button class="btn btn-primary" @click="submitV">{{ editingV ? '保存' : '注册' }}</button></div>
      </div></div>
    </template>

    <!-- Warehouse -->
    <template v-if="tab === 'warehouse'">
      <div class="search-bar">
        <div class="search-field"><label>类型</label><select v-model="wFilters.warehouseType"><option value="">全部</option><option value="1">原料仓</option><option value="2">成品仓</option><option value="3">冷链仓</option><option value="4">中转仓</option></select></div>
        <div class="search-field"><label>状态</label><select v-model="wFilters.warehouseStatus"><option value="">全部</option><option value="1">启用</option><option value="2">停用</option></select></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadWarehouses">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>仓库列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ warehouses.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateW">＋ 新增仓库</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>仓库名称</th><th>类型</th><th>地址</th><th>容量</th><th>管理员</th><th>温控</th><th>湿度</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
        <tbody><tr v-if="loadingW"><td colspan="9" style="text-align:center;padding:32px">加载中...</td></tr><tr v-else-if="!warehouses.length"><td colspan="9"><div class="empty-state"><div class="empty-icon">🏪</div><p>暂无仓库</p></div></td></tr>
          <tr v-for="row in warehouses" :key="row.warehouseId"><td><strong>{{ row.warehouseName }}</strong></td><td>{{ warehouseTypeLabels[row.warehouseType] || '-' }}</td><td>{{ row.address }}</td><td>{{ row.capacity }}</td><td>{{ row.manager }}</td><td>{{ row.tempRange || '-' }}</td><td>{{ row.humidityRange || '-' }}</td>
            <td><span class="tag" :class="row.warehouseStatus === 1 ? 'tag-success' : 'tag-neutral'">{{ warehouseStatusLabels[row.warehouseStatus] || '-' }}</span></td>
            <td class="col-actions"><button class="btn btn-outline btn-sm" @click="openEditW(row)">✎</button><button class="btn btn-danger btn-sm" style="margin-left:4px" @click="deleteWarehouse(row.warehouseId)">🗑</button></td></tr></tbody></table></div>

      <!-- Warehouse Modal -->
      <div v-if="showWModal" class="modal-overlay" @click.self="showWModal = false"><div class="modal"><div class="modal-header"><h3>{{ editingW ? '编辑仓库' : '新增仓库' }}</h3><button class="modal-close" @click="showWModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row"><div class="form-group"><label>仓库名称 *</label><input v-model="wForm.warehouseName" /></div><div class="form-group"><label>类型</label><select v-model.number="wForm.warehouseType"><option :value="0">请选择</option><option :value="1">原料仓</option><option :value="2">成品仓</option><option :value="3">冷链仓</option><option :value="4">中转仓</option></select></div></div>
          <div class="form-row"><div class="form-group"><label>地址</label><input v-model="wForm.address" /></div><div class="form-group"><label>容量</label><input v-model="wForm.capacity" /></div></div>
          <div class="form-row"><div class="form-group"><label>管理员</label><input v-model="wForm.manager" /></div><div class="form-group"><label>温控范围</label><input v-model="wForm.tempRange" placeholder="-5℃ ~ 15℃" /></div></div>
          <div class="form-row"><div class="form-group"><label>湿度范围</label><input v-model="wForm.humidityRange" /></div><div class="form-group"><label>状态</label><select v-model.number="wForm.warehouseStatus"><option :value="0">请选择</option><option :value="1">启用</option><option :value="2">停用</option></select></div></div>
          <div class="form-group"><label>备注</label><textarea v-model="wForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showWModal = false">取消</button><button class="btn btn-primary" @click="submitW">{{ editingW ? '保存' : '创建' }}</button></div>
      </div></div>
    </template>

    <!-- Delete Confirm -->
    <div v-if="showTConfirm" class="confirm-overlay"><div class="confirm-box"><div class="confirm-icon">⚠️</div><h3>确认删除</h3><p>确定要删除该运输订单吗？</p><div class="confirm-actions"><button class="btn btn-outline" @click="showTConfirm = false">取消</button><button class="btn btn-danger" @click="doDeleteT">确认删除</button></div></div></div>
  </div>
</template>
