---
id: map_ignore
sidebar_position: 4
title: 忽略某些列
description: 使用 dbVisitor ORM 映射数据库表，某些列会被忽略。
---

# 忽略某些列

```sql title='有如下表'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

在默认情况下(`autoMapping = true`)，可以通过 `@Ignore` 注解忽略某个属性到列的映射。

```java {7}
@Table(mapUnderscoreToCamelCase = true)
public class TestUser {
    private Integer id;
    private String  name;
    private Integer age;
    private Date    createTime;
    @Ignore
    private Date    modifyTime; // 忽略到列的映射

    // getters and setters omitted
}
```