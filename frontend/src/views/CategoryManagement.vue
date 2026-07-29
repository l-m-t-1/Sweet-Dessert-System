<template>
  <div class="page">
    <div class="page-head"><div><h2>甜品分类</h2><p class="muted">建立清晰的产品分类，便于筛选与统计。</p></div><el-button type="primary" @click="openCreate">＋ 新增分类</el-button></div>
    <section class="panel">
      <el-table :data="items" v-loading="loading"><el-table-column prop="name" label="分类名称" min-width="220"/><el-table-column prop="updateTime" label="更新时间" min-width="190"/><el-table-column label="操作" width="180"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">重命名</el-button><el-button text type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table>
      <el-empty v-if="!loading&&!items.length" description="还没有分类" />
    </section>
    <el-dialog v-model="visible" :title="editing?'重命名分类':'新增分类'" width="420px"><el-input v-model="name" maxlength="50" show-word-limit placeholder="请输入分类名称" @keyup.enter="submit"/><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">保存</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCategories,createCategory,updateCategory,deleteCategory } from '../api/category'
const items=ref([]),loading=ref(false),visible=ref(false),saving=ref(false),editing=ref(null),name=ref('')
async function load(){loading.value=true;try{items.value=await listCategories()}catch(e){ElMessage.error(e.message)}finally{loading.value=false}}
function openCreate(){editing.value=null;name.value='';visible.value=true} function openEdit(row){editing.value=row;name.value=row.name;visible.value=true}
async function submit(){if(!name.value.trim()){ElMessage.warning('分类名称不能为空');return}saving.value=true;try{editing.value?await updateCategory(editing.value.id,{name:name.value}):await createCategory({name:name.value});ElMessage.success('保存成功');visible.value=false;await load()}catch(e){ElMessage.error(e.message)}finally{saving.value=false}}
async function remove(row){try{await ElMessageBox.confirm(`确认删除“${row.name}”吗？`,'删除分类',{type:'warning'});await deleteCategory(row.id);ElMessage.success('删除成功');await load()}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(e.message)}}
onMounted(load)
</script>
