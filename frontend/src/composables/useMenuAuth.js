import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/stores/menu'

/**
 * 현재 라우트(또는 지정 URL)의 메뉴 버튼 권한
 * @param {string} [url]
 */
export function useMenuAuth(url) {
  const route = useRoute()
  const menuStore = useMenuStore()

  const targetUrl = computed(() => url || route.path)

  const buttons = computed(() => menuStore.getButtons(targetUrl.value))

  const canRead = computed(() => !!buttons.value.canRead)
  const canUpdate = computed(() => !!buttons.value.canUpdate)
  const canDelete = computed(() => !!buttons.value.canDelete)
  const canUpload = computed(() => !!buttons.value.canUpload)
  const canDownload = computed(() => !!buttons.value.canDownload)
  const canOther = computed(() => !!buttons.value.canOther)

  return {
    buttons,
    canRead,
    canUpdate,
    canDelete,
    canUpload,
    canDownload,
    canOther,
  }
}
