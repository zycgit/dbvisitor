---
id: array-handler
sidebar_position: 8
title: 8.8 数组类型处理器
description: dbVisitor 处理数组类型及 PostgreSQL pgvector 的类型处理器。
---

# 数组类型处理器

数组类型处理器位于 `net.hasor.dbvisitor.types.handler.array` 包中。

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `ArrayTypeHandler` | `java.sql.Array` | 通用数组处理，使用 getArray/setArray 读写 |
| `PgArrayTypeHandler` | `java.sql.Array` | PostgreSQL 数组类型专用处理 |
| `PgVectorTypeHandler` | `List<Float>` | PostgreSQL [pgvector](https://github.com/pgvector/pgvector) 向量类型处理 |

## PgVectorTypeHandler

`PgVectorTypeHandler` 用于处理 PostgreSQL pgvector 扩展的 `vector` 类型，将 `List<Float>` 与 pgvector 的文本格式 `[1.0,2.0,3.0]` 互转。

```java title='使用示例'
public class EmbeddingEntity {
    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;
}
```

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
