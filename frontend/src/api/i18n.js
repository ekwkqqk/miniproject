import client from './client'

export function getEnabledLocales() {
  return client.get('/i18n/locales')
}

export function getMessageBundle(locale, group) {
  return client.get('/i18n/messages', {
    params: { locale, ...(group ? { group } : {}) },
  })
}

export function resolveMessage(payload) {
  return client.post('/i18n/resolve', payload)
}
