<script setup lang="ts">
import { ref, onUnmounted, nextTick } from 'vue'
import { Html5Qrcode, type CameraDevice } from 'html5-qrcode'
import { showToast } from 'vant'

const emit = defineEmits<{
  scanned: [code: string]
  close: []
}>()

const visible = ref(false)
let scanner: Html5Qrcode | null = null
const scannerId = 'html5qr-reader'

async function open() {
  const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
  if (!isLocalhost && window.location.protocol !== 'https:') {
    showToast('手机端需要使用 HTTPS 访问，请联系管理员')
    return
  }

  visible.value = true
  await nextTick()
  startScan()
}

function close() {
  stopScan()
  visible.value = false
  emit('close')
}

async function startScan() {
  try {
    scanner = new Html5Qrcode(scannerId)

    let cameras: CameraDevice[] = []
    try {
      cameras = await Html5Qrcode.getCameras()
    } catch {}

    if (cameras.length === 0) {
      visible.value = false
      showToast('未检测到可用摄像头')
      return
    }

    const backCamera = cameras.find((c) =>
      c.label.toLowerCase().includes('back') ||
      c.label.toLowerCase().includes('environment') ||
      c.label.includes('后置')
    )
    const cameraId = backCamera?.id || cameras[0].id

    await scanner.start(
      cameraId,
      { fps: 10, qrbox: { width: 250, height: 250 }, aspectRatio: 1 },
      (decodedText) => { stopScan(); visible.value = false; emit('scanned', decodedText) },
      () => {}
    )
  } catch (err: any) {
    visible.value = false
    const msg = err?.message || err?.name || ''
    const name = err?.name || ''

    if (name === 'NotAllowedError' || msg.includes('NotAllowedError')) {
      showToast('请允许摄像头权限后重试')
    } else if (name === 'NotFoundError' || msg.includes('NotFoundError')) {
      showToast('未检测到可用摄像头')
    } else if (name === 'NotReadableError' || msg.includes('NotReadableError')) {
      showToast('摄像头被其他应用占用，请关闭后重试')
    } else if (msg.includes('NotSupportedError') || msg.includes('constraint')) {
      if (!scanner) return
      try {
        await scanner.start(
          { facingMode: 'environment' },
          { fps: 10, qrbox: { width: 250, height: 250 }, aspectRatio: 1 },
          (decodedText) => { stopScan(); visible.value = false; emit('scanned', decodedText) },
          () => {}
        )
        return
      } catch (retryErr: any) {
        showToast('摄像头不支持，请尝试更换浏览器')
        console.error('摄像头降级重试也失败:', retryErr)
      }
    } else {
      showToast(msg || '摄像头启动失败，请重试')
    }
    console.error('摄像头启动失败:', err)
  }
}

function stopScan() {
  if (scanner) { scanner.stop().catch(() => {}); scanner = null }
}

onUnmounted(() => { stopScan() })

defineExpose({ open, close })
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="scanner-overlay">
      <div class="scanner-mask" @click="close" />
      <div class="scanner-body">
        <div class="scanner-header">
          <span class="scanner-title">扫描溯源码</span>
          <van-icon name="cross" size="22" color="#fff" @click="close" class="scanner-close" />
        </div>
        <div class="scanner-viewport">
          <div :id="scannerId" class="scanner-camera" />
          <div class="scanner-decor">
            <div class="corner corner-tl" />
            <div class="corner corner-tr" />
            <div class="corner corner-bl" />
            <div class="corner corner-br" />
            <div class="laser-line" />
          </div>
        </div>
        <div class="scanner-hint">将二维码/条形码放入框内，即可自动扫描</div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.scanner-overlay {
  position: fixed; inset: 0; z-index: 3000;
  display: flex; flex-direction: column;
}
.scanner-mask {
  position: absolute; inset: 0;
  background: rgba(0, 0, 0, 0.92);
}
.scanner-body {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; align-items: center;
  padding: 0 20px; height: 100%;
}
.scanner-header {
  width: 100%; display: flex; align-items: center; justify-content: center;
  padding: 48px 0 24px; position: relative;
}
.scanner-title { font-size: 18px; color: #fff; font-weight: 500; }
.scanner-close { position: absolute; right: 0; cursor: pointer; }

.scanner-viewport {
  position: relative;
  width: 280px; height: 280px;
  border-radius: 16px; overflow: hidden;
  border: 2px solid rgba(7, 193, 96, 0.6);
  box-shadow: 0 0 0 4px rgba(7, 193, 96, 0.15);
}
.scanner-camera {
  width: 100%; height: 100%;
}
.scanner-camera :deep(video) { border-radius: 14px; }
.scanner-camera :deep(img) { display: none; }
.scanner-camera :deep(#html5qr-reader__dashboard_section) { display: none; }

/* 装饰层：覆盖在摄像头上方 */
.scanner-decor {
  position: absolute; inset: 0;
  pointer-events: none;
}

/* 四角边框 */
.corner {
  position: absolute; z-index: 5;
  width: 20px; height: 20px;
  border-color: #07c160;
  border-style: solid;
}
.corner-tl { top: 8px; left: 8px;  border-width: 3px 0 0 3px; border-radius: 4px 0 0 0; }
.corner-tr { top: 8px; right: 8px; border-width: 3px 3px 0 0; border-radius: 0 4px 0 0; }
.corner-bl { bottom: 8px; left: 8px; border-width: 0 0 3px 3px; border-radius: 0 0 0 4px; }
.corner-br { bottom: 8px; right: 8px; border-width: 0 3px 3px 0; border-radius: 0 0 4px 0; }

/* 绿色激光扫描线 */
.laser-line {
  position: absolute; z-index: 5;
  left: 10%; right: 10%; height: 2px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(7, 193, 96, 0.3) 15%,
    #07c160 50%,
    rgba(7, 193, 96, 0.3) 85%,
    transparent 100%
  );
  box-shadow:
    0 0 6px #07c160,
    0 0 18px rgba(7, 193, 96, 0.6),
    0 0 36px rgba(7, 193, 96, 0.25);
  border-radius: 1px;
  animation: laser-sweep 2.4s ease-in-out infinite;
}

@keyframes laser-sweep {
  0%   { top: 5%;  opacity: 0; }
  10%  { top: 5%;  opacity: 1; }
  50%  { top: 92%; opacity: 1; }
  90%  { top: 92%; opacity: 1; }
  100% { top: 92%; opacity: 0; }
}

.scanner-hint {
  margin-top: 28px; font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}
</style>
