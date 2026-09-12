---
id: select
slug: /features/milvus/sql/select
sidebar_position: 1
title: SELECT
---

:::info[说明]
对应 SDK 方法：`query`、`queryIterator`、`search`、`searchIteratorV2`。
:::

## 语法

```text
SELECT { * | field_name [, ...] }
    FROM collection_name [PARTITION partition_name]
    [WHERE condition]
    [ORDER BY vector_field distance_operator query_vector]
    [LIMIT row_count] [OFFSET row_offset]
    [WITH (option = value [, ...])];

```

子句按模板顺序书写。没有距离排序和向量范围条件时执行标量 Query；出现其中之一时执行向量 Search。驱动不会先读取全部实体再在 Java 中计算相似度。查询向量只能有一个，结果始终是一个 ResultSet。多路候选融合另见 [HYBRID](hybrid.md)。

<span id="dql" />

本适配器统一使用 `SELECT` 语法进行标量查询（Query）和向量相似度搜索（Search）。


## 标量查询 (Query)

用于精确匹配或范围过滤。
```sql
-- 查询所有字段
SELECT * FROM table_name;

-- 带条件过滤
SELECT * FROM table_name WHERE age > 20 AND status = 1;

-- 指定返回字段与分页
SELECT id, name FROM table_name LIMIT 10 OFFSET 0;

-- 查询指定分区
SELECT * FROM table_name PARTITION partition_name WHERE tag = 'A';
```


## 查询级选项 {#query-options}

标量 SELECT、COUNT 和普通 KNN/范围 SELECT 支持以下 `WITH` 选项；普通请求与官方分页迭代器使用相同设置。

| 选项 | JDBC 参数类型 | SDK 字段与行为 |
| --- | --- | --- |
| ignore_growing | Boolean / setBoolean | ignoreGrowing；true 时跳过 growing segments，可能排除刚写入、尚未封存的数据。默认沿用 SDK 的 false。 |
| timezone | String / setString | timezone；时区名称，例如 UTC、Asia/Shanghai，供服务端时间表达式使用。合法名称及实际时间语义由服务端判断；未指定或空字符串时沿用 SDK 默认行为，不发送时区参数。 |

```sql
SELECT id FROM books WHERE id > ? LIMIT ? WITH(ignore_growing=?,timezone=?);
SELECT COUNT(*) FROM books WHERE id > ? WITH(ignore_growing=?,timezone=?);
COUNT FROM books WITH(ignore_growing=false,timezone='UTC');
```

WITH 参数在 WHERE、LIMIT/OFFSET 后依次绑定，不能把布尔值写成 `'true'` 等字符串。标量 SELECT/COUNT 不接受 `nprobe`、`ef`、`round_decimal` 等向量搜索参数，也不允许通过 WITH 设置 limit/offset；未知选项会抛出 SQLException，不再静默忽略。原有通用 WITH 解析器对重复键保留最后一个值，但所有 `?` 仍按 SQL 顺序消费，建议每个键只写一次。

`timezone` 只传入 SDK 请求，不修改 JVM/JDBC 时区，也不改变 VARCHAR 字段的存储类型。`overwrite_find_as_count` 将 SELECT 改为 COUNT 时仍保留这两项标量选项。Hybrid 不接受这两项外层选项，其时区应按路设置，见 [Hybrid Search](hybrid.md#hybrid)。API 对照：[Query](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/query.md)、[QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md)。


## 向量搜索 (Search)

使用特有的 `<->` 运算符或 `vector_range` 函数表示向量距离计算。

```sql
-- 基本搜索 (KNN, 默认参数)
SELECT * FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;

-- 带前置过滤的搜索
SELECT * FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 5;

-- 范围搜索/距离过滤 (Range Search)
-- 方式1：使用 vector_range 函数 (推荐)
SELECT * FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.8) LIMIT 5;

-- 方式2：使用比较表达式 (WHERE vector_col <-> [vector] < distance_threshold)
SELECT * FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.8 LIMIT 5;
```


## 距离类型与范围约束

| 距离算子 | Milvus metric | 结果排序 |
| --- | --- | --- |
| `<->` | L2 | 分数越小越近。 |
| `<=>` | COSINE | 分数越大越相似；不是 `1 - cosine`。 |
| `<#>` | IP | 分数越大越相似；不对内积取负。 |
| `~=` | HAMMING | 二进制向量，分数越小越近。 |
| `<%>` | JACCARD | 二进制向量，距离越小越近。 |
| `<?>` | BM25 | 稀疏全文检索，分数越大越相关；文本输入见[函数与分词](../ddl/functions.md)。 |

SELECT、UPDATE、DELETE 使用相同映射。字段类型、索引和 metric 必须兼容；驱动不在客户端转换距离或重新排序，也不提供距离算子后的 ASC/DESC。不同 metric 的 score 不能直接作为统一的相似度百分比。

范围条件支持 `v <-> ? < radius`，以及 `v <=> ? > threshold`、`v <#> ? > threshold`。`vector_range(v, vector, radius)` 固定表示 L2 小于半径。半径/阈值必须为有限数值参数或数值字面量；L2 非负，COSINE 在 [-1,1] 内，IP 可为负；负阈值使用 PreparedStatement 参数绑定。字符串、null、NaN、Infinity 等会在搜索/写入前失败。

仅支持一个向量范围通过 AND 与标量条件组合，不支持含向量范围的 OR/NOT、多个向量范围、其他比较方向或与 ORDER BY 叠加；这些情况明确报错，不丢弃约束。WHERE 范围不能被 WITH 的 radius/range_filter 覆盖。边界方向遵循 [Milvus 2.6 范围搜索规则](https://milvus.io/docs/v2.6.x/range-search.md)。


## 分页与 JDBC 返回上限 {#pagination}

- SQL LIMIT 必须为正整数，OFFSET 必须为非负整数；不截断小数，不接受数字字符串，溢出时报错。参数可用 Byte/Short/Integer/Long/BigInteger，或数值为整数且范围合法的 BigDecimal；Float/Double 不用于条数。
- 优先顺序：Hint 的 `overwrite_find_limit/skip` 覆盖 SQL LIMIT/OFFSET；正值 `setMaxRows` 再限制最多返回行数。即使 SQL 参数被覆盖，也会按位置绑定和校验。
- 标量、KNN、范围 SELECT 均按需读取。无 LIMIT、有效上限大于单页或带 OFFSET 时使用官方迭代器；一页内且无 OFFSET 的有界查询保留普通 Query/Search 请求。COUNT 仍返回单行统计结果。
- `Statement.setFetchSize(n)` 在执行前设置页大小；未设置时使用 SDK 默认值，超过 SDK 单页最大值时截到单页上限，不影响返回总量。OFFSET 在 JDBC 游标内逐页跳过，再按有效 LIMIT/maxRows 返回；很大的 OFFSET 仍有相应扫描成本。OFFSET 与有效上限之和超出 long 范围时在查询前拒绝。
- 查询在 SQL 执行时建立，SDK 初始化可能预取首批数据；ResultSet 游标推进时才继续消费页，不在返回 ResultSet 前遍历完整集合。驱动只保留当前页和当前行；SDK 内部缓存与服务端搜索限制仍由 SDK/服务端决定，无 LIMIT 不另加固定总条数。
- 达到返回上限、读到 EOF、关闭 ResultSet、重新执行/关闭 Statement 或关闭 Connection 都释放迭代器。多语句结果通过标准 `getMoreResults` 访问，KEEP_CURRENT_RESULT 保留各自游标，CLOSE_ALL_RESULTS 释放已保留结果。
- execute 返回后仍可用 `Statement.cancel()` 取消本次执行的未读结果，不影响同连接其他 Statement。查询超时从执行开始计时，包含后续读取和调用方停顿；取页前后检查，空闲超时在下一次读取时报告并释放。取消/关闭不会等待在途 SDK 取页完成，该调用返回后丢弃结果并释放；不承诺强制中断 RPC。
- 后续取页错误可能从 `ResultSet.next()` 抛出，游标不会自动重开或重放；读失败与关闭失败同时发生时保留主异常及 suppressed。始终用 try-with-resources 关闭未读完的结果。

迭代器参数对照：[QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md)、[SearchIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/searchIterator.md)。


## 高级搜索参数 (WITH 子句)

本节描述向量 SELECT 的搜索参数；标量 SELECT/COUNT 仅接受[查询级选项](#query-options)，不接受索引搜索或分组参数。

WITH 在 SQL 最后绑定；键是固定名称，值支持字符串、数值、布尔和 `?`。属性通过 JSON 序列化器编码，引号、反斜杠、控制字符不会产生额外属性；带引号的数字仍是字符串。搜索参数例如 `nprobe`、`ef` 直接写在 WITH 中。

普通 KNN 和范围 SELECT 还支持 `round_decimal`（整数，距离分数的小数位数，具体范围由服务端验证），并使用上述 `ignore_growing` / `timezone` 查询级选项。这些值进入 SDK 独立请求字段，不混入索引 searchParams；普通、迭代及分组检索均保留设置。示例：`WITH (round_decimal=2, ignore_growing=false, timezone='UTC')`；参数分别使用 `setInt`、`setBoolean`、`setString`，不使用带引号的数字或布尔字符串。本段不适用于 Hybrid Search。

`metric_type` 若指定，必须与 SQL 距离运算符一致。分页用 SQL OFFSET 或 Hint，不使用 WITH offset。WITH 只接受本节表格列出的搜索参数；一致性使用 JDBC 连接参数，输出字段由 SELECT 决定。

```sql
SELECT id, score FROM table_name
WHERE age > ?
ORDER BY vector_col <=> ?
LIMIT ? OFFSET ?
WITH (metric_type='COSINE', nprobe=?);
```


## 向量分组搜索 {#grouping}

当同一文档的多个片段占据相似度搜索的前几名时，可以按文档 ID 或类别分组，由 Milvus 选取每组的匹配实体。这是原生 ANN/Hybrid 分组，不是关系型 GROUP BY、DISTINCT 或 SUM/COUNT 聚合。

```sql
SELECT id, category, score FROM books ORDER BY vector_col <-> ?
WITH (group_by_field='category', group_limit=3, group_size=2, strict_group_size=true);

SELECT id, category FROM books ORDER BY vector_col <-> ? LIMIT 3 OFFSET 1
WITH (group_by_field='category', group_limit=2, group_offset=1, group_size=2, strict_group_size=true);
```

| WITH 选项 | 含义 |
| --- | --- |
| group_by_field | 必填的非空字段名字符串，映射到 SDK groupByFieldName；具体字段类型和索引兼容性由 Milvus 检查 |
| group_limit | 必填的正 INT32，表示最多返回多少组，映射到原生 Search topK / Hybrid limit |
| group_offset | 非负 BIGINT，原生组偏移，省略时为 0；与 group_limit 相加不得溢出 |
| group_size | 可选正 INT32，每组期望返回的实体数；省略时保留 SDK/服务端默认值 |
| strict_group_size | 可选布尔值，要求服务端尽力达到每组的 group_size；不足的组不会由驱动补齐 |

以上值均可用 `?` 绑定，整数和布尔值使用对应 JDBC 类型。选项只放在 SELECT 最后的 WITH 中；Hybrid 的每路 ANN WITH 不接受分组选项。SQL LIMIT/OFFSET 及其覆盖 Hint、JDBC maxRows 始终以**行**为单位，在原生组窗口返回后应用，可能截断某个组。未写 SQL LIMIT 时读取所选组窗口的全部行，不是扫描全部组；group_limit 仍必须显式指定，不从 SQL LIMIT 推断，也没有固定上限兜底。

第一例选择最多 3 组、每组期望 2 条，可能返回 6 行；第二例先由 Milvus 跳过 1 组并选择 2 组，然后 JDBC 跳过其中 1 行、最多返回 3 行。分组、组内选择和排序由服务端完成，驱动不重新聚合、补满组或重新排序。结果仍是一个扁平 ResultSet，列只取 SELECT 投影；需要组字段时应显式选择该列。

分组搜索使用一次有界 Search/Hybrid 请求，不支持分组迭代器。fetchSize 不会把组窗口拆成多次服务端搜索，maxRows 也不会缩小已请求的组数；请按业务需要选择 group_limit 和 group_size，并遵守服务端检索窗口限制。范围检索、数据类型、索引和服务端版本的组合限制直接返回 SQLException，不回退成客户端分组。

Java SDK 2.6.22 的 SearchIteratorReqV2 虽声明了 groupByFieldName，但 Milvus 2.6.2 原生调用拒绝该组合，返回 `Not allowed to do groupBy when doing iteration`。同一环境的非分组迭代正常；不能以请求类存在字段推定支持分组分页，也不将此结论自动推广到后续服务端版本。

Hybrid 也可在最终 WITH 中同时指定 reranker 和以上选项；此时 group_limit 取代“必须提供外层 SQL LIMIT”的要求，各路候选 LIMIT 仍以候选实体为单位：

```sql
SELECT id,category,score FROM books ORDER BY HYBRID (
    vector_col <-> ? LIMIT 20,
    other_vector <-> ? LIMIT 20
) WITH (reranker='rrf',group_by_field='category',group_limit=3,group_size=2,strict_group_size=true);
```

分组能否用于特定向量类型、索引、字段和重排策略，取决于服务端支持的组合。参数语义参阅[官方分组搜索说明](https://blog.milvus.io/docs/v2.6.x/grouping-search.md)。


## JDBC 结果元数据

列顺序遵循 SELECT，空结果保留 schema 元数据。Int8/16/32/64 对应 TINYINT/SMALLINT/INTEGER/BIGINT，Float/Double/Bool/VarChar 对应标准类型；JSON 为 OTHER（JsonElement），FloatVector/Array 为 ARRAY（List/getArray），Binary/FP16/BF16/Int8Vector 为 VARBINARY（byte[]/getBytes），Sparse 为 OTHER（SPARSE_FLOAT_VECTOR，SortedMap&lt;Long,Float>）。getArray 元素类型、isNullable、isAutoIncrement 来自 schema。向量 score 为 Float，不作为存储字段发送；向量 SELECT * 包含它，显式投影仅指定 score 时返回；标量查询不生成 score。
