---
name: "pagination-pattern"
description: "Implements responsive pagination with infinite scroll for mobile and traditional pagination for PC, with scroll position restoration. Invoke when implementing list pages with different pagination behaviors for mobile and PC."
---

# Responsive Pagination Pattern

This skill provides a pattern for implementing responsive pagination that:
- Mobile: Infinite scroll (load more on scroll)
- PC: Traditional pagination with page numbers
- Both: Scroll position restoration when returning from detail pages

## Core Implementation

### 1. Mobile Detection

```javascript
// 移动端检测
const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('scroll', handleScroll)
})
```

### 2. Scroll Position Save/Restore

```javascript
// 保存滚动位置和分页数据
const saveScrollPosition = () => {
  sessionStorage.setItem('pageScrollPosition', window.scrollY.toString())
  sessionStorage.setItem('pageCurrentPage', currentPage.value.toString())
  sessionStorage.setItem('pageTotal', total.value.toString())
  
  // 移动端额外保存已加载的数据
  if (isMobile.value) {
    sessionStorage.setItem('pageData', JSON.stringify(items.value))
  }
}

// 恢复滚动位置和分页数据
const restoreScrollPosition = async () => {
  const savedPosition = sessionStorage.getItem('pageScrollPosition')
  const savedPage = sessionStorage.getItem('pageCurrentPage')
  const savedTotal = sessionStorage.getItem('pageTotal')
  const savedData = sessionStorage.getItem('pageData')
  
  if (savedPage) {
    currentPage.value = parseInt(savedPage)
  }
  
  if (savedTotal) {
    total.value = parseInt(savedTotal)
  }
  
  // 移动端直接恢复缓存数据，PC端重新加载
  if (isMobile.value && savedData) {
    items.value = JSON.parse(savedData)
  } else if (savedPage) {
    await loadData(false)
  }
  
  if (savedPosition) {
    setTimeout(() => {
      window.scrollTo(0, parseInt(savedPosition))
    }, 100)
  }
  
  // 清理缓存
  sessionStorage.removeItem('pageScrollPosition')
  sessionStorage.removeItem('pageCurrentPage')
  sessionStorage.removeItem('pageData')
  sessionStorage.removeItem('pageTotal')
}
```

### 3. Data Loading with Append Mode

```javascript
// loadData 支持 append 参数
const loadData = async (append = false) => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    // ... add other params
    
    const response = await api.getData(params)
    
    if (append) {
      items.value = [...items.value, ...(response.content || [])]
    } else {
      items.value = response.content || []
    }
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}
```

### 4. Scroll Handler for Mobile

```javascript
// 滚动监听 - 移动端自动加载更多
const handleScroll = () => {
  if (!isMobile.value) return
  
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 距离底部 100px 时触发加载
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadMore()
  }
}

// 移动端加载更多
const loadMore = () => {
  if (loading.value) return
  if (items.value.length >= total.value) return
  
  currentPage.value++
  loadData(true)  // append mode
}
```

### 5. PC Pagination Handler

```javascript
// PC端分页处理 - 注意：不使用 append 模式
const handlePageChange = () => {
  loadData(false)  // replace mode, NOT append
}
```

### 6. Clear Cache on Filter/Search

```javascript
// 筛选时清除缓存并重置
const clearCacheAndLoad = () => {
  sessionStorage.removeItem('pageScrollPosition')
  sessionStorage.removeItem('pageCurrentPage')
  sessionStorage.removeItem('pageData')
  sessionStorage.removeItem('pageTotal')
  
  currentPage.value = 1
  loadData(false)
}
```

### 7. Save Position Before Navigation

```javascript
// 跳转详情页前保存位置
const goDetail = (id) => {
  saveScrollPosition()
  router.push(`/detail/${id}`)
}
```

### 8. Template

```html
<!-- PC端分页器 -->
<div v-if="total > 0 && !isMobile" class="pagination">
  <el-pagination
    v-model:current-page="currentPage"
    v-model:page-size="pageSize"
    :total="total"
    layout="total, sizes, prev, pager, next"
    @current-change="handlePageChange"
    @size-change="handlePageChange"
  />
</div>

<!-- 移动端加载提示 -->
<div v-if="isMobile && items.length > 0 && loading" class="loading-more">
  加载中...
</div>
<div v-if="isMobile && items.length > 0 && !loading && items.length >= total" class="no-more">
  没有更多了
</div>
```

### 9. onMounted Logic

```javascript
onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('scroll', handleScroll)
  
  // 检查是否有缓存的分页数据
  const savedPage = sessionStorage.getItem('pageCurrentPage')
  
  if (savedPage) {
    await restoreScrollPosition()
  } else {
    loadData(false)
  }
})
```

## Important Notes

1. **Use unique sessionStorage keys** for each page to avoid conflicts (e.g., `homeScrollPosition`, `favoritesScrollPosition`)

2. **PC pagination must use `loadData(false)`** - The `@current-change` and `@size-change` events pass the page number as a parameter, which would be interpreted as `append=true` if bound directly to `loadData`

3. **Mobile saves data, PC reloads** - Mobile restores cached data to avoid re-fetching, PC reloads the specific page

4. **Clear cache on filter/search/sort** - Any filter change should clear the cached position and reset to page 1

5. **CSS styles for loading indicators**:
```css
.loading-more,
.no-more {
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 14px;
}
```

## Common Mistakes to Avoid

1. ❌ Binding pagination events directly to `loadData`:
```html
<!-- WRONG - page number becomes append parameter -->
@current-change="loadData"
```

2. ✅ Use separate handler:
```html
<!-- CORRECT -->
@current-change="handlePageChange"
```

3. ❌ Forgetting to check `isMobile` in scroll handler

4. ❌ Not clearing cache when filters change

5. ❌ Using same sessionStorage keys across different pages
