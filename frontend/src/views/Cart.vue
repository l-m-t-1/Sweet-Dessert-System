<template>
  <div class="customer-page">
    <div class="customer-head"><div><span>YOUR CART</span><h1>购物车</h1></div><router-link to="/shop">继续选购 →</router-link></div>
    <div v-if="cart.length" class="cart-layout">
      <section class="cart-list">
        <article v-for="item in cart" :key="item.id">
          <div class="thumb"><img v-if="item.image" :src="item.image" :alt="item.name"><span v-else>🍰</span></div>
          <div class="name"><h3>{{ item.name }}</h3><small>¥ {{ money(item.price) }} / 份</small></div>
          <el-input-number :model-value="item.quantity" :min="0" :max="item.stock" @change="value => quantity(item.id, value)" />
          <strong>¥ {{ money(item.price * item.quantity) }}</strong>
          <el-button text type="danger" @click="quantity(item.id, 0)">移除</el-button>
        </article>
      </section>
      <aside class="checkout">
        <h2>订单小计</h2>
        <div><span>商品数量</span><b>{{ count }} 份</b></div>
        <div class="total"><span>合计</span><b>¥ {{ money(total) }}</b></div>
        <el-input v-model="form.customerPhone" placeholder="联系电话（选填）" />
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="200" placeholder="订单备注（选填）" />
        <el-button type="primary" size="large" :loading="submitting" @click="submit">确认下单</el-button>
      </aside>
    </div>
    <el-empty v-else description="购物车还是空的"><el-button type="primary" @click="$router.push('/shop')">去选甜品</el-button></el-empty>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createMyOrder } from '../api/customerOrder'
import { cartTotal, clearCart, readCart, saveCart, setCartQuantity } from '../cart/cart'

const router = useRouter()
const cart = ref(readCart())
const form = ref({ customerPhone: '', remark: '' })
const submitting = ref(false)
const total = computed(() => cartTotal(cart.value))
const count = computed(() => cart.value.reduce((sum, item) => sum + item.quantity, 0))
const money = value => Number(value).toFixed(2)

function quantity(id, value) {
  cart.value = setCartQuantity(cart.value, id, value ?? 0)
  saveCart(cart.value)
}

async function submit() {
  submitting.value = true
  try {
    await createMyOrder({
      customerPhone: form.value.customerPhone.trim() || null,
      remark: form.value.remark.trim() || null,
      items: cart.value.map(item => ({ dessertId: item.id, quantity: item.quantity })),
    })
    clearCart()
    cart.value = []
    ElMessage.success('下单成功，库存已同步扣减')
    router.replace('/my-orders')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.customer-page{max-width:1180px;margin:0 auto;padding:58px 24px 90px}.customer-head{display:flex;align-items:end;justify-content:space-between;margin-bottom:34px}.customer-head span{color:var(--amber);font-size:11px;letter-spacing:3px}.customer-head h1{font-family:Georgia,"Microsoft YaHei",serif;font-size:48px;margin:8px 0 0}.customer-head a{color:var(--amber-strong);text-decoration:none}.cart-layout{display:grid;grid-template-columns:1fr 340px;gap:26px}.cart-list{background:var(--surface);border:1px solid var(--border);border-radius:20px;padding:8px 24px}.cart-list article{display:grid;grid-template-columns:76px 1fr auto 100px auto;align-items:center;gap:18px;padding:20px 0;border-bottom:1px solid var(--border)}.cart-list article:last-child{border-bottom:0}.thumb{width:76px;height:70px;display:grid;place-items:center;background:var(--surface-2);border-radius:13px;font-size:30px;overflow:hidden}.thumb img{width:100%;height:100%;object-fit:cover}.name h3{margin:0 0 7px}.name small{color:var(--muted)}.checkout{height:max-content;display:grid;gap:18px;padding:26px;background:var(--surface);border:1px solid var(--border);border-radius:20px}.checkout h2{margin:0 0 6px}.checkout>div{display:flex;justify-content:space-between;color:var(--muted)}.checkout .total{padding-top:18px;border-top:1px solid var(--border);color:var(--text);font-size:20px}.checkout .total b{color:var(--amber-strong)}@media(max-width:900px){.cart-layout{grid-template-columns:1fr}.cart-list article{grid-template-columns:60px 1fr auto}.cart-list article>strong,.cart-list article>.el-button{grid-column:2}.checkout{grid-row:1}}
</style>
