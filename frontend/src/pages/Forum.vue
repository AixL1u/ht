<template>
  <div class="message-wall" ref="wallRef">
    <!-- 返回首页按钮 -->
    <div class="back-home-btn" @click="$router.push('/')">
      <span class="back-icon">←</span>
      <span class="back-text">返回首页</span>
    </div>

    <!-- 便签墙区域 -->
    <div 
      class="wall-canvas" 
      ref="canvasRef"
      :style="{ transform: `scale(${scale}) translate(${panX}px, ${panY}px)` }"
      @mousedown="startPan"
      @wheel="handleWheel"
    >
      <!-- 便签卡片 -->
      <div
        v-for="note in notes"
        :key="note.id"
        class="sticky-note"
        :class="[note.color, { 'is-dragging': draggingId === note.id, 'is-own': isOwnNote(note) }]"
        :style="{ left: note.x + 'px', top: note.y + 'px', transform: `rotate(${note.rotation || 0}deg)` }"
        @mousedown.stop="startDrag($event, note)"
        @touchstart.stop="startDrag($event, note)"
      >
        <div class="note-header">
          <div class="note-user">
            <span class="user-avatar" :style="{ background: getAvatarColor(note.userName) }">{{ getAvatarText(note.userName) }}</span>
            <span class="user-name">{{ note.userName || '匿名' }}</span>
          </div>
          <span v-if="isOwnNote(note)" class="delete-note-btn" @click.stop="deleteNote(note)" title="删除留言">×</span>
        </div>
        <div class="note-content">{{ note.content }}</div>
        <div class="note-footer">
          <span class="note-time">{{ note.timeAgo }}</span>
          <div class="note-actions">
            <span class="action-btn like-btn" :class="{ liked: note.liked }" @click.stop="likeNote(note)">
              <svg v-if="note.liked" viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              {{ note.likes || 0 }}
            </span>
            <span class="action-btn comment-btn" @click.stop="openComments(note)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              {{ note.comments || 0 }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="toolbar-hint">拖动便签移动（仅自己的） | 滚轮缩放</span>
      </div>
      <div class="toolbar-center">
        <div class="search-box" title="搜索留言内容">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input type="text" placeholder="搜索..." v-model="searchText" @input="filterNotes" />
        </div>
        <div class="page-info" title="当前显示/总数">{{ filteredCount }}/{{ totalNotes }}</div>
        <div class="nav-btns">
          <span class="nav-btn" title="上一页" @click="prevPage">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
          </span>
          <span class="nav-btn" title="下一页" @click="nextPage">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m9 18 6-6-6-6"/></svg>
          </span>
        </div>
        <div class="view-btns">
          <span class="view-btn" :class="{ active: viewMode === 'all' }" title="显示全部留言" @click="setViewMode('all')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
          </span>
          <span class="view-btn" :class="{ active: viewMode === 'liked' }" title="显示我点赞的" @click="setViewMode('liked')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          </span>
          <span class="view-btn" :class="{ active: viewMode === 'mine' }" title="显示我的留言" @click="setViewMode('mine')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="5"/><path d="M3 21v-2a7 7 0 0 1 7-7h4a7 7 0 0 1 7 7v2"/></svg>
          </span>
        </div>
        <div class="count-display" title="留言总数">+{{ totalNotes }}</div>
      </div>
      <div class="toolbar-right">
        <span class="zoom-btn" title="缩小" @click="zoomOut">−</span>
        <span class="zoom-level" title="当前缩放比例">{{ Math.round(scale * 100) }}%</span>
        <span class="zoom-btn" title="放大" @click="zoomIn">+</span>
        <span class="fullscreen-btn" title="全屏" @click="toggleFullscreen">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"/></svg>
        </span>
      </div>
    </div>

    <!-- 添加便签按钮 -->
    <div class="add-note-btn" @click="showAddDialog = true" title="添加留言">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 20h9"></path>
        <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
      </svg>
    </div>

    <!-- 添加便签对话框 -->
    <div class="add-dialog-overlay" v-if="showAddDialog" @click.self="showAddDialog = false">
      <div class="add-dialog">
        <div class="dialog-header">
          <span class="dialog-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
          </span>
          <span class="dialog-title">写下你的留言</span>
          <span class="dialog-count">{{ newNote.content.length }}/200</span>
        </div>
        <div class="dialog-body">
          <textarea 
            v-model="newNote.content"
            placeholder="在这里写下你想说的话..."
            maxlength="200"
            class="message-textarea"
          ></textarea>
        </div>
        <div class="dialog-footer">
          <div class="footer-left">
            <div class="color-section">
              <span class="color-label">颜色：</span>
              <div class="color-picker">
                <span
                  v-for="color in colors"
                  :key="color.name"
                  class="color-option"
                  :style="{ background: color.bg }"
                  :class="{ active: newNote.color === color.name }"
                  @click="newNote.color = color.name"
                ></span>
              </div>
            </div>
            <label class="anonymous-check">
              <input type="checkbox" v-model="newNote.anonymous" />
              <span>匿名发布</span>
            </label>
            <div class="dialog-actions">
              <span class="cancel-btn" @click="showAddDialog = false">取消</span>
              <button class="submit-btn" @click="addNote" :disabled="isSubmitting">
                {{ isSubmitting ? '发布中...' : '发布' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 评论弹窗 -->
    <div class="comment-dialog-overlay" v-if="showCommentDialog" @click.self="showCommentDialog = false">
      <div class="comment-dialog">
        <div class="comment-dialog-header">
          <span class="dialog-title">评论</span>
          <span class="close-btn" @click="showCommentDialog = false">×</span>
        </div>
        <div class="comment-list">
          <div v-if="comments.length === 0" class="no-comments">暂无评论，快来抢沙发吧~</div>
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-avatar" :style="{ background: getAvatarColor(c.userName) }">{{ getAvatarText(c.userName) }}</div>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-user">{{ c.userName || '匿名' }}</span>
                <span v-if="isOwnComment(c)" class="delete-comment-btn" @click="deleteComment(c)">删除</span>
              </div>
              <div class="comment-content">{{ c.content }}</div>
              <div class="comment-time">{{ getTimeAgo(c.createdAt) }}</div>
            </div>
          </div>
        </div>
        <div class="comment-input-area">
          <input 
            v-model="newComment" 
            placeholder="写下你的评论..." 
            @keyup.enter="submitComment"
          />
          <button @click="submitComment" :disabled="isSubmittingComment">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, computed } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'

interface Note {
  id: number
  content: string
  userName: string
  userId: number
  color: string
  x: number
  y: number
  rotation: number
  likes: number
  comments: number
  liked: boolean
  createdAt: string
  timeAgo: string
}

const notes = ref<Note[]>([])
const allNotes = ref<Note[]>([]) // 保存所有留言
const totalNotes = ref(0)
const filteredCount = ref(0)
const showAddDialog = ref(false)
const showCommentDialog = ref(false)
const currentCommentNote = ref<Note | null>(null)
const comments = ref<any[]>([])
const newComment = ref('')
const isSubmitting = ref(false)
const isSubmittingComment = ref(false)
const wallRef = ref<HTMLElement>()
const canvasRef = ref<HTMLElement>()
const draggingId = ref<number | null>(null)
const scale = ref(0.5)
const panX = ref(0)
const panY = ref(0)
const searchText = ref('')
const viewMode = ref<'all' | 'liked' | 'mine'>('all')

const colors = [
  { name: 'yellow', bg: '#e8f5a3' },
  { name: 'pink', bg: '#ffc0cb' },
  { name: 'blue', bg: '#a8d8ea' },
  { name: 'green', bg: '#b5ead7' },
  { name: 'purple', bg: '#e0d4f7' },
  { name: 'orange', bg: '#ffd9b3' },
  { name: 'gray', bg: '#d0d0d0' }
]

const avatarColors = ['#4ade80', '#60a5fa', '#f472b6', '#a78bfa', '#fbbf24', '#34d399']

const newNote = reactive({
  content: '',
  color: 'yellow',
  anonymous: false
})

// 获取当前用户ID
const currentUserId = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr).id : null
})

// 检查是否是自己的便签
function isOwnNote(note: Note): boolean {
  return currentUserId.value && note.userId === currentUserId.value
}

let dragOffset = { x: 0, y: 0 }
let currentNote: Note | null = null
let isPanning = false
let panStart = { x: 0, y: 0 }

// 加载留言
async function loadNotes() {
  try {
    // 获取当前用户ID
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : null
    
    // 请求时传入当前用户ID，后端会返回每个帖子的点赞状态
    const url = userId ? `/forum/posts?currentUserId=${userId}` : '/forum/posts'
    const { data } = await http.get(url).catch(() => ({ data: [] }))
    
    const canvasWidth = 2400
    const canvasHeight = 1600
    
    allNotes.value = data.map((post: any, index: number) => ({
      id: post.id,
      content: post.content || post.title,
      userName: post.userName || '匿名用户',
      userId: post.userId,
      color: colors[index % colors.length].name,
      x: post.posX || Math.random() * canvasWidth,
      y: post.posY || Math.random() * canvasHeight,
      rotation: (Math.random() - 0.5) * 6,
      likes: post.likeCount || 0,
      comments: post.commentCount || 0,
      liked: post.liked || false,
      createdAt: post.createdAt,
      timeAgo: getTimeAgo(post.createdAt)
    }))
    totalNotes.value = allNotes.value.length
    applyFilters()
  } catch (e) {
    ElMessage.error('加载留言失败')
  }
}

// 应用筛选
function applyFilters() {
  let filtered = [...allNotes.value]
  
  // 按视图模式筛选
  if (viewMode.value === 'liked') {
    filtered = filtered.filter(n => n.liked)
  } else if (viewMode.value === 'mine') {
    filtered = filtered.filter(n => isOwnNote(n))
  }
  
  // 按搜索文本筛选
  if (searchText.value.trim()) {
    const keyword = searchText.value.toLowerCase()
    filtered = filtered.filter(n => 
      n.content.toLowerCase().includes(keyword) || 
      n.userName.toLowerCase().includes(keyword)
    )
  }
  
  notes.value = filtered
  filteredCount.value = filtered.length
}

// 筛选留言
function filterNotes() {
  applyFilters()
}

// 设置视图模式
function setViewMode(mode: 'all' | 'liked' | 'mine') {
  viewMode.value = mode
  applyFilters()
}

// 上一页/下一页（暂时只是提示）
function prevPage() {
  ElMessage.info('已经是第一页')
}

function nextPage() {
  ElMessage.info('已经是最后一页')
}

// 全屏切换
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

function getTimeAgo(date: string): string {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return d.toLocaleDateString('zh-CN')
}

function getAvatarText(name: string): string {
  return (name || '匿')[0]
}

function getAvatarColor(name: string): string {
  const index = (name || '').charCodeAt(0) % avatarColors.length
  return avatarColors[index]
}

// 添加留言
async function addNote() {
  if (!newNote.content.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  const u = localStorage.getItem('user')
  if (!u) {
    ElMessage.warning('请先登录')
    location.href = '/login'
    return
  }
  isSubmitting.value = true
  try {
    const user = JSON.parse(u)
    const x = Math.random() * 2000 + 200
    const y = Math.random() * 1200 + 200
    await http.post('/forum/posts', {
      topicId: 1,
      userId: user.id,
      title: newNote.content.substring(0, 50),
      content: newNote.content,
      status: 'VISIBLE',
      posX: x,
      posY: y,
      color: newNote.color,
      anonymous: newNote.anonymous
    })
    ElMessage.success('留言发布成功！')
    showAddDialog.value = false
    newNote.content = ''
    newNote.color = 'yellow'
    newNote.anonymous = false
    await loadNotes()
  } catch (e: any) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    isSubmitting.value = false
  }
}

// 拖拽便签（只能拖动自己发布的）
function startDrag(e: MouseEvent | TouchEvent, note: Note) {
  // 检查是否是自己的便签
  if (!isOwnNote(note)) {
    return // 不是自己的便签，不允许拖动
  }
  
  e.preventDefault()
  draggingId.value = note.id
  currentNote = note
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY
  dragOffset = {
    x: clientX / scale.value - note.x,
    y: clientY / scale.value - note.y
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', stopDrag)
}

function onDrag(e: MouseEvent | TouchEvent) {
  if (!currentNote) return
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY
  currentNote.x = Math.max(0, clientX / scale.value - dragOffset.x)
  currentNote.y = Math.max(0, clientY / scale.value - dragOffset.y)
}

function stopDrag() {
  draggingId.value = null
  currentNote = null
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

// 画布平移
function startPan(e: MouseEvent) {
  if (e.target !== canvasRef.value) return
  isPanning = true
  panStart = { x: e.clientX - panX.value, y: e.clientY - panY.value }
  document.addEventListener('mousemove', onPan)
  document.addEventListener('mouseup', stopPan)
}

function onPan(e: MouseEvent) {
  if (!isPanning) return
  panX.value = e.clientX - panStart.x
  panY.value = e.clientY - panStart.y
}

function stopPan() {
  isPanning = false
  document.removeEventListener('mousemove', onPan)
  document.removeEventListener('mouseup', stopPan)
}

// 缩放
function handleWheel(e: WheelEvent) {
  e.preventDefault()
  const delta = e.deltaY > 0 ? -0.05 : 0.05
  scale.value = Math.max(0.3, Math.min(1.5, scale.value + delta))
}

function zoomIn() { scale.value = Math.min(1.5, scale.value + 0.1) }
function zoomOut() { scale.value = Math.max(0.3, scale.value - 0.1) }

async function likeNote(note: Note) {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    return
  }
  const userId = JSON.parse(userStr).id
  
  try {
    if (note.liked) {
      // 取消点赞
      const { data } = await http.delete(`/forum/posts/${note.id}/like?userId=${userId}`)
      note.liked = false
      note.likes = data.likeCount
    } else {
      // 点赞
      const { data } = await http.post(`/forum/posts/${note.id}/like?userId=${userId}`)
      if (data.message) {
        ElMessage.warning(data.message)
      } else {
        note.liked = true
        note.likes = data.likeCount
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 打开评论弹窗
async function openComments(note: Note) {
  currentCommentNote.value = note
  showCommentDialog.value = true
  await loadComments(note.id)
}

// 加载评论
async function loadComments(postId: number) {
  try {
    const { data } = await http.get(`/forum/posts/${postId}/comments`)
    comments.value = data || []
  } catch (e) {
    comments.value = []
  }
}

// 提交评论
async function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    return
  }
  const user = JSON.parse(userStr)
  isSubmittingComment.value = true
  try {
    await http.post('/forum/comments', {
      postId: currentCommentNote.value?.id,
      userId: user.id,
      content: newComment.value
    })
    newComment.value = ''
    await loadComments(currentCommentNote.value!.id)
    // 更新评论数
    if (currentCommentNote.value) {
      currentCommentNote.value.comments = comments.value.length
      // 同步到 allNotes
      const noteInAll = allNotes.value.find(n => n.id === currentCommentNote.value!.id)
      if (noteInAll) noteInAll.comments = comments.value.length
    }
    ElMessage.success('评论成功')
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    isSubmittingComment.value = false
  }
}

// 检查是否是自己的评论
function isOwnComment(comment: any): boolean {
  return currentUserId.value && comment.userId === currentUserId.value
}

// 删除留言
async function deleteNote(note: Note) {
  if (!confirm('确定要删除这条留言吗？')) return
  try {
    await http.delete(`/forum/posts/${note.id}?userId=${currentUserId.value}`)
    ElMessage.success('已删除')
    await loadNotes()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 删除评论
async function deleteComment(comment: any) {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    await http.delete(`/forum/comments/${comment.id}?userId=${currentUserId.value}`)
    await loadComments(currentCommentNote.value!.id)
    // 更新评论数
    if (currentCommentNote.value) {
      currentCommentNote.value.comments = comments.value.length
      const noteInAll = allNotes.value.find(n => n.id === currentCommentNote.value!.id)
      if (noteInAll) noteInAll.comments = comments.value.length
    }
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => { loadNotes() })
onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<style scoped>
.message-wall {
  width: 100vw;
  height: 100vh;
  background: #1a1a2e;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

/* 返回首页按钮 */
.back-home-btn {
  position: fixed;
  top: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 100;
  backdrop-filter: blur(8px);
}

.back-home-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.back-icon {
  font-size: 16px;
}

.back-text {
  font-weight: 500;
}

.wall-canvas {
  position: absolute;
  width: 3000px;
  height: 2000px;
  transform-origin: 0 0;
  cursor: grab;
  background-image: 
    linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 50px 50px;
}

.wall-canvas:active { cursor: grabbing; }

/* 便签卡片 */
.sticky-note {
  position: absolute;
  width: 200px;
  min-height: 120px;
  padding: 12px 14px;
  border-radius: 2px;
  cursor: default;
  box-shadow: 2px 2px 8px rgba(0,0,0,0.3);
  transition: box-shadow 0.2s;
  user-select: none;
  display: flex;
  flex-direction: column;
}

/* 自己的便签可以拖动 */
.sticky-note.is-own {
  cursor: grab;
}

.sticky-note:hover {
  box-shadow: 4px 4px 16px rgba(0,0,0,0.4);
  z-index: 10;
}

.sticky-note.is-own.is-dragging {
  cursor: grabbing;
  box-shadow: 8px 8px 24px rgba(0,0,0,0.5);
  z-index: 100;
}

/* 便签颜色 */
.sticky-note.yellow { background: #e8f5a3; color: #4a4a2c; }
.sticky-note.pink { background: #ffc0cb; color: #4a2c2c; }
.sticky-note.blue { background: #a8d8ea; color: #2c3a4a; }
.sticky-note.green { background: #b5ead7; color: #2c4a3a; }
.sticky-note.purple { background: #e0d4f7; color: #3a2c4a; }
.sticky-note.orange { background: #ffd9b3; color: #4a3a2c; }
.sticky-note.gray { background: #d0d0d0; color: #3a3a3a; }

.note-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.delete-note-btn {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.1);
  color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
}

.sticky-note:hover .delete-note-btn {
  opacity: 1;
}

.delete-note-btn:hover {
  background: #ef4444;
  color: #fff;
}

.note-user {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
}

.user-name {
  font-size: 11px;
  font-weight: 600;
  opacity: 0.8;
}

.note-content {
  flex: 1;
  font-size: 13px;
  line-height: 1.4;
  word-break: break-word;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  line-clamp: 5;
  -webkit-box-orient: vertical;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 6px;
  border-top: 1px dashed rgba(0,0,0,0.1);
}

.note-time {
  font-size: 10px;
  opacity: 0.5;
}

.note-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  font-size: 11px;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s, transform 0.2s;
}

.action-btn:hover { opacity: 1; }

.action-btn.liked {
  opacity: 1;
  color: #e74c3c;
}

.action-btn.liked:hover {
  transform: scale(1.1);
}

/* 底部工具栏 */
.toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 48px;
  background: rgba(20, 20, 35, 0.95);
  border-top: 1px solid rgba(255,255,255,0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 50;
}

.toolbar-left { flex: 1; }
.toolbar-hint {
  color: rgba(255,255,255,0.4);
  font-size: 12px;
}

.toolbar-center {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.08);
  border-radius: 4px;
  padding: 4px 10px;
}

.search-box .search-icon { 
  width: 14px; 
  height: 14px; 
  margin-right: 6px;
  color: rgba(255,255,255,0.5);
}
.search-box input {
  background: none;
  border: none;
  color: #fff;
  font-size: 12px;
  width: 60px;
  outline: none;
}

.page-info {
  color: rgba(255,255,255,0.6);
  font-size: 12px;
}

.nav-btns, .view-btns {
  display: flex;
  gap: 4px;
}

.nav-btn, .view-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.nav-btn svg, .view-btn svg {
  width: 16px;
  height: 16px;
}

.nav-btn:hover, .view-btn:hover { 
  background: rgba(255,255,255,0.15); 
  color: rgba(255,255,255,0.9);
}

.view-btn.active { 
  background: rgba(255,255,255,0.2); 
  color: #fbbf24;
}

.view-btn.active svg {
  fill: #fbbf24;
}

.count-display {
  background: rgba(255,255,255,0.1);
  color: rgba(255,255,255,0.7);
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.toolbar-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.zoom-btn {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  background: rgba(255,255,255,0.08);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
}

.zoom-btn:hover { background: rgba(255,255,255,0.15); }

.zoom-level {
  color: rgba(255,255,255,0.6);
  font-size: 12px;
  min-width: 40px;
  text-align: center;
}

.fullscreen-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-left: 8px;
}

.fullscreen-btn svg {
  width: 16px;
  height: 16px;
}

.fullscreen-btn:hover {
  background: rgba(255,255,255,0.15);
  color: rgba(255,255,255,0.9);
}

/* 添加按钮 */
.add-note-btn {
  position: fixed;
  bottom: 70px;
  right: 50%;
  transform: translateX(50%);
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(34, 197, 94, 0.5);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 60;
}

.add-note-btn svg {
  width: 24px;
  height: 24px;
}

.add-note-btn:hover {
  transform: translateX(50%) scale(1.1);
  box-shadow: 0 8px 28px rgba(34, 197, 94, 0.6);
}

/* 颜色选择器 */
/* 添加留言对话框 */
.add-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.add-dialog {
  background: #2a2a3e;
  border-radius: 16px;
  width: 520px;
  max-width: 90vw;
  padding: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.dialog-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-icon svg {
  width: 20px;
  height: 20px;
  color: #22c55e;
}

.dialog-title {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  flex: 1;
}

.dialog-count {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.dialog-body {
  margin-bottom: 20px;
}

.message-textarea {
  width: 100%;
  height: 140px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 14px;
  color: #fff;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
  font-family: inherit;
}

.message-textarea::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.message-textarea:focus {
  border-color: rgba(255, 255, 255, 0.2);
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.color-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.anonymous-check {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}

.anonymous-check input {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #22c55e;
  flex-shrink: 0;
}

.anonymous-check span {
  white-space: nowrap;
}

.anonymous-check:hover {
  color: rgba(255, 255, 255, 0.8);
}

.dialog-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.color-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  white-space: nowrap;
}

.color-picker {
  display: flex;
  gap: 6px;
}

.color-option {
  width: 24px;
  height: 24px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: transform 0.2s, border-color 0.2s;
}

.color-option:hover { transform: scale(1.15); }
.color-option.active { border-color: #fff; box-shadow: 0 0 0 2px rgba(255,255,255,0.3); }

.dialog-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cancel-btn {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
  white-space: nowrap;
}

.cancel-btn:hover {
  color: #fff;
}

.submit-btn {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.8);
  border: none;
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.submit-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 评论弹窗 */
.comment-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.comment-dialog {
  background: #2a2a3e;
  border-radius: 16px;
  width: 420px;
  max-width: 90vw;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.comment-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.comment-dialog-header .dialog-title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  color: rgba(255, 255, 255, 0.5);
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #fff;
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  min-height: 200px;
  max-height: 400px;
}

.no-comments {
  color: rgba(255, 255, 255, 0.4);
  text-align: center;
  padding: 40px 0;
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-user {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.delete-comment-btn {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: color 0.2s;
}

.delete-comment-btn:hover {
  color: #ef4444;
}

.comment-content {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
  word-break: break-word;
}

.comment-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 4px;
}

.comment-input-area {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.comment-input-area input {
  flex: 1;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 10px 16px;
  color: #fff;
  font-size: 14px;
  outline: none;
}

.comment-input-area input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.comment-input-area button {
  background: #22c55e;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.comment-input-area button:hover {
  background: #16a34a;
}

.comment-input-area button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 点赞和评论按钮图标样式 */
.action-btn svg {
  width: 14px;
  height: 14px;
  vertical-align: middle;
  margin-right: 2px;
}

.like-btn.liked {
  color: #e74c3c;
}

.like-btn.liked svg {
  fill: #e74c3c;
}
</style>
