<template>
  <div class="novel-detail">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">小说详情</span>
      </template>
      <template #extra>
        <el-button
          v-if="novel.isFavorite !== null"
          :type="novel.isFavorite ? 'warning' : 'default'"
          :icon="novel.isFavorite ? StarFilled : Star"
          @click="toggleFavorite(novel)"
        >
          {{ novel.isFavorite ? '已收藏' : '收藏' }}
        </el-button>
      </template>
    </el-page-header>

    <el-card shadow="hover" style="margin-top: 20px;">
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
            <span style="margin-left: 20px;">更新时间：{{ novel.latestUpdateTime || '-' }}</span>
          </div>
          <div class="crawl-info">
            <span>抓取次数：{{ novel.crawlCount || 0 }} 次</span>
            <span style="margin-left: 20px;">最后抓取：{{ novel.lastCrawlTime || '-' }}</span>
          </div>
          <div class="description">
            <h4>简介</h4>
            <p>{{ novel.description || '暂无简介' }}</p>
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
          <span>评论 ({{ totalComments }})</span>
          <el-button type="primary" size="small" @click="showAddCommentDialog">
            发表评论
          </el-button>
        </div>
      </template>

      <!-- 评论列表 -->
      <div class="comment-list" v-loading="commentsLoading">
        <div class="comment-item" v-for="comment in topLevelComments" :key="comment.id">
          <!-- 评论内容 -->
          <div class="comment-main">
            <el-avatar :src="comment.user.avatar" :size="40" />
            <div class="comment-content">
              <div class="comment-info">
                <span class="username">{{ comment.user.nickname || comment.user.username }}</span>
                <span class="time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-actions">
                <el-button
                  type="default"
                  size="small"
                  @click="showReplyDialog(comment)"
                >
                  回复
                </el-button>
              </div>
            </div>
          </div>

          <!-- 回复列表 -->
          <div class="reply-list" v-if="comment.replyCount > 0">
            <div class="reply-item" v-for="reply in comment.replies" :key="reply.id">
              <el-avatar :src="reply.user.avatar" :size="32" />
              <div class="reply-content">
                <div class="reply-info">
                  <span class="username">{{ reply.user.nickname || reply.user.username }}</span>
                  <span class="reply-to">回复</span>
                  <span class="target-user">@{{ reply.parentUser.nickname || reply.parentUser.username }}</span>
                  <span class="time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <div class="reply-text">{{ reply.content }}</div>
                <div class="reply-actions">
                  <el-button
                    size="small"
                    @click="showReplyDialog(comment, reply)"
                  >
                    回复
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 展开更多回复 -->
            <el-button
              v-if="comment.replyCount > comment.replies.length"
              type="primary"
              link
              @click="loadMoreReplies(comment)"
            >
              展开更多回复 ({{ comment.replyCount - comment.replies.length }})
            </el-button>
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
import { Picture, Star, StarFilled, ChatDotRound } from '@element-plus/icons-vue'
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
const commentPage = ref(0)
const commentPageSize = ref(10)

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

const goBack = () => {
  router.push('/novels')
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
    const data = await crawlerApi.getNovels({ page: 0, size: 1000 })
    if (data.content) {
      const found = data.content.find(n => n.id == novelId)
      if (found) {
        novel.value = found
        // 加载评论
        loadComments()
      }
    }
  } catch (error) {
    console.error('加载小说详情失败:', error)
  }
}

// 加载评论
const loadComments = async () => {
  commentsLoading.value = true
  try {
    const response = await crawlerApi.getComments(novel.value.id, {
      page: commentPage.value,
      size: commentPageSize.value,
      floor: 1
    })
    if (response) {
      topLevelComments.value = response.content || []
      totalComments.value = response.totalElements || 0
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

// 加载更多回复
const loadMoreReplies = async (comment) => {
  try {
    const response = await crawlerApi.getComments(novel.value.id, {
      page: 0,
      size: 10,
      floor: 2,
      parentId: comment.id
    })
    if (response && response.content) {
      comment.replies = [...(comment.replies || []), ...response.content]
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

// 显示回复对话框
const showReplyDialog = (comment, reply = null) => {
  replyTarget.value = reply || comment
  replyDialogVisible.value = true
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.novel-detail {
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
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

.update-info,
.crawl-info {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.description {
  margin-top: 20px;
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

.ai-summary {
  line-height: 1.8;
  color: #606266;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
</style>

/* 评论区域样式 */
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-list {
  min-height: 200px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-info .username {
  color: #303133;
  font-weight: 500;
}

.comment-info .time {
  color: #909399;
  font-size: 12px;
}

.comment-text {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

/* 回复列表样式 */
.reply-list {
  margin-top: 12px;
  padding-left: 52px;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-content {
  flex: 1;
}

.reply-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.reply-info .username {
  color: #303133;
  font-weight: 500;
  font-size: 13px;
}

.reply-info .reply-to {
  color: #909399;
  font-size: 12px;
}

.reply-info .target-user {
  color: #409EFF;
  font-size: 13px;
}

.reply-info .time {
  color: #909399;
  font-size: 12px;
}

.reply-text {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 6px;
  font-size: 14px;
}

.reply-actions {
  display: flex;
  gap: 6px;
}
