---
id: builder
slug: /features/milvus/builder
sidebar_position: 30
title: Builder API
---

## Data Writes {#writes}

Inserts use Insert; updates use native Partial Update rather than reading and replacing whole entities. Multi-page operations do not commit or roll back as one unit.

Deleting known primary keys may count keys that do not exist. Do not use that count alone to establish existence. See [Data Writes](write.mdx#delete-count) for deletion after scalar or vector selection.

For chunked deletion, repeatedly fetch the first page of remaining rows and delete those keys. Scalar ordering is unnecessary; do not increment the page number while deleting.

## Write Conflicts {#conflicts}

| Strategy | Milvus behavior |
| --- | --- |
| Default / `Into` | Ordinary Insert; duplicate primary keys do not reliably cause an error |
| `Ignore` | Unsupported: there is no atomic insert-if-absent operation |
| `Update` | Partial Upsert: updates specified fields of existing entities or inserts new entities |

See [Data Writes](write.mdx#insert-conflict). Do not use an ordinary Insert exception as a duplicate-key check.

## Query {#queries}

Field selection, entity and Map results, scalar results, and `COUNT(*)` are supported. Computed projections, `DISTINCT`, and general SQL aggregates are not supported.

`queryForObject()` returns the first matching row. Without scalar ordering, it does not mean “the row with the smallest primary key.” Use a primary-key condition to identify a specific row. See [Query Operations](query.mdx#query-apis).

## Map Query Mode {#map-mode}

Mapped Map mode uses Java property names; free Map mode uses collection field names. For a property `wordCount` mapped to `word_count`, use the respective name in each mode.

See [Query Operations](query.mdx#entity-mapping) for entity and field mapping. Map mode does not add scalar ordering, computed columns, or grouping.

## Pagination {#pagination}

Ordinary queries use `LIMIT / OFFSET` and a separate count query. The driver reads data on demand. Pagination iteration uses page queries; `fetchSize` controls driver fetch batches, not page numbers or total rows.

Scalar fields cannot be used for ordering. Vector queries order by distance; see [Pagination](pagination.mdx).

## Where Builder {#predicates}

Comparisons, ranges, IN, NULL, and AND/OR conditions are available. Remove nulls from collections and use `isNull` or `isNotNull` to express null predicates explicitly.

Milvus 2.6.2 does not currently support these parameterized predicates:

- LIKE patterns, including `like`, `likeLeft`, `likeRight`, and their negated forms.
- NOT around comparisons with integer parameters, repeated NOT, and some negated half-open ranges. Use `ne` to exclude one value, or combine the two outside comparisons with OR for a range.

The driver does not bypass parameter binding by concatenating strings.

An ordinary predicate example:

```java
lambda.query(BookVector.class)
        .ge(BookVector::getWordCount, 1000)
        .isNotNull(BookVector::getTitle)
        .queryForList();
```

## Predicate Values {#parameter-values}

Comparisons, collections, ranges, and `apply` pass values through SDK parameter binding. Do not concatenate or escape quotes in string values yourself. LIKE pattern parameters are currently unsupported; see [Where Builder](#predicates).

Do not paste SQL fragments from other databases into `apply`, such as `1=1` for dynamic conditions. Use the API's boolean flag for optional conditions:

```java
lambda.query(BookVector.class)
        .eq(title != null, BookVector::getTitle, title)
        .queryForList();
```

Expressions passed to `apply` must follow [Milvus expression syntax](../basics/operators.md).

## Group By {#grouping}

The current dialect does not support builder `groupBy` or grouped aggregation. Use `queryForCount()` for ordinary counts. Grouping search in vector retrieval is a separate feature, not SQL grouped aggregation.

## Order By {#ordering}

The current dialect does not support scalar `asc`, `desc`, or `orderBy`, including their null-placement strategies. Use `orderByL2`, `orderByCosine`, or `orderByIP` for vector-distance ordering. See [Vector Operations](vectors.mdx).
