---
id: hybrid
slug: /features/milvus/sql/hybrid
sidebar_position: 3
title: HYBRID
---

:::info[说明]
对应 SDK 方法：`hybridSearch`。
:::

## 语法

```text
SELECT { * | field_name [, ...] }
    FROM collection_name [PARTITION partition_name]
    [WHERE scalar_condition]
    ORDER BY HYBRID (
        vector_field distance_operator query_vector
            [WHERE scalar_condition] LIMIT candidate_count
            [WITH (search_option = value [, ...])],
        vector_field distance_operator query_vector
            [WHERE scalar_condition] LIMIT candidate_count
            [WITH (search_option = value [, ...])]
        [, ...]
    )
    [LIMIT row_count] [OFFSET row_offset]
    WITH (reranker = reranker_name [, option = value ...]);
```

至少提供两路候选检索，reranker 必填。普通融合检索还必须指定外层 LIMIT；启用分组时改为必填 group_limit。各路由 Milvus 检索，原生 hybridSearch 完成融合和重排，驱动不在客户端合并多个 SELECT。返回一个扁平 ResultSet，不是一组查询向量对应的多个结果集。


## Hybrid Search 与 rerank {#hybrid}

```sql
SELECT id,score FROM docs WHERE id > ? ORDER BY HYBRID (
    dense <-> ? LIMIT 20 WITH (nprobe=10),
    sparse <?> ? LIMIT 30
) LIMIT 10 OFFSET 2 WITH (reranker='rrf',k=60);

SELECT id,score FROM docs ORDER BY HYBRID (
    dense <=> ? LIMIT 20,
    sparse <#> ? LIMIT 30
) LIMIT 10 WITH (reranker='weighted',weights='[0.7,0.3]');
```

每路仍只有一个查询向量，可以查询相同字段的不同向量，也可以不同字段/metric；向量列表嵌套不表示另一种 NQ 批量接口。可在每路向量后、LIMIT 前写独立的标量 WHERE。该路的有效过滤条件为 `(公共 WHERE) AND (本路 WHERE)`；局部 OR 保留括号，不能绕过公共条件。两种 WHERE 都可省略，局部过滤不会影响其他路。参数按 SQL 顺序连续绑定：公共 WHERE、逐路向量/WHERE/LIMIT/WITH、最终 LIMIT/OFFSET/WITH；所有条件值仍通过 SDK filterTemplateValues 绑定，不拼接回表达式。

```sql
SELECT id,score FROM docs WHERE tenant_id = ? ORDER BY HYBRID (
    dense <-> ? WHERE category = ? OR category = ? LIMIT 20 WITH(timezone='UTC',nprobe=10),
    sparse <?> ? WHERE publish_year >= ? LIMIT 30 WITH(timezone=?)
) LIMIT 10 WITH(reranker='rrf',k=60,round_decimal=3);
```

上例要求集合中已存在 tenant_id、category、publish_year 及相应向量字段。每路 `timezone` 是字符串，进入 AnnSearchReq 的独立字段；空字符串沿用 SDK 默认行为。公共时间过滤也在各路指定的时区下解释。时区不放入最外层 WITH，不提供原生时间类型的客户端模拟。每路 WHERE 只接受标量条件；不要在其中另加向量范围表达式，范围参数仍使用该路的原生搜索参数。

外层 `round_decimal` 是 INT32 范围的整数，映射 HybridSearchReq.roundDecimal，由服务端对最终融合分数舍入，不改变 JDBC 的 score 类型，也不由驱动重新排序。未指定时沿用 SDK 默认值 -1（不舍入）；具体可用精度范围由服务端判断。该参数不能放入每路 WITH。它也可与外层分组参数同时使用。对应 API：[hybridSearch](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/hybridSearch.md)。

每路 LIMIT 是候选量，最终 LIMIT/OFFSET 作用于服务端融合后的一个结果集。RRF 的 k 省略时沿用 SDK 默认；Weighted 必须提供与路数相同的 [0,1] 有限权重。reranker 必须显式选择；不识别的 rerank 参数报错。SDK 无 Hybrid Iterator，所以普通 Hybrid 外层必须显式 LIMIT；启用[分组搜索](select.md#grouping)时改由必填的 group_limit 限定组窗口，SQL LIMIT/OFFSET 仍限定结果行。fetchSize 不将融合变为分页；服务端窗口和候选路数限制仍适用，驱动不以固定总量兜底。普通 ORDER BY 只接受一个查询向量的规则不变。
