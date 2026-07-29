# API 前缀统一设计

## 背景与问题

前端 Axios 实例以 `/api` 为基础路径，但 Vite 开发代理会删除这个前缀。旧接口位于 `/category`、`/dessert` 等路径，因此可以正常访问；新订单和库存接口本身位于 `/api/orders`、`/api/stock-records`，经过代理后却变成 `/orders`、`/stock-records`，最终由后端返回错误。

## 目标

- 所有业务接口统一使用 `/api/...`。
- 开发环境代理只转发请求，不改写路径。
- `/uploads/...` 静态图片路径保持不变。
- 修复订单和库存页面，同时避免登录、分类、甜品、首页和上传功能回归。

## 方案

### 后端

将以下控制器的基础路径补上 `/api`：

- `/user` → `/api/user`
- `/category` → `/api/category`
- `/dessert` → `/api/dessert`
- `/dashboard` → `/api/dashboard`
- `/upload` → `/api/upload`

订单 `/api/orders` 和库存 `/api/stock-records` 保持不变。

### 前端与开发代理

- Axios 继续使用 `baseURL: '/api'`，业务 API 文件无需拼接重复前缀。
- Vite `/api` 代理删除 `rewrite`，把原始 `/api/...` 路径完整转发到 8080。
- `/uploads` 代理保持原样，用于展示后端保存的图片。

## 数据流

浏览器调用 `request.get('/orders')` 后，请求路径为 `/api/orders`；Vite 将其原样转发到 `http://localhost:8080/api/orders`；控制器查询 MySQL 并返回统一的 `ApiResponse`。

## 兼容与错误处理

- 这是本地开发阶段的接口规范统一，不保留无前缀的旧接口别名，避免维护两套地址。
- 数据库表和已有数据不发生变化。
- 后端异常继续由全局异常处理器转换为统一响应；前端拦截器继续显示后端消息。

## 验证标准

- 自动检查所有业务控制器都以 `/api` 开头，并确认 Vite 不再移除 `/api`。
- 后端完整测试通过，前端生产构建成功。
- 经 `localhost:5173` 代理访问登录、分类、甜品、首页、订单、库存接口均返回正常响应。
- 图片上传经 `/api/upload/dessert` 成功，返回的 `/uploads/...` 地址可访问。
