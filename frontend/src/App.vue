<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const domain = ref('')
const result = ref(null)
const loading = ref(false)
const errorMsg = ref('')
const activeTab = ref('dns')

const TABS = [
  { key: 'dns', label: 'DNS' },
  { key: 'ip', label: 'IP' },
  { key: 'ssl', label: 'SSL' },
  { key: 'whois', label: 'WHOIS' },
]

const whoisSummary = computed(() => {
  if (!result.value?.whois?.rawData) return null
  try {
    const data = JSON.parse(result.value.whois.rawData)
    const registrarEntity = data.entities?.find(e => e.roles?.includes('registrar'))
    const registrarName = registrarEntity?.vcardArray?.[1]?.find(item => item[0] === 'fn')?.[3] || 'Unknown'
    const registrationEvent = data.events?.find(e => e.eventAction === 'registration')
    const expirationEvent = data.events?.find(e => e.eventAction === 'expiration')
    const nameservers = data.nameservers?.map(ns => ns.ldhName) || []
    return {
      registrar: registrarName,
      registrationDate: registrationEvent?.eventDate?.split('T')[0] || 'Unknown',
      expirationDate: expirationEvent?.eventDate?.split('T')[0] || 'Unknown',
      nameservers
    }
  } catch (e) {
    return null
  }
})

const handleScan = async () => {
  if (!domain.value.trim()) {
    errorMsg.value = 'Enter a domain to scan.'
    return
  }
  loading.value = true
  errorMsg.value = ''
  result.value = null
  try {
    const response = await fetch(`http://localhost:8080/scan?domain=${encodeURIComponent(domain.value)}`)
    if (!response.ok) throw new Error(`Server returned status ${response.status}`)
    result.value = await response.json()
    activeTab.value = 'dns'
  } catch (err) {
    errorMsg.value = 'Scan failed: ' + err.message
  } finally {
    loading.value = false
  }
}

/* ---------- batch mode ---------- */
const batchMode = ref(false)
const batchInput = ref('')
const batchResults = ref([])
const batchLoading = ref(false)

const handleBatchScan = async () => {
  const domains = batchInput.value
      .split('\n')
      .map(d => d.trim())
      .filter(d => d.length > 0)

  if (domains.length === 0) {
    errorMsg.value = 'Enter at least one domain (one per line).'
    return
  }

  batchLoading.value = true
  errorMsg.value = ''
  batchResults.value = []

  try {
    const response = await fetch('http://localhost:8080/scan/batch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ domains })
    })
    if (!response.ok) throw new Error(`Server returned status ${response.status}`)
    const data = await response.json()
    batchResults.value = data.results
  } catch (err) {
    errorMsg.value = 'Batch scan failed: ' + err.message
  } finally {
    batchLoading.value = false
  }
}

/* ---------- save to log ---------- */
const savingLog = ref(false)
const savedMsg = ref('')

const handleSaveLog = async () => {
  if (!result.value) return
  savingLog.value = true
  savedMsg.value = ''
  try {
    const response = await fetch('http://localhost:8080/scan/log', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(result.value)
    })
    if (!response.ok) throw new Error(`Server returned status ${response.status}`)
    savedMsg.value = 'Saved ✓'
  } catch (err) {
    savedMsg.value = 'Save failed'
  } finally {
    savingLog.value = false
    setTimeout(() => { savedMsg.value = '' }, 2000)
  }
}

/* ---------- ghost sprite: 2-frame idle bitmap ---------- */
const W = '#8080b8'
const Wd = '#5858a0'
const Ey = '#080814'
const _ = null
const GHOST = [
  [
    [_, _, W, W, W, W, _, _],
    [_, W, W, W, W, W, W, _],
    [W, W, W, W, W, W, W, W],
    [W, W, Ey, W, W, Ey, W, W],
    [W, W, W, W, W, W, W, W],
    [W, W, W, W, W, W, W, W],
    [W, W, W, W, W, W, W, W],
    [W, _, W, _, W, _, W, _],
  ],
  [
    [_, _, W, W, W, W, _, _],
    [_, W, W, W, W, W, W, _],
    [W, W, W, W, W, W, W, W],
    [W, W, Ey, W, W, Ey, W, W],
    [W, W, W, W, W, W, W, W],
    [W, W, W, W, W, W, W, W],
    [W, W, W, W, W, W, W, W],
    [_, W, _, W, _, W, _, W],
  ],
]
const ghostFrame = ref(0)
let ghostTimer = null

/* ---------- decorative sky (fixed positions) ---------- */
const STARS = [
  [6, 6, 2, 0.0], [14, 3, 1, 1.2], [28, 9, 3, 0.5], [43, 3, 1, 2.1],
  [56, 8, 2, 0.9], [70, 4, 1, 1.7], [84, 10, 3, 0.2], [93, 3, 2, 2.7],
  [4, 21, 1, 1.5], [20, 27, 2, 0.6], [38, 16, 1, 2.4], [62, 22, 3, 1.0],
  [79, 29, 1, 1.9], [91, 18, 2, 0.4], [9, 38, 2, 2.9], [51, 33, 1, 1.4],
  [87, 42, 2, 0.7], [74, 51, 1, 2.3], [33, 46, 1, 1.1], [47, 57, 2, 3.1],
]
const PLANETS = [
  [83, 12, 22, '#483478', '#6048a0'],
  [11, 64, 15, '#784030', '#a05040'],
  [71, 73, 11, '#326468', '#4888a0'],
  [37, 82, 8, '#585868', '#7878a0'],
]

function starStyle([x, y, sz, delay]) {
  const arm = sz * 3
  const col = sz === 3 ? '#d0d0ff' : sz === 2 ? '#8080b8' : '#404060'
  const dur = `${2.6 + delay * 0.55}s`
  return {
    left: x + '%', top: y + '%', width: arm + 'px', height: arm + 'px',
    animation: `twinkle ${dur} ${delay}s ease-in-out infinite`,
    '--bar': sz + 'px', '--col': col,
  }
}
function planetStyle([x, y, d, fill, border]) {
  return {
    left: x + '%', top: y + '%', width: d + 'px', height: d + 'px',
    background: `radial-gradient(circle at 36% 32%, ${fill}ff 0%, ${fill}99 55%, ${fill}44 100%)`,
    border: `2px solid ${border}`,
    boxShadow: `0 0 ${Math.round(d * 0.9)}px ${fill}28`,
  }
}

onMounted(() => {
  ghostTimer = setInterval(() => { ghostFrame.value = (ghostFrame.value + 1) % 2 }, 480)
})
onUnmounted(() => clearInterval(ghostTimer))

const bgDim = computed(() => loading.value || !!result.value)

const batchSavingIndex = ref(null)
const batchSavedIndex = ref(null)

const handleBatchSaveLog = async (item, index) => {
  batchSavingIndex.value = index
  try {
    const response = await fetch('http://localhost:8080/scan/log', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(item)
    })
    if (!response.ok) throw new Error(`Server returned status ${response.status}`)
    batchSavedIndex.value = index
    setTimeout(() => { batchSavedIndex.value = null }, 2000)
  } catch (err) {
    console.error('Batch save failed:', err)
  } finally {
    batchSavingIndex.value = null
  }
}
</script>

<template>
  <div class="page">
    <!-- Background layer -->
    <div class="sky" :class="{ dim: bgDim }">
      <div v-for="(s, i) in STARS" :key="'s' + i" class="pixel-star" :style="starStyle(s)">
        <span class="bar-h"></span><span class="bar-v"></span>
      </div>
      <div v-for="(p, i) in PLANETS" :key="'p' + i" class="pixel-planet" :style="planetStyle(p)"></div>

      <div class="moon">
        <div class="moon-glow"></div>
        <div class="moon-body"></div>
        <div class="moon-outline"></div>
      </div>

      <div class="ghost-wrap">
        <div class="ghost">
          <div v-for="(row, r) in GHOST[ghostFrame]" :key="r" class="ghost-row">
            <div v-for="(c, ci) in row" :key="ci" class="ghost-px" :style="{ background: c || 'transparent' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Foreground content -->
    <div class="content">
      <header class="header">
        <h1 class="title">CrockTail</h1>
        <p class="subtitle">Passive OSINT Domain Reconnaissance Tool</p>
      </header>

      <div class="mode-toggle">
        <button class="px-btn-sm" :class="{ active: !batchMode }" @click="batchMode = false">SINGLE</button>
        <button class="px-btn-sm" :class="{ active: batchMode }" @click="batchMode = true">BATCH</button>
      </div>

      <div v-if="!batchMode" class="search-container">
        <div class="search-box">
          <span class="prompt-symbol">&gt;</span>
          <input
              v-model="domain"
              type="text"
              class="search-input"
              placeholder="Enter Domain..."
              spellcheck="false"
              @keyup.enter="handleScan"
          />
        </div>
        <button class="px-btn" @click="handleScan" :disabled="loading">
          {{ loading ? '...' : 'SCAN' }}
        </button>
      </div>

      <div v-else class="batch-container">
  <textarea
      v-model="batchInput"
      class="batch-input"
      placeholder="example.com&#10;google.com&#10;one per line..."
      spellcheck="false"
  ></textarea>
        <button class="px-btn" @click="handleBatchScan" :disabled="batchLoading">
          {{ batchLoading ? '...' : 'SCAN ALL' }}
        </button>
      </div>

      <p v-if="errorMsg" class="error-line">✕ {{ errorMsg }}</p>
      <p v-if="loading" class="scanning-line">SCANNING...</p>

      <div v-if="result" class="panel">
        <div class="panel-titlebar">
          <div class="corner-dots"><span></span><span></span><span></span></div>
          <span class="panel-label">RESULTS //</span>
          <span class="panel-target">{{ result.domain }}</span>
        </div>

        <div class="tab-bar">
          <button
              v-for="t in TABS" :key="t.key"
              class="tab-btn" :class="{ active: activeTab === t.key }"
              @click="activeTab = t.key"
          >{{ t.label }}</button>
        </div>

        <div class="tab-content">
          <template v-if="activeTab === 'dns'">
            <div class="kv"><span>A</span><code>{{ result.dns.a.join(', ') || '—' }}</code></div>
            <div class="kv"><span>AAAA</span><code>{{ result.dns.aaaa.join(', ') || '—' }}</code></div>
            <div class="kv"><span>MX</span><code>{{ result.dns.mx.join(', ') || '—' }}</code></div>
            <div class="kv"><span>NS</span><code>{{ result.dns.ns.join(', ') || '—' }}</code></div>
            <div class="kv"><span>TXT</span><code class="wrap">{{ result.dns.txt.join('  |  ') || '—' }}</code></div>
            <div class="kv"><span>Source</span><code>{{ result.dns.source }}</code></div>

          </template>

          <template v-if="activeTab === 'ip'">
            <p v-if="result.ipInfo?.suspectedIntercepted" class="warn-line">
              ⚠ RESERVED/TEST RANGE — LIKELY DNS INTERCEPTION
            </p>
            <div class="kv"><span></span><code>{{ result.ipInfo?.ip || '—' }}</code></div>
            <div class="kv"><span>Country</span><code>{{ result.ipInfo?.country || 'Unknown' }}</code></div>
            <div class="kv"><span>ISP</span><code>{{ result.ipInfo?.isp || 'Unknown' }}</code></div>
            <div class="kv"><span>ASN</span><code>{{ result.ipInfo?.asNumber || 'Unknown' }}</code></div>
            <div class="kv"><span>Source</span><code>{{ result.ipInfo?.source }}</code></div>
          </template>

          <template v-if="activeTab === 'ssl'">
            <p v-if="result.sslCert?.error" class="error-line">✕ {{ result.sslCert.error }}</p>
            <template v-else>
              <div class="kv"><span>Issuer</span><code>{{ result.sslCert?.issuer || '—' }}</code></div>
              <div class="kv"><span>Valid to</span><code>{{ result.sslCert?.validTo || '—' }}</code></div>
              <p class="sub-label">SAN ({{ result.sslCert?.sanDomains?.length || 0 }})</p>
              <ul class="san-list">
                <li v-for="san in result.sslCert?.sanDomains" :key="san">{{ san }}</li>
              </ul>
              <div class="kv"><span>Source</span><code>{{ result.sslCert?.source }}</code></div>
            </template>
          </template>

          <template v-if="activeTab === 'whois'">
            <div class="kv"><span>Source</span><code>{{ result.whois?.source }}</code></div>
            <template v-if="whoisSummary">
              <div class="kv"><span>Registrar</span><code>{{ whoisSummary.registrar }}</code></div>
              <div class="kv"><span>Registered</span><code>{{ whoisSummary.registrationDate }}</code></div>
              <div class="kv"><span>Expires</span><code>{{ whoisSummary.expirationDate }}</code></div>
              <div class="kv"><span>NS</span><code>{{ whoisSummary.nameservers.join(', ') || '—' }}</code></div>
            </template>
            <p v-else-if="result.whois?.error" class="error-line">✕ {{ result.whois.error }}</p>
            <p v-else class="sub-label">No WHOIS data available.</p>
          </template>
        </div>

        <div class="panel-footer">
          <span>crockTail v1.0 // passive // no active probing</span>
          <button class="log-btn" @click="handleSaveLog" :disabled="savingLog">
            {{ savedMsg || (savingLog ? 'SAVING...' : '↓ SAVE LOG') }}
          </button>
        </div>

      <p v-if="!loading && !result" class="idle-hint">enter a domain and press scan</p>
    </div>

    <div v-if="batchResults.length > 0" class="batch-results">
      <div class="batch-header">
        <span>DOMAIN</span><span>A RECORD</span><span>ISP</span><span>SSL ISSUER</span><span></span>
      </div>
      <div v-for="(r, index) in batchResults" :key="r.domain" class="batch-row">
        <span>{{ r.domain }}</span>
        <span>{{ r.dns?.a?.[0] || '—' }}</span>
        <span>{{ r.ipInfo?.isp || '—' }}</span>
        <span>{{ r.sslCert?.issuer || '—' }}</span>
        <button class="log-btn-sm" @click="handleBatchSaveLog(r, index)" :disabled="batchSavingIndex === index">
          {{ batchSavedIndex === index ? '✓' : (batchSavingIndex === index ? '...' : '↓') }}
        </button>
      </div>
    </div>
  </div>
  </div>
</template>

<style>
html, body {
  margin: 0; padding: 0;
  background-color: #080812;
  width: 100%; min-height: 100%;
  scrollbar-width: thin;
  scrollbar-color: #1e1e44 #08080e;
}
#app { margin: 0; padding: 0; }
::-webkit-scrollbar { width: 10px; }
::-webkit-scrollbar-track { background: #08080e; border-left: 2px solid #16163a; }
::-webkit-scrollbar-thumb { background: #1e1e44; border-left: 2px solid #2a2a5a; }
::-webkit-scrollbar-thumb:hover { background: #28285a; }
</style>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Press+Start+2P&family=VT323&display=swap');

* { image-rendering: pixelated; image-rendering: crisp-edges; box-sizing: border-box; }

.page {
  position: relative; width: 100%; min-height: 100vh;
  background: linear-gradient(172deg, #080812 0%, #0c0c1e 100%);
  color: #9898c8; overflow-x: hidden;
  display: flex; flex-direction: column; align-items: center;
  padding-bottom: 4rem;
}
.content { position: relative; z-index: 10; width: 100%; max-width: 640px; padding: 0 28px; display: flex; flex-direction: column; align-items: center; }

/* header/search kept as-is */
.header { margin-top: 70px; text-align: center; }
.title {
  font-family: 'Press Start 2P', monospace;
  font-size: 3rem; color: #fff9e6;
  text-shadow: 0 0 10px rgba(255, 249, 230, 0.5), 4px 4px 0px #000;
  letter-spacing: 2px; margin-bottom: 20px;
}
.subtitle { font-family: 'Press Start 2P', monospace; font-size: 0.65rem; color: #a0a5c0; letter-spacing: 1px; line-height: 1.6; }

.search-container {
  margin-top: 40px; display: flex; gap: 12px;
  background: #16192b; padding: 8px; border: 4px solid #3b4269;
  box-shadow: inset -4px -4px 0px #0e101d, inset 4px 4px 0px #2a2f4c, 0 8px 0px rgba(0,0,0,0.5);
}
.search-box { display: flex; align-items: center; background: #090a10; border: 2px solid #232740; padding: 0 16px; width: 380px; }
.prompt-symbol { color: #8c97ca; font-size: 0.8rem; margin-right: 12px; }
.search-input {
  background: transparent; border: none; outline: none; color: #c2c9ee;
  font-family: 'Press Start 2P', monospace; font-size: 0.75rem; width: 100%; padding: 14px 0;
}
.search-input::placeholder { color: #4a5173; }

/* pixel button (from new design) */
.px-btn {
  font-family: 'Press Start 2P', monospace;
  font-size: 0.75rem; letter-spacing: 1px;
  background: #2828a0; color: #a0a0f0;
  border: none; padding: 14px 24px;
  box-shadow: 3px 3px 0 #101054;
  cursor: pointer; outline: none; user-select: none;
  transition: background 0.08s;
}
.px-btn:hover:not(:disabled) { background: #3535b8; }
.px-btn:active:not(:disabled) { box-shadow: 1px 1px 0 #101054; transform: translate(2px, 2px); }
.px-btn:disabled { background: #141430; color: #242448; box-shadow: none; cursor: not-allowed; }

.error-line { margin-top: 1.5rem; color: #d06878; font-size: 0.65rem; font-family: 'Press Start 2P', monospace; }
.warn-line { color: #c89040; font-size: 0.6rem; margin-bottom: 0.6rem; font-family: 'Press Start 2P', monospace; }
.scanning-line { margin-top: 1.5rem; font-family: 'Press Start 2P', monospace; font-size: 0.65rem; color: #7272e0; animation: titlePulse 1.6s ease-in-out infinite; }
.idle-hint { font-family: 'VT323', monospace; font-size: 16px; color: #1e1e3c; letter-spacing: 3px; margin-top: 4px; }

/* ── Background layer ── */
.sky { opacity: 1; transition: opacity 1.1s ease; pointer-events: none; }
.sky.dim { opacity: 0.25; }



.pixel-planet { position: fixed; border-radius: 50%; }

.moon { position: fixed; left: -146px; bottom: -146px; width: 340px; height: 340px; border-radius: 50%; z-index: 1; }
.moon-glow {
  position: absolute; inset: -70px; border-radius: 50%;
  background: radial-gradient(circle, rgba(196,192,44,0.18) 18%, rgba(160,155,20,0.06) 50%, transparent 68%);
  filter: blur(24px); animation: moonGlow 5.5s ease-in-out infinite;
}
.moon-body {
  position: absolute; inset: 0; border-radius: 50%;
  background-image:
      radial-gradient(circle at 30% 24%, rgba(0,0,0,0.34) 0%, rgba(0,0,0,0.34) 9%, transparent 9%),
      radial-gradient(circle at 64% 18%, rgba(0,0,0,0.26) 0%, rgba(0,0,0,0.26) 5%, transparent 5%),
      radial-gradient(circle at 43% 58%, rgba(0,0,0,0.22) 0%, rgba(0,0,0,0.22) 12%, transparent 12%),
      radial-gradient(circle at 73% 45%, rgba(0,0,0,0.17) 0%, rgba(0,0,0,0.17) 4%, transparent 4%),
      radial-gradient(circle at 54% 66%, rgba(0,0,0,0.13) 0%, rgba(0,0,0,0.13) 7%, transparent 7%),
      radial-gradient(circle at 62% 52%, #c8c438 0%, #a8a228 42%, #747010 100%);
}
.moon-outline { position: absolute; inset: 0; border-radius: 50%; border: 3px solid #ceca42; box-sizing: border-box; }

.ghost-wrap { position: fixed; right: 9%; top: 52%; animation: ghostFloat 1.3s ease-in-out infinite; z-index: 1; }
.ghost { display: inline-block; line-height: 0; }
.ghost-row { display: flex; }
.ghost-px { width: 4px; height: 4px; }

/* ── Results panel ── */
.panel {
  width: 100%; margin-top: 2rem; background: #0d0d20;
  border: 2px solid #222244; box-shadow: 4px 4px 0 #03030a, inset 0 0 0 1px #080812;
  animation: panelReveal 0.38s ease forwards; margin-bottom: 2rem;
}
.panel-titlebar {
  border-bottom: 2px solid #222244; background: #121228;
  padding: 7px 14px; display: flex; align-items: center; gap: 10px;
}
.corner-dots { display: flex; gap: 4px; }
.corner-dots span { width: 8px; height: 8px; display: block; }
.corner-dots span:nth-child(1) { background: #222244; }
.corner-dots span:nth-child(2) { background: #3434a0; }
.corner-dots span:nth-child(3) { background: #7272e0; }
.panel-label { font-family: 'Press Start 2P', monospace; font-size: 7px; color: #505078; letter-spacing: 1px; }
.panel-target { font-family: 'VT323', monospace; font-size: 22px; color: #9090c0; flex: 1; }

.tab-bar { display: flex; border-bottom: 2px solid #222244; }
.tab-btn {
  flex: 1; padding: 10px 0; font-family: 'Press Start 2P', monospace; font-size: 7px; letter-spacing: 1px;
  background: transparent; color: #1e1e3c; border: none; border-right: 1px solid #222244;
  border-bottom: 2px solid transparent; cursor: pointer; outline: none;
}
.tab-btn.active { background: #121228; color: #c4c4e4; border-bottom-color: #7272e0; }

.tab-content { max-height: 300px; overflow-y: auto; padding: 14px 18px; }
.tab-content::-webkit-scrollbar { width: 8px; }
.tab-content::-webkit-scrollbar-track { background: transparent; }
.tab-content::-webkit-scrollbar-thumb { background: #1e1e44; }

.kv { display: flex; gap: 12px; margin-bottom: 6px; font-size: 0.6rem; }
.kv span { font-family: 'Press Start 2P', monospace; font-size: 7px; color: #505078; min-width: 88px; padding-top: 5px; flex-shrink: 0; letter-spacing: 1px; }
.kv code { font-family: 'VT323', monospace; font-size: 19px; color: #c4c4e4; word-break: break-word; }
.kv code.wrap { white-space: pre-wrap; }

.sub-label { font-family: 'Press Start 2P', monospace; font-size: 7px; color: #505078; margin: 0.7rem 0 0.4rem; letter-spacing: 1px; }
.san-list {
  list-style: none; margin: 0; padding: 0;
  display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 4px 12px; max-height: 160px; overflow-y: auto;
}
.san-list li { font-family: 'VT323', monospace; font-size: 17px; color: #9090c0; }

.panel-footer {
  border-top: 2px solid #222244; padding: 5px 14px; background: #121228;
  display: flex; justify-content: space-between;
  font-family: 'VT323', monospace; font-size: 14px; color: #505078;
}

@keyframes twinkle { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.05; transform: scale(0.4); } }
@keyframes moonGlow { 0%, 100% { opacity: 0.65; } 50% { opacity: 1; } }
@keyframes ghostFloat { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-5px); } }
@keyframes titlePulse { 0%, 100% { opacity: 0.6; } 50% { opacity: 1; } }
@keyframes panelReveal { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }

.mode-toggle { display: flex; gap: 6px; margin-top: 40px; }
.px-btn-sm {
  font-family: 'Press Start 2P', monospace; font-size: 0.6rem;
  background: #16192b; color: #505078; border: 2px solid #232740;
  padding: 8px 16px; cursor: pointer;
}
.px-btn-sm.active { background: #2828a0; color: #c4c4e4; }

.batch-container { margin-top: 12px; display: flex; flex-direction: column; gap: 10px; width: 100%; max-width: 460px; }
.batch-input {
  background: #090a10; border: 2px solid #232740; color: #c2c9ee;
  font-family: 'VT323', monospace; font-size: 18px; padding: 12px;
  min-height: 100px; resize: vertical; outline: none;
}
.batch-input::placeholder { color: #4a5173; }

.batch-results { width: 100%; margin-top: 2rem; border: 2px solid #222244; }
.batch-header, .batch-row {
  display: grid; grid-template-columns: 1fr 1fr 1fr 1fr 40px; gap: 8px;
  padding: 8px 12px; font-family: 'VT323', monospace; font-size: 16px;
}
.batch-header { background: #121228; color: #505078; font-size: 12px; text-transform: uppercase; }
.batch-row { color: #9090c0; border-top: 1px solid #1a1a30; }

.log-btn {
  font-family: 'VT323', monospace; font-size: 14px; color: #7272e0;
  background: transparent; border: 1px solid #3434a0; padding: 3px 10px; cursor: pointer;
}
.log-btn:hover:not(:disabled) { color: #a0a0f0; border-color: #5858d0; background: rgba(52,52,160,0.15); }
.log-btn:disabled { cursor: not-allowed; opacity: 0.5; }

.log-btn-sm {
  font-family: 'VT323', monospace; font-size: 16px; color: #7272e0;
  background: transparent; border: 1px solid #3434a0; cursor: pointer;
  padding: 2px 6px; line-height: 1;
}
.log-btn-sm:hover:not(:disabled) { color: #a0a0f0; border-color: #5858d0; }
.log-btn-sm:disabled { cursor: not-allowed; opacity: 0.5; }
</style>