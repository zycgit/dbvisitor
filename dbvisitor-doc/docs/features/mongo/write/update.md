---
id: update
sidebar_position: 2
title: 更新与替换
---

:::info[说明]
对应 SDK 方法：`updateOne`、`updateMany`、`replaceOne`。
:::

```text
test.user_info.updateOne({uid: ?}, {$inc: {loginCount: ?}})
test.user_info.updateMany({status: ?}, {$set: {enabled: false}})
test.user_info.replaceOne({uid: ?}, {uid: ?, name: ?})
```

`updateOne` 修改一条匹配文档，`updateMany` 修改全部匹配文档；`$set`、`$inc` 由数据库执行。`replaceOne` 替换整篇文档，未提供的普通字段会被移除。

:::info[更新计数]
返回实际修改数，不是匹配数。将字段设置为已有值时可返回 0；使用 upsert 新增文档也不能用该计数推断插入数。
:::

旧式 `update(filter, update, options)` 默认更新一条；使用 `multi: true` 更新全部匹配文档。
