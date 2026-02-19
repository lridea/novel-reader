<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-card">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            action=""
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <el-avatar :size="80" :src="user?.avatarUrl">
              {{ (user?.nickname || user?.username)?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="avatar-upload-overlay">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
          <h2>{{ user?.nickname || user?.username }}</h2>
          <p>{{ user?.email }}</p>
        </div>

        <el-divider />

        <div class="stats-section">
          <div class="stat-item">
            <span class="stat-value">{{ stats.favoriteCount }}</span>
            <span class="stat-label">收藏书籍</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.commentCount }}</span>
            <span class="stat-label">评论数</span>
          </div>
        </div>

        <el-divider />

        <div class="form-section">
          <h3>修改资料</h3>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="updateProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-divider />

        <div class="password-section">
          <h3>修改密码</h3>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="changePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { crawlerApi } from '../api'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'

const user = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)
const formRef = ref(null)
const passwordFormRef = ref(null)

const stats = reactive({
  favoriteCount: 0,
  commentCount: 0
})

const form = reactive({
  username: '',
  nickname: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度为1-20个字符', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadUser = async () => {
  try {
    const response = await crawlerApi.getCurrentUser()
    if (response.success && response.user) {
      user.value = response.user
      form.username = response.user.username
      form.nickname = response.user.nickname || ''
      localStorage.setItem('user', JSON.stringify(response.user))
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const loadUserStats = async () => {
  try {
    const response = await crawlerApi.getUserStats()
    if (response.success) {
      stats.favoriteCount = response.favoriteCount
      stats.commentCount = response.commentCount
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只支持JPEG、PNG、GIF、WEBP格式的图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  try {
    const response = await crawlerApi.uploadAvatar(options.file)
    if (response.success) {
      ElMessage.success('头像上传成功')
      // 更新用户头像URL
      const updateResponse = await crawlerApi.updateUserProfile({ avatarUrl: response.avatarUrl })
      if (updateResponse.success && updateResponse.user) {
        user.value = updateResponse.user
        localStorage.setItem('user', JSON.stringify(updateResponse.user))
        window.dispatchEvent(new CustomEvent('user-info-updated', { detail: updateResponse.user }))
      }
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
  }
}

const updateProfile = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const response = await crawlerApi.updateUserProfile({ nickname: form.nickname })
      if (response.success) {
        ElMessage.success('修改成功')
        // 更新本地存储的用户信息
        if (response.user) {
          localStorage.setItem('user', JSON.stringify(response.user))
          // 触发自定义事件通知其他组件更新
          window.dispatchEvent(new CustomEvent('user-info-updated', { detail: response.user }))
        }
        loadUser()
      } else {
        ElMessage.error(response.message || '修改失败')
      }
    } catch (error) {
      console.error('修改失败:', error)
      ElMessage.error('修改失败')
    } finally {
      loading.value = false
    }
  })
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    passwordLoading.value = true
    try {
      const response = await crawlerApi.changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      if (response.success) {
        ElMessage.success('密码修改成功')
        passwordFormRef.value.resetFields()
      } else {
        ElMessage.error(response.message || '密码修改失败')
      }
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error('修改密码失败')
    } finally {
      passwordLoading.value = false
    }
  })
}

onMounted(() => {
  loadUser()
  loadUserStats()
})
</script>

<style scoped>
.profile-page {
  padding: 20px 0;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 24px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  text-align: center;
  padding: 20px 0;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-uploader:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 12px;
}

.avatar-upload-overlay .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-section h2 {
  margin: 16px 0 8px;
  font-size: 20px;
  color: #303133;
}

.avatar-section p {
  margin: 0;
  color: #909399;
}

.stats-section {
  display: flex;
  justify-content: center;
  gap: 60px;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.form-section h3,
.password-section h3 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 20px 0;
}
</style>
