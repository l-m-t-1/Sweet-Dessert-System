# API Prefix Unification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every browser API request keep a single `/api` prefix from Axios through the Vite proxy to Spring Boot.

**Architecture:** Spring Boot controllers expose all business routes below `/api`, while uploaded files remain below `/uploads`. Axios retains `baseURL: '/api'`; Vite forwards `/api` without rewriting it.

**Tech Stack:** Java 17, Spring Boot 4, JUnit 5, Vue 3, Vite 8, Node.js built-in test runner

## Global Constraints

- Every business controller base path starts with `/api`.
- Vite must forward `/api/...` unchanged to `http://localhost:8080`.
- `/uploads/...` remains outside the API prefix and keeps its existing proxy.
- Database schema and stored data must not change.

---

### Task 1: Enforce the backend API prefix

**Files:**
- Create: `backend/src/test/java/com/sweet/dessertsystem/web/ApiPrefixMappingTests.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/controller/UserController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/controller/DessertController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/category/CategoryController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/upload/UploadController.java`

**Interfaces:**
- Consumes: Spring MVC `@RequestMapping` annotations on the seven business controllers.
- Produces: `/api/user`, `/api/category`, `/api/dessert`, `/api/dashboard`, `/api/upload`, `/api/orders`, and `/api/stock-records`.

- [ ] **Step 1: Write the failing controller-prefix test**

```java
package com.sweet.dessertsystem.web;

import com.sweet.dessertsystem.category.CategoryController;
import com.sweet.dessertsystem.controller.DessertController;
import com.sweet.dessertsystem.controller.UserController;
import com.sweet.dessertsystem.dashboard.DashboardController;
import com.sweet.dessertsystem.order.OrderController;
import com.sweet.dessertsystem.stock.StockRecordController;
import com.sweet.dessertsystem.upload.UploadController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ApiPrefixMappingTests {
    @Test
    void allBusinessControllersUseApiPrefix() {
        Map<Class<?>, String> expectedMappings = Map.of(
                UserController.class, "/api/user",
                CategoryController.class, "/api/category",
                DessertController.class, "/api/dessert",
                DashboardController.class, "/api/dashboard",
                UploadController.class, "/api/upload",
                OrderController.class, "/api/orders",
                StockRecordController.class, "/api/stock-records"
        );

        expectedMappings.forEach((controller, expected) -> {
            RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
            assertArrayEquals(new String[]{expected}, mapping.value(), controller.getSimpleName());
        });
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
cd backend
.\mvnw.cmd -Dtest=ApiPrefixMappingTests test
```

Expected: FAIL because `UserController`, `CategoryController`, `DessertController`, `DashboardController`, and `UploadController` do not yet start with `/api`.

- [ ] **Step 3: Add `/api` to the five legacy controller mappings**

Change the annotations to:

```java
@RequestMapping("/api/user")
@RequestMapping("/api/category")
@RequestMapping("/api/dessert")
@RequestMapping("/api/dashboard")
@RequestMapping("/api/upload")
```

Leave the order and stock controller mappings unchanged.

- [ ] **Step 4: Run the focused test and full backend tests**

Run:

```powershell
.\mvnw.cmd -Dtest=ApiPrefixMappingTests test
.\mvnw.cmd test
```

Expected: the focused test passes and all backend tests pass with zero failures.

- [ ] **Step 5: Commit the backend contract**

```powershell
git add backend/src/main/java backend/src/test/java/com/sweet/dessertsystem/web/ApiPrefixMappingTests.java
git commit -m "fix: unify backend api prefix"
```

### Task 2: Preserve `/api` in the Vite proxy

**Files:**
- Create: `frontend/test/viteProxy.test.js`
- Modify: `frontend/package.json`
- Modify: `frontend/vite.config.js`

**Interfaces:**
- Consumes: Axios `baseURL: '/api'` and Spring Boot routes from Task 1.
- Produces: unchanged proxy forwarding from `/api/orders` to `http://localhost:8080/api/orders`.

- [ ] **Step 1: Add a failing proxy configuration test**

Add `"test": "node --test"` to `frontend/package.json` and create:

```javascript
import test from 'node:test'
import assert from 'node:assert/strict'
import config from '../vite.config.js'

test('api proxy preserves the api prefix', () => {
  const proxy = config.server.proxy['/api']

  assert.equal(proxy.target, 'http://localhost:8080')
  assert.equal(Object.hasOwn(proxy, 'rewrite'), false)
})

test('uploads keep their original path', () => {
  const proxy = config.server.proxy['/uploads']

  assert.equal(proxy.target, 'http://localhost:8080')
  assert.equal(Object.hasOwn(proxy, 'rewrite'), false)
})
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
cd frontend
npm test
```

Expected: FAIL because the `/api` proxy currently contains `rewrite`.

- [ ] **Step 3: Remove the `/api` rewrite**

Use this proxy configuration:

```javascript
'/api': {
  target: 'http://localhost:8080',
  changeOrigin: true,
},
```

Keep the `/uploads` proxy unchanged.

- [ ] **Step 4: Run the frontend test and production build**

Run:

```powershell
npm test
npm run build
```

Expected: both proxy tests pass and Vite completes the production build.

- [ ] **Step 5: Commit the proxy fix**

```powershell
git add frontend/package.json frontend/vite.config.js frontend/test/viteProxy.test.js
git commit -m "fix: preserve api prefix in dev proxy"
```

### Task 3: Merge, restart, and verify the browser-to-database path

**Files:**
- Modify: `README.md`
- Runtime-only: `backend/application-local.properties` remains ignored by Git.

**Interfaces:**
- Consumes: unchanged `/api` routing and the existing local MySQL account.
- Produces: working login, dashboard, category, dessert, upload, order, and stock calls through port 5173.

- [ ] **Step 1: Document the unified base path**

Add this sentence to the startup section:

```markdown
所有业务接口统一使用 `/api` 前缀；开发服务器会将该前缀原样转发给后端，图片访问路径继续使用 `/uploads`。
```

- [ ] **Step 2: Commit the documentation**

```powershell
git add README.md
git commit -m "docs: document unified api routing"
```

- [ ] **Step 3: Fast-forward the actual project branch and restart backend**

Run:

```powershell
git merge --ff-only codex/database-orders-inventory
cd backend
.\start-local.ps1
```

Expected: Flyway validates four migrations, reports schema version 3, and Spring Boot listens on 8080.

- [ ] **Step 4: Restart Vite so it loads the new proxy configuration**

Run:

```powershell
cd frontend
npm run dev
```

Expected: Vite listens on 5173 and uses the proxy without `rewrite`.

- [ ] **Step 5: Verify every API boundary through Vite**

Run HTTP checks for:

```text
GET  /api/category
GET  /api/dessert/page?page=1&size=1
GET  /api/dashboard/summary
GET  /api/orders?page=1&size=10
GET  /api/stock-records?page=1&size=10
POST /api/upload/dessert
```

Expected: every GET returns HTTP 200; upload returns HTTP 200 with a non-empty `/uploads/desserts/...` path, and the test upload is removed afterward.

- [ ] **Step 6: Final verification**

Run:

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm test
npm run build
```

Expected: all backend and frontend tests pass, production build succeeds, and `git status --short` is empty.
