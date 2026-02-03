<template>
  <div class="page-section">
    <PageHeader title="站点设置" />
    <el-form inline :model="form" class="toolbar" style="margin-bottom:12px">
      <el-form-item label="键名"><el-input v-model="form.k" style="width:200px" /></el-form-item>
      <el-form-item label="键值"><el-input v-model="form.v" style="width:260px" /></el-form-item>
      <el-form-item label="类型"><el-input v-model="form.type" style="width:160px" placeholder="string/number..." /></el-form-item>
      <el-form-item><el-button type="primary" @click="add">新增</el-button></el-form-item>
    </el-form>

    <el-table :data="items" style="margin-top:8px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="k" label="键名" width="200" />
      <el-table-column label="键值">
        <template #default="{ row }">
          <el-input v-model="row.v" />
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="160" align="center" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="save(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import PageHeader from '../../components/PageHeader.vue'

const items = ref<any[]>([])
const form = ref({ k: '', v: '', type: '' })

async function load() {
  const { data } = await http.get('/settings')
  items.value = data
}

onMounted(load)

async function add() {
  if (!form.value.k) return
  await http.post('/admin/settings', form.value)
  ElMessage.success('已新增')
  form.value = { k: '', v: '', type: '' }
  load()
}

// @ts-ignore
async function save(row: any) {
  await http.put(`/admin/settings/${row.id}`, { k: row.k, v: row.v, type: row.type })
  ElMessage.success('已保存')
}
</script>
