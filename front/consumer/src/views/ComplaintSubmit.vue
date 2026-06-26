<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { submitComplaint } from '@/api/complaint'
import { useAppStore } from '@/store/app'
import type { ComplaintTypeKey } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useAppStore()

const formRef = ref()
const submitting = ref(false)
const submitted = ref(false)
const complaintNo = ref('')

const form = reactive({
  batchNumber: (route.query.batchNumber as string) || '',
  enterpriseName: (route.query.enterpriseName as string) || '',
  traceCode: (route.query.traceCode as string) || '',
  complaintType: '' as unknown as ComplaintTypeKey,
  description: '',
  complainantName: '',
  phone: '',
  isAnonymous: false
})

onMounted(() => {
  if (store.isLoggedIn && store.userInfo) {
    form.phone = store.userInfo.phone || ''
    form.complainantName = store.userInfo.nickName || ''
  }
})

const complaintTypes = [
  { value: 1 as ComplaintTypeKey, label: '产品质量' },
  { value: 2 as ComplaintTypeKey, label: '食品安全' },
  { value: 3 as ComplaintTypeKey, label: '包装问题' },
  { value: 4 as ComplaintTypeKey, label: '虚假宣传' },
  { value: 5 as ComplaintTypeKey, label: '其他' }
]

function onTypeSelect(type: ComplaintTypeKey) {
  form.complaintType = type
}

function onSubmit() {
  if (!form.complaintType) {
    showToast('请选择投诉类型')
    return
  }
  formRef.value?.validate().then(() => {
    showConfirmDialog({
      title: '确认提交',
      message: '确认提交投诉信息吗？提交后将由相关部门进行处理。',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }).then(() => {
      doSubmit()
    }).catch(() => {})
  }).catch(() => {})
}

function doSubmit() {
  submitting.value = true
  submitComplaint({
    batchNumber: form.batchNumber,
    enterpriseName: form.enterpriseName,
    traceCode: form.traceCode || undefined,
    consumerUuid: store.userInfo?.consumerUuid || undefined,
    complainantName: form.isAnonymous ? '匿名' : (form.complainantName || '匿名'),
    phone: form.phone,
    isAnonymous: form.isAnonymous ? 1 : 0,
    complaintType: form.complaintType,
    description: form.description,
    photoUrls: undefined
  })
    .then((res) => {
      if (res.code === 200 && res.data) {
        complaintNo.value = res.data.complaintNo
        submitted.value = true
      } else {
        showToast(res.message || '提交失败')
      }
    })
    .catch((err) => {
      showToast(err.message || '提交失败')
    })
    .finally(() => {
      submitting.value = false
    })
}
</script>

<template>
  <div class="complaint-page">
    <van-nav-bar
      title="投诉反馈"
      left-arrow
      @click-left="router.back"
      fixed
      placeholder
    />

    <template v-if="submitted">
      <div class="success-page">
        <van-icon name="checked" size="64" color="#07c160" />
        <h2>提交成功</h2>
        <p class="success-no">投诉编号：{{ complaintNo }}</p>
        <p class="success-tip">您的投诉已提交，我们将尽快处理</p>
        <van-button type="primary" round block @click="router.push('/complaint-query')">
          查看投诉进度
        </van-button>
        <van-button round block class="mt-10" @click="router.push('/')">
          返回首页
        </van-button>
      </div>
    </template>

    <template v-else>
      <van-form ref="formRef" @submit="onSubmit" :scroll-to-error="true">
        <van-cell-group inset title="投诉对象">
          <van-field
            v-model="form.batchNumber"
            name="batchNumber"
            label="产品批次号"
            placeholder="请输入产品批次号"
            :rules="[{ required: true, message: '请输入产品批次号' }]"
          />
          <van-field
            v-model="form.enterpriseName"
            name="enterpriseName"
            label="被投诉企业"
            placeholder="请输入企业名称"
            :rules="[{ required: true, message: '请输入企业名称' }]"
          />
        </van-cell-group>

        <van-cell-group inset title="投诉类型">
          <div class="type-options">
            <div
              v-for="t in complaintTypes"
              :key="t.value"
              class="type-item"
              :class="{ active: form.complaintType === t.value }"
              role="radio"
              :aria-checked="form.complaintType === t.value"
              :aria-label="t.label"
              tabindex="0"
              @click="onTypeSelect(t.value)"
              @keydown.enter="onTypeSelect(t.value)"
            >
              {{ t.label }}
            </div>
          </div>
        </van-cell-group>

        <van-cell-group inset title="投诉内容">
          <van-field
            v-model="form.description"
            name="description"
            type="textarea"
            rows="4"
            placeholder="请详细描述投诉内容"
            :rules="[{ required: true, message: '请输入投诉内容' }]"
            maxlength="500"
            show-word-limit
          />
        </van-cell-group>

        <van-cell-group inset title="投诉人信息">
          <van-field
            v-model="form.complainantName"
            name="complainantName"
            label="姓名"
            placeholder="请输入姓名（可匿名）"
            :rules="[{ required: !form.isAnonymous, message: '请输入姓名' }]"
          />
          <van-field
            v-model="form.phone"
            name="phone"
            label="电话"
            placeholder="请输入手机号"
            type="tel"
            maxlength="11"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
            ]"
          />
          <div class="anonymous-row">
            <van-checkbox v-model="form.isAnonymous" shape="square" icon-size="16px">
              匿名提交
            </van-checkbox>
          </div>
        </van-cell-group>

        <div class="submit-area">
          <van-button
            type="danger"
            block
            round
            native-type="submit"
            :loading="submitting"
            loading-text="提交中..."
          >
            确认提交
          </van-button>
        </div>
      </van-form>
    </template>
  </div>
</template>

<style scoped>
.complaint-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

.type-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
}

.type-item {
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 14px;
  color: #646566;
  cursor: pointer;
  transition: all 0.2s;
}

.type-item.active {
  background: #1989fa;
  color: #fff;
}

.submit-area {
  margin: 32px 20px;
}

.mt-10 {
  margin-top: 10px;
}

.anonymous-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}

.success-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 24px 40px;
  text-align: center;
}

.success-page h2 {
  font-size: 20px;
  color: #323233;
  margin: 20px 0 8px;
}

.success-no {
  font-size: 14px;
  color: #07c160;
  margin: 0 0 4px;
}

.success-tip {
  font-size: 13px;
  color: #969799;
  margin: 0 0 32px;
}
</style>
