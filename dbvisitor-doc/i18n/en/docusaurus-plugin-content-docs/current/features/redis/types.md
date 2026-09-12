---
id: types
sidebar_position: 1
title: Type Support
description: Redis type support
---

# Type Support

Redis organizes data as keys and native structures. The table lists Java types for common command results. See [Java/JDBC types](../../guides/types/java-jdbc.md).

## Type Mappings

| Redis data type | Java type | Description |
| --- | --- | --- |
| String | String | GET results are usually read as String; choose Long, Boolean, java.sql.Date or Instant only with a defined text format. |
| String | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| String | Enum | Store enum names; declare the concrete enum class and keep values aligned with constant names. |
| Hash | String | HGETALL returns each field and value as String. |
| List | String | Each LRANGE result row contains one String element. |
| Set | String | Each SMEMBERS result row contains one String element. |
| Sorted Set | Double | Score returned by score-aware commands; approximate and unsuitable for exact decimal amounts. |

## Limits

- Store `BigInteger` and exact decimals as text and parse them in the application.
- Default SET/GET does not preserve arbitrary binary data losslessly.
- Redis List / Set are native structures, not JDBC `ARRAY` values.
