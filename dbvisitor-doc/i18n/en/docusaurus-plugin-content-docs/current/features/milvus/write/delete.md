---
id: delete
sidebar_position: 4
title: DELETE
---

:::info[Note]
SDK methods: `query`, `queryIterator`, `search`, `searchIteratorV2`, `delete`.
:::

## Syntax

```text
DELETE FROM [TABLE] collection_name [PARTITION partition_name]
    [WHERE condition]
    [ORDER BY vector_field distance_operator query_vector]
    [LIMIT row_count];
```

## Delete Data

> **Note**: DELETE without WHERE deletes all records in the collection, or only in the specified PARTITION. Confirm the scope before executing it. Scalar DELETE without LIMIT directly submits a server filter. With LIMIT it selects primary keys page by page before deletion. Vector DELETE searches primary keys page by page before deleting, without whole-SQL atomicity or rollback.

For scalar DELETE without WHERE, the driver builds a `primary_key IS NOT NULL` filter using the collection's actual primary-key field. Milvus primary keys cannot be NULL, so this covers all records, including negative, zero, and string keys; the field does not need to be named `id`. This conversion applies only when WHERE is omitted and never rewrites a supplied condition. It does not substitute `1=1`, which the native Milvus 2.6.2 execution path does not support as an always-true filter. Existing LIMIT, partition, and JDBC fetchSize semantics are preserved; fetchSize controls the read page size, not the total deletion limit.

### 1. Basic Delete (Scalar Filtering)

Use the scalar WHERE syntax documented here.

```sql
-- Delete all records while preserving the collection, schema, and indexes
DELETE FROM table_name;

-- Delete at most 1000 records; without ordering, the selected records are unspecified
DELETE FROM table_name LIMIT 1000;

-- Delete all records only from the specified partition
DELETE FROM table_name PARTITION partition_name;

-- Delete by Primary Key
DELETE FROM table_name WHERE id = 1;
DELETE FROM table_name WHERE id IN [1, 2, 3];

-- Delete by scalar condition (direct server-side filter deletion without LIMIT)
DELETE FROM table_name WHERE age > 18 AND status = 'inactive';

-- Delete from specific partition
DELETE FROM table_name PARTITION partition_name WHERE age > 10;
```

### 2. KNN Delete (Nearest Neighbor Delete)

Use ORDER BY for distance ordering. LIMIT caps the selected count; without LIMIT, the driver iterates the complete matching search result.
Underlying mechanism: select primary keys by distance page by page -> delete each page.

```sql
-- Delete 100 records closest to [0.1, 0.2]
DELETE FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 100;

-- Combined with scalar filtering: Delete 10 most similar records where category='book'
DELETE FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;
```

### 3. Range Delete

Delete all records falling within a specified distance (radius) of the target vector. Can use `vector_range` function or `<->` comparison expression.
Underlying mechanism: iterate primary keys selected by range search -> delete each page.

```sql
-- Using vector_range function (Recommended)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- Using comparison expression: Delete all records with distance < 0.5 from [0.1, 0.2]
DELETE FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.5;

-- Combined with LIMIT for protection (Delete at most 1000 matching records)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5) LIMIT 1000;
```

Paging, cancellation and retries: [UPDATE](update.md).

## Delete Counts {#delete-count}

DELETE without LIMIT and with a simple primary-key condition is handled directly by the server. On Milvus 2.6.2, repeating `pk = ?` deletion can return 1 even when the entity is already absent: the count represents submitted deletion keys, not existing rows found by a preliminary query. The driver does not query first or rewrite this into a relational affected-row count. Do not use the DELETE return value to infer whether an entity existed beforehand. See the [server deletion implementation](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/proxy/task_delete.go).
