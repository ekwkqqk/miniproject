<script setup>
import { computed } from 'vue'
import { useI18n } from '@/features/i18n/useI18n'
import SidebarMenuNode from '@/features/menu/components/SidebarMenuNode.vue'
import { useApprovalBadgeStore } from '@/features/approval/badgeStore'

const props = defineProps({
  menu: {
    type: Object,
    required: true,
  },
})

// messages/locale을 직접 구독해야 메시지 변경·번들 로드 시 라벨이 갱신됨
const { messages, locale } = useI18n()
const badgeStore = useApprovalBadgeStore()

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

const badge = computed(() => badgeStore.badgeForUrl(props.menu.url))
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
    <span class="menu-item-label">
      <span>{{ label }}</span>
      <el-badge v-if="badge > 0" :value="badge" :offset="[0, 0]" class="menu-badge" />
    </span>
  </el-menu-item>
</template>

<style scoped>
.menu-item-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  line-height: 1;
  vertical-align: middle;
}

.menu-badge {
  display: inline-flex;
  align-items: center;
  height: auto;
  line-height: 1;
}

:deep(.menu-badge .el-badge__content) {
  position: static;
  transform: none;
  top: auto;
  right: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 18px;
  min-width: 18px;
  padding: 0 6px;
  line-height: 1;
  vertical-align: middle;
}
</style>
