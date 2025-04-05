---
id: orderby
sidebar_position: 7
hide_table_of_contents: true
title: 排序
description: 使用 WrapperAdapter 通过如下方式可以构建排序查询。
---

使用 WrapperAdapter 通过如下方式可以构建排序查询。

```java title='排序用法'
WrapperAdapter adapter = ...

List<User> result = null;
result = adapter.queryByEntity(User.class)
                .ge(User::getId, 100)
                .orderBy(User::getName);//默认排序（order by name）
              //.asc(User::getName);    //生序   （order by name asc）
              //.desc(User::getName);   //降序   （order by name desc）

// 对应的 SQL
//   select * from users where id >= 100 order by name;
```

```java title='多个排序列'
WrapperAdapter adapter = ...

List<User> result = null;
result = adapter.queryByEntity(User.class)
                .ge(User::getId, 100)
                .orderBy(User::getName)
                .orderBy(User::getAge);

// 对应的 SQL
//   select * from users where id >= 100 order by name, age;
```

```java title='NULL 最前'
WrapperAdapter adapter = ...

List<User> result = null;
result = adapter.queryByEntity(User.class)
                .ge(User::getId, 100)
                .orderBy(User::getName, OrderType.DEFAULT, OrderNullsStrategy.FIRST);

// 对应的 SQL(MySQL)
//   select * from users where id >= 100 order by name is null desc, name
```

```java title='NULL 最后'
WrapperAdapter adapter = ...

List<User> result = null;
result = adapter.queryByEntity(User.class)
                .ge(User::getId, 100)
                .orderBy(User::getName, OrderType.DEFAULT, OrderNullsStrategy.LAST);

// 对应的 SQL(MySQL)
//   select * from users where id >= 100 order by name is null asc, name
```

:::info[提示]
- NULL 最前/最后，所使用的 OrderNullsStrategy 策略是否支持请参考 **[数据库支持性](../../yourproject/support)** 中的空值排序策略。
:::
