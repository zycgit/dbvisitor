---
id: wrapper
sidebar_position: 3
hide_table_of_contents: true
title: 调用构造器 API
description: 通过 BaseMapper 接口无参的 insert、update、delete、query 可以无需数据准备直接使用 构造器 API 进行数据库操作。
---

# 调用构造器 API

通过 BaseMapper 接口无参的 insert、update、delete、query 可以无需数据准备直接使用 [构造器 API](../wrapper/about#principle) 进行数据库操作。

```java title='示例：新增'
User user = ...

BaseMapper<User> mapper = session.createBaseMapper(User.class);
mapper.insert().applyEntity(user);
               .executeSumResult();
```

```java title='示例：修改'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
mapper.update().eq(User::getId, 1)              // 匹配条件
               .updateTo(User::getName, "Mary") // 更新字段，使用 Lambda
               .updateTo(User::getStatus, 2)    // 可通过链式调用更新多个字段
               .doUpdate();
```

```java title='示例：删除'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
mapper.delete().eq(User::getId, 1) // 匹配条件
               .doDelete();
```

```java title='示例：查询'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> result = null;
result = mapper.query().le(User::getId, 100)   // 匹配 ID 小于等于 100
                       .queryForList();        // 将结果集映射实体类型
```

:::info[有关 构造器 API 的详细用法，请参阅：]
- 详细请参考 [WrapperAdapter 类使用指引](../wrapper/about#guide)
:::
