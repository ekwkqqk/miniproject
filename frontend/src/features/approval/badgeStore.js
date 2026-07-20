import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as approvalApi from './api'

export const useApprovalBadgeStore = defineStore('approvalBadges', () => {
  const inbox = ref(0)
  const notices = ref(0)
  let timer = null

  async function refresh() {
    try {
      const { data } = await approvalApi.getBadges()
      if (data.success && data.data) {
        inbox.value = Number(data.data.inbox) || 0
        notices.value = Number(data.data.notices) || 0
      }
    } catch {
      /* ignore — menu may load before login finishes */
    }
  }

  function startPolling(intervalMs = 60000) {
    if (timer) {
      refresh()
      return
    }
    refresh()
    timer = setInterval(refresh, intervalMs)
  }

  function stopPolling() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  function clear() {
    stopPolling()
    inbox.value = 0
    notices.value = 0
  }

  function badgeForUrl(url) {
    if (url === '/approval/inbox') return inbox.value
    if (url === '/approval/notices') return notices.value
    return 0
  }

  return { inbox, notices, refresh, startPolling, stopPolling, clear, badgeForUrl }
})
