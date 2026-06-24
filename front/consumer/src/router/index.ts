import { createRouter, createWebHashHistory } from 'vue-router'

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
      path: '/trace-result',
      name: 'TraceResult',
      component: () => import('@/views/TraceResult.vue'),
      meta: { title: '溯源结果' }
    },
    {
      path: '/complaint-submit',
      name: 'ComplaintSubmit',
      component: () => import('@/views/ComplaintSubmit.vue'),
      meta: { title: '投诉举报' }
    },
    {
      path: '/complaint-query',
      name: 'ComplaintQuery',
      component: () => import('@/views/ComplaintQuery.vue'),
      meta: { title: '投诉查询' }
    },
    {
      path: '/user-center',
      name: 'UserCenter',
      component: () => import('@/views/UserCenter.vue'),
      meta: { title: '用户中心' }
    },
    {
      path: '/scan',
      name: 'Scanner',
      component: () => import('@/views/ScannerView.vue'),
      meta: { title: '扫一扫' }
    }
  ]
})

router.afterEach((to) => {
  document.title = (to.meta.title as string) || '食品安全追溯'
})

export default router
