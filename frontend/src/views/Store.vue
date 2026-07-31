<template>
  <div class="store-page">
    <section class="hero">
      <div>
        <span class="eyebrow">FRESHLY MADE · DAILY JOY</span>
        <h1>把今天，过得<br><em>甜一点。</em></h1>
        <p>从醇厚巧克力到清新水果风味，每一款甜品都值得慢慢挑选。</p>
      </div>
      <div class="hero-art">🍫<span>🍓</span><small>今日甜度<br><b>刚刚好</b></small></div>
    </section>

    <section class="catalog">
      <div class="catalog-head">
        <div><span class="eyebrow">OUR DESSERTS</span><h2>精选甜品</h2></div>
        <div class="filters">
          <el-select v-model="categoryId" clearable placeholder="全部分类" @change="applyFilters">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
          <el-input v-model="keyword" clearable placeholder="搜索甜品" class="search" @input="applyFilters" />
        </div>
      </div>
      <div v-loading="loading" class="dessert-grid">
        <article v-for="item in records" :key="item.id" class="dessert-card">
          <div class="dessert-image">
            <img v-if="item.image" :src="item.image" :alt="item.name">
            <span v-else>🍰</span>
            <div v-if="item.stock <= 0" class="sold-out">已售罄</div>
          </div>
          <div class="dessert-info">
            <small>{{ item.categoryName || '经典甜品' }}</small>
            <h3>{{ item.name }}</h3>
            <p>{{ item.description || '新鲜制作，愿这一口甜为你带来好心情。' }}</p>
            <div><strong>¥ {{ money(item.price) }}</strong><el-button type="primary" circle :disabled="item.stock <= 0" @click="add(item)">＋</el-button></div>
          </div>
        </article>
        <el-empty v-if="!loading && !records.length" description="没有找到甜品" />
      </div>
      <el-pagination v-if="total > size" v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="load" />
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageStoreDesserts } from '../api/store'
import { listCategories } from '../api/category'
import { addCartItem, readCart, saveCart } from '../cart/cart'

const records = ref([])
const categories = ref([])
const categoryId = ref(null)
const keyword = ref('')
const loading = ref(false)
const page = ref(1)
const size = 12
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const result = await pageStoreDesserts({
      page: page.value,
      size,
      name: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
    })
    records.value = result.records
    total.value = result.total
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  page.value = 1
  load()
}

function add(item) {
  saveCart(addCartItem(readCart(), item))
  ElMessage.success(`已将「${item.name}」加入购物车`)
}

const money = value => Number(value).toFixed(2)
onMounted(async () => {
  try {
    categories.value = await listCategories()
  } catch (error) {
    ElMessage.error(error.message)
  }
  await load()
})
</script>

<style scoped>
.store-page{padding-bottom:80px}.hero{min-height:510px;padding:70px clamp(24px,8vw,130px);display:grid;grid-template-columns:1.1fr .9fr;align-items:center;gap:40px}.eyebrow{font-size:11px;letter-spacing:3px;color:var(--amber)}.hero h1{font-family:Georgia,"Microsoft YaHei",serif;font-size:clamp(58px,7vw,96px);line-height:1.02;margin:22px 0}.hero em{color:var(--amber-strong);font-weight:400}.hero p{max-width:520px;color:var(--muted);font-size:18px;line-height:1.8}.hero-art{justify-self:center;font-size:clamp(100px,13vw,190px);filter:drop-shadow(0 28px 32px #0008);position:relative;transform:rotate(-8deg)}.hero-art>span{font-size:.42em;position:absolute;right:-10px;top:0}.hero-art small{position:absolute;right:-45px;bottom:-26px;font-size:13px;line-height:1.5;transform:rotate(8deg);padding:14px 18px;background:var(--amber);color:#2b190f;border-radius:50%;text-align:center}.catalog{padding:0 clamp(24px,6vw,88px)}.catalog-head{display:flex;justify-content:space-between;align-items:end;margin-bottom:28px}.catalog-head h2{font-family:Georgia,"Microsoft YaHei",serif;font-size:42px;margin:8px 0 0}.filters{display:flex;gap:12px}.filters .el-select{width:160px}.search{width:260px}.dessert-grid{min-height:220px;display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:22px}.dessert-card{overflow:hidden;background:var(--surface);border:1px solid var(--border);border-radius:22px;transition:.25s}.dessert-card:hover{transform:translateY(-5px);border-color:#e5ad5266}.dessert-image{height:220px;background:linear-gradient(145deg,#3b2820,#211815);display:grid;place-items:center;position:relative}.dessert-image img{width:100%;height:100%;object-fit:cover}.dessert-image>span{font-size:76px}.sold-out{position:absolute;inset:0;display:grid;place-items:center;background:#160f0dbb;font-size:18px}.dessert-info{padding:20px}.dessert-info small{color:var(--amber)}.dessert-info h3{margin:8px 0;font-size:21px}.dessert-info p{height:44px;overflow:hidden;color:var(--muted);font-size:13px;line-height:1.7}.dessert-info>div{display:flex;align-items:center;justify-content:space-between;margin-top:18px}.dessert-info strong{font-size:20px;color:var(--amber-strong)}.el-pagination{justify-content:center;margin-top:36px}@media(max-width:760px){.hero{min-height:420px;grid-template-columns:1fr;padding-top:46px}.hero-art{display:none}.catalog-head{align-items:stretch;flex-direction:column;gap:18px}.filters{flex-direction:column}.filters .el-select,.search{width:100%}}
</style>
