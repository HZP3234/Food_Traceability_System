<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { queryComplaintPage } from '@/api/complaint'
import { getUserInfo } from '@/api/user'
import { useAppStore } from '@/store/app'
import headImg from '@/assets/user_head_image.png'

const router = useRouter()
const store = useAppStore()

const complaintCount = ref(0)
const scanCount = ref(0)
const loading = ref(true)

const userName = computed(() => store.userInfo?.nickName || '消费者用户')
const userId = computed(() => store.userInfo?.consumerUuid || '')

onMounted(async () => {
  if (store.userInfo?.phone) {
    // 从后端刷新最新用户数据
    try {
      const res = await getUserInfo(store.userInfo.phone)
      if (res.code === 200 && res.data) {
        store.setUserInfo(res.data)
        scanCount.value = res.data.totalScans ?? 0
      }
    } catch {}

    // 查询投诉数量
    queryComplaintPage({ pageNum: 1, pageSize: 1, consumerPhone: store.userInfo.phone })
      .then((res) => {
        if (res.code === 200 && res.data) {
          complaintCount.value = res.data.total
        }
      })
      .catch(() => {})
  }

  if (scanCount.value === 0) {
    scanCount.value = store.userInfo?.totalScans ?? 0
  }

  loading.value = false
})

function goMyComplaints() {
  router.push('/complaint-query')
}

function goScanHistory() {
  router.push('/scan-history')
}

function onLogout() {
  store.logout()
  store.clearHistory()
  showToast('已退出登录')
  router.push('/')
}
</script>

<template>
  <div class="uc-page">
    <van-nav-bar
      title="用户中心"
      left-arrow
      @click-left="router.back"
      fixed
      placeholder
    />

    <!-- 用户信息头部 -->
    <div class="uc-header">
      <div class="avatar-wrap">
        <img :src="headImg" alt="头像" class="avatar" />
      </div>
      <div class="user-name">{{ userName }}</div>
      <div class="user-id">UUID: {{ userId || '加载中...' }}</div>
    </div>

    <!-- 数据统计 -->
    <div class="stats-row">
      <div class="stat-item" @click="goMyComplaints">
        <span class="stat-num">{{ complaintCount }}</span>
        <span class="stat-label">我的投诉</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item" @click="goScanHistory">
        <span class="stat-num">{{ scanCount }}</span>
        <span class="stat-label">扫码记录</span>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <van-cell-group inset>
        <van-cell title="编辑资料" icon="user-o" is-link to="/profile-edit" />
        <van-cell title="我的投诉" icon="records-o" is-link @click="goMyComplaints" />
        <van-cell title="投诉提交" icon="add-o" is-link to="/complaint-submit" />
        <van-cell title="结果查询" icon="search" is-link to="/complaint-query" />
      </van-cell-group>

      <van-cell-group inset style="margin-top: 12px">
        <van-cell title="关于平台" icon="info-o" is-link @click="showToast('食品安全追溯平台 v1.0')" />
        <van-cell title="隐私政策" icon="shield-o" is-link @click="showToast('隐私政策')" />
        <van-cell title="客服热线" icon="phone-o" is-link @click="showToast('客服热线: 400-000-1234')" />
      </van-cell-group>
    </div>

    <!-- 退出登录 -->
    <div class="logout-area">
      <van-button type="default" block round @click="onLogout">退出登录</van-button>
    </div>
  </div>
</template>

<style scoped>
.uc-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

.uc-header {
  background: linear-gradient(135deg, #07c160, #06ad56);
  padding: 32px 20px 36px;
  text-align: center;
  color: #fff;
}

.avatar-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.6);
  margin: 0 auto 12px;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-id {
  font-size: 12px;
  opacity: 0.75;
}

/* 统计 */
.stats-row {
  display: flex;
  margin: -16px 16px 16px;
  padding: 16px 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: #07c160;
}

.stat-label {
  font-size: 12px;
  color: #969799;
}

.stat-divider {
  width: 1px;
  background: #ebedf0;
  align-self: stretch;
}

/* 菜单 */
.menu-section {
  margin: 0 16px;
}

.logout-area {
  margin: 32px 16px;
}
</style>
