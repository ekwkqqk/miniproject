import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as settingsApi from '@/features/settings/api'

const THEME_KEY = 'app.themePrimaryColor'

function applyThemeColor(color) {
  if (!color) return
  document.documentElement.style.setProperty('--el-color-primary', color)
  // Element Plus 파생 톤 (대략값)
  document.documentElement.style.setProperty('--el-color-primary-light-3', color)
  document.documentElement.style.setProperty('--el-color-primary-light-5', color)
  document.documentElement.style.setProperty('--el-color-primary-light-7', color)
  document.documentElement.style.setProperty('--el-color-primary-light-8', color)
  document.documentElement.style.setProperty('--el-color-primary-light-9', color)
  document.documentElement.style.setProperty('--el-color-primary-dark-2', color)
  localStorage.setItem(THEME_KEY, color)
}

export const useSettingsStore = defineStore('settings', () => {
  const themePrimaryColor = ref(localStorage.getItem(THEME_KEY) || '#409EFF')
  const passwordMinLength = ref(6)
  const loaded = ref(false)

  function applyLocalTheme() {
    applyThemeColor(themePrimaryColor.value)
  }

  async function loadPublicSettings() {
    applyLocalTheme()
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
