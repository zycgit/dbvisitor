---
id: array
sidebar_position: 4
title: ARRAY
---

必须指定元素类型和最大容量，例如 `ARRAY<INT64>(16)`、`ARRAY<VARCHAR(30)>(8)`。容量为 1–4096 个元素，VARCHAR 长度以 UTF-8 字节计。

```sql
CREATE TABLE array_docs (
    id INT64 PRIMARY KEY, v FLOAT_VECTOR(2),
    numbers ARRAY<INT64>(16), tags ARRAY<VARCHAR(30)>(8) NULL
);
INSERT INTO array_docs (id,v,numbers,tags) VALUES (?, ?, ?, ?);
```

元素类型可为 BOOL、INT8、INT16、INT32、INT64、FLOAT、DOUBLE 或 VARCHAR(n)。不支持嵌套数组、NULL 元素或 DEFAULT。声明 NULL 的数组字段可保存 NULL；空数组与 NULL 是两个不同的值。

