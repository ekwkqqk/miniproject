/** Shared labels/helpers for demo screens (search options, tags, formatting). */

export const CATEGORIES = ['전자제품', '주변기기', '가구']

export const STATUSES = [
  { value: 'ACTIVE', label: '판매중', type: 'success' },
  { value: 'INACTIVE', label: '판매중지', type: 'info' },
  { value: 'PENDING', label: '검수중', type: 'warning' },
]

export function statusMeta(status) {
  return STATUSES.find((s) => s.value === status) || { value: status, label: status, type: 'info' }
}

export function formatPrice(value) {
  return new Intl.NumberFormat('ko-KR').format(value ?? 0) + '원'
}
