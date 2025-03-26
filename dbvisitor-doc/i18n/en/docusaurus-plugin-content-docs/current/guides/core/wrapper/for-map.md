---
id: for-map
sidebar_position: 8
title: 使用 Map 结构
description: Map 模式的特点是可以将类型化的 DeleteWrapper、InsertWrapper、QueryWrapper、UpdateWrapper 转换为对应 MapWrapper 进行操作。
---

Map 模式的特点是可以将类型化的 DeleteWrapper、InsertWrapper、QueryWrapper、UpdateWrapper 转换为对应 MapWrapper 进行操作。

:::info[提示]
WrapperAdapter 提供的 INSERT、UPDATE、DELETE、QUERY 所有 SQL 操作接口本身对于 Map 结构的数据已有着很好的支持。
处理 Map 结构的数据和查询应当优先选择它们。
:::

```java title='类型化 InsertWrapper 转换为 MapInsertWrapper'
WrapperAdapter adapter = ...
MapInsertWrapper wrapper = newLambda().insertByEntity(User.class).asMap();
...
```

```java title='类型化 UpdateWrapper 转换为 MapUpdateWrapper'
WrapperAdapter adapter = ...
MapUpdateWrapper wrapper = newLambda().updateByEntity(User.class).asMap();
...
```

```java title='类型化 DeleteWrapper 转换为 MapDeleteWrapper'
WrapperAdapter adapter = ...
MapDeleteWrapper wrapper = newLambda().deleteByEntity(User.class).asMap();
...
```

```java title='类型化 QueryWrapper 转换为 MapQueryWrapper'
WrapperAdapter adapter = ...
MapQueryWrapper wrapper = newLambda().queryByEntity(User.class).asMap();
...
```
