---
id: freedom
sidebar_position: 9
title: 自由模式
description: 自由模式中，允许在没有任何实体定义的前提下直接处理表的 INSERT、UPDATE、DELETE、QUERY。
---

# 自由模式

自由模式中，允许在没有任何实体定义的前提下直接处理表的 INSERT、UPDATE、DELETE、QUERY。

:::info[提示]
在常规模式中 LambdaTemplate 会识别列在映射中是否存在，并且在一些操作中会自动过滤不存在的列。在自由模式中没有这些特性。
:::

```java title='使用自由模式插入数据'
Map<String, Object> u = ...

LambdaTemplate lambda = ...
MapInsert insert = lambda.insertFreedom("users");
insert.applyMap(u)
      .executeSumResult();
...
```

```java title='使用自由模式更新数据'
LambdaTemplate lambda = ...
MapUpdate update = lambda.updateFreedom("users");

Map<String, Object> u = ...
int result = update.eq("loginName", "admin")
                   .eq("password", "pass")
                   .updateRow(u)
                   .doUpdate();
...
```

```java title='使用自由模式删除数据'
LambdaTemplate lambda = ...
MapDelete delete = lambda.deleteFreedom("users");

int result = delete.eq("loginName", "admin")
                   .eq("password", "pass")
                   .doDelete();
...
```

```java title='使用自由模式查询数据'
LambdaTemplate lambda = ...
MapQuery query = lambda.queryFreedom("users");

List<Map<String, Object>> result = null;
result = query.eq("loginName", "admin")
              .eq("password", "pass")
              .queryForList();
...
```

:::info[小技巧]
在查询中可以参考 [查询操作](./query) 中提供的方法来处理查询结果。
:::
