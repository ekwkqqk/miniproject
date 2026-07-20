import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/shared/styles/responsive.css'
import '@/shared/styles/dark.css'
import '@/shared/styles/dialog-theme.css'

import App from './App.vue'
import router from './router'
import { createCanDirectivePlugin } from '@/features/menu/directives/can'
import { useSettingsStore } from '@/features/settings/store'

// 이전 localStorage 토큰 방식 잔여 데이터 제거
localStorage.removeItem('accessToken')
localStorage.removeItem('refreshToken')
localStorage.removeItem('user')

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(createCanDirectivePlugin())

const settingsStore = useSettingsStore(pinia)
settingsStore.applyLocalTheme()
settingsStore.loadPublicSettings()

app.mount('#app')
