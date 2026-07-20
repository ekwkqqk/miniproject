export const approvalRoutes = [
  {
    path: 'approval/inbox',
    name: 'approval-inbox',
    component: () => import('./views/ApprovalInboxView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/notices',
    name: 'approval-notices',
    component: () => import('./views/ApprovalNoticesView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/drafts',
    name: 'approval-drafts',
    component: () => import('./views/ApprovalDraftsView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/documents',
    name: 'approval-documents',
    component: () => import('./views/ApprovalDocumentsView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/documents/new',
    name: 'approval-new',
    component: () => import('./views/ApprovalFormView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/documents/:id/edit',
    name: 'approval-edit',
    component: () => import('./views/ApprovalFormView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/documents/:id',
    name: 'approval-detail',
    component: () => import('./views/ApprovalDetailView.vue'),
    meta: { requiresMenu: true },
  },
]
