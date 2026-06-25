<script setup lang="ts">
import { ref, computed, inject, onMounted, type Ref } from 'vue'
import { Close, Plus, Search, Shop, SoldOut } from '@element-plus/icons-vue'
import { salesApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')!
const isSeller = computed(() => currentRole.value === 'seller')
const isManufacturer = computed(() => currentRole.value === 'manufacturer' || currentRole.value === 'super-admin')

const tab = ref<'terminal' | 'stock' | 'supplement'>('terminal')
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
const supFilters = ref({ traceBatchNo: '', terminalCode: '' })
const showSupModal = ref(false); const editingSup = ref<any>(null)
const supForm = ref({ supplementCode: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' })

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(s: string) { if (['启用', '在售', '已审核', '已补充'].some(x => s.includes(x))) return 'status-active'; if (['装修中', '待', '已提交'].some(x => s.includes(x))) return 'status-pending'; if (['停用', '异常', '售罄'].some(x => s.includes(x))) return 'status-void'; return 'status-active' }

// Terminal CRUD
async function loadTerminals() { loadingT.value = true; try { const p: Record<string, any> = {}; if (tFilters.value.terminalType) p.terminalType = Number(tFilters.value.terminalType); if (tFilters.value.area) p.area = tFilters.value.area; if (tFilters.value.terminalStatus) p.terminalStatus = Number(tFilters.value.terminalStatus); const data = await salesApi.listTerminal(p); terminals.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载失败') } finally { loadingT.value = false } }
function openCreateT() { editingT.value = null; tForm.value = { terminalCode: '', terminalName: '', terminalType: 0, area: '', address: '', operatorName: '', remark: '' }; showTModal.value = true }
function openEditT(row: any) { editingT.value = row; tForm.value = { terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', terminalType: row.terminalType ?? 0, area: row.area ?? '', address: row.address ?? '', operatorName: row.operatorName ?? '', remark: row.remark ?? '' }; showTModal.value = true }
async function submitT() { try { const data: Record<string, any> = { ...tForm.value }; if (editingT.value) { data.terminalId = editingT.value.terminalId; await salesApi.updateTerminal(data); notify('success', '终端更新成功') } else { await salesApi.createTerminal(data); notify('success', '终端注册成功') } showTModal.value = false; loadTerminals() } catch (e: any) { notify('error', '操作失败') } }
function confirmDeleteT(id: number) { deletingTId.value = id; showTConfirm.value = true }
async function doDeleteT() { try { await salesApi.deleteTerminal(deletingTId.value!); notify('success', '已删除'); showTConfirm.value = false; loadTerminals() } catch (e: any) { notify('error', '删除失败') } }

// Stock
async function loadStocks() { loadingS.value = true; try { if (sFilters.value.terminalId) { const d = await salesApi.listStock(sFilters.value.terminalId); stocks.value = Array.isArray(d) ? d : [] } else if (sFilters.value.prodBatchNo) { const d = await salesApi.listStockByBatch(sFilters.value.prodBatchNo); stocks.value = Array.isArray(d) ? d : [] } else { const d = await salesApi.listAllStock(); stocks.value = Array.isArray(d) ? d : [] } } catch (e: any) { console.error('Stock load error:', e); notify('error', '加载失败: ' + (e.message || '未知错误')) } finally { loadingS.value = false } }
function openCreateS() { editingS.value = null; sForm.value = { stockCode: '', terminalId: '', terminalName: '', prodBatchNo: '', productName: '', quantity: '', receivedTime: '', stockStatus: 0, remark: '' }; showSModal.value = true }
function openEditS(row: any) { editingS.value = row; sForm.value = { stockCode: row.stockCode ?? '', terminalId: row.terminalId ?? '', terminalName: row.terminalName ?? '', prodBatchNo: row.prodBatchNo ?? '', productName: row.productName ?? '', quantity: row.quantity ?? '', receivedTime: row.receivedTime ?? '', stockStatus: row.stockStatus ?? 0, remark: row.remark ?? '' }; showSModal.value = true }
async function submitS() { try { const data: Record<string, any> = { ...sForm.value }; if (editingS.value) { data.stockId = editingS.value.stockId; await salesApi.updateStock(data); notify('success', '库存更新成功') } else { await salesApi.stockIn(data); notify('success', '产品入库成功') } showSModal.value = false; loadStocks() } catch (e: any) { notify('error', '操作失败') } }

// Supplement
async function loadSupplements() { loadingSup.value = true; try { if (supFilters.value.traceBatchNo) { const d = await salesApi.listSupplement(supFilters.value.traceBatchNo); supplements.value = Array.isArray(d) ? d : [] } else if (supFilters.value.terminalCode) { const d = await salesApi.listSupplementByTerminal(supFilters.value.terminalCode); supplements.value = Array.isArray(d) ? d : [] } else supplements.value = [] } catch (e: any) { notify('error', '加载失败') } finally { loadingSup.value = false } }
function openCreateSup() { editingSup.value = null; supForm.value = { supplementCode: '', traceBatchNo: '', terminalCode: '', terminalName: '', salesCompany: '', salesBatchNo: '', temperature: '', humidity: '', storageMethod: 0, lightCondition: 0, shelfLife: '', locationCode: '', quantity: '', inboundTime: '', operator: '', supplementStatus: 1, remark: '' }; showSupModal.value = true }
function openEditSup(row: any) { editingSup.value = row; supForm.value = { supplementCode: row.supplementCode ?? '', traceBatchNo: row.traceBatchNo ?? '', terminalCode: row.terminalCode ?? '', terminalName: row.terminalName ?? '', salesCompany: row.salesCompany ?? '', salesBatchNo: row.salesBatchNo ?? '', temperature: row.temperature ?? '', humidity: row.humidity ?? '', storageMethod: row.storageMethod ?? 0, lightCondition: row.lightCondition ?? 0, shelfLife: row.shelfLife ?? '', locationCode: row.locationCode ?? '', quantity: row.quantity ?? '', inboundTime: row.inboundTime ?? '', operator: row.operator ?? '', supplementStatus: row.supplementStatus ?? 1, remark: row.remark ?? '' }; showSupModal.value = true }
async function submitSup() { try { const data: Record<string, any> = { ...supForm.value }; if (editingSup.value) { data.supplementId = editingSup.value.supplementId; await salesApi.updateSupplement(data); notify('success', '补充信息更新成功') } else { await salesApi.supplementInfo(data); notify('success', '销售详情补充成功') } showSupModal.value = false; loadSupplements() } catch (e: any) { notify('error', '操作失败') } }

async function runAntiFraud() { try { await salesApi.runAntiFraud(); notify('success', '防窜货核验执行完毕') } catch (e: any) { notify('error', '核验失败') } }

const stats = computed(() => {
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

function switchTab(t: 'terminal' | 'stock' | 'supplement') { tab.value = t; if (t === 'terminal') loadTerminals(); else if (t === 'stock') loadStocks(); else loadSupplements() }
onMounted(loadTerminals)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner" :class="isSeller ? 'seller' : 'manufacturer'">
      <span class="trace-role-badge">{{ isSeller ? '销售商' : '加工生产商' }}</span>
      <span v-if="isManufacturer">您创建销售终端 → 设置销售公司和批次号 → <strong>销售商补充储存与销售详情</strong></span>
      <span v-else>生产商已设置销售公司和批次号 → <strong>您补充入库环境、储存条件、销售信息</strong></span>
    </div>

    <section class="trace-stats">
      <article v-for="s in stats" :key="s.label" :class="s.cls">
        <span><el-icon><component :is="s.icon" /></el-icon> {{ s.label }}</span>
        <b>{{ s.val }}</b><em>条记录</em>
      </article>
    </section>

    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'terminal' }" @click="switchTab('terminal')"><el-icon><Shop /></el-icon> 销售终端</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'stock' }" @click="switchTab('stock')"><el-icon><SoldOut /></el-icon> 库存管理</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'supplement' }" @click="switchTab('supplement')"><el-icon><SoldOut /></el-icon> 销售补充</button>
    </div>

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
            <tr v-for="row in terminals" :key="row.terminalId">
              <td><code>{{ row.terminalCode }}</code></td><td><strong>{{ row.terminalName }}</strong></td>
              <td>{{ terminalTypeLabels[row.terminalType] || '-' }}</td><td>{{ row.area }}</td><td>{{ row.operatorName }}</td>
              <td><span class="status" :class="tagClass(['','启用','停用','装修中'][row.terminalStatus] || '')">{{ ['','启用','停用','装修中'][row.terminalStatus] || '-' }}</span></td>
              <td>{{ row.createTime }}</td>
              <td class="actions">
                <button @click="openEditT(row)"><el-icon><SoldOut /></el-icon> 编辑</button>
                <button v-if="isManufacturer" class="danger" @click="confirmDeleteT(row.terminalId)"><el-icon><Close /></el-icon> 删除</button>
              </td>
            </tr></tbody></table></div>
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
            <tr v-for="row in stocks" :key="row.stockId"><td><code>{{ row.stockCode }}</code></td><td>{{ row.productName }}</td><td><code>{{ row.prodBatchNo }}</code></td><td>{{ row.terminalName }}</td><td>{{ row.quantity }}</td><td>{{ row.receivedTime }}</td>
              <td><span class="status" :class="tagClass(stockStatusLabels[row.stockStatus] || '')">{{ stockStatusLabels[row.stockStatus] || '-' }}</span></td>
              <td class="actions"><button @click="openEditS(row)"><el-icon><SoldOut /></el-icon> 盘点</button></td></tr></tbody></table></div>
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
            <tr v-for="row in supplements" :key="row.supplementId"><td><code>{{ row.supplementCode }}</code></td><td><code>{{ row.traceBatchNo }}</code></td><td>{{ row.salesCompany }}</td><td>{{ row.salesBatchNo }}</td><td>{{ row.temperature || '-' }}</td><td>{{ row.humidity || '-' }}</td><td>{{ ['','常温','冷藏','冷冻'][row.storageMethod] || row.storageMethod || '-' }}</td><td>{{ row.shelfLife || '-' }}</td>
              <td><span class="status" :class="row.supplementStatus === 2 ? 'status-active' : 'status-pending'">{{ row.supplementStatus === 1 ? '已提交' : '已审核' }}</span></td>
              <td class="actions"><button v-if="isSeller" @click="openEditSup(row)"><el-icon><SoldOut /></el-icon> 编辑</button></td></tr></tbody></table></div>
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
</style>
