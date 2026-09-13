---
id: count
sidebar_position: 2
title: 计数与去重
---

计数调用 SDK `countDocuments`；去重调用 `distinct`。

```text
test.user_info.count({age: {$gte: 18}})
test.user_info.distinct('name', {age: {$gte: 18}})
```

命令使用 `count(...)`，不是 SDK 方法名 `countDocuments(...)`。计数使用查询方法读取，不是 `executeUpdate()`；去重返回多行结果。分页总数的用法见[分页查询](../dbvisitor/pagination.mdx)。
