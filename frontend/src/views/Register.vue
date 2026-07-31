<template>
  <div class="register-page">
    <section class="register-card">
      <router-link to="/shop" class="back">← 返回甜品商城</router-link>
      <div class="mark">🍮</div>
      <h1>创建顾客账户</h1>
      <p class="muted">只需用户名和密码，就能保存并管理自己的订单。</p>
      <el-form @submit.prevent="submit">
        <el-form-item label="用户名"><el-input v-model="form.username" size="large" maxlength="30" placeholder="3–30 个字符" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" size="large" type="password" show-password placeholder="6–72 个字符" @keyup.enter="submit" /></el-form-item>
        <el-button native-type="submit" type="primary" size="large" :loading="loading">注册账户</el-button>
      </el-form>
      <p>已有账户？<router-link to="/login">直接登录</router-link></p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function submit() {
  if (form.username.trim().length < 3 || form.password.length < 6) {
    ElMessage.warning('用户名至少 3 个字符，密码至少 6 个字符')
    return
  }
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page{min-height:100vh;display:grid;place-items:center;padding:24px;background:radial-gradient(circle at 80% 10%,#533121 0,transparent 35%),var(--bg)}.register-card{width:min(460px,100%);padding:42px;background:var(--surface);border:1px solid var(--border);border-radius:24px;box-shadow:var(--shadow)}.back,.register-card a{color:var(--amber-strong);text-decoration:none}.mark{font-size:44px;margin-top:28px}.register-card h1{margin:12px 0 6px}.register-card form{margin:30px 0}.register-card .el-button{width:100%}
</style>
