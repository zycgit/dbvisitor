---
id: types
sidebar_position: 1
title: Type Support
description: Recommended Java property types for Dameng database types
---

# Type Support

The table recommends Java property types for common Dameng columns. Use wrapper types for nullable fields. `NUMBER` precision does not select a Java type automatically. See [Java/JDBC types](../../guides/types/java-jdbc.md).

## Common Types

| Database column type | Java type | Notes |
| --- | --- | --- |
| BIT | Boolean | `0` is false and a nonzero value is true. |
| TINYINT | Byte | Signed integer from -128 to 127. |
| BYTE | Byte | Integer with precision 3 and scale 0. |
| SMALLINT | Short | Signed integer from -32768 to 32767. |
| INTEGER | Integer | Signed 32-bit integer. |
| INT | Integer | Synonym for `INTEGER`. |
| BIGINT | Long | Signed 64-bit integer. |
| NUMERIC(p, s) | BigDecimal | Precision is at most 38; writes are constrained by the column precision and scale. |
| DECIMAL(p, s) | BigDecimal | Similar semantics to `NUMERIC`. |
| DEC(p, s) | BigDecimal | Synonym for `DECIMAL`. |
| NUMBER(p, s) | BigDecimal | Same semantics as `NUMERIC`. |
| REAL | Float | Single-precision approximate number. |
| FLOAT(p) | Float / Double | Stored as single precision when `p` is at most 24 and as double precision when `p` is greater than 24. Choose the Java property from the column definition. |
| DOUBLE | Double | Double-precision approximate number. |
| DOUBLE PRECISION | Double | Double-precision approximate number. |
| CHAR(n) | String | Fixed-length string; the database pads shorter values with spaces. |
| CHARACTER(n) | String | Synonym for `CHAR`. |
| VARCHAR(n) | String | Variable-length string. |
| VARCHAR2(n) | String | Same usage as `VARCHAR`. |
| TEXT | String | Large text. For JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| LONG | String | Synonym for `TEXT`. |
| LONGVARCHAR | String | Synonym for `TEXT`. |
| CLOB | String | Large text. For JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| BINARY(n) | byte[] | Fixed-length binary content. |
| VARBINARY(n) | byte[] | Variable-length binary content. |
| RAW(n) | byte[] | Synonym for `VARBINARY`. |
| IMAGE | byte[] | Binary large object; it may also hold non-image binary content. |
| LONGVARBINARY | byte[] | Synonym for `IMAGE`. |
| BLOB | byte[] | Binary large object. |
| DATE | java.sql.Date | Contains only year, month, and day. |
| TIME | java.sql.Time | Contains only time fields; fractional-second precision defaults to 0 when omitted. |
| TIME(p) | String | When `p` is from 1 through 6 and fractional seconds must be preserved, use a string or a Dameng JDBC extension object; `java.sql.Time` cannot represent that precision completely. |
| TIMESTAMP(p) | java.sql.Timestamp | Contains date and time; Dameng supports fractional-second precision up to 9. `DATETIME` is a synonym. |
| ROWID | String | An 18-character value used for a Dameng row identifier. |

Scale-zero `NUMERIC`, `DECIMAL`, `DEC`, and `NUMBER` columns may also map to integer types, but the application must ensure that values have no fractional part and fit the selected Java range. Prefer `BigDecimal` when the complete column range must be preserved.

## Interval Types

Dameng JDBC provides extension objects for interval types. The official driver also permits reading and writing them with `getString` and `setString`. Use `String` when the entity only needs to preserve the complete literal value:

| Database column type | Java type | Notes |
| --- | --- | --- |
| INTERVAL YEAR | String | Year interval; the value must follow the Dameng interval literal format. |
| INTERVAL MONTH | String | Month interval; the value must follow the Dameng interval literal format. |
| INTERVAL YEAR TO MONTH | String | Year-to-month interval. |
| INTERVAL DAY | String | Day interval. |
| INTERVAL DAY TO HOUR | String | Day-to-hour interval. |
| INTERVAL DAY TO MINUTE | String | Day-to-minute interval. |
| INTERVAL DAY TO SECOND | String | Day-to-second interval, optionally with fractional seconds. |
| INTERVAL HOUR | String | Hour interval. |
| INTERVAL HOUR TO MINUTE | String | Hour-to-minute interval. |
| INTERVAL HOUR TO SECOND | String | Hour-to-second interval, optionally with fractional seconds. |
| INTERVAL MINUTE | String | Minute interval. |
| INTERVAL MINUTE TO SECOND | String | Minute-to-second interval, optionally with fractional seconds. |
| INTERVAL SECOND | String | Second interval, optionally with fractional seconds. |

When individual year, month, day, hour, minute, or second components are required, use the Dameng JDBC extension object through a custom mapping. dbVisitor does not automatically convert those proprietary objects to `java.time.Duration` or `Period`.

## Example: Declare Entity Properties

```java
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import net.hasor.dbvisitor.mapping.Column;

@Column("quantity")
private Short quantity;       // SMALLINT

@Column("amount")
private BigDecimal amount;    // NUMBER(10, 2)

@Column("business_date")
private Date businessDate;    // DATE

@Column("created_at")
private Timestamp createdAt;  // TIMESTAMP(6)

@Column("payload")
private byte[] payload;       // BLOB
```

## Boundaries

- `BFILE` is a read-only server-side file, not a `BLOB`.
- Use a custom mapping or `RowMapper` for zoned time types and `XMLTYPE`.
- Use `NUMBER` with `BigDecimal` for exact decimals, not a floating-point type.
- Use `Timestamp` for `TIMESTAMP`. Use text or a Dameng JDBC extension object when `TIME(p)` fractional seconds must be preserved.
