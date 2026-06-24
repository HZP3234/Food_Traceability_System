<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { productionApi } from '../services/api'

const tab = ref<'prod' | 'input' | 'env' | 'inspection'>('prod')
const loading = ref(false)//
const list = ref<any[]>([])
const materialInputs = ref<any[]>([])
const envRecords = ref<any[]>([])
const inspections = ref<any[]>([])
const toast = ref<{ type: string; msg: string } | null>(null)
const showModal = ref(false); const showInputModal = ref(false)
const showEnvModal = ref(false); const showInspectionModal = ref(false)
const showConfirm = ref(false); const showChainModal = ref(false)
const editing = ref<any>(null); const deletingId = ref<number | null>(null)
const chainData = ref<any>(null)

const filters = ref({ productName: '', productionLine: '', checkResult: '', batchStatus: '', codeStatus: '' })
const envLine = ref(''); const inspFilters = ref({ bizType: '', bizBatchNo: '', inspectionType: '', inspectionResult: '' })

const stats = computed(() => ({
  total: list.value.length, qualified: list.value.filter((r: any) => r.checkResult === 1).length,
  completed: list.value.filter((r: any) => r.batchStatus === 3).length,
  binded: list.value.filter((r: any) => r.codeStatus === 2).length,
}))

const checkResultLabels = ['', '合格', '不合格']
const batchStatusLabels = ['', '待生产', '生产中', '生产完成', '已废弃']
const codeStatusLabels = ['', '未绑定', '已绑定']

const form = ref({ batchNo: '', productName: '', processBatchNo: '', productionLine: '', plannedAmount: '', actualAmount: '', productionDate: '', remark: '' })
const inputForm = ref({ rawBatchNo: '', materialName: '', inputAmount: '', unit: '', operator: '', inputTime: '', remark: '' })
const envForm = ref({ productionLine: '', temperature: '', humidity: '', cleanliness: '', isAbnormal: 0, abnormalDesc: '', recordTime: '' })
const inspectionForm = ref({ inspectionNo: '', bizType: 2, bizBatchNo: '', inspectionType: 1, inspector: '', inspectionDate: '', inspectionResult: 1, resultDesc: '', remark: '' })

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(s: string) {
  if (['合格', '已完成', '已绑定', '正常'].some(x => s.includes(x))) return 'tag-success'
  if (['生产中', '待', '未'].some(x => s.includes(x))) return 'tag-warn'
  return 'tag-info'
}

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
function confirmDeleteProd(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDeleteProd() { try { await productionApi.deleteProdBatch(deletingId.value!); flash('success', '删除成功'); showConfirm.value = false; loadProdBatch() } catch (e: any) { flash('error', '删除失败: ' + e.message) } }
async function completeProd(id: number) { try { await productionApi.completeProdBatch(id); flash('success', '生产批次已完成'); loadProdBatch() } catch (e: any) { flash('error', '操作失败: ' + e.message) } }
async function bindCode(id: number) { try { await productionApi.bindCode(id); flash('success', '溯源码绑定完成'); loadProdBatch() } catch (e: any) { flash('error', '绑码失败: ' + e.message) } }
async function qualityCheckProd(batchNo: string, result: number) { try { await productionApi.qualityCheckProd(batchNo, result); flash('success', '质检录入成功'); loadProdBatch() } catch (e: any) { flash('error', '质检失败: ' + e.message) } }
async function traceChain(batchNo: string) { try { const result = await productionApi.traceProcessChain(batchNo); chainData.value = result; showChainModal.value = true } catch (e: any) { flash('error', '追溯失败: ' + e.message) } }

// 投料/环境/质检
async function loadMaterialInput() { try { const d = await productionApi.listMaterialInput(); materialInputs.value = Array.isArray(d) ? d : [] } catch (e: any) { flash('error', '加载失败') } }
async function submitMaterialInput() { try { await productionApi.recordMaterialInput(inputForm.value); flash('success', '投料记录成功'); showInputModal.value = false; loadMaterialInput() } catch (e: any) { flash('error', '投料失败: ' + e.message) } }
async function loadEnvRecords() { try { if (envLine.value) { const d = await productionApi.listEnvRecord(envLine.value); envRecords.value = Array.isArray(d) ? d : [] } } catch (e: any) { flash('error', '加载失败') } }
async function submitEnv() { try { await productionApi.recordEnv(envForm.value); flash('success', '环境数据采集成功'); showEnvModal.value = false; loadEnvRecords() } catch (e: any) { flash('error', '采集失败: ' + e.message) } }
async function loadInspections() {
  try { const p: Record<string, any> = {}; if (inspFilters.value.bizType) p.bizType = Number(inspFilters.value.bizType); if (inspFilters.value.bizBatchNo) p.bizBatchNo = inspFilters.value.bizBatchNo; if (inspFilters.value.inspectionResult) p.inspectionResult = Number(inspFilters.value.inspectionResult); const d = await productionApi.listInspection(p); inspections.value = Array.isArray(d) ? d : [] } catch (e: any) { flash('error', '加载失败') }
}
async function submitInspection() { try { await productionApi.createInspection(inspectionForm.value); flash('success', '质检记录创建成功'); showInspectionModal.value = false; loadInspections() } catch (e: any) { flash('error', '创建失败: ' + e.message) } }

function switchTab(t: 'prod' | 'input' | 'env' | 'inspection') { tab.value = t; if (t === 'prod') loadProdBatch(); else if (t === 'input') loadMaterialInput(); else if (t === 'env') loadEnvRecords(); else loadInspections() }
onMounted(loadProdBatch)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <div class="page-stats">
      <div class="stat-card"><div class="stat-meta"><span>生产批次总数</span><span class="stat-ico">产</span></div><b>{{ stats.total }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>质检合格</span><span class="stat-ico">✓</span></div><b>{{ stats.qualified }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>生产完成</span><span class="stat-ico">完</span></div><b>{{ stats.completed }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>已绑码</span><span class="stat-ico">码</span></div><b>{{ stats.binded }}</b></div>
    </div>

    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'prod' }" @click="switchTab('prod')">🏭 生产批次</button>
      <button class="tab-btn" :class="{ active: tab === 'input' }" @click="switchTab('input')">📥 投料记录</button>
      <button class="tab-btn" :class="{ active: tab === 'env' }" @click="switchTab('env')">🌡 环境监测</button>
      <button class="tab-btn" :class="{ active: tab === 'inspection' }" @click="switchTab('inspection')">🔬 质检记录</button>
    </div>

    <!-- 生产批次 -->
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
              <td><span class="tag" :class="tagClass(batchStatusLabels[row.batchStatus] || '')">{{ batchStatusLabels[row.batchStatus] || '-' }}</span></td>
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

    <!-- 投料记录 -->
    <template v-if="tab === 'input'">
      <div class="toolbar"><h3>投料记录 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ materialInputs.length }} 条)</span></h3><button class="btn btn-primary" @click="showInputModal = true">＋ 记录投料</button></div>
      <div class="data-table-wrap"><table class="data-table"><thead><tr><th>原料批次</th><th>原料名称</th><th>投料数量</th><th>单位</th><th>操作员</th><th>投料时间</th></tr></thead>
        <tbody><tr v-if="!materialInputs.length"><td colspan="6"><div class="empty-state"><div class="empty-icon">📥</div><p>暂无投料记录</p></div></td></tr>
          <tr v-for="row in materialInputs" :key="row.inputId"><td><code style="font-size:12px">{{ row.rawBatchNo }}</code></td><td>{{ row.materialName }}</td><td>{{ row.inputAmount }}{{ row.unit }}</td><td>{{ row.unit }}</td><td>{{ row.operator }}</td><td>{{ row.inputTime }}</td></tr>
        </tbody></table></div>
    </template>

    <!-- 环境监测 -->
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

    <!-- 质检记录 -->
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

    <!-- 模态框与前面类似，省略重复结构 -->
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

    <!-- 环境采集模态框 (对齐 app.js 环境字段) -->
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

    <!-- 质检模态框 -->
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

    <!-- 投料模态框 -->
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

    <!-- 全链路追溯 (对齐 app.js chain flow) -->
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

    <div v-if="showConfirm" class="confirm-overlay"><div class="confirm-box"><div class="confirm-icon">⚠️</div><h3>确认删除</h3><p>确定要删除该生产批次吗？</p><div class="confirm-actions"><button class="btn btn-outline" @click="showConfirm = false">取消</button><button class="btn btn-danger" @click="doDeleteProd">确认删除</button></div></div></div>
  </div>
</template>
