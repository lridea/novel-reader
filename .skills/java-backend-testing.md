# Java Backend Testing Skill

**适用场景**: Java Spring Boot 后端开发，特别是 novel-reader 项目

---

## 核心规则

### 🚨 必须遵守的规则

**Java后端代码开发如果新增功能或者修改功能，必须增加测试用例，只有单元测试通过之后才能推送代码。**

这是项目开发的核心规则，必须严格执行。

---

## 什么时候需要编写测试

### ✅ 必须编写测试的情况

1. **新增 Service 类**
   - 所有公共方法都需要测试
   - 包括正常场景、边界情况、异常情况

2. **修改 Service 类的逻辑**
   - 修改的方法需要更新测试用例
   - 受影响的其他方法也需要验证

3. **新增 Controller 类**
   - 所有接口端点需要测试
   - 包括正常响应、错误响应、权限检查

4. **修改 Controller 类的接口**
   - 修改的接口需要更新测试用例
   - 接口签名变更需要确保测试失败时能及时发现问题

5. **新增配置类**
   - 如 JwtUtil、SecurityConfig 等核心配置类
   - 所有公共方法都需要测试

6. **修改安全配置**
   - 权限规则的修改需要验证
   - 认证逻辑的修改需要验证

### ❌ 可以跳过测试的情况

1. **仅修改 Entity 类的字段或注解**
   - 集成测试会覆盖数据库操作

2. **仅修改 Repository 的查询方法**
   - 数据库集成测试会覆盖

3. **仅修改前端代码**
   - 前端测试（如果有）由前端负责

---

## 测试覆盖率要求

- **Service 层**: ≥ 80%
- **Config 层**: ≥ 80%
- **Controller 层**: ≥ 80%
- **整体覆盖率**: ≥ 75%

---

## 测试规范

### 1. 测试类命名

```
{被测试类名}Test.java
```

例如：
- `AuthServiceTest.java` 测试 `AuthService.java`
- `JwtUtilTest.java` 测试 `JwtUtil.java`

### 2. 测试方法命名

使用 `test{方法名}_{场景}` 格式：

```java
// ✅ 好的命名
testRegister_Success()
testRegister_UsernameExists()
testLogin_WrongPassword()

// ❌ 不好的命名
test1()
testRegister()
registerSuccess()
```

### 3. 测试结构

使用 Given-When-Then 结构：

```java
@Test
void testRegister_Success() {
    // Given - 准备测试数据
    String username = "newuser";
    String email = "new@example.com";
    String password = "NewPass@123";

    when(userRepository.existsByUsername(username)).thenReturn(false);
    when(userRepository.existsByEmail(email)).thenReturn(false);

    // When - 执行被测试的方法
    Map<String, Object> result = authService.register(username, email, password);

    // Then - 验证结果
    assertNotNull(result);
    assertTrue((Boolean) result.get("success"));
    verify(userRepository).existsByUsername(username);
}
```

### 4. 使用 Mockito

对于数据库调用、外部服务调用等，使用 Mock：

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前初始化测试数据
        testUser = new User();
        testUser.setId(1L);
    }
}
```

### 5. 测试场景覆盖

每个方法需要覆盖以下场景：

- ✅ 正常场景（最常见的情况）
- ✅ 边界情况（空值、空字符串、零、负数等）
- ✅ 异常情况（资源不存在、权限不足、格式错误等）
- ✅ 权限检查（如果涉及）

---

## 开发流程

### 开发新功能

```
1. 编写代码
2. 编写测试用例
3. 运行测试
4. 确保所有测试通过
5. 检查测试覆盖率
6. 推送代码
```

### 修改现有功能

```
1. 修改代码
2. 更新或添加测试用例
3. 运行所有测试
4. 确保所有测试通过
5. 检查测试覆盖率
6. 推送代码
```

---

## 推送代码前的检查清单

- [ ] 所有新增的 Service 类都有对应的测试类
- [ ] 所有修改的 Service 类都更新了测试用例
- [ ] 所有单元测试都能通过
- [ ] 测试覆盖率不低于 80%
- [ ] 没有使用 `@Ignore` 或 `@Disabled` 跳过测试
- [ ] 测试命名符合规范
- [ ] 测试使用了 Given-When-Then 结构

---

## 运行测试

### 运行所有测试

```bash
cd novel-reader/novel-reader-backend
mvn test
```

### 运行特定测试类

```bash
mvn test -Dtest=AuthServiceTest
```

### 运行特定测试方法

```bash
mvn test -Dtest=AuthServiceTest#testRegister_Success
```

### 生成覆盖率报告

```bash
mvn test jacoco:report
```

---

## 注意事项

### 1. 测试隔离

每个测试方法应该独立运行，不依赖其他测试方法的执行结果：

```java
@BeforeEach
void setUp() {
    // 在每个测试方法执行前初始化测试数据
    testUser = new User();
    testUser.setId(1L);
}
```

### 2. Mock 对象

对于数据库调用、外部服务调用等，使用 Mock：

```java
@Mock
private UserRepository userRepository;

// ✅ 使用 Mock
when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

// ❌ 不应该直接调用真实方法
userRepository.findById(1L);
```

### 3. 验证 Mock 调用

使用 `verify()` 确保方法被正确调用：

```java
// 验证方法被调用一次
verify(userRepository).findById(1L);

// 验证方法从未被调用
verify(userRepository, never()).delete(any(User.class));

// 验证方法被调用多次
verify(userRepository, times(3)).save(any(User.class));
```

---

## 项目中的测试示例

### 已完成的测试用例

1. **AuthServiceTest** (13个测试方法)
   - 用户注册（成功、用户名已存在、邮箱已被注册）
   - 用户登录（成功、用户不存在、密码错误、账号禁用）
   - 获取当前用户信息

2. **UserServiceTest** (9个测试方法)
   - 获取用户信息
   - 更新用户信息（成功、仅昵称、用户不存在）
   - 修改密码（成功、用户不存在、旧密码错误）

3. **FavoriteServiceTest** (8个测试方法)
   - 添加收藏（成功、小说不存在、已收藏）
   - 取消收藏（成功、未收藏）
   - 获取收藏列表
   - 更新收藏备注（成功、未收藏）

4. **FavoriteCategoryServiceTest** (10个测试方法)
   - 创建分类
   - 获取分类列表
   - 更新分类（成功、分类不存在、无权限、仅名称）
   - 删除分类（成功、分类不存在、无权限）

5. **JwtUtilTest** (14个测试方法)
   - 生成Token（成功、管理员角色）
   - 验证Token（有效、无效、过期、空值）
   - 提取信息（用户ID、用户名、角色、声明）
   - Token结构（三部分、过期时间）

---

## 相关文档

- `/novel-reader/测试指南.md` - 详细的测试指南
- `/novel-reader/run-tests.sh` - 测试运行脚本

---

**创建时间**: 2026-02-18  
**版本**: v1.0  
**状态**: 已记录到 skills

🦞
