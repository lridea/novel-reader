<template>
  <div class="novel-detail">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">小说详情</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { crawlerApi } from '../api'

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
  status: 1
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

const loadData = async () => {
  const novelId = route.params.id
  try {
    const data = await crawlerApi.getNovels({ page: 0, size: 1000 })
    if (data.content) {
      const found = data.content.find(n => n.id == novelId)
      if (found) {
        novel.value = found
      }
    }
  } catch (error) {
    console.error('加载小说详情失败:', error)
  }
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
