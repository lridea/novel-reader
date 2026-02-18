import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '../views/UserLayout.vue'
import AdminLayout from '../views/Layout.vue'

const routes = [
  {
    path: '/',
    component: UserLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'novel/:id',
        name: 'NovelDetailUser',
        component: () => import('../views/NovelDetail.vue'),
        meta: { title: '小说详情' }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('../views/Favorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '管理首页', requiresAdmin: true }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('../views/Tasks.vue'),
        meta: { title: '任务列表', requiresAdmin: true }
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('../views/TaskDetail.vue'),
        meta: { title: '任务详情', requiresAdmin: true }
      },
      {
        path: 'platforms',
        name: 'Platforms',
        component: () => import('../views/Platforms.vue'),
        meta: { title: '平台配置', requiresAdmin: true }
      },
      {
        path: 'novel-management',
        name: 'NovelManagement',
        component: () => import('../views/NovelManagement.vue'),
        meta: { title: '书籍管理', requiresAdmin: true }
      },
      {
        path: 'tag-audit',
        name: 'TagAudit',
        component: () => import('../views/TagAudit.vue'),
        meta: { title: '标签审核', requiresAdmin: true }
      },
      {
        path: 'sensitive-words',
        name: 'SensitiveWords',
        component: () => import('../views/SensitiveWords.vue'),
        meta: { title: '敏感词管理', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 读书网站` : '读书网站'

  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)

  if (requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (requiresAdmin) {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user.role !== 'ADMIN') {
          next({ name: 'Home' })
          return
        }
      } catch {
        next({ name: 'Home' })
        return
      }
    } else {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router
