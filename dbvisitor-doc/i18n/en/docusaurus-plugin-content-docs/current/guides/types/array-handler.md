---
id: array-handler
sidebar_position: 8
title: 8.8 Array Type Handler
description: Type handlers for array types and PostgreSQL pgvector in dbVisitor.
---

<span id="array-type-handlers" />

# 8.8 Array Type Handler

Array type handlers are located in the `net.hasor.dbvisitor.types.handler.array` package.

| Handler | Java Type | Purpose |
|---|---|---|
| `ArrayTypeHandler` | Input: java.sql.Array or object array; output: Java array | Uses getArray/setArray; frees SQL Array after reading |
| `PgArrayTypeHandler` | Input: java.sql.Array or object array; output: Object[] | Explicit PostgreSQL element type |
| `PgVectorTypeHandler` | `List<Float>` | PostgreSQL [pgvector](https://github.com/pgvector/pgvector) vector type handling |

## PgVectorTypeHandler

`PgVectorTypeHandler` handles the PostgreSQL pgvector extension's `vector` type, converting between `List<Float>` and pgvector's text format `[1.0,2.0,3.0]`.

```java title='Usage example'
public class EmbeddingEntity {
    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;
}
```

:::caution
When given Java arrays, both SQL ARRAY handlers use Object[]. Pass object arrays such as Integer[], not primitive arrays such as int[]. Milvus JDBC vector parameter support for primitive arrays follows a separate path; see [Milvus Usage](../../features/milvus/jdbc).

The general ArrayTypeHandler converts non-empty arrays of Double elements to Float arrays when reading. To preserve double precision, use JDBC getArray() with your own conversion or a dedicated handler. Callers are responsible for freeing SQL Array inputs they supply.
:::

## Array Element Type Mapping

When creating a `java.sql.Array` type, you need to specify the element type, for example:

```java
Array array = ps.getConnection().createArrayOf(typeName, ...);
```

Array element Java types map to the following JDBC types:

| Element Java type            | JDBC type               |
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
