<script setup lang="ts">
import { ref, onMounted, inject, type Ref } from 'vue'
import { Check, Close, Refresh, Search, View, Warning, Timer, CircleCheck, CircleClose, Promotion } from '@element-plus/icons-vue'
import Pagination from '../components/Pagination.vue'
import { complaintApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')

// ==================== State ====================
const loading = ref(false)
const list = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const complaintTypeLabels: Record<number, string> = { 1: '产品质量', 2: '食品安全', 3: '包装问题', 4: '虚假宣传', 5: '其他' }
const statusLabels: Record<number, string> = { 1: '已提交', 2: '处理中', 3: '已处理', 4: '已驳回' }
const priorityLabels: Record<number, string> = { 1: '普通', 2: '紧急', 3: '重大' }
const actionLabels: Record<string, string> = { SUBMIT: '消费者提交', ACCEPT: '监管受理', INVESTIGATE: '调查取证', RESPOND: '回复反馈', CLOSE: '办结关闭', REJECT: '驳回' }

const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showDetail = ref(false)
const showConfirm = ref(false)
const viewing = ref<any>(null)
const handleLogs = ref<any[]>([])

// Handle action state
const showHandleDialog = ref(false)
const handleAction = ref<'ACCEPT' | 'INVESTIGATE' | 'RESPOND' | 'CLOSE' | 'REJECT'>('ACCEPT')
const handleRemark = ref('')
const handleConclusion = ref('')
const handleProofUrls = ref('')
const handlePriority = ref<number>(1)
const handlingId = ref<number | null>(null)

const filters = ref({ status: '', complaintType: '', priority: '', complainantName: '', enterpriseName: '', submitTimeStart: '', submitTimeEnd: '' })

// ==================== Stats ====================
const stats = ref({ total: 0, pending: 0, processing: 0, resolved: 0, rejected: 0, urgent: 0 })

async function loadStats() {
  try {
    const json: any = await complaintApi.stats()
    if (json.code === 200 && json.data) {
      stats.value = json.data
    }
  } catch { /* ignore */ }
}

// ==================== API helpers ====================
function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

function statusClass(status: number) {
  if (status === 1) return 'status-pending'
  if (status === 2) return 'status-active'
  if (status === 3) return 'status-active'
  if (status === 4) return 'status-void'
  return ''
}

function priorityClass(priority: number) {
  if (priority === 3) return 'status-void'
  if (priority === 2) return 'status-pending'
  return 'status-active'
}

// ==================== Load data ====================
async function loadList() {
  loading.value = true
  try {
    const body: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (filters.value.status) body.status = Number(filters.value.status)
    if (filters.value.complaintType) body.complaintType = Number(filters.value.complaintType)
    if (filters.value.priority) body.priority = Number(filters.value.priority)
    if (filters.value.complainantName) body.complainantName = filters.value.complainantName
    if (filters.value.enterpriseName) body.enterpriseName = filters.value.enterpriseName
    if (filters.value.submitTimeStart) body.submitTimeStart = filters.value.submitTimeStart
    if (filters.value.submitTimeEnd) body.submitTimeEnd = filters.value.submitTimeEnd

    const json: any = await complaintApi.page(body)
    if (json.code === 200 && json.data) {
      list.value = json.data.records ?? []
      total.value = json.data.total ?? 0
    } else {
      notify('error', json.message || '加载失败')
    }
  } catch (e: any) { notify('error', '加载投诉列表失败: ' + e.message) }
  finally { loading.value = false }
}

function resetFilters() {
  filters.value = { status: '', complaintType: '', priority: '', complainantName: '', enterpriseName: '', submitTimeStart: '', submitTimeEnd: '' }
  currentPage.value = 1
  loadList()
}

function onPageChange(page: number) {
  currentPage.value = page
  loadList()
}

// ==================== Detail ====================
async function openDetail(row: any) {
  viewing.value = row
  handleLogs.value = []
  showDetail.value = true
  try {
    const json: any = await complaintApi.detail(row.complaintRecordId)
    if (json.code === 200 && json.data) {
      viewing.value = json.data.complaint
      handleLogs.value = json.data.handleLogs ?? []
    }
  } catch { /* ignore */ }
}

// ==================== Handle actions ====================
function openHandleDialog(id: number, action: 'ACCEPT' | 'INVESTIGATE' | 'RESPOND' | 'CLOSE' | 'REJECT') {
  handlingId.value = id
  handleAction.value = action
  handleRemark.value = ''
  handleConclusion.value = ''
  handleProofUrls.value = ''
  handlePriority.value = 1
  showHandleDialog.value = true
}

async function doHandle() {
  if (!handlingId.value) return
  try {
    const body: Record<string, any> = {
      complaintRecordId: handlingId.value,
      action: handleAction.value,
    }
    if (handleRemark.value) body.remark = handleRemark.value
    if (handleConclusion.value) body.handlingConclusion = handleConclusion.value
    if (handleProofUrls.value) body.handlingProofUrls = handleProofUrls.value
    if (handleAction.value === 'ACCEPT' && handlePriority.value) body.priority = handlePriority.value

    const json: any = await complaintApi.handle(body)
    if (json.code === 200) {
      notify('success', actionLabels[handleAction.value] + '成功')
      showHandleDialog.value = false
      loadList()
      loadStats()
      if (showDetail.value && viewing.value) openDetail(viewing.value)
    } else {
      notify('error', json.message || '操作失败')
    }
  } catch (e: any) { notify('error', '操作失败: ' + e.message) }
}

// ==================== Lifecycle ====================
onMounted(() => { loadList(); loadStats() })
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner regulator">
      <span class="trace-role-badge">投诉处理</span>
      <span>监管端<strong>投诉处理中心</strong>，受理消费者投诉，跟踪处理全过程。</span>
    </div>

    <!-- 统计卡片 -->
    <section class="trace-stats">
      <article><span><el-icon><Promotion /></el-icon> 投诉总数</span><b>{{ stats.total }}</b><em>条记录</em></article>
      <article class="amber"><span><el-icon><Timer /></el-icon> 待受理</span><b>{{ stats.pending }}</b><em>需处理</em></article>
      <article class="green"><span><el-icon><CircleCheck /></el-icon> 处理中</span><b>{{ stats.processing }}</b><em>进行中</em></article>
      <article class="green"><span><el-icon><Check /></el-icon> 已处理</span><b>{{ stats.resolved }}</b><em>已办结</em></article>
      <article class="red"><span><el-icon><CircleClose /></el-icon> 已驳回</span><b>{{ stats.rejected }}</b><em>已退回</em></article>
      <article class="amber"><span><el-icon><Warning /></el-icon> 紧急/重大</span><b>{{ stats.urgent }}</b><em>优先处理</em></article>
    </section>

    <!-- 筛选 -->
    <section class="trace-panel filter-panel">
      <div class="filter-grid-4">
        <label>状态
          <select v-model="filters.status">
            <option value="">全部</option>
            <option value="1">已提交</option>
            <option value="2">处理中</option>
            <option value="3">已处理</option>
            <option value="4">已驳回</option>
          </select>
        </label>
        <label>投诉类型
          <select v-model="filters.complaintType">
            <option value="">全部</option>
            <option value="1">产品质量</option>
            <option value="2">食品安全</option>
            <option value="3">包装问题</option>
            <option value="4">虚假宣传</option>
            <option value="5">其他</option>
          </select>
        </label>
        <label>优先级
          <select v-model="filters.priority">
            <option value="">全部</option>
            <option value="1">普通</option>
            <option value="2">紧急</option>
            <option value="3">重大</option>
          </select>
        </label>
        <label>投诉人<input v-model="filters.complainantName" placeholder="投诉人姓名" @keyup.enter="loadList" /></label>
        <label>被投诉企业<input v-model="filters.enterpriseName" placeholder="企业名称" @keyup.enter="loadList" /></label>
        <label>提交时间起<input type="date" v-model="filters.submitTimeStart" /></label>
        <label>提交时间止<input type="date" v-model="filters.submitTimeEnd" /></label>
        <div class="filter-actions">
          <button class="secondary" @click="resetFilters"><el-icon><Refresh /></el-icon> 重置</button>
          <button class="primary" @click="loadList"><el-icon><Search /></el-icon> 查询</button>
        </div>
      </div>
    </section>

    <!-- 投诉列表 -->
    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>投诉台账</p><h2>消费者投诉一览</h2></div>
        <button class="secondary" @click="loadList"><el-icon><Refresh /></el-icon> 刷新</button>
      </header>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>投诉编号</th>
              <th>投诉类型</th>
              <th>投诉人</th>
              <th>联系电话</th>
              <th>被投诉企业</th>
              <th>优先级</th>
              <th>状态</th>
              <th>提交时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="9" class="empty">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="9" class="empty">暂无投诉数据</td></tr>
            <tr v-for="row in list" :key="row.complaintRecordId">
              <td><code>{{ row.complaintNo }}</code></td>
              <td>{{ complaintTypeLabels[row.complaintType] || '-' }}</td>
              <td>
                <template v-if="row.isAnonymous">匿名用户</template>
                <strong v-else>{{ row.complainantName || '-' }}</strong>
              </td>
              <td>{{ row.isAnonymous ? '***' : (row.phone || '-') }}</td>
              <td>{{ row.enterpriseName || '-' }}</td>
              <td><span class="status" :class="priorityClass(row.priority)">{{ priorityLabels[row.priority] || '-' }}</span></td>
              <td><span class="status" :class="statusClass(row.status)">{{ statusLabels[row.status] ?? '-' }}</span></td>
              <td>{{ row.submitTime || '-' }}</td>
              <td class="actions">
                <button @click="openDetail(row)"><el-icon><View /></el-icon> 详情</button>
                <button v-if="row.status === 1" class="success" @click="openHandleDialog(row.complaintRecordId, 'ACCEPT')"><el-icon><Check /></el-icon> 受理</button>
                <button v-if="row.status === 2" class="primary" @click="openHandleDialog(row.complaintRecordId, 'INVESTIGATE')"><el-icon><Search /></el-icon> 调查</button>
                <button v-if="row.status === 2" class="success" @click="openHandleDialog(row.complaintRecordId, 'CLOSE')"><el-icon><CircleCheck /></el-icon> 办结</button>
                <button v-if="row.status === 1" class="danger" @click="openHandleDialog(row.complaintRecordId, 'REJECT')"><el-icon><Close /></el-icon> 驳回</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <Pagination :model-value="currentPage" :total="total" :page-size="pageSize" @update:model-value="onPageChange" />
    </section>

    <!-- 详情模态框 -->
    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false">
      <section class="trace-modal" style="width:680px">
        <header>
          <div><p>{{ viewing?.complaintNo }}</p><h2>投诉详情</h2></div>
          <button @click="showDetail = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body" v-if="viewing">
          <!-- 基本信息 -->
          <h3 style="margin:0 0 12px;color:#48627e;font-size:14px">基本信息</h3>
          <div class="detail-grid">
            <div><span>投诉编号</span><code>{{ viewing.complaintNo }}</code></div>
            <div><span>溯源码</span><code>{{ viewing.traceCode || '-' }}</code></div>
            <div><span>关联批次</span><code>{{ viewing.batchNumber || '-' }}</code></div>
            <div><span>投诉类型</span><b>{{ complaintTypeLabels[viewing.complaintType] || '-' }}</b></div>
            <div><span>投诉人</span><b>{{ viewing.isAnonymous ? '匿名用户' : (viewing.complainantName || '-') }}</b></div>
            <div><span>联系电话</span><b>{{ viewing.isAnonymous ? '***' : (viewing.phone || '-') }}</b></div>
            <div><span>被投诉企业</span><b>{{ viewing.enterpriseName || '-' }}</b></div>
            <div><span>优先级</span><b :style="{ color: viewing.priority === 3 ? '#c04550' : viewing.priority === 2 ? '#a4730a' : '#198658' }">{{ priorityLabels[viewing.priority] || '-' }}</b></div>
            <div><span>状态</span><b :style="{ color: viewing.status === 4 ? '#c04550' : '#198658' }">{{ statusLabels[viewing.status] ?? '-' }}</b></div>
            <div><span>提交时间</span><b>{{ viewing.submitTime || '-' }}</b></div>
            <div><span>受理时间</span><b>{{ viewing.acceptTime || '-' }}</b></div>
            <div><span>办结时间</span><b>{{ viewing.closeTime || '-' }}</b></div>
          </div>

          <!-- 投诉描述 -->
          <div class="detail-grid" style="margin-top:16px">
            <div class="full"><span>投诉描述</span><p style="margin:4px 0 0;line-height:1.6;white-space:pre-wrap">{{ viewing.description || '-' }}</p></div>
            <div class="full" v-if="viewing.photoUrls"><span>照片附件</span><code style="word-break:break-all">{{ viewing.photoUrls }}</code></div>
          </div>

          <!-- 处理信息 -->
          <template v-if="viewing.handlerName">
            <h3 style="margin:20px 0 12px;color:#48627e;font-size:14px">处理信息</h3>
            <div class="detail-grid">
              <div><span>处理人</span><b>{{ viewing.handlerName }}</b></div>
              <div class="full" v-if="viewing.handlingConclusion"><span>处理结论</span><p style="margin:4px 0 0;line-height:1.6">{{ viewing.handlingConclusion }}</p></div>
              <div class="full" v-if="viewing.handlingProofUrls"><span>处理凭证</span><code style="word-break:break-all">{{ viewing.handlingProofUrls }}</code></div>
            </div>
          </template>

          <!-- 处理日志链路 -->
          <template v-if="handleLogs.length">
            <h3 style="margin:20px 0 12px;color:#48627e;font-size:14px">处理日志</h3>
            <div class="timeline" style="border-left:2px solid #d7e4f0;padding-left:20px;margin-left:8px">
              <div v-for="(log, i) in handleLogs" :key="i" style="position:relative;padding:0 0 16px 0">
                <span style="position:absolute;left:-27px;top:2px;width:12px;height:12px;border-radius:50%;background:#198658;display:block"></span>
                <p style="margin:0;font-weight:700;font-size:13px;color:#2a4a6b">{{ actionLabels[log.action] || log.action }}
                  <span style="font-weight:400;color:#889cb0;font-size:12px;margin-left:8px">{{ log.createTime }}</span>
                </p>
                <p v-if="log.remark" style="margin:4px 0 0;color:#48627e;font-size:13px">{{ log.remark }}</p>
                <p style="margin:2px 0 0;color:#889cb0;font-size:12px">操作人: {{ log.operatorName || '-' }}
                  <span v-if="log.beforeStatus !== null && log.afterStatus !== null" style="margin-left:8px">
                    {{ statusLabels[log.beforeStatus] || '?' }} → {{ statusLabels[log.afterStatus] || '?' }}
                  </span>
                </p>
              </div>
            </div>
          </template>
        </div>
        <footer>
          <!-- 详情页内快捷操作 -->
          <div v-if="viewing && viewing.status === 1" style="display:flex;gap:8px;margin-right:auto">
            <button class="primary btn-success" @click="showDetail=false; openHandleDialog(viewing.complaintRecordId, 'ACCEPT')"><el-icon><Check /></el-icon> 受理</button>
            <button class="primary btn-danger-fill" @click="showDetail=false; openHandleDialog(viewing.complaintRecordId, 'REJECT')"><el-icon><Close /></el-icon> 驳回</button>
          </div>
          <div v-else-if="viewing && viewing.status === 2" style="display:flex;gap:8px;margin-right:auto">
            <button class="secondary" @click="showDetail=false; openHandleDialog(viewing.complaintRecordId, 'INVESTIGATE')"><el-icon><Search /></el-icon> 调查</button>
            <button class="primary btn-success" @click="showDetail=false; openHandleDialog(viewing.complaintRecordId, 'CLOSE')"><el-icon><CircleCheck /></el-icon> 办结</button>
          </div>
          <button class="secondary" @click="showDetail = false"><el-icon><Close /></el-icon> 关闭</button>
        </footer>
      </section>
    </div>

    <!-- 处理操作对话框 -->
    <div v-if="showHandleDialog" class="trace-modal-backdrop" @click.self="showHandleDialog = false">
      <section class="trace-modal" style="width:500px">
        <header>
          <div><p>投诉处理</p><h2>{{ actionLabels[handleAction] }}</h2></div>
          <button @click="showHandleDialog = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body">
          <!-- 受理时可调优先级 -->
          <label v-if="handleAction === 'ACCEPT'" style="display:grid;gap:8px;color:#718ba6;font-size:13px;font-weight:700;margin-bottom:14px">
            优先级
            <select v-model="handlePriority" style="width:100%;padding:10px;border:1px solid #d7e4f0;border-radius:7px;font-size:13px">
              <option :value="1">普通</option>
              <option :value="2">紧急</option>
              <option :value="3">重大</option>
            </select>
          </label>
          <label style="display:grid;gap:8px;color:#718ba6;font-size:13px;font-weight:700;margin-bottom:14px">
            处理备注
            <textarea v-model="handleRemark" placeholder="请输入处理备注说明" style="width:100%;padding:10px;border:1px solid #d7e4f0;border-radius:7px;min-height:80px;resize:vertical;font-size:13px" />
          </label>
          <template v-if="handleAction === 'CLOSE' || handleAction === 'REJECT'">
            <label style="display:grid;gap:8px;color:#718ba6;font-size:13px;font-weight:700;margin-bottom:14px">
              处理结论 {{ handleAction === 'CLOSE' ? '' : '(选填)' }}
              <textarea v-model="handleConclusion" placeholder="请输入处理结论" style="width:100%;padding:10px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px;resize:vertical;font-size:13px" />
            </label>
          </template>
          <label v-if="handleAction === 'CLOSE'" style="display:grid;gap:8px;color:#718ba6;font-size:13px;font-weight:700">
            处理凭证URL（选填）
            <input v-model="handleProofUrls" placeholder="多个URL用逗号分隔" style="width:100%;padding:10px;border:1px solid #d7e4f0;border-radius:7px;font-size:13px" />
          </label>
        </div>
        <footer>
          <button class="secondary" @click="showHandleDialog = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" :class="handleAction === 'REJECT' ? 'btn-danger-fill' : 'btn-success'" @click="doHandle">
            <el-icon><Check /></el-icon> 确认{{ actionLabels[handleAction] }}
          </button>
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

.timeline > div:last-child { padding-bottom: 0; }
.success { color: #198658 !important; }
</style>
