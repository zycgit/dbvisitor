---
id: types
slug: /features/elastic/types
sidebar_position: 1
title: Type Support
description: Elasticsearch type support
---

# Type Support

The table recommends Java property types for common Elasticsearch fields. See [Java/JDBC types](../../../guides/types/java-jdbc.md).

## Type Mappings

| Elasticsearch field type | Java type | Description |
| --- | --- | --- |
| keyword | String | Read text by field name or entity property mapping. |
| text | String | Read text by field name or entity property mapping. |
| integer | Integer | Signed 32-bit integer. |
| long | Long | Signed 64-bit integer. |
| double | Double | 64-bit floating-point value. |
| boolean | Boolean | Use Boolean semantics, not a general integer property. |
| date | java.sql.Date (date-only use) | Does not preserve instant millisecond precision. |
| object | Map / List / Bean | See [JSON Field Mapping](../../../guides/core/mapping/json-field.md). |
| keyword | Enum | Store enum names; declare the concrete enum class and keep field values aligned with constant names. |

## Example: Bind a Boolean and Read Its Field

```java
jdbcTemplate.execute("PUT /type_example");
jdbcTemplate.executeUpdate("POST /type_example/_doc {\"id\": ?, \"enabled\": ?}",
        new Object[] { 1, true });
jdbcTemplate.execute("POST /type_example/_refresh");
Boolean enabled = jdbcTemplate.queryForObject(
        "POST /type_example/_search {\"query\": {\"term\": {\"id\": ?}}}",
        new Object[] { 1 }, (rs, rowNum) -> rs.getBoolean("enabled"));
```

## Limits

- Date mapping does not preserve millisecond precision.
- `BigInteger`, exact decimals, and arbitrary binary content are not guaranteed to round-trip losslessly.
- Map arrays and objects as JSON, not JDBC `ARRAY`.
- Read business fields by name, `RowMapper`, or entity; the first two columns are `_ID` and `_DOC`.
- Applies to the ES6 and ES7 adapters.
