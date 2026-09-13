---
id: types
slug: /features/redis/types
sidebar_position: 1
title: 类型支持
description: Redis 类型支持
---

# 类型支持

Redis 按键和原生数据结构组织数据。下表列出常用命令结果对应的 Java 类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../../guides/types/java-jdbc.md)。

## 类型映射

| Redis 数据类型 | Java 类型 | 说明 |
| --- | --- | --- |
| String | String | GET 结果通常读取为 String；格式约定明确时可选 Long、Boolean、java.sql.Date 或 Instant。 |
| String | Map / List / Bean | 保存 JSON 文本时，配置方式见 [JSON 字段映射](../../../guides/core/mapping/json-field.md)。 |
| String | Enum | 保存枚举名称；指定具体枚举类，并保证字段值与枚举常量一致。 |
| String | BigDecimal | 保存十进制文本，按 BigDecimal 读取时保留精度。 |
| String | BigInteger | 保存整数文本，按 BigInteger 读取。 |
| String（二进制内容） | byte[] | SET 接受字节数组；GET 的键参数使用 byte[]，读取原始字节。 |
| Hash | String | HGETALL 的每行键和值均按 String 读取。 |
| List | String | LRANGE 的每个结果行包含一个 String 元素。 |
| Set | String | SMEMBERS 的每个结果行包含一个 String 元素。 |
| Sorted Set | Double | 带分数查询的分值；属于近似数值，不用于要求十进制精确计算的金额。 |

## 二进制内容

文本键按 UTF-8 编码后仍是同一个 Redis 键。读取时绑定字节数组键，选择原始字节结果，避免文本解码损坏数据。

```java
import java.nio.charset.StandardCharsets;

String key = "demo:binary";
byte[] content = new byte[] { 0, (byte) 255, (byte) 128 };
jdbc.executeUpdate("SET ? ?", new Object[] { key, content });
byte[] loaded = jdbc.queryForObject("GET ?",
        new Object[] { key.getBytes(StandardCharsets.UTF_8) }, byte[].class);
```

普通字符串键的 GET 保持文本结果；SET 的键或值为 byte[] 时使用二进制写入，SET ... GET 同时返回旧值的原始字节。

## 使用限制

- 精确小数使用 SET／GET 存取，不通过浮点数增量命令计算。
- Redis List／Set 是原生结构，不是 JDBC `ARRAY`。
