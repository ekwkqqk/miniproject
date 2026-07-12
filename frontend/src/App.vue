<script setup>
import { computed } from 'vue'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'

const { isMobile } = useBreakpoint()

const dialogConfig = computed(() => ({
  draggable: !isMobile.value,
  overflow: !isMobile.value,
}))
</script>

<template>
  <el-config-provider :dialog="dialogConfig">
    <router-view />
  </el-config-provider>
</template>

<style>
* {
  box-sizing: border-box;
}

html,
body,
#app {
  margin: 0;
  min-height: 100%;
  max-width: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  overflow-x: hidden;
}

a {
  color: #409eff;
  text-decoration: none;
}

img,
video {
  max-width: 100%;
  height: auto;
}
</style>
