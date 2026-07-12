export const sharedRoutes = [
  {
    path: '/unauthorized',
    name: 'unauthorized',
    component: () => import('./views/UnauthorizedView.vue'),
    meta: { errorPage: true },
  },
  {
    path: '/forbidden',
    name: 'forbidden',
    component: () => import('./views/ForbiddenView.vue'),
    meta: { errorPage: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('./views/NotFoundView.vue'),
    meta: { errorPage: true },
  },
]
