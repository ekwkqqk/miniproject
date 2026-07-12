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

export const useMenuStore = defineStore('menu', () => {
  const menus = ref([])
  const loaded = ref(false)

  async function fetchMyMenus() {
    try {
      const { data } = await menusApi.getMyMenus()
      if (data.success) {
        menus.value = data.data || []
      }
    } catch (error) {
      menus.value = []
      throw new Error(getErrorMessage(error))
    } finally {
      loaded.value = true
    }
  }

  function clearMenus() {
    menus.value = []
    loaded.value = false
  }

  function resolveMenu(url) {
    const leaves = flattenLeaves(menus.value)
    const exact = leaves.find((menu) => menu.url === url)
    if (exact) return exact
    // /demo/view/1001 → /demo/view
    return leaves
      .filter((menu) => url.startsWith(`${menu.url}/`))
      .sort((a, b) => b.url.length - a.url.length)[0] || null
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
