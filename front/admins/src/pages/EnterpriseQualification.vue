<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { enterpriseApi } from '../services/api'

const tab = ref<'list' | 'qual'>('list')
const loading = ref(false)
const list = ref<any[]>([])
const toast = ref<{ type: string; msg: string } | null>(null)
const showModal = ref(false)
const showDetail = ref(false)
const showConfirm = ref(false)
const editing = ref<any>(null)
const viewing = ref<any>(null)
const deletingId = ref<number | null>(null)
const statusChecking = ref(false)

const filters = ref({ enterpriseName: '', enterpriseType: '', riskLevel: '' })

const enterpriseTypeLabels: Record<number, string> = { 1: '供应商', 2: '加工商', 3: '物流商', 4: '零售商' }
const riskLevelLabels: Record<number, string> = { 1: '低风险', 2: '中风险', 3: '高风险' }
const statusLabels: Record<number, string> = { 1: '正常', 0: '停用' }

const stats = computed(() => ({
  total: list.value.length,
  highRisk: list.value.filter((r: any) => r.riskLevel === 3).length,
  normal: list.value.filter((r: any) => r.status === 1).length,
  disabled: list.value.filter((r: any) => r.status === 0).length,
}))

const form = ref({
  enterpriseUuid: '', enterpriseName: '', enterpriseType: 1, certNo: '',
  address: '', contactPhone: '', contactPerson: '', riskLevel: 1, status: 1, remark: '',
})

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(label: string) {
  if (label.includes('低风险') || label === '正常') return 'tag-success'
  if (label.includes('中风险')) return 'tag-warn'
  if (label.includes('高风险') || label === '停用') return 'tag-danger'
  return 'tag-info'
}

async function loadList() {
  loading.value = true
  try {
    const p: Record<string, any> = { page: 1, size: 200 }
    if (filters.value.enterpriseName) p.enterpriseName = filters.value.enterpriseName
    if (filters.value.enterpriseType) p.enterpriseType = Number(filters.value.enterpriseType)
    if (filters.value.riskLevel) p.riskLevel = Number(filters.value.riskLevel)
    const res = await enterpriseApi.list(p)
    // 后端返回 Result<Page>，data.records 是列表
    list.value = (res as any).data?.records ?? (Array.isArray(res) ? res : [])
  } catch (e: any) { flash('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function openCreate() {
  editing.value = null
  form.value = { enterpriseUuid: '', enterpriseName: '', enterpriseType: 1, certNo: '', address: '', contactPhone: '', contactPerson: '', riskLevel: 1, status: 1, remark: '' }
  showModal.value = true
}
function openEdit(row: any) {
  editing.value = row
  form.value = {
    enterpriseUuid: row.enterpriseUuid ?? '', enterpriseName: row.enterpriseName ?? '',
    enterpriseType: row.enterpriseType ?? 1, certNo: row.certNo ?? '', address: row.address ?? '',
    contactPhone: row.contactPhone ?? '', contactPerson: row.contactPerson ?? '',
    riskLevel: row.riskLevel ?? 1, status: row.status ?? 1, remark: row.remark ?? '',
  }
  showModal.value = true
}
function openDetail(row: any) { viewing.value = row; showDetail.value = true }

async function submitForm() {
  try {
    if (editing.value) {
      await enterpriseApi.update(editing.value.enterpriseId, form.value)
      flash('success', '企业信息更新成功')
    } else {
      await enterpriseApi.create(form.value)
      flash('success', '企业创建成功')
    }
    showModal.value = false; loadList()
  } catch (e: any) { flash('error', '操作失败: ' + e.message) }
}

function confirmDelete(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDelete() {
  try { await enterpriseApi.delete(deletingId.value!); flash('success', '删除成功'); showConfirm.value = false; loadList() }
  catch (e: any) { flash('error', '删除失败: ' + e.message) }
}

async function doCheckStatus() {
  statusChecking.value = true
  try { await enterpriseApi.checkStatus(); flash('success', '资质状态检查已触发'); loadList() }
  catch (e: any) { flash('error', '检查失败: ' + e.message) }
  finally { statusChecking.value = false }
}

onMounted(loadList)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 统计卡片 -->
    <div class="page-stats">
      <div class="stat-card"><div class="stat-meta"><span>企业总数</span><span class="stat-ico">企</span></div><b>{{ stats.total }}</b></div>
      <div class="stat-card red"><div class="stat-meta"><span>高风险企业</span><span class="stat-ico">!</span></div><b>{{ stats.highRisk }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>正常运营</span><span class="stat-ico">✓</span></div><b>{{ stats.normal }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>已停用</span><span class="stat-ico">✕</span></div><b>{{ stats.disabled }}</b></div>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'list' }" @click="tab = 'list'">🏢 企业列表</button>
      <button class="tab-btn" :class="{ active: tab === 'qual' }" @click="tab = 'qual'">📋 资质管理</button>
    </div>

    <!-- 企业列表 tab -->
    <template v-if="tab === 'list'">
      <div class="search-bar">
        <div class="search-field"><label>企业名称</label><input v-model="filters.enterpriseName" placeholder="企业名称" @keyup.enter="loadList" /></div>
        <div class="search-field"><label>企业类型</label><select v-model="filters.enterpriseType"><option value="">全部</option><option value="1">供应商</option><option value="2">加工商</option><option value="3">物流商</option><option value="4">零售商</option></select></div>
        <div class="search-field"><label>风险等级</label><select v-model="filters.riskLevel"><option value="">全部</option><option value="1">低风险</option><option value="2">中风险</option><option value="3">高风险</option></select></div>
        <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadList">🔍 查询</button></div>
      </div>

      <div class="toolbar">
        <h3>企业列表 <span class="count-note">({{ list.length }} 条)</span></h3>
        <div class="toolbar-actions">
          <button class="btn btn-outline" :disabled="statusChecking" @click="doCheckStatus">🔄 资质状态检查</button>
          <button class="btn btn-primary" @click="openCreate">＋ 新增企业</button>
        </div>
      </div>

      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>企业名称</th><th>企业类型</th><th>信用代码</th><th>联系人</th><th>联系电话</th><th>风险等级</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="8" style="text-align:center;padding:32px">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="8"><div class="empty-state"><div class="empty-icon">🏢</div><p>暂无企业数据</p></div></td></tr>
            <tr v-for="row in list" :key="row.enterpriseId">
              <td><strong>{{ row.enterpriseName }}</strong></td>
              <td><span class="tag tag-info">{{ enterpriseTypeLabels[row.enterpriseType] || '-' }}</span></td>
              <td><code style="font-size:12px">{{ row.certNo || '-' }}</code></td>
              <td>{{ row.contactPerson || '-' }}</td>
              <td>{{ row.contactPhone || '-' }}</td>
              <td><span class="tag" :class="tagClass(riskLevelLabels[row.riskLevel] || '')">{{ riskLevelLabels[row.riskLevel] || '-' }}</span></td>
              <td><span class="tag" :class="row.status === 1 ? 'tag-success' : 'tag-danger'">{{ statusLabels[row.status] ?? '-' }}</span></td>
              <td class="col-actions">
                <button class="btn btn-outline btn-sm" @click="openDetail(row)">📄</button>
                <button class="btn btn-outline btn-sm" style="margin-left:3px" @click="openEdit(row)">✎</button>
                <button class="btn btn-danger btn-sm" style="margin-left:3px" @click="confirmDelete(row.enterpriseId)">🗑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- 资质管理 tab -->
    <template v-if="tab === 'qual'">
      <div class="data-table-wrap" style="margin-top:0">
        <table class="data-table">
          <thead><tr><th>企业名称</th><th>资质类型</th><th>资质编号</th><th>颁发机构</th><th>有效期</th><th>资质状态</th><th>审核状态</th></tr></thead>
          <tbody>
            <tr><td colspan="7"><div class="empty-state"><div class="empty-icon">📋</div><p>资质管理功能需对接 qualification 接口，当前展示占位</p><p style="font-size:12px;color:#91a4bc">后端已支持 t_qualification 表 CRUD，前端可后续扩展</p></div></td></tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- 新增/编辑模态框 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header"><h3>{{ editing ? '编辑企业信息' : '新增企业' }}</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">基</span>基本信息</div>
            <div class="form-row">
              <div class="form-group"><label>企业名称 *</label><input v-model="form.enterpriseName" placeholder="请输入企业名称" /></div>
              <div class="form-group"><label>企业类型 *</label><select v-model.number="form.enterpriseType"><option :value="1">供应商</option><option :value="2">加工商</option><option :value="3">物流商</option><option :value="4">零售商</option></select></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>统一社会信用代码</label><input v-model="form.certNo" placeholder="18位信用代码" /></div>
              <div class="form-group"><label>企业UUID</label><input v-model="form.enterpriseUuid" placeholder="自动生成或手动输入" /></div>
            </div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">联</span>联系信息</div>
            <div class="form-row">
              <div class="form-group"><label>联系人</label><input v-model="form.contactPerson" placeholder="联系人姓名" /></div>
              <div class="form-group"><label>联系电话</label><input v-model="form.contactPhone" placeholder="联系电话" /></div>
            </div>
            <div class="form-group"><label>注册地址</label><input v-model="form.address" placeholder="企业注册地址" /></div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">管</span>监管信息</div>
            <div class="form-row">
              <div class="form-group"><label>风险等级</label><select v-model.number="form.riskLevel"><option :value="1">低风险</option><option :value="2">中风险</option><option :value="3">高风险</option></select></div>
              <div class="form-group"><label>状态</label><select v-model.number="form.status"><option :value="1">正常</option><option :value="0">停用</option></select></div>
            </div>
            <div class="form-group"><label>备注</label><textarea v-model="form.remark" placeholder="备注信息" /></div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showModal = false">取消</button><button class="btn btn-primary" @click="submitForm">{{ editing ? '保存' : '创建' }}</button></div>
      </div>
    </div>

    <!-- 详情模态框 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal">
        <div class="modal-header"><h3>企业详情</h3><button class="modal-close" @click="showDetail = false">✕</button></div>
        <div class="modal-body">
          <div class="detail-panel" v-if="viewing">
            <h4>基本信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>企业名称</span><strong>{{ viewing.enterpriseName }}</strong></div>
              <div class="kv"><span>企业UUID</span><code style="font-size:12px">{{ viewing.enterpriseUuid }}</code></div>
              <div class="kv"><span>企业类型</span><span class="tag tag-info">{{ enterpriseTypeLabels[viewing.enterpriseType] }}</span></div>
              <div class="kv"><span>信用代码</span><strong>{{ viewing.certNo || '-' }}</strong></div>
            </div>
            <h4>联系信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>联系人</span><strong>{{ viewing.contactPerson || '-' }}</strong></div>
              <div class="kv"><span>联系电话</span><strong>{{ viewing.contactPhone || '-' }}</strong></div>
              <div class="kv"><span>注册地址</span><strong>{{ viewing.address || '-' }}</strong></div>
            </div>
            <h4>监管信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>风险等级</span><span class="tag" :class="tagClass(riskLevelLabels[viewing.riskLevel] || '')">{{ riskLevelLabels[viewing.riskLevel] || '-' }}</span></div>
              <div class="kv"><span>状态</span><span class="tag" :class="viewing.status === 1 ? 'tag-success' : 'tag-danger'">{{ statusLabels[viewing.status] ?? '-' }}</span></div>
              <div class="kv"><span>备注</span><strong>{{ viewing.remark || '-' }}</strong></div>
              <div class="kv"><span>创建时间</span><strong>{{ viewing.createTime || '-' }}</strong></div>
              <div class="kv"><span>更新时间</span><strong>{{ viewing.updateTime || '-' }}</strong></div>
            </div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showDetail = false">关闭</button></div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="showConfirm" class="confirm-overlay"><div class="confirm-box"><div class="confirm-icon">⚠️</div><h3>确认删除</h3><p>确定要删除该企业吗？此操作为逻辑删除。</p><div class="confirm-actions"><button class="btn btn-outline" @click="showConfirm = false">取消</button><button class="btn btn-danger" @click="doDelete">确认删除</button></div></div></div>
  </div>
</template>
