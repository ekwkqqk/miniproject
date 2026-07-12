export const mailRoutes = [
  {
    path: 'admin/mail/templates',
    name: 'admin-mail-templates',
    component: () => import('./views/AdminMailTemplatesView.vue'),
    meta: { requiresMenu: true },
  },
]
