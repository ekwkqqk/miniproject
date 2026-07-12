import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMenuStore } from '@/stores/menu'
import { useI18nStore } from '@/stores/i18n'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/unauthorized',
      name: 'unauthorized',
      component: () => import('@/views/UnauthorizedView.vue'),
      meta: { errorPage: true },
    },
    {
      path: '/forbidden',
      name: 'forbidden',
      component: () => import('@/views/ForbiddenView.vue'),
      meta: { errorPage: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
        },
        {
          path: 'special',
          name: 'special',
          component: () => import('@/views/SpecialView.vue'),
          meta: { requiresMenu: true },
        },
        {
          path: 'admin/users',
          name: 'admin-users',
          component: () => import('@/views/AdminUsersView.vue'),
          meta: { requiresMenu: true },
        },
        {
          path: 'admin/roles',
          name: 'admin-roles',
          component: () => import('@/views/AdminRolesView.vue'),
          meta: { requiresMenu: true },
        },
        {
          path: 'admin/menus',
          name: 'admin-menus',
          component: () => import('@/views/AdminMenusView.vue'),
          meta: { requiresMenu: true },
        },
        {
          path: 'admin/i18n/locales',
          name: 'admin-i18n-locales',
          component: () => import('@/views/AdminI18nLocalesView.vue'),
          meta: { requiresMenu: true },
        },
        {
          path: 'admin/i18n/groups',
          name: 'admin-i18n-groups',
          component: () => import('@/views/AdminI18nGroupsView.vue'),
          meta: { requiresMenu: true },
        },
        {
          path: 'admin/i18n/messages',
          name: 'admin-i18n-messages',
          component: () => import('@/views/AdminI18nMessagesView.vue'),
          meta: { requiresMenu: true },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { errorPage: true },
    },
  ],
})

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
      if (!i18nStore.loaded) {
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

export default router
