import client, { getErrorMessage } from '@/shared/api/client'
import { filenameFromContentDisposition, saveBlob } from '@/shared/utils/download'

/**
 * multipart 업로드 — 동일 요청 파일들은 같은 fileGroupId로 묶임
 * @returns {{ success, data: { fileGroupId, fileIds, files } }}
 */
export async function uploadFiles(files, { accept, limit } = {}) {
  const form = new FormData()
  for (const file of files) {
    form.append('files', file)
  }
  if (accept) form.append('accept', accept)
  if (limit != null) form.append('limit', String(limit))

  try {
    const { data } = await client.post('/files/upload', form)
    return data
  } catch (error) {
    throw new Error(getErrorMessage(error))
  }
}

export async function getFilesByGroup(fileGroupId) {
  try {
    const { data } = await client.get(`/files/groups/${fileGroupId}`)
    return data
  } catch (error) {
    throw new Error(getErrorMessage(error))
  }
}

export async function deleteFile(id) {
  try {
    const { data } = await client.delete(`/files/${id}`)
    return data
  } catch (error) {
    throw new Error(getErrorMessage(error))
  }
}

export function downloadUrl(id) {
  return `/api/files/${id}/download`
}

export async function downloadFile(id, filename) {
  const { data, headers } = await client.get(`/files/${id}/download`, {
    responseType: 'blob',
  })
  const resolvedName = filenameFromContentDisposition(
    headers,
    filename || `file-${id}`,
  )
  saveBlob(data, resolvedName)
}
