<script setup lang="ts">
import { computed, ref, markRaw, provide, type Component } from 'vue'
import { Box, Close, Connection, Document, DocumentChecked, Grid, House, OfficeBuilding, SetUp, Shop, SwitchButton, User, Van } from '@element-plus/icons-vue'
import { navigation, roles, type RoleKey } from './config/navigation'
import RawMaterials from './pages/RawMaterials.vue'
import SupplierRawMaterials from './pages/SupplierRawMaterials.vue'
import ProductionBatchManagement from './pages/ProductionBatchManagement.vue'
import ColdChain from './pages/ColdChain.vue'
import Sales from './pages/Sales.vue'
import TraceCode from './pages/TraceCode.vue'
import LoginRegister from './pages/LoginRegister.vue'
import LandingPage from './pages/LandingPage.vue'
import AuditLog from './pages/AuditLog.vue'
import EnterpriseQualification from './pages/EnterpriseQualification.vue'
import RegulatoryTrace from './pages/RegulatoryTrace.vue'
import Dashboard from './pages/Dashboard.vue'
import Warnings from './pages/Warnings.vue'
import QualityInspection from './pages/QualityInspection.vue'
import QualificationUpload from './pages/QualificationUpload.vue'

import { authApi } from './services/api'

const loginRegisterRef = ref<InstanceType<typeof LoginRegister> | null>(null)

// 刷新后从 sessionStorage 恢复登录态，避免回到展示页
function restoreSession(): 'admin' | 'landing' {
  const token = sessionStorage.getItem('fts-admin-token')
  const userJson = sessionStorage.getItem('fts-admin-user')
  if (!token || !userJson) return 'landing'
  try {
    const user = JSON.parse(userJson)
    if (!user?.username || !user?.roleType) return 'landing'
    currentUser.value = user

    const enterpriseTypeToRole: Record<number, RoleKey> = {
      1: 'supplier', 2: 'manufacturer', 3: 'logistics', 4: 'seller',
    }
    const roleMap: Record<string, RoleKey> = {
      ADMIN: 'super-admin',
      REGULATOR: 'regulator',
      ENTERPRISE: user.enterpriseType ? (enterpriseTypeToRole[user.enterpriseType] || 'manufacturer') : 'manufacturer',
    }
    currentRole.value = roleMap[user.roleType] || 'manufacturer'
    return 'admin'
  } catch {
    return 'landing'
  }
}

const currentRole = ref<RoleKey>('manufacturer')
const activePage = ref('dashboard')
const screen = ref<'landing' | 'login' | 'admin'>(restoreSession())
const showPageInfo = ref(false)
const showLogoutConfirm = ref(false)
const currentUser = ref<{ username: string; roleType: string; realName: string; enterpriseUuid: string; enterpriseType?: number } | null>(null)

provide('currentRole', currentRole)

const pageComponents: Record<string, Component> = {
  'dashboard': markRaw(Dashboard),
  'raw-batch': markRaw(RawMaterials),
  'supplier-raw': markRaw(SupplierRawMaterials),
  'production-batch': markRaw(ProductionBatchManagement),
  'cold-chain': markRaw(ColdChain),
  'sales-terminal': markRaw(Sales),
  'trace-code': markRaw(TraceCode),
  'enterprise-qualification': markRaw(EnterpriseQualification),
  'regulatory-trace': markRaw(RegulatoryTrace),
  'audit-log': markRaw(AuditLog),
  'warnings': markRaw(Warnings),
  'quality-inspection': markRaw(QualityInspection),
  'qualification-upload': markRaw(QualificationUpload),
}

const visibleNavigation = computed(() =>
  navigation
    .map((group) => ({ ...group, items: group.items.filter((item) => item.roles.includes(currentRole.value)) }))
    .filter((group) => group.items.length > 0),
)

const activeItem = computed(() =>
  visibleNavigation.value.flatMap((group) => group.items).find((item) => item.id === activePage.value),
)

const currentComponent = computed(() => pageComponents[activePage.value] || null)

const pageDesc = computed(() => activeItem.value?.desc || '')

const navIcons: Record<string, Component> = {
  dashboard: House,
  'raw-batch': Box,
  'supplier-raw': Document,
  'production-batch': SetUp,
  'cold-chain': Van,
  'sales-terminal': Shop,
  'trace-code': Grid,
  'enterprise-qualification': OfficeBuilding,
  'quality-inspection': DocumentChecked,
  'qualification-upload': OfficeBuilding,
  'regulatory-trace': Connection,
  'audit-log': Document,
  warnings: Connection,
}

function changeRole(event: Event) {
  if (currentUser.value?.roleType !== 'ADMIN') return
  currentRole.value = (event.target as HTMLSelectElement).value as RoleKey
  if (!activeItem.value) activePage.value = 'dashboard'
}

async function handleLogin(data: { username: string; password: string; role: string }) {
  const loginRef = loginRegisterRef.value
  try {
    const res: any = await authApi.login(data.username, data.password)
    if (res.code === 200 && res.data) {
      const { token, username, roleType, realName, enterpriseUuid, enterpriseType } = res.data
      sessionStorage.setItem('fts-admin-token', token)
      sessionStorage.setItem('fts-admin-authenticated', 'true')
      const userInfo = { username, roleType, realName, enterpriseUuid, enterpriseType }
      sessionStorage.setItem('fts-admin-user', JSON.stringify(userInfo))
      currentUser.value = userInfo

      const enterpriseTypeToRole: Record<number, RoleKey> = {
        1: 'supplier', 2: 'manufacturer', 3: 'logistics', 4: 'seller',
      }
      const roleMap: Record<string, RoleKey> = {
        ADMIN: 'super-admin',
        REGULATOR: 'regulator',
        ENTERPRISE: enterpriseType ? (enterpriseTypeToRole[enterpriseType] || 'manufacturer') : 'manufacturer',
      }
      currentRole.value = roleMap[roleType] || 'manufacturer'
      screen.value = 'admin'
      if (loginRef) loginRef.setError('')
    } else {
      if (loginRef) loginRef.setError(res.message || '登录失败')
    }
  } catch (e: any) {
    if (loginRef) loginRef.setError(e.message || '网络请求失败，请检查后端服务')
  } finally {
    if (loginRef) loginRef.setLoading(false)
  }
}

function handleLogout() { showLogoutConfirm.value = true }

function confirmLogout() {
  sessionStorage.removeItem('fts-admin-token')
  sessionStorage.removeItem('fts-admin-authenticated')
  sessionStorage.removeItem('fts-admin-user')
  currentUser.value = null
  showLogoutConfirm.value = false
  screen.value = 'landing'
  activePage.value = 'dashboard'
}
</script>

<template>
  <LandingPage v-if="screen === 'landing'" @enter-admin="screen = 'login'" />
  <LoginRegister v-else-if="screen === 'login'" ref="loginRegisterRef" @login="handleLogin" />
  <main v-else class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">溯</span>
        <span>食品溯源系统</span>
      </div>

      <section class="role-card" aria-label="当前角色">
        <template v-if="currentUser?.roleType === 'ADMIN'">
          <label for="role"><el-icon><User /></el-icon> 当前角色</label>
          <select id="role" :value="currentRole" @change="changeRole">
            <option v-for="(label, key) in roles" :key="key" :value="key">{{ label }}</option>
          </select>
        </template>
        <template v-else>
          <p class="role-display-label"><el-icon><User /></el-icon> 当前角色</p>
          <p class="role-display-value">{{ roles[currentRole] }}</p>
        </template>
      </section>

      <nav class="side-nav" aria-label="主导航">
        <section v-for="group in visibleNavigation" :key="group.label" class="nav-group">
          <p>{{ group.label }}</p>
          <button
            v-for="item in group.items"
            :key="item.id"
            class="nav-item"
            :class="{ active: activePage === item.id }"
            type="button"
            @click="activePage = item.id"
          >
            <span class="nav-icon"><el-icon aria-hidden="true"><component :is="navIcons[item.id] || Grid" /></el-icon></span>
            <span>{{ item.label }}</span>
          </button>
        </section>
      </nav>

      <footer class="sidebar-footer">
        <span><el-icon><Grid /></el-icon> 系统运行正常</span>
        <button class="logout-btn" type="button" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出
        </button>
      </footer>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div class="breadcrumb">{{ roles[currentRole] }} <span>/</span> {{ activeItem?.label ?? '系统首页' }}</div>
        <div class="account">
          <span>2026年06月24日</span>
          <i>{{ currentUser?.realName?.charAt(0) || '用' }}</i>
          <strong>{{ currentUser?.realName || roles[currentRole] }}</strong>
        </div>
      </header>

      <section class="page-slot">
        <div class="page-title">
          <div>
            <p>{{ pageDesc || '后台管理框架' }}</p>
            <h1>{{ activeItem?.label ?? '系统首页' }}</h1>
          </div>
          <button type="button" class="ghost-button" @click="showPageInfo = true">
            <el-icon><Document /></el-icon> 页面说明
          </button>
        </div>

        <component v-if="currentComponent" :is="currentComponent" @navigate="activePage = $event" />
        <article v-else class="module-placeholder">
          <div class="placeholder-icon"><el-icon><Grid /></el-icon></div>
          <h2>{{ activeItem?.label ?? '系统首页' }} 模块内容区</h2>
          <p>这是统一后台框架预留的内容插槽。负责该模块的成员可在此接入自己的列表、表单、图表和接口逻辑。</p>
          <div class="handoff-note">
            <span>模块标识</span>
            <code>{{ activePage }}</code>
          </div>
        </article>
      </section>
    </section>
  </main>

  <!-- 页面说明模态框 -->
  <div v-if="showPageInfo" class="trace-modal-backdrop" @click.self="showPageInfo = false">
    <section class="trace-modal" style="width:520px">
      <header>
        <div><p>模块信息</p><h2>{{ activeItem?.label ?? '系统首页' }} — 模块说明</h2></div>
        <button @click="showPageInfo = false"><el-icon><Close /></el-icon></button>
      </header>
      <div class="modal-body">
        <p style="color:#48627e;line-height:1.8;font-size:14px;margin:0 0 16px">{{ pageDesc || '暂无详细说明' }}</p>
        <div class="handoff-note" style="margin-top:8px">
          <span>模块标识</span>
          <code>{{ activePage }}</code>
        </div>
      </div>
      <footer>
        <button class="secondary" @click="showPageInfo = false"><el-icon><Close /></el-icon> 关闭</button>
      </footer>
    </section>
  </div>

  <!-- 退出确认 -->
  <div v-if="showLogoutConfirm" class="trace-confirm-overlay" @click.self="showLogoutConfirm = false">
    <div class="trace-confirm-box">
      <div class="confirm-icon" style="font-size:40px;margin-bottom:14px"><el-icon><SwitchButton /></el-icon></div>
      <h3>确认退出</h3>
      <p>确定要退出管理后台吗？</p>
      <div class="trace-confirm-actions">
        <button class="secondary" @click="showLogoutConfirm = false"><el-icon><Close /></el-icon> 取消</button>
        <button class="primary" @click="confirmLogout"><el-icon><SwitchButton /></el-icon> 确认退出</button>
      </div>
    </div>
  </div>
</template>
