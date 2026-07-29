<template>
  <div class="page">
    <div class="welcome"><div><span class="muted">今日经营数据</span><h2>欢迎回来，{{ username }}</h2><p class="muted">库存与甜品信息均来自实时数据库。</p></div><el-button type="primary" @click="$router.push('/dessert')">管理甜品</el-button></div>
    <el-skeleton :loading="loading" animated :rows="5">
      <template #default>
        <div v-if="error" class="panel error">{{ error }} <el-button @click="load">重新加载</el-button></div>
        <template v-else>
          <div class="metrics">
            <article v-for="item in metrics" :key="item.label" class="metric"><span>{{ item.icon }}</span><div><strong>{{ item.value }}</strong><small>{{ item.label }}</small></div></article>
          </div>
          <div class="grid">
            <section class="panel"><div class="section-head"><div><h3>低库存提醒</h3><p class="muted">库存不超过 5 件的甜品</p></div><el-button text @click="$router.push('/dessert')">查看全部 →</el-button></div>
              <el-empty v-if="!summary.lowStockDesserts?.length" description="库存状态良好" />
              <div v-else class="stock-row" v-for="item in summary.lowStockDesserts" :key="item.id"><div class="thumb">{{ item.image ? '🍰' : '🍮' }}</div><strong>{{ item.name }}</strong><el-tag type="danger">{{ item.stock }} 件</el-tag></div>
            </section>
            <section class="panel quick"><h3>快捷操作</h3><button @click="$router.push('/dessert')"><span>＋</span><div><strong>新增甜品</strong><small>录入产品、价格与库存</small></div></button><button @click="$router.push('/category')"><span>▤</span><div><strong>管理分类</strong><small>整理甜品分类结构</small></div></button></section>
          </div>
        </template>
      </template>
    </el-skeleton>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getDashboardSummary } from '../api/dashboard'
const loading=ref(true), error=ref(''), summary=reactive({})
const username=JSON.parse(sessionStorage.getItem('currentUser')||'{}').username||'管理员'
const metrics=computed(()=>[
  {label:'甜品总数',value:summary.dessertCount||0,icon:'🍰'},
  {label:'分类数量',value:summary.categoryCount||0,icon:'▤'},
  {label:'当前总库存',value:summary.totalStock||0,icon:'▣'},
  {label:'低库存项目',value:summary.lowStockCount||0,icon:'!'},
])
async function load(){loading.value=true;error.value='';try{Object.assign(summary,await getDashboardSummary())}catch(e){error.value=e.message}finally{loading.value=false}}
onMounted(load)
</script>
<style scoped>
.welcome{padding:10px 4px 6px;display:flex;justify-content:space-between;align-items:end}.welcome h2{font-family:Georgia,serif;font-size:31px;margin:7px 0}.metrics{display:grid;grid-template-columns:repeat(4,1fr);gap:16px}.metric{display:flex;gap:16px;align-items:center;padding:22px;background:linear-gradient(145deg,#2b1f1b,#211815);border:1px solid var(--border);border-radius:16px}.metric>span{display:grid;place-items:center;width:46px;height:46px;border-radius:13px;background:#3b2b22;color:var(--amber);font-size:21px}.metric strong,.metric small{display:block}.metric strong{font-size:27px;color:var(--amber-strong)}.metric small{margin-top:4px;color:var(--muted)}.grid{display:grid;grid-template-columns:1.7fr 1fr;gap:18px}.section-head{display:flex;justify-content:space-between}.section-head h3,.quick h3{margin:0 0 5px}.stock-row{display:grid;grid-template-columns:42px 1fr auto;align-items:center;gap:12px;padding:13px 0;border-bottom:1px solid var(--border)}.thumb{width:38px;height:38px;display:grid;place-items:center;background:#382820;border-radius:10px}.quick button{width:100%;display:flex;align-items:center;gap:13px;text-align:left;padding:15px;margin-top:12px;border:1px solid var(--border);border-radius:13px;background:var(--surface-2);color:var(--text);cursor:pointer}.quick button>span{font-size:22px;color:var(--amber)}.quick strong,.quick small{display:block}.quick small{color:var(--muted);margin-top:3px}.error{display:flex;justify-content:space-between;align-items:center}@media(max-width:1050px){.metrics{grid-template-columns:repeat(2,1fr)}.grid{grid-template-columns:1fr}}@media(max-width:600px){.metrics{grid-template-columns:1fr}.welcome{align-items:start}.welcome .el-button{display:none}}
</style>
