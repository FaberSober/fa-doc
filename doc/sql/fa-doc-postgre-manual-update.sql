-- fa-doc PostgreSQL 手工更新脚本
--
-- 说明：
-- 1. fa-doc 的 deleted 字段应与 BaseDelEntity.Boolean 对齐为 boolean。
-- 2. 当前报错的直接原因是 DocUserMapper.xml 使用了 deleted = 0，代码修正后才会使用 deleted = false。
-- 3. 本脚本只在已有数据库中的 deleted 字段仍为整数类型时进行转换；已经是 boolean 的字段只补充默认值，不会重复改类型。

DO $$
DECLARE
    table_name text;
    current_type text;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'base_user',
        'dm_doc',
        'dm_doc_chapter',
        'dm_doc_chapter_his',
        'dm_doc_user'
    ] LOOP
        SELECT c.data_type
          INTO current_type
          FROM information_schema.columns c
         WHERE c.table_schema = current_schema()
           AND c.table_name = table_name
           AND c.column_name = 'deleted';

        IF current_type IN ('smallint', 'integer', 'bigint') THEN
            EXECUTE format(
                'ALTER TABLE %I ALTER COLUMN "deleted" TYPE boolean USING COALESCE("deleted", 0) <> 0',
                table_name
            );
        END IF;

        IF current_type IS NOT NULL THEN
            EXECUTE format(
                'ALTER TABLE %I ALTER COLUMN "deleted" SET DEFAULT FALSE',
                table_name
            );
        END IF;
    END LOOP;
END
$$;
