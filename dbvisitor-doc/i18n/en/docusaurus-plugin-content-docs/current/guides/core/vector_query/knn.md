---
id: knn
sidebar_position: 3
title: KNN Ordering
description: Use orderByL2, orderByCosine, orderByIP, and orderByMetric to query the N most similar records.
---

# KNN Ordering

KNN ordering answers "find the N most similar records". dbVisitor uses `orderBy*` to generate vector-distance ordering, usually combined with `initPage` to limit the result count.

## Suitable For

- Search the N most similar articles, products, images, or knowledge chunks.
- The result count is fixed, such as Top 5 or Top 10.
- Scalar predicates narrow the candidates before vector-distance ordering.

## Not Suitable For

- The query needs all records below a distance threshold; use [Distance Range Filtering](./range).
- Complex reranking from multiple retrieval scores is required; use [JdbcTemplate](../jdbc/about).
- The data source does not support vector ordering SQL.

## KNN Query Pattern

```text title='KNN query'
All records or candidate records
        |
        | Compute distance between embedding and query vector
        v
Sort by distance ascending
        |
        | initPage(N, 0)
        v
Return the N most similar records
```

`orderBy*` is emitted in the SQL `ORDER BY` part. It does not decide the candidate set; ordinary `WHERE` predicates decide that.

## Build Query Vector

`orderBy*` converts vector arguments with the field mapping TypeHandler. After a PostgreSQL entity field is configured with `PgVectorTypeHandler`, pass `List<Float>` directly.

```java title='pgvector query argument'
List<Float> target = List.of(0.1f, 0.2f, 0.3f);
```

## Query Top N

```java title='Top-K query'
List<ProductVector> rows = lambda.query(ProductVector.class)
        .orderByL2(ProductVector::getEmbedding, target)
        .initPage(5, 0)
        .queryForList();
```

SQL shape with pgvector:

```sql
SELECT * FROM product_vector
ORDER BY embedding <-> ? ASC
LIMIT 5
```

`initPage(5, 0)` returns only the first 5 results. On PostgreSQL, omitting LIMIT sorts matching records without bounding their count. Milvus uses a search iterator to read ordinary KNN results on demand when LIMIT is omitted; this is not a fixed Top-K query. Hybrid Search requires an explicit LIMIT.

## Choose A Metric

| Goal | Method | Notes |
| --- | --- | --- |
| L2 distance | `orderByL2` | General nearest-neighbor search. |
| Cosine distance | `orderByCosine` | Common for text semantic vectors. |
| Inner product | `orderByIP` | Common for recommendation and ranking. |
| Runtime metric | `orderByMetric` | Metric comes from configuration or runtime input. |

```java title='Choose metric at runtime'
MetricType metric = MetricType.COSINE;

List<ProductVector> rows = lambda.query(ProductVector.class)
        .orderByMetric(metric, ProductVector::getEmbedding, target)
        .initPage(10, 0)
        .queryForList();
```

:::info[Inner Product]
pgvector's `<#>` operator returns the negative inner product. With `orderByIP` ascending order, records with larger inner product rank first.
:::

## Compose With Scalar Predicates

KNN query commonly narrows candidates with business fields before vector ordering.

```java title='Top-K within a category'
List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, "book")
        .orderByCosine(ProductVector::getEmbedding, target)
        .initPage(10, 0)
        .queryForList();
```

SQL shape:

```sql
SELECT * FROM product_vector
WHERE category = ?
ORDER BY embedding <=> ? ASC
LIMIT 10
```

## Common Questions

### How to bind vectors without entity mapping

`queryFreedom`, Map mode, and fields without a vector TypeHandler have no field conversion rule to reuse. Use `SqlArg` to specify a TypeHandler explicitly, or pass a vector object recognized by the database driver.

### When initPage is needed

KNN usually expects a fixed number of nearest-neighbor results. Without `initPage`, PostgreSQL has no Top-K bound; Milvus also allows unbounded ordinary KNN iteration. Explicitly set the number of neighbors required by the application.

## Further Reading

- [Vector Type Mapping](./mapping), vector fields and TypeHandler setup.
- [Combined Queries](./combined), scalar predicates with vector ordering.
- [Builder Query](../lambda/query), LambdaTemplate query basics.
