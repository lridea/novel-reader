<template>
  <div class="novel-detail">
    <el-card shadow="hover">
      <div class="novel-header">
        <el-button link @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
      </div>
      <div class="novel-info">
        <div class="cover-section">
          <el-image 
            :src="novel.coverUrl || '/placeholder.png'" 
            style="width: 150px; height: 200px;"
            fit="cover"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="48"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div class="info-section">
          <h2 class="title">{{ novel.title }}</h2>
          <div class="meta-info">
            <el-tag>{{ getPlatformName(novel.platform) }}</el-tag>
            <span class="author">作者：{{ novel.author || '未知' }}</span>
            <el-tag :type="getStatusType(novel.status)">
              {{ getStatusText(novel.status) }}
            </el-tag>
            <span class="favorite-count">
              <el-icon><Star /></el-icon>
              {{ novel.favoriteCount || 0 }}
            </span>
            <span class="comment-count">
              <el-icon><ChatDotRound /></el-icon>
              {{ novel.commentCount || 0 }}
            </span>
          </div>
          <div class="update-info">
            <span>最新章节：{{ novel.latestChapterTitle || '-' }}</span>
            <span style="margin-left: 20px;">更新时间：{{ formatDateTime(novel.latestUpdateTime) }}</span>
          </div>
          <div class="tags-section" v-if="novel.tags && parseTags(novel.tags).length > 0">
            <el-tag
              v-for="tag in parseTags(novel.tags)"
              :key="tag"
              size="small"
              type="warning"
              class="novel-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="description">
            <h4>简介</h4>
            <p>{{ novel.description || '暂无简介' }}</p>
          </div>
          <div class="action-section">
            <el-button
              :type="novel.isFavorite ? 'warning' : 'primary'"
              :icon="novel.isFavorite ? StarFilled : Star"
              @click="toggleFavorite(novel)"
              size="large"
            >
              {{ novel.isFavorite ? '已收藏' : '收藏本书' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px;" v-if="novel.firstChaptersSummary">
      <template #header>
        <span>AI 概括（前3章）</span>
      </template>
      <div class="ai-summary">
        {{ novel.firstChaptersSummary }}
      </div>
    </el-card>

    <!-- 评论区域 -->
    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="comment-header">
          <span class="comment-title">评论 <span class="comment-count-num">{{ totalComments }}</span></span>
        </div>
      </template>

      <!-- 发表评论输入框 -->
      <div class="comment-input-section">
        <el-avatar :size="40" :src="isLoggedIn ? userAvatar : '/default-avatar.png'" class="comment-input-avatar" />
        <div v-if="isLoggedIn" class="comment-input-wrapper">
          <el-input
            v-model="topCommentContent"
            type="textarea"
            :rows="1"
            placeholder="发表你的评论..."
            maxlength="500"
            resize="none"
            @keydown.enter.prevent="submitTopComment"
          />
        </div>
        <div v-else class="comment-input-login" @click="goToLogin">
          <span class="login-hint">请先</span>
          <span class="login-btn-text">登录</span>
          <span class="login-hint">后发表评论 (·ω·)</span>
        </div>
        <el-button v-if="isLoggedIn" type="primary" @click="submitTopComment" :disabled="!topCommentContent.trim()">
          发送
        </el-button>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list" v-loading="commentsLoading">
        <div class="comment-item" v-for="comment in topLevelComments" :key="comment.id">
          <!-- 评论内容 -->
          <div class="comment-main">
            <el-avatar :src="comment.user.avatar || '/default-avatar.png'" :size="40" class="comment-avatar" />
            <div class="comment-content">
              <div class="comment-header-row">
                <span class="username">{{ comment.user.nickname || comment.user.username }}</span>
                <span class="time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-actions">
                <span class="action-item" :class="{ liked: comment.liked }" @click="toggleLike(comment)">
                  <el-icon><StarFilled /></el-icon>
                  <span>{{ comment.likeCount || 0 }}</span>
                </span>
                <span class="action-item" @click="showReplyInput(comment)">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>回复</span>
                </span>
              </div>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div class="reply-input-section" v-if="activeReplyComment?.id === comment.id">
            <el-avatar :size="32" :src="isLoggedIn ? userAvatar : '/default-avatar.png'" />
            <div v-if="isLoggedIn" class="reply-input-wrapper">
              <div class="reply-input-hint">回复 @{{ comment.user.nickname || comment.user.username }}：</div>
              <el-input
                v-model="replyInputContent"
                type="textarea"
                :rows="2"
                :placeholder="`回复 ${comment.user.nickname || comment.user.username}...`"
                maxlength="500"
                resize="none"
                ref="replyInputRef"
                @keydown.enter.prevent="submitReplyInput"
              />
            </div>
            <div v-else class="reply-input-login" @click="goToLogin">
              <span class="login-hint">请先</span>
              <span class="login-btn-text">登录</span>
              <span class="login-hint">后回复 (·ω·)</span>
            </div>
            <div v-if="isLoggedIn" class="reply-input-actions">
              <el-button size="small" @click="cancelReplyInput">取消</el-button>
              <el-button type="primary" size="small" @click="submitReplyInput" :disabled="!replyInputContent.trim()">
                发布
              </el-button>
            </div>
          </div>

          <!-- 回复列表 -->
          <div class="reply-list" v-if="comment.replyCount > 0">
            <!-- 回复内容 -->
            <div v-if="comment.replies && comment.replies.length > 0">
              <div class="reply-item" v-for="reply in comment.replies" :key="reply.id">
                <el-avatar :src="reply.user.avatar || '/default-avatar.png'" :size="28" class="reply-avatar" />
                <div class="reply-content">
                  <div class="reply-header-row">
                    <span class="username">{{ reply.user.nickname || reply.user.username }}</span>
                    <span class="reply-arrow">回复</span>
                    <span class="target-user">@{{ reply.parentUser.nickname || reply.parentUser.username }}</span>
                    <span class="time">{{ formatTime(reply.createdAt) }}</span>
                  </div>
                  <div class="reply-text">{{ reply.content }}</div>
                  <div class="reply-actions">
                    <span class="action-item" :class="{ liked: reply.liked }" @click="toggleLike(reply)">
                      <el-icon><StarFilled /></el-icon>
                      <span>{{ reply.likeCount || 0 }}</span>
                    </span>
                  </div>
                </div>
              </div>

              <!-- 展开后的分页导航 -->
              <div class="reply-pagination" v-if="comment.showReplies && comment.replyTotalPages > 1">
                <span class="reply-page-info">共{{ comment.replyTotalPages }}页</span>
                <span
                  v-for="page in comment.replyTotalPages"
                  :key="page"
                  class="reply-page-num"
                  :class="{ active: comment.replyPage === page }"
                  @click="loadReplyPage(comment, page)"
                >
                  {{ page }}
                </span>
                <span
                  v-if="comment.replyPage < comment.replyTotalPages"
                  class="reply-page-next"
                  @click="loadReplyPage(comment, comment.replyPage + 1)"
                >
                  下一页
                </span>
                <span class="reply-page-collapse" @click="collapseReplies(comment)">收起</span>
              </div>

              <!-- 展开后只有一页时的收起按钮 -->
              <div class="reply-pagination" v-else-if="comment.showReplies">
                <span class="reply-page-collapse" @click="collapseReplies(comment)">收起</span>
              </div>
            </div>

            <!-- 展开回复按钮（未展开时显示） -->
            <div v-if="!comment.showReplies && comment.replyCount > 2" class="reply-expand" @click="expandReplies(comment)">
              <span>共{{ comment.replyCount }}条回复，点击查看</span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty v-if="topLevelComments.length === 0 && !commentsLoading" description="暂无评论" />

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="commentPage"
          v-model:page-size="commentPageSize"
          :total="totalComments"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadComments"
          @current-change="loadComments"
          style="margin-top: 20px; justify-content: flex-end;"
        />
      </div>

    </el-card>

    <!-- 悬浮评论输入框 - 滚动时显示 -->
    <div class="comment-input-sticky" v-if="showStickyInput">
      <div class="sticky-content">
        <el-avatar :size="36" :src="isLoggedIn ? userAvatar : '/default-avatar.png'" />
        <div v-if="isLoggedIn" class="sticky-input-wrapper">
          <el-input
            v-model="stickyCommentContent"
            type="textarea"
            :rows="1"
            placeholder="发表你的评论..."
            maxlength="500"
            resize="none"
            @keydown.enter.prevent="submitStickyComment"
          />
        </div>
        <div v-else class="sticky-input-login" @click="goToLogin">
          <span class="login-hint">请先</span>
          <span class="login-btn-text">登录</span>
          <span class="login-hint">后发表评论 (·ω·)</span>
        </div>
        <el-button v-if="isLoggedIn" type="primary" @click="submitStickyComment" :disabled="!stickyCommentContent.trim()">
          发送
        </el-button>
      </div>
    </div>

    <!-- 添加评论对话框 -->
    <el-dialog
      v-model="addCommentDialogVisible"
      title="发表评论"
      width="500px"
      @close="resetAddCommentForm"
    >
      <el-form :model="addCommentForm" label-width="80px">
        <el-form-item label="评论内容">
          <el-input
            v-model="addCommentForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评论内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addCommentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment">发表</el-button>
      </template>
    </el-dialog>

    <!-- 回复评论对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      :title="`回复 ${replyTarget?.user?.nickname || replyTarget?.user?.username}`"
      width="500px"
      @close="resetReplyForm"
    >
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">发表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Star, StarFilled, ChatDotRound, ArrowDown, ArrowLeft } from '@element-plus/icons-vue'
import { crawlerApi, favoriteApi } from '../api'

const route = useRoute()
const router = useRouter()

const novel = ref({
  id: null,
  title: '',
  author: '',
  platform: '',
  description: '',
  coverUrl: '',
  latestChapterTitle: '',
  latestUpdateTime: '',
  firstChaptersSummary: '',
  lastCrawlTime: '',
  crawlCount: 0,
  status: 1,
  favoriteCount: 0,
  commentCount: 0,
  isFavorite: null
})

// 评论相关数据
const commentsLoading = ref(false)
const topLevelComments = ref([])
const totalComments = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(10)

// 当前用户头像
const userAvatar = ref('')

// 是否显示悬浮评论输入框
const showStickyInput = ref(false)

// 悬浮评论输入框内容
const stickyCommentContent = ref('')

// 顶部评论输入框内容
const topCommentContent = ref('')

// 回复输入框相关
const activeReplyComment = ref(null)
const replyInputContent = ref('')
const replyInputRef = ref(null)

// 登录状态
const isLoggedIn = ref(false)

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 添加评论对话框
const addCommentDialogVisible = ref(false)
const addCommentForm = ref({
  content: ''
})

// 回复评论对话框
const replyDialogVisible = ref(false)
const replyTarget = ref(null)
const replyForm = ref({
  content: ''
})

const getPlatformName = (platform) => {
  const names = {
    ciweimao: '刺猬猫',
    sf: 'SF轻小说',
    ciyuanji: '次元姬',
    qidian: '起点'
  }
  return names[platform] || platform
}

const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'success',
    2: 'primary'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '停更',
    1: '连载',
    2: '完结'
  }
  return texts[status] || '未知'
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  try {
    const parsed = JSON.parse(tagsStr)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return tagsStr.split(',').filter(tag => tag.trim())
  }
}

const goBack = () => {
  router.push('/')
}

const toggleFavorite = async (row) => {
  try {
    if (row.isFavorite) {
      const response = await favoriteApi.removeFavorite(row.id)
      if (response && response.success) {
        row.isFavorite = false
        row.favoriteCount = Math.max(0, row.favoriteCount - 1)
        ElMessage.success('取消收藏成功')
      } else {
        ElMessage.error(response.message || '取消收藏失败')
      }
    } else {
      const response = await favoriteApi.addFavorite({ novelId: row.id, note: '' })
      if (response && response.success) {
        row.isFavorite = true
        row.favoriteCount = (row.favoriteCount || 0) + 1
        ElMessage.success('收藏成功')
      } else {
        ElMessage.error(response.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('切换收藏失败:', error)
    ElMessage.error('操作失败')
  }
}

const loadData = async () => {
  const novelId = route.params.id
  try {
    const response = await crawlerApi.getNovelById(novelId)
    if (response && response.success && response.data) {
      novel.value = response.data
      novel.value.isFavorite = false
      loadComments()
      checkFavoriteStatus(novelId)
    } else {
      ElMessage.error(response?.message || '获取小说详情失败')
    }
  } catch (error) {
    console.error('加载小说详情失败:', error)
    ElMessage.error('加载小说详情失败')
  }
}

const checkFavoriteStatus = async (novelId) => {
  try {
    const response = await favoriteApi.checkBatchFavorites(novelId)
    if (response && response.success) {
      novel.value.isFavorite = response.favorites && response.favorites[novelId]
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 加载评论
const loadComments = async () => {
  if (!novel.value.id) {
    return
  }
  commentsLoading.value = true
  try {
    const response = await crawlerApi.getComments(novel.value.id, {
      page: commentPage.value - 1,
      size: commentPageSize.value,
      floor: 1
    })
    if (response) {
      topLevelComments.value = (response.content || []).map(comment => ({
        ...comment,
        showReplies: false,
        replyPage: 1,
        replyTotalPages: 1,
        replies: []
      }))
      totalComments.value = response.totalElements || 0
      // 默认加载2条回复
      for (const comment of topLevelComments.value) {
        if (comment.replyCount > 0) {
          await loadReplyPage(comment, 1, 2)
        }
      }
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

// 展开回复
const expandReplies = async (comment) => {
  comment.showReplies = true
  comment.replyPage = 1
  await loadReplyPage(comment, 1, 10)
}

// 收起回复
const collapseReplies = async (comment) => {
  comment.showReplies = false
  // 收起后只保留前2条
  await loadReplyPage(comment, 1, 2)
}

// 加载指定页回复
const loadReplyPage = async (comment, page, pageSize = 10) => {
  try {
    const response = await crawlerApi.getComments(novel.value.id, {
      page: page - 1,
      size: pageSize,
      floor: 2,
      parentId: comment.id
    })
    if (response && response.content) {
      comment.replies = response.content
      comment.replyPage = page
      comment.replyTotalPages = response.totalPages || 1
    }
  } catch (error) {
    console.error('加载回复失败:', error)
  }
}

// 显示添加评论对话框
const showAddCommentDialog = () => {
  addCommentDialogVisible.value = true
}

// 重置添加评论表单
const resetAddCommentForm = () => {
  addCommentForm.value = {
    content: ''
  }
}

// 提交评论
const submitComment = async () => {
  if (!addCommentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    const response = await crawlerApi.addComment({
      novelId: novel.value.id,
      parentId: null,
      floor: 1,
      content: addCommentForm.value.content
    })
    if (response && response.success) {
      ElMessage.success('评论成功')
      novel.value.commentCount = response.novelCommentCount || 0
      addCommentDialogVisible.value = false
      resetAddCommentForm()
      loadComments()
    } else {
      ElMessage.error(response.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  }
}

// 显示回复对话框（保留用于回复的回复）
const showReplyDialog = (comment, reply = null) => {
  replyTarget.value = reply || comment
  replyDialogVisible.value = true
}

// 显示回复输入框
const showReplyInput = (comment) => {
  activeReplyComment.value = comment
  replyInputContent.value = ''
  // 自动聚焦输入框
  setTimeout(() => {
    replyInputRef.value?.focus()
  }, 100)
}

// 取消回复输入
const cancelReplyInput = () => {
  activeReplyComment.value = null
  replyInputContent.value = ''
}

// 提交回复
const submitReplyInput = async () => {
  if (!replyInputContent.value.trim() || !activeReplyComment.value) {
    return
  }
  try {
    const response = await crawlerApi.addComment({
      novelId: novel.value.id,
      parentId: activeReplyComment.value.id,
      floor: 2,
      content: replyInputContent.value
    })
    if (response && response.success) {
      ElMessage.success('回复成功')
      novel.value.commentCount = response.novelCommentCount || 0
      activeReplyComment.value = null
      replyInputContent.value = ''
      loadComments()
    } else {
      ElMessage.error(response.message || '回复失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

// 重置回复表单
const resetReplyForm = () => {
  replyForm.value = {
    content: ''
  }
  replyTarget.value = null
}

// 提交回复
const submitReply = async () => {
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    const response = await crawlerApi.addComment({
      novelId: novel.value.id,
      parentId: replyTarget.value.id,
      floor: 2,
      content: replyForm.value.content
    })
    if (response && response.success) {
      ElMessage.success('回复成功')
      novel.value.commentCount = response.novelCommentCount || 0
      replyDialogVisible.value = false
      resetReplyForm()
      loadComments()
    } else {
      ElMessage.error(response.message || '回复失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 小于1天
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }

  // 小于7天
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  }

  // 超过7天，显示具体日期
  return date.toLocaleDateString('zh-CN')
}

// 点赞/取消点赞评论
const toggleLike = async (comment) => {
  try {
    let response
    if (comment.liked) {
      // 取消点赞
      response = await crawlerApi.unlikeComment(comment.id)
      if (response && response.success) {
        ElMessage.success('取消点赞成功')
        comment.liked = false
        comment.likeCount = response.commentLikeCount || 0
      } else {
        ElMessage.error(response.message || '取消点赞失败')
      }
    } else {
      // 点赞
      response = await crawlerApi.likeComment(comment.id)
      if (response && response.success) {
        ElMessage.success('点赞成功')
        comment.liked = true
        comment.likeCount = response.commentLikeCount || 0
      } else {
        ElMessage.error(response.message || '点赞失败')
      }
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
  checkLoginStatus()
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
})

// 滚动处理 - 当滚动到评论区时显示悬浮输入框
const handleScroll = () => {
  const commentSection = document.querySelector('.comment-list')
  if (commentSection) {
    const rect = commentSection.getBoundingClientRect()
    // 当评论区进入视口且顶部评论输入框不在视口时显示悬浮框
    showStickyInput.value = rect.top < 0 && rect.bottom > 200
  }
}

// 提交悬浮评论
const submitStickyComment = async () => {
  if (!stickyCommentContent.value.trim()) {
    return
  }
  try {
    const response = await crawlerApi.addComment({
      novelId: novel.value.id,
      parentId: null,
      floor: 1,
      content: stickyCommentContent.value
    })
    if (response && response.success) {
      ElMessage.success('评论成功')
      novel.value.commentCount = response.novelCommentCount || 0
      stickyCommentContent.value = ''
      loadComments()
    } else {
      ElMessage.error(response.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  }
}

// 提交顶部评论
const submitTopComment = async () => {
  if (!topCommentContent.value.trim()) {
    return
  }
  try {
    const response = await crawlerApi.addComment({
      novelId: novel.value.id,
      parentId: null,
      floor: 1,
      content: topCommentContent.value
    })
    if (response && response.success) {
      ElMessage.success('评论成功')
      novel.value.commentCount = response.novelCommentCount || 0
      topCommentContent.value = ''
      loadComments()
    } else {
      ElMessage.error(response.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  }
}
</script>

<style scoped>
.novel-detail {
  padding: 0;
}

.novel-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  font-size: 14px;
  color: #606266;
  padding: 0;
}

.back-btn:hover {
  color: #409eff;
}

.novel-info {
  display: flex;
  gap: 30px;
}

.cover-section {
  flex-shrink: 0;
}

.image-placeholder {
  width: 150px;
  height: 200px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

.info-section {
  flex: 1;
}

.title {
  margin: 0 0 16px 0;
  font-size: 24px;
  color: #303133;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.author {
  color: #606266;
}

.favorite-count {
  color: #F56C6C;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-count {
  color: #409EFF;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.update-info {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.tags-section {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.novel-tag {
  cursor: default;
}

.description {
  margin-top: 16px;
}

.description h4 {
  margin: 0 0 8px 0;
  color: #606266;
}

.description p {
  color: #303133;
  line-height: 1.8;
  margin: 0;
}

.action-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.ai-summary {
  line-height: 1.8;
  color: #606266;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

/* 评论区域样式 */
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  color: #18191c;
}

.comment-count-num {
  font-size: 14px;
  color: #9499a0;
  font-weight: normal;
  margin-left: 4px;
}

/* 评论输入框区域 */
.comment-input-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #f1f2f3;
}

.comment-input-avatar {
  flex-shrink: 0;
}

.comment-input-wrapper {
  flex: 1;
}

.comment-input-wrapper :deep(.el-textarea__inner) {
  min-height: 44px !important;
  max-height: 120px;
  background: #f1f2f3;
  border: none;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
}

.comment-input-wrapper :deep(.el-textarea__inner:focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #00a1d6;
}

/* 底部固定评论输入框 */
.comment-input-fixed {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  margin: 20px -20px -20px -20px;
  background: #fff;
  border-top: 1px solid #e3e5e7;
}

.comment-input-fixed .comment-input-wrapper {
  flex: 1;
  min-height: 36px;
  background: #f1f2f3;
  border-radius: 6px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  cursor: pointer;
  transition: background 0.2s;
}

/* 悬浮评论输入框 */
.comment-input-sticky {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e3e5e7;
  padding: 12px 20px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.sticky-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sticky-input-wrapper {
  flex: 1;
}

.sticky-input-wrapper :deep(.el-textarea__inner) {
  min-height: 40px !important;
  max-height: 120px;
  background: #f1f2f3;
  border: none;
  border-radius: 4px;
  padding: 10px 16px;
  font-size: 14px;
}

.comment-input-wrapper :deep(.el-textarea__inner:focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #00a1d6;
}

.sticky-input-login {
  flex: 1;
  height: 40px;
  background: #f1f2f3;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}

.sticky-input-login:hover {
  background: #e3e5e7;
}

.comment-input-login {
  flex: 1;
  height: 44px;
  background: #f1f2f3;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}

.comment-input-login:hover {
  background: #e3e5e7;
}

.login-hint {
  color: #9499a0;
  font-size: 14px;
}

.login-btn-text {
  color: #00a1d6;
  font-size: 14px;
  margin: 0 4px;
}

.comment-list {
  min-height: 200px;
}

.comment-item {
  padding: 24px 0 16px 0;
  border-bottom: 1px solid #f1f2f3;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  display: flex;
  gap: 16px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-header-row .username {
  color: #61666d;
  font-size: 14px;
  font-weight: 500;
}

.comment-header-row .time {
  color: #9499a0;
  font-size: 12px;
}

.comment-text {
  color: #18191c;
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 12px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 20px;
}

.action-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #9499a0;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.action-item:hover {
  color: #00a1d6;
  background: #e3e5e7;
}

.action-item.liked {
  color: #00a1d6;
}

.action-item .el-icon {
  font-size: 16px;
}

/* 回复列表样式 - B站风格 */
.reply-list {
  margin-top: 12px;
  margin-left: 56px;
  padding: 12px 16px;
  background: #f6f7f8;
  border-radius: 4px;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
}

.reply-item:first-child {
  padding-top: 0;
}

.reply-item:not(:last-child) {
  border-bottom: 1px solid #e3e5e7;
}

.reply-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.reply-avatar {
  flex-shrink: 0;
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-header-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.reply-header-row .username {
  color: #61666d;
  font-weight: 500;
  font-size: 13px;
}

.reply-arrow {
  color: #9499a0;
  font-size: 12px;
}

.reply-header-row .target-user {
  color: #61666d;
  font-size: 13px;
}

.reply-header-row .time {
  color: #9499a0;
  font-size: 12px;
  margin-left: auto;
}

.reply-text {
  color: #18191c;
  line-height: 1.6;
  margin-bottom: 8px;
  font-size: 14px;
  word-break: break-word;
}

.reply-actions {
  display: flex;
  gap: 16px;
}

/* 回复输入框 */
.reply-input-section {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin: 16px 0 16px 56px;
  padding: 16px;
  background: #f6f7f8;
  border-radius: 8px;
}

.reply-input-wrapper {
  flex: 1;
}

.reply-input-hint {
  font-size: 13px;
  color: #9499a0;
  margin-bottom: 8px;
}

.reply-input-wrapper :deep(.el-textarea__inner) {
  background: #fff;
  border: 1px solid #e3e5e7;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 14px;
  min-height: 60px !important;
}

.reply-input-wrapper :deep(.el-textarea__inner:focus) {
  border-color: #00a1d6;
  box-shadow: 0 0 0 1px rgba(0, 161, 214, 0.2);
}

.reply-input-login {
  flex: 1;
  height: 60px;
  background: #fff;
  border: 1px solid #e3e5e7;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.reply-input-login:hover {
  border-color: #00a1d6;
}

.reply-input-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.more-replies {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #9499a0;
  font-size: 13px;
  cursor: pointer;
  padding: 8px 0 4px 0;
  margin-top: 4px;
  border-top: 1px solid #e3e5e7;
}

.more-replies:hover {
  color: #00a1d6;
}

.more-replies .el-icon {
  font-size: 14px;
}

/* 回复展开按钮 */
.reply-expand {
  margin-top: 12px;
  margin-left: 56px;
  padding: 6px 0;
  color: #9499a0;
  font-size: 13px;
  cursor: pointer;
  display: inline-block;
}

.reply-expand:hover {
  color: #00a1d6;
}

/* 回复分页 */
.reply-pagination {
  margin-top: 12px;
  margin-left: 56px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.reply-page-info {
  color: #9499a0;
}

.reply-page-num {
  color: #9499a0;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 4px;
}

.reply-page-num:hover {
  color: #00a1d6;
}

.reply-page-num.active {
  color: #00a1d6;
  background: #e3e5e7;
}

.reply-page-next {
  color: #9499a0;
  cursor: pointer;
}

.reply-page-next:hover {
  color: #00a1d6;
}

.reply-page-collapse {
  color: #9499a0;
  cursor: pointer;
}

.reply-page-collapse:hover {
  color: #00a1d6;
}
</style>
