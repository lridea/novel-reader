<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo" @click="goHome">
        <el-icon :size="24"><Reading /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/tasks">
          <el-icon><List /></el-icon>
          <span>任务列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/platforms">
          <el-icon><Setting /></el-icon>
          <span>平台配置</span>
        </el-menu-item>
        <el-menu-item index="/admin/novel-management">
          <el-icon><Folder /></el-icon>
          <span>书籍管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tag-audit">
          <el-icon><PriceTag /></el-icon>
          <span>标签审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/sensitive-words">
          <el-icon><Warning /></el-icon>
          <span>敏感词管理</span>
        </el-menu-item>
      </el-menu>
      <div class="back-home">
        <el-button type="primary" text @click="goHome">
          <el-icon><Back /></el-icon>
          返回前台
        </el-button>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32">
                {{ user?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ user?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">
                  <el-icon><House /></el-icon>
                  返回前台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Refresh, DataAnalysis, List, Setting, Reading, 
  Folder, PriceTag, Warning, Back, House, SwitchButton 
} from '@element-plus/icons-vue'
import { crawlerApi } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const user = ref(null)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/tasks/')) return '/admin/tasks'
  if (path.startsWith('/admin/novels/')) return '/admin/novels'
  return path
})

const pageTitle = computed(() => route.meta?.title || '管理后台')

const goHome = () => {
  router.push('/')
}

const handleCommand = (command) => {
  if (command === 'home') {
    goHome()
  } else if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = async () => {
  try {
    await crawlerApi.logout()
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    ElMessage.success('已退出登录')
    router.push('/')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

const loadUser = async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      user.value = JSON.parse(userStr)
    } catch {
      user.value = null
    }
  }
  
  if (!user.value) {
    try {
      const response = await crawlerApi.getCurrentUser()
      user.value = response
      localStorage.setItem('user', JSON.stringify(response))
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background: linear-gradient(180deg, #1d1e1f 0%, #2d2e2f 100%);
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #3d3e3f;
  cursor: pointer;
}

.logo:hover {
  opacity: 0.8;
}

.menu {
  border-right: none;
  background: transparent;
  flex: 1;
}

.menu :deep(.el-menu-item) {
  color: #a0a0a0;
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-menu-item.is-active) {
  background: #3d3e3f;
  color: #fff;
}

.back-home {
  padding: 16px;
  border-top: 1px solid #3d3e3f;
}

.back-home .el-button {
  width: 100%;
  color: #a0a0a0;
}

.back-home .el-button:hover {
  color: #fff;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
