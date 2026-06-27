<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Box, Close, Document, Location, Refresh, Search, SetUp, Shop, Van, View } from '@element-plus/icons-vue'
import { traceApi, productionApi, rawApi, coldChainApi, salesOrderApi } from '../services/api'
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

const codeTypeLabels: Record<number, string> = { 1: '单品码', 2: '箱码', 3: '托盘码' }

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

  const batchNo = row.batchNo
  if (!batchNo) { chainLoading.value = false; return }

  try {
    // 1. 先查生产批次（核心：后续原料查询需要它的 rawBatchNo）
    let prodBatch: any = null
    let rawBatchNo: string | null = null
    try {
      const prodRes = await productionApi.queryProdBatch(batchNo)
      const prodData = (prodRes as any)?.data ?? prodRes
      prodBatch = prodData ? (Array.isArray(prodData) ? prodData[0] : prodData) : null
      if (prodBatch) {
        chainData.value.productionBatches = [prodBatch]
        rawBatchNo = prodBatch.rawBatchNo || null
      }
    } catch (e: any) { /* 生产批次查询失败不影响后续 */ }

    // 2. 并行获取原料、冷链、销售数据
    const [rawRes, coldRes, salesRes] = await Promise.allSettled([
      rawBatchNo ? rawApi.queryByBatchNo(rawBatchNo) : Promise.resolve(null),
      coldChainApi.traceByProdBatch(batchNo),
      salesOrderApi.listOrder({ prodBatchNo: batchNo }),
    ])

    if (rawRes.status === 'fulfilled' && rawRes.value) {
      const d = (rawRes.value as any)?.data ?? rawRes.value
      chainData.value.rawMaterials = d ? (Array.isArray(d) ? d : [d]) : []
    }
    if (coldRes.status === 'fulfilled') {
      const d = (coldRes.value as any)?.data ?? coldRes.value
      chainData.value.coldChainTransports = d ? (Array.isArray(d) ? d : [d]) : []
    }
    if (salesRes.status === 'fulfilled') {
      const d = (salesRes.value as any)?.data ?? salesRes.value
      chainData.value.salesOrders = d ? (Array.isArray(d) ? d : [d]) : []
    }
  } catch (e: any) { /* 部分数据获取失败不影响整体展示 */ }
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
                  <div class="chain-node-row"><span>原料批次</span><code>{{ r.batchNo || '-' }}</code></div>
                  <div class="chain-node-row"><span>产品名称</span><b>{{ r.productName || '-' }}</b></div>
                  <div class="chain-node-row"><span>供应商</span><b>{{ r.supplierName || '-' }}</b></div>
                  <div class="chain-node-row"><span>质检结果</span><b :style="{ color: r.checkResult === 1 ? '#198658' : r.checkResult === 2 ? '#c04550' : '#92a6b9' }">{{ r.checkResult === 1 ? '合格' : r.checkResult === 2 ? '不合格' : '未检测' }}</b></div>
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
                  <div class="chain-node-row"><span>生产批次</span><code>{{ p.batchNo || p.prodBatchNo || '-' }}</code></div>
                  <div class="chain-node-row"><span>产品名称</span><b>{{ p.productName || '-' }}</b></div>
                  <div class="chain-node-row"><span>工艺模板</span><b>{{ p.templateName || '-' }}</b></div>
                  <div class="chain-node-row"><span>生产状态</span><b>{{ p.batchStatus === 1 ? '待生产' : p.batchStatus === 2 ? '生产中' : p.batchStatus === 3 ? '已完成' : '-' }}</b></div>
                  <div class="chain-node-row"><span>生产日期</span><b>{{ p.productionDate || p.createTime || '-' }}</b></div>
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
                  <div class="chain-node-row"><span>运输单号</span><code>{{ c.orderNo || '-' }}</code></div>
                  <div class="chain-node-row"><span>车牌号</span><b>{{ c.plateNo || '-' }}</b></div>
                  <div class="chain-node-row"><span>发运地</span><b>{{ c.departureName || '-' }}</b></div>
                  <div class="chain-node-row"><span>目的地</span><b>{{ c.destinationName || '-' }}</b></div>
                  <div class="chain-node-row"><span>运输状态</span><b>{{ c.transportStatus === 0 ? '待发运' : c.transportStatus === 1 ? '运输中' : c.transportStatus === 2 ? '已到达' : c.transportStatus === 3 ? '已签收' : c.transportStatus === 4 ? '温度预警' : '-' }}</b></div>
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
                  <div class="chain-node-row"><span>销售单号</span><code>{{ s.salesOrderCode || '-' }}</code></div>
                  <div class="chain-node-row"><span>购买方</span><b>{{ s.buyerEnterpriseName || '-' }}</b></div>
                  <div class="chain-node-row"><span>销售数量</span><b>{{ s.orderQuantity ?? '-' }}</b></div>
                  <div class="chain-node-row"><span>销售日期</span><b>{{ s.orderDate || '-' }}</b></div>
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
.chain-node-card { padding: 12px 14px; border: 1px solid #e8eff6; border-radius: 10px; background: #fcfdff; display: grid; gap: 6px; margin-bottom: 6px; }
.chain-node-row { display: flex; gap: 10px; font-size: 13px; align-items: center; }
.chain-node-row span { color: #91a3b4; font-size: 12px; min-width: 60px; flex-shrink: 0; }
.chain-node-row b { color: #34536f; }
.chain-node-row code { color: #2366d9; font-family: Consolas, monospace; font-size: 12px; }

.trace-page .danger-fill { color: #fff; background: #d34f59; box-shadow: 0 5px 10px #d34f5929; }
</style>
