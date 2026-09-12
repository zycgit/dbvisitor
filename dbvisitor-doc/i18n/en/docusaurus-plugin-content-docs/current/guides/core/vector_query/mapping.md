---
id: mapping
sidebar_position: 2
title: Vector Type Mapping
description: Configure vector fields, entity types, and TypeHandler so vector values can be written, read, and queried.
---

# Vector Type Mapping

Vector Query requires the database field, Java field, and TypeHandler to match. The field type controls how the database stores vectors, the Java type controls how application code passes vectors, and the TypeHandler converts between them.

## Suitable For

- PostgreSQL pgvector, Milvus, ElasticSearch, or another data source with vector capability is used.
- The entity should represent vectors as `List<Float>`.
- The field will be queried later through `orderBy*` or `vectorBy*`.

## Not Suitable For

- The vector field is only used in handwritten SQL and does not need object mapping.
- The database driver requires a dedicated vector object and application code already uses that object directly.
- Vector dimensions, indexes, or embedding generation are not decided yet.

## Mapping Relationship

```text title='Vector field mapping'
Database field
embedding vector(3)
        |
        | TypeHandler
        v
Java field
List<Float> embedding
```

dbVisitor does not generate embeddings and does not replace database vector indexes. It maps vector fields into objects and generates vector query SQL through the builder API.

## Create Table

The example below uses PostgreSQL + pgvector.

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_vector (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100),
    category  VARCHAR(50),
    embedding vector(3)
);
```

The vector field dimension must match the actual embedding dimension. Dimension mismatches are usually reported by the database or driver during write or query execution.

## Entity Mapping

```java title='ProductVector.java'
@Table("product_vector")
public class ProductVector {
    @Column(primary = true, keyType = KeyType.Auto)
    private Integer id;

    private String name;

    private String category;

    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;

    // getter / setter ...
}
```

`PgVectorTypeHandler` converts between `List<Float>` and the pgvector text format. Other databases require the corresponding vector TypeHandler or a driver-supported argument type.

## Write Vectors

After mapping is configured, inserting vector fields is the same as inserting regular entity fields.

```java title='Insert vector'
ProductVector row = new ProductVector();
row.setName("sample");
row.setCategory("book");
row.setEmbedding(Arrays.asList(0.1f, 0.2f, 0.3f));

lambda.insert(ProductVector.class)
      .applyEntity(row)
      .executeSumResult();
```

## Update Vectors

```java title='Update vector'
List<Float> newVector = Arrays.asList(0.9f, 0.8f, 0.7f);

lambda.update(ProductVector.class)
      .eq(ProductVector::getId, row.getId())
      .updateTo(ProductVector::getEmbedding, newVector)
      .doUpdate();
```

## Read Vectors

```java title='Read vector'
ProductVector loaded = lambda.query(ProductVector.class)
        .eq(ProductVector::getId, row.getId())
        .queryForObject();

List<Float> vector = loaded.getEmbedding();
```

## Argument Types

Both `vectorBy*` and `orderBy*` convert vector arguments through the entity field mapping TypeHandler. After a PostgreSQL field is configured with `PgVectorTypeHandler`, pass `List<Float>` directly.

```java title='pgvector query argument'
List<Float> target = List.of(0.1f, 0.2f, 0.3f);
```

Without entity mapping or a field TypeHandler, use `SqlArg` to specify the handler explicitly, or pass the database driver's dedicated vector type.

## Further Reading

- [KNN Ordering](./knn), use `orderBy*` for Top-K query.
- [Distance Range Filtering](./range), use `vectorBy*` for threshold filtering.
- [Object Mapping](../mapping/about), field mapping and TypeHandler basics.
