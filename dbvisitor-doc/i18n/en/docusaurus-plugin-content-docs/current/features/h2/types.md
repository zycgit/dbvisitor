---
id: types
sidebar_position: 1
title: Type Support
description: H2 type support
---

# Type Support

The table recommends Java property types for common columns. Use wrapper types for nullable fields. See [Java/JDBC types](../../guides/types/java-jdbc.md) and [enum mapping](../../guides/types/enum-handler.md).

## Type Mappings

| Database field type | Java type | Description |
| --- | --- | --- |
| TINYINT | Byte | Covers the range of this signed integer type. |
| SMALLINT | Short | Covers the range of this signed integer type. |
| INT | Integer | Covers the range of this signed integer type. |
| BIGINT | Long | Covers the range of this signed integer type. |
| REAL | Float | Approximate values; not for amounts requiring exact decimal arithmetic. |
| DOUBLE | Double | Approximate values; not for amounts requiring exact decimal arithmetic. |
| DECIMAL(10, 2) | BigDecimal | Retains decimal values; writes are constrained by column precision and scale. |
| NUMERIC(20) | BigInteger / BigDecimal | Use BigInteger for integral business values, or BigDecimal for a common decimal model. |
| BIT | Boolean | Use Boolean semantics, not a general integer property. |
| BOOLEAN | Boolean | Use Boolean semantics, not a general integer property. |
| CHAR(1) | String | Text content; the column definition constrains length. |
| VARCHAR(255) | String | Text content; the column definition constrains length. |
| NVARCHAR(255) | String | Text content; the column definition constrains length. |
| VARBINARY(1000) | byte[] | Binary content without character encoding conversion. |
| BLOB | byte[] | Binary content without character encoding conversion. |
| DATE | java.sql.Date | Date only; not for preserving a complete timestamp. |
| TIME | java.sql.Time | Time only; text storage requires a parseable time format. |
| TIMESTAMP | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| VARCHAR(2000) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| INT ARRAY | Integer[] | Array elements match the database element type; configure the handler explicitly when needed. |
| VARCHAR ARRAY | String[] | Array elements match the database element type; configure the handler explicitly when needed. |
| REAL ARRAY | Float[] | Array elements match the database element type; configure the handler explicitly when needed. |

## Example: Native Array

```sql
CREATE TABLE array_example (id INTEGER PRIMARY KEY, values_col INTEGER ARRAY);
```


```java
import java.sql.Types;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;

Integer[] values = { 10, 20, 30 };
jdbcTemplate.executeUpdate("INSERT INTO array_example (id, values_col) VALUES (?, ?)",
        new Object[] { 1, new SqlArg(values, Types.ARRAY, new ArrayTypeHandler()) });
Integer[] loaded = jdbcTemplate.queryForObject(
        "SELECT values_col FROM array_example WHERE id = ?",
        new Object[] { 1 }, Integer[].class);
```

## Limits

- `TIMESTAMP` does not preserve a time zone. Precision follows the column definition and the value returned by the JDBC driver.
