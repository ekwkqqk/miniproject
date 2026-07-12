import { computed, onMounted, onUnmounted, ref } from 'vue'

export const BREAKPOINTS = {
  mobileMax: 767,
  tabletMin: 768,
  tabletMax: 1023,
  desktopMin: 1024,
}

function resolveDevice(width) {
  if (width <= BREAKPOINTS.mobileMax) return 'mobile'
  if (width <= BREAKPOINTS.tabletMax) return 'tablet'
  return 'desktop'
}

const width = ref(typeof window !== 'undefined' ? window.innerWidth : BREAKPOINTS.desktopMin)
const device = ref(resolveDevice(width.value))

let listenerCount = 0

function onResize() {
  width.value = window.innerWidth
  device.value = resolveDevice(width.value)
}

function bind() {
  if (typeof window === 'undefined') return
  listenerCount += 1
  if (listenerCount === 1) {
    window.addEventListener('resize', onResize, { passive: true })
    onResize()
  }
}

function unbind() {
  if (typeof window === 'undefined') return
  listenerCount = Math.max(0, listenerCount - 1)
  if (listenerCount === 0) {
    window.removeEventListener('resize', onResize)
  }
}

/**
 * Shared responsive breakpoint state for layout and pages.
 * mobile < 768 | tablet 768–1023 | desktop ≥ 1024
 */
export function useBreakpoint() {
  onMounted(bind)
  onUnmounted(unbind)

  const isMobile = computed(() => device.value === 'mobile')
  const isTablet = computed(() => device.value === 'tablet')
  const isDesktop = computed(() => device.value === 'desktop')
  const isCompact = computed(() => device.value !== 'desktop')

  return {
    width,
    device,
    isMobile,
    isTablet,
    isDesktop,
    isCompact,
    BREAKPOINTS,
  }
}
