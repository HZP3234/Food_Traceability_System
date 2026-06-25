<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'scan-success', text: string): void
  (e: 'manual-input'): void
}>()

const facingMode = ref<'environment' | 'user'>('environment')
const torchOn = ref(false)
const torchAvailable = ref(false)
const cameraAllowed = ref(false)
const cameraStarting = ref(true)
const cameraDenied = ref(false)
let html5QrCode: Html5Qrcode | null = null

onMounted(() => {
  html5QrCode = new Html5Qrcode('qr-reader')
  startCamera()
})

onUnmounted(() => {
  releaseCamera()
})

function getVideoTrack(): MediaStreamTrack | null {
  const video = document.querySelector('#qr-reader video') as HTMLVideoElement | null
  if (!video?.srcObject) return null
  return (video.srcObject as MediaStream).getVideoTracks()[0] ?? null
}

async function startCamera() {
  cameraStarting.value = true
  cameraDenied.value = false
  torchOn.value = false
  torchAvailable.value = false
  try {
    if (html5QrCode!.isScanning) {
      await html5QrCode!.stop()
    }
    await html5QrCode!.start(
      { facingMode: facingMode.value },
      {
        fps: 10,
        qrbox: { width: 250, height: 250 },
        aspectRatio: 1,
      },
      (decodedText: string) => {
        if (navigator.vibrate) navigator.vibrate(50)
        releaseCamera()
        emit('scan-success', decodedText)
      },
      () => {},
    )
    cameraAllowed.value = true
    detectTorchSupport()
  } catch {
    cameraDenied.value = true
  } finally {
    cameraStarting.value = false
  }
}

function detectTorchSupport() {
  const track = getVideoTrack()
  if (!track) return
  try {
    const caps = track.getCapabilities?.()
    torchAvailable.value = !!(caps as any)?.torch
  } catch {
    torchAvailable.value = false
  }
}

async function releaseCamera() {
  if (torchOn.value) {
    await setTorch(false)
  }
  if (html5QrCode?.isScanning) {
    try { await html5QrCode.stop() } catch { /* ignore */ }
  }
}

async function switchCamera() {
  facingMode.value = facingMode.value === 'environment' ? 'user' : 'environment'
  await startCamera()
}

async function toggleTorch() {
  await setTorch(!torchOn.value)
}

async function setTorch(on: boolean) {
  const track = getVideoTrack()
  if (!track) return
  try {
    await track.applyConstraints({
      advanced: [{ torch: on } as any],
    })
    torchOn.value = on
  } catch {
    torchAvailable.value = false
    torchOn.value = false
  }
}
</script>

<template>
  <div class="scanner-overlay">
    <div class="scanner-header">
      <van-icon name="arrow-left" size="24" color="#fff" @click="emit('close')" />
      <span class="scanner-title">扫一扫溯源码</span>
      <span class="scanner-placeholder"></span>
    </div>

    <div class="scanner-body">
      <div id="qr-reader"></div>

      <div v-if="cameraAllowed" class="scan-mask">
        <div class="scan-window">
          <div class="corner top-left"></div>
          <div class="corner top-right"></div>
          <div class="corner bottom-left"></div>
          <div class="corner bottom-right"></div>
          <div class="scan-line"></div>
        </div>
      </div>
      <p v-if="cameraAllowed" class="scan-tip">将二维码放入框内即可自动扫描</p>

      <div v-if="cameraDenied" class="camera-error">
        <van-icon name="warning-o" size="48" color="#ff976a" />
        <p class="error-title">无法访问相机</p>
        <p class="error-detail">请在浏览器设置中允许相机权限后重试</p>
        <van-button round type="primary" size="small" class="retry-btn" @click="startCamera">
          重新尝试
        </van-button>
      </div>

      <div v-if="cameraStarting && !cameraDenied" class="camera-loading">
        <van-loading type="spinner" color="#07c160" size="32" />
        <p>正在开启相机...</p>
      </div>
    </div>

    <div v-if="cameraAllowed" class="scanner-toolbar">
      <div v-if="torchAvailable" class="toolbar-item" @click="toggleTorch">
        <van-icon :name="torchOn ? 'bulb-o' : 'bulb-o'" :color="torchOn ? '#07c160' : '#fff'" size="22" />
        <span :class="{ active: torchOn }">{{ torchOn ? '已开启' : '手电筒' }}</span>
      </div>
      <div class="toolbar-item" @click="switchCamera">
        <van-icon name="replay" color="#fff" size="22" />
        <span>翻转镜头</span>
      </div>
      <div class="toolbar-item" @click="emit('manual-input')">
        <van-icon name="edit" color="#fff" size="22" />
        <span>手动输入</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.scanner-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: #000;
  display: flex;
  flex-direction: column;
}

.scanner-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  padding-top: env(safe-area-inset-top, 14px);
  background: rgba(0, 0, 0, 0.6);
  z-index: 10;
}

.scanner-title {
  font-size: 17px;
  color: #fff;
  font-weight: 500;
}

.scanner-placeholder {
  width: 24px;
}

.scanner-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

#qr-reader {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

#qr-reader :deep(video) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

#qr-reader :deep(#qr-shaded-region) {
  border-width: 0 !important;
}

.scan-mask {
  position: absolute;
  inset: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.scan-window {
  width: 250px;
  height: 250px;
  position: relative;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.55);
}

.scan-line {
  position: absolute;
  left: 8px;
  right: 8px;
  top: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #07c160, transparent);
  box-shadow: 0 0 8px 2px rgba(7, 193, 96, 0.4);
  animation: qr-scan-line 2.2s ease-in-out infinite;
}

@keyframes qr-scan-line {
  0%   { top: 4px; }
  50%  { top: calc(100% - 6px); }
  100% { top: 4px; }
}

.corner {
  position: absolute;
  width: 28px;
  height: 28px;
  border-color: #07c160;
  border-style: solid;
}
.corner::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: inherit;
  border-color: rgba(7, 193, 96, 0.25);
  border-style: solid;
  border-width: inherit;
}

.top-left     { top: 0;  left: 0;   border-width: 3px 0 0 3px; border-radius: 6px 0 0 0; }
.top-right    { top: 0;  right: 0;  border-width: 3px 3px 0 0; border-radius: 0 6px 0 0; }
.bottom-left  { bottom: 0; left: 0;  border-width: 0 0 3px 3px; border-radius: 0 0 0 6px; }
.bottom-right { bottom: 0; right: 0; border-width: 0 3px 3px 0; border-radius: 0 0 6px 0; }

.scan-tip {
  position: absolute;
  bottom: 80px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  text-align: center;
  padding: 8px 20px;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 20px;
  z-index: 6;
  letter-spacing: 1px;
}

/* error */
.camera-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #fff;
  z-index: 10;
  text-align: center;
  padding: 24px;
}
.camera-error .error-title {
  font-size: 17px;
  font-weight: 500;
}
.camera-error .error-detail {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  max-width: 260px;
  line-height: 1.5;
}
.camera-error .retry-btn {
  margin-top: 8px;
}

/* loading */
.camera-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #fff;
  z-index: 10;
  font-size: 14px;
}

/* bottom toolbar */
.scanner-toolbar {
  display: flex;
  justify-content: space-around;
  align-items: flex-start;
  padding: 16px 24px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom, 16px));
  background: rgba(0, 0, 0, 0.6);
  z-index: 10;
}

.toolbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.toolbar-item span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.toolbar-item span.active {
  color: #07c160;
}
</style>
