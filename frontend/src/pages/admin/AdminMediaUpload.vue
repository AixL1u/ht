<template>
  <div class="page-section">
    <PageHeader title="媒体上传" />
    <div style="background: #fff; padding: 24px; border-radius: 8px;">
      <el-form label-width="100px" class="stack">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="标题" required>
              <el-input v-model="title" placeholder="请输入媒体标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" required>
              <el-select v-model="type" style="width: 100%" @change="onTypeChange">
                <el-option label="视频" value="video" />
                <el-option label="图片" value="image" />
                <el-option label="音频" value="audio" />
                <el-option label="文件" value="file" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="分类标签">
              <el-select v-model="category" style="width: 100%" clearable placeholder="选择分类">
                <el-option-group v-if="type === 'video'" label="视频分类">
                  <el-option label="非遗纪录片" value="documentary" />
                  <el-option label="技艺展示" value="craft" />
                  <el-option label="传承人访谈" value="interview" />
                  <el-option label="活动记录" value="activity" />
                  <el-option label="教学视频" value="tutorial" />
                </el-option-group>
                <el-option-group v-else-if="type === 'image'" label="图片分类">
                  <el-option label="作品展示" value="artwork" />
                  <el-option label="活动照片" value="event" />
                  <el-option label="传承人照片" value="inheritor" />
                  <el-option label="项目图片" value="project" />
                </el-option-group>
                <el-option-group v-else-if="type === 'audio'" label="音频分类">
                  <el-option label="口述历史" value="oral" />
                  <el-option label="传统音乐" value="music" />
                  <el-option label="讲座录音" value="lecture" />
                </el-option-group>
                <el-option-group v-else label="文件分类">
                  <el-option label="研究资料" value="research" />
                  <el-option label="申报材料" value="application" />
                  <el-option label="其他" value="other" />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联项目">
              <el-select v-model="projectId" style="width: 100%" clearable filterable placeholder="选择关联项目">
                <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="关联传承人">
              <el-select v-model="inheritorId" style="width: 100%" clearable filterable placeholder="选择关联传承人">
                <el-option v-for="i in inheritors" :key="i.id" :label="i.name" :value="i.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-select v-model="selectedTags" multiple style="width: 100%" placeholder="选择标签" allow-create filterable>
                <el-option v-for="tag in availableTags" :key="tag" :label="tag" :value="tag" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 视频封面上传 -->
        <el-form-item label="视频封面" v-if="type === 'video'">
          <div class="cover-upload">
            <el-upload
              :auto-upload="true"
              :show-file-list="false"
              accept="image/*"
              :http-request="uploadCover"
            >
              <div class="cover-preview" v-if="coverUrl">
                <img :src="coverUrl" />
                <div class="cover-mask">
                  <span>更换封面</span>
                </div>
              </div>
              <div class="cover-placeholder" v-else>
                <el-icon size="24"><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
            <div class="cover-tip">建议尺寸 16:9，如不上传将自动截取视频首帧</div>
          </div>
        </el-form-item>

        <el-form-item label="选择文件" required>
          <div style="width: 100%">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="true"
              :limit="1"
              :on-change="onFileChange"
              :on-remove="onFileRemove"
              drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽文件到此处，或 <em>点击上传</em></div>
              <template #tip>
                <div class="el-upload__tip">支持视频、图片、音频、文档等格式</div>
              </template>
            </el-upload>
            <el-progress v-if="progress > 0" :percentage="Number(progress.toFixed(2))" :status="progress === 100 ? 'success' : ''" style="margin-top: 12px;" />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :disabled="!file || !title" @click="start" size="large" :loading="uploading">
            {{ uploading ? '上传中...' : '开始上传' }}
          </el-button>
          <el-button size="large" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-if="asset" style="margin-top: 20px;">
        <el-alert type="success" title="上传完成" show-icon :closable="false">
          <template #default>
            <div style="margin-top: 8px;">
              访问链接: <el-link :href="asset.url" target="_blank" type="primary">{{ asset.url }}</el-link>
            </div>
          </template>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import { UploadFilled, Plus } from '@element-plus/icons-vue'
import PageHeader from '../../components/PageHeader.vue'

const CHUNK_SIZE = 5 * 1024 * 1024

const title = ref('')
const type = ref('video')
const category = ref('')
const projectId = ref<number | null>(null)
const inheritorId = ref<number | null>(null)
const coverUrl = ref('')
const selectedTags = ref<string[]>([])
const availableTags = ['传统技艺', '民间艺术', '非遗传承', '手工艺', '文化遗产', '戏曲', '音乐', '舞蹈', '美术', '工艺']
const file = ref<File | null>(null)
const uploadId = ref<string>('')
const progress = ref(0)
const asset = ref<any>(null)
const uploading = ref(false)
const uploadRef = ref()

const projects = ref<any[]>([])
const inheritors = ref<any[]>([])

onMounted(async () => {
  try {
    const [pRes, iRes] = await Promise.all([
      http.get('/catalog/projects'),
      http.get('/catalog/inheritors')
    ])
    projects.value = pRes.data || []
    inheritors.value = iRes.data || []
  } catch (e) { console.error(e) }
})

function onTypeChange() {
  category.value = ''
  coverUrl.value = ''
}

async function uploadCover(options: any) {
  const formData = new FormData()
  formData.append('file', options.file)
  formData.append('type', 'image')
  formData.append('title', '视频封面')
  try {
    const { data } = await http.post('/admin/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    coverUrl.value = data.url
    ElMessage.success('封面上传成功')
  } catch (e: any) {
    ElMessage.error('封面上传失败')
  }
}

function onFileChange(uploadFile: any) {
  file.value = uploadFile.raw
  progress.value = 0
  asset.value = null
}

function onFileRemove() {
  file.value = null
  progress.value = 0
}

function reset() {
  title.value = ''
  type.value = 'video'
  category.value = ''
  projectId.value = null
  inheritorId.value = null
  coverUrl.value = ''
  selectedTags.value = []
  file.value = null
  progress.value = 0
  asset.value = null
  uploadRef.value?.clearFiles()
}

async function start() {
  if (!file.value || !title.value) {
    ElMessage.warning('请填写标题并选择文件')
    return
  }
  uploading.value = true
  try {
    const totalChunks = Math.ceil(file.value.size / CHUNK_SIZE)
    const initResp = await http.post('/admin/media/chunk/initiate', {
      filename: file.value.name,
      totalChunks
    })
    uploadId.value = initResp.data.uploadId

    const statusResp = await http.get('/admin/media/chunk/status', { params: { uploadId: uploadId.value } })
    const uploaded: number[] = statusResp.data?.uploaded || []

    for (let i = 0; i < totalChunks; i++) {
      if (uploaded.includes(i)) {
        progress.value = (i / totalChunks) * 100
        continue
      }
      const start = i * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, file.value.size)
      const blob = file.value.slice(start, end)
      const form = new FormData()
      form.append('uploadId', uploadId.value)
      form.append('index', String(i))
      form.append('chunk', blob)
      await http.post('/admin/media/chunk/upload', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      progress.value = ((i + 1) / totalChunks) * 100
    }

    const mergeResp = await http.post('/admin/media/chunk/merge', {
      uploadId: uploadId.value,
      filename: file.value.name,
      type: type.value,
      title: title.value,
      category: category.value,
      tags: selectedTags.value.join(','),
      coverUrl: coverUrl.value,
      projectId: projectId.value,
      inheritorId: inheritorId.value
    })
    asset.value = mergeResp.data
    ElMessage.success('上传完成')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.cover-upload {
  width: 100%;
}

.cover-preview {
  width: 200px;
  height: 112px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-mask {
  opacity: 1;
}

.cover-placeholder {
  width: 200px;
  height: 112px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
}

.cover-placeholder:hover {
  border-color: #c23531;
  color: #c23531;
}

.cover-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
