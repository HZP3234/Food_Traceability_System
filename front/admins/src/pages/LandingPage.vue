<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import videoUrl from '../assets/traceability-journey.mp4';

const emit = defineEmits(['enter-admin']);//

const videoEl = ref(null);
const activeStage = ref(0);
const videoReady = ref(false);
const detailVisible = ref(false);

// These nodes match the actual generated video: factory -> laboratory -> cold storage -> transport -> retail / scan.
const stages = [
  { label: '源头种植', title: ['从源头开始，', '记录每一份原料'], body: '种植地、供应主体与原料批次，从进入链路的第一刻起建立可信档案。' },
  { label: '加工建批', title: ['加工过程，', '形成可追溯批次'], body: '清洗、分拣、加工与入库动作被绑定到同一生产批次，过程可回看。', chips: ['原料入库', '加工建批', '成品入库'] },
  { label: '质检留样', title: ['合格之前，', '先有检验凭证'], body: '检测报告、留样记录与批次 Hash 共同构成可验证的质量证据。', verify: true },
  { label: '冷链入库', title: ['进入冷库，', '温度持续在线'], body: '仓储环境、入库时间与设备状态连续归档，确保货物进入受控区间。', cold: true },
  { label: '冷链配送', title: ['装车出库，', '链路继续向前'], body: '车辆、装卸、运输与交接节点被持续记录，冷链过程不留盲区。' },
  { label: '终端验真', title: ['抵达终端，', '一键验证全程'], body: '门店、货架与消费者扫码共同完成食品可信旅程的最后一段。', scan: true },
];

const stage = computed(() => stages[activeStage.value]);
let scrollFrame = 0;
let videoFrame = 0;
let scrollAnimation = 0;
let targetTime = 0;
let lastReverseSeekTarget = -1;

function storyMetrics() {
  const shell = document.querySelector('.story-shell');
  if (!shell) return null;
  const start = shell.offsetTop;
  const end = start + Math.max(1, shell.offsetHeight - window.innerHeight);
  return { shell, start, end, range: end - start };
}
function nodeProgress(index) { return index / (stages.length - 1); }
function setVideoProgress(progress) {
  const safeProgress = Math.min(1, Math.max(0, progress));
  const nextStage = Math.min(stages.length - 1, Math.round(safeProgress * (stages.length - 1)));
  if (nextStage !== activeStage.value) activeStage.value = nextStage;
  document.documentElement.style.setProperty('--scroll-progress', safeProgress.toFixed(4));
  const video = videoEl.value;
  if (!videoReady.value || !video?.duration) return;
  targetTime = Math.min(video.duration - 0.035, Math.max(0, video.duration * safeProgress));
}
function animateScrollTo(target, duration) {
  if (scrollAnimation) window.cancelAnimationFrame(scrollAnimation);
  const initial = window.scrollY;
  const distance = target - initial;
  const startedAt = performance.now();
  const tick = (now) => {
    const progress = Math.min(1, (now - startedAt) / duration);
    const eased = 1 - (1 - progress) ** 3;
    window.scrollTo(0, initial + distance * eased);
    if (progress < 1) scrollAnimation = window.requestAnimationFrame(tick);
    else scrollAnimation = 0;
  };
  scrollAnimation = window.requestAnimationFrame(tick);
}
function goToNode(index, speed = 1) {
  const metrics = storyMetrics();
  if (!metrics) return;
  const safeIndex = Math.min(stages.length - 1, Math.max(0, index));
  const duration = Math.max(360, Math.min(980, 820 / speed));
  animateScrollTo(metrics.start + metrics.range * nodeProgress(safeIndex), duration);
}
function resetJourney() {
  const metrics = storyMetrics();
  if (!metrics) return;
  activeStage.value = 0;
  targetTime = 0;
  lastReverseSeekTarget = -1;
  const video = videoEl.value;
  if (video) { video.pause(); if (typeof video.fastSeek === 'function') video.fastSeek(0); else video.currentTime = 0; }
  animateScrollTo(metrics.start, 460);
}
function handleWheel(event) {
  const metrics = storyMetrics();
  if (!metrics || detailVisible.value) return;
  const insideJourney = window.scrollY >= metrics.start - 2 && window.scrollY <= metrics.end + 2;
  if (!insideJourney || !event.cancelable) return;
  event.preventDefault();
  if (scrollAnimation) return;
  const speed = Math.max(.7, Math.min(2.1, Math.abs(event.deltaY) / 80));
  if (event.deltaY < 0) {
    if (activeStage.value > 0) goToNode(activeStage.value - 1, speed);
    return;
  }
  if (activeStage.value < stages.length - 1) {
    goToNode(activeStage.value + 1, speed);
  } else {
    animateScrollTo(metrics.end + window.innerHeight * .32, 620);
  }
}
function syncScene() {
  scrollFrame = 0;
  const metrics = storyMetrics();
  if (!metrics) return;
  detailVisible.value = window.scrollY > metrics.end + 8;
  if (!detailVisible.value) setVideoProgress((window.scrollY - metrics.start) / metrics.range);
}
function requestSync() {
  if (scrollFrame) return;
  scrollFrame = window.requestAnimationFrame(syncScene);
}
function driveVideo() {
  const video = videoEl.value;
  if (videoReady.value && video?.duration && !detailVisible.value) {
    const difference = targetTime - video.currentTime;
    if (difference > .045) {
      lastReverseSeekTarget = -1;
      video.playbackRate = Math.min(3.2, Math.max(1, 1 + difference * 2));
      if (video.paused) video.play().catch(() => {});
    } else if (difference < -.045) {
      if (!video.paused) video.pause();
      video.playbackRate = 1;
      if (!video.seeking && Math.abs(targetTime - lastReverseSeekTarget) > .08) {
        if (typeof video.fastSeek === 'function') video.fastSeek(targetTime); else video.currentTime = targetTime;
        lastReverseSeekTarget = targetTime;
      }
    } else if (!video.paused) {
      video.pause(); video.playbackRate = 1;
    }
  }
  videoFrame = window.requestAnimationFrame(driveVideo);
}
function onVideoReady() { videoReady.value = true; videoEl.value?.pause(); requestSync(); if (!videoFrame) driveVideo(); }
function enterAdmin() {
  emit('enter-admin');
}

onMounted(() => {
  window.addEventListener('scroll', requestSync, { passive: true });
  window.addEventListener('resize', requestSync);
  window.addEventListener('wheel', handleWheel, { passive: false });
  requestSync();
});
onUnmounted(() => {
  window.removeEventListener('scroll', requestSync);
  window.removeEventListener('resize', requestSync);
  window.removeEventListener('wheel', handleWheel);
  if (scrollFrame) window.cancelAnimationFrame(scrollFrame);
  if (videoFrame) window.cancelAnimationFrame(videoFrame);
  if (scrollAnimation) window.cancelAnimationFrame(scrollAnimation);
});
</script>

<template>
  <header class="topbar"><a class="brand" href="#top" @click.prevent="resetJourney"><span>溯</span>食安溯源</a><nav aria-label="主导航"><button @click="resetJourney">全链旅程</button><button @click="goToNode(2)">可信证据</button><button @click="goToNode(4)">冷链配送</button></nav><button class="nav-action" @click="enterAdmin">进入管理后台</button></header>

  <main id="top" class="story-shell" :class="{ 'story-complete': detailVisible }">
    <div class="video-layer" aria-hidden="true"><video ref="videoEl" :src="videoUrl" muted playsinline preload="auto" @loadedmetadata="onVideoReady"></video><div class="video-vignette"></div></div>
    <section class="story-panel" :class="[`stage-${activeStage}`]">
      <p class="eyebrow">{{ String(activeStage + 1).padStart(2, '0') }} / {{ stage.label }}</p>
      <h1>{{ stage.title[0] }}<br><em>{{ stage.title[1] }}</em></h1><p class="body-copy">{{ stage.body }}</p>
      <button v-if="stage.scan" class="primary" @click="enterAdmin">进入管理后台 <b>↗</b></button>
      <div v-if="stage.chips" class="chips"><template v-for="(chip, index) in stage.chips" :key="chip"><span>{{ chip }}</span><i v-if="index < stage.chips.length - 1"></i></template></div>
      <div v-if="stage.verify" class="verify"><b>✓</b><div><small>HASH VERIFICATION</small><strong>批次校验通过</strong></div><code>91FA...6C2E</code></div>
      <div v-if="stage.cold" class="temperature"><strong>-1.8<small>°C</small></strong><span>仓储温度 · 正常区间</span><i></i></div>
    </section>
    <p class="scroll-tip">向下滚动，进入下一个可信节点 <i></i></p>
    <div class="progress-nav" aria-label="可信旅程节点"><div v-for="(item, index) in stages" :key="item.label" :class="{ active: activeStage === index }"><b>{{ String(index + 1).padStart(2, '0') }}</b><span>{{ item.label }}</span></div></div>
  </main>

  <section id="system-detail" class="system-detail">
    <div class="detail-intro"><p class="detail-kicker">FOOD TRACEABILITY SYSTEM</p><h2>让一条食品链，<em>成为可验证的数据链</em></h2><p>从生产主体到消费者扫码，系统以批次为核心串联原料、加工、质检、冷链、终端与异常处置，让每一次流转都留下可追溯的证据。</p></div>
    <div class="detail-flow"><article><b>01</b><h3>一物一码</h3><p>为产品和批次生成唯一溯源码，承载产地、生产、质检与流转记录。</p></article><article><b>02</b><h3>全程留痕</h3><p>通过节点采集与批次关联，沉淀可查询、可核验、可审计的过程数据。</p></article><article><b>03</b><h3>风险闭环</h3><p>发现异常时按批次快速定位影响范围，形成预警、处置、召回与复盘闭环。</p></article></div>
    <div class="detail-band"><div><p class="detail-kicker">SYSTEM CAPABILITY</p><h2>可信不是一句口号，<br>而是每个节点都经得起回看。</h2></div><dl><div><dt>生产主体</dt><dd>资质、品类、批次建档</dd></div><div><dt>质量检测</dt><dd>报告、留样、校验留痕</dd></div><div><dt>冷链物流</dt><dd>仓储、车辆、温度监测</dd></div><div><dt>消费者查询</dt><dd>扫码验真、链路可视</dd></div></dl></div>
    <footer class="detail-footer"><span>食安溯源系统</span><button @click="resetJourney">重新观看可信旅程 ↑</button></footer>
  </section>

</template>

<style scoped src="../styles/landing.css"></style>
