<template>
  <div class="tag-audit">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">标签审核</span>
      </template>
      <template #extra>
        <el-button type="primary" :disabled="selectedIds.length === 0" @click="batchApprove">
          批量通过 ({{ selectedIds.length }})
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="batchReject">
          批量拒绝 ({{ selectedIds.length }})
        </el-button>
      </template>
    </el-page-header>

    <el-card shadow="hover" style="margin-top: 20px;">
      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px;">
        <el-form-item label="审核状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 150px;">
            <el-option label="全部" :value="null"></el-option>
            <el-option label="待审核" :value="0"></el-option>
            <el-option label="已通过" :value="1"></el-option>
            <el-option label="已拒绝" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAudits">查询</el-button>
          <el-button @click="resetQueryForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 标签审核列表 -->
      <el-table
        :data="audits"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="novelTitle" label="书名" width="200" show-overflow-tooltip />
        <el-table-column prop="tag" label="标签" width="120" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="approve(scope.row)">
              通过
            </el-button>
            <el-button type="danger" link @click="reject(scope.row)">
              拒绝
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
        @size-change="loadAudits"
        @current-change="loadAudits"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { crawlerApi } from '../api'

const router = useRouter()

// 数据
const audits = ref([])
const loading = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 查询表单
const queryForm = ref({
  status: null
})

// 返回
const goBack = () => {
  router.back()
}

// 加载标签审核列表
const loadAudits = async () => {
  loading.value = true
  try {
    const response = await crawlerApi.getPendingAudits({
      page: currentPage.value,
      size: pageSize.value,
      status: queryForm.value.status
    })
    if (response && response.success) {
      audits.value = response.content || []
      total.value = response.totalElements || 0
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载标签审核列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 重置查询表单
const resetQueryForm = () => {
  queryForm.value = {
    status: null
  }
  currentPage.value = 0
  loadAudits()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 审核通过
const approve = async (row) => {
  try {
    await crawlerApi.auditTag(row.id, { status: 1, reason: '' })
    ElMessage.success('审核通过')
    loadAudits()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 审核拒绝
const reject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '拒绝原因不能为空'
    })

    await crawlerApi.auditTag(row.id, { status: 2, reason })
    ElMessage.success('审核拒绝')
    loadAudits()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

// 批量通过
const batchApprove = async () => {
  try {
    await crawlerApi.batchAuditTags({
      ids: selectedIds.value,
      status: 1,
      reason: ''
    })
    ElMessage.success('批量审核通过')
    selectedIds.value = []
    loadAudits()
  } catch (error) {
    console.error('批量审核失败:', error)
    ElMessage.error('批量审核失败')
  }
}

// 批量拒绝
const batchReject = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '批量审核拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '拒绝原因不能为空'
    })

    await crawlerApi.batchAuditTags({
      ids: selectedIds.value,
      status: 2,
      reason: value
    })
    ElMessage.success('批量审核拒绝')
    selectedIds.value = []
    loadAudits()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  loadAudits()
})
</script>

<style scoped>
.tag-audit {
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}
</style>
