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
  },
  {
    id: 4,
    platform: 'qidian',
    baseUrl: 'https://www.qidian.com',
    enabled: 0,
    tags: '["武侠", "仙侠", "玄幻", "都市"]',
    crawlInterval: 7200,
    maxRetry: 3,
    lastCrawlTime: null,
    lastSuccessCrawlTime: null,
    isRunning: 0,
    runningTaskId: null,
    crawlCount: 0,
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

  // 根据平台获取小说（返回数组，与后端一致）
  async getNovelsByPlatform(platform) {
    return MOCK_NOVELS.filter(n => n.platform === platform)
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
  }
}

export default mockApi
