<template>
  <div class="platforms">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>平台配置</span>
          <div>
            <el-button type="success" @click="triggerAll" :loading="triggering">启动全部爬虫</el-button>
            <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12" v-for="config in configs" :key="config.platform">
          <el-card shadow="hover" class="config-card">
            <template #header>
              <div class="config-header">
                <span class="platform-name">{{ getPlatformName(config.platform) }}</span>
                <div class="header-right">
                  <el-tag v-if="config.isRunning" type="success" effect="dark" size="small">运行中</el-tag>
                  <el-tag v-else-if="config.enabled" type="info" size="small">空闲</el-tag>
                  <el-tag v-else type="danger" size="small">已禁用</el-tag>
                  <el-switch v-model="config.enabled" @change="handleEnabledChange(config)" style="margin-left: 10px;" />
                </div>
              </div>
            </template>

            <el-form label-width="100px">
              <el-form-item label="平台标识">
                <el-input :value="config.platform" disabled />
              </el-form-item>
              <el-form-item label="抓取间隔">
                <el-input-number v-model="config.crawlInterval" :min="600" :max="86400" :step="600" />
                <span style="margin-left: 10px; color: #909399;">秒 ({{ formatInterval(config.crawlInterval) }})</span>
              </el-form-item>
              <el-form-item label="标签列表">
                <div class="tags-container">
                  <el-tag 
                    v-for="tag in parseTags(config.tags)" 
                    :key="tag" 
                    closable
                    @close="removeTag(config, tag)"
                    style="margin-right: 8px; margin-bottom: 8px;"
                  >
                    {{ tag }}
                  </el-tag>
                  <el-input
                    v-model="newTags[config.platform]"
                    placeholder="输入标签后回车"
                    size="small"
                    style="width: 120px;"
                    @keyup.enter="addTag(config)"
                  />
                </div>
              </el-form-item>
              <el-form-item label="最后执行">
                <span>{{ config.lastCrawlTime || '-' }}</span>
              </el-form-item>
              <el-form-item label="成功时间">
                <span>{{ config.lastSuccessCrawlTime || '-' }}</span>
              </el-form-item>
              <el-form-item label="执行统计">
                <span>成功: {{ config.crawlCount || 0 }} 次, 失败: {{ config.failCount || 0 }} 次</span>
              </el-form-item>
              <el-form-item label="最后错误" v-if="config.lastErrorMessage">
                <el-text type="danger" size="small">{{ config.lastErrorMessage }}</el-text>
              </el-form-item>
            </el-form>

            <div class="config-actions">
              <el-button type="primary" @click="saveConfig(config)" :loading="config.saving">
                保存配置
              </el-button>
              <el-button 
                type="success" 
                @click="triggerCrawler(config.platform)"
                :disabled="!config.enabled || config.isRunning"
                :loading="config.triggering"
              >
                {{ config.isRunning ? '运行中...' : '手动执行' }}
              </el-button>
              <el-button 
                type="warning" 
                @click="testCrawler(config.platform)"
                :loading="config.testing"
              >
                测试爬虫
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="testDialogVisible" :title="`测试结果 - ${testResult.platform}`" width="800px">
      <div v-if="testResult.loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在测试爬虫...</p>
      </div>
      <div v-else>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="平台">{{ testResult.platform }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ testResult.tag }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="testResult.success ? 'success' : 'danger'">
              {{ testResult.success ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数量">{{ testResult.count || 0 }} 本</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="testResult.error" style="margin-top: 16px;">
          <el-alert type="error" :title="testResult.error" show-icon />
        </div>

        <div v-if="testResult.novels && testResult.novels.length > 0" style="margin-top: 16px;">
          <h4>小说列表 (前10本)</h4>
          <el-table :data="testResult.novels" border stripe max-height="400">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="novelId" label="ID" width="100" />
            <el-table-column prop="latestChapter" label="最新章节" min-width="150" />
            <el-table-column prop="updateTime" label="更新时间" width="160" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { crawlerApi } from '../api'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const loading = ref(false)
const triggering = ref(false)
const configs = ref([])
const newTags = reactive({})
const testDialogVisible = ref(false)
const testResult = ref({})

const getPlatformName = (platform) => {
  const names = {
    ciweimao: '刺猬猫',
    sf: 'SF轻小说',
    ciyuanji: '次元姬',
    qidian: '起点'
  }
  return names[platform] || platform
}

const formatInterval = (seconds) => {
  if (seconds < 3600) {
    return `${Math.floor(seconds / 60)} 分钟`
  } else if (seconds < 86400) {
    return `${Math.floor(seconds / 3600)} 小时`
  } else {
    return `${Math.floor(seconds / 86400)} 天`
  }
}

const parseTags = (tagsJson) => {
  if (!tagsJson) return []
  try {
    return JSON.parse(tagsJson)
  } catch {
    return []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await crawlerApi.getConfigs()
    configs.value = (data || []).map(c => ({
      ...c,
      saving: false,
      triggering: false,
      testing: false,
      isRunning: false
    }))
    
    for (const config of configs.value) {
      try {
        const status = await crawlerApi.getStatus(config.platform)
        config.isRunning = status.isRunning
        config.lastCrawlTime = status.lastCrawlTime
        config.lastSuccessCrawlTime = status.lastSuccessCrawlTime
        config.crawlCount = status.crawlCount
        config.failCount = status.failCount
        config.lastErrorMessage = status.lastError
      } catch (e) {
        console.error(`获取 ${config.platform} 状态失败:`, e)
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

const handleEnabledChange = (config) => {
  ElMessage.info(`${getPlatformName(config.platform)} ${config.enabled ? '已启用' : '已禁用'}`)
}

const addTag = (config) => {
  const tag = newTags[config.platform]?.trim()
  if (!tag) return
  
  const tags = parseTags(config.tags)
  if (!tags.includes(tag)) {
    tags.push(tag)
    config.tags = JSON.stringify(tags)
  }
  newTags[config.platform] = ''
}

const removeTag = (config, tag) => {
  const tags = parseTags(config.tags)
  const index = tags.indexOf(tag)
  if (index > -1) {
    tags.splice(index, 1)
    config.tags = JSON.stringify(tags)
  }
}

const saveConfig = async (config) => {
  config.saving = true
  try {
    await crawlerApi.updateConfig(config.id, {
      enabled: config.enabled,
      tags: config.tags,
      crawlInterval: config.crawlInterval
    })
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
  } finally {
    config.saving = false
  }
}

const triggerAll = async () => {
  triggering.value = true
  try {
    await crawlerApi.trigger()
    ElMessage.success('已启动全部爬虫任务')
    setTimeout(loadData, 2000)
  } catch (error) {
    console.error('启动爬虫失败:', error)
  } finally {
    triggering.value = false
  }
}

const triggerCrawler = async (platform) => {
  const config = configs.value.find(c => c.platform === platform)
  if (config) {
    config.triggering = true
  }
  try {
    const result = await crawlerApi.triggerPlatform(platform)
    if (result.success) {
      ElMessage.success(result.message)
      config.isRunning = true
      setTimeout(() => loadStatus(platform), 3000)
    } else {
      ElMessage.warning(result.message)
    }
  } catch (error) {
    console.error('触发爬虫失败:', error)
  } finally {
    if (config) {
      config.triggering = false
    }
  }
}

const loadStatus = async (platform) => {
  const config = configs.value.find(c => c.platform === platform)
  if (config) {
    try {
      const status = await crawlerApi.getStatus(platform)
      config.isRunning = status.isRunning
      config.lastCrawlTime = status.lastCrawlTime
      config.lastSuccessCrawlTime = status.lastSuccessCrawlTime
      config.crawlCount = status.crawlCount
      config.failCount = status.failCount
      config.lastErrorMessage = status.lastError
      
      if (status.isRunning) {
        setTimeout(() => loadStatus(platform), 3000)
      }
    } catch (e) {
      console.error(`获取 ${platform} 状态失败:`, e)
    }
  }
}

const testCrawler = async (platform) => {
  const config = configs.value.find(c => c.platform === platform)
  if (config) {
    config.testing = true
  }
  
  testResult.value = {
    loading: true,
    platform: getPlatformName(platform)
  }
  testDialogVisible.value = true
  
  try {
    const tags = parseTags(config?.tags)
    const tag = tags.length > 0 ? tags[0] : '动漫穿越'
    
    const result = await crawlerApi.testCrawler(platform, tag)
    testResult.value = {
      ...result,
      loading: false
    }
  } catch (error) {
    testResult.value = {
      success: false,
      loading: false,
      platform: getPlatformName(platform),
      error: error.message || '测试失败'
    }
  } finally {
    if (config) {
      config.testing = false
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.platforms {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-card {
  margin-bottom: 20px;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.platform-name {
  font-size: 16px;
  font-weight: 500;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.config-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
