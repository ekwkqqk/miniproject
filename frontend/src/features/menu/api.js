import client from '@/shared/api/client'

export function getMyMenus() {
  return client.get('/menus/my')
}
