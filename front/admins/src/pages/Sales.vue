<script setup lang="ts">
import { ref, computed, inject, onMounted, type Ref } from 'vue'
import { salesApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')!
const isSeller = computed(() => currentRole.value === 'seller')
const isManufacturer = computed(() => currentRole.value === 'manufacturer' || currentRole.value === 'super-admin')

const tab = ref<'terminal' | 'stock' | 'supplement'>('terminal')
const toast = ref<{ type: string; msg: string } | null>(null)

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
const supFilters = ref({ traceBatchNo: '', terminalCode: '' })
const showSupModal = ref(false); const editingSup = ref<any>(null)
const supForm = ref({ supplementCode: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' })

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(s: string) { if (['启用', '在售', '已审核', '已补充'].some(x => s.includes(x))) return 'tag-success'; if (['装修中', '待', '已提交'].some(x => s.includes(x))) return 'tag-warn'; if (['停用', '异常', '售罄'].some(x => s.includes(x))) return 'tag-danger'; return 'tag-info' }

// Terminal CRUD
async function loadTerminals() { loadingT.value = true; try { const p: Record<string, any> = {}; if (tFilters.value.terminalType) p.terminalType = Number(tFilters.value.terminalType); if (tFilters.value.area) p.area = tFilters.value.area; if (tFilters.value.terminalStatus) p.terminalStatus = Number(tFilters.value.terminalStatus); const data = await salesApi.listTerminal(p); terminals.value = Array.isArray(data) ? data : [] } catch (e: any) { flash('error', '加载失败') } finally { loadingT.value = false } }
function openCreateT() { editingT.value = null; tForm.value = { terminalCode: '', terminalName: '', terminalType: 0, area: '', address: '', operatorName: '', remark: '' }; showTModal.value = true }
function openEditT(row: any) { editingT.value = row; tForm.value = { terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', terminalType: row.terminalType ?? 0, area: row.area ?? '', address: row.address ?? '', operatorName: row.operatorName ?? '', remark: row.remark ?? '' }; showTModal.value = true }
async function submitT() { try { const data: Record<string, any> = { ...tForm.value }; if (editingT.value) { data.terminalId = editingT.value.terminalId; await salesApi.updateTerminal(data); flash('success', '终端更新成功') } else { await salesApi.createTerminal(data); flash('success', '终端注册成功') } showTModal.value = false; loadTerminals() } catch (e: any) { flash('error', '操作失败') } }
function confirmDeleteT(id: number) { deletingTId.value = id; showTConfirm.value = true }
async function doDeleteT() { try { await salesApi.deleteTerminal(deletingTId.value!); flash('success', '已删除'); showTConfirm.value = false; loadTerminals() } catch (e: any) { flash('error', '删除失败') } }

// Stock
async function loadStocks() { loadingS.value = true; try { if (sFilters.value.terminalId) { const d = await salesApi.listStock(sFilters.value.terminalId); stocks.value = Array.isArray(d) ? d : [] } else if (sFilters.value.prodBatchNo) { const d = await salesApi.listStockByBatch(sFilters.value.prodBatchNo); stocks.value = Array.isArray(d) ? d : [] } else stocks.value = [] } catch (e: any) { flash('error', '加载失败') } finally { loadingS.value = false } }
function openCreateS() { editingS.value = null; sForm.value = { stockCode: '', terminalId: '', terminalName: '', prodBatchNo: '', productName: '', quantity: '', receivedTime: '', stockStatus: 0, remark: '' }; showSModal.value = true }
function openEditS(row: any) { editingS.value = row; sForm.value = { stockCode: row.stockCode ?? '', terminalId: row.terminalId ?? '', terminalName: row.terminalName ?? '', prodBatchNo: row.prodBatchNo ?? '', productName: row.productName ?? '', quantity: row.quantity ?? '', receivedTime: row.receivedTime ?? '', stockStatus: row.stockStatus ?? 0, remark: row.remark ?? '' }; showSModal.value = true }
async function submitS() { try { const data: Record<string, any> = { ...sForm.value }; if (editingS.value) { data.stockId = editingS.value.stockId; await salesApi.updateStock(data); flash('success', '库存更新成功') } else { await salesApi.stockIn(data); flash('success', '产品入库成功') } showSModal.value = false; loadStocks() } catch (e: any) { flash('error', '操作失败') } }

// Supplement
async function loadSupplements() { loadingSup.value = true; try { if (supFilters.value.traceBatchNo) { const d = await salesApi.listSupplement(supFilters.value.traceBatchNo); supplements.value = Array.isArray(d) ? d : [] } else if (supFilters.value.terminalCode) { const d = await salesApi.listSupplementByTerminal(supFilters.value.terminalCode); supplements.value = Array.isArray(d) ? d : [] } else supplements.value = [] } catch (e: any) { flash('error', '加载失败') } finally { loadingSup.value = false } }
function openCreateSup() { editingSup.value = null; supForm.value = { supplementCode: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' }; showSupModal.value = true }
function openEditSup(row: any) { editingSup.value = row; supForm.value = { supplementCode: row.supplementCode ?? '', traceBatchNo: row.traceBatchNo ?? '', terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', salesCompany: row.salesCompany ?? '', salesBatchNo: row.salesBatchNo ?? '', temperature: row.temperature ?? '', humidity: row.humidity ?? '', storageMethod: row.storageMethod ?? 0, lightCondition: row.lightCondition ?? 0, shelfLife: row.shelfLife ?? '', locationCode: row.locationCode ?? '', quantity: row.quantity ?? '', inboundTime: row.inboundTime ?? '', operator: row.operator ?? '', supplementStatus: row.supplementStatus ?? 1, remark: row.remark ?? '' }; showSupModal.value = true }
async function submitSup() { try { const data: Record<string, any> = { ...supForm.value }; if (editingSup.value) { data.supplementId = editingSup.value.supplementId; await salesApi.updateSupplement(data); flash('success', '补充信息更新成功') } else { await salesApi.supplementInfo(data); flash('success', '销售详情补充成功，溯源码信息已更新') } showSupModal.value = false; loadSupplements() } catch (e: any) { flash('error', '操作失败') } }

async function runAntiFraud() { try { await salesApi.runAntiFraud(); flash('success', '防窜货核验执行完毕') } catch (e: any) { flash('error', '核验失败') } }

function switchTab(t: 'terminal' | 'stock' | 'supplement') { tab.value = t; if (t === 'terminal') loadTerminals(); else if (t === 'stock') loadStocks(); else loadSupplements() }
onMounted(loadTerminals)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 角色提示 -->
    <div class="role-banner" :class="isSeller ? 'seller' : 'manufacturer'">
      <span class="role-badge">{{ isSeller ? '销售商' : '加工生产商' }}</span>
      <span v-if="isManufacturer">您创建销售终端 → 设置销售公司和批次号 → <strong>销售商补充储存与销售详情</strong></span>
      <span v-else>生产商已设置销售公司和批次号 → <strong>您补充入库环境、储存条件、销售信息</strong></span>
    </div>

    <!-- 动态信息提示 -->
    <div class="dynamic-hint" :class="isSeller ? 'info' : 'success'">
      <span v-if="isManufacturer">💡 您设置的<strong>销售公司和批次号</strong>会被写入溯源码。销售商后续补充的<strong>储存温度、湿度、光照、保质期</strong>等信息会动态追加到溯源码中。</span>
      <span v-else>💡 您上传的储存环境信息会<strong>动态追加到溯源码</strong>中，消费者扫码后能看到完整的销售环节数据。</span>
    </div>

    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'terminal' }" @click="switchTab('terminal')">🏪 销售终端</button>
      <button class="tab-btn" :class="{ active: tab === 'stock' }" @click="switchTab('stock')">📦 库存管理</button>
      <button class="tab-btn" :class="{ active: tab === 'supplement' }" @click="switchTab('supplement')">📝 销售补充</button>
    </div>

    <!-- Terminal -->
    <template v-if="tab === 'terminal'">
      <div class="search-bar">
        <div class="search-field"><label>类型</label><select v-model="tFilters.terminalType"><option value="">全部</option><option value="1">超市</option><option value="2">便利店</option><option value="3">餐饮</option><option value="4">电商</option><option value="5">农贸</option><option value="6">其他</option></select></div>
        <div class="search-field"><label>区域</label><input v-model="tFilters.area" @keyup.enter="loadTerminals" /></div>
        <div class="search-field" style="align-self:end;display:flex;gap:8px"><button class="btn btn-primary" @click="loadTerminals">🔍 查询</button><button v-if="isManufacturer" class="btn btn-outline btn-sm" @click="runAntiFraud">🔎 防窜货核验</button></div>
      </div>
      <div class="toolbar"><h3>销售终端 <span class="count-note">({{ terminals.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateT">＋ 注册终端</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>终端编码</th><th>终端名称</th><th>类型</th><th>区域</th><th>运营商</th><th>状态</th><th>创建时间</th><th class="col-actions">操作</th></tr></thead>
        <tbody><tr v-if="loadingT"><td colspan="8" style="text-align:center;padding:40px">加载中...</td></tr><tr v-else-if="!terminals.length"><td colspan="8"><div class="empty-state"><div class="empty-icon">🏪</div><p>暂无销售终端</p></div></td></tr>
          <tr v-for="row in terminals" :key="row.terminalId"><td><code style="color:#2666df;font-size:13px">{{ row.terminalCode }}</code></td><td><strong>{{ row.terminalName }}</strong></td><td>{{ terminalTypeLabels[row.terminalType] || '-' }}</td><td>{{ row.area }}</td><td>{{ row.operatorName }}</td>
            <td><span class="tag" :class="tagClass(['','启用','停用','装修中'][row.terminalStatus] || '')">{{ ['','启用','停用','装修中'][row.terminalStatus] || '-' }}</span></td><td>{{ row.createTime }}</td>
            <td class="col-actions"><button class="btn btn-outline btn-sm" @click="openEditT(row)">✎</button><button v-if="isManufacturer" class="btn btn-danger btn-sm" style="margin-left:4px" @click="confirmDeleteT(row.terminalId)">🗑</button></td></tr></tbody></table></div>

      <!-- Terminal Modal -->
      <div v-if="showTModal" class="modal-overlay" @click.self="showTModal = false"><div class="modal"><div class="modal-header"><h3>{{ editingT ? '编辑终端' : '注册销售终端' }}</h3><button class="modal-close" @click="showTModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row"><div class="form-group"><label>终端编码 *</label><input v-model="tForm.terminalCode" /></div><div class="form-group"><label>终端名称 *</label><input v-model="tForm.terminalName" /></div></div>
          <div class="form-row"><div class="form-group"><label>类型</label><select v-model.number="tForm.terminalType"><option :value="0">请选择</option><option :value="1">超市</option><option :value="2">便利店</option><option :value="3">餐饮</option><option :value="4">电商</option><option :value="5">农贸</option><option :value="6">其他</option></select></div><div class="form-group"><label>区域</label><input v-model="tForm.area" /></div></div>
          <div class="form-row"><div class="form-group"><label>地址</label><input v-model="tForm.address" /></div><div class="form-group"><label>运营商</label><input v-model="tForm.operatorName" /></div></div>
          <div class="form-group"><label>备注</label><textarea v-model="tForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showTModal = false">取消</button><button class="btn btn-primary" @click="submitT">{{ editingT ? '保存' : '注册' }}</button></div>
      </div></div>
    </template>

    <!-- Stock -->
    <template v-if="tab === 'stock'">
      <div class="search-bar">
        <div class="search-field"><label>终端ID</label><input v-model="sFilters.terminalId" @keyup.enter="loadStocks" /></div>
        <div class="search-field"><label>生产批次</label><input v-model="sFilters.prodBatchNo" @keyup.enter="loadStocks" /></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadStocks">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>库存 <span class="count-note">({{ stocks.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateS">＋ 产品入库</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>库存编码</th><th>产品</th><th>生产批次</th><th>终端</th><th>数量</th><th>入库时间</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
        <tbody><tr v-if="loadingS"><td colspan="8" style="text-align:center;padding:40px">加载中...</td></tr><tr v-else-if="!stocks.length"><td colspan="8"><div class="empty-state"><div class="empty-icon">📦</div><p>暂无库存</p></div></td></tr>
          <tr v-for="row in stocks" :key="row.stockId"><td><code style="font-size:12px">{{ row.stockCode }}</code></td><td>{{ row.productName }}</td><td><code style="font-size:12px">{{ row.prodBatchNo }}</code></td><td>{{ row.terminalName }}</td><td>{{ row.quantity }}</td><td>{{ row.receivedTime }}</td>
            <td><span class="tag" :class="tagClass(stockStatusLabels[row.stockStatus] || '')">{{ stockStatusLabels[row.stockStatus] || '-' }}</span></td>
            <td class="col-actions"><button class="btn btn-outline btn-sm" @click="openEditS(row)">✎ 盘点</button></td></tr></tbody></table></div>

      <!-- Stock Modal -->
      <div v-if="showSModal" class="modal-overlay" @click.self="showSModal = false"><div class="modal"><div class="modal-header"><h3>{{ editingS ? '库存盘点' : '产品入库' }}</h3><button class="modal-close" @click="showSModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row"><div class="form-group"><label>产品名称 *</label><input v-model="sForm.productName" /></div><div class="form-group"><label>生产批次 *</label><input v-model="sForm.prodBatchNo" /></div></div>
          <div class="form-row"><div class="form-group"><label>终端名称</label><input v-model="sForm.terminalName" /></div><div class="form-group"><label>数量 *</label><input v-model="sForm.quantity" type="number" /></div></div>
          <div class="form-row"><div class="form-group"><label>入库时间</label><input v-model="sForm.receivedTime" /></div><div class="form-group"><label>状态</label><select v-model.number="sForm.stockStatus"><option :value="0">请选择</option><option :value="1">在库</option><option :value="2">在售</option><option :value="3">下架</option><option :value="4">售罄</option></select></div></div>
          <div class="form-group"><label>终端ID</label><input v-model="sForm.terminalId" /></div>
          <div class="form-group"><label>备注</label><textarea v-model="sForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showSModal = false">取消</button><button class="btn btn-primary" @click="submitS">{{ editingS ? '保存' : '入库' }}</button></div>
      </div></div>
    </template>

    <!-- Supplement (关键：销售商补充) -->
    <template v-if="tab === 'supplement'">
      <div class="dynamic-hint" :class="isSeller ? 'info' : 'success'">
        <span v-if="isManufacturer">📋 生产商在<strong>溯源码管理</strong>中设置销售公司和批次号后，销售商在此补充储存环境等详情。溯源码信息<strong>动态增长</strong>。</span>
        <span v-else>📋 生产商已设置销售公司，请补充<strong>入库环境、储存条件、光照、保质期</strong>等详情。信息将自动更新到溯源码中。</span>
      </div>
      <div class="search-bar">
        <div class="search-field"><label>溯源码批次</label><input v-model="supFilters.traceBatchNo" @keyup.enter="loadSupplements" /></div>
        <div class="search-field"><label>终端编码</label><input v-model="supFilters.terminalCode" @keyup.enter="loadSupplements" /></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadSupplements">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>销售补充 <span class="count-note">({{ supplements.length }} 条)</span></h3><button v-if="isSeller" class="btn btn-primary" @click="openCreateSup">＋ 补充销售详情</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>补充编码</th><th>溯源码批次</th><th>销售企业</th><th>销售批次</th><th>温度</th><th>湿度</th><th>储存方式</th><th>保质期</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
        <tbody><tr v-if="loadingSup"><td colspan="10" style="text-align:center;padding:40px">加载中...</td></tr><tr v-else-if="!supplements.length"><td colspan="10"><div class="empty-state"><div class="empty-icon">📝</div><p>暂无补充信息</p></div></td></tr>
          <tr v-for="row in supplements" :key="row.supplementId"><td><code style="font-size:12px">{{ row.supplementCode }}</code></td><td><code style="font-size:12px">{{ row.traceBatchNo }}</code></td><td>{{ row.salesCompany }}</td><td>{{ row.salesBatchNo }}</td><td>{{ row.temperature || '-' }}</td><td>{{ row.humidity || '-' }}</td><td>{{ ['','常温','冷藏','冷冻'][row.storageMethod] || row.storageMethod || '-' }}</td><td>{{ row.shelfLife || '-' }}</td>
            <td><span class="tag" :class="row.supplementStatus === 2 ? 'tag-success' : 'tag-warn'">{{ row.supplementStatus === 1 ? '已提交' : '已审核' }}</span></td>
            <td class="col-actions"><button v-if="isSeller" class="btn btn-outline btn-sm" @click="openEditSup(row)">✎</button><span v-else class="tag tag-info">查看</span></td></tr></tbody></table></div>

      <!-- Supplement Modal (销售商补充表单 - 核心) -->
      <div v-if="showSupModal" class="modal-overlay" @click.self="showSupModal = false"><div class="modal" style="width:720px"><div class="modal-header"><h3>补充销售详情</h3><button class="modal-close" @click="showSupModal = false">✕</button></div>
        <div class="modal-body">
          <div style="padding:10px;background:#e8f0ff;border-radius:7px;font-size:12px;color:#2666df;margin-bottom:12px">💡 生产商已填写<strong>销售公司</strong>和<strong>批次号</strong>。请补充以下销售终端详细信息，完善溯源码中的<strong>销售环节数据</strong>。</div>
          <div class="form-section"><div class="form-section-title"><span class="ico">关</span>关联信息</div>
            <div class="form-row"><div class="form-group"><label>溯源码批次 *</label><input v-model="supForm.traceBatchNo" /></div><div class="form-group"><label>销售企业</label><input v-model="supForm.salesCompany" placeholder="生产商已设置" /></div></div>
            <div class="form-row"><div class="form-group"><label>销售批次号</label><input v-model="supForm.salesBatchNo" /></div><div class="form-group"><label>终端编码</label><input v-model="supForm.terminalCode" /></div></div>
            <div class="form-row"><div class="form-group"><label>终端名称</label><input v-model="supForm.terminalName" /></div><div class="form-group"><label>数量</label><input v-model="supForm.quantity" type="number" /></div></div>
            <div class="form-row"><div class="form-group"><label>入库时间</label><input v-model="supForm.inboundTime" /></div><div class="form-group"><label>操作员</label><input v-model="supForm.operator" /></div></div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">环</span>入库环境与储存条件（动态添加到溯源码）</div>
            <div class="form-row"><div class="form-group"><label>入库温度</label><input v-model="supForm.temperature" placeholder="2-6℃" /></div><div class="form-group"><label>入库湿度</label><input v-model="supForm.humidity" placeholder="45-65%RH" /></div></div>
            <div class="form-row"><div class="form-group"><label>储存方式</label><select v-model.number="supForm.storageMethod"><option :value="0">常温</option><option :value="1">冷藏</option><option :value="2">冷冻</option></select></div><div class="form-group"><label>光照条件</label><select v-model.number="supForm.lightCondition"><option :value="0">避光</option><option :value="1">散光</option><option :value="2">光照</option></select></div></div>
            <div class="form-row"><div class="form-group"><label>上架保质期</label><input v-model="supForm.shelfLife" placeholder="上架后7天" /></div><div class="form-group"><label>仓库/柜位编号</label><input v-model="supForm.locationCode" /></div></div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="supForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showSupModal = false">取消</button><button class="btn btn-primary" @click="submitSup">确认提交并更新溯源码</button></div>
      </div></div>
    </template>

    <div v-if="showTConfirm" class="confirm-overlay"><div class="confirm-box"><div class="confirm-icon">⚠️</div><h3>确认删除</h3><p>确定要删除该销售终端吗？</p><div class="confirm-actions"><button class="btn btn-outline" @click="showTConfirm = false">取消</button><button class="btn btn-danger" @click="doDeleteT">确认删除</button></div></div></div>
  </div>
</template>
