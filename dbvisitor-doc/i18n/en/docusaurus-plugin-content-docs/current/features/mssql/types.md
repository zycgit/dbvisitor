---
id: types
sidebar_position: 1
title: Type Support
description: SQL Server type support
---

# Type Support

The table recommends Java property types for common columns. Use wrapper types for nullable fields. See [Java/JDBC types](../../guides/types/java-jdbc.md) and [enum mapping](../../guides/types/enum-handler.md).

## Type Mappings

| Database field type | Java type | Description |
| --- | --- | --- |
| TINYINT | Short | Covers 0–255; use Byte only when business values are restricted to 0–127. |
| SMALLINT | Short | Covers the range of this signed integer type. |
| INT | Integer | Covers the range of this signed integer type. |
| BIGINT | Long | Covers the range of this signed integer type. |
| REAL | Float | Approximate values; not for amounts requiring exact decimal arithmetic. |
| FLOAT | Double | Approximate values; not for amounts requiring exact decimal arithmetic. |
| DECIMAL(10, 2) | BigDecimal | Retains decimal values; writes are constrained by column precision and scale. |
| DECIMAL(20, 0) | BigInteger / BigDecimal | Use BigInteger for integral business values, or BigDecimal for a common decimal model. |
| BIT | Boolean | Use Boolean semantics, not a general integer property. |
| CHAR(1) | String | Text content; the column definition constrains length. |
| VARCHAR(255) | String | Text content; the column definition constrains length. |
| NVARCHAR(255) | String | Text content; the column definition constrains length. |
| VARBINARY(1000) | byte[] | Binary content without character encoding conversion. |
| VARBINARY(MAX) | byte[] | Binary content without character encoding conversion. |
| DATE | java.sql.Date | Date only; not for preserving a complete timestamp. |
| TIME | java.sql.Time | Time only; text storage requires a parseable time format. |
| DATETIME2(3) | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| NVARCHAR(2000) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| NVARCHAR(MAX) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |

## Limits

- SQL Server `TINYINT` is unsigned; use `SMALLINT` for negative values.
