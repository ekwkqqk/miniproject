import client from './client'

export function getMyMenus() {
  return client.get('/menus/my')
}
