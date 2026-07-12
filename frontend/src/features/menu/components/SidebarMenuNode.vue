<script setup>
import { computed } from 'vue'
import { useI18n } from '@/features/i18n/useI18n'
import SidebarMenuNode from '@/features/menu/components/SidebarMenuNode.vue'

const props = defineProps({
  menu: {
    type: Object,
    required: true,
  },
})

// messages/locale을 직접 구독해야 언어 변경·번들 로드 시 라벨이 갱신됨
const { messages, locale } = useI18n()

const label = computed(() => {
  const key = props.menu.nameI18nKey
  const bundle = messages.value || {}
  // locale 의존성 추적 (언어 전환 시 재계산)
  void locale.value
  if (key && Object.prototype.hasOwnProperty.call(bundle, key) && bundle[key] != null) {
    return bundle[key]
  }
  return props.menu.name
})
</script>

<template>
  <el-sub-menu v-if="menu.folder || menu.children?.length" :index="`folder-${menu.id}`">
    <template #title>
      <span>{{ label }}</span>
    </template>
    <SidebarMenuNode
      v-for="child in menu.children"
      :key="child.id"
      :menu="child"
    />
  </el-sub-menu>
  <el-menu-item v-else-if="menu.url" :index="menu.url">
    <span>{{ label }}</span>
  </el-menu-item>
</template>
