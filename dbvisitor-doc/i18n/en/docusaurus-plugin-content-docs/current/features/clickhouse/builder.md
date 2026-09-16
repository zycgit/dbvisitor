---
id: builder
sidebar_position: 30
title: Builder API
---

## Data Writes {#writes}

The builder supports inserts, updates, and deletes, but the JDBC affected-row count need not equal the actual number of matched rows. Do not use `doUpdate() == 0` or `doDelete() == 0` to decide that a row does not exist.

To verify the final state, query after the write completes. See [Data Writes](write.mdx#affected-rows) for mutation waiting behavior.

## Write Conflicts {#conflicts}

The ClickHouse dialect does not support the `Ignore` or `Update` insert-conflict strategies. A primary key on a regular MergeTree table controls ordering; it does not reject duplicate keys.

Changing the strategy is not a deduplication solution. See [Insert Conflicts](conflict.mdx) for table-engine and deduplication choices.

## Pagination {#pagination}

The builder uses `LIMIT offset, size`. The total is queried separately. Pagination iteration issues successive page queries; order by a unique field for stable results. See [Pagination](pagination.mdx).

## Where Builder {#predicates}

Collection predicates are supported, but do not rely on `NOT IN (..., NULL)` to exclude both listed values and null fields. Remove nulls from the input collection and explicitly exclude null fields:

```java
lambda.query(UserInfo.class)
        .notIn(UserInfo::getAge, List.of(20))
        .isNotNull(UserInfo::getAge)
        .queryForList();
```

This selects rows whose age is non-null and not 20.

## Predicate Values {#parameter-values}

`String` has no character-length constraint equivalent to `VARCHAR(100)`. Binding more than 100 characters therefore does not cause a length error, and the builder does not truncate the value.

Validate the length before calling the write API if your business field has a limit.
