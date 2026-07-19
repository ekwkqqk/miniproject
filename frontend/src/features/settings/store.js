import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as settingsApi from '@/features/settings/api'

const THEME_KEY = 'app.themePrimaryColor'
const DARK_KEY = 'app.darkMode'
const ELEMENT_DARK_BG = '#141414'

function normalizeHex(color) {
  if (!color || typeof color !== 'string') return null
  let hex = color.trim()
  if (!hex.startsWith('#')) hex = `#${hex}`
  if (/^#[0-9a-fA-F]{3}$/.test(hex)) {
    hex = `#${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}`
  }
  if (!/^#[0-9a-fA-F]{6}$/.test(hex)) return null
  return hex.toUpperCase()
}

function hexToRgb(hex) {
  const n = parseInt(hex.slice(1), 16)
  return { r: (n >> 16) & 255, g: (n >> 8) & 255, b: n & 255 }
}

function rgbToHex(r, g, b) {
  return `#${[r, g, b]
    .map((v) => Math.round(Math.min(255, Math.max(0, v))).toString(16).padStart(2, '0'))
    .join('')}`.toUpperCase()
}

/** Sass-compatible mix: weight% of color1 + (100-weight)% of color2 */
function mix(color1, color2, weight) {
  const w = weight / 100
  const a = hexToRgb(color1)
  const b = hexToRgb(color2)
  return rgbToHex(
    a.r * w + b.r * (1 - w),
    a.g * w + b.g * (1 - w),
    a.b * w + b.b * (1 - w)
  )
}

function readStoredDarkMode() {
  try {
    return localStorage.getItem(DARK_KEY) === 'true'
  } catch {
    return false
  }
}

/**
 * Element Plus primary 파생톤 생성.
 * light: mix(#fff, primary) / dark: mix(#141414, primary)
 */
function applyThemeColor(color, isDark = false) {
  const primary = normalizeHex(color)
  if (!primary) return

  const root = document.documentElement
  const lightMixBase = isDark ? ELEMENT_DARK_BG : '#FFFFFF'
  root.style.setProperty('--el-color-primary', primary)
  ;[3, 5, 7, 8, 9].forEach((level) => {
    root.style.setProperty(
      `--el-color-primary-light-${level}`,
      mix(lightMixBase, primary, level * 10)
    )
  })
  const primaryDark = isDark
    ? mix('#FFFFFF', primary, 20)
    : mix('#000000', primary, 20)
  root.style.setProperty('--el-color-primary-dark-2', primaryDark)

  root.style.setProperty('--app-sidebar-bg', primary)
  root.style.setProperty('--app-sidebar-active-bg', primaryDark)
  root.style.setProperty(
    '--app-sidebar-hover-bg',
    isDark ? mix('#FFFFFF', primary, 14) : mix('#FFFFFF', primary, 12)
  )

  const themeMeta = document.querySelector('meta[name="theme-color"]')
  if (themeMeta) {
    themeMeta.setAttribute('content', isDark ? ELEMENT_DARK_BG : primary)
  }

  localStorage.setItem(THEME_KEY, primary)
}

/** 테마 전환 중 CSS transition을 잠시 끄고, 다음 페인트 후 복구 */
function withInstantThemePaint(apply) {
  const root = document.documentElement
  root.classList.add('theme-switching')
  apply()
  // 레이아웃을 강제로 한 번 반영한 뒤, 두 프레임 후 transition 복구
  void root.offsetHeight
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      root.classList.remove('theme-switching')
    })
  })
}

export const useSettingsStore = defineStore('settings', () => {
  const themePrimaryColor = ref(localStorage.getItem(THEME_KEY) || '#409EFF')
  const darkMode = ref(readStoredDarkMode())
  const passwordMinLength = ref(6)
  const loaded = ref(false)
  const isDark = computed(() => darkMode.value)

  function applyDarkMode(enabled = darkMode.value, { animate = true } = {}) {
    const next = !!enabled
    const run = () => {
      // DOM/CSS를 Vue 상태보다 먼저 한꺼번에 적용 → 중간 페인트 방지
      document.documentElement.classList.toggle('dark', next)
      applyThemeColor(themePrimaryColor.value, next)
      darkMode.value = next
      try {
        localStorage.setItem(DARK_KEY, String(next))
      } catch {
        // ignore
      }
    }

    if (animate && typeof document !== 'undefined') {
      withInstantThemePaint(run)
    } else {
      run()
    }
  }

  function toggleDarkMode() {
    applyDarkMode(!darkMode.value)
  }

  function applyLocalTheme() {
    // 초기 로드는 transition 억제만 하고 animate 플래그는 끔 (불필요한 rAF 대기 없음)
    applyDarkMode(darkMode.value, { animate: false })
  }

  async function loadPublicSettings(force = false) {
    applyLocalTheme()
    if (loaded.value && !force) return
    try {
      const { data } = await settingsApi.getPublicSettings()
      if (data.success && data.data) {
        themePrimaryColor.value = data.data.themePrimaryColor
        passwordMinLength.value = data.data.passwordMinLength
        withInstantThemePaint(() => {
          applyThemeColor(themePrimaryColor.value, darkMode.value)
        })
        loaded.value = true
      }
    } catch {
      applyLocalTheme()
    }
  }

  function setThemeFromAdmin(color) {
    themePrimaryColor.value = color
    withInstantThemePaint(() => {
      applyThemeColor(color, darkMode.value)
    })
  }

  return {
    themePrimaryColor,
    darkMode,
    isDark,
    passwordMinLength,
    loaded,
    applyLocalTheme,
    applyDarkMode,
    toggleDarkMode,
    loadPublicSettings,
    setThemeFromAdmin,
  }
})
