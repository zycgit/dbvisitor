---
id: java-jdbc
sidebar_position: 1
title: 8.1 Java/JDBC 类型关系
description: dbVisitor ORM 工具 Java类型映射表介绍。
---

<span id="javajdbc-类型关系" />

# 8.1 Java/JDBC 类型关系

在没有明确指定 JDBC 类型时，dbVisitor 会根据下面表格中的映射自动选择 Java 类型对应的 JDBC 类型作为参数。

## 默认映射表

| Java 类型 | JDBC 类型 |
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

`byte[]` 是基本类型数组，`Byte[]` 是包装类型数组；通用 JDBC 参数绑定默认都将它们作为二进制值，而不是数据库 `ARRAY`。特定适配器若依据目标字段赋予其他语义，会在对应数据源文档中明确说明。例如 Milvus 会根据向量字段 schema 区分数值序列、打包编码和 Int8 分量。

## 延伸阅读

上表说明参数绑定时的默认类型选择，不代表各数据库都支持对应的存储类型。数据库列类型、JDBC 结果类型、可用 Java 映射及具体使用限制，请参阅各数据源的类型支持文档：

- [MySQL](../../features/mysql/types.md)
- [PostgreSQL](../../features/postgresql/types.md)
- [Oracle](../../features/oracle/types.md)
- [达梦](../../features/dm/types.md)
- [SQL Server](../../features/mssql/types.md)
- [DB2](../../features/db2/types.md)
- [H2](../../features/h2/types.md)
- [ClickHouse](../../features/clickhouse/types.md)
- [Redis](../../features/redis/types.md)
- [MongoDB](../../features/mongo/types.md)
- [Elasticsearch](../../features/elastic/types.md)
- [Milvus](../../features/milvus/dbvisitor/types.md)

类型处理器的匹配规则与内置实现见[基础类型处理器](./handlers/about.md)。
