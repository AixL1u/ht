<template>
  <div class="image-upload">
    <div class="upload-area" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
      <input 
        ref="fileInput" 
        type="file" 
        accept="image/*" 
        @change="handleFileChange" 
        style="display: none"
      />
      
      <div v-if="previewUrl" class="preview">
        <img :src="previewUrl" alt="预览" />
        <div class="preview-actions">
          <span class="action-btn" @click.stop="triggerUpload">更换</span>
          <span class="action-btn delete" @click.stop="clearImage">删除</span>
        </div>
      </div>
      
      <div v-else class="placeholder">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
        <span>点击或拖拽上传图片</span>
        <span class="hint">支持 JPG/PNG/GIF/WEBP</span>
      </div>
    </div>
    
    <div v-if="uploading" class="upload-progress">
      <div class="progress-bar" :style="{ width: progress + '%' }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue?: string
  category?: string
}>()

const emit = defineEmits(['update:modelValue'])

const fileInput = ref<HTMLInputElement>()
const previewUrl = ref(props.modelValue || '')
const uploading = ref(false)
const progress = ref(0)

watch(() => props.modelValue, (val) => {
  previewUrl.value = val || ''
})

function triggerUpload() {
  fileInput.value?.click()
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) uploadFile(file)
}

function handleDrop(e: DragEvent) {
  const file = e.dataTransfer?.files?.[0]
  if (file && file.type.startsWith('image/')) {
    uploadFile(file)
  }
}

async function uploadFile(file: File) {
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  uploading.value = true
  progress.value = 0

  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', props.category || 'common')

  try {
    const { data } = await http.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total) progress.value = Math.round((e.loaded / e.total) * 100)
      }
    })
    
    previewUrl.value = data.url
    emit('update:modelValue', data.url)
    ElMessage.success('上传成功')
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function clearImage() {
  previewUrl.value = ''
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
  background: #fafafa;
}

.upload-area:hover {
  border-color: #c23531;
  background: #fff5f5;
}

.placeholder {
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.placeholder svg {
  width: 48px;
  height: 48px;
  opacity: 0.5;
}

.placeholder .hint {
  font-size: 12px;
  color: #c0c4cc;
}

.preview {
  position: relative;
  aspect-ratio: 16/9;
}

.preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.preview:hover .preview-actions {
  opacity: 1;
}

.action-btn {
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  background: rgba(255,255,255,0.2);
}

.action-btn:hover {
  background: rgba(255,255,255,0.3);
}

.action-btn.delete:hover {
  background: #f56c6c;
}

.upload-progress {
  margin-top: 8px;
  height: 4px;
  background: #ebeef5;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #c23531;
  transition: width 0.2s;
}
</style>
