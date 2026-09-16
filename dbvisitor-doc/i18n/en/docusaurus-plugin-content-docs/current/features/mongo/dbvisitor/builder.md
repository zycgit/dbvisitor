---
id: builder
slug: /features/mongo/builder
sidebar_position: 30
title: Builder API
---

## Write Conflicts {#conflicts}

`Ignore` and `Update` use MongoDB upserts, locating documents by the mapped primary-key field. Adding `@Column(primary = true)` does not create a unique index. Create one on that field if duplicates must be prevented.

See [Data Writes](write.mdx#insert-conflict) for usage and affected counts.

## Query {#queries}

Ordinary builder queries generate `find`. Computed projections and aggregates use MongoDB expressions, not SQL functions. For example, grouped counts use `$sum` below, not `COUNT(*)`.

## Pagination {#pagination}

Ordinary queries use `skip` and `limit`, with a separate count query. Pagination iteration fetches successive pages; order by a unique field for stable paging. See [Pagination](pagination.mdx) for custom aggregation pipelines.

## Where Builder {#predicates}

Do not treat null inside `notIn` as SQL's three-valued logic. To exclude both a specified age and null fields, remove nulls from the input collection and use:

```java
lambda.query(UserInfo.class)
        .notIn(UserInfo::getAge, List.of(20))
        .isNotNull(UserInfo::getAge)
        .queryForList();
```

## Group By {#grouping}

`groupBy` generates a `$group` pipeline stage. Supply MongoDB aggregate expressions through `applySelect`:

```java
List<Map<String, Object>> rows = lambda.query(UserInfo.class)
        .applySelect("{cnt: {$sum: 1}}")
        .groupBy("age").orderBy("age")
        .queryForMapList();
```

The result contains `age` and `cnt`. See [Query Operations](query.mdx#aggregation) for full pipelines and projections.

## Order By {#ordering}

Ascending, descending, and multi-field ordering are supported. The MongoDB dialect does not yet support `OrderNullsStrategy.FIRST` or `LAST`. Default ordering follows MongoDB's null ordering and is not an explicit null-placement strategy.

For explicit placement, use an aggregation pipeline that adds a sort key and then applies `$sort`.
