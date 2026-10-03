-- ------------------------- info -------------------------
-- @@ver: 1_000_003
-- @@info: 分享码与文档用户唯一约束，章节树/历史查询组合索引
-- ------------------------- info -------------------------

-- 说明：
-- 1) 升级脚本按语句自动提交，每条语句都要能重复执行；MySQL 建索引无 IF NOT EXISTS，统一用 information_schema 判断后动态执行。
-- 2) 建唯一约束前先收敛历史重复数据：
--    dm_doc.share_code 重复时保留最小 ID 文档，其余置空（重复分享码无法解析为唯一文档，保留最早一条可用）；
--    dm_doc_user 同文档同用户重复关联时保留一条（优先保留有效关联），其余物理删除。
-- 3) 不修改 pageVo/getDocUserList 现有 SQL，索引直接匹配其过滤条件。

-- ------------------------- 1. dm_doc.share_code 唯一约束 -------------------------

-- 空串分享码统一为 NULL，唯一索引不约束 NULL
UPDATE `dm_doc` SET `share_code` = NULL WHERE `share_code` = '';

-- 重复分享码收敛：保留最小 ID 的文档
UPDATE `dm_doc` AS d
INNER JOIN (
    SELECT `share_code`, MIN(`id`) AS `keep_id`
    FROM `dm_doc`
    WHERE `share_code` IS NOT NULL
    GROUP BY `share_code`
    HAVING COUNT(*) > 1
) AS t ON d.`share_code` = t.`share_code`
SET d.`share_code` = NULL
WHERE d.`id` <> t.`keep_id`;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm_doc' AND INDEX_NAME = 'uk_dm_doc_share_code') = 0,
    'CREATE UNIQUE INDEX `uk_dm_doc_share_code` ON `dm_doc` (`share_code`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------- 2. dm_doc_user(doc_id, user_id) 唯一约束与查询索引 -------------------------

-- 重复关联收敛：每组保留一条，优先保留未删除记录
DELETE u FROM `dm_doc_user` u
INNER JOIN (
    SELECT `doc_id`, `user_id`,
           COALESCE(MIN(CASE WHEN `deleted` = 0 THEN `id` END), MIN(`id`)) AS `keep_id`
    FROM `dm_doc_user`
    GROUP BY `doc_id`, `user_id`
    HAVING COUNT(*) > 1
) AS d ON u.`doc_id` = d.`doc_id` AND u.`user_id` = d.`user_id`
WHERE u.`id` <> d.`keep_id`;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm_doc_user' AND INDEX_NAME = 'uk_dm_doc_user_doc_user') = 0,
    'CREATE UNIQUE INDEX `uk_dm_doc_user_doc_user` ON `dm_doc_user` (`doc_id`, `user_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 反查用户参与的文档列表（DocAccessBiz.getAccessibleDocIds）
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm_doc_user' AND INDEX_NAME = 'idx_dm_doc_user_user_id') = 0,
    'CREATE INDEX `idx_dm_doc_user_user_id` ON `dm_doc_user` (`user_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------- 3. 章节树与章节历史组合索引 -------------------------

-- 章节树按文档 + 父节点过滤并按 sort 排序
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm_doc_chapter' AND INDEX_NAME = 'idx_dm_doc_chapter_doc_parent_sort') = 0,
    'CREATE INDEX `idx_dm_doc_chapter_doc_parent_sort` ON `dm_doc_chapter` (`doc_id`, `parent_id`, `sort`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 章节最新历史版本按 chapter_id 过滤并按 id 倒序取一条
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm_doc_chapter_his' AND INDEX_NAME = 'idx_dm_doc_chapter_his_chapter_id') = 0,
    'CREATE INDEX `idx_dm_doc_chapter_his_chapter_id` ON `dm_doc_chapter_his` (`chapter_id`, `id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 章节历史按文档过滤（DocAccessBiz.getAccessibleHistoryIds）
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm_doc_chapter_his' AND INDEX_NAME = 'idx_dm_doc_chapter_his_doc_id') = 0,
    'CREATE INDEX `idx_dm_doc_chapter_his_doc_id` ON `dm_doc_chapter_his` (`doc_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
