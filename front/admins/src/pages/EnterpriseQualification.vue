<script setup lang="ts">
import { ref, computed, onMounted, inject, type Ref } from 'vue'
import { Check, Close, Delete, OfficeBuilding, Plus, Refresh, Search, View, Warning } from '@element-plus/icons-vue'
import { enterpriseApi } from '../services/api'
import Pagination from '../components/Pagination.vue'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
const isRegulator = computed(() => currentRole?.value === 'regulator' || currentRole?.value === 'super-admin')

const tab = ref<'list' | 'qual'>('list')
const loading = ref(false)
const list = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return list.value.slice(start, start + pageSize.value)
})
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showDetail = ref(false)
const showConfirm = ref(false)
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

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function statusClass(label: string) {
  if (label.includes('低风险') || label === '正常') return 'status-active'
  if (label.includes('中风险')) return 'status-pending'
  if (label.includes('高风险') || label === '停用') return 'status-void'
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
  } catch (e: any) { notify('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function resetFilters() {
  filters.value = { enterpriseName: '', enterpriseType: '', riskLevel: '' }
  loadList()
}

function openDetail(row: any) { viewing.value = row; showDetail.value = true }

function confirmDelete(id: number) { deletingId.value = id; showConfirm.value = true }
async function doDelete() { try { await enterpriseApi.delete(deletingId.value!); notify('success', '删除成功'); showConfirm.value = false; loadList() } catch (e: any) { notify('error', '删除失败: ' + e.message) } }

async function doCheckStatus() {
  statusChecking.value = true
  try { await enterpriseApi.checkStatus(); notify('success', '资质状态检查已触发'); loadList() }
  catch (e: any) { notify('error', '检查失败: ' + e.message) }
  finally { statusChecking.value = false }
}

// 跳转到资质上传页面
const emit = defineEmits<{ navigate: [page: string] }>()
function goToUpload() { emit('navigate', 'qualification-upload') }

onMounted(loadList)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner" :class="isRegulator ? 'manufacturer' : 'supplier'">
      <span class="trace-role-badge">{{ isRegulator ? '监管审核' : '企业资质' }}</span>
      <span v-if="isRegulator">在线<strong>审批</strong>企业资质（通过/退回/驳回），含有效期管理和到期预警。</span>
      <span v-else>上传并维护<strong>本企业</strong>的资质证书，监管机构在线审核。</span>
    </div>

    <section class="trace-stats">
      <article><span><el-icon><OfficeBuilding /></el-icon> 企业总数</span><b>{{ stats.total }}</b><em>家注册企业</em></article>
      <article class="red"><span><el-icon><Warning /></el-icon> 高风险企业</span><b>{{ stats.highRisk }}</b><em>需要关注</em></article>
      <article class="green"><span><el-icon><Check /></el-icon> 正常运营</span><b>{{ stats.normal }}</b><em>资质有效</em></article>
      <article class="amber"><span><el-icon><Close /></el-icon> 已停用</span><b>{{ stats.disabled }}</b><em>已暂停</em></article>
    </section>

    <!-- 资质上传入口提示 -->
    <section class="trace-panel" style="margin-bottom:18px">
      <header class="panel-header">
        <div><p>资质管理</p><h2>{{ isRegulator ? '企业资质管理' : '本企业资质管理' }}</h2></div>
        <button class="primary create" @click="goToUpload"><el-icon><Plus /></el-icon> 上传/编辑资质</button>
      </header>
      <div class="panel-body">
        <div class="trace-hint info">
          💡 点击上方<strong>「上传/编辑资质」</strong>按钮进入专门的资质上传页面。
          在该页面中可以新增、编辑企业资质信息，提交后由监管机构审核。
        </div>
      </div>
    </section>

    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'list' }" @click="tab = 'list'"><el-icon><OfficeBuilding /></el-icon> 企业列表</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'qual' }" @click="tab = 'qual'"><el-icon><View /></el-icon> 资质管理</button>
    </div>

    <template v-if="tab === 'list'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>企业名称<input v-model="filters.enterpriseName" placeholder="企业名称" @keyup.enter="loadList" /></label>
          <label>企业类型<select v-model="filters.enterpriseType"><option value="">全部</option><option value="1">供应商</option><option value="2">加工商</option><option value="3">物流商</option><option value="4">零售商</option></select></label>
          <label>风险等级<select v-model="filters.riskLevel"><option value="">全部</option><option value="1">低风险</option><option value="2">中风险</option><option value="3">高风险</option></select></label>
          <div class="filter-actions"><button class="secondary" @click="resetFilters"><el-icon><Refresh /></el-icon> 重置</button><button class="primary" @click="loadList"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
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

    <template v-if="tab === 'qual'">
      <section class="trace-panel filter-panel">
        <div class="filter-grid-4">
          <label>企业名称<input v-model="filters.enterpriseName" placeholder="企业名称" @keyup.enter="loadList" /></label>
          <label>风险等级<select v-model="filters.riskLevel"><option value="">全部</option><option value="1">低风险</option><option value="2">中风险</option><option value="3">高风险</option></select></label>
          <div class="filter-actions"><button class="primary" @click="loadList"><el-icon><Search /></el-icon> 查询</button></div>
        </div>
      </section>
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>资质台账</p><h2>企业资质一览</h2></div><button class="secondary" :disabled="statusChecking" @click="doCheckStatus"><el-icon><Refresh /></el-icon> 资质状态检查</button></header>
        <div class="table-wrap"><table><thead><tr><th>企业名称</th><th>企业类型</th><th>信用代码</th><th>联系人</th><th>风险等级</th><th>运营状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="7" class="empty">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="7" class="empty">暂无企业资质数据</td></tr>
            <tr v-for="row in paginatedList" :key="row.enterpriseId">
              <td><strong>{{ row.enterpriseName }}</strong></td>
              <td><span class="status status-active">{{ enterpriseTypeLabels[row.enterpriseType] || '-' }}</span></td>
              <td><code>{{ row.certNo || '-' }}</code></td>
              <td>{{ row.contactPerson || '-' }}</td>
              <td><span class="status" :class="statusClass(riskLevelLabels[row.riskLevel] || '')">{{ riskLevelLabels[row.riskLevel] || '-' }}</span></td>
              <td><span class="status" :class="row.status === 1 ? 'status-active' : 'status-void'">{{ statusLabels[row.status] ?? '-' }}</span></td>
              <td class="actions"><button @click="openDetail(row)"><el-icon><View /></el-icon> 详情</button></td>
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

    <!-- 删除确认 -->
    <div v-if="showConfirm" class="trace-confirm-overlay" @click.self="showConfirm = false">
      <div class="trace-confirm-box"><h3>确认删除</h3><p>确定要删除该企业吗？此操作为逻辑删除。</p><div class="trace-confirm-actions"><button class="secondary" @click="showConfirm = false"><el-icon><Close /></el-icon> 取消</button><button class="primary danger-fill" @click="doDelete"><el-icon><Delete /></el-icon> 确认删除</button></div></div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';
</style>
