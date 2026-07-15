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
  const switching = ref(false)

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

  async function loadMessages(group, localeCode = locale.value) {
    const { data } = await i18nApi.getMessageBundle(localeCode, group)
    if (data.success) {
      if (group) {
        messages.value = { ...messages.value, ...data.data }
      } else {
        messages.value = { ...(data.data || {}) }
      }
      loaded.value = true
    }
    return data?.success === true
  }

  /**
   * 언어 전환 시 메시지를 비우지 않는다.
   * (빈 번들이면 t()가 키 문자열을 반환해 화면 폭이 늘어났다 줄어드는 깜빡임이 난다)
   * 새 번들을 받은 뒤 locale + messages를 동시에 교체한다.
   */
  async function setLocale(code) {
    if (!code || code === locale.value) {
      if (!loaded.value) {
        await loadMessages(undefined, code || locale.value)
      }
      return
    }
    switching.value = true
    try {
      const { data } = await i18nApi.getMessageBundle(code)
      if (data.success) {
        messages.value = { ...(data.data || {}) }
        locale.value = code
        localStorage.setItem(STORAGE_KEY, code)
        loaded.value = true
      }
    } finally {
      switching.value = false
    }
  }

  function t(key, params = {}) {
    void locale.value
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
    switching,
    loadLocales,
    loadMessages,
    setLocale,
    t,
    tCode,
  }
})
