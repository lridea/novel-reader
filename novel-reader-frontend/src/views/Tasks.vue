<template>
  <div class="tasks">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>爬虫任务列表</span>
          <el-button type="primary" @click="loadData" :icon="Refresh">刷新</el-button>
        </div>
      </template>

      <el-table :data="tasks" style="width: 100%" v-loading="loading">
        <el-table-column prop="platform" label="平台" width="120">
          <template #default="{ row }">
            <el-tag>{{ getPlatformName(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskType" label="任务类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="totalCount" label="总数" width="80" />
        <el-table-column prop="successCount" label="成功" width="80">
          <template #default="{ row }">
            <span style="color: #67c23a;">{{ row.successCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failCount" label="失败" width="80">
          <template #default="{ row }">
            <span style="color: #f56c6c;">{{ row.failCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'FAILED'" 
              type="warning" 
              size="small"
              @click="retryTask(row)"
            >
              重试
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
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tasks = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    PENDING: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    RUNNING: '运行中',
    SUCCESS: '成功',
    FAILED: '失败',
    PENDING: '等待中'
  }
  return texts[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    tasks.value = [
      {
        id: 1,
        platform: 'ciweimao',
        taskType: '定时抓取',
        status: 'SUCCESS',
        startTime: '2026-02-17 12:00:00',
        endTime: '2026-02-17 12:30:00',
        totalCount: 500,
        successCount: 480,
        failCount: 20
      },
      {
        id: 2,
        platform: 'sf',
        taskType: '定时抓取',
        status: 'SUCCESS',
        startTime: '2026-02-17 12:00:00',
        endTime: '2026-02-17 12:25:00',
        totalCount: 400,
        successCount: 390,
        failCount: 10
      },
      {
        id: 3,
        platform: 'ciyuanji',
        taskType: '定时抓取',
        status: 'RUNNING',
        startTime: '2026-02-17 14:00:00',
        endTime: null,
        totalCount: 350,
        successCount: 280,
        failCount: 0
      }
    ]
    total.value = 3
  } catch (error) {
    console.error('加载任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  router.push(`/tasks/${row.id}`)
}

const retryTask = (row) => {
  ElMessage.info(`重试任务: ${row.id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.tasks {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
