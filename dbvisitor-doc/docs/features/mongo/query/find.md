---
id: find
sidebar_position: 1
title: 文档查询
---

:::info[说明]
对应 SDK 方法：`find`。`findOne` 使用单条读取方式。
:::

```text
test.user_info.find({age: {$gte: ?}}, {name: 1, age: 1}).sort({_id: 1}).skip(0).limit(10)
test.user_info.findOne({uid: ?})
```

第一个参数是过滤条件，第二个是投影。链式方法支持 `sort`、`skip`、`limit`、`hint`；不支持任意 mongosh 链式调用。

## 组合条件

```text
test.user_info.find({$or: [{name: ?}, {uid: ?}]})
test.user_info.find({name: {$regex: ?}})
```

`$regex` 接收正则表达式，不是 SQL LIKE 模式。按普通文本匹配时，应先处理正则元字符。

结果默认展开文档字段，也可读取 `_JSON`。实体和标量的读取方式见[结果读取](../dbvisitor/results.mdx)。
