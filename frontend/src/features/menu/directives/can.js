import { watch } from 'vue'
import { useMenuStore } from '@/features/menu/store'
import router from '@/router'

const ACTION_MAP = {
  read: 'canRead',
  update: 'canUpdate',
  delete: 'canDelete',
  upload: 'canUpload',
  download: 'canDownload',
  other: 'canOther',
}

function resolveAction(binding) {
  if (binding.arg) {
    return String(binding.arg).trim().toLowerCase()
  }
  const value = binding.value
  if (value == null || value === '') return null
  if (typeof value === 'object') {
    const action = value.action ?? value.permission
    return action == null ? null : String(action).trim().toLowerCase()
  }
  return String(value).trim().toLowerCase()
}

function resolveUrl(binding) {
  const value = binding.value
  if (typeof value === 'object' && value?.url) {
    return value.url
  }
  return router.currentRoute.value.path
}

function resolvePermissionKey(action) {
  if (!action) return null
  if (ACTION_MAP[action]) return ACTION_MAP[action]
  // canRead / canUpdate 형태도 허용
  if (action.startsWith('can') && action.length > 3) {
    return `can${action.charAt(3).toUpperCase()}${action.slice(4)}`
  }
  return null
}

function applyPermission(el, binding) {
  const menuStore = useMenuStore()
  const action = resolveAction(binding)
  const key = resolvePermissionKey(action)
  const url = resolveUrl(binding)

  const buttons = menuStore.getButtons(url)
  const allowed = key ? !!buttons[key] : false

  if (binding.modifiers.disable) {
    el.disabled = !allowed
    el.setAttribute('aria-disabled', String(!allowed))
    el.classList.toggle('is-disabled', !allowed)
    el.style.display = ''
    return
  }

  // 기본: 권한 없으면 숨김
  el.style.display = allowed ? '' : 'none'
}

/**
 * 현재 메뉴(또는 지정 URL) 버튼 권한 디렉티브
 *
 * @example
 * <el-button v-can:read>조회</el-button>
 * <el-button v-can="'update'">수정</el-button>
 * <el-button v-can:delete.disable>삭제</el-button>
 * <el-button v-can="{ action: 'read', url: '/special' }">조회</el-button>
 */
export const vCan = {
  mounted(el, binding) {
    applyPermission(el, binding)

    const menuStore = useMenuStore()
    el.__vCanStop = watch(
      () => [menuStore.menus, router.currentRoute.value.fullPath, binding.arg, binding.value],
      () => applyPermission(el, binding),
      { deep: true }
    )
  },
  updated(el, binding) {
    applyPermission(el, binding)
  },
  unmounted(el) {
    el.__vCanStop?.()
    delete el.__vCanStop
  },
}

export function createCanDirectivePlugin() {
  return {
    install(app) {
      app.directive('can', vCan)
    },
  }
}
