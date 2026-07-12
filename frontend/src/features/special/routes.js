export const specialRoutes = [
  {
    path: 'special',
    name: 'special',
    component: () => import('./views/SpecialView.vue'),
    meta: { requiresMenu: true },
  },
]
