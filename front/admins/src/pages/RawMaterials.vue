<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { rawApi } from '../services/api'

// ---- 状态 ----
const loading = ref(false)
const list = ref<any[]>([])
const toast = ref<{ type: string; msg: string } | null>(null)
const showModal = ref(false)
const showConfirm = ref(false)
const editing = ref<any>(null)
const deletingId = ref<number | null>(null)

// 筛选
const filters = ref({ supplierName: '', productCategory: '', checkResult: '', warehouse: '', batchStatus: '', detailStatus: '' })

// 表单
const form = ref({
  batchNo: '', productName: '', productCategory: '', amount: '', unit: '',
  supplierId: '', supplierName: '', warehouse: '', storageMethod: 0,
  shelfLife: '', purchaseDate: '', remark: '',
})

const storageMethods = ['常温', '冷藏', '冷冻']
const checkResultLabels = ['', '合格', '不合格']
const batchStatusLabels = ['', '待入库', '已入库', '已启用']
const detailStatusLabels = ['', '未上传', '已上传']

function flash(type: string, msg: string) {
  toast.value = { type, msg }
  setTimeout(() => (toast.value = null), 2600)
}

// ---- 数据加载 ----
async function load() {
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
  } catch (e: any) { flash('error', '加载原料批次失败: ' + e.message) }
  finally { loading.value = false }
}

function resetFilters() {
  filters.value = { supplierName: '', productCategory: '', checkResult: '', warehouse: '', batchStatus: '', detailStatus: '' }
  load()
}

// ---- 编辑/新建 ----
function openCreate() {
  editing.value = null
  form.value = { batchNo: '', productName: '', productCategory: '', amount: '', unit: '', supplierId: '', supplierName: '', warehouse: '', storageMethod: 0, shelfLife: '', purchaseDate: '', remark: '' }
  showModal.value = true
}

function openEdit(row: any) {
  editing.value = row
  form.value = {
    batchNo: row.batchNo ?? '', productName: row.productName ?? '', productCategory: row.productCategory ?? '',
    amount: row.amount ?? '', unit: row.unit ?? '', supplierId: row.supplierId ?? '', supplierName: row.supplierName ?? '',
    warehouse: row.warehouse ?? '', storageMethod: row.storageMethod ?? 0, shelfLife: row.shelfLife ?? '',
    purchaseDate: row.purchaseDate ?? '', remark: row.remark ?? '',
  }
  showModal.value = true
}

async function submit() {
  try {
    const data: Record<string, any> = { ...form.value }
    if (editing.value) {
      data.rawBatchId = editing.value.rawBatchId
      await rawApi.update(data)
      flash('success', '原料批次更新成功')
    } else {
      await rawApi.create(data)
      flash('success', '原料批次创建成功')
    }
    showModal.value = false
    load()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}

// ---- 删除 ----
function confirmDelete(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDelete() {
  try {
    await rawApi.delete(deletingId.value!)
    flash('success', '原料批次删除成功')
    showConfirm.value = false
    load()
  } catch (e: any) { flash('error', '删除失败: ' + e.message) }
}

// ---- 质检 ----
async function doQualityCheck(row: any, result: number) {
  try {
    await rawApi.qualityCheck(row.batchNo, result)
    flash('success', '质检结果录入成功')
    load()
  } catch (e: any) { flash('error', '质检失败: ' + e.message) }
}

onMounted(load)
</script>

<template>
  <div class="page-module">
    <!-- Toast -->
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 搜索 -->
    <div class="search-bar">
      <div class="search-field"><label>供应商</label><input v-model="filters.supplierName" placeholder="供应商名称" @keyup.enter="load" /></div>
      <div class="search-field"><label>产品类别</label><input v-model="filters.productCategory" placeholder="产品类别" @keyup.enter="load" /></div>
      <div class="search-field">
        <label>质检结果</label>
        <select v-model="filters.checkResult"><option value="">全部</option><option value="1">合格</option><option value="2">不合格</option></select>
      </div>
      <div class="search-field"><label>仓库</label><input v-model="filters.warehouse" placeholder="仓库名称" @keyup.enter="load" /></div>
      <div class="search-field">
        <label>批次状态</label>
        <select v-model="filters.batchStatus"><option value="">全部</option><option value="1">待入库</option><option value="2">已入库</option><option value="3">已启用</option></select>
      </div>
      <div class="search-field">
        <label>溯源详情</label>
        <select v-model="filters.detailStatus"><option value="">全部</option><option value="1">未上传</option><option value="2">已上传</option></select>
      </div>
      <div class="search-field" style="align-self:end">
        <button class="btn btn-primary" @click="load">🔍 查询</button>
        <button class="btn btn-outline" style="margin-left:8px" @click="resetFilters">↻ 重置</button>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <h3>原料批次列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ list.length }} 条)</span></h3>
      <div class="toolbar-actions">
        <button class="btn btn-primary" @click="openCreate">＋ 新增原料批次</button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="data-table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>批次号</th><th>产品名称</th><th>产品类别</th><th>供应商</th>
            <th>数量</th><th>仓库</th><th>质检结果</th><th>批次状态</th>
            <th>溯源详情</th><th>采购日期</th><th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="11" style="text-align:center;padding:32px">加载中...</td></tr>
          <tr v-else-if="!list.length"><td colspan="11"><div class="empty-state"><div class="empty-icon">📦</div><p>暂无原料批次数据</p></div></td></tr>
          <tr v-for="row in list" :key="row.rawBatchId">
            <td><code style="color:#2666df;font-size:13px">{{ row.batchNo }}</code></td>
            <td>{{ row.productName }}</td>
            <td>{{ row.productCategory }}</td>
            <td>{{ row.supplierName }}</td>
            <td>{{ row.amount }}{{ row.unit }}</td>
            <td>{{ row.warehouse }}</td>
            <td><span class="tag" :class="row.checkResult === 1 ? 'tag-success' : row.checkResult === 2 ? 'tag-danger' : 'tag-neutral'">{{ checkResultLabels[row.checkResult] || '未检测' }}</span></td>
            <td><span class="tag" :class="row.batchStatus === 3 ? 'tag-success' : row.batchStatus === 1 ? 'tag-warn' : 'tag-info'">{{ batchStatusLabels[row.batchStatus] || '-' }}</span></td>
            <td><span class="tag" :class="row.detailStatus === 2 ? 'tag-success' : 'tag-warn'">{{ detailStatusLabels[row.detailStatus] || '-' }}</span></td>
            <td>{{ row.purchaseDate }}</td>
            <td class="col-actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(row)">✎ 编辑</button>
              <button v-if="!row.checkResult || row.checkResult === 0" class="btn btn-success btn-sm" style="margin-left:6px" @click="doQualityCheck(row, 1)">✓ 合格</button>
              <button v-if="!row.checkResult || row.checkResult === 0" class="btn btn-danger btn-sm" style="margin-left:6px" @click="doQualityCheck(row, 2)">✗ 不合格</button>
              <button class="btn btn-danger btn-sm" style="margin-left:6px" @click="confirmDelete(row.rawBatchId)">🗑 删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新建/编辑模态框 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header"><h3>{{ editing ? '编辑原料批次' : '新增原料批次' }}</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group"><label>批次号 *</label><input v-model="form.batchNo" placeholder="自动生成可留空" /></div>
            <div class="form-group"><label>产品名称 *</label><input v-model="form.productName" placeholder="如：有机大豆" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>产品类别</label><input v-model="form.productCategory" placeholder="如：豆制品原料" /></div>
            <div class="form-group"><label>供应商名称</label><input v-model="form.supplierName" placeholder="供应商名称" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>供应商编码</label><input v-model="form.supplierId" placeholder="供应商ID" /></div>
            <div class="form-group"><label>仓库</label><input v-model="form.warehouse" placeholder="仓库名称" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>数量</label><input v-model="form.amount" placeholder="数量" type="number" /></div>
            <div class="form-group"><label>单位</label><input v-model="form.unit" placeholder="kg / 吨 / 箱" /></div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>储存方式</label>
              <select v-model.number="form.storageMethod"><option v-for="(m,i) in storageMethods" :key="i" :value="i">{{ m }}</option></select>
            </div>
            <div class="form-group"><label>保质期</label><input v-model="form.shelfLife" placeholder="如：12个月" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>采购日期</label><input v-model="form.purchaseDate" placeholder="2025-01-01" /></div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="form.remark" placeholder="备注信息" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="submit">{{ editing ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="showConfirm" class="confirm-overlay" @click.self="showConfirm = false">
      <div class="confirm-box">
        <div class="confirm-icon">⚠️</div>
        <h3>确认删除</h3>
        <p>删除后将不可恢复，确定要删除该原料批次吗？</p>
        <div class="confirm-actions">
          <button class="btn btn-outline" @click="showConfirm = false">取消</button>
          <button class="btn btn-danger" @click="doDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>
