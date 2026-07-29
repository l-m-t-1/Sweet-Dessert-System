import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import AdminLayout from '../layout/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/login', name: 'login', component: Login },
    {
      path: '/',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: 'home', name: 'home', meta: { title: '经营概览' }, component: () => import('../views/Home.vue') },
        { path: 'dessert', name: 'dessert', meta: { title: '甜品管理' }, component: () => import('../views/DessertManagement.vue') },
        { path: 'category', name: 'category', meta: { title: '分类管理' }, component: () => import('../views/CategoryManagement.vue') },
        { path: 'orders', name: 'orders', meta: { title: '订单管理' }, component: () => import('../views/OrderManagement.vue') },
        { path: 'stock-records', name: 'stock-records', meta: { title: '库存流水' }, component: () => import('../views/StockRecords.vue') },
      ],
    },
  ],
})

router.beforeEach(to => {
  if (to.matched.some(record => record.meta.requiresAuth) && !sessionStorage.getItem('currentUser')) return { name: 'login' }
  if (to.name === 'login' && sessionStorage.getItem('currentUser')) return { name: 'home' }
})
export default router
