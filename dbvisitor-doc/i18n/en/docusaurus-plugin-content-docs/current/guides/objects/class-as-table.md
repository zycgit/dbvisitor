---
id: class_as_table
sidebar_position: 1
title: 类名即表名
description: 使用 dbVisitor ORM 映射数据库表，将类名作为表名。
---

# 类名即表名

```sql title='有如下表'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

默认情况下 dbVisitor 采用名称映射策略，即：`test_user` 类映射 `test_user` 表，`create_time` 属性映射 `create_time` 列。由此 pojo 如下：

```java
public class test_user {
    private BigInteger id;
    private String     name;
    private Integer    age;
    private Date       create_time;

    // getters and setters omitted
}
```