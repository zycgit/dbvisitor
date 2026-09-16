---
id: builder
sidebar_position: 30
title: Builder API
---

## Write Conflicts {#conflicts}

Both `Ignore` and `Update` generate `MERGE`, matching the primary-key columns in the entity mapping. The former skips existing rows; the latter updates existing rows and inserts new ones.

The mapping must match the table's constraints. A conflict on another unique key does not automatically become a primary-key match. See [Insert Conflicts](conflict.mdx).

## Pagination {#pagination}

The builder assigns row numbers with `ROWNUMBER() OVER()` and selects the requested range. The total is queried separately.

:::caution Stable paging
The current rewrite does not put the ordering inside `OVER(...)`. For strictly ordered pages, write SQL with an explicit `ROW_NUMBER() OVER(ORDER BY ...)`. See [Pagination](pagination.mdx#dbvisitor-usage).
:::
