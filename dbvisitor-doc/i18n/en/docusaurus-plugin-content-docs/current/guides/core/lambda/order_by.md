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

Ordering conditions retain their call order. Repeated fields are not deduplicated.

## Null Ordering {#null-ordering}

`OrderType` controls the direction of non-null values; `OrderNullsStrategy` controls the position of nulls:

- `DEFAULT`: retain the database's default null order.
- `FIRST`: place nulls before non-null values.
- `LAST`: place nulls after non-null values.

`FIRST` and `LAST` do not reverse with the sort direction. For example, descending order with `FIRST` returns nulls first, followed by non-null values from largest to smallest.

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
- NULLS FIRST/LAST support depends on dialect; see **[database support](../../../features/differences/builder)** for null ordering support.
:::
