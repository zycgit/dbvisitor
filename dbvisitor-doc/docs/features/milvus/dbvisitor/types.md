---
id: types
slug: /features/milvus/types
sidebar_position: 1
title: 类型支持
description: Milvus 类型支持
---

# 类型支持

下表给出 Milvus 字段的 Java 属性类型建议。向量类型和维度以集合 schema 为准。Java/JDBC 类型见 [Java/JDBC 类型关系](../../../guides/types/java-jdbc.md)。

## 类型映射

表中向量行同时列出 JDBC 读写形式。

| Milvus 字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| BOOL | Boolean | 使用布尔语义，不作为普通整数属性。 |
| INT8 | Byte | 有符号 8 位整数。 |
| INT16 | Short | 有符号 16 位整数。 |
| INT32 | Integer | 有符号 32 位整数。 |
| INT64 | Long | 有符号 64 位整数。 |
| FLOAT | Float | 32 位浮点数。 |
| DOUBLE | Double | 64 位浮点数。 |
| VARCHAR(max_length) | String | `max_length` 按 UTF-8 字节数限制字段内容。 |
| JSON | Map / List / Bean | 实体属性配置见 [JSON 字段映射](../../../guides/core/mapping/json-field.md)；直接 `getObject()` 返回 `JsonElement`。 |
| ARRAY&lt;BOOL>(max_capacity) | Boolean[] | 数组元素为布尔值。 |
| ARRAY&lt;INT8>(max_capacity) | Byte[] | 数组元素为有符号 8 位整数。 |
| ARRAY&lt;INT16>(max_capacity) | Short[] | 数组元素为有符号 16 位整数。 |
| ARRAY&lt;INT32>(max_capacity) | Integer[] | 数组元素为有符号 32 位整数。 |
| ARRAY&lt;INT64>(max_capacity) | Long[] | 数组元素为有符号 64 位整数。 |
| ARRAY&lt;FLOAT>(max_capacity) | Float[] | 数组元素为 32 位浮点数。 |
| ARRAY&lt;DOUBLE>(max_capacity) | Double[] | 数组元素为 64 位浮点数。 |
| ARRAY&lt;VARCHAR(max_length)>(max_capacity) | String[] | `max_length` 限制单个字符串，`max_capacity` 限制元素数量。 |
| FLOAT_VECTOR(n) | List&lt;Float> | `getObject()` 返回 `List<Float>`。写入接受数值 List 或一维数值基本类型数组，各元素转换为 Float；向量长度须为 n。 |
| BINARY_VECTOR(n) | byte[] | `getBytes()` 返回按位打包的字节，长度为 n/8。写入接受 `byte[]`、`ByteBuffer` 或字节值 List；它不是任意长度 BLOB。 |
| FLOAT16_VECTOR(n) | byte[] | 查询返回每维 2 字节的小端半精度编码；写入也接受数值 List 或数值基本类型数组。 |
| BFLOAT16_VECTOR(n) | byte[] | 查询返回每维 2 字节的小端 BFloat16 编码；写入也接受数值 List 或数值基本类型数组。 |
| INT8_VECTOR(n) | byte[] | `getBytes()` 返回每维一个有符号字节。写入还接受 `ByteBuffer`、数值 List 或一维数值基本类型数组；元素必须是 -128 至 127 的整数，长度须为 n。 |
| SPARSE_FLOAT_VECTOR | SortedMap&lt;Long, Float> | `getObject()` 按维度索引升序返回。写入接受非空 `Map<Number, Number>`；键为维度索引，值为有限浮点权重。 |

`byte[]` 的含义由目标字段 schema 决定：用于 `FLOAT_VECTOR` 时是逐元素转换的有符号数值序列；用于 `BINARY_VECTOR`、`FLOAT16_VECTOR`、`BFLOAT16_VECTOR` 时是打包编码；用于 `INT8_VECTOR` 时每个字节是一个向量分量。驱动不会只根据 Java 参数类型猜测向量种类。

## 示例：数组、JSON 与浮点向量

```sql
CREATE TABLE type_example (
    id INT64 PRIMARY KEY,
    profile JSON NULL,
    tags `ARRAY<INT32>`(10) NULL,
    embedding FLOAT_VECTOR(3)
) WITH (consistency_level=Strong);
CREATE INDEX type_embedding ON type_example(embedding) USING AUTOINDEX WITH (metric_type=L2);
LOAD TABLE type_example;
```

以已建立的 `Connection conn` 为例：
```java
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

Array tags = conn.createArrayOf("INTEGER", new Integer[] { 1, 2 });
try (PreparedStatement ps = conn.prepareStatement(
        "INSERT INTO type_example (id, profile, tags, embedding) VALUES (?, ?, ?, ?)")) {
    ps.setLong(1, 1L);
    ps.setString(2, "{\"city\":\"Hangzhou\"}");
    ps.setArray(3, tags);
    ps.setObject(4, List.of(0.1f, 0.2f, 0.3f));
    ps.executeUpdate();
} finally {
    tags.free();
}
try (PreparedStatement ps = conn.prepareStatement(
        "SELECT profile, tags, embedding FROM type_example WHERE id = ?")) {
    ps.setLong(1, 1L);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            Object profile = rs.getObject("profile");
            Array loadedTags = rs.getArray("tags");
            try {
                Object values = loadedTags.getArray();
            } finally {
                loadedTags.free();
            }
            Object embedding = rs.getObject("embedding");
        }
    }
}
```


各向量类型的编码及维度限制见 [SQL 类型](../types/fields.md)。BinaryVector 不是通用 BLOB 列。

## 使用限制

- `BigDecimal` 写入 `DOUBLE` 会损失精度；`INT64` 只覆盖有符号 64 位整数。
- 日期时间以 `VARCHAR` 或 `INT64` 保存，Milvus 没有原生日期类型。
- Milvus 没有通用 `BLOB`／`VARBINARY`；`BinaryVector` 只保存定维位向量。

## 实体映射

实体须匹配已有集合，包括单字段主键。字段改名和类型转换不会创建字段，也不会改变 Milvus 字段类型。

Java 枚举使用 VARCHAR 保存名称或字符串代码，或使用 INT32 保存数值代码，配置见[枚举映射](../../../guides/types/enum-handler.md)。Milvus 没有 ENUM 字段类型。

`ARRAY<INT32>` 可映射为 Integer[]，元素类型、容量和可空约束仍须匹配。Bean、Map 属性保存为 JSON 的配置见[JSON 字段映射](../../../guides/core/mapping/json-field.md)；存入 VARCHAR 的 JSON 文本不具备原生 JSON 过滤能力。
