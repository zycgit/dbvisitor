---
id: types
sidebar_position: 1
title: 类型支持
description: SQL Server 类型支持
---

# 类型支持

下表给出常用字段的 Java 属性类型建议；可空字段使用包装类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)；枚举见[枚举映射](../../guides/types/enum-handler.md)。

## 类型映射

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| TINYINT | Short | 覆盖 0–255；仅当业务限制为 0–127 时才使用 Byte。 |
| SMALLINT | Short | 覆盖此有符号整数类型的范围。 |
| INT | Integer | 覆盖此有符号整数类型的范围。 |
| BIGINT | Long | 覆盖此有符号整数类型的范围。 |
| REAL | Float | 近似数值；不用于要求十进制精确计算的金额。 |
| FLOAT | Double | 近似数值；不用于要求十进制精确计算的金额。 |
| DECIMAL(10, 2) | BigDecimal | 保留十进制数值；写入受列精度与小数位限制。 |
| DECIMAL(20, 0) | BigInteger / BigDecimal | 无小数的整数业务可用 BigInteger；统一十进制模型可用 BigDecimal。 |
| BIT | Boolean | 使用布尔语义，不作为普通整数属性。 |
| CHAR(1) | String | 文本内容；字符长度由列定义约束。 |
| VARCHAR(255) | String | 文本内容；字符长度由列定义约束。 |
| NVARCHAR(255) | String | 文本内容；字符长度由列定义约束。 |
| VARBINARY(1000) | byte[] | 二进制内容；不经字符编码转换。 |
| VARBINARY(MAX) | byte[] | 二进制内容；不经字符编码转换。 |
| DATE | java.sql.Date | 仅日期；不用于保留完整时间戳。 |
| TIME | java.sql.Time | 仅时间；文本存储时须使用可解析的时间格式。 |
| DATETIME2(3) | java.sql.Timestamp | 保留日期和时间，精度以数据库列定义为准。 |
| NVARCHAR(2000) | Map / List / Bean | 保存 JSON 文本时，实体属性配置见[JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| NVARCHAR(MAX) | Map / List / Bean | 保存 JSON 文本时，实体属性配置见[JSON 字段映射](../../guides/core/mapping/json-field.md)。 |

## 使用限制

- SQL Server `TINYINT` 无符号；负数应使用 `SMALLINT`。
