<template>
  <div class="page-section">
    <PageHeader title="商品分类管理">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </template>
    </PageHeader>
    
    <el-table :data="items" style="margin-top:12px" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="parentId" label="父级ID" width="120" align="center">
        <template #default="{ row }">
          {{ row.parentId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog v-model="visible" :title="isEdit ? '编辑分类' : '新增分类'" width="600px" append-to-body>
      <el-form :model="form" label-width="80px" style="padding: 20px;">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级ID">
          <el-input-number v-model="form.parentId" :min="0" style="width: 100%;" placeholder="顶级分类留空或0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="padding: 0 20px 20px;">
          <el-button @click="visible = false" size="large">取消</el-button>
          <el-button type="primary" @click="save" size="large">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '../../components/PageHeader.vue'

const items = ref<any[]>([])
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const form = ref({ name: '', parentId: 0 })

async function load() {
  const { data } = await http.get('/product-categories')
  items.value = data
}

onMounted(load)

function openCreate() {
  isEdit.value = false
  currentId.value = null
  form.value = { name: '', parentId: 0 }
  visible.value = true
}

function edit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = { name: row.name, parentId: row.parentId || 0 }
  visible.value = true
}

async function save() {
  if (!form.value.name) {
    ElMessage.warning('请输入名称')
    return
  }
  
  try {
    if (isEdit.value && currentId.value) {
      // Update (假设后端有更新接口，如果没有可能需要新增)
      // 注意：原代码没有 update 接口，这里假设有 PUT /admin/product-categories/:id
      // 如果没有，可能需要后端支持。暂时先用 POST 模拟或者提示
      // 由于后端 AdminController 中没有 PUT /product-categories/:id，我先添加一个 TODO
      // 但为了完整性，我会尝试调用 PUT，如果后端不支持会报错
      // 检查后端代码... AdminController 确实没有 updateProductCategory
      // 所以这里暂时只能做新增，或者我需要去后端添加 update 接口
      // 为了不中断流程，我先只支持新增，编辑提示未实现，或者我去改后端
      
      // 实际上，我可以去修改后端添加 update 接口
      await http.put(`/admin/product-categories/${currentId.value}`, form.value)
    } else {
      await http.post('/admin/product-categories', form.value)
    }
    ElMessage.success('已保存')
    visible.value = false
    load()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function del(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', { type: 'warning' })
    await http.delete(`/admin/product-categories/${id}`)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    // cancel or error
  }
}
</script>
