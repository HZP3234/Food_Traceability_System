<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { queryComplaintPage } from '@/api/complaint'
import { useAppStore } from '@/store/app'
import type { Complaint } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useAppStore()

const filters = reactive({
  productName: '',
  complaintType: undefined as number | undefined,
  status: undefined as number | undefined
})

const complaintTypes = [
  { value: 1, label: '质量问题' },
  { value: 2, label: '包装问题' },
  { value: 3, label: '保质期异常' },
  { value: 4, label: '假冒伪劣' },
  { value: 5, label: '其他问题' }
]

const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '已处理' },
  { value: 3, label: '已关闭' }
]

const typeMap: Record<number, string> = { 1: '质量问题', 2: '包装问题', 3: '保质期异常', 4: '假冒伪劣', 5: '其他问题' }
const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '待处理', color: '#f90' },
  1: { text: '处理中', color: '#1989fa' },
  2: { text: '已处理', color: '#07c160' },
  3: { text: '已关闭', color: '#969799' }
}

const list = ref<Complaint[]>([])
const loading = ref(false)
const hasMore = ref(false)
const page = reactive({ pageNum: 1, pageSize: 10 })

const showDetail = ref(false)
const detailItem = ref<Complaint | null>(null)

const showTypeSheet = ref(false)
const showStatusSheet = ref(false)

function buildParams() {
  return {
    productName: filters.productName || undefined,
    complaintType: filters.complaintType,
    status: filters.status,
    consumerPhone: store.userInfo?.phone || undefined,
    pageNum: page.pageNum,
    pageSize: page.pageSize
  }
}

function fetchData(append: boolean) {
  if (loading.value) return
  loading.value = true
  queryComplaintPage(buildParams())
    .then((res) => {
      console.log('complaint page response:', res)
      if (res.code === 200 && res.data) {
        console.log('records count:', res.data.records?.length, 'total:', res.data.total)
        const { records, current, pages } = res.data
        list.value = append ? [...list.value, ...(records || [])] : (records || [])
        hasMore.value = current < pages
        if (current < pages) page.pageNum = current + 1
      } else {
        if (!append) list.value = []
        hasMore.value = false
      }
    })
    .catch(() => {
      if (!append) list.value = []
      hasMore.value = false
      showToast('查询失败，请检查网络')
    })
    .finally(() => {
      loading.value = false
    })
}

function onSearch() {
  page.pageNum = 1
  list.value = []
  fetchData(false)
}

function onLoadMore() {
  fetchData(true)
}

function openDetail(item: Complaint) {
  detailItem.value = item
  showDetail.value = true
}

function formatTime(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  if (route.query.complaintNo) {
    const no = route.query.complaintNo as string
    if (no) {
      loading.value = true
      queryComplaintPage({ complaintNo: no, pageNum: 1, pageSize: 1 })
        .then((res) => {
          if (res.code === 200 && res.data) {
            list.value = (res.data.records || []) as Complaint[]
            hasMore.value = false
          }
        })
        .catch(() => {
          showToast('查询失败，请检查网络')
        })
        .finally(() => {
          loading.value = false
        })
    }
  } else {
    fetchData(false)
  }
})
</script>

<template>
  <div class="query-page">
    <van-nav-bar
      title="结果查询"
      left-arrow
      @click-left="router.back"
      fixed
      placeholder
    />

    <div class="subtitle">食品溯源平台</div>

    <!-- 筛选区 -->
    <div class="filter-section">
      <van-field
        v-model="filters.productName"
        label="公司名"
        placeholder="请输入公司名称"
        clearable
      />
      <div class="filter-row">
        <van-field
          v-model="filters.complaintType"
          label="投诉类型"
          placeholder="请选择"
          readonly
          is-link
          @click="showTypeSheet = true"
          class="filter-half"
        >
          <template #input>
            <span v-if="filters.complaintType !== undefined" style="color:#323233">
              {{ typeMap[filters.complaintType] }}
            </span>
          </template>
        </van-field>
        <van-field
          v-model="filters.status"
          label="处理状态"
          placeholder="请选择"
          readonly
          is-link
          @click="showStatusSheet = true"
          class="filter-half"
        >
          <template #input>
            <span v-if="filters.status !== undefined" style="color:#323233">
              {{ statusMap[filters.status]?.text }}
            </span>
          </template>
        </van-field>
      </div>
      <div class="filter-btn-area">
        <van-button type="primary" block round @click="onSearch" :loading="loading">
          查询
        </van-button>
      </div>
    </div>

    <!-- 结果列表 -->
    <div class="list-wrapper">
      <van-loading v-if="loading && list.length === 0" class="loading-center" type="spinner" size="24" />

      <div
        v-for="item in list"
        :key="item.id"
        class="result-card"
      >
        <div class="card-top">
          <div class="card-info">
            <span class="card-company">{{ item.productName }}</span>
            <span class="card-type">{{ typeMap[item.complaintType] || '其他' }}</span>
          </div>
          <span class="card-status" :style="{ color: statusMap[item.status]?.color }">
            {{ statusMap[item.status]?.text || '未知' }}
          </span>
        </div>
        <div class="card-mid">
          <span>投诉编号：{{ item.complaintNo }}</span>
        </div>
        <div class="card-bottom">
          <span class="card-time">提交时间：{{ formatTime(item.createTime) }}</span>
        </div>
        <div class="card-footer">
          <van-button size="small" plain type="primary" round @click="openDetail(item)">
            详情
          </van-button>
        </div>
      </div>

      <van-empty v-if="!loading && list.length === 0" description="暂无投诉记录">
        <van-button round type="primary" to="/complaint-submit">去投诉</van-button>
      </van-empty>

      <div v-if="hasMore && list.length > 0" class="load-more-area">
        <van-button plain round block :loading="loading" @click="onLoadMore">
          加载更多
        </van-button>
      </div>
    </div>

    <!-- 投诉类型选择 -->
    <van-action-sheet
      v-model:show="showTypeSheet"
      title="选择投诉类型"
      :actions="[
        { name: '全部', callback: () => { filters.complaintType = undefined; showTypeSheet = false } },
        ...complaintTypes.map(t => ({ name: t.label, callback: () => { filters.complaintType = t.value; showTypeSheet = false } }))
      ]"
    />

    <!-- 处理状态选择 -->
    <van-action-sheet
      v-model:show="showStatusSheet"
      title="选择处理状态"
      :actions="[
        { name: '全部', callback: () => { filters.status = undefined; showStatusSheet = false } },
        ...statusOptions.map(s => ({ name: s.label, callback: () => { filters.status = s.value; showStatusSheet = false } }))
      ]"
    />

    <!-- 详情弹窗 -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '70%' }"
    >
      <div class="detail-popup" v-if="detailItem">
        <div class="popup-header">
          <span class="popup-title">投诉信息</span>
          <van-icon name="cross" size="18" @click="showDetail = false" />
        </div>
        <div class="popup-body">
          <div class="detail-row">
            <span class="detail-label">投诉编号</span>
            <span class="detail-value">{{ detailItem.complaintNo }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">产品批次</span>
            <span class="detail-value">{{ detailItem.productBatchNo }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">产品/公司</span>
            <span class="detail-value">{{ detailItem.productName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">投诉类型</span>
            <span class="detail-value">{{ typeMap[detailItem.complaintType] || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">投诉内容</span>
            <span class="detail-value">{{ detailItem.complaintTitle }}</span>
          </div>
          <div class="detail-row full-width">
            <span class="detail-label">详细描述</span>
            <span class="detail-value desc">{{ detailItem.complaintContent }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">投诉人</span>
            <span class="detail-value">{{ detailItem.consumerName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">联系电话</span>
            <span class="detail-value">{{ detailItem.consumerPhone }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">处理状态</span>
            <span class="detail-value" :style="{ color: statusMap[detailItem.status]?.color }">
              {{ statusMap[detailItem.status]?.text || '-' }}
            </span>
          </div>
          <div v-if="detailItem.feedbackContent" class="detail-row full-width">
            <span class="detail-label">处理反馈</span>
            <span class="detail-value desc">{{ detailItem.feedbackContent }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">提交时间</span>
            <span class="detail-value">{{ formatTime(detailItem.createTime) }}</span>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.query-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

.subtitle {
  text-align: center;
  font-size: 13px;
  color: #999;
  padding: 8px 0 4px;
}

.filter-section {
  margin: 12px 20px 16px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.filter-row {
  display: flex;
}

.filter-half {
  flex: 1;
}

.filter-btn-area {
  padding: 8px 16px 12px;
}

.list-wrapper {
  margin: 0 16px;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.result-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-company {
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.card-type {
  font-size: 12px;
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 10px;
  color: #666;
}

.card-status {
  font-size: 12px;
  font-weight: 500;
}

.card-mid {
  font-size: 12px;
  color: #969799;
  margin-bottom: 6px;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #969799;
}

.card-footer {
  margin-top: 10px;
  text-align: right;
}

.load-more-area {
  margin-top: 12px;
  padding-bottom: 20px;
}

.detail-popup {
  padding: 0 0 30px;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebedf0;
}

.popup-title {
  font-size: 16px;
  font-weight: 600;
}

.popup-body {
  padding: 16px 20px;
}

.detail-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  color: #969799;
}

.detail-value {
  font-size: 14px;
  color: #323233;
  flex: 1;
}

.detail-value.desc {
  line-height: 1.5;
  white-space: pre-wrap;
}
</style>
