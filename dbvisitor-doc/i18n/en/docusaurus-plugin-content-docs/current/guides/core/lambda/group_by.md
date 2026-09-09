---
id: group_by
sidebar_position: 7
title: Group By
description: Build grouped queries with the where builder.
---

# Group By

Use the Fluent API to construct grouped queries as follows.

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .le(User::getId, 500)
               .groupBy(User::getStatus)
               .selectAdd(User::getStatus)
               .queryForList();
// selectAdd explicitly selects status; groupBy does not change projection
```

```java title='Equivalent SQL'
select status from users where id >= 100 and id <= 500 group by status;
```

## Aggregate functions

```java title='Group and sum' {14,15}
class UserGroupBy{
    private String  status;
    private Long cnt;
    ...
}

LambdaTemplate lambda = ...

List<UserGroupBy> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .le(User::getId, 500)
               .groupBy(User::getStatus)
               .selectAdd(User::getStatus)
               .applySelectAdd("count(*) as cnt")
               .queryForList(UserGroupBy.class);
```

```java title='Equivalent SQL'
select status, count(*) as cnt
from users
where id >= 100 and id <= 500
group by status;
```
