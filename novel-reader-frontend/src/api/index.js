import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 使用Mock数据开关（开发时设为true，生产时设为false）
const USE_MOCK = false

// 条件导入 mockApi
let mockApi = null
if (USE_MOCK) {
  try {
    mockApi = require('../mock').mockApi
  } catch (e) {
    console.warn('Mock module not available')
  }
}

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器：添加Token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理错误
api.interceptors.response.use(
  response => response.data,
  error => {
    // 处理401错误（未登录）
    if (error.response && error.response.status === 401) {
      ElMessage.error('请先登录')
      // 跳转到登录页面
      router.push('/login')
      return Promise.reject(error)
    }

    // 处理403错误（权限不足）
    if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足，仅管理员可访问')
      return Promise.reject(error)
    }

    // 处理其他错误
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

  getNovelsPublic(params) {
    return USE_MOCK ? mockApi.getNovels(params) : api.get('/novels/page', { params })
  },

  getNovelById(id) {
    return USE_MOCK ? mockApi.getNovel(id) : api.get(`/novels/${id}`)
  },

  getTags() {
    return USE_MOCK ? mockApi.getTags() : api.get('/crawler/novels/tags')
  },

  getTagsPublic() {
    return USE_MOCK ? mockApi.getTags() : api.get('/novels/tags')
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

  checkBatchFavorites(novelIds) {
    return USE_MOCK ? mockApi.checkBatchFavorites(novelIds) : api.get(`/favorites/check-batch`, { params: { novelIds } })
  },

  // 评论相关API
  getComments(novelId, params) {
    return USE_MOCK ? mockApi.getComments(novelId, params) : api.get(`/comments/novel/${novelId}`, { params })
  },

  addComment(data) {
    return USE_MOCK ? mockApi.addComment(data) : api.post('/comments', data)
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
  },

  // 评论点赞相关API
  likeComment(commentId) {
    return USE_MOCK ? mockApi.likeComment(commentId) : api.post(`/comments/${commentId}/like`)
  },

  unlikeComment(commentId) {
    return USE_MOCK ? mockApi.unlikeComment(commentId) : api.delete(`/comments/${commentId}/like`)
  },

  // 敏感词管理API
  getSensitiveWords(params) {
    return USE_MOCK ? mockApi.getSensitiveWords(params) : api.get('/sensitive-words', { params })
  },

  addSensitiveWord(data) {
    return USE_MOCK ? mockApi.addSensitiveWord(data) : api.post('/sensitive-words', data)
  },

  updateSensitiveWord(id, data) {
    return USE_MOCK ? mockApi.updateSensitiveWord(id, data) : api.put(`/sensitive-words/${id}`, data)
  },

  deleteSensitiveWord(id) {
    return USE_MOCK ? mockApi.deleteSensitiveWord(id) : api.delete(`/sensitive-words/${id}`)
  },

  batchDeleteSensitiveWords(ids) {
    return USE_MOCK ? mockApi.batchDeleteSensitiveWords(ids) : api.delete('/sensitive-words/batch', { data: { ids } })
  },

  importSensitiveWords(words) {
    return USE_MOCK ? mockApi.importSensitiveWords(words) : api.post('/sensitive-words/import', { words })
  },

  // 书籍点踩相关API
  dislikeNovel(novelId) {
    return USE_MOCK ? mockApi.dislikeNovel(novelId) : api.post(`/novels/${novelId}/dislike`)
  },

  undislikeNovel(novelId) {
    return USE_MOCK ? mockApi.undislikeNovel(novelId) : api.delete(`/novels/${novelId}/dislike`)
  },

  // 书籍管理API（管理员）
  searchNovels(params) {
    return USE_MOCK ? mockApi.searchNovels(params) : api.get('/novels/admin/search', { params })
  },

  batchDeleteNovels(ids) {
    return USE_MOCK ? mockApi.batchDeleteNovels(ids) : api.delete('/novels/batch', { data: { ids } })
  },

  // 用户标签API
  addTag(data) {
    return USE_MOCK ? mockApi.addTag(data) : api.post('/tags/user', data)
  },

  getMyTags(params) {
    return USE_MOCK ? mockApi.getMyTags(params) : api.get('/tags/user/my', { params })
  },

  // 标签审核API（管理员）
  getPendingAudits(params) {
    return USE_MOCK ? mockApi.getPendingAudits(params) : api.get('/tags/admin/audits', { params })
  },

  auditTag(id, data) {
    return USE_MOCK ? mockApi.auditTag(id, data) : api.put(`/tags/admin/audits/${id}`, data)
  },

  batchAuditTags(data) {
    return USE_MOCK ? mockApi.batchAuditTags(data) : api.put('/tags/admin/audits/batch', data)
  },

  // 获取所有用户标签
  getAllUserTags() {
    return USE_MOCK ? mockApi.getAllUserTags() : api.get('/tags/user-tags')
  },

  // 删除标签（用户）
  deleteTag(data) {
    return USE_MOCK ? mockApi.deleteTag(data) : api.delete('/tags/user', { data })
  }
}

export const favoriteApi = {
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

  checkBatchFavorites(novelIds) {
    return USE_MOCK ? mockApi.checkBatchFavorites(novelIds) : api.get(`/favorites/check-batch`, { params: { novelIds } })
  }
}

export default api
