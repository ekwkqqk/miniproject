export const menuRoutes = [
  {
    path: 'admin/menus',
    name: 'admin-menus',
    component: () => import('./views/AdminMenusView.vue'),
    meta: { requiresMenu: true },
  },
]
