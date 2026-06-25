<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ArrowRight, CircleCheckFilled, Lock, Monitor, OfficeBuilding, User, View, Hide } from '@element-plus/icons-vue'

const emit = defineEmits<{
  (e: 'login', data: { username: string; password: string; role: string }): void
  (e: 'register', data: { username: string; password: string; role: string; enterpriseName?: string; creditCode?: string; contactPerson?: string; contactPhone?: string; address?: string; supplierCode?: string; extraInfo?: string }): void
}>()

const mode = ref<'login' | 'register'>('login')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const role = ref('supplier')
const errorMsg = ref('')
const loading = ref(false)
const activeField = ref('')
const showPassword = ref(false)
const cardStyle = ref<Record<string, string>>({})

// 注册额外字段
const enterpriseName = ref('')
const creditCode = ref('')
const contactPerson = ref('')
const contactPhone = ref('')
const address = ref('')
const supplierCode = ref('')
const extraInfo = ref('')

const roleOptions = [
  { value: 'supplier', label: '原料供应商', hint: '上传原料源头信息、企业资质，绑定供应商编码' },
  { value: 'manufacturer', label: '加工生产商', hint: '创建原料/生产批次，管理工艺模板，生成溯源码' },
  { value: 'logistics', label: '物流商', hint: '管理冷链运输、车辆、仓库，执行发运与签收' },
  { value: 'seller', label: '销售商', hint: '管理销售终端，补充储存与销售环境详情' },
  { value: 'regulator', label: '监管机构', hint: '全链追溯查询、企业资质审核、异常预警' },
  { value: 'super-admin', label: '超级管理员', hint: '系统全局管理，所有模块权限' },
]

const showEnterpriseFields = computed(() => role.value !== 'regulator' && role.value !== 'super-admin')
const showSupplierCode = computed(() => role.value === 'supplier')

const extraInfoLabel = computed(() => {
  switch (role.value) {
    case 'manufacturer': return '主要生产线'
    case 'logistics': return '冷链类型（冷藏/冷冻/恒温）'
    case 'seller': return '终端类型（超市/便利店/餐饮等）'
    case 'regulator': return '监管机构编号'
    case 'super-admin': return '管理员备注'
    default: return ''
  }
})

const extraInfoPlaceholder = computed(() => {
  switch (role.value) {
    case 'manufacturer': return '如：液态奶一号线 / 烘焙车间'
    case 'logistics': return '如：冷藏 2-8℃ / 冷冻 -18℃'
    case 'seller': return '如：连锁超市 / 社区便利店'
    case 'regulator': return '如：REG20260001'
    case 'super-admin': return '如：IT运维管理员'
    default: return ''
  }
})

const canSubmit = computed(() => {
  if (!username.value.trim() || !password.value.trim()) return false
  if (mode.value === 'register') {
    if (!confirmPassword.value.trim() || password.value !== confirmPassword.value) return false
    // 企业类角色必须填写企业名称
    if (showEnterpriseFields.value && !enterpriseName.value.trim()) return false
    // 供应商必须填写供应商编码
    if (showSupplierCode.value && !supplierCode.value.trim()) return false
  }
  return true
})

const passwordMatched = computed(() => !confirmPassword.value || password.value === confirmPassword.value)
const modeTitle = computed(() => mode.value === 'login' ? '开启可信工作台' : '创建可信身份')

// 切换角色时清空角色特定字段
watch(role, () => {
  supplierCode.value = ''
  extraInfo.value = ''
})

function switchMode(target: 'login' | 'register') {
  mode.value = target
  errorMsg.value = ''
  activeField.value = ''
}

function tiltCard(event: MouseEvent) {
  const target = event.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  const x = ((event.clientX - rect.left) / rect.width - .5) * 7
  const y = ((event.clientY - rect.top) / rect.height - .5) * -7
  cardStyle.value = { transform: `perspective(1000px) rotateX(${y}deg) rotateY(${x}deg)` }
}

function resetCard() { cardStyle.value = {} }

function handleSubmit() {
  errorMsg.value = ''
  if (mode.value === 'register' && password.value !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致，请重新确认。'
    return
  }
  if (mode.value === 'register') {
    if (showEnterpriseFields.value && !enterpriseName.value.trim()) {
      errorMsg.value = '请填写企业名称。'
      return
    }
    if (showSupplierCode.value && !supplierCode.value.trim()) {
      errorMsg.value = '请填写供应商编码。'
      return
    }
  }
  loading.value = true
  if (mode.value === 'register') {
    emit('register', {
      username: username.value.trim(),
      password: password.value,
      role: role.value,
      enterpriseName: enterpriseName.value.trim(),
      creditCode: creditCode.value.trim(),
      contactPerson: contactPerson.value.trim(),
      contactPhone: contactPhone.value.trim(),
      address: address.value.trim(),
      supplierCode: supplierCode.value.trim(),
      extraInfo: extraInfo.value.trim(),
    })
  } else {
    emit('login', {
      username: username.value.trim(),
      password: password.value,
      role: role.value,
    })
  }
  setTimeout(() => { loading.value = false }, 5000)
}

function setLoading(value: boolean) { loading.value = value }
function setError(message: string) { errorMsg.value = message }
defineExpose({ setLoading, setError })
</script>

<template>
  <main class="auth-screen" :class="{ registering: mode === 'register' }">
    <div class="grain" aria-hidden="true"></div>
    <div class="orb orb-one" aria-hidden="true"></div>
    <div class="orb orb-two" aria-hidden="true"></div>

    <section class="auth-stage">
      <aside class="auth-story" aria-label="系统介绍">
        <div class="story-topline"><span></span> FOOD TRACEABILITY NETWORK</div>
        <div class="story-copy">
          <p>数据有来处，<br><em>信任才有去处。</em></p>
          <span>每一次登录，都是进入食品全链路档案的一把钥匙。</span>
        </div>

        <div class="trace-orbit" aria-hidden="true">
          <i class="orbit-line line-one"></i><i class="orbit-line line-two"></i>
          <span class="trace-node source"><OfficeBuilding /></span>
          <span class="trace-node process"><Monitor /></span>
          <span class="trace-node verified"><CircleCheckFilled /></span>
          <b class="orbit-core">TRACE<br>PASS</b>
        </div>

        <div class="trust-row">
          <div><el-icon><CircleCheckFilled /></el-icon><span><strong>链路已加密</strong><small>ENCRYPTED ACCESS</small></span></div>
          <div><b>24/7</b><span><strong>实时守护</strong><small>TRACEABILITY ONLINE</small></span></div>
        </div>
      </aside>

      <section class="auth-panel" @mousemove="tiltCard" @mouseleave="resetCard">
        <div class="auth-card" :style="cardStyle">
          <div class="card-accent" aria-hidden="true"></div>
          <header class="auth-header">
            <div class="auth-logo"><span>溯</span><i></i></div>
            <div><p>食品溯源系统</p><h1>{{ modeTitle }}</h1></div>
          </header>

          <div class="auth-tabs" role="tablist" aria-label="登录方式">
            <button class="auth-tab" :class="{ active: mode === 'login' }" type="button" @click="switchMode('login')">登录系统</button>
            <button class="auth-tab" :class="{ active: mode === 'register' }" type="button" @click="switchMode('register')">注册身份</button>
            <span class="tab-indicator" :class="mode"></span>
          </div>

          <form class="auth-form" @submit.prevent="handleSubmit">
            <label class="field" :class="{ focused: activeField === 'username', filled: username }">
              <span class="field-icon"><el-icon><User /></el-icon></span>
              <span class="field-copy"><small>账号</small><input v-model="username" autocomplete="username" placeholder="输入用户名" @focus="activeField = 'username'" @blur="activeField = ''" /></span>
            </label>

            <label class="field" :class="{ focused: activeField === 'password', filled: password }">
              <span class="field-icon"><el-icon><Lock /></el-icon></span>
              <span class="field-copy"><small>密码</small><input v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="输入登录密码" @focus="activeField = 'password'" @blur="activeField = ''" /></span>
              <button class="password-toggle" type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword"><el-icon><Hide v-if="showPassword" /><View v-else /></el-icon></button>
            </label>

            <transition name="form-slide">
              <div v-if="mode === 'register'" class="register-fields">
                <label class="field" :class="{ focused: activeField === 'confirm', filled: confirmPassword, invalid: !passwordMatched }">
                  <span class="field-icon"><el-icon><CircleCheckFilled /></el-icon></span>
                  <span class="field-copy"><small>确认密码</small><input v-model="confirmPassword" :type="showPassword ? 'text' : 'password'" autocomplete="new-password" placeholder="再次输入密码" @focus="activeField = 'confirm'" @blur="activeField = ''" /></span>
                </label>

                <!-- 角色选择 -->
                <label class="role-select">
                  <span>选择身份角色</span>
                  <select v-model="role">
                    <option v-for="item in roleOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                  </select>
                  <small class="role-hint">{{ roleOptions.find(r => r.value === role)?.hint }}</small>
                </label>

                <!-- 企业资质（非监管/超管角色显示） -->
                <template v-if="showEnterpriseFields">
                  <div class="reg-section-title">企业资质信息</div>
                  <label class="field" :class="{ focused: activeField === 'entName', filled: enterpriseName }">
                    <span class="field-icon"><el-icon><OfficeBuilding /></el-icon></span>
                    <span class="field-copy"><small>企业名称 *</small><input v-model="enterpriseName" placeholder="请输入企业全称" @focus="activeField = 'entName'" @blur="activeField = ''" /></span>
                  </label>
                  <label class="field" :class="{ focused: activeField === 'credit', filled: creditCode }">
                    <span class="field-icon"><el-icon><Monitor /></el-icon></span>
                    <span class="field-copy"><small>统一信用代码</small><input v-model="creditCode" placeholder="18位信用代码" @focus="activeField = 'credit'" @blur="activeField = ''" /></span>
                  </label>
                  <div class="reg-row">
                    <label class="field half" :class="{ focused: activeField === 'contact', filled: contactPerson }">
                      <span class="field-copy"><small>联系人</small><input v-model="contactPerson" placeholder="姓名" @focus="activeField = 'contact'" @blur="activeField = ''" /></span>
                    </label>
                    <label class="field half" :class="{ focused: activeField === 'phone', filled: contactPhone }">
                      <span class="field-copy"><small>联系电话</small><input v-model="contactPhone" placeholder="11位手机号" @focus="activeField = 'phone'" @blur="activeField = ''" /></span>
                    </label>
                  </div>
                  <label class="field" :class="{ focused: activeField === 'addr', filled: address }">
                    <span class="field-copy"><small>企业地址</small><input v-model="address" placeholder="省市区详细地址" @focus="activeField = 'addr'" @blur="activeField = ''" /></span>
                  </label>

                  <!-- 供应商编码（仅供应商角色显示） -->
                  <template v-if="showSupplierCode">
                    <div class="reg-section-title">供应商绑定</div>
                    <label class="field" :class="{ focused: activeField === 'supCode', filled: supplierCode }">
                      <span class="field-icon" style="font-weight:900;color:#4a7b4e">#</span>
                      <span class="field-copy"><small>供应商编码 *</small><input v-model="supplierCode" placeholder="如：SUP20260001" @focus="activeField = 'supCode'" @blur="activeField = ''" /></span>
                    </label>
                  </template>
                </template>

                <!-- 角色特定字段 -->
                <template v-if="extraInfoLabel">
                  <label class="field" :class="{ focused: activeField === 'extra', filled: extraInfo }">
                    <span class="field-copy"><small>{{ extraInfoLabel }}</small><input v-model="extraInfo" :placeholder="extraInfoPlaceholder" @focus="activeField = 'extra'" @blur="activeField = ''" /></span>
                  </label>
                </template>
              </div>
            </transition>

            <p v-if="mode === 'register' && !passwordMatched" class="field-hint is-error">密码尚未一致</p>
            <p v-else class="field-hint"><el-icon><CircleCheckFilled /></el-icon> 已启用安全传输与身份校验</p>
            <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>

            <button class="auth-submit" type="submit" :disabled="!canSubmit || loading">
              <span v-if="loading" class="auth-spinner"></span>
              <template v-else>{{ mode === 'login' ? '进入工作台' : '创建并进入' }} <el-icon><ArrowRight /></el-icon></template>
            </button>
          </form>

          <footer class="auth-footer"><span>{{ mode === 'login' ? '还没有系统账号？' : '已经拥有系统账号？' }}</span><button type="button" @click="switchMode(mode === 'login' ? 'register' : 'login')">{{ mode === 'login' ? '创建身份' : '返回登录' }}</button></footer>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
:global(body){margin:0}.auth-screen{--ink:#102317;--lime:#d5ff75;min-height:100vh;display:grid;place-items:center;position:relative;overflow:hidden;padding:32px;background:#0b1911;color:#f4f7ee;font-family:"Microsoft YaHei",sans-serif}.grain{position:absolute;inset:0;opacity:.1;pointer-events:none;background-image:radial-gradient(#dffbb0 0.65px,transparent .65px);background-size:6px 6px}.orb{position:absolute;border-radius:50%;filter:blur(3px);opacity:.72}.orb-one{width:43vw;height:43vw;left:-17vw;top:-21vw;background:#34653b}.orb-two{width:35vw;height:35vw;right:-11vw;bottom:-18vw;background:#355329}.auth-stage{position:relative;z-index:1;display:grid;grid-template-columns:minmax(360px,.9fr) minmax(420px,.8fr);width:min(1120px,100%);min-height:650px;border:1px solid #d5ff752e;background:#102317d4;box-shadow:0 35px 100px #0008;backdrop-filter:blur(18px)}.auth-story{display:flex;flex-direction:column;justify-content:space-between;position:relative;overflow:hidden;padding:46px 54px;border-right:1px solid #d5ff7526}.story-topline{display:flex;align-items:center;gap:9px;color:#c7dcae;font:700 10px ui-monospace,monospace;letter-spacing:.15em}.story-topline span{width:28px;height:1px;background:var(--lime)}.story-copy{position:relative;z-index:2;margin-top:46px}.story-copy p{margin:0;color:#f4f7ee;font-family:Georgia,"STKaiti",serif;font-size:clamp(38px,4vw,56px);line-height:1.06;letter-spacing:-.07em}.story-copy em{color:var(--lime);font-style:normal}.story-copy>span{display:block;max-width:310px;margin-top:20px;color:#b9c9b8;font-size:13px;line-height:1.9}.trace-orbit{position:absolute;right:-61px;bottom:92px;width:330px;height:330px;border:1px solid #d5ff7542;border-radius:50%;animation:float 7s ease-in-out infinite}.trace-orbit:before,.trace-orbit:after{position:absolute;inset:34px;border:1px solid #d5ff7531;border-radius:50%;content:""}.trace-orbit:after{inset:98px;border-color:#d5ff757a}.orbit-line{position:absolute;top:50%;left:50%;width:240px;height:1px;background:linear-gradient(90deg,transparent,#d5ff7594);transform-origin:left}.line-one{transform:rotate(25deg)}.line-two{transform:rotate(155deg)}.trace-node{position:absolute;display:grid;place-items:center;width:42px;height:42px;border:1px solid #d5ff7580;border-radius:50%;background:#183b25;color:var(--lime);box-shadow:0 0 0 7px #102317;animation:pulse 2.4s ease-in-out infinite}.trace-node svg{width:19px}.source{top:35px;left:68px}.process{right:35px;bottom:68px;animation-delay:.7s}.verified{left:32px;bottom:45px;animation-delay:1.4s}.orbit-core{position:absolute;top:50%;left:50%;display:grid;place-items:center;width:105px;height:105px;border-radius:50%;color:#102317;background:var(--lime);font:800 12px/1.1 ui-monospace,monospace;letter-spacing:.08em;text-align:center;transform:translate(-50%,-50%)}.trust-row{display:flex;gap:28px;position:relative;z-index:2}.trust-row>div{display:flex;align-items:center;gap:9px;color:var(--lime)}.trust-row b{font:800 19px Georgia,serif}.trust-row span{display:grid;gap:2px;color:#f4f7ee}.trust-row strong{font-size:11px}.trust-row small{color:#91aa90;font:9px ui-monospace,monospace;letter-spacing:.05em}.auth-panel{display:grid;place-items:center;padding:34px;background:linear-gradient(145deg,#f7f5ec,#e7ecde);perspective:1000px}.auth-card{position:relative;width:min(100%,410px);max-height:90vh;padding:31px 34px 27px;overflow-y:auto;border:1px solid #c9d5c1;background:#fcfcf7;box-shadow:0 18px 42px #18331e24;transition:transform .18s ease-out}.card-accent{position:absolute;top:0;right:0;width:112px;height:5px;background:var(--lime)}.auth-header{display:flex;align-items:center;gap:13px;margin-bottom:28px}.auth-logo{position:relative;display:grid;place-items:center;width:43px;height:43px;border-radius:50%;color:var(--lime);background:#102317;font:800 15px Georgia,serif}.auth-logo i{position:absolute;right:-2px;bottom:-2px;width:11px;height:11px;border:2px solid #fcfcf7;border-radius:50%;background:var(--lime)}.auth-header p{margin:0 0 3px;color:#718271;font-size:11px}.auth-header h1{margin:0;color:#173a25;font-family:Georgia,"STKaiti",serif;font-size:23px;letter-spacing:-.04em}.auth-tabs{position:relative;display:grid;grid-template-columns:1fr 1fr;margin-bottom:22px;padding:4px;border:1px solid #d9e0d4;background:#eef1e9}.auth-tab{position:relative;z-index:1;border:0;padding:9px;color:#718271;background:transparent;font-size:13px;font-weight:700;cursor:pointer}.auth-tab.active{color:#163c25}.tab-indicator{position:absolute;top:4px;bottom:4px;left:4px;width:calc(50% - 4px);background:var(--lime);transition:transform .28s cubic-bezier(.2,.8,.2,1)}.tab-indicator.register{transform:translateX(100%)}.auth-form{display:grid;gap:12px}.field{display:flex;align-items:center;min-height:61px;border:1px solid #d6ded0;background:#fff;transition:border-color .2s,box-shadow .2s,transform .2s}.field.focused{border-color:#4a7b4e;box-shadow:0 0 0 3px #d5ff7582;transform:translateX(3px)}.field.invalid{border-color:#cc584e}.field-icon{display:grid;place-items:center;width:48px;align-self:stretch;border-right:1px solid #e0e6dd;color:#668069}.field-copy{display:grid;flex:1;gap:2px;padding:8px 11px}.field-copy small,.role-select>span{color:#8a9989;font-size:10px;font-weight:700;letter-spacing:.08em}.field input{width:100%;border:0;outline:0;color:#173a25;background:transparent;font:600 13px "Microsoft YaHei",sans-serif}.field input::placeholder{color:#aab5a9}.password-toggle{display:grid;place-items:center;width:40px;border:0;color:#779177;background:transparent;cursor:pointer}.register-fields{display:grid;gap:12px}.role-select{display:grid;gap:6px;padding:0 2px}.role-select select{height:37px;border:1px solid #d6ded0;padding:0 10px;color:#244b30;background:#fff;outline-color:#4a7b4e}.role-hint{color:#8a9989;font-size:10px;line-height:1.5}.reg-section-title{margin-top:4px;padding:6px 2px 2px;color:#4a7b4e;font-size:11px;font-weight:800;letter-spacing:.06em;border-bottom:1px solid #d5ff7580}.reg-row{display:grid;grid-template-columns:1fr 1fr;gap:12px}.field.half{padding:0}.field.half .field-icon{width:36px}.field.half .field-copy{padding:6px 8px}.field-hint{display:flex;align-items:center;gap:5px;margin:1px 0;color:#7c917d;font-size:11px}.field-hint :deep(svg){color:#5a8f55}.field-hint.is-error{color:#bd4f47}.auth-error{margin:0;padding:9px 10px;border-left:3px solid #c75148;color:#a63e37;background:#fff0ed;font-size:12px}.auth-submit{display:flex;align-items:center;justify-content:center;gap:9px;min-height:45px;margin-top:5px;border:0;color:#102317;background:var(--lime);font-weight:800;letter-spacing:.08em;cursor:pointer;transition:transform .18s,box-shadow .18s}.auth-submit:hover:not(:disabled){transform:translateY(-2px);box-shadow:0 9px 20px #96c64166}.auth-submit:disabled{opacity:.46;cursor:not-allowed}.auth-spinner{width:15px;height:15px;border:2px solid #10231755;border-top-color:#102317;border-radius:50%;animation:spin .7s linear infinite}.auth-footer{display:flex;justify-content:center;gap:5px;margin-top:20px;color:#829481;font-size:11px}.auth-footer button{border:0;padding:0;color:#245d37;background:transparent;font-weight:800;cursor:pointer}.auth-footer button:hover{text-decoration:underline}.form-slide-enter-active,.form-slide-leave-active{transition:all .25s ease}.form-slide-enter-from,.form-slide-leave-to{max-height:0;opacity:0;transform:translateY(-8px)}.form-slide-enter-to,.form-slide-leave-from{max-height:800px;opacity:1}.form-slide-enter-active{overflow:hidden}@keyframes spin{to{transform:rotate(360deg)}}@keyframes float{50%{transform:translateY(-12px)}}@keyframes pulse{50%{box-shadow:0 0 0 7px #102317,0 0 0 12px #d5ff7520}}@media(max-width:820px){.auth-screen{padding:18px}.auth-stage{grid-template-columns:1fr;max-width:510px;min-height:0}.auth-story{min-height:250px;padding:30px;border-right:0;border-bottom:1px solid #d5ff7526}.story-copy{margin-top:27px}.story-copy p{font-size:37px}.trace-orbit{right:-78px;bottom:-105px;transform:scale(.75)}.trust-row{margin-top:34px}.auth-panel{padding:24px}.auth-card{width:100%}}@media(max-width:440px){.auth-screen{padding:0}.auth-stage{min-height:100vh;border:0}.auth-story{display:none}.auth-panel{padding:18px}.auth-card{padding:27px 21px}.auth-header h1{font-size:21px}}
</style>
