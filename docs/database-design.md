# 甜品管理系统数据库设计

数据库由 Flyway 版本脚本自动创建和升级，脚本位于 `backend/src/main/resources/db/migration/`。

## 表关系

- `category` 1:N `dessert`
- `user` 1:N `orders`（顾客订单归属）
- `orders` 1:N `order_detail`
- `dessert` 1:N `order_detail`
- `dessert` 1:N `stock_record`
- `orders` 1:N `stock_record`（仅订单相关流水）

## 1. 用户表 `user`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `username` | varchar(30) | 唯一登录名 |
| `password` | varchar(255) | BCrypt 密码哈希 |
| `role` | varchar(20) | `ADMIN` 或 `USER` |
| `status` | tinyint | 1 启用，0 停用 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |

## 2. 分类表 `category`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `user_id` | bigint | 顾客账户 ID，后台历史订单可为空 |
| `name` | varchar(50) | 分类名称 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |

## 3. 甜品表 `dessert`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `name` | varchar(100) | 甜品名称 |
| `category_id` | bigint | 分类 ID |
| `price` | decimal(10,2) | 当前售价 |
| `stock` | int | 当前库存 |
| `image` | varchar(255) | 图片地址 |
| `description` | text | 商品介绍 |
| `status` | tinyint | 1 上架，0 下架 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |

## 4. 订单主表 `orders`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `order_no` | varchar(50) | 唯一订单号 |
| `customer_name` | varchar(100) | 顾客姓名 |
| `customer_phone` | varchar(30) | 联系电话 |
| `total_amount` | decimal(10,2) | 订单总金额 |
| `status` | varchar(20) | `CREATED`、`COMPLETED`、`CANCELLED` |
| `remark` | varchar(255) | 订单备注 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |

## 5. 订单明细表 `order_detail`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `order_id` | bigint | 订单 ID |
| `dessert_id` | bigint | 甜品 ID |
| `dessert_name` | varchar(100) | 下单时商品名称快照 |
| `unit_price` | decimal(10,2) | 下单时单价快照 |
| `quantity` | int | 购买数量 |
| `subtotal` | decimal(10,2) | 明细小计 |

保存名称与价格快照后，即使商品后来改名或调价，历史订单仍能保持原始成交信息。

## 6. 库存流水表 `stock_record`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `dessert_id` | bigint | 甜品 ID |
| `order_id` | bigint | 关联订单 ID，手工调整时为空 |
| `change_type` | varchar(20) | `MANUAL_IN`、`MANUAL_OUT`、`ORDER_OUT`、`ORDER_RETURN` |
| `quantity` | int | 正数入库、负数出库 |
| `stock_before` | int | 变化前库存 |
| `stock_after` | int | 变化后库存 |
| `remark` | varchar(255) | 调整原因 |
| `create_time` | datetime | 创建时间 |

## 一致性策略

- 顾客订单写入当前 JWT 对应的用户 ID，查询、查看详情和取消均附带用户归属条件。
- JWT 每次鉴权都会读取当前账户状态，停用账户无需等待令牌过期。
- 创建订单、扣减库存、写入订单明细和库存流水在同一个数据库事务内完成。
- 扣库前通过行锁读取商品，防止并发订单造成超卖。
- 取消订单、恢复库存和写入回库流水也在同一个事务内完成。
- 业务状态限制重复完成或重复取消，避免重复扣库、回库。
