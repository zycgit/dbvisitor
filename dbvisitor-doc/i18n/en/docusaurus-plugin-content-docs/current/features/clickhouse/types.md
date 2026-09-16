---
id: types
sidebar_position: 80
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
| Nullable(String) | String | Text content; no VARCHAR(n)-style length constraint. |
| Nullable(Date) | java.sql.Date | Date only; not for preserving a complete timestamp. |
| Nullable(String) | java.sql.Time | Time only; field content must use a parseable time format. |
| Nullable(DateTime64(3)) | java.sql.Timestamp | Preserves date and time, subject to column precision. |
| Nullable(String) | Map / List / Bean | When storing JSON text, see [JSON Field Mapping](../../guides/core/mapping/json-field.md). |

## Text Length {#text-length}

`String` does not impose a declared text length. Even when written as `VARCHAR(100)`, ClickHouse ignores `100`: a value is not rejected simply because it contains 101 characters. [String type reference](https://clickhouse.com/docs/reference/data-types/string)

dbVisitor does not add a string-length check. If names must contain at most 100 characters, validate them before writing rather than expecting a length error from the Builder API or BaseMapper.

## Limits

- The time zone and precision of `DateTime64(3)` follow the column definition and the ClickHouse JDBC driver.
- `String` mappings do not preserve arbitrary binary content.

## Array Types {#array-values}

Non-null arrays can be read and written, but the entire array cannot be NULL. Use an empty array when there are no elements; see [Empty Arrays and NULL](#array-null).

| Database field type | Java type | Notes |
| --- | --- | --- |
| Array(Int32) | Integer[] | Ordered integers. |
| Array(Float32) | Float[] | Ordered floating-point values. |
| Array(String) | String[] | Ordered strings. |

Arrays can be bound, read, replaced, and written in multiple rows. For example:

```java
@Table("array_values")
public class ArrayValues {
    @Column(value = "id", primary = true)
    private Integer id;
    @Column(value = "values", jdbcType = Types.ARRAY)
    private Integer[] values;
    // getters and setters
}
```

The corresponding fields are `id Int32` and `values Array(Int32)`. Use the normal [array mapping](../../guides/types/array-handler.md) APIs.

### Empty arrays and NULL {#array-null}

Use an empty array for “no items”. An entire Array value cannot be NULL: `Nullable(Array(Int32))` is not valid. Nullable elements, such as `Array(Nullable(Int32))`, do not make the array itself nullable.

When writing null elements, explicitly name the nullable JDBC array element type as well. Binding with plain `INTEGER` turns null into 0 in the driver; `Nullable(Int32)` preserves it:

```java
ArrayTypeHandler handler = new ArrayTypeHandler() {
    @Override
    protected String resolveTypeName(Class<?> type) {
        return type == Integer.class ? "Nullable(Int32)" : super.resolveTypeName(type);
    }
};
Integer[] values = { null, -1, 0, 7, null };
jdbc.executeUpdate("INSERT INTO array_values (id, values) VALUES (?, ?)",
        new Object[] { 1, new SqlArg(values, Types.ARRAY, handler) });
```

The `values` column in this example must be `Array(Nullable(Int32))`. For strings, use `Nullable(String)` instead.

## Vector Types {#vector-types}

See [Vector Operations](vectors.md#vector-mapping) for field mapping and reads/writes.
