---
id: array
sidebar_position: 4
title: ARRAY
---

Declare an element type and maximum capacity, for example `ARRAY<INT64>(16)` or `ARRAY<VARCHAR(30)>(8)`. Capacity is 1–4096 elements; VARCHAR length is measured in UTF-8 bytes.

```sql
CREATE TABLE array_docs (
    id INT64 PRIMARY KEY, v FLOAT_VECTOR(2),
    numbers ARRAY<INT64>(16), tags ARRAY<VARCHAR(30)>(8) NULL
);
INSERT INTO array_docs (id,v,numbers,tags) VALUES (?, ?, ?, ?);
```

Elements may be BOOL, INT8, INT16, INT32, INT64, FLOAT, DOUBLE or VARCHAR(n). Nested arrays, NULL elements and DEFAULT are not supported. A nullable array field may be NULL; an empty array is a distinct value.

