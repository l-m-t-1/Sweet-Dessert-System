# Database Orders and Inventory Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add versioned MySQL migrations, secure user responses, transactional inventory records, multi-item orders, database-backed dashboard metrics, and matching Vue management pages.

**Architecture:** Flyway owns schema evolution while MyBatis Plus remains the persistence layer. Order creation, cancellation, stock adjustment, and stock history writes run inside Spring transactions; the frontend consumes unified `ApiResponse` endpoints and never calculates authoritative prices or inventory.

**Tech Stack:** Java 17, Spring Boot 4, MyBatis Plus 3.5, Flyway, MySQL 8, JUnit 5, Mockito, Vue 3, Vue Router, Element Plus, Vite.

## Global Constraints

- Preserve existing MySQL user, category, and dessert data.
- Database credentials remain environment variables and never enter Git.
- All new backend behavior follows test-first red-green-refactor.
- Authoritative price, total amount, status, and stock checks happen in the backend.
- Order, details, stock updates, and stock records commit or roll back together.
- Do not add payment, delivery, coupons, customer storefront, or a new permission framework.

---

### Task 1: Versioned Database Schema and Demo Data

**Files:**
- Modify: `.gitignore`
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.properties`
- Replace: `backend/src/main/resources/db/migration/V1__category_and_dessert_fields.sql`
- Create: `backend/src/main/resources/db/migration/V1__baseline_core_schema.sql`
- Create: `backend/src/main/resources/db/migration/V2__orders_and_stock_records.sql`
- Create: `backend/src/main/resources/db/migration/V3__seed_demo_data.sql`
- Test: `backend/src/test/java/com/sweet/dessertsystem/migration/MigrationResourcesTests.java`

**Interfaces:**
- Produces tables `user`, `category`, `dessert`, `orders`, `order_detail`, and `stock_record`.
- Produces Flyway configuration used by all later persistence tasks.

- [ ] **Step 1: Write the failing migration resource test**

```java
class MigrationResourcesTests {
    @Test
    void migrationFilesArePackagedAndContainRequiredTables() throws Exception {
        String orders = resource("db/migration/V2__orders_and_stock_records.sql");
        assertThat(orders).contains("CREATE TABLE IF NOT EXISTS orders");
        assertThat(orders).contains("CREATE TABLE IF NOT EXISTS order_detail");
        assertThat(orders).contains("CREATE TABLE IF NOT EXISTS stock_record");
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `backend\mvnw.cmd -Dtest=MigrationResourcesTests test`  
Expected: FAIL because V2 and V3 resources do not exist.

- [ ] **Step 3: Add Flyway and migration configuration**

Add `org.flywaydb:flyway-core` and `org.flywaydb:flyway-mysql`. Configure:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
spring.flyway.validate-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

Allow only migration SQL through `.gitignore`:

```gitignore
*.sql
!backend/src/main/resources/db/
!backend/src/main/resources/db/migration/
!backend/src/main/resources/db/migration/*.sql
```

V1 creates the full core schema for an empty database. Existing non-empty databases are baselined at version 1, so V2 and V3 run without reapplying the old core migration. V3 uses `INSERT ... SELECT ... WHERE NOT EXISTS` for every demo row.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run: `backend\mvnw.cmd -Dtest=MigrationResourcesTests test`  
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add .gitignore backend/pom.xml backend/src/main/resources backend/src/test/java/com/sweet/dessertsystem/migration
git commit -m "feat: add versioned mysql migrations"
```

### Task 2: Prevent Password Exposure

**Files:**
- Create: `backend/src/main/java/com/sweet/dessertsystem/dto/UserView.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/controller/UserController.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/dto/UserViewTests.java`

**Interfaces:**
- Produces `UserView(Long id, String username, String role)`.
- `GET /user/list` returns `ApiResponse<List<UserView>>`.

- [ ] **Step 1: Write the failing DTO serialization test**

```java
@Test
void userViewDoesNotExposePassword() throws Exception {
    User user = new User();
    user.setId(1L);
    user.setUsername("admin");
    user.setPassword("secret");
    user.setRole("ADMIN");
    String json = objectMapper.writeValueAsString(UserView.from(user));
    assertThat(json).contains("\"username\":\"admin\"");
    assertThat(json).doesNotContain("password").doesNotContain("secret");
}
```

- [ ] **Step 2: Run the test and verify RED**

Run: `backend\mvnw.cmd -Dtest=UserViewTests test`  
Expected: compilation failure because `UserView` is missing.

- [ ] **Step 3: Implement `UserView` and update the controller**

```java
public record UserView(Long id, String username, String role) {
    public static UserView from(User user) {
        return new UserView(user.getId(), user.getUsername(), user.getRole());
    }
}
```

Map the list through `UserView::from` and wrap it in `ApiResponse.ok`.

- [ ] **Step 4: Run focused and full tests**

Run: `backend\mvnw.cmd -Dtest=UserViewTests test` then `backend\mvnw.cmd test`  
Expected: all tests pass.

- [ ] **Step 5: Commit**

```powershell
git add backend/src/main/java/com/sweet/dessertsystem/dto/UserView.java backend/src/main/java/com/sweet/dessertsystem/controller/UserController.java backend/src/test/java/com/sweet/dessertsystem/dto/UserViewTests.java
git commit -m "fix: hide stored passwords from user responses"
```

### Task 3: Transactional Stock Adjustments and History

**Files:**
- Create: `backend/src/main/java/com/sweet/dessertsystem/stock/StockRecord.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/stock/StockRecordMapper.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/stock/StockAdjustmentRequest.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/stock/StockRecordView.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/stock/StockRecordPageResult.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/stock/StockRecordService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/stock/StockRecordController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/mapper/DessertMapper.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/stock/StockRecordServiceTests.java`

**Interfaces:**
- `StockAdjustmentRequest(Long dessertId, String direction, Integer quantity, String remark)`.
- `StockRecordService.adjust(StockAdjustmentRequest)` returns `StockRecordView`.
- `StockRecordService.page(long page, long size, Long dessertId, String type)` returns `StockRecordPageResult`.
- `DessertMapper.findByIdForUpdate(Long id)` locks a dessert row.
- `POST /api/stock-records/adjust` and `GET /api/stock-records`.

- [ ] **Step 1: Write failing tests for inbound, insufficient outbound, and required remark**

Tests assert that inbound stock creates `MANUAL_IN`, outbound stock cannot become negative, and a blank reason is rejected before any insert.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `backend\mvnw.cmd -Dtest=StockRecordServiceTests test`  
Expected: compilation failure because stock classes are missing.

- [ ] **Step 3: Implement the stock transaction**

Use `@Transactional`, `SELECT ... FOR UPDATE`, validate the locked row, update it, and save:

```java
StockRecord record = new StockRecord();
record.setDessertId(dessert.getId());
record.setChangeQuantity(change);
record.setBeforeStock(before);
record.setAfterStock(after);
record.setType(change > 0 ? "MANUAL_IN" : "MANUAL_OUT");
record.setRemark(request.remark().trim());
stockRecordMapper.insert(record);
```

The list query joins `stock_record`, `dessert`, and optional `orders`.

- [ ] **Step 4: Verify focused and full tests**

Run: `backend\mvnw.cmd -Dtest=StockRecordServiceTests test` then `backend\mvnw.cmd test`  
Expected: all pass.

- [ ] **Step 5: Commit**

```powershell
git add backend/src/main/java/com/sweet/dessertsystem/stock backend/src/main/java/com/sweet/dessertsystem/mapper/DessertMapper.java backend/src/test/java/com/sweet/dessertsystem/stock
git commit -m "feat: add transactional inventory records"
```

### Task 4: Transactional Multi-item Orders

**Files:**
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/Order.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderDetail.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderMapper.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderDetailMapper.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/CreateOrderItemRequest.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/CreateOrderRequest.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderItemView.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderView.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderPageResult.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/OrderController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/mapper/DessertMapper.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/stock/StockRecordMapper.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/order/OrderServiceTests.java`

**Interfaces:**
- `CreateOrderRequest(String customerName, String customerPhone, String remark, List<CreateOrderItemRequest> items)`.
- `CreateOrderItemRequest(Long dessertId, Integer quantity)`.
- `OrderService.create`, `detail`, `page`, `complete`, and `cancel`.
- REST endpoints under `/api/orders`.

- [ ] **Step 1: Write failing service tests**

Cover a successful multi-item order, unavailable dessert, insufficient stock, cancellation return, repeated cancellation, and completion.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `backend\mvnw.cmd -Dtest=OrderServiceTests test`  
Expected: compilation failure because order classes are missing.

- [ ] **Step 3: Implement minimal transactional order behavior**

Generate order numbers as `DS` plus timestamp plus a short random suffix. Lock each dessert row in sorted ID order, use database prices, insert order/details, decrement stock, and insert `ORDER_OUT` records. Cancellation locks the same desserts, restores quantities, and inserts `ORDER_RETURN` records. Status updates reject invalid transitions.

- [ ] **Step 4: Verify focused and full tests**

Run: `backend\mvnw.cmd -Dtest=OrderServiceTests test` then `backend\mvnw.cmd test`  
Expected: all pass.

- [ ] **Step 5: Commit**

```powershell
git add backend/src/main/java/com/sweet/dessertsystem/order backend/src/main/java/com/sweet/dessertsystem/mapper/DessertMapper.java backend/src/main/java/com/sweet/dessertsystem/stock/StockRecordMapper.java backend/src/test/java/com/sweet/dessertsystem/order
git commit -m "feat: add transactional order management"
```

### Task 5: Database-backed Dashboard Metrics

**Files:**
- Modify: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardMapper.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardSummary.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/dashboard/DashboardService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/dashboard/RecentOrder.java`
- Modify: `backend/src/test/java/com/sweet/dessertsystem/dashboard/DashboardServiceTests.java`

**Interfaces:**
- `DashboardSummary` adds `todayOrderCount`, `todaySalesAmount`, and `recentOrders`.
- Cancelled orders are excluded from sales.

- [ ] **Step 1: Extend the test and verify RED**

Assert the service returns database values for order count, sales amount, and recent orders. Expected initial failure: missing record fields and mapper methods.

- [ ] **Step 2: Implement mapper queries and service composition**

Use `CURRENT_DATE` boundaries in MySQL and `status <> 'CANCELLED'` for sales.

- [ ] **Step 3: Run focused and full tests**

Run: `backend\mvnw.cmd -Dtest=DashboardServiceTests test` then `backend\mvnw.cmd test`  
Expected: all pass.

- [ ] **Step 4: Commit**

```powershell
git add backend/src/main/java/com/sweet/dessertsystem/dashboard backend/src/test/java/com/sweet/dessertsystem/dashboard
git commit -m "feat: add order metrics to dashboard"
```

### Task 6: Vue Order and Inventory Management

**Files:**
- Create: `frontend/src/api/order.js`
- Create: `frontend/src/api/stock.js`
- Create: `frontend/src/views/OrderManagement.vue`
- Create: `frontend/src/views/StockRecords.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/layout/AdminLayout.vue`
- Modify: `frontend/src/views/Home.vue`

**Interfaces:**
- `orderApi.page/detail/create/complete/cancel`.
- `stockApi.page/adjust`.
- Routes `/orders` and `/stock-records`.

- [ ] **Step 1: Add API wrappers and route contracts**

Follow the existing `request.js` response handling. Use route names `orders` and `stock-records`.

- [ ] **Step 2: Build the order page**

Implement filters, pagination, creation dialog, dynamic item rows, database-loaded desserts, detail drawer, and state actions. Display the backend result as authoritative after creation.

- [ ] **Step 3: Build the stock page**

Implement filters, colored type tags, pagination, and an adjustment dialog requiring a reason.

- [ ] **Step 4: Extend navigation and dashboard**

Add menu entries and show today orders, sales, low stock, and recent orders without frontend mock data.

- [ ] **Step 5: Run production build**

Run: `frontend\npm run build`  
Expected: success with no compilation errors.

- [ ] **Step 6: Commit**

```powershell
git add frontend/src
git commit -m "feat: add order and inventory management pages"
```

### Task 7: Real MySQL Verification and Documentation

**Files:**
- Modify: `README.md`
- Modify: `docs/database-design.md`
- Create: `.env.example`

**Interfaces:**
- Documents `DB_USERNAME`, `DB_PASSWORD`, migration behavior, demo credentials, and verification commands.

- [ ] **Step 1: Update documentation**

Document automatic Flyway migrations, the six business tables, order/stock workflow, environment variables, and screenshots-ready run instructions. Do not include a real password.

- [ ] **Step 2: Run full automated verification**

Run:

```powershell
backend\mvnw.cmd test
Set-Location frontend
npm run build
```

Expected: 0 test failures and a successful production build.

- [ ] **Step 3: Run real MySQL transaction verification**

Start the backend with the user's environment credentials, then exercise:

1. list demo categories and desserts;
2. create an order;
3. verify order details and `ORDER_OUT`;
4. cancel the order;
5. verify stock restoration and `ORDER_RETURN`;
6. restart the backend and confirm persistence;
7. delete only the verification rows.

- [ ] **Step 4: Check secrets and repository state**

Run:

```powershell
rg -n -i "password\\s*=|jdbc:mysql://.*:.*@" --glob "!**/target/**" --glob "!**/node_modules/**"
git diff --check
git status -sb
```

Expected: no real password, no whitespace errors, and only intended changes.

- [ ] **Step 5: Commit**

```powershell
git add README.md docs/database-design.md .env.example
git commit -m "docs: document mysql order workflow"
```
