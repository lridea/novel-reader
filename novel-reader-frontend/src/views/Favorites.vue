<template>
  <div class="favorites-page">
    <div class="container">
      <h2 class="page-title">我的收藏</h2>
      
      <div v-loading="loading" class="novel-grid">
        <div
          v-for="novel in favorites"
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
            <div class="meta">
              <span>{{ formatDate(novel.latestUpdateTime) }}</span>
            </div>
            <p v-if="novel.note" class="note">{{ novel.note }}</p>
          </div>
          <div class="actions">
            <el-button type="danger" size="small" @click.stop="removeFavorite(novel.id)">
              取消收藏
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="!loading && favorites.length === 0" class="empty">
        <el-empty description="暂无收藏">
          <el-button type="primary" @click="goHome">去逛逛</el-button>
        </el-empty>
      </div>

      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 40, 60]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { crawlerApi, favoriteApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const favorites = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

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
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const goDetail = (id) => {
  router.push(`/novel/${id}`)
}

const goHome = () => {
  router.push('/')
}

const loadData = async () => {
  loading.value = true
  try {
    const response = await favoriteApi.getFavorites({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    favorites.value = response.content || []
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载收藏失败:', error)
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (novelId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      type: 'warning'
    })
    await favoriteApi.removeFavorite(novelId)
    ElMessage.success('已取消收藏')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
    }
  }
}

onMounted(() => {
  loadData()
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

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
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

.meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.note {
  font-size: 13px;
  color: #606266;
  margin: 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.actions {
  padding: 0 12px 12px;
}

.empty {
  padding: 60px 0;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
