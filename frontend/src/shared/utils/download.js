import client from '@/shared/api/client'

/**
 * Content-Disposition 헤더에서 파일명 추출
 */
export function filenameFromContentDisposition(headers, fallback = 'download.bin') {
  const disposition =
    headers?.['content-disposition'] ||
    headers?.['Content-Disposition'] ||
    ''
  const utf8Match = /filename\*=UTF-8''([^;]+)/i.exec(disposition)
  if (utf8Match?.[1]) {
    try {
      return decodeURIComponent(utf8Match[1])
    } catch {
      return utf8Match[1]
    }
  }
  const plainMatch = /filename="?([^";]+)"?/i.exec(disposition)
  if (plainMatch?.[1]) {
    return plainMatch[1]
  }
  return fallback
}

/**
 * Blob을 브라우저 다운로드로 저장
 */
export function saveBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename || 'download.bin'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

/**
 * API에서 blob을 받아 파일로 저장
 * @param {{ url: string, params?: object, method?: string, fallbackFilename?: string }} options
 */
export async function downloadBlobFile({
  url,
  params,
  method = 'get',
  fallbackFilename = 'download.bin',
} = {}) {
  const response = await client.request({
    url,
    method,
    params,
    responseType: 'blob',
  })
  const filename = filenameFromContentDisposition(response.headers, fallbackFilename)
  saveBlob(response.data, filename)
  return { filename, blob: response.data }
}

/**
 * 엑셀(xlsx) 다운로드 전용 헬퍼
 */
export async function downloadExcel({
  url,
  params,
  method = 'get',
  fallbackFilename = 'export.xlsx',
} = {}) {
  return downloadBlobFile({ url, params, method, fallbackFilename })
}
