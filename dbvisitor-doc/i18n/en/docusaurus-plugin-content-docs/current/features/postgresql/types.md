---
id: types
sidebar_position: 1
title: Type Support
description: PostgreSQL type support
---

# Type Support

The table recommends Java property types for common columns. Use wrapper types for nullable fields. See [Java/JDBC types](../../guides/types/java-jdbc.md) and [enum mapping](../../guides/types/enum-handler.md).

## Type Mappings

| Database field type | Java type | Description |
| --- | --- | --- |
| SMALLINT | Short | Covers the range of this signed integer type. |
| INTEGER | Integer | Covers the range of this signed integer type. |
| BIGINT | Long | Covers the range of this signed integer type. |
| REAL | Float | Approximate values; not for amounts requiring exact decimal arithmetic. |
| DOUBLE PRECISION | Double | Approximate values; not for amounts requiring exact decimal arithmetic. |
| DECIMAL(10, 2) | BigDecimal | Retains decimal values; writes are constrained by column precision and scale. |
| NUMERIC(20) | BigInteger / BigDecimal | Use BigInteger for integral business values, or BigDecimal for a common decimal model. |
| BIT(1) | Boolean | Suitable only when a one-bit value is used as an application flag. PostgreSQL `BIT` is a bit-string type, not a synonym for `BOOLEAN`. |
| BOOLEAN | Boolean | Use Boolean semantics, not a general integer property. |
| CHAR(1) | String | Text content; the column definition constrains length. |
| VARCHAR(255) | String | Text content; the column definition constrains length. |
| BYTEA | byte[] | Binary content without character encoding conversion. |
| DATE | java.sql.Date | Date only; not for preserving a complete timestamp. |
| TIME | java.sql.Time | Time only; text storage requires a parseable time format. |
| TIMESTAMP | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| VARCHAR(2000) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |
| JSONB | Map / List / Bean | See [JSON Field Mapping](../../guides/core/mapping/json-field.md) for entity-property configuration. |
| INTEGER[] | Integer[] | Array elements match the database element type; configure the handler explicitly when needed. |
| VARCHAR[] | String[] | Array elements match the database element type; configure the handler explicitly when needed. |
| REAL[] | Float[] | Array elements match the database element type; configure the handler explicitly when needed. |
| vector(n) | List&lt;Float> | See [Vector Operations](./vectors.mdx) for mapping; the vector length must match the column dimension. |

## Example: Native Array

```sql
CREATE TABLE array_example (id INTEGER PRIMARY KEY, values_col INTEGER[]);
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
