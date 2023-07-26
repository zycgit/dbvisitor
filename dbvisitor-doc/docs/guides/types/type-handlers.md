---
sidebar_position: 1
title: 类型处理器
description: dbVisitor ORM 工具类型处理器介绍。
---

# 类型处理器

确定一个类型处理器的顺序如下：

- **1st** `Java` + `Jdbc`
- **2st** `Java`
- **3st** `Jdbc`
- **4st** 使用 `UnknownTypeHandler`

## 类型处理器名称规则

dbVisitor 有着丰富的类型处理器，为了方便理解这些类型处理器它们有着统一的命名规则。这些规则分为三种：
1. `<Java类型名>TypeHandler`
2. `<JDBC类型名>As<Java类型名>TypeHandler`
3. `<xxx>TypeHandler`


## 类型处理器匹配表（第一优先级）

:::tip 
下面表格中 Java类型 和 JDBC 必须全部满足才能选择对应的 `类型处理器`，也是第一优先级。
:::

| JDBC 类型                                                                                                                                           | Java 类型               | 类型处理器                             |
|---------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------|-----------------------------------|
| `Types.CHAR`, `Types.VARCHAR`, `Types.LONGVARCHAR`, `Types.NCHAR`, `Types.NVARCHAR`, `Types.LONGNVARCHAR`                                         | `java.time.MonthDay`  | `StringAsMonthDayTypeHandler`     |
| `Types.TINYINT`, `Types.SMALLINT`, `Types.INTEGER`, `Types.BIGINT`, `Types.FLOAT`, `Types.DOUBLE`, `Types.REAL`, `Types.NUMERIC`, `Types.DECIMAL` | `java.time.MonthDay`  | `IntegerAsMonthDayTypeHandler`    |
| `Types.CHAR`, `Types.VARCHAR`, `Types.LONGVARCHAR`, `Types.NCHAR`, `Types.NVARCHAR`, `Types.LONGNVARCHAR`                                         | `java.time.YearMonth` | `StringAsYearMonthTypeHandler`    |
| `Types.TINYINT`, `Types.SMALLINT`, `Types.INTEGER`, `Types.BIGINT`, `Types.FLOAT`, `Types.DOUBLE`, `Types.REAL`, `Types.NUMERIC`, `Types.DECIMAL` | `java.time.YearMonth` | `IntegerAsYearMonthTypeHandler`   |
| `Types.CHAR`, `Types.VARCHAR`, `Types.LONGVARCHAR`, `Types.NCHAR`, `Types.NVARCHAR`, `Types.LONGNVARCHAR`                                         | `java.time.Year`      | `StringAsYearTypeHandler`         |
| `Types.TINYINT`, `Types.SMALLINT`, `Types.INTEGER`, `Types.BIGINT`, `Types.FLOAT`, `Types.DOUBLE`, `Types.REAL`, `Types.NUMERIC`, `Types.DECIMAL` | `java.time.Year`      | `IntegerAsYearTypeHandler`        |
| `Types.CHAR`, `Types.VARCHAR`, `Types.LONGVARCHAR`, `Types.NCHAR`, `Types.NVARCHAR`, `Types.LONGNVARCHAR`                                         | `java.time.Month`     | `StringAsMonthTypeHandler`        |
| `Types.TINYINT`, `Types.SMALLINT`, `Types.INTEGER`, `Types.BIGINT`, `Types.FLOAT`, `Types.DOUBLE`, `Types.REAL`, `Types.NUMERIC`, `Types.DECIMAL` | `java.time.Month`     | `IntegerAsMonthTypeHandler`       |
| `Types.CHAR`, `Types.VARCHAR`, `Types.LONGVARCHAR`, `Types.DATALINK`, `Types.ROWID`                                                               | `java.lang.String`    | `StringTypeHandler`               |
| `Types.NCHAR`, `Types.NVARCHAR`, `Types.LONGNVARCHAR`                                                                                             | `java.lang.String`    | `NStringTypeHandler`              |
| `Types.CLOB`                                                                                                                                      | `java.lang.String`    | `ClobAsStringTypeHandler`         |
| `Types.NCLOB`                                                                                                                                     | `java.lang.String`    | `NClobAsStringTypeHandler`        |
| `Types.SQLXML`                                                                                                                                    | `java.lang.String`    | `SqlXmlTypeHandler`               |
| `Types.CHAR`, `Types.VARCHAR`, `Types.LONGVARCHAR`                                                                                                | `java.io.Reader`      | `StringAsReaderTypeHandler`       |
| `Types.NCHAR`, `Types.NVARCHAR`, `Types.LONGNVARCHAR`                                                                                             | `java.io.Reader`      | `NStringAsReaderTypeHandler`      |
| `Types.CLOB`                                                                                                                                      | `java.io.Reader`      | `ClobAsReaderTypeHandler`         |
| `Types.NCLOB`                                                                                                                                     | `java.io.Reader`      | `NClobAsReaderTypeHandler`        |
| `Types.SQLXML`                                                                                                                                    | `java.io.Reader`      | `SqlXmlForReaderTypeHandler`      |
| `Types.BINARY`, `Types.VARBINARY`, `Types.LONGVARBINARY`, `Types.ROWID`                                                                           | `byte[]`              | `BytesTypeHandler`                |
| `Types.BLOB`                                                                                                                                      | `byte[]`              | `BlobAsBytesTypeHandler`          |
| `Types.BINARY`, `Types.VARBINARY`, `Types.LONGVARBINARY`, `Types.ROWID`                                                                           | `java.lang.Byte[]`    | `BytesAsBytesWrapTypeHandler`     |
| `Types.BLOB`                                                                                                                                      | `java.lang.Byte[]`    | `BlobAsBytesWrapTypeHandler`      |
| `Types.BINARY`, `Types.VARBINARY`, `Types.LONGVARBINARY`                                                                                          | `java.io.InputStream` | `BytesAsInputStreamTypeHandler`   |
| `Types.BLOB`                                                                                                                                      | `java.io.InputStream` | `BlobAsInputStreamTypeHandler`    |
| `Types.SQLXML`                                                                                                                                    | `java.io.InputStream` | `SqlXmlForInputStreamTypeHandler` |
| `Types.ARRAY`                                                                                                                                     | `java.lang.Object`    | `ArrayTypeHandler`                |
| `Types.DATALINK`                                                                                                                                  | `java.net.URI`        | `URITypeHandler`                  |
| `Types.DATALINK`                                                                                                                                  | `java.net.URL`        | `URLTypeHandler`                  |

## 类型处理器匹配表（第二、三优先级）

:::tip 下面表格中 Java类型 和 JDBC 只需满足任意一个，即可激活对应的 `类型处理器`
:::

| JDBC 类型                                                  | Java 类型                         | 类型处理器                                      |
|----------------------------------------------------------|---------------------------------|--------------------------------------------|
| `Types.BIT`, `Types.BOOLEAN`                             | `java.lang.Boolean`, `boolean`  | `BooleanTypeHandler`                       |
| `Types.TINYINT`                                          | `java.lang.Byte`, `byte`        | `ByteTypeHandler`                          |
| `Types.SMALLINT`                                         | `java.lang.Short`, `short`      | `ShortTypeHandler`                         |
| `Types.INTEGER`                                          | `java.lang.Integer`, `int`      | `IntegerTypeHandler`                       |
| `Types.BIGINT`                                           | `java.lang.Long`, `long`        | `LongTypeHandler`                          |
| `Types.FLOAT`                                            | `java.lang.Float`, `float`      | `FloatTypeHandler`                         |
| `Types.DOUBLE`                                           | `java.lang.Double`, `double`    | `DoubleTypeHandler`                        |
| `Types.REAL`, `Types.NUMERIC`, `Types.DECIMAL`           | `java.math.BigDecimal`          | `BigDecimalTypeHandler`                    |
| -                                                        | `java.lang.Number`              | `NumberTypeHandler`                        |
| `Types.CHAR`                                             | `java.lang.Character`, `char`   | `StringAsCharTypeHandler`                  |
| `Types.NCHAR`                                            | -                               | `NStringAsCharTypeHandler`                 |
| `Types.VARCHAR`, `Types.LONGVARCHAR`, `Types.ROWID`      | `java.lang.String`              | `StringTypeHandler`                        |
| `Types.NVARCHAR`, `Types.LONGNVARCHAR`                   | -                               | `NStringTypeHandler`                       |
| `Types.CLOB`                                             | `java.sql.Clob`                 | `ClobAsStringTypeHandler`                  |
| `Types.NCLOB`                                            | `java.sql.NClob`                | `NClobAsStringTypeHandler`                 |
| `Types.TIMESTAMP`                                        | `java.util.Date`                | `SqlTimestampAsDateTypeHandler`            |
| `Types.DATE`                                             | -                               | `SqlDateAsDateHandler`                     |
| -                                                        | `java.sql.Date`                 | `SqlDateTypeHandler`                       |
| -                                                        | `java.sql.Timestamp`            | `SqlTimestampTypeHandler`                  |
| -                                                        | `java.sql.Time`                 | `SqlTimeTypeHandler`                       |
| `Types.TIME`                                             | -                               | `SqlTimeAsDateTypeHandler`                 |
| -                                                        | `java.time.Instant`             | `SqlTimestampAsInstantTypeHandler`         |
| -                                                        | `java.time.chrono.JapaneseDate` | `JapaneseDateAsSqlDateTypeHandler`         |
| -                                                        | `java.time.Year`                | `SqlTimestampAsYearTypeHandler`            |
| -                                                        | `java.time.Month`               | `SqlTimestampAsMonthTypeHandler`           |
| -                                                        | `java.time.YearMonth`           | `SqlTimestampAsYearMonthTypeHandler`       |
| -                                                        | `java.time.MonthDay`            | `SqlTimestampAsMonthDayTypeHandler`        |
| -                                                        | `java.time.LocalDate`           | `LocalDateTimeAsLocalDateTypeHandler`      |
| -                                                        | `java.time.LocalTime`           | `LocalTimeTypeHandler`                     |
| -                                                        | `java.time.LocalDateTime`       | `LocalDateTimeTypeHandler`                 |
| -                                                        | `java.time.ZonedDateTime`       | `OffsetDateTimeAsZonedDateTimeTypeHandler` |
| `Types.TIMESTAMP_WITH_TIMEZONE`                          | `java.time.OffsetDateTime`      | `OffsetDateTimeTypeHandler`                |
| `Types.TIME_WITH_TIMEZONE`                               | `java.time.OffsetTime`          | `OffsetTimeTypeHandler`                    |
| -                                                        | `java.math.BigInteger`          | `BigIntegerTypeHandler`                    |
| -                                                        | `java.io.Reader`                | `StringAsReaderTypeHandler`                |
| -                                                        | `java.io.InputStream`           | `BytesAsInputStreamTypeHandler `           |
| -                                                        | `java.lang.Byte[]`              | `BytesAsBytesWrapTypeHandler`              |
| `Types.BINARY`, `Types.VARBINARY`, `Types.LONGVARBINARY` | `byte[]`                        | `BytesTypeHandler`                         |
| `Types.BLOB`                                             | `java.sql.Blob`                 | `BlobAsBytesTypeHandler`                   |
| `Types.DATALINK`                                         | `java.net.URI`                  | `URLTypeHandler`                           |
| -                                                        | `java.net.URL`                  | `URLTypeHandler`                           |
| `Types.JAVA_OBJECT`                                      | -                               | `ObjectTypeHandler`                        |
| `Types.ARRAY`                                            | `java.lang.Object[]`            | `ArrayTypeHandler`                         |
| `Types.SQLXML`                                           | -                               | `SqlXmlTypeHandler`                        |
| `Types.OTHER`                                            | `java.lang.Object`              | `UnknownTypeHandler`                       |

## 其它类型处理器

地理信息类型处理器，用于处理 GIS 相关数据。主要格式有 WKB、WKT 两种格式

| 类型处理器                               | Java类型                         | 作用                                                        |
|-------------------------------------|--------------------------------|-----------------------------------------------------------|
| `JtsGeometryWktAsWkbTypeHandler`    | `byte[]`                       | 基于 JTS 的地理信息处理器，以 WKB 形式，读写 WKT 格式数据                      |
| `JtsGeometryWkbAsWktTypeHandler`    | `java.lang.String`             | 基于 JTS 的地理信息处理器，以 WKT 格式读写 WKB 格式数据                       |
| `JtsGeometryWkbHexAsWktTypeHandler` | `java.lang.String`             | 基于 JTS 的地理信息处理器，以 WKT 形式，读写 WKB(HEX) 数据（数据库存储读使用 十六进制字符串） |

自动时区转换，时区的写入和读取会转换为 UTC。

| 类型处理器                                        | Java类型                     | 作用                                           |
|----------------------------------------------|----------------------------|----------------------------------------------|
| `SqlTimestampAsUTCOffsetDateTimeTypeHandler` | `java.time.OffsetDateTime` | 自动时区转换，使用时区 OffsetDateTime 类型读写 Timestamp 数据 |
| `SqlTimestampAsUTCOffsetTimeTypeHandler`     | `java.time.OffsetTime`     | 自动时区转换，使用时区 OffsetTime 类型读写 Timestamp 数据     |

用以超大数值的存储，当某个数值已经超出了数据库存储精度范围，那么可以使用字符串进行存储。
同时程序上依然使用 BigDecimal 或 BigInteger 来处理它们

| 类型处理器                           | Java类型                 | 作用                                 |
|---------------------------------|------------------------|------------------------------------|
| `StringAsBigDecimalTypeHandler` | `java.math.BigDecimal` | 超大数读写，使用 BigDecimal 类型读写 string 数据 |
| `StringAsBigIntegerTypeHandler` | `java.math.BigInteger` | 超大数读写，使用 BigInteger 类型读写 string 数据 |

对于 `java.sql.Timestamp` 类型提供到 Java8 新时间类型的映射和读写。减少程序上的转换和处理

| 类型处理器                                    | Java类型                    | 作用                                        |
|------------------------------------------|---------------------------|-------------------------------------------|
| `SqlTimestampAsLocalDateTimeTypeHandler` | `java.time.LocalDateTime` | 使用 LocalDateTime 类型读写，提供类型转换              |
| `SqlTimestampAsLocalDateTypeHandler`     | `java.time.LocalDate`     | 使用 LocalDate 类型读写，缺失的时间信息使用 00:00 补充      |
| `SqlTimestampAsLocalTimeTypeHandler`     | `java.time.LocalTime`     | 使用 LocalTime 类型读写，缺失的时间信息使用 0000-01-01 补充 |

PostgreSQL 数据库的特种类型处理器

| 类型处理器                            | Java类型                 | 作用                     |
|----------------------------------|------------------------|------------------------|
| `PgArrayTypeHandler`             | `Array`                | 处理 PostgreSQL 的数组类读写   |
| `PgMoneyAsBigDecimalTypeHandler` | `java.math.BigDecimal` | 支持 PostgreSQL,Money 类型 |


其它处理器

| 类型处理器                         | Java类型                         | 作用                                               |
|-------------------------------|--------------------------------|--------------------------------------------------|
| `EnumTypeHandler`             | `java.lang.Enum`               | 用于将枚举类型的映射和读写，并提供 EnumOfCode、EnumOfValue 两个接口的支持 |
| `IntegerAsBooleanTypeHandler` | `java.lang.Boolean`, `boolean` | 用于数值类型和布尔类型的映射，任何一个非零的整数都会被解析为 true              |
| `IntegerAsBooleanTypeHandler` | `java.lang.Boolean`, `boolean` | 用于数值类型和布尔类型的映射，任何一个非零的整数都会被解析为 true              |