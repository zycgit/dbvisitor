---
id: java-jdbc
sidebar_position: 1
title: 8.1 Java/JDBC Type Mapping
description: Java-to-JDBC type mapping used by dbVisitor ORM.
---

<span id="javajdbc-type-mapping" />

# 8.1 Java/JDBC Type Mapping

When no JDBC type is explicitly specified, dbVisitor automatically selects the JDBC type corresponding to the Java type from the mapping table.

## Default Mapping Table

| Java Type | JDBC Type |
|---|---|
| `java.lang.Boolean`, `boolean` | `Types.BIT` |
| `java.lang.Byte`, `byte` | `Types.TINYINT` |
| `java.lang.Short`, `short`, `java.time.Year`, `java.time.Month` | `Types.SMALLINT` |
| `java.lang.Integer`, `int` | `Types.INTEGER` |
| `java.lang.Long`, `long`, `java.math.BigInteger` | `Types.BIGINT` |
| `java.lang.Float`, `float` | `Types.FLOAT` |
| `java.lang.Double`, `double` | `Types.DOUBLE` |
| `java.lang.Character`, `char` | `Types.CHAR` |
| `java.sql.Date`, `java.time.LocalDate`, `oracle.sql.DATE` | `Types.DATE` |
| `java.sql.Time`, `java.time.LocalTime` | `Types.TIME` |
| `java.util.Date`, `java.sql.Timestamp`, `java.time.Instant`, `java.time.LocalDateTime`, `java.time.chrono.JapaneseDate`, `oracle.sql.TIMESTAMP` | `Types.TIMESTAMP` |
| `java.time.OffsetDateTime`, `java.time.ZonedDateTime`, `oracle.sql.TIMESTAMPTZ`, `oracle.sql.TIMESTAMPLTZ` | `Types.TIMESTAMP_WITH_TIMEZONE` |
| `java.time.OffsetTime` | `Types.TIME_WITH_TIMEZONE` |
| `java.lang.String`, `java.time.YearMonth` | `Types.VARCHAR` |
| `java.math.BigDecimal` | `Types.DECIMAL` |
| `java.io.Reader`, `oracle.jdbc.OracleClob` | `Types.CLOB` |
| `java.io.InputStream`, `oracle.jdbc.OracleBlob` | `Types.BLOB` |
| `oracle.jdbc.OracleNClob` | `Types.NCLOB` |
| `java.lang.Byte[]`, `byte[]` | `Types.VARBINARY` |
| `java.net.URL`, `java.net.URI` | `Types.DATALINK` |
| `java.lang.Object[]` | `Types.ARRAY` |
| `java.lang.Object` | `Types.JAVA_OBJECT` |

`byte[]` is a primitive array and `Byte[]` is a boxed array. General JDBC parameter binding treats both as binary values, not as a database `ARRAY`. If an adapter assigns schema-specific semantics, its data-source documentation states that explicitly. For example, Milvus uses the target vector schema to distinguish numeric elements, packed encodings, and Int8 components.

## Further Reading

The table above describes default type selection for parameter binding; it does not mean every database supports the corresponding storage types. For database column types, JDBC result types, available Java mappings and usage limits, see the data source's type support documentation:

- [MySQL](../../features/mysql/types.md)
- [PostgreSQL](../../features/postgresql/types.md)
- [Oracle](../../features/oracle/types.md)
- [Dameng](../../features/dm/types.md)
- [SQL Server](../../features/mssql/types.md)
- [DB2](../../features/db2/types.md)
- [H2](../../features/h2/types.md)
- [ClickHouse](../../features/clickhouse/types.md)
- [Redis](../../features/redis/dbvisitor/types.md)
- [MongoDB](../../features/mongo/dbvisitor/types.md)
- [Elasticsearch](../../features/elastic/dbvisitor/types.md)
- [Milvus](../../features/milvus/dbvisitor/types.md)

For handler matching rules and built-in implementations, see [Basic Type Handlers](./handlers/about.md).
