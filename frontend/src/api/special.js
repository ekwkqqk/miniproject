import client from './client'

export function getSpecialInfo() {
  return client.get('/special/info')
}
