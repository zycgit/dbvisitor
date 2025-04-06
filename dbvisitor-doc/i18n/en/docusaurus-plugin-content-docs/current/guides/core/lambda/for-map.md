---
id: for-map
sidebar_position: 8
title: 使用 Map 结构
description: Map 模式的特点是可以将类型化的 EntityDelete、EntityInsert、EntityUpdate、EntityQuery 转换为对应 Map 操作接口进行操作。
---

Map 模式的特点是可以将类型化的 EntityDelete、EntityInsert、EntityUpdate、EntityQuery 转换为对应 Map 操作接口进行操作。

:::info[提示]
LambdaTemplate 提供的 INSERT、UPDATE、DELETE、QUERY 所有 SQL 操作接口本身对于 Map 结构的数据已有着很好的支持。
处理 Map 结构的数据和查询应当优先选择它们。
:::

```java title='类型化 EntityInsert 转换为 MapInsert'
LambdaTemplate lambda = ...
MapInsert insert = lambda.insert(User.class).asMap();
...
```

```java title='类型化 EntityUpdate 转换为 MapUpdate'
LambdaTemplate lambda = ...
MapUpdate update = lambda.update(User.class).asMap();
...
```

```java title='类型化 EntityDelete 转换为 MapDelete'
LambdaTemplate lambda = ...
MapDelete delete = lambda.delete(User.class).asMap();
...
```

```java title='类型化 EntityQuery 转换为 MapQuery'
LambdaTemplate lambda = ...
MapQuery query = lambda.query(User.class).asMap();
...
```
