# 甜品管理系统数据库、订单与库存设计

**日期：** 2026-07-29  
**状态：** 已确认  
**目标：** 在保留现有 Vue 3、Spring Boot、MyBatis Plus 和 MySQL 技术栈的基础上，建立可自动迁移、可重复初始化、具备事务一致性的订单与库存持久化能力。

## 1. 当前问题

项目已经通过 `spring.datasource.*` 连接 MySQL，用户、分类、甜品和仪表盘也已读取真实数据库，但仍存在以下缺口：

- 数据库升级 SQL 被根目录 `.gitignore` 中的 `*.sql` 规则排除，无法随项目发布。
- 项目没有数据库版本管理工具，新环境无法自动建立或升级表结构。
- 库存流水、订单和订单详情只存在于数据库设计文档中，没有后端、前端和测试实现。
- 当前库存更新没有完整的业务流水，无法解释每次库存变化的来源。
- 订单创建与库存扣减之间没有事务边界，未来实现时可能产生数据不一致或超卖。
- 用户列表接口可能序列化密码字段。
- 缺少不会覆盖用户数据的演示初始化方案。

## 2. 技术路线

采用 Flyway、MyBatis Plus 和 Spring 声明式事务：

- Flyway 管理 MySQL 表结构、索引和演示数据版本。
- MyBatis Plus 保留现有实体、Mapper 和 Service 风格。
- 复杂查询继续使用显式 SQL，避免为了订单功能改写成 JPA。
- `@Transactional` 覆盖订单、订单明细、库存扣减和库存流水。
- 数据库账号和密码继续从环境变量读取，不进入源码或 Git 历史。

不采用手工建表作为主要流程，也不重写为 JPA/Hibernate。

## 3. 数据模型

### 3.1 用户表 `user`

保留现有管理员账号数据。登录和用户查询响应不得包含密码。

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | 主键、自增 | 用户 ID |
| username | VARCHAR(50) | 唯一、非空 | 登录名 |
| password | VARCHAR(100) | 非空 | 现阶段兼容已有密码；后续可单独升级加密 |
| role | VARCHAR(20) | 非空 | 用户角色 |
| create_time | DATETIME | 非空 | 创建时间 |
| update_time | DATETIME | 非空 | 更新时间 |

### 3.2 分类表 `category`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | 主键、自增 | 分类 ID |
| name | VARCHAR(50) | 唯一、非空 | 分类名称 |
| create_time | DATETIME | 非空 | 创建时间 |
| update_time | DATETIME | 非空 | 更新时间 |

分类存在甜品引用时禁止删除。

### 3.3 甜品表 `dessert`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | 主键、自增 | 甜品 ID |
| name | VARCHAR(100) | 非空 | 甜品名称 |
| category_id | BIGINT | 外键、非空 | 分类 ID |
| price | DECIMAL(10,2) | 非空、非负 | 当前售价 |
| stock | INT | 非空、非负 | 当前库存 |
| image | VARCHAR(255) | 可空 | 图片地址 |
| description | TEXT | 可空 | 描述 |
| status | TINYINT | 非空 | 1 上架，0 下架 |
| create_time | DATETIME | 非空 | 创建时间 |
| update_time | DATETIME | 非空 | 更新时间 |

为 `category_id`、`status` 和 `update_time` 建立索引。

### 3.4 库存流水表 `stock_record`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | 主键、自增 | 流水 ID |
| dessert_id | BIGINT | 外键、非空 | 甜品 ID |
| order_id | BIGINT | 可空 | 关联订单 |
| change_quantity | INT | 非零 | 正数入库，负数出库 |
| before_stock | INT | 非空 | 操作前库存 |
| after_stock | INT | 非空 | 操作后库存 |
| type | VARCHAR(30) | 非空 | MANUAL_IN、MANUAL_OUT、ORDER_OUT、ORDER_RETURN |
| remark | VARCHAR(255) | 可空 | 调整原因 |
| create_time | DATETIME | 非空 | 操作时间 |

为 `dessert_id`、`order_id`、`type` 和 `create_time` 建立索引。

### 3.5 订单表 `orders`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | 主键、自增 | 订单 ID |
| order_no | VARCHAR(32) | 唯一、非空 | 业务订单号 |
| customer_name | VARCHAR(50) | 非空 | 客户称呼 |
| customer_phone | VARCHAR(30) | 可空 | 联系方式 |
| total_amount | DECIMAL(12,2) | 非空、非负 | 订单总额 |
| status | VARCHAR(20) | 非空 | CREATED、COMPLETED、CANCELLED |
| remark | VARCHAR(255) | 可空 | 订单备注 |
| create_time | DATETIME | 非空 | 创建时间 |
| update_time | DATETIME | 非空 | 更新时间 |

为 `order_no`、`status` 和 `create_time` 建立索引。

### 3.6 订单详情表 `order_detail`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | 主键、自增 | 明细 ID |
| order_id | BIGINT | 外键、非空 | 订单 ID |
| dessert_id | BIGINT | 外键、非空 | 甜品 ID |
| dessert_name | VARCHAR(100) | 非空 | 下单时商品名称快照 |
| unit_price | DECIMAL(10,2) | 非空、非负 | 下单时成交单价 |
| quantity | INT | 非空、正数 | 购买数量 |
| subtotal | DECIMAL(12,2) | 非空、非负 | 单价乘数量 |
| create_time | DATETIME | 非空 | 创建时间 |

名称和价格使用快照，避免甜品改名或调价后历史订单变化。

## 4. 数据库迁移与初始化

引入 `flyway-core` 和 MySQL Flyway 支持，并启用启动时迁移。迁移文件放在 `backend/src/main/resources/db/migration/`：

- `V1__baseline_existing_schema.sql`：以幂等方式建立或补齐用户、分类和甜品表。
- `V2__orders_and_stock_records.sql`：建立订单、订单详情和库存流水表及索引。
- `V3__seed_demo_data.sql`：只在对应数据不存在时插入演示分类、甜品和管理员。

根目录 `.gitignore` 改为继续忽略普通 SQL 导出文件，但明确允许迁移目录：

```gitignore
*.sql
!backend/src/main/resources/db/
!backend/src/main/resources/db/migration/
!backend/src/main/resources/db/migration/*.sql
```

迁移不得删除现有用户数据，也不得依赖每次启动重复执行的 `schema.sql`。

## 5. 订单事务与库存一致性

### 5.1 创建订单

1. 验证客户名称和订单项。
2. 开启数据库事务。
3. 按甜品 ID 查询数据库中的最新状态、价格和库存。
4. 拒绝不存在、已下架、数量非法或库存不足的商品。
5. 为每个订单项执行带条件的原子扣减：

```sql
UPDATE dessert
SET stock = stock - :quantity
WHERE id = :dessertId
  AND status = 1
  AND stock >= :quantity;
```

6. 根据数据库价格计算订单明细小计和总额，忽略前端提交的价格。
7. 保存订单、订单详情和 `ORDER_OUT` 库存流水。
8. 任意一步影响行数异常或写入失败时抛出业务异常并整体回滚。

### 5.2 手动库存调整

- 入库数量必须大于零。
- 出库数量必须大于零且不能使库存小于零。
- 调整使用原子 SQL 更新库存。
- 每次调整都保存操作前库存、操作后库存、类型和原因。

### 5.3 订单状态

- `CREATED` 可以转为 `COMPLETED` 或 `CANCELLED`。
- `COMPLETED` 和 `CANCELLED` 为终态。
- 取消 `CREATED` 订单时返还所有明细库存，并写入 `ORDER_RETURN` 流水。
- 已完成或已取消订单不得再次取消，避免重复返还库存。

## 6. 后端接口

所有接口返回统一 `ApiResponse<T>`，业务错误由全局异常处理器转换为中文提示。

### 6.1 库存

- `GET /api/stock-records`：按甜品、类型、时间分页查询流水。
- `POST /api/stock-records/adjust`：提交甜品 ID、调整方向、数量和原因。

### 6.2 订单

- `GET /api/orders`：按订单号、状态分页查询。
- `GET /api/orders/{id}`：查询订单及明细。
- `POST /api/orders`：创建多商品订单。
- `PUT /api/orders/{id}/complete`：将已创建订单标记为已完成。
- `PUT /api/orders/{id}/cancel`：取消已创建订单并返还库存。

### 6.3 仪表盘

`GET /api/dashboard/summary` 在现有甜品和库存数据上增加：

- 今日订单数。
- 今日销售额，不统计已取消订单。
- 低库存甜品数量。
- 最近订单列表。

## 7. 前端设计

### 7.1 导航

在现有后台布局中增加：

- 订单管理。
- 库存流水。

### 7.2 订单管理

- 支持订单号和状态筛选、分页。
- 创建订单使用抽屉或弹窗，可增加多个甜品并调整数量。
- 前端实时展示预计金额，但最终金额以后端数据库计算结果为准。
- 订单详情展示客户信息、状态、总额和商品快照。
- 只有 `CREATED` 状态显示“完成”和“取消”操作。

### 7.3 库存流水

- 显示甜品、变化量、前后库存、类型、订单号、备注和时间。
- 入库使用绿色，出库与订单扣减使用暖红色，订单返还使用蓝色。
- 支持按甜品和类型筛选。
- 手动调整库存时必须填写原因。

### 7.4 仪表盘

增加今日订单、今日销售额、低库存数量和最近订单模块，数据全部来自 MySQL。

## 8. 安全与错误处理

- 数据库密码使用 `${DB_PASSWORD:}`，不提交真实密码。
- 用户列表与登录接口使用响应 DTO，不序列化密码。
- 所有数量、金额和状态在后端重新验证。
- 不信任前端传入的价格、库存和订单总额。
- 外键冲突、唯一键冲突和库存不足转换为明确业务提示。
- 日志不得输出数据库密码或完整登录密码。

密码哈希升级不包含在本阶段，以避免在不了解现有账号迁移方式时破坏登录数据；该项作为后续独立安全阶段处理。

## 9. 测试与验收

### 9.1 自动化测试

- Flyway 能从空数据库建立完整结构。
- 同一数据库重复启动不会重复建表或覆盖数据。
- 创建订单会写入订单、明细、库存扣减和库存流水。
- 多商品订单任一商品库存不足时整体回滚。
- 下架商品不能下单。
- 手动出库不能产生负库存。
- 取消订单返还库存并保存流水。
- 重复取消不会重复返还库存。
- 用户接口响应不包含密码。
- 仪表盘统计不包含已取消订单销售额。

### 9.2 最终验证

- `backend\mvnw.cmd test` 全部通过。
- `frontend\npm run build` 成功。
- 使用真实 MySQL 完成：迁移、创建订单、扣减库存、完成订单、取消订单、库存返还和测试数据清理。
- 关闭后重新启动后端后，数据库数据仍然存在。
- GitHub 仓库包含全部迁移脚本、README 运行说明和环境变量示例。

## 10. 范围边界

本阶段不包含在线支付、顾客端商城、配送、优惠券、复杂权限系统、并发压测和密码迁移。这些功能不影响本阶段形成完整、可信、适合简历展示的数据库业务闭环。
