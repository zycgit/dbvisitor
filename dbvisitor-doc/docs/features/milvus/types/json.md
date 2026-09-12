---
id: json
sidebar_position: 3
title: JSON
---

字段类型写作 `JSON`，不指定长度或维度。INSERT、UPSERT 按字段类型编码参数；JSON 字段不支持 DEFAULT。

```sql
CREATE TABLE json_docs (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2), payload JSON NULL);
INSERT INTO json_docs (id,v,payload) VALUES (?, ?, ?);
```

字段值可绑定 Map、List、JsonElement 或 JSON 字符串。过滤表达式以 Milvus 支持范围为准，声明 JSON 字段不意味着可以使用任意 SQL JSON 函数。

