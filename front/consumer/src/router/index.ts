import { createRouter, createWebHashHistory } from 'vue-router'
import { useAppStore } from '@/store/app'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { title: '食品安全追溯' }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/trace-result',
      name: 'TraceResult',
      component: () => import('@/views/TraceResult.vue'),
      meta: { title: '溯源结果' }
    },
    {
      path: '/complaint-submit',
      name: 'ComplaintSubmit',
      component: () => import('@/views/ComplaintSubmit.vue'),
      meta: { title: '投诉举报', needLogin: true }
    },
    {
      path: '/complaint-query',
      name: 'ComplaintQuery',
      component: () => import('@/views/ComplaintQuery.vue'),
      meta: { title: '投诉查询', needLogin: true }
    },
    {
      path: '/profile-edit',
      name: 'ProfileEdit',
      component: () => import('@/views/ProfileEdit.vue'),
      meta: { title: '编辑资料', needLogin: true }
    },
    {
      path: '/user-center',
      name: 'UserCenter',
      component: () => import('@/views/UserCenter.vue'),
      meta: { title: '用户中心', needLogin: true }
    },
    {
      path: '/scan',
      name: 'Scanner',
      component: () => import('@/views/ScannerView.vue'),
      meta: { title: '扫一扫', needLogin: true }
    }
  ]
})

router.beforeEach((to, _from) => {
  if (to.meta.needLogin) {
    const store = useAppStore()
    if (!store.isLoggedIn) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }
  }
})

router.afterEach((to) => {
  document.title = (to.meta.title as string) || '食品安全追溯'
})

export default router
