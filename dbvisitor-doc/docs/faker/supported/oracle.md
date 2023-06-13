---
id: oracle
sidebar_position: 4
title: Oracle
description: faker 对于 Oracle 的数值生成支持特性
---
# Oracle 支持特性

引用资料
- https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/data-types.html
- 本文会涵盖 Oracle 到 jdbc 的类型映射、不同策略数值生成范围描述

## 支持的类型

| Oracle Type                                 | 支持性 |
|---------------------------------------------|-----|
| `BINARY_FLOAT`、`BINARY_DOUBLE`              | 支持  |
| `NUMBER`、`FLOAT`                            | 支持  |
| `CHAR`、`VARCHAR`<br/>`VARCHAR2`、`NVARCHAR2` | 支持  |
| `CLOB`、`NCLOB`                              | 支持  |
| `LONG`                                      | 支持  |
| `BLOB`、`RAW`、`LONG RAW`                     | 支持  |
| `ROWID`、`UROWID`                            | 支持  |
| `DATE`                                      | 支持  |
| `TIMESTAMP`                                 | 支持  |
| `TIMESTAMP WITH LOCAL TIME ZONE`            | 支持  |
| `TIMESTAMP WITH TIME ZONE`                  | 支持  |
| `INTERVAL YEAR TO MONTH`                    | 不支持 |
| `INTERVAL DAY TO SECOND`                    | 不支持 |
| `XMLTYPE`                                   | 不支持 |
| `BFILE`                                     | 不支持 |

## 默认策略(widely)
- 策略名：`widely`
- 默认策略：`true`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/oracle-widely.tpc`

| Oracle Type                                                     | JDBC Type                       | 描述                                                                                    |
|-----------------------------------------------------------------|---------------------------------|---------------------------------------------------------------------------------------|
| `BINARY_FLOAT`、`BINARY_DOUBLE`<br/>`NUMBER`、`FLOAT`             | `Types.DOUBLE`                  | 取值范围：0 ~ 9999999.999<br/>小数精度：参考元信息                                                   |
| `CHAR`、`VARCHAR`<br/>`VARCHAR2`、`NVARCHAR2`                     | `Types.VARCHAR`                 | 长度范围：1 ~ 250，默认 10<br/>不会出现空字符串                                                       |
| `CLOB`、`NCLOB`                                                  | `Types.VARCHAR`                 | 长度范围：1 ~ 250，默认 10<br/>不会出现空字符串<br/>不参与 `DeleteWhere`、`UpdateWhere`                   |
| `LONG`                                                          | `Types.VARCHAR`                 | 长度范围：1 ~ 250，默认 10<br/>不会出现空字符串<br/>不参与 `DeleteWhere`                                 |
| `BLOB`、`RAW`、`LONG RAW`                                         | `Types.BLOB`                    | 长度范围：0 ~ 16，默认 4<br/>不参与 `DeleteWhere`、`UpdateWhere`                                  |
| `ROWID`、`UROWID`                                                | `Types.VARCHAR`                 | 不参与 `UpdateSet`、`Insert`                                                              |
| `DATE`                                                          | `Types.DATE`                    | 取值范围：2000-01-01 ~ 2030-12-31                                                          |
| `TIMESTAMP`                                                     | `Types.TIMESTAMP`               | 最小值：2000-01-01 00:00:00<br/>最大值：2030-12-31 23:59:59<br/>时间精度：0 ~ 9位，默认 3位             |
| `TIMESTAMP WITH LOCAL TIME ZONE`<br/>`TIMESTAMP WITH TIME ZONE` | `Types.TIMESTAMP_WITH_TIMEZONE` | 最小值：2000-01-01 00:00:00-08:00<br/>最大值：2030-12-31 23:59:59+08:00<br/>时间精度：0 ~ 9位，默认 3位 |

## 极值策略(extreme)
- 策略名：`extreme`
- 默认策略：`false`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/oracle-extreme.tpc`

| Oracle Type                                                     | JDBC Type                       | 描述                                                                                                                                                                                                             |
|-----------------------------------------------------------------|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BINARY_FLOAT`、`FLOAT`                                          | `Types.FLOAT`                   | 10%，-1.17549E-38 ~ -3.40282E+38<br/>10%，1.17549E-38 ~ 3.40282E+38<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：参照元信息                                         |
| `BINARY_DOUBLE`                                                 | `Types.DOUBLE`                  | 10%，-2.22507485850720E-308 ~ -1.79769313486231E+308<br/>10%，2.22507485850720E-308 ~ 1.79769313486231E+308<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：参照元信息 |
| `NUMBER`                                                        | `Types.DECIMAL`                 | precision 最大：40<br/>scale 最大：20<br/>参考元信息                                                                                                                                                                      |
| `CHAR`、`NCHAR`<br/>`VARCHAR2`、`NVARCHAR2`                       | `Types.VARCHAR`                 | 长度范围：1 ~ 1000，默认 10<br/>不会出现空字符串                                                                                                                                                                               |
| `CLOB`、`NCLOB`                                                  | `Types.VARCHAR`                 | 长度范围：1 ~ 1000，默认 10<br/>不会出现空字符串<br/>不参与 `DeleteWhere`、`UpdateWhere`                                                                                                                                           |
| `LONG`                                                          | `Types.VARCHAR`                 | 长度范围：1 ~ 1000，默认 10<br/>不会出现空字符串<br/>不参与 `DeleteWhere`                                                                                                                                                         |
| `BLOB`、`RAW`、`LONG RAW`                                         | `Types.BLOB`                    | 长度范围：0 ~ 4096，默认 10<br/>不参与 `DeleteWhere`、`UpdateWhere`                                                                                                                                                        |
| `ROWID`、`UROWID`                                                | `Types.VARCHAR`                 | 不参与 `UpdateSet`、`Insert`                                                                                                                                                                                       |
| `DATE`                                                          | `Types.DATE`                    | 取值范围：0001-01-01 ~ 9999-12-31                                                                                                                                                                                   |
| `TIMESTAMP`                                                     | `Types.TIMESTAMP`               | 最小值：0001-01-01 00:00:00<br/>最大值：9999-12-31 23:59:59<br/>时间精度：0 ~ 9，默认 3                                                                                                                                        |
| `TIMESTAMP WITH LOCAL TIME ZONE`<br/>`TIMESTAMP WITH TIME ZONE` | `Types.TIMESTAMP_WITH_TIMEZONE` | 最小值：0001-01-01 00:00:00-14:00<br/>最大值：9999-12-31 23:59:59+14:00<br/>时间精度：0 ~ 9，默认 3                                                                                                                            |
