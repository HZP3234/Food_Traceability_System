<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { queryByTraceCode } from '@/api/traceability'
import headImg from '@/assets/user_head_image.png'

const router = useRouter()
const store = useAppStore()
const showCodeDialog = ref(false)
const traceCodeInput = ref('')
const codeLoading = ref(false)

function requireLogin(action: () => void) {
  if (!store.isLoggedIn) {
    showToast('请先登录后再使用此功能')
    router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  action()
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

function openCodeDialog() {
  traceCodeInput.value = ''
  showCodeDialog.value = true
}

function onCodeSearch() {
  const code = traceCodeInput.value.trim()
  if (!code) {
    showToast('请输入溯源码')
    return
  }
  codeLoading.value = true
  queryByTraceCode(code, store.userInfo?.consumerUuid)
    .then((res) => {
      if (res.code === 200 && res.data) {
        store.setTraceResult(res.data)
        showCodeDialog.value = false
        router.push({ name: 'TraceResult', query: { batchNo: res.data.productBatchNo } })
      } else if (res.code === 404) {
        showToast('未查到该溯源码对应的商品信息，请确认溯源码是否正确')
      } else if (res.code === 400) {
        showToast(res.message || '该溯源码已失效')
      } else {
        showToast(res.message || '查询失败，请稍后重试')
      }
    })
    .catch(() => {
      showToast('网络异常，请检查网络后重试')
    })
    .finally(() => {
      codeLoading.value = false
    })
}
</script>

<template>
  <div class="home-page">
    <div class="header">
      <div class="header-top">
        <span class="header-city">食品安全追溯</span>
        <div class="avatar-entry" @click="goUserCenter" aria-label="用户中心" role="button" tabindex="0">
          <img :src="headImg" alt="头像" draggable="false" />
        </div>
      </div>
      <div class="header-main">
        <div class="logo-circle">
          <van-icon name="passed" size="32" color="#fff" />
        </div>
        <h1>食品安全追溯平台</h1>
        <p>输入溯源码，了解食品全程信息</p>
      </div>
    </div>

    <!-- 扫码查询区 -->
    <div class="scan-area">
      <div class="scan-btn" @click="openCodeDialog" role="button" tabindex="0" aria-label="输入溯源码查询">
        <div class="scan-icon">
          <van-icon name="label-o" size="28" />
        </div>
        <span>输入溯源码</span>
      </div>
    </div>

    <!-- 溯源码输入弹窗 -->
    <van-dialog
      v-model:show="showCodeDialog"
      title="输入溯源码"
      show-cancel-button
      :confirm-loading="codeLoading"
      @confirm="onCodeSearch"
    >
      <div class="code-dialog-body">
        <van-field
          v-model="traceCodeInput"
          label="溯源码"
          placeholder="请输入溯源码"
          clearable
          autofocus
        />
      </div>
    </van-dialog>

    <!-- 功能入口 -->
    <div class="menu-section">
      <div class="menu-title">服务专区</div>
      <div class="menu-grid">
        <div class="menu-card complaint-card" @click="goComplaintSubmit" role="button" tabindex="0" aria-label="投诉反馈">
          <div class="menu-icon-wrap icon-danger">
            <van-icon name="warning-o" size="24" color="#f56c6c" />
          </div>
          <span class="menu-label">投诉反馈</span>
          <span class="menu-sub">反馈食品安全问题</span>
        </div>
        <div class="menu-card query-card" @click="goComplaintQuery" role="button" tabindex="0" aria-label="结果查询">
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
  background: rgba(25, 137, 250, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1989fa;
}

.code-dialog-body {
  padding: 16px 0;
}

.scan-btn span {
  font-size: 16px;
  color: #323233;
  font-weight: 500;
}

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
