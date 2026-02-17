import axios from 'axios'
import { ElMessage } from 'element-plus'

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
    return api.get('/crawler/health')
  },

  trigger() {
    return api.post('/crawler/trigger')
  },

  triggerPlatform(platform) {
    return api.post(`/crawler/trigger/${platform}`)
  },

  testCrawler(platform, tag) {
    return api.get(`/crawler/test/${platform}`, { params: { tag } })
  },

  getStatus(platform) {
    return api.get(`/crawler/status/${platform}`)
  },

  getNovels(params) {
    return api.get('/crawler/novels', { params })
  },

  getNovelsByPlatform(platform) {
    return api.get(`/crawler/novels/platform/${platform}`)
  },

  getConfigs() {
    return api.get('/crawler/configs')
  },

  updateConfig(id, data) {
    return api.put(`/crawler/configs/${id}`, data)
  },

  getCrawlers() {
    return api.get('/crawler/crawlers')
  }
}

export default api
