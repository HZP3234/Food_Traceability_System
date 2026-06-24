<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { auditApi } from '../services/api'

const loading = ref(false)
const list = ref<any[]>([])
const toast = ref<{ type: string; msg: string } | null>(null)
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

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }

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
  } catch (e: any) { flash('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function openDetail(row: any) { viewing.value = row; showDetail.value = true }

async function doVerifyChain() {
  verifying.value = true
  try {
    const res = await auditApi.verifyChain()
    chainResult.value = (res as any).data ?? res
    showChain.value = true
  } catch (e: any) { flash('error', '校验失败: ' + e.message) }
  finally { verifying.value = false }
}

async function doArchive() {
  archiving.value = true
  try {
    const res = await auditApi.archive()
    const data = (res as any).data ?? res
    flash('success', data?.message ?? '归档完成')
    loadList()
  } catch (e: any) { flash('error', '归档失败: ' + e.message) }
  finally { archiving.value = false }
}

function openWrite() {
  writeForm.value = { operatorId: '', operatorName: '', actionType: 1, targetId: '', targetDesc: '', beforeData: '', afterData: '', operationResult: 1, failureReason: '', isAbnormal: 0, abnormalDesc: '' }
  showWrite.value = true
}

async function submitWrite() {
  try {
    await auditApi.write(writeForm.value)
    flash('success', '审计日志写入成功')
    showWrite.value = false; loadList()
  } catch (e: any) { flash('error', '写入失败: ' + e.message) }
}

function formatDataJson(data: string | null | undefined): string {
  if (!data) return '-'
  try { return JSON.stringify(JSON.parse(data), null, 2) }
  catch { return data }
}

onMounted(loadList)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 统计卡片 -->
    <div class="page-stats">
      <div class="stat-card"><div class="stat-meta"><span>日志总数</span><span class="stat-ico">志</span></div><b>{{ stats.total }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>操作成功</span><span class="stat-ico">✓</span></div><b>{{ stats.success }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>操作失败</span><span class="stat-ico">!</span></div><b>{{ stats.failed }}</b></div>
      <div class="stat-card red"><div class="stat-meta"><span>异常操作</span><span class="stat-ico">⚠</span></div><b>{{ stats.abnormal }}</b></div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-field"><label>操作人</label><input v-model="filters.operatorName" placeholder="操作人姓名" @keyup.enter="loadList" /></div>
      <div class="search-field"><label>操作类型</label><select v-model="filters.actionType"><option value="">全部</option><option value="1">新增</option><option value="2">修改</option><option value="3">删除</option><option value="4">查询</option><option value="5">导出</option></select></div>
      <div class="search-field"><label>开始时间</label><input v-model="filters.startTime" type="datetime-local" /></div>
      <div class="search-field"><label>结束时间</label><input v-model="filters.endTime" type="datetime-local" /></div>
      <div class="search-field" style="align-self:end"><button class="btn btn-primary" @click="loadList">🔍 查询</button></div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <h3>审计日志列表 <span class="count-note">({{ list.length }} 条)</span></h3>
      <div class="toolbar-actions">
        <button class="btn btn-outline" :disabled="verifying" @click="doVerifyChain">🔗 Hash链校验</button>
        <button class="btn btn-outline" :disabled="archiving" @click="doArchive">📦 日志归档</button>
        <button class="btn btn-primary" @click="openWrite">＋ 写入日志</button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>操作人</th><th>操作类型</th><th>目标描述</th><th>操作时间</th><th>结果</th><th>异常</th><th class="col-actions">操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="7" style="text-align:center;padding:32px">加载中...</td></tr>
          <tr v-else-if="!list.length"><td colspan="7"><div class="empty-state"><div class="empty-icon">📋</div><p>暂无审计日志</p></div></td></tr>
          <tr v-for="row in list" :key="row.logId">
            <td><strong>{{ row.operatorName || '-' }}</strong></td>
            <td><span class="tag tag-info">{{ actionTypeLabels[row.actionType] || '-' }}</span></td>
            <td>{{ row.targetDesc || '-' }}</td>
            <td style="font-size:13px">{{ row.operationTime || '-' }}</td>
            <td><span class="tag" :class="row.operationResult === 1 ? 'tag-success' : 'tag-danger'">{{ resultLabels[row.operationResult] ?? '-' }}</span></td>
            <td><span class="tag" :class="row.isAbnormal === 1 ? 'tag-danger' : 'tag-success'">{{ row.isAbnormal === 1 ? '异常' : '正常' }}</span></td>
            <td class="col-actions">
              <button class="btn btn-outline btn-sm" @click="openDetail(row)">📄 详情</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 日志详情模态框（含前后数据对比） -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal" style="width:800px">
        <div class="modal-header"><h3>审计日志详情</h3><button class="modal-close" @click="showDetail = false">✕</button></div>
        <div class="modal-body">
          <div class="detail-panel" v-if="viewing">
            <h4>操作信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>操作人</span><strong>{{ viewing.operatorName || '-' }}</strong></div>
              <div class="kv"><span>操作人ID</span><code style="font-size:12px">{{ viewing.operatorId || '-' }}</code></div>
              <div class="kv"><span>操作类型</span><span class="tag tag-info">{{ actionTypeLabels[viewing.actionType] || '-' }}</span></div>
              <div class="kv"><span>操作时间</span><strong>{{ viewing.operationTime || '-' }}</strong></div>
              <div class="kv"><span>目标描述</span><strong>{{ viewing.targetDesc || '-' }}</strong></div>
              <div class="kv"><span>目标ID</span><code style="font-size:12px">{{ viewing.targetId || '-' }}</code></div>
            </div>
            <h4>操作结果</h4>
            <div class="kv-grid">
              <div class="kv"><span>结果</span><span class="tag" :class="viewing.operationResult === 1 ? 'tag-success' : 'tag-danger'">{{ resultLabels[viewing.operationResult] ?? '-' }}</span></div>
              <div class="kv"><span>失败原因</span><strong style="color:#c72929">{{ viewing.failureReason || '-' }}</strong></div>
              <div class="kv"><span>是否异常</span><span class="tag" :class="viewing.isAbnormal === 1 ? 'tag-danger' : 'tag-success'">{{ viewing.isAbnormal === 1 ? '异常' : '正常' }}</span></div>
              <div class="kv"><span>异常描述</span><strong style="color:#c72929">{{ viewing.abnormalDesc || '-' }}</strong></div>
            </div>
            <h4>数据对比</h4>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px">
              <div>
                <p style="font-size:12px;font-weight:700;color:#6c84a3;margin:0 0 6px">📋 操作前数据</p>
                <pre style="background:#f8fafc;padding:10px;border-radius:6px;font-size:11px;max-height:200px;overflow:auto;margin:0;border:1px solid #e8eef5;white-space:pre-wrap">{{ formatDataJson(viewing.beforeData) }}</pre>
              </div>
              <div>
                <p style="font-size:12px;font-weight:700;color:#6c84a3;margin:0 0 6px">📝 操作后数据</p>
                <pre style="background:#f8fafc;padding:10px;border-radius:6px;font-size:11px;max-height:200px;overflow:auto;margin:0;border:1px solid #e8eef5;white-space:pre-wrap">{{ formatDataJson(viewing.afterData) }}</pre>
              </div>
            </div>
            <h4>安全信息</h4>
            <div class="kv-grid">
              <div class="kv"><span>日志Hash (SHA-256)</span><code style="font-size:11px;word-break:break-all">{{ viewing.logHash || '-' }}</code></div>
              <div class="kv"><span>日志UUID</span><code style="font-size:12px">{{ viewing.logUuid || '-' }}</code></div>
              <div class="kv"><span>创建时间</span><strong>{{ viewing.createTime || '-' }}</strong></div>
            </div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showDetail = false">关闭</button></div>
      </div>
    </div>

    <!-- Hash链校验结果模态框 -->
    <div v-if="showChain" class="modal-overlay" @click.self="showChain = false">
      <div class="modal" style="width:520px">
        <div class="modal-header"><h3>Hash链完整性校验</h3><button class="modal-close" @click="showChain = false">✕</button></div>
        <div class="modal-body">
          <div v-if="chainResult" style="text-align:center;padding:20px">
            <div v-if="chainResult.chainValid" style="font-size:60px;margin-bottom:12px">✅</div>
            <div v-else style="font-size:60px;margin-bottom:12px">❌</div>
            <h3 :style="{color: chainResult.chainValid ? '#15824b' : '#c72929'}">{{ chainResult.message }}</h3>
            <div v-if="chainResult.chainValid" class="dynamic-hint success" style="margin-top:16px;text-align:left">
              ✅ 所有日志Hash链完整，通过防篡改检测。系统采用 SHA-256 链式结构，每条日志的Hash包含前一条日志的Hash值，确保日志不可篡改。
            </div>
            <div v-else class="dynamic-hint info" style="margin-top:16px;text-align:left;background:#ffeaea;border-color:#fab8b8;color:#c72929">
              ❌ 日志Hash链存在断裂，可能存在篡改行为！请立即核查相关日志记录。
            </div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showChain = false">关闭</button></div>
      </div>
    </div>

    <!-- 手工写入日志模态框 -->
    <div v-if="showWrite" class="modal-overlay" @click.self="showWrite = false">
      <div class="modal">
        <div class="modal-header"><h3>手工写入审计日志</h3><button class="modal-close" @click="showWrite = false">✕</button></div>
        <div class="modal-body">
          <div class="form-section"><div class="form-section-title"><span class="ico">操</span>操作信息</div>
            <div class="form-row">
              <div class="form-group"><label>操作人姓名 *</label><input v-model="writeForm.operatorName" placeholder="操作人姓名" /></div>
              <div class="form-group"><label>操作人ID</label><input v-model="writeForm.operatorId" placeholder="操作人ID" /></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>操作类型 *</label><select v-model.number="writeForm.actionType"><option :value="1">新增</option><option :value="2">修改</option><option :value="3">删除</option><option :value="4">查询</option><option :value="5">导出</option></select></div>
              <div class="form-group"><label>操作结果</label><select v-model.number="writeForm.operationResult"><option :value="1">成功</option><option :value="0">失败</option></select></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>目标ID</label><input v-model="writeForm.targetId" placeholder="targetId" /></div>
              <div class="form-group"><label>目标描述</label><input v-model="writeForm.targetDesc" placeholder="操作目标描述" /></div>
            </div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">数</span>数据对比</div>
            <div class="form-row">
              <div class="form-group"><label>操作前数据 (JSON)</label><textarea v-model="writeForm.beforeData" placeholder='{"key": "old_value"}' style="min-height:60px;font-family:monospace;font-size:12px" /></div>
              <div class="form-group"><label>操作后数据 (JSON)</label><textarea v-model="writeForm.afterData" placeholder='{"key": "new_value"}' style="min-height:60px;font-family:monospace;font-size:12px" /></div>
            </div>
          </div>
          <div class="form-section"><div class="form-section-title"><span class="ico">异</span>异常信息</div>
            <div class="form-row">
              <div class="form-group"><label>失败原因</label><input v-model="writeForm.failureReason" placeholder="如操作失败请填写" /></div>
              <div class="form-group"><label>是否异常</label><select v-model.number="writeForm.isAbnormal"><option :value="0">正常</option><option :value="1">异常</option></select></div>
            </div>
            <div class="form-group"><label>异常描述</label><textarea v-model="writeForm.abnormalDesc" placeholder="如异常请描述" /></div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" @click="showWrite = false">取消</button><button class="btn btn-primary" @click="submitWrite">确认写入</button></div>
      </div>
    </div>
  </div>
</template>
