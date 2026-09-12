---
id: binding
sidebar_position: 5
title: JSON, ARRAY and Vector Binding
---

## Type binding and schema

For Java input types and a binding example, see [Parameter Binding](../../../drivers/milvus/parameters.mdx#typed-values). Binary accepts byte[], ByteBuffer or byte Lists. FP16/BF16 numeric inputs use official Float16Utils; packed byte[]/ByteBuffer uses little endian. Sparse accepts nonempty Map&lt;Number,Number>, integer indices in [0,4294967295), and finite weights. ByteBuffer uses remaining without changing position. The target field schema determines the meaning of the same byte[]: FloatVector converts signed values element by element, Binary/FP16/BF16 interpret packed encodings, and Int8Vector uses one signed byte per dimension.

```sql
CREATE TABLE types_demo (
    id INT64 PRIMARY KEY AUTO_ID,
    tags ARRAY<VARCHAR(30)>(8) NULL,
    flags ARRAY<BOOL>(8),
    bits BINARY_VECTOR(16),
    half FLOAT16_VECTOR(2),
    brain BFLOAT16_VECTOR(2),
    sparse SPARSE_FLOAT_VECTOR
);
INSERT INTO types_demo (tags,flags,bits,half,brain,sparse) VALUES (?, ?, ?, ?, ?, ?);
SELECT id,bits FROM types_demo ORDER BY bits ~= ? LIMIT 10;
SELECT id,score FROM types_demo WHERE bits <%> ? < 0.5 LIMIT 10;
SELECT id,half FROM types_demo ORDER BY half <=> ? LIMIT 10;
SELECT id,brain FROM types_demo ORDER BY brain <-> ? LIMIT 10;
SELECT id,sparse FROM types_demo ORDER BY sparse <#> ? LIMIT 10;
SELECT id,tags FROM types_demo WHERE tags IS NOT NULL LIMIT 10;
```

ARRAY capacity, element type and VARCHAR byte limit reach the SDK schema. getArray exposes its element JDBC type; arrays may be NULL or empty but elements cannot be NULL. isNullable follows schema. SHOW TABLE appends NULLABLE, ELEMENT_TYPE, MAX_CAPACITY and MAX_LENGTH, preserving previous column positions. SHOW CREATE preserves definitions. Nullable vectors require Milvus 2.6.18+ and do not support IS NULL predicates; see [release notes](https://github.com/milvus-io/milvus/releases/tag/v2.6.18) and [nullable documentation](https://milvus.io/docs/v2.6.x/nullable-and-default.md).
