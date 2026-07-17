import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as settingsApi from '@/features/settings/api'

const THEME_KEY = 'app.themePrimaryColor'

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

/**
 * Element Plus primary 파생톤 생성.
 * light-N = mix(#fff, primary, N*10%), dark-2 = mix(#000, primary, 20%)
 * (전부 원색으로 두면 default 버튼 호버 시 파란 배경 + 파란 글자로 안 보임)
 */
function applyThemeColor(color) {
  const primary = normalizeHex(color)
  if (!primary) return

  const root = document.documentElement
  root.style.setProperty('--el-color-primary', primary)
  ;[3, 5, 7, 8, 9].forEach((level) => {
    root.style.setProperty(
      `--el-color-primary-light-${level}`,
      mix('#FFFFFF', primary, level * 10)
    )
  })
  const primaryDark = mix('#000000', primary, 20)
  root.style.setProperty('--el-color-primary-dark-2', primaryDark)

  // 사이드바/메뉴 배경도 테마색 반영 (본색 + 활성·호버용 어두운 톤)
  root.style.setProperty('--app-sidebar-bg', primary)
  root.style.setProperty('--app-sidebar-active-bg', primaryDark)
  root.style.setProperty('--app-sidebar-hover-bg', mix('#FFFFFF', primary, 12))

  localStorage.setItem(THEME_KEY, primary)
}

export const useSettingsStore = defineStore('settings', () => {
  const themePrimaryColor = ref(localStorage.getItem(THEME_KEY) || '#409EFF')
  const passwordMinLength = ref(6)
  const loaded = ref(false)

  function applyLocalTheme() {
    applyThemeColor(themePrimaryColor.value)
  }

  async function loadPublicSettings(force = false) {
    applyLocalTheme()
    if (loaded.value && !force) return
    try {
      const { data } = await settingsApi.getPublicSettings()
      if (data.success && data.data) {
        themePrimaryColor.value = data.data.themePrimaryColor
        passwordMinLength.value = data.data.passwordMinLength
        applyThemeColor(themePrimaryColor.value)
        loaded.value = true
      }
    } catch {
      applyLocalTheme()
    }
  }

  function setThemeFromAdmin(color) {
    themePrimaryColor.value = color
    applyThemeColor(color)
  }

  return {
    themePrimaryColor,
    passwordMinLength,
    loaded,
    applyLocalTheme,
    loadPublicSettings,
    setThemeFromAdmin,
  }
})
