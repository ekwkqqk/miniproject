import client from '@/shared/api/client'
import { downloadExcel } from '@/shared/utils/download'

export function getDemoItems(params = {}) {
  return client.get('/demo/items', { params })
}

export function getDemoItem(id) {
  return client.get(`/demo/items/${id}`)
}

export function downloadDemoItemsExcel(params = {}) {
  return downloadExcel({
    url: '/demo/items/export',
    params,
    fallbackFilename: 'demo-items.xlsx',
  })
}
