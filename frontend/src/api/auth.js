import client from './client'

export function register(payload) {
  return client.post('/auth/register', payload)
}

export function login(payload) {
  return client.post('/auth/login', payload)
}

export function refresh(refreshToken) {
  return client.post('/auth/refresh', { refreshToken })
}

export function logout(refreshToken) {
  return client.post('/auth/logout', { refreshToken })
}

export function getMe() {
  return client.get('/users/me')
}
