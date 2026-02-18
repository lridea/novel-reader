<template>
  <div class="task-detail">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">任务详情</span>
      </template>
    </el-page-header>

    <el-card shadow="hover" style="margin-top: 20px;" v-loading="loading">
      <template #header>
        <span>基本信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="任务ID">{{ task.id }}</el-descriptions-item>
        <el-descriptions-item label="平台">
          <el-tag>{{ getPlatformName(task.platform) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(task.status)">
            {{ getStatusText(task.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(task.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(task.endTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ calculateDuration(task.startTime, task.endTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总数">{{ task.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="成功">
          <span style="color: #67c23a;">{{ task.successCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="失败">
          <span style="color: #f56c6c;">{{ task.failCount }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px;" v-loading="loading">
      <template #header>
        <span>执行日志</span>
      </template>
      <div class="log-container">
        <pre class="log-content">{{ task.logs }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { crawlerApi } from '../api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const task = ref({
  id: null,
  platform: '',
  status: '',
  startTime: '',
  endTime: '',
  duration: '',
  totalCount: 0,
  successCount: 0,
  failCount: 0,
  logs: ''
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

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', { hour12: false })
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime) return ''
  const start = new Date(startTime).getTime()
  const end = endTime ? new Date(endTime).getTime() : Date.now()
  const diff = end - start

  const hours = Math.floor(diff / 3600000)
  const minutes = Math.floor((diff % 3600000) / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds}秒`
  } else {
    return `${seconds}秒`
  }
}

const goBack = () => {
  router.push('/tasks')
}

const loadData = async () => {
  loading.value = true
  try {
    const taskId = route.params.id
    const data = await crawlerApi.getTask(taskId)
    if (data) {
      task.value = data
    } else {
      ElMessage.error('任务不存在')
      goBack()
    }
  } catch (error) {
    console.error('加载任务详情失败:', error)
    ElMessage.error('加载任务详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.task-detail {
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.log-container {
  background: #1d1e1f;
  border-radius: 8px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.log-content {
  color: #a0a0a0;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}
</style>
