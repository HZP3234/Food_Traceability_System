<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Check, Close, DocumentChecked, Edit, Warning } from '@element-plus/icons-vue'
import { productionApi, rawApi } from '../services/api'

const tab = ref<'pending' | 'history'>('pending')
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const loading = ref(false)

// 所有待处理 / 已处理的质检汇总
const pendingInspections = ref<any[]>([])
const historyInspections = ref<any[]>([])
const showQcModal = ref(false)
const qcTarget = ref<any>(null)
const qcForm = ref({ inspectionResult: 1, resultDesc: '', inspector: '' })
let currentUser = ''
try {
  const raw = sessionStorage.getItem('fts-admin-user')
  if (raw) { const p = JSON.parse(raw); currentUser = p.realName || p.username || '' }
} catch { currentUser = sessionStorage.getItem('fts-admin-user') || '' }

const bizTypeLabels: Record<number, string> = { 1: '原料', 3: '生产', 4: '冷链', 5: '销售' }
const inspectionTypeLabels: Record<number, string> = { 1: '自检', 2: '抽检', 3: '全检' }

const pendingStats = computed(() => ({
  total: pendingInspections.value.length,
  raw: pendingInspections.value.filter((r: any) => r.bizType === 1).length,
  production: pendingInspections.value.filter((r: any) => r.bizType === 3).length,
  coldChain: pendingInspections.value.filter((r: any) => r.bizType === 4).length,
  sales: pendingInspections.value.filter((r: any) => r.bizType === 5).length,
}))

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function loadAll() {
  loading.value = true
  try {
    // 加载各环节质检记录
    const [prodInsp] = await Promise.all([
      productionApi.listInspection({}).catch(() => []),
    ])
    const allProd = Array.isArray(prodInsp) ? prodInsp : []
    // 简单分开待处理(不合格/待复检)和已完成
    pendingInspections.value = allProd.filter((r: any) => !r.inspectionResult || r.inspectionResult === 2)
    historyInspections.value = allProd.filter((r: any) => r.inspectionResult === 1)
  } catch (e: any) { notify('error', '加载质检数据失败: ' + e.message) }
  finally { loading.value = false }
}

function openQc(row: any) {
  qcTarget.value = row
  qcForm.value = { inspectionResult: 1, resultDesc: row.resultDesc || '', inspector: row.inspector || currentUser || '' }
  showQcModal.value = true
}

async function submitQc() {
  if (!qcTarget.value) return
  const resultLabel = qcForm.value.inspectionResult === 1 ? '合格' : '不合格'
  try {
    if (qcTarget.value.bizType === 1) {
      await rawApi.qualityCheck(qcTarget.value.bizBatchNo, qcForm.value.inspectionResult)
    } else if (qcTarget.value.bizType === 3 || qcTarget.value.bizType === 4 || qcTarget.value.bizType === 5) {
      if (qcTarget.value.inspectionId) {
        await productionApi.updateInspection({
          inspectionId: qcTarget.value.inspectionId,
          inspectionResult: qcForm.value.inspectionResult,
          resultDesc: qcForm.value.resultDesc,
          inspector: qcForm.value.inspector,
          inspectionNo: qcTarget.value.inspectionNo,
          bizType: qcTarget.value.bizType,
          bizBatchNo: qcTarget.value.bizBatchNo,
          inspectionType: qcTarget.value.inspectionType || 1,
          inspectionDate: qcTarget.value.inspectionDate || '',
          remark: '',
        })
      }
    }
    notify('success', `质检确认完成（${resultLabel}）`)
    showQcModal.value = false
    loadAll()
  } catch (e: any) { notify('error', '操作失败: ' + e.message) }
}

onMounted(loadAll)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner manufacturer">
      <span class="trace-role-badge">专职质检</span>
      <span>对<strong>原料、生产、冷链、销售</strong>各环节进行质检确认，确保每批次产品符合质量标准</span>
    </div>

    <section class="trace-stats">
      <article><span><el-icon><DocumentChecked /></el-icon> 待质检总数</span><b>{{ pendingStats.total }}</b><em>条待处理</em></article>
      <article class="amber"><span><el-icon><Warning /></el-icon> 原料待检</span><b>{{ pendingStats.raw }}</b><em>原料环节</em></article>
      <article><span><el-icon><Edit /></el-icon> 生产待检</span><b>{{ pendingStats.production }}</b><em>生产环节</em></article>
      <article class="green"><span><el-icon><Check /></el-icon> 已通过</span><b>{{ historyInspections.length }}</b><em>已完成</em></article>
    </section>

    <div class="trace-tabs">
      <button class="trace-tab-btn" :class="{ active: tab === 'pending' }" @click="tab = 'pending'"><el-icon><Warning /></el-icon> 待质检 ({{ pendingStats.total }})</button>
      <button class="trace-tab-btn" :class="{ active: tab === 'history' }" @click="tab = 'history'"><el-icon><Check /></el-icon> 已确认</button>
    </div>

    <!-- 待质检列表 -->
    <template v-if="tab === 'pending'">
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>质检台账</p><h2>待质检确认记录</h2></div><span style="color:#8195aa;font-size:13px">({{ pendingStats.total }} 条)</span></header>
        <div v-if="!pendingInspections.length" class="trace-empty-state" style="margin:18px">
          <div class="empty-icon"><el-icon><Check /></el-icon></div>
          <p>✓ 所有质检记录已确认完毕</p>
        </div>
        <div v-else class="table-wrap"><table><thead><tr><th>检验编号</th><th>环节</th><th>业务批次</th><th>检验类型</th><th>检验人</th><th>日期</th><th>结果</th><th>描述</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in pendingInspections" :key="row.inspectionId || row.bizBatchNo">
              <td><code>{{ row.inspectionNo || '-' }}</code></td>
              <td><span class="status status-pending">{{ bizTypeLabels[row.bizType] || '未知' }}</span></td>
              <td><code>{{ row.bizBatchNo || '-' }}</code></td>
              <td>{{ inspectionTypeLabels[row.inspectionType] || '自检' }}</td>
              <td>{{ row.inspector || '-' }}</td>
              <td>{{ row.inspectionDate || '-' }}</td>
              <td><span class="status" :class="row.inspectionResult === 1 ? 'status-active' : 'status-void'">{{ row.inspectionResult === 1 ? '合格' : row.inspectionResult === 2 ? '不合格' : '待确认' }}</span></td>
              <td>{{ row.resultDesc || '-' }}</td>
              <td class="actions">
                <button class="primary btn-sm" @click="openQc(row)"><el-icon><Check /></el-icon> 质检确认</button>
              </td>
            </tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- 已确认列表 -->
    <template v-if="tab === 'history'">
      <section class="trace-panel list-panel">
        <header class="panel-header"><div><p>质检台账</p><h2>已确认质检记录</h2></div><span style="color:#8195aa;font-size:13px">({{ historyInspections.length }} 条)</span></header>
        <div v-if="!historyInspections.length" class="trace-empty-state" style="margin:18px">
          <div class="empty-icon"><el-icon><DocumentChecked /></el-icon></div>
          <p>暂无已确认的质检记录</p>
        </div>
        <div v-else class="table-wrap"><table><thead><tr><th>检验编号</th><th>环节</th><th>业务批次</th><th>检验类型</th><th>检验人</th><th>日期</th><th>结果</th><th>描述</th></tr></thead>
          <tbody>
            <tr v-for="row in historyInspections" :key="row.inspectionId">
              <td><code>{{ row.inspectionNo || '-' }}</code></td>
              <td><span class="status status-active">{{ bizTypeLabels[row.bizType] || '未知' }}</span></td>
              <td><code>{{ row.bizBatchNo || '-' }}</code></td>
              <td>{{ inspectionTypeLabels[row.inspectionType] || '自检' }}</td>
              <td>{{ row.inspector || '-' }}</td>
              <td>{{ row.inspectionDate || '-' }}</td>
              <td><span class="status status-active">合格</span></td>
              <td>{{ row.resultDesc || '-' }}</td>
            </tr>
          </tbody></table></div>
      </section>
    </template>

    <!-- QC 确认模态框 -->
    <div v-if="showQcModal" class="trace-modal-backdrop" @click.self="showQcModal = false">
      <section class="trace-modal" style="width:500px">
        <header><div><p>质检确认</p><h2>质检确认 — {{ qcTarget?.bizBatchNo }}</h2></div><button @click="showQcModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <div class="detail-grid" style="margin-bottom:16px">
            <div><span>检验编号</span><b><code>{{ qcTarget?.inspectionNo || '-' }}</code></b></div>
            <div><span>业务环节</span><b>{{ bizTypeLabels[qcTarget?.bizType] || '未知' }}</b></div>
            <div><span>业务批次</span><b><code>{{ qcTarget?.bizBatchNo || '-' }}</code></b></div>
            <div><span>检验类型</span><b>{{ inspectionTypeLabels[qcTarget?.inspectionType] || '自检' }}</b></div>
          </div>
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">检</span>质检结论</div>
            <div class="grid-form">
              <label>检验结果 *
                <select v-model.number="qcForm.inspectionResult">
                  <option :value="1">✓ 合格 — 予以放行</option>
                  <option :value="2">✗ 不合格 — 退回/报废</option>
                </select>
              </label>
              <label>检验人 *<input v-model="qcForm.inspector" placeholder="质检员姓名" /></label>
            </div>
            <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:15px">结果说明<textarea v-model="qcForm.resultDesc" placeholder="请详细描述检验结论和依据。" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:80px" /></label>
          </div>
          <div class="trace-hint" :class="qcForm.inspectionResult === 1 ? 'info' : 'info'" :style="qcForm.inspectionResult === 2 ? 'background:#fff1f1;border-color:#f5c6cb;color:#d34f59' : ''">
            {{ qcForm.inspectionResult === 1 ? '✓ 确认合格后该批次可进入下一环节。' : '⚠ 确认不合格后该批次将被标记，需启动召回或报废流程。' }}
          </div>
        </div>
        <footer>
          <button class="secondary" @click="showQcModal = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" :class="qcForm.inspectionResult === 1 ? '' : 'danger-fill'" @click="submitQc"><el-icon><Check /></el-icon> 确认{{ qcForm.inspectionResult === 1 ? '合格' : '不合格' }}</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

.form-section {
  margin-bottom: 18px;
}

.form-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 2px solid #eaf2ff;
  font-size: 14px;
  font-weight: 800;
  color: #2467df;
}

.section-ico {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: inline-grid;
  place-items: center;
  background: #eaf2ff;
  color: #2467df;
  font-size: 12px;
  font-weight: 900;
  flex-shrink: 0;
}
</style>