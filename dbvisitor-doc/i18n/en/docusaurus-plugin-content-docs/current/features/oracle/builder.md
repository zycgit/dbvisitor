---
id: builder
sidebar_position: 30
title: Builder API
---

## Write Conflicts {#conflicts}

`Ignore` and `Update` use `MERGE`, matching the mapped primary-key columns. The former inserts only new rows; the latter also updates existing rows.

The mapping must match the table's constraints. See [Insert Conflicts](conflict.mdx).

## Pagination {#pagination}

The builder uses nested queries and `ROWNUM` to select the current page. The total is queried separately. Pagination iteration issues successive page queries rather than loading all rows at once.

See [Pagination](pagination.mdx) for usage and generated SQL.

## Where Builder {#predicates}

Comparisons, ranges, collections, LIKE, NULL, and grouped conditions are supported. The following cases require Oracle-specific usage.

### Querying empty strings

Oracle stores an empty character string as NULL. Use `isNull` to find these rows:

```java
lambda.query(UserInfo.class)
        .isNull(UserInfo::getName)
        .queryForList();
```

`eq(UserInfo::getName, "")` remains an equality predicate. It does not become `IS NULL` and does not find these rows.

### Querying many IDs

The builder does not split `IN` lists automatically. If the list exceeds the limit of your Oracle version, query it in application-managed batches:

```java
List<UserInfo> rows = new ArrayList<>();
for (int start = 0; start < ids.size(); start += 500) {
    List<Integer> batch = ids.subList(start, Math.min(start + 500, ids.size()));
    rows.addAll(lambda.query(UserInfo.class)
            .in(UserInfo::getId, batch)
            .queryForList());
}
```

Here, 500 is the application's batch size, not automatic driver pagination. Deduplicate the input IDs to avoid collecting the same row in multiple batches.
