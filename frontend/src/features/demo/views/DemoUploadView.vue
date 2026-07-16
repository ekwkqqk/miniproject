<script setup>
import { ref } from 'vue'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import FileAttachment from '@/shared/components/FileAttachment.vue'

const imageFiles = ref([])
const pdfFiles = ref([])
const anyFiles = ref([])
</script>

<template>
  <PageLayout
    title="파일 첨부"
    subtitle="드래그 앤 드롭 · 타입/개수 제한 · 서버 업로드 데모"
  >
    <ContentPanel title="이미지 (image/*, 최대 3개)">
      <FileAttachment
        v-model="imageFiles"
        accept="image/*"
        :limit="3"
        tip="이미지 파일만 · 최대 3개"
      />
    </ContentPanel>

    <ContentPanel title="PDF (.pdf, 최대 1개)">
      <FileAttachment
        v-model="pdfFiles"
        accept=".pdf,application/pdf"
        :limit="1"
        :multiple="false"
        tip="PDF만 · 1개"
      />
    </ContentPanel>

    <ContentPanel title="제한 없음 (최대 5개)">
      <FileAttachment v-model="anyFiles" :limit="5" />
    </ContentPanel>
  </PageLayout>
</template>
