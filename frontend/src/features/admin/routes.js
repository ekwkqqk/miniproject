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
]
