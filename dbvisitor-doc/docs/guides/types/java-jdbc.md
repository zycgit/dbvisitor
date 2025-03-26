---
id: java-jdbc
sidebar_position: 1
hide_table_of_contents: true
title: 8.1 Java/JDBC 类型关系
description: dbVisitor ORM 工具 Java类型映射表介绍。
---

# Java/JDBC 类型关系

在没有明确指定 JDBC 类型时，dbVisitor 会根据下面表格中的映射自动选择 Java 类型对应的 JDBC 类型作为参数。

| Java 类型                                                                                                                                         | JDBC 类型                         |
|-------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------|
| `java.lang.Boolean`, `boolean`                                                                                                                  | `Types.BIT`                     |
| `java.lang.Byte`, `byte`                                                                                                                        | `Types.TINYINT`                 |
| `java.lang.Short`, `short`, `java.time.Year`, `java.time.Month`                                                                                 | `Types.SMALLINT`                |
| `java.lang.Integer`, `int`                                                                                                                      | `Types.INTEGER`                 |
| `java.lang.Long`, `long`, `java.math.BigInteger`                                                                                                | `Types.BIGINT`                  |
| `java.lang.Float`, `float`                                                                                                                      | `Types.FLOAT`                   |
| `java.lang.Double`, `double`                                                                                                                    | `Types.DOUBLE`                  |
| `java.lang.Character`, `char`                                                                                                                   | `Types.CHAR`                    |
| `java.sql.Date`, `java.time.LocalDate`, `oracle.sql.DATE`                                                                                       | `Types.DATE`                    |
| `java.sql.Time`, `java.time.LocalTime`                                                                                                          | `Types.TIME`                    |
| `java.util.Date`, `java.sql.Timestamp`, `java.time.Instant`, `java.time.LocalDateTime`, `java.time.chrono.JapaneseDate`, `oracle.sql.TIMESTAMP` | `Types.TIMESTAMP`               |
| `java.time.OffsetDateTime`, `java.time.ZonedDateTime`, `oracle.sql.TIMESTAMPTZ`, `oracle.sql.TIMESTAMPLTZ`                                      | `Types.TIMESTAMP_WITH_TIMEZONE` |
| `java.time.OffsetTime`                                                                                                                          | `Types.TIME_WITH_TIMEZONE`      |
| `java.lang.String`, `java.time.YearMonth`                                                                                                       | `Types.VARCHAR`                 |
| `java.math.BigDecimal`                                                                                                                          | `Types.DECIMAL`                 |
| `java.io.Reader`, `oracle.jdbc.OracleClob`                                                                                                      | `Types.CLOB`                    |
| `java.io.InputStream`, `oracle.jdbc.OracleBlob`                                                                                                 | `Types.BLOB`                    |
| `java.lang.Byte[]`, `byte[]`                                                                                                                    | `Types.VARBINARY`               |
| `java.net.URL`                                                                                                                                  | `Types.DATALINK`                |
| `java.lang.Object[]`                                                                                                                            | `Types.ARRAY`                   |
| `java.lang.Object`                                                                                                                              | `Types.JAVA_OBJECT`             |
| `oracle.jdbc.OracleBlob`                                                                                                                        | `Types.BLOB`                    |
| `oracle.jdbc.OracleClob`                                                                                                                        | `Types.CLOB`                    |
| `oracle.jdbc.OracleNClob`                                                                                                                       | `Types.NCLOB`                   |
| `java.net.URL`, `java.net.URI`                                                                                                                  | `Types.DATALINK`                |
