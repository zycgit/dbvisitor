---
id: common
sidebar_position: 2
hide_table_of_contents: true
title: 通用 Mapper
description: 通用 BaseMapper 接口提供了一组常见的数据库操作方法利用对象映射信息完成对数据库的基础 CRUD 操作。
---

# 常见操作
通用 BaseMapper 接口提供了一组常见的数据库操作方法，利用对象映射信息完成对数据库的基础 CRUD 操作。

:::tip[提示]
Session 的获取方式取决于项目架构，详见 **[框架整合](../../yourproject/buildtools#integration)**。
:::

## 增、删、改

```java title='新增'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.insert(user);
```

```java title='批量新增'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.insert(Arrays.asList(user1, user2));
```

```java title='局部更新'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.update(user); // 根据主键，更新 user 对象中不为空的数据
```

```java title='替换更新'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.replace(user); // 根据主键，替换 user 表整行数据
```

```java title='插入或替换更新'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.upsert(user); // 根据 user 对象中的主键，在数据库中不存在时会使用 insert 否则会进行更新
```

```java title='删除对象'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.delete(user); // 根据 user 对象中的主键删除
```

```java title='根据 ID 删除'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.deleteById(userId);
```

```java title='根据 ID 删除一批对象'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.deleteByIds(Arrays.asList(userId1, userId2));
```

## 查询 {#query}

```java title='根据 ID 查询对象'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
User user = mapper.selectById(userId);
```

```java title='根据 ID 查询一组对象'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> users = mapper.selectByIds(Arrays.asList(userId1, userId2));
```

```java title='根据样本查询列表'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> users = mapper.listBySample(sampleUser);
```

## 分页 {#page}

- PageResult 分页结果中还会包含 **原始分页信息**、**总记录数**、**总页数**。

```java title='分页查询'
User sample = ...
Page pageInfo = PageObject.of(0, 20);   // 第 0 页每页 20条，页码从 0 开始
//or pageInfo = PageObject.of(1, 20, 1);// 第 1 页每页 20条，页码从 1 开始
BaseMapper<User> mapper = session.createBaseMapper(User.class);
PageResult<User> result = mapper.pageBySample(sample, pageInfo);
```

```java title='分页查询并指定排序策略'
User sample = ...
Page pageInfo = PageObject.of(0, 20);   // 第 0 页每页 20条，页码从 0 开始
//or pageInfo = PageObject.of(1, 20, 1);// 第 1 页每页 20条，页码从 1 开始
Map<String, OrderType> orderBy = new HashMap<>();
orderBy.put("id", OrderType.DESC);
orderBy.put("name", OrderType.ASC);

BaseMapper<User> mapper = session.createBaseMapper(User.class);
PageResult<User> result = mapper.pageBySample(sample, pageInfo, orderBy);
```

```java title='在排序时指定 Null 值排序策略'
User sample = ...
Page pageInfo = PageObject.of(0, 20);   // 第 0 页每页 20条，页码从 0 开始
//or pageInfo = PageObject.of(1, 20, 1);// 第 1 页每页 20条，页码从 1 开始

Map<String, OrderType> orderBy = new HashMap<>();
orderBy.put("id", OrderType.DESC);
orderBy.put("name", OrderType.ASC);

Map<String, OrderNullsStrategy> nulls = new HashMap<>();
nulls.put("name", OrderNullsStrategy.FIRST);

BaseMapper<User> mapper = session.createBaseMapper(User.class);
PageResult<User> result = mapper.pageBySample(sample, pageInfo, orderBy, nulls);
```
