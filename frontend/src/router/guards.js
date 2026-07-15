import { useAuthStore } from '@/features/auth/store'
import { useMenuStore } from '@/features/menu/store'
import { useI18nStore } from '@/features/i18n/store'
import * as menusApi from '@/features/menu/api'

export async function setupRouterGuards(router) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const menuStore = useMenuStore()

    if (to.meta.requiresAuth || to.meta.guestOnly) {
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
      const i18nStore = useI18nStore()
      try {
        if (!i18nStore.locales.length) {
          await i18nStore.loadLocales()
        }
        // table/menu 키가 없으면 번들 재로딩 (시드 반영 후 구버전 캐시 방지)
        const msgs = i18nStore.messages || {}
        if (!i18nStore.loaded || !msgs['table.id'] || !msgs['menu.dashboard'] || !msgs['common.changePassword'] || !msgs['common.refresh']) {
          await i18nStore.loadMessages()
        }
      } catch {
        // ignore
      }
    }

    // 로그인/비번변경 등 게스트 화면에서도 common 액션 문구 사용
    if (to.meta.guestOnly || to.name === 'change-password') {
      const i18nStore = useI18nStore()
      try {
        if (!i18nStore.locales.length) {
          await i18nStore.loadLocales()
        }
        const msgs = i18nStore.messages || {}
        if (!i18nStore.loaded || !msgs['common.changePassword'] || !msgs['common.logout']) {
          await i18nStore.loadMessages()
        }
      } catch {
        // ignore
      }
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
