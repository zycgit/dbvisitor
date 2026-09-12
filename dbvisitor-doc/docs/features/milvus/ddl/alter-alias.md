---
id: alter-alias
sidebar_position: 14
title: ALTER ALIAS
---

:::info[说明]
对应 SDK 方法：`alterAlias`。
:::

```text
ALTER ALIAS alias_name FOR TABLE table_name;
```

别名改指向由 Milvus 执行，不复制实体。改指向后新执行的查询访问新集合；已经打开的 ResultSet 不因此重新执行，也不提供跨查询事务保证。

对象名使用 SQL 标识符，不能用值参数代替。操作成功返回更新计数 0。
