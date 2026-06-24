<script setup lang="ts">
import { ref, computed } from 'vue'

const emit = defineEmits<{
  (e: 'login', data: { username: string; password: string; role: string }): void
}>()

const mode = ref<'login' | 'register'>('login')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const role = ref('enterprise')
const errorMsg = ref('')
const loading = ref(false)

const roleOptions = [
  { value: 'enterprise', label: '企业用户' },
  { value: 'regulator', label: '监管人员' },
]

const canSubmit = computed(() => {
  if (!username.value.trim() || !password.value.trim()) return false
  if (mode.value === 'register') {
    if (!confirmPassword.value.trim()) return false
    if (password.value !== confirmPassword.value) return false
  }
  return true
})

function switchMode(target: 'login' | 'register') {
  mode.value = target
  errorMsg.value = ''
}

function handleSubmit() {
  errorMsg.value = ''
  if (mode.value === 'register' && password.value !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  emit('login', {
    username: username.value.trim(),
    password: password.value,
    role: role.value,
  })
  // 防止父组件未重置 loading 的情况
  setTimeout(() => { loading.value = false }, 5000)
}

function setLoading(v: boolean) {
  loading.value = v
}

function setError(msg: string) {
  errorMsg.value = msg
}

defineExpose({ setLoading, setError })
</script>

<template>
  <div class="auth-screen">
    <div class="auth-card">
      <div class="auth-brand">
        <span class="auth-brand-mark">溯</span>
        <h1>食品溯源系统</h1>
        <p>后台管理中心</p>
      </div>

      <div class="auth-tabs">
        <button
          class="auth-tab"
          :class="{ active: mode === 'login' }"
          type="button"
          @click="switchMode('login')"
        >
          登录
        </button>
        <button
          class="auth-tab"
          :class="{ active: mode === 'register' }"
          type="button"
          @click="switchMode('register')"
        >
          注册
        </button>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            autocomplete="username"
            placeholder="请输入用户名"
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
          />
        </div>

        <div v-if="mode === 'register'" class="form-group">
          <label>确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            autocomplete="new-password"
            placeholder="请再次输入密码"
          />
        </div>

        <div v-if="mode === 'register'" class="form-group">
          <label>注册身份</label>
          <select v-model="role">
            <option
              v-for="opt in roleOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </option>
          </select>
        </div>

        <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>

        <button
          class="auth-submit"
          type="submit"
          :disabled="!canSubmit || loading"
        >
          <span v-if="loading" class="auth-spinner"></span>
          {{ mode === 'login' ? '登 录' : '注 册' }}
        </button>

        <p class="auth-footnote">
          {{ mode === 'login' ? '还没有账号？' : '已有账号？' }}
          <button
            type="button"
            class="auth-switch-link"
            @click="switchMode(mode === 'login' ? 'register' : 'login')"
          >
            {{ mode === 'login' ? '立即注册' : '去登录' }}
          </button>
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.auth-screen {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #eaf1f9 0%, #dbe8f5 40%, #eef4fa 100%);
  padding: 40px 20px;
}

.auth-card {
  width: 440px;
  padding: 44px 40px 36px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 16px 44px rgba(23, 50, 89, 0.1), 0 2px 8px rgba(23, 50, 89, 0.06);
}

.auth-brand {
  text-align: center;
  margin-bottom: 32px;
}

.auth-brand-mark {
  display: inline-grid;
  place-items: center;
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  border-radius: 12px 12px 16px 16px;
  color: #fff;
  background: linear-gradient(135deg, #13a4bd, #245ee8);
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 24px;
  box-shadow: 0 8px 22px rgba(29, 120, 205, 0.28);
}

.auth-brand h1 {
  margin: 0 0 4px;
  color: #1262d9;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 2px;
}

.auth-brand p {
  margin: 0;
  color: #91a4bc;
  font-size: 13px;
}

.auth-tabs {
  display: flex;
  margin-bottom: 28px;
  border-bottom: 2px solid #dce7f1;
}

.auth-tab {
  flex: 1;
  padding: 10px 0;
  border: 0;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  color: #6c84a3;
  background: transparent;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.16s, border-color 0.16s;
}

.auth-tab:hover {
  color: #2666df;
}

.auth-tab.active {
  color: #2666df;
  border-bottom-color: #2666df;
}

.auth-form .form-group {
  margin-bottom: 18px;
}

.auth-form .form-group label {
  display: block;
  margin-bottom: 6px;
  color: #48627e;
  font-size: 13px;
  font-weight: 700;
}

.auth-form .form-group input,
.auth-form .form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d7e4f0;
  border-radius: 8px;
  color: #284666;
  background: #fff;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.auth-form .form-group input:focus,
.auth-form .form-group select:focus {
  border-color: #2b6cea;
  box-shadow: 0 0 0 3px rgba(43, 108, 234, 0.08);
}

.auth-form .form-group input::placeholder {
  color: #bcc8d6;
}

.auth-error {
  margin: 0 0 12px;
  padding: 10px 14px;
  border-radius: 8px;
  color: #c72929;
  background: #fff5f5;
  font-size: 13px;
  font-weight: 600;
}

.auth-submit {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 11px 0;
  border: 0;
  border-radius: 9px;
  color: #fff;
  background: linear-gradient(135deg, #2666df, #1c52c0);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.16s;
  letter-spacing: 4px;
}

.auth-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(38, 102, 223, 0.32);
}

.auth-submit:active:not(:disabled) {
  transform: translateY(0);
}

.auth-submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.auth-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: auth-spin 0.6s linear infinite;
}

@keyframes auth-spin {
  to { transform: rotate(360deg); }
}

.auth-footnote {
  margin: 20px 0 0;
  text-align: center;
  color: #91a4bc;
  font-size: 13px;
}

.auth-switch-link {
  border: 0;
  padding: 0;
  color: #2666df;
  background: transparent;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.auth-switch-link:hover {
  text-decoration: underline;
}
</style>
