---
id: select
slug: /features/milvus/sql/select
sidebar_position: 1
title: SELECT
---

:::info[Note]
SDK methods: `query`, `queryIterator`, `search`, `searchIteratorV2`.
:::

## Syntax

```text
SELECT { * | field_name [, ...] }
    FROM collection_name [PARTITION partition_name]
    [WHERE condition]
    [ORDER BY vector_field distance_operator query_vector]
    [LIMIT row_count] [OFFSET row_offset]
    [WITH (option = value [, ...])];

```

Clauses follow the order shown. Without distance ordering or a vector-range condition, SELECT executes a scalar Query; either vector form selects Search. The driver does not read all entities to compute similarity in Java. Each search takes one query vector and returns one ResultSet. See [HYBRID](hybrid.md) for multi-branch fusion.

<span id="dql" />

This adapter unifies scalar queries (Query) and vector similarity searches (Search) using `SELECT` syntax.


## Scalar Query (Query)

Used for exact matching or range filtering.
```sql
-- Query all fields
SELECT * FROM table_name;

-- With conditional filtering
SELECT * FROM table_name WHERE age > 20 AND status = 1;

-- Specify return fields and pagination
SELECT id, name FROM table_name LIMIT 10 OFFSET 0;

-- Query specific partition
SELECT * FROM table_name PARTITION partition_name WHERE tag = 'A';
```


## Query-level options {#query-options}

Scalar SELECT, COUNT and regular KNN/range SELECT accept these WITH options. Bounded requests and native paginated iterators retain the same settings.

| Option | JDBC parameter type | SDK field and behavior |
| --- | --- | --- |
| ignore_growing | Boolean / setBoolean | ignoreGrowing; true skips growing segments and can exclude recently inserted, unsealed data. The SDK default is false. |
| timezone | String / setString | timezone; a zone name such as UTC or Asia/Shanghai for server-side time expressions. The server determines valid names and time semantics; omission or an empty string retains the SDK default without sending a timezone parameter. |

```sql
SELECT id FROM books WHERE id > ? LIMIT ? WITH(ignore_growing=?,timezone=?);
SELECT COUNT(*) FROM books WHERE id > ? WITH(ignore_growing=?,timezone=?);
COUNT FROM books WITH(ignore_growing=false,timezone='UTC');
```

WITH parameters bind after WHERE and LIMIT/OFFSET, in SQL order. Do not supply booleans as strings such as `'true'`. Scalar SELECT/COUNT reject vector-search parameters such as nprobe, ef and round_decimal, and do not allow limit/offset through WITH. Unknown options throw SQLException instead of being silently ignored. The existing shared WITH parser retains the last value for duplicate keys while consuming every `?` in SQL order; specify each key once to avoid ambiguity.

`timezone` only enters the SDK request. It does not change the JVM/JDBC timezone or the storage type of a VARCHAR field. Rewriting SELECT to COUNT with `overwrite_find_as_count` retains these two scalar options. Hybrid rejects both as outer options; configure its timezone per candidate as described in [Hybrid Search](hybrid.md#hybrid). API references: [Query](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/query.md), [QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md).


## Vector Search (Search)

Use the specific `<->` operator or `vector_range` function to represent vector distance calculation.

```sql
-- Basic Search (KNN, default params)
SELECT * FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;

-- Search with pre-filtering
SELECT * FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 5;

-- Range Search / Distance Filtering
-- Method 1: Using vector_range function (Recommended)
SELECT * FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.8) LIMIT 5;

-- Method 2: Using comparison expression (WHERE vector_col <-> [vector] < distance_threshold)
SELECT * FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.8 LIMIT 5;
```


## Metrics and Range Constraints

| Distance operator | Milvus metric | Result ordering |
| --- | --- | --- |
| `<->` | L2 | Lower scores are nearer. |
| `<=>` | COSINE | Higher scores are more similar; this is not `1 - cosine`. |
| `<#>` | IP | Higher scores are more similar; the inner product is not negated. |
| `~=` | HAMMING | Binary vectors; lower scores are nearer. |
| `<%>` | JACCARD | Binary vectors; lower distances are nearer. |
| `<?>` | BM25 | Sparse full-text retrieval; higher scores are more relevant. For text input, see [Functions and Analyzers](../ddl/functions.md). |

SELECT, UPDATE and DELETE share these mappings. Field types, indexes and metrics must be compatible. The driver neither converts distances nor reorders results in Java, and ASC/DESC cannot follow a distance expression. Scores from different metrics are not interchangeable similarity percentages.

Supported ranges are `v <-> ? < radius`, `v <=> ? > threshold`, and `v <#> ? > threshold`. `vector_range(v, vector, radius)` means an L2 distance below the radius. Bounds must be finite numeric literals or parameters: L2 is nonnegative, COSINE lies in [-1,1], and IP may be negative. Bind negative thresholds with a PreparedStatement parameter. Strings, null, NaN, and Infinity fail before search or mutation.

One vector range may be combined with scalar predicates using AND. Vector ranges under OR/NOT, multiple ranges, other comparison directions, or a range combined with ORDER BY are explicitly rejected. WITH radius/range_filter cannot override a WHERE range. Bound directions follow [Milvus 2.6 range search](https://milvus.io/docs/v2.6.x/range-search.md).


## Pagination and JDBC Row Limits {#pagination}

- LIMIT requires a positive integer; OFFSET requires a nonnegative integer. Fractions, numeric strings, and overflow are rejected, not truncated. Bind Byte/Short/Integer/Long/BigInteger, or an integral, in-range BigDecimal; Float/Double are not count parameters.
- Hint `overwrite_find_limit/skip` overrides SQL LIMIT/OFFSET; positive `setMaxRows` further caps returned rows. Overridden SQL parameters are still bound and validated in their original positions.
- Scalar, KNN, and range SELECT read on demand. Unlimited queries, effective limits larger than one page, and queries with OFFSET use official iterators. Bounded, single-page queries without OFFSET retain ordinary Query/Search requests. COUNT still returns one aggregate row.
- Set `Statement.setFetchSize(n)` before execution to control page size. Unset values use the SDK default; values above its maximum are capped per page, not in total. The JDBC cursor skips OFFSET page by page and then returns up to the effective LIMIT/maxRows. Large offsets still incur scanning cost. An OFFSET plus effective limit overflowing long is rejected before querying.
- SQL execution establishes the query; SDK initialization may prefetch initial data. Further pages are consumed as the ResultSet cursor advances, without traversing the whole collection before returning ResultSet. The driver retains the current page and row only; SDK-internal caches and server search restrictions remain SDK/server concerns. Omitted LIMIT adds no fixed total cap.
- Reaching the limit or EOF, closing ResultSet, reexecuting/closing Statement, and closing Connection release iterators. Multiple SQL results use standard `getMoreResults`: KEEP_CURRENT_RESULT retains independent cursors, and CLOSE_ALL_RESULTS releases retained results.
- After execute returns, `Statement.cancel()` still cancels this execution's unread results, without affecting other Statements on the connection. Query timeout starts at execution and includes subsequent reads and caller pauses. Checks run before and after page reads; an idle timeout is reported and released on the next read. Cancellation/close does not wait for an in-flight SDK page read; its result is discarded and its iterator released when the call returns. RPC interruption is not guaranteed.
- Later page failures may surface from `ResultSet.next()`. Cursors are not automatically reopened or replayed. Concurrent read/close failures retain the primary and suppressed errors. Always use try-with-resources for results that might not be fully consumed.

Iterator parameters: [QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md), [SearchIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/searchIterator.md).


## Advanced Search Parameters (WITH Clause)

This section describes vector SELECT search parameters. Scalar SELECT/COUNT only accept [query-level options](#query-options), not index-search or grouping parameters.

WITH values are bound last in SQL order. Keys are fixed names; values support strings, numbers, booleans, and `?`. JSON serialization escapes quotes, backslashes, and control characters. Quoted numbers remain strings. Write index search parameters such as `nprobe` or `ef` directly in WITH.

Regular KNN and range SELECT also accept `round_decimal` (an integer specifying distance-score decimal places, with the allowed range validated by the server), plus the ignore_growing/timezone options above. These enter dedicated SDK request fields, not index searchParams, and remain set for bounded, iterator and grouped searches. Example: `WITH (round_decimal=2, ignore_growing=false, timezone='UTC')`. Bind them with setInt, setBoolean and setString, not quoted numbers or boolean strings. This does not apply to Hybrid Search.

If provided, `metric_type` must match the SQL distance operator. Use SQL OFFSET or a hint instead of WITH offset. WITH accepts only the search parameters listed in this section. Configure consistency through the JDBC connection and output fields through SELECT.

```sql
SELECT id, score FROM table_name
WHERE age > ?
ORDER BY vector_col <=> ?
LIMIT ? OFFSET ?
WITH (metric_type='COSINE', nprobe=?);
```


## Grouped vector search {#grouping}

When multiple chunks from one document dominate the nearest matches, Milvus can select matching entities per document ID or category. This is native ANN/Hybrid grouping, not relational GROUP BY, DISTINCT or SUM/COUNT aggregation.

```sql
SELECT id, category, score FROM books ORDER BY vector_col <-> ?
WITH (group_by_field='category', group_limit=3, group_size=2, strict_group_size=true);

SELECT id, category FROM books ORDER BY vector_col <-> ? LIMIT 3 OFFSET 1
WITH (group_by_field='category', group_limit=2, group_offset=1, group_size=2, strict_group_size=true);
```

| WITH option | Meaning |
| --- | --- |
| group_by_field | Required nonempty field-name string, mapped to SDK groupByFieldName; Milvus validates field-type and index compatibility |
| group_limit | Required positive INT32: maximum number of groups, mapped to native Search topK / Hybrid limit |
| group_offset | Nonnegative BIGINT group offset, default 0; adding group_limit must not overflow |
| group_size | Optional positive INT32 target entities per group; omission preserves SDK/server defaults |
| strict_group_size | Optional boolean asking the server to try to meet group_size; the driver does not fill undersized groups |

All values accept `?` bindings; use the corresponding JDBC integer and boolean types. Grouping options belong only to the final SELECT WITH clause, not a Hybrid ANN candidate's WITH. SQL LIMIT/OFFSET, their override hints, and JDBC maxRows always count **rows**, applied after the native group window; they may truncate a group. Without SQL LIMIT, all rows in the selected group window are readable, not all groups in the collection. group_limit remains mandatory, is not inferred from SQL LIMIT, and has no magic fallback total.

The first example selects up to 3 groups with a target of 2 entities each, potentially returning 6 rows. The second skips 1 group and selects 2 groups on the server, then skips 1 returned row and exposes at most 3 rows through JDBC. Grouping, within-group selection and ordering happen on the server; the driver neither regroups, fills groups nor reorders results. There is still one flat ResultSet containing only SELECT columns; explicitly select the grouping field if needed.

Grouping uses one bounded Search/Hybrid request, not a grouping iterator. fetchSize does not split the group window into server requests, and maxRows does not reduce requested group counts. Choose group_limit and group_size for the workload and respect server search-window limits. Unsupported range-search, type, index or server-version combinations return SQLException without client-side grouping fallback.

Although Java SDK 2.6.22 declares groupByFieldName on SearchIteratorReqV2, a native call against Milvus 2.6.2 rejects this combination with `Not allowed to do groupBy when doing iteration`. Ungrouped iteration works in the same environment. A request field alone does not establish grouping-pagination support, and this result is not automatically extended to later server versions.

Hybrid accepts reranker and grouping options together in its final WITH. In this mode, mandatory group_limit replaces the requirement for an outer SQL LIMIT; each ANN candidate LIMIT still counts candidate entities:

```sql
SELECT id,category,score FROM books ORDER BY HYBRID (
    vector_col <-> ? LIMIT 20,
    other_vector <-> ? LIMIT 20
) WITH (reranker='rrf',group_by_field='category',group_limit=3,group_size=2,strict_group_size=true);
```

Grouping availability depends on the server-supported combination of vector type, index, field and reranker. See the [official grouping guide](https://blog.milvus.io/docs/v2.6.x/grouping-search.md) for parameter semantics.


## JDBC Result Metadata

Column order follows SELECT, including metadata for empty results. Int8/16/32/64 map to TINYINT/SMALLINT/INTEGER/BIGINT; Float/Double/Bool/VarChar to corresponding types. JSON is OTHER (JsonElement), FloatVector/Array is ARRAY (List/getArray), Binary/FP16/BF16/Int8Vector is VARBINARY (byte[]/getBytes), Sparse is OTHER (SPARSE_FLOAT_VECTOR, SortedMap&lt;Long,Float>). Array element type, isNullable and isAutoIncrement follow schema. Vector score is Float and is never requested as a stored field; vector SELECT * or explicit score projections include it. Scalar SELECT has no score.
