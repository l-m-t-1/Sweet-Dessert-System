# Customer Store and Secure Authentication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add public registration, BCrypt passwords, JWT role enforcement, a customer storefront, cart, and user-owned orders without breaking the existing admin order and inventory workflow.

**Architecture:** One Spring Boot application exposes public, customer, and administrator API groups protected by Spring Security resource-server JWT authentication. One Vue application contains public pages, customer routes under `/shop`, and existing administration routes under `/admin`; shared authentication and cart modules keep state outside page components.

**Tech Stack:** Java 17, Spring Boot 4.0.7, Spring Security, OAuth2 Resource Server JWT, MyBatis Plus, MySQL 8, Flyway, Vue 3, Vue Router, Axios, Element Plus, Vite 8, JUnit 5, Mockito, Node test runner

## Global Constraints

- Public registration always creates role `USER`; no API can create another `ADMIN`.
- Registration accepts only username and password.
- Passwords are stored only as BCrypt hashes.
- JWT signing secret comes from `JWT_SECRET` or ignored local configuration.
- Every protected request re-checks the account status in MySQL.
- Existing orders remain valid when `user_id` is null.
- Customer order ownership is enforced in the service layer.
- Existing order stock deduction, cancellation return, and stock records remain transactional.
- Uploaded image URLs remain public under `/uploads/**`.

---

### Task 1: Add the account and order ownership migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V4__customer_accounts_and_order_ownership.sql`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/entity/User.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/order/Order.java`
- Modify: `backend/src/test/java/com/sweet/dessertsystem/migration/MigrationResourcesTests.java`

**Interfaces:**
- Produces: `user.status`, timestamps, unique username, `orders.user_id`, and an index for user order history.

- [ ] **Step 1: Extend the migration test and verify RED**

Add:

```java
String accounts = resource("db/migration/V4__customer_accounts_and_order_ownership.sql");
assertThat(accounts)
        .contains("ADD COLUMN status")
        .contains("ADD COLUMN user_id")
        .contains("idx_orders_user_id_create_time")
        .contains("UPDATE `user`")
        .contains("$2");
```

Run:

```powershell
cd backend
.\mvnw.cmd -Dtest=MigrationResourcesTests test
```

Expected: FAIL because V4 does not exist.

- [ ] **Step 2: Create V4**

Use idempotent MySQL 8 statements:

```sql
ALTER TABLE `user`
    MODIFY COLUMN username VARCHAR(30) NOT NULL,
    MODIFY COLUMN password VARCHAR(100) NOT NULL,
    ADD COLUMN status TINYINT NOT NULL DEFAULT 1,
    ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    ADD UNIQUE KEY uk_user_username (username);

UPDATE `user`
SET password = '$2b$10$h3FHNf8rsh7X8YjB29KbdOmQesaV8QbMyuqwmZX.5qroyEH/22Bv2'
WHERE username = 'admin' AND password = 'admin123';

ALTER TABLE orders
    ADD COLUMN user_id BIGINT NULL AFTER id,
    ADD INDEX idx_orders_user_id_create_time (user_id, create_time),
    ADD CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES `user`(id);
```

The hash above has been verified against `admin123`; keep the migration conditional so already-hashed passwords are not changed.

- [ ] **Step 3: Map the new fields**

Add to `User`:

```java
private Integer status;
private LocalDateTime createTime;
private LocalDateTime updateTime;
```

Add to `Order`:

```java
private Long userId;
```

- [ ] **Step 4: Run migration and full backend tests**

```powershell
.\mvnw.cmd -Dtest=MigrationResourcesTests test
.\mvnw.cmd test
```

Expected: migration test and the existing 24 tests pass.

- [ ] **Step 5: Commit**

```powershell
git add backend/src/main/resources/db/migration/V4__customer_accounts_and_order_ownership.sql backend/src/main/java/com/sweet/dessertsystem/entity/User.java backend/src/main/java/com/sweet/dessertsystem/order/Order.java backend/src/test/java/com/sweet/dessertsystem/migration/MigrationResourcesTests.java
git commit -m "feat: migrate customer accounts and order ownership"
```

### Task 2: Implement registration, BCrypt, and JWT authentication

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.properties`
- Create: `backend/src/main/java/com/sweet/dessertsystem/auth/RegisterRequest.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/auth/LoginRequest.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/auth/AuthResponse.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/auth/AuthService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/auth/AuthController.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/auth/TokenService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/security/SecurityConfig.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/security/ActiveAccountJwtConverter.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/service/UserService.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/exception/GlobalExceptionHandler.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/auth/AuthServiceTests.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/security/ActiveAccountJwtConverterTests.java`

**Interfaces:**
- Produces: `POST /api/auth/register`, `POST /api/auth/login`, BCrypt hashes, and two-hour bearer JWTs with `ROLE_ADMIN` or `ROLE_USER`.

- [ ] **Step 1: Add failing auth service tests**

Cover these exact assertions:

```java
assertThat(service.register(new RegisterRequest("alice", "secret12")).role())
        .isEqualTo("USER");
verify(passwordEncoder).encode("secret12");
verify(userMapper).insert(argThat(user ->
        user.getRole().equals("USER") && user.getStatus() == 1));

assertThatThrownBy(() -> service.register(new RegisterRequest("alice", "secret12")))
        .isInstanceOf(BusinessException.class)
        .hasMessage("用户名已存在");

assertThat(service.login(new LoginRequest("alice", "secret12")).token())
        .isEqualTo("signed-token");
verify(passwordEncoder).matches("secret12", storedHash);
```

Run:

```powershell
.\mvnw.cmd -Dtest=AuthServiceTests test
```

Expected: compilation FAIL because the auth classes do not exist.

- [ ] **Step 2: Add Spring Security dependencies and properties**

Add:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Add properties:

```properties
app.jwt-secret=${JWT_SECRET:change-this-local-demo-secret-to-at-least-32-bytes}
app.jwt-ttl=PT2H
```

- [ ] **Step 3: Implement auth records and service**

Use:

```java
public record RegisterRequest(String username, String password) {}
public record LoginRequest(String username, String password) {}
public record AuthResponse(String token, Long id, String username, String role) {}
```

`AuthService.register` trims username, enforces username length 3–30 and password length 6–72, checks uniqueness, BCrypt-encodes the password, and inserts `role=USER,status=1`. `AuthService.login` finds by username, rejects missing/disabled accounts and BCrypt mismatches, then delegates to `TokenService.issue(user)`.

- [ ] **Step 4: Implement JWT encoding, decoding, and status re-check**

Configure `NimbusJwtEncoder` and `NimbusJwtDecoder` with an HMAC-SHA256 `SecretKey`. `TokenService.issue` uses the user ID as `sub` and adds `username` and `role` claims.

`ActiveAccountJwtConverter.convert(Jwt jwt)`:

```java
Long userId = Long.valueOf(jwt.getSubject());
User user = userMapper.selectById(userId);
if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
    throw new BadCredentialsException("账号已停用");
}
return new JwtAuthenticationToken(
        jwt,
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
        user.getUsername()
);
```

- [ ] **Step 5: Configure authorization**

Use stateless security, disable CSRF for bearer-token APIs, return JSON 401/403, and apply:

```java
requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
requestMatchers(HttpMethod.GET, "/api/store/**", "/api/category", "/uploads/**").permitAll()
requestMatchers("/api/customer/**").hasRole("USER")
requestMatchers("/api/admin/**").hasRole("ADMIN")
anyRequest().hasRole("ADMIN")
```

- [ ] **Step 6: Verify focused and full tests**

```powershell
.\mvnw.cmd -Dtest=AuthServiceTests,ActiveAccountJwtConverterTests test
.\mvnw.cmd test
```

Expected: all authentication tests and the full suite pass.

- [ ] **Step 7: Commit**

```powershell
git add backend/pom.xml backend/src/main/resources/application.properties backend/src/main/java/com/sweet/dessertsystem/auth backend/src/main/java/com/sweet/dessertsystem/security backend/src/main/java/com/sweet/dessertsystem/service/UserService.java backend/src/main/java/com/sweet/dessertsystem/exception/GlobalExceptionHandler.java backend/src/test/java/com/sweet/dessertsystem/auth backend/src/test/java/com/sweet/dessertsystem/security
git commit -m "feat: add bcrypt jwt authentication"
```

### Task 3: Add customer catalog and owned-order APIs

**Files:**
- Create: `backend/src/main/java/com/sweet/dessertsystem/store/StoreController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/mapper/DessertMapper.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/service/DessertService.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/service/impl/DessertServiceImpl.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/order/CustomerOrderController.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/order/Order.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/order/OrderMapper.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/order/OrderService.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/order/CustomerOrderServiceTests.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/service/StoreDessertServiceTests.java`

**Interfaces:**
- Produces: public `GET /api/store/desserts` and protected customer order create/list/detail/cancel endpoints.

- [ ] **Step 1: Add failing ownership and catalog tests**

Assert:

```java
OrderView created = service.createForUser(request, 9L, "alice");
verify(orderMapper).insert(argThat(order ->
        order.getUserId().equals(9L) && order.getCustomerName().equals("alice")));

assertThatThrownBy(() -> service.detailForUser(8L, 9L))
        .isInstanceOf(BusinessException.class)
        .hasMessage("无权访问该订单");

assertThat(storeService.pageAvailable(1, 12, null, null).records())
        .allMatch(item -> item.status() == 1);
```

Run the focused tests and confirm RED.

- [ ] **Step 2: Add available-dessert queries**

Add mapper methods whose SQL always includes `d.status = 1`, and expose:

```java
DessertPageResult pageAvailable(long page, long size, String name, Long categoryId);
```

`StoreController` delegates GET `/api/store/desserts` to this method.

- [ ] **Step 3: Add user-aware order queries**

Add mapper methods:

```java
long countPageByUserId(Long userId, String status);
List<OrderView> findPageByUserId(Long userId, String status, long offset, long size);
OrderView findViewByIdAndUserId(Long id, Long userId);
Order findByIdAndUserIdForUpdate(Long id, Long userId);
```

All queries include `WHERE user_id = #{userId}`.

- [ ] **Step 4: Refactor order creation without duplicating inventory logic**

Keep `create(CreateOrderRequest request)` for admin orders and add:

```java
public OrderView createForUser(CreateOrderRequest request, Long userId, String username) {
    return createInternal(request, userId, username);
}
```

`createInternal` writes `userId`; when username is supplied it ignores `request.customerName()` and uses the authenticated username. Add `pageForUser`, `detailForUser`, and `cancelForUser`, with ownership checks inside `OrderService`.

- [ ] **Step 5: Add the customer controller**

Read identity from `Jwt`:

```java
Long userId = Long.valueOf(jwt.getSubject());
String username = jwt.getClaimAsString("username");
```

Expose `/api/customer/orders` GET/POST and `/{id}` GET plus `/{id}/cancel` PUT.

- [ ] **Step 6: Run tests and commit**

```powershell
.\mvnw.cmd -Dtest=CustomerOrderServiceTests,StoreDessertServiceTests,OrderServiceTests test
.\mvnw.cmd test
git add backend/src/main/java backend/src/test/java
git commit -m "feat: add customer catalog and owned orders"
```

### Task 4: Add administrator user management

**Files:**
- Create: `backend/src/main/java/com/sweet/dessertsystem/admin/AdminUserController.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/admin/AdminUserService.java`
- Create: `backend/src/main/java/com/sweet/dessertsystem/admin/UserStatusRequest.java`
- Modify: `backend/src/main/java/com/sweet/dessertsystem/dto/UserView.java`
- Test: `backend/src/test/java/com/sweet/dessertsystem/admin/AdminUserServiceTests.java`

**Interfaces:**
- Produces: `GET /api/admin/users` and `PUT /api/admin/users/{id}/status`.

- [ ] **Step 1: Add failing service tests**

Verify that setting another `USER` to status 0 updates one row, while changing the authenticated administrator's own ID throws `BusinessException("不能停用当前管理员账号")`.

- [ ] **Step 2: Implement list and status change**

`UserView` includes `status` and `createTime` but never exposes password. `AdminUserService.changeStatus(targetId, currentAdminId, status)` accepts only 0 or 1, rejects self-disable, rejects missing users, and updates the target.

- [ ] **Step 3: Add controller and verify**

The controller obtains current admin ID from JWT subject. Run focused and full backend tests.

- [ ] **Step 4: Commit**

```powershell
git add backend/src/main/java/com/sweet/dessertsystem/admin backend/src/main/java/com/sweet/dessertsystem/dto/UserView.java backend/src/test/java/com/sweet/dessertsystem/admin
git commit -m "feat: add admin user management"
```

### Task 5: Build the shared frontend authentication and role router

**Files:**
- Create: `frontend/src/auth/session.js`
- Create: `frontend/src/auth/access.js`
- Modify: `frontend/src/api/request.js`
- Modify: `frontend/src/api/auth.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/views/Login.vue`
- Create: `frontend/src/views/Register.vue`
- Test: `frontend/test/authSession.test.js`
- Test: `frontend/test/routeAccess.test.js`

**Interfaces:**
- Produces: session token storage, Authorization header, role redirects, registration, and login.

- [ ] **Step 1: Add failing pure-module tests**

Test these functions with an injected storage object:

```javascript
saveSession(storage, { token: 'jwt', user: { role: 'USER' } })
assert.equal(readSession(storage).token, 'jwt')
assert.equal(homeForRole('ADMIN'), '/admin/home')
assert.equal(homeForRole('USER'), '/shop')
assert.equal(canAccess({ role: 'USER' }, ['ADMIN']), false)
```

Run `npm test` and confirm RED.

- [ ] **Step 2: Implement session and access modules**

Export `readSession`, `saveSession`, `clearSession`, `homeForRole`, and `canAccess`. Store one JSON document under `sweet-dessert-session`.

- [ ] **Step 3: Add Axios bearer handling**

Request interceptor reads the token and sets `Authorization`. Response errors preserve backend messages; 401 clears the session and redirects to `/login`, while 403 reports `无权执行此操作`.

- [ ] **Step 4: Restructure routes**

Public routes: `/login`, `/register`, `/shop`. Customer routes: `/cart`, `/my-orders`. Admin routes move below `/admin`. Route metadata uses `roles: ['USER']` or `roles: ['ADMIN']`; the guard redirects unauthenticated users to login and mismatched roles to their own home.

- [ ] **Step 5: Update login and registration pages**

Login saves `{token,user}` from `/auth/login` and routes by role. Registration posts only username/password, validates matching confirmation locally, then redirects to login after success.

- [ ] **Step 6: Run tests, build, and commit**

```powershell
cd frontend
npm test
npm run build
git add src/auth src/api src/router src/views/Login.vue src/views/Register.vue test
git commit -m "feat: add frontend jwt authentication"
```

### Task 6: Build the customer storefront, cart, orders, and admin users page

**Files:**
- Create: `frontend/src/api/store.js`
- Create: `frontend/src/api/customerOrder.js`
- Create: `frontend/src/api/adminUser.js`
- Create: `frontend/src/cart/cartStore.js`
- Create: `frontend/src/layout/StoreLayout.vue`
- Create: `frontend/src/views/Store.vue`
- Create: `frontend/src/views/Cart.vue`
- Create: `frontend/src/views/MyOrders.vue`
- Create: `frontend/src/views/UserManagement.vue`
- Modify: `frontend/src/layout/AdminLayout.vue`
- Modify: `frontend/src/style.css`
- Test: `frontend/test/cartStore.test.js`

**Interfaces:**
- Produces: public catalog browsing, persistent cart, authenticated checkout, owned order history/cancellation, and admin account activation controls.

- [ ] **Step 1: Add failing cart tests**

Verify add, quantity merge, remove, total calculation, unavailable stock limit, persistence, and clear:

```javascript
cart.add({ id: 3, name: '提拉米苏', price: 28, stock: 5 }, 2)
cart.add({ id: 3, name: '提拉米苏', price: 28, stock: 5 }, 1)
assert.equal(cart.items()[0].quantity, 3)
assert.equal(cart.total(), 84)
cart.clear()
assert.deepEqual(cart.items(), [])
```

- [ ] **Step 2: Implement API wrappers and cart module**

API paths are `/store/desserts`, `/customer/orders`, and `/admin/users`. The cart stores only product snapshot and quantity; the server remains authoritative for price, status, and stock.

- [ ] **Step 3: Implement customer layout and pages**

`Store.vue` provides keyword/category filters and product cards. `Cart.vue` allows quantity changes, optional phone, and submits `{customerPhone,remark,items}`. `MyOrders.vue` lists the current user's orders, loads details, and cancels only `CREATED` orders.

- [ ] **Step 4: Implement admin user page**

List username, role, status, and creation time. Only `USER` rows have enable/disable controls; the current administrator row is read-only.

- [ ] **Step 5: Run tests, build, and commit**

```powershell
npm test
npm run build
git add src test
git commit -m "feat: add customer store and user management ui"
```

### Task 7: Real MySQL migration, integration verification, and documentation

**Files:**
- Modify: `.env.example`
- Modify: `README.md`
- Modify: `backend/start-local.ps1` only if the JWT secret needs a local-property startup note.

**Interfaces:**
- Produces: a reproducible local setup and verified register-to-order workflow.

- [ ] **Step 1: Document secrets and roles**

Add `JWT_SECRET` to `.env.example`, explain that the default admin password is local-demo-only, document `/shop` and `/admin/home`, and list the user order workflow.

- [ ] **Step 2: Merge into the actual local branch and restart**

Fast-forward `codex/phase-one-management`, stop only the verified 8080/5173 Java and Node listeners, then restart with `start-local.ps1` and Vite.

- [ ] **Step 3: Verify the real database workflow**

Use a uniquely named temporary user:

1. Register and assert stored role `USER` and a BCrypt password beginning `$2`.
2. Login and obtain JWT.
3. Call the public store endpoint.
4. Create an order through `/api/customer/orders`.
5. Verify the order `user_id`, reduced dessert stock, and `ORDER_OUT` row.
6. Verify another user cannot read the order.
7. Cancel it and verify stock restoration plus `ORDER_RETURN`.
8. Login as admin, list users, disable the temporary account, and confirm its old JWT returns 401.
9. Delete only the exact temporary order, details, stock rows, and temporary user after restoring inventory.

- [ ] **Step 4: Run final verification**

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm test
npm run build
```

Expected: zero backend failures, all frontend tests pass, Vite production build succeeds, all live API checks return expected 200/401/403 results, and both Git workspaces are clean.

- [ ] **Step 5: Commit documentation**

```powershell
git add .env.example README.md backend/start-local.ps1
git commit -m "docs: document customer store authentication"
```
