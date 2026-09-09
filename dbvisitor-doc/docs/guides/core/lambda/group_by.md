---
id: group_by
sidebar_position: 7
title: 分组
description: 通过如下方式可以使用条件构造器构建分组查询。分组不会自动把投影改成分组列，应显式选择分组列或聚合表达式。
---

# 分组

通过如下方式可以使用条件构造器构建分组查询。分组不会自动把投影改成分组列，应显式选择分组列或聚合表达式。

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .ge(User::getId, 100)
               .le(User::getId, 500)
               .groupBy(User::getStatus)
               .selectAdd(User::getStatus)
               .queryForList();
// result 结果集 User 对象中将只有 status 会有值
```

```java title='对应的 SQL'
select status from users where id >= 100 and id <= 500 group by status;
```

## 使用聚合函数

```java title='分组求和' {14,15}
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

```java title='对应的 SQL'
select status, count(*) as cnt
from users
where id >= 100 and id <= 500
group by status;
```
