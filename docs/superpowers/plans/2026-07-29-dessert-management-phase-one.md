# 甜品管理系统第一阶段实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保留现有 MySQL 数据的前提下，交付具有“深焙可可”统一视觉的分类管理、甜品管理、图片上传、库存展示和实时首页统计闭环。

**Architecture:** 后端继续采用 Spring Boot 4、MyBatis Plus 与 MySQL，通过 Flyway 接管现有数据库并按 controller/service/mapper/dto 分层；前端继续采用 Vue 3、Vue Router、Element Plus 与 Axios，通过 `/api` 和 `/uploads` 代理访问后端。每个后端业务模块先写服务或接口测试，再实现最小代码；每个前端模块以 API 封装、页面行为和生产构建作为验收边界。

**Tech Stack:** Java 17、Spring Boot 4.0.7、MyBatis Plus 3.5.16、Flyway、MySQL 8、JUnit 5、Mockito、Vue 3.5、Vite 8、Vue Router 5、Element Plus 2.14、Axios 1.18、Vitest。

## Global Constraints

- 保留现有 `user` 与 `dessert` 数据，禁止删除或重建已有业务表。
- Flyway 使用基线版本 `0`，第一份迁移使用版本 `1`。
- 甜品价格在 Java 中统一使用 `BigDecimal`，库存不得小于 `0`。
- 图片只允许常见图片格式，单张不超过 `5 MB`，服务端使用 UUID 文件名。
- 数据库只保存 `/uploads/desserts/<唯一文件名>`，实际文件写入 `backend/uploads/desserts/`。
- 前端所有接口通过 `frontend/src/api/request.js` 调用，不在页面中写死 `localhost`。
- 视觉统一采用深棕黑背景、深棕卡片、琥珀金重点色和暖白正文。
- 第一阶段不实现订单、库存流水、细粒度权限、云对象存储或顾客商城。

---

## 文件职责总览

### 后端

- `backend/src/main/resources/db/migration/V1__category_and_dessert_fields.sql`：以保留数据方式新增分类和甜品扩展字段。
- `backend/src/main/java/com/sweet/dessertsystem/common/ApiResponse.java`：稳定的成功/错误响应结构。
- `backend/src/main/java/com/sweet/dessertsystem/exception/*`：业务异常与统一异常映射。
- `backend/src/main/java/com/sweet/dessertsystem/config/*`：分页、静态资源和上传目录配置。
- `backend/src/main/java/com/sweet/dessertsystem/category/*`：分类实体、DTO、Mapper、Service、Controller。
- `backend/src/main/java/com/sweet/dessertsystem/dessert/*`：甜品 DTO、查询对象、视图对象和业务实现。
- `backend/src/main/java/com/sweet/dessertsystem/upload/UploadController.java`：图片校验、保存与路径返回。
- `backend/src/main/java/com/sweet/dessertsystem/dashboard/*`：统计查询与首页聚合接口。

### 前端

- `frontend/src/api/request.js`：Axios 实例、响应解包和统一错误处理。
- `frontend/src/api/category.js`、`dessert.js`、`dashboard.js`、`upload.js`：按业务域封装 API。
- `frontend/src/styles/theme.css`：深焙可可设计令牌与全局基础样式。
- `frontend/src/layout/AdminLayout.vue`：导航、标题、当前用户和退出。
- `frontend/src/views/Login.vue`：登录校验与提交状态。
- `frontend/src/views/Home.vue`：实时统计卡、低库存列表和快捷入口。
- `frontend/src/views/CategoryManagement.vue`：分类增改删。
- `frontend/src/views/DessertManagement.vue`：甜品搜索、筛选、分页、表单、上传与状态切换。
- `frontend/src/router/index.js`：路由元信息和登录守卫。

---

### Task 1: 数据库安全迁移与后端基础配置

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.properties`
- Create: `backend/src/main/resources/db/migration/V1__category_and_dessert_fields.sql`
- Create: `backend/src/main/java/com/sweet/dessertsystem/config/MybatisPlusConfig.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/config/ApplicationConfigurationTests.java`

**Interfaces:**
- Consumes: 现有 MySQL 库 `dessert_system`、现有 `user` 和 `dessert` 表。
- Produces: `category` 表、扩展后的 `dessert` 字段、MyBatis Plus 分页拦截器与可覆盖的上传目录配置。

- [ ] **Step 1: 编写配置测试并确认失败**

```java
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:configtest;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false"
})
class ApplicationConfigurationTests {
    @Autowired
    private MybatisPlusInterceptor interceptor;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Test
    void registersPaginationAndUploadDirectory() {
        assertThat(interceptor.getInterceptors())
            .anyMatch(inner -> inner instanceof PaginationInnerInterceptor);
        assertThat(uploadDir).isEqualTo("uploads");
    }
}
```

运行：

```powershell
cd backend
.\mvnw.cmd -Dtest=ApplicationConfigurationTests test
```

预期：因缺少 H2、分页 Bean 或 `app.upload-dir` 而失败。

- [ ] **Step 2: 添加 Flyway、校验和测试依赖**

在 `backend/pom.xml` 增加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 3: 添加分页配置与应用配置**

`MybatisPlusConfig.java`：

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

`application.properties` 增加：

```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
app.upload-dir=uploads
```

- [ ] **Step 4: 编写保留数据的 V1 迁移**

```sql
CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
);

INSERT INTO category (name)
SELECT '未分类'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '未分类');

ALTER TABLE dessert
    ADD COLUMN IF NOT EXISTS category_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS stock INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status TINYINT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

UPDATE dessert
SET category_id = (SELECT id FROM category WHERE name = '未分类' LIMIT 1)
WHERE category_id IS NULL;

ALTER TABLE dessert
    MODIFY category_id BIGINT NOT NULL,
    ADD INDEX IF NOT EXISTS idx_dessert_category_id (category_id),
    ADD INDEX IF NOT EXISTS idx_dessert_status (status);
```

- [ ] **Step 5: 运行配置测试并验证真实迁移前备份**

```powershell
.\mvnw.cmd -Dtest=ApplicationConfigurationTests test
mysqldump -uroot -p dessert_system user dessert > dessert_system_before_phase_one.sql
.\mvnw.cmd spring-boot:run
```

预期：测试通过；启动日志显示 Flyway 成功执行 V1；原有 `user`、`dessert` 行数不变。

- [ ] **Step 6: 提交**

```powershell
git add backend/pom.xml backend/src/main/resources backend/src/main/java/com/sweet/dessertsystem/config backend/src/test/java/com/sweet/dessertsystem/config
git commit -m "feat: add safe database migration and backend config"
```

---

### Task 2: 统一响应、异常处理与分类管理后端

**Files:**
- Create: `backend/src/main/java/com/sweet/dessertsystem/common/ApiResponse.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/exception/BusinessException.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/exception/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/category/Category.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/category/CategoryRequest.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/category/CategoryMapper.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/category/CategoryService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/category/CategoryController.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/category/CategoryServiceTests.java`

**Interfaces:**
- Consumes: `category` 表与 `dessert.category_id`。
- Produces: `GET/POST/PUT/DELETE /category`；错误响应 `{success:false,message:"..."}`。

- [ ] **Step 1: 写分类业务失败测试**

```java
@ExtendWith(MockitoExtension.class)
class CategoryServiceTests {
    @Mock CategoryMapper mapper;
    @InjectMocks CategoryService service;

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> service.create(new CategoryRequest("  ")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("分类名称不能为空");
    }

    @Test
    void rejectsDuplicateName() {
        when(mapper.selectCount(any())).thenReturn(1L);
        assertThatThrownBy(() -> service.create(new CategoryRequest("蛋糕")))
            .hasMessage("分类名称已存在");
    }

    @Test
    void rejectsDeletingUsedCategory() {
        when(mapper.countDesserts(2L)).thenReturn(1L);
        assertThatThrownBy(() -> service.delete(2L))
            .hasMessage("该分类下仍有甜品，不能删除");
    }
}
```

运行 `.\mvnw.cmd -Dtest=CategoryServiceTests test`，预期编译失败，因为分类类尚不存在。

- [ ] **Step 2: 实现稳定响应与异常映射**

```java
public record ApiResponse<T>(boolean success, T data, String message) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "");
    }
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
```

`GlobalExceptionHandler` 将 `BusinessException` 映射为 `400`，`MethodArgumentNotValidException` 映射为 `400`，`MaxUploadSizeExceededException` 映射为 `413`，未预期异常映射为 `500`，均返回明确中文消息。

- [ ] **Step 3: 实现分类实体、请求与 Mapper**

```java
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    // 标准 getter/setter
}

public record CategoryRequest(
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    String name
) {}

public interface CategoryMapper extends BaseMapper<Category> {
    @Select("SELECT COUNT(*) FROM dessert WHERE category_id = #{categoryId}")
    long countDesserts(Long categoryId);
}
```

- [ ] **Step 4: 实现分类业务规则和接口**

`CategoryService` 对名称统一执行 `trim()`，按名称查重；更新时排除当前 ID；删除前调用 `countDesserts`。Controller 暴露：

```java
@GetMapping
public ApiResponse<List<Category>> list()

@PostMapping
public ApiResponse<Category> create(@Valid @RequestBody CategoryRequest request)

@PutMapping("/{id}")
public ApiResponse<Category> update(@PathVariable Long id,
                                    @Valid @RequestBody CategoryRequest request)

@DeleteMapping("/{id}")
public ApiResponse<Void> delete(@PathVariable Long id)
```

- [ ] **Step 5: 运行分类测试**

```powershell
.\mvnw.cmd -Dtest=CategoryServiceTests test
```

预期：3 个测试通过。

- [ ] **Step 6: 提交**

```powershell
git add backend/src/main/java/com/sweet/dessertsystem/common backend/src/main/java/com/sweet/dessertsystem/exception backend/src/main/java/com/sweet/dessertsystem/category backend/src/test/java/com/sweet/dessertsystem/category
git commit -m "feat: add category management API"
```

---

### Task 3: 甜品分页、筛选、增改删与上下架后端

**Files:**
- Modify: `backend/src/main/java/com/sweet/dessertsystem/entity/Dessert.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/mapper/DessertMapper.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/service/DessertService.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/service/impl/DessertServiceImpl.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/controller/DessertController.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dto/DessertRequest.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dto/DessertPageQuery.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dto/DessertView.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/service/DessertServiceTests.java`

**Interfaces:**
- Consumes: `CategoryMapper.selectById(Long)` 与扩展后的 `dessert` 表。
- Produces: `/dessert/page`、`POST /dessert`、`PUT /dessert/{id}`、`DELETE /dessert/{id}`、`PATCH /dessert/{id}/status`。

- [ ] **Step 1: 写校验与状态切换失败测试**

```java
@Test
void rejectsNegativePrice() {
    DessertRequest request = new DessertRequest(
        "提拉米苏", 1L, new BigDecimal("-1.00"), 3, null, null, 1);
    assertThatThrownBy(() -> service.create(request))
        .hasMessage("价格不能小于0");
}

@Test
void rejectsMissingCategory() {
    when(categoryMapper.selectById(99L)).thenReturn(null);
    DessertRequest request = new DessertRequest(
        "提拉米苏", 99L, new BigDecimal("28.00"), 3, null, null, 1);
    assertThatThrownBy(() -> service.create(request))
        .hasMessage("所选分类不存在");
}

@Test
void togglesStatus() {
    Dessert dessert = new Dessert();
    dessert.setId(1L);
    dessert.setStatus(1);
    when(dessertMapper.selectById(1L)).thenReturn(dessert);
    service.changeStatus(1L, 0);
    verify(dessertMapper).updateById(argThat(item -> item.getStatus() == 0));
}
```

运行 `.\mvnw.cmd -Dtest=DessertServiceTests test`，预期因新 DTO 和方法不存在而失败。

- [ ] **Step 2: 扩展实体并使用精确金额**

`Dessert` 字段统一为：

```java
private Long id;
private String name;
private Long categoryId;
private BigDecimal price;
private Integer stock;
private String image;
private String description;
private Integer status;
private LocalDateTime createTime;
private LocalDateTime updateTime;
```

- [ ] **Step 3: 定义请求与查询模型**

```java
public record DessertRequest(
    @NotBlank(message = "甜品名称不能为空") String name,
    @NotNull(message = "请选择分类") Long categoryId,
    @NotNull(message = "请输入价格") @DecimalMin("0.00") BigDecimal price,
    @NotNull(message = "请输入库存") @Min(0) Integer stock,
    String image,
    @Size(max = 1000, message = "描述不能超过1000个字符") String description,
    @Min(0) @Max(1) Integer status
) {}

public record DessertPageQuery(
    @Min(1) long page,
    @Min(1) @Max(100) long size,
    String name,
    Long categoryId
) {}
```

- [ ] **Step 4: 实现分页和业务规则**

`DessertServiceImpl.page` 使用 `LambdaQueryWrapper` 对非空名称执行 `like`，对分类执行 `eq`，按 `updateTime` 倒序；新增和编辑前验证分类存在、价格非负、库存非负；删除和状态切换先验证甜品存在。返回的 `DessertView` 包含 `categoryName`。

- [ ] **Step 5: 实现 REST 接口**

```java
@GetMapping("/page")
public ApiResponse<IPage<DessertView>> page(@Valid DessertPageQuery query)

@PostMapping
public ApiResponse<DessertView> create(@Valid @RequestBody DessertRequest request)

@PutMapping("/{id}")
public ApiResponse<DessertView> update(@PathVariable Long id,
                                      @Valid @RequestBody DessertRequest request)

@DeleteMapping("/{id}")
public ApiResponse<Void> delete(@PathVariable Long id)

@PatchMapping("/{id}/status")
public ApiResponse<Void> changeStatus(@PathVariable Long id,
                                     @RequestBody Map<String, Integer> body)
```

- [ ] **Step 6: 测试并提交**

```powershell
.\mvnw.cmd -Dtest=DessertServiceTests test
git add backend/src/main/java/com/sweet/dessertsystem backend/src/test/java/com/sweet/dessertsystem/service
git commit -m "feat: add dessert management API"
```

预期：甜品服务测试全部通过。

---

### Task 4: 图片上传、静态访问与首页统计后端

**Files:**
- Create: `backend/src/main/java/com/sweet/dessertsystem/config/WebConfig.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/upload/UploadController.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardSummary.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardMapper.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardController.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/upload/UploadControllerTests.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/dashboard/DashboardServiceTests.java`

**Interfaces:**
- Consumes: `MultipartFile`、`app.upload-dir`、分类与甜品数据。
- Produces: `POST /upload/dessert` 返回 `{path:"/uploads/desserts/<uuid>.<ext>"}`；`GET /dashboard/summary` 返回四项统计与最多五条低库存记录。

- [ ] **Step 1: 写上传安全测试**

```java
@Test
void rejectsNonImage() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "note.txt", "text/plain", "hello".getBytes());
    mockMvc.perform(multipart("/upload/dessert").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("只允许上传 JPG、PNG、GIF 或 WEBP 图片"));
}

@Test
void storesImageWithGeneratedName() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "cake.png", "image/png", new byte[]{1, 2, 3});
    mockMvc.perform(multipart("/upload/dessert").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.path",
            matchesPattern("/uploads/desserts/[0-9a-f-]+\\.png")));
}
```

- [ ] **Step 2: 实现上传与静态映射**

`UploadController` 只接受 `image/jpeg`、`image/png`、`image/gif`、`image/webp`，同时校验扩展名；使用 `UUID.randomUUID()` 生成文件名，调用 `Files.copy` 写入 `uploads/desserts`。`WebConfig` 将 `/uploads/**` 映射到规范化后的上传目录。

- [ ] **Step 3: 写统计聚合测试**

```java
@Test
void returnsAllDashboardMetrics() {
    when(mapper.countDesserts()).thenReturn(12L);
    when(mapper.countCategories()).thenReturn(4L);
    when(mapper.sumStock()).thenReturn(86L);
    when(mapper.countLowStock(5)).thenReturn(2L);
    when(mapper.findLowStock(5, 5)).thenReturn(List.of());

    DashboardSummary result = service.summary();

    assertThat(result.dessertCount()).isEqualTo(12);
    assertThat(result.categoryCount()).isEqualTo(4);
    assertThat(result.totalStock()).isEqualTo(86);
    assertThat(result.lowStockCount()).isEqualTo(2);
}
```

- [ ] **Step 4: 实现统计 SQL 和接口**

`DashboardMapper` 使用独立 SQL 完成 `COUNT(*)`、分类计数、`COALESCE(SUM(stock),0)`、`stock <= 5` 计数，并按 `stock ASC, update_time DESC LIMIT 5` 返回低库存甜品。`DashboardController` 暴露 `GET /dashboard/summary`。

- [ ] **Step 5: 运行测试并提交**

```powershell
.\mvnw.cmd -Dtest=UploadControllerTests,DashboardServiceTests test
git add backend/src/main/java/com/sweet/dessertsystem/config/WebConfig.java backend/src/main/java/com/sweet/dessertsystem/upload backend/src/main/java/com/sweet/dessertsystem/dashboard backend/src/test/java/com/sweet/dessertsystem/upload backend/src/test/java/com/sweet/dessertsystem/dashboard
git commit -m "feat: add dessert uploads and dashboard metrics"
```

---

### Task 5: 前端请求层、登录守卫与深焙可可框架

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/vite.config.js`
- Modify: `frontend/src/main.js`
- Modify: `frontend/src/style.css`
- Create: `frontend/src/styles/theme.css`
- Modify: `frontend/src/api/request.js`
- Create: `frontend/src/api/auth.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/layout/AdminLayout.vue`
- Modify: `frontend/src/views/Login.vue`
- Test: `frontend/src/api/request.test.js`
- Test: `frontend/src/router/router.test.js`

**Interfaces:**
- Consumes: 后端 `ApiResponse<T>`、`POST /user/login`。
- Produces: 自动解包 `data` 的请求实例、`sessionStorage.currentUser` 登录态、受保护后台路由和统一后台壳层。

- [ ] **Step 1: 配置 Vitest 并写请求解包测试**

`package.json` 增加：

```json
"scripts": {
  "dev": "vite",
  "build": "vite build",
  "preview": "vite preview",
  "test": "vitest run"
},
"devDependencies": {
  "@vitejs/plugin-vue": "^6.0.7",
  "@vue/test-utils": "^2.4.6",
  "jsdom": "^26.1.0",
  "vite": "^8.1.1",
  "vitest": "^3.2.4"
}
```

测试成功响应返回业务 `data`，失败响应抛出后端中文 `message`，网络失败抛出“网络连接失败，请稍后重试”。

- [ ] **Step 2: 实现请求层与开发代理**

`request.js`：

```javascript
import axios from 'axios'

const request = axios.create({ baseURL: '/api', timeout: 10000 })

request.interceptors.response.use(
  response => {
    const body = response.data
    if (body && typeof body.success === 'boolean') {
      if (body.success) return body.data
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  error => Promise.reject(new Error(
    error.response?.data?.message || '网络连接失败，请稍后重试'
  ))
)

export default request
```

`vite.config.js` 将 `/api` 代理到 `http://localhost:8080` 并移除 `/api`，将 `/uploads` 原路径代理到同一后端。

- [ ] **Step 3: 添加主题令牌**

```css
:root {
  --cocoa-bg: #17110f;
  --cocoa-surface: #241a17;
  --cocoa-surface-2: #30231f;
  --cocoa-border: rgba(240, 190, 107, 0.18);
  --amber: #e5ad52;
  --amber-strong: #f2c46f;
  --warm-white: #fff7e8;
  --muted: #b9a99c;
  --danger: #e47b72;
  --shadow: 0 18px 50px rgba(0, 0, 0, 0.28);
}
```

全局移除 Vite 默认的固定宽度、居中和 `color-scheme` 规则，保证 `#app` 占满视口。

- [ ] **Step 4: 重构登录页、后台布局和路由守卫**

登录成功只保存 `{id,username,role}`，不得保存密码。路由包含：

```javascript
{
  path: '/',
  component: AdminLayout,
  meta: { requiresAuth: true },
  children: [
    { path: 'home', name: 'home', meta: { title: '经营概览' }, component: () => import('../views/Home.vue') },
    { path: 'dessert', name: 'dessert', meta: { title: '甜品管理' }, component: () => import('../views/DessertManagement.vue') },
    { path: 'category', name: 'category', meta: { title: '分类管理' }, component: () => import('../views/CategoryManagement.vue') }
  ]
}
```

`beforeEach` 在 `requiresAuth` 且无 `currentUser` 时跳转 `/login`。布局根据 `route.meta.title` 展示标题，提供退出并清理登录态。

- [ ] **Step 5: 测试、构建并提交**

```powershell
cd frontend
npm install
npm test
npm run build
git add package.json package-lock.json vite.config.js src
git commit -m "feat: add cocoa admin shell and frontend infrastructure"
```

预期：请求层和路由测试通过，Vite 构建成功。

---

### Task 6: 分类管理前端

**Files:**
- Create: `frontend/src/api/category.js`
- Create: `frontend/src/views/CategoryManagement.vue`
- Test: `frontend/src/views/CategoryManagement.test.js`

**Interfaces:**
- Consumes: `GET/POST/PUT/DELETE /category`。
- Produces: 分类表格、新增/重命名弹窗、二次确认删除与错误提示。

- [ ] **Step 1: 写页面行为测试**

```javascript
it('loads, creates and refreshes categories', async () => {
  vi.mocked(categoryApi.listCategories)
    .mockResolvedValueOnce([{ id: 1, name: '蛋糕' }])
    .mockResolvedValueOnce([{ id: 1, name: '蛋糕' }, { id: 2, name: '布丁' }])
  vi.mocked(categoryApi.createCategory).mockResolvedValue({ id: 2, name: '布丁' })

  const wrapper = mount(CategoryManagement)
  await flushPromises()
  await wrapper.get('[data-test="add-category"]').trigger('click')
  await wrapper.get('[data-test="category-name"]').setValue('布丁')
  await wrapper.get('[data-test="submit-category"]').trigger('click')
  await flushPromises()

  expect(categoryApi.createCategory).toHaveBeenCalledWith({ name: '布丁' })
  expect(wrapper.text()).toContain('布丁')
})
```

- [ ] **Step 2: 实现分类 API**

```javascript
export const listCategories = () => request.get('/category')
export const createCategory = data => request.post('/category', data)
export const updateCategory = (id, data) => request.put(`/category/${id}`, data)
export const deleteCategory = id => request.delete(`/category/${id}`)
```

- [ ] **Step 3: 实现分类页面**

页面必须具有加载骨架、空状态、表格、更新时间、添加按钮、编辑按钮和删除按钮；表单提交期间禁用按钮；删除使用 `ElMessageBox.confirm`；所有失败使用 `ElMessage.error(error.message)`；成功后重新读取列表。

- [ ] **Step 4: 测试、构建并提交**

```powershell
npm test -- CategoryManagement.test.js
npm run build
git add src/api/category.js src/views/CategoryManagement.vue src/views/CategoryManagement.test.js
git commit -m "feat: add category management page"
```

---

### Task 7: 甜品管理前端

**Files:**
- Create: `frontend/src/api/dessert.js`
- Create: `frontend/src/api/upload.js`
- Create: `frontend/src/views/DessertManagement.vue`
- Test: `frontend/src/views/DessertManagement.test.js`

**Interfaces:**
- Consumes: 甜品分页 CRUD、状态切换、分类列表与图片上传接口。
- Produces: 搜索、分类筛选、分页、甜品表单、图片预览、上下架与删除完整操作。

- [ ] **Step 1: 写筛选和保存测试**

```javascript
it('queries by name and category', async () => {
  const wrapper = mount(DessertManagement)
  await flushPromises()
  await wrapper.get('[data-test="name-filter"]').setValue('提拉米苏')
  await wrapper.get('[data-test="category-filter"]').setValue('2')
  await wrapper.get('[data-test="search"]').trigger('click')
  await flushPromises()

  expect(dessertApi.pageDesserts).toHaveBeenLastCalledWith({
    page: 1,
    size: 10,
    name: '提拉米苏',
    categoryId: 2
  })
})
```

- [ ] **Step 2: 实现甜品与上传 API**

```javascript
export const pageDesserts = params => request.get('/dessert/page', { params })
export const createDessert = data => request.post('/dessert', data)
export const updateDessert = (id, data) => request.put(`/dessert/${id}`, data)
export const deleteDessert = id => request.delete(`/dessert/${id}`)
export const changeDessertStatus = (id, status) =>
  request.patch(`/dessert/${id}/status`, { status })

export const uploadDessertImage = file => {
  const data = new FormData()
  data.append('file', file)
  return request.post('/upload/dessert', data)
}
```

- [ ] **Step 3: 实现甜品管理页面**

筛选栏包含名称、分类、查询和重置；表格包含图片、名称、分类、价格、库存、状态和操作；库存 `<= 5` 使用危险色标签。新增/编辑弹窗校验名称、分类、非负价格和非负整数库存。上传成功将返回的 `path` 写入表单并显示预览。切换状态失败时恢复开关原值。

- [ ] **Step 4: 测试、构建并提交**

```powershell
npm test -- DessertManagement.test.js
npm run build
git add src/api/dessert.js src/api/upload.js src/views/DessertManagement.vue src/views/DessertManagement.test.js
git commit -m "feat: add dessert management page"
```

---

### Task 8: 实时经营概览与全流程验收

**Files:**
- Create: `frontend/src/api/dashboard.js`
- Modify: `frontend/src/views/Home.vue`
- Test: `frontend/src/views/Home.test.js`
- Modify: `README.md`
- Create: `.gitignore`

**Interfaces:**
- Consumes: `GET /dashboard/summary`。
- Produces: 四张统计卡、低库存列表、快捷入口、失败重试；可复现的项目启动与验收说明。

- [ ] **Step 1: 写首页加载与重试测试**

```javascript
it('shows metrics returned by database', async () => {
  vi.mocked(getDashboardSummary).mockResolvedValue({
    dessertCount: 12,
    categoryCount: 4,
    totalStock: 86,
    lowStockCount: 2,
    lowStockDesserts: [{ id: 1, name: '草莓慕斯', stock: 2 }]
  })
  const wrapper = mount(Home)
  await flushPromises()
  expect(wrapper.text()).toContain('12')
  expect(wrapper.text()).toContain('86')
  expect(wrapper.text()).toContain('草莓慕斯')
})
```

- [ ] **Step 2: 实现首页 API 与页面**

```javascript
export const getDashboardSummary = () => request.get('/dashboard/summary')
```

首页挂载时读取统计；加载中显示骨架；失败显示错误文案和重试按钮；四张卡分别展示甜品总数、分类数、总库存和低库存数；低库存列表最多五条；快捷入口通过命名路由进入甜品和分类页面。

- [ ] **Step 3: 清理仓库并补充运行说明**

`.gitignore` 至少包含：

```gitignore
.idea/
backend/target/
backend/uploads/
frontend/node_modules/
frontend/dist/
*.sql
```

`README.md` 使用 UTF-8 中文说明 Java 17、MySQL 8、Node.js 版本要求、数据库名、后端启动命令、前端启动命令、默认上传目录和功能截图位置；不得写入真实数据库密码。

- [ ] **Step 4: 执行自动化验证**

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm test
npm run build
```

预期：Maven 全量测试通过；Vitest 全量测试通过；Vite 生产构建成功且无错误。

- [ ] **Step 5: 执行真实数据库完整流程**

同时启动 MySQL、Spring Boot 和 Vite 后依次验证：

1. 现有账号可以登录，响应和浏览器存储中不暴露密码。
2. 新增“验收分类”。
3. 上传 PNG 图片并新增库存为 `3` 的“验收甜品”。
4. 按名称与分类均能筛选到该甜品。
5. 将价格改为 `29.90`、库存改为 `8`。
6. 下架后刷新页面，状态仍为下架。
7. 首页甜品数、总库存和低库存数随操作正确变化。
8. 删除甜品后可以删除“验收分类”。
9. 原有用户数和甜品数据未丢失。
10. 在 `1366×768` 与 `1920×1080` 下检查登录、首页、分类和甜品页面无溢出、遮挡或配色断层。

- [ ] **Step 6: 最终提交**

```powershell
git add README.md .gitignore frontend/src
git commit -m "feat: complete phase one dessert management experience"
git status --short
```

预期：最终提交成功；`git status --short` 不包含本阶段应提交的源代码、测试或文档。

---

## 实施顺序与停止条件

严格按 Task 1 至 Task 8 顺序执行。每项任务只有在对应测试通过并完成独立提交后才进入下一项。遇到真实数据库结构与迁移假设不一致时，停止启动应用，保留备份，先根据 `SHOW CREATE TABLE user;` 和 `SHOW CREATE TABLE dessert;` 修订 V1 迁移，禁止通过删表或清库绕过迁移问题。

## 计划自检

- 规格中的数据库保留、分类、甜品、上传、首页、视觉、错误处理、测试和文档均有对应任务。
- 订单、库存流水、细粒度权限、云存储和顾客商城未进入本计划。
- 后端接口路径、Java 字段类型与前端 API 名称在各任务间保持一致。
- 计划中没有未定义的占位任务；所有验收命令和预期结果均已明确。
