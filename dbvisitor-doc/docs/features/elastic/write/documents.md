---
id: documents
sidebar_position: 1
title: 文档写入与删除
---

:::info[说明]
对应 REST 接口：`_doc`、`_create`、`_update`。
:::

## 插入与覆盖

```text
POST /user_info/_doc {"name":"mali"}
PUT /user_info/_doc/1001 {"name":"mali"}
POST /user_info/_create/1002 {"name":"alice"}
```

POST 不带 ID 时生成 `_id`；PUT 指定 ID 时覆盖已有文档；`_create` 在 ID 已存在时失败。主键回填见[主键生成](../dbvisitor/generated-keys.mdx)。

## 修改部分字段

```text
POST /user_info/_update/1001?refresh=true {"doc":{"name":"new name"}}
```

只修改 `doc` 中的字段。需要覆盖整篇文档时使用 PUT，而不是 `_update`。

## 删除

```text
DELETE /user_info/_doc/1001?refresh=true
```

更新计数与写后可见性见[写入结果](results.md)。
