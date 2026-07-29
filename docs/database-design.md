# 甜品管理系统数据库设计

## 1. 用户表 user

用途：
存储管理员和员工账号信息。

字段：

|字段|类型|说明|
|-|-|-|
|id|bigint|主键|
|username|varchar(50)|用户名|
|password|varchar(100)|密码|
|role|varchar(20)|角色|
|create_time|datetime|创建时间|


---

## 2. 甜品分类表 category

用途：
管理甜品分类。


字段：

|字段|类型|说明|
|-|-|-|
|id|bigint|主键|
|name|varchar(50)|分类名称|
|create_time|datetime|创建时间|


---

## 3. 甜品商品表 dessert

用途：
保存甜品信息。


字段：

|字段|类型|说明|
|-|-|-|
|id|bigint|主键|
|name|varchar(100)|甜品名称|
|category_id|bigint|分类ID|
|price|decimal|价格|
|stock|int|库存|
|image|varchar(255)|图片|
|description|text|介绍|
|status|int|状态|


---

## 4. 库存记录表 stock

用途：
记录库存变化。


字段：

|字段|类型|说明|
|-|-|-|
|id|bigint|主键|
|dessert_id|bigint|甜品ID|
|quantity|int|数量|
|type|varchar(20)|入库/出库|
|create_time|datetime|时间|


---

## 5. 订单表 orders

用途：
保存订单。


字段：

|字段|类型|说明|
|-|-|-|
|id|bigint|主键|
|order_no|varchar(50)|订单编号|
|user_id|bigint|用户ID|
|total_price|decimal|总金额|
|status|varchar(20)|订单状态|
|create_time|datetime|创建时间|


---

## 6. 订单详情表 order_detail

用途：
记录订单购买商品。


字段：

|字段|类型|说明|
|-|-|-|
|id|bigint|主键|
|order_id|bigint|订单ID|
|dessert_id|bigint|甜品ID|
|quantity|int|数量|
|price|decimal|单价|