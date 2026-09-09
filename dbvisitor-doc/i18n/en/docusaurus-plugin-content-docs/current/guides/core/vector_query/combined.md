---
id: combined
sidebar_position: 5
title: Combined Queries
description: Compose scalar predicates, KNN ordering, and distance range filtering into business queries.
---

# Combined Queries

The examples below use PostgreSQL pgvector. Data sources such as Milvus have different metric and composition constraints; consult the corresponding driver manual.

Combined queries put ordinary field predicates and vector query in the same query. A common pattern is to narrow candidates with business fields, then use `orderBy*` for nearest-neighbor ordering or `vectorBy*` for distance range filtering.

## Suitable For

- Similarity search only within a category, tenant, status, or time range.
- Results must satisfy both business predicates and vector similarity predicates.
- The query needs Top-K or threshold results within a candidate set.

## Not Suitable For

- Complex reranking across multiple retrieval paths is required; use [JdbcTemplate](../jdbc/about).
- Business filtering and vector query are unrelated and clearer as separate queries.
- Model scoring, reranking, or fusion outside the database is required.

## Combination Pattern

```text title='Scalar predicates + vector query'
Scalar predicates
tenant_id / category / status / create_time
        |
        v
Candidate records
        |
        +-- orderBy*  -> Top-K within candidates
        |
        +-- vectorBy* -> Distance threshold within candidates
```

Ordinary field predicates always belong to `WHERE`. `orderBy*` belongs to `ORDER BY`, and `vectorBy*` also belongs to `WHERE`.

## Scalar Predicates + KNN Ordering

```java title='Top-K within a category'
PGobject target = new PGobject();
target.setType("vector");
target.setValue("[0.1,0.2,0.3]");

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

This form is for "find the N most similar records in a specified scope".

## Scalar Predicates + Distance Range Filtering

```java title='Range filtering within a category'
List<Float> target = Arrays.asList(0.1f, 0.2f, 0.3f);

List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, "book")
        .vectorByCosine(ProductVector::getEmbedding, target, 0.2)
        .queryForList();
```

SQL shape:

```sql
SELECT * FROM product_vector
WHERE category = ?
  AND embedding <=> ? < ?
```

This form is for "find all sufficiently similar records in a specified scope".

## Dynamic Condition Composition

Builder API condition methods can use a boolean parameter to control whether the condition is emitted into SQL. The example assumes `PgVectorTypeHandler` is configured on the vector field and `request.getVector()` returns `List<Float>`; range conditions bind parameters through the field's TypeHandler.

```java title='Dynamic composition'
boolean hasCategory = request.getCategory() != null;
boolean hasVector = request.getVector() != null;

List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(hasCategory, ProductVector::getCategory, request.getCategory())
        .vectorByL2(hasVector, ProductVector::getEmbedding, request.getVector(), 5.0)
        .queryForList();
```

Fixed Top-K query can also append vector ordering when it is required:

```java title='Dynamic KNN'
var query = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, "book");
if (target != null) {
    query.orderByL2(ProductVector::getEmbedding, target);
} else {
    query.orderByAsc(ProductVector::getId);
}
List<ProductVector> rows = query.initPage(10, 0).queryForList();
```

## Choose A Combination

| Goal | Recommended Form |
| --- | --- |
| Find the 10 most similar records within a category | `eq(category)` + `orderBy*` + `initPage(10, 0)` |
| Find all records under a threshold within a category | `eq(category)` + `vectorBy*` |
| Optional query conditions | Use condition methods with a `boolean` parameter |
| Complex reranking | Use JdbcTemplate and handwritten SQL |

## Further Reading

- [KNN Ordering](./knn), `orderBy*` usage and argument requirements.
- [Distance Range Filtering](./range), `vectorBy*` usage and threshold meaning.
- [Where Builder](../lambda/where_builder), scalar predicates and dynamic conditions.
