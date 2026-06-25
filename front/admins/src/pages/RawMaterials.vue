<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Box, Check, Close, Delete, Edit, Plus, Refresh, Search, Select, Upload } from '@element-plus/icons-vue'
import { rawApi, enterpriseApi } from '../services/api'
import Pagination from '../components/Pagination.vue'

let currentUser = ''
try {
  const raw = sessionStorage.getItem('fts-admin-user')
  if (raw) { const p = JSON.parse(raw); currentUser = p.realName || p.username || '' }
} catch { currentUser = sessionStorage.getItem('fts-admin-user') || '' }
const loading = ref(false)
const list = ref<any[]>([])
const pendingList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return list.value.slice(start, start + pageSize.value)
})
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showBatchModal = ref(false)
const showQcModal = ref(false)
const showConfirm = ref(false)
const editing = ref<any>(null)
const deletingId = ref<number | null>(null)
const showMatchModal = ref(false)
const matchPendingItem = ref<any>(null)
const matchTargetBatch = ref('')

const filters = ref({ supplierName: '', productCategory: '', checkResult: '', warehouse: '', batchStatus: '', detailStatus: '' })

const stats = computed(() => ({
  total: list.value.length,
  matched: list.value.filter((r: any) => r.detailStatus === 1 || r.detailStatus === 2).length,
  unmatched: list.value.filter((r: any) => !r.detailStatus || r.detailStatus === 0).length,
  unchecked: list.value.filter((r: any) => !r.checkResult || r.checkResult === 0).length,
}))

const batchForm = ref({ batchNo: '', productName: '', productCategory: '', amount: '', unit: '', supplierId: '', supplierName: '', warehouse: '', storageMethod: 0, shelfLife: '', purchaseDate: '', remark: '' })
const qcForm = ref({ batchNo: '', checkResult: 1 })
const customProductCategory = ref('')

// 供应商搜索下拉
const supplierOptions = ref<any[]>([])
const supplierSearchText = ref('')
const supplierDropdownVisible = ref(false)

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function loadSupplierOptions() {
  try {
    const data = await enterpriseApi.search(supplierSearchText.value || '')
    supplierOptions.value = Array.isArray(data) ? data : []
  } catch { supplierOptions.value = [] }
}

const filteredSupplierOptions = computed(() => {
  if (!supplierSearchText.value) return supplierOptions.value
  const q = supplierSearchText.value.toLowerCase()
  return supplierOptions.value.filter((e: any) =>
    (e.enterpriseName || '').toLowerCase().includes(q) ||
    (e.address || '').toLowerCase().includes(q)
  )
})

function onSupplierSelect(ent: any) {
  supplierDropdownVisible.value = false
  batchForm.value.supplierName = ent.enterpriseName || ''
  batchForm.value.supplierId = ent.enterpriseUuid || ''
  supplierSearchText.value = ent.enterpriseName || ''
}

function onSupplierFocus() { supplierDropdownVisible.value = true; if (supplierOptions.value.length === 0) loadSupplierOptions() }
function onSupplierInput() { supplierDropdownVisible.value = true; loadSupplierOptions() }
function onSupplierBlur() { setTimeout(() => supplierDropdownVisible.value = false, 200) }

async function loadList() {
  loading.value = true
  try {
    const p: Record<string, any> = {}
    if (filters.value.supplierName) p.supplierName = filters.value.supplierName
    if (filters.value.productCategory) p.productCategory = filters.value.productCategory
    if (filters.value.checkResult) p.checkResult = Number(filters.value.checkResult)
    if (filters.value.warehouse) p.warehouse = filters.value.warehouse
    if (filters.value.batchStatus) p.batchStatus = Number(filters.value.batchStatus)
    if (filters.value.detailStatus) p.detailStatus = Number(filters.value.detailStatus)
    const data = await rawApi.list(p)
    list.value = Array.isArray(data) ? data : []
    currentPage.value = 1
    const pdata = await rawApi.listPending(undefined, 1)
    pendingList.value = Array.isArray(pdata) ? pdata : []
  } catch (e: any) { notify('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function resetFilters() {
  filters.value = { supplierName: '', productCategory: '', checkResult: '', warehouse: '', batchStatus: '', detailStatus: '' }
  loadList()
}

function openCreate() { editing.value = null; batchForm.value = { batchNo: '', productName: '', productCategory: '', amount: '', unit: '', supplierId: '', supplierName: '', warehouse: '', storageMethod: 0, shelfLife: '', purchaseDate: '', remark: '' }; customProductCategory.value = ''; supplierSearchText.value = ''; showBatchModal.value = true }
function openEdit(row: any) { editing.value = row; batchForm.value = { batchNo: row.batchNo ?? '', productName: row.productName ?? '', productCategory: row.productCategory ?? '', amount: row.amount ?? '', unit: row.unit ?? '', supplierId: row.supplierId ?? '', supplierName: row.supplierName ?? '', warehouse: row.warehouse ?? '', storageMethod: row.storageMethod ?? 0, shelfLife: row.shelfLife ?? '', purchaseDate: row.purchaseDate ?? '', remark: row.remark ?? '' }; customProductCategory.value = ''; supplierSearchText.value = row.supplierName ?? ''; showBatchModal.value = true }

// 原料类别切换到非"其他"时清空自定义值
function onCategoryChange() {
  if (batchForm.value.productCategory !== '其他') {
    customProductCategory.value = ''
  }
}
async function submitBatch() {
  if (!batchForm.value.productName.trim()) { notify('error', '请填写产品名称'); return }
  // 如果选了"其他"则使用自定义类别
  const finalCategory = batchForm.value.productCategory === '其他' ? customProductCategory.value.trim() : batchForm.value.productCategory
  if (!finalCategory) { notify('error', '请选择或填写产品类别'); return }
  if (!batchForm.value.supplierName.trim()) { notify('error', '请填写供应商名称'); return }
  if (!batchForm.value.supplierId.trim()) { notify('error', '请填写供应商编码'); return }
  if (!batchForm.value.warehouse.trim()) { notify('error', '请填写仓库'); return }
  try {
    const data: Record<string, any> = { ...batchForm.value, productCategory: finalCategory }
    if (editing.value) { data.rawBatchId = editing.value.rawBatchId; await rawApi.update(data); notify('success', '批次更新成功') }
    else { await rawApi.create(data); notify('success', '批次创建成功，供应商可在"原料信息上传"页面主动上传源头信息') }
    showBatchModal.value = false; loadList()
  } catch (e: any) { notify('error', '操作失败: ' + e.message) }
}

function confirmDelete(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDelete() { try { await rawApi.delete(deletingId.value!); notify('success', '已删除'); showConfirm.value = false; loadList() } catch (e: any) { notify('error', '删除失败') } }

function openQc(row: any) { qcForm.value = { batchNo: row.batchNo, checkResult: 1 }; showQcModal.value = true }
async function submitQc() { try { await rawApi.qualityCheck(qcForm.value.batchNo, qcForm.value.checkResult); notify('success', `质检录入成功（${qcForm.value.checkResult === 1 ? '合格' : '不合格'}）`); showQcModal.value = false; loadList() } catch (e: any) { notify('error', '质检失败') } }

function openMatchPending(p: any) { matchPendingItem.value = p; matchTargetBatch.value = list.value[0]?.batchNo || ''; showMatchModal.value = true }
async function submitMatch() { if (!matchTargetBatch.value) { notify('error', '请选择目标批次号'); return }; try { await rawApi.matchBatch(matchPendingItem.value.pendingCode, matchTargetBatch.value); notify('success', '匹配成功'); showMatchModal.value = false; loadList() } catch (e: any) { notify('error', '匹配失败: ' + e.message) } }

onMounted(loadList)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner manufacturer">
      <span class="trace-role-badge">加工生产商</span>
      <span>您创建批次 → <strong>供应商主动上传源头详情</strong> → 系统自动匹配或手动匹配</span>
    </div>

    <section class="trace-stats">
      <article><span><el-icon><Box /></el-icon> 批次总数</span><b>{{ stats.total }}</b><em>个原料批次</em></article>
      <article class="green"><span><el-icon><Check /></el-icon> 已匹配溯源</span><b>{{ stats.matched }}</b><em>源头已关联</em></article>
      <article class="amber"><span><el-icon><Upload /></el-icon> 待匹配溯源</span><b>{{ stats.unmatched }}</b><em>等待源头信息</em></article>
      <article :class="stats.unchecked > 0 ? 'amber' : ''"><span><el-icon><Search /></el-icon> 待质检</span><b>{{ stats.unchecked }}</b><em>需检验</em></article>
    </section>

    <section class="trace-panel filter-panel">
      <div class="filter-grid">
        <label>供应商<input v-model="filters.supplierName" placeholder="供应商名称" @keyup.enter="loadList" /></label>
        <label>产品类别<input v-model="filters.productCategory" @keyup.enter="loadList" /></label>
        <label>质检<select v-model="filters.checkResult"><option value="">全部</option><option value="1">合格</option><option value="2">不合格</option></select></label>
        <label>状态<select v-model="filters.batchStatus"><option value="">全部</option><option value="1">待入库</option><option value="2">已入库</option><option value="3">已启用</option></select></label>
        <label>溯源匹配<select v-model="filters.detailStatus"><option value="">全部</option><option value="0">待上传</option><option value="1">已上传</option><option value="2">已匹配</option></select></label>
        <div class="filter-actions"><button class="secondary" @click="resetFilters"><el-icon><Refresh /></el-icon> 重置</button><button class="primary" @click="loadList"><el-icon><Search /></el-icon> 查询</button></div>
      </div>
    </section>

    <section class="trace-panel list-panel" style="margin-bottom:28px">
      <header class="panel-header"><div><p>原料台账</p><h2>原料批次列表</h2></div><button class="primary create" @click="openCreate"><el-icon><Plus /></el-icon> 创建原料批次</button></header>
      <div class="table-wrap"><table><thead><tr><th>批次号</th><th>产品</th><th>供应商</th><th>数量</th><th>仓库</th><th>质检</th><th>状态</th><th>溯源匹配</th><th>日期</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="10" class="empty">加载中...</td></tr>
          <tr v-else-if="!list.length"><td colspan="10" class="empty">暂无原料批次，点击"创建原料批次"开始</td></tr>
          <tr v-for="row in paginatedList" :key="row.rawBatchId">
            <td><code>{{ row.batchNo }}</code></td><td>{{ row.productName }}</td><td>{{ row.supplierName }}</td>
            <td>{{ row.amount }}{{ row.unit }}</td><td>{{ row.warehouse }}</td>
            <td><span class="status" :class="row.checkResult === 1 ? 'status-active' : row.checkResult === 2 ? 'status-void' : 'status-pending'">{{ row.checkResult === 1 ? '合格' : row.checkResult === 2 ? '不合格' : '未检测' }}</span></td>
            <td><span class="status" :class="row.batchStatus === 2 ? 'status-active' : row.batchStatus === 1 ? 'status-pending' : 'status-active'">{{ ['','待入库','已入库','已启用'][row.batchStatus] || '-' }}</span></td>
            <td>
              <span v-if="row.detailStatus >= 1" class="status status-active">✓ 已匹配</span>
              <span v-else class="status status-pending">⏳ 待上传</span>
            </td>
            <td>{{ row.purchaseDate }}</td>
            <td class="actions">
              <button @click="openEdit(row)"><el-icon><Edit /></el-icon> 编辑</button>
              <button v-if="!row.checkResult" @click="openQc(row)"><el-icon><Check /></el-icon> 质检</button>
              <button class="danger" @click="confirmDelete(row.rawBatchId)"><el-icon><Delete /></el-icon> 删除</button>
            </td>
          </tr>
        </tbody></table></div>
      <Pagination v-model="currentPage" :total="list.length" :page-size="pageSize" />
    </section>

    <!-- 待匹配列表 -->
    <section class="trace-panel list-panel">
      <header class="panel-header"><div><p>待匹配</p><h2>供应商主动上传的待匹配原料</h2></div><span style="color:#a4730a;font-size:13px">({{ pendingList.length }} 条)</span></header>
      <div class="trace-row-list" style="padding:0 18px 18px">
        <div v-if="!pendingList.length" style="text-align:center;color:#96a8b9;padding:16px 0;font-size:13px">暂无待匹配记录，供应商上传源头信息后将在此显示并自动匹配</div>
        <div v-for="p in pendingList" :key="p.rawPendingId" class="trace-row-card">
          <span class="trace-mini-badge amber">待</span>
          <div style="flex:1"><strong>{{ p.pendingCode }}</strong><small style="display:block;color:#96a8b9;font-size:11px">{{ p.productName || '未知原料' }} · {{ p.supplierName || '' }} · {{ p.uploadTime }}</small></div>
          <button class="primary btn-sm" @click="openMatchPending(p)"><el-icon><Select /></el-icon> 匹配批次</button>
        </div>
      </div>
    </section>

    <!-- 批次模态框 -->
    <div v-if="showBatchModal" class="trace-modal-backdrop" @click.self="showBatchModal = false">
      <section class="trace-modal" style="width:760px"><header><div><p>批次管理</p><h2>{{ editing ? '编辑原料批次' : '创建原料批次' }}</h2></div><button @click="showBatchModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <!-- Section: 基础信息 -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">基</span>基础信息（生产商录入）</div>
            <div class="trace-hint info">💡 生产商只需录入<strong>供应商名称</strong>和<strong>原料批次号</strong>即可入库。原料详细信息由原料供应商上传后自动关联。</div>
            <div class="grid-form">
              <label>原料名称 *<input v-model="batchForm.productName" placeholder="如：生牛乳" /></label>
              <label>原料类别 *
                <select v-model="batchForm.productCategory" @change="onCategoryChange">
                  <option value="">请选择原料类别</option>
                  <option value="乳制品原料">乳制品原料</option>
                  <option value="果蔬原料">果蔬原料</option>
                  <option value="肉禽原料">肉禽原料</option>
                  <option value="粮油原料">粮油原料</option>
                  <option value="其他">其他</option>
                </select>
              </label>
              <!-- 选择"其他"时出现自定义填写框 -->
              <label v-if="batchForm.productCategory === '其他'">自定义类别 *<input v-model="customProductCategory" placeholder="请输入自定义原料类别" /></label>
              <label>原料供应商 *
                <div class="searchable-select">
                  <input v-model="supplierSearchText" placeholder="输入企业名搜索..." @focus="onSupplierFocus" @blur="onSupplierBlur" @input="onSupplierInput" />
                  <div v-if="supplierDropdownVisible && filteredSupplierOptions.length" class="select-dropdown">
                    <div v-for="ent in filteredSupplierOptions" :key="ent.enterpriseUuid" class="select-option" @mousedown.prevent="onSupplierSelect(ent)">
                      <strong>{{ ent.enterpriseName }}</strong> — {{ ent.address || '地址未登记' }}
                    </div>
                  </div>
                </div>
              </label>
              <label>原料批次号<input v-model="batchForm.batchNo" placeholder="填写批次号，留空自动生成" /></label>
              <label>数量<input v-model="batchForm.amount" type="number" placeholder="0.00" /></label>
              <label>单位<input v-model="batchForm.unit" placeholder="kg / t" /></label>
              <label>采购日期<input v-model="batchForm.purchaseDate" type="date" /></label>
            </div>
          </div>

          <!-- Section: 入库信息 -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">库</span>入库信息</div>
            <div class="grid-form">
              <label>入库仓库 *<input v-model="batchForm.warehouse" placeholder="如：华北一号冷库" /></label>
              <label>保质期至<input v-model="batchForm.shelfLife" type="date" /></label>
            </div>
          </div>

          <div class="trace-hint info" style="background:#fff8ed;border-color:#f5d78b;color:#a45f00;">⚡ 入库后，<strong>原料供应商</strong>将收到提醒并上传原料详细信息（产地、种养、认证、检验报告、运输等），上传后将自动匹配到该批次号。<br/>储存条件与质检状态请在列表<strong>操作列</strong>中单独设置。</div>

          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:10px">供应商编码 *<input v-model="batchForm.supplierId" placeholder="如：SUP2026001" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px" /></label>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:15px">备注<textarea v-model="batchForm.remark" placeholder="请输入原料采购相关说明。" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        </div>
        <footer><button class="secondary" @click="showBatchModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitBatch"><el-icon><Check /></el-icon> {{ editing ? '保存' : '创建批次' }}</button></footer>
      </section>
    </div>

    <!-- 质检模态框 -->
    <div v-if="showQcModal" class="trace-modal-backdrop" @click.self="showQcModal = false">
      <section class="trace-modal" style="width:420px"><header><div><p>质检录入</p><h2>批次质检</h2></div><button @click="showQcModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-bottom:16px">批次号<input v-model="qcForm.batchNo" readonly style="background:#f8fafc;width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px" /></label>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-bottom:16px">质检结果<select v-model.number="qcForm.checkResult" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px"><option :value="1">✓ 合格</option><option :value="2">✗ 不合格</option></select></label>
          <div class="trace-hint info">操作人：<strong>{{ currentUser || '当前用户' }}</strong></div>
        </div>
        <footer><button class="secondary" @click="showQcModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitQc"><el-icon><Check /></el-icon> 确认提交</button></footer>
      </section>
    </div>

    <!-- 匹配模态框 -->
    <div v-if="showMatchModal" class="trace-modal-backdrop" @click.self="showMatchModal = false">
      <section class="trace-modal" style="width:480px"><header><div><p>批次匹配</p><h2>匹配原料批次</h2></div><button @click="showMatchModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <p class="target-code">{{ matchPendingItem?.pendingCode }}</p>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700">目标批次号 *<select v-model="matchTargetBatch" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px"><option value="">-- 请选择目标批次 --</option><option v-for="r in list" :key="r.rawBatchId" :value="r.batchNo">{{ r.batchNo }} - {{ r.productName }}</option></select></label>
        </div>
        <footer><button class="secondary" @click="showMatchModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitMatch" :disabled="!matchTargetBatch"><el-icon><Select /></el-icon> 确认匹配</button></footer>
      </section>
    </div>

    <!-- 删除确认 -->
    <div v-if="showConfirm" class="trace-confirm-overlay" @click.self="showConfirm = false">
      <div class="trace-confirm-box"><h3>确认删除</h3><p>确定要删除该原料批次吗？</p><div class="trace-confirm-actions"><button class="secondary" @click="showConfirm = false"><el-icon><Close /></el-icon> 取消</button><button class="primary danger-fill" @click="doDelete"><el-icon><Delete /></el-icon> 确认删除</button></div></div>
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

/* 可搜索下拉框 (复用 ColdChain 样式) */
.searchable-select { position: relative; }
.searchable-select input { width: 100%; padding: 9px 12px; border: 1px solid #d7e4f0; border-radius: 7px; font-size: 13px; outline: none; transition: border-color 0.2s; }
.searchable-select input:focus { border-color: #2467df; box-shadow: 0 0 0 2px rgba(36, 103, 223, 0.1); }
.select-dropdown { position: absolute; top: 100%; left: 0; right: 0; max-height: 220px; overflow-y: auto; background: #fff; border: 1px solid #d7e4f0; border-radius: 0 0 8px 8px; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12); z-index: 100; }
.select-option { padding: 10px 12px; font-size: 13px; cursor: pointer; border-bottom: 1px solid #f0f4fa; transition: background 0.15s; }
.select-option:hover { background: #eaf2ff; }
.select-option strong { color: #2467df; }
</style>
