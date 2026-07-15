import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as menusApi from '@/features/menu/api'
import { getErrorMessage } from '@/shared/api/client'

const emptyButtons = () => ({
  canRead: false,
  canUpdate: false,
  canDelete: false,
  canUpload: false,
  canDownload: false,
  canOther: false,
})

function flattenLeaves(nodes, result = []) {
  nodes.forEach((node) => {
    if (!node.folder && node.url) {
      result.push(node)
    }
    if (node.children?.length) {
      flattenLeaves(node.children, result)
    }
  })
  return result
}

function buildLeafIndex(nodes) {
  const byUrl = new Map()
  const leaves = flattenLeaves(nodes)
  for (const leaf of leaves) {
    byUrl.set(leaf.url, leaf)
  }
  // longest URLs first for prefix matching
  const sortedByLength = [...leaves].sort((a, b) => b.url.length - a.url.length)
  return { byUrl, sortedByLength }
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref([])
  const loaded = ref(false)
  let leafIndex = buildLeafIndex([])

  function rebuildLeafIndex() {
    leafIndex = buildLeafIndex(menus.value)
  }

  async function fetchMyMenus() {
    try {
      const { data } = await menusApi.getMyMenus()
      if (data.success) {
        menus.value = data.data || []
        rebuildLeafIndex()
      }
    } catch (error) {
      menus.value = []
      rebuildLeafIndex()
      throw new Error(getErrorMessage(error))
    } finally {
      loaded.value = true
    }
  }

  function clearMenus() {
    menus.value = []
    loaded.value = false
    rebuildLeafIndex()
  }

  function resolveMenu(url) {
    const exact = leafIndex.byUrl.get(url)
    if (exact) return exact
    // /demo/view/1001 → /demo/view
    return leafIndex.sortedByLength.find((menu) => url.startsWith(`${menu.url}/`)) || null
  }

  function getMenuByUrl(url) {
    return resolveMenu(url)
  }

  function canAccess(url) {
    return !!resolveMenu(url)
  }

  function getButtons(url) {
    return resolveMenu(url)?.buttons || emptyButtons()
  }

  return {
    menus,
    loaded,
    fetchMyMenus,
    clearMenus,
    getMenuByUrl,
    canAccess,
    getButtons,
  }
})
