---
id: array-handler
sidebar_position: 8
title: 8.8 数组类型处理器
description: dbVisitor 处理数组类型及 PostgreSQL pgvector 的类型处理器。
---

<span id="数组类型处理器" />

# 8.8 数组类型处理器

数组类型处理器位于 `net.hasor.dbvisitor.types.handler.array` 包中。

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `ArrayTypeHandler` | 输入 `java.sql.Array` 或对象数组，输出 Java 数组 | 通过 getArray/setArray 读写，读取后释放 SQL Array |
| `PgArrayTypeHandler` | 输入 `java.sql.Array` 或对象数组，输出 `Object[]` | 显式指定 PostgreSQL 元素类型 |
| `PgVectorTypeHandler` | `List<Float>` | PostgreSQL [pgvector](https://github.com/pgvector/pgvector) 向量类型处理 |

## PgVectorTypeHandler

`PgVectorTypeHandler` 用于处理 PostgreSQL pgvector 扩展的 `vector` 类型，将 `List<Float>` 与 pgvector 的文本格式 `[1.0,2.0,3.0]` 互转。

```java title='使用示例'
public class EmbeddingEntity {
    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;
}
```

:::caution
这两个 SQL ARRAY 处理器接收 Java 数组时使用 `Object[]`，应传 `Integer[]` 等对象数组，不能直接传 `int[]` 等基础类型数组。Milvus JDBC 驱动对向量参数的基础类型数组支持是另一条处理路径，见 [Milvus 用法](../../features/milvus/jdbc)。

通用 ArrayTypeHandler 读取非空的 Double 元素数组时会转成 Float 数组；需要保留双精度时，请直接使用 JDBC `getArray()` 并自行转换，或配置专用处理器。调用者提供的 SQL Array 由调用者负责 `free()`。
:::

## 数组元素类型映射


在创建 java.sql.Array 类型时需要指定元素类型，例如：

```java
Array array = ps.getConnection().createArrayOf(typeName, ...);
```

数组中的元素 Java 类型所对应的 JDBC 类型为下表所示：

| 数组元素类型                   | 对应的 JDBC 类型        |
|--------------------------|--------------------|
| boolean.class            | JDBCType.BOOLEAN   |
| Boolean.class            | JDBCType.BOOLEAN   |
| byte.class               | JDBCType.TINYINT   |
| Byte.class               | JDBCType.TINYINT   |
| short.class              | JDBCType.SMALLINT  |
| Short.class              | JDBCType.SMALLINT  |
| int.class                | JDBCType.INTEGER   |
| Integer.class            | JDBCType.INTEGER   |
| long.class               | JDBCType.BIGINT    |
| Long.class               | JDBCType.BIGINT    |
| float.class              | JDBCType.FLOAT     |
| Float.class              | JDBCType.FLOAT     |
| double.class             | JDBCType.DOUBLE    |
| Double.class             | JDBCType.DOUBLE    |
| Calendar.class           | JDBCType.CHAR      |
| char.class               | JDBCType.CHAR      |
| java.util.Date.class     | JDBCType.TIMESTAMP |
| java.sql.Date.class      | JDBCType.TIMESTAMP |
| java.sql.Timestamp.class | JDBCType.TIMESTAMP |
| java.sql.Time.class      | JDBCType.TIMESTAMP |
| Instant.class            | JDBCType.TIMESTAMP |
| LocalDateTime.class      | JDBCType.TIMESTAMP |
| LocalDate.class          | JDBCType.TIMESTAMP |
| LocalTime.class          | JDBCType.TIMESTAMP |
| ZonedDateTime.class      | JDBCType.TIMESTAMP |
| JapaneseDate.class       | JDBCType.TIMESTAMP |
| YearMonth.class          | JDBCType.TIMESTAMP |
| Year.class               | JDBCType.TIMESTAMP |
| Month.class              | JDBCType.TIMESTAMP |
| OffsetDateTime.class     | JDBCType.TIMESTAMP |
| OffsetTime.class         | JDBCType.TIMESTAMP |
| String.class             | JDBCType.VARCHAR   |
| BigInteger.class         | JDBCType.BIGINT    |
| BigDecimal.class         | JDBCType.NUMERIC   |
| Byte[].class             | JDBCType.VARBINARY |
| byte[].class             | JDBCType.VARBINARY |
| URL.class                | JDBCType.DATALINK  |
| URI.class                | JDBCType.DATALINK  |
