<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { updateUserInfo } from '@/api/user'
import { useAppStore } from '@/store/app'

const router = useRouter()
const store = useAppStore()

const nickName = ref(store.userInfo?.nickName || '')
const realName = ref(store.userInfo?.realName || '')
const gender = ref(store.userInfo?.gender || '')
const region = ref(store.userInfo?.region || '')
const showGenderSheet = ref(false)
const saving = ref(false)

const genderLabel = computed(() => {
  const map: Record<string, string> = { 'M': '男', 'F': '女', 'U': '保密' }
  return map[gender.value] || '请选择'
})

const genderOptions = [
  { name: '男', value: 'M' },
  { name: '女', value: 'F' },
  { name: '保密', value: 'U' }
]

function onSelectGender(item: { name: string; value: string }) {
  gender.value = item.value
  showGenderSheet.value = false
}

async function onSave() {
  if (!store.userInfo?.phone) {
    showToast('用户信息异常')
    return
  }
  saving.value = true
  try {
    const res = await updateUserInfo({
      phone: store.userInfo.phone,
      nickName: nickName.value || undefined,
      realName: realName.value || undefined,
      gender: gender.value || undefined,
      region: region.value || undefined
    })
    if (res.code === 200 && res.data) {
      store.setUserInfo(res.data)
      showToast('保存成功')
      router.back()
    } else {
      showToast(res.message || '保存失败')
    }
  } catch {
    showToast('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="pe-page">
    <van-nav-bar
      title="编辑资料"
      left-arrow
      @click-left="router.back"
      fixed
      placeholder
    />

    <div class="pe-form">
      <van-field
        label="手机号"
        :model-value="store.userInfo?.phone || ''"
        readonly
        input-align="right"
      />
      <van-field
        v-model="nickName"
        label="昵称"
        placeholder="请输入昵称"
        maxlength="20"
        input-align="right"
      />
      <van-field
        v-model="realName"
        label="真实姓名"
        placeholder="请输入真实姓名"
        maxlength="20"
        input-align="right"
      />
      <van-field
        :model-value="genderLabel"
        label="性别"
        placeholder="请选择"
        readonly
        clickable
        is-link
        input-align="right"
        @click="showGenderSheet = true"
      />
      <van-field
        v-model="region"
        label="地区"
        placeholder="请输入地区"
        maxlength="50"
        input-align="right"
      />
    </div>

    <div class="pe-save">
      <van-button
        type="primary"
        block
        round
        :loading="saving"
        @click="onSave"
      >
        保存
      </van-button>
    </div>

    <van-action-sheet
      v-model:show="showGenderSheet"
      :actions="genderOptions"
      cancel-text="取消"
      @select="onSelectGender"
    />
  </div>
</template>

<style scoped>
.pe-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.pe-form {
  margin-top: 8px;
}

.pe-save {
  margin: 32px 16px;
}
</style>
