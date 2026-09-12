---
id: vectors
sidebar_position: 4
title: Vector Values
---

## Vector Field Types

| SQL type | Description |
| --- | --- |
| `FLOAT_VECTOR(dim)` | Main read/write/search paths support numeric Lists and primitive arrays. |
| `BINARY_VECTOR(dim)` | Packed-bit vectors with read/write, KNN and range search. |
| `FLOAT16_VECTOR(dim)` | Half-precision vectors with read/write, KNN and range search. |
| `BFLOAT16_VECTOR(dim)` | BFloat16 vectors with read/write, KNN and range search. |
| `SPARSE_FLOAT_VECTOR` | Sparse float vectors have no dimension; legacy `(dim)` is accepted only for parsing compatibility. |
| `INT8_VECTOR(dim)` | One signed integer from -128 to 127 per dimension, stored as bytes. Supports KNN/range and Hybrid search with HNSW indexes; floating-point vectors are not automatically quantized. |

## Vector Data Format Support

In statements involving vector operations such as `INSERT`, `SEARCH` (SELECT ... ORDER BY vector), `DELETE`, multiple vector expression forms are supported:

1. **SQL Array Literal**:
   - `[0.1, 0.2]`
2. **JDBC Parameter Binding**:
   - `?` (PreparedStatement)
   - FloatVector accepts numeric lists (`List<? extends Number>`), including `List<Byte>`, `List<Short>`, `List<Integer>`, `List<Long>`, `List<Float>`, and `List<Double>`.
   - Six one-dimensional numeric primitive arrays are supported: `byte[]`, `short[]`, `int[]`, `long[]`, `float[]`, and `double[]`. Bind with `PreparedStatement.setObject(index, vector)`.
   - Applies to FloatVector INSERT/UPSERT and KNN/range vector conditions in SELECT, UPDATE, and DELETE (`vector_range` or distance comparisons). UPDATE SET values are sent through native partial update.
   - Vector queries and INSERT/UPSERT convert elements to Float; large integers and doubles may lose precision. A `byte[]` is converted element by element as signed numbers, not interpreted as BinaryVector bits.
   - `boolean[]`, `char[]`, boxed arrays such as `Float[]`, and multidimensional Java arrays are not supported as a single FloatVector. Dimensions must match the field schema.
3. **Single Query Vector**:
   - `ORDER BY vector_col <-> ?` accepts one query vector, such as `[1, 1]`, with or without LIMIT.
   - Nested vector lists such as `[[1, 1], [99, 99]]` (including `[[1, 1]]`) cause a parameter error. The same restriction applies to UPDATE/DELETE distance ordering and vector range conditions.
   - Milvus SDK batch search with multiple query vectors is not exposed through this ORDER BY syntax and is distinct from JDBC `addBatch`/`executeBatch`. SELECT continues to return one result set with the requested output fields.
