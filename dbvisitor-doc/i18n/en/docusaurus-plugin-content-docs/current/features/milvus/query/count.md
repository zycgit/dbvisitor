---
id: count
sidebar_position: 2
title: COUNT / SELECT COUNT(*)
---

:::info[Note]
SDK methods: `query`.
:::

## Syntax

```text
{ COUNT | SELECT COUNT(*) } FROM collection_name
    [PARTITION partition_name] [WHERE scalar_condition]
    [WITH (option = value [, ...])];
```

## Count Statistics

Use `COUNT FROM ...` or `SELECT COUNT(*) FROM ...` to count entities in a collection or partition. Both use the same native Milvus count request, without scanning results on the client.
The result is one row with `COUNT` (BIGINT), read using `rs.getLong("COUNT")`; an empty match returns 0. Append WITH for [query-level options](select.md#query-options); ignore_growing=true also excludes growing segments from counts. COUNT(field), mixed projections, GROUP BY and vector-range filters are unsupported.

```sql
-- Query total count of full table
count from table_name;
SELECT COUNT(*) FROM table_name;

-- Query total count of specific partition
COUNT FROM table_name PARTITION partition_name;

-- Count with conditional filtering (supports scalar filtering)
count from table_name where age > 18;
```
