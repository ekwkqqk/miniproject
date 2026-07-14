import client from '@/shared/api/client'

export function getRoles() {
  return client.get('/admin/roles')
}

export function createRole(payload) {
  return client.post('/admin/roles', payload)
}

export function deleteRole(roleId) {
  return client.delete(`/admin/roles/${roleId}`)
}

export function getMenus() {
  return client.get('/admin/menus')
}

export function createMenu(payload) {
  return client.post('/admin/menus', payload)
}

export function updateMenu(menuId, payload) {
  return client.put(`/admin/menus/${menuId}`, payload)
}

export function deleteMenu(menuId) {
  return client.delete(`/admin/menus/${menuId}`)
}

export function reorderMenus(payload) {
  return client.put('/admin/menus/reorder', payload)
}

export function getUsers() {
  return client.get('/admin/users')
}

export function createUser(payload) {
  return client.post('/admin/users', payload)
}

export function updateUserRoles(userId, roleIds) {
  return client.put(`/admin/users/${userId}/roles`, { roleIds })
}

export function updateUserEnabled(userId, enabled) {
  return client.put(`/admin/users/${userId}/enabled`, { enabled })
}

export function getMenuAccessLogs(params = {}) {
  return client.get('/admin/menu-access-logs', { params })
}
