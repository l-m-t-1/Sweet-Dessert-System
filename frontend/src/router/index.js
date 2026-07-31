import { createRouter, createWebHistory } from 'vue-router'
import { canAccess, homeForRole } from '../auth/access'
import { readSession } from '../auth/session'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import StoreLayout from '../layout/StoreLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/shop' },
    { path: '/login', name: 'login', component: Login },
    { path: '/register', name: 'register', component: Register },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        { path: '', redirect: '/admin/home' },
        { path: 'home', name: 'admin-home', meta: { title: '经营概览' }, component: () => import('../views/Home.vue') },
        { path: 'desserts', name: 'admin-desserts', meta: { title: '甜品管理' }, component: () => import('../views/DessertManagement.vue') },
        { path: 'categories', name: 'admin-categories', meta: { title: '分类管理' }, component: () => import('../views/CategoryManagement.vue') },
        { path: 'orders', name: 'admin-orders', meta: { title: '订单管理' }, component: () => import('../views/OrderManagement.vue') },
        { path: 'stock-records', name: 'admin-stock', meta: { title: '库存流水' }, component: () => import('../views/StockRecords.vue') },
        { path: 'users', name: 'admin-users', meta: { title: '用户管理' }, component: () => import('../views/UserManagement.vue') },
      ],
    },
    {
      path: '/',
      component: StoreLayout,
      children: [
        { path: 'shop', name: 'shop', component: () => import('../views/Store.vue') },
        { path: 'cart', name: 'cart', meta: { requiresAuth: true, role: 'USER' }, component: () => import('../views/Cart.vue') },
        { path: 'my-orders', name: 'my-orders', meta: { requiresAuth: true, role: 'USER' }, component: () => import('../views/MyOrders.vue') },
      ],
    },
    { path: '/home', redirect: '/admin/home' },
    { path: '/dessert', redirect: '/admin/desserts' },
    { path: '/category', redirect: '/admin/categories' },
    { path: '/orders', redirect: '/admin/orders' },
    { path: '/stock-records', redirect: '/admin/stock-records' },
    { path: '/:pathMatch(.*)*', redirect: '/shop' },
  ],
})

router.beforeEach(to => {
  const session = readSession()
  const requiredRole = to.matched.map(record => record.meta.role).find(Boolean)
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  if (requiresAuth && !session) return { name: 'login', query: { redirect: to.fullPath } }
  if (requiredRole && !canAccess(session?.role, requiredRole)) {
    return session ? homeForRole(session.role) : { name: 'login' }
  }
  if ((to.name === 'login' || to.name === 'register') && session) {
    return homeForRole(session.role)
  }
})

export default router
