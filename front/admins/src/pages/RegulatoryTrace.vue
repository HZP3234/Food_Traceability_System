<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { traceApi } from '../services/api'

const searchMode = ref<'code' | 'batch' | 'enterprise'>('code')
const searchValue = ref('')
const loading = ref(false)
const list = ref<any[]>([])
const toast = ref<{ type: string; msg: string } | null>(null)
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

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }
function tagClass(status: number) {
  if (status === 0) return 'tag-success'
  if (status === 1) return 'tag-warn'
  return 'tag-danger'
}

async function doSearch() {
  if (!searchValue.value.trim()) { flash('info', '请输入查询条件'); return }
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
    if (!list.value.length) flash('info', '未找到匹配的溯源码')
  } catch (e: any) { flash('error', '查询失败: ' + e.message); list.value = [] }
  finally { loading.value = false }
}

function openDetail(row: any) { viewing.value = row; showDetail.value = true }

async function doVerifyHash(row: any) {
  try {
    const res = await traceApi.verifyHash(row.traceCode)
    hashResult.value = (res as any).data ?? res
    actionTarget.value = row
    showHash.value = true
  } catch (e: any) { flash('error', '校验失败: ' + e.message) }
}

function openDisable(row: any) { actionTarget.value = row; reason.value = ''; showDisable.value = true }
async function confirmDisable() {
  if (!reason.value.trim()) { flash('info', '请输入禁用原因'); return }
  try {
    await traceApi.disable(actionTarget.value.traceCode, reason.value.trim())
    flash('success', '溯源码已禁用'); showDisable.value = false; doSearch()
  } catch (e: any) { flash('error', '禁用失败: ' + e.message) }
}

function openVoid(row: any) { actionTarget.value = row; reason.value = ''; showVoid.value = true }
async function confirmVoid() {
  if (!reason.value.trim()) { flash('info', '请输入作废原因'); return }
  try {
    await traceApi.void(actionTarget.value.traceCode, reason.value.trim())
    flash('success', '溯源码已作废'); showVoid.value = false; doSearch()
  } catch (e: any) { flash('error', '作废失败: ' + e.message) }
}

onMounted(() => { list.value = [] })
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 统计卡片 -->
    <div class="page-stats">
      <div class="stat-card"><div class="stat-meta"><span>当前结果</span><span class="stat-ico">码</span></div><b>{{ stats.total }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>正常</span><span class="stat-ico">✓</span></div><b>{{ stats.active }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>已禁用</span><span class="stat-ico">⊘</span></div><b>{{ stats.disabled }}</b></div>
      <div class="stat-card red"><div class="stat-meta"><span>已作废</span><span class="stat-ico">✕</span></div><b>{{ stats.voided }}</b></div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-bar" style="flex-wrap:wrap">
      <div class="search-field" style="min-width:180px">
        <label>查询方式</label>
        <select v-model="searchMode"><option value="code">按溯源码</option><option value="batch">按批次号</option><option value="enterprise">按企业UUID</option></select>
      </div>
      <div class="search-field" style="flex:1;min-width:240px">
        <label>{{ searchMode === 'code' ? '溯源码' : searchMode === 'batch' ? '批次号' : '企业UUID' }}</label>
        <div style="display:flex;gap:8px">
          <input v-model="searchValue" :placeholder="searchMode === 'code' ? '输入溯源码精确查询' : searchMode === 'batch' ? '输入批次号' : '输入企业UUID'" style="flex:1" @keyup.enter="doSearch" />
          <button class="btn btn-primary" @click="doSearch">🔍 查询</button>
        </div>
      </div>
    </div>

    <!-- 结果表格 -->
    <div class="toolbar"><h3>溯源码列表 <span class="count-note">({{ list.length }} 条)</span></h3></div>

    <div class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>溯源码</th><th>产品名称</th><th>企业名称</th><th>编码类型</th><th>批次号</th><th>状态</th><th class="col-actions">操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="7" style="text-align:center;padding:32px">查询中...</td></tr>
          <tr v-else-if="!list.length"><td colspan="7"><div class="empty-state"><div class="empty-icon">🔍</div><p>请选择查询方式并输入条件后点击查询</p></div></td></tr>
          <tr v-for="row in list" :key="row.traceCodeId ?? row.traceCode">
            <td><code style="color:#2666df;font-size:13px;font-weight:700">{{ row.traceCode }}</code></td>
            <td>{{ row.productName || '-' }}</td>
            <td>{{ row.enterpriseName || '-' }}</td>
            <td><span class="tag tag-info">{{ codeTypeLabels[row.codeType] || '-' }}</span></td>
            <td><code style="font-size:12px">{{ row.batchNo || '-' }}</code></td>
            <td><span class="tag" :class="tagClass(row.traceCodeStatus)">{{ statusLabels[row.traceCodeStatus] ?? '-' }}</span></td>
            <td class="col-actions">
              <button class="btn btn-outline btn-sm" @click="openDetail(row)">📄</button>
              <button class="btn btn-outline btn-sm" style="margin-left:3px" @click="doVerifyHash(row)">🔐 验真</button>
              <button v-if="row.traceCodeStatus === 0" class="btn btn-outline btn-sm" style="margin-left:3px" @click="openDisable(row)">⊘ 禁用</button>
              <button v-if="row.traceCodeStatus !== 2" class="btn btn-danger btn-sm" style="margin-left:3px" @click="openVoid(row)">✕ 作废</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 详情模态框 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal">
        <div class="modal-header"><h3>溯源码详情</h3><button class="modal-close" @click="showDetail = false">✕</button></div>
        <div class="modal-body">
          <div class="detail-panel" v-if="viewing">
            <h4>基本信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>溯源码</span><code style="color:#2666df;font-size:14px;font-weight:700">{{ viewing.traceCode }}</code></div>
              <div class="kv"><span>产品名称</span><strong>{{ viewing.productName || '-' }}</strong></div>
              <div class="kv"><span>企业名称</span><strong>{{ viewing.enterpriseName || '-' }}</strong></div>
              <div class="kv"><span>编码类型</span><span class="tag tag-info">{{ codeTypeLabels[viewing.codeType] || '-' }}</span></div>
              <div class="kv"><span>包装层级</span><strong>{{ viewing.packageLevel ?? '-' }}</strong></div>
              <div class="kv"><span>批次号</span><code style="font-size:12px">{{ viewing.batchNo || '-' }}</code></div>
            </div>
            <h4>安全信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>内容Hash (SHA-256)</span><code style="font-size:11px;word-break:break-all">{{ viewing.contentHash || '-' }}</code></div>
              <div class="kv"><span>存证ID</span><code style="font-size:12px">{{ viewing.proofId || '-' }}</code></div>
              <div class="kv"><span>交易Hash</span><code style="font-size:11px;word-break:break-all">{{ viewing.txHash || '-' }}</code></div>
              <div class="kv"><span>生成次数</span><strong>{{ viewing.generateCount ?? 0 }}</strong></div>
              <div class="kv"><span>过期时间</span><strong>{{ viewing.expireTime || '-' }}</strong></div>
            </div>
            <h4>状态信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>状态</span><span class="tag" :class="tagClass(viewing.traceCodeStatus)">{{ statusLabels[viewing.traceCodeStatus] ?? '-' }}</span></div>
              <div class="kv"><span>禁用原因</span><strong>{{ viewing.disableReason || '-' }}</strong></div>
              <div class="kv"><span>作废原因</span><strong>{{ viewing.voidReason || '-' }}</strong></div>
              <div class="kv"><span>创建时间</span><strong>{{ viewing.createTime || '-' }}</strong></div>
            </div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showDetail = false">关闭</button></div>
      </div>
    </div>

    <!-- Hash 校验结果模态框 -->
    <div v-if="showHash" class="modal-overlay" @click.self="showHash = false">
      <div class="modal" style="width:520px">
        <div class="modal-header"><h3>Hash 完整性校验</h3><button class="modal-close" @click="showHash = false">✕</button></div>
        <div class="modal-body">
          <div v-if="hashResult" style="text-align:center;padding:20px">
            <div v-if="hashResult.hashValid" style="font-size:60px;margin-bottom:12px">✅</div>
            <div v-else style="font-size:60px;margin-bottom:12px">❌</div>
            <h3 :style="{color: hashResult.hashValid ? '#15824b' : '#c72929'}">{{ hashResult.message }}</h3>
            <div class="kv-grid" style="margin-top:18px;text-align:left">
              <div class="kv"><span>溯源码</span><code style="color:#2666df">{{ hashResult.traceCode || actionTarget?.traceCode }}</code></div>
              <div v-if="actionTarget?.contentHash" class="kv"><span>内容Hash</span><code style="font-size:11px;word-break:break-all">{{ actionTarget.contentHash }}</code></div>
            </div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showHash = false">关闭</button></div>
      </div>
    </div>

    <!-- 禁用模态框 -->
    <div v-if="showDisable" class="modal-overlay" @click.self="showDisable = false">
      <div class="modal" style="width:480px">
        <div class="modal-header"><h3>禁用溯源码</h3><button class="modal-close" @click="showDisable = false">✕</button></div>
        <div class="modal-body">
          <p style="color:#6c84a3;margin-bottom:16px">您正在禁用溯源码: <code style="color:#2666df;font-weight:700">{{ actionTarget?.traceCode }}</code></p>
          <div class="form-group"><label>禁用原因 *</label><textarea v-model="reason" placeholder="请输入禁用原因" style="min-height:80px" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showDisable = false">取消</button><button class="btn btn-danger" @click="confirmDisable">确认禁用</button></div>
      </div>
    </div>

    <!-- 作废模态框 -->
    <div v-if="showVoid" class="modal-overlay" @click.self="showVoid = false">
      <div class="modal" style="width:480px">
        <div class="modal-header"><h3>作废溯源码</h3><button class="modal-close" @click="showVoid = false">✕</button></div>
        <div class="modal-body">
          <p style="color:#6c84a3;margin-bottom:16px">您正在作废溯源码: <code style="color:#2666df;font-weight:700">{{ actionTarget?.traceCode }}</code></p>
          <div class="form-group"><label>作废原因 *</label><textarea v-model="reason" placeholder="请输入作废原因" style="min-height:80px" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showVoid = false">取消</button><button class="btn btn-danger" @click="confirmVoid">确认作废</button></div>
      </div>
    </div>
  </div>
</template>
