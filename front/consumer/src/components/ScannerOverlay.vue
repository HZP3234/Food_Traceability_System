<script setup lang="ts">
const emit = defineEmits<{
  (e: 'close'): void
}>()
</script>

<template>
  <div class="scanner-overlay">
    <div class="scanner-header">
      <van-icon name="arrow-left" size="24" color="#fff" @click="emit('close')" />
      <span class="scanner-title">扫一扫溯源码</span>
      <span class="scanner-placeholder"></span>
    </div>

    <div class="scanner-body">
      <div class="scan-frame">
        <div class="corner top-left"></div>
        <div class="corner top-right"></div>
        <div class="corner bottom-left"></div>
        <div class="corner bottom-right"></div>
      </div>
      <p class="scan-tip">将二维码放入框内即可自动扫描</p>
    </div>

    <div class="scanner-footer">
      <div class="footer-actions">
        <van-button round block>
          <van-icon name="photo-o" />
          拍照扫码
        </van-button>
        <van-button round block plain class="manual-btn">
          手动输入溯源码
        </van-button>
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
  z-index: 1;
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

.scan-frame {
  position: absolute;
  width: 250px;
  height: 250px;
  pointer-events: none;
}

.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border-color: #07c160;
  border-style: solid;
}

.top-left { top: 0; left: 0; border-width: 3px 0 0 3px; border-radius: 4px 0 0 0; }
.top-right { top: 0; right: 0; border-width: 3px 3px 0 0; border-radius: 0 4px 0 0; }
.bottom-left { bottom: 0; left: 0; border-width: 0 0 3px 3px; border-radius: 0 0 0 4px; }
.bottom-right { bottom: 0; right: 0; border-width: 0 3px 3px 0; border-radius: 0 0 4px 0; }

.scan-tip {
  position: absolute;
  bottom: 120px;
  color: #fff;
  font-size: 14px;
  text-align: center;
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 20px;
}

.scanner-footer {
  padding: 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom, 0));
  background: rgba(0, 0, 0, 0.6);
  z-index: 1;
}

.footer-actions {
  max-width: 280px;
  margin: 0 auto;
}

.manual-btn {
  margin-top: 12px;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
}
</style>
