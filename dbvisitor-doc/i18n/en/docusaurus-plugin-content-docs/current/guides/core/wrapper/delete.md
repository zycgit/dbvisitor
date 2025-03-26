---
id: delete
sidebar_position: 2
hide_table_of_contents: true
title: 删除操作
description: 在 dbVisitor 中使用 WrapperAdapter 删除数据。
---

在 dbVisitor 中使用 WrapperAdapter 删除数据如下所示：

:::info[提示]
删除操作中涉及查询条件相关内容请参考 **[条件构造器](./where-builder)**。
:::

```java
WrapperAdapter adapter = ...
int result = adapter.deleteByEntity(User.class)
                    .eq(User::getId, 1) // 匹配条件
                    .doDelete();
// 返回 result 为受影响的行数
```

## 空条件

不指定任何条件的删除是一项十分危险的操作，默认情况下 dbVisitor 会禁止此类操作。

若想不指定条件删除整张表的数据需要调用 allowEmptyWhere 方法以打开此次操作。

```java
WrapperAdapter adapter = ...
int result = adapter.deleteByEntity(User.class)
                    .allowEmptyWhere() // 允许 doDelete 时没有任何条件
                    .doDelete();
// 返回 result 为受影响的行数
```
