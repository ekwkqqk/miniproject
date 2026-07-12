export const dashboardRoutes = [
  {
    path: '',
    redirect: { name: 'dashboard' },
  },
  {
    path: 'dashboard',
    name: 'dashboard',
    component: () => import('./views/DashboardView.vue'),
  },
]
