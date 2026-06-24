<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { queryTraceability } from '@/api/traceability'
import headImg from '@/assets/user_head_image.png'

const router = useRouter()
const store = useAppStore()
const searchValue = ref('')
const loading = ref(false)

function requireLogin(action: () => void) {
  if (!store.isLoggedIn) {
    showToast('请先登录后再使用此功能')
    router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  action()
}

function onSearch(val: string) {
  if (!val.trim()) return
  goTrace(val.trim())
}

function goTrace(batchNo: string) {
  loading.value = true
  queryTraceability({ productBatchNo: batchNo })
    .then((res) => {
      if (res.code === 200 && res.data) {
        store.setBatchNo(batchNo)
        store.addHistory(batchNo)
        router.push({ name: 'TraceResult', query: { batchNo } })
      } else {
        showToast(res.message || '未查到该批次信息')
      }
    })
    .catch((err) => {
      showToast(err.message || '查询失败')
    })
    .finally(() => {
      loading.value = false
    })
}

function onScan() {
  requireLogin(() => {
    router.push({ name: 'Scanner' })
  })
}

function onHistoryClick(item: string) {
  searchValue.value = item
  goTrace(item)
}

function onClearHistory() {
  store.clearHistory()
}

function goUserCenter() {
  requireLogin(() => {
    router.push('/user-center')
  })
}

function goComplaintSubmit() {
  requireLogin(() => {
    router.push('/complaint-submit')
  })
}

function goComplaintQuery() {
  requireLogin(() => {
    router.push('/complaint-query')
  })
}
</script>

<template>
  <div class="home-page">
    <!-- 头部：含右上角头像入口 -->
    <div class="header">
      <div class="header-top">
        <span class="header-city">食品安全追溯</span>
        <div class="avatar-entry" @click="goUserCenter">
          <img :src="headImg" alt="头像" />
        </div>
      </div>
      <div class="header-main">
        <div class="logo-circle">
          <van-icon name="passed" size="32" color="#fff" />
        </div>
        <h1>食品安全追溯平台</h1>
        <p>扫码或输入溯源码，了解食品全程信息</p>
      </div>
    </div>

    <!-- 搜索区 -->
    <div class="search-area">
      <van-search
        v-model="searchValue"
        placeholder="请输入溯源码/批次号"
        shape="round"
        @search="onSearch"
      />
    </div>

    <!-- 扫码区 -->
    <div class="scan-area">
      <div class="scan-btn" @click="onScan">
        <div class="scan-icon">
          <van-icon name="scan" size="32" />
        </div>
        <span>扫一扫溯源码</span>
      </div>
    </div>

    <!-- 搜索历史 -->
    <div v-if="store.searchHistory.length" class="history-section">
      <div class="section-title">
        <span>搜索历史</span>
        <span class="clear-btn" @click="onClearHistory">清除</span>
      </div>
      <div class="history-list">
        <span
          v-for="item in store.searchHistory"
          :key="item"
          class="history-item"
          @click="onHistoryClick(item)"
        >
          {{ item }}
        </span>
      </div>
    </div>

    <!-- 功能入口 -->
    <div class="menu-section">
      <div class="menu-title">服务专区</div>
      <div class="menu-grid">
        <div class="menu-card complaint-card" @click="goComplaintSubmit">
          <div class="menu-icon-wrap icon-danger">
            <van-icon name="warning-o" size="24" color="#f56c6c" />
          </div>
          <span class="menu-label">投诉反馈</span>
          <span class="menu-sub">反馈食品安全问题</span>
        </div>
        <div class="menu-card query-card" @click="goComplaintQuery">
          <div class="menu-icon-wrap icon-primary">
            <van-icon name="search" size="24" color="#1989fa" />
          </div>
          <span class="menu-label">结果查询</span>
          <span class="menu-sub">查看投诉处理进度</span>
        </div>
      </div>
    </div>

    <div class="footer-tip">
      食品安全 全民监督
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

/* 头部 */
.header {
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  padding: 0 20px 36px;
  color: #fff;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
}

.header-city {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
}

.avatar-entry {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.7);
  overflow: hidden;
  cursor: pointer;
}

.avatar-entry img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-main {
  text-align: center;
  padding-top: 16px;
}

.logo-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.header-main h1 {
  font-size: 24px;
  margin: 0 0 8px;
  font-weight: 600;
}

.header-main p {
  font-size: 13px;
  opacity: 0.8;
  margin: 0;
}

/* 搜索 */
.search-area {
  margin: 12px 20px 0;
}

.search-area :deep(.van-search__content) {
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* 扫码 */
.scan-area {
  display: flex;
  justify-content: center;
  margin: 32px 0;
}

.scan-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 36px;
  background: #fff;
  border-radius: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.scan-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(7, 193, 96, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #07c160;
}

.scan-btn span {
  font-size: 16px;
  color: #323233;
  font-weight: 500;
}

/* 搜索历史 */
.history-section {
  margin: 0 20px 28px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
}

.clear-btn {
  font-size: 12px;
  color: #999;
  font-weight: 400;
  cursor: pointer;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

/* 功能菜单 */
.menu-section {
  margin: 36px 20px 0;
}

.menu-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 14px;
}

.menu-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.menu-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 16px 24px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.2s;
}

.menu-card:active {
  transform: scale(0.96);
}

.menu-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.icon-danger {
  background: rgba(245, 108, 108, 0.08);
}

.icon-primary {
  background: rgba(25, 137, 250, 0.08);
}

.menu-label {
  font-size: 15px;
  color: #323233;
  font-weight: 600;
}

.menu-sub {
  font-size: 11px;
  color: #969799;
}

.footer-tip {
  text-align: center;
  font-size: 12px;
  color: #c8c9cc;
  margin-top: 40px;
}
</style>
