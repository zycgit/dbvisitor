---
id: types
sidebar_position: 1
title: 类型支持
description: ClickHouse 类型支持
---

# 类型支持

下表给出常用字段的 Java 属性类型建议；可空字段使用包装类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)；枚举见[枚举映射](../../guides/types/enum-handler.md)。

## 类型映射

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| Nullable(Int16) | Short | 覆盖此有符号整数类型的范围。 |
| Nullable(Int32) | Integer | 覆盖此有符号整数类型的范围。 |
| Nullable(Int64) | Long | 覆盖此有符号整数类型的范围。 |
| Nullable(Float32) | Float | 近似数值；不用于要求十进制精确计算的金额。 |
| Nullable(Float64) | Double | 近似数值；不用于要求十进制精确计算的金额。 |
| Nullable(Decimal(10, 2)) | BigDecimal | 保留十进制数值；写入受列精度与小数位限制。 |
| Nullable(Decimal(20, 0)) | BigInteger / BigDecimal | 无小数的整数业务可用 BigInteger；统一十进制模型可用 BigDecimal。 |
| Nullable(Bool) | Boolean | 使用布尔语义，不作为普通整数属性。 |
| Nullable(String) | String | 文本内容；字符长度由列定义约束。 |
| Nullable(Date) | java.sql.Date | 仅日期；不用于保留完整时间戳。 |
| Nullable(String) | java.sql.Time | 仅时间；字段内容须使用可解析的时间格式。 |
| Nullable(DateTime64(3)) | java.sql.Timestamp | 保留日期和时间，精度以数据库列定义为准。 |
| Nullable(String) | Map / List / Bean | 保存 JSON 文本，实体属性配置见[JSON 字段映射](../../guides/core/mapping/json-field.md)。 |

## 使用限制

- `DateTime64(3)` 的时区和精度由字段定义及 ClickHouse JDBC 驱动决定。
- `String` 文本映射不适合保存任意二进制内容。
