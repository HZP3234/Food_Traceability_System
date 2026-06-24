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

const currentRole = ref<RoleKey>('manufacturer')
const activePage = ref('dashboard')
const screen = ref<'landing' | 'login' | 'admin'>('landing')

// 通过 provide 让子页面感知当前角色
provide('currentRole', currentRole)

const pageComponents: Record<string, Component> = {
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
  currentRole.value = (event.target as HTMLSelectElement).value as RoleKey
  if (!activeItem.value) activePage.value = 'dashboard'
}

// 临时开发登录态：后端认证接口完成后，将此处替换为真实的 /auth/login 请求。
function handleLogin(data: { username: string; password: string; role: string }) {
  const roleMap: Record<string, RoleKey> = { enterprise: 'manufacturer', regulator: 'regulator' }
  currentRole.value = roleMap[data.role] || 'manufacturer'
  sessionStorage.setItem('fts-admin-authenticated', 'true')
  sessionStorage.setItem('fts-admin-user', data.username)
  screen.value = 'admin'
}
</script>

<template>
  <LandingPage v-if="screen === 'landing'" @enter-admin="screen = 'login'" />
  <LoginRegister v-else-if="screen === 'login'" @login="handleLogin" />
  <main v-else class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">溯</span>
        <span>食品溯源系统</span>
      </div>

      <section class="role-card" aria-label="当前角色">
        <label for="role">当前角色</label>
        <select id="role" :value="currentRole" @change="changeRole">
          <option v-for="(label, key) in roles" :key="key" :value="key">{{ label }}</option>
        </select>
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

      <footer class="sidebar-footer"><span></span>系统运行正常</footer>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div class="breadcrumb">{{ roles[currentRole] }} <span>/</span> {{ activeItem?.label ?? '系统首页' }}</div>
        <div class="account">
          <span>2026年06月24日</span>
          <i>超</i>
          <strong>{{ roles[currentRole] }}</strong>
        </div>
      </header>

      <section class="page-slot">
        <div class="page-title">
          <div>
            <p>{{ pageDesc || '后台管理框架' }}</p>
            <h1>{{ activeItem?.label ?? '系统首页' }}</h1>
          </div>
          <button type="button" class="ghost-button">页面说明</button>
        </div>

        <component v-if="currentComponent" :is="currentComponent" />
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
</template>
