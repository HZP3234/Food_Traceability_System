<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { submitComplaint } from '@/api/complaint'
import { uploadImage } from '@/api/upload'
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
  productBatchNo: (route.query.productBatchNo as string) || '',
  productName: (route.query.productName as string) || '',
  complaintType: '' as unknown as ComplaintTypeKey,
  complaintContent: '',
  consumerName: '',
  consumerPhone: ''
})

onMounted(() => {
  if (store.isLoggedIn && store.userInfo) {
    form.consumerPhone = store.userInfo.phone
    form.consumerName = store.userInfo.nickName
  }
})

const complaintTypes = [
  { value: 1 as ComplaintTypeKey, label: '质量问题' },
  { value: 2 as ComplaintTypeKey, label: '包装问题' },
  { value: 3 as ComplaintTypeKey, label: '保质期异常' },
  { value: 4 as ComplaintTypeKey, label: '假冒伪劣' },
  { value: 5 as ComplaintTypeKey, label: '其他问题' }
]

const fileList = ref<any[]>([])
const uploadedUrls = ref<string[]>([])
const uploading = ref(false)
const MAX_IMAGES = 4

function onAfterRead(file: any) {
  const files = Array.isArray(file) ? file : [file]
  files.forEach(async (f: any) => {
    if (fileList.value.length >= MAX_IMAGES) {
      showToast(`最多上传${MAX_IMAGES}张照片`)
      return
    }
    f.status = 'uploading'
    fileList.value.push(f)
    uploading.value = true
    try {
      const formData = new FormData()
      formData.append('file', f.file as File)
      const res = await uploadImage(formData)
      if (res.code === 200 && res.data) {
        f.status = 'done'
        f.url = res.data
        uploadedUrls.value.push(res.data)
      } else {
        showToast(res.message || '上传失败')
        fileList.value = fileList.value.filter(item => item !== f)
      }
    } catch {
      showToast('图片上传失败，请重试')
      fileList.value = fileList.value.filter(item => item !== f)
    } finally {
      uploading.value = false
    }
  })
}

function onDeleteImage(file: any) {
  fileList.value = fileList.value.filter(item => item !== file)
  if (file.url) {
    uploadedUrls.value = uploadedUrls.value.filter(u => u !== file.url)
  }
}

function onTypeSelect(type: ComplaintTypeKey) {
  form.complaintType = type
}

function onSubmit() {
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
    productBatchNo: form.productBatchNo,
    productName: form.productName,
    consumerName: form.consumerName || '匿名',
    consumerPhone: form.consumerPhone,
    complaintType: form.complaintType,
    complaintTitle: `${complaintTypes.find(t => t.value === form.complaintType)?.label || ''}投诉`,
    complaintContent: form.complaintContent,
    imageUrls: uploadedUrls.value.length > 0 ? uploadedUrls.value.join(',') : undefined
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

    <!-- 提交成功 -->
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

    <!-- 投诉表单 -->
    <template v-else>
      <van-form ref="formRef" @submit="onSubmit" :scroll-to-error="true">
        <!-- 投诉对象 -->
        <van-cell-group inset title="投诉对象">
          <van-field
            v-model="form.productBatchNo"
            name="productBatchNo"
            label="产品批次号"
            placeholder="请输入产品批次号"
            :rules="[{ required: true, message: '请输入产品批次号' }]"
          />
          <van-field
            v-model="form.productName"
            name="productName"
            label="投诉的公司"
            placeholder="请输入公司名称"
            :rules="[{ required: true, message: '请输入公司名称' }]"
          />
        </van-cell-group>

        <!-- 投诉类型 -->
        <van-cell-group inset title="投诉类型">
          <div class="type-options">
            <div
              v-for="t in complaintTypes"
              :key="t.value"
              class="type-item"
              :class="{ active: form.complaintType === t.value }"
              @click="onTypeSelect(t.value)"
            >
              {{ t.label }}
            </div>
          </div>
        </van-cell-group>

        <!-- 投诉内容 -->
        <van-cell-group inset title="投诉内容">
          <van-field
            v-model="form.complaintContent"
            name="complaintContent"
            type="textarea"
            rows="4"
            placeholder="请输入内容"
            :rules="[{ required: true, message: '请输入投诉内容' }]"
            maxlength="500"
            show-word-limit
          />
          <!-- 照片上传 -->
          <div class="photo-upload">
            <span class="photo-label">照片</span>
            <div class="photo-grid">
              <van-uploader
                v-model="fileList"
                :after-read="onAfterRead"
                :max-count="MAX_IMAGES"
                :preview-full-image="true"
                accept="image/*"
                :disabled="uploading"
                @delete="onDeleteImage"
              />
            </div>
          </div>
        </van-cell-group>

        <!-- 投诉人信息 -->
        <van-cell-group inset title="投诉人信息">
          <van-field
            v-model="form.consumerName"
            name="consumerName"
            label="姓名"
            placeholder="请输入内容（可匿名）"
          />
          <van-field
            v-model="form.consumerPhone"
            name="consumerPhone"
            label="电话"
            placeholder="请输入手机号"
            type="tel"
            maxlength="11"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
            ]"
          />
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

/* 类型选择 */
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

/* 照片上传 */
.photo-upload {
  padding: 12px 16px;
  background: #fff;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.photo-label {
  font-size: 14px;
  color: #323233;
  flex-shrink: 0;
  padding-top: 4px;
}

.photo-grid {
  display: flex;
  gap: 8px;
}

.photo-add {
  width: 80px;
  height: 80px;
  border: 1px dashed #c8c9cc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.submit-area {
  margin: 32px 20px;
}

.mt-10 {
  margin-top: 10px;
}

/* 提交成功 */
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
