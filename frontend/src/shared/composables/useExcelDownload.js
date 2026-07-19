import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadExcel } from '@/shared/utils/download'

/**
 * 엑셀 다운로드 로딩/메시지 공통 처리
 */
export function useExcelDownload() {
  const exporting = ref(false)

  async function download(options = {}) {
    exporting.value = true
    try {
      await downloadExcel(options)
      ElMessage.success('엑셀 다운로드를 시작했습니다.')
    } catch (error) {
      ElMessage.error(
        error.response?.data?.message || error.message || '엑셀 다운로드에 실패했습니다.',
      )
      throw error
    } finally {
      exporting.value = false
    }
  }

  return {
    exporting,
    download,
  }
}
