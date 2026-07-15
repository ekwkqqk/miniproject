import { useAuthStore } from '@/features/auth/store'
import { useMenuStore } from '@/features/menu/store'
import { useI18nStore } from '@/features/i18n/store'
import * as menusApi from '@/features/menu/api'

async function ensureI18nLoaded() {
  const i18nStore = useI18nStore()
  try {
    if (!i18nStore.locales.length) {
      await i18nStore.loadLocales()
    }
    if (!i18nStore.loaded) {
      await i18nStore.loadMessages()
    }
  } catch {
    // ignore
  }
}

export async function setupRouterGuards(router) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const menuStore = useMenuStore()

    // 로그인/가입(guestOnly)에서는 refresh를 치지 않는다.
    // (비로그인 첫 접속 시 예상되는 401을 피함. 쿠키 세션 복원은 보호 라우트 진입 시 수행)
    if (to.meta.requiresAuth) {
      await authStore.restoreSession()
    }

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      return { name: 'login' }
    }

    if (to.meta.guestOnly && authStore.isAuthenticated) {
      return { name: 'dashboard' }
    }

    if (to.meta.requiresAuth && authStore.isAuthenticated && !menuStore.loaded) {
      try {
        await menuStore.fetchMyMenus()
      } catch {
        // ignore
      }
    }

    if (to.meta.requiresAuth && authStore.isAuthenticated) {
      await ensureI18nLoaded()
    }

    // 로그인/비번변경 등 게스트 화면에서도 common 액션 문구 사용
    if (to.meta.guestOnly || to.name === 'change-password') {
      await ensureI18nLoaded()
    }

    if (to.meta.requiresMenu && !menuStore.canAccess(to.path)) {
      return { name: 'forbidden' }
    }

    return true
  })

  router.afterEach((to) => {
    const authStore = useAuthStore()
    const menuStore = useMenuStore()
    if (!authStore.isAuthenticated) return

    const matched = menuStore.getMenuByUrl(to.path)
    if (!matched && !to.meta.requiresMenu) return

    const menuUrl = matched?.url || to.path
    menusApi.reportMenuAccess(menuUrl).catch(() => {
      // ignore logging failures
    })
  })
}
