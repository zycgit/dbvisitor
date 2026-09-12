---
id: hybrid
slug: /features/milvus/sql/hybrid
sidebar_position: 3
title: HYBRID
---

:::info[Note]
SDK methods: `hybridSearch`.
:::

## Syntax

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

At least two candidate branches and an explicit reranker are required. Ordinary hybrid search also requires an outer LIMIT; grouped search requires group_limit instead. Milvus retrieves candidates and native hybridSearch performs fusion and reranking. The driver does not merge independent SELECT results in Java. The result is one flat ResultSet, not multiple result sets for a batch of query vectors.


## Hybrid Search and rerank {#hybrid}

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

Each candidate has one query vector; candidates may target the same or different fields/metrics. Nested lists are not an NQ batch interface. Each candidate may have a scalar WHERE after its vector and before LIMIT. Its effective filter is `(shared WHERE) AND (local WHERE)`; local OR expressions remain parenthesized and cannot bypass the shared scope. Both WHERE clauses are optional, and local conditions do not affect other candidates. Bind in SQL order: shared WHERE, each vector/WHERE/LIMIT/WITH, final LIMIT/OFFSET/WITH. All filter values remain in SDK filterTemplateValues rather than being interpolated into expressions.

```sql
SELECT id,score FROM docs WHERE tenant_id = ? ORDER BY HYBRID (
    dense <-> ? WHERE category = ? OR category = ? LIMIT 20 WITH(timezone='UTC',nprobe=10),
    sparse <?> ? WHERE publish_year >= ? LIMIT 30 WITH(timezone=?)
) LIMIT 10 WITH(reranker='rrf',k=60,round_decimal=3);
```

This example requires existing tenant_id, category, publish_year and vector fields. Each candidate's string timezone enters the dedicated AnnSearchReq field; an empty string retains SDK defaults. Shared temporal filters are interpreted under each candidate's timezone as well. Do not put timezone in the outer WITH clause; native temporal types are not emulated. Candidate WHERE accepts scalar conditions only, not an additional vector-range expression; use that candidate's native search parameters for range settings.

Outer round_decimal is an INT32-range integer mapped to HybridSearchReq.roundDecimal. The server rounds final fused scores; the driver neither changes the JDBC score type nor re-sorts results. Omission retains the SDK default -1 (no rounding); the server determines valid precision values. Do not put round_decimal in candidate WITH clauses. It can accompany outer grouping options. API reference: [hybridSearch](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/hybridSearch.md).

Candidate LIMITs control candidate counts; outer LIMIT/OFFSET applies to one server-fused result. RRF k defaults to the SDK value when omitted. Weighted requires one finite [0,1] weight per candidate. Select reranker explicitly; unknown ranker options are rejected. There is no SDK Hybrid Iterator: ordinary Hybrid requires outer LIMIT; [grouped search](select.md#grouping) instead requires group_limit to bound groups, while SQL LIMIT/OFFSET still bounds result rows. fetchSize does not paginate fusion, and server window/candidate limits apply without a magic fallback total. Ordinary ORDER BY retains its single-vector rule.
