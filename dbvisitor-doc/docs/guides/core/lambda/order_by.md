---
id: order_by
sidebar_position: 8
title: 排序
description: 使用 LambdaTemplate 通过如下方式可以构建排序查询。
---

# 排序

使用 LambdaTemplate 通过如下方式可以构建排序查询。

## 基础排序

```java title='排序用法'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName) // 默认排序（order by name）
               .queryForList();
             //.asc(User::getName);    //升序   （order by name asc）
             //.desc(User::getName);   //降序   （order by name desc）

// 对应的 SQL
//   select * from users where id >= 100 order by name;
```

## 多列排序

```java title='多个排序列'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName)
               .orderBy(User::getAge)
               .queryForList();

// 对应的 SQL
//   select * from users where id >= 100 order by name, age;
```

排序条件按调用顺序保留，不对重复字段去重。

## 空值排序 {#null-ordering}

`OrderType` 控制非空值的升降序，`OrderNullsStrategy` 控制空值的位置：

- `DEFAULT`：保留数据库默认的空值顺序。
- `FIRST`：空值排在非空值之前。
- `LAST`：空值排在非空值之后。

`FIRST`、`LAST` 不随升降序反转。例如降序配合 `FIRST` 时，先返回空值，再将非空值从大到小排列。

```java title='NULL 最前'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName, OrderType.DEFAULT, OrderNullsStrategy.FIRST)
               .queryForList();

// 对应的 SQL(MySQL)
//   select * from users where id >= 100 order by name is null desc, name
```

```java title='NULL 最后'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName, OrderType.DEFAULT, OrderNullsStrategy.LAST)
               .queryForList();

// 对应的 SQL(MySQL)
//   select * from users where id >= 100 order by name is null asc, name
```

:::info[提示]
- NULL 最前/最后，所使用的 OrderNullsStrategy 策略是否支持请参考 **[数据库支持性](../../../features/differences/builder)** 中的空值排序策略。
:::
