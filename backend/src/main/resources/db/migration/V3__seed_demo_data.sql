INSERT INTO `user` (username, password, role)
SELECT 'admin', 'admin123', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE username = 'admin'
);

INSERT INTO category (name)
SELECT '未分类'
WHERE NOT EXISTS (
    SELECT 1 FROM category WHERE name = '未分类'
);

INSERT INTO category (name)
SELECT '蛋糕'
WHERE NOT EXISTS (
    SELECT 1 FROM category WHERE name = '蛋糕'
);

INSERT INTO category (name)
SELECT '饮品'
WHERE NOT EXISTS (
    SELECT 1 FROM category WHERE name = '饮品'
);

INSERT INTO category (name)
SELECT '面包'
WHERE NOT EXISTS (
    SELECT 1 FROM category WHERE name = '面包'
);

INSERT INTO dessert (name, category_id, price, stock, description, status)
SELECT '经典提拉米苏', c.id, 28.00, 18, '咖啡与马斯卡彭交织的经典甜品', 1
FROM category c
WHERE c.name = '蛋糕'
  AND NOT EXISTS (
      SELECT 1 FROM dessert WHERE name = '经典提拉米苏'
  );

INSERT INTO dessert (name, category_id, price, stock, description, status)
SELECT '草莓奶油蛋糕', c.id, 36.00, 12, '新鲜草莓搭配轻盈奶油', 1
FROM category c
WHERE c.name = '蛋糕'
  AND NOT EXISTS (
      SELECT 1 FROM dessert WHERE name = '草莓奶油蛋糕'
  );

INSERT INTO dessert (name, category_id, price, stock, description, status)
SELECT '可可拿铁', c.id, 22.00, 30, '浓郁可可与牛奶的温暖组合', 1
FROM category c
WHERE c.name = '饮品'
  AND NOT EXISTS (
      SELECT 1 FROM dessert WHERE name = '可可拿铁'
  );
