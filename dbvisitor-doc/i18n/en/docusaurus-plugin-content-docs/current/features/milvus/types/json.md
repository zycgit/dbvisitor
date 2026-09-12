---
id: json
sidebar_position: 3
title: JSON
---

Declare a JSON field with `JSON`, without a length or dimension. INSERT and UPSERT encode the bound value according to this field type. JSON fields do not accept DEFAULT.

```sql
CREATE TABLE json_docs (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2), payload JSON NULL);
INSERT INTO json_docs (id,v,payload) VALUES (?, ?, ?);
```

Bind a Map, List, JsonElement or JSON string as the field value. Supported filter expressions are determined by Milvus; a JSON field does not imply that arbitrary SQL JSON functions are available.

