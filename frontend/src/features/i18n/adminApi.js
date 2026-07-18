import client from '@/shared/api/client'

export function getLocales() {
  return client.get('/admin/i18n/locales')
}

export function createLocale(payload) {
  return client.post('/admin/i18n/locales', payload)
}

export function updateLocale(id, payload) {
  return client.put(`/admin/i18n/locales/${id}`, payload)
}

export function deleteLocale(id) {
  return client.delete(`/admin/i18n/locales/${id}`)
}

export function getGroups() {
  return client.get('/admin/i18n/groups')
}

export function createGroup(payload) {
  return client.post('/admin/i18n/groups', payload)
}

export function updateGroup(id, payload) {
  return client.put(`/admin/i18n/groups/${id}`, payload)
}

export function deleteGroup(id) {
  return client.delete(`/admin/i18n/groups/${id}`)
}

export function getMessages(group) {
  return client.get('/admin/i18n/messages', { params: group ? { group } : {} })
}

export function getMessagePage(params = {}) {
  return client.get('/admin/i18n/messages/page', { params })
}

export function createMessage(payload) {
  return client.post('/admin/i18n/messages', payload)
}

export function updateMessage(id, payload) {
  return client.put(`/admin/i18n/messages/${id}`, payload)
}

export function deleteMessage(id) {
  return client.delete(`/admin/i18n/messages/${id}`)
}
