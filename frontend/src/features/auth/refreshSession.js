import axios from 'axios'
import { getAccessToken, setAuthSession, clearAccessToken } from './tokenHolder'

/** 시계 오차·경계 만료를 피하기 위한 여유(ms) */
const EXPIRY_SKEW_MS = 10_000

let refreshPromise = null

export function isAccessTokenExpired(token = getAccessToken()) {
  if (!token) return true
  try {
    const [, payloadPart] = token.split('.')
    if (!payloadPart) return true
    const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(normalized))
    if (!payload.exp) return true
    return payload.exp * 1000 <= Date.now() + EXPIRY_SKEW_MS
  } catch {
    return true
  }
}

/**
 * refresh cookie로 access token 갱신.
 * 동시 호출은 하나의 요청으로 합친다 (refresh token rotation 충돌 방지).
 * @returns {Promise<{ accessToken: string, user: object }>}
 */
export function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post('/api/auth/refresh', null, { withCredentials: true })
      .then(({ data }) => {
        const payload = data?.data
        if (!payload?.accessToken) {
          throw new Error('토큰 갱신에 실패했습니다.')
        }
        setAuthSession(payload.accessToken, payload.user)
        return payload
      })
      .catch((error) => {
        clearAccessToken()
        throw error
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}
