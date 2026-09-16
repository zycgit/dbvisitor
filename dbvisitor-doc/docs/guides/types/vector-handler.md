---
id: vector-handler
sidebar_position: 9
title: 8.9 向量类型处理器
description: 为向量字段配置 Java 类型与类型处理器。
---

向量类型处理器用于读写向量字段，位于 `net.hasor.dbvisitor.types.handler.vector` 包中。

| 数据库字段类型 | Java 属性类型 | 类型处理器 |
| --- | --- | --- |
| PostgreSQL `vector(n)` | `List<Float>` | `PgVectorTypeHandler` |
| ClickHouse `Array(Float32)` | `List<Float>` | `ChVectorTypeHandler` |

## 配置字段映射

在向量属性上指定处理器，不要为所有 `List` 属性全局注册同一个向量处理器。

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

ClickHouse 的 `Array(Float32)` 字段使用相同的属性类型，将处理器换成 `ChVectorTypeHandler` 即可。

实体读写使用字段上的处理器。构造器 API 的 `vectorBy*`、`orderBy*` 也使用向量字段的处理器绑定查询向量，具体用法见[向量查询](../core/vector_query/mapping)。

## 处理器行为

- `PgVectorTypeHandler`：以 pgvector 文本格式绑定参数，读取为 `List<Float>`；支持 `null`。
- `ChVectorTypeHandler`：通过 JDBC 数组读写 `Array(Float32)`，读取为 `List<Float>`；不接受 `null` 向量。

:::info
Milvus 的向量参数由 JDBC 驱动直接处理，不需要配置上述处理器。不同向量字段接受的 Java 类型见 [Milvus 类型支持](../../features/milvus/types)。
:::

数据库特有的距离含义及检索行为见 [PostgreSQL 向量操作](../../features/postgresql/vectors.mdx)。
