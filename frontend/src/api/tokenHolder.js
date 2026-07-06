let accessToken = null
const listeners = new Set()

export function getAccessToken() {
  return accessToken
}

export function setAccessToken(token) {
  accessToken = token
  notifyListeners()
}

export function setAuthSession(token, user) {
  accessToken = token
  notifyListeners(user)
}

export function clearAccessToken() {
  accessToken = null
  notifyListeners(null)
}

export function subscribeAuthListener(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

function notifyListeners(user = undefined) {
  listeners.forEach((listener) => listener({ accessToken, user }))
}
