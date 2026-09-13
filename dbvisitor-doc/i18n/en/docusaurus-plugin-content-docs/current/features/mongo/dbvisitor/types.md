---
id: types
slug: /features/mongo/types
sidebar_position: 1
title: Type Support
description: MongoDB type support
---

# Type Support

The table recommends Java property types for common BSON types. See [Java/JDBC types](../../../guides/types/java-jdbc.md).

## Type Mappings

| BSON type | Java type | Description |
| --- | --- | --- |
| BSON string | String | Read text by field name or entity property mapping. |
| BSON int32 | Integer | Signed 32-bit integer. |
| BSON int64 | Long | Signed 64-bit integer. |
| BSON double | Double | 64-bit floating-point value. |
| BSON boolean | Boolean | Use Boolean semantics, not a general integer property. |
| BSON date | Instant / java.sql.Timestamp | Use when retaining the instant; choose java.sql.Date only for date-only business values. |
| BSON document | Map / Bean | See [JSON Field Mapping](../../../guides/core/mapping/json-field.md). |
| BSON array | List | See [JSON Field Mapping](../../../guides/core/mapping/json-field.md). |
| BSON string | Enum | Store enum names; declare the concrete enum class and keep field values aligned with constant names. |
| BSON binary | byte[] | Binary content without character encoding conversion. |
| BSON ObjectId | String | Store the hexadecimal string in the entity; use `ObjectId(?)` in commands. |

A BSON date stores an instant; a `java.sql.Date` target represents only the date. Use `Timestamp` or `Instant` when the time of day matters. Document and array content must match the configured object or list mapping.

## Example: Bind a Boolean and Read Its Field

```java
jdbcTemplate.execute("use test");
jdbcTemplate.execute("db.createCollection('type_example')");
jdbcTemplate.executeUpdate("test.type_example.insert({id: ?, enabled: ?})",
        new Object[] { 1, true });
Boolean enabled = jdbcTemplate.queryForObject(
        "test.type_example.find({id: ?}, {enabled: 1})",
        new Object[] { 1 }, (rs, rowNum) -> rs.getBoolean("enabled"));
```

## Limits

- Default mapping does not preserve `BigInteger` or exact decimals losslessly.
- MongoDB arrays are not JDBC `ARRAY` values.
- The first column is document metadata; read business fields by name, `RowMapper`, or entity.
