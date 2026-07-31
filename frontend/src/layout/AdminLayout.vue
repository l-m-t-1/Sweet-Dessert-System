<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand"><span>🍰</span><div><strong>Sweet Dessert</strong><small>甜品管理系统</small></div></div>
      <el-menu router :default-active="route.path" class="menu">
        <el-menu-item index="/admin/home">◆　经营概览</el-menu-item>
        <el-menu-item index="/admin/desserts">♟　甜品管理</el-menu-item>
        <el-menu-item index="/admin/categories">▦　分类管理</el-menu-item>
        <el-menu-item index="/admin/orders">▤　订单管理</el-menu-item>
        <el-menu-item index="/admin/stock-records">↕　库存流水</el-menu-item>
        <el-menu-item index="/admin/users">♙　用户管理</el-menu-item>
      </el-menu>
      <router-link class="store-link" to="/shop">查看顾客商城 →</router-link>
    </aside>
    <section class="main">
      <header class="topbar">
        <div><span class="eyebrow">ADMIN CONSOLE</span><h1>{{ route.meta.title }}</h1></div>
        <el-dropdown>
          <button class="user"><span class="avatar">{{ user.username?.[0]?.toUpperCase() }}</span><span>{{ user.username }}</span>⌄</button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item @click="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
      </header>
      <main class="content"><router-view /></main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession, readSession } from '../auth/session'

const route = useRoute()
const router = useRouter()
const user = computed(() => readSession() || {})
const logout = () => {
  clearSession()
  router.replace('/login')
}
</script>

<style scoped>
.shell{display:flex;min-height:100vh}.sidebar{position:fixed;inset:0 auto 0 0;width:248px;padding:28px 18px;background:#1d1512;border-right:1px solid var(--border);display:flex;flex-direction:column}.brand{display:flex;align-items:center;gap:12px;padding:0 10px 28px}.brand>span{font-size:34px}.brand strong,.brand small{display:block}.brand small{margin-top:3px;color:var(--muted);font-size:12px}.menu{border:0;background:transparent;--el-menu-bg-color:transparent;--el-menu-text-color:#bfaea1;--el-menu-hover-bg-color:#30231f;--el-menu-active-color:var(--amber-strong)}.menu :deep(.el-menu-item){margin:5px 0;border-radius:12px}.menu :deep(.is-active){background:#39291f}.store-link{margin-top:auto;padding:18px 12px;color:var(--amber);font-size:13px;border-top:1px solid var(--border);text-decoration:none}.main{flex:1;margin-left:248px}.topbar{height:92px;padding:18px 34px;display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid var(--border);background:rgba(23,17,15,.84);position:sticky;top:0;z-index:20;backdrop-filter:blur(12px)}.topbar h1{font-size:23px;margin:3px 0 0}.eyebrow{font-size:10px;letter-spacing:2px;color:var(--amber)}.user{border:1px solid var(--border);background:var(--surface);color:var(--text);padding:8px 12px;border-radius:99px;display:flex;align-items:center;gap:9px}.avatar{display:grid;place-items:center;width:28px;height:28px;border-radius:50%;background:var(--amber);color:#2b1a11;font-weight:800}.content{padding:28px 34px}@media(max-width:800px){.sidebar{width:76px;padding:24px 8px}.brand div,.store-link{display:none}.main{margin-left:76px}.content{padding:20px}.topbar{padding:16px 20px}.menu :deep(.el-menu-item){font-size:0}}
</style>
