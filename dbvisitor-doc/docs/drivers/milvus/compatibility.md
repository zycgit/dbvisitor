---
id: compatibility
sidebar_position: 5
title: 发布与支持矩阵
description: jdbc-milvus 的源码与发布版本、SDK 依赖、服务端基线、功能要求和验证范围。
---

## 源码与发布版本 {#release}

本组 Milvus 文档描述当前仓库的实现，不应直接套用到旧发布版。以下版本信息核对于 2026-09-08。

| 对象 | 版本 / 状态 | 含义 |
| --- | --- | --- |
| 当前源码 | `6.7.1-SNAPSHOT` | 来自根目录 `gradle.properties`；本文新增能力以该源码为准。 |
| 构建与运行 | Java 17+ | 当前构建按 Java 17 编译；不是 SDK 自身的最低 Java 要求。 |
| Milvus Java SDK | `2.6.22` | 来自 `jdbc-milvus/build.gradle`；驱动直接使用 V2 API。 |
| Maven Central 的 jdbc-milvus | `6.7.0` | 核对时元数据仅列出此版本；不包含当前工作区新增能力的承诺。 |

已发布制品以 [Maven Central 元数据](https://repo.maven.apache.org/maven2/net/hasor/jdbc-milvus/maven-metadata.xml)为准。仓库版本号、Git tag、网站的全局版本提示与 Central 制品是不同对象，不能互相代替发布证据。这里不把 SNAPSHOT 标为已发布。

当前源码本地安装、Maven/Gradle 坐标和 `all` classifier 的用法见[安装与使用](./usecase.mdx)。发布版应核对实际 POM、依赖和 classifier，再阅读对应源码版本的文档。

## 服务端支持范围 {#servers}

“最低基线”是项目采用的版本门槛，不表示该版本覆盖所有可选能力，也不表示每个后续版本都已验收。

| 服务端 / 环境 | 当前范围 | 验证边界 |
| --- | --- | --- |
| Milvus 2.6.2 | 最低基线；UPDATE 使用原生 Partial Upsert | 提供此版本的本地 Docker 环境；完整功能真集群矩阵仍需验收。 |
| Milvus 2.6.x，版本不低于 2.6.2 | 当前适配目标，使用 SDK 2.6.22 | 不声明整个版本区间均已通过；按具体补丁版本和功能验证。 |
| Milvus 2.6.18+ 的向量 nullable | 驱动已建模，服务端从 2.6.18 提供此能力 | 不能用最低基线 2.6.2 验收；向量不支持 IS NULL/IS NOT NULL 过滤。 |
| Milvus 2.5.x 及更早版本、2.6.0/2.6.1 | 不在当前支持范围 | 不维护旧版本 API 回退路径。 |
| Milvus 3.0.x | 尚未建立支持承诺 | 需要独立评估 SDK、协议与真集群行为，不能从“2.6.2+”推导出支持。 |
| Zilliz Cloud | 标准端点、token/API key、TLS 连接配置已接入 | 实际云端权限、网络和 Import 能力尚需真实集群验收。 |

版本依据：[官方 SDK 兼容表](https://github.com/milvus-io/milvus-sdk-java#milvus-java-sdk)、[Partial Upsert 的 2.6.2 要求](https://milvus.io/docs/v2.6.x/upsert-entities.md)、[2.6.18 向量 nullable 版本说明](https://github.com/milvus-io/milvus/releases/tag/v2.6.18)。官方 SDK 兼容表不是 jdbc-milvus 的测试报告。

## 已接入能力及条件 {#features}

| 能力 | 驱动行为 | 使用条件 / 限制 |
| --- | --- | --- |
| 标量、KNN、范围 SELECT | 小结果单次查询，大结果或无 LIMIT 时按需取页 | 一个距离表达式仅接受一个查询向量；fetchSize 是单页大小，LIMIT/maxRows 才是总量限制。 |
| UPDATE / DELETE | 分页选取主键后写入；UPDATE 只提交主键与 SET 字段 | 无 LIMIT 不附加固定总量；非事务，可能部分成功。 |
| 参数化表达式 | WHERE 参数通过 SDK `filterTemplateValues` 绑定；查询、COUNT、删除、UPDATE 选行、Hybrid 子查询及分页均传递模板参数 | 参数只表示值；字段名、运算符和 SQL 片段不能通过参数替换。LIMIT/OFFSET 独立按整数及范围校验。 |
| Float/Binary/FP16/BF16/Sparse | 按字段 schema 编码、读取、搜索 | Java 输入、metric、索引必须匹配字段；详见[类型绑定](./usecase.mdx#typed-values)。 |
| nullable / DEFAULT / Array | 约束、默认值和 Array 元素/容量映射到 SDK | DEFAULT 仅支持规定的非主键标量；Array 不能嵌套或含 NULL 元素；向量 nullable 有更高版本要求。 |
| Generated Keys | INSERT/UPSERT 请求 RETURN_GENERATED_KEYS 后读取 SDK 返回的 ID | 支持 Int64/VarChar 和 AutoID；不请求时不积攒主键，请求全部主键需要相应内存。 |
| Hybrid / rerank | 多路候选由服务端以 RRF 或 Weighted 融合为一个结果集 | 必须显式外层 LIMIT；没有 Hybrid Iterator，fetchSize 不能绕过服务端搜索窗口。 |
| BM25 / TextEmbedding | 建模分析器和函数，输出由服务端计算 | TextEmbedding 需目标版本与 provider/model、凭据、网络配置，不能仅凭驱动编译通过认定可用。 |
| 多行写入 / Import | VALUES 或 Iterable/Iterator 分页写入；REST 提交、查询任务与失败信息 | 不等于 JDBC batch；Import 文件须预先生成并上传；吞吐和大规模导入需压测。 |
| TLS / mTLS / Cloud | SDK 与 Import REST 共用 JDBC 主机、端口、认证和证书配置 | 不跳过证书校验，不探测第二个端口；统一入口必须同时提供 gRPC 与 REST，见[连接与部署要求](./params.md#tls)。 |

精确语法和返回列见[语法手册](./commands.md)；上表不扩展解析器或服务端的能力。

## JDBC 与非事务边界 {#boundaries}

不提供事务、savepoint、整体回滚、JDBC addBatch/executeBatch、存储过程、可更新 ResultSet、JOIN/GROUP BY、任意投影表达式、列别名或标量 ORDER BY。`COUNT FROM ...` 是本驱动的计数语法。

UPDATE 原生 Partial Upsert 避免由驱动回写未修改字段，但不提供跨页原子性、隔离或精确一次保证。`maxRetry` 只重试已识别的临时分页 UPDATE/DELETE 写错误；已成功页面不会回滚，失败页可能已经生效。失败进度与取消的区别见[分页写入](./commands.md#dml)。

多条 SQL 通过 `execute` / `getMoreResults` 访问，不是 JDBC batch 或事务。分页读取错误可能在 `ResultSet.next()` 抛出；始终关闭结果集。依赖任意关系型 SQL、完整 DatabaseMetaData 或事务行为的 ORM、BI、迁移工具不能仅凭 JDBC 接口就认定兼容。

## 验证与发布核对 {#validation}

在仓库根目录运行离线回归（要求依赖已缓存）：

```bash
./gradlew :jdbc-milvus:build :dbvisitor-driver:test --no-daemon --offline
```

普通 test 排除 `realdb`。文档测试会编译中英文 Java 示例、通过拦截器执行入门/分页/多结果片段、解析 SQL 示例，并核对连接参数表；这些检查不能替代真实服务端验收。

单端口明文/TLS/mTLS 的独立真环境测试：

```bash
./gradlew :jdbc-milvus:tlsTest --no-daemon --offline
```

须先按 [Docker 测试环境](https://github.com/zycgit/dbvisitor/blob/main/dbvisitor-test/docker/README.md)启动对应可选服务。服务缺失时此任务失败，不跳过；运行命令的存在不等于已有完整 CI 兼容矩阵。

发布前核对：

1. 源码/tag、版本号、发布 POM、classifier、SDK 依赖和对应文档一致；从发布制品独立运行 JDBC 示例。
2. 对声明支持的具体 Milvus 版本记录测试环境和结果，区分离线、真集群、TLS、Cloud 与性能测试；未验证项不得标为通过。
3. 验证新增字段类型、向量 nullable、Hybrid/functions、导入状态及失败观察；外部 provider 和 Cloud 使用真实配置验收。
4. 核对 Central 制品可获取后再更新发布声明；本地构建、文档更新或 tag 均不等于已发布。
