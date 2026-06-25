<script setup lang="ts">
import { ref, computed, onMounted, inject, type Ref } from 'vue'
import { Close, DocumentAdd, Plus, Select, Upload } from '@element-plus/icons-vue'
import { rawApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentUser = sessionStorage.getItem('fts-admin-user') || ''
const currentRole = inject<Ref<RoleKey>>('currentRole')

const loading = ref(false)
const needUploadBatches = ref<any[]>([])
const matchedBatches = ref<any[]>([])
const pendingList = ref<any[]>([])
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showUploadModal = ref(false)
const showMatchModal = ref(false)
const viewingBatchNo = ref('')
const isNewUpload = ref(false)

const stats = computed(() => ({
  needUpload: needUploadBatches.value.length,
  matched: matchedBatches.value.length,
  pending: pendingList.value.length,
}))

const uploadForm = ref({
  productName: '', productCategory: '', amount: '', uploadTime: '',
  origin: '', lngLat: '', farmType: '', feed: '', cert: '',
  inspectionNo: '', breed: '', scale: '', collectDate: '',
  transportCar: '', transportTemp: '', storage: '', shelfLife: '',
  supplierQualNo: '', sourceInspectionNo: '',
  uploader: '',
})

const matchForm = ref({ pendingCode: '', targetBatchNo: '' })

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function loadData() {
  loading.value = true
  try {
    const isSupplier = currentRole?.value === 'supplier'
    const supplierFilter = isSupplier && currentUser ? currentUser : undefined
    const data = await rawApi.list(supplierFilter ? { supplierName: supplierFilter } : {})
    const all = Array.isArray(data) ? data : []
    needUploadBatches.value = all.filter((r: any) => !r.detailStatus || r.detailStatus < 1)
    matchedBatches.value = all.filter((r: any) => r.detailStatus === 1 || r.detailStatus === 2)
    const pdata = await rawApi.listPending(supplierFilter, 1)
    pendingList.value = Array.isArray(pdata) ? pdata : []
  } catch (e: any) { notify('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function openUploadForBatch(row: any) {
  isNewUpload.value = false
  viewingBatchNo.value = row.batchNo
  uploadForm.value = { productName: row.productName || '', productCategory: '', amount: row.amount ? String(row.amount) + (row.unit || '') : '', uploadTime: new Date().toISOString().slice(0, 16).replace('T', ' '), origin: '', lngLat: '', farmType: '', feed: '', cert: '', inspectionNo: '', breed: '', scale: '', collectDate: '', transportCar: '', transportTemp: '', storage: '', shelfLife: '', supplierQualNo: '', sourceInspectionNo: '', uploader: row.supplierName || currentUser || '' }
  showUploadModal.value = true
}
function openNewUpload() {
  isNewUpload.value = true
  viewingBatchNo.value = ''
  uploadForm.value = { productName: '', productCategory: '', amount: '', uploadTime: new Date().toISOString().slice(0, 16).replace('T', ' '), origin: '', lngLat: '', farmType: '', feed: '', cert: '', inspectionNo: '', breed: '', scale: '', collectDate: '', transportCar: '', transportTemp: '', storage: '', shelfLife: '', supplierQualNo: '', sourceInspectionNo: '', uploader: currentUser || '' }
  showUploadModal.value = true
}

async function submitUploadForBatch() {
  try {
    await rawApi.uploadDetail(viewingBatchNo.value, {
      origin: uploadForm.value.origin, lngLat: uploadForm.value.lngLat,
      farmType: uploadForm.value.farmType, feedType: uploadForm.value.feed,
      certType: uploadForm.value.cert, inspectionNo: uploadForm.value.inspectionNo,
      breed: uploadForm.value.breed, scaleDesc: uploadForm.value.scale,
      collectDate: uploadForm.value.collectDate,
      plateNo: uploadForm.value.transportCar, transportTemp: uploadForm.value.transportTemp,
      storageMethod: uploadForm.value.storage, shelfLife: uploadForm.value.shelfLife,
      sourceInspectionNo: uploadForm.value.sourceInspectionNo,
      supplierQualNo: uploadForm.value.supplierQualNo,
      productCategory: uploadForm.value.productCategory,
      uploader: uploadForm.value.uploader || 'SYSTEM',
    })
    notify('success', '源头信息上传成功，已自动匹配到批次 ' + viewingBatchNo.value)
    showUploadModal.value = false; loadData()
  } catch (e: any) { notify('error', '上传失败: ' + e.message) }
}
async function submitProactiveUpload() {
  try {
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    await rawApi.proactiveUpload(
      {
        origin: uploadForm.value.origin, lngLat: uploadForm.value.lngLat,
        farmType: uploadForm.value.farmType, feedType: uploadForm.value.feed,
        certType: uploadForm.value.cert, inspectionNo: uploadForm.value.inspectionNo,
        breed: uploadForm.value.breed, scaleDesc: uploadForm.value.scale,
        collectDate: uploadForm.value.collectDate,
        plateNo: uploadForm.value.transportCar, transportTemp: uploadForm.value.transportTemp,
        storageMethod: uploadForm.value.storage,
        sourceInspectionNo: uploadForm.value.sourceInspectionNo,
        supplierQualNo: uploadForm.value.supplierQualNo,
        productCategory: uploadForm.value.productCategory,
        uploader: currentUser || uploadForm.value.uploader || 'SYSTEM', uploadTime: now
      },
      {
        pendingCode: '', supplierName: currentUser || uploadForm.value.uploader || '',
        productName: uploadForm.value.productName || '待匹配原料',
        productCategory: uploadForm.value.productCategory || '',
        amount: uploadForm.value.amount || '0', uploadTime: now, pendingStatus: 1
      }
    )
    notify('success', '原料信息已保存至待匹配列表')
    showUploadModal.value = false; loadData()
  } catch (e: any) { notify('error', '上传失败: ' + e.message) }
}

function openMatch(p: any) { matchForm.value = { pendingCode: p.pendingCode, targetBatchNo: '' }; showMatchModal.value = true }
async function submitMatch() { try { await rawApi.matchBatch(matchForm.value.pendingCode, matchForm.value.targetBatchNo); notify('success', '匹配成功'); showMatchModal.value = false; loadData() } catch (e: any) { notify('error', '匹配失败: ' + e.message) } }

onMounted(loadData)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner supplier">
      <span class="trace-role-badge">原料供应商</span>
      <span>生产商创建批次 → <strong>您为批次上传源头详情</strong> → 自动匹配。也可以<strong>先上传信息</strong>，等生产商创建批次后再匹配。</span>
    </div>

    <section class="trace-stats">
      <article class="amber"><span><el-icon><Upload /></el-icon> 待我上传</span><b>{{ stats.needUpload }}</b><em>个批次等待</em></article>
      <article class="green"><span><el-icon><Select /></el-icon> 已匹配</span><b>{{ stats.matched }}</b><em>已完成溯源</em></article>
      <article class="amber"><span><el-icon><Plus /></el-icon> 待匹配</span><b>{{ stats.pending }}</b><em>等待关联</em></article>
      <article><span><el-icon><DocumentAdd /></el-icon> 匹配率</span><b>{{ stats.needUpload + stats.matched > 0 ? Math.round(stats.matched / (stats.needUpload + stats.matched) * 100) : 0 }}%</b><em>完成度</em></article>
    </section>

    <section class="trace-panel list-panel" style="margin-bottom:24px">
      <header class="panel-header">
        <div><p>原料台账</p><h2>原料信息管理</h2></div>
        <button class="primary create" @click="openNewUpload"><el-icon><Upload /></el-icon> 主动上传原料信息</button>
      </header>
    </section>

    <!-- 待上传批次 -->
    <section class="trace-panel list-panel" style="margin-bottom:24px">
      <header class="panel-header">
        <div><p>等待上传</p><h2>待上传源头信息的批次</h2></div>
        <span style="color:#a4730a;font-size:13px">({{ stats.needUpload }} 条 — 生产商已创建)</span>
      </header>
      <div v-if="!needUploadBatches.length" class="trace-empty-state">
        <div class="empty-icon"><el-icon><Select /></el-icon></div>
        <p>✓ 所有批次已完成源头信息上传</p>
      </div>
      <div v-else class="trace-row-list" style="padding:0 18px 18px">
        <div v-for="row in needUploadBatches" :key="row.rawBatchId" class="trace-row-card">
          <span class="trace-mini-badge amber">待</span>
          <div style="flex:1"><strong>{{ row.batchNo }}</strong> — {{ row.productName }}<small style="display:block;color:#96a8b9;font-size:11px">供应商：{{ row.supplierName }} · {{ row.amount }}{{ row.unit }} · 入库：{{ row.purchaseDate }}</small></div>
          <button class="primary btn-sm" @click="openUploadForBatch(row)"><el-icon><Upload /></el-icon> 上传源头信息</button>
        </div>
      </div>
    </section>

    <!-- 待匹配列表 -->
    <section v-if="pendingList.length" class="trace-panel list-panel" style="margin-bottom:24px">
      <header class="panel-header">
        <div><p>等待匹配</p><h2>待匹配原料</h2></div>
        <span style="color:#a4730a;font-size:13px">({{ pendingList.length }} 条)</span>
      </header>
      <div class="trace-row-list" style="padding:0 18px 18px">
        <div v-for="p in pendingList" :key="p.rawPendingId" class="trace-row-card">
          <span class="trace-mini-badge amber">待</span>
          <div style="flex:1"><strong>{{ p.pendingCode }}</strong><small style="display:block;color:#96a8b9;font-size:11px">{{ p.productName || '未知' }} · {{ p.supplierName || '' }} · {{ p.uploadTime }}</small></div>
          <button class="secondary btn-sm" @click="openMatch(p)"><el-icon><Select /></el-icon> 手动匹配</button>
        </div>
      </div>
    </section>

    <!-- 已匹配列表 -->
    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>已匹配</p><h2>已关联源头信息的批次</h2></div>
        <span style="color:#8195aa;font-size:13px">({{ stats.matched }} 条)</span>
      </header>
      <div v-if="!matchedBatches.length" class="trace-empty-state" style="margin:18px">
        <div class="empty-icon"><el-icon><DocumentAdd /></el-icon></div>
        <p>暂无已匹配记录</p>
      </div>
      <div v-else class="trace-row-list" style="padding:0 18px 18px">
        <div v-for="row in matchedBatches" :key="row.rawBatchId" class="trace-row-card">
          <span class="trace-mini-badge green">✓</span>
          <div style="flex:1"><strong>{{ row.batchNo }}</strong> — {{ row.productName }}<small style="display:block;color:#96a8b9;font-size:11px">{{ row.supplierName }} · 状态：{{ ['','待入库','已入库','已启用'][row.batchStatus] || '-' }}</small></div>
          <span class="status status-active">已匹配</span>
        </div>
      </div>
    </section>

    <!-- 上传模态框 -->
    <div v-if="showUploadModal" class="trace-modal-backdrop" @click.self="showUploadModal = false">
      <section class="trace-modal" style="width:780px">
        <header>
          <div><p>源头上传</p><h2>{{ isNewUpload ? '主动上传原料信息' : '为批次 ' + viewingBatchNo + ' 上传源头信息' }}</h2></div>
          <button @click="showUploadModal = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body">
          <!-- 提示信息 -->
          <div v-if="isNewUpload" class="trace-hint info">💡 您可以先上传原料详细信息，批次号可<strong>稍后匹配</strong>。上传后信息将进入"待匹配"列表。</div>
          <div v-else class="trace-hint success">✓ 为批次 <strong>{{ viewingBatchNo }}</strong> 上传源头信息，提交后自动匹配到该批次号。</div>

          <!-- Section: 批次匹配信息（已有批次上传） -->
          <div v-if="!isNewUpload" class="form-section">
            <div class="form-section-title"><span class="section-ico">匹</span>批次自动匹配</div>
            <div class="grid-form">
              <label>选择批次号（自动匹配）<input :value="viewingBatchNo" readonly style="background:#f8fafc" /></label>
              <label>供应商名称<input v-model="uploadForm.uploader" readonly style="background:#f8fafc" /></label>
              <label>原料名称<input v-model="uploadForm.productName" placeholder="如：生牛乳" /></label>
              <label>原料类别
                <select v-model="uploadForm.productCategory">
                  <option value="">请选择</option>
                  <option value="乳制品原料">乳制品原料</option>
                  <option value="果蔬原料">果蔬原料</option>
                  <option value="肉禽原料">肉禽原料</option>
                  <option value="粮油原料">粮油原料</option>
                </select>
              </label>
              <label>数量<input v-model="uploadForm.amount" placeholder="如：8.6t" /></label>
              <label>上传时间<input :value="uploadForm.uploadTime" readonly style="background:#f8fafc;color:#718ba6" /></label>
            </div>
          </div>

          <!-- Section: 主动上传原料信息 -->
          <div v-if="isNewUpload" class="form-section">
            <div class="form-section-title"><span class="section-ico">主</span>主动上传原料信息</div>
            <div class="grid-form">
              <label>原料名称 <span class="required">*</span><input v-model="uploadForm.productName" placeholder="如：有机牧草" /></label>
              <label>原料类别
                <select v-model="uploadForm.productCategory">
                  <option value="">请选择</option>
                  <option value="乳制品原料">乳制品原料</option>
                  <option value="果蔬原料">果蔬原料</option>
                  <option value="肉禽原料">肉禽原料</option>
                  <option value="粮油原料">粮油原料</option>
                </select>
              </label>
              <label>供应商名称 <span class="required">*</span><input v-model="uploadForm.uploader" placeholder="供应商名称" /></label>
              <label>数量<input v-model="uploadForm.amount" placeholder="如：12t" /></label>
              <label>批次号（可选）
                <select v-model="viewingBatchNo">
                  <option value="">-- 暂不匹配，稍后处理 --</option>
                  <option v-for="r in needUploadBatches" :key="r.rawBatchId" :value="r.batchNo">{{ r.batchNo }} - {{ r.productName }}</option>
                </select>
              </label>
              <label>上传日期<input :value="uploadForm.uploadTime" readonly style="background:#f8fafc;color:#718ba6" /></label>
            </div>
          </div>

          <!-- Section: 源头详细信息 -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">源</span>源头详细信息</div>
            <div class="grid-form">
              <label>产地/牧场 <span class="required">*</span><input v-model="uploadForm.origin" placeholder="河北燕北牧场" /></label>
              <label>产地经纬度<input v-model="uploadForm.lngLat" placeholder="39.9289, 116.3883" /></label>
              <label>种养类型 <span class="required">*</span>
                <select v-model="uploadForm.farmType">
                  <option value="">请选择种养类型</option>
                  <option value="规模化牧场">规模化牧场</option>
                  <option value="农户散养">农户散养</option>
                  <option value="合作社">合作社</option>
                </select>
              </label>
              <label>饲料/肥料类型<input v-model="uploadForm.feed" placeholder="有机牧草+精饲料" /></label>
              <label>品种/品系<input v-model="uploadForm.breed" placeholder="荷斯坦奶牛" /></label>
              <label>规模/面积<input v-model="uploadForm.scale" placeholder="5000头" /></label>
              <label>采收/收集日期<input v-model="uploadForm.collectDate" type="date" /></label>
              <label>产地认证
                <select v-model="uploadForm.cert">
                  <option value="">请选择认证类型</option>
                  <option value="有机认证">有机认证</option>
                  <option value="绿色食品">绿色食品</option>
                  <option value="无公害">无公害</option>
                </select>
              </label>
              <label>检验报告编号<input v-model="uploadForm.inspectionNo" placeholder="JC20260610001" /></label>
            </div>
          </div>

          <!-- Section: 运输与储存信息 -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">运</span>运输与储存信息</div>
            <div class="grid-form">
              <label>运输车辆<input v-model="uploadForm.transportCar" placeholder="冀C·B9180" /></label>
              <label>运输温度<input v-model="uploadForm.transportTemp" placeholder="3.5℃" /></label>
              <label>储存条件
                <select v-model="uploadForm.storage">
                  <option value="">请选择储存条件</option>
                  <option value="冷藏 0-4℃">冷藏 0-4℃</option>
                  <option value="冷冻 -18℃以下">冷冻 -18℃以下</option>
                  <option value="常温避光">常温避光</option>
                </select>
              </label>
              <label>保质期至<input v-model="uploadForm.shelfLife" type="date" /></label>
              <label>源头检测报告<input v-model="uploadForm.sourceInspectionNo" placeholder="JC20260610001" /></label>
              <label>供应商资质编号<input v-model="uploadForm.supplierQualNo" placeholder="QF20260611001" /></label>
            </div>
          </div>

          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:10px">备注<textarea style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" placeholder="请输入原料相关补充说明。" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showUploadModal = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="secondary" @click="showUploadModal = false"><el-icon><Upload /></el-icon> 保存草稿</button>
          <button v-if="isNewUpload" class="primary" @click="submitProactiveUpload"><el-icon><Upload /></el-icon> 上传并进入待匹配</button>
          <button v-else class="primary" @click="submitUploadForBatch"><el-icon><Upload /></el-icon> 确认上传并自动匹配</button>
        </footer>
      </section>
    </div>

    <!-- 匹配模态框 -->
    <div v-if="showMatchModal" class="trace-modal-backdrop" @click.self="showMatchModal = false">
      <section class="trace-modal" style="width:460px">
        <header><div><p>批次匹配</p><h2>手动匹配批次</h2></div><button @click="showMatchModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-bottom:16px">待匹配编码<input v-model="matchForm.pendingCode" readonly style="background:#f8fafc;width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px" /></label>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700">目标批次号<select v-model="matchForm.targetBatchNo" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px"><option value="">-- 选择批次 --</option><option v-for="r in needUploadBatches" :key="r.rawBatchId" :value="r.batchNo">{{ r.batchNo }} - {{ r.productName }}</option></select></label>
        </div>
        <footer><button class="secondary" @click="showMatchModal = false"><el-icon><Close /></el-icon> 取消</button><button class="primary" @click="submitMatch"><el-icon><Select /></el-icon> 确认匹配</button></footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

.required { color: #e74c3c; font-weight: bold; margin-left: 2px; }

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
