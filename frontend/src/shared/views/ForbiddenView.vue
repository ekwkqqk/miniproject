<script setup>
import ErrorPage from '@/shared/components/ErrorPage.vue'
</script>

<template>
  <ErrorPage
    :code="403"
    title="접근 권한이 없습니다"
    message="이 페이지에 접근할 수 있는 권한이 없습니다. 관리자에게 문의해 주세요."
  />
</template>
