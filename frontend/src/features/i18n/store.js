import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as i18nApi from '@/features/i18n/api'

const STORAGE_KEY = 'app.locale'

function formatTemplate(template, params = {}) {
  if (!template) return template
  return template.replace(/\{([^{}]+)}/g, (match, key) => {
    if (Object.prototype.hasOwnProperty.call(params, key) && params[key] != null) {
      return String(params[key])
    }
    return match
  })
}

export const useI18nStore = defineStore('i18n', () => {
  const locale = ref(localStorage.getItem(STORAGE_KEY) || 'ko')
  const locales = ref([])
  const messages = ref({})
  const loaded = ref(false)

  async function loadLocales() {
    const { data } = await i18nApi.getEnabledLocales()
    if (data.success) {
      locales.value = data.data
      if (!locales.value.some((l) => l.code === locale.value) && locales.value.length) {
        locale.value = locales.value[0].code
        localStorage.setItem(STORAGE_KEY, locale.value)
      }
    }
  }

  async function loadMessages(group) {
    const { data } = await i18nApi.getMessageBundle(locale.value, group)
    if (data.success) {
      messages.value = { ...messages.value, ...data.data }
      loaded.value = true
    }
  }

  async function setLocale(code) {
    locale.value = code
    localStorage.setItem(STORAGE_KEY, code)
    messages.value = {}
    loaded.value = false
    await loadMessages()
  }

  function t(key, params = {}) {
    const template = messages.value[key]
    if (template == null) return key
    return formatTemplate(template, params)
  }

  /** group + code 형태. 예: tCode('common', 'welcome', { name: '홍길동' }) */
  function tCode(group, code, params = {}) {
    return t(`${group}.${code}`, params)
  }

  return {
    locale,
    locales,
    messages,
    loaded,
    loadLocales,
    loadMessages,
    setLocale,
    t,
    tCode,
  }
})
