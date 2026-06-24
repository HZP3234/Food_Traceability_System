<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { productionApi } from '../services/api'

const tab = ref<'process' | 'template'>('process')
const loading = ref(false)
const list = ref<any[]>([])
const templates = ref<any[]>([])
const toast = ref<{ type: string; msg: string } | null>(null)
const showModal = ref(false)
const showTemplateModal = ref(false)
const showConfirm = ref(false)
const showChainModal = ref(false)
const editing = ref<any>(null)
const editingTemplate = ref<any>(null)
const deletingId = ref<number | null>(null)

const filters = ref({ productName: '', productionLine: '', batchStatus: '', shift: '' })
const templateFilters = ref({ applicableProduct: '', templateStatus: '' })

const stats = computed(() => ({
  total: list.value.length,
  processing: list.value.filter((r: any) => r.batchStatus === 1).length,
  done: list.value.filter((r: any) => r.batchStatus === 2 || r.batchStatus === 3).length,
  templates: templates.value.length,
}))

const batchStatusLabels = ['', '加工中', '加工完成', '已存证', '已废弃']
const shiftLabels = ['', '早班', '中班', '晚班']

const form = ref({
  batchNo: '', productName: '', templateName: '', rawBatchNo: '',
  plannedAmount: '', processDate: '', operator: '', productionLine: '', shift: 0,
  actualTemp: '', actualDuration: '', actualPressure: '', actualCoolTemp: '',
  actualFillTemp: '', actualPh: '', actualViscosity: '', remark: '',
})

const templateForm = ref({
  templateName: '', version: '', applicableProduct: '', targetTemp: '',
  duration: '', pressure: '', coolTemp: '', fillTemp: '', stirSpeed: '',
  phValue: '', viscosity: '', cleanLevel: 0, templateStatus: 1, remark: '',
})

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }

function tagClass(s: string) {
  if (['已完成', '已存证', '已绑定', '已启用', '合格'].some(x => s.includes(x))) return 'tag-success'
  if (['加工中', '进行中', '待'].some(x => s.includes(x))) return 'tag-warn'
  return 'tag-info'
}

async function loadProcess() {
  loading.value = true
  try {
    const p: Record<string, any> = {}
    if (filters.value.productName) p.productName = filters.value.productName
    if (filters.value.productionLine) p.productionLine = filters.value.productionLine
    if (filters.value.batchStatus) p.batchStatus = Number(filters.value.batchStatus)
    if (filters.value.shift) p.shift = Number(filters.value.shift)
    const data = await productionApi.listProcessBatch(p)
    list.value = Array.isArray(data) ? data : []
  } catch (e: any) { flash('error', '加载加工批次失败: ' + e.message) }
  finally { loading.value = false }
}

async function loadTemplates() {
  try {
    const p: Record<string, any> = {}
    if (templateFilters.value.applicableProduct) p.applicableProduct = templateFilters.value.applicableProduct
    if (templateFilters.value.templateStatus) p.templateStatus = Number(templateFilters.value.templateStatus)
    const data = await productionApi.listTemplate(p)
    templates.value = Array.isArray(data) ? data : []
  } catch (e: any) { flash('error', '加载工艺模板失败: ' + e.message) }
}

function openCreateProcess() {
  editing.value = null
  form.value = { batchNo: '', productName: '', templateName: '', rawBatchNo: '', plannedAmount: '', processDate: '', operator: '', productionLine: '', shift: 0, actualTemp: '', actualDuration: '', actualPressure: '', actualCoolTemp: '', actualFillTemp: '', actualPh: '', actualViscosity: '', remark: '' }
  showModal.value = true
}
function openEditProcess(row: any) {
  editing.value = row
  form.value = {
    batchNo: row.batchNo ?? '', productName: row.productName ?? '', templateName: row.templateName ?? '',
    rawBatchNo: row.rawBatchNo ?? '', plannedAmount: row.plannedAmount ?? '', processDate: row.processDate ?? '',
    operator: row.operator ?? '', productionLine: row.productionLine ?? '', shift: row.shift ?? 0,
    actualTemp: row.actualTemp ?? '', actualDuration: row.actualDuration ?? '', actualPressure: row.actualPressure ?? '',
    actualCoolTemp: row.actualCoolTemp ?? '', actualFillTemp: row.actualFillTemp ?? '', actualPh: row.actualPh ?? '',
    actualViscosity: row.actualViscosity ?? '', remark: row.remark ?? '',
  }
  showModal.value = true
}
async function submitProcess() {
  try {
    const data: Record<string, any> = { ...form.value }
    if (editing.value) { data.processBatchId = editing.value.processBatchId; await productionApi.updateProcessBatch(data); flash('success', '加工批次更新成功') }
    else { await productionApi.createProcessBatch(data); flash('success', '加工批次创建成功') }
    showModal.value = false; loadProcess()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}

function confirmDeleteProcess(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDeleteProcess() {
  try { await productionApi.deleteProcessBatch(deletingId.value!); flash('success', '加工批次删除成功'); showConfirm.value = false; loadProcess() }
  catch (e: any) { flash('error', '删除失败: ' + e.message) }
}

async function completeProcess(id: number) {
  try { await productionApi.completeProcessBatch(id); flash('success', '加工批次已完成'); loadProcess() }
  catch (e: any) { flash('error', '操作失败: ' + e.message) }
}

// 模板 CRUD
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
async function deleteTemplate(id: number) {
  if (!confirm('确认删除该工艺模板？')) return
  try { await productionApi.deleteTemplate(id); flash('success', '工艺模板删除成功'); loadTemplates() }
  catch (e: any) { flash('error', '删除失败: ' + e.message) }
}

function switchTab(t: 'process' | 'template') { tab.value = t; if (t === 'process') loadProcess(); else loadTemplates() }

onMounted(loadProcess)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 统计卡片 -->
    <div class="page-stats">
      <div class="stat-card"><div class="stat-meta"><span>加工批次总数</span><span class="stat-ico">工</span></div><b>{{ stats.total }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>加工中</span><span class="stat-ico">进</span></div><b>{{ stats.processing }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>已完成/存证</span><span class="stat-ico">✓</span></div><b>{{ stats.done }}</b></div>
      <div class="stat-card"><div class="stat-meta"><span>工艺模板</span><span class="stat-ico">模</span></div><b>{{ stats.templates }}</b></div>
    </div>

    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'process' }" @click="switchTab('process')">⚙️ 加工批次</button>
      <button class="tab-btn" :class="{ active: tab === 'template' }" @click="switchTab('template')">📐 工艺模板</button>
    </div>

    <!-- ========== 加工批次 ========== -->
    <template v-if="tab === 'process'">
      <div class="search-bar">
        <div class="search-field"><label>产品名称</label><input v-model="filters.productName" placeholder="产品名称" @keyup.enter="loadProcess" /></div>
        <div class="search-field"><label>生产线</label><input v-model="filters.productionLine" placeholder="生产线" @keyup.enter="loadProcess" /></div>
        <div class="search-field"><label>批次状态</label>
          <select v-model="filters.batchStatus"><option value="">全部</option><option value="1">加工中</option><option value="2">加工完成</option><option value="3">已存证</option></select>
        </div>
        <div class="search-field"><label>班次</label>
          <select v-model="filters.shift"><option value="">全部</option><option value="1">早班</option><option value="2">中班</option><option value="3">晚班</option></select>
        </div>
        <div class="search-field" style="align-self:end">
          <button class="btn btn-primary" @click="loadProcess">🔍 查询</button>
        </div>
      </div>

      <div class="toolbar">
        <h3>加工批次列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ list.length }} 条)</span></h3>
        <button class="btn btn-primary" @click="openCreateProcess">＋ 新增加工批次</button>
      </div>

      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>加工批次号</th><th>产品名称</th><th>工艺模板</th><th>原料批次</th><th>生产线</th><th>班次</th><th>计划数量</th><th>实际温度</th><th>加工日期</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="11" style="text-align:center;padding:32px">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="11"><div class="empty-state"><div class="empty-icon">⚙️</div><p>暂无加工批次数据</p></div></td></tr>
            <tr v-for="row in list" :key="row.processBatchId">
              <td><code style="color:#2666df;font-size:13px">{{ row.batchNo }}</code></td>
              <td>{{ row.productName }}</td><td>{{ row.templateName }}</td>
              <td><code style="font-size:12px">{{ row.rawBatchNo }}</code></td>
              <td>{{ row.productionLine }}</td><td>{{ shiftLabels[row.shift] || '-' }}</td>
              <td>{{ row.plannedAmount }}</td><td>{{ row.actualTemp || '-' }}</td>
              <td>{{ row.processDate }}</td>
              <td><span class="tag" :class="tagClass(batchStatusLabels[row.batchStatus] || '')">{{ batchStatusLabels[row.batchStatus] || '-' }}</span></td>
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

    <!-- ========== 工艺模板 ========== -->
    <template v-if="tab === 'template'">
      <div class="search-bar">
        <div class="search-field"><label>适用产品</label><input v-model="templateFilters.applicableProduct" placeholder="适用产品" @keyup.enter="loadTemplates" /></div>
        <div class="search-field"><label>模板状态</label>
          <select v-model="templateFilters.templateStatus"><option value="">全部</option><option value="1">启用</option><option value="2">停用</option></select>
        </div>
        <div class="search-field" style="align-self:end">
          <button class="btn btn-primary" @click="loadTemplates">🔍 查询</button>
        </div>
      </div>

      <div class="toolbar">
        <h3>工艺模板列表 <span style="color:#91a4bc;font-size:13px;font-weight:400">({{ templates.length }} 条)</span></h3>
        <button class="btn btn-primary" @click="openCreateTemplate">＋ 新增模板</button>
      </div>

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

    <!-- 加工批次模态框 (对齐 app.js 工艺参数) -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal" style="width:720px">
        <div class="modal-header"><h3>{{ editing ? '编辑加工批次' : '新增加工批次' }}</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">基</span>基本信息</div>
            <div class="form-row">
              <div class="form-group"><label>批次号</label><input v-model="form.batchNo" placeholder="自动生成可留空" /></div>
              <div class="form-group"><label>产品名称 *</label><input v-model="form.productName" placeholder="如：鲜奶基料" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>工艺模板</label><input v-model="form.templateName" placeholder="如：巴氏杀菌标准工艺" /></div>
              <div class="form-group"><label>原料批次号 *</label><input v-model="form.rawBatchNo" placeholder="关联原料批次号" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>生产线</label><input v-model="form.productionLine" placeholder="生产线" /></div>
              <div class="form-group"><label>计划数量</label><input v-model="form.plannedAmount" placeholder="计划数量" type="number" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>加工日期</label><input v-model="form.processDate" placeholder="2025-01-01" /></div>
              <div class="form-group"><label>操作员</label><input v-model="form.operator" placeholder="如：李工" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>班次</label>
                <select v-model.number="form.shift"><option :value="0">请选择</option><option :value="1">早班</option><option :value="2">中班</option><option :value="3">晚班</option></select>
              </div>
            </div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">参</span>实际工艺参数</div>
            <div class="form-row">
              <div class="form-group"><label>杀菌温度 (℃)</label><input v-model="form.actualTemp" placeholder="72.0" /></div>
              <div class="form-group"><label>杀菌时长 (s)</label><input v-model="form.actualDuration" placeholder="15" /></div>
              <div class="form-group"><label>均质压力 (MPa)</label><input v-model="form.actualPressure" placeholder="18" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>冷却温度 (℃)</label><input v-model="form.actualCoolTemp" placeholder="4.0" /></div>
              <div class="form-group"><label>灌装温度 (℃)</label><input v-model="form.actualFillTemp" placeholder="6.0" /></div>
              <div class="form-group"><label>pH 值</label><input v-model="form.actualPh" placeholder="6.8" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>粘度 (mPa·s)</label><input v-model="form.actualViscosity" placeholder="120" /></div>
            </div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="form.remark" placeholder="加工相关补充说明" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="submitProcess">{{ editing ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- 模板模态框 -->
    <div v-if="showTemplateModal" class="modal-overlay" @click.self="showTemplateModal = false">
      <div class="modal">
        <div class="modal-header"><h3>{{ editingTemplate ? '编辑工艺模板' : '新增工艺模板' }}</h3><button class="modal-close" @click="showTemplateModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">模</span>模板信息</div>
            <div class="form-row">
              <div class="form-group"><label>模板名称 *</label><input v-model="templateForm.templateName" placeholder="如：巴氏杀菌标准工艺" /></div>
              <div class="form-group"><label>版本</label><input v-model="templateForm.version" placeholder="V3.2" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>适用产品</label><input v-model="templateForm.applicableProduct" placeholder="如：鲜奶基料" /></div>
              <div class="form-group"><label>杀菌温度 (℃)</label><input v-model="templateForm.targetTemp" placeholder="72.0" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>时长 (s)</label><input v-model="templateForm.duration" placeholder="15" /></div>
              <div class="form-group"><label>压力 (MPa)</label><input v-model="templateForm.pressure" placeholder="18" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>冷却温度 (℃)</label><input v-model="templateForm.coolTemp" placeholder="4.0" /></div>
              <div class="form-group"><label>灌装温度 (℃)</label><input v-model="templateForm.fillTemp" placeholder="6.0" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>搅拌速度 (rpm)</label><input v-model="templateForm.stirSpeed" placeholder="200" /></div>
              <div class="form-group"><label>pH 值</label><input v-model="templateForm.phValue" placeholder="6.8" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>粘度 (mPa·s)</label><input v-model="templateForm.viscosity" placeholder="120" /></div>
              <div class="form-group"><label>清洁等级</label><input v-model.number="templateForm.cleanLevel" type="number" placeholder="十万级=1 万级=2" /></div>
            </div>
            <div class="form-group"><label>模板状态</label>
              <select v-model.number="templateForm.templateStatus"><option :value="1">启用</option><option :value="2">停用</option></select>
            </div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="templateForm.remark" placeholder="模板更新说明" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showTemplateModal = false">取消</button>
          <button class="btn btn-outline" @click="submitTemplate;flash('success','新模板已保存')">另存为新模板</button>
          <button class="btn btn-primary" @click="submitTemplate">{{ editingTemplate ? '更新模板' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="showConfirm" class="confirm-overlay" @click.self="showConfirm = false">
      <div class="confirm-box">
        <div class="confirm-icon">⚠️</div><h3>确认删除</h3><p>删除后将不可恢复，确定要删除该加工批次吗？</p>
        <div class="confirm-actions"><button class="btn btn-outline" @click="showConfirm = false">取消</button><button class="btn btn-danger" @click="doDeleteProcess">确认删除</button></div>
      </div>
    </div>
  </div>
</template>
