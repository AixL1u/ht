<template>
  <div class="page-section">
    <PageHeader title="用户管理" sub="管理系统用户和权限" />
    
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="搜索用户名" clearable style="width: 200px;" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load" icon="Search">搜索</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="openCreate" icon="Plus">新增用户</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="items" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="150" show-overflow-tooltip />
      <el-table-column label="角色" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" effect="dark">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'" effect="dark">
            {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="edit(row)" icon="Edit">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)" icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      background
      layout="total, sizes, prev, pager, next, jumper"
      :page-size="query.size"
      :page-sizes="[10, 20, 50, 100]"
      :current-page="query.page"
      :total="total"
      @current-change="(p:number)=>{query.page=p; load()}"
      @size-change="(s:number)=>{query.size=s; query.page=1; load()}"
    />
  </div>

  <!-- Create/Edit Dialog -->
  <el-dialog v-model="visible" :title="isEdit ? '编辑用户' : '新增用户'" width="900px" :close-on-click-modal="false" append-to-body>
    <el-form :model="form" label-width="110px" style="padding: 20px;">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="用户名" required>
            <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="密码" :required="!isEdit">
            <el-input 
              v-model="form.password" 
              type="password" 
              show-password 
              :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'" 
              clearable 
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="角色" required>
            <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%;">
              <el-option label="管理员" value="ADMIN">
                <span style="float: left">管理员</span>
                <span style="float: right; color: var(--muted); font-size: 13px">拥有所有权限</span>
              </el-option>
              <el-option label="普通用户" value="USER">
                <span style="float: left">普通用户</span>
                <span style="float: right; color: var(--muted); font-size: 13px">基础权限</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" required>
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%;">
              <el-option label="启用" value="ENABLED" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱地址" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电话">
            <el-input v-model="form.phone" placeholder="请输入联系电话" clearable />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div style="padding: 0 20px 20px; display: flex; justify-content: flex-end; gap: 12px;">
        <el-button @click="visible = false" size="large">取消</el-button>
        <el-button type="primary" @click="save" size="large" icon="Check">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '../../components/PageHeader.vue'

const items = ref<any[]>([])
const total = ref(0)
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const query = ref({ keyword: '', page: 1, size: 10 })
const form = ref({ username: '', password: '', role: 'USER', email: '', phone: '', status: 'ENABLED' })

async function load() {
  const { data } = await http.get('/admin/users/search', { 
    params: { 
      page: query.value.page - 1, 
      size: query.value.size, 
      keyword: query.value.keyword 
    } 
  })
  items.value = data.content
  total.value = data.totalElements
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { username: '', password: '', role: 'USER', email: '', phone: '', status: 'ENABLED' }
  visible.value = true
}

// @ts-ignore
function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    username: row.username || '',
    password: '',
    role: row.role || 'USER',
    email: row.email || '',
    phone: row.phone || '',
    status: row.status || 'ENABLED'
  }
  visible.value = true
}

async function save() {
  if (!form.value.username) { 
    ElMessage.error('请填写用户名')
    return 
  }
  if (!isEdit.value && !form.value.password) { 
    ElMessage.error('请填写密码')
    return 
  }
  
  try {
    if (isEdit.value && currentId.value != null) {
      await http.put(`/admin/users/${currentId.value}`, form.value)
      if (form.value.password) {
        await http.put(`/admin/users/${currentId.value}/password`, { newPassword: form.value.password })
      }
      ElMessage.success('用户更新成功')
    } else {
      await http.post('/admin/users/create', form.value)
      ElMessage.success('用户创建成功')
    }
    visible.value = false
    load()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

async function del(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    await http.delete(`/admin/users/${id}`)
    ElMessage.success('用户删除成功')
    load()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}
</script>
