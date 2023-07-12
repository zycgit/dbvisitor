---
id: postgresql
sidebar_position: 2
title: PostgreSQL
description: faker 对于 PostgreSQL 的数值生成支持特性
---
# PostgreSQL 支持特性

引用资料
- https://www.postgresql.org/docs/13/datatype.html
- 本文会涵盖 PostgreSQL 到 jdbc 的类型映射、不同策略数值生成范围描述

:::info
PostgreSQL 的每一个类型都可被对应为一个数组类型。Faker 对于相应的数组类型支持性也同下表
- 数组元素数量：0 ~ 10
:::

## 支持的类型

| PostgreSQL Type                                                  | 支持性 |
|------------------------------------------------------------------|-----|
| `BOOL`                                                           | 支持  |
| `SMALLSERIAL`、`INT2`                                             | 支持  |
| `SERIAL`、`INT4`                                                  | 支持  |
| `OID`                                                            | 支持  |
| `BIGSERIAL`、`INT8`                                               | 支持  |
| `FLOAT4`、`FLOAT8`                                                | 支持  |
| `NUMERIC`                                                        | 支持  |
| `MONEY`                                                          | 支持  |
| `NAME`、`BPCHAR`、`TEXT`、`VARCHAR`                                 | 支持  |
| `UUID`                                                           | 支持  |
| `DATE`                                                           | 支持  |
| `TIME`                                                           | 支持  |
| `TIMETZ`                                                         | 支持  |
| `INTERVAL`                                                       | 支持  |
| `TIMESTAMP`                                                      | 支持  |
| `TIMESTAMPTZ`                                                    | 支持  |
| `BIT`、`VARBIT`                                                   | 支持  |
| `BYTEA`                                                          | 支持  |
| `POINT`、`LINE`、`LSEG`、`BOX`、`PATH`、`POLYGON`、`CIRCLE`、`GEOMETRY` | 支持  |
| `BYTEA`                                                          | 支持  |
| `BYTEA`                                                          | 支持  |
| `BYTEA`                                                          | 支持  |
| `BYTEA`                                                          | 支持  |
| `BYTEA`                                                          | 支持  |
| `JSON`                                                           | 不支持 |
| `JSONB`                                                          | 不支持 |
| `XML`                                                            | 不支持 |
| `CIDR`                                                           | 不支持 |
| `INET`                                                           | 不支持 |
| `MACADDR`                                                        | 不支持 |
| `MACADDR8`                                                       | 不支持 |
| `INT4RANGE`                                                      | 不支持 |
| `INT8RANGE`                                                      | 不支持 |
| `NUMRANGE`                                                       | 不支持 |
| `TSRANGE`                                                        | 不支持 |
| `TSTZRANGE`                                                      | 不支持 |
| `DATERANGE`                                                      | 不支持 |
| `TSVECTOR`                                                       | 不支持 |
| `TSQUERY`                                                        | 不支持 |
| `PG_LSN`                                                         | 不支持 |
| `TXID_SNAPSHOT`                                                  | 不支持 |

## 默认策略(widely)
- 策略名：`widely`
- 默认策略：`true`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/postgresql-widely.tpc`

| PostgreSQL Type                                                          | JDBC Type                       | 描述                                                                                                                                    |
|--------------------------------------------------------------------------|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `BOOL`                                                                   | `Types.BOOLEAN`                 | 取值范围：true, false                                                                                                                      |
| `SMALLSERIAL`、`INT2`                                                     | `Types.TINYINT`                 | 取值范围：0 ~ 100                                                                                                                          |
| `SERIAL`、`INT4`                                                          | `Types.INTEGER`                 | 取值范围：0 ~ 99999999                                                                                                                     |
| `OID`                                                                    | `Types.BIGINT`                  | 取值范围：0 ~ 100000000                                                                                                                    |
| `BIGSERIAL`、`INT8`                                                       | `Types.BIGINT`                  | 取值范围：0 ~ 9999999999                                                                                                                   |
| `FLOAT4`、`FLOAT8`                                                        | `Types.DOUBLE`                  | 取值范围：0 ~ 9999999.999<br/>小数精度：最大 3 位                                                                                                  |
| `NUMERIC`                                                                | `Types.DECIMAL`                 | 参照元信息决定 `precision`、`scale`<br/>precision值：默认为 6<br/>scale值：默认为 2                                                                     |
| `MONEY`                                                                  | `Types.DECIMAL`                 | 取值范围：0 ~ 99999.999<br/>小数精度：固定 3 位                                                                                                    |
| `NAME`、`BPCHAR`<br/>`TEXT`、`VARCHAR`                                     | `Types.VARCHAR`                 | 长度范围：0 ~ 250，默认 10                                                                                                                    |
| `UUID`                                                                   | `Types.OTHER`                   | 长度为 36 的 UUID                                                                                                                         |
| `DATE`                                                                   | `Types.DATE`                    | 取值范围：2000-01-01 ~ 2030-12-31                                                                                                          |
| `TIME`                                                                   | `Types.TIME`                    | 最小值：00:00:00.000000<br/>最大值：23:59:59.999999<br/>时间精度：0 ~ 6，默认 3                                                                       |
| `TIMETZ`                                                                 | `Types.TIME_WITH_TIMEZONE`      | 最小值：00:00:00.000000-08:00<br/>最大值：23:59:59.999999+08:00<br/>时间精度：0 ~ 6，默认 3                                                           |
| `INTERVAL`                                                               | `Types.OTHER`                   | 最小值：2000-01-01 00:00:00<br/>最大值：2030-12-31 23:59:59<br/>时间格式：ISO8601                                                                  |
| `TIMESTAMP`                                                              | `Types.TIMESTAMP`               | 最小值：2000-01-01 00:00:00.000000<br/>最大值：2030-12-31 23:59:59.999999<br/>时间精度：0 ~ 6，默认 3                                                 |
| `TIMESTAMPTZ`                                                            | `Types.TIMESTAMP_WITH_TIMEZONE` | 最小值：2000-01-01 00:00:00.000000-08:00<br/>最大值：2030-12-31 23:59:59.999999+08:00<br/>时间精度：0 ~ 6，默认 3                                     |
| `BIT`、`VARBIT`                                                           | `Types.VARCHAR`                 | 取值范围：0,1<br/>长度范围：1 ~ 24，默认 8                                                                                                         |
| `BYTEA`                                                                  | `Types.VARBINARY`               | 长度范围：0 ~ 16，默认 4                                                                                                                      |
| `POINT`、`LINE`、`LSEG`<br/>`BOX`、`PATH`、`POLYGON`<br/>`CIRCLE`、`GEOMETRY` | `Types.VARCHAR`                 | 写入格式：WKT<br/>坐标系：平面直角坐标系<br/>矩形区域 A 点：50, 0<br/>矩形区域 B 点：0, 1000<br/>点数量：2 ~ 10<br/>点坐标精度：5 位小数<br/>不参与 `DeleteWhere` 与 `UpdateWhere` |

## 极值策略(extreme)
- 策略名：`extreme`
- 默认策略：`false`
- 同名策略优先级：`1`
- 策略脚本：`META-INF/faker-default-dbtpc/postgresql-extreme.tpc`

| PostgreSQL Type                                                          | JDBC Type                       | 描述                                                                                                                                                      |
|--------------------------------------------------------------------------|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BOOL`                                                                   | `Types.BOOLEAN`                 | 取值范围：true, false                                                                                                                                        |
| `SMALLSERIAL`                                                            | `Types.TINYINT`                 | 取值范围：0 ~ 32767                                                                                                                                          |
| `INT2`                                                                   | `Types.INTEGER`                 | 取值范围：-32768 ~ 32767                                                                                                                                     |
| `SERIAL`                                                                 | `Types.INTEGER`                 | 取值范围：0 ~ 2147483647                                                                                                                                     |
| `INT4`                                                                   | `Types.INTEGER`                 | 取值范围：-2147483648 ~ 2147483647                                                                                                                           |
| `BIGSERIAL`                                                              | `Types.BIGINT`                  | 取值范围：0 ~ 9223372036854775807                                                                                                                            |
| `INT8`                                                                   | `Types.BIGINT`                  | 取值范围：-9223372036854775808 ~ 9223372036854775807                                                                                                         |
| `OID`                                                                    | `Types.BIGINT`                  | 取值范围：0 ~ 100000000                                                                                                                                      |
| `FLOAT4`                                                                 | `Types.DOUBLE`                  | 10%，1e-37 ~ 1e+37<br/>10%，-1e-37 ~ -1e+37<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：0 ~ 6，默认 3     |
| `FLOAT8`                                                                 | `Types.DOUBLE`                  | 10%，1e-307 ~ 1e+308<br/>10%，-1e-307 ~ -1e-308<br/>30%，-999999999.999999999 ~ 999999999.999999999<br/>30%，-0.999999999 ~ 0.999999999<br/>精度范围：0 ~ 6，默认 3 |
| `NUMERIC`                                                                | `Types.DECIMAL`                 | 参照元信息决定 `precision`、`scale`<br/>precision值：默认为 6<br/>scale值：默认为 2                                                                                       |
| `MONEY`                                                                  | `Types.DECIMAL`                 | 取值范围：-92233720368547758.08 ~ 92233720368547758.07<br/>小数精度：固定 2 位                                                                                       |
| `NAME`                                                                   | `Types.VARCHAR`                 | 长度范围：1 ~ 100，默认 10                                                                                                                                      |
| `BPCHAR`<br/>`TEXT`、`VARCHAR`                                            | `Types.VARCHAR`                 | 长度范围：0 ~ 1000，默认 10                                                                                                                                     |
| `UUID`                                                                   | `Types.OTHER`                   | 长度为 36 的 UUID                                                                                                                                           |
| `DATE`                                                                   | `Types.DATE`                    | 取值范围：0000-01-01 ~ 9999-12-31                                                                                                                            |
| `TIME`                                                                   | `Types.TIME`                    | 最小值：00:00:00.000000<br/>最大值：23:59:59.999999<br/>时间精度：0 ~ 6，默认 3                                                                                         |
| `TIMETZ`                                                                 | `Types.TIME_WITH_TIMEZONE`      | 最小值：00:00:00.000000-14:00<br/>最大值：23:59:59.999999+14:00<br/>时间精度：0 ~ 6，默认 3                                                                             |
| `INTERVAL`                                                               | `Types.OTHER`                   | 最小值：0000-01-01 00:00:00<br/>最大值：9999-12-31 23:59:59<br/>时间格式：ISO8601                                                                                    |
| `TIMESTAMP`                                                              | `Types.TIMESTAMP`               | 最小值：0000-01-01 00:00:00.000000<br/>最大值：9999-12-31 23:59:59.999999<br/>时间精度：0 ~ 6，默认 3                                                                   |
| `TIMESTAMPTZ`                                                            | `Types.TIMESTAMP_WITH_TIMEZONE` | 最小值：0000-01-01 00:00:00.000000-14:00<br/>最大值：9999-12-31 23:59:59.999999+14:00<br/>时间精度：0 ~ 6，默认 3                                                       |
| `BIT`、`VARBIT`                                                           | `Types.VARCHAR`                 | 取值范围：0,1<br/>长度范围：1 ~ 512，默认 4                                                                                                                          |
| `BYTEA`                                                                  | `Types.VARBINARY`               | 长度范围：0 ~ 4096，默认 10                                                                                                                                     |
| `POINT`、`LINE`、`LSEG`<br/>`BOX`、`PATH`、`POLYGON`<br/>`CIRCLE`、`GEOMETRY` | `Types.VARCHAR`                 | 写入格式：WKT<br/>坐标系：平面直角坐标系<br/>矩形区域 A 点：50, 0<br/>矩形区域 B 点：0, 1000<br/>点数量：2 ~ 10<br/>点坐标精度：5 位小数<br/>不参与 `DeleteWhere` 与 `UpdateWhere`                   |
