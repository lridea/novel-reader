import axios from 'axios'
import { ElMessage } from 'element-plus'
import mockApi from '../mock'

// 使用Mock数据开关（开发时设为true，生产时设为false）
const USE_MOCK = true

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    const message = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export const crawlerApi = {
  health() {
    return USE_MOCK ? mockApi.health() : api.get('/crawler/health')
  },

  trigger() {
    return USE_MOCK ? mockApi.trigger() : api.post('/crawler/trigger')
  },

  triggerPlatform(platform) {
    return USE_MOCK ? mockApi.triggerPlatform(platform) : api.post(`/crawler/trigger/${platform}`)
  },

  testCrawler(platform, tag) {
    return USE_MOCK ? mockApi.testCrawler(platform, tag) : api.get(`/crawler/test/${platform}`, { params: { tag } })
  },

  getStatus(platform) {
    return USE_MOCK ? mockApi.getStatus(platform) : api.get(`/crawler/status/${platform}`)
  },

  getNovels(params) {
    return USE_MOCK ? mockApi.getNovels(params) : api.get('/crawler/novels/page', { params })
  },

  getNovelsByPlatform(platform) {
    return USE_MOCK ? mockApi.getNovelsByPlatform(platform) : api.get(`/crawler/novels/platform/${platform}`)
  },

  getConfigs() {
    return USE_MOCK ? mockApi.getConfigs() : api.get('/crawler/configs')
  },

  updateConfig(id, data) {
    return USE_MOCK ? mockApi.updateConfig(id, data) : api.put(`/crawler/configs/${id}`, data)
  },

  getCrawlers() {
    return USE_MOCK ? mockApi.getCrawlers() : api.get('/crawler/crawlers')
  },

  // 任务相关API
  getTasks(params) {
    return USE_MOCK ? mockApi.getTasks(params) : api.get('/crawler/tasks', { params })
  },

  getTask(id) {
    return USE_MOCK ? mockApi.getTask(id) : api.get(`/crawler/tasks/${id}`)
  }
}

export default api
