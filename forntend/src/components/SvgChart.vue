<template>
  <div class="svg-chart">
    <div class="chart-legend">
      <span v-for="s in series" :key="s.name" class="legend-item">
        <i :style="{ background: s.color }"></i>{{ s.name }}
      </span>
    </div>
    <svg :viewBox="`0 0 ${W} ${H}`" class="chart-svg" @mousemove="onMove" @mouseleave="hoverIdx = null">
      <!-- 网格线与 Y 轴刻度 -->
      <g v-for="i in 5" :key="`g${i}`">
        <line
          :x1="pad.l" :x2="W - pad.r" :y1="gridY(i - 1)" :y2="gridY(i - 1)"
          :stroke="i === 1 ? '#e5e6eb' : '#f2f3f5'" stroke-width="1"
        />
        <text :x="pad.l - 8" :y="gridY(i - 1) + 3.5" text-anchor="end" class="axis-text">
          {{ fmtAxis((i - 1) * step) }}
        </text>
      </g>
      <!-- X 轴标签（抽稀） -->
      <text
        v-for="(lb, i) in labels" v-show="showX(i)" :key="`x${i}`"
        :x="xAt(i)" :y="H - 8" text-anchor="middle" class="axis-text"
      >{{ lb }}</text>
      <!-- 柱状系列（底层） -->
      <g v-for="s in barSeries" :key="`bar-${s.name}`">
        <rect
          v-for="(v, i) in s.data" :key="i" v-show="v > 0"
          :x="xAt(i) - barW / 2" :y="yAt(v)" :width="barW" :height="baseY - yAt(v)"
          :fill="s.color" opacity="0.85" rx="2"
        />
      </g>
      <!-- 面积系列（线 + 渐变面） -->
      <g v-for="s in areaSeries" :key="`area-${s.name}`">
        <path :d="areaPath(s.data)" :fill="s.color" opacity="0.12" />
        <polyline :points="linePoints(s.data)" fill="none" :stroke="s.color" stroke-width="2" stroke-linejoin="round" stroke-linecap="round" />
      </g>
      <!-- 折线系列 -->
      <g v-for="s in lineSeries" :key="`line-${s.name}`">
        <polyline :points="linePoints(s.data)" fill="none" :stroke="s.color" stroke-width="2" stroke-linejoin="round" stroke-linecap="round" />
      </g>
      <!-- 悬停参考线与数据点 -->
      <g v-if="hoverIdx !== null">
        <line :x1="xAt(hoverIdx)" :x2="xAt(hoverIdx)" :y1="pad.t" :y2="baseY" stroke="#c9cdd4" stroke-dasharray="3 3" />
        <circle
          v-for="s in pointSeries" :key="s.name"
          :cx="xAt(hoverIdx)" :cy="yAt(s.data[hoverIdx] || 0)" r="3.5"
          :fill="s.color" stroke="#fff" stroke-width="1.5"
        />
      </g>
    </svg>
    <div v-if="hoverIdx !== null" class="chart-tip" :style="tipStyle">
      <strong>{{ labels[hoverIdx] }}</strong>
      <div v-for="s in series" :key="s.name" class="tip-row">
        <i :style="{ background: s.color }"></i>
        <em>{{ s.name }}</em>
        <b>{{ (s.data[hoverIdx] ?? 0).toLocaleString() }}</b>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  labels: { type: Array, default: () => [] },
  // [{ name, color, data: number[], type: 'line' | 'area' | 'bar' }]
  series: { type: Array, default: () => [] },
  height: { type: Number, default: 240 }
})

// 逻辑画布尺寸，viewBox 等比缩放到容器宽度
const W = 720
const H = computed(() => props.height)
const pad = { l: 46, r: 14, t: 16, b: 26 }
const plotW = W - pad.l - pad.r
const baseY = computed(() => H.value - pad.b)
const plotH = computed(() => baseY.value - pad.t)

const n = computed(() => props.labels.length)
const maxVal = computed(() => {
  let m = 0
  for (const s of props.series) for (const v of s.data || []) if (v > m) m = v
  return m
})
// Y 轴取 4 段，刻度取 1/2/5×10^k 的整值
const step = computed(() => niceStep(maxVal.value / 4))
const yMax = computed(() => step.value * 4 || 1)

function niceStep(raw) {
  if (raw <= 0) return 1
  const pow = 10 ** Math.floor(Math.log10(raw))
  const r = raw / pow
  return (r <= 1 ? 1 : r <= 2 ? 2 : r <= 5 ? 5 : 10) * pow
}

// 所有系列统一按「槽位中心」定位，折线/柱状/悬停共用一套坐标
function xAt(i) {
  return n.value ? pad.l + ((i + 0.5) * plotW) / n.value : pad.l
}
function yAt(v) {
  return pad.t + (1 - v / yMax.value) * plotH.value
}
function gridY(i) {
  return pad.t + (1 - i / 4) * plotH.value
}
const barW = computed(() => Math.min(24, (plotW / Math.max(n.value, 1)) * 0.55))

const linePoints = (data) =>
  data.map((v, i) => `${xAt(i).toFixed(1)},${yAt(v).toFixed(1)}`).join(' ')

const areaPath = (data) => {
  if (!data.length) return ''
  const pts = data.map((v, i) => `${xAt(i).toFixed(1)},${yAt(v).toFixed(1)}`).join(' L ')
  return `M ${xAt(0).toFixed(1)},${baseY.value} L ${pts} L ${xAt(data.length - 1).toFixed(1)},${baseY.value} Z`
}

const barSeries = computed(() => props.series.filter((s) => s.type === 'bar'))
const areaSeries = computed(() => props.series.filter((s) => s.type === 'area'))
const lineSeries = computed(() => props.series.filter((s) => s.type === 'line' || !s.type))
const pointSeries = computed(() => props.series.filter((s) => s.type !== 'bar'))

// X 轴标签抽稀：最多约 9 个
function showX(i) {
  const stride = Math.ceil(n.value / 9)
  return i % stride === 0 || i === n.value - 1
}

// ===== 悬停交互：定位最近的数据槽位 =====
const hoverIdx = ref(null)
function onMove(e) {
  if (!n.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  const vx = ((e.clientX - rect.left) / rect.width) * W
  const idx = Math.round(((vx - pad.l) / plotW) * n.value - 0.5)
  hoverIdx.value = Math.min(n.value - 1, Math.max(0, idx))
}
const tipStyle = computed(() => {
  const left = (xAt(hoverIdx.value ?? 0) / W) * 100
  return {
    left: `${left}%`,
    transform: left > 60 ? 'translateX(calc(-100% - 12px))' : 'translateX(12px)'
  }
})
function fmtAxis(v) {
  if (v >= 10000) return `${(v / 10000).toFixed(v % 10000 ? 1 : 0)}万`
  if (v >= 1000) return `${(v / 1000).toFixed(v % 1000 ? 1 : 0)}k`
  return v
}
</script>

<style scoped>
.svg-chart {
  position: relative;
}

.chart-legend {
  display: flex;
  gap: 16px;
  margin-bottom: 4px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #4e5969;
}

.legend-item i {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.chart-svg {
  display: block;
  width: 100%;
  height: auto;
}

.axis-text {
  font-family: var(--font-num);
  font-size: 11px;
  fill: #86909c;
}

.chart-tip {
  position: absolute;
  top: 26px;
  z-index: 2;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 120px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(31, 35, 41, 0.92);
  color: #fff;
  font-size: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
}

.chart-tip strong {
  color: #f7f8fa;
}

.tip-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tip-row i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.tip-row em {
  flex: 1;
  font-style: normal;
  color: #c9cdd4;
}

.tip-row b {
  font-variant-numeric: tabular-nums;
}
</style>
