/** 결재함·기안 작성 메뉴 — 문서 상세/작성/수정은 이 중 하나만 있어도 접근 가능 */
const APPROVAL_MENU_ANY_OF = [
  '/approval/submitted',
  '/approval/held',
  '/approval/pending',
  '/approval/upcoming',
  '/approval/completed',
  '/approval/notices',
  '/approval/documents/new',
]

export const approvalRoutes = [
  {
    path: 'approval/submitted',
    name: 'approval-submitted',
    component: () => import('./views/ApprovalSubmittedView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/held',
    name: 'approval-held',
    component: () => import('./views/ApprovalHeldView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/pending',
    name: 'approval-pending',
    component: () => import('./views/ApprovalPendingView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/upcoming',
    name: 'approval-upcoming',
    component: () => import('./views/ApprovalUpcomingView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/completed',
    name: 'approval-completed',
    component: () => import('./views/ApprovalCompletedView.vue'),
    meta: { requiresMenu: true },
  },
  {
    path: 'approval/notices',
    name: 'approval-notices',
    component: () => import('./views/ApprovalNoticesView.vue'),
    meta: { requiresMenu: true },
  },
  // legacy redirects
  {
    path: 'approval/inbox',
    redirect: { name: 'approval-pending' },
  },
  {
    path: 'approval/drafts',
    redirect: { name: 'approval-submitted' },
  },
  {
    path: 'approval/documents',
    redirect: { name: 'approval-submitted' },
  },
  {
    path: 'approval/documents/new',
    name: 'approval-new',
    component: () => import('./views/ApprovalFormView.vue'),
    meta: { requiresMenu: true, menuAnyOf: APPROVAL_MENU_ANY_OF },
  },
  {
    path: 'approval/documents/:id/edit',
    name: 'approval-edit',
    component: () => import('./views/ApprovalFormView.vue'),
    meta: { requiresMenu: true, menuAnyOf: APPROVAL_MENU_ANY_OF },
  },
  {
    path: 'approval/documents/:id',
    name: 'approval-detail',
    component: () => import('./views/ApprovalDetailView.vue'),
    meta: { requiresMenu: true, menuAnyOf: APPROVAL_MENU_ANY_OF },
  },
]
