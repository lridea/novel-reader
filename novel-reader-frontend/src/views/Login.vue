<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">
        <el-icon :size="40"><Reading /></el-icon>
        <h1>读书网站</h1>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="footer-links">
        <span>还没有账号？</span>
        <el-button type="primary" link @click="goRegister">立即注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Reading } from '@element-plus/icons-vue'
import { crawlerApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const response = await crawlerApi.login(form)
      if (response.success) {
        localStorage.setItem('token', response.token)
        if (response.user) {
          localStorage.setItem('user', JSON.stringify(response.user))
        }
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(response.message || '登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 400px;
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo h1 {
  margin: 10px 0 0 0;
  font-size: 24px;
  color: #303133;
}

.footer-links {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}
</style>
