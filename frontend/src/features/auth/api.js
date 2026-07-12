import client from '@/shared/api/client'

export function register(payload) {
  return client.post('/auth/register', payload)
}

export function login(payload) {
  return client.post('/auth/login', payload)
}

export function refresh() {
  return client.post('/auth/refresh')
}

export function logout() {
  return client.post('/auth/logout')
}

export function changePassword(payload) {
  return client.post('/auth/change-password', payload)
}

export function getMe() {
  return client.get('/users/me')
}
