<template>
  <div class="novel-management">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">书籍管理</span>
      </template>
      <template #extra>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="batchDelete">
          批量删除 ({{ selectedIds.length }})
        </el-button>
      </template>
    </el-page-header>

    <el-card shadow="hover" style="margin-top: 20px;">
      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px;">
        <el-form-item label="书籍名称">
          <el-input v-model="queryForm.keyword" placeholder="请输入书籍名称或作者" clearable style="width: 300px;" />
        </el-form-item>
        <el-form-item label="最小点踩数">
          <el-input-number v-model="queryForm.minDislikeCount" :min="0" controls-position="right" style="width: 150px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchNovels">查询</el-button>
          <el-button @click="resetQueryForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 书籍列表 -->
      <el-table
        :data="novels"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="80">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverUrl"
              :src="scope.row.coverUrl"
              :preview-src-list="[scope.row.coverUrl]"
              fit="cover"
              style="width: 50px; height: 70px;"
            />
            <div v-else style="width: 50px; height: 70px; background: #f0f0f0; display: flex; align-items: center; justify-content: center;">
              暂无封面
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="scope">
            <el-tag>{{ scope.row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="favoriteCount" label="收藏数" width="100" sortable />
        <el-table-column prop="commentCount" label="评论数" width="100" sortable />
        <el-table-column prop="dislikeCount" label="点踩数" width="100" sortable />
        <el-table-column prop="latestUpdateTime" label="最后更新时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button type="danger" link @click="deleteNovel(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="searchNovels"
        @current-change="searchNovels"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { crawlerApi } from '../api'

const router = useRouter()

// 数据
const novels = ref([])
const loading = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 查询表单
const queryForm = ref({
  keyword: '',
  minDislikeCount: null
})

// 返回
const goBack = () => {
  router.back()
}

// 搜索书籍
const searchNovels = async () => {
  loading.value = true
  try {
    const response = await crawlerApi.searchNovels({
      page: currentPage.value,
      size: pageSize.value,
      keyword: queryForm.value.keyword,
      minDislikeCount: queryForm.value.minDislikeCount
    })
    if (response && response.success) {
      novels.value = response.content || []
      total.value = response.totalElements || 0
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('搜索书籍失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 重置查询表单
const resetQueryForm = () => {
  queryForm.value = {
    keyword: '',
    minDislikeCount: null
  }
  currentPage.value = 0
  searchNovels()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/novels/${row.id}`)
}

// 删除书籍
const deleteNovel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await crawlerApi.batchDeleteNovels([id])
    if (response && response.success) {
      ElMessage.success('删除成功')
      searchNovels()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除书籍失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 本书吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await crawlerApi.batchDeleteNovels(selectedIds.value)
    if (response && response.success) {
      ElMessage.success(`批量删除成功，共删除 ${response.deleteCount} 本书`)
      selectedIds.value = []
      searchNovels()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '停更',
    1: '连载',
    2: '完结'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  searchNovels()
})
</script>

<style scoped>
.novel-management {
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}
</style>
