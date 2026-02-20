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
          <h2 class="title" @click="copyTitle" title="点击复制书名">{{ novel.title }}</h2>
          <div class="meta-info">
            <el-tag>{{ getPlatformName(novel.platform) }}</el-tag>
            <div class="author-row">
              <span class="author">作者：{{ novel.author || '未知' }}</span>
              <el-tag :type="getStatusType(novel.status)">
                {{ getStatusText(novel.status) }}
              </el-tag>
            </div>
            <div class="mobile-stats">
              <span class="favorite-count">
                <el-icon><Star /></el-icon>
                {{ novel.favoriteCount || 0 }}
              </span>
              <span class="comment-count">
                <el-icon><ChatDotRound /></el-icon>
                {{ novel.commentCount || 0 }}
              </span>
              <span class="dislike-count">
                <svg class="dislike-icon" viewBox="0 0 24 24" width="14" height="14" style="transform: rotate(180deg) scaleX(-1);">
                  <path d="M9 21h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.58 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2zM9 9l4.34-4.34L12 10h9v2l-3 7H9V9zM1 9h4v12H1V9z"/>
                </svg>
                {{ novel.dislikeCount || 0 }}
              </span>
            </div>
          </div>
          <div class="update-info">
            <span>更新时间：{{ formatDateTime(novel.latestUpdateTime) }}</span>
            <span class="word-count">字数：{{ formatWordCount(novel.wordCount) }}</span>
          </div>
          <div class="tags-section">
            <!-- 爬取的标签 -->
            <el-tag
              v-for="tag in parseTags(novel.tags)"
              :key="'crawl-' + tag"
              size="small"
              type="warning"
              class="novel-tag"
            >
              {{ tag }}
            </el-tag>
            <!-- 用户申请的标签 -->
            <el-tag
              v-for="tag in userTags"
              :key="'user-' + tag"
              size="small"
              type="danger"
              class="novel-tag"
            >
              {{ tag }}
            </el-tag>
            <el-button
              v-if="isLoggedIn"
              link
              type="danger"
              size="small"
              class="add-tag-btn"
              @click="showAddTagDialog"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
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
            <el-button
              :type="novel.isDisliked ? 'danger' : 'default'"
              @click="toggleDislike"
              size="large"
            >
              <template #icon>
                <svg class="dislike-btn-icon" viewBox="0 0 24 24" width="16" height="16" style="transform: rotate(180deg) scaleX(-1);">
                  <path d="M9 21h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.58 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2zM9 9l4.34-4.34L12 10h9v2l-3 7H9V9zM1 9h4v12H1V9z"/>
                </svg>
              </template>
              {{ novel.isDisliked ? '取消点踩' : '点踩' }}
            </el-button>
            <el-button
              v-if="novel.sourceUrl"
              type="default"
              size="large"
              @click="goToSource"
            >
              查看源站
            </el-button>
          </div>
        </div>
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
                <span class="floor-number">{{ comment.floorNumber }}楼</span>
                <span class="time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-actions">
                <span class="action-item" :class="{ liked: comment.liked }" @click="toggleLike(comment)">
                  <el-icon>
                    <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="1em" height="1em">
                      <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52.8 0-98.1 38.8-105.1 90.6l-26.3 184.9H176.3c-37.5 0-68 30.5-68 68v352c0 37.5 30.5 68 68 68h523.9c6.9 0 13.8-1.1 20.3-3.2 28.3-9.4 52.5-27.7 69.5-52.1l95.9-134.6z" fill="currentColor"/>
                    </svg>
                  </el-icon>
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
                placeholder="输入回复..."
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
                <div class="reply-content-wrapper">
                  <div class="reply-content">
                    <div class="reply-header-row">
                      <span class="username">{{ reply.user.nickname || reply.user.username }}</span>
                      <span class="floor-number reply-floor">{{ reply.replyFloorNumber }}楼</span>
                      <template v-if="reply.replyToUser">
                        <span class="reply-arrow">回复</span>
                        <span class="target-user">@{{ reply.replyToUser.nickname || reply.replyToUser.username }} {{ reply.replyToUser.floorNumber }}楼</span>
                      </template>
                      <span class="time">{{ formatTime(reply.createdAt) }}</span>
                    </div>
                    <div class="reply-text">{{ reply.content }}</div>
                    <div class="reply-actions">
                      <span class="action-item" :class="{ liked: reply.liked }" @click="toggleLike(reply)">
                        <el-icon>
                          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="1em" height="1em">
                            <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52.8 0-98.1 38.8-105.1 90.6l-26.3 184.9H176.3c-37.5 0-68 30.5-68 68v352c0 37.5 30.5 68 68 68h523.9c6.9 0 13.8-1.1 20.3-3.2 28.3-9.4 52.5-27.7 69.5-52.1l95.9-134.6z" fill="currentColor"/>
                          </svg>
                        </el-icon>
                        <span>{{ reply.likeCount || 0 }}</span>
                      </span>
                      <span class="action-item reply-btn" @click="showReplyToReplyInput(comment, reply)">
                        回复
                      </span>
                    </div>
                  </div>
                  <!-- 回复的回复输入框 - 显示在该条回复下方 -->
                  <div v-if="activeReplyToReply && activeReplyToReply.parentCommentId === comment.id && activeReplyToReply.replyId === reply.id" class="reply-to-reply-input">
                    <div class="reply-input-hint">回复 @{{ activeReplyToReply.targetUser.nickname || activeReplyToReply.targetUser.username }}：</div>
                    <el-input
                      v-if="isLoggedIn"
                      v-model="replyToReplyContent"
                      type="textarea"
                      :rows="2"
                      placeholder="输入回复..."
                      maxlength="500"
                      resize="none"
                      @keydown.enter.prevent="submitReplyToReply"
                    />
                    <div v-else class="reply-input-login" @click="goToLogin">
                      <span class="login-hint">请先</span>
                      <span class="login-btn-text">登录</span>
                      <span class="login-hint">后回复 (·ω·)</span>
                    </div>
                    <div v-if="isLoggedIn" class="reply-input-actions">
                      <el-button size="small" @click="cancelReplyToReply">取消</el-button>
                      <el-button type="primary" size="small" @click="submitReplyToReply" :disabled="!replyToReplyContent.trim()">发送</el-button>
                    </div>
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

        <!-- PC端分页 -->
        <el-pagination
          v-if="!isMobile"
          v-model:current-page="commentPage"
          v-model:page-size="commentPageSize"
          :total="totalComments"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadComments"
          @current-change="loadComments"
          style="margin-top: 20px; justify-content: center;"
        />

        <!-- 移动端加载状态 -->
        <div v-if="isMobile && topLevelComments.length > 0 && commentsLoading" class="loading-more-comments">
          <el-icon class="is-loading"><ArrowDown /></el-icon>
          <span>加载中...</span>
        </div>

        <!-- 移动端没有更多 -->
        <div v-if="isMobile && topLevelComments.length > 0 && !commentsLoading && topLevelComments.length >= totalComments" class="no-more-comments">
          没有更多评论了
        </div>
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
            maxlength="2000"
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

    <el-dialog
      v-model="showCategoryDialog"
      title="选择收藏夹"
      width="320px"
    >
      <el-select
        v-model="selectedCategoryId"
        placeholder="请选择收藏夹"
        class="category-select"
        style="width: 100%"
      >
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name + ' (' + (category.favoriteCount || 0) + '本)'"
          :value="category.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="showCategoryDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddFavorite">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加标签对话框 -->
    <el-dialog
      v-model="addTagDialogVisible"
      title="申请添加标签"
      width="90%"
      class="add-tag-dialog"
      :style="{ maxWidth: '320px' }"
      align-center
    >
      <el-form>
        <el-form-item label="标签名称">
          <el-input
            v-model="newTagName"
            placeholder="请输入标签名称"
            :maxlength="MAX_TAG_LENGTH"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addTagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTag">提交</el-button>
      </template>
    </el-dialog>

    <!-- 登录提示弹窗 -->
    <el-dialog
      v-model="showLoginDialog"
      title="提示"
      width="300px"
      :show-close="false"
      :close-on-click-modal="false"
    >
      <div class="login-dialog-content">
        <p>请先登录后再进行此操作</p>
      </div>
      <template #footer>
        <el-button @click="showLoginDialog = false">取消</el-button>
        <el-button type="primary" @click="goToLogin">去登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Star, StarFilled, ChatDotRound, ArrowDown, ArrowLeft, Plus } from '@element-plus/icons-vue'
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
  wordCount: 0,
  firstChaptersSummary: '',
  lastCrawlTime: '',
  crawlCount: 0,
  status: 1,
  favoriteCount: 0,
  commentCount: 0,
  dislikeCount: 0,
  isFavorite: null,
  isDisliked: false
})

// 用户申请的标签列表
const userTags = ref([])

// 添加标签对话框
const addTagDialogVisible = ref(false)
const newTagName = ref('')
const MAX_TAG_LENGTH = 9

// 评论相关数据
const commentsLoading = ref(false)
const topLevelComments = ref([])
const totalComments = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(10)

// 移动端检测
const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

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

// 回复的回复相关
const activeReplyToReply = ref(null)
const replyToReplyContent = ref('')

// 登录状态
const isLoggedIn = ref(false)
const showLoginDialog = ref(false)

// 收藏夹相关
const categories = ref([])
const showCategoryDialog = ref(false)
const selectedCategoryId = ref(null)

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
}

// 加载收藏夹列表
const loadCategories = async () => {
  try {
    const response = await crawlerApi.getCategories()
    if (response && response.success) {
      // 将默认收藏夹排在第一位
      const list = response.categories || []
      categories.value = list.sort((a, b) => {
        if (a.isDefault && !b.isDefault) return -1
        if (!a.isDefault && b.isDefault) return 1
        return 0
      })
    }
  } catch (error) {
    console.error('加载收藏夹失败:', error)
  }
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

const formatWordCount = (count) => {
  if (!count) return '-'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toLocaleString()
}

const copyTitle = async () => {
  if (!novel.value.title) return
  try {
    await navigator.clipboard.writeText(novel.value.title)
    ElMessage.success('书名已复制到剪贴板')
  } catch (err) {
    const textArea = document.createElement('textarea')
    textArea.value = novel.value.title
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('书名已复制到剪贴板')
    } catch (e) {
      ElMessage.error('复制失败')
    }
    document.body.removeChild(textArea)
  }
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

// 判断是否为用户的标签
const isUserTag = (tag) => {
  return userTags.value.includes(tag)
}

// 显示添加标签对话框
const showAddTagDialog = () => {
  newTagName.value = ''
  addTagDialogVisible.value = true
}

// 提交标签申请
const submitTag = async () => {
  const tag = newTagName.value.trim()
  if (!tag) {
    ElMessage.warning('请输入标签名称')
    return
  }
  if (tag.length > MAX_TAG_LENGTH) {
    ElMessage.warning(`标签名称不能超过${MAX_TAG_LENGTH}个字符`)
    return
  }
  
  try {
    const response = await crawlerApi.addTag({
      novelId: novel.value.id,
      tag: tag
    })
    if (response && response.success) {
      ElMessage.success(response.message)
      addTagDialogVisible.value = false
      newTagName.value = ''
    } else {
      ElMessage.error(response?.message || '提交失败')
    }
  } catch (error) {
    console.error('提交标签失败:', error)
    ElMessage.error('提交失败')
  }
}

const goBack = () => {
  router.back()
}

const toggleFavorite = async (row) => {
  if (!isLoggedIn.value) {
    showLoginDialog.value = true
    return
  }

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
      if (categories.value.length === 0) {
        await loadCategories()
      }
      
      if (categories.value.length === 1) {
        const response = await favoriteApi.addFavorite({ 
          novelId: row.id, 
          categoryId: categories.value[0].id,
          note: '' 
        })
        if (response && response.success) {
          row.isFavorite = true
          row.favoriteCount = (row.favoriteCount || 0) + 1
          ElMessage.success('收藏成功')
        } else {
          ElMessage.error(response.message || '收藏失败')
        }
      } else {
        selectedCategoryId.value = categories.value.find(c => c.isDefault)?.id || categories.value[0]?.id
        showCategoryDialog.value = true
      }
    }
  } catch (error) {
    console.error('切换收藏失败:', error)
    ElMessage.error('操作失败')
  }
}

const confirmAddFavorite = async () => {
  try {
    const response = await favoriteApi.addFavorite({ 
      novelId: novel.value.id, 
      categoryId: selectedCategoryId.value,
      note: '' 
    })
    if (response && response.success) {
      novel.value.isFavorite = true
      novel.value.favoriteCount = (novel.value.favoriteCount || 0) + 1
      showCategoryDialog.value = false
      ElMessage.success('收藏成功')
    } else {
      ElMessage.error(response.message || '收藏失败')
    }
  } catch (error) {
    console.error('收藏失败:', error)
    ElMessage.error('收藏失败')
  }
}

const goToSource = () => {
  if (novel.value.sourceUrl) {
    window.open(novel.value.sourceUrl, '_blank')
  }
}

const loadData = async () => {
  const novelId = route.params.id
  try {
    const response = await crawlerApi.getNovelById(novelId)
    if (response && response.success && response.data) {
      novel.value = response.data
      novel.value.isFavorite = false
      novel.value.isDisliked = response.isDisliked || false
      // 从 response.userTags 解析用户标签（后端在根级别返回）
      userTags.value = response.userTags || []
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

// 点踩/取消点踩
const toggleDislike = async () => {
  if (!isLoggedIn.value) {
    showLoginDialog.value = true
    return
  }

  try {
    let response
    if (novel.value.isDisliked) {
      response = await crawlerApi.undislikeNovel(novel.value.id)
    } else {
      response = await crawlerApi.dislikeNovel(novel.value.id)
    }

    if (response && response.success) {
      novel.value.isDisliked = response.disliked
      novel.value.dislikeCount = response.dislikeCount
      ElMessage.success(response.message)
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    console.error('点踩操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 加载评论
const loadComments = async (preserveState = false, append = false) => {
  if (!novel.value.id) {
    return
  }
  commentsLoading.value = true
  try {
    // 保存当前展开状态
    const stateMap = new Map()
    if (preserveState) {
      topLevelComments.value.forEach(c => {
        stateMap.set(c.id, {
          showReplies: c.showReplies,
          replyPage: c.replyPage,
          replyTotalPages: c.replyTotalPages
        })
      })
    }

    const response = await crawlerApi.getComments(novel.value.id, {
      page: commentPage.value - 1,
      size: commentPageSize.value,
      floor: 1
    })
    if (response) {
      const newComments = (response.content || []).map(comment => {
        const savedState = stateMap.get(comment.id)
        return {
          ...comment,
          showReplies: savedState?.showReplies || false,
          replyPage: savedState?.replyPage || 1,
          replyTotalPages: savedState?.replyTotalPages || 1,
          replies: []
        }
      })
      
      if (append) {
        topLevelComments.value = [...topLevelComments.value, ...newComments]
      } else {
        topLevelComments.value = newComments
      }
      totalComments.value = response.totalElements || 0
      
      // 恢复展开状态或默认加载2条回复
      for (const comment of newComments) {
        if (comment.replyCount > 0) {
          if (comment.showReplies) {
            await loadReplyPage(comment, comment.replyPage, 10)
          } else {
            await loadReplyPage(comment, 1, 2)
          }
        }
      }
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

// 移动端加载更多评论
const loadMoreComments = () => {
  if (commentsLoading.value) return
  if (topLevelComments.value.length < totalComments.value) {
    commentPage.value++
    loadComments(false, true)
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
      loadComments(true)
    } else {
      ElMessage.error(response.message || '回复失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

// 显示回复的回复输入框
const showReplyToReplyInput = (parentComment, reply) => {
  activeReplyToReply.value = {
    parentCommentId: parentComment.id,
    targetUser: reply.user,
    replyId: reply.id
  }
  replyToReplyContent.value = ''
    // 自动聚焦输入框
  setTimeout(() => {
    replyToReplyInputRef.value?.focus()
  }, 100)
}

// 取消回复的回复
const cancelReplyToReply = () => {
  activeReplyToReply.value = null
  replyToReplyContent.value = ''
}

// 提交回复的回复
const submitReplyToReply = async () => {
  if (!replyToReplyContent.value.trim() || !activeReplyToReply.value) {
    return
  }
  try {
    const response = await crawlerApi.addComment({
      novelId: novel.value.id,
      parentId: activeReplyToReply.value.parentCommentId,
      replyToId: activeReplyToReply.value.replyId,
      floor: 2,
      content: replyToReplyContent.value
    })
    if (response && response.success) {
      ElMessage.success('回复成功')
      novel.value.commentCount = response.novelCommentCount || 0
      activeReplyToReply.value = null
      replyToReplyContent.value = ''
      loadComments(true)
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
      loadComments(true)
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
    const commentId = comment.id
    if (comment.liked) {
      // 取消点赞
      response = await crawlerApi.unlikeComment(commentId)
      if (response && response.success) {
        ElMessage.success('取消点赞成功')
        updateCommentLike(commentId, false, response.commentLikeCount)
      } else {
        ElMessage.error(response.message || '取消点赞失败')
      }
    } else {
      // 点赞
      response = await crawlerApi.likeComment(commentId)
      if (response && response.success) {
        ElMessage.success('点赞成功')
        updateCommentLike(commentId, true, response.commentLikeCount)
      } else {
        ElMessage.error(response.message || '点赞失败')
      }
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('操作失败')
  }
}

// 更新评论点赞状态
const updateCommentLike = (commentId, liked, likeCount) => {
  // 在顶层评论中查找
  for (let i = 0; i < topLevelComments.value.length; i++) {
    const comment = topLevelComments.value[i]
    if (comment.id === commentId) {
      topLevelComments.value[i] = {
        ...comment,
        liked: liked,
        likeCount: likeCount !== undefined ? likeCount : comment.likeCount
      }
      return
    }
    // 在回复中查找
    if (comment.replies) {
      for (let j = 0; j < comment.replies.length; j++) {
        if (comment.replies[j].id === commentId) {
          comment.replies[j] = {
            ...comment.replies[j],
            liked: liked,
            likeCount: likeCount !== undefined ? likeCount : comment.replies[j].likeCount
          }
          return
        }
      }
    }
  }
}

// 滚动处理 - 当滚动到评论区时显示悬浮输入框，移动端自动加载更多评论
const handleScroll = () => {
  const commentSection = document.querySelector('.comment-list')
  if (commentSection) {
    const rect = commentSection.getBoundingClientRect()
    // 当评论区进入视口且顶部评论输入框不在视口时显示悬浮框
    showStickyInput.value = rect.top < 0 && rect.bottom > 200
  }
  
  // 移动端自动加载更多评论
  if (isMobile.value) {
    const scrollTop = window.scrollY || document.documentElement.scrollTop
    const windowHeight = window.innerHeight
    const documentHeight = document.documentElement.scrollHeight
    
    if (scrollTop + windowHeight >= documentHeight - 100) {
      loadMoreComments()
    }
  }
}

onMounted(() => {
  // 重置页面滚动位置到顶部
  window.scrollTo(0, 0)
  checkMobile()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('scroll', handleScroll)
  loadData()
  checkLoginStatus()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('scroll', handleScroll)
})

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
  cursor: pointer;
  transition: color 0.2s;
}

.title:hover {
  color: #409eff;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.mobile-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 12px;
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

.dislike-count {
  color: #909399;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.dislike-icon {
  fill: currentColor;
}

.dislike-btn-icon {
  fill: currentColor;
}

.update-info {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
  display: flex;
  gap: 20px;
}

.word-count {
  color: #606266;
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

.add-tag-btn {
  font-weight: bold;
  font-size: 16px;
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

.comment-header-row .floor-number {
  color: #9499a0;
  font-size: 12px;
  background: #f4f5f7;
  padding: 2px 6px;
  border-radius: 4px;
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

.reply-content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
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

.reply-header-row .floor-number.reply-floor {
  color: #9499a0;
  font-size: 11px;
  background: #f4f5f7;
  padding: 1px 4px;
  border-radius: 3px;
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
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 12px;
  margin: 16px 0 16px 56px;
  padding: 16px;
  background: #f6f7f8;
  border-radius: 8px;
}

.reply-input-wrapper {
  flex: 1;
  min-width: 0;
}

.reply-input-section .reply-input-actions {
  width: 100%;
  margin-left: 44px;
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
  justify-content: flex-end;
}

/* 回复的回复输入框 */
.reply-to-reply-input {
  margin: 12px 0 0 0;
  padding: 12px;
  background: #f4f5f7;
  border-radius: 8px;
}

.reply-to-reply-input .reply-input-hint {
  font-size: 13px;
  color: #61666d;
  margin-bottom: 8px;
}

.reply-to-reply-input .reply-input-actions {
  justify-content: flex-end;
}

.reply-actions .reply-btn {
  color: #9499a0;
  font-size: 13px;
}

.reply-actions .reply-btn:hover {
  color: #00a1d6;
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

.category-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 0;
}

.login-dialog-content {
  text-align: center;
  padding: 20px 0;
}

.login-dialog-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.category-btn {
  min-width: 120px;
  padding: 12px 20px;
  height: auto;
  border-radius: 8px;
  transition: all 0.3s;
}

.category-btn .category-name {
  font-size: 14px;
  font-weight: 500;
}

.category-btn .category-count {
  font-size: 12px;
  margin-left: 6px;
  opacity: 0.8;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .novel-detail {
    padding: 0 12px;
  }

  .novel-info {
    flex-direction: column;
    gap: 16px;
  }

  .cover-section {
    display: flex;
    justify-content: center;
  }

  .image-placeholder {
    width: 120px;
    height: 160px;
  }

  .info-section {
    width: 100%;
  }

  .title {
    font-size: 18px;
    margin-bottom: 12px;
    text-align: center;
    cursor: pointer;
  }

  .meta-info {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  .meta-info .author-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .meta-info .author {
    margin-bottom: 0;
    text-align: center;
  }

  .meta-info .el-tag {
    margin-right: 0;
  }

  .meta-info .mobile-stats {
    display: flex;
    gap: 16px;
    align-items: center;
  }

  .meta-info .favorite-count,
  .meta-info .comment-count,
  .meta-info .dislike-count {
    margin-left: 0;
  }

  .update-info {
    text-align: center;
    font-size: 12px;
    justify-content: center;
    gap: 12px;
  }

  .word-count {
    font-size: 12px;
  }

  .tags-section {
    justify-content: center;
  }

  .description {
    margin-top: 12px;
  }

  .action-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .action-section .el-button {
    width: 100%;
    margin: 0;
  }

  /* 评论区移动端适配 */
  .comment-input-section {
    padding: 12px 0;
    gap: 8px;
  }

  .comment-main {
    gap: 12px;
  }

  .reply-list {
    margin-left: 0;
    padding: 8px 12px;
  }

  .reply-input-section {
    margin-left: 0;
    padding: 12px;
  }

  .reply-expand,
  .reply-pagination {
    margin-left: 0;
  }

  .comment-input-sticky {
    padding: 8px 12px;
  }

  .sticky-content {
    padding: 0;
  }

  /* 收藏夹选择器移动端适配 */
  .category-buttons {
    flex-wrap: wrap;
    gap: 8px;
    padding: 8px 0;
  }

  .category-btn {
    min-width: auto;
    padding: 8px 16px;
  }

  /* 修复内容溢出 */
  .novel-content {
    padding: 0 12px;
  }

  /* 修复评论内容溢出 */
  .comment-text {
    word-break: break-all;
  }

  .reply-text {
    word-break: break-all;
  }

  /* 修复标签区域溢出 */
  .tags-section {
    flex-wrap: wrap;
    overflow-x: visible;
  }

  /* 修复操作按钮溢出 */
  .action-section {
    padding: 0 12px;
  }

  /* 修复评论区溢出 */
  .comment-section {
    padding: 0 12px;
  }

  /* 修复回复输入框溢出 */
  .reply-input-section {
    margin-left: 0;
    margin-right: 0;
    flex-direction: column;
    gap: 8px;
    padding: 12px;
  }

  .reply-input-section .el-avatar {
    display: none;
  }

  .reply-input-wrapper {
    width: 100%;
  }

  .reply-input-section .reply-input-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
    margin-top: 8px;
  }

  /* 修复回复输入框中的登录提示 */
  .reply-input-section .reply-input-login {
    width: 100%;
    height: 44px;
    min-height: 44px;
    flex: none;
  }

  /* 修复悬浮评论框 */
  .comment-input-sticky {
    left: 0;
    right: 0;
  }

  .sticky-content {
    padding: 0 12px;
  }

  /* 移动端加载更多评论样式 */
  .loading-more-comments,
  .no-more-comments {
    text-align: center;
    padding: 16px;
    color: #909399;
    font-size: 14px;
  }

  .loading-more-comments {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .loading-more-comments .el-icon {
    animation: rotate 1s linear infinite;
  }

  @keyframes rotate {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
}
</style>
