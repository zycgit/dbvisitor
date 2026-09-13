---
id: insert
sidebar_position: 1
title: 插入文档
---

:::info[说明]
对应 SDK 方法：`insertOne`、`insertMany`。
:::

```text
test.user_info.insertOne({uid: ?, name: ?})
test.user_info.insertMany([{uid: '1001', name: 'mali'}, {uid: '1002', name: 'alice'}])
```

`insert` 接受单个文档或文档数组。未提供 `_id` 时生成 ID；回填用法见[主键生成](../dbvisitor/generated-keys.mdx)。`insertMany` 是一次原生多文档写入，不是 JDBC batch。
