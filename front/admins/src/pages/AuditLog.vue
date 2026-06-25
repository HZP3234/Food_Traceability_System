<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Close, Document, DocumentAdd, Files, Link, Search, Warning } from '@element-plus/icons-vue'
import { auditApi } from '../services/api'
import Pagination from '../components/Pagination.vue'

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
const showChain = ref(false)
const showWrite = ref(false)
const viewing = ref<any>(null)
const chainResult = ref<any>(null)
const verifying = ref(false)
const archiving = ref(false)

const filters = ref({ operatorName: '', actionType: '', startTime: '', endTime: '' })

const actionTypeLabels: Record<number, string> = { 1: '新增', 2: '修改', 3: '删除', 4: '查询', 5: '导出' }
const resultLabels: Record<number, string> = { 1: '成功', 0: '失败' }

const stats = computed(() => ({
  total: list.value.length,
  success: list.value.filter((r: any) => r.operationResult === 1).length,
  failed: list.value.filter((r: any) => r.operationResult === 0).length,
  abnormal: list.value.filter((r: any) => r.isAbnormal === 1).length,
}))

const writeForm = ref({
  operatorId: '', operatorName: '', actionType: 1, targetId: '', targetDesc: '',
  beforeData: '', afterData: '', operationResult: 1, failureReason: '', isAbnormal: 0, abnormalDesc: '',
})

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function loadList() {
  loading.value = true
  try {
    const p: Record<string, any> = { page: 1, size: 200 }
    if (filters.value.operatorName) p.operatorName = filters.value.operatorName
    if (filters.value.actionType) p.actionType = Number(filters.value.actionType)
    if (filters.value.startTime) p.startTime = filters.value.startTime
    if (filters.value.endTime) p.endTime = filters.value.endTime
    const res = await auditApi.list(p)
    list.value = (res as any).data?.records ?? (Array.isArray(res) ? res : [])
    currentPage.value = 1
  } catch (e: any) { notify('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function resetFilters() {
  filters.value = { operatorName: '', actionType: '', startTime: '', endTime: '' }
  loadList()
}

function openDetail(row: any) { viewing.value = row; showDetail.value = true }

async function doVerifyChain() {
  verifying.value = true
  try {
    const res = await auditApi.verifyChain()
    chainResult.value = (res as any).data ?? res
    showChain.value = true
  } catch (e: any) { notify('error', '校验失败: ' + e.message) }
  finally { verifying.value = false }
}

async function doArchive() {
  archiving.value = true
  try {
    const res = await auditApi.archive()
    const data = (res as any).data ?? res
    notify('success', data?.message ?? '归档完成')
    loadList()
  } catch (e: any) { notify('error', '归档失败: ' + e.message) }
  finally { archiving.value = false }
}

function openWrite() {
  writeForm.value = { operatorId: '', operatorName: '', actionType: 1, targetId: '', targetDesc: '', beforeData: '', afterData: '', operationResult: 1, failureReason: '', isAbnormal: 0, abnormalDesc: '' }
  showWrite.value = true
}

async function submitWrite() {
  try {
    await auditApi.write(writeForm.value)
    notify('success', '审计日志写入成功')
    showWrite.value = false; loadList()
  } catch (e: any) { notify('error', '写入失败: ' + e.message) }
}

function formatDataJson(data: string | null | undefined): string {
  if (!data) return '-'
  try { return JSON.stringify(JSON.parse(data), null, 2) }
  catch { return data }
}

onMounted(loadList)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <section class="trace-stats">
      <article><span><el-icon><Files /></el-icon> 日志总数</span><b>{{ stats.total }}</b><em>条审计记录</em></article>
      <article class="green"><span><el-icon><Search /></el-icon> 操作成功</span><b>{{ stats.success }}</b><em>操作执行成功</em></article>
      <article class="amber"><span><el-icon><Warning /></el-icon> 操作失败</span><b>{{ stats.failed }}</b><em>需要关注</em></article>
      <article class="red"><span><el-icon><Warning /></el-icon> 异常操作</span><b>{{ stats.abnormal }}</b><em>可能存在风险</em></article>
    </section>

    <section class="trace-panel filter-panel">
      <div class="filter-grid-4">
        <label>操作人<input v-model="filters.operatorName" placeholder="操作人姓名" @keyup.enter="loadList" /></label>
        <label>操作类型<select v-model="filters.actionType"><option value="">全部</option><option value="1">新增</option><option value="2">修改</option><option value="3">删除</option><option value="4">查询</option><option value="5">导出</option></select></label>
        <label>开始时间<input v-model="filters.startTime" type="datetime-local" /></label>
        <label>结束时间<input v-model="filters.endTime" type="datetime-local" /></label>
        <div class="filter-actions">
          <button class="secondary" @click="resetFilters"><el-icon><Close /></el-icon> 重置</button>
          <button class="primary" @click="loadList"><el-icon><Search /></el-icon> 查询</button>
        </div>
      </div>
    </section>

    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>审计台账</p><h2>审计日志列表</h2></div>
        <div style="display:flex;gap:8px">
          <button class="secondary" :disabled="verifying" @click="doVerifyChain"><el-icon><Link /></el-icon> Hash链校验</button>
          <button class="secondary" :disabled="archiving" @click="doArchive"><el-icon><Files /></el-icon> 日志归档</button>
          <button class="primary create" @click="openWrite"><el-icon><DocumentAdd /></el-icon> 写入日志</button>
        </div>
      </header>
      <div class="table-wrap">
        <table>
          <thead><tr><th>操作人</th><th>操作类型</th><th>目标描述</th><th>操作时间</th><th>结果</th><th>异常</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="7" class="empty">加载中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="7" class="empty">暂无审计日志</td></tr>
            <tr v-for="row in paginatedList" :key="row.logId">
              <td><strong>{{ row.operatorName || '-' }}</strong></td>
              <td><span class="status status-active">{{ actionTypeLabels[row.actionType] || '-' }}</span></td>
              <td>{{ row.targetDesc || '-' }}</td>
              <td>{{ row.operationTime || '-' }}</td>
              <td><span class="status" :class="row.operationResult === 1 ? 'status-active' : 'status-disabled'">{{ resultLabels[row.operationResult] ?? '-' }}</span></td>
              <td><span class="status" :class="row.isAbnormal === 1 ? 'status-void' : 'status-active'">{{ row.isAbnormal === 1 ? '异常' : '正常' }}</span></td>
              <td class="actions">
                <button @click="openDetail(row)"><el-icon><Document /></el-icon> 详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <Pagination v-model="currentPage" :total="list.length" :page-size="pageSize" />
    </section>

    <!-- 详情模态框 -->
    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false">
      <section class="trace-modal" style="width:800px">
        <header><div><p>日志记录</p><h2>审计日志详情</h2></div><button @click="showDetail = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body" v-if="viewing">
          <div class="detail-grid">
            <div><span>操作人</span><b>{{ viewing.operatorName || '-' }}</b></div>
            <div><span>操作人ID</span><code>{{ viewing.operatorId || '-' }}</code></div>
            <div><span>操作类型</span><b>{{ actionTypeLabels[viewing.actionType] || '-' }}</b></div>
            <div><span>操作时间</span><b>{{ viewing.operationTime || '-' }}</b></div>
            <div><span>目标描述</span><b>{{ viewing.targetDesc || '-' }}</b></div>
            <div><span>目标ID</span><code>{{ viewing.targetId || '-' }}</code></div>
            <div><span>结果</span><b :style="{ color: viewing.operationResult === 1 ? '#198658' : '#c04550' }">{{ resultLabels[viewing.operationResult] ?? '-' }}</b></div>
            <div><span>失败原因</span><b style="color:#c72929">{{ viewing.failureReason || '-' }}</b></div>
            <div><span>是否异常</span><b :style="{ color: viewing.isAbnormal === 1 ? '#c04550' : '#198658' }">{{ viewing.isAbnormal === 1 ? '异常' : '正常' }}</b></div>
            <div><span>异常描述</span><b style="color:#c72929">{{ viewing.abnormalDesc || '-' }}</b></div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-top:18px">
            <div><span style="color:#91a3b4;font-size:12px;font-weight:700;display:block;margin-bottom:6px">📋 操作前数据</span>
              <pre style="background:#f8fafc;padding:10px;border-radius:6px;font-size:11px;max-height:200px;overflow:auto;margin:0;border:1px solid #e8eef5;white-space:pre-wrap">{{ formatDataJson(viewing.beforeData) }}</pre>
            </div>
            <div><span style="color:#91a3b4;font-size:12px;font-weight:700;display:block;margin-bottom:6px">📝 操作后数据</span>
              <pre style="background:#f8fafc;padding:10px;border-radius:6px;font-size:11px;max-height:200px;overflow:auto;margin:0;border:1px solid #e8eef5;white-space:pre-wrap">{{ formatDataJson(viewing.afterData) }}</pre>
            </div>
          </div>
          <div class="detail-grid" style="margin-top:18px">
            <div class="full"><span>日志Hash (SHA-256)</span><code>{{ viewing.logHash || '-' }}</code></div>
            <div><span>日志UUID</span><b>{{ viewing.logUuid || '-' }}</b></div>
            <div><span>创建时间</span><b>{{ viewing.createTime || '-' }}</b></div>
          </div>
        </div>
        <footer><button class="secondary" @click="showDetail = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

    <!-- Hash链校验结果 -->
    <div v-if="showChain" class="trace-modal-backdrop" @click.self="showChain = false">
      <section class="trace-modal" style="width:520px">
        <header><div><p>校验结果</p><h2>Hash链完整性校验</h2></div><button @click="showChain = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body" style="text-align:center;padding:20px" v-if="chainResult">
          <div v-if="chainResult.chainValid" style="font-size:60px;margin-bottom:12px">✅</div>
          <div v-else style="font-size:60px;margin-bottom:12px">❌</div>
          <h3 :style="{ margin: 0, color: chainResult.chainValid ? '#15824b' : '#c72929' }">{{ chainResult.message }}</h3>
          <div v-if="chainResult.chainValid" class="trace-hint success" style="text-align:left;margin-top:16px">✅ 所有日志Hash链完整，通过防篡改检测。系统采用 SHA-256 链式结构，确保日志不可篡改。</div>
          <div v-else class="trace-hint info" style="text-align:left;margin-top:16px;background:#ffeaea;border-color:#fab8b8;color:#c72929">❌ 日志Hash链存在断裂，可能存在篡改行为！请立即核查相关日志记录。</div>
        </div>
        <footer><button class="secondary" @click="showChain = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

    <!-- 写入日志模态框 -->
    <div v-if="showWrite" class="trace-modal-backdrop" @click.self="showWrite = false">
      <section class="trace-modal">
        <header><div><p>手工记录</p><h2>写入审计日志</h2></div><button @click="showWrite = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <div class="grid-form">
            <label>操作人姓名 *<input v-model="writeForm.operatorName" placeholder="操作人姓名" /></label>
            <label>操作人ID<input v-model="writeForm.operatorId" placeholder="操作人ID" /></label>
            <label>操作类型 *<select v-model.number="writeForm.actionType"><option :value="1">新增</option><option :value="2">修改</option><option :value="3">删除</option><option :value="4">查询</option><option :value="5">导出</option></select></label>
            <label>操作结果<select v-model.number="writeForm.operationResult"><option :value="1">成功</option><option :value="0">失败</option></select></label>
            <label>目标ID<input v-model="writeForm.targetId" placeholder="targetId" /></label>
            <label>目标描述<input v-model="writeForm.targetDesc" placeholder="操作目标描述" /></label>
            <label>是否异常<select v-model.number="writeForm.isAbnormal"><option :value="0">正常</option><option :value="1">异常</option></select></label>
            <label>失败原因<input v-model="writeForm.failureReason" placeholder="如操作失败请填写" /></label>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:15px;margin-top:15px">
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700">操作前数据 (JSON)<textarea v-model="writeForm.beforeData" placeholder='{"key": "old_value"}' style="min-height:60px;font-family:monospace;font-size:12px;padding:9px;border:1px solid #d7e4f0;border-radius:7px" /></label>
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700">操作后数据 (JSON)<textarea v-model="writeForm.afterData" placeholder='{"key": "new_value"}' style="min-height:60px;font-family:monospace;font-size:12px;padding:9px;border:1px solid #d7e4f0;border-radius:7px" /></label>
          </div>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:15px">异常描述<textarea v-model="writeForm.abnormalDesc" placeholder="如异常请描述" style="min-height:60px;padding:9px;border:1px solid #d7e4f0;border-radius:7px" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showWrite = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" @click="submitWrite"><el-icon><DocumentAdd /></el-icon> 确认写入</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';
</style>
