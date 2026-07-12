import client from '@/shared/api/client'

export function getPublicSettings() {
  return client.get('/settings/public')
}

export function getSettings() {
  return client.get('/admin/settings')
}

export function updateSettings(payload) {
  return client.put('/admin/settings', payload)
}
