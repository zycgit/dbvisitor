---
id: order_by
sidebar_position: 8
title: Order By
description: Build ordered queries with LambdaTemplate.
---

# Order By

Build ordered queries with `LambdaTemplate` as follows.

## Basic Ordering

```java title='Ordering basics'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName) // Default ordering (order by name)
               .queryForList();
             //.asc(User::getName);    //Ascending (order by name asc)
             //.desc(User::getName);   //Descending (order by name desc)

// Equivalent SQL
//   select * from users where id >= 100 order by name;
```

## Multiple Columns

```java title='Multiple sort columns'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName)
               .orderBy(User::getAge)
               .queryForList();

// Equivalent SQL
//   select * from users where id >= 100 order by name, age;
```

## Null Ordering

```java title='NULLS FIRST'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName, OrderType.DEFAULT, OrderNullsStrategy.FIRST)
               .queryForList();

// Equivalent SQL(MySQL)
//   select * from users where id >= 100 order by name is null desc, name
```

```java title='NULLS LAST'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .orderBy(User::getName, OrderType.DEFAULT, OrderNullsStrategy.LAST)
               .queryForList();

// Equivalent SQL(MySQL)
//   select * from users where id >= 100 order by name is null asc, name
```

:::info[Tip]
- NULLS FIRST/LAST support depends on dialect; see **[database support](../../../features/support#dialect)** for null ordering support.
:::
