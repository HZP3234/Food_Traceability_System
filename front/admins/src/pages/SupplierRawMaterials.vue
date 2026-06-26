<script setup lang="ts">
import { ref, computed, onMounted, inject, type Ref } from 'vue'
import { Close, DocumentAdd, Plus, Select, Upload, Warning } from '@element-plus/icons-vue'
import { rawApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
let currentUser = ''
let enterpriseName = ''
try {
  const raw = sessionStorage.getItem('fts-admin-user')
  if (raw) {
    const parsed = JSON.parse(raw)
    currentUser = parsed.realName || parsed.username || ''
    enterpriseName = parsed.enterpriseName || currentUser
  }
} catch { currentUser = sessionStorage.getItem('fts-admin-user') || '' }

const loading = ref(false)
const pendingList = ref<any[]>([])
const matchedBatches = ref<any[]>([])
const needsDetailBatches = ref<any[]>([])
const allBatches = ref<any[]>([])
const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const showUploadModal = ref(false)
const showMatchModal = ref(false)
const showSupplementModal = ref(false)
const supplementTarget = ref<any>(null)

const stats = computed(() => ({
  needsDetail: needsDetailBatches.value.length,
  pending: pendingList.value.length,
  matched: matchedBatches.value.length,
  total: needsDetailBatches.value.length + pendingList.value.length + matchedBatches.value.length,
}))

const uploadForm = ref({
  productName: '', productCategory: '', amount: '', uploadTime: '',
  origin: '', cert: '', inspectionNo: '',
  supplierQualNo: '',
  uploader: '',
})

const matchForm = ref({ pendingCode: '', targetBatchNo: '' })

const supplementForm = ref({
  origin: '', cert: '0', inspectionNo: '',
  breed: '', farmType: '', feedType: '',
  scaleDesc: '', collectDate: '',
  plateNo: '', transportTemp: '',
  storageMethod: '4', shelfLife: '',
  uploader: '',
  remark: '',
})

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function loadData() {
  loading.value = true
  try {
    const isSupplier = currentRole?.value === 'supplier'
    const supplierName = enterpriseName || currentUser
    const supplierFilter = isSupplier && supplierName ? supplierName : undefined
    // 加载所有批次，按 detailStatus 分为三组
    const data = await rawApi.list(supplierFilter ? { supplierName: supplierFilter } : {})
    const all = Array.isArray(data) ? data : []
    allBatches.value = all
    needsDetailBatches.value = all.filter((r: any) => r.detailStatus === 0)
    matchedBatches.value = all.filter((r: any) => r.detailStatus === 1 || r.detailStatus === 2)
    // 加载待匹配记录
    const pdata = await rawApi.listPending(supplierFilter, 1)
    pendingList.value = Array.isArray(pdata) ? pdata : []
  } catch (e: any) { notify('error', '加载失败: ' + e.message) }
  finally { loading.value = false }
}

function openNewUpload() {
  uploadForm.value = {
    productName: '', productCategory: '', amount: '',
    uploadTime: new Date().toISOString().slice(0, 16).replace('T', ' '),
    origin: '', cert: '0', inspectionNo: '',
    supplierQualNo: '',
    uploader: currentUser || '',
  }
  showUploadModal.value = true
}

async function submitProactiveUpload() {
  try {
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    await rawApi.proactiveUpload(
      {
        origin: uploadForm.value.origin,
        certType: uploadForm.value.cert || '0',
        inspectionNo: uploadForm.value.inspectionNo,
        uploader: currentUser || uploadForm.value.uploader || 'SYSTEM',
        uploadTime: now,
      },
      {
        pendingCode: '',
        supplierName: enterpriseName || currentUser || '',
        productName: uploadForm.value.productName || '待匹配原料',
        productCategory: uploadForm.value.productCategory || '',
        amount: String(parseInt(uploadForm.value.amount) || 0),
        uploadTime: now,
        pendingStatus: 1,
      }
    )
    notify('success', '原料信息已保存至待匹配列表')
    showUploadModal.value = false
    loadData()
  } catch (e: any) { notify('error', '上传失败: ' + e.message) }
}

function openMatch(p: any) {
  matchForm.value = { pendingCode: p.pendingCode, targetBatchNo: '' }
  showMatchModal.value = true
}
async function submitMatch() {
  try {
    await rawApi.matchBatch(matchForm.value.pendingCode, matchForm.value.targetBatchNo)
    notify('success', '匹配成功')
    showMatchModal.value = false
    loadData()
  } catch (e: any) { notify('error', '匹配失败: ' + e.message) }
}

function openSupplement(row: any) {
  supplementTarget.value = row
  supplementForm.value = {
    origin: '', cert: '0', inspectionNo: '',
    breed: '', farmType: '', feedType: '',
    scaleDesc: '', collectDate: new Date().toISOString().slice(0, 10),
    plateNo: '', transportTemp: '',
    storageMethod: '4', shelfLife: '2099-12-31',
    uploader: currentUser || '',
    remark: '',
  }
  showSupplementModal.value = true
}

async function submitSupplement() {
  try {
    await rawApi.supplementDetail(
      {
        origin: supplementForm.value.origin,
        certType: supplementForm.value.cert || '0',
        inspectionNo: supplementForm.value.inspectionNo,
        breed: supplementForm.value.breed,
        farmType: supplementForm.value.farmType,
        feedType: supplementForm.value.feedType,
        scaleDesc: supplementForm.value.scaleDesc,
        collectDate: supplementForm.value.collectDate,
        plateNo: supplementForm.value.plateNo,
        transportTemp: supplementForm.value.transportTemp,
        storageMethod: supplementForm.value.storageMethod,
        shelfLife: supplementForm.value.shelfLife,
        uploader: currentUser || supplementForm.value.uploader || 'SYSTEM',
        remark: supplementForm.value.remark,
      },
      supplementTarget.value.batchNo,
      supplementTarget.value.productName || '未知原料',
      supplementTarget.value.productCategory || '未分类',
      enterpriseName || currentUser || ''
    )
    notify('success', '原料详情补充成功，已关联至批次 ' + supplementTarget.value.batchNo)
    showSupplementModal.value = false
    supplementTarget.value = null
    loadData()
  } catch (e: any) { notify('error', '补充失败: ' + e.message) }
}

// 获取待匹配记录可选的目标批次（所有 detailStatus < 1 的批次或当前所有批次都可匹配）
const matchableBatches = computed(() => allBatches.value)

onMounted(loadData)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner supplier">
      <span class="trace-role-badge">原料供应商</span>
      <span>主动上传原料溯源信息，等待生产商创建批次后自动或手动匹配。运输信息请前往<strong>冷链物流管理</strong>页面单独上传。</span>
    </div>

    <section class="trace-stats">
      <article class="red"><span><el-icon><Warning /></el-icon> 待补充</span><b>{{ stats.needsDetail }}</b><em>加工商已创建批次</em></article>
      <article class="amber"><span><el-icon><Plus /></el-icon> 待匹配</span><b>{{ stats.pending }}</b><em>等待关联批次</em></article>
      <article class="green"><span><el-icon><Select /></el-icon> 已匹配</span><b>{{ stats.matched }}</b><em>已完成溯源</em></article>
      <article><span><el-icon><DocumentAdd /></el-icon> 完成率</span><b>{{ stats.total > 0 ? Math.round(stats.matched / stats.total * 100) : 0 }}%</b><em>已匹配 / 全部</em></article>
    </section>

    <section class="trace-panel list-panel" style="margin-bottom:24px">
      <header class="panel-header">
        <div><p>原料台账</p><h2>原料信息管理</h2></div>
        <button class="primary create" @click="openNewUpload"><el-icon><Upload /></el-icon> 主动上传原料信息</button>
      </header>
    </section>

    <!-- 待补充详情列表 -->
    <section class="trace-panel list-panel" style="margin-bottom:24px">
      <header class="panel-header">
        <div><p>需要补充</p><h2>待补充详情的原料批次</h2></div>
        <span class="col-count red-count">({{ needsDetailBatches.length }} 条)</span>
      </header>
      <div v-if="!needsDetailBatches.length" class="trace-empty-state" style="margin:18px">
        <div class="empty-icon"><el-icon><DocumentAdd /></el-icon></div>
        <p>所有批次已完成详情上传</p>
      </div>
      <div v-else class="trace-row-list" style="padding:0 18px 18px">
        <div v-for="row in needsDetailBatches" :key="row.rawBatchId" class="trace-row-card">
          <span class="trace-mini-badge red" style="background:#ffe8e8;color:#c04550">!</span>
          <div style="flex:1">
            <strong>{{ row.batchNo }}</strong> — {{ row.productName }}
            <small style="display:block;color:#96a8b9;font-size:11px">{{ row.supplierName }} · 创建：{{ row.createTime }} · 状态：待补充详情</small>
          </div>
          <button class="primary btn-sm" @click="openSupplement(row)"><el-icon><Upload /></el-icon> 补充详情</button>
        </div>
      </div>
    </section>

    <!-- 待匹配列表 -->
    <section class="trace-panel list-panel" style="margin-bottom:24px">
      <header class="panel-header">
        <div><p>等待匹配</p><h2>待匹配原料</h2></div>
        <span class="col-count amber-count">({{ pendingList.length }} 条)</span>
      </header>
      <div v-if="!pendingList.length" class="trace-empty-state" style="margin:18px">
        <div class="empty-icon">📤</div>
        <p>暂无待匹配的记录，点击上方按钮主动上传原料信息</p>
      </div>
      <div v-else class="trace-row-list" style="padding:0 18px 18px">
        <div v-for="p in pendingList" :key="p.rawPendingId" class="trace-row-card">
          <span class="trace-mini-badge amber">待</span>
          <div style="flex:1">
            <strong>{{ p.pendingCode }}</strong>
            <small style="display:block;color:#96a8b9;font-size:11px">{{ p.productName || '未知' }} · {{ p.supplierName || '' }} · {{ p.uploadTime }}</small>
          </div>
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
          <div style="flex:1">
            <strong>{{ row.batchNo }}</strong> — {{ row.productName }}
            <small style="display:block;color:#96a8b9;font-size:11px">{{ row.supplierName }} · 状态：{{ ['','待入库','已入库','已启用'][row.batchStatus] || '-' }}</small>
          </div>
          <span class="status status-active">已匹配</span>
        </div>
      </div>
    </section>

    <!-- 上传模态框 — 只有主动上传 -->
    <div v-if="showUploadModal" class="trace-modal-backdrop" @click.self="showUploadModal = false">
      <section class="trace-modal" style="width:680px">
        <header>
          <div><p>源头上传</p><h2>主动上传原料溯源信息</h2></div>
          <button @click="showUploadModal = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body">
          <div class="trace-hint info">💡 上传原料源头信息后，将进入<strong>待匹配列表</strong>。当生产商创建对应原料批次后，系统会自动匹配或由您手动关联。</div>

          <!-- Section: 原料基本信息 -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">基</span>原料基本信息</div>
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
              <label>供应商名称 <span class="required">*</span><input :value="enterpriseName || currentUser" readonly style="background:#f8fafc;color:#8195aa" /></label>
              <label>数量<input v-model="uploadForm.amount" placeholder="如：12t" /></label>
              <label>上传日期<input v-model="uploadForm.uploadTime" type="datetime-local" /></label>
            </div>
          </div>

          <!-- Section: 源头溯源信息 -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">源</span>源头溯源信息</div>
            <div class="grid-form">
              <label>产地 <span class="required">*</span><input v-model="uploadForm.origin" placeholder="如：河北燕北牧场" /></label>
              <label>认证类型
                <select v-model="uploadForm.cert">
                  <option value="0">无认证</option>
                  <option value="1">绿色食品</option>
                  <option value="2">有机认证</option>
                  <option value="3">无公害</option>
                  <option value="4">其他认证</option>
                </select>
              </label>
              <label>检验报告编号<input v-model="uploadForm.inspectionNo" placeholder="JC20260610001" /></label>
              <label>供应商资质编号<input v-model="uploadForm.supplierQualNo" placeholder="QF20260611001" /></label>
            </div>
          </div>

          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:10px">备注<textarea style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" placeholder="请输入原料相关补充说明。" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showUploadModal = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" @click="submitProactiveUpload"><el-icon><Upload /></el-icon> 上传并进入待匹配</button>
        </footer>
      </section>
    </div>

    <!-- 补充详情模态框 — 为已有批次补充溯源信息 -->
    <div v-if="showSupplementModal" class="trace-modal-backdrop" @click.self="showSupplementModal = false">
      <section class="trace-modal" style="width:680px">
        <header>
          <div><p>补充详情</p><h2>为批次 {{ supplementTarget?.batchNo }} 补充溯源信息</h2></div>
          <button @click="showSupplementModal = false"><el-icon><Close /></el-icon></button>
        </header>
        <div class="modal-body">
          <div class="trace-hint info">💡 加工商已创建原料批次 <strong>{{ supplementTarget?.batchNo }}</strong>（{{ supplementTarget?.productName }}）。请补充源头溯源信息，提交后将直接关联到此批次。</div>

          <!-- Section: 批次基本信息（只读） -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">批</span>批次基本信息（只读）</div>
            <div class="grid-form">
              <label>原料名称 <input :value="supplementTarget?.productName" readonly style="background:#f8fafc;color:#8195aa" /></label>
              <label>原料类别 <input :value="supplementTarget?.productCategory" readonly style="background:#f8fafc;color:#8195aa" /></label>
              <label>批次号 <input :value="supplementTarget?.batchNo" readonly style="background:#f8fafc;color:#8195aa" /></label>
              <label>供应商 <input :value="supplementTarget?.supplierName" readonly style="background:#f8fafc;color:#8195aa" /></label>
            </div>
          </div>

          <!-- Section: 源头溯源信息 -->
          <div class="form-section">
            <div class="form-section-title"><span class="section-ico">源</span>源头溯源信息</div>
            <div class="grid-form">
              <label>产地 <span class="required">*</span><input v-model="supplementForm.origin" placeholder="如：河北燕北牧场" /></label>
              <label>认证类型
                <select v-model="supplementForm.cert">
                  <option value="0">无认证</option>
                  <option value="1">绿色食品</option>
                  <option value="2">有机认证</option>
                  <option value="3">无公害</option>
                  <option value="4">其他认证</option>
                </select>
              </label>
              <label>检验报告编号<input v-model="supplementForm.inspectionNo" placeholder="JC20260610001" /></label>
              <label>品种/品系<input v-model="supplementForm.breed" placeholder="如：西门塔尔牛" /></label>
              <label>种养类型<input v-model="supplementForm.farmType" placeholder="如：散养牧场" /></label>
              <label>饲料/肥料类型<input v-model="supplementForm.feedType" placeholder="如：有机饲料" /></label>
              <label>养殖规模/种植面积<input v-model="supplementForm.scaleDesc" placeholder="如：500头" /></label>
              <label>采收/收集日期<input v-model="supplementForm.collectDate" type="date" /></label>
              <label>承运车牌号<input v-model="supplementForm.plateNo" placeholder="冀A12345" /></label>
              <label>运输温度要求<input v-model="supplementForm.transportTemp" placeholder="如：0-4°C" /></label>
              <label>储存方式
                <select v-model="supplementForm.storageMethod">
                  <option value="1">冷藏立式展示柜</option>
                  <option value="2">冷藏风幕柜</option>
                  <option value="3">冷冻卧式冷柜</option>
                  <option value="4">常温货架</option>
                </select>
              </label>
              <label>保质期至<input v-model="supplementForm.shelfLife" type="date" /></label>
            </div>
          </div>

          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-top:10px">备注<textarea v-model="supplementForm.remark" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px;min-height:60px" placeholder="请输入原料相关补充说明。" /></label>
        </div>
        <footer>
          <button class="secondary" @click="showSupplementModal = false"><el-icon><Close /></el-icon> 取消</button>
          <button class="primary" @click="submitSupplement"><el-icon><Upload /></el-icon> 提交详情并关联批次</button>
        </footer>
      </section>
    </div>

    <!-- 匹配模态框 -->
    <div v-if="showMatchModal" class="trace-modal-backdrop" @click.self="showMatchModal = false">
      <section class="trace-modal" style="width:460px">
        <header><div><p>批次匹配</p><h2>手动匹配批次</h2></div><button @click="showMatchModal = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body">
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700;margin-bottom:16px">待匹配编码<input v-model="matchForm.pendingCode" readonly style="background:#f8fafc;width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px" /></label>
          <label style="display:grid;gap:6px;color:#718ba6;font-size:12px;font-weight:700">目标批次号
            <select v-model="matchForm.targetBatchNo" style="width:100%;padding:9px;border:1px solid #d7e4f0;border-radius:7px">
              <option value="">-- 选择批次 --</option>
              <option v-for="r in matchableBatches" :key="r.rawBatchId" :value="r.batchNo">{{ r.batchNo }} - {{ r.productName }}</option>
            </select>
          </label>
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

.col-count {
  font-size: 13px;
  font-weight: 700;
}

.amber-count {
  color: #a4730a;
}

.red-count {
  color: #c04550;
}
</style>
