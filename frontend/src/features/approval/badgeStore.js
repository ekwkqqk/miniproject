import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as approvalApi from './api'

export const useApprovalBadgeStore = defineStore('approvalBadges', () => {
  const pending = ref(0)
  const held = ref(0)
  const upcoming = ref(0)
  const notices = ref(0)
  let timer = null

  async function refresh() {
    try {
      const { data } = await approvalApi.getBadges()
      if (data.success && data.data) {
        pending.value = Number(data.data.pending ?? data.data.inbox) || 0
        held.value = Number(data.data.held) || 0
        upcoming.value = Number(data.data.upcoming) || 0
        notices.value = Number(data.data.notices) || 0
      }
    } catch {
      /* ignore */
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
    pending.value = 0
    held.value = 0
    upcoming.value = 0
    notices.value = 0
  }

  function badgeForUrl(url) {
    if (url === '/approval/pending' || url === '/approval/inbox') return pending.value
    if (url === '/approval/held') return held.value
    if (url === '/approval/upcoming') return upcoming.value
    if (url === '/approval/notices') return notices.value
    return 0
  }

  return { pending, held, upcoming, notices, refresh, startPolling, stopPolling, clear, badgeForUrl }
})
