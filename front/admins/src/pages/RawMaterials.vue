<script setup lang="ts">
/**
 * 原料批次管理 —— 加工生产商视角
 * 职责：创建原料批次（批次号+供应商+产品）、质检、查看供应商匹配状态
 * 供应商通过"原料信息上传"页面上传源头详情
 */
import { ref, computed, onMounted } from 'vue'
import { rawApi } from '../services/api'

const loading = ref(false)
const list = ref<any[]>([])
const pendingList = ref<any[]>([])
const toast = ref<{ type: string; msg: string } | null>(null)
const showBatchModal = ref(false)
const showQcModal = ref(false)
const showConfirm = ref(false)
const editing = ref<any>(null)
const deletingId = ref<number | null>(null)

const filters = ref({ supplierName: '', productCategory: '', checkResult: '', warehouse: '', batchStatus: '', detailStatus: '' })

const stats = computed(() => ({
  total: list.value.length,
  matched: list.value.filter((r: any) => r.detailStatus === 1 || r.detailStatus === 2).length,
  unmatched: list.value.filter((r: any) => !r.detailStatus || r.detailStatus === 0).length,
  unchecked: list.value.filter((r: any) => !r.checkResult || r.checkResult === 0).length,
}))

const batchForm = ref({ batchNo: '', productName: '', productCategory: '', amount: '', unit: '', supplierId: '', supplierName: '', warehouse: '', storageMethod: 0, shelfLife: '', purchaseDate: '', remark: '' })
const qcForm = ref({ batchNo: '', checkResult: 1 })

const storageMethods = ['常温', '冷藏', '冷冻']
const detailStatusLabels = ['', '待上传', '已上传', '已匹配']

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }

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
    // 同时加载待匹配列表（供应商主动上传但未匹配的）
    const pdata = await rawApi.listPending(undefined, 1)
    pendingList.value = Array.isArray(pdata) ? pdata : []
  } catch (e: any) { flash('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function openCreate() {
  editing.value = null
  batchForm.value = { batchNo: '', productName: '', productCategory: '', amount: '', unit: '', supplierId: '', supplierName: '', warehouse: '', storageMethod: 0, shelfLife: '', purchaseDate: '', remark: '' }
  showBatchModal.value = true
}
function openEdit(row: any) {
  editing.value = row
  batchForm.value = { batchNo: row.batchNo ?? '', productName: row.productName ?? '', productCategory: row.productCategory ?? '', amount: row.amount ?? '', unit: row.unit ?? '', supplierId: row.supplierId ?? '', supplierName: row.supplierName ?? '', warehouse: row.warehouse ?? '', storageMethod: row.storageMethod ?? 0, shelfLife: row.shelfLife ?? '', purchaseDate: row.purchaseDate ?? '', remark: row.remark ?? '' }
  showBatchModal.value = true
}
async function submitBatch() {
  // 前端校验必填字段
  if (!batchForm.value.productName.trim()) { flash('error', '请填写产品名称'); return }
  if (!batchForm.value.productCategory.trim()) { flash('error', '请填写产品类别'); return }
  if (!batchForm.value.supplierName.trim()) { flash('error', '请填写供应商名称'); return }
  if (!batchForm.value.supplierId.trim()) { flash('error', '请填写供应商编码'); return }
  if (!batchForm.value.warehouse.trim()) { flash('error', '请填写仓库'); return }
  try {
    const data: Record<string, any> = { ...batchForm.value }
    if (editing.value) { data.rawBatchId = editing.value.rawBatchId; await rawApi.update(data); flash('success', '批次更新成功') }
    else { await rawApi.create(data); flash('success', '批次创建成功，等待供应商上传源头信息') }
    showBatchModal.value = false; loadList()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}

function confirmDelete(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDelete() { try { await rawApi.delete(deletingId.value!); flash('success', '已删除'); showConfirm.value = false; loadList() } catch (e: any) { flash('error', '删除失败') } }

function openQc(row: any) { qcForm.value = { batchNo: row.batchNo, checkResult: 1 }; showQcModal.value = true }
async function submitQc() { try { await rawApi.qualityCheck(qcForm.value.batchNo, qcForm.value.checkResult); flash('success', '质检录入成功'); showQcModal.value = false; loadList() } catch (e: any) { flash('error', '质检失败') } }

async function matchPending(p: any) {
  const target = prompt('请输入目标批次号：', list[0]?.batchNo || '')
  if (!target) return
  try { await rawApi.matchBatch(p.pendingCode, target); flash('success', '匹配成功'); loadList() } catch (e: any) { flash('error', '匹配失败: ' + e.message) }
}

onMounted(loadList)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 角色提示 -->
    <div class="role-banner manufacturer">
      <span class="role-badge">加工生产商</span>
      <span>您创建批次（批次号 + 供应商 + 产品）→ <strong>供应商在"原料信息上传"页面上传源头详情</strong>（产地/认证/运输）→ 自动匹配</span>
    </div>

    <!-- 统计 -->
    <div class="page-stats">
      <div class="stat-card"><div class="stat-meta"><span>批次总数</span><span class="stat-ico">原</span></div><b>{{ stats.total }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>已匹配溯源</span><span class="stat-ico">✓</span></div><b>{{ stats.matched }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>待供应商上传</span><span class="stat-ico">待</span></div><b>{{ stats.unmatched }}</b></div>
      <div class="stat-card" :class="stats.unchecked > 0 ? 'amber' : ''"><div class="stat-meta"><span>待质检</span><span class="stat-ico">检</span></div><b>{{ stats.unchecked }}</b></div>
    </div>

    <!-- 搜索 -->
    <div class="search-bar">
      <div class="search-field"><label>供应商</label><input v-model="filters.supplierName" placeholder="供应商名称" @keyup.enter="loadList" /></div>
      <div class="search-field"><label>产品类别</label><input v-model="filters.productCategory" @keyup.enter="loadList" /></div>
      <div class="search-field"><label>质检</label><select v-model="filters.checkResult"><option value="">全部</option><option value="1">合格</option><option value="2">不合格</option></select></div>
      <div class="search-field"><label>状态</label><select v-model="filters.batchStatus"><option value="">全部</option><option value="1">待入库</option><option value="2">已入库</option><option value="3">已启用</option></select></div>
      <div class="search-field"><label>溯源匹配</label><select v-model="filters.detailStatus"><option value="">全部</option><option value="0">待上传</option><option value="1">已上传</option><option value="2">已匹配</option></select></div>
      <div class="search-field" style="align-self:end;display:flex;gap:8px"><button class="btn btn-primary" @click="loadList">🔍 查询</button><button class="btn btn-outline" @click="filters = { supplierName: '', productCategory: '', checkResult: '', warehouse: '', batchStatus: '', detailStatus: '' }; loadList()">↻ 重置</button></div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <h3>原料批次列表 <span class="count-note">({{ list.length }} 条)</span></h3>
      <button class="btn btn-primary" @click="openCreate">＋ 创建原料批次</button>
    </div>

    <!-- 表格 -->
    <div class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>批次号</th><th>产品</th><th>供应商</th><th>数量</th><th>仓库</th><th>质检</th><th>状态</th><th>溯源匹配</th><th>日期</th><th class="col-actions">操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="10" style="text-align:center;padding:40px">加载中...</td></tr>
          <tr v-else-if="!list.length"><td colspan="10"><div class="empty-state"><div class="empty-icon">📦</div><p>暂无原料批次，点击"创建原料批次"开始</p></div></td></tr>
          <tr v-for="row in list" :key="row.rawBatchId">
            <td><code style="color:#2666df;font-size:13px">{{ row.batchNo }}</code></td>
            <td>{{ row.productName }}</td>
            <td>{{ row.supplierName }}</td>
            <td>{{ row.amount }}{{ row.unit }}</td>
            <td>{{ row.warehouse }}</td>
            <td>
              <span class="tag" :class="row.checkResult === 1 ? 'tag-success' : row.checkResult === 2 ? 'tag-danger' : 'tag-warn'">
                {{ row.checkResult === 1 ? '合格' : row.checkResult === 2 ? '不合格' : '未检测' }}
              </span>
            </td>
            <td><span class="tag" :class="row.batchStatus === 2 ? 'tag-success' : row.batchStatus === 1 ? 'tag-warn' : 'tag-info'">{{ ['','待入库','已入库','已启用'][row.batchStatus] || '-' }}</span></td>
            <td>
              <span v-if="row.detailStatus >= 1" class="tag tag-success">✓ 已匹配</span>
              <span v-else class="tag tag-warn">⏳ {{ detailStatusLabels[row.detailStatus] || '待上传' }}</span>
            </td>
            <td>{{ row.purchaseDate }}</td>
            <td class="col-actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(row)">✎</button>
              <button v-if="!row.checkResult || row.checkResult === 0" class="btn btn-success btn-sm" style="margin-left:3px" @click="openQc(row)">✓ 质检</button>
              <button class="btn btn-danger btn-sm" style="margin-left:3px" @click="confirmDelete(row.rawBatchId)">🗑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 供应商主动上传的待匹配列表 -->
    <div v-if="pendingList.length" style="margin-top:28px">
      <div class="toolbar"><h3>⏳ 供应商主动上传的待匹配原料 <span class="count-note" style="color:#f59e0b">({{ pendingList.length }} 条 — 供应商已上传详情，等待匹配批次号)</span></h3></div>
      <div class="row-list">
        <div v-for="p in pendingList" :key="p.rawPendingId" class="row-card">
          <span class="mini-badge amber-bg">待</span>
          <div style="flex:1"><strong>{{ p.pendingCode }}</strong><div class="muted">{{ p.productName || '未知原料' }} · {{ p.supplierName || '' }} · {{ p.uploadTime }}</div></div>
          <button class="btn btn-primary btn-sm" @click="matchPending(p)">匹配批次</button>
        </div>
      </div>
    </div>

    <!-- 创建/编辑批次模态框 -->
    <div v-if="showBatchModal" class="modal-overlay" @click.self="showBatchModal = false"><div class="modal"><div class="modal-header"><h3>{{ editing ? '编辑原料批次' : '创建原料批次' }}</h3><button class="modal-close" @click="showBatchModal = false">✕</button></div>
      <div class="modal-body">
        <div style="padding:10px;background:#e8f0ff;border-radius:7px;font-size:12px;color:#2666df;margin-bottom:14px">
          💡 填写<strong>批次号、产品、供应商</strong>。创建后，原料供应商将在"原料信息上传"页面补充<strong>产地、认证、运输等源头详情</strong>。
        </div>
        <div class="form-section"><div class="form-section-title"><span class="ico">基</span>基本信息</div>
          <div class="form-row"><div class="form-group"><label>批次号</label><input v-model="batchForm.batchNo" placeholder="留空自动生成" /></div><div class="form-group"><label>产品名称 *</label><input v-model="batchForm.productName" placeholder="如：生牛乳" required /></div></div>
          <div class="form-row"><div class="form-group"><label>产品类别 *</label><input v-model="batchForm.productCategory" placeholder="如：乳制品原料" required /></div><div class="form-group"><label>供应商名称 *</label><input v-model="batchForm.supplierName" placeholder="哪家公司供货" required /></div></div>
          <div class="form-row"><div class="form-group"><label>数量</label><input v-model="batchForm.amount" type="number" min="0" step="0.01" placeholder="0.00" /></div><div class="form-group"><label>单位</label><input v-model="batchForm.unit" placeholder="kg / t" /></div></div>
          <div class="form-row"><div class="form-group"><label>仓库 *</label><input v-model="batchForm.warehouse" placeholder="如：A1-03" required /></div><div class="form-group"><label>储存方式</label><select v-model.number="batchForm.storageMethod"><option v-for="(m,i) in storageMethods" :key="i" :value="i">{{ m }}</option></select></div></div>
          <div class="form-row"><div class="form-group"><label>保质期</label><input v-model="batchForm.shelfLife" type="date" /></div><div class="form-group"><label>采购日期</label><input v-model="batchForm.purchaseDate" type="date" /></div></div>
        </div>
        <div class="form-group"><label>供应商编码 *</label><input v-model="batchForm.supplierId" placeholder="如：SUP2026001" required /></div>
        <div class="form-group"><label>备注</label><textarea v-model="batchForm.remark" /></div>
      </div>
      <div class="modal-footer"><button class="btn btn-outline" @click="showBatchModal = false">取消</button><button class="btn btn-primary" @click="submitBatch">{{ editing ? '保存' : '创建批次' }}</button></div>
    </div></div>

    <!-- 质检模态框 -->
    <div v-if="showQcModal" class="modal-overlay" @click.self="showQcModal = false"><div class="modal" style="width:420px"><div class="modal-header"><h3>质检录入</h3><button class="modal-close" @click="showQcModal = false">✕</button></div>
      <div class="modal-body">
        <div class="form-group"><label>批次号</label><input v-model="qcForm.batchNo" readonly style="background:#f8fafc" /></div>
        <div class="form-group"><label>质检结果</label><select v-model.number="qcForm.checkResult"><option :value="1">✓ 合格</option><option :value="2">✗ 不合格</option></select></div>
      </div>
      <div class="modal-footer"><button class="btn btn-outline" @click="showQcModal = false">取消</button><button class="btn btn-primary" @click="submitQc">确认提交</button></div>
    </div></div>

    <!-- 删除确认 -->
    <div v-if="showConfirm" class="confirm-overlay"><div class="confirm-box"><div class="confirm-icon">⚠️</div><h3>确认删除</h3><p>确定要删除该原料批次吗？</p><div class="confirm-actions"><button class="btn btn-outline" @click="showConfirm = false">取消</button><button class="btn btn-danger" @click="doDelete">确认删除</button></div></div></div>
  </div>
</template>
