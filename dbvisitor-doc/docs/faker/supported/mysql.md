---
id: mysql
sidebar_position: 2
title: MySQL
description: faker 对于 MySQL 的数值生成支持特性
---
# MySQL 支持特性

引用资料
- https://dev.mysql.com/doc/refman/8.0/en/data-types.html
- 本文会涵盖 MySQL 到 jdbc 的类型映射、不同策略数值生成范围描述

## 支持的类型

| MySQL Type                                | 支持性  |
|-------------------------------------------|------|
| `BIT`                                     | 支持   |
| `TINYINT`、`TINYINT UNSIGNED`              | 支持   |
| `SMALLINT`、`SMALLINT UNSIGNED`            | 支持   |
| `MEDIUMINT`、`MEDIUMINT UNSIGNED`          | 支持   |
| `INT`、`INT UNSIGNED`                      | 支持   |
| `BIGINT`、`BIGINT UNSIGNED`                | 支持   |
| `DECIMAL`、`DECIMAL UNSIGNED`              | 支持   |
| `FLOAT`、`FLOAT UNSIGNED`                  | 支持   |
| `DOUBLE`、`DOUBLE UNSIGNED`                | 支持   |
| `DATE`                                    | 支持   |
| `DATETIME`                                | 支持   |
| `TIMESTAMP`                               | 支持   |
| `TIME`                                    | 支持   |
| `YEAR`                                    | 支持   |
| `CHAR`、`VARCHAR`                          | 支持   |
| `TINYTEXT`、`TEXT`、`MEDIUMTEXT`、`LONGTEXT` | 支持   |
| `BINARY`、`VARBINARY`                      | 支持   |
| `MEDIUMBLOB`、`TINYBLOB`、`BLOB`、`LONGBLOB` | 支持   |
| `ENUM`、`SET`                              | 有限支持 |
| `GEOMETRY`                                | 支持   |
| `POINT`                                   | 不支持  |
| `LINESTRING`                              | 不支持  |
| `POLYGON`                                 | 不支持  |
| `MULTIPOINT`                              | 不支持  |
| `MULTILINESTRING`                         | 不支持  |
| `MULTIPOLYGON`                            | 不支持  |
| `GEOMCOLLECTION`                          | 不支持  |
| `JSON`                                    | 不支持  |

## 默认策略(widely)
- 策略名：`widely`
- 默认策略：`true`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/mysql-widely.tpc`

| MySQL Type                                                                               | JDBC Type         | 描述                                                                                              |
|------------------------------------------------------------------------------------------|-------------------|-------------------------------------------------------------------------------------------------|
| `BIT`                                                                                    | `Types.INT`       | 取值范围：0,1<br/>随机长度：1 ~ 24，默认 8                                                                   |
| `TINYINT`、`TINYINT UNSIGNED`                                                             | `Types.INTEGER`   | 取值范围：0 ~ 100                                                                                    |
| `SMALLINT`、`SMALLINT UNSIGNED`                                                           | `Types.INTEGER`   | 取值范围：0 ~ 9999                                                                                   |
| `MEDIUMINT`、`MEDIUMINT UNSIGNED`                                                         | `Types.INTEGER`   | 取值范围：0 ~ 999999                                                                                 |
| `INT`、`INT UNSIGNED`                                                                     | `Types.BIGINT`    | 取值范围：0 ~ 99999999                                                                               |
| `BIGINT`、`BIGINT UNSIGNED`                                                               | `Types.BIGINT`    | 取值范围：0 ~ 9999999999                                                                             |
| `DECIMAL`、`DECIMAL UNSIGNED`<br/>`FLOAT`、`FLOAT UNSIGNED`<br/>`DOUBLE`、`DOUBLE UNSIGNED` | `Types.DECIMAL`   | 取值范围：0 ~ 9999999.999<br/>小数精度：最大 3 位                                                            |
| `DATE`                                                                                   | `Types.DATE`      | 取值范围：2000-01-01 ~ 2030-12-31                                                                    |
| `DATETIME`、`TIMESTAMP`                                                                   | `Types.TIMESTAMP` | 最小值：2000-01-01 00:00:00.000000<br/>最大值：2030-12-31 23:59:59.999999<br/>时间精度：0 ~ 6位，默认 3 位        |
| `TIME`                                                                                   | `Types.VARCHAR`   | 最小值：00:00:00.000000<br/>最大值：23:59:59.999999<br/>时间精度：0 ~ 6位，默认 3 位                              |
| `YEAR`                                                                                   | `Types.INTEGER`   | 取值范围：2000 ~ 2030                                                                                |
| `CHAR`、`VARCHAR`<br/>`TINYTEXT`、`TEXT`<br/>`MEDIUMTEXT`、`LONGTEXT`                       | `Types.VARCHAR`   | 长度范围：1 ~ 250，默认 10                                                                              |
| `BINARY`、`VARBINARY`<br/>`MEDIUMBLOB`、`TINYBLOB`<br/>`BLOB`、`LONGBLOB`                   | `Types.VARBINARY` | 长度范围：0 ~ 16，默认 4                                                                                |
| `ENUM`、`SET`                                                                             | `Types.VARCHAR`   | ENUM 类型暂时还不支持识别数据库定义的字典项                                                                        |
| `GEOMETRY`                                                                               | `Types.VARCHAR`   | 写入格式：WKT<br/>坐标系：平面直角坐标系<br/>矩形区域 A 点：50, 0<br/>矩形区域 B 点：0, 1000<br/>点数量：2 ~ 10<br/>点坐标精度：5 位小数 |

## 极值策略(extreme)
说明
- 策略名：`extreme`
- 默认策略：`false`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/mysql-extreme.tpc`

| MySQL Type                                                             | JDBC Type           | 描述                                                                                                                                                                                                                     |
|------------------------------------------------------------------------|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BIT`                                                                  | `Types.INT`         | 取值范围：0,1<br/>随机长度：1 ~ 64，默认 24                                                                                                                                                                                         |
| `TINYINT`                                                              | `Types.TINYINT`     | 取值范围：-128 ~ 127                                                                                                                                                                                                        |
| `TINYINT UNSIGNED`                                                     | `Types.SMALLINT`    | 取值范围：0 ~ 255                                                                                                                                                                                                           |
| `SMALLINT`                                                             | `Types.SMALLINT`    | 取值范围：-32768 ~ 32767                                                                                                                                                                                                    |
| `SMALLINT UNSIGNED`                                                    | `Types.INTEGER`     | 取值范围：0 ~ 65535                                                                                                                                                                                                         |
| `MEDIUMINT`                                                            | `Types.INTEGER`     | 取值范围：-8388608 ~ 8388607                                                                                                                                                                                                |
| `MEDIUMINT UNSIGNED`                                                   | `Types.INTEGER`     | 取值范围：0 ~ 16777215                                                                                                                                                                                                      |
| `INT`                                                                  | `Types.INTEGER`     | 取值范围：-2147483648 ~ 2147483647                                                                                                                                                                                          |
| `INT UNSIGNED`                                                         | `Types.BIGINT`      | 取值范围：0 ~ 4294967295                                                                                                                                                                                                    |
| `BIGINT`                                                               | `Types.BIGINT`      | 取值范围：-9223372036854775808 ~ 9223372036854775807                                                                                                                                                                        |
| `BIGINT UNSIGNED`                                                      | `Types.BIGINT`      | 取值范围：0 ~ 18446744073709551615                                                                                                                                                                                          |
| `DECIMAL`、`DECIMAL UNSIGNED`                                           | `Types.DECIMAL`     | 类型为 `UNSIGNED` 时会取绝对值<br/>精度范围：参照元信息                                                                                                                                                                                   |
| `FLOAT`、`FLOAT UNSIGNED`                                               | `Types.FLOAT`       | 10%，-3.402823466E+38 ~ -1.175494351E-38<br/>10%，1.175494351E-38 ~ 3.402823466E+38<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：参照元信息                                 |
| `DOUBLE`、`DOUBLE UNSIGNED`                                             | `Types.DOUBLE`      | 10%，-2.2250738585072014E-308 ~ -1.7976931348623157E+308<br/>10%，2.2250738585072014E-308 ~ 1.7976931348623157E+308<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：参照元信息 |
| `DATE`                                                                 | `Types.DATE`        | 取值范围：1000-01-01 ~ 9999-12-31                                                                                                                                                                                           |
| `DATETIME`                                                             | `Types.TIMESTAMP`   | 最小值：1000-01-01 00:00:00.000000<br/>最大值：9999-12-31 23:59:59.999999<br/>时间精度：0 ~ 6位，默认 3 位                                                                                                                               |
| `TIMESTAMP`                                                            | `Types.TIMESTAMP`   | 最小值：1970-01-01 00:00:01.000000<br/>最大值：2038-01-19 03:14:07.999999<br/>时间精度：0 ~ 6位，默认 3 位                                                                                                                               |
| `TIME`                                                                 | `Types.VARCHAR`     | 最小值：-838:59:59.000000<br/>最大值：838:59:59.000000<br/>时间精度：0 ~ 6位，默认 3 位                                                                                                                                                  |
| `YEAR`                                                                 | `Types.INTEGER`     | 10%，0<br/>90%，1901 ~ 2155                                                                                                                                                                                              |
| `CHAR`、`VARCHAR`<br/>`TINYTEXT`、`TEXT`<br/>`MEDIUMTEXT`、`LONGTEXT`     | `Types.LONGVARCHAR` | 长度范围：1 ~ 1000，默认 10                                                                                                                                                                                                    |
| `BINARY`、`VARBINARY`<br/>`MEDIUMBLOB`、`TINYBLOB`<br/>`BLOB`、`LONGBLOB` | `Types.VARBINARY`   | 长度范围：0 ~ 4096，默认 10                                                                                                                                                                                                    |
| `ENUM`、`SET`                                                           | `Types.VARCHAR`     | ENUM 类型暂时还不支持识别数据库定义的字典项                                                                                                                                                                                               |
| `GEOMETRY`                                                             | `Types.VARCHAR`     | 写入格式：WKT<br/>坐标系：平面直角坐标系<br/>矩形区域 A 点：50, 0<br/>矩形区域 B 点：0, 1000<br/>点数量：2 ~ 10<br/>点坐标精度：5 位小数                                                                                                                        |
