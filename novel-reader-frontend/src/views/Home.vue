<template>
  <div class="home">
    <div class="container">
      <div class="filter-section">
        <div class="filter-row">
          <span class="filter-label">平台:</span>
          <div class="filter-buttons">
            <el-button
              :type="selectedPlatforms.length === 0 ? 'primary' : ''"
              @click="selectedPlatforms = []; filterAndLoad()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="platform in platformOptions"
              :key="platform.value"
              :type="selectedPlatforms.includes(platform.value) ? 'primary' : ''"
              @click="togglePlatform(platform.value)"
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
              @click="selectedStatus = ''; filterAndLoad()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="status in statusOptions"
              :key="status.value"
              :type="selectedStatus === status.value ? 'primary' : ''"
              @click="selectedStatus = status.value; filterAndLoad()"
              size="small"
            >
              {{ status.label }}
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">字数:</span>
          <div class="filter-buttons">
            <el-button
              :type="!wordCountMax ? 'primary' : ''"
              @click="wordCountMax = ''; filterAndLoad()"
              size="small"
            >
              不限
            </el-button>
            <el-button
              :type="wordCountMax === '100000' ? 'primary' : ''"
              @click="wordCountMax = '100000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="10w"
            >
              <span class="pc-text">10万以下</span>
              <span class="mobile-text">10w-</span>
            </el-button>
            <el-button
              :type="wordCountMax === '300000' ? 'primary' : ''"
              @click="wordCountMax = '300000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="30w"
            >
              <span class="pc-text">30万以下</span>
              <span class="mobile-text">30w-</span>
            </el-button>
            <el-button
              :type="wordCountMax === '500000' ? 'primary' : ''"
              @click="wordCountMax = '500000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="50w"
            >
              <span class="pc-text">50万以下</span>
              <span class="mobile-text">50w-</span>
            </el-button>
            <el-button
              :type="wordCountMax === '1000000' ? 'primary' : ''"
              @click="wordCountMax = '1000000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="100w"
            >
              <span class="pc-text">100万以下</span>
              <span class="mobile-text">100w-</span>
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">字数:</span>
          <div class="filter-buttons">
            <el-button
              :type="!wordCountMin ? 'primary' : ''"
              @click="wordCountMin = ''; filterAndLoad()"
              size="small"
            >
              不限
            </el-button>
            <el-button
              :type="wordCountMin === '100000' ? 'primary' : ''"
              @click="wordCountMin = '100000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="10w+"
            >
              <span class="pc-text">10万以上</span>
              <span class="mobile-text">10w+</span>
            </el-button>
            <el-button
              :type="wordCountMin === '300000' ? 'primary' : ''"
              @click="wordCountMin = '300000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="30w+"
            >
              <span class="pc-text">30万以上</span>
              <span class="mobile-text">30w+</span>
            </el-button>
            <el-button
              :type="wordCountMin === '500000' ? 'primary' : ''"
              @click="wordCountMin = '500000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="50w+"
            >
              <span class="pc-text">50万以上</span>
              <span class="mobile-text">50w+</span>
            </el-button>
            <el-button
              :type="wordCountMin === '1000000' ? 'primary' : ''"
              @click="wordCountMin = '1000000'; filterAndLoad()"
              size="small"
              class="word-count-btn"
              data-value="100w+"
            >
              <span class="pc-text">100万以上</span>
              <span class="mobile-text">100w+</span>
            </el-button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">收藏:</span>
          <div class="filter-buttons">
            <el-button
              :type="!favoriteCountMin ? 'primary' : ''"
              @click="favoriteCountMin = ''; filterAndLoad()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="fc in favoriteCountOptions"
              :key="fc.value"
              :type="favoriteCountMin === fc.value ? 'primary' : ''"
              @click="favoriteCountMin = fc.value; filterAndLoad()"
              size="small"
            >
              {{ fc.label }}
            </el-button>
          </div>
        </div>

        <div class="filter-row" :class="{ 'tag-expanded-row': showAllTags && isMobile }">
          <span class="filter-label">标签:</span>
          <div class="filter-buttons tag-filter-wrapper" :class="{ 'tag-expanded': showAllTags && isMobile }">
            <el-button
              :type="!selectedTag ? 'primary' : ''"
              @click="selectedTag = ''; filterAndLoad()"
              size="small"
            >
              全部
            </el-button>
            <el-button
              v-for="tag in displayedTags"
              :key="tag"
              :type="selectedTag === tag ? 'primary' : ''"
              @click="selectedTag = tag; filterAndLoad()"
              size="small"
            >
              {{ tag }}
            </el-button>
            <span
              v-if="(isMobile && tags.length > 3) || (!isMobile && tags.length > 5)"
              class="expand-toggle"
              @click="showAllTags = !showAllTags"
            >
              {{ showAllTags ? '收起' : '展开' }}
              <el-icon><arrow-down v-if="!showAllTags" /><arrow-up v-else /></el-icon>
            </span>
          </div>
        </div>
        <div class="filter-row tag-search-row">
          <span class="filter-label"></span>
          <div class="tag-search-wrapper">
            <el-input
              v-model="tagSearchKeyword"
              placeholder="搜索标签"
              size="small"
              clearable
              @input="filterTags"
            >
              <template #prefix>
                <el-icon><search /></el-icon>
              </template>
            </el-input>
            <div v-if="filteredTags.length > 0" class="tag-search-results">
              <el-button
                v-for="tag in filteredTags"
                :key="tag"
                :type="selectedTag === tag ? 'primary' : ''"
                @click="selectedTag = tag; filterAndLoad()"
                size="small"
              >
                {{ tag }}
              </el-button>
            </div>
            <div v-else-if="tagSearchKeyword" class="tag-search-empty">
              未找到匹配的标签
            </div>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-label">排序:</span>
          <div class="filter-buttons">
            <el-button
              :type="sortBy === 'updateTime' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'updateTime'; sortOrder = 'desc'; filterAndLoad()"
              size="small"
            >
              最新更新
            </el-button>
            <el-button
              :type="sortBy === 'createdAt' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'createdAt'; sortOrder = 'desc'; filterAndLoad()"
              size="small"
            >
              最新入库
            </el-button>
            <el-button
              :type="sortBy === 'wordCount' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'wordCount'; sortOrder = 'desc'; filterAndLoad()"
              size="small"
            >
              字数最多
            </el-button>
            <el-button
              :type="sortBy === 'favoriteCount' && sortOrder === 'desc' ? 'primary' : ''"
              @click="sortBy = 'favoriteCount'; sortOrder = 'desc'; filterAndLoad()"
              size="small"
            >
              收藏最多
            </el-button>
          </div>
        </div>

        <!-- PC端重置按钮 -->
        <div class="filter-row reset-row pc-reset-row">
          <span class="filter-label"></span>
          <div class="filter-buttons">
            <el-button type="info" @click="resetFilters" size="small">
              重置
            </el-button>
          </div>
        </div>

        <!-- 移动端搜索和重置 -->
        <div class="filter-row reset-row mobile-search-row">
          <div class="mobile-search-wrapper">
            <el-input
              v-model="keyword"
              placeholder="搜索书名/作者"
              size="small"
              clearable
              @keyup.enter="handleKeywordSearch"
              class="mobile-search-input"
            />
            <el-button :icon="Search" @click="handleKeywordSearch" size="small" class="mobile-search-btn" />
            <el-button type="info" @click="resetFilters" size="small">
              重置
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
            <div class="tags" ref="tagScrollContainer">
              <div class="tags-scroll" :data-novel-id="novel.id">
                <!-- 爬取的标签 -->
                <el-tag 
                  v-for="tag in parseTags(novel.tags)" 
                  :key="'crawl-' + tag" 
                  size="small" 
                  type="warning"
                >
                  {{ tag }}
                </el-tag>
                <!-- 用户申请的标签 -->
                <el-tag 
                  v-for="tag in parseTags(novel.userTags)" 
                  :key="'user-' + tag" 
                  size="small" 
                  type="danger"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
            <p class="desc">{{ novel.description }}</p>
            <div class="meta">
              <span>{{ formatWordCount(novel.wordCount) }}</span>
              <span>{{ formatDateTime(novel.latestUpdateTime) }}</span>
            </div>
            <div class="stats">
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ novel.favoriteCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ novel.commentCount || 0 }}
              </span>
              <span class="stat-item">
                <svg class="dislike-icon" viewBox="0 0 24 24" width="14" height="14" style="transform: rotate(180deg) scaleX(-1);">
                  <path d="M9 21h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.58 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2zM9 9l4.34-4.34L12 10h9v2l-3 7H9V9zM1 9h4v12H1V9z"/>
                </svg>
                {{ novel.dislikeCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- PC端分页 -->
      <div v-if="total > 0 && !isMobile" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 40, 60, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handlePageChange"
        />
      </div>

      <!-- 移动端加载状态 -->
      <div v-if="isMobile && novels.length > 0 && loading" class="loading-more">
        <el-icon class="is-loading"><loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-if="isMobile && novels.length > 0 && !loading && novels.length >= total" class="no-more">
        没有更多了
      </div>

      <el-empty v-if="!loading && novels.length === 0" description="暂无数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { crawlerApi } from '../api'
import { ArrowDown, ArrowUp, Search, Star, ChatDotRound, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const novels = ref([])
const tags = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedPlatforms = ref([])
const selectedTag = ref('')
const selectedStatus = ref('')
const wordCountMin = ref('')
const wordCountMax = ref('')
const favoriteCountMin = ref('')
const sortBy = ref('updateTime')
const sortOrder = ref('desc')
const keyword = ref('')

const defaultCover = 'https://via.placeholder.com/150x200?text=No+Cover'

// 移动端检测
const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 保存滚动位置和分页数据
const saveScrollPosition = () => {
  sessionStorage.setItem('homeScrollPosition', window.scrollY.toString())
  sessionStorage.setItem('homeCurrentPage', currentPage.value.toString())
  sessionStorage.setItem('homeTotal', total.value.toString())
  
  // 保存筛选条件
  sessionStorage.setItem('homeFilters', JSON.stringify({
    selectedPlatforms: selectedPlatforms.value,
    selectedTag: selectedTag.value,
    selectedStatus: selectedStatus.value,
    wordCountMin: wordCountMin.value,
    wordCountMax: wordCountMax.value,
    favoriteCountMin: favoriteCountMin.value,
    sortBy: sortBy.value,
    sortOrder: sortOrder.value,
    keyword: keyword.value
  }))
  
  // 移动端额外保存已加载的数据
  if (isMobile.value) {
    sessionStorage.setItem('homeNovelsData', JSON.stringify(novels.value))
  }
}

// 恢复滚动位置和分页数据
const restoreScrollPosition = async () => {
  const savedPosition = sessionStorage.getItem('homeScrollPosition')
  const savedPage = sessionStorage.getItem('homeCurrentPage')
  const savedTotal = sessionStorage.getItem('homeTotal')
  const savedNovels = sessionStorage.getItem('homeNovelsData')
  const savedFilters = sessionStorage.getItem('homeFilters')
  
  // 恢复筛选条件
  if (savedFilters) {
    const filters = JSON.parse(savedFilters)
    selectedPlatforms.value = filters.selectedPlatforms || []
    selectedTag.value = filters.selectedTag || ''
    selectedStatus.value = filters.selectedStatus || ''
    wordCountMin.value = filters.wordCountMin || ''
    wordCountMax.value = filters.wordCountMax || ''
    favoriteCountMin.value = filters.favoriteCountMin || ''
    sortBy.value = filters.sortBy || 'updateTime'
    sortOrder.value = filters.sortOrder || 'desc'
    keyword.value = filters.keyword || ''
  }
  
  if (savedPage) {
    currentPage.value = parseInt(savedPage)
  }
  
  if (savedTotal) {
    total.value = parseInt(savedTotal)
  }
  
  // 移动端直接恢复缓存数据，PC端重新加载
  if (isMobile.value && savedNovels) {
    novels.value = JSON.parse(savedNovels)
  } else if (savedPage) {
    // PC端重新加载对应页的数据
    await loadData(false)
  }
  
  if (savedPosition) {
    setTimeout(() => {
      window.scrollTo(0, parseInt(savedPosition))
    }, 100)
  }
  
  // 清理缓存
  sessionStorage.removeItem('homeScrollPosition')
  sessionStorage.removeItem('homeCurrentPage')
  sessionStorage.removeItem('homeNovelsData')
  sessionStorage.removeItem('homeTotal')
  sessionStorage.removeItem('homeFilters')
}

// 标签展开/收起状态
const showAllTags = ref(false)
const tagSearchKeyword = ref('')
const filteredTags = ref([])

// 计算显示的标签（PC端默认显示前5个，移动端默认显示前2个，展开后显示全部）
const displayedTags = computed(() => {
  if (showAllTags.value) {
    return tags.value
  }
  const defaultCount = isMobile.value ? 2 : 5
  return tags.value.slice(0, defaultCount)
})

// 搜索标签
const filterTags = () => {
  const keyword = tagSearchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    filteredTags.value = []
    return
  }
  filteredTags.value = tags.value.filter(tag => 
    tag.toLowerCase().includes(keyword)
  )
}

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
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return tagsStr.split(',')
  }
}

// 判断是否为用户的标签
const isUserTag = (novel, tag) => {
  // novel.userTags 是 JSON 字符串，需要解析
  const userTagList = parseTags(novel.userTags)
  return userTagList.includes(tag)
}

const formatWordCount = (count) => {
  if (!count) return '0字'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万字'
  }
  return count + '字'
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

const goDetail = (id) => {
  // 保存当前滚动位置
  saveScrollPosition()
  router.push(`/novel/${id}`)
}

const loadData = async (append = false) => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    }

    if (selectedPlatforms.value && selectedPlatforms.value.length > 0) {
      params.platforms = selectedPlatforms.value.join(',')
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
    if (append) {
      novels.value = [...novels.value, ...(response.content || [])]
    } else {
      novels.value = response.content || []
    }
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// PC端分页处理
const handlePageChange = () => {
  loadData(false)
}

// 移动端加载更多
const loadMore = () => {
  if (loading.value) return
  if (novels.value.length < total.value) {
    currentPage.value++
    loadData(true)
  }
}

// 滚动监听
const handleScroll = () => {
  if (!isMobile.value) return
  
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 距离底部 100px 时触发加载
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadMore()
  }
}

// 筛选时重置并加载数据
const filterAndLoad = () => {
  // 清除缓存数据
  sessionStorage.removeItem('homeScrollPosition')
  sessionStorage.removeItem('homeCurrentPage')
  sessionStorage.removeItem('homeNovelsData')
  sessionStorage.removeItem('homeTotal')
  
  // 从 sessionStorage 读取最新的搜索关键字
  const savedKeyword = sessionStorage.getItem('searchKeyword')
  if (savedKeyword !== null) {
    keyword.value = savedKeyword
    sessionStorage.removeItem('searchKeyword')
  }
  
  currentPage.value = 1
  loadData(false)
}

// 切换平台选择（多选）
const togglePlatform = (platform) => {
  const index = selectedPlatforms.value.indexOf(platform)
  if (index > -1) {
    selectedPlatforms.value.splice(index, 1)
  } else {
    selectedPlatforms.value.push(platform)
  }
  filterAndLoad()
}

// 处理移动端搜索
const handleKeywordSearch = () => {
  filterAndLoad()
}

// 重置所有筛选条件
const resetFilters = () => {
  selectedPlatforms.value = []
  selectedStatus.value = ''
  selectedTag.value = ''
  wordCountMin.value = ''
  wordCountMax.value = ''
  favoriteCountMin.value = ''
  sortBy.value = 'updateTime'
  sortOrder.value = 'desc'
  keyword.value = ''
  
  // 清除搜索框的 sessionStorage
  sessionStorage.removeItem('searchKeyword')
  
  // 清除 URL 中的 keyword 参数并通知搜索框清空
  if (route.query.keyword) {
    router.replace({ path: '/', query: {} })
  }
  window.dispatchEvent(new CustomEvent('clear-search-keyword'))
  
  filterAndLoad()
}

const loadTags = async () => {
  try {
    const response = await crawlerApi.getTagsPublic()
    tags.value = (response.tags || []).map(t => t.name).filter(Boolean)
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

let isInitialized = false

watch(() => route.query.keyword, (newKeyword, oldKeyword) => {
  keyword.value = newKeyword || ''
  if (isInitialized) {
    filterAndLoad()
  }
})

onMounted(async () => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
  }
  checkMobile()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('scroll', handleScroll)
  
  // 检查是否有缓存的分页数据
  const savedPage = sessionStorage.getItem('homeCurrentPage')
  
  if (savedPage) {
    // 有缓存数据，恢复状态
    await restoreScrollPosition()
  } else {
    // 无缓存数据，正常加载
    await loadData(false)
  }
  
  loadTags()
  startTagAutoScroll()
  isInitialized = true
})

onUnmounted(() => {
  // 组件卸载时的清理工作
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('scroll', handleScroll)
})

// 存储已停止滚动的小说ID
const stoppedNovelIds = new Set()

// 标签自动滚动
const startTagAutoScroll = () => {
  setInterval(() => {
    const scrollContainers = document.querySelectorAll('.tags-scroll')
    scrollContainers.forEach(container => {
      const novelId = container.dataset.novelId
      
      // 如果已经停止，不再处理
      if (stoppedNovelIds.has(novelId)) {
        return
      }
      
      if (container.scrollWidth > container.clientWidth) {
        // 需要滚动
        const maxScrollLeft = container.scrollWidth - container.clientWidth
        
        // 检查是否已到达底部（允许5px误差）
        if (container.scrollLeft >= maxScrollLeft - 5) {
          // 已到达底部，标记为停止
          stoppedNovelIds.add(novelId)
          return
        }
        
        // 继续滚动
        container.scrollLeft += 1.2
      }
    })
  }, 60)
}
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

.reset-row {
  justify-content: flex-end;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

/* PC端隐藏移动端搜索框，显示PC端重置按钮 */
.mobile-search-row {
  display: none;
}

.pc-reset-row {
  display: flex;
}

.filter-label {
  width: 45px;
  font-size: 14px;
  color: #606266;
  padding-top: 2px;
  flex-shrink: 0;
}

.filter-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-filter-wrapper {
  flex-wrap: wrap;
  gap: 8px;
}

.expand-toggle {
  color: #409eff;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.expand-toggle:hover {
  background-color: #f0f9ff;
}

.tag-search-row {
  margin-top: 4px;
}

.tag-search-wrapper {
  flex: 1;
}

.tag-search-wrapper .el-input {
  width: 200px;
  margin-bottom: 8px;
}

.tag-search-results {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.tag-search-empty {
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
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
  margin-bottom: 8px;
  overflow: hidden;
}

.tags-scroll {
  display: flex;
  flex-wrap: nowrap;
  gap: 4px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tags-scroll::-webkit-scrollbar {
  display: none;
}

.desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin: 0 0 8px 0;
  height: 40px;
  overflow: hidden;
}

/* PC端显示PC文本，隐藏移动端文本 */
.pc-text {
  display: inline;
}

.mobile-text {
  display: none;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.stat-item:nth-child(1) {
  color: #F56C6C;
}

.stat-item:nth-child(1) .el-icon {
  color: #F56C6C;
}

.stat-item:nth-child(2) {
  color: #409EFF;
}

.stat-item:nth-child(2) .el-icon {
  color: #409EFF;
}

.stat-item:nth-child(3) {
  color: #909399;
}

.stat-item .dislike-icon {
  fill: currentColor;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .container {
    padding: 0 8px;
  }

  .filter-section {
    padding: 8px;
  }

  /* 筛选行改为横向滚动，不换行 */
  .filter-row {
    flex-direction: row;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    overflow-x: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }

  .filter-row::-webkit-scrollbar {
    display: none;
  }

  .filter-label {
    width: auto;
    padding-top: 0;
    font-size: 13px;
    color: #606266;
    flex-shrink: 0;
    white-space: nowrap;
  }

  .filter-buttons {
    display: flex;
    flex-wrap: nowrap;
    gap: 4px;
    flex-shrink: 0;
  }

  .filter-buttons .el-button {
    flex-shrink: 0;
    padding: 4px 8px;
    font-size: 12px;
    height: 28px;
  }

  /* 标签筛选行特殊处理 */
  .tag-filter-wrapper {
    display: flex;
    flex-wrap: nowrap;
    overflow-x: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
    gap: 6px;
    align-items: center;
  }

  .tag-filter-wrapper::-webkit-scrollbar {
    display: none;
  }

  /* 移动端标签展开后换行显示 */
  .tag-filter-wrapper.tag-expanded {
    flex-wrap: wrap !important;
    overflow-x: visible !important;
    width: 100%;
  }

  /* 标签展开时，父行也要允许换行 */
  .tag-expanded-row {
    flex-wrap: wrap !important;
    overflow-x: visible !important;
  }

  .tag-expanded-row .filter-label {
    width: 100%;
    margin-bottom: 4px;
  }

  .tag-expanded-row .filter-buttons {
    flex-wrap: wrap !important;
    width: 100%;
  }

  /* 标签搜索行 */
  .tag-search-row {
    flex-direction: row;
    align-items: center;
  }

  .tag-search-wrapper {
    width: 100%;
    min-width: 200px;
  }

  .tag-search-wrapper .el-input {
    width: 100%;
  }

  /* 书籍列表 */
  .novel-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .novel-card {
    display: flex;
    flex-direction: row;
    padding: 6px;
  }

  .cover-wrapper {
    width: 75px;
    height: 100px;
    flex-shrink: 0;
  }

  .cover {
    height: 100%;
    border-radius: 4px;
  }

  .platform-tag {
    top: 2px;
    left: 2px;
    font-size: 9px;
    padding: 1px 3px;
    border-radius: 2px;
  }

  .info {
    flex: 1;
    padding: 0 0 0 8px;
    min-width: 0;
  }

  .info > * {
    margin: 0 !important;
    padding: 0 !important;
  }

  .title {
    font-size: 14px;
    margin-bottom: 2px !important;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.2;
  }

  .author {
    font-size: 12px;
    margin-bottom: 2px !important;
    color: #909399;
    line-height: 1.1;
  }

  /* 标签区域 */
  .tags {
    margin-bottom: 2px !important;
    overflow-x: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
    white-space: nowrap;
  }

  .tags::-webkit-scrollbar {
    display: none;
  }

  .tags-scroll {
    display: inline-flex;
    gap: 4px;
    flex-wrap: nowrap;
  }

  .tags-scroll .el-tag {
    flex-shrink: 0;
    font-size: 11px;
    height: 18px;
    padding: 0 4px;
    white-space: nowrap;
  }

  .desc {
    font-size: 11px;
    color: #909399;
    margin-bottom: 2px !important;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.1;
    max-width: 100%;
    height: auto !important;
  }

  /* 移动端显示移动端文本，隐藏PC文本 */
  .pc-text {
    display: none;
  }

  .mobile-text {
    display: inline;
  }

  /* 元信息区域 */
  .meta {
    display: flex;
    justify-content: space-between;
    font-size: 11px;
    color: #909399;
    margin-bottom: 2px !important;
    line-height: 1.1;
  }

  /* 统计区域 */
  .stats {
    display: flex;
    gap: 12px;
    font-size: 12px;
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .stat-item .el-icon {
    font-size: 12px;
  }

  .stat-item .dislike-icon {
    width: 12px;
    height: 12px;
  }

  /* 展开按钮 */
  .expand-toggle {
    font-size: 12px;
    padding: 2px 6px;
  }

  /* 加载中 */
  .loading-more {
    text-align: center;
    padding: 16px 0;
    color: #909399;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .no-more {
    text-align: center;
    padding: 16px 0;
    color: #909399;
    font-size: 13px;
  }

  /* 重置按钮行 */
  .reset-row {
    justify-content: flex-end;
    border-top: none;
    margin-top: 4px;
    padding-top: 0;
  }

  /* 移动端隐藏PC端重置按钮，显示移动端搜索行 */
  .pc-reset-row {
    display: none !important;
  }

  .mobile-search-row {
    display: flex !important;
    width: 100%;
    padding: 0;
    overflow-x: visible;
    margin-left: 0;
    margin-right: 0;
  }

  .mobile-search-wrapper {
    display: flex;
    align-items: center;
    gap: 6px;
    width: 100%;
    flex-wrap: nowrap;
  }

  .mobile-search-input {
    display: block;
    flex: 1;
    min-width: 0;
  }

  .mobile-search-input :deep(.el-input) {
    width: 100%;
  }

  .mobile-search-input :deep(.el-input__wrapper) {
    width: 100%;
  }

  .mobile-search-btn {
    flex-shrink: 0;
    padding: 0 12px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .mobile-search-btn .el-icon {
    font-size: 16px;
  }

  .mobile-search-wrapper .el-button--info {
    height: 32px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .mobile-search-input :deep(.el-input__inner) {
    height: 32px;
    line-height: 32px;
  }

  .mobile-search-input :deep(.el-input__wrapper) {
    padding: 0 8px;
  }

  /* 隐藏PC端分页 */
  .pagination {
    display: none;
  }
}
</style>
