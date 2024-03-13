---
id: map_schema
sidebar_position: 7
title: 跨Schema映射
description: 使用 dbVisitor ORM 进行跨Schema映射。
---

# 跨Schema映射

```sql title='有如下表'
create table `db_1`.`test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);

create table `db_2`.`test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
)
```

如要同时映射它们则需要通过 `@Table` 注解的 schema 属性来分别标识它们所属的 schema。

```java {1,6}
@Table(schema = "db_1", table = "test_user")
public class TestUserForMy {
    // fields and getters and setters omitted
}

@Table(schema = "db_2", table = "test_user")
public class TestUserForYou {
    // fields and getters and setters omitted
}
```
