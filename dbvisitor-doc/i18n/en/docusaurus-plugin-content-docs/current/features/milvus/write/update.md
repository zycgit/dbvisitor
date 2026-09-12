---
id: update
slug: /features/milvus/sql/mutations
sidebar_position: 3
title: UPDATE
---

:::info[Note]
SDK methods: `query`, `queryIterator`, `search`, `searchIteratorV2`, `upsert`.
:::

## Syntax

```text
UPDATE collection_name [PARTITION partition_name]
    SET field_name = value [, ...]
    [WHERE condition]
    [ORDER BY vector_field distance_operator query_vector]
    [LIMIT row_count];
```

## Update Data

> **Note**: UPDATE uses Milvus 2.6.2+ native partial update. The driver selects primary keys page by page and writes only the primary key and SET fields, without collecting the entire selection or rewriting untouched fields. Pages do not form a transaction and cannot be rolled back together. Concurrent writes to the same SET field follow Milvus write semantics.

### 1. Basic Update (Scalar Filtering)

Use the scalar WHERE syntax in this manual. Omitting WHERE and vector conditions updates the entire collection; specify conditions or LIMIT as needed. Primary-key updates are prohibited. SET assigns values/constants/parameters, not per-row expressions such as `age = age + 1`.

```sql
-- Update by Primary Key
UPDATE table_name SET age = 20 WHERE id = 1;

-- Select primary keys by scalar condition, then submit partial updates
UPDATE table_name SET status = 'active' WHERE age > 18;
```

### 2. KNN Update (Nearest Neighbor Update)

Update the K records closest to the target vector.
Underlying mechanism: iterate selected primary keys by distance -> partial upsert each page.

```sql
-- Update status to 1 for the 1 record closest to [0.1, 0.2]
UPDATE table_name SET status = 1 ORDER BY vector_col <-> [0.1, 0.2] LIMIT 1;
```

### 3. Range Update

Update all records falling within a specified distance (radius) of the target vector.
Can use `vector_range` function or `<->` comparison expression.

```sql
-- Using vector_range function (Recommended)
-- Syntax: vector_range(vector_field, target_vector, radius)
UPDATE table_name SET tag = 'A' WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- Using comparison expression
UPDATE table_name SET tag = 'A' WHERE vector_col <-> [0.1, 0.2] < 0.5;
```


## Paged Writes, Cancellation, and Retries

- UPDATE/DELETE take LIMIT from SQL itself. SELECT overwrite_find_limit/skip hints and JDBC maxRows do not cap DML writes. Explicit vector UPDATE/DELETE LIMIT currently accepts at most Integer.MAX_VALUE; scalar LIMIT uses long. Omitted LIMIT still imposes no driver total cap.
- UPDATE, scalar DELETE with LIMIT, and KNN/range DELETE use iterators. Omitted LIMIT has no arbitrary total cap. Scalar DELETE without LIMIT uses server-side filter deletion directly.
- `Statement.setFetchSize(n)` sets page size, not a total limit. Unset fetchSize uses the SDK default; values above the SDK page maximum are capped to that page maximum.
- After `Statement.cancel()` or query timeout takes effect, the driver stops initiating subsequent reads, writes, and retries, and closes the iterator. In-flight RPCs and completed pages are not undone.
- The JDBC connection parameter `maxRetry` limits retries after the initial write attempt. It is shared by paged partial upsert and DELETE writes, not every SDK operation. Exhausted retries may leave partial success; retries do not promise exactly-once execution.
- Only recognized transient errors are retried, such as gRPC UNAVAILABLE, RESOURCE_EXHAUSTED, ABORTED, DEADLINE_EXCEEDED, SDK rate limiting/temporary unavailability, and JDBC transient connection errors. Parameter, permission, missing-collection, and unknown errors fail immediately. Backoff starts at 100ms, doubles up to 1000ms per wait, and checks cancellation and query timeout while waiting.
- Reading the next page is not automatically retried: an uncertain iterator position could otherwise skip or duplicate data. Ordinary INSERT/UPSERT and Import creation do not automatically use this retry policy either.
- Paged DML errors include `phase=read/write/close`, `iterator`, `page`, `confirmedPages`, `confirmedRows`, and `currentPageRows`, preserving SQLState, error code, and cause. Iterator-close failures are suppressed when a primary error already exists. Confirmed progress is not a complete account of server state: part of a failing page may already have taken effect.
- Update counts, including totals across pages, remain `long` and are available through `executeLargeUpdate()` / `getLargeUpdateCount()`. Above `Integer.MAX_VALUE`, ordinary `executeUpdate()` / `getUpdateCount()` follow the common driver's policy of returning `Statement.SUCCESS_NO_INFO`, without truncating counts or rejecting a completed paged operation for integer overflow.

<span id="dml" />
