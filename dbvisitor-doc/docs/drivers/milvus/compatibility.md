---
id: compatibility
sidebar_position: 5
title: 版本与支持范围
description: jdbc-milvus 的运行环境、服务端版本要求、功能条件和 JDBC 使用边界。
---

## 驱动与运行环境 {#release}

本组文档适用于 `jdbc-milvus 6.7.1-SNAPSHOT`，不适用于旧版驱动。运行环境要求 Java 17+，使用 Milvus Java SDK `2.6.22` 的 V2 API。

SNAPSHOT 版本可从源码构建并安装到本地仓库，依赖配置见[安装与使用](./usecase.mdx)。使用已发布版本时，请阅读对应版本的文档；可用制品见 [Maven Central](https://repo.maven.apache.org/maven2/net/hasor/jdbc-milvus/maven-metadata.xml)。

## 服务端要求 {#servers}

| 服务端 / 环境 | 要求与限制 |
| --- | --- |
| Milvus 2.6.x | 最低版本为 2.6.2，UPDATE 依赖该版本提供的 Partial Upsert。可选功能可能要求更高的补丁版本。 |
| 向量字段 nullable | 要求 Milvus 2.6.18+；向量不支持 IS NULL / IS NOT NULL 过滤。 |
| Milvus 2.5.x 及更早版本、2.6.0 / 2.6.1 | 不支持，不提供旧版 API 回退。 |
| Milvus 3.0.x | 不在本文的支持范围内。 |
| Zilliz Cloud | 使用集群标准端点、token/API key 和 TLS。需具备相应权限并满足集群的网络访问要求；Import 还需要端点提供对应 REST API。 |

版本依据：[官方 SDK 兼容表](https://github.com/milvus-io/milvus-sdk-java#milvus-java-sdk)、[Partial Upsert](https://milvus.io/docs/v2.6.x/upsert-entities.md)、[向量 nullable](https://github.com/milvus-io/milvus/releases/tag/v2.6.18)。

## 功能条件 {#features}

| 能力 | 驱动行为 | 使用条件 / 限制 |
| --- | --- | --- |
| 标量、KNN、范围 SELECT | 小结果单次查询，大结果或无 LIMIT 时按需取页 | 一个距离表达式只接受一个查询向量；fetchSize 是页大小，LIMIT/maxRows 才是总量限制。 |
| UPDATE / DELETE | UPDATE 分页选取主键，只提交主键与 SET 字段；无 LIMIT 的标量 DELETE 直接提交过滤条件，其余 DELETE 分页选取主键后删除 | 无 LIMIT 不附加固定总量；不提供事务，可能部分成功。 |
| 参数化表达式 | WHERE 参数通过 SDK 的 filterTemplateValues 绑定，适用于查询、COUNT、删除、UPDATE 选行、Hybrid 子查询及分页 | 参数只表示值，不能替换字段名、运算符或 SQL 片段。LIMIT/OFFSET 单独校验整数范围。 |
| Float/Binary/FP16/BF16/Sparse | 按字段 schema 编码、读取和搜索 | Java 输入、距离类型、索引必须匹配字段，见[类型绑定](./usecase.mdx#typed-values)。 |
| nullable / DEFAULT / Array | 约束、默认值和 Array 元素/容量映射到 SDK | DEFAULT 只支持规定的非主键标量；Array 不能嵌套或含 NULL 元素；向量 nullable 有更高版本要求。 |
| Generated Keys | INSERT/UPSERT 请求 RETURN_GENERATED_KEYS 后读取 SDK 返回的 ID | 支持 Int64/VarChar 和 AutoID；不请求时不积攒主键，请求全部主键需要相应内存。 |
| Hybrid / rerank | 多路候选由服务端以 RRF 或 Weighted 融合为一个结果集 | 必须显式指定外层 LIMIT；没有 Hybrid Iterator，fetchSize 不能绕过服务端搜索窗口。 |
| BM25 / TextEmbedding | 定义分析器和函数，输出由服务端计算 | TextEmbedding 需要服务端支持，并配置 provider/model、凭据和网络。 |
| 多行写入 / Import | VALUES 或 Iterable/Iterator 分页写入；REST 提交、查询任务与失败信息 | 不等于 JDBC Batch；Import 文件须预先生成并上传到 Milvus 可访问的对象存储。 |
| TLS / mTLS / Cloud | SDK 与 Import REST 共用 JDBC 主机、端口、认证和证书配置 | 不跳过证书校验，不探测第二个端口；使用 Import 时，入口必须同时提供 gRPC 与 REST，见[连接与部署要求](./params.md#tls)。 |

具体语法和返回列见[语法手册](./commands.md)。索引、函数、导入等操作还受服务端配置、权限和资源限额约束。

## JDBC 与非事务边界 {#boundaries}

不支持事务、savepoint、整体回滚、JDBC addBatch/executeBatch、存储过程和可更新 ResultSet。SQL 不支持 JOIN/GROUP BY、任意投影表达式、列别名或标量 ORDER BY；计数使用 `COUNT FROM ...`。

UPDATE 的 Partial Upsert 不会由驱动回写未修改字段，但同一字段的并发更新仍可能相互覆盖，也不提供跨页原子性、隔离或精确一次保证。`maxRetry` 只重试已识别的临时 UPDATE Partial Upsert / DELETE 写错误；成功的页面不会回滚，失败页也可能已经生效。错误信息与取消行为见[分页写入](./commands.md#dml)。

多条 SQL 通过 `execute()` / `getMoreResults()` 访问，不是 JDBC Batch 或事务。分页读取错误可能在 `ResultSet.next()` 时抛出，使用后应及时关闭结果集。依赖任意关系型 SQL、完整 DatabaseMetaData 或事务行为的 ORM、BI、迁移工具，需要确认其实际使用的接口是否在驱动支持范围内。
