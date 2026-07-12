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
  background: #1f2d3d;
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
  color: #d3dce6;
}

:deep(.el-menu-item.is-active) {
  background: #263445 !important;
  color: #fff;
}
</style>
