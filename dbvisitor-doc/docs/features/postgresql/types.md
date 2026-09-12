---
id: types
sidebar_position: 1
title: 类型支持
description: PostgreSQL 类型支持
---

# 类型支持

下表给出常用字段的 Java 属性类型建议；可空字段使用包装类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)；枚举见[枚举映射](../../guides/types/enum-handler.md)。

## 类型映射

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| SMALLINT | Short | 覆盖此有符号整数类型的范围。 |
| INTEGER | Integer | 覆盖此有符号整数类型的范围。 |
| BIGINT | Long | 覆盖此有符号整数类型的范围。 |
| REAL | Float | 近似数值；不用于要求十进制精确计算的金额。 |
| DOUBLE PRECISION | Double | 近似数值；不用于要求十进制精确计算的金额。 |
| DECIMAL(10, 2) | BigDecimal | 保留十进制数值；写入受列精度与小数位限制。 |
| NUMERIC(20) | BigInteger / BigDecimal | 无小数的整数业务可用 BigInteger；统一十进制模型可用 BigDecimal。 |
| BIT(1) | Boolean | 仅适合把单个位值当作业务标志；PostgreSQL 的 `BIT` 是位串类型，不是 `BOOLEAN` 的同义类型。 |
| BOOLEAN | Boolean | 使用布尔语义，不作为普通整数属性。 |
| CHAR(1) | String | 文本内容；字符长度由列定义约束。 |
| VARCHAR(255) | String | 文本内容；字符长度由列定义约束。 |
| BYTEA | byte[] | 二进制内容；不经字符编码转换。 |
| DATE | java.sql.Date | 仅日期；不用于保留完整时间戳。 |
| TIME | java.sql.Time | 仅时间；文本存储时须使用可解析的时间格式。 |
| TIMESTAMP | java.sql.Timestamp | 保留日期和时间，精度以数据库列定义为准。 |
| VARCHAR(2000) | Map / List / Bean | 保存 JSON 文本时，实体属性配置见[JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| JSONB | Map / List / Bean | 实体属性配置见 [JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| INTEGER[] | Integer[] | 数组元素与数据库元素类型一致，按需显式配置处理器。 |
| VARCHAR[] | String[] | 数组元素与数据库元素类型一致，按需显式配置处理器。 |
| REAL[] | Float[] | 数组元素与数据库元素类型一致，按需显式配置处理器。 |
| vector(n) | List&lt;Float> | pgvector 字段；配置方式见[向量数据操作](./vectors.mdx)，向量长度与字段维度一致。 |

## 示例：原生数组

```sql
CREATE TABLE array_example (id INTEGER PRIMARY KEY, values_col INTEGER[]);
```


```java
import java.sql.Types;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;

Integer[] values = { 10, 20, 30 };
jdbcTemplate.executeUpdate("INSERT INTO array_example (id, values_col) VALUES (?, ?)",
        new Object[] { 1, new SqlArg(values, Types.ARRAY, new ArrayTypeHandler()) });
Integer[] loaded = jdbcTemplate.queryForObject(
        "SELECT values_col FROM array_example WHERE id = ?",
        new Object[] { 1 }, Integer[].class);
```
