---
id: types
slug: /features/elastic/types
sidebar_position: 80
title: 类型支持
description: Elasticsearch 类型支持
---

# 类型支持

下表给出常用 Elasticsearch 字段的 Java 属性类型建议。Java/JDBC 类型见 [Java/JDBC 类型关系](../../../guides/types/java-jdbc.md)。

## 类型映射

| Elasticsearch 字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| keyword | String | 按字段名或实体属性映射读取文本。 |
| text | String | 按字段名或实体属性映射读取文本。 |
| integer | Integer | 有符号 32 位整数。 |
| long | Long | 有符号 64 位整数。 |
| double | Double | 64 位浮点数。 |
| boolean | Boolean | 使用布尔语义，不作为普通整数属性。 |
| date | java.sql.Date（仅日期业务） | 不保留时间点的毫秒精度。 |
| object | Map / List / Bean | 配置方式见 [JSON 字段映射](../../../guides/core/mapping/json-field.md)。 |
| keyword | Enum | 保存枚举名称；指定具体枚举类，并保证字段值与枚举常量一致。 |

## 文本长度 {#text-length}

`text`、`keyword` 不提供 `VARCHAR(100)` 式的超长写入校验。dbVisitor 也不会替这些字段拒绝超过 100 个字符的值；业务长度限制应在写入前校验。

`keyword` 的 `ignore_above` 控制是否索引，不是拒绝写入。例如设置为 `100` 后，101 个字符的值仍保留在 `_source` 中，但该字段不会参与精确检索和聚合。[ignore_above 说明](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/ignore-above.html)

## 示例：绑定布尔值并按字段读取

```java
jdbcTemplate.execute("PUT /type_example");
jdbcTemplate.executeUpdate("POST /type_example/_doc {\"id\": ?, \"enabled\": ?}",
        new Object[] { 1, true });
jdbcTemplate.execute("POST /type_example/_refresh");
Boolean enabled = jdbcTemplate.queryForObject(
        "POST /type_example/_search {\"query\": {\"term\": {\"id\": ?}}}",
        new Object[] { 1 }, (rs, rowNum) -> rs.getBoolean("enabled"));
```

## 使用限制

- 日期默认映射不能保证保留毫秒精度。
- `BigInteger`、精确小数和任意二进制内容不保证无损往返。
- 业务字段按字段名、`RowMapper` 或实体读取；前两列是 `_ID`、`_DOC`。
- 适用于 ES6 和 ES7 适配器。

## 数组类型 {#array-values}

Elasticsearch 没有单独的数组类型：integer、float、keyword 等字段可以保存多个值。适配器支持通过 JDBC ARRAY 绑定和读取这些值；Java 属性可用 Integer[]、Float[]、String[]，元素须符合字段 Mapping。

先准备空的 array_example 索引，id 和 int_array 均映射为 integer：

```java
import java.sql.Types;
import net.hasor.dbvisitor.types.SqlArg;

Integer[] values = { 10, 20, 30 };
jdbc.executeUpdate("POST /array_example/_doc {\"id\": ?, \"int_array\": ?}",
        new Object[] { 1, SqlArg.valueOf(values, Types.ARRAY) });
jdbc.execute("POST /array_example/_refresh");
Integer[] loaded = jdbc.queryForObject(
        "POST /array_example/_search {\"_source\": [\"int_array\"], "
                + "\"query\": {\"term\": {\"id\": ?}}}",
        new Object[] { 1 }, Integer[].class);
```
