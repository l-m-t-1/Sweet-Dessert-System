<template>
  <div class="store-shell">
    <header class="store-nav">
      <router-link to="/shop" class="store-brand"><span>🍰</span><strong>Sweet Dessert</strong></router-link>
      <nav>
        <router-link to="/shop">甜品商城</router-link>
        <router-link v-if="session?.role === 'USER'" to="/my-orders">我的订单</router-link>
        <router-link v-if="session?.role === 'USER'" to="/cart">购物车</router-link>
      </nav>
      <div class="account">
        <template v-if="session">
          <span>你好，{{ session.username }}</span>
          <el-button text @click="logout">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register" class="signup">注册</router-link>
        </template>
      </div>
    </header>
    <main><router-view /></main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { clearSession, readSession } from '../auth/session'

const router = useRouter()
const session = computed(() => readSession())
function logout() {
  clearSession()
  router.replace('/shop')
}
</script>

<style scoped>
.store-shell{min-height:100vh;background:radial-gradient(circle at 80% 0,#563524 0,transparent 28%),var(--bg)}.store-nav{height:76px;padding:0 clamp(20px,6vw,88px);display:flex;align-items:center;gap:36px;border-bottom:1px solid var(--border);background:rgba(23,17,15,.84);position:sticky;top:0;z-index:50;backdrop-filter:blur(14px)}.store-brand{display:flex;align-items:center;gap:10px;color:var(--text);text-decoration:none;font-family:Georgia,serif;font-size:20px}.store-brand span{font-size:30px}.store-nav nav{display:flex;gap:24px}.store-nav a{color:var(--muted);text-decoration:none}.store-nav .router-link-active{color:var(--amber-strong)}.account{margin-left:auto;display:flex;align-items:center;gap:14px;color:var(--muted);font-size:14px}.signup{padding:9px 16px;border:1px solid var(--amber);border-radius:99px;color:var(--amber-strong)!important}@media(max-width:720px){.store-nav{padding:0 16px;gap:16px}.store-brand strong,.account>span{display:none}.store-nav nav{gap:12px;font-size:13px}}
</style>
