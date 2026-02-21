<template>
  <div class="user-layout">
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="goHome">
          <el-icon :size="24"><Reading /></el-icon>
          <span>读书网站</span>
        </div>
        <div class="search-bar" v-if="!isFavoritesPage && !isMobile">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索书名/作者"
            clearable
            @keyup.enter="handleSearch"
            @input="onSearchInput"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
        <div class="search-bar-placeholder" v-else></div>
        <div class="user-actions">
          <template v-if="user">
            <el-dropdown @command="handleUserCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="user.avatarUrl">
                  {{ user.nickname?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span class="username">{{ user.nickname || user.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="favorites">
                    <el-icon><Star /></el-icon>
                    我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="admin" divided>
                    <el-icon><Setting /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="goLogin">登录</el-button>
            <el-button @click="goRegister">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <p>&copy; 2026 读书网站 - All Rights Reserved</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Reading, Search, Star, User, Setting, SwitchButton } from '@element-plus/icons-vue'
import { crawlerApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const searchKeyword = ref('')
const user = ref(null)
const isMobile = ref(false)

const isAdmin = computed(() => {
  return user.value?.role === 'ADMIN'
})

const isFavoritesPage = computed(() => {
  return route.path === '/favorites'
})

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

watch(() => [route.path, route.query.keyword], ([path, keyword]) => {
  if (path === '/' && !keyword) {
    searchKeyword.value = ''
  } else if (keyword) {
    searchKeyword.value = keyword
  }
}, { immediate: true })

const onSearchInput = () => {
  sessionStorage.setItem('searchKeyword', searchKeyword.value.trim())
}

const goHome = () => {
  router.push('/')
}

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({ path: '/', query: { keyword } })
  } else {
    if (route.query.keyword) {
      router.push({ path: '/' })
    }
  }
}

const goLogin = () => {
  router.push('/login')
}

const goRegister = () => {
  router.push('/register')
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'favorites':
      router.push('/favorites')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await crawlerApi.logout()
  } catch (error) {
    console.error('退出登录API调用失败:', error)
  } finally {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    user.value = null
    ElMessage.success('已退出登录')
    router.push('/')
  }
}

const loadUser = async () => {
  const token = localStorage.getItem('token')
  if (token) {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch {
        localStorage.removeItem('user')
      }
    }
    try {
      const response = await crawlerApi.getCurrentUser()
      user.value = response.user || response
      localStorage.setItem('user', JSON.stringify(user.value))
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果是401或403错误，清除本地存储
      if (error.response?.status === 401 || error.response?.status === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        user.value = null
      }
    }
  }
}

// 监听用户信息更新事件
const handleUserInfoUpdated = (event) => {
  user.value = event.detail
}

// 监听清空搜索关键字事件
const handleClearSearchKeyword = () => {
  searchKeyword.value = ''
}

onMounted(() => {
  loadUser()
  // 首页且没有 keyword 时清空搜索框
  if (route.path === '/' && !route.query.keyword) {
    searchKeyword.value = ''
  } else if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }
  window.addEventListener('user-info-updated', handleUserInfoUpdated)
  window.addEventListener('clear-search-keyword', handleClearSearchKeyword)
})

onUnmounted(() => {
  window.removeEventListener('user-info-updated', handleUserInfoUpdated)
  window.removeEventListener('clear-search-keyword', handleClearSearchKeyword)
})
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.logo:hover {
  opacity: 0.8;
}

.search-bar {
  flex: 1;
  max-width: 500px;
}

.search-bar-placeholder {
  flex: 1;
  max-width: 500px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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
  flex: 1;
  background: #f5f7fa;
}

.footer {
  background: #fff;
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
  border-top: 1px solid #e6e6e6;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .header-content {
    padding: 10px 12px;
    gap: 12px;
  }

  .logo span {
    display: inline;
  }

  .search-bar {
    flex: 1;
    max-width: 100%;
  }

  .search-bar-placeholder {
    flex: 1;
    max-width: 100%;
  }

  .user-actions {
    gap: 8px;
  }

  .username {
    display: none;
  }

  .main {
    flex: none;
    min-height: auto;
  }
}
</style>
