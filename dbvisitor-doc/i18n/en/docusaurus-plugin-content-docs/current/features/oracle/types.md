---
id: types
sidebar_position: 1
title: Type Support
description: Oracle type support
---

# Type Support

The table recommends Java property types for common columns. Use wrapper types for nullable fields. See [Java/JDBC types](../../guides/types/java-jdbc.md) and [enum mapping](../../guides/types/enum-handler.md).

## Type Mappings

| Database field type | Java type | Description |
| --- | --- | --- |
| NUMBER(2) | Byte | Covers -99 to 99. |
| NUMBER(3) | Short | Covers -999 to 999; Byte cannot cover the full column range. |
| NUMBER(5) | Integer | Covers -99999 to 99999; Short cannot cover the full column range. |
| NUMBER(10) | Long | Covers 10-digit decimal integers; Integer cannot cover the full column range. |
| NUMBER(19) | BigInteger | Covers 19-digit decimal integers; Long cannot cover the full column range. |
| NUMBER(p, 0) | BigInteger | General integral mapping; choose smaller integer types when the range permits, or BigDecimal for a common decimal model. |
| NUMBER(p, s) | BigDecimal | Use for fractional values or a common decimal representation. |
| NUMBER(1) | Boolean | Only for application-defined 0/1 flags; use Byte for ordinary one-digit integers. |
| BINARY_FLOAT | Float | Approximate values; not for amounts requiring exact decimal arithmetic. |
| BINARY_DOUBLE | Double | Approximate values; not for amounts requiring exact decimal arithmetic. |
| CHAR(1) | String | Text content; the column definition constrains length. |
| VARCHAR2(255) | String | Text content; the column definition constrains length. |
| NVARCHAR2(255) | String | Text content; the column definition constrains length. |
| RAW(1000) | byte[] | Binary content without character encoding conversion. |
| BLOB | byte[] | Binary content without character encoding conversion. |
| DATE | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| TIMESTAMP | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| VARCHAR2(2000) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| CLOB | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |

## Example: Declare Entity Properties for Table Fields

These are field declarations inside an entity, with column names set by `@Column`. Handlers are selected from property types and mapping configuration; these ordinary numeric properties do not require explicit JDBC types.

```java
import java.math.BigDecimal;
import net.hasor.dbvisitor.mapping.Column;

// NUMBER(3)
@Column("quantity")
private Short quantity;

// NUMBER(5)
@Column("sort_no")
private Integer sortNo;

// NUMBER(10, 2)
@Column("amount")
private BigDecimal amount;
```

## Limits

- Empty strings become `NULL`.
