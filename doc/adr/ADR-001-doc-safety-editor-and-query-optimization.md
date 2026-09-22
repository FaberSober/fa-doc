# ADR-001：fa-doc 安全、编辑器与查询链路优化

- 状态：✅已完成
- 日期：2026-09-22
- 范围：`fa-doc` 后端、`fa-doc-pages` 管理端和公开文档页
- 目标：先修复越权、富文本安全和编辑数据一致性，再处理 loading、查询性能和维护性问题。

## 功能清单（按开发顺序）

| 编号 | 模块 | 功能 | 功能详情 | 当前规划 | 进度 |
| --- | --- | --- | --- | --- | --- |
| D01 | 公开访问 | 公开文档资源校验 | 章节树、章节和详情必须校验分享码对应的公开文档 | 执行开发（P0） | ✅已完成 |
| D02 | 内容安全 | 富文本白名单清洗 | 保存当前内容和历史内容前统一清洗 HTML | 执行开发（P0） | ✅已完成 |
| D03 | 资源授权 | 文档级权限校验 | 章节、详情、历史和文档用户操作不能只依赖菜单或前端限制 | 执行开发（P0） | ✅已完成 |
| D04 | 分享安全 | 分享码强度与唯一性 | 使用服务端安全随机值，并增加数据库唯一约束 | 执行开发（P1） | ❌未完成 |
| D05 | API 契约 | 空数据与异常处理 | 修复不存在文档空指针、空分页和详情请求错误反馈 | 执行开发（P1） | ❌未完成 |
| D06 | 前端状态 | loading 机制迁移 | 文档页面改用 `useApiLoading`，并修正实际请求 URL | 执行开发（P1） | ❌未完成 |
| D07 | 编辑器 | 保存基线与生命周期 | 保存成功更新基线，移除模块级共享状态，清理卸载事件 | 执行开发（P1） | ❌未完成 |
| D08 | 章节交互 | 树刷新与首章排序 | 章节保存后刷新树，内网查看页按 `sort ASC` 打开首章 | 执行开发（P1） | ❌未完成 |
| D09 | 数据一致性 | 历史与访问统计 | 主内容和历史保存保持一致，统计 SQL 排除软删除章节 | 执行开发（P1） | ❌未完成 |
| D10 | 数据库 | 索引与关联约束 | 增加分享码、文档用户和章节查询所需索引/唯一约束 | 执行开发（P2） | ❌未完成 |
| D11 | 性能维护 | 查询与页面复用 | 视数据量优化文档用户 N+1、重复导航请求和双页面重复代码 | 执行开发（P2） | ❌未完成 |
| D12 | 验证 | 定向回归 | 覆盖权限、XSS、空数据、编辑保存、公开访问和两种数据库脚本 | 执行开发 | ❌未完成 |
| D13 | 视觉交互 | TOC 与图片重排 | 验证 resize、图片加载后目录定位是否漂移，再决定是否修改 | 待验证后执行 | 👀待确认 |

## 背景

- `/admin/dm/doc/doc` 使用 `pageMine` 查询当前用户文档；`/admin/dm/doc/all` 使用通用 `page` 查询全部文档。
- `/admin/dm/doc/edit/:id` 由文档权限、章节树、章节详情、TinyMCE 和历史版本组成。
- `/open/dm/doc/view/:shareCode` 先校验分享码，再独立请求公开章节树和详情。
- 当前最大风险是公开章节/详情接口没有重新校验所属文档是否公开，认证接口也缺少统一的文档资源权限。

## 决策

1. 权限和内容安全以服务端为准，前端隐藏按钮不能视为授权控制。
2. 公开接口统一从 `shareCode` 解析文档，再校验章节归属和 `isPublic`；不接受只凭客户端 `docId` 或 `chapterId` 放行。
3. 复用现有 `BaseBiz`、`BaseTreeBiz`、`BaseController`、`BaseTreeController`、`TableRet`、`useTableQueryParams` 和 `useApiLoading`，不新增状态管理库或重复 CRUD 框架。
4. DDL 同时提供 MySQL 和 PostgreSQL 升级脚本；不以另一种数据库方言作为回退，也不新增不必要的外键。
5. 不在本 ADR 中默认增加自动保存、全文搜索、缓存或复杂的上传/内容版本系统。

## 开发说明

### D01：公开文档资源校验

- 修改 `DocChapterController` 的公开树、分页、章节接口，以及 `DocChapterDetailController` 的公开详情接口。
- 服务端根据 `shareCode` 获取公开文档，再验证章节的 `docId` 与目标文档一致。
- 前端公开页继续使用分享码作为入口，不自行拼接可信的文档权限。
- 参考：`fa-doc/src/main/java/com/faber/api/dm/doc/rest/DocChapterController.java`、`DocChapterDetailController.java`、`DocBiz.java`。

### D02：富文本白名单清洗

- 在 `DocChapterDetailBiz.updateById` 的保存边界执行允许标签/属性/协议的 HTML 清洗。
- 历史版本保存使用清洗后的内容，公开页、内网查看页和历史页继续渲染同一安全结果。
- 白名单保留标题、段落、列表、表格、链接、图片、音视频和代码块；脚本、事件属性、危险协议、`style` 和 `iframe` 删除。
- 参考：`fa-doc/src/main/java/com/faber/api/dm/doc/biz/DocChapterDetailBiz.java`、`frontend/apps/admin/features/fa-doc-pages/pages/open/dm/doc/view/[shareCode].tsx`。

### D03：文档级资源授权

- 在 Biz 层校验当前用户是否为文档创建者、参与用户或系统管理员。
- 覆盖章节树、章节详情、章节历史、文档用户列表、添加/删除用户以及继承自基类的更新/删除接口。
- 保留菜单权限作为入口权限，但不能用菜单权限替代资源归属校验。
- 参考：`fa-base/src/main/java/com/faber/config/interceptor/PermissionInterceptor.java`、`fa-core/src/main/java/com/faber/core/web/rest/BaseController.java`、`fa-doc/src/main/java/com/faber/api/dm/doc/biz/`。

### D04：分享码强度与唯一性

- 将 `DocModal` 的默认生成逻辑改为调用服务端生成或使用密码学安全随机值。
- 不主动使已有分享链接失效；新生成和修改分享码必须满足唯一性。
- MySQL、PostgreSQL 都增加 `share_code` 唯一约束前，先检查并处理已有重复数据。
- 参考：`frontend/apps/admin/features/fa-doc-pages/pages/admin/dm/doc/doc/modal/DocModal.tsx`、`fa-doc/src/main/java/com/faber/api/dm/doc/biz/DocBiz.java`。

### D05：空数据与异常处理

- `DocBiz.getMineById` 在文档不存在或已删除时返回明确业务异常，不访问空对象。
- `pageMine` 在无文档时返回完整的空分页结构，至少包含 `pagination` 和空 `rows`。
- 编辑页、查看页、章节详情和弹窗请求失败时保留错误反馈，不把“请求未完成”误显示为“文档不存在”。
- 参考：`DocBiz.java`、`TableRet.java`、`useTableQueryParams.tsx`。

### D06：loading 机制迁移

- 文档模块从 `ApiEffectLayoutContext` 迁移到 `useApiLoading`。
- 编辑页和内网查看页使用 `getMineById/:id` 的真实 URL；弹窗使用 `save`、`update` 的真实 URL。
- 公开页和 H5 页也统一使用当前请求 loading store，不继续依赖旧 bus 事件。
- 参考：`frontend/fa-ui/packages/ui/src/utils/request.ts`、`frontend/fa-ui/packages/ui/src/hooks/useApiLoading.ts`、`fa-doc-pages/pages/**`。

### D07：编辑器保存基线与生命周期

- 将 `hasChange`、当前章节详情和保存基线放到组件实例内，禁止使用模块级共享变量。
- 保存成功后更新基线；保存失败显示明确反馈，并保留未保存状态。
- 卸载时清理 `window.onbeforeunload`，切换章节时按需等待当前保存完成。
- 不在没有需求确认前增加自动保存；先保证手动保存、章节切换和历史恢复正确。
- 参考：`frontend/apps/admin/features/fa-doc-pages/pages/admin/dm/doc/edit/[id].tsx`、`frontend/fa-ui/packages/ui/src/components/base-editor/BaseTinyMCE.tsx`。

### D08：章节树刷新与首章排序

- `DocChapterModal` 保存成功后调用 `BaseTree` 传入的 `fetchFinish`，确保新增/编辑后树立即刷新。
- 内网查看页首章查询显式传递 `sorter: 'sort ASC'`，与公开页和 H5 页一致。
- 为编辑页、内网查看页和公开页补充必要的路由参数依赖；只有确认存在同页快速切换问题时再加入请求取消。
- 参考：`BaseTree.tsx`、`DocChapterModal.tsx`、`pages/admin/dm/doc/view/[id].tsx`。

### D09：历史与访问统计一致性

- 主章节详情更新和历史版本写入应处于同一业务事务边界，避免只生成历史但主内容更新失败。
- 访问统计 SQL 显式排除已软删除章节，章节自增也不能更新已删除记录。
- 公开详情计数必须建立在 D01 的公开文档和章节归属校验之后。
- 参考：`DocChapterDetailBiz.java`、`DocChapterHisBiz.java`、`DocMapper.xml`、`DocChapterMapper.xml`。

### D10：索引与关联约束

- 增加 `share_code` 唯一约束。
- 增加 `dm_doc_user(doc_id, user_id)` 唯一约束及查询索引。
- 增加章节树、章节历史常用过滤字段的组合索引。
- 同步提供 MySQL 和 PostgreSQL 版本升级脚本，不直接修改已发布基线脚本。
- 参考：`fa-doc/src/main/resources/sql/fa-doc/mysql/`、`fa-doc/src/main/resources/sql/fa-doc/postgre/`。

### D11：查询性能与页面复用

- 数据量较大时，将 `DocBiz.decorateOne` 的逐文档用户查询改为批量查询；小数据量阶段不提前引入缓存。
- 上下两个 `DocFooterNav` 复用父页面已加载的章节顺序，避免同一页面重复请求章节树。
- `doc` 与 `all` 列表在确认权限差异可参数化后再合并，避免为了消除重复代码引入复杂抽象。
- 用户批量添加/删除在需要时改为批量查询并增加事务，先以数据库唯一约束保证正确性。

### D12：定向验证

- 验证未授权用户不能读取或修改其他文档的章节、详情和用户关联。
- 验证私有文档不能通过公开章节树、章节 ID 或详情 ID访问。
- 验证危险 HTML 被清洗，允许的图片、视频、代码块和链接仍可用。
- 验证无文档列表、错误 ID、loading、章节切换保存、历史恢复、树刷新和首章排序。
- 分别检查 MySQL/PostgreSQL 升级脚本、重复数据处理和索引语法；不运行全量测试或启动完整服务作为本 ADR 的必要条件。

### D13：TOC 与图片重排（待确认）

- 本项先不修改实现。
- 只有在浏览器验证确认图片加载、resize 或多实例导致目录定位漂移后，再调整 `FaToc` 和 `FaRichHtmlImgPreview` 的测量时机。

## 非目标

- 不重写公共 `BaseTree`、`BaseBizTable`、`useTableQueryParams` 或 TinyMCE。
- 不新增全文搜索、缓存、自动保存、分享访问统计平台或复杂版本合并能力。
- 不在没有数据规模证据前引入缓存、虚拟滚动或新的状态管理方案。
- 不修改已有分享链接的格式，除非后续安全评估确认必须迁移。

## 验收标准

- 公开接口只能返回分享码对应的公开文档章节和详情。
- 认证接口对章节、详情、历史和用户关联执行文档级权限校验。
- 富文本保存、历史查看和公开渲染不存在未清洗的危险 HTML。
- 无文档列表、错误 ID、loading、章节切换保存和历史恢复均有稳定结果。
- 章节保存后树立即更新，内网和公开页首章均按章节 `sort` 顺序打开。
- MySQL/PostgreSQL 的约束和索引脚本可重复执行，并与实体字段类型一致。

## 状态维护

- 开发开始后，将对应功能进度改为 `🟡进行中`。
- 代码完成后进入 `🔍验证中`，保留未通过项的说明。
- 用户确认验证成功后，将对应功能和 ADR 状态更新为 `✅已完成`。
- 如果需求范围改变，优先更新本 ADR 的功能清单和非目标，再开始扩展实现。
