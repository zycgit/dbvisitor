---
id: types
sidebar_position: 1
title: Type Support
description: ClickHouse type support
---

# Type Support

The table recommends Java property types for common columns. Use wrapper types for nullable fields. See [Java/JDBC types](../../guides/types/java-jdbc.md) and [enum mapping](../../guides/types/enum-handler.md).

## Type Mappings

| Database field type | Java type | Description |
| --- | --- | --- |
| Nullable(Int16) | Short | Covers the range of this signed integer type. |
| Nullable(Int32) | Integer | Covers the range of this signed integer type. |
| Nullable(Int64) | Long | Covers the range of this signed integer type. |
| Nullable(Float32) | Float | Approximate values; not for amounts requiring exact decimal arithmetic. |
| Nullable(Float64) | Double | Approximate values; not for amounts requiring exact decimal arithmetic. |
| Nullable(Decimal(10, 2)) | BigDecimal | Retains decimal values; writes are constrained by column precision and scale. |
| Nullable(Decimal(20, 0)) | BigInteger / BigDecimal | Use BigInteger for integral business values, or BigDecimal for a common decimal model. |
| Nullable(Bool) | Boolean | Use Boolean semantics, not a general integer property. |
| Nullable(String) | String | Text content; the column definition constrains length. |
| Nullable(Date) | java.sql.Date | Date only; not for preserving a complete timestamp. |
| Nullable(String) | java.sql.Time | Time only; field content must use a parseable time format. |
| Nullable(DateTime64(3)) | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| Nullable(String) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |

## Limits

- The time zone and precision of `DateTime64(3)` follow the column definition and the ClickHouse JDBC driver.
- `String` mappings do not preserve arbitrary binary content.
