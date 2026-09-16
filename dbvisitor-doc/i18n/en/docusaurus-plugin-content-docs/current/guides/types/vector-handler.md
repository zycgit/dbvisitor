---
id: vector-handler
sidebar_position: 9
title: 8.9 Vector Type Handlers
description: Configure Java types and type handlers for vector fields.
---

Vector type handlers read and write vector fields. They are in the `net.hasor.dbvisitor.types.handler.vector` package.

| Database field type | Java property type | Type handler |
| --- | --- | --- |
| PostgreSQL `vector(n)` | `List<Float>` | `PgVectorTypeHandler` |
| ClickHouse `Array(Float32)` | `List<Float>` | `ChVectorTypeHandler` |

## Configure Field Mapping

Specify the handler on the vector property. Do not register one vector handler globally for all `List` properties.

```java
import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.vector.PgVectorTypeHandler;

@Table("documents")
public class Document {
    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;

    public List<Float> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
}
```

For a ClickHouse `Array(Float32)` field, keep the same property type and use `ChVectorTypeHandler` instead.

Entity reads and writes use the field's handler. The builder API's `vectorBy*` and `orderBy*` methods also use it to bind query vectors. See [Vector Queries](../core/vector_query/mapping) for API usage.

## Handler Behavior

- `PgVectorTypeHandler`: binds parameters in pgvector text format and reads them as `List<Float>`; accepts `null`.
- `ChVectorTypeHandler`: reads and writes `Array(Float32)` through JDBC arrays and returns `List<Float>`; rejects `null` vectors.

:::info
The Milvus JDBC driver handles vector parameters directly; neither handler above is required. See [Milvus Type Support](../../features/milvus/types) for the Java types accepted by each vector field type.
:::

For database-specific distance semantics and search behavior, see [PostgreSQL Vector Operations](../../features/postgresql/vectors.mdx).
