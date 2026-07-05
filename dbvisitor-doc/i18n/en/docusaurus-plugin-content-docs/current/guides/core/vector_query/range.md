---
id: range
sidebar_position: 4
title: Distance Range Filtering
description: Use vectorByL2, vectorByCosine, vectorByIP, and other methods to filter records by vector distance threshold.
---

# Distance Range Filtering

Distance range filtering answers "find all records whose distance is below a threshold". dbVisitor uses `vectorBy*` to generate vector-distance predicates in the SQL `WHERE` part.

## Suitable For

- The query needs all records that satisfy a similarity threshold.
- The result count is decided by the threshold and data distribution.
- Vector predicates need to compose with ordinary field predicates.

## Not Suitable For

- The query needs a fixed number of nearest records; use [KNN Ordering](./knn).
- The threshold is unknown and a ranked candidate list is preferred.
- The data source does not support vector range filtering SQL.

## Range Filter Pattern

```text title='Range filtering'
All records or candidate records
        |
        | Compute distance between embedding and query vector
        v
Keep records where distance < threshold
        |
        v
Return all matching records
```

`vectorBy*` is itself a query predicate. It can compose with ordinary predicates such as `eq`, `likeRight`, and `gt` in the same `WHERE` clause.

## Basic Usage

```java title='L2 distance filter'
List<Float> target = Arrays.asList(0.1f, 0.2f, 0.3f);

List<ProductVector> rows = lambda.query(ProductVector.class)
        .vectorByL2(ProductVector::getEmbedding, target, 5.0)
        .queryForList();
```

SQL shape with pgvector:

```sql
SELECT * FROM product_vector
WHERE embedding <-> ? < ?
```

`vectorBy*` vector arguments are converted through the TypeHandler in entity mapping, so `List<Float>` can usually be passed directly.

## Available Filter Methods

| Goal | Method | Notes |
| --- | --- | --- |
| L2 distance | `vectorByL2` | Smaller distance is more similar. |
| Cosine distance | `vectorByCosine` | Common for text semantic vectors. |
| Inner product | `vectorByIP` | Common for recommendation and ranking. |
| Hamming distance | `vectorByHamming` | Common for binary vectors. |
| Jaccard distance | `vectorByJaccard` | Common for set similarity. |
| BM25 | `vectorByBM25` | Applies to data sources that support BM25. |

## Enable Filtering Dynamically

All `vectorBy*` methods support a first `boolean` parameter that controls whether the predicate is emitted.

```java title='Conditional vector filter'
boolean enableVectorFilter = request.hasVector();
List<Float> target = request.getVector();

List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, request.getCategory())
        .vectorByL2(enableVectorFilter, ProductVector::getEmbedding, target, 5.0)
        .queryForList();
```

When `enableVectorFilter` is `false`, the vector-distance predicate is not emitted into SQL.

## Choose A Threshold

The threshold is not a fixed value computed by dbVisitor. It is a business condition chosen from the vector model, distance metric, and data distribution. A common approach is to inspect distance distributions offline and choose a threshold that reaches the target recall.

```text title='Threshold effect'
Smaller threshold -> fewer matches, closer results
Larger threshold  -> more matches, looser results
```

Different metrics have different value meanings, so the same threshold cannot be reused blindly.

## Further Reading

- [Vector Type Mapping](./mapping), TypeHandler and query argument setup.
- [KNN Ordering](./knn), return a fixed number of nearest records.
- [Combined Queries](./combined), scalar predicates with vector range filtering.
