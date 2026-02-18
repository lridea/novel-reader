// Mock API 数据
const MOCK_TAGS = [
  { tag_name: '玄幻', tag_count: 1250 },
  { tag_name: '修仙', tag_count: 980 },
  { tag_name: '穿越', tag_count: 856 },
  { tag_name: '都市', tag_count: 723 },
  { tag_name: '系统', tag_count: 654 },
  { tag_name: '重生', tag_count: 521 },
  { tag_name: '同人', tag_count: 498 },
  { tag_name: '动漫', tag_count: 445 },
  { tag_name: '二次元', tag_count: 389 },
  { tag_name: '神医', tag_count: 312 }
]

const MOCK_NOVELS = [
  {
    id: 1,
    platform: 'ciweimao',
    novelId: '1001',
    title: '修仙从系统开始',
    author: '逍遥子',
    description: '一个现代人穿越到修仙世界，获得系统加持的故事。主角林风意外穿越到玄天大陆，觉醒了最强修仙系统，从此踏上了无敌之路。',
    coverUrl: 'https://picsum.photos/150/200?random=1',
    latestChapterTitle: '第200章 飞升仙界',
    tags: '["玄幻", "修仙", "系统"]',
    wordCount: 500000,
    latestUpdateTime: '2026-02-18T08:30:00',
    firstChaptersSummary: '现代人林风穿越到玄天大陆，觉醒了最强修仙系统。第一章中，林风在玄天大陆的青云宗醒来，发现自己穿越了，脑海中响起"叮！修仙系统已激活"。系统赋予了他快速修炼的能力。第二章，林风开始修炼，系统发布第一个任务：在一个月内突破到筑基期。第三章，林风在宗门大比中一鸣惊人，击败了宗门天才，震惊四座。故事节奏紧凑，主角性格鲜明，系统设定有趣，是一本值得阅读的修仙小说。',
    lastCrawlTime: '2026-02-18T09:00:00',
    crawlCount: 15,
    status: 1,
    favoriteCount: 125,
    commentCount: 15,
    deleted: 0,
    createdAt: '2026-02-15T10:00:00',
    updatedAt: '2026-02-18T09:00:00'
  },
  {
    id: 2,
    platform: 'ciweimao',
    novelId: '1002',
    title: '都市之超级神医',
    author: '神医下山',
    description: '山村少年陈凡偶然获得医圣传承，从此悬壶济世，妙手回春。',
    coverUrl: 'https://picsum.photos/150/200?random=2',
    latestChapterTitle: '第150章 医圣传承',
    tags: '["都市", "神医", "系统"]',
    wordCount: 300000,
    latestUpdateTime: '2026-02-18T07:45:00',
    firstChaptersSummary: '山村少年陈凡在山中采药时，意外发现一座古老洞府，获得了上古医圣的传承。第一章讲述陈凡发现洞府并获得医术传承的过程，系统赋予了他透视能力和医学知识。第二章，陈凡回村治疗疑难杂症，一针见效，名扬十里八乡。第三章，城里富豪病重，陈凡受邀前往治疗，以神奇医术震惊名医。本书风格轻松爽快，医术描写细致，主角性格低调但不懦弱，适合都市爽文爱好者阅读。',
    lastCrawlTime: '2026-02-18T08:30:00',
    crawlCount: 12,
    status: 1,
    favoriteCount: 89,
    commentCount: 8,
    deleted: 0,
    createdAt: '2026-02-15T12:00:00',
    updatedAt: '2026-02-18T08:30:00'
  },
  {
    id: 3,
    platform: 'sf',
    novelId: '2001',
    title: '二次元之无限进化',
    author: '二次元之王',
    description: '穿越各个二次元世界，收集能力，不断进化。',
    coverUrl: 'https://picsum.photos/150/200?random=3',
    latestChapterTitle: '第80章 刀剑神域',
    tags: '["二次元", "穿越", "同人"]',
    wordCount: 250000,
    latestUpdateTime: '2026-02-18T09:15:00',
    firstChaptersSummary: '主角叶羽获得二次元系统，可以穿越到各个动漫世界。第一章，叶羽来到第一个世界——《火影忍者》，系统赋予他写轮眼。第二章，叶羽在木叶村学习忍术，与鸣人等人成为朋友。第三章，叶羽参加中忍考试，展现出强大的实力，但引起了暗部的注意。本书节奏明快，世界观设定丰富，主角穿越到多个经典动漫世界，适合二次元爱好者阅读。',
    lastCrawlTime: '2026-02-18T09:30:00',
    crawlCount: 8,
    status: 1,
    favoriteCount: 67,
    commentCount: 5,
    deleted: 0,
    createdAt: '2026-02-16T10:00:00',
    updatedAt: '2026-02-18T09:30:00'
  },
  {
    id: 4,
    platform: 'sf',
    novelId: '2002',
    title: '魔法学院的异类',
    author: '魔法师',
    description: '在一个魔法世界中，一个没有魔法天赋的少年创造了奇迹。',
    coverUrl: 'https://picsum.photos/150/200?random=4',
    latestChapterTitle: '第60章 魔法奇迹',
    tags: '["魔法", "奇幻", "成长"]',
    wordCount: 200000,
    latestUpdateTime: '2026-02-18T06:20:00',
    firstChaptersSummary: '在魔法世界，每个人都有魔法天赋，唯独主角艾伦是"零魔"。第一章，艾伦被魔法学院录取，却被同学嘲笑没有魔法。第二章，艾伦意外发现，虽然他没有魔法，但可以通过科学知识创造魔法般的效果。第三章，艾伦用化学和物理知识制造出"魔法道具"，震惊了所有同学。本书创意新颖，将科学与魔法结合，主角性格坚毅不拔，是一部优秀的奇幻小说。',
    lastCrawlTime: '2026-02-18T07:00:00',
    crawlCount: 6,
    status: 1,
    commentCount: 2,
    deleted: 0,
    createdAt: '2026-02-16T14:00:00',
    updatedAt: '2026-02-18T07:00:00'
  },
  {
    id: 5,
    platform: 'ciyuanji',
    novelId: '3001',
    title: '动漫之最强主角',
    author: '宅神',
    description: '成为各个动漫世界的主角，体验不同的人生。',
    coverUrl: 'https://picsum.photos/150/200?random=5',
    latestChapterTitle: '第45章 海贼王',
    tags: '["动漫", "穿越", "同人"]',
    wordCount: 180000,
    latestUpdateTime: '2026-02-18T08:50:00',
    firstChaptersSummary: '主角王宅获得主角系统，可以成为各个动漫世界的主角。第一章，王宅来到《海贼王》世界，成为路飞的哥哥，同样吃了恶魔果实。第二章，王宅和路飞一起出海，招募伙伴，开始冒险。第三章，王宅在伟大航路上展现出强大的实力，引起了海军的注意。本书轻松有趣，主角性格开朗，与原著角色互动自然，适合海贼王粉丝阅读。',
    lastCrawlTime: '2026-02-18T09:15:00',
    crawlCount: 5,
    status: 1,
    commentCount: 0,
    deleted: 0,
    createdAt: '2026-02-17T10:00:00',
    updatedAt: '2026-02-18T09:15:00'
  },
  {
    id: 6,
    platform: 'ciyuanji',
    novelId: '3002',
    title: '我的动漫人生',
    author: '动漫迷',
    description: '一个动漫迷穿越到动漫世界，开始新的人生。',
    coverUrl: 'https://picsum.photos/150/200?random=6',
    latestChapterTitle: '第30章 青春猪头少年',
    tags: '["动漫", "校园", "恋爱"]',
    wordCount: 150000,
    latestUpdateTime: '2026-02-17T22:30:00',
    firstChaptersSummary: '动漫迷李明穿越到动漫世界，成为了一名高中生。第一章，李明发现自己来到了《青春猪头少年》的世界，遇到了咲太和麻衣。第二章，李明和咲太一起帮助遇到青春期综合征的少女们。第三章，李明逐渐融入这个世界，开始了自己的青春生活。本书风格温馨治愈，描写细腻，主角性格温和，适合喜欢青春恋爱题材的读者。',
    lastCrawlTime: '2026-02-18T08:00:00',
    crawlCount: 4,
    status: 1,
    commentCount: 0,
    deleted: 0,
    createdAt: '2026-02-17T15:00:00',
    updatedAt: '2026-02-18T08:00:00'
  }
]

const MOCK_CONFIGS = [
  {
    id: 1,
    platform: 'ciweimao',
    baseUrl: 'https://mip.ciweimao.com',
    enabled: 1,
    tags: '["玄幻", "修仙", "都市", "系统"]',
    crawlInterval: 7200,
    maxRetry: 3,
    lastCrawlTime: '2026-02-18T09:00:00',
    lastSuccessCrawlTime: '2026-02-18T09:30:00',
    isRunning: 0,
    runningTaskId: null,
    crawlCount: 15,
    failCount: 1,
    lastErrorMessage: null,
    createdAt: '2026-02-15T10:00:00',
    updatedAt: '2026-02-18T10:00:00'
  },
  {
    id: 2,
    platform: 'sf',
    baseUrl: 'https://book.sfacg.com',
    enabled: 1,
    tags: '["动漫", "穿越", "同人", "魔法", "奇幻"]',
    crawlInterval: 7200,
    maxRetry: 3,
    lastCrawlTime: '2026-02-18T07:00:00',
    lastSuccessCrawlTime: '2026-02-18T09:30:00',
    isRunning: 0,
    runningTaskId: null,
    crawlCount: 8,
    failCount: 0,
    lastErrorMessage: null,
    createdAt: '2026-02-15T10:00:00',
    updatedAt: '2026-02-18T10:00:00'
  },
  {
    id: 3,
    platform: 'ciyuanji',
    baseUrl: 'https://www.ciyuanji.com',
    enabled: 1,
    tags: '["二次元", "同人", "动漫", "校园", "恋爱"]',
    crawlInterval: 7200,
    maxRetry: 3,
    lastCrawlTime: '2026-02-18T08:00:00',
    lastSuccessCrawlTime: '2026-02-18T09:15:00',
    isRunning: 0,
    runningTaskId: null,
    crawlCount: 5,
    failCount: 0,
    lastErrorMessage: null,
    createdAt: '2026-02-15T10:00:00',
    updatedAt: '2026-02-18T10:00:00'
  }
]

const MOCK_TASKS = [
  {
    id: 1,
    platform: 'ciweimao',
    taskType: '定时抓取',
    status: 'SUCCESS',
    startTime: '2026-02-18T09:00:00',
    endTime: '2026-02-18T09:30:00',
    totalCount: 500,
    successCount: 480,
    failCount: 20,
    logs: '2026-02-18 09:00:00 [INFO] 开始执行爬虫任务\n2026-02-18 09:00:05 [INFO] 加载平台配置\n2026-02-18 09:00:10 [INFO] 标签：玄幻, 修仙, 都市, 系统\n2026-02-18 09:00:15 [INFO] 解析小说列表...\n2026-02-18 09:00:20 [INFO] 抓取到100本小说\n2026-02-18 09:00:25 [INFO] 保存到数据库...\n2026-02-18 09:00:30 [INFO] 处理小说: 修仙从系统开始\n2026-02-18 09:00:35 [INFO] 首次抓取，获取前3章内容\n2026-02-18 09:00:40 [INFO] 调用AI生成概括...\n2026-02-18 09:00:50 [INFO] AI概括生成成功\n...\n2026-02-18 09:30:00 [INFO] 爬虫任务完成'
  },
  {
    id: 2,
    platform: 'sf',
    taskType: '定时抓取',
    status: 'SUCCESS',
    startTime: '2026-02-18T08:00:00',
    endTime: '2026-02-18T08:25:00',
    totalCount: 400,
    successCount: 390,
    failCount: 10,
    logs: '2026-02-18 08:00:00 [INFO] 开始执行爬虫任务\n...\n2026-02-18 08:25:00 [INFO] 爬虫任务完成'
  },
  {
    id: 3,
    platform: 'ciyuanji',
    taskType: '定时抓取',
    status: 'RUNNING',
    startTime: '2026-02-18T09:15:00',
    endTime: null,
    totalCount: 350,
    successCount: 280,
    failCount: 0,
    logs: '2026-02-18 09:15:00 [INFO] 开始执行爬虫任务\n2026-02-18 09:15:05 [INFO] 加载平台配置\n2026-02-18 09:15:10 [INFO] 标签：二次元, 同人, 动漫\n2026-02-18 09:15:15 [INFO] 解析小说列表...\n2026-02-18 09:15:20 [INFO] 抓取到50本小说\n...'
  },
  {
    id: 4,
    platform: 'ciweimao',
    taskType: '手动执行',
    status: 'FAILED',
    startTime: '2026-02-18T07:00:00',
    endTime: '2026-02-18T07:05:00',
    totalCount: 0,
    successCount: 0,
    failCount: 0,
    logs: '2026-02-18 07:00:00 [INFO] 开始执行爬虫任务\n2026-02-18 07:00:05 [ERROR] 请求超时：无法连接到 ciweimao 服务器\n2026-02-18 07:05:00 [ERROR] 任务失败'
  }
]

const MOCK_FAVORITES = [
  { userId: 1, novelId: 1, category: 'default', note: '', createdAt: '2026-02-18T09:00:00' },
  { userId: 1, novelId: 3, category: 'default', note: '', createdAt: '2026-02-18T09:30:00' },
  { userId: 1, novelId: 5, category: 'default', note: '', createdAt: '2026-02-18T10:00:00' }
]

const MOCK_COMMENTS = [
  {
    id: 1,
    userId: 1,
    novelId: 1,
    parentId: null,
    floor: 1,
    content: '这本书太好看了！主角林风太厉害了！',
    likeCount: 10,
    replyCount: 2,
    deleted: 0,
    createdAt: '2026-02-18T08:00:00',
    updatedAt: '2026-02-18T08:00:00'
  },
  {
    id: 2,
    userId: 2,
    novelId: 1,
    parentId: 1,
    floor: 2,
    content: '同意！系统设定很有趣！',
    likeCount: 5,
    replyCount: 0,
    deleted: 0,
    createdAt: '2026-02-18T08:30:00',
    updatedAt: '2026-02-18T08:30:00'
  },
  {
    id: 3,
    userId: 3,
    novelId: 1,
    parentId: 1,
    floor: 2,
    content: '我也觉得，特别是宗门大比那段！',
    likeCount: 3,
    replyCount: 0,
    deleted: 0,
    createdAt: '2026-02-18T09:00:00',
    updatedAt: '2026-02-18T09:00:00'
  },
  {
    id: 4,
    userId: 2,
    novelId: 1,
    parentId: null,
    floor: 1,
    content: '作者更新太慢了，求快更！',
    likeCount: 2,
    replyCount: 0,
    deleted: 0,
    createdAt: '2026-02-18T09:30:00',
    updatedAt: '2026-02-18T09:30:00'
  }
]

// 模拟延迟
const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

// Mock API 响应
export const mockApi = {
  // 健康检查
  async health() {
    return {
      status: 'ok',
      crawlers: 3,
      timestamp: Date.now()
    }
  },

  // 触发所有爬虫
  async trigger() {
    return {
      success: true,
      message: '爬虫任务已启动'
    }
  },

  // 触发指定平台爬虫
  async triggerPlatform(platform) {
    const config = MOCK_CONFIGS.find(c => c.platform === platform)
    if (!config) {
      return { success: false, message: `平台 ${platform} 配置不存在` }
    }
    if (config.enabled !== 1) {
      return { success: false, message: `平台 ${platform} 已禁用` }
    }
    return { success: true, message: `平台 ${platform} 爬虫任务已启动` }
  },

  // 测试爬虫
  async testCrawler(platform, tag = '动漫穿越') {
    const novels = MOCK_NOVELS.filter(n => n.platform === platform).slice(0, 3)
    return {
      success: true,
      platform,
      tag,
      count: novels.length,
      novels: novels.map(n => ({
        novelId: n.novelId,
        title: n.title,
        author: n.author,
        coverUrl: n.coverUrl,
        latestChapter: n.latestChapterTitle,
        updateTime: n.latestUpdateTime
      }))
    }
  },

  // 获取平台状态
  async getStatus(platform) {
    const config = MOCK_CONFIGS.find(c => c.platform === platform)
    if (!config) {
      return { exists: false }
    }
    return {
      exists: true,
      platform: config.platform,
      enabled: config.enabled,
      isRunning: config.isRunning === 1,
      lastCrawlTime: config.lastCrawlTime,
      lastSuccessCrawlTime: config.lastSuccessCrawlTime,
      crawlCount: config.crawlCount,
      failCount: config.failCount,
      lastError: config.lastErrorMessage
    }
  },

  // 获取所有小说（分页）
  async getNovels(params = {}) {
    const { page = 0, size = 10, platform: filterPlatform, keyword } = params

    let novels = [...MOCK_NOVELS]

    // 平台过滤
    if (filterPlatform) {
      novels = novels.filter(n => n.platform === filterPlatform)
    }

    // 关键词搜索
    if (keyword) {
      const lowerKeyword = keyword.toLowerCase()
      novels = novels.filter(n =>
        n.title.toLowerCase().includes(lowerKeyword) ||
        n.author.toLowerCase().includes(lowerKeyword)
      )
    }

    const total = novels.length
    const start = page * size
    const end = start + size
    const content = novels.slice(start, end)

    return {
      content,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  // 获取所有标签
  async getTags() {
    await delay()
    return {
      success: true,
      tags: MOCK_TAGS
    }
  },

  // 获取指定平台的标签
  async getTagsByPlatform(platform) {
    await delay()
    // 简化处理：返回所有标签（实际应该根据平台筛选）
    return {
      success: true,
      platform,
      tags: MOCK_TAGS.slice(0, 5)
    }
  },

  // 获取所有配置
  async getConfigs() {
    return MOCK_CONFIGS
  },

  // 更新配置
  async updateConfig(id, data) {
    await delay()
    const config = MOCK_CONFIGS.find(c => c.id === id)
    if (!config) {
      throw new Error('配置不存在')
    }
    Object.assign(config, data, { updatedAt: new Date().toISOString() })
    return config
  },

  // 获取所有爬虫
  async getCrawlers() {
    return {
      ciweimao: 'CiweimaoCrawler',
      sf: 'SfCrawler',
      ciyuanji: 'CiyuanjiCrawler'
    }
  },

  // 获取任务列表（新增）
  async getTasks(params = {}) {
    const { page = 0, size = 10 } = params
    const total = MOCK_TASKS.length
    const start = page * size
    const end = start + size
    const content = MOCK_TASKS.slice(start, end)

    return {
      content,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  // 获取任务详情（新增）
  async getTask(id) {
    return MOCK_TASKS.find(t => t.id === id)
  },

  // 收藏相关API
  async addFavorite(data) {
    await delay()
    const { novelId, note = '' } = data
    
    // 检查是否已收藏
    if (MOCK_FAVORITES.some(f => f.novelId === novelId)) {
      throw new Error('已收藏该小说')
    }
    
    // 添加收藏
    const favorite = {
      userId: 1,
      novelId,
      category: 'default',
      note,
      createdAt: new Date().toISOString()
    }
    MOCK_FAVORITES.push(favorite)
    
    // 更新小说的收藏数
    const novel = MOCK_NOVELS.find(n => n.id === novelId)
    if (novel) {
      novel.favoriteCount = (novel.favoriteCount || 0) + 1
    }
    
    return {
      success: true,
      message: '收藏成功',
      favorite,
      novelFavoriteCount: novel?.favoriteCount || 0
    }
  },

  async removeFavorite(novelId) {
    await delay()
    
    // 删除收藏
    const index = MOCK_FAVORITES.findIndex(f => f.novelId === novelId)
    if (index === -1) {
      throw new Error('未收藏该小说')
    }
    MOCK_FAVORITES.splice(index, 1)
    
    // 更新小说的收藏数
    const novel = MOCK_NOVELS.find(n => n.id === novelId)
    if (novel && novel.favoriteCount > 0) {
      novel.favoriteCount = novel.favoriteCount - 1
    }
    
    return {
      success: true,
      message: '取消成功',
      novelFavoriteCount: novel?.favoriteCount || 0
    }
  },

  async getFavorites(params = {}) {
    await delay()
    const { page = 0, size = 10 } = params
    
    // 过滤当前用户的收藏
    const userFavorites = MOCK_FAVORITES.filter(f => f.userId === 1)
    const total = userFavorites.length
    const start = page * size
    const end = start + size
    const content = userFavorites.slice(start, end)
    
    // 联合小说信息
    const favorites = content.map(f => {
      const novel = MOCK_NOVELS.find(n => n.id === f.novelId)
      return {
        ...f,
        novel: novel ? {
          title: novel.title,
          author: novel.author,
          coverUrl: novel.coverUrl,
          description: novel.description
        } : null
      }
    })
    
    return {
      content: favorites,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  async updateFavoriteNote(novelId, note) {
    await delay()
    
    const favorite = MOCK_FAVORITES.find(f => f.novelId === novelId)
    if (!favorite) {
      throw new Error('未收藏该小说')
    }
    favorite.note = note
    
    return {
      success: true,
      message: '更新成功',
      favorite
    }
  },

  async checkBatchFavorites(novelIds) {
    await delay()

    const novelIdList = novelIds.split(',').map(id => parseInt(id.trim()))
    const favorites = MOCK_FAVORITES.filter(f => f.userId === 1 && novelIdList.includes(f.novelId))

    const favoritesMap = {}
    novelIdList.forEach(id => {
      favoritesMap[id] = favorites.some(f => f.novelId === id)
    })

    return {
      success: true,
      favorites: favoritesMap
    }
  },

  // 评论相关API
  async getComments(novelId, params = {}) {
    await delay()
    const { page = 0, size = 10, floor, parentId } = params

    let comments = MOCK_COMMENTS.filter(c => c.novelId === novelId && c.deleted === 0)

    // 楼层过滤
    if (floor === 2 && parentId) {
      comments = comments.filter(c => c.parentId === parentId)
    } else {
      comments = comments.filter(c => c.floor === 1)
    }

    // 按点赞数降序排序（顶层评论）
    if (floor !== 2) {
      comments.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
    }

    const total = comments.length
    const start = page * size
    const end = start + size
    const content = comments.slice(start, end)

    // 转换为DTO格式
    const MOCK_COMMENT_LIKES = global.MOCK_COMMENT_LIKES || []
    const contentDtos = content.map(c => {
      // 计算liked状态
      const liked = MOCK_COMMENT_LIKES.some(l => l.userId === 1 && l.commentId === c.id)

      const dto = {
        id: c.id,
        userId: c.userId,
        novelId: c.novelId,
        parentId: c.parentId,
        floor: c.floor,
        content: c.content,
        likeCount: c.likeCount,
        replyCount: c.replyCount,
        createdAt: c.createdAt,
        updatedAt: c.updatedAt,
        liked,
        isOwner: c.userId === 1,
        user: {
          id: c.userId,
          username: `user${c.userId}`,
          nickname: `用户${c.userId}`,
          avatar: `https://picsum.photos/100?random=${c.userId}`
        }
      }

      // 如果是回复，添加父评论用户信息
      if (c.parentId) {
        const parent = MOCK_COMMENTS.find(p => p.id === c.parentId)
        if (parent) {
          dto.parentUser = {
            id: parent.userId,
            username: `user${parent.userId}`,
            nickname: `用户${parent.userId}`,
            avatar: `https://picsum.photos/100?random=${parent.userId}`
          }
        }
      }

      return dto
    })

    return {
      content: contentDtos,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  async addComment(data) {
    await delay()
    const { novelId, parentId, floor, content } = data

    // 检查小说是否存在
    const novel = MOCK_NOVELS.find(n => n.id === novelId)
    if (!novel) {
      throw new Error('小说不存在')
    }

    // 创建评论
    const comment = {
      id: MOCK_COMMENTS.length + 1,
      userId: 1,
      novelId,
      parentId,
      floor,
      content,
      likeCount: 0,
      replyCount: 0,
      deleted: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }

    MOCK_COMMENTS.unshift(comment)

    // 更新小说的评论数
    novel.commentCount = (novel.commentCount || 0) + 1

    // 如果是回复，更新父评论的回复数
    if (floor === 2 && parentId) {
      const parent = MOCK_COMMENTS.find(c => c.id === parentId)
      if (parent) {
        parent.replyCount = (parent.replyCount || 0) + 1
      }
    }

    const commentDto = {
      id: comment.id,
      userId: comment.userId,
      novelId: comment.novelId,
      parentId: comment.parentId,
      floor: comment.floor,
      content: comment.content,
      likeCount: comment.likeCount,
      replyCount: comment.replyCount,
      createdAt: comment.createdAt,
      updatedAt: comment.updatedAt,
      liked: false,
      isOwner: true,
      user: {
        id: 1,
        username: 'user1',
        nickname: '用户1',
        avatar: 'https://picsum.photos/100?random=1'
      }
    }

    return {
      success: true,
      message: '评论成功',
      comment: commentDto,
      novelCommentCount: novel.commentCount || 0
    }
  },

  // 点赞评论
  async likeComment(commentId) {
    await delay()

    const comment = MOCK_COMMENTS.find(c => c.id === commentId)
    if (!comment) {
      throw new Error('评论不存在')
    }

    // 检查是否已点赞
    const MOCK_COMMENT_LIKES = MOCK_COMMENT_LIKES || []
    if (MOCK_COMMENT_LIKES.some(l => l.userId === 1 && l.commentId === commentId)) {
      return {
        success: false,
        message: '已点赞该评论',
        liked: true,
        commentLikeCount: comment.likeCount
      }
    }

    // 添加点赞记录
    MOCK_COMMENT_LIKES.push({
      userId: 1,
      commentId,
      createdAt: new Date().toISOString()
    })

    // 更新评论的点赞数
    comment.likeCount = (comment.likeCount || 0) + 1

    return {
      success: true,
      message: '点赞成功',
      liked: true,
      commentLikeCount: comment.likeCount
    }
  },

  // 取消点赞评论
  async unlikeComment(commentId) {
    await delay()

    const comment = MOCK_COMMENTS.find(c => c.id === commentId)
    if (!comment) {
      throw new Error('评论不存在')
    }

    // 检查是否已点赞
    const MOCK_COMMENT_LIKES = MOCK_COMMENT_LIKES || []
    const likeIndex = MOCK_COMMENT_LIKES.findIndex(l => l.userId === 1 && l.commentId === commentId)
    if (likeIndex === -1) {
      return {
        success: false,
        message: '未点赞该评论',
        liked: false,
        commentLikeCount: comment.likeCount
      }
    }

    // 删除点赞记录
    MOCK_COMMENT_LIKES.splice(likeIndex, 1)

    // 更新评论的点赞数
    if (comment.likeCount > 0) {
      comment.likeCount = comment.likeCount - 1
    }

    return {
      success: true,
      message: '取消点赞成功',
      liked: false,
      commentLikeCount: comment.likeCount
    }
  },

  // 敏感词管理API
  async getSensitiveWords(params) {
    await delay()
    const { page = 0, size = 10, category, enabled } = params

    let words = MOCK_SENSITIVE_WORDS || []

    // 过滤分类
    if (category) {
      words = words.filter(w => w.category === category)
    }

    // 过滤状态
    if (enabled !== undefined && enabled !== null) {
      words = words.filter(w => w.enabled === enabled)
    }

    const total = words.length
    const start = page * size
    const end = start + size
    const content = words.slice(start, end)

    return {
      success: true,
      content,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  async addSensitiveWord(data) {
    await delay()
    const { word, category, severity = 1 } = data

    // 检查是否已存在
    if (MOCK_SENSITIVE_WORDS && MOCK_SENSITIVE_WORDS.some(w => w.word === word)) {
      throw new Error('敏感词已存在')
    }

    const newWord = {
      id: Date.now(),
      word,
      category,
      severity,
      enabled: 1,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }

    if (!MOCK_SENSITIVE_WORDS) {
      global.MOCK_SENSITIVE_WORDS = []
    }
    global.MOCK_SENSITIVE_WORDS.push(newWord)

    return {
      success: true,
      message: '添加成功',
      sensitiveWord: newWord
    }
  },

  async updateSensitiveWord(id, data) {
    await delay()
    const word = MOCK_SENSITIVE_WORDS.find(w => w.id === id)
    if (!word) {
      throw new Error('敏感词不存在')
    }

    // 更新字段
    if (data.word) word.word = data.word
    if (data.category) word.category = data.category
    if (data.severity) word.severity = data.severity
    if (data.enabled !== undefined && data.enabled !== null) word.enabled = data.enabled

    word.updatedAt = new Date().toISOString()

    return {
      success: true,
      message: '更新成功',
      sensitiveWord: word
    }
  },

  async deleteSensitiveWord(id) {
    await delay()
    const index = MOCK_SENSITIVE_WORDS.findIndex(w => w.id === id)
    if (index === -1) {
      throw new Error('敏感词不存在')
    }

    MOCK_SENSITIVE_WORDS.splice(index, 1)

    return {
      success: true,
      message: '删除成功'
    }
  },

  async batchDeleteSensitiveWords(ids) {
    await delay()
    let deleteCount = 0

    for (const id of ids) {
      const index = MOCK_SENSITIVE_WORDS.findIndex(w => w.id === id)
      if (index !== -1) {
        MOCK_SENSITIVE_WORDS.splice(index, 1)
        deleteCount++
      }
    }

    return {
      success: true,
      message: '批量删除成功',
      deleteCount
    }
  },

  async importSensitiveWords(words) {
    await delay()
    let successCount = 0
    let skipCount = 0

    if (!MOCK_SENSITIVE_WORDS) {
      global.MOCK_SENSITIVE_WORDS = []
    }

    for (const word of words) {
      const trimmedWord = word.trim()
      if (!trimmedWord) continue

      // 检查是否已存在
      if (MOCK_SENSITIVE_WORDS.some(w => w.word === trimmedWord)) {
        skipCount++
        continue
      }

      const newWord = {
        id: Date.now() + Math.random(),
        word: trimmedWord,
        category: '其他',
        severity: 1,
        enabled: 1,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }

      MOCK_SENSITIVE_WORDS.push(newWord)
      successCount++
    }

    return {
      success: true,
      message: '导入成功',
      successCount,
      skipCount
    }
  },

  // 书籍点踩相关API
  async dislikeNovel(novelId) {
    await delay()

    // 初始化Mock点踩数据
    if (!global.MOCK_DISLIKES) {
      global.MOCK_DISLIKES = []
    }
    const MOCK_DISLIKES = global.MOCK_DISLIKES

    // 检查书籍是否存在
    const novel = MOCK_NOVELS.find(n => n.id === novelId)
    if (!novel) {
      throw new Error('书籍不存在')
    }

    // 检查是否已点踩
    if (MOCK_DISLIKES.some(d => d.novelId === novelId)) {
      return {
        success: false,
        message: '已点踩该书籍',
        disliked: true,
        dislikeCount: novel.dislikeCount || 0
      }
    }

    // 创建点踩记录
    const dislike = {
      id: Date.now(),
      userId: 1,
      novelId,
      createdAt: new Date().toISOString()
    }
    MOCK_DISLIKES.push(dislike)

    // 更新书籍的点踩数
    if (!novel.dislikeCount) {
      novel.dislikeCount = 0
    }
    novel.dislikeCount++

    return {
      success: true,
      message: '点踩成功',
      disliked: true,
      dislikeCount: novel.dislikeCount
    }
  },

  async undislikeNovel(novelId) {
    await delay()

    // 初始化Mock点踩数据
    if (!global.MOCK_DISLIKES) {
      global.MOCK_DISLIKES = []
    }
    const MOCK_DISLIKES = global.MOCK_DISLIKES

    // 检查书籍是否存在
    const novel = MOCK_NOVELS.find(n => n.id === novelId)
    if (!novel) {
      throw new Error('书籍不存在')
    }

    // 检查是否已点踩
    const dislikeIndex = MOCK_DISLIKES.findIndex(d => d.novelId === novelId)
    if (dislikeIndex === -1) {
      return {
        success: false,
        message: '未点踩该书籍',
        disliked: false,
        dislikeCount: novel.dislikeCount || 0
      }
    }

    // 删除点踩记录
    MOCK_DISLIKES.splice(dislikeIndex, 1)

    // 更新书籍的点踩数
    if (novel.dislikeCount > 0) {
      novel.dislikeCount--
    }

    return {
      success: true,
      message: '取消点踩成功',
      disliked: false,
      dislikeCount: novel.dislikeCount
    }
  },

  // 书籍管理API（管理员）
  async searchNovels(params) {
    await delay()
    const { page = 0, size = 10, keyword, minDislikeCount } = params

    let novels = MOCK_NOVELS || []

    // 根据关键词搜索
    if (keyword && keyword.trim()) {
      const keywordLower = keyword.toLowerCase()
      novels = novels.filter(novel =>
        (novel.title && novel.title.toLowerCase().includes(keywordLower)) ||
        (novel.author && novel.author.toLowerCase().includes(keywordLower))
      )
    }

    // 根据最小点踩数过滤
    if (minDislikeCount && minDislikeCount > 0) {
      novels = novels.filter(novel => (novel.dislikeCount || 0) >= minDislikeCount)
    }

    // 分页
    const total = novels.length
    const start = page * size
    const end = start + size
    const content = novels.slice(start, end)

    return {
      success: true,
      content,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  async batchDeleteNovels(ids) {
    await delay()
    let deleteCount = 0

    if (!global.MOCK_NOVELS) {
      global.MOCK_NOVELS = []
    }
    const MOCK_NOVELS = global.MOCK_NOVELS

    for (const id of ids) {
      const index = MOCK_NOVELS.findIndex(n => n.id === id)
      if (index !== -1) {
        MOCK_NOVELS.splice(index, 1)
        deleteCount++
      }
    }

    return {
      success: true,
      message: '批量删除成功',
      deleteCount
    }
  }
}

// 初始化Mock敏感词数据
global.MOCK_SENSITIVE_WORDS = global.MOCK_SENSITIVE_WORDS || [
  { id: 1, word: '暴力', category: '暴力', severity: 2, enabled: 1, createdAt: '2026-02-18T08:00:00', updatedAt: '2026-02-18T08:00:00' },
  { id: 2, word: '色情', category: '色情', severity: 3, enabled: 1, createdAt: '2026-02-18T08:00:00', updatedAt: '2026-02-18T08:00:00' },
  { id: 3, word: '毒品', category: '违法', severity: 3, enabled: 1, createdAt: '2026-02-18T08:00:00', updatedAt: '2026-02-18T08:00:00' },
  { id: 4, word: '赌博', category: '违法', severity: 3, enabled: 1, createdAt: '2026-02-18T08:00:00', updatedAt: '2026-02-18T08:00:00' },
  { id: 5, word: '诈骗', category: '违法', severity: 3, enabled: 1, createdAt: '2026-02-18T08:00:00', updatedAt: '2026-02-18T08:00:00' }
]

const MOCK_SENSITIVE_WORDS = global.MOCK_SENSITIVE_WORDS

export default mockApi

  // 用户标签API
  async addTag(data) {
    await delay()
    const { novelId, tag } = data

    // 初始化Mock标签审核数据
    if (!global.MOCK_TAG_AUDITS) {
      global.MOCK_TAG_AUDITS = []
    }
    const MOCK_TAG_AUDITS = global.MOCK_TAG_AUDITS

    // 检查书籍是否存在
    const novel = MOCK_NOVELS.find(n => n.id === novelId)
    if (!novel) {
      throw new Error('书籍不存在')
    }

    // 检查是否已添加相同标签
    if (MOCK_TAG_AUDITS.some(a => a.novelId === novelId && a.tag === tag)) {
      return {
        success: false,
        message: '已添加该标签，请勿重复添加'
      }
    }

    // 创建标签审核记录
    const tagAudit = {
      id: Date.now(),
      novelId,
      userId: 1,
      tag,
      status: 0,
      createdAt: new Date().toISOString()
    }
    MOCK_TAG_AUDITS.push(tagAudit)

    return {
      success: true,
      message: '标签提交成功，等待审核'
    }
  },

  async getMyTags(params) {
    await delay()
    const { page = 0, size = 10, status } = params

    if (!global.MOCK_TAG_AUDITS) {
      global.MOCK_TAG_AUDITS = []
    }
    const MOCK_TAG_AUDITS = global.MOCK_TAG_AUDITS

    let audits = MOCK_TAG_AUDITS

    // 根据状态过滤
    if (status !== undefined) {
      audits = audits.filter(a => a.status === status)
    }

    // 分页
    const total = audits.length
    const start = page * size
    const end = start + size
    const content = audits.slice(start, end)

    // 添加书名
    const contentWithNovelTitle = content.map(audit => {
      const novel = MOCK_NOVELS.find(n => n.id === audit.novelId)
      return {
        ...audit,
        novelTitle: novel ? novel.title : ''
      }
    })

    return {
      success: true,
      content: contentWithNovelTitle,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  async getPendingAudits(params) {
    await delay()
    const { page = 0, size = 10, status } = params

    if (!global.MOCK_TAG_AUDITS) {
      global.MOCK_TAG_AUDITS = []
    }
    const MOCK_TAG_AUDITS = global.MOCK_TAG_AUDITS

    let audits = MOCK_TAG_AUDITS

    // 根据状态过滤
    if (status !== undefined) {
      audits = audits.filter(a => a.status === status)
    } else {
      audits = audits.filter(a => a.status === 0)
    }

    // 分页
    const total = audits.length
    const start = page * size
    const end = start + size
    const content = audits.slice(start, end)

    // 添加书名
    const contentWithNovelTitle = content.map(audit => {
      const novel = MOCK_NOVELS.find(n => n.id === audit.novelId)
      return {
        ...audit,
        novelTitle: novel ? novel.title : '',
        userId: audit.userId,
        username: '用户1'
      }
    })

    return {
      success: true,
      content: contentWithNovelTitle,
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page
    }
  },

  async auditTag(id, data) {
    await delay()
    const { status, reason } = data

    if (!global.MOCK_TAG_AUDITS) {
      global.MOCK_TAG_AUDITS = []
    }
    const MOCK_TAG_AUDITS = global.MOCK_TAG_AUDITS

    if (!global.MOCK_USER_TAGS) {
      global.MOCK_USER_TAGS = []
    }
    const MOCK_USER_TAGS = global.MOCK_USER_TAGS

    const auditIndex = MOCK_TAG_AUDITS.findIndex(a => a.id === id)
    if (auditIndex === -1) {
      throw new Error('审核记录不存在')
    }

    const audit = MOCK_TAG_AUDITS[auditIndex]

    if (audit.status !== 0) {
      return {
        success: false,
        message: '该标签已审核'
      }
    }

    // 更新审核状态
    audit.status = status
    audit.reviewedBy = 1
    audit.reviewedAt = new Date().toISOString()
    audit.reason = reason

    // 如果审核通过，创建用户标签记录
    if (status === 1) {
      const userTag = {
        id: Date.now(),
        userId: audit.userId,
        novelId: audit.novelId,
        tag: audit.tag,
        createdAt: new Date().toISOString()
      }
      MOCK_USER_TAGS.push(userTag)

      // 更新书籍的userTags字段
      const novel = MOCK_NOVELS.find(n => n.id === audit.novelId)
      if (novel) {
        if (!novel.userTags) {
          novel.userTags = []
        }
        if (!novel.userTags.includes(audit.tag)) {
          novel.userTags.push(audit.tag)
        }
      }
    }

    return {
      success: true,
      message: '审核成功'
    }
  },

  async batchAuditTags(data) {
    await delay()
    const { ids, status, reason } = data

    if (!global.MOCK_TAG_AUDITS) {
      global.MOCK_TAG_AUDITS = []
    }
    const MOCK_TAG_AUDITS = global.MOCK_TAG_AUDITS

    if (!global.MOCK_USER_TAGS) {
      global.MOCK_USER_TAGS = []
    }
    const MOCK_USER_TAGS = global.MOCK_USER_TAGS

    let approveCount = 0
    let rejectCount = 0

    for (const id of ids) {
      const auditIndex = MOCK_TAG_AUDITS.findIndex(a => a.id === id)
      if (auditIndex === -1) {
        continue
      }

      const audit = MOCK_TAG_AUDITS[auditIndex]

      if (audit.status !== 0) {
        continue
      }

      // 更新审核状态
      audit.status = status
      audit.reviewedBy = 1
      audit.reviewedAt = new Date().toISOString()
      audit.reason = reason

      // 如果审核通过，创建用户标签记录
      if (status === 1) {
        const userTag = {
          id: Date.now(),
          userId: audit.userId,
          novelId: audit.novelId,
          tag: audit.tag,
          createdAt: new Date().toISOString()
        }
        MOCK_USER_TAGS.push(userTag)

        // 更新书籍的userTags字段
        const novel = MOCK_NOVELS.find(n => n.id === audit.novelId)
        if (novel) {
          if (!novel.userTags) {
            novel.userTags = []
          }
          if (!novel.userTags.includes(audit.tag)) {
            novel.userTags.push(audit.tag)
          }
        }

        approveCount++
      } else {
        rejectCount++
      }
    }

    return {
      success: true,
      message: '批量审核成功',
      approveCount,
      rejectCount
    }
  },

  async getAllUserTags() {
    await delay()

    if (!global.MOCK_USER_TAGS) {
      global.MOCK_USER_TAGS = []
    }
    const MOCK_USER_TAGS = global.MOCK_USER_TAGS

    const tags = [...new Set(MOCK_USER_TAGS.map(ut => ut.tag))]

    return {
      success: true,
      tags
    }
  }
}

global.MOCK_TAG_AUDITS = global.MOCK_TAG_AUDITS || []
global.MOCK_USER_TAGS = global.MOCK_USER_TAGS || []

  async deleteTag(data) {
    await delay()
    const { novelId, tag } = data

    if (!global.MOCK_USER_TAGS) {
      global.MOCK_USER_TAGS = []
    }
    const MOCK_USER_TAGS = global.MOCK_USER_TAGS

    // 删除用户标签
    const index = MOCK_USER_TAGS.findIndex(ut => ut.novelId === novelId && ut.tag === tag)
    if (index === -1) {
      throw new Error('标签不存在')
    }

    MOCK_USER_TAGS.splice(index, 1)

    // 更新书籍的userTags字段
    const novel = MOCK_NOVELS.find(n => n.id === novelId)
    if (novel && novel.userTags) {
      const tagIndex = novel.userTags.indexOf(tag)
      if (tagIndex > -1) {
        novel.userTags.splice(tagIndex, 1)
      }
    }

    return {
      success: true,
      message: '删除成功'
    }
  }
}
