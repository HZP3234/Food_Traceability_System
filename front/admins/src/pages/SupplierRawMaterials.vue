<script setup lang="ts">
/**
 * 原料信息上传 —— 原料供应商视角
 * 职责：为生产商创建的批次上传源头详情（产地/认证/运输），或主动上传后等待匹配
 * 生产商在"原料批次管理"页面创建批次
 */
import { ref, computed, onMounted, inject, type Ref } from 'vue'
import { rawApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

// 当前登录用户信息
const currentUser = sessionStorage.getItem('fts-admin-user') || ''
const currentRole = inject<Ref<RoleKey>>('currentRole')

const loading = ref(false)
const needUploadBatches = ref<any[]>([])   // 生产商已创建，等待供应商上传
const matchedBatches = ref<any[]>([])      // 已匹配（供应商已上传）
const pendingList = ref<any[]>([])         // 供应商主动上传，等待匹配
const toast = ref<{ type: string; msg: string } | null>(null)
const showUploadModal = ref(false)
const showMatchModal = ref(false)
const viewingBatchNo = ref('')
const isNewUpload = ref(false)  // true=主动上传(无批次号), false=为已有批次上传

const stats = computed(() => ({
  needUpload: needUploadBatches.value.length,
  matched: matchedBatches.value.length,
  pending: pendingList.value.length,
}))

// 上传表单
const uploadForm = ref({
  origin: '', lngLat: '', farmType: '', feed: '', cert: '',
  inspectionNo: '', breed: '', scale: '', collectDate: '',
  transportCar: '', transportTemp: '', storage: '', shelfLife: '',
  uploader: '', productName: '',
})

const matchForm = ref({ pendingCode: '', targetBatchNo: '' })

function flash(type: string, msg: string) { toast.value = { type, msg }; setTimeout(() => (toast.value = null), 2600) }

async function loadData() {
  loading.value = true
  try {
    // 供应商角色只看自己的数据，超管看全部
    const isSupplier = currentRole?.value === 'supplier'
    const supplierFilter = isSupplier && currentUser ? currentUser : undefined

    // 所有原料批次（按供应商过滤）
    const data = await rawApi.list(supplierFilter ? { supplierName: supplierFilter } : {})
    const all = Array.isArray(data) ? data : []
    // 还没有溯源详情的 = 需要供应商上传
    needUploadBatches.value = all.filter((r: any) => !r.detailStatus || r.detailStatus < 1)
    // 已经有溯源详情的 = 已匹配
    matchedBatches.value = all.filter((r: any) => r.detailStatus === 1 || r.detailStatus === 2)
    // 待匹配列表（供应商主动上传的，按供应商过滤）
    const pdata = await rawApi.listPending(supplierFilter, 1)
    pendingList.value = Array.isArray(pdata) ? pdata : []
  } catch (e: any) { flash('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

// 为已有批次上传源头信息
function openUploadForBatch(row: any) {
  isNewUpload.value = false
  viewingBatchNo.value = row.batchNo
  uploadForm.value = { origin: '', lngLat: '', farmType: '', feed: '', cert: '', inspectionNo: '', breed: '', scale: '', collectDate: '', transportCar: '', transportTemp: '', storage: '', shelfLife: '', uploader: row.supplierName || '', productName: row.productName || '' }
  showUploadModal.value = true
}
// 主动上传（无批次号）
function openNewUpload() {
  isNewUpload.value = true
  viewingBatchNo.value = ''
  uploadForm.value = { origin: '', lngLat: '', farmType: '', feed: '', cert: '', inspectionNo: '', breed: '', scale: '', collectDate: '', transportCar: '', transportTemp: '', storage: '', shelfLife: '', uploader: currentUser, productName: '' }
  showUploadModal.value = true
}

// 为已有批次上传 → 自动匹配
async function submitUploadForBatch() {
  try {
    await rawApi.uploadDetail(viewingBatchNo.value, {
      origin: uploadForm.value.origin, lngLat: uploadForm.value.lngLat, farmType: uploadForm.value.farmType,
      feed: uploadForm.value.feed, cert: uploadForm.value.cert, inspectionNo: uploadForm.value.inspectionNo,
      breed: uploadForm.value.breed, scale: uploadForm.value.scale, collectDate: uploadForm.value.collectDate,
      transportCar: uploadForm.value.transportCar, transportTemp: uploadForm.value.transportTemp,
      storage: uploadForm.value.storage, shelfLife: uploadForm.value.shelfLife, uploader: uploadForm.value.uploader || 'SYSTEM',
    })
    flash('success', '源头信息上传成功，已自动匹配到批次 ' + viewingBatchNo.value)
    showUploadModal.value = false; loadData()
  } catch (e: any) { flash('error', '上传失败: ' + e.message) }
}
// 主动上传 → 进入待匹配
async function submitProactiveUpload() {
  try {
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    await rawApi.proactiveUpload(
      { origin: uploadForm.value.origin, lngLat: uploadForm.value.lngLat, farmType: uploadForm.value.farmType, feed: uploadForm.value.feed, cert: uploadForm.value.cert, inspectionNo: uploadForm.value.inspectionNo, breed: uploadForm.value.breed, scale: uploadForm.value.scale, collectDate: uploadForm.value.collectDate, transportCar: uploadForm.value.transportCar, transportTemp: uploadForm.value.transportTemp, storage: uploadForm.value.storage, uploader: currentUser || uploadForm.value.uploader || 'SYSTEM', uploadTime: now },
      { pendingCode: '', supplierName: currentUser || uploadForm.value.uploader || '', productName: uploadForm.value.productName || '待匹配原料', amount: '0', uploadTime: now, pendingStatus: 1 }
    )
    flash('success', '原料信息已保存至待匹配列表，等生产商创建批次后自动匹配')
    showUploadModal.value = false; loadData()
  } catch (e: any) { flash('error', '上传失败: ' + e.message) }
}

// 手动匹配
function openMatch(p: any) { matchForm.value = { pendingCode: p.pendingCode, targetBatchNo: '' }; showMatchModal.value = true }
async function submitMatch() { try { await rawApi.matchBatch(matchForm.value.pendingCode, matchForm.value.targetBatchNo); flash('success', '匹配成功'); showMatchModal.value = false; loadData() } catch (e: any) { flash('error', '匹配失败: ' + e.message) } }

onMounted(loadData)
</script>

<template>
  <div class="page-module">
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>

    <!-- 角色提示 -->
    <div class="role-banner supplier">
      <span class="role-badge">原料供应商</span>
      <span>生产商创建批次 → <strong>您为批次上传源头详情（产地/认证/运输）</strong> → 自动匹配。也可以<strong>先上传信息</strong>，等生产商创建批次后再匹配。</span>
    </div>

    <!-- 统计 -->
    <div class="page-stats">
      <div class="stat-card amber"><div class="stat-meta"><span>待我上传</span><span class="stat-ico">待</span></div><b>{{ stats.needUpload }}</b></div>
      <div class="stat-card green"><div class="stat-meta"><span>已匹配</span><span class="stat-ico">✓</span></div><b>{{ stats.matched }}</b></div>
      <div class="stat-card amber"><div class="stat-meta"><span>待匹配</span><span class="stat-ico">⏳</span></div><b>{{ stats.pending }}</b></div>
      <div class="stat-card"><div class="stat-meta"><span>匹配率</span><span class="stat-ico">%</span></div><b>{{ stats.needUpload + stats.matched > 0 ? Math.round(stats.matched / (stats.needUpload + stats.matched) * 100) : 0 }}%</b></div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <h3>原料信息管理</h3>
      <div class="toolbar-actions">
        <button class="btn btn-outline" @click="openNewUpload">📤 主动上传原料信息</button>
      </div>
    </div>

    <!-- ========== 区块1：待上传的批次（生产商已创建） ========== -->
    <div style="margin-bottom:24px">
      <div class="toolbar"><h3>📋 待上传源头信息的批次 <span class="count-note" style="color:#f59e0b">({{ stats.needUpload }} 条 — 生产商已创建，等待您上传)</span></h3></div>
      <div v-if="!needUploadBatches.length" style="padding:24px;text-align:center;color:#91a4bc;border:1px dashed #dce7f1;border-radius:10px">✓ 所有批次已完成源头信息上传</div>
      <div v-else class="row-list">
        <div v-for="row in needUploadBatches" :key="row.rawBatchId" class="row-card">
          <span class="mini-badge amber-bg">待</span>
          <div style="flex:1"><strong>{{ row.batchNo }}</strong> — {{ row.productName }}<div class="muted">供应商：{{ row.supplierName }} · {{ row.amount }}{{ row.unit }} · 入库：{{ row.purchaseDate }}</div></div>
          <button class="btn btn-primary btn-sm" @click="openUploadForBatch(row)">📤 上传源头信息</button>
        </div>
      </div>
    </div>

    <!-- ========== 区块2：待匹配列表（供应商主动上传的） ========== -->
    <div v-if="pendingList.length" style="margin-bottom:24px">
      <div class="toolbar"><h3>⏳ 待匹配原料 <span class="count-note" style="color:#f59e0b">({{ pendingList.length }} 条 — 您已上传，等待生产商创建批次)</span></h3></div>
      <div class="row-list">
        <div v-for="p in pendingList" :key="p.rawPendingId" class="row-card">
          <span class="mini-badge amber-bg">待</span>
          <div style="flex:1"><strong>{{ p.pendingCode }}</strong><div class="muted">{{ p.productName || '未知' }} · {{ p.supplierName || '' }} · {{ p.uploadTime }}</div></div>
          <button class="btn btn-outline btn-sm" @click="openMatch(p)">手动匹配</button>
        </div>
      </div>
    </div>

    <!-- ========== 区块3：已匹配的批次 ========== -->
    <div>
      <div class="toolbar"><h3>✓ 已匹配的批次 <span class="count-note">({{ stats.matched }} 条 — 源头信息已关联)</span></h3></div>
      <div v-if="!matchedBatches.length" style="padding:24px;text-align:center;color:#91a4bc;border:1px dashed #dce7f1;border-radius:10px">暂无已匹配记录</div>
      <div v-else class="row-list">
        <div v-for="row in matchedBatches" :key="row.rawBatchId" class="row-card">
          <span class="mini-badge green-bg">✓</span>
          <div style="flex:1"><strong>{{ row.batchNo }}</strong> — {{ row.productName }}<div class="muted">{{ row.supplierName }} · 状态：{{ ['','待入库','已入库','已启用'][row.batchStatus] || '-' }}</div></div>
          <span class="tag tag-success">已匹配</span>
        </div>
      </div>
    </div>

    <!-- ========== 模态框：上传源头信息 ========== -->
    <div v-if="showUploadModal" class="modal-overlay" @click.self="showUploadModal = false"><div class="modal" style="width:760px"><div class="modal-header">
      <h3>{{ isNewUpload ? '主动上传原料信息（稍后匹配）' : '为批次 ' + viewingBatchNo + ' 上传源头信息' }}</h3>
      <button class="modal-close" @click="showUploadModal = false">✕</button>
    </div>
    <div class="modal-body">
      <div v-if="isNewUpload" style="padding:10px;background:#fff7e6;border-radius:7px;font-size:12px;color:#a45f00;margin-bottom:12px">⚡ 信息先保存至<strong>待匹配列表</strong>，等生产商创建批次后自动匹配。也可稍后手动匹配。</div>
      <div v-else style="padding:10px;background:#e8f7ef;border-radius:7px;font-size:12px;color:#16a34a;margin-bottom:12px">✓ 为批次 <strong>{{ viewingBatchNo }}</strong> 上传源头信息，提交后<strong>自动匹配</strong>，生产商即可查看。</div>

      <div v-if="isNewUpload" class="form-row" style="margin-bottom:12px">
        <div class="form-group"><label>原料名称</label><input v-model="uploadForm.productName" placeholder="如：有机牧草" /></div>
        <div class="form-group"><label>供应商</label><input v-model="uploadForm.uploader" placeholder="供应商名称" /></div>
      </div>

      <div class="form-section"><div class="form-section-title"><span class="ico">源</span>源头详细信息 — 产地、认证、种养殖</div>
        <div class="form-row"><div class="form-group"><label>产地/牧场</label><input v-model="uploadForm.origin" placeholder="河北燕北牧场" /></div><div class="form-group"><label>经纬度</label><input v-model="uploadForm.lngLat" placeholder="39.9289, 116.3883" /></div><div class="form-group"><label>种养类型</label><input v-model="uploadForm.farmType" placeholder="规模化牧场/合作社/散养" /></div></div>
        <div class="form-row"><div class="form-group"><label>饲料/肥料</label><input v-model="uploadForm.feed" placeholder="有机牧草+精饲料" /></div><div class="form-group"><label>品种/品系</label><input v-model="uploadForm.breed" placeholder="荷斯坦奶牛" /></div><div class="form-group"><label>规模/面积</label><input v-model="uploadForm.scale" placeholder="5000头" /></div></div>
        <div class="form-row"><div class="form-group"><label>采收日期</label><input v-model="uploadForm.collectDate" type="date" /></div><div class="form-group"><label>产地认证</label><input v-model="uploadForm.cert" placeholder="有机认证/绿色食品/无公害" /></div><div class="form-group"><label>检验报告编号</label><input v-model="uploadForm.inspectionNo" placeholder="JC20260610001" /></div></div>
      </div>

      <div class="form-section"><div class="form-section-title"><span class="ico">运</span>运输与储存信息</div>
        <div class="form-row"><div class="form-group"><label>运输车辆</label><input v-model="uploadForm.transportCar" placeholder="冀C·B9180" /></div><div class="form-group"><label>运输温度</label><input v-model="uploadForm.transportTemp" placeholder="3.5℃" /></div><div class="form-group"><label>储存条件</label><input v-model="uploadForm.storage" placeholder="冷藏 0-4℃" /></div></div>
        <div class="form-row"><div class="form-group"><label>保质期至</label><input v-model="uploadForm.shelfLife" type="date" /></div></div>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-outline" @click="showUploadModal = false">取消</button>
      <button v-if="isNewUpload" class="btn btn-primary" @click="submitProactiveUpload">上传并进入待匹配</button>
      <button v-else class="btn btn-primary" @click="submitUploadForBatch">确认上传并自动匹配</button>
    </div>
  </div></div>

    <!-- 匹配模态框 -->
    <div v-if="showMatchModal" class="modal-overlay" @click.self="showMatchModal = false"><div class="modal" style="width:460px"><div class="modal-header"><h3>手动匹配批次</h3><button class="modal-close" @click="showMatchModal = false">✕</button></div>
    <div class="modal-body">
      <div class="form-group"><label>待匹配编码</label><input v-model="matchForm.pendingCode" readonly style="background:#f8fafc" /></div>
      <div class="form-group"><label>目标批次号</label><select v-model="matchForm.targetBatchNo"><option value="">-- 选择批次 --</option><option v-for="r in needUploadBatches" :key="r.rawBatchId" :value="r.batchNo">{{ r.batchNo }} - {{ r.productName }}</option></select></div>
    </div>
    <div class="modal-footer"><button class="btn btn-outline" @click="showMatchModal = false">取消</button><button class="btn btn-primary" @click="submitMatch">确认匹配</button></div>
  </div></div>
  </div>
</template>
