<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>我的地址</span>
        <el-button type="primary" size="small" @click="openAdd">新增地址</el-button>
      </div>
    </template>
    <el-table :data="addresses" stripe>
      <el-table-column prop="name" label="收件人" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="region" label="地区" width="160" />
      <el-table-column prop="detail" label="详细地址" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="delAddr(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Address Dialog -->
    <el-dialog v-model="visible" :title="isEdit ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="收件人">
          <el-input v-model="form.name" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="电话" />
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="form.region" placeholder="省市区" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detail" placeholder="街道门牌" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="saveAddr">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  addresses: any[]
  userId: number
}>()

const addresses = ref(props.addresses)
const visible = ref(false)
const isEdit = ref(false)
const form = ref({ id: null as any, name: '', phone: '', region: '', detail: '' })

const emit = defineEmits(['refresh'])

watch(() => props.addresses, (newVal) => {
  addresses.value = newVal
}, { deep: true })

function openAdd() {
  if (!props.userId) {
    ElMessage.error('请先登录')
    return
  }
  isEdit.value = false
  form.value = { id: null, name: '', phone: '', region: '', detail: '' }
  visible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  form.value = { 
    id: row.id,
    name: row.name, 
    phone: row.phone, 
    region: row.region, 
    detail: row.detail 
  }
  visible.value = true
}

async function saveAddr() {
  if (!form.value.name || !form.value.phone || !form.value.detail) {
    ElMessage.error('请完善地址信息')
    return
  }
  
  try {
    if (!isEdit.value) {
      await http.post('/addresses', {
        userId: props.userId,
        name: form.value.name,
        phone: form.value.phone,
        region: form.value.region,
        detail: form.value.detail
      })
    } else {
      await http.put(`/addresses/${form.value.id}`, {
        userId: props.userId,
        name: form.value.name,
        phone: form.value.phone,
        region: form.value.region,
        detail: form.value.detail
      })
    }
    ElMessage.success('已保存')
    visible.value = false
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  }
}

async function delAddr(id: number) {
  if (!confirm('确定要删除该地址吗？')) return
  try {
    await http.delete(`/addresses/${id}`)
    ElMessage.success('已删除')
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}
</script>
