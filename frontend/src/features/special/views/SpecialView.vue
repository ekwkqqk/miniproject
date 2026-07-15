<script setup>
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
</script>

<template>
  <PageLayout title="특별 사용자 영역" subtitle="메뉴 Role이 있는 사용자만 접근할 수 있습니다.">
    <ContentPanel title="권한 테스트">
      <el-button v-can:read type="info">조회</el-button>
      <el-button v-can:update type="primary">수정</el-button>
      <el-button v-can:delete type="danger">삭제</el-button>
    </ContentPanel>
  </PageLayout>
</template>
