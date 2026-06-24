<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { productionApi } from '../services/api'

const tab = ref<'prod' | 'process' | 'template' | 'input' | 'env' | 'inspection'>('prod')
const loading = ref(false)
const toast = ref<{ type: string; msg: string } | null>(null)

// ---- modal flags ----
const showModal = ref(false)
const showProcessModal = ref(false)
const showTemplateModal = ref(false)
const showInputModal = ref(false)
const showEnvModal = ref(false)
const showInspectionModal = ref(false)
const showConfirm = ref(false)
const showChainModal = ref(false)
const confirmMsg = ref('')

// ---- lists ----
const list = ref<any[]>([])           // prod batch
const processList = ref<any[]>([])    // process batch
const templates = ref<any[]>([])
const materialInputs = ref<any[]>([])
const envRecords = ref<any[]>([])
const inspections = ref<any[]>([])

// ---- editing refs ----
const editing = ref<any>(null)
const editingProcess = ref<any>(null)
const editingTemplate = ref<any>(null)
const deletingId = ref<number | null>(null)
const deleteKind = ref<'prod' | 'process'>('prod')
const chainData = ref<any>(null)

// ---- filters ----
const filters = ref({ productName: '', productionLine: '', checkResult: '', batchStatus: '', codeStatus: '' })
const processFilters = ref({ productName: '', productionLine: '', batchStatus: '', shift: '' })
const templateFilters = ref({ applicableProduct: '', templateStatus: '' })
const envLine = ref('')
const inspFilters = ref({ bizType: '', bizBatchNo: '', inspectionType: '', inspectionResult: '' })

// ---- forms ----
const form = ref({ batchNo: '', productName: '', processBatchNo: '', productionLine: '', plannedAmount: '', actualAmount: '', productionDate: '', remark: '' })
const processForm = ref({ batchNo: '', productName: '', templateName: '', rawBatchNo: '', plannedAmount: '', processDate: '', operator: '', productionLine: '', shift: 0, actualTemp: '', actualDuration: '', actualPressure: '', actualCoolTemp: '', actualFillTemp: '', actualPh: '', actualViscosity: '', remark: '' })
const templateForm = ref({ templateName: '', version: '', applicableProduct: '', targetTemp: '', duration: '', pressure: '', coolTemp: '', fillTemp: '', stirSpeed: '', phValue: '', viscosity: '', cleanLevel: 0, templateStatus: 1, remark: '' })
const inputForm = ref({ rawBatchNo: '', materialName: '', inputAmount: '', unit: '', operator: '', inputTime: '', remark: '' })
const envForm = ref({ productionLine: '', temperature: '', humidity: '', cleanliness: '', isAbnormal: 0, abnormalDesc: '', recordTime: '' })
const inspectionForm = ref({ inspectionNo: '', bizType: 2, bizBatchNo: '', inspectionType: 1, inspector: '', inspectionDate: '', inspectionResult: 1, resultDesc: '', remark: '' })

// ---- labels ----
const checkResultLabels = ['', '合格', '不合格']
const prodBatchStatusLabels = ['', '待生产', '生产中', '生产完成', '已废弃']
const codeStatusLabels = ['', '未绑定', '已绑定']
const processBatchStatusLabels = ['', '加工中', '加工完成', '已存证', '已废弃']
const shiftLabels = ['', '早班', '中班', '晚班']

// ---- dynamic stats ----
const stats = computed(() => {
  switch (tab.value) {
    case 'prod':
      return [
        { label: '生产批次总数', icon: '产', cls: '', val: list.value.length },
        { label: '质检合格', icon: '✓', cls: 'green', val: list.value.filter((r: any) => r.checkResult === 1).length },
        { label: '生产完成', icon: '完', cls: 'green', val: list.value.filter((r: any) => r.batchStatus === 3).length },
        { label: '已绑码', icon: '码', cls: 'amber', val: list.value.filter((r: any) => r.codeStatus === 2).length },
      ]
    case 'process':
      return [
        { label: '加工批次总数', icon: '工', cls: '', val: processList.value.length },
        { label: '加工中', icon: '进', cls: 'amber', val: processList.value.filter((r: any) => r.batchStatus === 1).length },
        { label: '已完成/存证', icon: '✓', cls: 'green', val: processList.value.filter((r: any) => r.batchStatus === 2 || r.batchStatus === 3).length },
        { label: '工艺模板', icon: '模', cls: '', val: templates.value.length },
      ]
    case 'template':
      return [
        { label: '工艺模板总数', icon: '模', cls: '', val: templates.value.length },
        { label: '启用', icon: '✓', cls: 'green', val: templates.value.filter((r: any) => r.templateStatus === 1).length },
        { label: '停用', icon: '✕', cls: '', val: templates.value.filter((r: any) => r.templateStatus === 2).length },
      ]
    case 'input':
      return [
        { label: '投料记录', icon: '📥', cls: '', val: materialInputs.value.length },
      ]
    case 'env':
      return [
        { label: '环境记录', icon: '🌡', cls: '', val: envRecords.value.length },
        { label: '异常', icon: '⚠', cls: 'amber', val: envRecords.value.filter((r: any) => r.isAbnormal === 1).length },
      ]
    case 'inspection':
      return [
        { label: '质检记录', icon: '🔬', cls: '', val: inspections.value.length },
        { label: '合格', icon: '✓', cls: 'green', val: inspections.value.filter((r: any) => r.inspectionResult === 1).length },
        { label: '不合格', icon: '✕', cls: 'amber', val: inspections.value.filter((r: any) => r.inspectionResult === 2).length },
      ]
    default: return []
  }
})

// ---- utils ----
function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(s: string) {
  if (['已完成', '已存证', '已绑定', '已启用', '合格', '正常'].some(x => s.includes(x))) return 'tag-success'
  if (['加工中', '进行中', '待', '未', '生产中'].some(x => s.includes(x))) return 'tag-warn'
  return 'tag-info'
}

// ==================== 生产批次 ====================
async function loadProdBatch() {
  loading.value = true
  try {
    const p: Record<string, any> = {}
    if (filters.value.productName) p.productName = filters.value.productName
    if (filters.value.productionLine) p.productionLine = filters.value.productionLine
    if (filters.value.checkResult) p.checkResult = Number(filters.value.checkResult)
    if (filters.value.batchStatus) p.batchStatus = Number(filters.value.batchStatus)
    if (filters.value.codeStatus) p.codeStatus = Number(filters.value.codeStatus)
    const data = await productionApi.listProdBatch(p); list.value = Array.isArray(data) ? data : []
  } catch (e: any) { flash('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}
function openCreateProd() { editing.value = null; form.value = { batchNo: '', productName: '', processBatchNo: '', productionLine: '', plannedAmount: '', actualAmount: '', productionDate: '', remark: '' }; showModal.value = true }
function openEditProd(row: any) { editing.value = row; form.value = { batchNo: row.batchNo ?? '', productName: row.productName ?? '', processBatchNo: row.processBatchNo ?? '', productionLine: row.productionLine ?? '', plannedAmount: row.plannedAmount ?? '', actualAmount: row.actualAmount ?? '', productionDate: row.productionDate ?? '', remark: row.remark ?? '' }; showModal.value = true }
async function submitProdBatch() {
  try {
    const data: Record<string, any> = { ...form.value }
    if (editing.value) { data.prodBatchId = editing.value.prodBatchId; await productionApi.updateProdBatch(data); flash('success', '生产批次更新成功') }
    else { await productionApi.createProdBatch(data); flash('success', '生产批次创建成功') }
    showModal.value = false; loadProdBatch()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}
async function completeProd(id: number) { try { await productionApi.completeProdBatch(id); flash('success', '生产批次已完成'); loadProdBatch() } catch (e: any) { flash('error', '操作失败: ' + e.message) } }
async function bindCode(id: number) { try { await productionApi.bindCode(id); flash('success', '溯源码绑定完成'); loadProdBatch() } catch (e: any) { flash('error', '绑码失败: ' + e.message) } }
async function qualityCheckProd(batchNo: string, result: number) { try { await productionApi.qualityCheckProd(batchNo, result); flash('success', '质检录入成功'); loadProdBatch() } catch (e: any) { flash('error', '质检失败: ' + e.message) } }
async function traceChain(batchNo: string) { try { const result = await productionApi.traceProcessChain(batchNo); chainData.value = result; showChainModal.value = true } catch (e: any) { flash('error', '追溯失败: ' + e.message) } }

// ==================== 加工批次 ====================
async function loadProcess() {
  loading.value = true
  try {
    const p: Record<string, any> = {}
    if (processFilters.value.productName) p.productName = processFilters.value.productName
    if (processFilters.value.productionLine) p.productionLine = processFilters.value.productionLine
    if (processFilters.value.batchStatus) p.batchStatus = Number(processFilters.value.batchStatus)
    if (processFilters.value.shift) p.shift = Number(processFilters.value.shift)
    const data = await productionApi.listProcessBatch(p); processList.value = Array.isArray(data) ? data : []
  } catch (e: any) { flash('error', '加载加工批次失败: ' + e.message) }
  finally { loading.value = false }
}
function openCreateProcess() {
  editingProcess.value = null
  processForm.value = { batchNo: '', productName: '', templateName: '', rawBatchNo: '', plannedAmount: '', processDate: '', operator: '', productionLine: '', shift: 0, actualTemp: '', actualDuration: '', actualPressure: '', actualCoolTemp: '', actualFillTemp: '', actualPh: '', actualViscosity: '', remark: '' }
  showProcessModal.value = true
}
function openEditProcess(row: any) {
  editingProcess.value = row
  processForm.value = {
    batchNo: row.batchNo ?? '', productName: row.productName ?? '', templateName: row.templateName ?? '',
    rawBatchNo: row.rawBatchNo ?? '', plannedAmount: row.plannedAmount ?? '', processDate: row.processDate ?? '',
    operator: row.operator ?? '', productionLine: row.productionLine ?? '', shift: row.shift ?? 0,
    actualTemp: row.actualTemp ?? '', actualDuration: row.actualDuration ?? '', actualPressure: row.actualPressure ?? '',
    actualCoolTemp: row.actualCoolTemp ?? '', actualFillTemp: row.actualFillTemp ?? '', actualPh: row.actualPh ?? '',
    actualViscosity: row.actualViscosity ?? '', remark: row.remark ?? '',
  }
  showProcessModal.value = true
}
async function submitProcess() {
  try {
    const data: Record<string, any> = { ...processForm.value }
    if (editingProcess.value) { data.processBatchId = editingProcess.value.processBatchId; await productionApi.updateProcessBatch(data); flash('success', '加工批次更新成功') }
    else { await productionApi.createProcessBatch(data); flash('success', '加工批次创建成功') }
    showProcessModal.value = false; loadProcess()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}
async function completeProcess(id: number) { try { await productionApi.completeProcessBatch(id); flash('success', '加工批次已完成'); loadProcess() } catch (e: any) { flash('error', '操作失败: ' + e.message) } }

// ==================== 工艺模板 ====================
async function loadTemplates() {
  try {
    const p: Record<string, any> = {}
    if (templateFilters.value.applicableProduct) p.applicableProduct = templateFilters.value.applicableProduct
    if (templateFilters.value.templateStatus) p.templateStatus = Number(templateFilters.value.templateStatus)
    const data = await productionApi.listTemplate(p); templates.value = Array.isArray(data) ? data : []
  } catch (e: any) { flash('error', '加载工艺模板失败: ' + e.message) }
}
function openCreateTemplate() {
  editingTemplate.value = null
  templateForm.value = { templateName: '', version: '', applicableProduct: '', targetTemp: '', duration: '', pressure: '', coolTemp: '', fillTemp: '', stirSpeed: '', phValue: '', viscosity: '', cleanLevel: 0, templateStatus: 1, remark: '' }
  showTemplateModal.value = true
}
function openEditTemplate(row: any) {
  editingTemplate.value = row
  templateForm.value = {
    templateName: row.templateName ?? '', version: row.version ?? '', applicableProduct: row.applicableProduct ?? '',
    targetTemp: row.targetTemp ?? '', duration: row.duration ?? '', pressure: row.pressure ?? '',
    coolTemp: row.coolTemp ?? '', fillTemp: row.fillTemp ?? '', stirSpeed: row.stirSpeed ?? '',
    phValue: row.phValue ?? '', viscosity: row.viscosity ?? '', cleanLevel: row.cleanLevel ?? 0,
    templateStatus: row.templateStatus ?? 1, remark: row.remark ?? '',
  }
  showTemplateModal.value = true
}
async function submitTemplate() {
  try {
    const data: Record<string, any> = { ...templateForm.value }
    if (editingTemplate.value) { data.templateId = editingTemplate.value.templateId; await productionApi.updateTemplate(data); flash('success', '工艺模板更新成功') }
    else { await productionApi.createTemplate(data); flash('success', '工艺模板创建成功') }
    showTemplateModal.value = false; loadTemplates()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}
async function deleteTemplate(id: number) { if (!confirm('确认删除该工艺模板？')) return; try { await productionApi.deleteTemplate(id); flash('success', '工艺模板删除成功'); loadTemplates() } catch (e: any) { flash('error', '删除失败: ' + e.message) } }

// ==================== 投料记录 ====================
async function loadMaterialInput() { try { const d = await productionApi.listMaterialInput(); materialInputs.value = Array.isArray(d) ? d : [] } catch (e: any) { flash('error', '加载失败') } }
async function submitMaterialInput() { try { await productionApi.recordMaterialInput(inputForm.value); flash('success', '投料记录成功'); showInputModal.value = false; loadMaterialInput() } catch (e: any) { flash('error', '投料失败: ' + e.message) } }

// ==================== 环境监测 ====================
async function loadEnvRecords() { try { if (envLine.value) { const d = await productionApi.listEnvRecord(envLine.value); envRecords.value = Array.isArray(d) ? d : [] } } catch (e: any) { flash('error', '加载失败') } }
async function submitEnv() { try { await productionApi.recordEnv(envForm.value); flash('success', '环境数据采集成功'); showEnvModal.value = false; loadEnvRecords() } catch (e: any) { flash('error', '采集失败: ' + e.message) } }

// ==================== 质检记录 ====================
async function loadInspections() { try { const p: Record<string, any> = {}; if (inspFilters.value.bizType) p.bizType = Number(inspFilters.value.bizType); if (inspFilters.value.bizBatchNo) p.bizBatchNo = inspFilters.value.bizBatchNo; if (inspFilters.value.inspectionResult) p.inspectionResult = Number(inspFilters.value.inspectionResult); const d = await productionApi.listInspection(p); inspections.value = Array.isArray(d) ? d : [] } catch (e: any) { flash('error', '加载失败') } }
async function submitInspection() { try { await productionApi.createInspection(inspectionForm.value); flash('success', '质检记录创建成功'); showInspectionModal.value = false; loadInspections() } catch (e: any) { flash('error', '创建失败: ' + e.message) } }

// ==================== 删除确认 ====================
function confirmDeleteProd(id: number) { deletingId.value = id; deleteKind.value = 'prod'; confirmMsg.value = '确定要删除该生产批次吗？'; showConfirm.value = true }
function confirmDeleteProcess(id: number) { deletingId.value = id; deleteKind.value = 'process'; confirmMsg.value = '确定要删除该加工批次吗？'; showConfirm.value = true }
async function doDelete() {
  try {
    if (deleteKind.value === 'prod') { await productionApi.deleteProdBatch(deletingId.value!); flash('success', '删除成功'); showConfirm.value = false; loadProdBatch() }
    else { await productionApi.deleteProcessBatch(deletingId.value!); flash('success', '加工批次删除成功'); showConfirm.value = false; loadProcess() }
  } catch (e: any) { flash('error', '删除失败: ' + e.message) }
}

// ==================== tab switch ====================
function switchTab(t: typeof tab.value) {
  tab.value = t
  if (t === 'prod') loadProdBatch()
  else if (t === 'process') loadProcess()
  else if (t === 'template') loadTemplates()
  else if (t === 'input') loadMaterialInput()
  else if (t === 'env') loadEnvRecords()
  else loadInspections()
}

onMounted(loadProdBatch)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 统计卡片 -->
    <div class="page-stats">
      <div v-for="s in stats" :key="s.label" class="stat-card" :class="s.cls">
        <div class="stat-meta"><span>{{ s.label }}</span><span class="stat-ico">{{ s.icon }}</span></div>
        <b>{{ s.val }}</b>
      </div>
    </div>

    <!-- Tab 栏 -->
    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'prod' }" @click="switchTab('prod')">🏭 生产批次</button>
      <button class="tab-btn" :class="{ active: tab === 'process' }" @click="switchTab('process')">⚙️ 加工批次</button>
      <button class="tab-btn" :class="{ active: tab === 'template' }" @click="switchTab('template')">📐 工艺模板</button>
      <button class="tab-btn" :class="{ active: tab === 'input' }" @click="switchTab('input')">📥 投料记录</button>
      <button class="tab-btn" :class="{ active: tab === 'env' }" @click="switchTab('env')">🌡 环境监测</button>
      <button class="tab-btn" :class="{ active: tab === 'inspection' }" @click="switchTab('inspection')">🔬 质检记录</button>
    </div>

    <!-- ==================== 生产批次 ==================== -->
    <template v-if="tab === 'prod'">
      <div class="search-bar">
        <div class="search-field"><label>产品名称</label><input v-model="filters.productName" placeholder="产品名称" @keyup.enter="loadProdBatch" /></div>
        <div class="search-field"><label>生产线</label><input v-model="filters.productionLine" placeholder="生产线" @keyup.enter="loadProdBatch" /></div>
        <div class="search-field"><label>质检结果</label><select v-model="filters.checkResult"><option value="">全部</option><option value="1">合格</option><option value="2">不合格</option></select></div>
        <div class="search-field"><label>状态</label><select v-model="filters.batchStatus"><option value="">全部</option><option value="1">待生产</option><option value="2">生产中</option><option value="3">生产完成</option></select></div>
        <div class="search-field"><label>绑码</label><select v-model="filters.codeStatus"><option value="">全部</option><option value="1">未绑定</option><option value="2">已绑定</option></select></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadProdBatch">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>生产批次列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ list.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateProd">＋ 新增生产批次</button></div>
      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>生产批次号</th><th>产品名称</th><th>加工批次</th><th>生产线</th><th>计划/实际</th><th>生产日期</th><th>质检</th><th>状态</th><th>绑码</th><th class="col-actions">操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="10" style="text-align:center;padding:32px">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="10"><div class="empty-state"><div class="empty-icon">🏭</div><p>暂无生产批次数据</p></div></td></tr>
            <tr v-for="row in list" :key="row.prodBatchId">
              <td><code style="color:#2666df;font-size:13px">{{ row.batchNo }}</code></td>
              <td><strong>{{ row.productName }}</strong></td>
              <td><code style="font-size:12px">{{ row.processBatchNo || '-' }}</code></td>
              <td>{{ row.productionLine }}</td>
              <td>{{ row.plannedAmount }}/{{ row.actualAmount }}</td>
              <td>{{ row.productionDate }}</td>
              <td><span class="tag" :class="tagClass(checkResultLabels[row.checkResult] || '未检测')">{{ checkResultLabels[row.checkResult] || '未检测' }}</span></td>
              <td><span class="tag" :class="tagClass(prodBatchStatusLabels[row.batchStatus] || '')">{{ prodBatchStatusLabels[row.batchStatus] || '-' }}</span></td>
              <td><span class="tag" :class="row.codeStatus === 2 ? 'tag-success' : 'tag-warn'">{{ codeStatusLabels[row.codeStatus] || '-' }}</span></td>
              <td class="col-actions">
                <button class="btn btn-outline btn-sm" @click="openEditProd(row)">✎</button>
                <button v-if="row.batchStatus === 2" class="btn btn-success btn-sm" style="margin-left:3px" @click="completeProd(row.prodBatchId)">✓ 完成</button>
                <button v-if="row.batchStatus === 3 && row.codeStatus !== 2" class="btn btn-outline btn-sm" style="margin-left:3px" @click="bindCode(row.prodBatchId)">🔗 绑码</button>
                <button v-if="!row.checkResult" class="btn btn-success btn-sm" style="margin-left:3px" @click="qualityCheckProd(row.batchNo, 1)">✓ 合格</button>
                <button v-if="!row.checkResult" class="btn btn-danger btn-sm" style="margin-left:3px" @click="qualityCheckProd(row.batchNo, 2)">✗ 不合格</button>
                <button class="btn btn-outline btn-sm" style="margin-left:3px" @click="traceChain(row.batchNo)">🔍 追溯</button>
                <button class="btn btn-danger btn-sm" style="margin-left:3px" @click="confirmDeleteProd(row.prodBatchId)">🗑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ==================== 加工批次 ==================== -->
    <template v-if="tab === 'process'">
      <div class="search-bar">
        <div class="search-field"><label>产品名称</label><input v-model="processFilters.productName" placeholder="产品名称" @keyup.enter="loadProcess" /></div>
        <div class="search-field"><label>生产线</label><input v-model="processFilters.productionLine" placeholder="生产线" @keyup.enter="loadProcess" /></div>
        <div class="search-field"><label>批次状态</label>
          <select v-model="processFilters.batchStatus"><option value="">全部</option><option value="1">加工中</option><option value="2">加工完成</option><option value="3">已存证</option></select>
        </div>
        <div class="search-field"><label>班次</label>
          <select v-model="processFilters.shift"><option value="">全部</option><option value="1">早班</option><option value="2">中班</option><option value="3">晚班</option></select>
        </div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadProcess">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>加工批次列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ processList.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateProcess">＋ 新增加工批次</button></div>
      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>加工批次号</th><th>产品名称</th><th>工艺模板</th><th>原料批次</th><th>生产线</th><th>班次</th><th>计划数量</th><th>实际温度</th><th>加工日期</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="11" style="text-align:center;padding:32px">加载中...</td></tr>
            <tr v-else-if="!processList.length"><td colspan="11"><div class="empty-state"><div class="empty-icon">⚙️</div><p>暂无加工批次数据</p></div></td></tr>
            <tr v-for="row in processList" :key="row.processBatchId">
              <td><code style="color:#2666df;font-size:13px">{{ row.batchNo }}</code></td>
              <td>{{ row.productName }}</td><td>{{ row.templateName }}</td>
              <td><code style="font-size:12px">{{ row.rawBatchNo }}</code></td>
              <td>{{ row.productionLine }}</td><td>{{ shiftLabels[row.shift] || '-' }}</td>
              <td>{{ row.plannedAmount }}</td><td>{{ row.actualTemp || '-' }}</td>
              <td>{{ row.processDate }}</td>
              <td><span class="tag" :class="tagClass(processBatchStatusLabels[row.batchStatus] || '')">{{ processBatchStatusLabels[row.batchStatus] || '-' }}</span></td>
              <td class="col-actions">
                <button class="btn btn-outline btn-sm" @click="openEditProcess(row)">✎</button>
                <button v-if="row.batchStatus === 1" class="btn btn-success btn-sm" style="margin-left:4px" @click="completeProcess(row.processBatchId)">✓ 完成</button>
                <button class="btn btn-danger btn-sm" style="margin-left:4px" @click="confirmDeleteProcess(row.processBatchId)">🗑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ==================== 工艺模板 ==================== -->
    <template v-if="tab === 'template'">
      <div class="search-bar">
        <div class="search-field"><label>适用产品</label><input v-model="templateFilters.applicableProduct" placeholder="适用产品" @keyup.enter="loadTemplates" /></div>
        <div class="search-field"><label>模板状态</label>
          <select v-model="templateFilters.templateStatus"><option value="">全部</option><option value="1">启用</option><option value="2">停用</option></select>
        </div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadTemplates">🔍 查询</button></div>
      </div>
      <div class="toolbar"><h3>工艺模板列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ templates.length }} 条)</span></h3><button class="btn btn-primary" @click="openCreateTemplate">＋ 新增模板</button></div>
      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>模板名称</th><th>版本</th><th>适用产品</th><th>目标温度</th><th>时长</th><th>压力</th><th>冷却温度</th><th>灌装温度</th><th>pH值</th><th>搅拌速度</th><th>清洁等级</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
          <tbody>
            <tr v-if="!templates.length"><td colspan="13"><div class="empty-state"><div class="empty-icon">📐</div><p>暂无工艺模板数据</p></div></td></tr>
            <tr v-for="row in templates" :key="row.templateId">
              <td><strong>{{ row.templateName }}</strong></td><td>{{ row.version }}</td><td>{{ row.applicableProduct }}</td>
              <td>{{ row.targetTemp || '-' }}</td><td>{{ row.duration || '-' }}</td><td>{{ row.pressure || '-' }}</td>
              <td>{{ row.coolTemp || '-' }}</td><td>{{ row.fillTemp || '-' }}</td><td>{{ row.phValue || '-' }}</td>
              <td>{{ row.stirSpeed || '-' }}</td><td>{{ row.cleanLevel }}</td>
              <td><span class="tag" :class="row.templateStatus === 1 ? 'tag-success' : 'tag-neutral'">{{ row.templateStatus === 1 ? '启用' : '停用' }}</span></td>
              <td class="col-actions">
                <button class="btn btn-outline btn-sm" @click="openEditTemplate(row)">✎</button>
                <button class="btn btn-danger btn-sm" style="margin-left:4px" @click="deleteTemplate(row.templateId)">🗑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ==================== 投料记录 ==================== -->
    <template v-if="tab === 'input'">
      <div class="toolbar"><h3>投料记录 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ materialInputs.length }} 条)</span></h3><button class="btn btn-primary" @click="showInputModal = true">＋ 记录投料</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>原料批次</th><th>原料名称</th><th>投料数量</th><th>单位</th><th>操作员</th><th>投料时间</th></tr></thead>
        <tbody><tr v-if="!materialInputs.length"><td colspan="6"><div class="empty-state"><div class="empty-icon">📥</div><p>暂无投料记录</p></div></td></tr>
          <tr v-for="row in materialInputs" :key="row.inputId"><td><code style="font-size:12px">{{ row.rawBatchNo }}</code></td><td>{{ row.materialName }}</td><td>{{ row.inputAmount }}{{ row.unit }}</td><td>{{ row.unit }}</td><td>{{ row.operator }}</td><td>{{ row.inputTime }}</td></tr>
        </tbody></table></div>
    </template>

    <!-- ==================== 环境监测 ==================== -->
    <template v-if="tab === 'env'">
      <div class="search-bar">
        <div class="search-field"><label>生产线</label><input v-model="envLine" placeholder="输入生产线" @keyup.enter="loadEnvRecords" /></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadEnvRecords">🔍 查询</button><button class="btn btn-outline btn-sm" style="margin-left:8px" @click="showEnvModal = true">＋ 采集环境数据</button></div>
      </div>
      <div class="toolbar"><h3>环境记录 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ envRecords.length }} 条)</span></h3></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>生产线</th><th>温度</th><th>湿度</th><th>洁净度</th><th>是否异常</th><th>异常描述</th><th>记录时间</th></tr></thead>
        <tbody><tr v-if="!envRecords.length"><td colspan="7"><div class="empty-state"><div class="empty-icon">🌡</div><p>暂无环境记录</p></div></td></tr>
          <tr v-for="row in envRecords" :key="row.recordId"><td>{{ row.productionLine }}</td><td>{{ row.temperature }}</td><td>{{ row.humidity }}</td><td>{{ row.cleanliness }}</td><td><span class="tag" :class="row.isAbnormal === 1 ? 'tag-danger' : 'tag-success'">{{ row.isAbnormal === 1 ? '异常' : '正常' }}</span></td><td>{{ row.abnormalDesc || '-' }}</td><td>{{ row.recordTime }}</td></tr>
        </tbody></table></div>
    </template>

    <!-- ==================== 质检记录 ==================== -->
    <template v-if="tab === 'inspection'">
      <div class="search-bar">
        <div class="search-field"><label>业务类型</label><select v-model="inspFilters.bizType"><option value="">全部</option><option value="2">加工</option><option value="3">生产</option></select></div>
        <div class="search-field"><label>业务批次</label><input v-model="inspFilters.bizBatchNo" placeholder="批次号" /></div>
        <div class="search-field"><label>检验结果</label><select v-model="inspFilters.inspectionResult"><option value="">全部</option><option value="1">合格</option><option value="2">不合格</option></select></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadInspections">🔍 查询</button><button class="btn btn-outline btn-sm" style="margin-left:8px" @click="showInspectionModal = true">＋ 新增质检</button></div>
      </div>
      <div class="toolbar"><h3>质检记录 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ inspections.length }} 条)</span></h3></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>检验编号</th><th>业务类型</th><th>业务批次</th><th>检验类型</th><th>检验人</th><th>日期</th><th>结果</th><th>描述</th></tr></thead>
        <tbody><tr v-if="!inspections.length"><td colspan="8"><div class="empty-state"><div class="empty-icon">🔬</div><p>暂无质检记录</p></div></td></tr>
          <tr v-for="row in inspections" :key="row.inspectionId"><td><code style="font-size:12px">{{ row.inspectionNo }}</code></td>
            <td>{{ ['','原料','加工','生产','冷链','销售'][row.bizType] || row.bizType }}</td><td><code style="font-size:12px">{{ row.bizBatchNo }}</code></td>
            <td>{{ ['','自检','抽检','全检'][row.inspectionType] || '其他' }}</td><td>{{ row.inspector }}</td><td>{{ row.inspectionDate }}</td>
            <td><span class="tag" :class="row.inspectionResult === 1 ? 'tag-success' : 'tag-danger'">{{ row.inspectionResult === 1 ? '合格' : '不合格' }}</span></td><td>{{ row.resultDesc || '-' }}</td></tr>
        </tbody></table></div>
    </template>

    <!-- ==================== 模态框：生产批次 ==================== -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal"><div class="modal-header"><h3>{{ editing ? '编辑生产批次' : '新增生产批次' }}</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">基</span>基本信息</div>
            <div class="form-row"><div class="form-group"><label>批次号</label><input v-model="form.batchNo" /></div><div class="form-group"><label>产品名称 *</label><input v-model="form.productName" /></div></div>
            <div class="form-row"><div class="form-group"><label>加工批次号</label><input v-model="form.processBatchNo" /></div><div class="form-group"><label>生产线</label><input v-model="form.productionLine" /></div></div>
            <div class="form-row"><div class="form-group"><label>计划数量</label><input v-model="form.plannedAmount" type="number" /></div><div class="form-group"><label>实际数量</label><input v-model="form.actualAmount" type="number" /></div></div>
            <div class="form-row"><div class="form-group"><label>生产日期</label><input v-model="form.productionDate" /></div></div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="form.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showModal = false">取消</button><button class="btn btn-primary" @click="submitProdBatch">{{ editing ? '保存' : '创建' }}</button></div>
      </div>
    </div>

    <!-- ==================== 模态框：加工批次 ==================== -->
    <div v-if="showProcessModal" class="modal-overlay" @click.self="showProcessModal = false">
      <div class="modal" style="width:720px">
        <div class="modal-header"><h3>{{ editingProcess ? '编辑加工批次' : '新增加工批次' }}</h3><button class="modal-close" @click="showProcessModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">基</span>基本信息</div>
            <div class="form-row"><div class="form-group"><label>批次号</label><input v-model="processForm.batchNo" placeholder="自动生成可留空" /></div><div class="form-group"><label>产品名称 *</label><input v-model="processForm.productName" placeholder="如：鲜奶基料" /></div></div>
            <div class="form-row"><div class="form-group"><label>工艺模板</label><input v-model="processForm.templateName" placeholder="如：巴氏杀菌标准工艺" /></div><div class="form-group"><label>原料批次号 *</label><input v-model="processForm.rawBatchNo" placeholder="关联原料批次号" /></div></div>
            <div class="form-row"><div class="form-group"><label>生产线</label><input v-model="processForm.productionLine" placeholder="生产线" /></div><div class="form-group"><label>计划数量</label><input v-model="processForm.plannedAmount" placeholder="计划数量" type="number" /></div></div>
            <div class="form-row"><div class="form-group"><label>加工日期</label><input v-model="processForm.processDate" placeholder="2025-01-01" /></div><div class="form-group"><label>操作员</label><input v-model="processForm.operator" placeholder="如：李工" /></div></div>
            <div class="form-row"><div class="form-group"><label>班次</label>
              <select v-model.number="processForm.shift"><option :value="0">请选择</option><option :value="1">早班</option><option :value="2">中班</option><option :value="3">晚班</option></select>
            </div></div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">参</span>实际工艺参数</div>
            <div class="form-row">
              <div class="form-group"><label>杀菌温度 (℃)</label><input v-model="processForm.actualTemp" placeholder="72.0" /></div>
              <div class="form-group"><label>杀菌时长 (s)</label><input v-model="processForm.actualDuration" placeholder="15" /></div>
              <div class="form-group"><label>均质压力 (MPa)</label><input v-model="processForm.actualPressure" placeholder="18" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>冷却温度 (℃)</label><input v-model="processForm.actualCoolTemp" placeholder="4.0" /></div>
              <div class="form-group"><label>灌装温度 (℃)</label><input v-model="processForm.actualFillTemp" placeholder="6.0" /></div>
              <div class="form-group"><label>pH 值</label><input v-model="processForm.actualPh" placeholder="6.8" /></div>
            </div>
            <div class="form-row"><div class="form-group"><label>粘度 (mPa·s)</label><input v-model="processForm.actualViscosity" placeholder="120" /></div></div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="processForm.remark" placeholder="加工相关补充说明" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showProcessModal = false">取消</button><button class="btn btn-primary" @click="submitProcess">{{ editingProcess ? '保存' : '创建' }}</button></div>
      </div>
    </div>

    <!-- ==================== 模态框：工艺模板 ==================== -->
    <div v-if="showTemplateModal" class="modal-overlay" @click.self="showTemplateModal = false">
      <div class="modal">
        <div class="modal-header"><h3>{{ editingTemplate ? '编辑工艺模板' : '新增工艺模板' }}</h3><button class="modal-close" @click="showTemplateModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">模</span>模板信息</div>
            <div class="form-row"><div class="form-group"><label>模板名称 *</label><input v-model="templateForm.templateName" placeholder="如：巴氏杀菌标准工艺" /></div><div class="form-group"><label>版本</label><input v-model="templateForm.version" placeholder="V3.2" /></div></div>
            <div class="form-row"><div class="form-group"><label>适用产品</label><input v-model="templateForm.applicableProduct" placeholder="如：鲜奶基料" /></div><div class="form-group"><label>杀菌温度 (℃)</label><input v-model="templateForm.targetTemp" placeholder="72.0" /></div></div>
            <div class="form-row"><div class="form-group"><label>时长 (s)</label><input v-model="templateForm.duration" placeholder="15" /></div><div class="form-group"><label>压力 (MPa)</label><input v-model="templateForm.pressure" placeholder="18" /></div></div>
            <div class="form-row"><div class="form-group"><label>冷却温度 (℃)</label><input v-model="templateForm.coolTemp" placeholder="4.0" /></div><div class="form-group"><label>灌装温度 (℃)</label><input v-model="templateForm.fillTemp" placeholder="6.0" /></div></div>
            <div class="form-row"><div class="form-group"><label>搅拌速度 (rpm)</label><input v-model="templateForm.stirSpeed" placeholder="200" /></div><div class="form-group"><label>pH 值</label><input v-model="templateForm.phValue" placeholder="6.8" /></div></div>
            <div class="form-row"><div class="form-group"><label>粘度 (mPa·s)</label><input v-model="templateForm.viscosity" placeholder="120" /></div><div class="form-group"><label>清洁等级</label><input v-model.number="templateForm.cleanLevel" type="number" placeholder="十万级=1 万级=2" /></div></div>
            <div class="form-group"><label>模板状态</label>
              <select v-model.number="templateForm.templateStatus"><option :value="1">启用</option><option :value="2">停用</option></select>
            </div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="templateForm.remark" placeholder="模板更新说明" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showTemplateModal = false">取消</button>
          <button class="btn btn-primary" @click="submitTemplate">{{ editingTemplate ? '更新模板' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- ==================== 模态框：投料记录 ==================== -->
    <div v-if="showInputModal" class="modal-overlay" @click.self="showInputModal = false">
      <div class="modal"><div class="modal-header"><h3>记录投料</h3><button class="modal-close" @click="showInputModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row"><div class="form-group"><label>原料批次 *</label><input v-model="inputForm.rawBatchNo" /></div><div class="form-group"><label>原料名称</label><input v-model="inputForm.materialName" /></div></div>
          <div class="form-row"><div class="form-group"><label>投料数量</label><input v-model="inputForm.inputAmount" type="number" /></div><div class="form-group"><label>单位</label><input v-model="inputForm.unit" placeholder="kg" /></div></div>
          <div class="form-row"><div class="form-group"><label>操作员</label><input v-model="inputForm.operator" /></div><div class="form-group"><label>投料时间</label><input v-model="inputForm.inputTime" /></div></div>
          <div class="form-group"><label>备注</label><textarea v-model="inputForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showInputModal = false">取消</button><button class="btn btn-primary" @click="submitMaterialInput">记录</button></div>
      </div>
    </div>

    <!-- ==================== 模态框：环境采集 ==================== -->
    <div v-if="showEnvModal" class="modal-overlay" @click.self="showEnvModal = false">
      <div class="modal"><div class="modal-header"><h3>生产环境采集</h3><button class="modal-close" @click="showEnvModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">环</span>环境数据</div>
            <div class="form-row"><div class="form-group"><label>生产线 *</label><input v-model="envForm.productionLine" placeholder="L-05 自动化线" /></div><div class="form-group"><label>采集时间</label><input v-model="envForm.recordTime" placeholder="2026-06-11T09:00" /></div></div>
            <div class="form-row"><div class="form-group"><label>车间温度</label><input v-model="envForm.temperature" placeholder="22.5 ℃" /></div><div class="form-group"><label>车间湿度</label><input v-model="envForm.humidity" placeholder="55%RH" /></div></div>
            <div class="form-row"><div class="form-group"><label>洁净度等级</label><input v-model="envForm.cleanliness" placeholder="十万级" /></div><div class="form-group"><label>是否异常</label><select v-model.number="envForm.isAbnormal"><option :value="0">正常</option><option :value="1">异常</option></select></div></div>
            <div class="form-group"><label>异常描述</label><textarea v-model="envForm.abnormalDesc" placeholder="如异常请描述" /></div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showEnvModal = false">取消</button><button class="btn btn-primary" @click="submitEnv">确认提交</button></div>
      </div>
    </div>

    <!-- ==================== 模态框：质检录入 ==================== -->
    <div v-if="showInspectionModal" class="modal-overlay" @click.self="showInspectionModal = false">
      <div class="modal"><div class="modal-header"><h3>生产质检录入</h3><button class="modal-close" @click="showInspectionModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row"><div class="form-group"><label>检验编号</label><input v-model="inspectionForm.inspectionNo" /></div><div class="form-group"><label>业务类型 *</label><select v-model.number="inspectionForm.bizType"><option :value="2">加工</option><option :value="3">生产</option></select></div></div>
          <div class="form-row"><div class="form-group"><label>业务批次号 *</label><input v-model="inspectionForm.bizBatchNo" /></div><div class="form-group"><label>检验人</label><input v-model="inspectionForm.inspector" placeholder="张宏伟" /></div></div>
          <div class="form-row"><div class="form-group"><label>检验日期</label><input v-model="inspectionForm.inspectionDate" /></div><div class="form-group"><label>检验类型</label><select v-model.number="inspectionForm.inspectionType"><option :value="1">自检</option><option :value="2">抽检</option><option :value="3">全检</option></select></div></div>
          <div class="form-row"><div class="form-group"><label>检验结果 *</label><select v-model.number="inspectionForm.inspectionResult"><option :value="1">合格</option><option :value="2">不合格</option></select></div></div>
          <div class="form-group"><label>结果描述</label><textarea v-model="inspectionForm.resultDesc" /></div>
          <div class="form-group"><label>备注</label><textarea v-model="inspectionForm.remark" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showInspectionModal = false">取消</button><button class="btn btn-primary" @click="submitInspection">确认提交</button></div>
      </div>
    </div>

    <!-- ==================== 模态框：全链路追溯 ==================== -->
    <div v-if="showChainModal" class="modal-overlay" @click.self="showChainModal = false">
      <div class="modal" style="width:800px"><div class="modal-header"><h3>生产全链路追溯</h3><button class="modal-close" @click="showChainModal = false">✕</button></div>
        <div class="modal-body">
          <div class="chain-flow" v-if="chainData">
            <div class="chain-node"><div class="chain-ico">供</div><strong>原料供应商</strong><div class="muted">{{ chainData.rawBatchNo || '-' }}</div></div>
            <div class="chain-node"><div class="chain-ico">原</div><strong>原料入库</strong><div class="muted">批次匹配</div></div>
            <div class="chain-node"><div class="chain-ico">工</div><strong>加工生产</strong><div class="muted">{{ chainData.processBatchNo || '-' }}</div></div>
            <div class="chain-node"><div class="chain-ico">产</div><strong>生产批次</strong><div class="muted">{{ chainData.prodBatchNo || '-' }}</div></div>
            <div class="chain-node"><div class="chain-ico">冷</div><strong>冷链运输</strong><div class="muted">全程温控</div></div>
            <div class="chain-node"><div class="chain-ico">售</div><strong>销售终端</strong><div class="muted">终端入库</div></div>
            <div class="chain-node"><div class="chain-ico">监</div><strong>监管核验</strong><div class="muted">Hash验真</div></div>
          </div>
          <pre v-if="chainData" style="background:#f8fafc;padding:12px;border-radius:8px;font-size:12px;max-height:200px;overflow:auto">{{ JSON.stringify(chainData, null, 2) }}</pre>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showChainModal = false">关闭</button></div>
      </div>
    </div>

    <!-- ==================== 删除确认 ==================== -->
    <div v-if="showConfirm" class="confirm-overlay" @click.self="showConfirm = false">
      <div class="confirm-box">
        <div class="confirm-icon">⚠️</div><h3>确认删除</h3><p>{{ confirmMsg }}</p>
        <div class="confirm-actions"><button class="btn btn-outline" @click="showConfirm = false">取消</button><button class="btn btn-danger" @click="doDelete">确认删除</button></div>
      </div>
    </div>
  </div>
</template>
