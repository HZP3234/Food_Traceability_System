import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import './styles/admin-redesign.css'
import './styles/admin-refined.css'
import './styles/admin-operations.css'
import './styles/admin-shell-fixes.css'
import './styles/components.css'
import './styles/trace-page.css'
import App from './App.vue'

createApp(App).use(ElementPlus).mount('#app')
