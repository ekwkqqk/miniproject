export const adminRoutes = [
  {
    path: 'admin/users',
    name: 'admin-users',
    component: () => import('./views/AdminUsersView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'admin/roles',
    name: 'admin-roles',
    component: () => import('./views/AdminRolesView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'admin/menu-access-logs',
    name: 'admin-menu-access-logs',
    component: () => import('./views/AdminMenuAccessLogsView.vue'),
    meta: { requiresMenu: true },
  },
]
