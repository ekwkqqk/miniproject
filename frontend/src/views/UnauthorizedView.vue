<script setup>
import ErrorPage from '@/components/ErrorPage.vue'
</script>

<template>
  <ErrorPage
    :code="401"
    title="인증이 필요합니다"
    message="로그인이 만료되었거나 권한이 없습니다. 다시 로그인해 주세요."
  />
</template>
