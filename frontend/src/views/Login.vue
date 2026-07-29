<template>
  <div class="login-page">
    <section class="story">
      <div class="badge">HANDCRAFTED MANAGEMENT</div>
      <h1>让每一份甜品，<br><em>都有迹可循。</em></h1>
      <p>从分类、库存到经营数据，用更清晰的方式管理你的甜品事业。</p>
      <div class="quote">“好的管理，是让创意有更多空间。”</div>
    </section>
    <section class="login-card">
      <div class="mark">🍫</div><h2>欢迎回来</h2><p class="muted">登录 Sweet Dessert 管理后台</p>
      <el-form @submit.prevent="submit">
        <el-form-item><el-input v-model="form.username" size="large" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" @keyup.enter="submit" /></el-form-item>
        <el-button native-type="submit" type="primary" size="large" :loading="loading" class="submit">进入管理后台</el-button>
      </el-form>
      <small>© 2026 Sweet Dessert · Crafted with care</small>
    </section>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
const router = useRouter(); const loading = ref(false)
const form = reactive({ username: '', password: '' })
async function submit(){
  if(!form.username.trim() || !form.password){ ElMessage.warning('请输入用户名和密码'); return }
  loading.value=true
  try{ const user=await login(form); sessionStorage.setItem('currentUser',JSON.stringify(user)); ElMessage.success('登录成功'); router.replace('/home') }
  catch(error){ ElMessage.error(error.message) } finally{ loading.value=false }
}
</script>
<style scoped>
.login-page{min-height:100vh;display:grid;grid-template-columns:1.15fr .85fr;align-items:center;padding:7vw;background:radial-gradient(circle at 15% 20%,#4b2c1e 0,transparent 34%),#17110f}.story{max-width:620px}.badge{display:inline-block;padding:8px 12px;border:1px solid var(--border);border-radius:99px;color:var(--amber);font-size:11px;letter-spacing:2px}.story h1{font-family:Georgia,serif;font-size:clamp(46px,6vw,78px);line-height:1.06;margin:28px 0}.story em{color:var(--amber-strong);font-weight:400}.story p{max-width:500px;color:var(--muted);font-size:18px;line-height:1.8}.quote{margin-top:70px;padding-left:18px;border-left:2px solid var(--amber);color:#9e8b7d}.login-card{justify-self:end;width:min(420px,100%);padding:42px;background:rgba(36,26,23,.96);border:1px solid var(--border);border-radius:24px;box-shadow:var(--shadow)}.mark{font-size:42px}.login-card h2{font-size:30px;margin:18px 0 6px}.login-card form{margin-top:30px}.submit{width:100%;margin-top:4px}.login-card small{display:block;text-align:center;margin-top:32px;color:#75655b}@media(max-width:900px){.login-page{grid-template-columns:1fr}.story{display:none}.login-card{justify-self:center}}
</style>
