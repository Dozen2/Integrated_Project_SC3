import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue';
import * as Icons from '@ant-design/icons-vue'


const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.use(Antd)
app.mount('#app')
Object.keys(Icons).forEach(key => {
  app.component(key, Icons[key])
})