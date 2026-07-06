import client from './client'

export function getUsers() {
  return client.get('/admin/users')
}

export function updateUserRole(userId, role) {
  return client.patch(`/admin/users/${userId}/role`, { role })
}
