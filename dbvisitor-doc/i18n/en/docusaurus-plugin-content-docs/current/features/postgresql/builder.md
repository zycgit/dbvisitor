---
id: builder
sidebar_position: 30
title: Builder API
---

## Write Conflicts {#conflicts}

`Ignore` uses `ON CONFLICT DO NOTHING`; `Update` uses `ON CONFLICT (primary_key_columns) DO UPDATE`.

For `Update`, the mapped primary key must match a primary or unique constraint. Use handwritten SQL to target another unique key or add an update condition. See [Insert Conflicts](conflict.mdx).

## Pagination {#pagination}

`initPage(pageSize, pageNumber)` generates `LIMIT size OFFSET offset`, with page numbers starting at 0. The total is queried separately. Pagination iteration uses successive page queries, not a server cursor.

Order by a unique field to avoid ambiguous ordering between equal values. See [Pagination](pagination.mdx).
