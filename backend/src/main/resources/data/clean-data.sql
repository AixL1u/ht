-- ============================================
-- 非遗传承信息管理系统 - 数据清除脚本
-- ============================================
-- 该脚本删除数据库中的所有数据
-- 执行前请备份数据库！

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 删除所有数据（TRUNCATE 重置自增ID）
-- ============================================

-- 帖子相关
TRUNCATE TABLE post_likes;
TRUNCATE TABLE comments;
TRUNCATE TABLE posts;

-- 订单相关
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;

-- 预约
TRUNCATE TABLE reservations;

-- 操作日志
TRUNCATE TABLE operation_logs;

-- 媒体资产
TRUNCATE TABLE media_assets;

-- 非遗项目相关
TRUNCATE TABLE project_inheritors;
TRUNCATE TABLE inheritors;
TRUNCATE TABLE heritage_projects;
TRUNCATE TABLE heritage_categories;

-- 商品相关
TRUNCATE TABLE products;
TRUNCATE TABLE product_categories;

-- 其他业务数据
TRUNCATE TABLE news;
TRUNCATE TABLE videos;
TRUNCATE TABLE activities;
TRUNCATE TABLE banners;
TRUNCATE TABLE page_banners;
TRUNCATE TABLE site_settings;
TRUNCATE TABLE addresses;
TRUNCATE TABLE forum_topics;

-- 用户和权限（保留，不清除）
-- TRUNCATE TABLE user_roles;
-- TRUNCATE TABLE role_permissions;
-- TRUNCATE TABLE users;
-- TRUNCATE TABLE roles;
-- TRUNCATE TABLE permissions;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 验证清除结果
-- ============================================

SELECT '商品' AS '数据类型', COUNT(*) AS '数量' FROM products
UNION ALL SELECT '非遗项目', COUNT(*) FROM heritage_projects
UNION ALL SELECT '活动', COUNT(*) FROM activities
UNION ALL SELECT '用户(保留)', COUNT(*) FROM users;

SELECT '✅ 业务数据已清除，用户数据已保留！' AS '状态';
