<template>
  <div class="sensitive-words">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">敏感词管理</span>
      </template>
      <template #extra>
        <el-button type="primary" @click="showAddDialog">新增敏感词</el-button>
        <el-button type="warning" @click="showImportDialog">批量导入</el-button>
        <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="batchDelete"
        >
          批量删除 ({{ selectedIds.length }})
        </el-button>
      </template>
    </el-page-header>

    <el-card shadow="hover" style="margin-top: 20px;">
      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px;">
        <el-form-item label="分类">
          <el-select v-model="queryForm.category" placeholder="全部分类" clearable style="width: 150px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="政治" value="政治"></el-option>
            <el-option label="违法" value="违法"></el-option>
            <el-option label="色情" value="色情"></el-option>
            <el-option label="暴力" value="暴力"></el-option>
            <el-option label="广告" value="广告"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.enabled" placeholder="全部状态" clearable style="width: 150px;">
            <el-option label="全部" :value="null"></el-option>
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSensitiveWords">查询</el-button>
          <el-button @click="resetQueryForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 敏感词列表 -->
      <el-table
        :data="sensitiveWords"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" width="200" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="scope">
            <el-tag :type="getCategoryType(scope.row.category)">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="120">
          <template #default="scope">
            <el-tag :type="getSeverityType(scope.row.severity)">
              {{ getSeverityText(scope.row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="toggleEnabled(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="showEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="deleteSensitiveWord(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadSensitiveWords"
        @current-change="loadSensitiveWords"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      :title="isEdit ? '编辑敏感词' : '新增敏感词'"
      width="500px"
      @close="resetAddForm"
    >
      <el-form :model="addForm" :rules="addFormRules" label-width="80px">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="addForm.word" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="addForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="政治" value="政治"></el-option>
            <el-option label="违法" value="违法"></el-option>
            <el-option label="色情" value="色情"></el-option>
            <el-option label="暴力" value="暴力"></el-option>
            <el-option label="广告" value="广告"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="addForm.severity" placeholder="请选择严重程度" style="width: 100%;">
            <el-option label="轻微" :value="1"></el-option>
            <el-option label="严重" :value="2"></el-option>
            <el-option label="禁止" :value="3"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSensitiveWord">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入敏感词"
      width="600px"
      @close="resetImportForm"
    >
      <el-alert
        title="导入格式"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        每行一个敏感词，以 ## 开头的行表示分类名称
      </el-alert>
      <el-form :model="importForm" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="importForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="政治" value="政治"></el-option>
            <el-option label="违法" value="违法"></el-option>
            <el-option label="色情" value="色情"></el-option>
            <el-option label="暴力" value="暴力"></el-option>
            <el-option label="广告" value="广告"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="敏感词">
          <el-input
            v-model="importForm.wordsText"
            type="textarea"
            :rows="10"
            placeholder="暴力&#10;色情&#10;毒品&#10;赌博&#10;..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="importWords">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { crawlerApi } from '../api'

const router = useRouter()

// 数据
const sensitiveWords = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 查询表单
const queryForm = ref({
  category: '',
  enabled: null
})

// 新增/编辑对话框
const addDialogVisible = ref(false)
const isEdit = ref(false)
const addForm = ref({
  id: null,
  word: '',
  category: '',
  severity: 1
})
const addFormRules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }]
}

// 导入对话框
const importDialogVisible = ref(false)
const importForm = ref({
  category: '其他',
  wordsText: ''
})

// 返回
const goBack = () => {
  router.back()
}

// 加载敏感词列表
const loadSensitiveWords = async () => {
  loading.value = true
  try {
    const response = await crawlerApi.getSensitiveWords(
      currentPage.value - 1,
      pageSize.value,
      queryForm.value.category,
      queryForm.value.enabled
    )
    if (response && response.success) {
      sensitiveWords.value = response.content || []
      total.value = response.totalElements || 0
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载敏感词失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 重置查询表单
const resetQueryForm = () => {
  queryForm.value = {
    category: '',
    enabled: null
  }
  currentPage.value = 1
  loadSensitiveWords()
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  addForm.value = {
    id: null,
    word: '',
    category: '',
    severity: 1
  }
  addDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  addForm.value = {
    id: row.id,
    word: row.word,
    category: row.category,
    severity: row.severity
  }
  addDialogVisible.value = true
}

// 重置新增表单
const resetAddForm = () => {
  addForm.value = {
    id: null,
    word: '',
    category: '',
    severity: 1
  }
}

// 保存敏感词
const saveSensitiveWord = async () => {
  try {
    if (isEdit.value) {
      // 更新
      const response = await crawlerApi.updateSensitiveWord(
        addForm.value.id,
        addForm.value.word,
        addForm.value.category,
        addForm.value.severity,
        null
      )
      if (response && response.success) {
        ElMessage.success('更新成功')
        addDialogVisible.value = false
        loadSensitiveWords()
      } else {
        ElMessage.error(response.message || '更新失败')
      }
    } else {
      // 新增
      const response = await crawlerApi.addSensitiveWord(
        addForm.value.word,
        addForm.value.category,
        addForm.value.severity
      )
      if (response && response.success) {
        ElMessage.success('添加成功')
        addDialogVisible.value = false
        loadSensitiveWords()
      } else {
        ElMessage.error(response.message || '添加失败')
      }
    }
  } catch (error) {
    console.error('保存敏感词失败:', error)
    ElMessage.error('保存失败')
  }
}

// 删除敏感词
const deleteSensitiveWord = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该敏感词吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await crawlerApi.deleteSensitiveWord(id)
    if (response && response.success) {
      ElMessage.success('删除成功')
      loadSensitiveWords()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除敏感词失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个敏感词吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await crawlerApi.batchDeleteSensitiveWords(selectedIds.value)
    if (response && response.success) {
      ElMessage.success(`批量删除成功，共删除 ${response.deleteCount} 个敏感词`)
      selectedIds.value = []
      loadSensitiveWords()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 切换启用状态
const toggleEnabled = async (row) => {
  try {
    const response = await crawlerApi.updateSensitiveWord(
      row.id,
      null,
      null,
      null,
      row.enabled
    )
    if (response && response.success) {
      ElMessage.success('更新成功')
    } else {
      row.enabled = row.enabled === 1 ? 0 : 1
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    row.enabled = row.enabled === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

// 显示导入对话框
const showImportDialog = () => {
  importDialogVisible.value = true
}

// 重置导入表单
const resetImportForm = () => {
  importForm.value = {
    category: '其他',
    wordsText: ''
  }
}

// 导入敏感词
const importWords = async () => {
  try {
    // 解析敏感词
    const words = importForm.value.wordsText
      .split('\n')
      .map(line => line.trim())
      .filter(line => line && !line.startsWith('##'))

    if (words.length === 0) {
      ElMessage.warning('请输入敏感词')
      return
    }

    const response = await crawlerApi.importSensitiveWords(words)
    if (response && response.success) {
      ElMessage.success(`导入成功，成功 ${response.successCount} 个，跳过 ${response.skipCount} 个`)
      importDialogVisible.value = false
      loadSensitiveWords()
    } else {
      ElMessage.error(response.message || '导入失败')
    }
  } catch (error) {
    console.error('导入敏感词失败:', error)
    ElMessage.error('导入失败')
  }
}

// 获取分类类型
const getCategoryType = (category) => {
  const typeMap = {
    '政治': 'danger',
    '违法': 'danger',
    '色情': 'warning',
    '暴力': 'warning',
    '广告': 'info',
    '其他': 'info'
  }
  return typeMap[category] || 'info'
}

// 获取严重程度类型
const getSeverityType = (severity) => {
  const typeMap = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[severity] || 'info'
}

// 获取严重程度文本
const getSeverityText = (severity) => {
  const textMap = {
    1: '轻微',
    2: '严重',
    3: '禁止'
  }
  return textMap[severity] || '未知'
}

onMounted(() => {
  loadSensitiveWords()
})
</script>

<style scoped>
.sensitive-words {
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}
</style>
