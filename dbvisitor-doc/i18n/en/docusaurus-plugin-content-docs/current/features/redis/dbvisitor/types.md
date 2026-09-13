---
id: types
slug: /features/redis/types
sidebar_position: 1
title: Type Support
description: Redis type support
---

# Type Support

Redis organizes data as keys and native structures. The table lists Java types for common command results. See [Java/JDBC types](../../../guides/types/java-jdbc.md).

## Type Mappings

| Redis data type | Java type | Description |
| --- | --- | --- |
| String | String | GET results are usually read as String; choose Long, Boolean, java.sql.Date or Instant only with a defined text format. |
| String | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../../guides/core/mapping/json-field.md). |
| String | Enum | Store enum names; declare the concrete enum class and keep values aligned with constant names. |
| String | BigDecimal | Store decimal text and read it as BigDecimal without precision loss. |
| String | BigInteger | Store integer text and read it as BigInteger. |
| String (binary content) | byte[] | SET accepts byte arrays; bind the GET key as byte[] to retrieve raw bytes. |
| Hash | String | HGETALL returns each field and value as String. |
| List | String | Each LRANGE result row contains one String element. |
| Set | String | Each SMEMBERS result row contains one String element. |
| Sorted Set | Double | Score returned by score-aware commands; approximate and unsuitable for exact decimal amounts. |

## Binary Content

A UTF-8 encoded text key identifies the same Redis key. Bind a byte-array key when reading to retrieve raw bytes without text decoding.

```java
import java.nio.charset.StandardCharsets;

String key = "demo:binary";
byte[] content = new byte[] { 0, (byte) 255, (byte) 128 };
jdbc.executeUpdate("SET ? ?", new Object[] { key, content });
byte[] loaded = jdbc.queryForObject("GET ?",
        new Object[] { key.getBytes(StandardCharsets.UTF_8) }, byte[].class);
```

GET with a String key continues to return text. SET uses binary writes when its key or value is byte[]; SET ... GET also returns the previous value as raw bytes.

## Limits

- Use SET/GET for exact decimals, not floating-point increment commands.
- Redis List / Set are native structures, not JDBC `ARRAY` values.
