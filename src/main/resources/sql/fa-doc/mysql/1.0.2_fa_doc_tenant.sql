-- ------------------------- info -------------------------
-- @@ver: 1_000_002
-- @@info: 文档主表租户隔离，子表通过文档归属间接隔离
-- ------------------------- info -------------------------

-- 仅文档主表 dm_doc 增加租户字段；dm_doc_chapter 等子表查询均收敛到 doc 访问校验（DocAccessBiz），无需加列。
-- 回填 UPDATE 跨表比较显式 COLLATE，避免与 tn_tenant_user 排序规则不一致报 Illegal mix of collations。
-- ADD COLUMN 用预处理语句做幂等：脚本失败重跑时列已存在，直接跳过加列。

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm_doc' AND COLUMN_NAME = 'tenant_id') = 0,
    'ALTER TABLE `dm_doc` ADD COLUMN `tenant_id` varchar(32) DEFAULT NULL COMMENT ''租户ID'' AFTER `view_chapter_num`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 历史文档归属到创建者默认租户；若创建者没有租户关联，则回退到平台排序最前的启用租户。
UPDATE `dm_doc` AS d
SET `tenant_id` = COALESCE(
    (SELECT tu.`tenant_id`
     FROM `tn_tenant_user` tu
     INNER JOIN `tn_tenant` t ON t.`id` = tu.`tenant_id`
     WHERE tu.`user_id` = d.`crt_user` COLLATE utf8mb4_general_ci AND tu.`status` = 1 AND tu.`deleted` = 0
       AND t.`status` = 1 AND t.`deleted` = 0
       AND (t.`expire_time` IS NULL OR t.`expire_time` > CURRENT_TIMESTAMP)
     ORDER BY tu.`sort`, tu.`id` LIMIT 1),
    (SELECT t.`id` FROM `tn_tenant` t
     WHERE t.`status` = 1 AND t.`deleted` = 0
       AND (t.`expire_time` IS NULL OR t.`expire_time` > CURRENT_TIMESTAMP)
     ORDER BY t.`sort`, t.`id` LIMIT 1)
)
WHERE d.`tenant_id` IS NULL;

CREATE INDEX `idx_dm_doc_tenant_id` ON `dm_doc` (`tenant_id`);
