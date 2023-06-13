---
sidebar_position: 5
title: 附录：类型映射
description: dbVisitor ORM 工具 Java类型映射表介绍。
---

# 附录：类型映射

根据 Java 类型获取对应的 JDBC Type

| JDBC 类型                         | Java 类型                                                                                                                                         |
|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| `Types.BIT`                     | `java.lang.Boolean`, `boolean`                                                                                                                  |
| `Types.TINYINT`                 | `java.lang.Byte`, `byte`                                                                                                                        |
| `Types.SMALLINT`                | `java.lang.Short`, `short`, `java.time.Year`, `java.time.Month`                                                                                 |
| `Types.INTEGER`                 | `java.lang.Integer`, `int`                                                                                                                      |
| `Types.BIGINT`                  | `java.lang.Long`, `long`, `java.math.BigInteger`                                                                                                |
| `Types.FLOAT`                   | `java.lang.Float`, `float`                                                                                                                      |
| `Types.DOUBLE`                  | `java.lang.Double`, `double`                                                                                                                    |
| `Types.CHAR`                    | `java.lang.Character`, `char`                                                                                                                   |
| `Types.DATE`                    | `java.sql.Date`, `java.time.LocalDate`, `oracle.sql.DATE`                                                                                       |
| `Types.TIME`                    | `java.sql.Time`, `java.time.LocalTime`                                                                                                          |
| `Types.TIMESTAMP`               | `java.util.Date`, `java.sql.Timestamp`, `java.time.Instant`, `java.time.LocalDateTime`, `java.time.chrono.JapaneseDate`, `oracle.sql.TIMESTAMP` |
| `Types.TIMESTAMP_WITH_TIMEZONE` | `java.time.OffsetDateTime`, `java.time.ZonedDateTime`, `oracle.sql.TIMESTAMPTZ`, `oracle.sql.TIMESTAMPLTZ`                                      |
| `Types.TIME_WITH_TIMEZONE`      | `java.time.OffsetTime`                                                                                                                          |
| `Types.VARCHAR`                 | `java.lang.String`, `java.time.YearMonth`                                                                                                       |
| `Types.DECIMAL`                 | `java.math.BigDecimal`                                                                                                                          |
| `Types.CLOB`                    | `java.io.Reader`, `oracle.jdbc.OracleClob`                                                                                                      |
| `Types.BLOB`                    | `java.io.InputStream`, `oracle.jdbc.OracleBlob`                                                                                                 |
| `Types.VARBINARY`               | `java.lang.Byte[]`, `byte[]`                                                                                                                    |
| `Types.DATALINK`                | `java.net.URL`                                                                                                                                  |
| `Types.ARRAY`                   | `java.lang.Object[]`                                                                                                                            |
| `Types.JAVA_OBJECT`             | `java.lang.Object`                                                                                                                              |
| `Types.BLOB`                    | `oracle.jdbc.OracleBlob`                                                                                                                        |
| `Types.CLOB`                    | `oracle.jdbc.OracleClob`                                                                                                                        |
| `Types.NCLOB`                   | `oracle.jdbc.OracleNClob`                                                                                                                       |
