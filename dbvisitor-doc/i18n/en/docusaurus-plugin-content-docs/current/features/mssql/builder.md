---
id: builder
sidebar_position: 30
title: Builder API
---

## Write Conflicts {#conflicts}

`Ignore` and `Update` use `MERGE`, matching the mapped primary-key columns. The former inserts only new rows; the latter also updates existing rows. See [Insert Conflicts](conflict.mdx).

## Pagination {#pagination}

The builder numbers rows with `ROW_NUMBER() OVER(ORDER BY ...)` and selects the requested page. The total is queried separately. Without an explicit order, a fallback expression is used and paging order is not guaranteed. Specify a stable order.

See [Pagination](pagination.mdx).

## Order By {#ordering}

Ascending, descending, multi-column, and null ordering are supported. Each call appends an ordering term; it does not replace the previous one:

```java
// Descending age, then ascending ID for equal ages.
lambda.query(UserInfo.class)
        .desc("age").asc("id")
        .queryForList();
```

Do not use `asc("age").desc("age")`: SQL Server rejects repeated ordering columns. To change direction, change the original call and keep only one ordering term for that field.
