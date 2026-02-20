<template>
  <div class="favorites-page">
    <div class="container">
      <!-- 筛选卡片 -->
      <div class="filter-section">
        <div class="page-header">
          <h2 class="page-title">我的收藏</h2>
          <el-button type="primary" @click="showCategoryManager = true">
            管理收藏夹
          </el-button>
        </div>

        <div class="toolbar">
          <div class="category-selector" v-if="categories.length > 0">
            <div class="selected-category" @click="toggleCategoryExpand">
              <span class="category-name">{{ currentCategory?.name || '选择收藏夹' }}</span>
              <span class="category-badge">{{ currentCategory?.favoriteCount || 0 }}</span>
              <el-icon class="expand-icon" :class="{ expanded: isCategoryExpanded }"><ArrowDown /></el-icon>
            </div>
            <div class="category-dropdown" v-show="isCategoryExpanded">
              <div
                v-for="category in categories"
                :key="category.id"
                :class="['category-option', currentCategoryId === category.id ? 'active' : '']"
                @click="selectCategory(category.id)"
              >
                <span class="category-name">{{ category.name }}</span>
                <span class="category-badge">{{ category.favoriteCount || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="filter-bar">
            <div class="search-wrapper">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索书籍名称"
                class="search-input"
                clearable
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </div>
            <div class="sort-wrapper">
              <span class="sort-label">排序：</span>
              <div class="sort-buttons-wrapper">
                <el-button-group class="sort-buttons">
                  <el-button
                    :type="sortBy === 'createdAt' ? 'primary' : 'default'"
                    @click="setSortBy('createdAt')"
                  >
                    收藏时间
                  </el-button>
                  <el-button
                    :type="sortBy === 'updateTime' ? 'primary' : 'default'"
                    @click="setSortBy('updateTime')"
                  >
                    更新时间
                  </el-button>
                </el-button-group>
              </div>
              <el-button
                :type="isBatchMode ? 'danger' : 'warning'"
                @click="toggleBatchMode"
                class="batch-btn"
              >
                {{ isBatchMode ? '取消' : '批量删除' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-loading="loading" class="novel-grid">
        <div
          v-for="novel in favorites"
          :key="novel.id"
          :class="['novel-card', { 'selected': selectedIds.includes(novel.id) }]"
          @click="handleCardClick(novel.id)"
        >
          <el-checkbox
            v-if="isBatchMode"
            :model-value="selectedIds.includes(novel.id)"
            class="batch-checkbox"
            @click.stop
            @change="toggleSelect(novel.id)"
          />
          <div class="cover-wrapper">
            <img :src="novel.coverUrl || defaultCover" :alt="novel.title" class="cover" />
            <div class="platform-tag" :class="novel.platform">
              {{ getPlatformName(novel.platform) }}
            </div>
          </div>
          <div class="info">
            <h3 class="title">{{ novel.title }}</h3>
            <p class="author">{{ novel.author }}</p>
            <div class="tags" v-if="parseTags(novel.tags).length > 0 || parseTags(novel.userTags).length > 0">
              <div class="tags-scroll">
                <el-tag
                  v-for="tag in parseTags(novel.tags)"
                  :key="'crawl-' + tag"
                  size="small"
                  type="warning"
                >
                  {{ tag }}
                </el-tag>
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
            <p class="desc" v-if="novel.description">{{ novel.description }}</p>
            <div class="meta">
              <span>{{ formatWordCount(novel.wordCount) }}</span>
              <span class="meta-item"><el-icon><Clock /></el-icon>{{ formatDate(novel.latestUpdateTime) }}</span>
            </div>
            <div class="stats-row">
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
              <span class="favorite-time"><el-icon><Star /></el-icon>{{ formatDate(novel.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="isBatchMode && selectedIds.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedIds.length }} 本</span>
        <el-button type="danger" @click="batchRemove">批量取消收藏</el-button>
      </div>

      <div v-if="!loading && favorites.length === 0" class="empty">
        <el-empty description="暂无收藏">
          <el-button type="primary" @click="goHome">去逛逛</el-button>
        </el-empty>
      </div>

      <div v-if="total > 0 && !isMobile" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 40, 60]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handlePageChange"
        />
      </div>

      <div v-if="isMobile && favorites.length > 0 && loading" class="loading-more">
        加载中...
      </div>
      <div v-if="isMobile && favorites.length > 0 && !loading && favorites.length >= total" class="no-more">
        没有更多了
      </div>
    </div>

    <el-dialog
      v-model="showCategoryManager"
      title="收藏夹管理"
      width="600px"
    >
      <div class="category-manager">
        <div class="category-list">
          <div v-for="category in categories" :key="category.id" class="category-item">
            <div class="category-info">
              <span class="category-name">{{ category.name }}</span>
              <el-tag v-if="category.isDefault" size="small" type="success" class="default-tag">默认</el-tag>
              <span class="category-count">{{ category.favoriteCount || 0 }}本</span>
            </div>
            <div class="category-actions">
              <el-button 
                link 
                type="primary" 
                size="small"
                @click="editCategory(category)"
                :disabled="category.isDefault"
              >
                编辑
              </el-button>
              <el-button 
                link 
                type="danger" 
                size="small"
                @click="deleteCategory(category.id)"
                :disabled="category.isDefault"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-divider />
        <div class="new-category-section">
          <span class="new-category-label">新建收藏夹</span>
          <div class="new-category-input-row">
            <el-input v-model="newCategoryForm.name" placeholder="收藏夹名称" class="new-category-input" maxlength="9" show-word-limit />
            <el-button type="primary" @click="createCategory">创建</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="showEditCategory"
      title="编辑收藏夹"
      width="450px"
    >
      <el-form :model="editCategoryForm" label-width="60px">
        <el-form-item label="名称">
          <el-input v-model="editCategoryForm.name" placeholder="请输入收藏夹名称" maxlength="9" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editCategoryForm.description" type="textarea" :rows="3" placeholder="请输入收藏夹描述（可选）" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditCategory = false">取消</el-button>
        <el-button type="primary" @click="updateCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { crawlerApi, favoriteApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Timer, Calendar, ArrowDown, Star, ChatDotRound, StarFilled, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const favorites = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const categories = ref([])
const currentCategoryId = ref(null)
const isCategoryExpanded = ref(false)

const showCategoryManager = ref(false)
const showEditCategory = ref(false)
const newCategoryForm = ref({ name: '' })
const editCategoryForm = ref({ id: null, name: '', description: '' })

const searchKeyword = ref('')
const sortBy = ref('createdAt')

const isBatchMode = ref(false)
const selectedIds = ref([])

const toggleBatchMode = () => {
  isBatchMode.value = !isBatchMode.value
  if (!isBatchMode.value) {
    selectedIds.value = []
  }
}

const toggleSelect = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handleCardClick = (id) => {
  if (isBatchMode.value) {
    toggleSelect(id)
  } else {
    goDetail(id)
  }
}

const batchRemove = async () => {
  if (selectedIds.value.length === 0) return
  
  try {
    const response = await favoriteApi.batchRemove(selectedIds.value)
    if (response.success) {
      ElMessage.success(`成功取消收藏 ${selectedIds.value.length} 本书籍`)
      selectedIds.value = []
      isBatchMode.value = false
      await loadCategories()
      await loadData(false)
    } else {
      ElMessage.error(response.message || '批量取消收藏失败')
    }
  } catch (error) {
    ElMessage.error('批量取消收藏失败')
  }
}

// 移动端检测
const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 保存滚动位置和分页数据
const saveScrollPosition = () => {
  sessionStorage.setItem('favoritesScrollPosition', window.scrollY.toString())
  sessionStorage.setItem('favoritesCurrentPage', currentPage.value.toString())
  sessionStorage.setItem('favoritesTotal', total.value.toString())
  sessionStorage.setItem('favoritesCategoryId', currentCategoryId.value?.toString() || '')
  sessionStorage.setItem('favoritesSortBy', sortBy.value)
  sessionStorage.setItem('favoritesSearchKeyword', searchKeyword.value)
  
  // 移动端额外保存已加载的数据
  if (isMobile.value) {
    sessionStorage.setItem('favoritesData', JSON.stringify(favorites.value))
  }
}

// 恢复滚动位置和分页数据
const restoreScrollPosition = async () => {
  const savedPosition = sessionStorage.getItem('favoritesScrollPosition')
  const savedPage = sessionStorage.getItem('favoritesCurrentPage')
  const savedTotal = sessionStorage.getItem('favoritesTotal')
  const savedData = sessionStorage.getItem('favoritesData')
  const savedCategoryId = sessionStorage.getItem('favoritesCategoryId')
  const savedSortBy = sessionStorage.getItem('favoritesSortBy')
  const savedSearchKeyword = sessionStorage.getItem('favoritesSearchKeyword')
  
  if (savedPage) {
    currentPage.value = parseInt(savedPage)
  }
  
  if (savedTotal) {
    total.value = parseInt(savedTotal)
  }
  
  if (savedCategoryId) {
    currentCategoryId.value = parseInt(savedCategoryId)
  }
  
  if (savedSortBy) {
    sortBy.value = savedSortBy
  }
  
  if (savedSearchKeyword) {
    searchKeyword.value = savedSearchKeyword
  }
  
  // 移动端直接恢复缓存数据，PC端重新加载
  if (isMobile.value && savedData) {
    favorites.value = JSON.parse(savedData)
  } else if (savedPage) {
    // PC端重新加载对应页的数据
    await loadData(false)
  }
  
  // 数据加载完成后再滚动，避免闪烁
  if (savedPosition) {
    requestAnimationFrame(() => {
      window.scrollTo(0, parseInt(savedPosition))
    })
  }
  
  // 清理缓存
  sessionStorage.removeItem('favoritesScrollPosition')
  sessionStorage.removeItem('favoritesCurrentPage')
  sessionStorage.removeItem('favoritesData')
  sessionStorage.removeItem('favoritesTotal')
  sessionStorage.removeItem('favoritesCategoryId')
  sessionStorage.removeItem('favoritesSortBy')
  sessionStorage.removeItem('favoritesSearchKeyword')
}

// 滚动监听 - 移动端自动加载更多
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

// 移动端加载更多
const loadMore = () => {
  if (loading.value) return
  if (favorites.value.length >= total.value) return
  
  currentPage.value++
  loadData(true)
}

// PC端分页处理
const handlePageChange = () => {
  loadData(false)
}

// 清除缓存并加载数据
const clearCacheAndLoad = () => {
  sessionStorage.removeItem('favoritesScrollPosition')
  sessionStorage.removeItem('favoritesCurrentPage')
  sessionStorage.removeItem('favoritesData')
  sessionStorage.removeItem('favoritesTotal')
  sessionStorage.removeItem('favoritesCategoryId')
  
  currentPage.value = 1
  loadData(false)
}

const currentCategory = computed(() => {
  return categories.value.find(c => c.id === currentCategoryId.value)
})

const defaultCover = 'https://via.placeholder.com/150x200?text=No+Cover'

const getPlatformName = (platform) => {
  const names = {
    ciweimao: '刺猬猫',
    sf: 'SF',
    ciyuanji: '次元姬',
    qidian: '起点'
  }
  return names[platform] || platform
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
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

const formatWordCount = (count) => {
  if (!count) return '-'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toLocaleString()
}

const goDetail = (id) => {
  saveScrollPosition()
  router.push(`/novel/${id}`)
}

const goHome = () => {
  router.push('/')
}

const loadCategories = async () => {
  try {
    const response = await crawlerApi.getCategories()
    if (response && response.success) {
      let list = response.categories || []
      // 默认收藏夹置顶
      list.sort((a, b) => {
        if (a.isDefault && !b.isDefault) return -1
        if (!a.isDefault && b.isDefault) return 1
        return a.sortOrder - b.sortOrder
      })
      categories.value = list
      if (categories.value.length > 0 && !currentCategoryId.value) {
        currentCategoryId.value = categories.value[0].id
      }
    }
  } catch (error) {
    console.error('加载收藏夹失败:', error)
  }
}

const loadData = async (append = false) => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (currentCategoryId.value) {
      params.categoryId = currentCategoryId.value
    }
    if (sortBy.value) {
      params.sortBy = sortBy.value
    }
    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    const response = await favoriteApi.getFavorites(params)
    if (append) {
      favorites.value = [...favorites.value, ...(response.content || [])]
    } else {
      favorites.value = response.content || []
    }
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载收藏失败:', error)
  } finally {
    loading.value = false
  }
}

const selectCategory = (categoryId) => {
  currentCategoryId.value = categoryId
  currentPage.value = 1
  isCategoryExpanded.value = false
  clearCacheAndLoad()
}

const toggleCategoryExpand = () => {
  isCategoryExpanded.value = !isCategoryExpanded.value
}

const handleSearch = () => {
  currentPage.value = 1
  clearCacheAndLoad()
}

const setSortBy = (value) => {
  sortBy.value = value
  currentPage.value = 1
  clearCacheAndLoad()
}

const removeFavorite = async (novelId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      type: 'warning'
    })
    await favoriteApi.removeFavorite(novelId, currentCategoryId.value)
    ElMessage.success('已取消收藏')
    loadCategories()
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
    }
  }
}

const createCategory = async () => {
  if (!newCategoryForm.value.name.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  try {
    const response = await crawlerApi.createCategory({
      name: newCategoryForm.value.name,
      description: ''
    })
    if (response && response.success) {
      ElMessage.success('创建成功')
      newCategoryForm.value.name = ''
      loadCategories()
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建收藏夹失败:', error)
    ElMessage.error('创建失败')
  }
}

const editCategory = (category) => {
  editCategoryForm.value = {
    id: category.id,
    name: category.name,
    description: category.description || ''
  }
  showEditCategory.value = true
}

const updateCategory = async () => {
  if (!editCategoryForm.value.name.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  try {
    const response = await crawlerApi.updateCategory(editCategoryForm.value.id, {
      name: editCategoryForm.value.name,
      description: editCategoryForm.value.description
    })
    if (response && response.success) {
      ElMessage.success('更新成功')
      showEditCategory.value = false
      loadCategories()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新收藏夹失败:', error)
    ElMessage.error('更新失败')
  }
}

const deleteCategory = async (categoryId) => {
  try {
    await ElMessageBox.confirm('删除收藏夹后，其中的书籍将移至默认收藏夹，确定删除吗？', '提示', {
      type: 'warning'
    })
    const response = await crawlerApi.deleteCategory(categoryId)
    if (response && response.success) {
      ElMessage.success('删除成功')
      if (currentCategoryId.value === categoryId) {
        currentCategoryId.value = categories.value.find(c => c.isDefault)?.id || categories.value[0]?.id
      }
      loadCategories()
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除收藏夹失败:', error)
    }
  }
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('scroll', handleScroll)
  
  await loadCategories()
  
  // 检查是否有缓存的分页数据
  const savedPage = sessionStorage.getItem('favoritesCurrentPage')
  
  if (savedPage) {
    // 有缓存数据，恢复状态
    await restoreScrollPosition()
  } else {
    // 无缓存数据，正常加载
    loadData(false)
  }
})
</script>

<style scoped>
.favorites-page {
  padding: 20px 0;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 筛选区域卡片 */
.filter-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.category-selector {
  position: relative;
  flex-shrink: 0;
}

.selected-category {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  background: #409eff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  width: fit-content;
  min-width: 150px;
}

.selected-category .category-name {
  font-weight: 500;
}

.selected-category .category-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.25);
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.selected-category .expand-icon {
  margin-left: auto;
  transition: transform 0.3s;
}

.selected-category .expand-icon.expanded {
  transform: rotate(180deg);
}

.category-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px;
  min-width: 200px;
  z-index: 100;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-option:hover {
  background: #f5f7fa;
}

.category-option.active {
  background: #ecf5ff;
  color: #409eff;
}

.category-option .category-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-option .category-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: #f0f2f5;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  flex-shrink: 0;
}

.category-option.active .category-badge {
  background: #409eff;
  color: #fff;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.filter-bar .search-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-bar .search-input {
  width: 500px;
}

.filter-bar .sort-buttons {
  flex-shrink: 0;
  margin-left: auto;
}

.sort-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.sort-buttons-wrapper {
  display: inline-flex;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  background: #fff;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #dcdfe6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.category-tab:hover {
  background: #f5f7fa;
  color: #409eff;
  border-color: #c6e2ff;
}

.category-tab.active {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.category-tab .category-name {
  font-weight: 500;
}

.category-tab .category-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-tab.active .category-badge {
  background: rgba(255, 255, 255, 0.25);
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
  position: relative;
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

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item .el-icon {
  font-size: 12px;
  position: relative;
  top: 0px;
}

.favorite-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.favorite-time .el-icon {
  font-size: 12px;
  position: relative;
  top: 0px;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.batch-btn {
  margin-left: 12px;
}

.batch-checkbox {
  position: absolute;
  top: 0px;
  right: 8px;
}

.novel-card.selected {
  border: 2px solid #409eff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
}

.batch-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.selected-count {
  color: #606266;
  font-size: 14px;
}

.empty {
  padding: 60px 0;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.loading-more,
.no-more {
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 14px;
}

.category-manager {
  min-height: auto;
}

.category-list {
  max-height: 300px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.category-item:last-child {
  border-bottom: none;
}

.category-info {
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex: 1;
}

.category-name {
  font-size: 14px;
  font-weight: 500;
  min-width: 80px;
}

.category-info .default-tag {
  flex-shrink: 0;
}

.category-count {
  font-size: 12px;
  color: #909399;
  min-width: 40px;
}

.category-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.new-category-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.new-category-label {
  font-size: 14px;
  color: #606266;
  width: 80px;
  text-align: left;
  flex-shrink: 0;
}

.new-category-input {
  flex: 1;
}

.new-category-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex: 1;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .favorites-page {
    padding: 0 4px;
  }

  .container {
    padding: 0 4px;
  }

  .filter-section {
    padding: 12px 12px 8px 12px;
    margin-top: 12px;
    margin-bottom: 12px;
    border-radius: 8px;
  }

  .page-header {
    flex-direction: row;
    gap: 12px;
    align-items: center;
    justify-content: space-between;
    padding-top: 0;
    margin-bottom: 12px;
  }

  .page-header h2 {
    font-size: 18px;
  }

  .filter-bar {
    flex-direction: column;
    gap: 12px;
    flex: none;
  }

  .filter-bar .category-selector {
    width: 100%;
  }

  .filter-bar .search-wrapper {
    width: 100%;
  }

  .filter-bar .search-input {
    width: 100%;
    max-width: 100%;
  }

  .filter-bar .sort-buttons {
    margin-left: 0;
    margin-top: 8px;
    width: 100%;
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .filter-bar .sort-buttons .el-button {
    flex-shrink: 0;
  }

  .category-tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
    gap: 8px;
    padding: 4px 0;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }

  .category-tabs::-webkit-scrollbar {
    display: none;
  }

  .category-tab {
    flex-shrink: 0;
    padding: 6px 12px;
    font-size: 13px;
  }

  .novel-grid {
    grid-template-columns: 1fr;
    gap: 8px;
    min-height: auto;
    align-content: start;
  }

  .novel-card {
    display: flex;
    flex-direction: row;
    padding: 6px;
    height: auto;
    min-height: 100px;
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

  .meta {
    display: flex;
    justify-content: space-between;
    font-size: 11px;
    color: #909399;
    margin-bottom: 2px !important;
    line-height: 1.1;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .meta-item .el-icon {
    font-size: 11px;
    position: relative;
    top: 0px;
  }

  .favorite-time {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: #909399;
  }

  .favorite-time .el-icon {
    font-size: 11px;
    position: relative;
    top: 0px;
  }

  .stats-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

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

  .batch-btn {
    margin-left: 8px;
    padding: 6px 10px;
    font-size: 12px;
  }

  .batch-checkbox {
    top: -3px;
    right: 6px;
  }

  .batch-actions {
    padding: 10px 16px;
  }

  .selected-count {
    font-size: 13px;
  }

  .batch-actions .el-button {
    padding: 6px 12px;
    font-size: 13px;
  }

  /* 收藏夹管理弹窗移动端适配 */
  :deep(.el-dialog) {
    width: 90% !important;
    max-width: 400px;
    margin: 10vh auto 0 !important;
  }

  :deep(.el-dialog__body) {
    padding: 12px;
  }

  .category-item {
    flex-direction: row;
    gap: 8px;
    align-items: center;
    padding: 10px 0;
  }

  .category-info {
    width: auto;
    flex: 1;
    flex-wrap: nowrap;
    gap: 6px;
    align-items: baseline;
  }

  .category-info .category-name {
    min-width: auto;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .category-actions {
    width: auto;
    justify-content: flex-end;
    flex-shrink: 0;
  }

  .new-category-section {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .new-category-label {
    display: none;
  }

  .new-category-input-row {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .new-category-input {
    flex: 1;
    width: auto;
  }

  .new-category-section .el-button {
    flex-shrink: 0;
  }

  /* 编辑收藏夹弹窗移动端适配 */
  :deep(.el-dialog__wrapper) {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  /* 搜索和排序区域移动端适配 */
  .toolbar {
    flex-direction: column;
    gap: 12px;
    margin-bottom: 0;
  }

  .toolbar .category-selector {
    width: 100%;
  }

  .toolbar .selected-category {
    width: 100%;
    justify-content: space-between;
  }

  .toolbar .filter-bar {
    width: 100%;
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    flex: none;
  }

  .toolbar .search-wrapper {
    width: 100%;
    flex-wrap: nowrap;
  }

  .toolbar .search-input {
    flex: 1;
    width: auto;
  }

  .toolbar .sort-wrapper {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 8px;
    width: 100%;
    flex-wrap: nowrap;
  }

  .toolbar .sort-label {
    font-size: 13px;
    flex-shrink: 0;
  }

  .toolbar .sort-buttons-wrapper {
    display: inline-flex;
    flex: 0 0 auto;
  }

  .toolbar .sort-buttons {
    margin-left: 0 !important;
    flex-shrink: 0;
    display: flex;
    flex-wrap: nowrap;
  }

  .toolbar .sort-buttons .el-button {
    margin-left: 0 !important;
    padding: 6px 12px;
    font-size: 13px;
  }

  .toolbar .batch-btn {
    margin-left: auto;
    flex-shrink: 0;
  }

  /* 书籍卡片移动端适配 - 参考首页样式 */
  .novel-card {
    display: flex;
    flex-direction: row;
    padding: 6px;
    position: relative;
  }

  .cover-wrapper {
    width: 75px;
    height: 100px;
    flex-shrink: 0;
  }

  .info {
    flex: 1;
    padding: 0 0 0 8px;
    min-width: 0;
  }

  .title {
    font-size: 14px;
    margin-bottom: 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding-right: 70px;
  }

  .author {
    font-size: 11px;
    color: #909399;
    margin-bottom: 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .note {
    display: none;
  }
}
</style>
