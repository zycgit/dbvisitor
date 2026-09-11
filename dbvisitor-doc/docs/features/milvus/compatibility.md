---
id: compatibility
sidebar_position: 5
title: 版本与支持范围
description: jdbc-milvus 的运行环境、服务端版本要求、功能条件和 JDBC 使用边界。
---

## 驱动与运行环境 {#release}

本组文档适用于 `jdbc-milvus 6.7.1-SNAPSHOT`，不适用于旧版驱动。运行环境要求 Java 17+，使用 Milvus Java SDK `2.6.22` 的 V2 API。

SNAPSHOT 版本可从源码构建并安装到本地仓库，依赖配置见[安装与连接](../../drivers/milvus/connection.mdx)。使用已发布版本时，请阅读对应版本的文档；可用制品见 [Maven Central](https://repo.maven.apache.org/maven2/net/hasor/jdbc-milvus/maven-metadata.xml)。

## 服务端要求 {#servers}

| 服务端 / 环境 | 要求与限制 |
| --- | --- |
| Milvus 2.6.x | 最低版本为 2.6.2，UPDATE 依赖该版本提供的 Partial Upsert。可选功能可能要求更高的补丁版本。 |
| 向量字段 nullable | 要求 Milvus 2.6.18+；向量不支持 IS NULL / IS NOT NULL 过滤。 |
| TRUNCATE TABLE | 原生 API 在 Milvus 2.6.11 引入；2.6.2 不支持，较新服务端的成功语义未完成真实验证。 |
| Milvus 2.5.x 及更早版本、2.6.0 / 2.6.1 | 不支持，不提供旧版 API 回退。 |
| Milvus 3.0.x | 不在本文的支持范围内。 |
| Zilliz Cloud | 使用集群标准端点、token/API key 和 TLS。需具备相应权限并满足集群的网络访问要求；Import 还需要端点提供对应 REST API。 |

版本依据：[官方 SDK 兼容表](https://github.com/milvus-io/milvus-sdk-java#milvus-java-sdk)、[Partial Upsert](https://milvus.io/docs/v2.6.x/upsert-entities.md)、[向量 nullable](https://github.com/milvus-io/milvus/releases/tag/v2.6.18)。

## 功能条件 {#features}

| 能力 | 驱动行为 | 使用条件 / 限制 |
| --- | --- | --- |
| 标量、KNN、范围 SELECT | 小结果单次查询，大结果或无 LIMIT 时按需取页 | 一个距离表达式只接受一个查询向量；fetchSize 是页大小，LIMIT/maxRows 才是总量限制。 |
| UPDATE / DELETE | UPDATE 分页选取主键，只提交主键与 SET 字段；无 LIMIT 的标量 DELETE 直接提交过滤条件，其余 DELETE 分页选取主键后删除 | 支持无 WHERE 的全量操作；标量 DELETE 内部使用实际主键 IS NOT NULL，不替换已有 WHERE。无 LIMIT 不附加固定总量；不提供事务，可能部分成功。 |
| 参数化表达式 | WHERE 参数通过 SDK 的 filterTemplateValues 绑定，适用于查询、COUNT、删除、UPDATE 选行、Hybrid 子查询及分页 | 参数只表示值，不能替换字段名、运算符或 SQL 片段。LIMIT/OFFSET 单独校验整数范围。Milvus 2.6.2 拒绝 LIKE 右侧的模板参数；驱动不拼接参数兜底。 |
| dbVisitor 结果处理 | JdbcTemplate 的自定义/内置 RowMapper、RowCallbackHandler、ResultSetExtractor，以及 Map、Bean、标量、键值对和过滤映射 | 映射只处理 SQL 返回的列，不扩展 SQL 语法；Extractor 内自定义聚合运行在客户端，不等于服务端 GROUP BY。逐行回调不代表底层 SDK 请求完全无缓冲。 |
| dbVisitor 参数源 | Object[]、SqlArg/TypeHandler、PreparedStatementSetter、Map/Bean、命名参数、SqlArgSource 和 and/in/set 动态规则 | dbVisitor 处理参数源和动态规则后交给驱动执行原生 SQL，不扩展服务端语法。`${...}` 是纯文本替换，只能使用可信 SQL 片段/标识符，不能作为不可信值的安全绑定。 |
| Float/Binary/FP16/BF16/Int8Vector/Sparse | 按字段 schema 编码、读取和搜索 | Java 输入、距离类型、索引必须匹配字段，见[类型绑定](./jdbc.mdx#typed-values)。 |
| nullable / DEFAULT / Array | 约束、默认值和 Array 元素/容量映射到 SDK | DEFAULT 只支持规定的非主键标量；Array 不能嵌套或含 NULL 元素；向量 nullable 有更高版本要求。 |
| Generated Keys | INSERT/UPSERT 请求 RETURN_GENERATED_KEYS 后读取 SDK 返回的 ID | 支持 Int64/VarChar 和 AutoID；不请求时不积攒主键，请求全部主键需要相应内存。 |
| Hybrid / rerank | 多路候选由服务端以 RRF 或 Weighted 融合为一个结果集 | 普通 Hybrid 必须显式指定外层 LIMIT；分组模式改用必填的 group_limit，SQL LIMIT 始终限制行数。没有 Hybrid Iterator，fetchSize 不能绕过服务端搜索窗口。 |
| BM25 / TextEmbedding | 定义分析器和函数，输出由服务端计算 | TextEmbedding 需要服务端支持，并配置 provider/model、凭据和网络。 |
| 多行写入 / Import | VALUES 或 Iterable/Iterator 分页写入；REST 提交、查询任务与失败信息 | 不等于 JDBC Batch；Import 文件须预先准备在服务端配置的存储中。2.6.2 单机本地存储已验证 JSON 导入及完成状态，见[Import](./commands.md#import)。 |
| TLS / mTLS / Cloud | SDK 与 Import REST 共用 JDBC 主机、端口、认证和证书配置 | 不跳过证书校验，不探测第二个端口；使用 Import 时，入口必须同时提供 gRPC 与 REST，见[连接与部署要求](../../drivers/milvus/connection.mdx#tls)。 |

具体语法和返回列见[语法手册](./commands.md)。索引、函数、导入等操作还受服务端配置、权限和资源限额约束。

## SQL 对 SDK 的覆盖范围 {#sdk-coverage}

以下按 Java SDK 2.6.22 的能力族对照 SQL 入口。“已接入”指列出的入口，不表示该 SDK 请求的所有可选参数均已暴露。完整语法、参数和返回列以[语法手册](./commands.md)为准；本表不用于表示 dbVisitor 的 Lambda、Mapper 等整合能力。

适配器面向向量数据库业务操作，不是官方 SDK 的完整替代品。数据读写、向量及混合检索、schema/索引、加载释放、导入和必要管理通过 SQL 使用；SDK 客户端工具、内部辅助接口及没有明确 SQL 业务场景的方法不逐一包装。

| SDK 能力 / API | SQL 入口 | 覆盖与限制 |
| --- | --- | --- |
| `insert` / `upsert` / `delete` | INSERT / UPSERT / UPDATE / DELETE | 已接入；UPDATE 使用 Partial Upsert，UPSERT 可用布尔 hint `partial_update` 选择部分更新。分页写入不具备跨页事务，见 [Upsert](./commands.md#upsert)。 |
| `query` / `queryIterator` / `search` / `searchIteratorV2` | SELECT、COUNT、向量 ORDER BY、LIMIT/OFFSET | 已接入按需分页、标量过滤及单向量搜索；不提供 SDK 多查询向量的批量结果入口。 |
| Query/Search 的 ignoreGrowing、timezone | SELECT / COUNT … WITH (ignore_growing=…,timezone=…) | 普通请求与对应迭代器使用独立 SDK 字段；标量查询未知选项报错。2.6.2 已验证 SELECT/COUNT 的 growing segment 排除行为；timezone 请求接入不等于原生时间类型支持。Hybrid 的 timezone 改为按路设置，见[查询级选项](./commands.md#query-options)。 |
| `hybridSearch`、RRF / Weighted ranker | Hybrid SELECT | 已接入融合搜索；并非所有 SDK 高级检索参数或 ranker 均有 SQL 入口。 |
| AnnSearchReq.filter / filterTemplateValues / timezone；HybridSearchReq.roundDecimal | HYBRID (向量 WHERE 条件 LIMIT … WITH(timezone=…), …) … WITH(round_decimal=…) | 每路标量过滤与公共 WHERE 取交集，参数由 SDK 绑定；按路设置时区、外层设置融合精度。2.6.2 已在稠密 FLAT/L2 下验证独立过滤、局部 OR 的范围边界及 RRF 分数舍入，见 [Hybrid 用法](./commands.md#hybrid)。 |
| Search / Hybrid 的 groupByFieldName、groupSize、strictGroupSize | SELECT … WITH (group_by_field=…,group_limit=…,…) | 原生组数/组偏移与 SQL/JDBC 行窗口分开；FLAT/L2、VARCHAR 分组和 RRF Hybrid 已在 2.6.2 验证，不表示关系型聚合，见[分组搜索](./commands.md#grouping)。 |
| Collection / Index / Partition API | CREATE / ALTER / DROP / SHOW、LOAD / RELEASE | 已接入基本生命周期、在线加字段及部分属性操作；不代表完整请求参数覆盖。 |
| `truncateCollection` | TRUNCATE TABLE … [IN DATABASE …] | 直接提交指定库、集合的原生清空请求，成功返回更新计数 0，不扫描或重建集合。2.6.11 引入，2.6.2 不可用；SQL 接入不等于已验收新版清空效果，见[清空集合](./commands.md#truncate)。 |
| `listIndexes` / `describeIndex` | SHOW INDEXES / SHOW INDEX / SHOW PROGRESS OF INDEX | 列表后逐个查询具名索引，保留四列详情；省略名称的进度汇总各索引工作量，不是集合实体数。2.6.2 已验证空/多索引列表及进度汇总，见[索引查询](./commands.md#index-metadata)。 |
| `createCollection` 的分区键、聚簇键和创建选项 | CREATE TABLE … PARTITION KEY / CLUSTERING KEY … WITH (…) | 已接入 isPartitionKey、isClusteringKey、numPartitions、numShards、description、consistencyLevel；SHOW TABLE / SHOW CREATE 保留键标志。2.6.2 已验证 INT64/VARCHAR 分区键、独立或共用聚簇键、分区数及过滤搜索/更新/删除；未验收聚簇压缩性能。见[集合键](./commands.md#collection-keys)。 |
| `loadCollection` / `loadPartitions` | LOAD TABLE … WITH (…) | 已接入 numReplicas、refresh、loadFields、skipLoadDynamicField、resourceGroups；同步等待与 timeout 使用原有 Hint 并交由 SDK 执行。多副本调度未真实验证；2.6.2 的字段加载限制见[加载说明](./commands.md#load)。 |
| `renameCollection` | ALTER TABLE … RENAME TO … [IN DATABASE …] | 已接入当前库重命名和 targetDbName；2.6.2 已验证跨库往返后的数据读写与索引可用性。不切换连接数据库、不复制数据或自动建库，见[重命名](./commands.md#rename)。 |
| `createDatabase` / `describeDatabase` / `alterDatabaseProperties` / `dropDatabaseProperties` / `listDatabases` / `dropDatabase` | CREATE / ALTER / SHOW / DROP DATABASE | 已接入数据库及属性管理。 |
| `createAlias` / `alterAlias` / `dropAlias` / `describeAlias` / `listAliases` | CREATE / ALTER / DROP / SHOW ALIAS | 已接入别名生命周期和查询。 |
| `getCollectionStats` / `getPartitionStats` | SHOW STATS | 返回原生统计，不等于即时精确 COUNT。 |
| `describeReplicas` | SHOW REPLICAS FROM [TABLE] collection | 当前 database 的副本、节点、分片及资源组快照；完整保留 SDK 2.6.22 的副本字段。2.6.2 已验证单副本查询和不存在集合的错误处理，未验证多节点调度；不扩展为驱动多地址路由，见[副本状态](./commands.md#replicas)。 |
| `getServerVersionV2` / `checkHealth` | SHOW VERSION / SHOW HEALTH | 服务版本、构建详情、健康原因和限流状态，见[诊断查询](./commands.md#diagnostics)。 |
| `getPersistentSegmentInfo` / `getQuerySegmentInfo` | SHOW PERSISTENT / QUERY SEGMENTS | 原生 segment 快照及所有 SDK 返回字段；不查询实体、不隐式加载或落盘，见[返回列与边界](./commands.md#diagnostics)。 |
| `flush` / `flushAll` / `getFlushAllState` | FLUSH 集合列表；FLUSH ALL TABLES；SHOW FLUSH ALL | 集合列表、数据库作用域及等待超时映射到 SDK；默认等待上限 60000ms，可显式配置。flushAll 也同步等待，成功返回 BIGINT 时间戳；SHOW 单次观察完成状态。2.6.2 的按库完成检查可能受其他数据库影响并超时，官方 SDK 可复现；不是完整全库落盘验收，见[Flush 语义与版本限制](./commands.md#flush)。 |
| `compact` / `getCompactionState` / `getCompactionPlans` | COMPACT、SHOW COMPACTION | COMPACT 异步提交，由 SHOW 观察，不自动等待压缩完成或承诺压缩收益。 |
| 用户、角色、`grantPrivilegeV2` / `revokePrivilegeV2`、权限组 API | USER / ROLE / PRIVILEGE / PRIVILEGE GROUP 命令 | 已接入生命周期、成员及显式范围授权。授权记录验证不等于生产鉴权验收。 |
| `CreateUserReq.description` / `CreateRoleReq.description` / `DropRoleReq.forceDrop` | CREATE USER / ROLE … WITH (description=…)；DROP ROLE … WITH (force_drop=…) | 已接入独立 SDK 字段和参数绑定。2.6.2 已验证强制删除角色及其授权、成员关系而保留用户；创建时传入的说明在该基线读回为空，官方 SDK 同样如此，不表示说明持久化支持。见[用户与角色管理](./commands.md#user)。 |
| `updateUser` / `alterRole` | ALTER USER / ROLE … WITH (description=?) | SQL 已接入；2.6.2 环境不支持这两项说明更新，其他版本需确认。不会用重设密码或重建角色替代。 |
| ResourceGroup API、`transferNode` / `transferReplica` | RESOURCE GROUP、TRANSFER NODES / REPLICAS | 已接入配置、查询和迁移请求；零节点配置已验证，实际迁移、标签选择及非零节点调度未验收。 |
| `runAnalyzer` | ANALYZE | standard 分词、多文本/空文本、详情与 hash 已验证；字段上下文和命名分词器参数已接入，但未完成真实服务验证。 |
| Collection schema 的向量、nullable、Array、functions | CREATE TABLE 字段和函数定义 | 支持 Float/Binary/FP16/BF16/Int8Vector/Sparse、标量 nullable/default、Array、BM25 及 TextEmbedding 定义，不代表所有 SDK 新类型。函数 DESCRIPTION 独立于 WITH 参数，2.6.2 已验证说明往返和 BM25 生成向量后的检索。 |
| `addCollectionFunction` / `alterCollectionFunction` / `dropCollectionFunction` | ALTER TABLE … ADD / ALTER / DROP FUNCTION | 原生请求已接入，ALTER 提交同名函数的完整新定义。2.6.2 三个 API 均返回 UNIMPLEMENTED，不重建集合兜底；新版在线变更效果未验收，见[在线函数管理](./commands.md#alter-functions)。 |
| Import REST API（不是 BulkWriter 文件生成） | IMPORT 与任务查询命令 | 支持提交和状态/失败观察；不生成或上传导入文件，不覆盖 BulkWriter 全部能力。 |

TLS/mTLS 与 Cloud 的连接配置见[连接参数](../../drivers/milvus/connection.mdx#tls)。SQL 功能通过不等于 Cloud、证书部署或吞吐性能已验收。尚未列出的 SDK 方法及可选参数不能据此推定支持，尤其是高级检索选项、新数据类型及其他维护 API。

## JDBC 与非事务边界 {#boundaries}

不支持事务、savepoint、整体回滚、JDBC addBatch/executeBatch、存储过程和可更新 ResultSet。SQL 不支持 JOIN/GROUP BY、任意投影表达式、列别名或标量 ORDER BY；计数使用 `COUNT FROM ...` 或独立的 `SELECT COUNT(*) FROM ...`。

VARCHAR 中的日期仍是字符串，不是 Milvus 原生 DATE/TIMESTAMP。无 Calendar 的 JDBC Date/Time/Timestamp 绑定已验证 INSERT/UPSERT、过滤和对应 getter 往返，另验证 Timestamp 的 UPDATE/NULL；Timestamp 的小数秒作为文本保留，java.util.Date 使用毫秒精度。不会自动解析或迁移旧地区格式文本，不表示 Calendar、跨时区或服务端日期运算已验证，见[参数类型](./jdbc.mdx#parameter-types)。

Milvus 2.6.2 的整数模板比较被 NOT 包裹时（包括双重 NOT），原生 SDK 可复现 QueryNode 断言。该边界不等于所有 NOT 都不支持：字面量 NOT 和 `NOT (field IS NULL)` 已验证可用。驱动不通过参数插值或消除 NOT 掩盖该服务端问题；Lambda 条件也受此限制，见[Lambda 查询](./jdbc.mdx#lambda-query)。

同一基线也拒绝字段条件与常量条件的 AND/OR 组合，例如 `id IN (1,2) AND 1=1`，见[官方解析实现](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/parser/planparserv2/parser_visitor.go#L1019)。原生 SDK 的 Query/Delete 均可复现；加括号或改成 `AND true` 不解决该问题。驱动不会删除条件、客户端求值或改为空过滤器兜底。已验证被拒绝的删除保留原记录并阻止同批后续语句执行；不代表运行中断的任意写入都能回滚。

UPDATE 的 Partial Upsert 不会由驱动回写未修改字段，但同一字段的并发更新仍可能相互覆盖，也不提供跨页原子性、隔离或精确一次保证。`maxRetry` 只重试已识别的临时 UPDATE Partial Upsert / DELETE 写错误；成功的页面不会回滚，失败页也可能已经生效。错误信息与取消行为见[分页写入](./commands.md#dml)。

多条 SQL 通过 `execute()` / `getMoreResults()` 访问，不是 JDBC Batch 或事务。分页读取错误可能在 `ResultSet.next()` 时抛出，使用后应及时关闭结果集。依赖任意关系型 SQL、完整 DatabaseMetaData 或事务行为的 ORM、BI、迁移工具，需要确认其实际使用的接口是否在驱动支持范围内。
