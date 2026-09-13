---
id: delete
sidebar_position: 3
title: 删除文档
---

:::info[说明]
对应 SDK 方法：`deleteOne`、`deleteMany`。
:::

```text
test.user_info.deleteOne({uid: ?})
test.user_info.deleteMany({status: ?})
```

`deleteOne` 删除一条匹配文档，`deleteMany` 删除全部匹配文档。返回删除数量；空过滤条件会匹配集合中的所有文档。
