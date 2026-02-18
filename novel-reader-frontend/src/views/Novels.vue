<template>
  <div class="novels">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据浏览</span>
          <div class="header-actions">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索书名/作者" 
              clearable
              style="width: 200px;"
              @keyup.enter="loadData"
            >
              <template #append>
                <el-button :icon="Search" @click="loadData" />
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <!-- 筛选条件区域 -->
      <div class="filter-section">
        <!-- 平台筛选 -->
        <div class="filter-row">
          <span class="filter-label">平台:</span>
          <div class="filter-buttons">
            <el-button
              v-for="platform in platformOptions"
              :key="platform.value"
              :type="filterPlatform === platform.value ? 'primary' : ''"
              :class="{ 'filter-button': true, 'active': filterPlatform === platform.value }"
              @click="filterPlatform = platform.value; loadData()"
              size="small"
            >
              {{ platform.label }}
            </el-button>
          </div>
        </div>

        <!-- 状态筛选 -->
        <div class="filter-row">
          <span class="filter-label">状态:</span>
          <div class="filter-buttons">
            <el-button
              v-for="status in statusOptions"
              :key="status.value"
              :type="filterStatus === status.value ? 'primary' : ''"
              :class="{ 'filter-button': true, 'active': filterStatus === status.value }"
              @click="filterStatus = status.value; loadData()"
              size="small"
            >
              {{ status.label }}
            </el-button>
          </div>
        </div>

        <!-- 字数筛选 -->
        <div class="filter-row">
          <span class="filter-label">字数:</span>
          <div class="filter-buttons word-count-filters">
            <div class="word-count-group">
              <span class="word-count-label">最小:</span>
              <el-button
                v-for="wc in wordCountOptions"
                :key="wc.value"
                :type="wordCountMin === wc.value ? 'primary' : ''"
                :class="{ 'filter-button': true, 'active': wordCountMin === wc.value }"
                @click="wordCountMin = wc.value; loadData()"
                size="small"
              >
                {{ wc.label }}
              </el-button>
            </div>
            <div class="word-count-group">
              <span class="word-count-label">最大:</span>
              <el-button
                v-for="wc in wordCountOptions"
                :key="wc.value"
                :type="wordCountMax === wc.value ? 'primary' : ''"
                :class="{ 'filter-button': true, 'active': wordCountMax === wc.value }"
                @click="wordCountMax = wc.value; loadData()"
                size="small"
              >
                {{ wc.label }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 排序方式 -->
        <div class="filter-row">
          <span class="filter-label">排序:</span>
          <div class="filter-buttons">
            <el-button
              v-for="sort in sortOptions"
              :key="`${sort.sortBy}-${sort.sortOrder}`"
              :type="sortBy === sort.sortBy && sortOrder === sort.sortOrder ? 'primary' : ''"
              :class="{ 'filter-button': true, 'active': sortBy === sort.sortBy && sortOrder === sort.sortOrder }"
              @click="sortBy = sort.sortBy; sortOrder = sort.sortOrder; loadData()"
              size="small"
            >
              {{ sort.label }}
            </el-button>
          </div>
        </div>

        <!-- 标签筛选 -->
        <div class="filter-row">
          <span class="filter-label">标签:</span>
          <div class="filter-buttons tags-container">
            <el-button
              v-for="tag in tags"
              :key="tag.name"
              :type="filterTag === tag.name ? 'primary' : ''"
              :class="{ 'filter-button': true, 'active': filterTag === tag.name }"
              @click="filterTag = tag.name; loadData()"
              size="small"
            >
              {{ tag.name }}({{ tag.count }})
            </el-button>
            <el-button
              v-if="tagsLoading"
              :loading="true"
              size="small"
            >
              加载中...
            </el-button>
          </div>
        </div>

        <!-- 收藏数筛选 -->
        <div class="filter-row">
          <span class="filter-label">收藏数:</span>
          <div class="filter-buttons">
            <el-button
              v-for="fc in favoriteCountOptions"
              :key="fc.value"
              :type="favoriteCountMin === fc.value ? 'primary' : ''"
              :class="{ 'filter-button': true, 'active': favoriteCountMin === fc.value }"
              @click="favoriteCountMin = fc.value; loadData()"
              size="small"
            >
              {{ fc.label }}
            </el-button>
          </div>
        </div>
      </div>

      <el-table :data="novels" style="width: 100%" v-loading="loading">
        <el-table-column prop="coverUrl" label="封面" width="80">
          <template #default="{ row }">
            <el-image 
              :src="row.coverUrl || '/placeholder.png'" 
              style="width: 50px; height: 70px;"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getPlatformName(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="latestChapterTitle" label="最新章节" min-width="150" show-overflow-tooltip />
        <el-table-column prop="latestUpdateTime" label="更新时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="favoriteCount" label="收藏数" width="100">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: 500;">
              <el-icon><Star /></el-icon>
              {{ row.favoriteCount || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="commentCount" label="评论数" width="100">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: 500;">
              <el-icon><ChatDotRound /></el-icon>
              {{ row.commentCount || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isFavorite !== null"
              :type="row.isFavorite ? 'warning' : 'default'"
              :icon="row.isFavorite ? StarFilled : Star"
              size="small"
              @click="toggleFavorite(row)"
            >
              {{ row.isFavorite ? '已收藏' : '收藏' }}
            </el-button>
            <el-button type="primary" size="small" @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Picture, Star, StarFilled } from '@element-plus/icons-vue'
import { crawlerApi, favoriteApi } from '../api'

const router = useRouter()
const loading = ref(false)
const novels = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选条件
const filterPlatform = ref('')
const searchKeyword = ref('')
const filterStatus = ref('')
const filterTag = ref('')
const wordCountMin = ref('')
const wordCountMax = ref('')
const favoriteCountMin = ref('')
const sortBy = ref('updateTime')
const sortOrder = ref('desc')

// 标签数据
const tags = ref([])
const tagsLoading = ref(false)

// 选项配置
const platformOptions = [
  { label: '全部', value: '' },
  { label: '刺猬猫', value: 'ciweimao' },
  { label: '起点', value: 'qidian' },
  { label: 'SF轻小说', value: 'sf' },
  { label: '次元姬', value: 'ciyuanji' }
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '连载中', value: 1 },
  { label: '已完结', value: 2 },
  { label: '停更', value: 0 }
]

const wordCountOptions = [
  { label: '全部', value: '' },
  { label: '10w', value: '10w' },
  { label: '30w', value: '30w' },
  { label: '50w', value: '50w' },
  { label: '100w', value: '100w' },
  { label: '200w', value: '200w' }
]

const favoriteCountOptions = [
  { label: '全部', value: '' },
  { label: '10+', value: 10 },
  { label: '50+', value: 50 },
  { label: '100+', value: 100 },
  { label: '500+', value: 500 },
  { label: '1000+', value: 1000 }
]

const sortOptions = [
  { label: '更新时间↓', value: 'updateTime-desc', sortBy: 'updateTime', sortOrder: 'desc' },
  { label: '更新时间↑', value: 'updateTime-asc', sortBy: 'updateTime', sortOrder: 'asc' },
  { label: '字数↓', value: 'wordCount-desc', sortBy: 'wordCount', sortOrder: 'desc' },
  { label: '字数↑', value: 'wordCount-asc', sortBy: 'wordCount', sortOrder: 'asc' },
  { label: '收藏数↓', value: 'favoriteCount-desc', sortBy: 'favoriteCount', sortOrder: 'desc' },
  { label: '收藏数↑', value: 'favoriteCount-asc', sortBy: 'favoriteCount', sortOrder: 'asc' }
]

// 加载标签列表
const loadTags = async () => {
  tagsLoading.value = true
  try {
    const response = await crawlerApi.getTags()
    if (response && response.success && response.tags) {
      tags.value = response.tags.map(tag => ({
        name: tag.tag_name,
        count: tag.tag_count
      }))
    }
  } catch (error) {
    console.error('加载标签列表失败:', error)
  } finally {
    tagsLoading.value = false
  }
}

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

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    // 添加筛选参数
    if (filterPlatform.value) {
      params.platform = filterPlatform.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    if (filterTag.value) {
      params.tag = filterTag.value
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
    if (sortBy.value) {
      params.sortBy = sortBy.value
    }
    if (sortOrder.value) {
      params.sortOrder = sortOrder.value
    }
    
    const data = await crawlerApi.getNovels(params)
    
    if (data.content) {
      novels.value = data.content
      total.value = data.totalElements
    }
  } catch (error) {
    console.error('加载小说列表失败:', error)
  } finally {
    loading.value = false
  }
  
  // 批量查询收藏状态
  await loadFavoriteStatusBatch()
}

const viewDetail = (row) => {
  router.push(`/novels/${row.id}`)
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

const loadFavoriteStatusBatch = async () => {
  if (novels.value.length === 0) {
    return
  }
  
  const novelIds = novels.value.map(n => n.id).join(',')
  try {
    const response = await favoriteApi.checkBatchFavorites(novelIds)
    if (response && response.success && response.favorites) {
      novels.value.forEach(novel => {
        novel.isFavorite = response.favorites[novel.id] || false
      })
    }
  } catch (error) {
    console.error('批量查询收藏状态失败:', error)
    novels.value.forEach(novel => {
      novel.isFavorite = false
    })
  }
}

onMounted(() => {
  loadTags()
  loadData()
})
</script>

<style scoped>
.novels {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.image-placeholder {
  width: 50px;
  height: 70px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

/* 筛选区域样式 */
.filter-section {
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.filter-label {
  width: 60px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.filter-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-button {
  border: 1px solid #DCDFE6;
  background-color: #F5F7FA;
  color: #606266;
  transition: all 0.3s ease;
}

.filter-button:hover {
  background-color: #ECF5FF;
  border-color: #409EFF;
  color: #409EFF;
}

.filter-button.active {
  background-color: #409EFF;
  border-color: #409EFF;
  color: #FFFFFF;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
}

/* 字数筛选特殊样式 */
.word-count-filters {
  display: flex;
  gap: 20px;
}

.word-count-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.word-count-label {
  font-size: 12px;
  color: #909399;
}

/* 标签横向滚动 */
.tags-container {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 4px 0;
  max-width: 100%;
  flex-wrap: nowrap;
}

.tags-container::-webkit-scrollbar {
  height: 4px;
}

.tags-container::-webkit-scrollbar-thumb {
  background: #DCDFE6;
  border-radius: 2px;
}

.tags-container::-webkit-scrollbar-thumb:hover {
  background: #C0C4CC;
}

.tags-container::-webkit-scrollbar-track {
  background: #F5F7FA;
}
</style>
