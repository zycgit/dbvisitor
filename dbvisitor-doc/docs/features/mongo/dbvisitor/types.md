---
id: types
slug: /features/mongo/types
sidebar_position: 80
title: 类型支持
description: MongoDB 类型支持
---

# 类型支持

下表给出常用 BSON 类型的 Java 属性类型建议。Java/JDBC 类型见 [Java/JDBC 类型关系](../../../guides/types/java-jdbc.md)。

## 类型映射

向量字段的用法见[向量类型](#vector-types)。

| BSON 类型 | Java 类型 | 说明 |
| --- | --- | --- |
| BSON string | String | 按字段名或实体属性映射读取文本。 |
| BSON int32 | Integer | 有符号 32 位整数。 |
| BSON int64 | Long | 有符号 64 位整数。 |
| BSON double | Double | 64 位浮点数。 |
| BSON boolean | Boolean | 使用布尔语义，不作为普通整数属性。 |
| BSON date | Instant / java.sql.Timestamp | 需要保留时间点时使用；仅日期业务才选择 java.sql.Date。 |
| BSON document | Map / Bean | 配置方式见 [JSON 字段映射](../../../guides/core/mapping/json-field.md)。 |
| BSON array | List | 配置方式见 [JSON 字段映射](../../../guides/core/mapping/json-field.md)。 |
| BSON string | Enum | 保存枚举名称；指定具体枚举类，并保证字段值与枚举常量一致。 |
| BSON binary | byte[] | 二进制内容；不经字符编码转换。 |
| BSON ObjectId | String | 实体中保存十六进制字符串；命令参数使用 `ObjectId(?)`。 |

BSON date 存储时间点，而 `java.sql.Date` 目标只表达日期。需要保留时刻时使用 `Timestamp` 或 `Instant`。文档与数组内容应分别符合所配置的对象或列表映射。

## 向量类型 {#vector-types}

向量字段映射与读写见[向量操作](vectors.md#vector-mapping)。

## 示例：绑定布尔值并按字段读取

```java
jdbcTemplate.execute("use test");
jdbcTemplate.execute("db.createCollection('type_example')");
jdbcTemplate.executeUpdate("test.type_example.insert({id: ?, enabled: ?})",
        new Object[] { 1, true });
Boolean enabled = jdbcTemplate.queryForObject(
        "test.type_example.find({id: ?}, {enabled: 1})",
        new Object[] { 1 }, (rs, rowNum) -> rs.getBoolean("enabled"));
```

## 使用限制

- 默认数值映射不保证 `BigInteger` 和精确小数的无损往返。
- 第一列是文档元数据；业务字段按字段名、`RowMapper` 或实体读取。

## 数组类型 {#array-values}

适配器已将 BSON 数组接入 JDBC ARRAY，可绑定并读取同类型的 Integer[]、Float[]、String[]。包含嵌套对象的数组可使用[JSON 映射](../../../guides/core/mapping/json-field.md)。

先在 test 库中准备空的 array_example 集合：

```java
import java.sql.Types;
import net.hasor.dbvisitor.types.SqlArg;

Integer[] values = { 10, 20, 30 };
jdbc.executeUpdate("test.array_example.insert({id: ?, int_array: ?})",
        new Object[] { 1, SqlArg.valueOf(values, Types.ARRAY) });
Integer[] loaded = jdbc.queryForObject(
        "test.array_example.find({id: ?}, {_id: 0, int_array: 1})",
        new Object[] { 1 }, Integer[].class);
```
