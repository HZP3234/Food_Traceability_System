<script setup lang="ts">
import { ref, computed, onMounted, inject, type Ref } from 'vue'
import { Check, Close, Delete, OfficeBuilding, Plus, Refresh, Search, View, Warning } from '@element-plus/icons-vue'
import { enterpriseApi, qualificationApi } from '../services/api'
import Pagination from '../components/Pagination.vue'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
const isRegulator = computed(() => currentRole?.value === 'regulator' || currentRole?.value === 'super-admin')

const tab = ref<'list' | 'qual'>('qual')
const loading = ref(false)
const list = ref<any[]>([])
const qualList = ref<any[]>([])
const currentPage = ref(1)
const qualPage = ref(1)
const pageSize = ref(10)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return list.value.slice(start, start + pageSize.value)
})
const paginatedQualList = computed(() => {
  const start = (qualPage.value - 1) * pageSize.value
  return qualList.value.slice(start, start + pageSize.value)
})

const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showDetail = ref(false)
const showConfirm = ref(false)
const viewing = ref<any>(null)
const deletingId = ref<number | null>(null)
const statusChecking = ref(false)
const auditRemark = ref('')
const showAuditDialog = ref(false)
const auditAction = ref<'approve' | 'reject'>('approve')
const auditingId = ref<number | null>(null)

const filters = ref({ enterpriseName: '', enterpriseType: '', riskLevel: '' })
const qualFilters = ref({ auditState: '', enterpriseName: '' })

const enterpriseTypeLabels: Record<number, string> = { 1: '供应商', 2: '加工商', 3: '物流商', 4: '零售商' }
const riskLevelLabels: Record<number, string> = { 1: '低风险', 2: '中风险', 3: '高风险' }
const statusLabels: Record<number, string> = { 1: '正常', 0: '停用' }
const qualTypeLabels: Record<number, string> = { 1: '营业执照', 2: '生产许可证', 3: '经营许可证', 4: '其他资质' }
const auditStateLabels: Record<number, string> = { 0: '待审核', 1: '审核通过', 2: '已拒绝' }

const stats = computed(() => ({
  total: list.value.length,
  highRisk: list.value.filter((r: any) => r.riskLevel === 3).length,
  normal: list.value.filter((r: any) => r.status === 1).length,
  disabled: list.value.filter((r: any) => r.status === 0).length,
}))

const qualStats = computed(() => ({
  total: qualList.value.length,
  pending: qualList.value.filter((r: any) => r.auditState === 0).length,
  approved: qualList.value.filter((r: any) => r.auditState === 1).length,
  rejected: qualList.value.filter((r: any) => r.auditState === 2).length,
}))

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function statusClass(label: string) {
  if (label.includes('低风险') || label === '正常' || label === '审核通过') return 'status-active'
  if (label.includes('中风险') || label === '待审核') return 'status-pending'
  if (label.includes('高风险') || label === '停用' || label === '已拒绝') return 'status-void'
  return 'status-active'
}

async function loadList() {
  loading.value = true
  try {
    const p: Record<string, any> = { page: 1, size: 200 }
    if (filters.value.enterpriseName) p.enterpriseName = filters.value.enterpriseName
    if (filters.value.enterpriseType) p.enterpriseType = Number(filters.value.enterpriseType)
    if (filters.value.riskLevel) p.riskLevel = Number(filters.value.riskLevel)
    const res = await enterpriseApi.list(p)
    list.value = (res as any).data?.records ?? (Array.isArray(res) ? res : [])
    currentPage.value = 1
  } catch (e: any) { notify('error', '加载企业列表失败: ' + e.message) }
  finally { loading.value = false }
}

async function loadQualList() {
  loading.value = true
  try {
    const p: Record<string, any> = {}
    if (qualFilters.value.auditState) p.auditState = Number(qualFilters.value.auditState)
    const res = isRegulator.value
      ? await qualificationApi.all(p)
      : await qualificationApi.my()
    qualList.value = (res as any).data ?? (Array.isArray(res) ? res : [])
    qualPage.value = 1
  } catch (e: any) { notify('error', '加载资质列表失败: ' + e.message) }
  finally { loading.value = false }
}

function resetFilters() {
  filters.value = { enterpriseName: '', enterpriseType: '', riskLevel: '' }
  loadList()
}

function openDetail(row: any) { viewing.value = row; showDetail.value = true }

const showQualDetail = ref(false)
const qualViewing = ref<any>(null)
function openQualDetail(row: any) { qualViewing.value = row; showQualDetail.value = true }

function openAuditDialog(id: number, action: 'approve' | 'reject') {
  auditingId.value = id
  auditAction.value = action
  auditRemark.value = ''
  showAuditDialog.value = true
}

async function doAudit() {
  try {
    if (auditAction.value === 'approve') {
      await qualificationApi.approve(auditingId.value!, auditRemark.value || undefined)
      notify('success', '资质已审核通过')
    } else {
      if (!auditRemark.value.trim()) { notify('error', '拒绝时请填写审核意见'); return }
      await qualificationApi.reject(auditingId.value!, auditRemark.value)
      notify('success', '资质已拒绝')
    }
    showAuditDialog.value = false
    showQualDetail.value = false
    auditRemark.value = ''
    loadQualList()
  } catch (e: any) { notify('error', '审核操作失败: ' + e.message) }
}

function confirmDelete(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDelete() { try { await enterpriseApi.delete(deletingId.value!); notify('success', '删除成功'); showConfirm.value = false; loadList() } catch (e: any) { notify('error', '删除失败: ' + e.message) } }

async function doCheckStatus() {
  statusChecking.value = true
  try { await enterpriseApi.checkStatus(); notify('success', '资质状态检查已触发'); loadList() }
  catch (e: any) { notify('error', '检查失败: ' + e.message) }
  finally { statusChecking.value = false }
}

const emit = defineEmits<{ navigate: [page: string] }>()
function goToUpload() { emit('navigate', 'qualification-upload') }

onMounted(() => {
  loadList()
  loadQualList()
})
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner" :class="isRegulator ? 'manufacturer' : 'supplier'">
      <span class="trace-role-badge">{{ isRegulator ? '监管审核' : '企业资质' }}</span>
      <span v-if="isRegulator">在线<strong>审批</strong>企业资质（通过/退回），含有效期管理和到期预警。</span>
      <span v-else>管理<strong>本企业</strong>的资质证书。提交后由监管机构在线审核。</span>
    </div>

// 资质上传入口
    <section class="trace-panel" style="margin-bottom:18px">
      <header class="panel-header">
        <div><p>资质操作</p><h2>{{ isRegulator ? '待审核资质' : '我的资质' }}</h2></div>
        <div style="display:flex;gap:8px">
          <button class="secondary" :disabled="statusChecking" @click="doCheckStatus"><el-icon><Refresh /></el-icon> 资质状态检查</button>
          <button class="primary create" @click="goToUpload"><el-icon><Plus /></el-icon> 上传/编辑资质</button>
        </div>
      </header>
    </section>

    <!-- 资质统计 -->
    <section class="trace-stats">
      <article><span><el-icon><OfficeBuilding /></el-icon> 资质总数</span><b>{{ qualStats.total }}</b><em>条记录</em></article>
      <article class="amber"><span><el-icon><Warning /></el-icon> 待审核</span><b>{{ qualStats.pending }}</b><em>需处理</em></article>
      <article class="green"><span><el-icon><Check /></el-icon> 审核通过</span><b>{{ qualStats.approved }}</b><em>已通过</em></article>
      <article class="red"><span><el-icon><Close /></el-icon> 已拒绝</span><b>{{ qualStats.rejected }}</b><em>已退回</em></article>
    </section>

    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'qual' }" @click="tab = 'qual'; loadQualList()"><el-icon><View /></el-icon> 资质管理</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'list' }" @click="tab = 'list'; loadList()"><el-icon><OfficeBuilding /></el-icon> 企业列表</button>
    </div>

    <!-- 资质管理 Tab -->
    <template v-if="tab === 'qual'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label v-if="isRegulator">
            审核状态
            <select v-model="qualFilters.auditState">
              <option value="">全部</option>
              <option value="0">待审核</option>
              <option value="1">审核通过</option>
              <option value="2">已拒绝</option>
            </select>
          </label>
          <div class="filter-actions"><button class="primary" @click="loadQualList"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>

      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>资质台账</p><h2>企业资质一览</h2></div></header>
        <div class="table-wrap"><table><thead><tr>
          <th>资质类型</th><th>资质编号</th><th>颁发机构</th><th>有效期起</th><th>有效期止</th><th>审核状态</th><th v-if="isRegulator">操作</th>
        </tr></thead>
          <tbody>
            <tr v-if="loading"><td :colspan="isRegulator ? 7 : 6" class="empty">加载中...</td></tr>
            <tr v-else-if="!qualList.length"><td :colspan="isRegulator ? 7 : 6" class="empty">暂无资质数据，请先上传资质</td></tr>
            <tr v-for="row in paginatedQualList" :key="row.qualificationId" @click="openQualDetail(row)" style="cursor:pointer">
              <td><span class="status status-active">{{ qualTypeLabels[row.qualificationType] || '-' }}</span></td>
              <td><code>{{ row.qualificationNo || '-' }}</code></td>
              <td>{{ row.issueAuthority || '-' }}</td>
              <td>{{ row.validFrom || '-' }}</td>
              <td>{{ row.validTo || '-' }}</td>
              <td><span class="status" :class="statusClass(auditStateLabels[row.auditState] || '')">{{ auditStateLabels[row.auditState] ?? '-' }}</span></td>
              <td v-if="isRegulator" class="actions" @click.stop>
                <button v-if="row.auditState === 0" class="success" @click="openAuditDialog(row.qualificationId, 'approve')"><el-icon><Check /></el-icon> 通过</button>
                <button v-if="row.auditState === 0" class="danger" @click="openAuditDialog(row.qualificationId, 'reject')"><el-icon><Close /></el-icon> 拒绝</button>
                <button class="secondary" @click="openQualDetail(row)"><el-icon><View /></el-icon> 详情</button>
                <span v-if="row.auditState !== 0" style="color:#889cb0;font-size:12px;margin-left:8px">{{ row.auditRemark || (row.auditState === 1 ? '已审核通过' : '已拒绝') }}</span>
              </td>
            </tr>
          </tbody></table></div>
      <Pagination v-model="qualPage" :total="qualList.length" :page-size="pageSize" />
      </section>
    </template>

    <!-- 企业列表 Tab -->
    <template v-if="tab === 'list'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>企业名称<input v-model="filters.enterpriseName" placeholder="企业名称" @keyup.enter="loadList" /></label>
          <label>企业类型<select v-model="filters.enterpriseType"><option value="">全部</option><option value="1">供应商</option><option value="2">加工商</option><option value="3">物流商</option><option value="4">零售商</option></select></label>
          <label>风险等级<select v-model="filters.riskLevel"><option value="">全部</option><option value="1">低风险</option><option value="2">中风险</option><option value="3">高风险</option></select></label>
          <div class="filter-actions"><button class="secondary" @click="resetFilters"><el-icon><Refresh /></el-icon> 重置</button><button class="primary" @click="loadList"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>

      <section class="trace-stats">
        <article><span><el-icon><OfficeBuilding /></el-icon> 企业总数</span><b>{{ stats.total }}</b><em>家注册企业</em></article>
        <article class="red"><span><el-icon><Warning /></el-icon> 高风险企业</span><b>{{ stats.highRisk }}</b><em>需要关注</em></article>
        <article class="green"><span><el-icon><Check /></el-icon> 正常运营</span><b>{{ stats.normal }}</b><em>资质有效</em></article>
        <article class="amber"><span><el-icon><Close /></el-icon> 已停用</span><b>{{ stats.disabled }}</b><em>已暂停</em></article>
      </section>

      <section class="trace-panel list-panel">
        <header class="panel-header">
          <div><p>企业台账</p><h2>企业列表</h2></div>
          <div style="display:flex;gap:8px">
            <button v-if="isRegulator" class="secondary" :disabled="statusChecking" @click="doCheckStatus"><el-icon><Refresh /></el-icon> 资质状态检查</button>
          </div>
        </header>
        <div class="table-wrap"><table><thead><tr><th>企业名称</th><th>企业类型</th><th>信用代码</th><th>联系人</th><th>联系电话</th><th>风险等级</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="8" class="empty">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="8" class="empty">暂无企业数据</td></tr>
            <tr v-for="row in paginatedList" :key="row.enterpriseId">
              <td><strong>{{ row.enterpriseName }}</strong></td>
              <td><span class="status status-active">{{ enterpriseTypeLabels[row.enterpriseType] || '-' }}</span></td>
              <td><code>{{ row.certNo || '-' }}</code></td>
              <td>{{ row.contactPerson || '-' }}</td>
              <td>{{ row.contactPhone || '-' }}</td>
              <td><span class="status" :class="statusClass(riskLevelLabels[row.riskLevel] || '')">{{ riskLevelLabels[row.riskLevel] || '-' }}</span></td>
              <td><span class="status" :class="row.status === 1 ? 'status-active' : 'status-void'">{{ statusLabels[row.status] ?? '-' }}</span></td>
              <td class="actions">
                <button @click="openDetail(row)"><el-icon><View /></el-icon> 详情</button>
                <button v-if="isRegulator" class="danger" @click="confirmDelete(row.enterpriseId)"><el-icon><Delete /></el-icon> 删除</button>
              </td>
            </tr>
          </tbody></table></div>
      <Pagination v-model="currentPage" :total="list.length" :page-size="pageSize" />
      </section>
    </template>

    <!-- 详情模态框 -->
    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false">
      <section class="trace-modal"><header><div><p>企业档案</p><h2>企业详情</h2></div><button @click="showDetail = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body" v-if="viewing">
          <div class="detail-grid">
            <div><span>企业名称</span><b>{{ viewing.enterpriseName }}</b></div>
            <div><span>企业UUID</span><code>{{ viewing.enterpriseUuid }}</code></div>
            <div><span>企业类型</span><b>{{ enterpriseTypeLabels[viewing.enterpriseType] }}</b></div>
            <div><span>信用代码</span><b>{{ viewing.certNo || '-' }}</b></div>
            <div><span>联系人</span><b>{{ viewing.contactPerson || '-' }}</b></div>
            <div><span>联系电话</span><b>{{ viewing.contactPhone || '-' }}</b></div>
            <div><span>注册地址</span><b>{{ viewing.address || '-' }}</b></div>
            <div><span>风险等级</span><b :style="{ color: viewing.riskLevel === 3 ? '#c04550' : viewing.riskLevel === 2 ? '#a4730a' : '#198658' }">{{ riskLevelLabels[viewing.riskLevel] || '-' }}</b></div>
            <div><span>状态</span><b :style="{ color: viewing.status === 1 ? '#198658' : '#c04550' }">{{ statusLabels[viewing.status] ?? '-' }}</b></div>
            <div><span>备注</span><b>{{ viewing.remark || '-' }}</b></div>
            <div><span>创建时间</span><b>{{ viewing.createTime || '-' }}</b></div>
            <div><span>更新时间</span><b>{{ viewing.updateTime || '-' }}</b></div>
          </div>
        </div>
        <footer><button class="secondary" @click="showDetail = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

    <!-- 资质详情模态框 -->
    <div v-if="showQualDetail" class="trace-modal-backdrop" @click.self="showQualDetail = false">
      <section class="trace-modal" style="width:560px">
        <header>
          <div><p>资质详情</p><h2>资质证书详细信息</h2></div>
          <button @click="showQualDetail = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body" v-if="qualViewing">
          <div class="detail-grid">
            <div><span>资质类型</span><b>{{ qualTypeLabels[qualViewing.qualificationType] || '-' }}</b></div>
            <div><span>资质编号</span><code>{{ qualViewing.qualificationNo || '-' }}</code></div>
            <div><span>颁发机构</span><b>{{ qualViewing.issueAuthority || '-' }}</b></div>
            <div><span>有效期起</span><b>{{ qualViewing.validFrom || '-' }}</b></div>
            <div><span>有效期止</span><b>{{ qualViewing.validTo || '-' }}</b></div>
            <div><span>文件路径</span><code>{{ qualViewing.filePath || '未上传' }}</code></div>
            <div><span>资质状态</span><b>{{ qualViewing.qualificationState === 1 ? '有效' : qualViewing.qualificationState === 2 ? '即将过期' : '已失效' }}</b></div>
            <div><span>审核状态</span><b :style="{ color: qualViewing.auditState === 1 ? '#198658' : qualViewing.auditState === 2 ? '#c04550' : '#a4730a' }">{{ auditStateLabels[qualViewing.auditState] ?? '-' }}</b></div>
            <div><span>审核意见</span><b>{{ qualViewing.auditRemark || '-' }}</b></div>
            <div><span>企业UUID</span><code>{{ qualViewing.enterpriseUuid }}</code></div>
            <div><span>创建时间</span><b>{{ qualViewing.createTime || '-' }}</b></div>
            <div><span>更新时间</span><b>{{ qualViewing.updateTime || '-' }}</b></div>
          </div>
          <!-- 详情页内直接审核 -->
          <div v-if="isRegulator && qualViewing.auditState === 0" style="margin-top:20px;padding-top:18px;border-top:1px solid #e0e6dd;display:flex;gap:10px">
            <textarea v-model="auditRemark" placeholder="审核意见（通过时选填，拒绝时必填）" style="flex:1;padding:10px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px;resize:vertical;font-size:13px" />
            <div style="display:flex;flex-direction:column;gap:8px">
              <button class="primary btn-success" @click="auditingId = qualViewing.qualificationId; auditAction = 'approve'; doAudit()"><el-icon><Check /></el-icon> 通过</button>
              <button class="primary btn-danger-fill" @click="auditingId = qualViewing.qualificationId; auditAction = 'reject'; doAudit()"><el-icon><Close /></el-icon> 拒绝</button>
            </div>
          </div>
        </div>
        <footer><button class="secondary" @click="showQualDetail = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

    <!-- 审核对话框 -->
    <div v-if="showAuditDialog" class="trace-modal-backdrop" @click.self="showAuditDialog = false">
      <section class="trace-modal" style="width:480px">
        <header>
          <div><p>资质审核</p><h2>{{ auditAction === 'approve' ? '审核通过' : '审核拒绝' }}</h2></div>
          <button @click="showAuditDialog = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body">
          <label style="display:grid;gap:8px;color:#718ba6;font-size:13px;font-weight:700">
            {{ auditAction === 'approve' ? '审核意见（选填）' : '拒绝原因 *' }}
            <textarea v-model="auditRemark" :placeholder="auditAction === 'approve' ? '可选填审核备注' : '请填写拒绝原因'" style="width:100%;padding:10px;border:1px solid #d7e4f0;border-radius:7px;min-height:80px;resize:vertical;font-size:13px" />
          </label>
        </div>
        <footer>
          <button class="secondary" @click="showAuditDialog = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" :class="auditAction === 'reject' ? 'btn-danger-fill' : 'btn-success'" @click="doAudit">
            <el-icon><Check v-if="auditAction === 'approve'" /><Close v-else /></el-icon>
            {{ auditAction === 'approve' ? '确认通过' : '确认拒绝' }}
          </button>
        </footer>
      </section>
    </div>

    <!-- 删除确认 -->
    <div v-if="showConfirm" class="trace-confirm-overlay" @click.self="showConfirm = false">
      <div class="trace-confirm-box"><h3>确认删除</h3><p>确定要删除该企业吗？此操作为逻辑删除。</p><div class="trace-confirm-actions"><button class="secondary" @click="showConfirm = false"><el-icon><Close /></el-icon> 取消</button><button class="primary btn-danger-fill" @click="doDelete"><el-icon><Delete /></el-icon> 确认删除</button></div></div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

.success { color: #198658 !important; }
</style>
