---
id: sqlserver
sidebar_position: 4
title: SQL SERVER
description: faker 对于 SQL SERVER 的数值生成支持特性
---
# SQL SERVER 支持特性

引用资料
- https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/data-types-transact-sql
- https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/time-transact-sql
- 本文会涵盖 SQL SERVER 到 jdbc 的类型映射、不同策略数值生成范围描述

## 支持的类型

| SQL SERVER Type                     | 支持性 |
|-------------------------------------|-----|
| `BIT`                               | 支持  |
| `TINYINT`                           | 支持  |
| `SMALLINT`                          | 支持  |
| `INT`                               | 支持  |
| `BIGINT`                            | 支持  |
| `MONEY`、`SMALLMONEY`                | 支持  |
| `FLOAT`、`REAL`                      | 支持  |
| `NUMERIC`、`DECIMAL`                 | 支持  |
| `CHAR`、`VARCHAR`、`NCHAR`、`NVARCHAR` | 支持  |
| `TEXT`、`NTEXT`                      | 支持  |
| `DATE`                              | 支持  |
| `TIME`                              | 支持  |
| `SMALLDATETIME`                     | 支持  |
| `DATETIME`、`DATETIME2`              | 支持  |
| `DATETIMEOFFSET`                    | 支持  |
| `TIMESTAMP`                         | 支持  |
| `BINARY`、`VARBINARY`、`IMAGE`        | 支持  |
| `UNIQUEIDENTIFIER`                  | 支持  |
| `SYSNAME`                           | 支持  |
| `GEOGRAPHY`                         | 不支持 |
| `GEOMETRY`                          | 不支持 |
| `XML`                               | 不支持 |
| `HIERARCHYID`                       | 不支持 |
| `SQL_VARIANT`                       | 不支持 |

## 默认策略(widely)
- 策略名：`widely`
- 默认策略：`true`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/sqlserver-widely.tpc`

| SQL SERVER Type                                            | JDBC Type                       | 描述                                                                                                     |
|------------------------------------------------------------|---------------------------------|--------------------------------------------------------------------------------------------------------|
| `BIT`                                                      | `Types.BOOLEAN`                 | 取值范围：true, false                                                                                       |
| `TINYINT`                                                  | `Types.TINYINT`                 | 取值范围：0 ~ 100                                                                                           |
| `SMALLINT`                                                 | `Types.INTEGER`                 | 取值范围：0 ~ 9999                                                                                          |
| `INT`                                                      | `Types.INTEGER`                 | 取值范围：0 ~ 99999999                                                                                      |
| `BIGINT`                                                   | `Types.BIGINT`                  | 取值范围：0 ~ 9999999999                                                                                    |
| `MONEY`、`SMALLMONEY`                                       | `Types.DOUBLE`                  | 取值范围：0 ~ 99999.999<br/>小数精度：固定 3 位                                                                     |
| `FLOAT`、`REAL`                                             | `Types.DOUBLE`                  | 取值范围：0 ~ 9999999.999<br/>小数精度：最小 3 位                                                                   |
| `NUMERIC`、`DECIMAL`                                        | `Types.DECIMAL`                 | 取值范围：0 ~ 9999999.999<br/>小数精度：最大 3 位                                                                   |
| `CHAR`、`VARCHAR`<br/>`NCHAR`、`NVARCHAR`<br/>`TEXT`、`NTEXT` | `Types.VARCHAR`                 | 长度范围：1 ~ 250，默认 10                                                                                     |
| `DATE`                                                     | `Types.DATE`                    | 取值范围：2000-01-01 ~ 2030-12-31                                                                           |
| `TIME`                                                     | `Types.TIME`                    | 最小值：00:00:00.0000000<br/>最大值：23:59:59.9999999<br/>时间精度：0 ~ 7位，默认 3 位                                   |
| `SMALLDATETIME`                                            | `Types.TIMESTAMP`               | 最小值：2000-01-01 00:00:00<br/>最大值：2030-12-31 23:59:59                                                    |
| `DATETIME`、`DATETIME2`                                     | `Types.TIMESTAMP`               | 最小值：2000-01-01 00:00:00.000<br/>最大值：2030-12-31 23:59:59.999<br/>时间精度：0 ~ 3位，默认 1 位                     |
| `DATETIMEOFFSET`                                           | `Types.TIMESTAMP_WITH_TIMEZONE` | 最小值：2000-01-01 00:00:00.0000000-08:00<br/>最大值：2030-12-31 23:59:59.9999999+08:00<br/>时间精度：0 ~ 7位，默认 3 位 |
| `TIMESTAMP`                                                | `Types.VARBINARY`               | 不参与 `Insert` 与 `UpdateSet`                                                                             |
| `BINARY`、`VARBINARY`<br/>`IMAGE`                           | `Types.VARBINARY`               | 长度范围：0 ~ 16，默认 4                                                                                       |
| `UNIQUEIDENTIFIER`                                         | `Types.VARCHAR`                 | 长度为 36 的 UUID                                                                                          |
| `SYSNAME`                                                  | `Types.VARCHAR`                 | 长度为：4 ~ 64，小写字母                                                                                        |

## 极值策略(extreme)
- 策略名：`extreme`
- 默认策略：`false`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/sqlserver-extreme.tpc`

| SQL SERVER Type                  | JDBC Type                       | 描述                                                                                                                                                             |
|----------------------------------|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BIT`                            | `Types.BOOLEAN`                 | 取值范围：true, false                                                                                                                                               |
| `TINYINT`                        | `Types.TINYINT`                 | 取值范围：0 ~ 255                                                                                                                                                   |
| `SMALLINT`                       | `Types.INTEGER`                 | 取值范围：-32768 ~ 32767                                                                                                                                            |
| `INT`                            | `Types.INTEGER`                 | 取值范围：-2147483648 ~ 2147483647                                                                                                                                  |
| `BIGINT`                         | `Types.BIGINT`                  | 取值范围：-9223372036854775808 ~ 9223372036854775807                                                                                                                |
| `SMALLMONEY`                     | `Types.DOUBLE`                  | 取值范围：-214748.3648 ~ 214748.3647<br/>小数精度：固定 4 位                                                                                                                |
| `MONEY`                          | `Types.DECIMAL`                 | 取值范围：-922337203685477.5808 ~ 922337203685477.5807<br/>小数精度：固定 4 位                                                                                              |
| `FLOAT`                          | `Types.FLOAT`                   | 10%，-2.23E-308 ~ -1.79E+308<br/>10%，2.23E-308 ~ 1.79E+308<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：参照元信息 |
| `REAL`                           | `Types.REAL`                    | 10%，-1.18E-38 ~ -3.40E+38<br/>10%，1.18E-38 ~ 3.40E+38<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：参照元信息     |
| `NUMERIC`、`DECIMAL`              | `Types.DECIMAL`                 | 参照元信息决定 `precision`、`scale`                                                                                                                                    |
| `CHAR`、`VARCHAR`                 | `Types.VARCHAR`                 | 长度范围：1 ~ 1000，默认 10                                                                                                                                            |
| `NCHAR`、`NVARCHAR`               | `Types.NVARCHAR`                | 长度范围：1 ~ 1000，默认 10                                                                                                                                            |
| `TEXT`                           | `Types.LONGVARCHAR`             | 长度范围：1 ~ 1000，默认 10                                                                                                                                            |
| `NTEXT`                          | `Types.LONGNVARCHAR`            | 长度范围：1 ~ 1000，默认 10                                                                                                                                            |
| `DATE`                           | `Types.DATE`                    | 取值范围：0001-01-01 ~ 9999-12-31                                                                                                                                   |
| `TIME`                           | `Types.TIME`                    | 最小值：00:00:00.0000000<br/>最大值：23:59:59.9999999<br/>时间精度：0 ~ 7位，默认 3 位                                                                                           |
| `SMALLDATETIME`                  | `Types.TIMESTAMP`               | 最小值：1900-01-01 00:00:00<br/>最大值：2079-06-06 23:59:00                                                                                                            |
| `DATETIME`                       | `Types.TIMESTAMP`               | 最小值：1753-01-01 00:00:00.000<br/>最大值：9999-12-31 23:59:59.997<br/>时间精度：0 ~ 3位，默认 1 位                                                                             |
| `DATETIME2`                      | `Types.TIMESTAMP`               | 最小值：0001-01-01 00:00:00.0000000<br/>最大值：9999-12-31 23:59:59.9999999<br/>时间精度：0 ~ 7位，默认 3 位                                                                     |
| `DATETIMEOFFSET`                 | `Types.TIMESTAMP_WITH_TIMEZONE` | 最小值：0001-01-01 00:00:00.0000000-14:00<br/>最大值：9999-12-31 23:59:59.9999999+14:00<br/>时间精度：0 ~ 7位，默认 3 位                                                         |
| `TIMESTAMP`                      | `Types.VARBINARY`               | 不参与 `Insert` 与 `UpdateSet`                                                                                                                                     |
| `BINARY`、`VARBINARY`<br/>`IMAGE` | `Types.VARBINARY`               | 长度范围：0 ~ 4096，默认 10                                                                                                                                            |
| `UNIQUEIDENTIFIER`               | `Types.VARCHAR`                 | 长度为 36 的 UUID                                                                                                                                                  |
| `SYSNAME`                        | `Types.VARCHAR`                 | 长度为：1 ~ 128，小写字母                                                                                                                                               |
