<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>个人资料</span>
      </div>
    </template>
    <el-form :model="formData" label-width="80px">
      <el-form-item label="头像">
        <el-upload
          class="avatar-uploader"
          action=""
          :http-request="uploadAvatar"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="formData.avatarUrl" :src="formData.avatarUrl" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div style="font-size:12px;color:#999;margin-top:4px">点击上传修改头像，支持 jpg/png 格式</div>
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="formData.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="formData.nickname" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="formData.email" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="formData.phone" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'

const props = defineProps<{
  profile: any
}>()

const formData = ref({ nickname: '', ...props.profile })

watch(() => props.profile, (newVal) => {
  formData.value = { nickname: '', ...newVal }
}, { deep: true })

async function saveProfile() {
  try {
    const res = await http.put(`/users/me`, formData.value)
    // Update local storage user info
    localStorage.setItem('user', JSON.stringify(res.data))
    ElMessage.success('保存成功，刷新页面后生效')
    // Optional: Reload or emit event to update header immediately
    setTimeout(() => location.reload(), 1000)
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function uploadAvatar(options: UploadRequestOptions) {
  try {
    const form = new FormData()
    form.append('file', options.file)
    const res = await http.post('/users/me/avatar', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    formData.value.avatarUrl = res.data.url
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

function beforeAvatarUpload(rawFile: any) {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}
</script>

<style scoped>
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--border);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100px;
  height: 100px;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--brand);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
</style>
