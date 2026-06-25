<script setup lang="ts">
import { ref, computed, inject, type Ref } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { qualificationApi } from '../services/api'
import type { RoleKey } from '../config/navigation'

const currentRole = inject<Ref<RoleKey>>('currentRole')
const currentUser = inject<Ref<{ enterpriseName?: string; enterpriseUuid?: string } | null>>('currentUser')
const isRegulator = computed(() => currentRole?.value === 'regulator' || currentRole?.value === 'super-admin')

const toast = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const saving = ref(false)

// 资质类型选项
const qualTypeOptions = [
  { value: 1, label: '营业执照' },
  { value: 2, label: '生产许可证' },
  { value: 3, label: '经营许可证' },
  { value: 4, label: '其他资质' },
]

const form = ref({
  qualificationType: 1,
  qualificationNo: '',
  issueAuthority: '',
  validFrom: '',
  validTo: '',
  filePath: '',
  remark: '',
})

function notify(type: 'success' | 'error', text: string) { toast.value = { type, text }; setTimeout(() => (toast.value = null), 2600) }

async function submitForm() {
  if (!form.value.qualificationNo.trim()) { notify('error', '请填写资质编号'); return }
  if (!form.value.issueAuthority.trim()) { notify('error', '请填写颁发机构'); return }
  if (!form.value.validFrom) { notify('error', '请选择有效期起'); return }
  if (!form.value.validTo) { notify('error', '请选择有效期止'); return }
  saving.value = true
  try {
    await qualificationApi.submit({
      qualificationType: form.value.qualificationType,
      qualificationNo: form.value.qualificationNo.trim(),
      issueAuthority: form.value.issueAuthority.trim(),
      validFrom: form.value.validFrom,
      validTo: form.value.validTo,
      filePath: form.value.filePath.trim(),
      auditRemark: form.value.remark.trim(),
    })
    notify('success', '资质提交成功，等待监管审核')
    // 保留部分字段，清空编号和文件路径
    form.value = { ...form.value, qualificationNo: '', filePath: '', remark: '' }
  } catch (e: any) { notify('error', '操作失败: ' + e.message) }
  finally { saving.value = false }
}
</script>

<template>
  <div class="trace-page">
    <div v-if="toast" class="trace-toast" :class="toast.type">{{ toast.text }}</div>

    <div class="trace-role-banner" :class="isRegulator ? 'manufacturer' : 'supplier'">
      <span class="trace-role-badge">{{ isRegulator ? '监管建档' : '资质上传' }}</span>
      <span v-if="isRegulator">为企业<strong>登记</strong>资质证书信息。已保存的资质可在「资质列表管理」中审核。</span>
      <span v-else>上传<strong>本企业</strong>的资质证书信息。提交后由监管机构在线审核。</span>
    </div>

    <section class="trace-panel">
      <header class="panel-header">
        <div><p>{{ currentUser?.enterpriseName || '当前企业' }}</p><h2>新增/编辑企业资质</h2></div>
      </header>
      <div class="panel-body" style="padding:24px 28px">
        <div class="trace-hint info">📋 请填写<strong>资质证书</strong>信息。提交后由<strong>监管机构</strong>在线审核（通过/退回）。所有资质记录可在<strong>「资质列表管理」</strong>页面查看。</div>

        <div class="upload-form-grid">
          <label>
            资质类型 *
            <select v-model="form.qualificationType">
              <option v-for="opt in qualTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </label>
          <label>资质编号 *<input v-model="form.qualificationNo" placeholder="如：SC10513030200889" /></label>
          <label>颁发机构 *<input v-model="form.issueAuthority" placeholder="如：河北省市场监督管理局" /></label>
          <label>有效期起 *<input type="date" v-model="form.validFrom" /></label>
          <label>有效期止 *<input type="date" v-model="form.validTo" /></label>
          <label>文件路径（选填）<input v-model="form.filePath" placeholder="如：/uploads/qual/xxx.pdf" /></label>
        </div>

        <label style="display:grid;gap:8px;color:#718ba6;font-size:13px;font-weight:700;margin-top:28px">
          备注说明
          <textarea v-model="form.remark" placeholder="补充说明信息，如证书范围、特殊备注等" style="width:100%;padding:10px;border:1px solid #d7e4f0;border-radius:7px;min-height:80px;resize:vertical;font-size:13px" />
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

.upload-form-grid input,
.upload-form-grid select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d7e4f0;
  border-radius: 7px;
  outline-color: #2a6cea;
  color: #294b6e;
  background: #fff;
  font-size: 13px;
}

.upload-form-grid input[type="date"] {
  color-scheme: light;
}
</style>
