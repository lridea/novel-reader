import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('../views/Tasks.vue'),
        meta: { title: '任务列表' }
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('../views/TaskDetail.vue'),
        meta: { title: '任务详情' }
      },
      {
        path: 'platforms',
        name: 'Platforms',
        component: () => import('../views/Platforms.vue'),
        meta: { title: '平台配置' }
      },
      {
        path: 'novels',
        name: 'Novels',
        component: () => import('../views/Novels.vue'),
        meta: { title: '数据浏览' }
      },
      {
        path: 'novels/:id',
        name: 'NovelDetail',
        component: () => import('../views/NovelDetail.vue'),
        meta: { title: '小说详情' }
      },
      {
        path: 'novel-management',
        name: 'NovelManagement',
        component: () => import('../views/NovelManagement.vue'),
        meta: { title: '书籍管理' }
      },
      {
        path: 'tag-audit',
        name: 'TagAudit',
        component: () => import('../views/TagAudit.vue'),
        meta: { title: '标签审核' }
      },
      {
        path: 'sensitive-words',
        name: 'SensitiveWords',
        component: () => import('../views/SensitiveWords.vue'),
        meta: { title: '敏感词管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
