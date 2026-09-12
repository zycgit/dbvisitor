---
id: types
sidebar_position: 1
title: 类型支持
description: Elasticsearch 类型支持
---

# 类型支持

下表给出常用 Elasticsearch 字段的 Java 属性类型建议。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)。

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
| object | Map / List / Bean | 配置方式见 [JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| keyword | Enum | 保存枚举名称；指定具体枚举类，并保证字段值与枚举常量一致。 |

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
- 数组和对象使用 JSON 映射，不使用 JDBC `ARRAY`。
- 业务字段按字段名、`RowMapper` 或实体读取；前两列是 `_ID`、`_DOC`。
- 适用于 ES6 和 ES7 适配器。
