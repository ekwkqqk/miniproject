import client from '@/shared/api/client'

export function getTemplates() {
  return client.get('/admin/mail/templates')
}

export function getTemplate(id) {
  return client.get(`/admin/mail/templates/${id}`)
}

export function createTemplate(payload) {
  return client.post('/admin/mail/templates', payload)
}

export function updateTemplate(id, payload) {
  return client.put(`/admin/mail/templates/${id}`, payload)
}

export function deleteTemplate(id) {
  return client.delete(`/admin/mail/templates/${id}`)
}

export function sendMail(payload) {
  return client.post('/mail/send', payload)
}
