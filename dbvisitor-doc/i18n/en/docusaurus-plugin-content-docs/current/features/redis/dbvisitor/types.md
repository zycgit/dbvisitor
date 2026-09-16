---
id: types
slug: /features/redis/types
sidebar_position: 80
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

## Basic Types {#basic-types}

Use native SET/HSET commands to store empty strings; the builder API does not provide Redis entity insertion.

Numbers, booleans, characters, and empty strings can be read and written. Redis values cannot store SQL NULL. To represent absence, delete the key; see [Missing Values](#null-values).

## Time Types {#time-types}

Non-null date and time values can be converted using type handlers. A Java null cannot be stored as a Redis value; delete the key instead. See [Missing Values](#null-values).

## Enum Types {#enum-types}

Non-null enum names, codes, and values can be converted using the corresponding handler. A null enum cannot be stored as a Redis value; see [Missing Values](#null-values).

## JSON Serialization {#json-types}

Objects and collections can be stored as JSON text. SQL NULL round trips are not supported. The JSON text `null` is not a missing Redis value; see [Missing Values](#null-values) to represent absence.

## Binary Types {#binary-types}

Non-null byte arrays can be read and written; null byte arrays cannot be stored. To clear the value, see [Missing Values](#null-values).

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

## Array Types {#array-types}

Redis List and Set are not read or written through JDBC ARRAY mapping. Use list/set commands or [JSON Field Mapping](../../../guides/core/mapping/json-field.md) to store a collection.

## Missing values {#null-values}

Redis cannot store SQL NULL values, including null enums and byte arrays. Use key or Hash-field deletion to represent absence; see [NULL Parameters](./parameters.md#null-values).
