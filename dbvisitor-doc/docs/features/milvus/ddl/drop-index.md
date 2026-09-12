---
id: drop-index
sidebar_position: 10
title: DROP INDEX
---

:::info[说明]
对应 SDK 方法：`dropIndex`。
:::

## 删除索引

```text
DROP INDEX index_name ON [TABLE] table_name;
```

删除指定索引，不删除集合中的实体。此语句不支持 `IF EXISTS`；不存在、无权限或服务端状态不允许删除时，请求失败会向 JDBC 调用方报告。成功返回更新计数 `0`。

对象名使用 SQL 标识符，不能用值参数代替。操作成功返回更新计数 0。
