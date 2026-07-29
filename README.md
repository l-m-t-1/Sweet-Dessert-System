# Sweet Dessert System

一个适合作为简历项目展示的甜品门店管理后台，采用 Vue 3 + Spring Boot + MyBatis Plus + MySQL，覆盖商品、分类、订单、库存和经营看板等核心业务。

## 功能亮点

- 管理员登录与前端路由保护
- 甜品分类新增、重命名和安全删除
- 甜品分页、搜索、分类筛选、增改删和上下架
- 本地甜品图片上传、库存数量与低库存提醒
- 订单创建、查询、完成和取消
- 创建订单自动扣减库存，取消订单自动恢复库存
- 手工入库/出库及完整库存流水
- 首页实时展示商品、分类、库存、当日订单与销售额
- Flyway 自动管理数据库版本，便于在新环境部署
- “深焙可可”响应式管理后台界面

## 技术栈

- 前端：Vue 3、Vue Router、Pinia、Element Plus、Axios、Vite
- 后端：Java 17、Spring Boot、MyBatis Plus、MySQL、Flyway
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
```

3. 启动后端时，Flyway 会自动执行 `backend/src/main/resources/db/migration/` 下的版本脚本。新数据库会自动创建表并写入演示数据；旧版非空数据库会从基线版本继续升级，无需手动执行 SQL。

默认演示管理员为 `admin / admin123`，仅用于本地展示，请勿用于生产环境。

## 本地运行

启动后端：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

前端开发地址为 `http://localhost:5173`，后端接口地址为 `http://localhost:8080`。上传图片保存在 `backend/uploads/desserts/`。

## 核心业务流程

1. 创建订单时锁定对应甜品库存，校验库存后扣减数量。
2. 每次订单扣库会生成 `ORDER_OUT` 库存流水。
3. 待处理订单可以标记为已完成，也可以取消。
4. 取消订单会在同一事务中恢复库存，并生成 `ORDER_RETURN` 流水。
5. 管理员还可以手工入库或出库，所有变化均可追溯。

## 项目验证

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm run build
```

预期结果：后端测试全部通过，前端生成 `dist` 生产构建。

## 数据库表

| 表名 | 用途 |
| --- | --- |
| `user` | 管理员和员工账号 |
| `category` | 甜品分类 |
| `dessert` | 甜品商品、价格、状态和实时库存 |
| `orders` | 订单主表 |
| `order_detail` | 订单商品明细和成交快照 |
| `stock_record` | 手工及订单产生的库存流水 |

详细字段与关系见 [数据库设计](docs/database-design.md)。
