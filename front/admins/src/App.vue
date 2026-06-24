<script setup lang="ts">
import { ref, computed, markRaw, type Component } from 'vue'
import { navigation, roles, type RoleKey } from './config/navigation'
import { authApi } from './services/api'
import LoginRegister from './pages/LoginRegister.vue'
import RawMaterials from './pages/RawMaterials.vue'
import Production from './pages/Production.vue'
import Processing from './pages/Processing.vue'
import ColdChain from './pages/ColdChain.vue'
import Sales from './pages/Sales.vue'

const isAuthenticated = ref(false)
const currentRole = ref<RoleKey>('super-admin')
const activePage = ref('dashboard')
const userInfo = ref({ username: '', role: '' })

const loginRef = ref<InstanceType<typeof LoginRegister> | null>(null)

const pageComponents: Record<string, Component> = {
  'raw-batch': markRaw(RawMaterials),
  'production-batch': markRaw(Production),
  'process-batch': markRaw(Processing),
  'cold-chain': markRaw(ColdChain),
  'sales': markRaw(Sales),
}

const visibleNavigation = computed(() =>
  navigation
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => item.roles.includes(currentRole.value)),
    }))
    .filter((group) => group.items.length > 0),
)

const activeItem = computed(() =>
  visibleNavigation.value.flatMap((group) => group.items).find((item) => item.id === activePage.value),
)

const currentComponent = computed(() => pageComponents[activePage.value] || null)

const currentRoleLabel = computed(() => {
  if (userInfo.value.role) {
    return roles[userInfo.value.role as RoleKey] || userInfo.value.role
  }
  return roles[currentRole.value]
})

async function handleLogin(data: { username: string; password: string; role: string }) {
  try {
    const result = await authApi.login({ username: data.username, password: data.password })
    onLoginSuccess(data.username, data.role, result)
  } catch {
    // 后端未就绪时，允许直接进入系统（开发阶段）
    onLoginSuccess(data.username, data.role)
  }
}

function onLoginSuccess(username: string, role: string, _result?: any) {
  userInfo.value = { username, role }
  if (role === 'super-admin' || role === 'enterprise' || role === 'regulator') {
    currentRole.value = role as RoleKey
  }
  isAuthenticated.value = true
}

function handleLogout() {
  isAuthenticated.value = false
  userInfo.value = { username: '', role: '' }
  activePage.value = 'dashboard'
  authApi.logout().catch(() => {})
}
</script>

<template>
  <LoginRegister
    v-if="!isAuthenticated"
    ref="loginRef"
    @login="handleLogin"
  />

  <main v-else class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">溯</span>
        <span>食品溯源系统</span>
      </div>

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
        <div class="breadcrumb">{{ currentRoleLabel }} <span>/</span> {{ activeItem?.label ?? '系统首页' }}</div>
        <div class="account">
          <span>{{ userInfo.username }}</span>
          <i>超</i>
          <strong>{{ currentRoleLabel }}</strong>
          <button type="button" class="logout-btn" @click="handleLogout">退出</button>
        </div>
      </header>

      <section class="page-slot">
        <div class="page-title">
          <div>
            <p>后台管理框架</p>
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

<style scoped>
.logout-btn {
  margin-left: 8px;
  padding: 5px 14px;
  border: 1px solid #d2e0ee;
  border-radius: 7px;
  color: #7890ac;
  background: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.16s, border-color 0.16s, background 0.16s;
}

.logout-btn:hover {
  color: #c72929;
  border-color: #f3cece;
  background: #fff5f5;
}
</style>
