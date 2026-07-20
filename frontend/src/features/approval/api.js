import client from '@/shared/api/client'

export function getDocuments(params) {
  return client.get('/approval/documents', { params })
}

export function getInbox(params) {
  return client.get('/approval/inbox', { params })
}

export function getNotices(params) {
  return client.get('/approval/notices', { params })
}

export function getDocument(id) {
  return client.get(`/approval/documents/${id}`)
}

export function createDocument(body) {
  return client.post('/approval/documents', body)
}

export function updateDocument(id, body) {
  return client.put(`/approval/documents/${id}`, body)
}

export function deleteDocument(id) {
  return client.delete(`/approval/documents/${id}`)
}

export function submitDocument(id) {
  return client.post(`/approval/documents/${id}/submit`)
}

export function recallDocument(id) {
  return client.post(`/approval/documents/${id}/recall`)
}

export function approveDocument(id, body) {
  return client.post(`/approval/documents/${id}/approve`, body)
}

export function rejectDocument(id, body) {
  return client.post(`/approval/documents/${id}/reject`, body)
}

export function acknowledgeDocument(id, body) {
  return client.post(`/approval/documents/${id}/acknowledge`, body)
}

export function searchUsers(params) {
  return client.get('/users/search', { params })
}

export function getBadges() {
  return client.get('/approval/badges')
}
