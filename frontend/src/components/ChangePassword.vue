<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>修改密码</span>
      </div>
    </template>
    <el-form :model="form" label-width="80px">
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="changePassword">修改密码</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'

const form = ref({
  newPassword: '',
  confirmPassword: ''
})

async function changePassword() {
  if (!form.value.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  try {
    await http.put(`/users/me/password`, { newPassword: form.value.newPassword })
    ElMessage.success('修改成功')
    form.value = { newPassword: '', confirmPassword: '' }
  } catch (e) {
    ElMessage.error('修改失败')
  }
}
</script>
