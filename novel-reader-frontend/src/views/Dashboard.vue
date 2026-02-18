<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalNovels }}</div>
            <div class="stat-label">总小说数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon :size="32"><Download /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayCrawled }}</div>
            <div class="stat-label">今日抓取</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon :size="32"><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.successRate }}%</div>
            <div class="stat-label">任务成功率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon :size="32"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.failedCount }}</div>
            <div class="stat-label">失败任务</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>各平台小说数量</span>
            </div>
          </template>
          <div class="platform-stats">
            <div v-for="item in platformStats" :key="item.platform" class="platform-item">
              <div class="platform-name">{{ item.name }}</div>
              <el-progress 
                :percentage="item.percentage" 
                :stroke-width="20"
                :color="item.color"
              />
              <div class="platform-count">{{ item.count }} 本</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>平台状态</span>
              <el-button type="primary" size="small" @click="loadData" :loading="loading">
                刷新
              </el-button>
            </div>
          </template>
          <el-table :data="platforms" style="width: 100%">
            <el-table-column prop="name" label="平台" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">
                  {{ row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastCrawlTime" label="最后执行" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleTrigger(row.platform)"
                  :disabled="!row.enabled"
                >
                  执行
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Download, Timer, Warning } from '@element-plus/icons-vue'
import { crawlerApi } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)

const stats = ref({
  totalNovels: 0,
  todayCrawled: 0,
  successRate: 95,
  failedCount: 0
})

const platformStats = ref([
  { platform: 'ciweimao', name: '刺猬猫', count: 0, percentage: 0, color: '#409eff' },
  { platform: 'sf', name: 'SF轻小说', count: 0, percentage: 0, color: '#67c23a' },
  { platform: 'ciyuanji', name: '次元姬', count: 0, percentage: 0, color: '#e6a23c' }
])

const platforms = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const [novels, configs] = await Promise.all([
      crawlerApi.getNovels({ page: 0, size: 1 }),
      crawlerApi.getConfigs()
    ])
    
    stats.value.totalNovels = novels.totalElements || 0
    
    platforms.value = (configs || []).map(config => ({
      platform: config.platform,
      name: getPlatformName(config.platform),
      enabled: config.enabled,
      lastCrawlTime: config.lastCrawlTime || '-'
    }))

    const platformCounts = {}
    platformStats.value.forEach(p => {
      platformCounts[p.platform] = 0
    })

    const allNovels = await crawlerApi.getNovels({ page: 0, size: 1000 })
    if (allNovels.content) {
      allNovels.content.forEach(novel => {
        if (platformCounts[novel.platform] !== undefined) {
          platformCounts[novel.platform]++
        }
      })
    }

    const total = Object.values(platformCounts).reduce((a, b) => a + b, 0)
    platformStats.value = platformStats.value.map(p => ({
      ...p,
      count: platformCounts[p.platform],
      percentage: total > 0 ? Math.round((platformCounts[p.platform] / total) * 100) : 0
    }))

  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
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

const handleTrigger = async (platform) => {
  try {
    await crawlerApi.trigger(platform)
    ElMessage.success(`已触发 ${getPlatformName(platform)} 爬虫任务`)
  } catch (error) {
    console.error('触发爬虫失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.platform-stats {
  padding: 10px 0;
}

.platform-item {
  margin-bottom: 24px;
}

.platform-name {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.platform-count {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}
</style>
