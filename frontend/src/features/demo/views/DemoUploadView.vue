<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import FileAttachment from '@/shared/components/FileAttachment.vue'

const editFiles = ref([])
const viewFiles = ref([])
const selectedIds = ref([])
const lastResult = ref(null)

function onUploaded(fileIds, fileGroupId) {
  lastResult.value = { fileIds, fileGroupId }
  // edit에서 올린 결과를 view 모드 목록에도 반영 (데모)
  viewFiles.value = [...editFiles.value.filter((f) => !f.pending)]
  ElMessage.info(`콜백: fileGroupId=${fileGroupId}, fileIds=[${fileIds.join(', ')}]`)
}

watch(editFiles, (list) => {
  // 업로드 완료된 파일만 view에 동기화
  const uploaded = list.filter((f) => f.id != null && !f.pending)
  if (uploaded.length) {
    viewFiles.value = uploaded
  }
})
</script>

<template>
  <PageLayout
    title="파일 첨부"
    subtitle="edit: 첨부/저장 · view: 체크박스 + 다운로드"
  >
    <ContentPanel v-if="lastResult" title="마지막 업로드 결과">
      <p>fileGroupId: {{ lastResult.fileGroupId }}</p>
      <p>fileIds: {{ lastResult.fileIds.join(', ') }}</p>
    </ContentPanel>

    <ContentPanel title="Edit 모드 (첨부)">
      <FileAttachment
        v-model="editFiles"
        mode="edit"
        :limit="5"
        tip="파일 선택 후 저장(업로드)"
        @uploaded="onUploaded"
      />
    </ContentPanel>

    <ContentPanel title="View 모드 (조회 · 다운로드)">
      <FileAttachment
        v-model="viewFiles"
        v-model:selected="selectedIds"
        mode="view"
      />
      <p v-if="selectedIds.length" style="margin-top: 8px; font-size: 13px; color: #606266">
        선택 id: {{ selectedIds.join(', ') }}
      </p>
    </ContentPanel>
  </PageLayout>
</template>
