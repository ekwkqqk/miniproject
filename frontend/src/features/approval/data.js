export const LINE_TYPES = [
  { value: 'APPROVE', label: '결재' },
  { value: 'AGREE', label: '합의' },
  { value: 'POST', label: '후결' },
  { value: 'NOTIFY', label: '통보' },
]

export const DOC_CLASSES = [
  { value: 'GENERAL', label: '일반' },
  { value: 'CONFIDENTIAL', label: '대외비' },
  { value: 'TOP_SECRET', label: '극비' },
  { value: 'URGENT', label: '긴급' },
]

export const DOC_STATUS = {
  DRAFT: { label: '임시저장', type: 'info' },
  SCHEDULED: { label: '예약상신', type: 'warning' },
  IN_PROGRESS: { label: '진행중', type: 'warning' },
  APPROVED: { label: '승인', type: 'success' },
  REJECTED: { label: '반려', type: 'danger' },
}

export const LINE_STATUS = {
  WAITING: '대기',
  PENDING: '처리대기',
  HELD: '보류',
  APPROVED: '승인',
  REJECTED: '반려',
  ACKNOWLEDGED: '확인',
  SKIPPED: '스킵',
}

export function statusMeta(status) {
  return DOC_STATUS[status] || { label: status || '-', type: 'info' }
}

export function lineTypeLabel(type) {
  return LINE_TYPES.find((t) => t.value === type)?.label || type
}

export function docClassLabel(value) {
  return DOC_CLASSES.find((c) => c.value === value)?.label || value || '일반'
}
