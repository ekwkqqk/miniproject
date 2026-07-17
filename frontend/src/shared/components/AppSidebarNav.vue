<script setup>
import { useMenuStore } from '@/features/menu/store'
import SidebarMenuNode from '@/features/menu/components/SidebarMenuNode.vue'

defineProps({
  activeMenu: { type: String, required: true },
})

const emit = defineEmits(['navigate'])
const menuStore = useMenuStore()

function onSelect() {
  emit('navigate')
}
</script>

<template>
  <div class="app-sidebar-nav">
    <div class="logo">miniproject</div>
    <el-menu :default-active="activeMenu" router unique-opened @select="onSelect">
      <SidebarMenuNode
        v-for="menu in menuStore.menus"
        :key="menu.id"
        :menu="menu"
      />
    </el-menu>
  </div>
</template>

<style scoped>
.app-sidebar-nav {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--app-sidebar-bg, #1f2d3d);
}

.logo {
  padding: 20px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent;
  flex: 1;
  overflow-y: auto;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.85);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: var(--app-sidebar-hover-bg, rgba(255, 255, 255, 0.12)) !important;
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background: var(--app-sidebar-active-bg, #263445) !important;
  color: #fff;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent;
}
</style>
