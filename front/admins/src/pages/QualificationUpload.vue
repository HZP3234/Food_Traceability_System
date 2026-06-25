<script setup lang="ts">
import { ref, computed, inject, type Ref } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { enterpriseApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
const isRegulator = computed(() => currentRole?.value === 'regulator' || currentRole?.value === 'super-admin')

const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const saving = ref(false)

const form = ref({
  enterpriseName: '', certNo: '', address: '',
  contactPhone: '', contactPerson: '', remark: '',
})

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function submitForm() {
  if (!form.value.enterpriseName.trim()) { notify('error', '请填写企业名称'); return }
  saving.value = true
  try {
    await enterpriseApi.create(form.value)
    notify('success', '企业资质提交成功，等待监管审核')
    form.value = { enterpriseName: '', certNo: '', address: '', contactPhone: '', contactPerson: '', remark: '' }
  } catch (e: any) { notify('error', '操作失败: ' + e.message) }
  finally { saving.value = false }
}
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner" :class="isRegulator ? 'manufacturer' : 'supplier'">
      <span class="trace-role-badge">{{ isRegulator ? '监管建档' : '资质上传' }}</span>
      <span v-if="isRegulator">为企业<strong>建档</strong>并录入资质信息。已保存的资质可在「资质列表管理」中查看和管理。</span>
      <span v-else>上传<strong>本企业</strong>的资质信息。提交后由监管机构审核。已保存的资质可在「资质列表管理」中查看。</span>
    </div>

    <section class="trace-panel">
      <header class="panel-header">
        <div><p>资质上传</p><h2>新增企业资质</h2></div>
      </header>
      <div class="panel-body" style="padding:24px 28px">
        <div class="trace-hint info">📋 请在此页面填写企业资质信息。提交后由<strong>监管机构</strong>在线审核。已保存的资质记录可在<strong>「资质列表管理」</strong>页面查看。</div>
        <div class="upload-form-grid">
          <label>企业名称 *<input v-model="form.enterpriseName" placeholder="公司全称" /></label>
          <label>统一社会信用代码<input v-model="form.certNo" placeholder="18位统一社会信用代码" /></label>
          <label>注册地址<input v-model="form.address" placeholder="企业注册地址" /></label>
          <label>联系人<input v-model="form.contactPerson" placeholder="联系人姓名" /></label>
          <label>联系电话<input v-model="form.contactPhone" placeholder="联系电话" /></label>
        </div>
        <label style="display:grid;gap:8px;color:#718ba6;font-size:13px;font-weight:700;margin-top:28px">
          资质描述 / 备注
          <textarea v-model="form.remark" placeholder="请详细描述资质证书类型、编号、发证机关、有效期等信息。例如：食品生产许可证 SC10513030200889，发证机关：河北省市场监管局，有效期至 2027-05-30" style="width:100%;padding:10px;border:1px solid #d7e4f0;border-radius:7px;min-height:100px;resize:vertical;font-size:13px" />
        </label>
        <div style="display:flex;gap:10px;margin-top:24px;justify-content:flex-end">
          <button class="primary" :disabled="saving" @click="submitForm">
            <el-icon><Check /></el-icon> {{ saving ? '提交中...' : '提交资质' }}
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/trace-page.css';

.upload-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px 24px;
}

.upload-form-grid label:first-child {
  grid-column: 1 / -1;
}

.upload-form-grid label {
  display: grid;
  gap: 8px;
  color: #718ba6;
  font-size: 13px;
  font-weight: 700;
}

.upload-form-grid input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d7e4f0;
  border-radius: 7px;
  outline-color: #2a6cea;
  color: #294b6e;
  background: #fff;
  font-size: 13px;
}
</style>