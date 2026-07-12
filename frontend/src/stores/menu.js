import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as menusApi from '@/api/menus'
import { getErrorMessage } from '@/api/client'

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

  function getMenuByUrl(url) {
    return flattenLeaves(menus.value).find((menu) => menu.url === url) || null
  }

  function canAccess(url) {
    return !!getMenuByUrl(url)
  }

  function getButtons(url) {
    return getMenuByUrl(url)?.buttons || emptyButtons()
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
