# Sweet Dessert System

一个适合作为简历项目展示的甜品商城与门店管理系统。项目采用 Vue 3 + Spring Boot + Spring Security + MyBatis Plus + MySQL，同时提供顾客商城和管理员后台，覆盖账号、商品、分类、订单、库存和经营看板等核心业务。

## 功能亮点

- 顾客仅凭用户名和密码注册，BCrypt 加密保存密码
- JWT 无状态登录、接口权限和前端角色路由保护
- 顾客商城、商品搜索、本地购物车和本人订单查询
- 顾客订单自动关联登录账户，不能查看或取消他人订单
- 唯一管理员可查看、启用或停用普通用户
- 甜品分类新增、重命名和安全删除
- 甜品分页、搜索、分类筛选、增改删和上下架
- 本地甜品图片上传、库存数量与低库存提醒
- 订单创建、查询、完成和取消
- 创建订单自动扣减库存，取消订单自动恢复库存
- 手工入库/出库及完整库存流水
- 首页实时展示商品、分类、库存、当日订单与销售额
- Flyway 自动管理数据库版本，便于在新环境部署
- “深焙可可”响应式顾客商城与管理后台界面

## 技术栈

- 前端：Vue 3、Vue Router、Pinia、Element Plus、Axios、Vite
- 后端：Java 17、Spring Boot、Spring Security、JWT、BCrypt、MyBatis Plus、MySQL、Flyway
- 测试：JUnit 5、Mockito、Spring Boot Test

## 数据库准备

要求：Java 17 或更高版本、MySQL 8、Node.js 20 或更高版本。

1. 创建空数据库：

```sql
CREATE DATABASE dessert_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

2. 参考根目录的 `.env.example`，在当前 PowerShell 会话中设置数据库账号。不要把真实密码提交到 Git：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的本机 MySQL 密码"
$env:JWT_SECRET="至少 32 字节的随机字符串"
```

3. 启动后端时，Flyway 会自动执行 `backend/src/main/resources/db/migration/` 下的版本脚本。新数据库会自动创建表并写入演示数据；旧版非空数据库会从基线版本继续升级，无需手动执行 SQL。

默认演示管理员为 `admin / admin123`，密码以 BCrypt 哈希保存，仅用于本地展示，请勿用于生产环境。V5 会将旧版数据库中仍为明文的 `admin` 密码重置为该演示密码，不影响其他账户。

## 本地运行

启动后端：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

如果使用 Git 忽略的 `backend/application-local.properties` 保存本机数据库账号，也可以直接运行：

```powershell
cd backend
.\start-local.ps1
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

前端开发地址为 `http://localhost:5173`，后端接口地址为 `http://localhost:8080`。顾客商城位于 `/shop`，管理员后台位于 `/admin`，上传图片保存在 `backend/uploads/desserts/`。

所有业务接口统一使用 `/api` 前缀；开发服务器会将该前缀原样转发给后端，图片访问路径继续使用 `/uploads`。

## 核心业务流程

1. 普通用户注册后登录，服务端签发两小时有效的 JWT；账号状态会在每次请求时重新从数据库确认。
2. 顾客可以浏览上架甜品，将商品加入本地购物车并创建只属于自己的订单。
3. 创建订单时锁定对应甜品库存，校验并扣减数量，同时生成 `ORDER_OUT` 库存流水。
4. 顾客只能查看和取消自己的待处理订单；取消时恢复库存并生成 `ORDER_RETURN` 流水。
5. 管理员可以完成或取消订单、调整库存，并可停用普通用户；已停用账户持有的旧 JWT 也会立即失效。

## 项目验证

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm test
npm run build
```

预期结果：后端测试全部通过，前端生成 `dist` 生产构建。

## 数据库表

| 表名 | 用途 |
| --- | --- |
| `user` | 唯一管理员与可注册顾客账号、角色和启停状态 |
| `category` | 甜品分类 |
| `dessert` | 甜品商品、价格、状态和实时库存 |
| `orders` | 订单主表及顾客账户归属 |
| `order_detail` | 订单商品明细和成交快照 |
| `stock_record` | 手工及订单产生的库存流水 |

详细字段与关系见 [数据库设计](docs/database-design.md)。
