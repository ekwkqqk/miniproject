export const ROLES = {
  USER: 'USER',
  SPECIAL_USER: 'SPECIAL_USER',
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
}

export const ROLE_LABELS = {
  USER: '일반사용자',
  SPECIAL_USER: '특별사용자',
  SYSTEM_ADMIN: '시스템관리자',
}

export function getRoleLabel(role) {
  return ROLE_LABELS[role] || role
}

export function hasRole(user, ...roles) {
  return roles.includes(user?.role)
}

export function canAccessSpecial(user) {
  return hasRole(user, ROLES.SPECIAL_USER, ROLES.SYSTEM_ADMIN)
}

export function canAccessAdmin(user) {
  return hasRole(user, ROLES.SYSTEM_ADMIN)
}
