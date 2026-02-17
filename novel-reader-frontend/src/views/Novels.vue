<template>
  <div class="novels">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据浏览</span>
          <div class="header-actions">
            <el-select v-model="filterPlatform" placeholder="选择平台" clearable style="width: 150px;">
              <el-option label="全部平台" value="" />
              <el-option label="刺猬猫" value="ciweimao" />
              <el-option label="SF轻小说" value="sf" />
              <el-option label="次元姬" value="ciyuanji" />
              <el-option label="起点" value="qidian" />
            </el-select>
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索书名/作者" 
              clearable
              style="width: 200px; margin-left: 10px;"
              @keyup.enter="loadData"
            >
              <template #append>
                <el-button :icon="Search" @click="loadData" />
              </template>
            </el-input>
          </div>
        </div>
      </template>

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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture } from '@element-plus/icons-vue'
import { crawlerApi } from '../api'

const router = useRouter()
const loading = ref(false)
const novels = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterPlatform = ref('')
const searchKeyword = ref('')

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
    let data
    if (filterPlatform.value) {
      data = await crawlerApi.getNovelsByPlatform(filterPlatform.value)
    } else {
      data = await crawlerApi.getNovels({
        page: currentPage.value - 1,
        size: pageSize.value
      })
    }
    
    if (Array.isArray(data)) {
      novels.value = data
      total.value = data.length
    } else if (data.content) {
      novels.value = data.content
      total.value = data.totalElements
    }
  } catch (error) {
    console.error('加载小说列表失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  router.push(`/novels/${row.id}`)
}

watch(filterPlatform, () => {
  currentPage.value = 1
  loadData()
})

onMounted(() => {
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
</style>
