---
id: types
sidebar_position: 1
title: 类型支持
description: Oracle 类型支持
---

# 类型支持

下表给出常用字段的 Java 属性类型建议；可空字段使用包装类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)；枚举见[枚举映射](../../guides/types/enum-handler.md)。

## 类型映射

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| NUMBER(2) | Byte | 覆盖 -99 至 99。 |
| NUMBER(3) | Short | 覆盖 -999 至 999；Byte 无法覆盖整列范围。 |
| NUMBER(5) | Integer | 覆盖 -99999 至 99999；Short 无法覆盖整列范围。 |
| NUMBER(10) | Long | 覆盖 10 位十进制整数；Integer 无法覆盖整列范围。 |
| NUMBER(19) | BigInteger | 覆盖 19 位十进制整数；Long 无法覆盖整列范围。 |
| NUMBER(p, 0) | BigInteger | 通用整数映射；范围可控时可选更小的整数类型，统一十进制模型也可用 BigDecimal。 |
| NUMBER(p, s) | BigDecimal | 需要小数或统一保留十进制数值时使用。 |
| NUMBER(1) | Boolean | 仅适用于业务约定为 0/1 的标志；普通一位整数建议 Byte。 |
| BINARY_FLOAT | Float | 近似数值；不用于要求十进制精确计算的金额。 |
| BINARY_DOUBLE | Double | 近似数值；不用于要求十进制精确计算的金额。 |
| CHAR(1) | String | 文本内容；字符长度由列定义约束。 |
| VARCHAR2(255) | String | 文本内容；字符长度由列定义约束。 |
| NVARCHAR2(255) | String | 文本内容；字符长度由列定义约束。 |
| RAW(1000) | byte[] | 二进制内容；不经字符编码转换。 |
| BLOB | byte[] | 二进制内容；不经字符编码转换。 |
| DATE | java.sql.Timestamp | 保留日期和时间，精度以数据库列定义为准。 |
| TIMESTAMP | java.sql.Timestamp | 保留日期和时间，精度以数据库列定义为准。 |
| VARCHAR2(2000) | Map / List / Bean | 保存 JSON 文本时，实体属性配置见[JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| CLOB | Map / List / Bean | 保存 JSON 文本时，实体属性配置见 [JSON 字段映射](../../guides/core/mapping/json-field.md)。 |

## 示例：根据表字段声明实体属性

下面是实体中的字段声明，列名通过 `@Column` 指定。处理器根据属性类型及映射配置选择，不需要为这些普通数值属性单独指定 JDBC 类型。

```java
import java.math.BigDecimal;
import net.hasor.dbvisitor.mapping.Column;

// NUMBER(3)
@Column("quantity")
private Short quantity;

// NUMBER(5)
@Column("sort_no")
private Integer sortNo;

// NUMBER(10, 2)
@Column("amount")
private BigDecimal amount;
```

## 使用限制

- 空字符串按 `NULL` 处理。
