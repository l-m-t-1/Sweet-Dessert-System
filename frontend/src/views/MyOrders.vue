<template>
  <div class="orders-page">
    <div class="orders-head">
      <div><span>ORDER HISTORY</span><h1>我的订单</h1><p>这里只会显示当前账户创建的订单。</p></div>
      <el-select v-model="status" clearable placeholder="全部状态" @change="load">
        <el-option label="待处理" value="CREATED" /><el-option label="已完成" value="COMPLETED" /><el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>
    <div v-loading="loading" class="order-list">
      <article v-for="order in records" :key="order.id" class="order-card">
        <header><div><small>订单号</small><b>{{ order.orderNo }}</b></div><el-tag :type="tagType(order.status)">{{ statusName(order.status) }}</el-tag></header>
        <div class="order-body">
          <div><small>下单时间</small><p>{{ formatTime(order.createTime) }}</p></div>
          <div><small>订单金额</small><p class="amount">¥ {{ money(order.totalAmount) }}</p></div>
          <div><small>联系电话</small><p>{{ order.customerPhone || '未填写' }}</p></div>
          <el-button @click="showDetail(order.id)">查看详情</el-button>
          <el-button v-if="order.status === 'CREATED'" type="danger" plain @click="cancel(order)">取消订单</el-button>
        </div>
      </article>
      <el-empty v-if="!loading && !records.length" description="还没有订单"><el-button type="primary" @click="$router.push('/shop')">去选甜品</el-button></el-empty>
    </div>
    <el-pagination v-if="total > size" v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="load" />

    <el-dialog v-model="detailVisible" title="订单详情" width="560px">
      <div v-if="detail" class="detail">
        <div v-for="item in detail.items" :key="item.id"><span>{{ item.dessertName }} × {{ item.quantity }}</span><b>¥ {{ money(item.subtotal) }}</b></div>
        <p v-if="detail.remark">备注：{{ detail.remark }}</p>
        <div class="detail-total"><span>合计</span><b>¥ {{ money(detail.totalAmount) }}</b></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelMyOrder, getMyOrder, pageMyOrders } from '../api/customerOrder'

const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const status = ref('')
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)

async function load() {
  loading.value = true
  try {
    const result = await pageMyOrders({ page: page.value, size, status: status.value || undefined })
    records.value = result.records
    total.value = result.total
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function showDetail(id) {
  try {
    detail.value = await getMyOrder(id)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function cancel(order) {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${order.orderNo}？库存会自动返还。`, '取消订单', { type: 'warning' })
    await cancelMyOrder(order.id)
    ElMessage.success('订单已取消，库存已返还')
    await load()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message)
  }
}

const money = value => Number(value).toFixed(2)
const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-'
const statusName = value => ({ CREATED: '待处理', COMPLETED: '已完成', CANCELLED: '已取消' }[value] || value)
const tagType = value => ({ CREATED: 'warning', COMPLETED: 'success', CANCELLED: 'info' }[value] || '')
onMounted(load)
</script>

<style scoped>
.orders-page{max-width:1080px;margin:0 auto;padding:58px 24px 90px}.orders-head{display:flex;align-items:end;justify-content:space-between;margin-bottom:34px}.orders-head span{color:var(--amber);font-size:11px;letter-spacing:3px}.orders-head h1{font-family:Georgia,"Microsoft YaHei",serif;font-size:48px;margin:8px 0}.orders-head p{margin:0;color:var(--muted)}.orders-head .el-select{width:160px}.order-list{display:grid;gap:18px;min-height:180px}.order-card{background:var(--surface);border:1px solid var(--border);border-radius:18px;padding:22px}.order-card header{display:flex;align-items:center;justify-content:space-between;padding-bottom:18px;border-bottom:1px solid var(--border)}.order-card header div{display:grid;gap:5px}.order-card small{color:var(--muted)}.order-body{display:grid;grid-template-columns:1.2fr .8fr .8fr auto auto;align-items:end;gap:20px;padding-top:18px}.order-body p{margin:7px 0 0}.amount{color:var(--amber-strong);font-size:18px;font-weight:700}.detail{display:grid;gap:14px}.detail>div{display:flex;justify-content:space-between}.detail-total{padding-top:16px;border-top:1px solid var(--border);font-size:20px;color:var(--amber-strong)}.el-pagination{justify-content:center;margin-top:30px}@media(max-width:760px){.order-body{grid-template-columns:1fr 1fr}.orders-head{align-items:stretch;flex-direction:column;gap:18px}}
</style>
