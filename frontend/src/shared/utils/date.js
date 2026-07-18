/**
 * API/화면 공통 날짜 표시 포맷.
 * - ISO(2026-07-17T19:51:04.292108) → 2026-07-17 19:51:04
 * - 이미 포맷된 값은 그대로 반환
 */
export function formatDateTime(value) {
  if (value == null || value === '') return ''
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return formatParts(value)
  }

  const text = String(value).trim()
  if (!text) return ''

  // 이미 yyyy-MM-dd HH:mm:ss 형태
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(text)) {
    return text
  }
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(text)) {
    return `${text}:00`
  }

  // ISO / 공백 구분 로컬 시간 (타임존 변환 없이 표시)
  const matched = text.match(/^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2}:\d{2})/)
  if (matched) {
    return `${matched[1]} ${matched[2]}`
  }

  const date = new Date(text)
  if (Number.isNaN(date.getTime())) {
    return text
  }
  return formatParts(date)
}

function formatParts(date) {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`
}
