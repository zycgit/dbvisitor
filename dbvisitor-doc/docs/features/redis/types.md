---
id: types
sidebar_position: 1
title: 类型支持
description: Redis 类型支持
---

# 类型支持

Redis 按键和原生数据结构组织数据。下表列出常用命令结果对应的 Java 类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)。

## 类型映射

| Redis 数据类型 | Java 类型 | 说明 |
| --- | --- | --- |
| String | String | GET 结果通常读取为 String；格式约定明确时可选 Long、Boolean、java.sql.Date 或 Instant。 |
| String | Map / List / Bean | 保存 JSON 文本时，配置方式见 [JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| String | Enum | 保存枚举名称；指定具体枚举类，并保证字段值与枚举常量一致。 |
| Hash | String | HGETALL 的每行键和值均按 String 读取。 |
| List | String | LRANGE 的每个结果行包含一个 String 元素。 |
| Set | String | SMEMBERS 的每个结果行包含一个 String 元素。 |
| Sorted Set | Double | 带分数查询的分值；属于近似数值，不用于要求十进制精确计算的金额。 |

## 使用限制

- `BigInteger` 和精确小数应以 `String` 保存并由应用解析。
- 默认 SET／GET 不能无损传输任意二进制数据。
- Redis List／Set 是原生结构，不是 JDBC `ARRAY`。
