---
id: alter-index
sidebar_position: 9
title: ALTER INDEX
---

:::info[Note]
SDK methods: `alterIndexProperties`, `dropIndexProperties`.
:::

## Alter Index Properties

```text
ALTER INDEX index_name ON [TABLE] table_name
    SET PROPERTIES (property_name = property_value, ...);
ALTER INDEX index_name ON [TABLE] table_name
    DROP PROPERTIES (property_name, ...);
```

SET property values can be bound with `?`; property names cannot. DROP PROPERTIES removes only the listed properties, not the index. Milvus determines which properties can be changed and the required load state. This is not a general mechanism for changing the index algorithm or rebuilding an index. The driver calls the native property-alteration API and returns update count `0` on success.

```sql
ALTER INDEX title_idx ON books SET PROPERTIES ('mmap.enabled'=true);
ALTER INDEX title_idx ON books DROP PROPERTIES ('mmap.enabled');
```
