import { storeToRefs } from 'pinia'
import { useI18nStore } from '@/stores/i18n'

export function useI18n() {
  const store = useI18nStore()
  const { locale, locales, messages, loaded } = storeToRefs(store)

  return {
    locale,
    locales,
    messages,
    loaded,
    t: store.t,
    tCode: store.tCode,
    setLocale: store.setLocale,
    loadLocales: store.loadLocales,
    loadMessages: store.loadMessages,
  }
}
