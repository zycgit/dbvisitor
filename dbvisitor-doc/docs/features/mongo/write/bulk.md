---
id: bulk
sidebar_position: 4
title: 批量命令
---

:::info[说明]
对应 SDK 方法：`bulkWrite`。
:::

```text
test.user_info.bulkWrite([
  {insertOne: {document: {uid: '1001', name: 'mali'}}},
  {updateOne: {filter: {uid: '1002'}, update: {$set: {name: 'alice'}}}},
  {deleteOne: {filter: {uid: '1003'}}}
])
```

一条命令组合多项写入操作。返回插入、修改、删除数之和，不包含独立的 upsert 数量。它不是 JDBC batch，也不保证所有文档一起成功或回滚。
