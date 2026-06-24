<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Check, Close, Connection, Delete, DocumentChecked, Edit, Link, Plus, Search, VideoPlay } from '@element-plus/icons-vue'
import { productionApi } from '../services/api'

const tab = ref<'prod' | 'process' | 'template' | 'input' | 'env' | 'inspection'>('prod')
const loading = ref(false)
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)

const showModal = ref(false); const showProcessModal = ref(false); const showTemplateModal = ref(false)
const showInputModal = ref(false); const showEnvModal = ref(false); const showInspectionModal = ref(false)
const showConfirm = ref(false); const showChainModal = ref(false); const showQcProdModal = ref(false)
const confirmTitle = ref('确认操作'); const confirmMsg = ref(''); const confirmBtnLabel = ref('确认'); const confirmBtnClass = ref('danger-fill')
const confirmCallback = ref<null | (() => void)>(null)

const list = ref<any[]>([]); const processList = ref<any[]>([]); const templates = ref<any[]>([]); const materialInputs = ref<any[]>([]); const envRecords = ref<any[]>([]); const inspections = ref<any[]>([])
const editing = ref<any>(null); const editingProcess = ref<any>(null); const editingTemplate = ref<any>(null); const qcTarget = ref<any>(null); const qcResult = ref(1); const chainData = ref<any>(null)

const filters = ref({ productName: '', productionLine: '', checkResult: '', batchStatus: '', codeStatus: '' })
const processFilters = ref({ productName: '', productionLine: '', batchStatus: '', shift: '' })
const templateFilters = ref({ applicableProduct: '', templateStatus: '' })
const envLine = ref(''); const inspFilters = ref({ bizType: '', bizBatchNo: '', inspectionType: '', inspectionResult: '' })

const form = ref({ batchNo: '', productName: '', processBatchNo: '', productionLine: '', plannedAmount: '', actualAmount: '', productionDate: '', remark: '' })
const processForm = ref({ batchNo: '', productName: '', templateName: '', rawBatchNo: '', plannedAmount: '', processDate: '', operator: '', productionLine: '', shift: 0, actualTemp: '', actualDuration: '', actualPressure: '', actualCoolTemp: '', actualFillTemp: '', actualPh: '', actualViscosity: '', remark: '' })
const templateForm = ref({ templateName: '', version: '', applicableProduct: '', targetTemp: '', duration: '', pressure: '', coolTemp: '', fillTemp: '', stirSpeed: '', phValue: '', viscosity: '', cleanLevel: 0, templateStatus: 1, remark: '' })
const inputForm = ref({ rawBatchNo: '', materialName: '', inputAmount: '', unit: '', operator: '', inputTime: '', remark: '' })
const envForm = ref({ productionLine: '', temperature: '', humidity: '', cleanliness: '', isAbnormal: 0, abnormalDesc: '', recordTime: '' })
const inspectionForm = ref({ inspectionNo: '', bizType: 2, bizBatchNo: '', inspectionType: 1, inspector: '', inspectionDate: '', inspectionResult: 1, resultDesc: '', remark: '' })

const checkResultLabels = ['', '合格', '不合格']; const prodBatchStatusLabels = ['', '待生产', '生产中', '生产完成', '已废弃']; const codeStatusLabels = ['', '未绑定', '已绑定']; const processBatchStatusLabels = ['', '加工中', '加工完成', '已存证', '已废弃']; const shiftLabels = ['', '早班', '中班', '晚班']

const stats = computed(() => {
  switch (tab.value) {
    case 'prod': return [
      { label: '生产批次总数', icon: Edit, cls: '', val: list.value.length },
      { label: '质检合格', icon: Check, cls: 'green', val: list.value.filter((r: any) => r.checkResult === 1).length },
      { label: '生产完成', icon: DocumentChecked, cls: 'green', val: list.value.filter((r: any) => r.batchStatus === 3).length },
      { label: '已绑码', icon: Link, cls: 'amber', val: list.value.filter((r: any) => r.codeStatus === 2).length },
    ]
    case 'process': return [
      { label: '加工批次总数', icon: Edit, cls: '', val: processList.value.length },
      { label: '加工中', icon: VideoPlay, cls: 'amber', val: processList.value.filter((r: any) => r.batchStatus === 1).length },
      { label: '已完成/存证', icon: Check, cls: 'green', val: processList.value.filter((r: any) => r.batchStatus === 2 || r.batchStatus === 3).length },
      { label: '工艺模板', icon: DocumentChecked, cls: '', val: templates.value.length },
    ]
    case 'template': return [
      { label: '工艺模板总数', icon: DocumentChecked, cls: '', val: templates.value.length },
      { label: '启用', icon: Check, cls: 'green', val: templates.value.filter((r: any) => r.templateStatus === 1).length },
      { label: '停用', icon: Close, cls: '', val: templates.value.filter((r: any) => r.templateStatus === 2).length },
    ]
    case 'input': return [{ label: '投料记录', icon: Edit, cls: '', val: materialInputs.value.length }]
    case 'env': return [
      { label: '环境记录', icon: Edit, cls: '', val: envRecords.value.length },
      { label: '异常', icon: Close, cls: 'amber', val: envRecords.value.filter((r: any) => r.isAbnormal === 1).length },
    ]
    case 'inspection': return [
      { label: '质检记录', icon: Edit, cls: '', val: inspections.value.length },
      { label: '合格', icon: Check, cls: 'green', val: inspections.value.filter((r: any) => r.inspectionResult === 1).length },
      { label: '不合格', icon: Close, cls: 'amber', val: inspections.value.filter((r: any) => r.inspectionResult === 2).length },
    ]
    default: return []
  }
})

const currentUser = sessionStorage.getItem('fts-admin-user') || ''
function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function statusClass(s: string) {
  if (['已完成', '已存证', '已绑定', '已启用', '合格', '正常'].some(x => s.includes(x))) return 'status-active'
  if (['加工中', '进行中', '待', '未', '生产中'].some(x => s.includes(x))) return 'status-pending'
  return 'status-active'
}

// Production Batch
async function loadProdBatch() {
  loading.value = true
  try {
    const p: Record<string, any> = {}; if (filters.value.productName) p.productName = filters.value.productName; if (filters.value.productionLine) p.productionLine = filters.value.productionLine; if (filters.value.checkResult) p.checkResult = Number(filters.value.checkResult); if (filters.value.batchStatus) p.batchStatus = Number(filters.value.batchStatus); if (filters.value.codeStatus) p.codeStatus = Number(filters.value.codeStatus)
    const data = await productionApi.listProdBatch(p); list.value = Array.isArray(data) ? data : []
  } catch (e: any) { notify('error', '加载失败: ' + e.message) } finally { loading.value = false }
}
function openCreateProd() { editing.value = null; form.value = { batchNo: '', productName: '', processBatchNo: '', productionLine: '', plannedAmount: '', actualAmount: '', productionDate: '', remark: '' }; showModal.value = true }
function openEditProd(row: any) { editing.value = row; form.value = { batchNo: row.batchNo ?? '', productName: row.productName ?? '', processBatchNo: row.processBatchNo ?? '', productionLine: row.productionLine ?? '', plannedAmount: row.plannedAmount ?? '', actualAmount: row.actualAmount ?? '', productionDate: row.productionDate ?? '', remark: row.remark ?? '' }; showModal.value = true }
function submitProdBatch() {
  if (!form.value.productName.trim()) { notify('error', '请填写产品名称'); return }; if (!form.value.productionLine.trim()) { notify('error', '请填写生产线'); return }; if (!form.value.productionDate.trim()) { notify('error', '请选择生产日期'); return }
  const label = editing.value ? '更新' : '创建'; confirmTitle.value = `确认${label}`; confirmMsg.value = `确认${label}该生产批次？`; confirmBtnLabel.value = `确认${label}`; confirmBtnClass.value = 'primary'
  confirmCallback.value = async () => {
    try {
      const data: Record<string, any> = { ...form.value }; if (!data.plannedAmount) data.plannedAmount = '0'; if (!data.actualAmount) data.actualAmount = '0'
      if (editing.value) { data.prodBatchId = editing.value.prodBatchId; await productionApi.updateProdBatch(data); notify('success', '生产批次更新成功') } else { await productionApi.createProdBatch(data); notify('success', '生产批次创建成功') }
      showModal.value = false; showConfirm.value = false; loadProdBatch()
    } catch (e: any) { notify('error', '操作失败: ' + e.message) }
  }; showConfirm.value = true
}
function startProd(id: number) { confirmTitle.value = '确认开始生产'; confirmMsg.value = '确认开始该批次的生产？'; confirmBtnLabel.value = '确认开始'; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { await productionApi.startProdBatch(id); notify('success', '生产已开始'); showConfirm.value = false; loadProdBatch() } catch (e: any) { notify('error', '操作失败: ' + e.message) } }; showConfirm.value = true }
function completeProd(id: number) { confirmTitle.value = '确认完成'; confirmMsg.value = '确认完成该生产批次？'; confirmBtnLabel.value = '确认完成'; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { await productionApi.completeProdBatch(id); notify('success', '生产批次已完成'); showConfirm.value = false; loadProdBatch() } catch (e: any) { notify('error', '操作失败: ' + e.message) } }; showConfirm.value = true }
function bindCode(id: number) { confirmTitle.value = '确认绑码'; confirmMsg.value = '确认为该生产批次绑定溯源码？'; confirmBtnLabel.value = '确认绑码'; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { await productionApi.bindCode(id); notify('success', '溯源码绑定完成'); showConfirm.value = false; loadProdBatch() } catch (e: any) { notify('error', '绑码失败: ' + e.message) } }; showConfirm.value = true }
function openQcProd(row: any) { qcTarget.value = row; qcResult.value = 1; showQcProdModal.value = true }
function submitQcProd() {
  const resultLabel = qcResult.value === 1 ? '合格' : '不合格'; confirmTitle.value = '确认质检'; confirmMsg.value = `确认将该批次质检结果标记为"${resultLabel}"？`; confirmBtnLabel.value = `确认${resultLabel}`; confirmBtnClass.value = qcResult.value === 1 ? 'primary' : 'danger-fill'
  confirmCallback.value = async () => { try { await productionApi.qualityCheckProd(qcTarget.value.batchNo, qcResult.value); notify('success', `质检结果已录入（${resultLabel}）`); showQcProdModal.value = false; showConfirm.value = false; loadProdBatch() } catch (e: any) { notify('error', '质检失败: ' + e.message) } }; showConfirm.value = true
}
async function traceChain(batchNo: string) { try { const result = await productionApi.traceProcessChain(batchNo); chainData.value = result; showChainModal.value = true } catch (e: any) { notify('error', '追溯失败: ' + e.message) } }

// Process
async function loadProcess() { loading.value = true; try { const p: Record<string, any> = {}; if (processFilters.value.productName) p.productName = processFilters.value.productName; if (processFilters.value.productionLine) p.productionLine = processFilters.value.productionLine; if (processFilters.value.batchStatus) p.batchStatus = Number(processFilters.value.batchStatus); if (processFilters.value.shift) p.shift = Number(processFilters.value.shift); const data = await productionApi.listProcessBatch(p); processList.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载加工批次失败: ' + e.message) } finally { loading.value = false } }
function openCreateProcess() { editingProcess.value = null; processForm.value = { batchNo: '', productName: '', templateName: '', rawBatchNo: '', plannedAmount: '', processDate: '', operator: '', productionLine: '', shift: 0, actualTemp: '', actualDuration: '', actualPressure: '', actualCoolTemp: '', actualFillTemp: '', actualPh: '', actualViscosity: '', remark: '' }; showProcessModal.value = true }
function openEditProcess(row: any) { editingProcess.value = row; processForm.value = { batchNo: row.batchNo ?? '', productName: row.productName ?? '', templateName: row.templateName ?? '', rawBatchNo: row.rawBatchNo ?? '', plannedAmount: row.plannedAmount ?? '', processDate: row.processDate ?? '', operator: row.operator ?? '', productionLine: row.productionLine ?? '', shift: row.shift ?? 0, actualTemp: row.actualTemp ?? '', actualDuration: row.actualDuration ?? '', actualPressure: row.actualPressure ?? '', actualCoolTemp: row.actualCoolTemp ?? '', actualFillTemp: row.actualFillTemp ?? '', actualPh: row.actualPh ?? '', actualViscosity: row.actualViscosity ?? '', remark: row.remark ?? '' }; showProcessModal.value = true }
function submitProcess() { if (!processForm.value.productName.trim()) { notify('error', '请填写产品名称'); return }; if (!processForm.value.templateName.trim()) { notify('error', '请填写工艺模板名称'); return }; if (!processForm.value.rawBatchNo.trim()) { notify('error', '请填写原料批次号'); return }; if (!processForm.value.productionLine.trim()) { notify('error', '请填写生产线'); return }; const label = editingProcess.value ? '更新' : '创建'; confirmTitle.value = `确认${label}`; confirmMsg.value = `确认${label}该加工批次？`; confirmBtnLabel.value = `确认${label}`; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { const data: Record<string, any> = { ...processForm.value }; if (editingProcess.value) { data.processBatchId = editingProcess.value.processBatchId; await productionApi.updateProcessBatch(data); notify('success', '加工批次更新成功') } else { await productionApi.createProcessBatch(data); notify('success', '加工批次创建成功') } showProcessModal.value = false; showConfirm.value = false; loadProcess() } catch (e: any) { notify('error', '操作失败: ' + e.message) } }; showConfirm.value = true }
function completeProcess(id: number) { confirmTitle.value = '确认完成'; confirmMsg.value = '确认完成该加工批次？'; confirmBtnLabel.value = '确认完成'; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { await productionApi.completeProcessBatch(id); notify('success', '加工批次已完成'); showConfirm.value = false; loadProcess() } catch (e: any) { notify('error', '操作失败: ' + e.message) } }; showConfirm.value = true }

// Template
async function loadTemplates() { try { const p: Record<string, any> = {}; if (templateFilters.value.applicableProduct) p.applicableProduct = templateFilters.value.applicableProduct; if (templateFilters.value.templateStatus) p.templateStatus = Number(templateFilters.value.templateStatus); const data = await productionApi.listTemplate(p); templates.value = Array.isArray(data) ? data : [] } catch (e: any) { notify('error', '加载失败') } }
function openCreateTemplate() { editingTemplate.value = null; templateForm.value = { templateName: '', version: '', applicableProduct: '', targetTemp: '', duration: '', pressure: '', coolTemp: '', fillTemp: '', stirSpeed: '', phValue: '', viscosity: '', cleanLevel: 0, templateStatus: 1, remark: '' }; showTemplateModal.value = true }
function openEditTemplate(row: any) { editingTemplate.value = row; templateForm.value = { templateName: row.templateName ?? '', version: row.version ?? '', applicableProduct: row.applicableProduct ?? '', targetTemp: row.targetTemp ?? '', duration: row.duration ?? '', pressure: row.pressure ?? '', coolTemp: row.coolTemp ?? '', fillTemp: row.fillTemp ?? '', stirSpeed: row.stirSpeed ?? '', phValue: row.phValue ?? '', viscosity: row.viscosity ?? '', cleanLevel: row.cleanLevel ?? 0, templateStatus: row.templateStatus ?? 1, remark: row.remark ?? '' }; showTemplateModal.value = true }
function submitTemplate() { if (!templateForm.value.templateName.trim()) { notify('error', '请填写模板名称'); return }; const label = editingTemplate.value ? '更新' : '创建'; confirmTitle.value = `确认${label}`; confirmMsg.value = `确认${label}该工艺模板？`; confirmBtnLabel.value = `确认${label}`; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { const data: Record<string, any> = { ...templateForm.value }; if (editingTemplate.value) { data.templateId = editingTemplate.value.templateId; await productionApi.updateTemplate(data); notify('success', '工艺模板更新成功') } else { await productionApi.createTemplate(data); notify('success', '工艺模板创建成功') } showTemplateModal.value = false; showConfirm.value = false; loadTemplates() } catch (e: any) { notify('error', '操作失败: ' + e.message) } }; showConfirm.value = true }
function confirmDeleteTemplate(id: number) { confirmTitle.value = '确认删除'; confirmMsg.value = '确定要删除该工艺模板吗？此操作不可恢复。'; confirmBtnLabel.value = '确认删除'; confirmBtnClass.value = 'danger-fill'; confirmCallback.value = async () => { try { await productionApi.deleteTemplate(id); notify('success', '工艺模板删除成功'); showConfirm.value = false; loadTemplates() } catch (e: any) { notify('error', '删除失败: ' + e.message) } }; showConfirm.value = true }

// Material Input
async function loadMaterialInput() { try { const d = await productionApi.listMaterialInput(); materialInputs.value = Array.isArray(d) ? d : [] } catch (e: any) { notify('error', '加载失败') } }
function submitMaterialInput() { confirmTitle.value = '确认投料'; confirmMsg.value = '确认记录该投料信息？'; confirmBtnLabel.value = '确认记录'; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { await productionApi.recordMaterialInput(inputForm.value); notify('success', '投料记录成功'); showInputModal.value = false; showConfirm.value = false; loadMaterialInput() } catch (e: any) { notify('error', '投料失败: ' + e.message) } }; showConfirm.value = true }

// Env Record
async function loadEnvRecords() { try { if (envLine.value) { const d = await productionApi.listEnvRecord(envLine.value); envRecords.value = Array.isArray(d) ? d : [] } } catch (e: any) { notify('error', '加载失败') } }
function submitEnv() { confirmTitle.value = '确认采集'; confirmMsg.value = '确认提交该环境监测数据？'; confirmBtnLabel.value = '确认提交'; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { await productionApi.recordEnv(envForm.value); notify('success', '环境数据采集成功'); showEnvModal.value = false; showConfirm.value = false; loadEnvRecords() } catch (e: any) { notify('error', '采集失败: ' + e.message) } }; showConfirm.value = true }

// Inspection
async function loadInspections() { try { const p: Record<string, any> = {}; if (inspFilters.value.bizType) p.bizType = Number(inspFilters.value.bizType); if (inspFilters.value.bizBatchNo) p.bizBatchNo = inspFilters.value.bizBatchNo; if (inspFilters.value.inspectionResult) p.inspectionResult = Number(inspFilters.value.inspectionResult); const d = await productionApi.listInspection(p); inspections.value = Array.isArray(d) ? d : [] } catch (e: any) { notify('error', '加载失败') } }
function submitInspection() { confirmTitle.value = '确认提交'; confirmMsg.value = '确认创建该质检记录？'; confirmBtnLabel.value = '确认提交'; confirmBtnClass.value = 'primary'; confirmCallback.value = async () => { try { await productionApi.createInspection(inspectionForm.value); notify('success', '质检记录创建成功'); showInspectionModal.value = false; showConfirm.value = false; loadInspections() } catch (e: any) { notify('error', '创建失败: ' + e.message) } }; showConfirm.value = true }

// Delete
function confirmDeleteProd(id: number) { confirmTitle.value = '确认删除'; confirmMsg.value = '确定要删除该生产批次吗？'; confirmBtnLabel.value = '确认删除'; confirmBtnClass.value = 'danger-fill'; confirmCallback.value = async () => { try { await productionApi.deleteProdBatch(id); notify('success', '删除成功'); showConfirm.value = false; loadProdBatch() } catch (e: any) { notify('error', '删除失败: ' + e.message) } }; showConfirm.value = true }
function confirmDeleteProcess(id: number) { confirmTitle.value = '确认删除'; confirmMsg.value = '确定要删除该加工批次吗？'; confirmBtnLabel.value = '确认删除'; confirmBtnClass.value = 'danger-fill'; confirmCallback.value = async () => { try { await productionApi.deleteProcessBatch(id); notify('success', '加工批次删除成功'); showConfirm.value = false; loadProcess() } catch (e: any) { notify('error', '删除失败: ' + e.message) } }; showConfirm.value = true }

function switchTab(t: typeof tab.value) { tab.value = t; if (t === 'prod') loadProdBatch(); else if (t === 'process') loadProcess(); else if (t === 'template') loadTemplates(); else if (t === 'input') loadMaterialInput(); else if (t === 'env') loadEnvRecords(); else loadInspections() }
function doConfirm() { if (confirmCallback.value) { confirmCallback.value() } }
onMounted(loadProdBatch)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <section class="trace-stats">
      <article v-for="s in stats" :key="s.label" :class="s.cls">
        <span><el-icon><component :is="s.icon" /></el-icon> {{ s.label }}</span>
        <b>{{ s.val }}</b><em>条记录</em>
      </article>
    </section>

    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'prod' }" @click="switchTab('prod')"><el-icon><Edit /></el-icon> 生产批次</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'process' }" @click="switchTab('process')"><el-icon><Edit /></el-icon> 加工批次</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'template' }" @click="switchTab('template')"><el-icon><DocumentChecked /></el-icon> 工艺模板</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'input' }" @click="switchTab('input')"><el-icon><Plus /></el-icon> 投料记录</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'env' }" @click="switchTab('env')"><el-icon><Search /></el-icon> 环境监测</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'inspection' }" @click="switchTab('inspection')"><el-icon><DocumentChecked /></el-icon> 质检记录</button>
    </div>

    <!-- Production Batch -->
    <template v-if="tab === 'prod'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid">
          <label>产品名称<input v-model="filters.productName" placeholder="产品名称" @keyup.enter="loadProdBatch" /></label>
          <label>生产线<input v-model="filters.productionLine" placeholder="生产线" @keyup.enter="loadProdBatch" /></label>
          <label>质检结果<select v-model="filters.checkResult"><option value="">全部</option><option value="1">合格</option><option value="2">不合格</option></select></label>
          <label>状态<select v-model="filters.batchStatus"><option value="">全部</option><option value="1">待生产</option><option value="2">生产中</option><option value="3">生产完成</option></select></label>
          <label>绑码<select v-model="filters.codeStatus"><option value="">全部</option><option value="1">未绑定</option><option value="2">已绑定</option></select></label>
          <div class="filter-actions"><button class="primary" @click="loadProdBatch"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>生产台账</p><h2>生产批次列表</h2></div><button class="primary create" @click="openCreateProd"><el-icon><Plus /></el-icon> 新增生产批次</button></header>
        <div class="table-wrap"><table><thead><tr><th>生产批次号</th><th>产品名称</th><th>加工批次</th><th>生产线</th><th>计划/实际</th><th>生产日期</th><th>质检</th><th>状态</th><th>绑码</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="10" class="empty">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="10" class="empty">暂无生产批次数据</td></tr>
            <tr v-for="row in list" :key="row.prodBatchId">
              <td><code>{{ row.batchNo }}</code></td><td><strong>{{ row.productName }}</strong></td><td><code>{{ row.processBatchNo || '-' }}</code></td><td>{{ row.productionLine }}</td><td>{{ row.plannedAmount }}/{{ row.actualAmount }}</td><td>{{ row.productionDate }}</td>
              <td><span class="status" :class="statusClass(checkResultLabels[row.checkResult] || '未检测')">{{ checkResultLabels[row.checkResult] || '未检测' }}</span></td>
              <td><span class="status" :class="statusClass(prodBatchStatusLabels[row.batchStatus] || '')">{{ prodBatchStatusLabels[row.batchStatus] || '-' }}</span></td>
              <td><span class="status" :class="row.codeStatus === 2 ? 'status-active' : 'status-pending'">{{ codeStatusLabels[row.codeStatus] || '-' }}</span></td>
              <td class="actions">
                <button @click="openEditProd(row)"><el-icon><Edit /></el-icon> 编辑</button>
                <button v-if="row.batchStatus === 1" @click="startProd(row.prodBatchId)"><el-icon><VideoPlay /></el-icon> 开始生产</button>
                <button v-if="row.batchStatus === 2" @click="completeProd(row.prodBatchId)"><el-icon><Check /></el-icon> 完成</button>
                <button v-if="row.batchStatus === 3 && row.codeStatus !== 2" @click="bindCode(row.prodBatchId)"><el-icon><Link /></el-icon> 绑码</button>
                <button v-if="!row.checkResult" @click="openQcProd(row)"><el-icon><DocumentChecked /></el-icon> 质检</button>
                <button @click="traceChain(row.batchNo)"><el-icon><Connection /></el-icon> 追溯</button>
                <button class="danger" @click="confirmDeleteProd(row.prodBatchId)"><el-icon><Delete /></el-icon> 删除</button>
              </td>
            </tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- Process Batch -->
    <template v-if="tab === 'process'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>产品名称<input v-model="processFilters.productName" placeholder="产品名称" @keyup.enter="loadProcess" /></label>
          <label>生产线<input v-model="processFilters.productionLine" placeholder="生产线" @keyup.enter="loadProcess" /></label>
          <label>批次状态<select v-model="processFilters.batchStatus"><option value="">全部</option><option value="1">加工中</option><option value="2">加工完成</option><option value="3">已存证</option></select></label>
          <label>班次<select v-model="processFilters.shift"><option value="">全部</option><option value="1">早班</option><option value="2">中班</option><option value="3">晚班</option></select></label>
          <div class="filter-actions"><button class="primary" @click="loadProcess"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>加工台账</p><h2>加工批次列表</h2></div><button class="primary create" @click="openCreateProcess"><el-icon><Plus /></el-icon> 新增加工批次</button></header>
        <div class="table-wrap"><table><thead><tr><th>加工批次号</th><th>产品名称</th><th>工艺模板</th><th>原料批次</th><th>生产线</th><th>班次</th><th>计划数量</th><th>实际温度</th><th>加工日期</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="11" class="empty">加载中...</td></tr>
            <tr v-else-if="!processList.length"><td colspan="11" class="empty">暂无加工批次数据</td></tr>
            <tr v-for="row in processList" :key="row.processBatchId">
              <td><code>{{ row.batchNo }}</code></td><td>{{ row.productName }}</td><td>{{ row.templateName }}</td><td><code>{{ row.rawBatchNo }}</code></td><td>{{ row.productionLine }}</td><td>{{ shiftLabels[row.shift] || '-' }}</td><td>{{ row.plannedAmount }}</td><td>{{ row.actualTemp || '-' }}</td><td>{{ row.processDate }}</td>
              <td><span class="status" :class="statusClass(processBatchStatusLabels[row.batchStatus] || '')">{{ processBatchStatusLabels[row.batchStatus] || '-' }}</span></td>
              <td class="actions">
                <button @click="openEditProcess(row)"><el-icon><Edit /></el-icon> 编辑</button>
                <button v-if="row.batchStatus === 1" @click="completeProcess(row.processBatchId)"><el-icon><Check /></el-icon> 完成</button>
                <button class="danger" @click="confirmDeleteProcess(row.processBatchId)"><el-icon><Delete /></el-icon> 删除</button>
              </td>
            </tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- Template -->
    <template v-if="tab === 'template'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>适用产品<input v-model="templateFilters.applicableProduct" placeholder="适用产品" @keyup.enter="loadTemplates" /></label>
          <label>模板状态<select v-model="templateFilters.templateStatus"><option value="">全部</option><option value="1">启用</option><option value="2">停用</option></select></label>
          <div class="filter-actions"><button class="primary" @click="loadTemplates"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>工艺台账</p><h2>工艺模板列表</h2></div><button class="primary create" @click="openCreateTemplate"><el-icon><Plus /></el-icon> 新增模板</button></header>
        <div class="table-wrap"><table><thead><tr><th>模板名称</th><th>版本</th><th>适用产品</th><th>目标温度</th><th>时长</th><th>压力</th><th>冷却温度</th><th>灌装温度</th><th>pH值</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="!templates.length"><td colspan="11" class="empty">暂无工艺模板数据</td></tr>
            <tr v-for="row in templates" :key="row.templateId">
              <td><strong>{{ row.templateName }}</strong></td><td>{{ row.version }}</td><td>{{ row.applicableProduct }}</td><td>{{ row.targetTemp || '-' }}</td><td>{{ row.duration || '-' }}</td><td>{{ row.pressure || '-' }}</td><td>{{ row.coolTemp || '-' }}</td><td>{{ row.fillTemp || '-' }}</td><td>{{ row.phValue || '-' }}</td>
              <td><span class="status" :class="row.templateStatus === 1 ? 'status-active' : 'status-disabled'">{{ row.templateStatus === 1 ? '启用' : '停用' }}</span></td>
              <td class="actions">
                <button @click="openEditTemplate(row)"><el-icon><Edit /></el-icon> 编辑</button>
                <button class="danger" @click="confirmDeleteTemplate(row.templateId)"><el-icon><Delete /></el-icon> 删除</button>
              </td>
            </tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- Material Input -->
    <template v-if="tab === 'input'">
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>投料台账</p><h2>投料记录</h2></div><button class="primary create" @click="showInputModal = true"><el-icon><Plus /></el-icon> 记录投料</button></header>
        <div class="table-wrap"><table><thead><tr><th>原料批次</th><th>原料名称</th><th>投料数量</th><th>单位</th><th>操作员</th><th>投料时间</th></tr></thead>
          <tbody><tr v-if="!materialInputs.length"><td colspan="6" class="empty">暂无投料记录</td></tr>
            <tr v-for="row in materialInputs" :key="row.inputId"><td><code>{{ row.rawBatchNo }}</code></td><td>{{ row.materialName }}</td><td>{{ row.inputAmount }}{{ row.unit }}</td><td>{{ row.unit }}</td><td>{{ row.operator }}</td><td>{{ row.inputTime }}</td></tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- Env Record -->
    <template v-if="tab === 'env'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>生产线<input v-model="envLine" placeholder="输入生产线" @keyup.enter="loadEnvRecords" /></label>
          <div class="filter-actions"><button class="primary" @click="loadEnvRecords"><el-icon><Search /></el-icon> 查询</button><button class="secondary" @click="showEnvModal = true"><el-icon><Plus /></el-icon> 采集环境数据</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>环境台账</p><h2>环境记录</h2></div></header>
        <div class="table-wrap"><table><thead><tr><th>生产线</th><th>温度</th><th>湿度</th><th>洁净度</th><th>是否异常</th><th>异常描述</th><th>记录时间</th></tr></thead>
          <tbody><tr v-if="!envRecords.length"><td colspan="7" class="empty">暂无环境记录</td></tr>
            <tr v-for="row in envRecords" :key="row.recordId"><td>{{ row.productionLine }}</td><td>{{ row.temperature }}</td><td>{{ row.humidity }}</td><td>{{ row.cleanliness }}</td>
              <td><span class="status" :class="row.isAbnormal === 1 ? 'status-void' : 'status-active'">{{ row.isAbnormal === 1 ? '异常' : '正常' }}</span></td><td>{{ row.abnormalDesc || '-' }}</td><td>{{ row.recordTime }}</td></tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- Inspection -->
    <template v-if="tab === 'inspection'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>业务类型<select v-model="inspFilters.bizType"><option value="">全部</option><option value="2">加工</option><option value="3">生产</option></select></label>
          <label>业务批次<input v-model="inspFilters.bizBatchNo" placeholder="批次号" /></label>
          <label>检验结果<select v-model="inspFilters.inspectionResult"><option value="">全部</option><option value="1">合格</option><option value="2">不合格</option></select></label>
          <div class="filter-actions"><button class="primary" @click="loadInspections"><el-icon><Search /></el-icon> 查询</button><button class="secondary" @click="showInspectionModal = true"><el-icon><Plus /></el-icon> 新增质检</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>质检台账</p><h2>质检记录</h2></div></header>
        <div class="table-wrap"><table><thead><tr><th>检验编号</th><th>业务类型</th><th>业务批次</th><th>检验类型</th><th>检验人</th><th>日期</th><th>结果</th><th>描述</th></tr></thead>
          <tbody><tr v-if="!inspections.length"><td colspan="8" class="empty">暂无质检记录</td></tr>
            <tr v-for="row in inspections" :key="row.inspectionId"><td><code>{{ row.inspectionNo }}</code></td>
              <td>{{ ['','原料','加工','生产','冷链','销售'][row.bizType] || row.bizType }}</td><td><code>{{ row.bizBatchNo }}</code></td>
              <td>{{ ['','自检','抽检','全检'][row.inspectionType] || '其他' }}</td><td>{{ row.inspector }}</td><td>{{ row.inspectionDate }}</td>
              <td><span class="status" :class="row.inspectionResult === 1 ? 'status-active' : 'status-void'">{{ row.inspectionResult === 1 ? '合格' : '不合格' }}</span></td><td>{{ row.resultDesc || '-' }}</td></tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- Modals -->
    <div v-if="showModal" class="trace-modal-backdrop" @click.self="showModal = false">
      <section class="trace-modal"><header><div><p>生产管理</p><h2>{{ editing ? '编辑生产批次' : '新增生产批次' }}</h2></div><button @click="showModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body grid-form">
          <label>批次号<input v-model="form.batchNo" placeholder="留空自动生成" /></label><label>产品名称 *<input v-model="form.productName" placeholder="如：鲜牛奶" /></label>
          <label>加工批次号<input v-model="form.processBatchNo" /></label><label>生产线 *<input v-model="form.productionLine" placeholder="如：L-01" /></label>
          <label>计划数量<input v-model="form.plannedAmount" type="number" /></label><label>实际数量<input v-model="form.actualAmount" type="number" /></label>
          <label>生产日期 *<input v-model="form.productionDate" type="date" /></label>
        </div>
        <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="form.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        <footer><button class="secondary" @click="showModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitProdBatch"><el-icon><Check /></el-icon> {{ editing ? '保存' : '创建' }}</button></footer>
      </section>
    </div>

    <!-- Other modals (Process, Template, Input, Env, Inspection, QC, Chain) -->
    <div v-if="showProcessModal" class="trace-modal-backdrop" @click.self="showProcessModal = false">
      <section class="trace-modal" style="width:760px"><header><div><p>加工管理</p><h2>{{ editingProcess ? '编辑加工批次' : '新增加工批次' }}</h2></div><button @click="showProcessModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body grid-form">
          <label>批次号<input v-model="processForm.batchNo" /></label><label>产品名称 *<input v-model="processForm.productName" /></label>
          <label>工艺模板<input v-model="processForm.templateName" /></label><label>原料批次号 *<input v-model="processForm.rawBatchNo" /></label>
          <label>生产线<input v-model="processForm.productionLine" /></label><label>计划数量<input v-model="processForm.plannedAmount" type="number" /></label>
          <label>加工日期<input v-model="processForm.processDate" /></label><label>操作员<input v-model="processForm.operator" /></label>
          <label>班次<select v-model.number="processForm.shift"><option :value="0">请选择</option><option :value="1">早班</option><option :value="2">中班</option><option :value="3">晚班</option></select></label>
          <label>杀菌温度 (℃)<input v-model="processForm.actualTemp" /></label><label>杀菌时长 (s)<input v-model="processForm.actualDuration" /></label>
          <label>均质压力 (MPa)<input v-model="processForm.actualPressure" /></label><label>冷却温度 (℃)<input v-model="processForm.actualCoolTemp" /></label>
          <label>灌装温度 (℃)<input v-model="processForm.actualFillTemp" /></label><label>pH 值<input v-model="processForm.actualPh" /></label>
          <label>粘度 (mPa·s)<input v-model="processForm.actualViscosity" /></label>
        </div>
        <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="processForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        <footer><button class="secondary" @click="showProcessModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitProcess"><el-icon><Check /></el-icon> {{ editingProcess ? '保存' : '创建' }}</button></footer>
      </section>
    </div>

    <div v-if="showTemplateModal" class="trace-modal-backdrop" @click.self="showTemplateModal = false">
      <section class="trace-modal"><header><div><p>模板管理</p><h2>{{ editingTemplate ? '编辑工艺模板' : '新增工艺模板' }}</h2></div><button @click="showTemplateModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body grid-form">
          <label>模板名称 *<input v-model="templateForm.templateName" /></label><label>版本<input v-model="templateForm.version" /></label>
          <label>适用产品<input v-model="templateForm.applicableProduct" /></label><label>杀菌温度 (℃)<input v-model="templateForm.targetTemp" /></label>
          <label>时长 (s)<input v-model="templateForm.duration" /></label><label>压力 (MPa)<input v-model="templateForm.pressure" /></label>
          <label>冷却温度 (℃)<input v-model="templateForm.coolTemp" /></label><label>灌装温度 (℃)<input v-model="templateForm.fillTemp" /></label>
          <label>搅拌速度 (rpm)<input v-model="templateForm.stirSpeed" /></label><label>pH 值<input v-model="templateForm.phValue" /></label>
          <label>粘度 (mPa·s)<input v-model="templateForm.viscosity" /></label><label>清洁等级<input v-model.number="templateForm.cleanLevel" type="number" /></label>
          <label>模板状态<select v-model.number="templateForm.templateStatus"><option :value="1">启用</option><option :value="2">停用</option></select></label>
        </div>
        <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="templateForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        <footer><button class="secondary" @click="showTemplateModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitTemplate"><el-icon><Check /></el-icon> {{ editingTemplate ? '更新模板' : '创建' }}</button></footer>
      </section>
    </div>

    <div v-if="showInputModal" class="trace-modal-backdrop" @click.self="showInputModal = false">
      <section class="trace-modal"><header><div><p>投料管理</p><h2>记录投料</h2></div><button @click="showInputModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body grid-form">
          <label>原料批次 *<input v-model="inputForm.rawBatchNo" /></label><label>原料名称<input v-model="inputForm.materialName" /></label>
          <label>投料数量<input v-model="inputForm.inputAmount" type="number" /></label><label>单位<input v-model="inputForm.unit" placeholder="kg" /></label>
          <label>操作员<input v-model="inputForm.operator" /></label><label>投料时间<input v-model="inputForm.inputTime" /></label>
        </div>
        <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">备注<textarea v-model="inputForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        <footer><button class="secondary" @click="showInputModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitMaterialInput"><el-icon><Check /></el-icon> 记录</button></footer>
      </section>
    </div>

    <div v-if="showEnvModal" class="trace-modal-backdrop" @click.self="showEnvModal = false">
      <section class="trace-modal"><header><div><p>环境采集</p><h2>生产环境采集</h2></div><button @click="showEnvModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body grid-form">
          <label>生产线 *<input v-model="envForm.productionLine" /></label><label>采集时间<input v-model="envForm.recordTime" /></label>
          <label>车间温度<input v-model="envForm.temperature" /></label><label>车间湿度<input v-model="envForm.humidity" /></label>
          <label>洁净度等级<input v-model="envForm.cleanliness" /></label>
          <label>是否异常<select v-model.number="envForm.isAbnormal"><option :value="0">正常</option><option :value="1">异常</option></select></label>
        </div>
        <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">异常描述<textarea v-model="envForm.abnormalDesc" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        <footer><button class="secondary" @click="showEnvModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitEnv"><el-icon><Check /></el-icon> 确认提交</button></footer>
      </section>
    </div>

    <div v-if="showInspectionModal" class="trace-modal-backdrop" @click.self="showInspectionModal = false">
      <section class="trace-modal"><header><div><p>质检管理</p><h2>生产质检录入</h2></div><button @click="showInspectionModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body grid-form">
          <label>检验编号<input v-model="inspectionForm.inspectionNo" /></label><label>业务类型 *<select v-model.number="inspectionForm.bizType"><option :value="2">加工</option><option :value="3">生产</option></select></label>
          <label>业务批次号 *<input v-model="inspectionForm.bizBatchNo" /></label><label>检验人<input v-model="inspectionForm.inspector" /></label>
          <label>检验日期<input v-model="inspectionForm.inspectionDate" /></label><label>检验类型<select v-model.number="inspectionForm.inspectionType"><option :value="1">自检</option><option :value="2">抽检</option><option :value="3">全检</option></select></label>
          <label>检验结果 *<select v-model.number="inspectionForm.inspectionResult"><option :value="1">合格</option><option :value="2">不合格</option></select></label>
        </div>
        <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:0 23px">结果描述<textarea v-model="inspectionForm.resultDesc" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin:15px 23px 0">备注<textarea v-model="inspectionForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" /></label>
        <footer><button class="secondary" @click="showInspectionModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitInspection"><el-icon><Check /></el-icon> 确认提交</button></footer>
      </section>
    </div>

    <div v-if="showQcProdModal" class="trace-modal-backdrop" @click.self="showQcProdModal = false">
      <section class="trace-modal" style="width:460px"><header><div><p>质检</p><h2>生产批次质检</h2></div><button @click="showQcProdModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <p style="color:#6c84a3;margin:0 0 16px;font-size:13px">批次号：<code>{{ qcTarget?.batchNo }}</code><br/>产品：<strong>{{ qcTarget?.productName }}</strong></p>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-bottom:16px">质检结果 *<select v-model.number="qcResult" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px"><option :value="1">✓ 合格</option><option :value="2">✗ 不合格</option></select></label>
          <div class="trace-hint info">操作人：<strong>{{ currentUser || '当前用户' }}</strong></div>
        </div>
        <footer><button class="secondary" @click="showQcProdModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" :class="qcResult === 1 ? '' : 'danger-fill'" @click="submitQcProd"><el-icon><Check /></el-icon> 确认{{ qcResult === 1 ? '合格' : '不合格' }}</button></footer>
      </section>
    </div>

    <div v-if="showChainModal" class="trace-modal-backdrop" @click.self="showChainModal = false">
      <section class="trace-modal" style="width:800px"><header><div><p>链路追溯</p><h2>生产全链路追溯</h2></div><button @click="showChainModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <div class="chain-flow" v-if="chainData" style="display:grid;grid-template-columns:repeat(7,minmax(90px,1fr));gap:10px;margin-top:14px">
            <div class="chain-node" style="min-height:100px;border:1px solid #dce8f4;border-radius:10px;background:#f8fbfd;padding:12px;text-align:center"><div style="width:30px;height:30px;border-radius:50%;display:grid;place-items:center;margin:0 auto 8px;background:#eaf2ff;color:#2467df;font-weight:900">供</div><strong>原料供应商</strong><div style="font-size:12px;color:#8195aa">{{ chainData.rawBatchNo || '-' }}</div></div>
            <div class="chain-node" style="min-height:100px;border:1px solid #dce8f4;border-radius:10px;background:#f8fbfd;padding:12px;text-align:center"><div style="width:30px;height:30px;border-radius:50%;display:grid;place-items:center;margin:0 auto 8px;background:#eaf2ff;color:#2467df;font-weight:900">原</div><strong>原料入库</strong></div>
            <div class="chain-node" style="min-height:100px;border:1px solid #dce8f4;border-radius:10px;background:#f8fbfd;padding:12px;text-align:center"><div style="width:30px;height:30px;border-radius:50%;display:grid;place-items:center;margin:0 auto 8px;background:#eaf2ff;color:#2467df;font-weight:900">工</div><strong>加工生产</strong><div style="font-size:12px;color:#8195aa">{{ chainData.processBatchNo || '-' }}</div></div>
            <div class="chain-node" style="min-height:100px;border:1px solid #dce8f4;border-radius:10px;background:#f8fbfd;padding:12px;text-align:center"><div style="width:30px;height:30px;border-radius:50%;display:grid;place-items:center;margin:0 auto 8px;background:#eaf2ff;color:#2467df;font-weight:900">产</div><strong>生产批次</strong><div style="font-size:12px;color:#8195aa">{{ chainData.prodBatchNo || '-' }}</div></div>
            <div class="chain-node" style="min-height:100px;border:1px solid #dce8f4;border-radius:10px;background:#f8fbfd;padding:12px;text-align:center"><div style="width:30px;height:30px;border-radius:50%;display:grid;place-items:center;margin:0 auto 8px;background:#eaf2ff;color:#2467df;font-weight:900">冷</div><strong>冷链运输</strong></div>
            <div class="chain-node" style="min-height:100px;border:1px solid #dce8f4;border-radius:10px;background:#f8fbfd;padding:12px;text-align:center"><div style="width:30px;height:30px;border-radius:50%;display:grid;place-items:center;margin:0 auto 8px;background:#eaf2ff;color:#2467df;font-weight:900">售</div><strong>销售终端</strong></div>
            <div class="chain-node" style="min-height:100px;border:1px solid #dce8f4;border-radius:10px;background:#f8fbfd;padding:12px;text-align:center"><div style="width:30px;height:30px;border-radius:50%;display:grid;place-items:center;margin:0 auto 8px;background:#eaf2ff;color:#2467df;font-weight:900">监</div><strong>监管核验</strong></div>
          </div>
          <pre v-if="chainData" style="background:#f8fafc;padding:12px;border-radius:8px;font-size:12px;max-height:200px;overflow:auto;margin-top:14px">{{ JSON.stringify(chainData, null, 2) }}</pre>
        </div>
        <footer><button class="secondary" @click="showChainModal = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

    <!-- Confirm Dialog -->
    <div v-if="showConfirm" class="trace-confirm-overlay" @click.self="showConfirm = false">
      <div class="trace-confirm-box"><h3>{{ confirmTitle }}</h3><p>{{ confirmMsg }}</p><div class="trace-confirm-actions"><button class="secondary" @click="showConfirm = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" :class="confirmBtnClass" @click="doConfirm"><el-icon><Check /></el-icon> {{ confirmBtnLabel }}</button></div></div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';
</style>
