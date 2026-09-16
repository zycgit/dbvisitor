---
id: types
slug: /features/elastic/types
sidebar_position: 80
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

## Text Length {#text-length}

`text` and `keyword` do not provide a `VARCHAR(100)`-style write-length constraint. dbVisitor does not reject values longer than 100 characters on their behalf; validate business length limits before writing.

The `keyword` setting `ignore_above` controls indexing, not write rejection. With `ignore_above: 100`, a 101-character value remains in `_source`, but that field is excluded from exact-match searches and aggregations. [ignore_above reference](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/ignore-above.html)

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
- Read business fields by name, `RowMapper`, or entity; the first two columns are `_ID` and `_DOC`.
- Applies to the ES6 and ES7 adapters.

## Array Types {#array-values}

Elasticsearch does not declare a separate array type: a field mapped as integer, float, or keyword can hold multiple values. The adapter supports JDBC ARRAY binding and reads for these values. Use Integer[], Float[], or String[] properties; keep all values compatible with the field mapping.

Use an empty array_example index with id and int_array mapped as integer:

```java
import java.sql.Types;
import net.hasor.dbvisitor.types.SqlArg;

Integer[] values = { 10, 20, 30 };
jdbc.executeUpdate("POST /array_example/_doc {\"id\": ?, \"int_array\": ?}",
        new Object[] { 1, SqlArg.valueOf(values, Types.ARRAY) });
jdbc.execute("POST /array_example/_refresh");
Integer[] loaded = jdbc.queryForObject(
        "POST /array_example/_search {\"_source\": [\"int_array\"], "
                + "\"query\": {\"term\": {\"id\": ?}}}",
        new Object[] { 1 }, Integer[].class);
```
