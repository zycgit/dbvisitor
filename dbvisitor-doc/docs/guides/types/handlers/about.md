---
id: about
sidebar_position: 1
title: 基础类型处理器
description: dbVisitor 基础类型处理器概览与命名规则。
---

## 基础类型处理器

dbVisitor 有着丰富的类型处理器，为了方便理解它们有着统一的命名规则，分为三种：

1. `<Java类型名>TypeHandler` 或 `<JDBC类型名>TypeHandler` — 直接对应 Java/JDBC 类型
2. `<JDBC类型名>As<Java类型名>TypeHandler` 或 `<一种类型>As<另一种类型>TypeHandler` — 跨类型转换
3. `<xxx>TypeHandler` — 特殊类型处理（前缀 `Pg` 表示 PostgreSQL 专用，前缀 `Oracle` 表示 Oracle 专用）

### 组合类型（第一优先级） {#mix_type}

:::info
下面表格中 Java 类型 和 JDBC 必须全部满足才能选择对应的类型处理器。
:::

| JDBC 类型                                                                   | Java 类型             | 类型处理器                          |
|---------------------------------------------------------------------------|---------------------|--------------------------------|
| CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR                 | java.time.MonthDay  | StringAsMonthDayTypeHandler    |
| TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, DOUBLE, REAL, NUMERIC, DECIMAL | java.time.MonthDay  | IntegerAsMonthDayTypeHandler   |
| CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR                 | java.time.YearMonth | StringAsYearMonthTypeHandler   |
| TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, DOUBLE, REAL, NUMERIC, DECIMAL | java.time.YearMonth | IntegerAsYearMonthTypeHandler  |
| CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR                 | java.time.Year      | StringAsYearTypeHandler        |
| TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, DOUBLE, REAL, NUMERIC, DECIMAL | java.time.Year      | IntegerAsYearTypeHandler       |
| CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR                 | java.time.Month     | StringAsMonthTypeHandler       |
| TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, DOUBLE, REAL, NUMERIC, DECIMAL | java.time.Month     | IntegerAsMonthTypeHandler      |
| CHAR, VARCHAR, LONGVARCHAR, DATALINK, ROWID                               | java.lang.String    | StringTypeHandler              |
| NCHAR, NVARCHAR, LONGNVARCHAR                                             | java.lang.String    | NStringTypeHandler             |
| CLOB                                                                      | java.lang.String    | ClobAsStringTypeHandler        |
| NCLOB                                                                     | java.lang.String    | NClobAsStringTypeHandler       |
| SQLXML                                                                    | java.lang.String    | SqlXmlTypeHandler              |
| CHAR, VARCHAR, LONGVARCHAR                                                | java.io.Reader      | StringAsReaderTypeHandler      |
| NCHAR, NVARCHAR, LONGNVARCHAR                                             | java.io.Reader      | NStringAsReaderTypeHandler     |
| CLOB                                                                      | java.io.Reader      | ClobAsReaderTypeHandler        |
| NCLOB                                                                     | java.io.Reader      | NClobAsReaderTypeHandler       |
| SQLXML                                                                    | java.io.Reader      | SqlXmlAsReaderTypeHandler      |
| BINARY, VARBINARY, LONGVARBINARY, ROWID                                   | byte[]              | BytesTypeHandler               |
| BLOB                                                                      | byte[]              | BlobAsBytesTypeHandler         |
| BINARY, VARBINARY, LONGVARBINARY, ROWID                                   | java.lang.Byte[]    | BytesAsBytesWrapTypeHandler    |
| BLOB                                                                      | java.lang.Byte[]    | BlobAsBytesWrapTypeHandler     |
| BINARY, VARBINARY, LONGVARBINARY                                          | java.io.InputStream | BytesAsInputStreamTypeHandler  |
| BLOB                                                                      | java.io.InputStream | BlobAsInputStreamTypeHandler   |
| SQLXML                                                                    | java.io.InputStream | SqlXmlAsInputStreamTypeHandler |
| ARRAY                                                                     | java.lang.Object    | ArrayTypeHandler               |
| DATALINK                                                                  | java.net.URI        | StringAsUriTypeHandler         |
| DATALINK                                                                  | java.net.URL        | StringAsUrlTypeHandler         |

### 单一类型（第二优先级） {#single_type}

:::info
下面表格中 Java 类型 和 JDBC 类型只需满足任意一个，即可激活对应的类型处理器。
:::

| JDBC 类型                               | Java 类型                       | 类型处理器                                    |
|---------------------------------------|-------------------------------|------------------------------------------|
| BIT, BOOLEAN                          | java.lang.Boolean, boolean    | BooleanTypeHandler                       |
| TINYINT                               | java.lang.Byte, byte          | ByteTypeHandler                          |
| SMALLINT                              | java.lang.Short, short        | ShortTypeHandler                         |
| INTEGER                               | java.lang.Integer, int        | IntegerTypeHandler                       |
| BIGINT                                | java.lang.Long, long          | LongTypeHandler                          |
| FLOAT                                 | java.lang.Float, float        | FloatTypeHandler                         |
| DOUBLE                                | java.lang.Double, double      | DoubleTypeHandler                        |
| REAL, NUMERIC, DECIMAL                | java.math.BigDecimal          | BigDecimalTypeHandler                    |
| -                                     | java.lang.Number              | NumberTypeHandler **(只支持数据读取)**          |
| CHAR                                  | java.lang.Character, char     | StringAsCharTypeHandler                  |
| NCHAR                                 | -                             | NStringAsCharTypeHandler                 |
| VARCHAR, LONGVARCHAR, ROWID           | java.lang.String              | StringTypeHandler                        |
| NVARCHAR, LONGNVARCHAR                | -                             | NStringTypeHandler                       |
| CLOB                                  | java.sql.Clob                 | ClobAsStringTypeHandler                  |
| NCLOB                                 | java.sql.NClob                | NClobAsStringTypeHandler                 |
| TIMESTAMP                             | java.util.Date                | SqlTimestampAsDateTypeHandler            |
| DATE                                  | -                             | SqlDateAsDateHandler                     |
| -                                     | java.sql.Date                 | SqlDateTypeHandler                       |
| -                                     | java.sql.Timestamp            | SqlTimestampTypeHandler                  |
| -                                     | java.sql.Time                 | SqlTimeTypeHandler                       |
| TIME                                  | -                             | SqlTimeAsDateTypeHandler                 |
| -                                     | java.time.Instant             | SqlTimestampAsInstantTypeHandler         |
| -                                     | java.time.chrono.JapaneseDate | JapaneseDateAsSqlDateTypeHandler         |
| -                                     | java.time.Year                | SqlTimestampAsYearTypeHandler            |
| -                                     | java.time.Month               | SqlTimestampAsMonthTypeHandler           |
| -                                     | java.time.YearMonth           | SqlTimestampAsYearMonthTypeHandler       |
| -                                     | java.time.MonthDay            | SqlTimestampAsMonthDayTypeHandler        |
| -                                     | java.time.LocalDate           | LocalDateTimeAsLocalDateTypeHandler      |
| -                                     | java.time.LocalTime           | LocalTimeTypeHandler                     |
| -                                     | java.time.LocalDateTime       | LocalDateTimeTypeHandler                 |
| -                                     | java.time.ZonedDateTime       | OffsetDateTimeAsZonedDateTimeTypeHandler |
| TIMESTAMP_WITH_TIMEZONE               | java.time.OffsetDateTime      | OffsetDateTimeTypeHandler                |
| TIME_WITH_TIMEZONE                    | java.time.OffsetTime          | OffsetTimeTypeHandler                    |
| -                                     | java.math.BigInteger          | BigIntegerTypeHandler                    |
| -                                     | java.io.Reader                | StringAsReaderTypeHandler                |
| -                                     | java.io.InputStream           | BytesAsInputStreamTypeHandler            |
| -                                     | java.lang.Byte[]              | BytesAsBytesWrapTypeHandler              |
| BINARY, VARBINARY, LONGVARBINARY      | byte[]                        | BytesTypeHandler                         |
| BLOB                                  | java.sql.Blob                 | BlobAsBytesTypeHandler                   |
| DATALINK                              | java.net.URI                  | StringAsUriTypeHandler                   |
| -                                     | java.net.URL                  | StringAsUrlTypeHandler                   |
| JAVA_OBJECT                           | -                             | ObjectTypeHandler                        |
| ARRAY                                 | java.lang.Object[]            | ArrayTypeHandler                         |
| SQLXML                                | -                             | SqlXmlTypeHandler                        |
| OTHER                                 | java.lang.Object              | UnknownTypeHandler                       |
