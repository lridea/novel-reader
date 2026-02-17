<template>
  <div class="task-detail">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">任务详情</span>
      </template>
    </el-page-header>

    <el-card shadow="hover" style="margin-top: 20px;">
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
        <el-descriptions-item label="开始时间">{{ task.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ task.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ task.duration || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总数">{{ task.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="成功">
          <span style="color: #67c23a;">{{ task.successCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="失败">
          <span style="color: #f56c6c;">{{ task.failCount }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px;">
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

const route = useRoute()
const router = useRouter()

const task = ref({
  id: 1,
  platform: 'ciweimao',
  status: 'SUCCESS',
  startTime: '2026-02-17 12:00:00',
  endTime: '2026-02-17 12:30:00',
  duration: '30分钟',
  totalCount: 500,
  successCount: 480,
  failCount: 20,
  logs: `2026-02-17 12:00:00 [INFO] 开始执行爬虫任务
2026-02-17 12:00:05 [INFO] 加载平台配置
2026-02-17 12:00:10 [INFO] 标签：玄幻, 修仙, 都市, 系统
2026-02-17 12:00:15 [INFO] 解析小说列表...
2026-02-17 12:00:20 [INFO] 抓取到100本小说
2026-02-17 12:00:25 [INFO] 保存到数据库...
2026-02-17 12:00:30 [INFO] 处理小说: 修仙从系统开始
2026-02-17 12:00:35 [INFO] 首次抓取，获取前3章内容
2026-02-17 12:00:40 [INFO] 调用AI生成概括...
2026-02-17 12:00:50 [INFO] AI概括生成成功
...
2026-02-17 12:30:00 [INFO] 爬虫任务完成`
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

const goBack = () => {
  router.push('/tasks')
}

onMounted(() => {
  const taskId = route.params.id
  console.log('加载任务详情:', taskId)
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
