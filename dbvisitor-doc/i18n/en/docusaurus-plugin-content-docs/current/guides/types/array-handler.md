---
id: array-handler
sidebar_position: 8
title: 8.8 Array Type Handler
description: Type handlers for SQL arrays in dbVisitor.
---

<span id="array-type-handlers" />

# 8.8 Array Type Handler

Array type handlers are located in the `net.hasor.dbvisitor.types.handler.array` package.

| Handler | Java Type | Purpose |
|---|---|---|
| `ArrayTypeHandler` | Input: java.sql.Array or object array; output: Java array | Uses getArray/setArray; frees SQL Array after reading |
| `PgArrayTypeHandler` | Input: java.sql.Array or object array; output: Object[] | Explicit PostgreSQL element type |

Vector field configuration is covered in [Vector Type Handlers](./vector-handler).

:::caution
`ArrayTypeHandler` accepts object arrays and primitive arrays such as `int[]`, boxing primitive elements automatically. `PgArrayTypeHandler` accepts object arrays such as `Integer[]`, but not primitive arrays. For vector parameters, see [Milvus Parameter Binding](../../drivers/milvus/parameters.mdx#typed-values).

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
