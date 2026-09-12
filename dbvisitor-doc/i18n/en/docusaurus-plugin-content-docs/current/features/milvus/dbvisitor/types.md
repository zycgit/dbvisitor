---
id: types
slug: /features/milvus/types
sidebar_position: 1
title: Type Support
description: Milvus type support
---

# Type Support

The table recommends Java property types for Milvus fields. Vector types and dimensions follow the collection schema. See [Java/JDBC types](../../../guides/types/java-jdbc.md).

## Type Mappings

Vector rows also list JDBC read and write forms.

| Milvus field type | Java type | Description |
| --- | --- | --- |
| BOOL | Boolean | Use Boolean semantics, not a general integer property. |
| INT8 | Byte | Signed 8-bit integer. |
| INT16 | Short | Signed 16-bit integer. |
| INT32 | Integer | Signed 32-bit integer. |
| INT64 | Long | Signed 64-bit integer. |
| FLOAT | Float | 32-bit floating-point value. |
| DOUBLE | Double | 64-bit floating-point value. |
| VARCHAR(max_length) | String | `max_length` limits content by UTF-8 byte length. |
| JSON | Map / List / Bean | See [JSON Field Mapping](../../../guides/core/mapping/json-field.md); direct `getObject()` returns `JsonElement`. |
| ARRAY&lt;BOOL>(max_capacity) | Boolean[] | Boolean elements. |
| ARRAY&lt;INT8>(max_capacity) | Byte[] | Signed 8-bit integer elements. |
| ARRAY&lt;INT16>(max_capacity) | Short[] | Signed 16-bit integer elements. |
| ARRAY&lt;INT32>(max_capacity) | Integer[] | Signed 32-bit integer elements. |
| ARRAY&lt;INT64>(max_capacity) | Long[] | Signed 64-bit integer elements. |
| ARRAY&lt;FLOAT>(max_capacity) | Float[] | 32-bit floating-point elements. |
| ARRAY&lt;DOUBLE>(max_capacity) | Double[] | 64-bit floating-point elements. |
| ARRAY&lt;VARCHAR(max_length)>(max_capacity) | String[] | `max_length` limits each string; `max_capacity` limits the element count. |
| FLOAT_VECTOR(n) | List&lt;Float> | `getObject()` returns `List<Float>`. Writes accept a numeric List or one-dimensional numeric primitive array and convert every element to Float; length must be n. |
| BINARY_VECTOR(n) | byte[] | `getBytes()` returns bit-packed bytes with length n/8. Writes accept `byte[]`, `ByteBuffer`, or a List of byte values; this is not an arbitrary-length BLOB. |
| FLOAT16_VECTOR(n) | byte[] | Queries return two little-endian half-precision bytes per dimension; writes also accept a numeric List or numeric primitive array. |
| BFLOAT16_VECTOR(n) | byte[] | Queries return two little-endian BFloat16 bytes per dimension; writes also accept a numeric List or numeric primitive array. |
| INT8_VECTOR(n) | byte[] | `getBytes()` returns one signed byte per dimension. Writes also accept `ByteBuffer`, a numeric List, or a one-dimensional numeric primitive array; elements must be integers from -128 through 127 and length must be n. |
| SPARSE_FLOAT_VECTOR | SortedMap&lt;Long, Float> | `getObject()` returns dimensions in ascending index order. Writes accept a nonempty `Map<Number, Number>` whose keys are dimension indices and values are finite floating-point weights. |

The target field schema determines what a `byte[]` means. It is a sequence of signed numeric elements for `FLOAT_VECTOR`, a packed encoding for `BINARY_VECTOR`, `FLOAT16_VECTOR`, and `BFLOAT16_VECTOR`, and one vector component per byte for `INT8_VECTOR`. The driver does not guess the vector kind from the Java parameter type alone.

## Example: Array, JSON and Float Vector

```sql
CREATE TABLE type_example (
    id INT64 PRIMARY KEY,
    profile JSON NULL,
    tags `ARRAY<INT32>`(10) NULL,
    embedding FLOAT_VECTOR(3)
) WITH (consistency_level=Strong);
CREATE INDEX type_embedding ON type_example(embedding) USING AUTOINDEX WITH (metric_type=L2);
LOAD TABLE type_example;
```

Given an established `Connection conn`:
```java
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

Array tags = conn.createArrayOf("INTEGER", new Integer[] { 1, 2 });
try (PreparedStatement ps = conn.prepareStatement(
        "INSERT INTO type_example (id, profile, tags, embedding) VALUES (?, ?, ?, ?)")) {
    ps.setLong(1, 1L);
    ps.setString(2, "{\"city\":\"Hangzhou\"}");
    ps.setArray(3, tags);
    ps.setObject(4, List.of(0.1f, 0.2f, 0.3f));
    ps.executeUpdate();
} finally {
    tags.free();
}
try (PreparedStatement ps = conn.prepareStatement(
        "SELECT profile, tags, embedding FROM type_example WHERE id = ?")) {
    ps.setLong(1, 1L);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            Object profile = rs.getObject("profile");
            Array loadedTags = rs.getArray("tags");
            try {
                Object values = loadedTags.getArray();
            } finally {
                loadedTags.free();
            }
            Object embedding = rs.getObject("embedding");
        }
    }
}
```


Vector representation and dimension restrictions are detailed in [SQL types](../types/fields.md). BinaryVector is not a general BLOB column.

## Limits

- Writing `BigDecimal` to `DOUBLE` loses precision; `INT64` covers only signed 64-bit integers.
- Dates and times use `VARCHAR` or `INT64`; Milvus has no native date type.
- Milvus has no general `BLOB` or `VARBINARY`; `BinaryVector` stores only fixed-dimension bit vectors.

## Entity Mapping

An entity must match an existing collection, including its single primary key. Field renaming and type conversion do not create fields or change their Milvus types.

Map Java enums to VARCHAR names/codes or INT32 numeric codes; see [Enum Mapping](../../../guides/types/enum-handler.md). Milvus does not provide an ENUM field type.

An `ARRAY<INT32>` field can map to Integer[]; its element type, capacity and nullability still apply. For Bean or Map properties stored as JSON, see [JSON Field Mapping](../../../guides/core/mapping/json-field.md). JSON text stored in VARCHAR does not acquire native JSON filtering.
