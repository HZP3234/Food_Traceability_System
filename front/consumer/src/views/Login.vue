<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/store/app'
import { login, getCaptcha } from '@/api/user'

const router = useRouter()
const route = useRoute()
const store = useAppStore()

const phone = ref('')
const code = ref('')
const captchaKey = ref('')
const captchaImage = ref('')
const captchaError = ref('')
const loginLoading = ref(false)

const redirectPath = (route.query.redirect as string) || '/'

async function onCaptchaRefresh() {
  captchaError.value = ''
  try {
    const res = await getCaptcha()
    if (res.code === 200 && res.data) {
      captchaKey.value = res.data.captchaKey
      captchaImage.value = res.data.imageBase64
    }
  } catch {
    captchaError.value = '图形验证码加载失败，请点击刷新重试'
  }
}

onMounted(() => {
  onCaptchaRefresh()
})

async function onLogin() {
  captchaError.value = ''

  if (!phone.value.trim()) {
    showToast('请输入手机号')
    return
  }
  if (!code.value.trim()) {
    captchaError.value = '请输入图形验证码'
    return
  }

  loginLoading.value = true
  try {
    const res = await login(phone.value.trim(), code.value.trim(), captchaKey.value)
    if (res.code === 200 && res.data) {
      store.setUserInfo(res.data)
      showToast('登录成功')
      router.replace(redirectPath)
    } else {
      showToast(res.message || '登录失败')
    }
  } catch (e: any) {
    const msg = e.message || '登录失败'
    if (msg.includes('验证码')) {
      captchaError.value = msg
      code.value = ''
      onCaptchaRefresh()
    } else {
      showToast(msg)
    }
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
          label="验证码"
          placeholder="请输入验证码"
          maxlength="4"
          clearable
        >
          <template #left-icon>
            <van-icon name="shield-o" />
          </template>
        </van-field>
      </van-cell-group>

      <div class="captcha-img-wrap" @click="onCaptchaRefresh">
        <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="captcha-img" />
        <span v-else class="captcha-loading">点击获取</span>
      </div>

      <div v-if="captchaError" class="captcha-error">{{ captchaError }}</div>

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
        新用户自动注册，点击验证码图片可刷新
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

.captcha-img-wrap {
  cursor: pointer;
  display: flex;
  justify-content: center;
  margin-top: 12px;
}

.captcha-img {
  height: 40px;
  width: auto;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.captcha-loading {
  display: inline-block;
  height: 38px;
  line-height: 38px;
  padding: 0 10px;
  font-size: 13px;
  color: #07c160;
  border: 1px dashed #07c160;
  border-radius: 4px;
}

.captcha-error {
  text-align: center;
  font-size: 13px;
  color: #000;
  margin-top: 10px;
}
</style>
