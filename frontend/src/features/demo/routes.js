export const demoRoutes = [
  {
    path: 'demo/search',
    name: 'demo-search',
    component: () => import('./views/DemoSearchView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'demo/view/:id?',
    name: 'demo-view',
    component: () => import('./views/DemoViewView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'demo/edit/:id?',
    name: 'demo-edit',
    component: () => import('./views/DemoEditView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'demo/popup',
    name: 'demo-popup',
    component: () => import('./views/DemoPopupView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'demo/upload',
    name: 'demo-upload',
    component: () => import('./views/DemoUploadView.vue'),
    meta: { requiresMenu: true },
  },
]
