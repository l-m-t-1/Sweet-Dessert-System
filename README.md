# Sweet Dessert System

一个基于 Vue 3、Spring Boot、MyBatis Plus 和 MySQL 的甜品管理后台。

## 已完成功能

- 管理员登录与路由保护
- 甜品分类新增、重命名和安全删除
- 甜品分页、搜索、分类筛选、增改删和上下架
- 本地甜品图片上传
- 库存与低库存提醒
- 数据库实时经营概览
- “深焙可可”响应式后台界面

## 本地运行

要求：Java 17 或更高版本、MySQL 8、Node.js 20 或更高版本。

1. 创建数据库 `dessert_system`，并在 PowerShell 中设置本机数据库账号：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
```
2. 首次升级旧数据库时执行 `backend/src/main/resources/db/migration/V1__category_and_dessert_fields.sql`。
3. 启动后端：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

4. 启动前端：

```powershell
cd frontend
npm install
npm run dev
```

前端开发地址默认为 `http://localhost:5173`，后端默认为 `http://localhost:8080`。上传图片保存在 `backend/uploads/desserts/`。

## 验证

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm run build
```
