---
id: types
sidebar_position: 1
title: Type Support
description: MySQL type support
---

# Type Support

The table recommends Java property types for common columns. Use wrapper types for nullable fields. See [Java/JDBC types](../../guides/types/java-jdbc.md) and [enum mapping](../../guides/types/enum-handler.md).

## Type Mappings

| Database field type | Java type | Description |
| --- | --- | --- |
| TINYINT | Byte | Byte is recommended for signed TINYINT; UNSIGNED needs a wider type. |
| SMALLINT | Short | Covers the range of this signed integer type. |
| INT | Integer | Covers the range of this signed integer type. |
| BIGINT | Long | Covers the range of this signed integer type. |
| FLOAT | Float | Approximate values; not for amounts requiring exact decimal arithmetic. |
| DOUBLE | Double | Approximate values; not for amounts requiring exact decimal arithmetic. |
| DECIMAL(10, 2) | BigDecimal | Retains decimal values; writes are constrained by column precision and scale. |
| DECIMAL(20, 0) | BigInteger / BigDecimal | Use BigInteger for integral business values, or BigDecimal for a common decimal model. |
| BIT(1) | Boolean | Use Boolean semantics, not a general integer property. |
| BOOLEAN | Boolean | MySQL treats it as a synonym for `TINYINT(1)`; use it for 0/1 Boolean semantics. |
| CHAR(1) | String | Text content; the column definition constrains length. |
| VARCHAR(255) | String | Text content; the column definition constrains length. |
| VARBINARY(1000) | byte[] | Binary content without character encoding conversion. |
| LONGBLOB | byte[] | Binary content without character encoding conversion. |
| DATE | java.sql.Date | Date only; not for preserving a complete timestamp. |
| TIME | java.sql.Time | Time only; text storage requires a parseable time format. |
| DATETIME(3) | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| VARCHAR(2000) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| JSON | Map / List / Bean | See [JSON Field Mapping](../../guides/core/mapping/json-field.md) for entity-property configuration. |

## Read TINYINT(1) as a Number

- **Flags**: use `Boolean` for `true/false`.
- **Numeric status codes**: use `Byte` or `Integer` and set `tinyInt1isBit=false` in the connection URL:

```text
jdbc:mysql://localhost:3306/appdb?tinyInt1isBit=false
```

For example, when a signed `TINYINT(1)` stores `2`, the default `getObject()` read returns `true`; with this setting, it returns the number `2`. Explicit integer reads are unaffected.
