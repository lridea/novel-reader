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

  // 标签相关API
  getTags() {
    return USE_MOCK ? mockApi.getTags() : api.get('/crawler/novels/tags')
  },

  getTagsByPlatform(platform) {
    return USE_MOCK ? mockApi.getTagsByPlatform(platform) : api.get(`/crawler/novels/tags/platform/${platform}`)
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
  },

  // 认证相关API
  register(data) {
    return USE_MOCK ? mockApi.register(data) : api.post('/auth/register', data)
  },

  login(data) {
    return USE_MOCK ? mockApi.login(data) : api.post('/auth/login', data)
  },

  getCurrentUser() {
    return USE_MOCK ? mockApi.getCurrentUser() : api.get('/auth/me')
  },

  logout() {
    return USE_MOCK ? mockApi.logout() : api.post('/auth/logout')
  },

  // 用户相关API
  updateUserProfile(data) {
    return USE_MOCK ? mockApi.updateUserProfile(data) : api.put('/users/profile', data)
  },

  changePassword(data) {
    return USE_MOCK ? mockApi.changePassword(data) : api.put('/users/password', data)
  },

  // 收藏相关API
  addFavorite(data) {
    return USE_MOCK ? mockApi.addFavorite(data) : api.post('/favorites', data)
  },

  removeFavorite(novelId) {
    return USE_MOCK ? mockApi.removeFavorite(novelId) : api.delete(`/favorites/${novelId}`)
  },

  getFavorites(params) {
    return USE_MOCK ? mockApi.getFavorites(params) : api.get('/favorites', { params })
  },

  updateFavoriteNote(novelId, note) {
    return USE_MOCK ? mockApi.updateFavoriteNote(novelId, note) : api.put(`/favorites/${novelId}/note`, { note })
  },

  // 分类相关API
  createCategory(data) {
    return USE_MOCK ? mockApi.createCategory(data) : api.post('/categories', data)
  },

  getCategories() {
    return USE_MOCK ? mockApi.getCategories() : api.get('/categories')
  },

  updateCategory(id, data) {
    return USE_MOCK ? mockApi.updateCategory(id, data) : api.put(`/categories/${id}`, data)
  },

  deleteCategory(id) {
    return USE_MOCK ? mockApi.deleteCategory(id) : api.delete(`/categories/${id}`)
  }
}

export default api
