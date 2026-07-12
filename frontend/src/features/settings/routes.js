export const settingsRoutes = [
  {
    path: 'admin/settings',
    name: 'admin-settings',
    component: () => import('./views/AdminSettingsView.vue'),
    meta: { requiresMenu: true },
  },
]
