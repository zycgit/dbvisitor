---
id: alter-index
sidebar_position: 9
title: ALTER INDEX
---

:::info[说明]
对应 SDK 方法：`alterIndexProperties`、`dropIndexProperties`。
:::

## 修改索引属性

```text
ALTER INDEX index_name ON [TABLE] table_name
    SET PROPERTIES (property_name = property_value, ...);
ALTER INDEX index_name ON [TABLE] table_name
    DROP PROPERTIES (property_name, ...);
```

SET 的属性值可通过 `?` 绑定，属性名不能绑定。DROP PROPERTIES 只移除列出的属性，不删除索引。允许变更的属性及加载状态要求由 Milvus 服务端决定；此命令不是修改索引算法或重新建索引的通用方式。驱动调用原生属性变更接口，成功返回更新计数 `0`。

```sql
ALTER INDEX title_idx ON books SET PROPERTIES ('mmap.enabled'=true);
ALTER INDEX title_idx ON books DROP PROPERTIES ('mmap.enabled');
```
