-- ------------------------- info -------------------------
-- @@ver: 1_000_000
-- @@info: 初始化fa-doc模块
-- ------------------------- info -------------------------

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `dm_doc` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '文档名称',
  `is_public` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否公开',
  `share_code` varchar(32) DEFAULT NULL COMMENT '分享码',
  `view_num` int(11) UNSIGNED DEFAULT 0 COMMENT '访问次数',
  `view_chapter_num` int(11) UNSIGNED DEFAULT 0 COMMENT '章节总访问次数',
  `crt_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `crt_user` varchar(32) NOT NULL COMMENT '创建用户ID',
  `crt_name` varchar(255) NOT NULL COMMENT '创建用户',
  `crt_host` varchar(255) DEFAULT NULL COMMENT '创建IP',
  `upd_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `upd_user` varchar(32) DEFAULT NULL COMMENT '更新用户ID',
  `upd_name` varchar(255) DEFAULT NULL COMMENT '更新用户',
  `upd_host` varchar(255) DEFAULT NULL COMMENT '更新IP',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOC-文档';

CREATE TABLE IF NOT EXISTS `dm_doc_chapter` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `parent_id` int(11) UNSIGNED NOT NULL COMMENT '父ID',
  `doc_id` int(11) UNSIGNED NOT NULL COMMENT '文档ID',
  `name` varchar(255) NOT NULL COMMENT '章节名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `view_num` int(11) UNSIGNED DEFAULT 0 COMMENT '访问次数',
  `crt_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `crt_user` varchar(32) NOT NULL COMMENT '创建用户ID',
  `crt_name` varchar(255) NOT NULL COMMENT '创建用户',
  `crt_host` varchar(255) DEFAULT NULL COMMENT '创建IP',
  `upd_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `upd_user` varchar(32) DEFAULT NULL COMMENT '更新用户ID',
  `upd_name` varchar(255) DEFAULT NULL COMMENT '更新用户',
  `upd_host` varchar(255) DEFAULT NULL COMMENT '更新IP',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOC-文档章节';

CREATE TABLE IF NOT EXISTS `dm_doc_chapter_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `content` longtext NOT NULL COMMENT '章节富文本内容',
  `crt_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `crt_user` varchar(32) NOT NULL COMMENT '创建用户ID',
  `crt_name` varchar(255) NOT NULL COMMENT '创建用户',
  `crt_host` varchar(255) DEFAULT NULL COMMENT '创建IP',
  `upd_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `upd_user` varchar(32) DEFAULT NULL COMMENT '更新用户ID',
  `upd_name` varchar(255) DEFAULT NULL COMMENT '更新用户',
  `upd_host` varchar(255) DEFAULT NULL COMMENT '更新IP',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOC-文档章节详情';

CREATE TABLE IF NOT EXISTS `dm_doc_chapter_his` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `doc_id` int(11) UNSIGNED NOT NULL COMMENT '文档ID',
  `chapter_id` int(11) UNSIGNED NOT NULL COMMENT '章节ID',
  `name` varchar(255) DEFAULT NULL COMMENT '版本名称',
  `crt_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `crt_user` varchar(32) NOT NULL COMMENT '创建用户ID',
  `crt_name` varchar(255) NOT NULL COMMENT '创建用户',
  `crt_host` varchar(255) DEFAULT NULL COMMENT '创建IP',
  `upd_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `upd_user` varchar(32) DEFAULT NULL COMMENT '更新用户ID',
  `upd_name` varchar(255) DEFAULT NULL COMMENT '更新用户',
  `upd_host` varchar(255) DEFAULT NULL COMMENT '更新IP',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOC-章节历史';

CREATE TABLE IF NOT EXISTS `dm_doc_chapter_his_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `content` longtext NOT NULL COMMENT '章节历史富文本内容',
  `crt_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `crt_user` varchar(32) NOT NULL COMMENT '创建用户ID',
  `crt_name` varchar(255) NOT NULL COMMENT '创建用户',
  `crt_host` varchar(255) DEFAULT NULL COMMENT '创建IP',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOC-章节历史详情';

CREATE TABLE IF NOT EXISTS `dm_doc_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `doc_id` int(11) UNSIGNED NOT NULL COMMENT '文档ID',
  `user_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '用户ID',
  `crt_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `crt_user` varchar(32) NOT NULL COMMENT '创建用户ID',
  `crt_name` varchar(255) NOT NULL COMMENT '创建用户',
  `crt_host` varchar(255) DEFAULT NULL COMMENT '创建IP',
  `upd_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `upd_user` varchar(32) DEFAULT NULL COMMENT '更新用户ID',
  `upd_name` varchar(255) DEFAULT NULL COMMENT '更新用户',
  `upd_host` varchar(255) DEFAULT NULL COMMENT '更新IP',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOC-文档用户';

INSERT INTO `base_rbac_menu` (
  `id`, `parent_id`, `name`, `sort`, `level`, `icon`, `status`, `link_type`, `link_url`,
  `crt_time`, `crt_user`, `crt_name`, `crt_host`, `upd_time`, `upd_user`, `upd_name`, `upd_host`, `deleted`
) VALUES
  (28000000, 0, '文档管理', 1, 0, 'book', 1, 1, '/admin/dm', '2023-06-30 16:59:00', '1', '超级管理员', '127.0.0.1', NULL, NULL, NULL, NULL, 0),
  (28010000, 28000000, '文档管理', 0, 1, 'book', 1, 1, '/admin/dm/doc/doc', '2023-06-30 17:00:28', '1', '超级管理员', '127.0.0.1', NULL, NULL, NULL, NULL, 0),
  (28010100, 28010000, '查看', 0, 9, NULL, 1, 1, '/admin/dm/doc/view/:id', '2023-07-26 16:54:18', '1', '超级管理员', '127.0.0.1', NULL, NULL, NULL, NULL, 0),
  (28010200, 28010000, '编辑', 1, 9, NULL, 1, 1, '/admin/dm/doc/edit/:id', '2023-07-26 17:38:17', '1', '超级管理员', '127.0.0.1', NULL, NULL, NULL, NULL, 0);
