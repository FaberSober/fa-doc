-- ------------------------- info -------------------------
-- @@ver: 1_000_002
-- @@info: 文档主表租户隔离，子表通过文档归属间接隔离
-- ------------------------- info -------------------------

-- 仅文档主表 dm_doc 增加租户字段；dm_doc_chapter 等子表查询均收敛到 doc 访问校验（DocAccessBiz），无需加列。

ALTER TABLE "dm_doc"
    ADD COLUMN IF NOT EXISTS "tenant_id" varchar(32) DEFAULT NULL;
COMMENT ON COLUMN "dm_doc"."tenant_id" IS '租户ID';

-- 历史文档归属到创建者默认租户；若创建者没有租户关联，则回退到平台排序最前的启用租户。
UPDATE "dm_doc" AS d
SET "tenant_id" = COALESCE(
    (SELECT tu."tenant_id"
     FROM "tn_tenant_user" tu
     INNER JOIN "tn_tenant" t ON t."id" = tu."tenant_id"
     WHERE tu."user_id" = d."crt_user" AND tu."status" IS TRUE AND tu."deleted" IS FALSE
       AND t."status" IS TRUE AND t."deleted" IS FALSE
       AND (t."expire_time" IS NULL OR t."expire_time" > CURRENT_TIMESTAMP)
     ORDER BY tu."sort", tu."id" LIMIT 1),
    (SELECT t."id" FROM "tn_tenant" t
     WHERE t."status" IS TRUE AND t."deleted" IS FALSE
       AND (t."expire_time" IS NULL OR t."expire_time" > CURRENT_TIMESTAMP)
     ORDER BY t."sort", t."id" LIMIT 1)
)
WHERE d."tenant_id" IS NULL;

CREATE INDEX IF NOT EXISTS "idx_dm_doc_tenant_id" ON "dm_doc" ("tenant_id");
