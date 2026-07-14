import client from '@/shared/api/client'

export function getMyMenus() {
  return client.get('/menus/my')
}

export function reportMenuAccess(menuUrl) {
  return client.post('/menus/access', { menuUrl })
}
