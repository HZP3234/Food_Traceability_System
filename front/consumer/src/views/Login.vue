<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { login } from '@/api/user'

const router = useRouter()
const route = useRoute()
const store = useAppStore()

const phone = ref('')
const code = ref('123456')
const loginLoading = ref(false)

const redirectPath = (route.query.redirect as string) || '/'

async function onLogin() {
  if (!phone.value.trim()) {
    showToast('请输入手机号')
    return
  }
  loginLoading.value = true
  try {
    const res = await login(phone.value.trim(), code.value)
    if (res.code === 200 && res.data) {
      store.setUserInfo(res.data)
      showToast('登录成功')
      router.replace(redirectPath)
    } else {
      showToast(res.message || '登录失败')
    }
  } catch (e: any) {
    showToast(e.message || '登录失败，请检查网络')
  } finally {
    loginLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-header">
      <h1>食品安全追溯平台</h1>
      <p>登录后享受更多服务</p>
    </div>

    <div class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="phone"
          type="tel"
          label="手机号"
          placeholder="请输入手机号"
          maxlength="11"
          clearable
        >
          <template #left-icon>
            <van-icon name="phone-o" />
          </template>
        </van-field>
        <van-field
          v-model="code"
          type="digit"
          label="验证码"
          placeholder="请输入验证码"
          maxlength="6"
          clearable
        >
          <template #left-icon>
            <van-icon name="shield-o" />
          </template>
        </van-field>
      </van-cell-group>

      <div class="login-btn-area">
        <van-button
          type="primary"
          block
          round
          :loading="loginLoading"
          loading-text="登录中..."
          @click="onLogin"
        >
          登录 / 注册
        </van-button>
      </div>

      <div class="login-tip">
        新用户自动注册，验证码固定为 123456
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.login-header {
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  padding: 48px 20px 40px;
  text-align: center;
  color: #fff;
}

.login-header h1 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px;
}

.login-header p {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}

.login-form {
  margin: 24px 16px;
}

.login-btn-area {
  margin: 24px 16px 0;
}

.login-tip {
  text-align: center;
  font-size: 12px;
  color: #c8c9cc;
  margin-top: 16px;
}
</style>
