export const i18nRoutes = [
  {
    path: 'admin/i18n/locales',
    name: 'admin-i18n-locales',
    component: () => import('./views/AdminI18nLocalesView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'admin/i18n/groups',
    name: 'admin-i18n-groups',
    component: () => import('./views/AdminI18nGroupsView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'admin/i18n/messages',
    name: 'admin-i18n-messages',
    component: () => import('./views/AdminI18nMessagesView.vue'),
    meta: { requiresMenu: true },
  },
]
