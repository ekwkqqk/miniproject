import client from '@/shared/api/client'

export function getDemoItems(params = {}) {
  return client.get('/demo/items', { params })
}

export function getDemoItem(id) {
  return client.get(`/demo/items/${id}`)
}
