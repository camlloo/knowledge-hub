import { createRouter, createWebHistory } from 'vue-router'
import { loadTokens } from '@/utils/http'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '', redirect: '/files' },
        { path: 'files', name: 'files', component: () => import('@/views/FilesView.vue') },
        { path: 'starred', name: 'starred', component: () => import('@/views/StarredView.vue') },
        { path: 'recent', name: 'recent', component: () => import('@/views/RecentView.vue') },
        { path: 'recycle', name: 'recycle', component: () => import('@/views/RecycleView.vue') },
      ],
    },
  ],
})

/** 登录守卫：无令牌一律回登录页 */
router.beforeEach((to) => {
  if (to.meta.public) return true
  return loadTokens() ? true : { name: 'login' }
})

export default router
