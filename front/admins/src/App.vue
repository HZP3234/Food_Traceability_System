<script setup lang="ts">
import { computed, ref, markRaw, provide, type Component } from 'vue'
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

import { authApi } from './services/api'

const loginRegisterRef = ref<InstanceType<typeof LoginRegister> | null>(null)

const currentRole = ref<RoleKey>('manufacturer')
const activePage = ref('dashboard')
const screen = ref<'landing' | 'login' | 'admin'>('landing')
const showPageInfo = ref(false)
const showLogoutConfirm = ref(false)
const currentUser = ref<{ username: string; roleType: string; realName: string; enterpriseUuid: string; enterpriseType?: number } | null>(null)

// 通过 provide 让子页面感知当前角色
provide('currentRole', currentRole)

const pageComponents: Record<string, Component> = {
  'dashboard': markRaw(Dashboard),
  'raw-batch': markRaw(RawMaterials),
  'supplier-raw': markRaw(SupplierRawMaterials),
  'production-batch': markRaw(ProductionBatchManagement),
  'process-batch': markRaw(ProductionBatchManagement),
  'cold-chain': markRaw(ColdChain),
  'sales-terminal': markRaw(Sales),
  'trace-code': markRaw(TraceCode),
  'enterprise-qualification': markRaw(EnterpriseQualification),
  'regulatory-trace': markRaw(RegulatoryTrace),
  'audit-log': markRaw(AuditLog),
  'warnings': markRaw(Warnings),
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

function changeRole(event: Event) {
  // 仅超级管理员可切换视角预览
  if (currentUser.value?.roleType !== 'ADMIN') return
  currentRole.value = (event.target as HTMLSelectElement).value as RoleKey
  if (!activeItem.value) activePage.value = 'dashboard'
}

// 调用后端认证接口进行登录/注册
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

      // 角色映射: 后端 roleType + enterpriseType -> 前端 RoleKey
      // enterpriseType: 1-供应商 2-加工商 3-物流商 4-零售商
      const enterpriseTypeToRole: Record<number, RoleKey> = {
        1: 'supplier',
        2: 'manufacturer',
        3: 'logistics',
        4: 'seller',
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

function handleLogout() {
  showLogoutConfirm.value = true
}

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
        <!-- 超级管理员可切换角色视角 -->
        <template v-if="currentUser?.roleType === 'ADMIN'">
          <label for="role">当前角色</label>
          <select id="role" :value="currentRole" @change="changeRole">
            <option v-for="(label, key) in roles" :key="key" :value="key">{{ label }}</option>
          </select>
        </template>
        <!-- 普通用户仅显示当前角色 -->
        <template v-else>
          <p class="role-display-label">当前角色</p>
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
            <span class="nav-icon">{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </button>
        </section>
      </nav>

      <footer class="sidebar-footer">
        <span></span>系统运行正常
        <button class="logout-btn" type="button" @click="handleLogout">退出</button>
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
          <button type="button" class="ghost-button" @click="showPageInfo = true">页面说明</button>
        </div>

        <component v-if="currentComponent" :is="currentComponent" @navigate="activePage = $event" />
        <article v-else class="module-placeholder">
          <div class="placeholder-icon">{{ activeItem?.icon ?? '⌂' }}</div>
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
  <div v-if="showPageInfo" class="modal-overlay" @click.self="showPageInfo = false">
    <div class="modal" style="width:520px">
      <div class="modal-header">
        <h3>{{ activeItem?.label ?? '系统首页' }} — 模块说明</h3>
        <button class="modal-close" @click="showPageInfo = false">✕</button>
      </div>
      <div class="modal-body">
        <p style="color:#48627e;line-height:1.8;font-size:14px;margin:0 0 16px">{{ pageDesc || '暂无详细说明' }}</p>
        <div class="handoff-note" style="margin-top:8px">
          <span>模块标识</span>
          <code>{{ activePage }}</code>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="showPageInfo = false">关闭</button>
      </div>
    </div>
  </div>

  <!-- 退出确认 -->
  <div v-if="showLogoutConfirm" class="confirm-overlay" @click.self="showLogoutConfirm = false">
    <div class="confirm-box">
      <div class="confirm-icon">🚪</div>
      <h3>确认退出</h3>
      <p>确定要退出管理后台吗？</p>
      <div class="confirm-actions">
        <button class="btn btn-outline" @click="showLogoutConfirm = false">取消</button>
        <button class="btn btn-primary" @click="confirmLogout">确认退出</button>
      </div>
    </div>
  </div>
</template>
