<template>
  <div class="home">
    <div class="container">
      <div class="filter-section">
        <div class="filter-row">
          <span class="filter-label">平台:</span>
          <div class="filter-buttons">
            <el-button
              :type="!selectedPlatform ? 'primary' : ''"
              @click="selectedPlatform = ''; loadData()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="platform in platformOptions"
              :key="platform.value"
              :type="selectedPlatform === platform.value ? 'primary' : ''"
              @click="selectedPlatform = platform.value; loadData()"
              size="small"
            >
              {{ platform.label }}
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">状态:</span>
          <div class="filter-buttons">
            <el-button
              :type="selectedStatus === '' ? 'primary' : ''"
              @click="selectedStatus = ''; loadData()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="status in statusOptions"
              :key="status.value"
              :type="selectedStatus === status.value ? 'primary' : ''"
              @click="selectedStatus = status.value; loadData()"
              size="small"
            >
              {{ status.label }}
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">字数以下:</span>
          <div class="filter-buttons">
            <el-button
              :type="!wordCountMax ? 'primary' : ''"
              @click="wordCountMax = ''; loadData()"
              size="small"
            >
              不限
            </el-button>
            <el-button
              :type="wordCountMax === '100000' ? 'primary' : ''"
              @click="wordCountMax = '100000'; loadData()"
              size="small"
            >
              10万以下
            </el-button>
            <el-button
              :type="wordCountMax === '300000' ? 'primary' : ''"
              @click="wordCountMax = '300000'; loadData()"
              size="small"
            >
              30万以下
            </el-button>
            <el-button
              :type="wordCountMax === '500000' ? 'primary' : ''"
              @click="wordCountMax = '500000'; loadData()"
              size="small"
            >
              50万以下
            </el-button>
            <el-button
              :type="wordCountMax === '1000000' ? 'primary' : ''"
              @click="wordCountMax = '1000000'; loadData()"
              size="small"
            >
              100万以下
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">字数以上:</span>
          <div class="filter-buttons">
            <el-button
              :type="!wordCountMin ? 'primary' : ''"
              @click="wordCountMin = ''; loadData()"
              size="small"
            >
              不限
            </el-button>
            <el-button
              :type="wordCountMin === '100000' ? 'primary' : ''"
              @click="wordCountMin = '100000'; loadData()"
              size="small"
            >
              10万以上
            </el-button>
            <el-button
              :type="wordCountMin === '300000' ? 'primary' : ''"
              @click="wordCountMin = '300000'; loadData()"
              size="small"
            >
              30万以上
            </el-button>
            <el-button
              :type="wordCountMin === '500000' ? 'primary' : ''"
              @click="wordCountMin = '500000'; loadData()"
              size="small"
            >
              50万以上
            </el-button>
            <el-button
              :type="wordCountMin === '1000000' ? 'primary' : ''"
              @click="wordCountMin = '1000000'; loadData()"
              size="small"
            >
              100万以上
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">收藏:</span>
          <div class="filter-buttons">
            <el-button
              :type="!favoriteCountMin ? 'primary' : ''"
              @click="favoriteCountMin = ''; loadData()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="fc in favoriteCountOptions"
              :key="fc.value"
              :type="favoriteCountMin === fc.value ? 'primary' : ''"
              @click="favoriteCountMin = fc.value; loadData()"
              size="small"
            >
              {{ fc.label }}
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">标签:</span>
          <div class="filter-buttons tag-buttons">
            <el-button
              :type="!selectedTag ? 'primary' : ''"
              @click="selectedTag = ''; loadData()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="tag in tags"
              :key="tag"
              :type="selectedTag === tag ? 'primary' : ''"
              @click="selectedTag = tag; loadData()"
              size="small"
            >
              {{ tag }}
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">排序:</span>
          <div class="filter-buttons">
            <el-button
              :type="sortBy === 'updateTime' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'updateTime'; sortOrder = 'desc'; loadData()"
              size="small"
            >
              最新更新
            </el-button>
            <el-button
              :type="sortBy === 'createdAt' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'createdAt'; sortOrder = 'desc'; loadData()"
              size="small"
            >
              最新入库
            </el-button>
            <el-button
              :type="sortBy === 'wordCount' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'wordCount'; sortOrder = 'desc'; loadData()"
              size="small"
            >
              字数最多
            </el-button>
            <el-button
              :type="sortBy === 'favoriteCount' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'favoriteCount'; sortOrder = 'desc'; loadData()"
              size="small"
            >
              收藏最多
            </el-button>
          </div>
        </div>
      </div>

      <div v-loading="loading" class="novel-grid">
        <div
          v-for="novel in novels"
          :key="novel.id"
          class="novel-card"
          @click="goDetail(novel.id)"
        >
          <div class="cover-wrapper">
            <img :src="novel.coverUrl || defaultCover" :alt="novel.title" class="cover" />
            <div class="platform-tag" :class="novel.platform">
              {{ getPlatformName(novel.platform) }}
            </div>
          </div>
          <div class="info">
            <h3 class="title">{{ novel.title }}</h3>
            <p class="author">{{ novel.author }}</p>
            <div class="tags">
              <el-tag v-for="tag in parseTags(novel.tags)" :key="tag" size="small" type="warning">
                {{ tag }}
              </el-tag>
            </div>
            <p class="desc">{{ novel.description?.slice(0, 80) }}...</p>
            <div class="meta">
              <span>{{ formatWordCount(novel.wordCount) }}</span>
              <span>{{ formatDate(novel.latestUpdateTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 40, 60, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>

      <el-empty v-if="!loading && novels.length === 0" description="暂无数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { crawlerApi } from '../api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const novels = ref([])
const tags = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedPlatform = ref('')
const selectedTag = ref('')
const selectedStatus = ref('')
const wordCountMin = ref('')
const wordCountMax = ref('')
const favoriteCountMin = ref('')
const sortBy = ref('updateTime')
const sortOrder = ref('desc')
const keyword = ref('')

const defaultCover = 'https://via.placeholder.com/150x200?text=No+Cover'

const platformOptions = [
  { label: '刺猬猫', value: 'ciweimao' },
  { label: 'SF轻小说', value: 'sf' },
  { label: '次元姬', value: 'ciyuanji' }
]

const statusOptions = [
  { label: '连载中', value: 1 },
  { label: '已完结', value: 2 },
  { label: '停更', value: 0 }
]

const favoriteCountOptions = [
  { label: '10+', value: 10 },
  { label: '50+', value: 50 },
  { label: '100+', value: 100 },
  { label: '500+', value: 500 }
]

const getPlatformName = (platform) => {
  const names = {
    ciweimao: '刺猬猫',
    sf: 'SF',
    ciyuanji: '次元姬',
    qidian: '起点'
  }
  return names[platform] || platform
}

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  try {
    const parsed = JSON.parse(tagsStr)
    return Array.isArray(parsed) ? parsed.slice(0, 3) : []
  } catch {
    return tagsStr.split(',').slice(0, 3)
  }
}

const formatWordCount = (count) => {
  if (!count) return '0字'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万字'
  }
  return count + '字'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}-${date.getDate()}`
}

const goDetail = (id) => {
  router.push(`/novel/${id}`)
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    }

    if (selectedPlatform.value) {
      params.platform = selectedPlatform.value
    }
    if (selectedTag.value) {
      params.tag = selectedTag.value
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    if (selectedStatus.value !== '') {
      params.status = selectedStatus.value
    }
    if (wordCountMin.value) {
      params.wordCountMin = wordCountMin.value
    }
    if (wordCountMax.value) {
      params.wordCountMax = wordCountMax.value
    }
    if (favoriteCountMin.value) {
      params.favoriteCountMin = favoriteCountMin.value
    }

    const response = await crawlerApi.getNovelsPublic(params)
    novels.value = response.content || []
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const response = await crawlerApi.getTagsPublic()
    tags.value = (response || []).map(t => t.tag_name).filter(Boolean).slice(0, 15)
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

watch(() => route.query.keyword, (newKeyword) => {
  keyword.value = newKeyword || ''
  currentPage.value = 1
  loadData()
})

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
  }
  loadData()
  loadTags()
})
</script>

<style scoped>
.home {
  padding: 20px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.filter-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  width: 70px;
  font-size: 14px;
  color: #606266;
  padding-top: 6px;
  flex-shrink: 0;
}

.filter-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-buttons {
  max-height: 100px;
  overflow-y: auto;
}

.novel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.novel-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.novel-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.cover-wrapper {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.platform-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.platform-tag.ciweimao {
  background: #409eff;
}

.platform-tag.sf {
  background: #67c23a;
}

.platform-tag.ciyuanji {
  background: #e6a23c;
}

.platform-tag.qidian {
  background: #f56c6c;
}

.info {
  padding: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author {
  font-size: 13px;
  color: #909399;
  margin: 0 0 8px 0;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin: 0 0 8px 0;
  height: 40px;
  overflow: hidden;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
