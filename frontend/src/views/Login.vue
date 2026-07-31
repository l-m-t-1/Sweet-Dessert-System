<template>
  <div class="auth-page">
    <section class="auth-story">
      <div class="badge">SWEET DESSERT</div>
      <h1>一间甜品店，<br><em>两种精彩体验。</em></h1>
      <p>顾客可以浏览甜品、加入购物车并查看自己的订单；管理员则管理商品、库存、订单与用户。</p>
      <router-link to="/shop" class="text-link">先逛逛今日甜品 →</router-link>
    </section>
    <section class="auth-card">
      <div class="mark">🍰</div>
      <h2>欢迎回来</h2>
      <p class="muted">登录后将自动进入与你身份对应的页面</p>
      <el-form @submit.prevent="submit">
        <el-form-item><el-input v-model="form.username" size="large" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" @keyup.enter="submit" /></el-form-item>
        <el-button native-type="submit" type="primary" size="large" :loading="loading" class="submit">登录</el-button>
      </el-form>
      <p class="switch">还没有账户？<router-link to="/register">立即注册</router-link></p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { homeForRole } from '../auth/access'
import { saveSession } from '../auth/session'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function submit() {
  if (!form.username.trim() || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const session = await login(form)
    saveSession(session)
    ElMessage.success('登录成功')
    const requested = typeof route.query.redirect === 'string' ? route.query.redirect : null
    router.replace(requested || homeForRole(session.role))
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page{min-height:100vh;display:grid;grid-template-columns:1.15fr .85fr;align-items:center;padding:7vw;background:radial-gradient(circle at 15% 20%,#4b2c1e 0,transparent 34%),#17110f}.auth-story{max-width:620px}.badge{display:inline-block;padding:8px 12px;border:1px solid var(--border);border-radius:99px;color:var(--amber);font-size:11px;letter-spacing:2px}.auth-story h1{font-family:Georgia,"Microsoft YaHei",serif;font-size:clamp(46px,6vw,78px);line-height:1.06;margin:28px 0}.auth-story em{color:var(--amber-strong);font-weight:400}.auth-story p{max-width:540px;color:var(--muted);font-size:18px;line-height:1.8}.text-link,.switch a{color:var(--amber-strong);text-decoration:none}.text-link{display:inline-block;margin-top:28px}.auth-card{justify-self:end;width:min(420px,100%);padding:42px;background:rgba(36,26,23,.96);border:1px solid var(--border);border-radius:24px;box-shadow:var(--shadow)}.mark{font-size:42px}.auth-card h2{font-size:30px;margin:18px 0 6px}.auth-card form{margin-top:30px}.submit{width:100%}.switch{text-align:center;margin:26px 0 0;color:var(--muted)}@media(max-width:900px){.auth-page{grid-template-columns:1fr}.auth-story{display:none}.auth-card{justify-self:center}}
</style>
