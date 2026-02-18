<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-card">
        <div class="avatar-section">
          <el-avatar :size="80" :src="user?.avatarUrl">
            {{ user?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <h2>{{ user?.username }}</h2>
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
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
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
  username: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
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
    user.value = response
    form.username = response.username
    localStorage.setItem('user', JSON.stringify(response))
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const updateProfile = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await crawlerApi.updateUserProfile({ username: form.username })
      ElMessage.success('修改成功')
      loadUser()
    } catch (error) {
      console.error('修改失败:', error)
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
      await crawlerApi.changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功')
      passwordFormRef.value.resetFields()
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      passwordLoading.value = false
    }
  })
}

onMounted(() => {
  loadUser()
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
