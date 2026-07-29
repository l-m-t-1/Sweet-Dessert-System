<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand"><span>🍫</span><div><strong>Sweet Dessert</strong><small>甜品管理系统</small></div></div>
      <el-menu router :default-active="route.path" class="menu">
        <el-menu-item index="/home">▦　经营概览</el-menu-item>
        <el-menu-item index="/dessert">🍰　甜品管理</el-menu-item>
        <el-menu-item index="/category">▤　分类管理</el-menu-item>
      </el-menu>
      <div class="sidebar-note">用数据记录每一份甜蜜</div>
    </aside>
    <section class="main">
      <header class="topbar">
        <div><span class="eyebrow">SWEET DESSERT</span><h1>{{ route.meta.title }}</h1></div>
        <el-dropdown>
          <button class="user"><span class="avatar">{{ user.username?.[0]?.toUpperCase() }}</span><span>{{ user.username }}</span>⌄</button>
          <template #dropdown>
            <el-dropdown-menu><el-dropdown-item @click="logout">退出登录</el-dropdown-item></el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <main class="content"><router-view /></main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
const user = computed(() => JSON.parse(sessionStorage.getItem('currentUser') || '{}'))
const logout = () => { sessionStorage.removeItem('currentUser'); router.replace('/login') }
</script>

<style scoped>
.shell{display:flex;min-height:100vh}.sidebar{position:fixed;inset:0 auto 0 0;width:248px;padding:28px 18px;background:#1d1512;border-right:1px solid var(--border);display:flex;flex-direction:column}.brand{display:flex;align-items:center;gap:12px;padding:0 10px 28px}.brand>span{font-size:34px}.brand strong,.brand small{display:block}.brand small{margin-top:3px;color:var(--muted);font-size:12px}.menu{border:0;background:transparent;--el-menu-bg-color:transparent;--el-menu-text-color:#bfaea1;--el-menu-hover-bg-color:#30231f;--el-menu-active-color:var(--amber-strong)}.menu :deep(.el-menu-item){margin:6px 0;border-radius:12px}.menu :deep(.is-active){background:#39291f}.sidebar-note{margin-top:auto;padding:18px 12px;color:#816f62;font-size:12px;border-top:1px solid var(--border)}.main{flex:1;margin-left:248px}.topbar{height:92px;padding:18px 34px;display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid var(--border);background:rgba(23,17,15,.84);backdrop-filter:blur(14px);position:sticky;top:0;z-index:20}.topbar h1{font-size:23px;margin:3px 0 0}.eyebrow{font-size:10px;letter-spacing:2px;color:var(--amber)}.user{border:1px solid var(--border);background:var(--surface);color:var(--text);padding:8px 12px;border-radius:99px;display:flex;align-items:center;gap:9px;cursor:pointer}.avatar{display:grid;place-items:center;width:28px;height:28px;border-radius:50%;background:var(--amber);color:#2b1a11;font-weight:800}.content{padding:28px 34px}@media(max-width:800px){.sidebar{width:76px;padding:24px 8px}.brand div,.sidebar-note{display:none}.brand{padding:0 12px 24px}.menu :deep(.el-menu-item){font-size:0;padding:0 22px}.menu :deep(.el-menu-item)::first-letter{font-size:18px}.main{margin-left:76px}.content{padding:20px}.topbar{padding:16px 20px}}
</style>
