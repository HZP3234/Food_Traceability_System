<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Box, Close, Document, Location, Refresh, Search, SetUp, Shop, Van, View } from '@element-plus/icons-vue'
import { traceApi, productionApi, rawApi, coldChainApi, salesOrderApi, enterpriseApi } from '../services/api'
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
const viewing = ref<any>(null)

// 全链路数据
const chainLoading = ref(false)
const chainData = ref<{
  rawMaterials: any[]
  productionBatches: any[]
  coldChainTransports: any[]
  salesOrders: any[]
}>({ rawMaterials: [], productionBatches: [], coldChainTransports: [], salesOrders: [] })

// 商家/企业信息
const enterpriseInfo = ref<any>(null)
const enterpriseLoading = ref(false)

const codeTypeLabels: Record<number, string> = { 1: '单品码', 2: '箱码', 3: '托盘码' }
const storageLabels: Record<number, string> = { 0: '常温', 1: '冷藏', 2: '冷冻' }
const shiftLabels: Record<number, string> = { 1: '早班', 2: '中班', 3: '晚班' }
const transportMethodLabels: Record<number, string> = { 1: '公路', 2: '铁路', 3: '冷链专线' }
const orderStatusLabels: Record<number, string> = { 1: '待补充', 2: '已补充', 3: '已完成' }
const enterpriseTypeLabels: Record<number, string> = { 1: '供应商', 2: '加工商', 3: '物流商', 4: '零售商' }
const riskLevelLabels: Record<number, string> = { 1: '低风险', 2: '中风险', 3: '高风险' }

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function loadAll() {
  loading.value = true
  try {
    const res = await traceApi.listAll()
    list.value = (res as any).data ?? (Array.isArray(res) ? res : [])
    currentPage.value = 1
  } catch (e: any) { notify('error', '加载失败: ' + e.message); list.value = [] }
  finally { loading.value = false }
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
      const res = await traceApi.searchByEnterpriseName(searchValue.value.trim())
      list.value = (res as any).data ?? (Array.isArray(res) ? res : [])
    }
    if (!list.value.length) notify('error', '未找到匹配的溯源码')
    currentPage.value = 1
  } catch (e: any) { notify('error', '查询失败: ' + e.message); list.value = [] }
  finally { loading.value = false }
}

async function openDetail(row: any) {
  viewing.value = row
  showDetail.value = true
  chainLoading.value = true
  chainData.value = { rawMaterials: [], productionBatches: [], coldChainTransports: [], salesOrders: [] }

  // 获取商家/企业详细信息
  enterpriseInfo.value = null
  enterpriseLoading.value = true
  if (row.enterpriseUuid) {
    try {
      const entRes = await enterpriseApi.getByUuid(row.enterpriseUuid)
      enterpriseInfo.value = (entRes as any).data ?? entRes
      console.log('[全链追溯] 企业信息查询结果:', enterpriseInfo.value)
    } catch (e: any) {
      console.warn('[全链追溯] 企业信息查询失败:', e.message || e)
    }
  }
  enterpriseLoading.value = false

  const batchNo = row.batchNo
  console.log('[全链追溯] 溯源码batchNo:', batchNo)
  if (!batchNo) {
    console.warn('[全链追溯] batchNo为空，无法查询')
    chainLoading.value = false; return
  }

  try {
    // 1. 先查生产批次（核心：后续原料查询需要它的 rawBatchNo）
    let prodBatch: any = null
    let rawBatchNo: string | null = null
    try {
      const prodRes = await productionApi.queryProdBatch(batchNo)
      const prodData = (prodRes as any)?.data ?? prodRes
      prodBatch = prodData ? (Array.isArray(prodData) ? prodData[0] : prodData) : null
      console.log('[全链追溯] 生产批次查询结果:', prodBatch
        ? `找到 batchNo=${prodBatch.batchNo}, rawBatchNo=${prodBatch.rawBatchNo || '无'}`
        : '未找到（数据库可能没有该批次号的生产批次）')
      if (prodBatch) {
        chainData.value.productionBatches = [prodBatch]
        rawBatchNo = prodBatch.rawBatchNo || null
      }
    } catch (e: any) {
      console.error('[全链追溯] 生产批次查询异常:', e.message || e)
    }

    // 2. 并行获取原料、冷链、销售数据
    const [rawRes, coldRes, salesRes] = await Promise.allSettled([
      rawBatchNo ? rawApi.queryByBatchNo(rawBatchNo) : Promise.resolve(null),
      coldChainApi.traceByProdBatch(batchNo),
      salesOrderApi.listOrder({ prodBatchNo: batchNo }),
    ])

    if (rawRes.status === 'fulfilled' && rawRes.value) {
      const d = (rawRes.value as any)?.data ?? rawRes.value
      chainData.value.rawMaterials = d ? (Array.isArray(d) ? d : [d]) : []
      console.log('[全链追溯] 原料查询结果:', chainData.value.rawMaterials.length, '条, rawBatchNo=', rawBatchNo)
    } else if (!rawBatchNo) {
      console.warn('[全链追溯] 原料跳过：生产批次无rawBatchNo，无法关联原料')
    } else {
      console.warn('[全链追溯] 原料查询失败或无数据, rawBatchNo=', rawBatchNo)
    }
    if (coldRes.status === 'fulfilled') {
      const d = (coldRes.value as any)?.data ?? coldRes.value
      chainData.value.coldChainTransports = d ? (Array.isArray(d) ? d : [d]) : []
      console.log('[全链追溯] 冷链查询结果:', chainData.value.coldChainTransports.length, '条')
    } else {
      console.warn('[全链追溯] 冷链查询失败')
    }
    if (salesRes.status === 'fulfilled') {
      const d = (salesRes.value as any)?.data ?? salesRes.value
      chainData.value.salesOrders = d ? (Array.isArray(d) ? d : [d]) : []
      console.log('[全链追溯] 销售查询结果:', chainData.value.salesOrders.length, '条')
    } else {
      console.warn('[全链追溯] 销售查询失败')
    }
  } catch (e: any) { console.error('[全链追溯] 整体异常:', e.message || e) }
  finally { chainLoading.value = false }
}

onMounted(loadAll)
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <section class="trace-panel filter-panel">
      <div class="filter-grid-4">
        <label>查询方式<select v-model="searchMode"><option value="code">按溯源码</option><option value="batch">按批次号</option><option value="enterprise">按企业名称</option></select></label>
        <label>{{ searchMode === 'code' ? '溯源码' : searchMode === 'batch' ? '批次号' : '企业名称' }}
          <input v-model="searchValue" :placeholder="searchMode === 'code' ? '输入溯源码精确查询' : searchMode === 'batch' ? '输入批次号' : '输入企业名称模糊查询'" @keyup.enter="doSearch" />
        </label>
        <div class="filter-actions" style="align-self:end">
          <button class="primary" @click="doSearch"><el-icon><Search /></el-icon> 查询</button>
          <button class="secondary" @click="loadAll"><el-icon><Refresh /></el-icon> 查询全部</button>
        </div>
      </div>
    </section>

    <section class="trace-panel list-panel">
      <header class="panel-header">
        <div><p>溯源备案</p><h2>溯源码列表</h2></div>
      </header>
      <div class="table-wrap">
        <table>
          <thead><tr><th>溯源码</th><th>产品名称</th><th>企业名称</th><th>编码类型</th><th>批次号</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="6" class="empty">查询中...</td></tr>
            <tr v-else-if="!list.length"><td colspan="6" class="empty">暂无溯源码数据</td></tr>
            <tr v-for="row in paginatedList" :key="row.traceCodeId ?? row.traceCode">
              <td><code>{{ row.traceCode }}</code></td>
              <td>{{ row.productName || '-' }}</td>
              <td>{{ row.enterpriseName || '-' }}</td>
              <td><span class="status status-active">{{ codeTypeLabels[row.codeType] || '-' }}</span></td>
              <td><code>{{ row.batchNo || '-' }}</code></td>
              <td class="actions">
                <button @click="openDetail(row)"><el-icon><View /></el-icon> 全链追溯</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <Pagination v-model="currentPage" :total="list.length" :page-size="pageSize" />
    </section>

    <!-- 全链追溯详情模态框 -->
    <div v-if="showDetail" class="trace-modal-backdrop" @click.self="showDetail = false">
      <section class="trace-modal">
        <header><div><p>全链路追溯</p><h2>溯源码全链路详情</h2></div><button @click="showDetail = false"><el-icon><Close /></el-icon></button></header>
        <div class="modal-body" v-if="viewing">
          <!-- 溯源码基本信息 -->
          <div class="chain-section-title"><el-icon><Document /></el-icon> 溯源码信息</div>
          <div class="detail-grid">
            <div><span>溯源码</span><code>{{ viewing.traceCode }}</code></div>
            <div><span>产品名称</span><b>{{ viewing.productName || '-' }}</b></div>
            <div><span>企业名称</span><b>{{ viewing.enterpriseName || '-' }}</b></div>
            <div><span>编码类型</span><b>{{ codeTypeLabels[viewing.codeType] || '-' }}</b></div>
            <div><span>批次号</span><code>{{ viewing.batchNo || '-' }}</code></div>
            <div><span>创建时间</span><b>{{ viewing.createTime || '-' }}</b></div>
            <div><span>过期时间</span><b>{{ viewing.expireTime || '-' }}</b></div>
          </div>

          <!-- 商家/企业信息 -->
          <div class="chain-section-title" style="margin-top:22px"><el-icon><Shop /></el-icon> 商家信息</div>
          <div v-if="enterpriseLoading" style="text-align:center;padding:20px;color:#92a6b9">加载商家信息中...</div>
          <div v-else-if="!enterpriseInfo" class="chain-node-empty">暂无商家信息</div>
          <div v-else class="detail-grid">
            <div><span>企业名称</span><b>{{ enterpriseInfo.enterpriseName || '-' }}</b></div>
            <div><span>企业类型</span><b>{{ enterpriseTypeLabels[enterpriseInfo.enterpriseType] || '-' }}</b></div>
            <div><span>统一社会信用代码</span><code>{{ enterpriseInfo.certNo || '-' }}</code></div>
            <div><span>联系电话</span><b style="color:#2467df">{{ enterpriseInfo.contactPhone || '-' }}</b></div>
            <div><span>联系人</span><b>{{ enterpriseInfo.contactPerson || '-' }}</b></div>
            <div><span>企业状态</span><b :style="{ color: enterpriseInfo.status === 1 ? '#198658' : '#c04550' }">{{ enterpriseInfo.status === 1 ? '✅ 正常' : '❌ 停用' }}</b></div>
            <div class="full"><span>注册地址</span><b>{{ enterpriseInfo.address || '-' }}</b></div>
            <div><span>风险等级</span><b :style="{ color: enterpriseInfo.riskLevel === 3 ? '#c04550' : enterpriseInfo.riskLevel === 2 ? '#a4730a' : '#198658' }">{{ riskLevelLabels[enterpriseInfo.riskLevel] || '-' }}</b></div>
            <div><span>备注</span><b>{{ enterpriseInfo.remark || '-' }}</b></div>
          </div>

          <!-- 全链路追踪 -->
          <div class="chain-section-title" style="margin-top:22px"><el-icon><Location /></el-icon> 全链路追踪</div>
          <div v-if="chainLoading" style="text-align:center;padding:20px;color:#92a6b9">加载全链路数据中...</div>
          <div v-else class="chain-timeline">

            <!-- 节点1: 原料 -->
            <div class="chain-node">
              <div class="chain-node-icon raw"><el-icon><Box /></el-icon></div>
              <div class="chain-node-content">
                <div class="chain-node-label">原料环节</div>
                <div v-if="chainData.rawMaterials.length === 0" class="chain-node-empty">暂无原料数据</div>
                <div v-for="(r, i) in chainData.rawMaterials" :key="i" class="chain-node-card">
                  <div class="chain-node-row chain-node-row-hl"><span>原料批次</span><code>{{ r.batchNo || '-' }}</code></div>
                  <div class="chain-node-card-grid">
                    <div class="chain-node-row"><span>产品名称</span><b>{{ r.productName || '-' }}</b></div>
                    <div class="chain-node-row"><span>产品类别</span><b>{{ r.productCategory || '-' }}</b></div>
                    <div class="chain-node-row"><span>供应商</span><b>{{ r.supplierName || '-' }}</b></div>
                    <div class="chain-node-row"><span>数量</span><b>{{ r.amount != null ? r.amount + (r.unit || '') : '-' }}</b></div>
                    <div class="chain-node-row"><span>仓库</span><b>{{ r.warehouse || '-' }}</b></div>
                    <div class="chain-node-row"><span>储存方式</span><b>{{ storageLabels[r.storageMethod] || '-' }}</b></div>
                    <div class="chain-node-row"><span>保质期</span><b>{{ r.shelfLife || '-' }}</b></div>
                    <div class="chain-node-row"><span>采购日期</span><b>{{ r.purchaseDate || '-' }}</b></div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 节点2: 生产 -->
            <div class="chain-node">
              <div class="chain-node-icon prod"><el-icon><SetUp /></el-icon></div>
              <div class="chain-node-content">
                <div class="chain-node-label">生产环节</div>
                <div v-if="chainData.productionBatches.length === 0" class="chain-node-empty">暂无生产数据</div>
                <div v-for="(p, i) in chainData.productionBatches" :key="i" class="chain-node-card">
                  <div class="chain-node-row chain-node-row-hl"><span>生产批次</span><code>{{ p.batchNo || p.prodBatchNo || '-' }}</code></div>
                  <div class="chain-node-card-grid">
                    <div class="chain-node-row"><span>产品名称</span><b>{{ p.productName || '-' }}</b></div>
                    <div class="chain-node-row"><span>工艺模板</span><b>{{ p.templateName || '-' }}</b></div>
                    <div class="chain-node-row"><span>关联原料</span><code>{{ p.rawBatchNo || '-' }}</code></div>
                    <div class="chain-node-row"><span>产线</span><b>{{ p.productionLine || '-' }}</b></div>
                    <div class="chain-node-row"><span>操作员</span><b>{{ p.operator || '-' }}</b></div>
                    <div class="chain-node-row"><span>计划数量</span><b>{{ p.plannedAmount ?? '-' }}</b></div>
                    <div class="chain-node-row"><span>实际数量</span><b>{{ p.actualAmount ?? '-' }}</b></div>
                    <div class="chain-node-row"><span>生产日期</span><b>{{ p.productionDate || '-' }}</b></div>
                    <div class="chain-node-row"><span>加工日期</span><b>{{ p.processDate || '-' }}</b></div>
                    <div class="chain-node-row"><span>生产状态</span><b :style="{ color: p.batchStatus === 3 ? '#198658' : p.batchStatus === 2 ? '#2467df' : '#a4730a' }">{{ p.batchStatus === 1 ? '待生产' : p.batchStatus === 2 ? '生产中' : p.batchStatus === 3 ? '已完成' : p.batchStatus === 4 ? '已废弃' : '-' }}</b></div>
                  </div>
                  <div class="chain-node-section-title">🔧 加工参数</div>
                  <div class="chain-node-card-grid">
                    <div class="chain-node-row"><span>杀菌温度</span><b>{{ p.actualTemp ? p.actualTemp + '℃' : '-' }}</b></div>
                    <div class="chain-node-row"><span>杀菌时长</span><b>{{ p.actualDuration ? p.actualDuration + 's' : '-' }}</b></div>
                    <div class="chain-node-row"><span>均质压力</span><b>{{ p.actualPressure ? p.actualPressure + 'MPa' : '-' }}</b></div>
                    <div class="chain-node-row"><span>冷却温度</span><b>{{ p.actualCoolTemp ? p.actualCoolTemp + '℃' : '-' }}</b></div>
                    <div class="chain-node-row"><span>灌装温度</span><b>{{ p.actualFillTemp ? p.actualFillTemp + '℃' : '-' }}</b></div>
                    <div class="chain-node-row"><span>pH值</span><b>{{ p.actualPh || '-' }}</b></div>
                    <div class="chain-node-row"><span>粘度</span><b>{{ p.actualViscosity ? p.actualViscosity + 'mPa·s' : '-' }}</b></div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 节点3: 冷链物流 -->
            <div class="chain-node">
              <div class="chain-node-icon cold"><el-icon><Van /></el-icon></div>
              <div class="chain-node-content">
                <div class="chain-node-label">冷链物流环节</div>
                <div v-if="chainData.coldChainTransports.length === 0" class="chain-node-empty">暂无冷链数据</div>
                <div v-for="(c, i) in chainData.coldChainTransports" :key="i" class="chain-node-card">
                  <div class="chain-node-row chain-node-row-hl"><span>运输单号</span><code>{{ c.orderNo || '-' }}</code></div>
                  <div class="chain-node-card-grid">
                    <div class="chain-node-row"><span>产品名称</span><b>{{ c.productName || '-' }}</b></div>
                    <div class="chain-node-row"><span>车牌号</span><b>{{ c.plateNo || '-' }}</b></div>
                    <div class="chain-node-row"><span>驾驶员</span><b>{{ c.driverName || '-' }}</b></div>
                    <div class="chain-node-row"><span>联系电话</span><b>{{ c.driverPhone || '-' }}</b></div>
                    <div class="chain-node-row"><span>运输方式</span><b>{{ transportMethodLabels[c.transportMethod] || '-' }}</b></div>
                    <div class="chain-node-row"><span>发运地</span><b>{{ c.departureName || '-' }}</b></div>
                    <div class="chain-node-row"><span>目的地</span><b>{{ c.destinationName || '-' }}</b></div>
                    <div class="chain-node-row"><span>发运时间</span><b>{{ c.departTime || '-' }}</b></div>
                    <div class="chain-node-row"><span>实际到达</span><b>{{ c.actualArrival || '-' }}</b></div>
                    <div class="chain-node-row full-width"><span>运输状态</span><b :style="{ color: c.transportStatus === 3 ? '#198658' : '#2467df' }">{{ c.transportStatus === 0 ? '📦 待发运' : c.transportStatus === 1 ? '🚚 运输中' : c.transportStatus === 2 ? '📬 已到达' : c.transportStatus === 3 ? '✅ 已签收' : '-' }}</b></div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 节点4: 销售 -->
            <div class="chain-node">
              <div class="chain-node-icon sale"><el-icon><Shop /></el-icon></div>
              <div class="chain-node-content">
                <div class="chain-node-label">销售环节</div>
                <div v-if="chainData.salesOrders.length === 0" class="chain-node-empty">暂无销售数据</div>
                <div v-for="(s, i) in chainData.salesOrders" :key="i" class="chain-node-card">
                  <div class="chain-node-row chain-node-row-hl"><span>销售单号</span><code>{{ s.salesOrderCode || '-' }}</code></div>
                  <div class="chain-node-card-grid">
                    <div class="chain-node-row"><span>产品名称</span><b>{{ s.productName || '-' }}</b></div>
                    <div class="chain-node-row"><span>购买方</span><b>{{ s.buyerEnterpriseName || '-' }}</b></div>
                    <div class="chain-node-row"><span>销售方</span><b>{{ s.sellerEnterpriseName || '-' }}</b></div>
                    <div class="chain-node-row"><span>订单日期</span><b>{{ s.orderDate || '-' }}</b></div>
                    <div class="chain-node-row"><span>销售数量</span><b>{{ s.orderQuantity ?? '-' }}</b></div>
                    <div class="chain-node-row"><span>单价</span><b>{{ s.unitPrice != null ? '¥' + s.unitPrice : '-' }}</b></div>
                    <div class="chain-node-row"><span>总金额</span><b style="color:#c04550;font-size:14px">{{ s.totalAmount != null ? '¥' + s.totalAmount : '-' }}</b></div>
                    <div class="chain-node-row full-width"><span>订单状态</span><b :style="{ color: s.orderStatus === 3 ? '#198658' : s.orderStatus === 2 ? '#2467df' : '#a4730a' }">{{ orderStatusLabels[s.orderStatus] || '-' }}</b></div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
        <footer><button class="secondary" @click="showDetail = false"><el-icon><Close /></el-icon> 关闭</button></footer>
      </section>
    </div>

  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

/* ---- 全链路时间线 ---- */
.chain-section-title {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 0 12px;
  color: #25486b; font-size: 15px; font-weight: 700;
  border-bottom: 1px solid #e8eff6; margin-bottom: 14px;
}
.chain-section-title .el-icon { color: #2467df; }

.chain-timeline { display: grid; gap: 0; }
.chain-node { display: flex; gap: 14px; position: relative; padding-bottom: 16px; }
.chain-node:not(:last-child)::after {
  content: '';
  position: absolute; left: 17px; top: 40px; bottom: 0;
  width: 2px; background: #dce8f4;
}
.chain-node-icon {
  width: 36px; height: 36px; border-radius: 50%;
  display: grid; place-items: center; flex-shrink: 0;
  color: #fff; font-size: 16px; z-index: 1;
}
.chain-node-icon.raw { background: #22b46d; }
.chain-node-icon.prod { background: #2467df; }
.chain-node-icon.cold { background: #4b83d7; }
.chain-node-icon.sale { background: #f59e0b; }

.chain-node-content { flex: 1; min-width: 0; }
.chain-node-label { font-size: 14px; font-weight: 700; color: #25486b; margin-bottom: 8px; }
.chain-node-empty { padding: 10px 12px; border-radius: 8px; background: #f8fbfe; color: #92a6b9; font-size: 13px; }
.chain-node-card { padding: 12px 14px; border: 1px solid #e8eff6; border-radius: 10px; background: #fcfdff; display: grid; gap: 8px; margin-bottom: 6px; }
.chain-node-card-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 7px 16px; }
.chain-node-row { display: flex; gap: 10px; font-size: 13px; align-items: center; }
.chain-node-row span { color: #91a3b4; font-size: 12px; min-width: 60px; flex-shrink: 0; }
.chain-node-row b { color: #34536f; }
.chain-node-row code { color: #2366d9; font-family: Consolas, monospace; font-size: 12px; }
.chain-node-row-hl { padding-bottom: 8px; border-bottom: 1px dashed #e8eff6; margin-bottom: 2px; }
.chain-node-row-hl code { font-size: 13px; font-weight: 700; }
.chain-node-row.full-width { grid-column: 1 / -1; }
.chain-node-section-title { padding: 8px 0 2px; color: #4b83d7; font-size: 12px; font-weight: 700; border-top: 1px solid #edf2f7; margin-top: 4px; }

.trace-page .danger-fill { color: #fff; background: #d34f59; box-shadow: 0 5px 10px #d34f5929; }
</style>
