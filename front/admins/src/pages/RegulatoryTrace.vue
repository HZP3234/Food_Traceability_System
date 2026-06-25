<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Check, Close, Document, Key, Search, View } from '@element-plus/icons-vue'
import { traceApi } from '../services/api'
import Pagination from '../components/Pagination.vue'

const searchMode = ref<'code' | 'batch' | 'enterprise'>('code')
const searchValue = ref('')
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
const showHash = ref(false)
const showDisable = ref(false)
const showVoid = ref(false)
const viewing = ref<any>(null)
const hashResult = ref<any>(null)
const actionTarget = ref<any>(null)
const reason = ref('')

const codeTypeLabels: Record<number, string> = { 1: '单品码', 2: '箱码', 3: '托盘码' }
const statusLabels: Record<number, string> = { 0: '正常', 1: '禁用', 2: '作废' }

const stats = computed(() => ({
  total: list.value.length,
  active: list.value.filter((r: any) => r.traceCodeStatus === 0).length,
  disabled: list.value.filter((r: any) => r.traceCodeStatus === 1).length,
  voided: list.value.filter((r: any) => r.traceCodeStatus === 2).length,
}))

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }
function statusClass(s: number) {
  if (s === 0) return 'status-active'
  if (s === 1) return 'status-disabled'
  return 'status-void'
}

async function doSearch() {
  if (!searchValue.value.trim()) { notify('error', '请输入查询条件'); return }
  loading.value = true
  try {
    if (searchMode.value === 'code') {
      const res = await traceApi.getByCode(searchValue.value.trim())
      const data = (res as any).data ?? res
      list.value = data ? [data] : []
    } else if (searchMode.value === 'batch') {
      const res = await traceApi.listByBatch(searchValue.value.trim())
      list.value = (res as any).data ?? (Array.isArray(res) ? res : [])
    } else {
      const res = await traceApi.listByEnterprise(searchValue.value.trim())
      list.value = (res as any).data ?? (Array.isArray(res) ? res : [])
    }
    if (!list.value.length) notify('error', '未找到匹配的溯源码')
    currentPage.value = 1
  } catch (e: any) { notify('error', '查询失败: ' + e.message); list.value = [] }
  finally { loading.value = false }
}

function openDetail(row: any) { viewing.value = row; showDetail.value = true }

async function doVerifyHash(row: any) {
  try {
    const res = await traceApi.verifyHash(row.traceCode)
    hashResult.value = (res as any).data ?? res
    actionTarget.value = row
    showHash.value = true
  } catch (e: any) { notify('error', '校验失败: ' + e.message) }
}

function openDisable(row: any) { actionTarget.value = row; reason.value = ''; showDisable.value = true }
async function confirmDisable() {
  if (!reason.value.trim()) { notify('error', '请输入禁用原因'); return }
  try {
    await traceApi.disable(actionTarget.value.traceCode, reason.value.trim())
    notify('success', '溯源码已禁用'); showDisable.value = false; doSearch()
  } catch (e: any) { notify('error', '禁用失败: ' + e.message) }
}

function openVoid(row: any) { actionTarget.value = row; reason.value = ''; showVoid.value = true }
async function confirmVoid() {
  if (!reason.value.trim()) { notify('error', '请输入作废原因'); return }
  try {
    await traceApi.void(actionTarget.value.traceCode, reason.value.trim())
    notify('success', '溯源码已作废'); showVoid.value = false; doSearch()
  } catch (e: any) { notify('error', '作废失败: ' + e.message) }
}

onMounted(() => { list.value = [] })
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <section class="trace-stats">
      <article><span><el-icon><Document /></el-icon> 当前结果</span><b>{{ stats.total }}</b><em>条记录</em></article>
      <article class="green"><span><el-icon><Check /></el-icon> 正常</span><b>{{ stats.active }}</b><em>可正常使用</em></article>
      <article class="amber"><span><el-icon><Close /></el-icon> 已禁用</span><b>{{ stats.disabled }}</b><em>已停用</em></article>
      <article class="red"><span><el-icon><Close /></el-icon> 已作废</span><b>{{ stats.voided }}</b><em>已废弃</em></article>
    </section>

    <section class="trace-panel filter-panel">
      <div class="filter-grid-4">
        <label>查询方式<select v-model="searchMode"><option value="code">按溯源码</option><option value="batch">按批次号</option><option value="enterprise">按企业UUID</option></select></label>
        <label>{{ searchMode === 'code' ? '溯源码' : searchMode === 'batch' ? '批次号' : '企业UUID' }}
          <input v-model="searchValue" :placeholder="searchMode === 'code' ? '输入溯源码精确查询' : searchMode === 'batch' ? '输入批次号' : '输入企业UUID'" @keyup.enter="doSearch" />
        </label>
        <div class="filter-actions" style="align-self:end">
          <button class="primary" @click="doSearch"><el-icon><Search /></el-icon> 查询</button>
        </div>
      </div>
    </section>

    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>溯源备案</p><h2>溯源码列表</h2></div>
      </header>
      <div class="table-wrap">
        <table>
          <thead><tr><th>溯源码</th><th>产品名称</th><th>企业名称</th><th>编码类型</th><th>批次号</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="7" class="empty">查询中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="7" class="empty">请选择查询方式并输入条件后点击查询</td></tr>
            <tr v-for="row in paginatedList" :key="row.traceCodeId ?? row.traceCode">
              <td><code>{{ row.traceCode }}</code></td>
              <td>{{ row.productName || '-' }}</td>
              <td>{{ row.enterpriseName || '-' }}</td>
              <td><span class="status status-active">{{ codeTypeLabels[row.codeType] || '-' }}</span></td>
              <td><code>{{ row.batchNo || '-' }}</code></td>
              <td><span class="status" :class="statusClass(row.traceCodeStatus)">{{ statusLabels[row.traceCodeStatus] ?? '-' }}</span></td>
              <td class="actions">
                <button @click="openDetail(row)"><el-icon><View /></el-icon> 详情</button>
                <button @click="doVerifyHash(row)"><el-icon><Key /></el-icon> 验真</button>
                <button v-if="row.traceCodeStatus === 0" @click="openDisable(row)"><el-icon><Close /></el-icon> 禁用</button>
                <button v-if="row.traceCodeStatus !== 2" class="danger" @click="openVoid(row)"><el-icon><Close /></el-icon> 作废</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <Pagination v-model="currentPage" :total="list.length" :page-size="pageSize" />
    </section>

    <!-- 详情模态框 -->
    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false">
      <section class="trace-modal">
        <header><div><p>编码备案</p><h2>溯源码详情</h2></div><button @click="showDetail = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body" v-if="viewing">
          <div class="detail-grid">
            <div><span>溯源码</span><code>{{ viewing.traceCode }}</code></div>
            <div><span>产品名称</span><b>{{ viewing.productName || '-' }}</b></div>
            <div><span>企业名称</span><b>{{ viewing.enterpriseName || '-' }}</b></div>
            <div><span>编码类型</span><b>{{ codeTypeLabels[viewing.codeType] || '-' }}</b></div>
            <div><span>包装层级</span><b>{{ viewing.packageLevel ?? '-' }}</b></div>
            <div><span>批次号</span><code>{{ viewing.batchNo || '-' }}</code></div>
            <div class="full"><span>内容Hash (SHA-256)</span><code>{{ viewing.contentHash || '-' }}</code></div>
            <div><span>存证ID</span><code>{{ viewing.proofId || '-' }}</code></div>
            <div><span>交易Hash</span><code>{{ viewing.txHash || '-' }}</code></div>
            <div><span>生成次数</span><b>{{ viewing.generateCount ?? 0 }}</b></div>
            <div><span>过期时间</span><b>{{ viewing.expireTime || '-' }}</b></div>
            <div><span>状态</span><b :style="{ color: viewing.traceCodeStatus === 0 ? '#198658' : '#c04550' }">{{ statusLabels[viewing.traceCodeStatus] ?? '-' }}</b></div>
            <div><span>禁用原因</span><b>{{ viewing.disableReason || '-' }}</b></div>
            <div><span>作废原因</span><b>{{ viewing.voidReason || '-' }}</b></div>
            <div><span>创建时间</span><b>{{ viewing.createTime || '-' }}</b></div>
          </div>
        </div>
        <footer><button class="secondary" @click="showDetail = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

    <!-- Hash校验模态框 -->
    <div v-if="showHash" class="trace-modal-backdrop" @click.self="showHash = false">
      <section class="trace-modal" style="width:520px">
        <header><div><p>校验结果</p><h2>Hash 完整性校验</h2></div><button @click="showHash = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body" style="text-align:center;padding:20px" v-if="hashResult">
          <div v-if="hashResult.hashValid" style="font-size:60px;margin-bottom:12px">✅</div>
          <div v-else style="font-size:60px;margin-bottom:12px">❌</div>
          <h3 :style="{ margin: 0, color: hashResult.hashValid ? '#15824b' : '#c72929' }">{{ hashResult.message }}</h3>
          <div class="detail-grid" style="margin-top:18px;text-align:left">
            <div><span>溯源码</span><code>{{ hashResult.traceCode || actionTarget?.traceCode }}</code></div>
            <div v-if="actionTarget?.contentHash"><span>内容Hash</span><code>{{ actionTarget.contentHash }}</code></div>
          </div>
        </div>
        <footer><button class="secondary" @click="showHash = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

    <!-- 禁用模态框 -->
    <div v-if="showDisable" class="trace-modal-backdrop" @click.self="showDisable = false">
      <section class="trace-modal" style="width:480px">
        <header><div><p>状态变更</p><h2>禁用溯源码</h2></div><button @click="showDisable = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <p class="target-code">{{ actionTarget?.traceCode }}</p>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700">禁用原因 *<textarea v-model="reason" placeholder="请输入禁用原因" style="min-height:80px;width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;resize:vertical" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showDisable = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary danger-fill" @click="confirmDisable"><el-icon><Close /></el-icon> 确认禁用</button>
        </footer>
      </section>
    </div>

    <!-- 作废模态框 -->
    <div v-if="showVoid" class="trace-modal-backdrop" @click.self="showVoid = false">
      <section class="trace-modal" style="width:480px">
        <header><div><p>状态变更</p><h2>作废溯源码</h2></div><button @click="showVoid = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <p class="target-code">{{ actionTarget?.traceCode }}</p>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700">作废原因 *<textarea v-model="reason" placeholder="请输入作废原因" style="min-height:80px;width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;resize:vertical" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showVoid = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary danger-fill" @click="confirmVoid"><el-icon><Close /></el-icon> 确认作废</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';
</style>
