---
id: common
sidebar_position: 2
hide_table_of_contents: true
title: 通用 Mapper
description: 通用 BaseMapper 接口提供了一组常见的数据库操作方法利用对象映射信息完成对数据库的基础 CRUD 操作。
---

# 常见操作
通用 BaseMapper 接口提供了一组常见的数据库操作方法利用对象映射信息完成对数据库的基础 CRUD 操作。

## 增、删、改

```java title='新增'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.insert(user);
```

```java title='批量新增'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.insert(Arrays.asList(user1, user2));
```

```java title='更新'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
int res = mapper.update(user); // 根据 user 对象中的主键值进行更新
```

```java title='插入或更新'
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

## 查询和分页 {#query_and_page}

```java title='根据 ID 查询对象'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
User user = mapper.selectById(userId);
```

```java title='根据 ID 查询一组对象'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> users = mapper.selectById(Arrays.asList(userId1, userId2));
```

```java title='根据样本查询列表'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> users = mapper.listBySample(sampleUser);
```

```java title='根据样本进行分页查询'
User sample = ...
Page pageInfo = PageObject.of(0, 20);   // 第 0 页每页 20条，页码从 0 开始
//or pageInfo = PageObject.of(1, 20, 1);// 第 1 页每页 20条，页码从 1 开始
BaseMapper<User> mapper = session.createBaseMapper(User.class);
PageResult<User> result = mapper.pageBySample(sample, pageInfo);
```

- PageResult 分页结果中还会包含 **原始分页信息**、**总记录数**、**总页数**。
