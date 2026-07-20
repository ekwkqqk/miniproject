import { createRouter, createWebHistory } from 'vue-router'
import { authRoutes } from '@/features/auth/routes'
import { dashboardRoutes } from '@/features/dashboard/routes'
import { specialRoutes } from '@/features/special/routes'
import { adminRoutes } from '@/features/admin/routes'
import { menuRoutes } from '@/features/menu/routes'
import { i18nRoutes } from '@/features/i18n/routes'
import { mailRoutes } from '@/features/mail/routes'
import { settingsRoutes } from '@/features/settings/routes'
import { demoRoutes } from '@/features/demo/routes'
import { approvalRoutes } from '@/features/approval/routes'
import { sharedRoutes } from '@/shared/routes'
import { setupRouterGuards } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...authRoutes,
    {
      path: '/',
      component: () => import('@/shared/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        ...dashboardRoutes,
        ...specialRoutes,
        ...adminRoutes,
        ...menuRoutes,
        ...i18nRoutes,
        ...mailRoutes,
        ...settingsRoutes,
        ...demoRoutes,
        ...approvalRoutes,
      ],
    },
    ...sharedRoutes,
  ],
})

setupRouterGuards(router)

export default router
