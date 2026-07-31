SET @duplicate_username_count = (
    SELECT COUNT(*)
    FROM (
        SELECT username
        FROM `user`
        GROUP BY username
        HAVING COUNT(*) > 1
    ) duplicate_usernames
);
SET @ddl = IF(
    @duplicate_username_count = 0,
    'SELECT 1',
    'SELECT * FROM migration_blocked_resolve_duplicate_usernames_before_V4'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

ALTER TABLE `user`
    MODIFY COLUMN username VARCHAR(50) NOT NULL,
    MODIFY COLUMN password VARCHAR(100) NOT NULL,
    MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

SET @ddl = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'status'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN status TINYINT NOT NULL DEFAULT 1'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'create_time'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'update_time'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = IF(
    EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND INDEX_NAME = 'uk_user_username'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD UNIQUE KEY uk_user_username (username)'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

UPDATE `user`
SET role = 'USER'
WHERE username <> 'admin';

UPDATE `user`
SET role = 'ADMIN',
    status = 1
WHERE username = 'admin';

UPDATE `user`
SET password = '$2b$10$h3FHNf8rsh7X8YjB29KbdOmQesaV8QbMyuqwmZX.5qroyEH/22Bv2'
WHERE username = 'admin'
  AND password = 'admin123';

INSERT INTO `user` (username, password, role, status)
SELECT 'admin',
       '$2b$10$h3FHNf8rsh7X8YjB29KbdOmQesaV8QbMyuqwmZX.5qroyEH/22Bv2',
       'ADMIN',
       1
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE username = 'admin'
);

SET @ddl = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'orders'
          AND COLUMN_NAME = 'user_id'
    ),
    'SELECT 1',
    'ALTER TABLE orders ADD COLUMN user_id BIGINT NULL AFTER id'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = IF(
    EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'orders'
          AND INDEX_NAME = 'idx_orders_user_id_create_time'
    ),
    'SELECT 1',
    'ALTER TABLE orders ADD INDEX idx_orders_user_id_create_time (user_id, create_time)'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = IF(
    EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'orders'
          AND CONSTRAINT_NAME = 'fk_orders_user'
    ),
    'SELECT 1',
    'ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES `user`(id)'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;
