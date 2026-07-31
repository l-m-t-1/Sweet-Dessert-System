<template>
  <div class="page">
    <div class="page-head">
      <div><h2>注册用户</h2><p class="muted">查看顾客账户并控制登录状态。密码不会在这里展示。</p></div>
      <el-input v-model="keyword" clearable placeholder="搜索用户名" style="width:260px" @input="search" />
    </div>
    <section class="panel">
      <el-table v-loading="loading" :data="records">
        <el-table-column prop="username" label="用户名" min-width="180" />
        <el-table-column label="身份" width="120"><template #default="{ row }"><el-tag :type="row.role === 'ADMIN' ? 'warning' : ''">{{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag></template></el-table-column>
        <el-table-column label="注册时间" min-width="190"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
        <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已启用' : '已停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <span v-if="row.role === 'ADMIN'" class="muted">受保护</span>
            <el-button v-else :type="row.status === 1 ? 'danger' : 'success'" plain @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > size" v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" />
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changeUserStatus, pageUsers } from '../api/adminUser'

const records = ref([])
const keyword = ref('')
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
let timer

async function load() {
  loading.value = true
  try {
    const result = await pageUsers({ page: page.value, size, keyword: keyword.value || undefined })
    records.value = result.records
    total.value = result.total
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function search() {
  clearTimeout(timer)
  timer = setTimeout(() => { page.value = 1; load() }, 250)
}

async function toggle(row) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认${action}用户「${row.username}」？`, `${action}账户`, { type: 'warning' })
    await changeUserStatus(row.id, next)
    ElMessage.success(`账户已${action}`)
    await load()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message)
  }
}

const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-'
onMounted(load)
</script>

<style scoped>.el-pagination{justify-content:flex-end;margin-top:20px}</style>
